package com.jme3.recast4j.debug;

import com.jme3.asset.AssetManager;
import com.jme3.environment.util.BoundingSphereDebug;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author capdevon
 */
public class DebugHelper {

    protected final AssetManager assetManager;

    // Node for attaching debug geometries.
    public Node debugNode = new Node("DebugShapeNode");

    public ColorRGBA color = ColorRGBA.Red;
    public float lineWidth = 1f;

    public DebugHelper(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void clear() {
        debugNode.detachAllChildren();
    }

    public Geometry drawArrow(Vector3f dir) {
        Arrow arrow = new Arrow(dir);
        Geometry geo = new Geometry("Arrow", arrow);
        geo.setMaterial(createWireMat());
        return geo;
    }

    /**
     * Draws a line starting at from towards to.
     *
     * @param from
     * @param to
     */
    public Geometry drawLine(Vector3f from, Vector3f to) {
        Line line = new Line(from, to);
        Geometry geo = new Geometry("Line", line);
        geo.setMaterial(createWireMat());
        debugNode.attachChild(geo);
        return geo;
    }

    /**
     * Draw a solid box with center and size.
     *
     * @param center
     * @param size
     */
    public Geometry drawCube(Vector3f center, Vector3f size) {
        Box box = new Box(size.x, size.y, size.z);
        Geometry geo = new Geometry("Box", box);
        geo.setMaterial(createColorMat());
        geo.setLocalTranslation(center);
        debugNode.attachChild(geo);
        return geo;
    }

    public Geometry drawCube(Vector3f center, float size) {
        return drawCube(center, new Vector3f(size, size, size));
    }

    /**
     * Use a wireframe cube (com.jme3.scene.debug.WireBox) as a stand-in object
     * to see whether your code scales, positions, or orients, loaded models
     * right.
     *
     * @param center
     * @param size
     */
    public Geometry drawWireCube(Vector3f center, Vector3f size) {
        WireBox box = new WireBox(size.x, size.y, size.z);
        Geometry geo = new Geometry("WireBox", box);
        geo.setMaterial(createWireMat());
        geo.setLocalTranslation(center);
        debugNode.attachChild(geo);
        return geo;
    }

    public Geometry drawWireCube(Vector3f center, float size) {
        return drawWireCube(center, new Vector3f(size, size, size));
    }

    /**
     * Draws a solid sphere with center and radius.
     *
     * @param center
     * @param radius
     */
    public Geometry drawSphere(Vector3f center, float radius) {
        Sphere sphere = new Sphere(10, 30, radius);
        Geometry geo = new Geometry("Sphere", sphere);
        geo.setMaterial(createColorMat());
        geo.setLocalTranslation(center);
        debugNode.attachChild(geo);
        return geo;
    }

    /**
     * Use a wireframe sphere (com.jme3.environment.util.BoundingSphereDebug) as
     * a stand-in object to see whether your code scales, positions, or orients,
     * loaded models right.
     *
     * @param center
     * @param radius
     */
    public Geometry drawWireSphere(Vector3f center, float radius) {
        BoundingSphereDebug sphere = new BoundingSphereDebug();
        Geometry geo = new Geometry("WireSphere", sphere);
        geo.setMaterial(createWireMat());
        geo.setLocalTranslation(center);
        geo.setLocalScale(radius);
        debugNode.attachChild(geo);
        return geo;
    }

    private Material createColorMat() {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        return mat;
    }

    private Material createWireMat() {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setLineWidth(lineWidth);
        return mat;
    }

    /**
     * Render all the debug geometries to the specified view port.
     *
     * @param rm the render manager (not null)
     * @param vp the view port (not null)
     */
    public void show(RenderManager rm, ViewPort vp) {
        debugNode.updateLogicalState(0);
        debugNode.updateGeometricState();
        rm.renderScene(debugNode, vp);
    }

}
