package com.jme3.recast4j.demo.utils;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

/**
 *
 * @author capdevon
 */
public class GameObject {

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private GameObject() {
    }

    /**
     * Returns the component of Type type if the game object has one attached, null
     * if it doesn't.
     */
    public static <T extends Control> T getComponent(Spatial sp, Class<T> type) {
        return sp.getControl(type);
    }

    /**
     * Returns the component of Type type in the GameObject or any of its children
     * using depth first search.
     */
    public static <T extends Control> T getComponentInChildren(Spatial sp, final Class<T> type) {
        T control = sp.getControl(type);
        if (control != null) {
            return control;
        }

        if (sp instanceof Node) {
            for (Spatial child : ((Node) sp).getChildren()) {
                control = getComponentInChildren(child, type);
                if (control != null) {
                    return control;
                }
            }
        }

        return null;
    }

    /**
     * Retrieves the component of Type type in the GameObject or any of its parents.
     */
    public static <T extends Control> T getComponentInParent(Spatial sp, Class<T> type) {
        Node parent = sp.getParent();
        while (parent != null) {
            T control = parent.getControl(type);
            if (control != null) {
                return control;
            }
            parent = parent.getParent();
        }
        return null;
    }

}
