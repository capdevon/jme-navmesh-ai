/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.controls;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.recast4j.ai.NavMeshAgent;
import com.jme3.scene.Spatial;

/**
 *
 * @author capdevon
 */
public class PCControl extends AdapterControl implements AnimEventListener {

    private NavMeshAgent agent;
    private AnimationControl animator;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            this.agent = getComponent(NavMeshAgent.class);
            requireNonNull(agent, NavMeshAgent.class, PCControl.class);

            this.animator = getComponent(AnimationControl.class);
            requireNonNull(animator, AnimationControl.class, PCControl.class);

            animator.addAnimListener(this);
        }
    }

    @Override
    public void controlUpdate(float tpf) {
        if (!agent.pathPending() && agent.isAtGoal()) {
            animator.setAnimation("Idle");
        } else {
            animator.setAnimation("Walk");
        }
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        System.out.println("onAnimChange: " + animName);
    }

}
