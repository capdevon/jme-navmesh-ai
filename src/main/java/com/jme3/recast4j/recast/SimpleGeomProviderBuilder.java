package com.jme3.recast4j.Recast;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.recast4j.recast.geom.InputGeomProvider;
import org.recast4j.recast.geom.SimpleInputGeomProvider;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import jme3tools.optimize.GeometryBatchFactory;

/**
 * This class will build a GeometryProvider for Recast to work with.<br />
 * <b>Note: </b>This code has to be run from the MainThread, but once the
 * Geometry is built, it can be run from every thread
 */
public class SimpleGeomProviderBuilder {

    private static final Predicate<Spatial> DefaultFilter = sp -> sp.getUserData("ignoreFromBuild") == null;

    private List<Geometry> geometryList;
    private Mesh mesh;
	
    /**
     * Provides this Geometry to the Builder
     * @param geo The Geometry to use
     */
    public SimpleGeomProviderBuilder(Geometry geo) {
    	geometryList = new ArrayList<>();
    	geometryList.add(geo);
    }

    /**
     * Provides this Node to the Builder and performs a search through the
     * SceneGraph to gather all Geometries<br/>
     * This uses the default filter: If userData "ignoreFromBuild" is set, ignore this
     * spatial
     * 
     * @param node The Node to use
     */
    public SimpleGeomProviderBuilder(Node node) {
        this(node, DefaultFilter);
    }
    
    /**
     * Provides this Node to the Builder and performs a search through the
     * SceneGraph to gather all Geometries
     * 
     * @param node   The Node to use
     * @param filter A Filter which defines when a Spatial should be gathered
     */
    public SimpleGeomProviderBuilder(Node node, Predicate<Spatial> filter) {
    	geometryList = findGeometries(node, new ArrayList<>(), filter);
    }
    
    protected List<Geometry> findGeometries(final Node node, final List<Geometry> geoms, final Predicate<Spatial> filter) {
        for (Spatial spatial : node.getChildren()) {
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

    protected float[] getVertices(Mesh mesh) {
        FloatBuffer buffer = mesh.getFloatBuffer(VertexBuffer.Type.Position);
        return BufferUtils.getFloatArray(buffer);
    }

    protected int[] getIndices(Mesh mesh) {
        int[] indices = new int[3];
        int[] triangles = new int[mesh.getTriangleCount() * 3];

        for (int i = 0; i < mesh.getTriangleCount(); i++) {
            mesh.getTriangle(i, indices);
            triangles[3 * i] = indices[0];
            triangles[3 * i + 1] = indices[1];
            triangles[3 * i + 2] = indices[2];
        }
        return triangles;
    }

    public InputGeomProvider build() {
        if (mesh == null) {
            mesh = new Mesh();
            GeometryBatchFactory.mergeGeometries(geometryList, mesh);
        }

        return new SimpleInputGeomProvider(getVertices(mesh), getIndices(mesh));
    }
}
