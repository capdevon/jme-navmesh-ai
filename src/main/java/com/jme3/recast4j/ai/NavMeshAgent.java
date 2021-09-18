package com.jme3.recast4j.ai;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.recast4j.detour.NavMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.debug.NavPathDebugViewer;
import com.jme3.recast4j.demo.utils.FRotator;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;

import mygame.controls.AdapterControl;

/**
 * Navigation mesh agent.
 * <p>
 * This component is attached to a mobile character in the game to allow it to
 * navigate the Scene using the NavMesh
 * 
 * @author capdevon
 */
public class NavMeshAgent extends AdapterControl {

    private static final Logger logger = LoggerFactory.getLogger(NavMeshAgent.class);

    private BetterCharacterControl bcc;
    private ScheduledExecutorService executor;
    private NavMeshTool navtool;
    private NavMeshQueryFilter filter = new NavMeshQueryFilter();
    private NavMeshPath navPath = new NavMeshPath();
    private final Vector3f destination = new Vector3f();
    private final Vector3f position2D = new Vector3f();
    private final Vector3f waypoint2D = new Vector3f();
    private final Vector3f viewDirection = new Vector3f(0, 0, 1);
    private final Quaternion lookRotation = new Quaternion();
    
    private NavPathDebugViewer pathViewer;
    private boolean debugEnabled = true;
    private boolean pathChanged;
    
    //Does the agent currently have a path? (Read Only)
    private boolean hasPath;
    //Is a path in the process of being computed but not yet ready? (Read Only)
    private boolean pathPending;
    //This property holds the stop or resume condition of the NavMesh agent.
    private boolean isStopped;
    
    //Stop within this distance from the target position.
    public float stoppingDistance = .25f;
    //Maximum movement speed when following a path.
    public float speed = 2;
    //Maximum turning speed in (deg/s) while following a path.
    public float angularSpeed = 6;
    //Should the agent update the transform orientation?
    public boolean updateRotation = true;

    /**
     * 
     * @param navMesh
     * @param app
     */
    public NavMeshAgent(NavMesh navMesh, Application app) {
        this.navtool = new NavMeshTool(navMesh);
        this.pathViewer = new NavPathDebugViewer(app.getAssetManager());
        this.executor = Executors.newScheduledThreadPool(1);
    }
    
    @Override
    public void setSpatial(Spatial sp) {
        super.setSpatial(sp);

        if (spatial != null) {
            this.bcc = getComponent(BetterCharacterControl.class);
            requireNonNull(bcc, BetterCharacterControl.class, NavMeshAgent.class);
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
        
        //must be called from the update loop
        if (pathChanged) {
            drawPath();
            pathChanged = false;
        }
        
        /**
         * getNextWayPoint will return always the same waypoint until we
         * manually advance to the next
         */
        Vector3f wayPoint = navPath.getNextWaypoint();
        
        if (wayPoint != null) {

            position2D.set(spatial.getWorldTranslation()).setY(0);
            waypoint2D.set(wayPoint).setY(0);
            float remainingDistance = position2D.distance(waypoint2D);

            // move char until waypoint reached
            if (remainingDistance > stoppingDistance) {
                Vector3f direction = waypoint2D.subtract(position2D).normalizeLocal();
                
                //smooth rotation
                if (updateRotation && direction.lengthSquared() > 0) {
                    lookRotation.lookAt(direction, Vector3f.UNIT_Y);
                    FRotator.smoothDamp(spatial.getWorldRotation(), lookRotation, angularSpeed * tpf, viewDirection);
                    bcc.setViewDirection(viewDirection);
                }
                bcc.setWalkDirection(direction.multLocal(speed));
                
            } //If at the final waypoint set at goal to true 
            else if (navPath.isAtGoalWaypoint()) {
                resetPath();

            } //If less than one from current waypoint and not the goal. Go to next waypoint 
            else {
            	navPath.goToNextWaypoint();
            }
        }
    }

    private void startPathfinder() {
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (pathPending) {
                	
                    hasPath = navtool.computePath(spatial.getWorldTranslation(), destination, filter, navPath);
                    logger.info("TargetPos {}, hasPath {}", destination, hasPath);

                    if (hasPath) {
                        // display motion path
                        pathChanged = true;
                    }

                    pathPending = false;
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

        if (debugEnabled) {
            pathViewer.clearPath();
        }
    }

    /**
     * Displays a motion path showing each waypoint. Stays in scene until
     * another path is set.
     */
    private void drawPath() {
        if (debugEnabled) {
            pathViewer.clearPath();
            pathViewer.drawPath(navPath.getCorners());
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // To change body of generated methods, choose Tools | Templates.
        if (debugEnabled) {
            pathViewer.show(rm, vp);
        }
    }

    public NavMeshQueryFilter getQueryFilter() {
        return filter;
    }

    public void setQueryFilter(NavMeshQueryFilter filter) {
        this.filter = filter;
    }

    /**
     * Gets the destination of the agent in world-space units.
     * 
     * @return
     */
    public Vector3f getDestination() {
        return destination;
    }

    /**
     * Set the destination of the agent in world-space units.
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

    /**
     * Set the maximum movement speed when following a path.
     * @param speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    
    /**
     * Maximum turning speed in (deg/s) while following a path.
     * @param angularSpeed 
     */
    public void setAngularSpeed(float angularSpeed) {
        this.angularSpeed = angularSpeed;
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
    
    public boolean isAtGoal() {
        return navPath.isEmpty();
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
            // display motion path
            pathChanged = true;
            return true;

        } else {
            navPath.clearCorners();
            return false;
        }
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

}
