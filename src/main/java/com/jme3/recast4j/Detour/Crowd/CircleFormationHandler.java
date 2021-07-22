package com.jme3.recast4j.Detour.Crowd;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import org.recast4j.detour.crowd.CrowdAgent;

/**
 * The Circle Formation Handler moves CrowdAgents on a circle around the target in evenly spaced portions.<br />
 * It does not prefer "back facing" angles, so when you have 2 agents one would be in front and one in back of the target
 * (where "in front" is hard to tell, it means the world global z axis actually).<br />
 * Note that this code does not like dynamic changes of the crowdSize, because other agents would have to be re-arranged.
 */
public class CircleFormationHandler implements FormationHandler {
    protected int numAgents;
    protected Crowd crowd;
    protected Vector3f target;
    protected float radius;
    protected int filledSlotIdx;
    protected float angle;
    private final float distanceSquared;

    public CircleFormationHandler(int maxAgents, Crowd crowd, float radius) {
        this.numAgents = maxAgents;
        this.crowd = crowd;
        this.radius = radius;
        distanceSquared = Math.max(0.1f * 0.1f, (radius * radius) / (3f * 3f));
    }

    @Override
    public void setTargetPosition(Vector3f targetPosition) {
        this.target = targetPosition;
        rebuildFormation();
    }

    protected void rebuildFormation() {
        // This only works when no one is yet part of the formation!
        filledSlotIdx = 0;
        angle = FastMath.TWO_PI / numAgents;
    }

    @Override
    public Vector3f moveIntoFormation(CrowdAgent crowdAgent) {
        //@TODO: Assumption here is, that crowdAgent is not yet part of our formation. Ensure that
        filledSlotIdx++;
        Quaternion q = new Quaternion();
        q.fromAngleAxis(angle * filledSlotIdx, Vector3f.UNIT_Y);

        // Offset Vector: Rotate and scale to the radius
        Vector3f v = target.add(q.mult(Vector3f.UNIT_Z).mult(radius));
        boolean b = crowd.requestMoveToTarget(crowdAgent, v);

        if (!b) {
            // @TODO: Consider throwing an exception
        }

        return target;
    }

    @Override
    public boolean isInFormationProximity(Vector3f actualPosition, Vector3f targetPosition) {
        float a = SimpleTargetProximityDetector.euclideanDistanceSquared(actualPosition, targetPosition);
        System.out.println(a + " < " + distanceSquared + " = " + (a < distanceSquared));
        return a < distanceSquared;
    }
}
