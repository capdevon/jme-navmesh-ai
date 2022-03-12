package com.jme3.recast4j.demo.controls;

import java.util.Objects;

import com.jme3.recast4j.demo.utils.GameObject;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author capdevon
 */
public class AdapterControl extends AbstractControl {

    public void requireNonNull(Object obj, Class to, Class ts) {
        String message = "Error: Component of type %s on GameObject %s expected to find an object of type %s, but none were found.";
        Objects.requireNonNull(obj, String.format(message, ts.getName(), spatial.toString(), to.getName()));
    }

    /**
     * Returns all components of Type type in the GameObject.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public <T extends Control> T[] getComponents(Class<T> clazz) {
        return GameObject.getComponents(spatial, clazz);
    }

    /**
     * Returns the component of Type type if the game object has one attached,
     * null if it doesn't.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public <T extends Control> T getComponent(Class<T> clazz) {
        return spatial.getControl(clazz);
    }

    /**
     * Returns the component of Type type in the GameObject or any of its
     * children using depth first search.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public <T extends Control> T getComponentInChildren(final Class<T> clazz) {
        return GameObject.getComponentInChildren(spatial, clazz);
    }

    /**
     * Retrieves the component of Type type in the GameObject or any of its
     * parents.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public <T extends Control> T getComponentInParent(Class<T> clazz) {
        return GameObject.getComponentInParent(spatial, clazz);
    }

    @Override
    protected void controlUpdate(float tpf) {
        //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //To change body of generated methods, choose Tools | Templates.
    }

}
