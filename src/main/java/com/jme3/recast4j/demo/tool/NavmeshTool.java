package com.jme3.recast4j.demo.tool;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.Result;
import org.recast4j.detour.StraightPathItem;

import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.DetourUtils;

/**
 * 
 * @author capdevon
 */
public class NavmeshTool {

    //Set the parameters for straight path. Paths cannot exceed 256 polygons.
    private final static int MAX_POLYS = 256;

    private NavMeshQuery navQuery;
    private DefaultQueryFilter m_filter;
    private float[] m_polyPickExt = new float[] { 2, 4, 2 };
    private int m_straightPathOptions = 0;
    private List<StraightPathItem> m_straightPath;
    private LinkedList<Vector3f> waypointList = new LinkedList<>();
    private Vector3f nextWaypoint = null;

    /**
     * @param navMesh
     */
    public NavmeshTool(NavMesh navMesh) {
        navQuery = new NavMeshQuery(navMesh);
        m_filter = new DefaultQueryFilter();
    }

    public boolean isAtGoalWaypoint() {
        return this.nextWaypoint == waypointList.getLast();
    }

    public void goToNextWaypoint() {
        int index = waypointList.indexOf(nextWaypoint);
        nextWaypoint = waypointList.get(index + 1);
    }

    public Vector3f getNextWaypoint() {
        return nextWaypoint;
    }

    public List<Vector3f> getPath() {
        return Collections.unmodifiableList(waypointList);
    }

    public void clearPath() {
        waypointList.clear();
        nextWaypoint = null;
    }

    /**
     * 
     * @param startPosition
     * @param endPosition
     * @return
     */
    public boolean computePath(Vector3f startPosition, Vector3f endPosition) {

        clearPath();

        // Convert to Recast4j native format.
        float[] m_spos = startPosition.toArray(null);
        float[] m_epos = endPosition.toArray(null);

        boolean result = computePath(m_spos, m_epos);

        if (result) {
            // Add waypoints to the list
            for (StraightPathItem p: m_straightPath) {
                Vector3f vector = DetourUtils.toVector3f(p.getPos());
                waypointList.add(vector);
            }

            nextWaypoint = waypointList.getFirst();
        }

        return result;
    }

    protected boolean computePath(float[] m_spos, float[] m_epos) {
    	if (navQuery == null) {
    		return false;
    	}

        boolean foundPath = false;

        //Get closet poly for start position.
        Result<FindNearestPolyResult> startPoly = navQuery.findNearestPoly(m_spos, m_polyPickExt, m_filter);
        //Get the closest poly for end position.
        Result<FindNearestPolyResult> endPoly = navQuery.findNearestPoly(m_epos, m_polyPickExt, m_filter);

        //Get the references for the found polygons.
        long m_startRef = startPoly.result.getNearestRef();
        long m_endRef = endPoly.result.getNearestRef();

        //Get the points inside the polygon.
        float[] startPos = startPoly.result.getNearestPos();
        float[] endPos = endPoly.result.getNearestPos();

        if (m_startRef != 0 && m_endRef != 0) {
            // Get list of polys along the path.
            Result<List<Long>> m_polys = navQuery.findPath(m_startRef, m_endRef, startPos, endPos, m_filter);

            if (m_polys.succeeded()) {
                // Calculate corners within the path corridor.
                m_straightPath = navQuery.findStraightPath(startPos, endPos, m_polys.result, MAX_POLYS, m_straightPathOptions).result;
                foundPath = true;
            }
        } else {
            m_straightPath = null;
        }

        return foundPath;
    }
    
    /**
     * Finds the closest point on NavMesh within specified range.
     * 
     * @param center - The origin of the sample query.
     * @param range  - Sample within this distance from center.
     * @param result - Holds the resulting location.
     * @return True if a nearest point is found.
     */
    public boolean randomPoint(Vector3f center, float range, Vector3f result) {

        boolean found = false;
        result.set(Vector3f.ZERO);

        float[] m_spos = center.toArray(null);
        long m_startRef = navQuery.findNearestPoly(m_spos, m_polyPickExt, m_filter).result.getNearestRef();

        if (m_startRef != 0) {
            for (int i = 0; i < 30; i++) {
                Result<FindRandomPointResult> rpResult = navQuery.findRandomPointAroundCircle(m_startRef, m_spos,
                    range, m_filter, new FRand());

                if (rpResult.succeeded()) {
                    float[] pt = rpResult.result.getRandomPt();
                    result.set(pt[0], pt[1], pt[2]);
                    found = true;
                }
            }
        }

        return found;
    }

    public void setPolyPickExtents(float[] extents) {
        this.m_polyPickExt = extents;
        Objects.requireNonNull(extents);
    }

    public void setQueryFilter(DefaultQueryFilter m_filter) {
        this.m_filter = m_filter;
        Objects.requireNonNull(m_filter);
    }

    public void setNavQuery(NavMesh navMesh) {
        navQuery = (navMesh != null) ? new NavMeshQuery(navMesh) : null;
    }

}
