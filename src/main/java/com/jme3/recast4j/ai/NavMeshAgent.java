package com.jme3.recast4j.ai;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.recast4j.detour.NavMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * Navigation mesh agent.
 * <p>
 * This component is attached to a mobile character in the game to allow it to
 * navigate the Scene using the NavMesh
 * 
 * @author capdevon
 */
public class NavMeshAgent extends AbstractControl {

    private static final Logger logger = LoggerFactory.getLogger(NavMeshAgent.class);

    protected BetterCharacterControl bcc;
    private ScheduledExecutorService executor;
    private NavMeshTool navtool;
    private NavMeshQueryFilter filter = new NavMeshQueryFilter();
    private NavMeshPath navPath = new NavMeshPath();
    
    private final Vector3f destination = new Vector3f();
    private final Vector3f viewDirection = new Vector3f(0, 0, 1);
    private final Quaternion lookRotation = new Quaternion();
    
    // Does the agent currently have a path? (Read Only)
    private boolean hasPath;
    // Is a path in the process of being computed but not yet ready? (Read Only)
    private boolean pathPending;
    // This property holds the stop or resume condition of the NavMesh agent.
    private boolean isStopped;

    // Stop within this distance from the target position.
    private float stoppingDistance = .25f;
    // Maximum movement speed when following a path.
    private float speed = 2;
    // Maximum turning speed in (deg/s) while following a path.
    private float angularSpeed = 6;
    // Should the agent update the transform orientation?
    private boolean updateRotation = true;

    /**
     * 
     * @param navMesh
     */
    public NavMeshAgent(NavMesh navMesh) {
        this.navtool = new NavMeshTool(navMesh);
        this.executor = Executors.newScheduledThreadPool(1);
    }
    
