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
package com.jme3.recast4j.Recast.Utils;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import jme3tools.optimize.GeometryBatchFactory;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.Poly;
import org.recast4j.detour.PolyDetail;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Class consisting of helper methods to simplify interfacing between recast4j and jMonkeyEngine
 * @author MeFisto94
 */
public class RecastUtils {

    public static Mesh getDebugMesh(PolyDetail[] meshes, float[] detailVerts, int[] detailTris) {
        Mesh mesh = new Mesh();
        List<Geometry> geometryList = new ArrayList<>(meshes.length);

        for (PolyDetail pd: meshes) {
            geometryList.add(new Geometry("", getDebugMesh(pd, detailVerts, detailTris)));
        }

        GeometryBatchFactory.mergeGeometries(geometryList, mesh);
        return mesh;
    }

    public static Mesh getDebugMesh(PolyDetail dmesh, float[] detailVerts, int[] detailTris) {
        FloatBuffer vertices = BufferUtils.createFloatBuffer(3 * dmesh.vertCount);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(3 * dmesh.triCount);

        for (int i = 0; i < dmesh.triCount; ++i) {
            indexBuffer.put(detailTris[3 * (dmesh.triBase + i)    ]);
            indexBuffer.put(detailTris[3 * (dmesh.triBase + i) + 1]);
            indexBuffer.put(detailTris[3 * (dmesh.triBase + i) + 2]);
        }

        for (int i = 0; i < dmesh.vertCount; ++i) {
            vertices.put(detailVerts[3 * (dmesh.vertBase + i)    ]);
            vertices.put(detailVerts[3 * (dmesh.vertBase + i) + 1]);
            vertices.put(detailVerts[3 * (dmesh.vertBase + i) + 2]);
        }

        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
        mesh.setBuffer(VertexBuffer.Type.Index, 3, indexBuffer);
        mesh.updateBound();
        return mesh;
    }

    /**
     * Builds a Debug Mesh out of the Poly Data passed to this.
     * Warning: This requires a max vert per poly setting of 3, if you don't respect it, the debug mesh won't work.
     * @param meshData The mesh data containing the polygons
     * @return
     */
    public static Mesh getDebugMesh(MeshData meshData) {
        FloatBuffer vertices = BufferUtils.createFloatBuffer(meshData.verts);

        int sumIndices = 0;
        for (Poly p: meshData.polys) {
            if (p.verts.length != 3) {
                throw new IllegalArgumentException("Error: Cannot display a polygon mesh, Triangle Mesh required. "
                + "Please ensure that the vertsPerPoly setting in the RecastConfig is set to 3");
            } // @TODO: Delaunay Traingulation, if you're brave enough.
            sumIndices += p.verts.length;
        }

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(sumIndices);

        for (Poly p: meshData.polys) {
            for (int idx: p.verts) {
                indexBuffer.put(idx);
            }
        }
        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
        mesh.setBuffer(VertexBuffer.Type.Index, 3, indexBuffer);
        mesh.updateBound();
        return mesh;
    }
}
