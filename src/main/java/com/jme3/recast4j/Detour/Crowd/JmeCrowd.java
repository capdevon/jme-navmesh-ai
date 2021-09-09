package com.jme3.recast4j.Detour.Crowd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final Logger logger = LoggerFactory.getLogger(JmeCrowd.class);

    private final Map<Integer, Spatial> characterMap = new ConcurrentHashMap<>(64);
    
    protected MoveFunction moveFunction;
    protected MovementType movementType = MovementType.SPATIAL;
    protected Proximity proximity = new TargetProximity(1f);

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

    public CrowdAgent createAgent(Spatial model, CrowdAgentParams params) {
        float[] pos = DetourUtils.toFloatArray(model.getWorldTranslation());
        int idx = addAgent(pos, params);
        if (idx != -1) {
            characterMap.put(idx, model);
            return getAgent(idx);
        }
        return null;
    }

    public void removeAgent(CrowdAgent agent) {
        removeAgent(agent.idx);
        characterMap.remove(agent.idx);
    }
    
    public void setProximityDetector(Proximity p) {
        this.proximity = p;
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

    public void update(float tpf) {
        preUpdateTick(tpf);
        updateTick(tpf);
        applyMovements();
    }
    
    protected void preUpdateTick(float deltaTime) {
        for (CrowdAgent ca : getActiveAgents()) {
            //Vector3f oldVec = DetourUtils.toVector3f(ca.npos);
            Vector3f newVec = characterMap.get(ca.idx).getWorldTranslation();
            //Vector3f vel = newVec.subtract(oldVec).divide(deltaTime);
            DetourUtils.toFloatArray(ca.npos, newVec);
            //DetourUtils.toFloatArray(ca.vel, vel);
        }
    }

    protected void updateTick(float deltaTime) {
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

        float xSpeed = (velocity == null) ? 0f : velocity.length();
        Spatial sp = characterMap.get(agent.idx);
        logger.debug("crowdAgent={}, newPos={}, velocity={}[{}]", sp, newPos, velocity, xSpeed);

        switch (movementType) {
            case NONE:
                break;

            case CUSTOM:
                moveFunction.applyMovement(agent, newPos, velocity);
                break;

            case SPATIAL:
                if (xSpeed > 0.01f) {
                    Quaternion rotation = new Quaternion();
                    rotation.lookAt(velocity.normalize(), Vector3f.UNIT_Y);
                    sp.setLocalRotation(rotation);
                    sp.setLocalTranslation(newPos);
                }
                break;

            case PHYSICS_CHARACTER:
                BetterCharacterControl bcc = sp.getControl(BetterCharacterControl.class);
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

        if (velocity != null) {
            Vector3f targetPos = DetourUtils.toVector3f(agent.targetPos);

            if (proximity.isInTargetProximity(agent, newPos, targetPos)) {
                // stop moving.
                logger.info("stop moving dist={} velocity={}", newPos.distance(targetPos), xSpeed);
                resetMoveTarget(agent);
            }
        }
    }

    public boolean resetMoveTarget(CrowdAgent agent) {
        applyMovement(agent, DetourUtils.toVector3f(agent.npos), null);
        return resetMoveTarget(agent.idx);
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
        return getActiveAgents().stream().allMatch(ag -> setMoveTarget(ag, to));
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
