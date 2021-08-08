package com.jme3.recast4j.Recast;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;

/**
 * 
 * @author capdevon
 */
public class NavMeshDataCreateParamsBuilder {
	
    protected RecastBuilderResult rcResult;

    /**
     * Constructor.
     * 
     * @param rcResult
     */
    public NavMeshDataCreateParamsBuilder(RecastBuilder.RecastBuilderResult rcResult) {
    	this.rcResult = rcResult;
    }

    /**
     * 
     * @param builderCfg
     * @param connections
     * @return
     */
    public NavMeshDataCreateParams build(RecastBuilderConfig builderCfg, OffMeshLink... connections) {
		
    	NavMeshDataCreateParams params = new NavMeshDataCreateParams();
    	
    	PolyMesh m_pmesh = rcResult.getMesh();
    	PolyMeshDetail m_dmesh = rcResult.getMeshDetail();
    	
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

        params.walkableHeight = builderCfg.cfg.walkableHeight;
        params.walkableRadius = builderCfg.cfg.walkableRadius;
        params.walkableClimb = builderCfg.cfg.walkableClimb;
        params.bmin = m_pmesh.bmin;
        params.bmax = m_pmesh.bmax;
        params.cs = builderCfg.cfg.cs;
        params.ch = builderCfg.cfg.ch;
        params.buildBvTree = true;

        if (connections != null && connections.length > 0) {
            params.offMeshConCount 	= connections.length;
            params.offMeshConVerts 	= new float[connections.length * 6];
            params.offMeshConRad 	= new float[connections.length];
            params.offMeshConDir 	= new int[connections.length];
            params.offMeshConAreas 	= new int[connections.length];
            params.offMeshConFlags 	= new int[connections.length];
            params.offMeshConUserID 	= new int[connections.length];

            for (int i = 0; i < connections.length; ++i) {
                OffMeshLink offMeshCon = connections[i];
                for (int j = 0; j < 6; j++) {
                    params.offMeshConVerts[6 * i + j] = offMeshCon.verts[j];
                }

                params.offMeshConRad[i] = offMeshCon.radius;
                params.offMeshConDir[i] = offMeshCon.direction;
                params.offMeshConAreas[i] = offMeshCon.areas;
                params.offMeshConFlags[i] = offMeshCon.flags;
                params.offMeshConUserID[i] = offMeshCon.userID;
            }
        }

        System.out.println(ReflectionToStringBuilder.toString(params, ToStringStyle.MULTI_LINE_STYLE));
        return params;
    }

}
