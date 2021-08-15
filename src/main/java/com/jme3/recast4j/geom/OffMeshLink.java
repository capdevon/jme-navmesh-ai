package com.jme3.recast4j.geom;

/**
 * Defines a navigation mesh off-mesh connection within a dtMeshTile object. An
 * off-mesh connection is a user defined traversable connection made up to two
 * vertices.
 * 
 * @author capdevon
 */
public class OffMeshLink {

    // Off-mesh connection vertices.
    public final float[] verts;
    // Off-mesh connection radii.
    public final float radius;
    // Can link be traversed in both directions.
    public final boolean biDirectional;
    // User defined area ids assigned to the off-mesh connections.
    public final int area;
    // User defined flags assigned to the off-mesh connections.
    public final int flags;
    // User-Defined ID, could be used to identify this in your game world together with {@link #area}
    public final int userID;

    public OffMeshLink(float[] start, float[] end, float radius, boolean biDirectional, int area, int flags, int userID) {
        verts = new float[6];
        verts[0] = start[0];
        verts[1] = start[1];
        verts[2] = start[2];
        verts[3] = end[0];
        verts[4] = end[1];
        verts[5] = end[2];
        this.radius = radius;
        this.flags = flags;
        this.area = area;
        this.biDirectional = biDirectional;
        this.userID = userID;
    }

}
