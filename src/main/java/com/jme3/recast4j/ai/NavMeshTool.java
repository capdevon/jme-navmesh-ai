package com.jme3.recast4j.ai;

import java.util.List;

import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.FindRandomPointResult;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.NavMeshQuery.FRand;
import org.recast4j.detour.Result;
import org.recast4j.detour.StraightPathItem;

import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.DetourUtils;

/**
 * 
 * @author capdevon
 */
public class NavMeshTool {

    //Set the parameters for straight path. Paths cannot exceed 256 polygons.
    private final static int MAX_POLYS = 256;

    private NavMeshQuery navQuery;
    private List<StraightPathItem> m_straightPath;

    /**
     * @param navMesh
     */
    public NavMeshTool(NavMesh navMesh) {
    	navQuery = new NavMeshQuery(navMesh);
    }

    /**
     * Calculate a path between two points and store the resulting path.
     * 
     * @param startPosition The initial position of the path requested.
     * @param endPosition   The final position of the path requested.
     * @param path          The resulting path.
     * @return True if either a complete or partial path is found. False otherwise.
     */
    public boolean computePath(Vector3f startPosition, Vector3f endPosition, NavMeshQueryFilter m_filter, NavMeshPath path) {

    	path.clearCorners();

        // Convert to Recast4j native format.
        float[] m_spos = startPosition.toArray(null);
        float[] m_epos = endPosition.toArray(null);

        boolean result = computePath(m_spos, m_epos, m_filter);
        path.status = result ? NavMeshPathStatus.PathComplete : NavMeshPathStatus.PathInvalid;

        if (result) {
            // Add waypoints to the list
            for (StraightPathItem p : m_straightPath) {
                Vector3f vector = DetourUtils.toVector3f(p.getPos());
                path.waypointList.add(vector);
            }

            path.nextWaypoint = path.waypointList.getFirst();
        }

        return result;
    }

    protected boolean computePath(float[] m_spos, float[] m_epos, NavMeshQueryFilter m_filter) {

        boolean foundPath = false;
        
        float[] m_polyPickExt = m_filter.m_polyExtents;
        int m_straightPathOptions = m_filter.m_straightPathOptions;

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
     * @param center The origin of the sample query.
     * @param range  Sample within this distance from center.
     * @param result Holds the resulting location.
     * @return True if a nearest point is found.
     */
    public boolean randomPoint(Vector3f center, float range, Vector3f result, NavMeshQueryFilter m_filter) {
    	
        boolean found = false;
        result.set(Vector3f.ZERO);

        float[] m_spos = center.toArray(null);
        float[] m_polyPickExt = m_filter.m_polyExtents;
        
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

}
