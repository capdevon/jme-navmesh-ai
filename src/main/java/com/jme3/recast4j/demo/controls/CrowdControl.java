package com.jme3.recast4j.demo.controls;

import java.util.Objects;

import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgent.MoveRequestState;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import mygame.controls.AnimationControl;

/**
 * 
 * @author capdevon
 */
public class CrowdControl extends AbstractControl {

    private CrowdAgent agent;
    private AnimationControl animator;

    /**
     * Constructor.
     * @param agent
     */
    public CrowdControl(CrowdAgent agent) {
        this.agent = agent;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            this.animator = spatial.getControl(AnimationControl.class);
            Objects.requireNonNull(animator, "AnimationControl not found: " + spatial);
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (agent.targetState == MoveRequestState.DT_CROWDAGENT_TARGET_VALID) {
            animator.setAnimation("Walk");
        } else {
            animator.setAnimation("Idle");
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // do nothing.
    }

}