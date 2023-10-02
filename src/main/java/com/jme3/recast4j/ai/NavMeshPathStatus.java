package com.jme3.recast4j.ai;

/**
 *
 * @author capdevon
 */
public enum NavMeshPathStatus {
    
    // The path terminates at the destination.
    PathComplete,
    // The path cannot reach the destination.
    PathPartial,
    // The path is invalid.
    PathInvalid
}
