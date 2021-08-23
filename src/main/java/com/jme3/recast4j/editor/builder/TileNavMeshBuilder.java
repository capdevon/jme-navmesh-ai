package com.jme3.recast4j.editor.builder;

import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.detour.NavMeshParams;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.RecastConfig;
import org.recast4j.recast.RecastVectors;
import org.recast4j.recast.geom.InputGeomProvider;

import com.jme3.recast4j.editor.NavMeshBuildSettings;
import com.jme3.recast4j.geom.NavMeshBuilderProgressListener;

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
    public NavMesh build(InputGeomProvider m_geom, NavMeshBuildSettings s) {

        // Initialize build config.
        RecastConfig cfg = new RecastConfig(s.partitionType, s.cellSize, s.cellHeight, s.agentHeight,
            s.agentRadius, s.agentMaxClimb, s.agentMaxSlope, s.regionMinSize, s.regionMergeSize,
            s.edgeMaxLen, s.edgeMaxError, s.vertsPerPoly, s.detailSampleDist, s.detailSampleMaxError,
            s.tileSize, s.walkableAreaMod, s.filterLowHangingObstacles, s.filterLedgeSpans, s.filterWalkableLowHeightSpans);

        // Build all tiles
        RecastBuilder rcBuilder = new RecastBuilder(new NavMeshBuilderProgressListener());
        //use listener when calling buildTiles
        int threads = 1;
        RecastBuilderResult[][] rcResult = rcBuilder.buildTiles(m_geom, cfg, threads);

        // Add tiles to nav mesh
        int tw = rcResult.length;
        int th = rcResult[0].length;

        // Create empty nav mesh
        NavMeshParams navMeshParams = new NavMeshParams();
        RecastVectors.copy(navMeshParams.orig, m_geom.getMeshBoundsMin());
        navMeshParams.tileWidth = s.tileSize * s.cellSize;
        navMeshParams.tileHeight = s.tileSize * s.cellSize;
        navMeshParams.maxTiles = tw * th;
        navMeshParams.maxPolys = 32768;

        NavMesh navMesh = new NavMesh(navMeshParams, s.vertsPerPoly);

        for (int y = 0; y < th; y++) {
            for (int x = 0; x < tw; x++) {

                PolyMesh pmesh = rcResult[x][y].getMesh();
                if (pmesh.npolys == 0) {
                    continue;
                }

                NavMeshDataCreateParams params = getNavMeshCreateParams(m_geom,
                    s.cellSize, s.cellHeight, s.agentHeight, s.agentRadius, s.agentMaxClimb, rcResult[x][y]);
                params.tileX = x;
                params.tileY = y;

                MeshData meshData = NavMeshBuilder.createNavMeshData(params);
                navMesh.addTile(meshData, 0, 0);
            }
        }

        return navMesh;
    }

}