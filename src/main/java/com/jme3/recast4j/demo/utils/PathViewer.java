/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.recast4j.demo.utils;

import java.util.List;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author capdevon
 */
public class PathViewer {

    // Asset manager
    protected AssetManager assetManager;
    // Node for attaching debug geometries
    public Node debugNode = new Node("PathViewer");
    // Unshaded material
    public Material debugMat;

    public PathViewer(AssetManager assetManager) {
        this.assetManager = assetManager;
        setupMaterial();
    }
    
    /**
     * Initialize debug material
     */
    private void setupMaterial() {
    	debugMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	debugMat.getAdditionalRenderState().setWireframe(true);
    	debugMat.setColor("Color", ColorRGBA.Orange);
    }
    
    public void drawPathCycle(List<Vector3f> points) {
        for (int i = 0; i < points.size(); i++) {
            int nextIndex = (i + 1) % points.size();
            drawLine(points.get(i), points.get(nextIndex), i);
            drawSphere(points.get(i), 0.2f, i);
        }
    }
    
    public void drawPath(List<Vector3f> points) {
        for (int i = 0; i < points.size(); i++) {
        	int nextIndex = (i + 1);
        	if (nextIndex < points.size()) {
        		drawLine(points.get(i), points.get(nextIndex), i);
        	}
            drawSphere(points.get(i), 0.2f, i);
        }
    }
    
    private void drawLine(Vector3f start, Vector3f end, int i) {
        Line line = new Line(start, end);
        Geometry geo = new Geometry("PathLine-" + i, line);
        geo.setMaterial(debugMat);
        debugNode.attachChild(geo);
    }
    
    private void drawSphere(Vector3f position, float radius, int i) {
    	Sphere sphere = new Sphere(9, 9, 0.1f);
        Geometry geo = new Geometry("PathSphere-" + i, sphere);
        geo.setLocalTranslation(position);
        geo.setMaterial(debugMat);
        debugNode.attachChild(geo);
    }

    /**
     * Helper method to place a colored box at a specific location and fill the pathGeometries list with it,
     * so that later on we can remove all existing pathGeometries (from a previous path finding)
     *
     * @param color The color the box should have
     * @param position The position where the box will be placed
     * @return the box
     */
    public Geometry putBox(ColorRGBA color, Vector3f position) {
        Geometry result = new Geometry("Box", new Box(0.25f, 0.25f, 0.25f));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        result.setMaterial(mat);
        result.setLocalTranslation(position);
        debugNode.attachChild(result);
        return result;
    }

    /**
     * Helper method to place a colored line between two specific locations and fill the pathGeometries list with it,
     * so that later on we can remove all existing pathGeometries (from a previous path finding)
     *
     * @param color The color the box should have
     * @param from The position where the line starts
     * @param to The position where the line is finished.
     * @return the line
     */
    public Geometry putLine(ColorRGBA color, Vector3f from, Vector3f to) {
        Geometry result = new Geometry("Line", new Line(from, to));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setLineWidth(2f);
        result.setMaterial(mat);
        debugNode.attachChild(result);
        return result;
    }

    public void clearPath() {
        debugNode.detachAllChildren();
    }

    /**
     * Render all the debug geometries to the specified view port.
     */
    public void show(RenderManager rm, ViewPort vp) {
        debugNode.updateLogicalState(0f);
        debugNode.updateGeometricState();
        rm.renderScene(debugNode, vp);
    }

}
