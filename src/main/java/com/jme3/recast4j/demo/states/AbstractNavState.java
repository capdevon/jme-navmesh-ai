package com.jme3.recast4j.demo.states;

import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.debug.NavMeshDebugViewer;
import com.jme3.recast4j.debug.NavPathDebugViewer;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;

/**
 * 
 * @author capdevon
 */
public abstract class AbstractNavState extends SimpleAppState {

    NavPathDebugViewer pathViewer;
    NavMeshDebugViewer nmDebugViewer;

    // Node for attaching debug geometries
    Node debugNode = new Node("DebugViewer");

    @Override
    protected void initialize(Application app) {
        refreshCacheFields();
        pathViewer = new NavPathDebugViewer(app.getAssetManager());
        nmDebugViewer = new NavMeshDebugViewer(app.getAssetManager());
    }

    @Override
    protected void cleanup(Application app) {}

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}

    @Override
    public void render(RenderManager rm) {
        pathViewer.show(rm, viewPort);
        nmDebugViewer.show(rm, viewPort);

        debugNode.updateLogicalState(0f);
        debugNode.updateGeometricState();
        rm.renderScene(debugNode, viewPort);
    }

    public void clearDebug() {
        debugNode.detachAllChildren();
    }

    /**
     * Helper method to place a colored box at a specific location.
     *
     * @param color    The color the box should have
     * @param position The position where the box will be placed
     * @return the box Geometry
     */
    public Geometry drawBox(ColorRGBA color, Vector3f position) {
        float size = 0.15f;
        Geometry result = new Geometry("Box", new Box(size, size, size));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        result.setMaterial(mat);
        result.setLocalTranslation(position);
        debugNode.attachChild(result);
        return result;
    }

    /**
     * Helper method to place a colored line between two specific locations.
     *
     * @param color The color the box should have
     * @param from  The position where the line starts
     * @param to    The position where the line is finished.
     * @return the line Geometry
     */
    public Geometry drawLine(ColorRGBA color, Vector3f from, Vector3f to) {
        Geometry result = new Geometry("Line", new Line(from, to));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setLineWidth(2f);
        result.setMaterial(mat);
        debugNode.attachChild(result);
        return result;
    }

}
