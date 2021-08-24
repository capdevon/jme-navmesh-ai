package com.jme3.recast4j.editor.builder;

import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastConfig;

import com.jme3.recast4j.editor.NavMeshBuildSettings;
import com.jme3.recast4j.geom.JmeInputGeomProvider;
import com.jme3.recast4j.geom.JmeRecastBuilder;

/**
 * 
 * @author capdevon
 */
public class SoloNavMeshBuilder extends AbstractNavMeshBuilder {

    /**
     * 
     * @param m_geom
     * @param s
     * @return
     */
    public NavMesh build(JmeInputGeomProvider m_geom, NavMeshBuildSettings s) {

        // Initialize build config.
        RecastConfig cfg = new RecastConfig(s.partitionType, s.cellSize, s.cellHeight, s.agentHeight,
            s.agentRadius, s.agentMaxClimb, s.agentMaxSlope, s.regionMinSize, s.regionMergeSize,
            s.edgeMaxLen, s.edgeMaxError, s.vertsPerPoly, s.detailSampleDist, s.detailSampleMaxError,
            s.tileSize, s.walkableAreaMod, s.filterLowHangingObstacles, s.filterLedgeSpans, s.filterWalkableLowHeightSpans);

        // Create a RecastBuilderConfig with world bounds of our geometry.
        RecastBuilderConfig bcfg = new RecastBuilderConfig(cfg, m_geom.getMeshBoundsMin(), m_geom.getMeshBoundsMax());

        // Build our Navmesh data using our gathered geometry and configuration.
        JmeRecastBuilder rcBuilder = new JmeRecastBuilder();
        RecastBuilderResult rcResult = rcBuilder.build(m_geom, bcfg);

        // Set the parameters needed to build our MeshData using the RecastBuilder results.
        NavMeshDataCreateParams params = getNavMeshCreateParams(m_geom,
            s.cellSize, s.cellHeight, s.agentHeight, s.agentRadius, s.agentMaxClimb, rcResult);

        //Generate MeshData using our parameters object.
        MeshData meshData = NavMeshBuilder.createNavMeshData(params);

        //Build the NavMesh.
        NavMesh navMesh = new NavMesh(meshData, s.vertsPerPoly, 0);

        return navMesh;
    }

}
