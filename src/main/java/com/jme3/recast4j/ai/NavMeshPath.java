package com.jme3.recast4j.ai;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.jme3.math.Vector3f;

/**
 * A path as calculated by the navigation system.
 * 
 * <p>
 * The path is represented as a list of waypoints stored in the corners array.
 * These points are not set directly from user scripts but a NavMeshPath with
 * points correctly assigned is returned by the NavmeshTool.calculatePath
 * function.
 * 
 * @author capdevon
 */
public class NavMeshPath {

	// Status of the path.
	protected NavMeshPathStatus status;
	// Corner points of the path.
	protected LinkedList<Vector3f> waypointList;
	protected Vector3f nextWaypoint = null;

	/**
	 * Constructor.
	 */
	public NavMeshPath() {
		waypointList = new LinkedList<>();
	}

	/**
	 * @return Status of the path. (Read Only)
	 */
	public NavMeshPathStatus getStatus() {
		return status;
	}

	/**
	 * @return Corner points of the path. (Read Only)
	 */
	public List<Vector3f> getCorners() {
		return Collections.unmodifiableList(waypointList);
	}

	/**
	 * Erase all corner points from path.
	 */
	public void clearCorners() {
		waypointList.clear();
		nextWaypoint = null;
		status = null;
	}

	protected boolean isAtGoalWaypoint() {
		return this.nextWaypoint == waypointList.getLast();
	}

	protected void goToNextWaypoint() {
		int index = waypointList.indexOf(nextWaypoint);
		nextWaypoint = waypointList.get(index + 1);
	}

	protected Vector3f getNextWaypoint() {
		return nextWaypoint;
	}

}
