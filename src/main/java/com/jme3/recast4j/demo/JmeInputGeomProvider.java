/*
 * The MIT License
 *
 * Copyright 2019 .
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * MODELS/DUNE.J3O:
 * Converted from http://quadropolis.us/node/2584 [Public Domain according to the Tags of this Map]
 */

package com.jme3.recast4j.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.recast4j.detour.OffMeshConnection;
import org.recast4j.recast.AreaModification;
import org.recast4j.recast.ConvexVolume;
import org.recast4j.recast.RecastVectors;
import org.recast4j.recast.geom.InputGeomProvider;
import org.recast4j.recast.geom.TriMesh;

/**
 *
 * @author Robert
 */
public class JmeInputGeomProvider implements InputGeomProvider {

    private final List<ConvexVolume> volumes = new ArrayList<>();
    private final List<OffMeshConnection> listOffMeshCons = new ArrayList<>();
    private final List<Modification> listModifications = new ArrayList<>();
    private final float[] vertices;
    private final int[] faces;
    private final float[] normals;
    private final float[] bmin;
    private final float[] bmax;

    /**
     * Constructor.
     * 
     * @param vertexPositions
     * @param meshFaces
     */
    public JmeInputGeomProvider(List<Float> vertexPositions, List<Integer> meshFaces) {
        this(mapVertices(vertexPositions), mapFaces(meshFaces));
    }

    private static int[] mapFaces(List<Integer> meshFaces) {
        int[] faces = new int[meshFaces.size()];
        for (int i = 0; i < faces.length; i++) {
            faces[i] = meshFaces.get(i);
        }
        return faces;
    }

    private static float[] mapVertices(List<Float> vertexPositions) {
        float[] vertices = new float[vertexPositions.size()];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = vertexPositions.get(i);
        }
        return vertices;
    }

    /**
     * Constructor.
     * 
     * @param vertices
     * @param faces
     */
    public JmeInputGeomProvider(float[] vertices, int[] faces) {
        this.vertices = vertices;
        this.faces = faces;
        normals = new float[faces.length];
        calculateNormals();
        bmin = new float[3];
        bmax = new float[3];
        RecastVectors.copy(bmin, vertices, 0);
        RecastVectors.copy(bmax, vertices, 0);
        for (int i = 1; i < vertices.length / 3; i++) {
            RecastVectors.min(bmin, vertices, i * 3);
            RecastVectors.max(bmax, vertices, i * 3);
        }
    }

    @Override
    public float[] getMeshBoundsMin() {
        return bmin;
    }

    @Override
    public float[] getMeshBoundsMax() {
        return bmax;
    }

    @Override
    public List<ConvexVolume> convexVolumes() {
        return volumes;
    }
    
    public void addConvexVolume(float[] verts, float minh, float maxh, AreaModification areaMod) {
        ConvexVolume vol = new ConvexVolume();
        vol.hmin = minh;
        vol.hmax = maxh;
        vol.verts = verts;
        vol.areaMod = areaMod;
        volumes.add(vol);
    }

    @Override
    public Iterable<TriMesh> meshes() {
        return Collections.singletonList(new TriMesh(vertices, faces));
    }

    public void calculateNormals() {
        for (int i = 0; i < faces.length; i += 3) {
            int v0 = faces[i] * 3;
            int v1 = faces[i + 1] * 3;
            int v2 = faces[i + 2] * 3;
            float[] e0 = new float[3], e1 = new float[3];
            for (int j = 0; j < 3; ++j) {
                e0[j] = vertices[v1 + j] - vertices[v0 + j];
                e1[j] = vertices[v2 + j] - vertices[v0 + j];
            }
            normals[i] = e0[1] * e1[2] - e0[2] * e1[1];
            normals[i + 1] = e0[2] * e1[0] - e0[0] * e1[2];
            normals[i + 2] = e0[0] * e1[1] - e0[1] * e1[0];
            float d = (float) Math.sqrt(normals[i] * normals[i] + normals[i + 1] * normals[i + 1] + normals[i + 2] * normals[i + 2]);
            if (d > 0) {
                d = 1.0f / d;
                normals[i] *= d;
                normals[i + 1] *= d;
                normals[i + 2] *= d;
            }
        }
    }

	public List<Modification> getListMods() {
		return this.listModifications;
	}
	
	public void addMod(Modification mod) {
		this.listModifications.add(mod);
	}

    public List<org.recast4j.detour.OffMeshConnection> getListOffMeshCons() {
        return listOffMeshCons;
    }

    public void addOffMeshCon(float[] start, float[] end, float radius, int bidir, int id) {
        org.recast4j.detour.OffMeshConnection con = new org.recast4j.detour.OffMeshConnection();
        float[] pos = new float[6];
        System.arraycopy(start, 0, pos, 0, start.length);
        System.arraycopy(end, 0, pos, 3, end.length);
        con.pos = pos;
        con.rad = radius;
        con.flags = bidir;
        con.userId = id;
        listOffMeshCons.add(con);
    }
}
