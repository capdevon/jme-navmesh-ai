package com.jme3.recast4j.Detour.Crowd;

/**
 * Determines the way individual crowds are updated while being attached to a Crowd Manager.
 * Note that PARALLEL isn't the magic solution because it only makes sense with many active crowds and
 * can't fix the fact that large crowds are straining the performance
 */
public enum CrowdUpdateType {
    /**
     * Update one Crowd after another, all in the caller's thread (most probably the game logic thread).
     * You could however also have your own UI/AI Update Thread there.
     */
    SEQUENTIAL,

    /**
     * Update all Crowds in Parallel using the Java 8 Stream API (So the Thread Count is guessed automatically
     * based on available Hardware). Note that this is not always the best solution and in most cases might perform
     * worse than {@link #SEQUENTIAL}
     */
    PARALLEL
}
