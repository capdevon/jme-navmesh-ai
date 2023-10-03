package com.jme3.recast4j.geom;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.recast4j.recast.CompactHeightfield;
import org.recast4j.recast.Context;
import org.recast4j.recast.ContourSet;
import org.recast4j.recast.ConvexVolume;
import org.recast4j.recast.Heightfield;
import org.recast4j.recast.HeightfieldLayerSet;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.Recast;
import org.recast4j.recast.RecastArea;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilder.RecastBuilderProgressListener;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastConfig;
import org.recast4j.recast.RecastConstants;
import org.recast4j.recast.RecastConstants.PartitionType;
import org.recast4j.recast.RecastContour;
import org.recast4j.recast.RecastFilter;
import org.recast4j.recast.RecastLayers;
import org.recast4j.recast.RecastMesh;
import org.recast4j.recast.RecastMeshDetail;
import org.recast4j.recast.RecastRegion;
import org.recast4j.recast.geom.InputGeomProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the Recast4j RecastBuilder class to allow for Area Type flag setting.
 * Works with any InputGeomProvider but is designed for use with the
 * JmeInputGeomProvider which has the ability to store the geometry lengths and
 * AreaModifications. If the provider has the AreaModifications, it will set the
 * Areas Type based off the geometry length and AreaModification. Otherwise, it
 * will just set the AreaType to what is supplied by the RecastConfig object
 * which is the current behavior of the Recast4j implementation.
 * 
 * See the {@link JmeRecastVoxelization}.buildSolidHeightfield method for
 * details.
 * 
 * @author Robert
 * @author capdevon
 */
