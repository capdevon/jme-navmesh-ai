/*
 *  MIT License
 *  Copyright (c) 2021 MeFisto94
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
    
    private static final Predicate<Spatial> DefaultFilter = sp -> sp.getUserData("no_collision") == null;

    private List<Geometry> geometryList;
    private Mesh mesh;
	
    /**
     * Provides this Geometry to the Builder
     * @param geo The Geometry to use
     */
    public JmeGeomProviderBuilder(Geometry geo) {
    	geometryList = new ArrayList<>();
    	geometryList.add(geo);
    }

    /**
     * Provides this Node to the Builder and performs a search through the SceneGraph to gather all Geometries<br />
     * This uses the default filter: If userData "no_collision" is set, ignore this spatial
     * @param node The Node to use
     */
    public JmeGeomProviderBuilder(Node node) {
        this(node, DefaultFilter);
    }
    
    /**
     * Provides this Node to the Builder and performs a search through the SceneGraph to gather all Geometries
     * @param node The Node to use
     * @param filter A Filter which defines when a Spatial should be gathered
     */
    public JmeGeomProviderBuilder(Node node, Predicate<Spatial> filter) {
    	geometryList = findGeometries(node, new ArrayList<>(), filter);
    }
    
    protected List<Geometry> findGeometries(Node node, List<Geometry> geoms, Predicate<Spatial> filter) {
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
        if (mesh == null) {
            mesh = new Mesh();
            GeometryBatchFactory.mergeGeometries(geometryList, mesh);
        }
        
        return new JmeInputGeomProvider(getVertices(mesh), getIndices(mesh));
    }
}
