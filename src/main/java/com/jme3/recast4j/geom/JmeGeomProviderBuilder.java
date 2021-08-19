package com.jme3.recast4j.geom;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.recast4j.recast.AreaModification;

import com.jme3.bounding.BoundingBox;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import jme3tools.optimize.GeometryBatchFactory;

/**
 * This class will build a GeometryProvider for Recast to work with.
 * 
 * @author capdevon
 */
public class JmeGeomProviderBuilder {

    private static final Predicate<Spatial> DefaultFilter = sp -> sp.getUserData("ignoreFromBuild") == null;

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
     * Provides this Node to the Builder and performs a search through the
     * SceneGraph to gather all Geometries<br />
     * This uses the default filter: If userData "ignoreFromBuild" is set, ignore
     * this spatial
     * 
     * @param node The Node to use
     */
    public JmeGeomProviderBuilder(Node node) {
        this(node, DefaultFilter);
    }

    /**
     * Provides this Node to the Builder and performs a search through the
     * SceneGraph to gather all Geometries.
     * 
     * @param node   The Node to use.
     * @param filter A Filter which defines when a Spatial should be gathered.
     */
    public JmeGeomProviderBuilder(Node node, Predicate<Spatial> filter) {
        geometryList = findGeometries(node, new ArrayList<>(), filter);
    }

    /**
     * Provides this Node to the Builder and performs a search through the
     * SceneGraph to gather all Geometries.
     * 
     * @param root                The Node to use.
     * @param includedWorldBounds The queried objects must overlap these bounds to be included in the results.
     */
    public JmeGeomProviderBuilder(Node root, BoundingBox includedWorldBounds) {
        geometryList = findGeometries(root, new ArrayList<>(), includedWorldBounds);
    }

    protected List<Geometry> findGeometries(Node node, List<Geometry> geoms, Predicate<Spatial> filter) {
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

    protected List<Geometry> findGeometries(Node node, List<Geometry> geoms, BoundingBox includedWorldBounds) {
        for (Spatial spatial : node.getChildren()) {
            if (spatial instanceof Geometry) {
                Geometry g = (Geometry) spatial;
                if (g.getWorldBound().intersects(includedWorldBounds)) {
                    geoms.add(g);
                }
            } else if (spatial instanceof Node) {
                findGeometries((Node) spatial, geoms, includedWorldBounds);
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

    public JmeInputGeomProvider build() {
        if (mesh == null) {
            mesh = new Mesh();
            GeometryBatchFactory.mergeGeometries(geometryList, mesh);
        }

        return new JmeInputGeomProvider(getVertices(mesh), getIndices(mesh));
    }
    
    /**
     * 
     * @param defaultArea Area type to assign to results, unless modified by NavMeshMarkup.
     * @param markups     List of markups which allows finer control over how objects are collected.
     * @param results     List where results are stored, the list is cleared at the beginning of the call.
     * @return
     */
    public JmeInputGeomProvider build(AreaModification defaultArea,
        List<NavMeshBuildMarkup> markups, List<NavMeshBuildSource> results) {

        results.clear();

        final Map<Geometry, AreaModification> map = new HashMap<>();
        geometryList.forEach(g -> map.put(g, defaultArea));

        for (NavMeshBuildMarkup markup : markups) {
            if (markup.root instanceof Geometry) {
                Geometry geo = (Geometry) markup.root;
                if (markup.ignoreFromBuild) {
                    map.remove(geo);
                } else if (markup.overrideArea && map.containsKey(geo)) {
                    map.put(geo, markup.area);
                }
            } else if (markup.root instanceof Node) {
                ((Node) markup.root).depthFirstTraversal(new SceneGraphVisitorAdapter() {
                    @Override
                    public void visit(Geometry geo) {
                        if (markup.ignoreFromBuild) {
                            map.remove(geo);
                        } else if (markup.overrideArea && map.containsKey(geo)) {
                            map.put(geo, markup.area);
                        }
                    }
                });
            }
        }

        for (Map.Entry<Geometry, AreaModification> entry : map.entrySet()) {
            NavMeshBuildSource source = new NavMeshBuildSource(entry.getKey(), entry.getValue());
            results.add(source);
            System.out.println(source);
        }

        Mesh optiMesh = new Mesh();
        GeometryBatchFactory.mergeGeometries(map.keySet(), optiMesh);

        JmeInputGeomProvider geomProvider = new JmeInputGeomProvider(getVertices(optiMesh), getIndices(optiMesh));
        results.forEach(s -> geomProvider.addModification(s));

        return geomProvider;
    }
}
