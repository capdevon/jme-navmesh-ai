package com.jme3.recast4j.ai;

import java.util.List;
import java.util.Objects;

import com.jme3.asset.AssetManager;
import com.jme3.environment.util.BoundingSphereDebug;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Line;

/**
 *
 * @author capdevon
 */
public class NavMeshAgentDebug extends AbstractControl {

    // Asset manager
    protected AssetManager assetManager;
    // Node for attaching debug geometries
    private Node debugNode = new Node("NavPathDebugViewer");
    // Unshaded material
    private Material debugMat;
    private BoundingSphereDebug sphere = new BoundingSphereDebug();
    private float pointSize = 0.1f;

    private NavMeshAgent agent;

    public NavMeshAgentDebug(AssetManager assetManager) {
        this.assetManager = assetManager;
        setupMaterial();
    }

    @Override
    public void setSpatial(Spatial sp) {
        super.setSpatial(sp);
        if (spatial != null) {
            this.agent = spatial.getControl(NavMeshAgent.class);
            Objects.requireNonNull(agent, "NavMeshAgent not found: " + spatial);
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        clearPath();
        if (!agent.getPath().isEmpty() && !agent.pathPending()) {
            drawPath(agent.getPath().getCorners());
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // Render all the debug geometries to the specified view port.
        debugNode.updateLogicalState(0f);
        debugNode.updateGeometricState();
        rm.renderScene(debugNode, vp);
    }

    /**
     * Initialize debug material
     */
    private void setupMaterial() {
        debugMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        debugMat.getAdditionalRenderState().setWireframe(true);
        debugMat.setColor("Color", ColorRGBA.Orange);
    }

    /**
     * Displays a motion path showing each waypoint.
     */
    private void drawPath(List<Vector3f> corners) {
        Vector3f prevCorner = spatial.getWorldTranslation();
        for (Vector3f corner : corners) {
            drawLine(prevCorner, corner);
            drawSphere(corner, pointSize);
            prevCorner = corner;
        }
    }

    private void drawLine(Vector3f start, Vector3f end) {
        Line line = new Line(start, end);
        Geometry geo = new Geometry("PathLine", line);
        geo.setMaterial(debugMat);
        debugNode.attachChild(geo);
    }

    private void drawSphere(Vector3f position, float radius) {
        Geometry geo = new Geometry("PathSphere", sphere);
        geo.setMaterial(debugMat);
        geo.setLocalTranslation(position);
        geo.setLocalScale(radius);
        debugNode.attachChild(geo);
    }

    private void clearPath() {
        debugNode.detachAllChildren();
    }
    
    public float getPointSize() {
        return pointSize;
    }

    public void setPointSize(float pointSize) {
        this.pointSize = pointSize;
    }

}
