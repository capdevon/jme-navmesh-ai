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

package com.jme3.recast4j.demo;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import jme3tools.optimize.GeometryBatchFactory;

/**
 * This class will build a GeometryProvider for Recast to work with.<br />
 * <b>Note: </b>This code has to be run from the MainThread, but once the Geometry is built, it can be run from every
 * thread
 */
public class JmeGeomProviderBuilder {
    
    List<Geometry> geometryList;
    Mesh m;

    private JmeGeomProviderBuilder(List<Geometry> geometryList) {
        this.geometryList = geometryList;
    }

    protected static List<Geometry> findGeometries(Node node, List<Geometry> geoms, Predicate<Spatial> filter) {
        for (Spatial spatial: node.getChildren()) {
            if (!filter.test(spatial)) {
                continue;
            }

            if (spatial instanceof Geometry) {
                geoms.add((Geometry) spatial);
            } else if (spatial instanceof Node) {
                findGeometries((Node) spatial, geoms, filter);
            }
        }
        return geoms;
    }

    /**
     * Provides this Node to the Builder and performs a search through the SceneGraph to gather all Geometries
     * @param n The Node to use
     * @param filter A Filter (analogue to the Java 8 Stream API) which defines when a Spatial should be gathered
     */
    public JmeGeomProviderBuilder(Node n, Predicate<Spatial> filter) {
        this(findGeometries(n, new ArrayList<>(), filter));
    }

    /**
     * Provides this Node to the Builder and performs a search through the SceneGraph to gather all Geometries<br />
     * This uses the default filter: If userData "no_collission" is set, ignore this spatial
     * @param n The Node to use
     */
    public JmeGeomProviderBuilder(Node n) {
        this(n, spatial -> spatial.getUserData("no_collission") == null);
    }

    // bypass java's "this only in line 1" limitation
    protected static ArrayList<Geometry> newAndAdd(Geometry g) {
        ArrayList<Geometry> geo = new ArrayList<>(1);
        geo.add(g);
        return geo;
    }

    /**
     * Provides this Geometry to the Builder
     * @param g The Geometry to use
     */
    public JmeGeomProviderBuilder(Geometry g) {
        this(newAndAdd(g));
    }

    /**
     * Provide this Mesh to the Builder.
     * <b>Note: </b>Mesh does not contain trans, rot, scale information, so we suggest to use other constructors instead
     * @see #GeometryProviderBuilder(Geometry)
     * @see #GeometryProviderBuilder(Node, Predicate)
     * @param m The Mesh to use
     */
    public JmeGeomProviderBuilder(Mesh m) {
        this.m = m;
    }

    protected List<Float> getVertices(Mesh mesh) {
        FloatBuffer buffer = mesh.getFloatBuffer(VertexBuffer.Type.Position);
        float[] vertexArray = BufferUtils.getFloatArray(buffer);
        List<Float> vertexList = new ArrayList<>(vertexArray.length);
        for (float vertex: vertexArray) {
            vertexList.add(vertex);
        }
        return vertexList;
    }

    protected List<Integer> getIndices(Mesh mesh) {
        int[] indices = new int[3];
        Integer[] triangles = new Integer[mesh.getTriangleCount() * 3];

        for (int i = 0; i < mesh.getTriangleCount(); i++) {
            mesh.getTriangle(i, indices);
            triangles[3 * i] = indices[0];
            triangles[3 * i + 1] = indices[1];
            triangles[3 * i + 2] = indices[2];
        }
        //Independent copy so Arrays.asList is garbage collected
        return new ArrayList<>(Arrays.asList(triangles));
    }

    public JmeInputGeomProvider build() {
        if (m == null) {
            m = new Mesh();
            GeometryBatchFactory.mergeGeometries(geometryList, m);
        }
        
        return new JmeInputGeomProvider(getVertices(m), getIndices(m));
    }
}
