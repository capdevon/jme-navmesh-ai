package com.jme3.recast4j.Detour.Crowd;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

/**
 * CrowdManagerAppState is a wrapper around the "CrowdManager" class to provide
 * easier interfacing with the jMonkeyEngine.<br />
 * Use this as starting point for your entity-based approaches.
 */
public class CrowdManagerAppState extends BaseAppState {

    protected CrowdManager crowdManager = new CrowdManager();

    @Override
    protected void initialize(Application application) {}

    @Override
    protected void cleanup(Application application) {}

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}

    @Override
    public void update(float tpf) {
        crowdManager.update(tpf);
    }

    public CrowdManager getCrowdManager() {
        return crowdManager;
    }

    public void setCrowdManager(CrowdManager crowdManager) {
        this.crowdManager = crowdManager;
    }
}