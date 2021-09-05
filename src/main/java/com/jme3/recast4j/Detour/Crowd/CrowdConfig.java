package com.jme3.recast4j.Detour.Crowd;

public class CrowdConfig {

    public final float maxAgentRadius;
    /**
     * Max number of path requests in the queue
     */
    public int pathQueueSize = 32;
    /**
     * Max number of sliced path finding iterations executed per update (used to handle longer paths and replans)
     */
    public int maxFindPathIterations = 100;
    /**
     * Max number of sliced path finding iterations executed per agent to find the initial path to target
     */
    public int maxTargetFindPathIterations = 20;
    /**
     * Min time between topology optimizations (in seconds)
     */
    public float topologyOptimizationTimeThreshold = 0.5f;
    /**
     * The number of polygons from the beginning of the corridor to check to ensure path validity
     */
    public int checkLookAhead = 10;
    /**
     * Min time between target re-planning (in seconds)
     */
    public float targetReplanDelay = 1.0f;
    /**
     * Max number of sliced path finding iterations executed per topology optimization per agent
     */
    public int maxTopologyOptimizationIterations = 32;
    public float collisionResolveFactor = 0.7f;

    public CrowdConfig(float maxAgentRadius) {
        this.maxAgentRadius = maxAgentRadius;
    }

}