package com.jme3.recast4j.test;

/**
 * As opposed to {@link com.jme3.recast4j.test.Scenario }, this Scenario will appear in jme3-recast4j-demo as well.
 * It might have to provide more API to work with the UI.
 */
public interface UIAwareScenario extends Scenario {
    String getDescription();

    /**
     * This is a hint to the UI which maps this Scenario is interesting to be viewed on.<br />
     * For every map known to the UI, there will be an entry in a drop-down field.<br />
     * The UI will then load the map and run the Scenario against that.
     * @return The "list" of recommended maps
     */
    String[] getRecommendedMaps();
}
