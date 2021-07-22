package com.jme3.recast4j.Detour.Crowd;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.BetterDefaultQueryFilter;
import com.jme3.recast4j.Detour.Crowd.Impl.CrowdManagerAppState;
import com.jme3.recast4j.Detour.DetourUtils;
import com.jme3.scene.Spatial;
import org.recast4j.detour.*;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.debug.CrowdAgentDebugInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.function.IntFunction;

/**
 * TODO: Javadoc
 * Important to know: Filters have to be done using the queryFilterFactory. If none is specified, the first filter
 * (index 0) will be the {@link BetterDefaultQueryFilter} and all others will be filled with {@link DefaultQueryFilter}s
 */
public class Crowd extends org.recast4j.detour.crowd.Crowd {
    private static final Logger log = LoggerFactory.getLogger(Crowd.class);

    protected boolean debug;
    protected CrowdAgentDebugInfo debugInfo;
    protected MovementApplicationType applicationType;
    protected ApplyFunction applyFunction;
    protected Spatial[] spatialMap;
    protected TargetProximityDetector proximityDetector;
    protected FormationHandler formationHandler;
    protected NavMeshQuery m_navquery;
    protected org.recast4j.detour.crowd.CrowdAgent[] m_agents;
    protected Vector3f[] formationTargets;

    public Crowd(MovementApplicationType applicationType, int maxAgents, float maxAgentRadius, NavMesh nav)
            throws InstantiationException {
        this(applicationType, maxAgents, maxAgentRadius, nav,
            i -> (i == 0 ? new BetterDefaultQueryFilter() : new DefaultQueryFilter())
        );
    }

    public Crowd(MovementApplicationType applicationType, int maxAgents, float maxAgentRadius, NavMesh nav,
        IntFunction<QueryFilter> queryFilterFactory) throws InstantiationException {
        super(maxAgents, maxAgentRadius, nav, queryFilterFactory);
        try {
            Field f = getClass().getSuperclass().getDeclaredField("m_agents");
            f.setAccessible(true);
            m_agents = (org.recast4j.detour.crowd.CrowdAgent[]) f.get(this);

            //@FIXME: Not very GC friendly, but avoids code duplication
            for (int i = 0; i < maxAgents; ++i) {
                m_agents[i] = new CrowdAgent(i, this);
                m_agents[i].active = false;
            }

            this.applicationType = applicationType;
            spatialMap = new Spatial[maxAgents];
            proximityDetector = new SimpleTargetProximityDetector(1f);
            formationHandler = new CircleFormationHandler(maxAgents, this, 1f);
            formationTargets = new Vector3f[maxAgents];

            f = getClass().getSuperclass().getDeclaredField("m_navquery");
            f.setAccessible(true);
            m_navquery = (NavMeshQuery) f.get(this);
        } catch (NoSuchFieldException | IllegalAccessException iae) {
            throw new InstantiationException("Internal Problem: Failed to reflectively access recast4j's Crowd fields." +
                    " This could be a Version mismatch?!\n" + iae.toString());
        }
    }

