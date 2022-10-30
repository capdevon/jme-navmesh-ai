package com.jme3.recast4j.demo.states;

import com.jme3.app.Application;
import com.jme3.recast4j.debug.DebugHelper;
import com.jme3.recast4j.debug.NavMeshDebugViewer;
import com.jme3.renderer.RenderManager;

/**
 *
 * @author capdevon
 */
public abstract class AbstractNavState extends SimpleAppState {

    DebugHelper debugHelper;
    NavMeshDebugViewer nmDebugViewer;

    @Override
    protected void simpleInit() {
        debugHelper = new DebugHelper(assetManager);
        nmDebugViewer = new NavMeshDebugViewer(assetManager);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void render(RenderManager rm) {
        debugHelper.show(rm, viewPort);
        nmDebugViewer.show(rm, viewPort);
    }

}