public class JmeRecastBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(JmeRecastBuilder.class.getName());
    
    private final RecastBuilderProgressListener progressListener;
    
    public JmeRecastBuilder() {
        this.progressListener = null;
    }
    
    /**
     * Sets the progress listener for this job. Reports back the completed tile
     * and number of tiles for the job. Setting timers on the callback allows 
     * for accurate determination of build times.
     * 
     * @param progressListener The listener to set for the job.
     */
    public JmeRecastBuilder(RecastBuilderProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    /**
     * Builds the polymesh and detailmesh by creating tiles.
     * 
     * @param geom The geometry to be used for constructing the meshes.
     * @param cfg The configuration parameters to be used for constructing the meshes.
     * @param threads The number of threads to use for this build job.
     * @return The build results.
     */
    public RecastBuilderResult[][] buildTiles(JmeInputGeomProvider geom, RecastConfig cfg, int threads) {
        float[] bmin = geom.getMeshBoundsMin();
        float[] bmax = geom.getMeshBoundsMax();
        int[] twh = Recast.calcTileCount(bmin, bmax, cfg.cs, cfg.tileSize);
        int tw = twh[0];
        int th = twh[1];
        RecastBuilderResult[][] result = null;
        if (threads == 1) {
            result = buildSingleThread(geom, cfg, bmin, bmax, tw, th);
        } else {
            result = buildMultiThread(geom, cfg, bmin, bmax, tw, th, threads);
        }
        return result;
    }
    
    private RecastBuilderResult[][] buildSingleThread(JmeInputGeomProvider geom, RecastConfig cfg, float[] bmin, float[] bmax, int tw, int th) {
        RecastBuilderResult[][] result = new RecastBuilderResult[tw][th];
        AtomicInteger counter = new AtomicInteger();
        for (int x = 0; x < tw; ++x) {
            for (int y = 0; y < th; ++y) {
                result[x][y] = buildTile(geom, cfg, bmin, bmax, x, y, counter, tw * th);
            }
        }
        return result;
    }

    private RecastBuilderResult[][] buildMultiThread(JmeInputGeomProvider geom, RecastConfig cfg, float[] bmin, float[] bmax, int tw, int th, int threads) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        RecastBuilderResult[][] result = new RecastBuilderResult[tw][th];
        AtomicInteger counter = new AtomicInteger();
        for (int x = 0; x < tw; ++x) {
            for (int y = 0; y < th; ++y) {
                final int tx = x;
                final int ty = y;
                executor.submit((Runnable) () -> {
                    result[tx][ty] = buildTile(geom, cfg, bmin, bmax, tx, ty, counter, tw * th);
                });
            }
        }
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Pool did not terminate {}", executor);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
            executor.shutdownNow();
        }
        
        logger.info("Pool shutdown {}", executor);
        return result;
    }

    private RecastBuilderResult buildTile(JmeInputGeomProvider geom, RecastConfig cfg, float[] bmin, float[] bmax, int tx, int ty, AtomicInteger counter, int total) {
        RecastBuilderResult result = build(geom, new RecastBuilderConfig(cfg, bmin, bmax, tx, ty, true));
        if (progressListener != null) {
            progressListener.onProgress(counter.incrementAndGet(), total);
        }
        return result;
    }
    
    public RecastBuilderResult build(JmeInputGeomProvider geom, RecastBuilderConfig builderCfg) {

        RecastConfig cfg = builderCfg.cfg;
        Context ctx = new Context();
        //
        // Step 1. Rasterize input polygon soup.
        //
        Heightfield solid = JmeRecastVoxelization.buildSolidHeightfield(geom, builderCfg, ctx);
        return build(builderCfg.borderSize, builderCfg.buildMeshDetail, geom, cfg, solid, ctx);
    }
    
    public RecastBuilderResult build(int borderSize, boolean buildMeshDetail, InputGeomProvider geom, RecastConfig cfg, Heightfield solid, Context ctx) {
    	
    	filterHeightfield(solid, cfg, ctx);
    	CompactHeightfield chf = buildCompactHeightfield(geom, cfg, ctx, solid);

        // Partition the heightfield so that we can use simple algorithm later
        // to triangulate the walkable areas.
        // There are 3 partitioning methods, each with some pros and cons:
        // 1) Watershed partitioning
        // - the classic Recast partitioning
        // - creates the nicest tessellation
        // - usually slowest
        // - partitions the heightfield into nice regions without holes or overlaps
        // - the are some corner cases where this method creates produces holes and overlaps
        // - holes may appear when a small obstacles is close to large open area (triangulation can handle this)
        // - overlaps may occur if you have narrow spiral corridors (i.e
        // stairs), this make triangulation to fail
        // * generally the best choice if you precompute the nacmesh, use this
        // if you have large open areas
        // 2) Monotone partioning
        // - fastest
        // - partitions the heightfield into regions without holes and overlaps (guaranteed)
        // - creates long thin polygons, which sometimes causes paths with detours
        // * use this if you want fast navmesh generation
        // 3) Layer partitoining
        // - quite fast
        // - partitions the heighfield into non-overlapping regions
        // - relies on the triangulation code to cope with holes (thus slower than monotone partitioning)
        // - produces better triangles than monotone partitioning
        // - does not have the corner cases of watershed partitioning
        // - can be slow and create a bit ugly tessellation (still better than monotone)
        // if you have large open areas with small obstacles (not a problem if you use tiles)
        // * good choice to use for tiled navmesh with medium and small sized tiles

        if (cfg.partitionType == PartitionType.WATERSHED) {
            // Prepare for region partitioning, by calculating distance field
            // along the walkable surface.
            RecastRegion.buildDistanceField(ctx, chf);
            // Partition the walkable surface into simple regions without holes.
            RecastRegion.buildRegions(ctx, chf, borderSize, cfg.minRegionArea, cfg.mergeRegionArea);
        } else if (cfg.partitionType == PartitionType.MONOTONE) {
            // Partition the walkable surface into simple regions without holes.
            // Monotone partitioning does not need distancefield.
            RecastRegion.buildRegionsMonotone(ctx, chf, borderSize, cfg.minRegionArea, cfg.mergeRegionArea);
        } else {
            // Partition the walkable surface into simple regions without holes.
            RecastRegion.buildLayerRegions(ctx, chf, borderSize, cfg.minRegionArea);
        }

        // Step 5. Trace and simplify region contours.
        ContourSet cset = RecastContour.buildContours(ctx, chf, cfg.maxSimplificationError, cfg.maxEdgeLen, RecastConstants.RC_CONTOUR_TESS_WALL_EDGES);

        // Step 6. Build polygons mesh from contours.
        PolyMesh pmesh = RecastMesh.buildPolyMesh(ctx, cset, cfg.maxVertsPerPoly);

        // Step 7. Create detail mesh which allows to access approximate height on each polygon.
        PolyMeshDetail dmesh = buildMeshDetail
                ? RecastMeshDetail.buildPolyMeshDetail(ctx, pmesh, chf, cfg.detailSampleDist, cfg.detailSampleMaxError)
                : null;
        return new RecastBuilder().new RecastBuilderResult(solid, chf, cset, pmesh, dmesh);
    }
    
    /*
     * Step 2. Filter walkable surfaces.
     */
    private void filterHeightfield(Heightfield solid, RecastConfig cfg, Context ctx) {

        // Once all geometry is rasterized, we do initial pass of filtering to
        // remove unwanted overhangs caused by the conservative rasterization
        // as well as filter spans where the character cannot possibly stand.
        if (cfg.filterLowHangingObstacles) {
            RecastFilter.filterLowHangingWalkableObstacles(ctx, cfg.walkableClimb, solid);
        }
        if (cfg.filterLedgeSpans) {
            RecastFilter.filterLedgeSpans(ctx, cfg.walkableHeight, cfg.walkableClimb, solid);
        }
        if (cfg.filterWalkableLowHeightSpans) {
            RecastFilter.filterWalkableLowHeightSpans(ctx, cfg.walkableHeight, solid);
        }
    }

    /*
     * Step 3. Partition walkable surface to simple regions.
     */
    private CompactHeightfield buildCompactHeightfield(InputGeomProvider geom, RecastConfig cfg, Context ctx, Heightfield solid) {
        // Compact the heightfield so that it is faster to handle from now on.
        // This will result more cache coherent data as well as the neighbours
        // between walkable cells will be calculated.
        CompactHeightfield chf = Recast.buildCompactHeightfield(ctx, cfg.walkableHeight, cfg.walkableClimb, solid);

        // Erode the walkable area by agent radius.
        RecastArea.erodeWalkableArea(ctx, cfg.walkableRadius, chf);
        // (Optional) Mark areas.
        for (ConvexVolume vol : geom.convexVolumes()) {
            RecastArea.markConvexPolyArea(ctx, vol.verts, vol.hmin, vol.hmax, vol.areaMod, chf);
        }
        return chf;
    }

    public HeightfieldLayerSet buildLayers(JmeInputGeomProvider geom, RecastBuilderConfig builderCfg) {
        Context ctx = new Context();
        Heightfield solid = JmeRecastVoxelization.buildSolidHeightfield(geom, builderCfg, ctx);
        filterHeightfield(solid, builderCfg.cfg, ctx);
        CompactHeightfield chf = buildCompactHeightfield(geom, builderCfg.cfg, ctx, solid);
        return RecastLayers.buildHeightfieldLayers(ctx, chf, builderCfg.borderSize, builderCfg.cfg.walkableHeight);
    }
}
