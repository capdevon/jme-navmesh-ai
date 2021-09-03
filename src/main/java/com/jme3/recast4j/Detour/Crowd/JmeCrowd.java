package com.jme3.recast4j.Detour.Crowd;

import java.util.List;
import java.util.function.IntFunction;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.Result;
import org.recast4j.detour.crowd.Crowd;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.ObstacleAvoidanceQuery.ObstacleAvoidanceParams;
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
public class JmeCrowd {

	private static final Logger log = LoggerFactory.getLogger(JmeCrowd.class);

	protected MovementType movementType = MovementType.DIRECT;
	protected ApplyFunction applyFunction;
	protected Spatial[] spatialMap;
	protected Proximity proximityDetector;
	
	protected NavMeshQuery m_navQuery;
	protected Crowd dtCrowd;
	protected CrowdAgentDebugInfo m_agentDebug = new CrowdAgentDebugInfo();
	protected boolean debug = false;

	public JmeCrowd(int maxAgents, float maxAgentRadius, NavMesh nav) {
		this(maxAgents, maxAgentRadius, nav, __ -> new DefaultQueryFilter());
	}

	public JmeCrowd(int maxAgents, float maxAgentRadius, NavMesh nav, IntFunction<QueryFilter> queryFilterFactory) {

		this.dtCrowd = new Crowd(maxAgents, maxAgentRadius, nav, queryFilterFactory);
		this.m_navQuery = new NavMeshQuery(nav);
		
		spatialMap = new Spatial[maxAgents];
		proximityDetector = new TargetProximity(1f);
	}
	
	public ObstacleAvoidanceParams getObstacleAvoidanceParams(int idx) {
		return dtCrowd.getObstacleAvoidanceParams(idx);
	}
	
	public void setObstacleAvoidanceParams(int idx, ObstacleAvoidanceParams params) {
		dtCrowd.setObstacleAvoidanceParams(idx, params);
	}

	public void setCustomApplyFunction(ApplyFunction applyFunction) {
		this.applyFunction = applyFunction;
	}
	
	public void setMovementType(MovementType movementType) {
		this.movementType = movementType;
	}

	public MovementType getMovementType() {
		return movementType;
	}

	public CrowdAgent getAgent(int idx) {
		CrowdAgent ca = dtCrowd.getAgent(idx);
		if (ca == null) {
			throw new IndexOutOfBoundsException("Invalid Index");
		}
		return ca;
	}

	public CrowdAgent createAgent(Vector3f pos, CrowdAgentParams params) {
		int idx = dtCrowd.addAgent(DetourUtils.toFloatArray(pos), params);
		if (idx == -1) {
			throw new IndexOutOfBoundsException("This crowd doesn't have a free slot anymore.");
		}
		return dtCrowd.getAgent(idx);
	}

	/**
	 * Call this method to update the internal data storage of spatials. This is
	 * required for some {@link MovementType}s.
	 * 
	 * @param agent   The Agent
	 * @param spatial The Agent's Spatial
	 */
	public void setSpatialForAgent(CrowdAgent agent, Spatial spatial) {
		spatialMap[agent.idx] = spatial;
	}

	/**
	 * Remove the Agent from this Crowd
	 * 
	 * @param agent The Agent to remove from the crowd
	 */
	public void removeAgent(CrowdAgent agent) {
		dtCrowd.removeAgent(agent.idx);
	}

	/**
	 * This method is used to prepare the correct state before update() is called.
	 * <br />
	 * It should be run in the main thread, as that is what
	 * {@link CrowdManagerAppState } would do.
	 */
	public void preUpdate(float deltaTime) {
//		dtCrowd.getActiveAgents().forEach(ca -> {
//			/*
//			 * See comment in applyMovement with BETTER_CHARACTER_CONTROL): DetourCrowd
//			 * advices against it, but it might work here, as we don't need Detour's
//			 * calculations for this Agent at all. Actually it shouldn't even move.
//			 */
//			Vector3f oldVec = DetourUtils.toVector3f(ca.npos);
//			Vector3f newVec = spatialMap[ca.idx].getWorldTranslation();
//			Vector3f vel = newVec.subtract(oldVec).divide(deltaTime);
//
//			DetourUtils.toFloatArray(ca.npos, newVec);
//			DetourUtils.toFloatArray(ca.vel, vel);
//		});
	}
	
	public void update(float deltaTime) {
		if (debug) {
			dtCrowd.update(deltaTime, m_agentDebug);
		} else {
			dtCrowd.update(deltaTime, null);
		}
	}

