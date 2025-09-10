package com.jme3.recast4j.detour.crowd;

/**
 * @author capdevon
 */
public class CrowdConfig {

    public final float maxAgentRadius;
    public final int maxAgents;

    public CrowdConfig(float maxAgentRadius) {
        this(maxAgentRadius, 100);
    }

    public CrowdConfig(float maxAgentRadius, int maxAgents) {
        this.maxAgentRadius = maxAgentRadius;
        this.maxAgents = maxAgents;
    }

}
