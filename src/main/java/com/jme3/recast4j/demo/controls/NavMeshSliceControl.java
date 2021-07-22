package com.jme3.recast4j.demo.controls;

import com.jme3.math.Vector3f;
import org.recast4j.detour.NavMeshQuery;

import java.util.List;
import org.recast4j.detour.Result;

/**
 * This Class shows how Pathfinding can be done in slices and dynamic.
 */
public class NavMeshSliceControl extends AbstractNavMeshControl {
    /**
     * Epsilon is the distance required to accept a waypoint as close enough, see how changing this value changes
     * walking behavior. Must be >= 0.2, because for some reason the height is always reported as 1.2 but Jaime walks
     * on the height of 1.0
     */
    protected static final float epsilon = 0.3f;

    /**
     * How many algorithm iterations should be done every frame. This is hard to answer as it depends on the available
     * time and especially fps. If you have low fps you can fit more iterations.
     */
    protected static final int maxIters = 1;
    protected int iterCount = 0;
    protected int lastIterCount = 0;
    protected NavMeshQuery query;
    boolean isFinished = false;

    public NavMeshSliceControl(NavMeshQuery query) {
        super();
        this.query = query;
    }

    @Override
    protected void controlUpdate(float tpf) {
        super.controlUpdate(tpf);

        updatePath();

        if (isPathListDone()) {
            if (hasSomethingToFlush()) {
                flushBuffers();
                moveToWaypoint();
            } else if (isFinished) {
                stopFollowing(); // We've reached our goal!
            }
        } else { // Regular Path Walking
            if (getSpatial().getWorldTranslation().distance(pathList.get(currentIndex)) < epsilon) {
                // reached a target, increase the index, that's all
                currentIndex++;

                if (isPathListDone()) { // still in the list?
                    moveToWaypoint();
                } else { // reached our target
                    stopFollowing();
                }
            } // else -> AntiStuck Detection?
        }
    }

    protected void updatePath() {
        if (isFinished) {
            return;
        }

        Result<Integer> res = query.updateSlicedFindPath(maxIters);
        if (res.succeeded()) {
            isFinished = true;
        }

        iterCount = res.result;
    }

    @Override
    public void followPath(List<Vector3f> pathList) {
        throw new IllegalArgumentException("Not Supported");
    }

    /**
     * We have two buffers, one is being worked on by detour, the other one is being walked off by our agent.
     * Flushing means stopping detour and  pushing it's progress to the agent. This could happen every frame,
     * but it's only required if the agent runs out of waypoints really..
     *
     * @return True if the Detour Buffer can be flushed
     */
    protected boolean hasSomethingToFlush() {
        return iterCount != 0 && iterCount > lastIterCount;
    }

    protected void flushBuffers() {
        lastIterCount = iterCount;
        // @TODO: Flushing, that means getting the below thing to work.
        //query.finalizeSlicedFindPathPartial();
        pathList.clear();
        //pathList.add(all results)
        currentIndex = 0;
    }
}
