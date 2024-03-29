package com.jme3.recast4j.ai;

import com.jme3.math.Vector3f;

/**
 * Result information for NavMesh queries.
 *
 * @author capdevon
 */
public class NavMeshHit {

    // Distance to the point of hit.
    protected float distance;
    // Flag set when hit.
    protected boolean hit;
    // Mask specifying NavMesh area at point of hit.
    protected int mask;
    // Normal at the point of hit.
    protected Vector3f normal = Vector3f.NAN.clone();
    // Position of hit.
    protected Vector3f position = Vector3f.NAN.clone();

    public void clear() {
        hit = false;
        distance = 0;
        mask = 0;
        normal.set(Vector3f.NAN);
        position.set(Vector3f.NAN);
    }

    public float getDistance() {
        return distance;
    }

    public boolean isHit() {
        return hit;
    }

    public int getMask() {
        return mask;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public Vector3f getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "NavMeshHit [distance=" + distance 
                + ", hit=" + hit 
                + ", mask=" + mask 
                + ", normal=" + normal
                + ", position=" + position 
                + "]";
    }

}
