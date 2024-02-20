package com.jme3.recast4j.geom;

import org.recast4j.recast.AreaModification;

import com.jme3.scene.Geometry;

/**
 * Stores the Geometry and AreaModification for use with JmeRecastBuilder
 * via JmeInputGeomProvider. JmeRecastBuilder will use the geometry length to
 * apply the AreaModification stored with this object.
 * 
 * @author capdevon
 */
public class NavMeshModifier {

    // Geometry input sources.
    public final Geometry sourceObj;
    // Describes the area type of the NavMesh surface for this object.
    public final AreaModification area;

    /**
     * Creates a new instance of {@code NavMeshModifier}.
     * 
     * @param sourceObj Geometry to modify.
     * @param area      AreaModification to set.
     */
    public NavMeshModifier(Geometry sourceObj, AreaModification area) {
        this.sourceObj = sourceObj;
        this.area = area;
    }

    /**
     * @return the geomLength
     */
    public int getGeomLength() {
        return sourceObj.getMesh().getTriangleCount() * 3;
    }

    /**
     * @return the AreaModification.
     */
    public AreaModification getAreaModification() {
        return area;
    }

    @Override
    public String toString() {
        return "NavMeshModifier [sourceObj=" + sourceObj +
            ", geomLength=" + getGeomLength() +
            ", area=" + area.getMaskedValue() +
            "]";
    }

}