    @Override
    public void setSpatial(Spatial sp) {
        super.setSpatial(sp);

        if (spatial != null) {
            this.bcc = spatial.getControl(BetterCharacterControl.class);
            Objects.requireNonNull(bcc, "BetterCharacterControl not found: " + spatial);
            startPathfinder();

        } else {
            stopPathfinder();
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (pathPending || isStopped) {
            return;
        }

        /**
         * getNextWayPoint will return always the same waypoint until we
         * manually advance to the next
         */
        Vector3f wayPoint = navPath.getNextWaypoint();

        if (wayPoint != null) {
            float remainingDistance = spatial.getWorldTranslation().distance(wayPoint);

            // move char until waypoint reached
            if (remainingDistance > stoppingDistance) {
                Vector3f dir = wayPoint.subtract(spatial.getWorldTranslation(), viewDirection).setY(0);
                dir.normalizeLocal();
                moveTo(dir, tpf);

            } //If at the final waypoint set at goal to true 
            else if (navPath.isAtGoalWaypoint()) {
                resetPath();

            } //If less than one from current waypoint and not the goal. Go to next waypoint 
            else {
                navPath.goToNextWaypoint();
            }
        }
    }

    private void moveTo(Vector3f direction, float tpf) {
        if (updateRotation && direction.lengthSquared() > 0) {
            lookRotation.lookAt(direction, Vector3f.UNIT_Y);
            smoothDamp(spatial.getWorldRotation(), lookRotation, angularSpeed * tpf, viewDirection);
            bcc.setViewDirection(viewDirection);
        }
        bcc.setWalkDirection(direction.multLocal(speed));
    }
    
    /**
     * Spherically interpolates between quaternions a and b by ratio t. The
     * parameter t is clamped to the range [0, 1].
     */
    private Vector3f smoothDamp(Quaternion from, Quaternion to, float smoothTime, Vector3f store) {
        from.slerp(to, FastMath.clamp(smoothTime, 0, 1));
        return from.mult(Vector3f.UNIT_Z, store);
    }

    private void startPathfinder() {
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (pathPending) {
                    hasPath = navtool.computePath(spatial.getWorldTranslation(), destination, filter, navPath);
                    pathPending = false;
                    logger.info("TargetPos {}, hasPath {}", destination, hasPath);
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void stopPathfinder() {
        // Disable new tasks from being submitted
        executor.shutdown();
        try {
            if (!executor.awaitTermination(6, TimeUnit.SECONDS)) {
                logger.warn("Pool did not terminate {}", executor);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("Interrupted!", e);
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
        logger.info("Pool shutdown {}", executor);
    }
    
    /**
     * Clears the current path.
     */
    public void resetPath() {
        navPath.clearCorners();
        bcc.setWalkDirection(Vector3f.ZERO);
        hasPath = false;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // To change body of generated methods, choose Tools | Templates.
    }

    public NavMeshQueryFilter getQueryFilter() {
        return filter;
    }

    public void setQueryFilter(NavMeshQueryFilter filter) {
        this.filter = filter;
    }

    /**
     * Gets the destination of the agent in world-space units.
     * @return
     */
    public Vector3f getDestination() {
        return destination;
    }

    /**
     * Sets or updates the destination thus triggering the calculation for a new path.
     * <p>
     * Note that the path may not become available until after a few frames later.
     * While the path is being computed, pathPending will be true. If a valid path
     * becomes available then the agent will resume movement.
     * 
     * @param target - The target point to navigate to.
     */
    public void setDestination(Vector3f target) {
        this.destination.set(target);
        pathPending = true;
    }
    
    public float getSpeed() {
        return speed;
    }

    /**
     * Set the maximum movement speed when following a path.
     * @param speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getAngularSpeed() {
        return angularSpeed;
    }

    /**
     * Maximum turning speed in (deg/s) while following a path.
     * @param angularSpeed
     */
    public void setAngularSpeed(float angularSpeed) {
        this.angularSpeed = angularSpeed;
    }
    
    public boolean isUpdateRotation() {
        return updateRotation;
    }

    /**
     * Should the agent update the transform orientation?
     * @param updateRotation
     */
    public void setUpdateRotation(boolean updateRotation) {
        this.updateRotation = updateRotation;
    }

    public float getStoppingDistance() {
        return stoppingDistance;
    }

    /**
     * Stop within this distance from the target position.
     * @param stoppingDistance
     */
    public void setStoppingDistance(float stoppingDistance) {
        this.stoppingDistance = stoppingDistance;
    }

    /**
     * This property holds the stop or resume condition of the NavMesh agent.
     * @param stopped
     */
    public void setStopped(boolean stopped) {
        this.isStopped = stopped;
        if (stopped) {
            bcc.setWalkDirection(Vector3f.ZERO);
        }
    }

    public boolean pathPending() {
        return pathPending;
    }

    public boolean hasPath() {
        return hasPath;
    }

    public NavMeshPathStatus pathStatus() {
        return navPath.getStatus();
    }

    public NavMeshPath getPath() {
        return navPath;
    }
    
    /**
     * Assign a new path to this agent.
     * <p>
     * If the path is successfully assigned the agent will resume movement toward
     * the new target. If the path cannot be assigned the path will be cleared.
     * 
     * @param path New path to follow.
     * @return True if the path is successfully assigned.
     */
    public boolean setPath(NavMeshPath path) {
        if (!hasPath) {
            navPath = path;
            return true;

        } else {
            navPath.clearCorners();
            return false;
        }
    }
    
    /**
     * @return The distance between the agent's position and the destination on the current path. (Read Only)
     */
    public float remainingDistance() {
        float pathLength = 0;
        List<Vector3f> corners = navPath.waypointList;

        for (int j = 0; j < corners.size(); ++j) {
            Vector3f va = (j == 0) ? spatial.getWorldTranslation() : corners.get(j - 1);
            Vector3f vb = corners.get(j);
            pathLength += va.distance(vb);
        }

        return pathLength;
    }
    
    /**
     * Calculate a path to a specified point and store the resulting path.
     * <p>
     * This function can be used to plan a path ahead of time to avoid a delay in
     * gameplay when the path is needed. Another use is to check if a target
     * position is reachable before moving the agent.
     * 
     * @param targetPosition The final position of the path requested.
     * @param path           The resulting path.
     * @return True if a path is found.
     */
    public boolean calculatePath(Vector3f targetPosition, NavMeshPath path) {
        return navtool.computePath(spatial.getWorldTranslation(), targetPosition, filter, path);
    }

    /**
     * Finds the closest point on NavMesh within specified range.
     * 
     * @param center The origin of the sample query.
     * @param range  Sample within this distance from center.
     * @param result Holds the resulting location.
     * @return True if a nearest point is found.
     */
    public boolean samplePosition(Vector3f center, float range, Vector3f result) {
        return navtool.randomPoint(center, range, result, filter);
    }
    
    /**
     * Trace a straight path towards a target position 
     * in the NavMesh without moving the agent.
     * 
     * @param targetPosition The desired end position of movement.
     * @param hit            Properties of the obstacle detected by the ray (if any).
     * @return True if there is an obstacle between the agent and the target position, otherwise false.
     */
    public boolean raycast(Vector3f targetPosition, NavMeshHit hit) {
        return navtool.raycast(spatial.getWorldTranslation(), targetPosition, hit, filter);
    }

}
