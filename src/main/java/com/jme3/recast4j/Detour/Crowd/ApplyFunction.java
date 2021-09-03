package com.jme3.recast4j.Detour.Crowd;

import com.jme3.math.Vector3f;
import org.recast4j.detour.crowd.CrowdAgent;

/**
 * When an application needs more control over how the changes are propagated
 * from this library, it can implement this interface and set the
 * {@link MovementType} to <code>CUSTOM</code> using
 * {@link JmeCrowd#setMovementType(MovementType)}. <br />
 * Then register this function using
 * {@link JmeCrowd#setCustomApplyFunction(ApplyFunction)}.
 * 
 * @author MeFisto94
 */
public interface ApplyFunction {
	/**
	 * Apply the calculations from Recast to your custom representation (e.g. Entity
	 * Component System).<br />
	 * <b>Caution:</b> When an agent has reached his target, this method will be
	 * called but with a velocity value of <code>null</code>, to signalizes that
	 * this agent shall stop moving entirely.
	 * 
	 * @param crowdAgent The agent of the crowd (you can use the userdata value to identify it)
	 * @param newPos     The position for this agent
	 * @param velocity   The velocity for this agent (can be <code>null</code>!)
	 */
	void applyMovement(CrowdAgent crowdAgent, Vector3f newPos, Vector3f velocity);
}
