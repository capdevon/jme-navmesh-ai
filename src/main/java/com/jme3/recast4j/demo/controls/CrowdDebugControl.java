package com.jme3.recast4j.demo.controls;

import org.recast4j.detour.crowd.CrowdAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Sphere;

/**
 * A debugging control that displays visual, verbose or both debug information
 * about an agents MoveRequestState inside the crowd.
 * 
 * @author Robert
 * @author capdevon
 */
public class CrowdDebugControl extends AbstractControl {

    private static final Logger LOG = LoggerFactory.getLogger(CrowdDebugControl.class.getName());

    private final CrowdAgent agent;
    private final Geometry halo;
    private ColorRGBA curColor;
    private boolean visual;
    private boolean verbose;
    private float timer;
    private float refreshTime = 1f;

    /**
     * This control will display a visual, verbose, or both representation of an
     * agents MoveRequestState while inside the given crowd.
     * 
     * @param agent The agent to look for inside the crowd.
     * @param assetManager The AssetManager
     */
    public CrowdDebugControl(CrowdAgent agent, AssetManager assetManager) {
        this.agent = agent;
        this.halo = createHalo(assetManager);
    }

    /**
     * A Geometry that will be used as the visual representation for the agents
     * MoveRequestState.
     * 
     * @param assetManager
     * @return
     */
    private Geometry createHalo(AssetManager assetManager) {
        Sphere sphere = new Sphere(16, 16, 0.1f);
        Geometry geo = new Geometry("halo", sphere);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        geo.setMaterial(mat);
        geo.setShadowMode(RenderQueue.ShadowMode.Off);
        geo.setLocalTranslation(0, agent.params.height + 0.5f, 0);

        return geo;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            ((Node) spatial).attachChild(halo);
        } else {
            halo.removeFromParent();
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        timer += tpf;
        if (timer > refreshTime) {
            timer = 0;

            if (visual) {
                ColorRGBA col = stateToColor(agent);
                setColor(col);
            }

            if (verbose) {
                LOG.info("<========== BEGIN CrowdDebugControl [{}] index [{}] ==========>", spatial.getName(), agent.idx);
                LOG.info("isActive              [{}]", agent.active);
                LOG.info("MoveRequestState      [{}]", agent.targetState);
                LOG.info("CrowdAgentState       [{}]", agent.state);
                LOG.info("<========== END   CrowdDebugControl [{}] index [{}] ==========>", spatial.getName(), agent.idx);
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

    protected void setColor(ColorRGBA c) {
        if (curColor != c) {
            halo.getMaterial().setColor("Color", c);
            curColor = c;
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
        this.halo.setCullHint(visual ? CullHint.Inherit : CullHint.Always);
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

}
