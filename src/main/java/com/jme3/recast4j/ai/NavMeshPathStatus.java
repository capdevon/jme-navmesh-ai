package com.jme3.recast4j.ai;

/**
 *
 * @author capdevon
 */
public enum NavMeshPathStatus {
    PathComplete,   // The path terminates at the destination.
    PathPartial,    // The path cannot reach the destination.
    PathInvalid     // The path is invalid.
}
