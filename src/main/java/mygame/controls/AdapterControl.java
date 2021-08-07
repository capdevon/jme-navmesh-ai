package mygame.controls;

import java.util.ArrayList;
import java.util.List;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.util.Objects;

/**
 * 
 * @author capdevon
 */
public class AdapterControl extends AbstractControl {
    
    /**
     * 
     * @param obj
     * @param to
     * @param ts 
     */
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
        final List<Node> lst = new ArrayList<>(10);
        spatial.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node node) {
                if (node.getControl(clazz) != null) {
                    lst.add(node);
                }
            }
        });
        return (T[]) lst.toArray();
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
        T control = spatial.getControl(clazz);
        return control;
    }
    
    /**
     * Returns the component of Type type in the GameObject or any of its
     * children using depth first search.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public <T extends Control> T getComponentInChild(final Class<T> clazz) {
        return getComponentInChild(spatial, clazz);
    }
    
    private <T extends Control> T getComponentInChild(Spatial spatial, final Class<T> clazz) {
        T control = spatial.getControl(clazz);
        if (control != null) {
            return control;
        }

        if (spatial instanceof Node) {
            for (Spatial child : ((Node) spatial).getChildren()) {
                control = getComponentInChild(child, clazz);
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
     * @param clazz
     * @return
     */
    public <T extends Control> T getComponentInParent(Class<T> clazz) {
        return getComponentInParent(spatial, clazz);
    }
    
    private <T extends Control> T getComponentInParent(Spatial spatial, Class<T> clazz) {
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

    @Override
    protected void controlUpdate(float tpf) {
        //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //To change body of generated methods, choose Tools | Templates.
    }

}