    public void setApplicationType(MovementApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public void setCustomApplyFunction(ApplyFunction applyFunction) {
        this.applyFunction = applyFunction;
    }

    public MovementApplicationType getApplicationType() {
        return applicationType;
    }

    public FormationHandler getFormationHandler() {
        return formationHandler;
    }

    /**
     * Sets the Handler which will move the agents into formation when they are close to the target.<br>
     * Passing null will disable formation.
     * @param formationHandler The handler to use
     */
    public void setFormationHandler(FormationHandler formationHandler) {
        this.formationHandler = formationHandler;
    }

    public void update(float deltaTime) {
        if (debug) {
            debugInfo = new CrowdAgentDebugInfo(); // Clear.
            update(deltaTime, debugInfo);
        } else {
            update(deltaTime, null);
        }
    }

    @Override
    public CrowdAgent getAgent(int idx) {
        CrowdAgent ca = (CrowdAgent)super.getAgent(idx);
        if (ca == null) {
            throw new IndexOutOfBoundsException("Invalid Index");
        }

        return ca;
    }

    public CrowdAgent createAgent(Vector3f pos, CrowdAgentParams params) {
        int idx = addAgent(DetourUtils.toFloatArray(pos), params);
        if (idx == -1) {
            throw new IndexOutOfBoundsException("This crowd doesn't have a free slot anymore.");
        }
        return (CrowdAgent)super.getAgent(idx);
    }

    /**
     * Call this method to update the internal data storage of spatials.
     * This is required for some {@link MovementApplicationType}s.
     * @param agent The Agent
     * @param spatial The Agent's Spatial
     */
    public void setSpatialForAgent(org.recast4j.detour.crowd.CrowdAgent agent, Spatial spatial) {
        spatialMap[agent.idx] = spatial;
    }

    /**
     * Remove the Agent from this Crowd (Convenience Wrapper around {@link #removeAgent(int)})
     * @param agent The Agent to remove from the crowd
     */
    public void removeAgent(org.recast4j.detour.crowd.CrowdAgent agent) {
        if (agent.idx != -1) {
            removeAgent(agent.idx);
        }
    }

    /**
     * This method is used to prepare the correct state before update() is called. <br />
     * It should be run in the main thread, as that is what
     * {@link CrowdManagerAppState } would do.
     */
    public void preUpdate(float deltaTime) {
        getActiveAgents().stream().filter(ca -> (ca instanceof CrowdAgent && ((CrowdAgent) ca).isGhost()))
            .forEach(ca -> {
                /* See comment in applyMovement with BETTER_CHARACTER_CONTROL): DetourCrowd advices against it, but it
                 * might work here, as we don't need Detour's calculations for this Agent at all. Actually it shouldn't
                 * even move.
                 */
                Vector3f oldVec = DetourUtils.toVector3f(ca.npos);
                Vector3f newVec = spatialMap[ca.idx].getWorldTranslation();
                Vector3f vel = newVec.subtract(oldVec).divide(deltaTime);

                DetourUtils.toFloatArray(ca.npos, newVec);
                DetourUtils.toFloatArray(ca.vel, vel);
            }
        );
    }

    /**
     * This method is called by the CrowdManager to move the agents on the screen.
     */
    protected void applyMovements() {
        getActiveAgents().stream().filter(this::isMoving)
            .forEach(ca -> applyMovement(ca, DetourUtils.toVector3f(ca.npos),
                            DetourUtils.toVector3f(ca.vel)));
    }

    protected void applyMovement(org.recast4j.detour.crowd.CrowdAgent crowdAgent, Vector3f newPos, Vector3f velocity) {
        float vel = velocity == null ? 0f : velocity.length();

        log.debug("crowdAgent i={}, newPos={}, velocity={}[{}]", crowdAgent.idx, newPos, velocity, vel);

        switch (applicationType) {
            case NONE:
                break;

            case CUSTOM:
                applyFunction.applyMovement(crowdAgent, newPos, velocity);
                break;

            case DIRECT:
                if (vel > 0.01f) {
                    Quaternion rotation = new Quaternion();
                    rotation.lookAt(velocity.normalize(), Vector3f.UNIT_Y);
                    spatialMap[crowdAgent.idx].setLocalTranslation(newPos);
                    spatialMap[crowdAgent.idx].setLocalRotation(rotation);
                }
                break;

            case BETTER_CHARACTER_CONTROL:
                BetterCharacterControl bcc = spatialMap[crowdAgent.idx].getControl(BetterCharacterControl.class);

                if (crowdAgent.state == org.recast4j.detour.crowd.CrowdAgent.CrowdAgentState.DT_CROWDAGENT_STATE_OFFMESH) {
                    bcc.warp(newPos); // Teleport through the air without any feedback.
                    return; // Initial "return": Don't even bother with formation or other logic.
                }

                if (velocity != null) {
                    bcc.setWalkDirection(velocity);
                    bcc.setViewDirection(velocity.normalize());
                } else {
                    bcc.setWalkDirection(Vector3f.ZERO);
                }

                /* Note: Unfortunately BetterCharacterControl does not expose getPhysicsLocation but it's tied to the
                 * SceneGraph Position
                 */
                if (SimpleTargetProximityDetector.euclideanDistanceSquared(newPos,
                        spatialMap[crowdAgent.idx].getWorldTranslation()) > 0.4f * 0.4f) {
                    /* Note: This should never occur but when collisions happen, they happen. Let's hope we can get away
                     * with that even though DtCrowd documentation explicitly states that one should not move agents
                     * constantly (okay, we only do it in rare cases, but still). Bugs could appear when some internal
                     * state is voided. The most clean solution would be removeAgent(), addAgent() but that has some
                     * overhead as well as possibly messing with the index, on which some 3rd-party code might rely on.
                      */
                    log.debug("Resetting Agent because of physics drift");
                    DetourUtils.toFloatArray(crowdAgent.npos, spatialMap[crowdAgent.idx].getWorldTranslation());
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown Application Type");
        }

        if (velocity == null) {
            return; // Bugfix: Don't get caught in an endless loop when this code already triggered a movement-stop
        }

        // If we aren't currently forming.
        if (formationTargets[crowdAgent.idx] == null) {
            // @TODO: Move Proximity Detector into Crowd Agent to allow for custom detectors?
            // Collides with Formation Handlers though.
            if (proximityDetector.isInTargetProximity(crowdAgent, newPos,
                    DetourUtils.toVector3f(crowdAgent.targetPos))) {
                // Handle Crowd Agent in proximity.
                if (formationHandler != null) {
                    resetMoveTarget(crowdAgent.idx); // Make him stop moving.
                    formationTargets[crowdAgent.idx] = formationHandler.moveIntoFormation(crowdAgent);
                    // It's up to moveIntoFormation to make the agent move, we could however also design the API so we just
                    // use the return value for this. Then it would be less prone to user error. On the other hand the
                    // "do" something pattern is more implicative than "getFormationPosition"
                } else {
                    resetMoveTarget(crowdAgent.idx); // Make him stop moving.
                }

            } else {
                log.debug("Crowd Agent i={} not in proximity of {} (Proximity Detection)", crowdAgent.idx, DetourUtils.toVector3f(crowdAgent.targetPos));
                // @TODO: Stuck detection?
            }
        } else {
            // alternatively let crowd handle that.
            if (vel < 0.01f) { // This happens when the formationHandler is too picky about the position and crowd stops moving.
                resetMoveTarget(crowdAgent.idx); // does formationTargets[crowdAgent.idx] = null; for us
                log.debug("Crowd has decided to stop here..");
            } else if (vel < 0.1f && formationHandler.isInFormationProximity(newPos, formationTargets[crowdAgent.idx])) {
                resetMoveTarget(crowdAgent.idx); // does formationTargets[crowdAgent.idx] = null; for us
                log.debug("CrowdAgent i={} reached target", crowdAgent.idx);
            } else {
                log.debug("CrowdAgent i={} is forming!", crowdAgent.idx); /*+ SimpleTargetProximityDetector.euclideanDistanceSquared(newPos,
                        formationTargets[crowdAgent.idx]) + " > " + 0.1f * 0.1f);*/
            }
        }
    }

    /**
     * Makes the whole Crowd move to a target. Know that you can also move individual agents.
     * @param to The Move Target
     * @param polyRef The Polygon to which the target belongs
     * @return Whether all agents could be scheduled to approach the target
     * @deprecated Will be removed because specifying the polyRef is undesired (as crashes happen
     * when this value is wrong (e.g. not taken the Filters into account)).
     */
    @Deprecated
    protected boolean requestMoveToTarget(Vector3f to, long polyRef) {
        if (polyRef == 0 || to == null) {
            throw new IllegalArgumentException("Invalid Target (" + to + ", " + polyRef + ")");
        }

        if (formationHandler != null) {
            formationHandler.setTargetPosition(to);
        }

        // Unfortunately ag.setTarget is not an exposed API, maybe we'll write a dispatcher class if that bugs me too much
        // Why? That way we could throw Exceptions when the index is wrong (IndexOutOfBoundsEx)
        return getActiveAgents().stream()
                .allMatch(ca -> requestMoveTarget(ca.idx, polyRef, DetourUtils.toFloatArray(to)));

    }

    /**
     * Makes the whole Crowd move to a target. Know that you can also move individual agents.
     * @param to The Move Target
     * @return Whether all agents could be scheduled to approach the target
     * @see #requestMoveToTarget(org.recast4j.detour.crowd.CrowdAgent, Vector3f)
     */
    public boolean requestMoveToTarget(Vector3f to) {
        if (formationHandler != null) {
            formationHandler.setTargetPosition(to);
        }
        return getActiveAgents().stream()
                .filter(ca -> (!(ca instanceof CrowdAgent) || !((CrowdAgent) ca).isGhost()))
                .allMatch(ca -> requestMoveToTarget(ca, to));
        // if all were successful, return true, else return false.
    }

    /**
     * Moves a specified Agent to a Location.<br />
     * This code implicitly searches for the correct polygon with a constant tolerance, in most cases you should prefer
     * to determine the poly ref manually with domain specific knowledge.
     * @see #requestMoveToTarget(org.recast4j.detour.crowd.CrowdAgent, long, Vector3f)
     * @param crowdAgent the agent to move
     * @param to where the agent shall move to
     * @return whether this operation was successful
     */
    public boolean requestMoveToTarget(org.recast4j.detour.crowd.CrowdAgent crowdAgent, Vector3f to) {
        Result<FindNearestPolyResult> res = m_navquery.findNearestPoly(DetourUtils.toFloatArray(to), getQueryExtents(),
                getFilter(crowdAgent.params.queryFilterType));

        if (res.status.isSuccess() && res.result.getNearestRef() != -1) {
            return requestMoveTarget(crowdAgent.idx, res.result.getNearestRef(), DetourUtils.toFloatArray(to));
        } else {
            return false;
        }
    }

    /**
     * Moves a specified Agent to a Location.
     * @param crowdAgent the agent to move
     * @param polyRef The Polygon where the position resides
     * @param to where the agent shall move to
     * @return whether this operation was successful
     * @deprecated Use non-polRef instead
     */
    @Deprecated
    protected boolean requestMoveToTarget(org.recast4j.detour.crowd.CrowdAgent crowdAgent, long polyRef, Vector3f to) {
        return requestMoveTarget(crowdAgent.idx, polyRef, DetourUtils.toFloatArray(to));
    }

    @Override
    public boolean requestMoveTarget(int idx, long ref, float[] pos) {
        formationTargets[idx] = null; // Reset formation state.
        return super.requestMoveTarget(idx, ref, pos);
    }

    @Override
    public boolean resetMoveTarget(int idx) {
        formationTargets[idx] = null;
        CrowdAgent agnt = getAgent(idx);
        applyMovement(agnt, agnt.getPosition(), null);
        return super.resetMoveTarget(idx);
    }

    public boolean resetMoveTarget(CrowdAgent agent) {
        assert agent.crowd == this;
        applyMovement(agent, agent.getPosition(), null);
        return resetMoveTarget(agent.idx);
    }

    /**
     * When the Agent is ACTIVE and moving (has a valid target set).
     * @param crowdAgent The agent to query
     * @return If the agent is moving
     * @deprecated See the Methods under {@link CrowdAgent}
     */
    @Deprecated
    public boolean isMoving(org.recast4j.detour.crowd.CrowdAgent crowdAgent) {
        return crowdAgent.active && crowdAgent.targetState == CrowdAgent.MoveRequestState.DT_CROWDAGENT_TARGET_VALID;
    }

    /**
     * When the Agent is ACTIVE and has no target (this is not the same as !{@link #isMoving(org.recast4j.detour.crowd.CrowdAgent)}).
     * @param crowdAgent The agent to query
     * @return If the agent has no target
     * @deprecated See the Methods under {@link CrowdAgent}
     */
    @Deprecated
    public boolean hasNoTarget(org.recast4j.detour.crowd.CrowdAgent crowdAgent) {
        return crowdAgent.active && crowdAgent.targetState == CrowdAgent.MoveRequestState.DT_CROWDAGENT_TARGET_NONE;
    }

    /**
     * When the Agent is ACTIVE and moving into a formation (which means he is close enough to his target, by the means
     * of {@link TargetProximityDetector#isInTargetProximity(org.recast4j.detour.crowd.CrowdAgent, Vector3f, Vector3f)}
     * @param crowdAgent The Agent to query
     * @return If the Agent is forming
     */
    public boolean isForming(org.recast4j.detour.crowd.CrowdAgent crowdAgent) {
        return crowdAgent.active && formationTargets[crowdAgent.idx] != null;
    }
}
