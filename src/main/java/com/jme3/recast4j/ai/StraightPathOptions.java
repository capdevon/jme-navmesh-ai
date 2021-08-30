package com.jme3.recast4j.ai;

import org.recast4j.detour.NavMeshQuery;

/**
 * Options for NavMeshQuery.findStraightPath.
 * 
 * @author capdevon
 */
public enum StraightPathOptions {

    //None
    None(0),
    //Add a vertex at every polygon edge crossing where area changes.
    AreaCrossings(NavMeshQuery.DT_STRAIGHTPATH_AREA_CROSSINGS),
    //Add a vertex at every polygon edge crossing.
    AllCrossings(NavMeshQuery.DT_STRAIGHTPATH_ALL_CROSSINGS);

    private int value;

    private StraightPathOptions(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
