package com.jme3.recast4j.ai;

import com.jme3.math.Vector3f;

/**
 * Result information for NavMesh queries.
 * 
 * @author capdevon
 */
public class NavMeshHit {

    // Distance to the point of hit.
    public float distance;
    // Flag set when hit.
    public boolean hit;
    // Mask specifying NavMesh area at point of hit.
    public int mask;
    // Normal at the point of hit.
    public Vector3f normal = new Vector3f();
    // Position of hit.
    public Vector3f position = new Vector3f();

    public void clear() {
        hit = false;
        distance = 0;
        mask = 0;
        normal.set(Vector3f.NAN);
        position.set(Vector3f.NAN);
    }

}