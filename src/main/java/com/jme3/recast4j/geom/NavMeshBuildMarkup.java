package com.jme3.recast4j.geom;

import org.recast4j.recast.AreaModification;

import com.jme3.scene.Spatial;

/**
 * The NavMesh build markup allows you to control how certain objects are
 * treated during the NavMesh build process, specifically when collecting
 * sources for building.
 * 
 * You can override the area type or specify that certain objects should be
 * excluded from collected sources. The markup is applied hierarchically.</br>
 * See Also: {@link JmeGeomProviderBuilder}.build
 * 
 * @author capdevon
 */
public class NavMeshBuildMarkup {

    // Use this to specify which GameObject (including the GameObject’s children)
    // the markup should be applied to.
    public final Spatial root;
    // Use this to specify whether the GameObject and its children should be ignored.
    public final boolean ignoreFromBuild;
    // Use this to specify whether the area type of the GameObject and its children
    // should be overridden by the area type specified in this struct.
    public final boolean overrideArea;
    // The area type to use when override area is enabled.
    public final AreaModification area;

    /**
     * 
     * @param root
     * @param ignoreFromBuild
     */
    public NavMeshBuildMarkup(Spatial root, boolean ignoreFromBuild) {
        this.root = root;
        this.ignoreFromBuild = ignoreFromBuild;
        this.area = null;
        this.overrideArea = false;
    }

    /**
     * 
     * @param root
     * @param area
     */
    public NavMeshBuildMarkup(Spatial root, AreaModification area) {
        this.root = root;
        this.area = area;
        this.overrideArea = true;
        this.ignoreFromBuild = false;
    }

}
