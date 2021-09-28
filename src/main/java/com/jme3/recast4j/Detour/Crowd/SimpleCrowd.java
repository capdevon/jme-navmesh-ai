package com.jme3.recast4j.Detour.Crowd;

import org.recast4j.detour.NavMesh;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.crowd.ObstacleAvoidanceQuery.ObstacleAvoidanceParams;

/**
 * 
 * @author capdevon
 */
public class SimpleCrowd extends JmeCrowd {

    /**
     * 
     * @param config
     * @param nav
     * @param queryFilter
     */
    public SimpleCrowd(CrowdConfig config, NavMesh nav, QueryFilter queryFilter) {
        super(config, nav, __ -> queryFilter);
        setDefaultObstacleAvoidanceParams();
    }

    /**
     * Setup local avoidance params to different qualities.
     */
    private void setDefaultObstacleAvoidanceParams() {
        ObstacleAvoidanceParams params = new ObstacleAvoidanceParams();

        // Low (11)
        params.velBias = 0.5f;
        params.adaptiveDivs = 5;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 1;
        setObstacleAvoidanceParams(ObstacleAvoidanceType.LowQuality.id, params);

        // Medium (22)
        params.velBias = 0.5f;
        params.adaptiveDivs = 5;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 2;
        setObstacleAvoidanceParams(ObstacleAvoidanceType.MedQuality.id, params);

        // Good (45)
        params.velBias = 0.5f;
        params.adaptiveDivs = 7;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 3;
        setObstacleAvoidanceParams(ObstacleAvoidanceType.GoodQuality.id, params);

        // High (66)
        params.velBias = 0.5f;
        params.adaptiveDivs = 7;
        params.adaptiveRings = 3;
        params.adaptiveDepth = 3;
        setObstacleAvoidanceParams(ObstacleAvoidanceType.HighQuality.id, params);
    }


}