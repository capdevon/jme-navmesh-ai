package com.jme3.recast4j.Detour.Crowd;

import com.jme3.math.Vector3f;
import org.recast4j.detour.crowd.CrowdAgent;

public class SimpleTargetProximityDetector implements TargetProximityDetector {
    protected float distanceThreshold;
    private float distanceSquared;

    public SimpleTargetProximityDetector(float distanceThreshold) {
        setDistanceThreshold(distanceThreshold);
    }

    @Override
    public boolean isInTargetProximity(CrowdAgent agent, Vector3f agentPos, Vector3f targetPos) {
        return targetPos.subtract(agentPos).lengthSquared() <= distanceSquared;
    }

    public float getDistanceThreshold() {
        return distanceThreshold;
    }

    public void setDistanceThreshold(float distanceThreshold) {
        this.distanceThreshold = distanceThreshold;
        this.distanceSquared = distanceThreshold * distanceThreshold;
    }

    public static float euclideanDistanceSquared(Vector3f agentPos, Vector3f targetPos) {
        return targetPos.subtract(agentPos).lengthSquared();
    }
}
