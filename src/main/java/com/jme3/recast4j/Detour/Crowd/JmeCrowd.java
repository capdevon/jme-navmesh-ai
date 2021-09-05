package com.jme3.recast4j.Detour.Crowd;

import java.util.function.IntFunction;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.crowd.Crowd;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgent.CrowdAgentState;
import org.recast4j.detour.crowd.CrowdAgent.MoveRequestState;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.debug.CrowdAgentDebugInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.DetourUtils;
import com.jme3.scene.Spatial;

/**
 * 
 * @author capdevon
 */
public class JmeCrowd extends Crowd {

	private static final Logger log = LoggerFactory.getLogger(JmeCrowd.class);

	protected MoveFunction moveFunction;
    protected MovementType movementType = MovementType.SPATIAL;
    protected Proximity proximityDetector = new TargetProximity(1f);

    protected NavMeshQuery m_navQuery;
    protected CrowdAgentDebugInfo m_agentDebug = new CrowdAgentDebugInfo();
    protected boolean debug = false;
    
    /**
     * 
     * @param config
     * @param nav
     */
    public JmeCrowd(CrowdConfig config, NavMesh nav) {
        this(config, nav, i -> new DefaultQueryFilter());
    }

    /**
     * 
     * @param config
     * @param nav
     * @param queryFilterFactory
     */
    public JmeCrowd(CrowdConfig config, NavMesh nav, IntFunction<QueryFilter> queryFilterFactory) {
    	this(100, config.maxAgentRadius, nav, queryFilterFactory);
    }
    
    @Deprecated
    public JmeCrowd(int maxAgents, float maxAgentRadius, NavMesh nav) {
        super(maxAgents, maxAgentRadius, nav, i -> new DefaultQueryFilter());
        this.m_navQuery = new NavMeshQuery(nav); //TODO:
    }

    @Deprecated
    public JmeCrowd(int maxAgents, float maxAgentRadius, NavMesh nav, IntFunction<QueryFilter> queryFilterFactory) {
    	super(maxAgents, maxAgentRadius, nav, queryFilterFactory);
    	this.m_navQuery = new NavMeshQuery(nav); //TODO:
    }
    
    public CrowdAgent createAgent(Vector3f pos, CrowdAgentParams params) {
        int idx = addAgent(DetourUtils.toFloatArray(pos), params);
        if (idx != -1) {
        	return getAgent(idx);
        }
        return null;
    }

    public void setMoveFunction(MoveFunction moveFunction) {
        this.moveFunction = moveFunction;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void updateTick(float tpf) {
    	update(tpf);
    	applyMovements();
    }

    protected void update(float deltaTime) {
        if (debug) {
            update(deltaTime, m_agentDebug);
        } else {
            update(deltaTime, null);
        }
    }

    protected void applyMovements() {
    	for (CrowdAgent ca : getActiveAgents()) {
    		if (hasValidTarget(ca)) {
    			applyMovement(ca, DetourUtils.toVector3f(ca.npos), DetourUtils.toVector3f(ca.vel));
    		}
    	}
    }

    /**
     * 
     * @param agent
     * @param newPos
     * @param velocity
     */
    protected void applyMovement(CrowdAgent agent, Vector3f newPos, Vector3f velocity) {

        float vel = (velocity == null) ? 0f : velocity.length();
        log.debug("crowdAgent i={}, newPos={}, velocity={}[{}]", agent.idx, newPos, velocity, vel);

        Spatial sp = ((Spatial) agent.params.userData);
        
        switch (movementType) {
            case NONE:
                break;

            case CUSTOM:
                moveFunction.applyMovement(agent, newPos, velocity);
                break;

            case SPATIAL:
                if (vel > 0.01f) {
                    Quaternion rotation = new Quaternion();
                    rotation.lookAt(velocity.normalize(), Vector3f.UNIT_Y);
                    sp.setLocalTranslation(newPos);
                    sp.setLocalRotation(rotation);
                }
                break;

            case PHYSICS_CHARACTER:
                BetterCharacterControl bcc = sp.getControl(BetterCharacterControl.class);

                if (hasOffMeshState(agent)) {
                    // Teleport through the air without any feedback.
                    bcc.warp(newPos);
                    return;
                }

                if (velocity != null) {
                    bcc.setWalkDirection(velocity);
                    bcc.setViewDirection(velocity.normalize());
                } else {
                    bcc.setWalkDirection(Vector3f.ZERO);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown MovementType: " + movementType);
        }

        if (velocity == null) {
            // Bugfix: Don't get caught in an endless loop when this code already triggered a movement-stop
            return;
        }

        if (proximityDetector.isInTargetProximity(agent, newPos, DetourUtils.toVector3f(agent.targetPos))) {
            resetMoveTarget(agent); // Make him stop moving.
        } else {
            log.debug("Crowd Agent i={} not in proximity of {} (Proximity Detection)", agent.idx, DetourUtils.toVector3f(agent.targetPos));
        }
    }

    /**
     * Makes the whole Crowd move to a target. Know that you can also move
     * individual agents.
     * 
     * @param to The Move Target
     * @return Whether all agents could be scheduled to approach the target
     */
    public boolean setMoveTarget(Vector3f to) {
        // if all were successful, return true, else return false.
        return getActiveAgents().stream().allMatch(ca -> setMoveTarget(ca, to));
    }

	/**
	 * Moves a specified Agent to a Location.<br />
	 * This code implicitly searches for the correct polygon with a constant
	 * tolerance, in most cases you should prefer to determine the poly ref manually
	 * with domain specific knowledge.
	 * 
	 * @param agent the agent to move
	 * @param to    where the agent shall move to
	 * @return whether this operation was successful
	 */
    protected boolean setMoveTarget(CrowdAgent agent, Vector3f to) {
    	
    	QueryFilter filter = getFilter(agent.params.queryFilterType);
        float[] halfExtents = getQueryExtents();
        float[] p = DetourUtils.toFloatArray(to);
        
        FindNearestPolyResult nearestPoly = m_navQuery.findNearestPoly(p, halfExtents, filter).result;

        return requestMoveTarget(agent.idx, nearestPoly.getNearestRef(), nearestPoly.getNearestPos());
    }

    public boolean resetMoveTarget(CrowdAgent agent) {
        applyMovement(agent, DetourUtils.toVector3f(agent.npos), null);
        return resetMoveTarget(agent.idx);
    }

    public boolean hasWalkingState(CrowdAgent agent) {
    	return agent.active && agent.state == CrowdAgentState.DT_CROWDAGENT_STATE_WALKING;
    }
    
    public boolean hasOffMeshState(CrowdAgent agent) {
        return agent.active && agent.state == CrowdAgentState.DT_CROWDAGENT_STATE_OFFMESH;
    }

    public boolean hasValidTarget(CrowdAgent agent) {
        return agent.active && agent.targetState == MoveRequestState.DT_CROWDAGENT_TARGET_VALID;
    }

    public boolean hasNoTarget(CrowdAgent agent) {
        return agent.active && agent.targetState == MoveRequestState.DT_CROWDAGENT_TARGET_NONE;
    }
    
}
