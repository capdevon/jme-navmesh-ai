package com.jme3.recast4j.demo.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.InputManager;
import com.jme3.recast4j.debug.NavMeshDebugViewer;
import com.jme3.recast4j.debug.NavPathDebugViewer;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

/**
 * 
 * @author capdevon
 */
public abstract class AbstractNavState extends BaseAppState {

    public NavPathDebugViewer pathViewer;
    public NavMeshDebugViewer nmDebugViewer;

    // cache fields
    public SimpleApplication sapp;
    public AppSettings settings;
    public AppStateManager stateManager;
    public AssetManager assetManager;
    public InputManager inputManager;
    public ViewPort viewPort;
    public Camera camera;
    public Node rootNode;
    public Node guiNode;

    protected void refreshCacheFields() {
        this.sapp 			= (SimpleApplication) getApplication();
        this.settings 		= sapp.getContext().getSettings();
        this.stateManager 	= sapp.getStateManager();
        this.assetManager 	= sapp.getAssetManager();
        this.inputManager 	= sapp.getInputManager();
        this.viewPort 		= sapp.getViewPort();
        this.camera 		= sapp.getCamera();
        this.rootNode 		= sapp.getRootNode();
        this.guiNode 		= sapp.getGuiNode();
    }

    protected PhysicsSpace getPhysicsSpace() {
        return getState(BulletAppState.class).getPhysicsSpace();
    }

    @Override
    protected void initialize(Application app) {
        refreshCacheFields();
        pathViewer = new NavPathDebugViewer(assetManager);
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
        pathViewer.show(rm, viewPort);
        nmDebugViewer.show(rm, viewPort);
    }

}
