package com.jme3.recast4j.demo.states;

import com.jme3.app.Application;
import com.jme3.recast4j.debug.DebugHelper;
import com.jme3.recast4j.debug.NavMeshDebugRenderer;
import com.jme3.renderer.RenderManager;

/**
 *
 * @author capdevon
 */
public abstract class AbstractNavState extends SimpleAppState {

    DebugHelper debugHelper;
    NavMeshDebugRenderer navMeshRenderer;

    @Override
    protected void simpleInit() {
        debugHelper = new DebugHelper(assetManager);
        navMeshRenderer = new NavMeshDebugRenderer(assetManager);
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
        navMeshRenderer.show(rm, viewPort);
    }

}
