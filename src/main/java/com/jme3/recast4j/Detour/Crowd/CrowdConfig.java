package com.jme3.recast4j.Detour.Crowd;

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
