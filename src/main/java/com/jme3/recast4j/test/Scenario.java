package com.jme3.recast4j.test;

import com.jme3.recast4j.Detour.Crowd.Crowd;
import org.recast4j.detour.NavMesh;

public interface Scenario {
    String getName();

    /**
     * Warning: This is WIP, the method signature most likely will change once we've come up with a few common test cases
     *
     * @param navMesh The NavMesh to use. The Scenario shouldn't make any assumptions based on this.
     * @return The Crowds that are required and have been produced.
     */
    Crowd[] run(NavMesh navMesh);
}
