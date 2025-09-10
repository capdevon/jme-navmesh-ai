package com.jme3.recast4j.geom;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.jme3.bounding.BoundingBox;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * This class will build a GeometryProvider for Recast to work with.
 * 
 * @author capdevon
 */
public class InputGeomProviderBuilder {
    
    private static final Predicate<Spatial> DefaultFilter =
            sp -> sp.getUserData(NavMeshData.JME_NAVMESH_IGNORE) == null;

    private InputGeomProviderBuilder() {}
    
    /**
     * Performs a search in the SceneGraph to collect all geometries of the supplied
     * node. It uses the default filter: if NavMeshData.JME_NAVMESH_IGNORE is set, it
     * ignores this space.
     * 
     * @param rootNode The Node to use.
     */
    public static JmeInputGeomProvider build(Node rootNode) {
        return build(rootNode, DefaultFilter);
    }

    /**
     * Provides this Node to the Builder and performs a search through the
     * SceneGraph to gather all Geometries.
     * 
     * @param rootNode The Node to use.
     * @param filter   A Filter which defines when a Spatial should be gathered.
     */
    public static JmeInputGeomProvider build(Node rootNode, Predicate<Spatial> filter) {
        List<Geometry> geometries = collectSources(rootNode, new ArrayList<>(), filter);
        return new JmeInputGeomProvider(geometries);
    }

    /**
     * Provides this Node to the Builder and performs a search through the
     * SceneGraph to gather all Geometries.
     * 
     * @param rootNode            The Node to use.
     * @param includedWorldBounds The queried objects must overlap these bounds to be included in the results.
     */
    public static JmeInputGeomProvider build(Node rootNode, BoundingBox includedWorldBounds) {
        List<Geometry> geometries = collectSources(rootNode, new ArrayList<>(), includedWorldBounds);
        return new JmeInputGeomProvider(geometries);
    }

    private static List<Geometry> collectSources(Node node, List<Geometry> geometries, Predicate<Spatial> filter) {
        for (Spatial sp : node.getChildren()) {
            if (!filter.test(sp)) {
                continue;
            }

            if (sp instanceof Geometry) {
                geometries.add((Geometry) sp);
            } else if (sp instanceof Node) {
                collectSources((Node) sp, geometries, filter);
            }
        }
        return geometries;
    }

    private static List<Geometry> collectSources(Node node, List<Geometry> geometries, BoundingBox includedWorldBounds) {
        for (Spatial sp : node.getChildren()) {
            if (sp instanceof Geometry) {
                Geometry geo = (Geometry) sp;
                if (geo.getWorldBound().intersects(includedWorldBounds)) {
                    geometries.add(geo);
                }
            } else if (sp instanceof Node) {
                collectSources((Node) sp, geometries, includedWorldBounds);
            }
        }
        return geometries;
    }

}
