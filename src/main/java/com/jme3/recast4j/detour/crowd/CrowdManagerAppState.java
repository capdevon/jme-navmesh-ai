package com.jme3.recast4j.detour.crowd;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.util.SafeArrayList;

/**
 * CrowdManagerAppState provide easier interfacing with the jMonkeyEngine.
 * Use this as starting point for your entity-based approaches.
 */
public class CrowdManagerAppState extends BaseAppState {

    protected SafeArrayList<JmeCrowd> crowdList = new SafeArrayList<>(JmeCrowd.class, 5);

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
        for (JmeCrowd crowd : crowdList) {
            crowd.update(tpf);
        }
    }
    
    public void addCrowd(JmeCrowd c) {
    	crowdList.add(c);
    }
    
    public void removeCrowd(JmeCrowd c) {
    	crowdList.remove(c);
    }
}
