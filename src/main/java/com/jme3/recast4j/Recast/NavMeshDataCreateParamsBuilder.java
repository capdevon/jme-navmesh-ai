package com.jme3.recast4j.Recast;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;

import com.jme3.recast4j.demo.JmeInputGeomProvider;
import com.jme3.recast4j.demo.OffMeshLink;

/**
 * 
 * @author capdevon
 */
@Deprecated
public class NavMeshDataCreateParamsBuilder {

    public static NavMeshDataCreateParams build(JmeInputGeomProvider m_geom, float m_cellSize,
        float m_cellHeight, float m_agentHeight, float m_agentRadius, float m_agentMaxClimb, RecastBuilderResult rcResult) {

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

        params.offMeshConCount = m_geom.getOffMeshConnections().size();
        params.offMeshConVerts 	= new float[params.offMeshConCount * 6];
        params.offMeshConRad 	= new float[params.offMeshConCount];
        params.offMeshConDir 	= new int[params.offMeshConCount];
        params.offMeshConAreas 	= new int[params.offMeshConCount];
        params.offMeshConFlags 	= new int[params.offMeshConCount];
        params.offMeshConUserID = new int[params.offMeshConCount];

        for (int i = 0; i < params.offMeshConCount; i++) {
            OffMeshLink offMeshCon = m_geom.getOffMeshConnections().get(i);
            for (int j = 0; j < 6; j++) {
                params.offMeshConVerts[6 * i + j] = offMeshCon.verts[j];
            }
            params.offMeshConRad[i] = offMeshCon.radius;
            params.offMeshConDir[i] = offMeshCon.direction;
            params.offMeshConAreas[i] = offMeshCon.areas;
            params.offMeshConFlags[i] = offMeshCon.flags;
            params.offMeshConUserID[i] = offMeshCon.userID;
        }

        System.out.println(ReflectionToStringBuilder.toString(params, ToStringStyle.MULTI_LINE_STYLE));
        return params;
    }

}
