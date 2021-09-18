package com.jme3.recast4j.Detour.Crowd;

/**
 * Level of obstacle avoidance.
 * 
 * @author capdevon
 */
public enum ObstacleAvoidanceType {
	
    // Enable simple avoidance. Low performance impact.
    LowQuality(0),
    // Medium avoidance. Medium performance impact
    MedQuality(1),
    // Good avoidance. High performance impact
    GoodQuality(2),
    // Enable highest precision. Highest performance impact.
    HighQuality(3);

    public final int id;

    private ObstacleAvoidanceType(int id) {
        this.id = id;
    }

}
