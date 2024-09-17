package com.jme3.recast4j.demo.controls;

import org.recast4j.detour.crowd.CrowdAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.demo.utils.Circle;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Line;

/**
 * A debugging control that displays visual, verbose or both debug information
 * about an agents MoveRequestState inside the crowd.
 * 
 * @author capdevon
 */
public class CrowdDebugControl extends AbstractControl {

    private static final Logger logger = LoggerFactory.getLogger(CrowdDebugControl.class.getName());

    private CrowdAgent agent;
    private Node debugNode;
    private Material wireMaterial;
    private ColorRGBA curColor;
    private boolean visual = true;
    private boolean verbose = false;
    private float timer = 0;
    private float refreshTime = 0.1f;

    /**
     * This control will display a visual, verbose, or both representation of an
     * agents MoveRequestState while inside the given crowd.
     * 
     * @param agent        The agent to look for inside the crowd.
     * @param assetManager The AssetManager
     */
    public CrowdDebugControl(CrowdAgent agent, AssetManager assetManager) {
        this.agent = agent;
        setupDebugNode(assetManager);
    }

    private void setupDebugNode(AssetManager assetManager) {
        wireMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        wireMaterial.getAdditionalRenderState().setWireframe(true);
        wireMaterial.setColor("Color", ColorRGBA.White);

        float radius = agent.params.radius;
        float height = agent.params.height;
        debugNode = createDebugCylinder(radius, height);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            ((Node) spatial).attachChild(debugNode);
        } else {
            debugNode.removeFromParent();
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        timer += tpf;
        if (timer > refreshTime) {
            timer = 0;

            if (visual) {
                ColorRGBA color = stateToColor(agent);
                setColor(color);
            }

            if (verbose) {
                logger.info("<========== BEGIN CrowdDebugControl [{}] index [{}] ==========>", spatial.getName(), agent.idx);
                logger.info("isActive              [{}]", agent.active);
                logger.info("MoveRequestState      [{}]", agent.targetState);
                logger.info("CrowdAgentState       [{}]", agent.state);
                logger.info("<========== END   CrowdDebugControl [{}] index [{}] ==========>", spatial.getName(), agent.idx);
            }
        }
    }

    private ColorRGBA stateToColor(CrowdAgent agent) {
        if (!agent.active) {
            return ColorRGBA.Gray;
        }

        switch (agent.targetState) {
            case DT_CROWDAGENT_TARGET_REQUESTING:
            case DT_CROWDAGENT_TARGET_WAITING_FOR_QUEUE:
                return ColorRGBA.Orange;
            case DT_CROWDAGENT_TARGET_WAITING_FOR_PATH:
                return ColorRGBA.Yellow;
            case DT_CROWDAGENT_TARGET_FAILED:
                return ColorRGBA.Red;
            case DT_CROWDAGENT_TARGET_VALID:
                return ColorRGBA.Green;
            case DT_CROWDAGENT_TARGET_VELOCITY:
                return ColorRGBA.Cyan;
            case DT_CROWDAGENT_TARGET_NONE:
                return ColorRGBA.Blue;
            default:
                return ColorRGBA.Black;
                //throw new IllegalArgumentException("Unknown MoveRequestState: " + agent.targetState);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    private void setColor(ColorRGBA color) {
        if (curColor != color) {
            wireMaterial.setColor("Color", color);
            curColor = color;
        }
    }

    /**
     * If true, the halo is not culled.
     * 
     * @return The visual state of the halo.
     */
    public boolean isVisual() {
        return visual;
    }

    /**
     * Sets the cullHint of the halo to inherit if true, otherwise always culled.
     * 
     * @param visual the visual to set.
     */
    public void setVisual(boolean visual) {
        this.debugNode.setCullHint(visual ? CullHint.Inherit : CullHint.Always);
        this.visual = visual;
    }

    /**
     * If true, logging is on.
     * 
     * @return Whether logging is on or off.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Turns logging on or off.
     * 
     * @param verbose True for logging.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private Node createDebugCylinder(float radius, float height) {

        Node body = new Node("Cylinder");

        for (int i = 0; i < 4; i++) {
            float angle = FastMath.HALF_PI * i;
            float x = FastMath.sin(angle) * radius;
            float z = FastMath.cos(angle) * radius;
            Vector3f start = new Vector3f(x, 0, z);
            Vector3f end = new Vector3f(x, height, z);

            Geometry line = createLine(start, end);
            line.setName("Line-" + i);
            body.attachChild(line);
        }

        Geometry c1 = createCircle(radius);
        c1.setName("Bottom-Circle");
        c1.setLocalTranslation(0, 0, 0);
        body.attachChild(c1);

        Geometry c2 = createCircle(radius);
        c1.setName("Top-Circle");
        c2.setLocalTranslation(0, height, 0);
        body.attachChild(c2);

        return body;
    }

    private Geometry createCircle(float radius) {
        int samples = 16;
        Circle circle = new Circle(radius, samples);
        return makeGeometry("Circle", circle);
    }

    private Geometry createLine(Vector3f start, Vector3f end) {
        Line line = new Line(start, end);
        return makeGeometry("Line", line);
    }
    
    private Geometry makeGeometry(String name, Mesh mesh) {
        Geometry geo = new Geometry(name, mesh);
        geo.setMaterial(wireMaterial);
        geo.setShadowMode(ShadowMode.Off);
        return geo;
    }

}
