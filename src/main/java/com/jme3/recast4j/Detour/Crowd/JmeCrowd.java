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

    private Map<Integer, Spatial> characterMap;
    private NavMeshQuery m_navQuery;
    private CrowdAgentDebugInfo m_agentDebug = new CrowdAgentDebugInfo();
    private boolean debugEnabled = false;

    private MoveFunction moveFunction;
    private MovementType movementType = MovementType.SPATIAL;
    private Proximity proximity = new TargetProximity(1f);
    
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
        characterMap = new ConcurrentHashMap<>(maxAgents);
        m_navQuery = new NavMeshQuery(nav);
    }

    @Deprecated
    public JmeCrowd(int maxAgents, float maxAgentRadius, NavMesh nav, IntFunction<QueryFilter> queryFilterFactory) {
        super(maxAgents, maxAgentRadius, nav, queryFilterFactory);
        characterMap = new ConcurrentHashMap<>(maxAgents);
        m_navQuery = new NavMeshQuery(nav);
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

    public void deleteAgent(CrowdAgent agent) {
        characterMap.remove(agent.idx);
        removeAgent(agent.idx);
    }
    
    public void hilightAgent(CrowdAgent agent) {
        m_agentDebug.idx = agent.idx;
        debugEnabled = (agent != null);
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
            Vector3f newVec = characterMap.get(ca.idx).getWorldTranslation();
            DetourUtils.toFloatArray(ca.npos, newVec);
        }
    }

    protected void updateTick(float deltaTime) {
        if (debugEnabled) {
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
                resetAgentTarget(agent);
            }
        }
    }

    /**
     * Resets any request for the specified agent.
     * 
     * @param agent The agent
     * @return True if the request was successfully reseted.
     */
    public boolean resetAgentTarget(CrowdAgent agent) {
        applyMovement(agent, DetourUtils.toVector3f(agent.npos), null);
        return resetMoveTarget(agent.idx);
    }

    /**
     * Makes the whole Crowd move to a target.
     * 
     * @param targetPos The Move Target
     * @return Whether all agents could be scheduled to approach the target
     */
    public boolean setMoveTarget(Vector3f targetPos) {
        // if all were successful, return true, else return false.
        return getActiveAgents().stream().allMatch(ag -> setAgentTarget(ag, targetPos));
    }

    /**
     * Submits a new move request for the specified agent.<br/>
     * This method is used when a new target is set.<br/>
     * The position will be constrained to the surface of the navigation mesh.<br/>
     * The request will be processed during the next #update().
     * 
     * @param agent     The agent
     * @param targetPos Where the agent shall move to
     * @return True if the request was successfully submitted.
     */
    public boolean setAgentTarget(CrowdAgent agent, Vector3f targetPos) {

        QueryFilter filter = getFilter(agent.params.queryFilterType);
        float[] halfExtents = getQueryExtents();
        float[] pos = DetourUtils.toFloatArray(targetPos);

        // Find nearest point on navmesh and set move request to that location.
        FindNearestPolyResult nearestPoly = m_navQuery.findNearestPoly(pos, halfExtents, filter).result;
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
