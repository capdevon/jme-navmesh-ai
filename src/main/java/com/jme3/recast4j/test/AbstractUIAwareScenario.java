package com.jme3.recast4j.test;

import com.jme3.recast4j.Detour.Crowd.Crowd;
import org.recast4j.detour.NavMesh;

public abstract class AbstractUIAwareScenario implements UIAwareScenario {
    protected String description;
    protected String name;
    protected String[] maps;

    public AbstractUIAwareScenario(String description, String name, String... maps) {
        this.description = description;
        this.name = name;
        this.maps = maps;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getRecommendedMaps() {
        return maps;
    }

    @Override
    public abstract Crowd[] run(NavMesh navMesh);
}
