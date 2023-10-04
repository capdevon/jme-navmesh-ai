package com.jme3.recast4j.builder;

import java.util.ArrayList;
import java.util.List;

import org.recast4j.detour.DetourCommon;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.detour.NavMeshParams;
import org.recast4j.recast.Recast;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.RecastConfig;
import org.recast4j.recast.RecastVectors;
import org.recast4j.recast.geom.InputGeomProvider;

import com.jme3.recast4j.geom.JmeInputGeomProvider;
import com.jme3.recast4j.geom.JmeRecastBuilder;

/**
 * 
 * @author capdevon
 */
public class TileNavMeshBuilder extends AbstractNavMeshBuilder {

    /**
     * 
     * @param m_geom
     * @param s
     * @return
     */
    public NavMesh build(JmeInputGeomProvider m_geom, NavMeshBuildSettings s) {

        // Initialize build config.
        RecastConfig cfg = new RecastConfig(
                s.partitionType,
                s.cellSize,
                s.cellHeight,
                s.agentHeight,
                s.agentRadius,
                s.agentMaxClimb,
                s.agentMaxSlope, 
                s.regionMinSize, 
                s.regionMergeSize,
                s.edgeMaxLen, 
                s.edgeMaxError, 
                s.vertsPerPoly, 
                s.detailSampleDist, 
                s.detailSampleMaxError,
                s.tileSize, 
                SampleAreaModifications.SAMPLE_AREAMOD_WALKABLE, 
                s.filterLowHangingObstacles, 
                s.filterLedgeSpans, 
                s.filterWalkableLowHeightSpans);
        
        // Build all tiles
        JmeRecastBuilder rcBuilder = new JmeRecastBuilder();
        
        int threads = 1;
        RecastBuilderResult[][] rcResult = rcBuilder.buildTiles(m_geom, cfg, threads);

        List<MeshData> lstMeshData = buildMeshData(m_geom, s.cellSize, s.cellHeight, s.agentHeight, s.agentRadius, s.agentMaxClimb, rcResult);
        NavMesh navMesh = buildNavMesh(m_geom, lstMeshData, s.cellSize, s.tileSize, s.vertsPerPoly);

        return navMesh;
    }

    private List<MeshData> buildMeshData(InputGeomProvider m_geom, float cellSize, float cellHeight, float agentHeight,
        float agentRadius, float agentMaxClimb, RecastBuilderResult[][] rcResult) {

        List<MeshData> meshData = new ArrayList<>();

        // Add tiles to nav mesh
        int tw = rcResult.length;
        int th = rcResult[0].length;

        for (int y = 0; y < th; y++) {
            for (int x = 0; x < tw; x++) {
                NavMeshDataCreateParams params = getNavMeshCreateParams(m_geom,
                    cellSize, cellHeight, agentHeight, agentRadius, agentMaxClimb, rcResult[x][y]);
                params.tileX = x;
                params.tileY = y;

                MeshData md = NavMeshBuilder.createNavMeshData(params);
                if (md != null) {
                    meshData.add(updateAreaAndFlags(md));
                }
            }
        }

        return meshData;
    }

    private NavMesh buildNavMesh(InputGeomProvider m_geom, List<MeshData> meshData, float cellSize, int tileSize, int vertsPerPoly) {

        NavMeshParams navMeshParams = new NavMeshParams();
        RecastVectors.copy(navMeshParams.orig, m_geom.getMeshBoundsMin());
        navMeshParams.tileWidth = tileSize * cellSize;
        navMeshParams.tileHeight = tileSize * cellSize;
        navMeshParams.maxTiles = getMaxTiles(m_geom, cellSize, tileSize);
        navMeshParams.maxPolys = getMaxPolysPerTile(m_geom, cellSize, tileSize);

        NavMesh navMesh = new NavMesh(navMeshParams, vertsPerPoly);
        meshData.forEach(md -> navMesh.addTile(md, 0, 0));

        return navMesh;
    }

    private int getMaxTiles(InputGeomProvider geom, float cellSize, int tileSize) {
        int tileBits = getTileBits(geom, cellSize, tileSize);
        return 1 << tileBits;
    }

    private int getMaxPolysPerTile(InputGeomProvider geom, float cellSize, int tileSize) {
        int polyBits = 22 - getTileBits(geom, cellSize, tileSize);
        return 1 << polyBits;
    }

    private int getTileBits(InputGeomProvider geom, float cellSize, int tileSize) {
        int[] wh = Recast.calcGridSize(geom.getMeshBoundsMin(), geom.getMeshBoundsMax(), cellSize);
        int tw = (wh[0] + tileSize - 1) / tileSize;
        int th = (wh[1] + tileSize - 1) / tileSize;
        int tileBits = Math.min(DetourCommon.ilog2(DetourCommon.nextPow2(tw * th)), 14);
        return tileBits;
    }

}
