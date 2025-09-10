package com.jme3.recast4j.demo.states;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.InputManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

/**
 *
 * @author capdevon
 */
public abstract class SimpleAppState extends BaseAppState {

    // cache fields
    protected AppSettings settings;
    protected AppStateManager stateManager;
    protected AssetManager assetManager;
    protected InputManager inputManager;
    protected ViewPort viewPort;
    protected Camera camera;
    protected Node rootNode;
    protected Node guiNode;

    @Override
    protected void initialize(Application app) {
        refreshCacheFields();
        simpleInit();
    }

    protected abstract void simpleInit();

    protected void refreshCacheFields() {
        SimpleApplication app = (SimpleApplication) getApplication();
        this.settings       = app.getContext().getSettings();
        this.stateManager   = app.getStateManager();
        this.assetManager   = app.getAssetManager();
        this.inputManager   = app.getInputManager();
        this.viewPort       = app.getViewPort();
        this.camera         = app.getCamera();
        this.rootNode       = app.getRootNode();
        this.guiNode        = app.getGuiNode();
    }

    protected PhysicsSpace getPhysicsSpace() {
        return getState(BulletAppState.class, true).getPhysicsSpace();
    }
    
    /**
     * Finds a GameObject by name and returns it.
     * 
     * @param childName
     * @return
     */
    public Spatial find(final String childName) {
        Spatial child = rootNode.getChild(childName);
        String errorMsg = String.format("The spatial %s could not be found", childName);
        return Objects.requireNonNull(child, errorMsg);
    }

    /**
     * Returns a list of GameObjects tagged tag. 
     * Returns empty list if no GameObject was found.
     * 
     * @param tagName
     * @return
     */
    public List<Spatial> findGameObjectsWithTag(final String tagName) {
        final List<Spatial> lst = new ArrayList<>();
        rootNode.breadthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial node) {
                if (tagName.equals(node.getUserData("TagName"))) {
                    lst.add(node);
                }
            }
        });
        return lst;
    }

    /**
     * Returns one GameObject tagged tag. 
     * Returns null if no GameObject was found.
     * 
     * @param tagName
     * @return
     */
    public Spatial findWithTag(final String tagName) {
        List<Spatial> lst = findGameObjectsWithTag(tagName);
        return lst.isEmpty() ? null : lst.get(0);
    }

    /**
     * By default the parent of the new object is null.
     */
    public Spatial instantiate(Spatial model, Vector3f position, Quaternion rotation) {
        Spatial sp = model.clone();
        sp.setLocalTranslation(position);
        sp.setLocalRotation(rotation);
        return sp;
    }

    public Spatial instantiate(Spatial model, Vector3f position, Quaternion rotation, Node parent) {
        Spatial sp = instantiate(model, position, rotation);
        parent.attachChild(sp);
        return sp;
    }

    /**
     * By default the parent of the new object is null.
     */
    public Spatial instantiate(String assetName, Vector3f position, Quaternion rotation) {
        Spatial sp = assetManager.loadModel(assetName);
        sp.setLocalTranslation(position);
        sp.setLocalRotation(rotation);
        return sp;
    }

    public Spatial instantiate(String assetName, Vector3f position, Quaternion rotation, Node parent) {
        Spatial sp = instantiate(assetName, position, rotation);
        parent.attachChild(sp);
        return sp;
    }

}
