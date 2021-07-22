package com.jme3.recast4j.Detour.Crowd;

/**
 * This class defines how the results of Detour Crowd calculation are applied to the World.
 * This can be moving the spatial directly, moving a BetterCharacterControl or some custom code.
 */
public enum MovementApplicationType {
    NONE,
    DIRECT,
    BETTER_CHARACTER_CONTROL,
    CUSTOM
}
