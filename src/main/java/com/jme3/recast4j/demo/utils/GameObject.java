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
    private GameObject() {}

    /**
     * Returns the component of Type type if the game object has one attached,
     * null if it doesn't.
     *
     * @param <T>
     * @param spatial
     * @param clazz
     * @return
     */
    public static <T extends Control> T getComponent(Spatial spatial, Class<T> clazz) {
        return spatial.getControl(clazz);
    }

    /**
     * Returns the component of Type type in the GameObject or any of its
     * children using depth first search.
     *
     * @param <T>
     * @param spatial
     * @param clazz
     * @return
     */
    public static <T extends Control> T getComponentInChildren(Spatial spatial, final Class<T> clazz) {
        T control = spatial.getControl(clazz);
        if (control != null) {
            return control;
        }

        if (spatial instanceof Node) {
            for (Spatial child : ((Node) spatial).getChildren()) {
                control = getComponentInChildren(child, clazz);
                if (control != null) {
                    return control;
                }
            }
        }

        return null;
    }

    /**
     * Retrieves the component of Type type in the GameObject or any of its
     * parents.
     *
     * @param <T>
     * @param spatial
     * @param clazz
     * @return
     */
    public static <T extends Control> T getComponentInParent(Spatial spatial, Class<T> clazz) {
        Node parent = spatial.getParent();
        while (parent != null) {
            T control = parent.getControl(clazz);
            if (control != null) {
                return control;
            }
            parent = parent.getParent();
        }
        return null;
    }

}
