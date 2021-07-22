package mygame.controls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.Result;
import org.recast4j.detour.StraightPathItem;

import com.jme3.animation.AnimChannel;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.cinematic.MotionPath;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Spline;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

import mygame.interfaces.DataKey;
import mygame.interfaces.EnumPosType;
import mygame.interfaces.ListenerKey;
import mygame.interfaces.Pickable;

/**
 *
 * @author mitm
 */
public class DetourMoveControl extends AbstractControl implements Pickable {

    private ScheduledExecutorService executor;
    private static final Logger LOGGER = Logger.getLogger(DetourMoveControl.class.getName());
    private Vector3f target, wayPosition, nextWaypoint;
    private boolean finding, showPath;
    private SimpleApplication app;
    private List<StraightPathItem> straightPath;
    private List<Vector3f> wayPoints;
    private MotionPath motionPath;

    private DetourMoveControl() {
    }

    public DetourMoveControl(Application app) {
        this.app = (SimpleApplication) app;
        wayPoints = new ArrayList<>();
        motionPath = new MotionPath();
        motionPath.setPathSplineType(Spline.SplineType.Linear);

        NavMesh recastNavMesh = getNavMesh();
        executor = Executors.newScheduledThreadPool(1);
        startRecastQuery(recastNavMesh);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial == null) {
            shutdownAndAwaitTermination(executor);
        }
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(6, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(6, TimeUnit.SECONDS)) {
                    LOGGER.log(Level.SEVERE, "Pool did not terminate {0}", pool);
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        
        if (getWayPosition() != null) {
            Vector3f spatialPosition = spatial.getWorldTranslation();
            Vector2f aiPosition = new Vector2f(spatialPosition.x, spatialPosition.z);
            Vector2f waypoint2D = new Vector2f(getWayPosition().x, getWayPosition().z);
            float distance = aiPosition.distance(waypoint2D);

            if (distance > 1f) {
                Vector2f direction = waypoint2D.subtract(aiPosition);
                direction.mult(tpf);
                getPCControl().setViewDirection(new Vector3f(direction.x, 0, direction.y).normalize());
                getPCControl().onAction(ListenerKey.MOVE_FORWARD, true, 1);
            } else {
                setWayPosition(null);
            }
        } else if (!this.isPathfinding() && getNextWaypoint() != null && !isAtGoalWaypoint()) {
            //must be called from the update loop
            if (showPath) {
                showPath();
                showPath = false;
            }
            goToNextWaypoint();
            setWayPosition(new Vector3f(getNextWaypoint()));

            if (getAutorun() && getPositionType() != EnumPosType.POS_RUNNING.pos()) {
                setPosition(EnumPosType.POS_RUNNING.pos());
                stopPlaying();
            } else if (!getAutorun() && getPositionType() != EnumPosType.POS_WALKING.pos()) {
                setPosition(EnumPosType.POS_WALKING.pos());
                stopPlaying();
            }
//            System.out.println("Next wayPosition = " + getWayPosition() + " SpatialWorldPosition " + spatialPosition);
        } else {
            if (canMove() && getPositionType() != EnumPosType.POS_STANDING.pos()) {
                setPosition(EnumPosType.POS_STANDING.pos());
                stopPlaying();
            }
            getPCControl().onAction(ListenerKey.MOVE_FORWARD, false, 1);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        DetourMoveControl control = new DetourMoveControl(app);
        control.setSpatial(spatial);
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        //TODO: load properties of this Control, e.g.
        //this.value = in.readFloat("name", defaultValue);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
        //TODO: save properties of this Control, e.g.
        //out.write(this.value, "name", defaultValue);
    }

    private void startRecastQuery(org.recast4j.detour.NavMesh navMesh) {
        NavMeshQuery query = new NavMeshQuery(navMesh);
        executor.scheduleWithFixedDelay(() -> {
            if (target != null) {
                finding = true;
                clearPath();
                QueryFilter filter = new DefaultQueryFilter();
                Vector3f spatialPos = getSpatial().getWorldTranslation();
                float[] extents = {2, 4, 2};
                boolean success;

                float[] startArray = spatialPos.toArray(new float[3]);
                float[] endArray = target.toArray(new float[3]);

                Result<FindNearestPolyResult> startPos = query.findNearestPoly(startArray, extents, filter);
                Result<FindNearestPolyResult> endPos = query.findNearestPoly(endArray, extents, filter);

                if (startPos.result.getNearestRef() == 0 || endPos.result.getNearestRef() == 0) {
                    success = false;
                } else {
                    Result<List<Long>> path = query.findPath(startPos.result.getNearestRef(), endPos.result.getNearestRef(), startPos.result.getNearestPos(), endPos.result.getNearestPos(), filter);
                    straightPath = query.findStraightPath(startPos.result.getNearestPos(), endPos.result.getNearestPos(), path.result, Integer.MAX_VALUE, 0).result;
                    
                    for (int i = 0; i < straightPath.size(); i++) {
                        float[] pos = straightPath.get(i).getPos();
                        Vector3f vector = new Vector3f(pos[0], pos[1], pos[2]);
                        wayPoints.add(vector);
                    }
                    nextWaypoint = this.getFirst();
                    success = true;
                }
                System.out.println("RECAST SUCCESS " + success);
                if (success) {
                    target = null;
                    showPath = true;
                }
                finding = false;
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }
    
    private void showPath() {
        if (motionPath.getNbWayPoints() > 0) {
            motionPath.clearWayPoints();
            motionPath.disableDebugShape();
        }

        for (Vector3f wp : getWaypoints()) {
            motionPath.addWayPoint(wp);
        }
        motionPath.enableDebugShape(this.app.getAssetManager(), this.app.getRootNode());
    }

    public boolean canMove() {

        int position = getPositionType();
        boolean move = true;

        for (EnumPosType pos : EnumPosType.values()) {
            if (pos.pos() == position) {
                switch (pos) {
                    case POS_DEAD:
                    case POS_MORTAL:
                    case POS_INCAP:
                    case POS_STUNNED:
                    case POS_TPOSE:
                        move = false;
                        break;
                }
            }
        }
        return move;
    }

    /**
     * @param target the target to set
     */
    @Override
    public void setTarget(Vector3f target) {
        this.target = target;
    }

    /**
     * @return the pathfinding
     */
    public boolean isPathfinding() {
        return finding;
    }

    /**
     * @return the wayPosition
     */
    public Vector3f getWayPosition() {
        return wayPosition;
    }

    /**
     * @param wayPosition the wayPosition to set
     */
    public void setWayPosition(Vector3f wayPosition) {
        this.wayPosition = wayPosition;
    }

    /**
     * @return the straightPath
     */
    public List<StraightPathItem> getStraightPath() {
        return straightPath;
    }

    /**
     * @return the wayPoints
     */
    public List<Vector3f> getWaypoints() {
        return wayPoints;
    }

    public void goToNextWaypoint() {
        int from = getWaypoints().indexOf(nextWaypoint);
        nextWaypoint = getWaypoints().get(from + 1);
    }

    public Vector3f getNextWaypoint() {
        return nextWaypoint;
    }

    public Vector3f getFirst() {
        return wayPoints.get(0);
    }

    public Vector3f getLast() {
        return wayPoints.get(wayPoints.size() - 1);
    }

    public boolean isAtGoalWaypoint() {
        return nextWaypoint == this.getLast();
    }
    
    public void clearPath() {
        wayPoints.clear();
        nextWaypoint = null;
        setWayPosition(null);
    }

    /**
     * @return the motionPath
     */
    public MotionPath getMotionPath() {
        return motionPath;
    }
    
    private void stopPlaying() {
        AnimChannel animChannel = spatial.getControl(AnimationControl.class).getAnimChannel();
        animChannel.setTime(animChannel.getAnimMaxTime());
    }
    
    private PCControl getPCControl() {
        return spatial.getControl(PCControl.class);
    }
    
    private boolean getAutorun() {
        return (Boolean) spatial.getUserData(DataKey.AUTORUN);
    }
    
    private int getPositionType() {
        return (Integer) spatial.getUserData(DataKey.POSITION_TYPE);
    }
    
    private void setPosition(int position) {
        spatial.setUserData(DataKey.POSITION_TYPE, position);
    }

    private NavMesh getNavMesh() {
    	return null;
//        return app.getStateManager().getState(RecastMeshGenState.class).getNavMesh();
    }
}