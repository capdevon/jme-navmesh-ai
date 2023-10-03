package com.jme3.recast4j.demo.utils;

import java.util.ArrayList;
import java.util.List;

import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
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
     */
    public static <T extends Control> T getComponent(Spatial sp, Class<T> type) {
       return sp.getControl(type);
    }
    
    /**
     * Returns all components of Type type in the GameObject.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Control> List<T> getComponents(Spatial sp, Class<T> type) {
        List<T> lst = new ArrayList<>(3);
        for (int i = 0; i < sp.getNumControls(); i++) {
            T control = (T) sp.getControl(i);
            if (type.isAssignableFrom(control.getClass())) {
                lst.add(control);
            }
        }
        return lst;
    }
    
    /**
     * Returns all components of Type type in the GameObject or any of its
     * children using depth first search. Works recursively.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Control> List<T> getComponentsInChildren(Spatial subtree, Class<T> type) {
        List<T> lst = new ArrayList<>(3);
        subtree.depthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial sp) {
                for (int i = 0; i < sp.getNumControls(); i++) {
                    T control = (T) sp.getControl(i);
                    if (type.isAssignableFrom(control.getClass())) {
                        lst.add(control);
                    }
                }
            }
        });
        return lst;
    }

    /**
     * Returns the component of Type type in the GameObject or any of its
     * children using depth first search.
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
     * Retrieves the component of Type type in the GameObject or any of its
     * parents.
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
