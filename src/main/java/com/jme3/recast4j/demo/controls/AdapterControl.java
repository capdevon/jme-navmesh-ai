package com.jme3.recast4j.demo.controls;

import java.util.List;

import com.jme3.recast4j.demo.utils.GameObject;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author capdevon
 */
public abstract class AdapterControl extends AbstractControl {

    /**
     * Returns all components of Type type in the GameObject.
     */
    public List<Node> getComponents(Class<? extends Control> clazz) {
        return GameObject.getComponents(spatial, clazz);
    }

    /**
     * Returns the component of Type type if the game object has one attached,
     * null if it doesn't.
     */
    public <T extends Control> T getComponent(Class<T> clazz) {
        return GameObject.getComponent(spatial, clazz);
    }

    /**
     * Returns the component of Type type in the GameObject or any of its
     * children using depth first search.
     */
    public <T extends Control> T getComponentInChildren(final Class<T> clazz) {
        return GameObject.getComponentInChildren(spatial, clazz);
    }

    /**
     * Retrieves the component of Type type in the GameObject or any of its
     * parents.
     */
    public <T extends Control> T getComponentInParent(Class<T> clazz) {
        return GameObject.getComponentInParent(spatial, clazz);
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

}
