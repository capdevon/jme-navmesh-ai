package com.jme3.recast4j.demo.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.InputManager;
import com.jme3.recast4j.demo.utils.MeshDataDebugViewer;
import com.jme3.recast4j.demo.utils.PathViewer;
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

    public PathViewer pathViewer;
    public MeshDataDebugViewer meshDebugViewer;

    // cache fields
    public SimpleApplication app;
    public AppSettings settings;
    public AppStateManager stateManager;
    public AssetManager assetManager;
    public InputManager inputManager;
    public ViewPort viewPort;
    public Camera camera;
    public Node rootNode;
    public Node guiNode;

    protected void refreshCacheFields() {
        this.app 			= (SimpleApplication) getApplication();
        this.settings 		= app.getContext().getSettings();
        this.stateManager 	= app.getStateManager();
        this.assetManager 	= app.getAssetManager();
        this.inputManager 	= app.getInputManager();
        this.viewPort 		= app.getViewPort();
        this.camera 		= app.getCamera();
        this.rootNode 		= app.getRootNode();
        this.guiNode 		= app.getGuiNode();
    }

    protected PhysicsSpace getPhysicsSpace() {
        return getState(BulletAppState.class).getPhysicsSpace();
    }

    @Override
    protected void initialize(Application app) {
        // TODO Auto-generated method stub
        refreshCacheFields();
        pathViewer = new PathViewer(assetManager);
        meshDebugViewer = new MeshDataDebugViewer(assetManager);
    }

    @Override
    protected void cleanup(Application app) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onEnable() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onDisable() {
        // TODO Auto-generated method stub
    }

    @Override
    public void render(RenderManager rm) {
        pathViewer.show(rm, viewPort);
        meshDebugViewer.show(rm, viewPort);
    }

}
