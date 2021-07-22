package com.jme3.recast4j.Detour.Crowd;

import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.DetourUtils;

/**
 * This is the jme3-recast4j Wrapper around recast4j's CrowdAgent to provide additional behavior.<br />
 * Ghost: This Crowd Agent wont participate in moving but will block other agents like a "ghost".<br />
 * Use this for player characters and others.
 */
public class CrowdAgent extends org.recast4j.detour.crowd.CrowdAgent {
    protected boolean isGhost = false;
    protected Crowd crowd;

    public CrowdAgent(int idx, Crowd crowd) {
        super(idx);
        this.crowd = crowd;
    }

    public boolean isMoving() {
        return active &&
                (state == CrowdAgentState.DT_CROWDAGENT_STATE_OFFMESH ||
                 state == CrowdAgentState.DT_CROWDAGENT_STATE_WALKING);
    }

    public boolean hasNoInvalidTarget() {
        return active && targetState != MoveRequestState.DT_CROWDAGENT_TARGET_FAILED;
    }

    public boolean hasValidTarget() {
        return active && targetState == CrowdAgent.MoveRequestState.DT_CROWDAGENT_TARGET_VALID;
    }

    public boolean hasNoTarget() {
        return active && targetState == CrowdAgent.MoveRequestState.DT_CROWDAGENT_TARGET_NONE;
    }

    public boolean isGhost() {
        return isGhost;
    }

    public void setGhost(boolean ghost) {
        isGhost = ghost;
    }

    public Crowd getCrowd() {
        return crowd;
    }


    /**
     * Sets the position of this crowd agent as used in the simulation.<br />
     * This <i>can</i> be used as feedback e.g. from the physics, but be warned that doing this in the mid of a
     * pathfinding process <b>could</b> break some internal state, as the upstream docs would suggest re-adding the
     * agent to the crowd.<br />
     * A <b>good</b> use for this method however is to re-place the agent before
     * {@link Crowd#requestMoveToTarget(org.recast4j.detour.crowd.CrowdAgent, Vector3f)} is called, for example when
     * the visual representation of the agent has been moved externally.
     * @param position The new position
     * @see #getPosition()
     */
    public void setPosition(Vector3f position) {
        DetourUtils.toFloatArray(npos, position);
    }

    /**
     * Gets the position of this crowd agent as used in the simulation.
     * @return the crowd agent position
     * @see #setPosition(Vector3f)
     */
    public Vector3f getPosition() {
        return DetourUtils.toVector3f(npos);
    }
}
