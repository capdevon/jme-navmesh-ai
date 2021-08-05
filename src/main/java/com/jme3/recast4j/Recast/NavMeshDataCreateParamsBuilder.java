/*
 *  MIT License
 *  Copyright (c) 2018 MeFisto94
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package com.jme3.recast4j.Recast;

import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilderConfig;

public class NavMeshDataCreateParamsBuilder {
	
    protected NavMeshDataCreateParams params;
    protected PolyMesh m_pmesh;
    protected PolyMeshDetail m_dmesh;

    public NavMeshDataCreateParamsBuilder(RecastBuilder.RecastBuilderResult rcResult) {
        params = new NavMeshDataCreateParams();
        m_pmesh = rcResult.getMesh();
        m_dmesh = rcResult.getMeshDetail();
    }

    /**
     * Sets the flags attribute for a specific polygon. This is later used in detour to filter whether you want to use
     * a certain polygon or not. It's a fast way to filter polygons on demand (e.g. in a boss battle, bridges become locked).
     *
     * @see #withPolyFlag(int, int) for setting individual flags instead of manually composing the flags attribute already
     * @see #withPolyFlagsAll(int) for setting flags for all polygons
     * @param id The Polygonal Id, good luck at finding this
     * @param flags The Flags to set
     * @return this
     */
    public NavMeshDataCreateParamsBuilder withPolyFlags(int id, int flags) {
        m_pmesh.flags[id] = flags;
        return this;
    }

    /**
     * Sets a flag attribute for a specific polygon. This is later used in detour to filter whether you want to use a
     * certain polygon or not. It's a fast way to filter polygons on demand (e.g. in a boss battle, bridges become locked)
     * @param id The Polygonal Id, good luck at finding this
     * @param flag The Flag to set
     * @return this
     */
    public NavMeshDataCreateParamsBuilder withPolyFlag(int id, int flag) {
        m_pmesh.flags[id] |= flag;
        return this;
    }

    /**
     * Sets the flags attribute for all polygons. This is later used in detour to filter whether you want to use
     * a certain polygon or not. It's a fast way to filter polygons on demand (e.g. in a boss battle, bridges become locked).
     *
     * @see #withPolyFlagAll(int) for setting individual flags instead of manually composing the flags attribute already
     * @see #withPolyFlagsAll(int) for setting flags for individual polygon
     * @param flags The Flags to set
     * @return this
     */
    public NavMeshDataCreateParamsBuilder withPolyFlagsAll(int flags) {
        for (int i = 0; i < m_pmesh.npolys; ++i) {
            m_pmesh.flags[i] = flags;
        }
        return this;
    }

    /**
     * Sets the flags attribute for all polygons. This is later used in detour to filter whether you want to use
     * a certain polygon or not. It's a fast way to filter polygons on demand (e.g. in a boss battle, bridges become locked).
     *
     * @see #withPolyFlags(int, int) for setting flags for individual polygon
     * @see #withPolyFlagAll(int) for setting individual flags instead of manually composing the flags attribute already
     * @param flag The Flag to set
     * @return this
     */
    public NavMeshDataCreateParamsBuilder withPolyFlagAll(int flag) {
        for (int i = 0; i < m_pmesh.npolys; ++i) {
            m_pmesh.flags[i] |= flag;
        }
        return this;
    }

    public NavMeshDataCreateParams build(RecastBuilderConfig builderCfg, OffMeshLink... connections) {
        params.verts = m_pmesh.verts;
        params.vertCount = m_pmesh.nverts;
        params.polys = m_pmesh.polys;
        params.polyAreas = m_pmesh.areas;
        params.polyFlags = m_pmesh.flags;
        params.polyCount = m_pmesh.npolys;
        params.nvp = m_pmesh.nvp;

        if (builderCfg.buildMeshDetail) {
            if (m_dmesh == null) {
                throw new IllegalStateException("Detail Mesh couldn't be built, this is a sign that the simple " +
                    "mesh didn't consist of any polygons/verts");
            }
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

        return params;
    }

}
