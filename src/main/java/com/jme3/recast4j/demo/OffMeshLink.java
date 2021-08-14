package com.jme3.recast4j.demo;

import org.recast4j.detour.NavMesh;

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
    // User defined flags assigned to the off-mesh connections.
    public final int flags;

    /** 
     * User defined area ids assigned to the off-mesh connections.
     * (https://digestingduck.blogspot.com/2010/01/off-mesh-connection-progress-pt-3.html?showComment=1334596410261#c3440987512618011308)
     * Can be used to weight off-mesh-connections based on their area type (teleporter would be a different area than elevator)
     */
    public final int areas;

    /** 
     * The permitted travel direction of the off-mesh connections.
     * 0 = Travel only from endpoint A to endpoint B
     * #NavMesh.DT_OFFMESH_CON_BIDIR = Bidirectional travel.
     */
    public final int direction;

    /** User-Defined ID, could be used to identify this in your game world together with {@link #areas} */
    public final int userID;

    public OffMeshLink(float[] start, float[] end, float radius, boolean biDirectional, int areas, int flags, int userID) {
        verts = new float[6];
        verts[0] = start[0];
        verts[1] = start[1];
        verts[2] = start[2];
        verts[3] = end[0];
        verts[4] = end[1];
        verts[5] = end[2];
        this.radius = radius;
        this.flags = flags;
        this.areas = areas;
        this.direction = biDirectional ? NavMesh.DT_OFFMESH_CON_BIDIR : 0;
        this.userID = userID;
    }

}