	/**
	 * This method is called by the CrowdManager to move the agents on the screen.
	 */
	protected void applyMovements() {
		dtCrowd.getActiveAgents().stream().filter(this::hasValidTarget)
				.forEach(ca -> applyMovement(ca, DetourUtils.toVector3f(ca.npos), DetourUtils.toVector3f(ca.vel)));
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

		switch (movementType) {
		case NONE:
			break;

		case CUSTOM:
			applyFunction.applyMovement(agent, newPos, velocity);
			break;

		case DIRECT:
			if (vel > 0.01f) {
				Quaternion rotation = new Quaternion();
				rotation.lookAt(velocity.normalize(), Vector3f.UNIT_Y);
				spatialMap[agent.idx].setLocalTranslation(newPos);
				spatialMap[agent.idx].setLocalRotation(rotation);
			}
			break;

		case BETTER_CHARACTER_CONTROL:
//			BetterCharacterControl bcc = spatialMap[agent.idx].getControl(BetterCharacterControl.class);
			Spatial sp = ((Spatial) agent.params.userData);
			BetterCharacterControl bcc = sp.getControl(BetterCharacterControl.class);
			
			if (isMoving(agent)) {
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

			/*
			 * Note: Unfortunately BetterCharacterControl does not expose getPhysicsLocation
			 * but it's tied to the SceneGraph Position
			 */
			float sqrDistance = newPos.subtract(sp.getWorldTranslation()).lengthSquared();
			if (sqrDistance > 0.4f * 0.4f) {
				/*
				 * Note: This should never occur but when collisions happen, they happen. Let's
				 * hope we can get away with that even though DtCrowd documentation explicitly
				 * states that one should not move agents constantly (okay, we only do it in
				 * rare cases, but still). Bugs could appear when some internal state is voided.
				 * The most clean solution would be removeAgent(), addAgent() but that has some
				 * overhead as well as possibly messing with the index, on which some 3rd-party
				 * code might rely on.
				 */
				log.debug("Resetting Agent because of physics drift");
				DetourUtils.toFloatArray(agent.npos, sp.getWorldTranslation());
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown Application Type: " + movementType);
		}

		if (velocity == null) {
			// Bugfix: Don't get caught in an endless loop when this code already triggered a movement-stop
			return;
		}
		
		if (proximityDetector.isInTargetProximity(agent, newPos, DetourUtils.toVector3f(agent.targetPos))) {
			resetMoveTarget(agent.idx); // Make him stop moving.
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
	 * @see #requestMoveToTarget(org.recast4j.detour.crowd.CrowdAgent, Vector3f)
	 */
	public boolean requestMoveToTarget(Vector3f to) {
		// if all were successful, return true, else return false.
		return dtCrowd.getActiveAgents().stream().allMatch(ca -> requestMoveToTarget(ca, to));
	}

	/**
	 * Moves a specified Agent to a Location.<br />
	 * This code implicitly searches for the correct polygon with a constant
	 * tolerance, in most cases you should prefer to determine the poly ref manually
	 * with domain specific knowledge.
	 * 
	 * @see #requestMoveToTarget(org.recast4j.detour.crowd.CrowdAgent, long, Vector3f)
	 * @param agent the agent to move
	 * @param to         where the agent shall move to
	 * @return whether this operation was successful
	 */
	protected boolean requestMoveToTarget(CrowdAgent agent, Vector3f to) {
		Result<FindNearestPolyResult> res = m_navQuery.findNearestPoly(DetourUtils.toFloatArray(to),
				dtCrowd.getQueryExtents(), dtCrowd.getFilter(agent.params.queryFilterType));

		if (res.succeeded() && res.result.getNearestRef() != -1) {
			return dtCrowd.requestMoveTarget(agent.idx, res.result.getNearestRef(), DetourUtils.toFloatArray(to));
		} else {
			return false;
		}
	}

	public boolean resetMoveTarget(int idx) {
		CrowdAgent ca = dtCrowd.getAgent(idx);
		applyMovement(ca, DetourUtils.toVector3f(ca.npos), null);
		return dtCrowd.resetMoveTarget(idx);
	}
	
	public boolean isMoving(CrowdAgent agent) {
//		return agent.active && (agent.state == CrowdAgentState.DT_CROWDAGENT_STATE_OFFMESH
//				|| agent.state == CrowdAgentState.DT_CROWDAGENT_STATE_WALKING);
		return agent.active && agent.state == CrowdAgent.CrowdAgentState.DT_CROWDAGENT_STATE_OFFMESH;
	}

	public boolean hasValidTarget(CrowdAgent agent) {
		return agent.active && agent.targetState == CrowdAgent.MoveRequestState.DT_CROWDAGENT_TARGET_VALID;
	}

	public boolean hasNoTarget(CrowdAgent agent) {
		return agent.active && agent.targetState == CrowdAgent.MoveRequestState.DT_CROWDAGENT_TARGET_NONE;
	}

	//------------------------------------------------------------
	// only for compatibility with gui-editor
	
	public List<CrowdAgent> getActiveAgents() {
		return dtCrowd.getActiveAgents();
	}

	public int getAgentCount() {
		return dtCrowd.getAgentCount();
	}

	public QueryFilter getFilter(int i) {
		return dtCrowd.getFilter(i);
	}

	public float[] getQueryExtents() {
		return dtCrowd.getQueryExtents();
	}
	
	public void updateAgentParameters(int idx, CrowdAgentParams params) {
		dtCrowd.updateAgentParameters(idx, params);
	}

}
