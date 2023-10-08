package com.jme3.recast4j.builder;

import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.RecastConfig;
import org.recast4j.recast.geom.InputGeomProvider;

/**
 * 
 * @author capdevon
 */
public abstract class AbstractNavMeshBuilder {
    
    protected RecastConfig toRecastConfig(NavMeshBuildSettings s) {
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
        
        return cfg;
    }

    protected NavMeshDataCreateParams getNavMeshCreateParams(InputGeomProvider m_geom, float m_cellSize, float m_cellHeight, 
            float m_agentHeight, float m_agentRadius, float m_agentMaxClimb, RecastBuilderResult rcResult) {

        PolyMesh m_pmesh = rcResult.getMesh();
        PolyMeshDetail m_dmesh = rcResult.getMeshDetail();
        NavMeshDataCreateParams params = new NavMeshDataCreateParams();

        for (int i = 0; i < m_pmesh.npolys; ++i) {
            m_pmesh.flags[i] = 1;
        }

        params.verts = m_pmesh.verts;
        params.vertCount = m_pmesh.nverts;
        params.polys = m_pmesh.polys;
        params.polyAreas = m_pmesh.areas;
        params.polyFlags = m_pmesh.flags;
        params.polyCount = m_pmesh.npolys;
        params.nvp = m_pmesh.nvp;

        if (m_dmesh != null) {
            params.detailMeshes = m_dmesh.meshes;
            params.detailVerts = m_dmesh.verts;
            params.detailVertsCount = m_dmesh.nverts;
            params.detailTris = m_dmesh.tris;
            params.detailTriCount = m_dmesh.ntris;
        }

        params.walkableHeight = m_agentHeight;
        params.walkableRadius = m_agentRadius;
        params.walkableClimb = m_agentMaxClimb;
        params.bmin = m_pmesh.bmin;
        params.bmax = m_pmesh.bmax;
        params.cs = m_cellSize;
        params.ch = m_cellHeight;
        params.buildBvTree = true;

        return params;
    }
    
    protected MeshData updateAreaAndFlags(MeshData meshData) {
        // Update poly flags from areas.
        for (int i = 0; i < meshData.polys.length; ++i) {
            if (meshData.polys[i].getArea() == SampleAreaModifications.SAMPLE_POLYAREA_TYPE_WALKABLE) {
                meshData.polys[i].setArea(SampleAreaModifications.SAMPLE_POLYAREA_TYPE_GROUND);
            }
            switch (meshData.polys[i].getArea()) {
                case SampleAreaModifications.SAMPLE_POLYAREA_TYPE_GROUND:
                case SampleAreaModifications.SAMPLE_POLYAREA_TYPE_GRASS:
                case SampleAreaModifications.SAMPLE_POLYAREA_TYPE_ROAD:
                    meshData.polys[i].flags = SampleAreaModifications.SAMPLE_POLYFLAGS_WALK;
                    break;
                case SampleAreaModifications.SAMPLE_POLYAREA_TYPE_WATER:
                    meshData.polys[i].flags = SampleAreaModifications.SAMPLE_POLYFLAGS_SWIM;
                    break;
                case SampleAreaModifications.SAMPLE_POLYAREA_TYPE_DOOR:
                    meshData.polys[i].flags = SampleAreaModifications.SAMPLE_POLYFLAGS_DOOR;
                    break;
                default:
                    break;
            }
        }
        return meshData;
    }

}
