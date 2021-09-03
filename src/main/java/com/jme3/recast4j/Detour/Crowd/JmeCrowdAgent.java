package com.jme3.recast4j.Detour.Crowd;

import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgent.CrowdAgentState;
import org.recast4j.detour.crowd.CrowdAgent.MoveRequestState;

import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.DetourUtils;

/**
 * 
 * @author capdevon
 */
public class JmeCrowdAgent {

	public static boolean isMoving(CrowdAgent ca) {
		return ca.active && (ca.state == CrowdAgentState.DT_CROWDAGENT_STATE_OFFMESH
				|| ca.state == CrowdAgentState.DT_CROWDAGENT_STATE_WALKING);
	}

	public static boolean hasNoInvalidTarget(CrowdAgent ca) {
		return ca.active && ca.targetState != MoveRequestState.DT_CROWDAGENT_TARGET_FAILED;
	}

	public static boolean hasValidTarget(CrowdAgent ca) {
		return ca.active && ca.targetState == CrowdAgent.MoveRequestState.DT_CROWDAGENT_TARGET_VALID;
	}

	public static boolean hasNoTarget(CrowdAgent ca) {
		return ca.active && ca.targetState == CrowdAgent.MoveRequestState.DT_CROWDAGENT_TARGET_NONE;
	}

	/**
	 * Gets the position of this crowd agent as used in the simulation.
	 * 
	 * @return the crowd agent position
	 */
	public static Vector3f getPosition(CrowdAgent ca) {
		return DetourUtils.toVector3f(ca.npos);
	}
}
