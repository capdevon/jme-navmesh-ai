package com.jme3.recast4j.Detour.Crowd;

import com.jme3.math.Vector3f;
import org.recast4j.detour.crowd.CrowdAgent;

/**
 * An interface to detect when an agent is in the acceptable range of a target.
 * <br />
 * DetourCrowd uses Steering Behaviors and as such might not perfectly navigate
 * on the waypoints (especially when doing collision avoidance). That's why we
 * need a way to determine when the agent has reached it's target to make him
 * stop moving (forcefully trying to approach the target and pushing away other
 * agents in the crowd).
 */
public interface Proximity {

	boolean isInTargetProximity(CrowdAgent agent, Vector3f agentPos, Vector3f targetPos);
}
