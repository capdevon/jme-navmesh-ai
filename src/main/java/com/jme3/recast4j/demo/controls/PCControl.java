/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.recast4j.demo.controls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.recast4j.ai.NavMeshAgent;
import com.jme3.scene.Spatial;

/**
 *
 * @author capdevon
 */
public class PCControl extends AdapterControl {

    private static final Logger logger = LoggerFactory.getLogger(PCControl.class);

    private NavMeshAgent agent;
    private Animator animator;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            this.agent = getComponent(NavMeshAgent.class);
            requireNonNull(agent, NavMeshAgent.class, PCControl.class);

            this.animator = getComponent(Animator.class);
            requireNonNull(animator, Animator.class, PCControl.class);
        }
    }

    @Override
    public void controlUpdate(float tpf) {
        if (agent.isAtGoal() && !agent.pathPending()) {
            animator.setAnimation("Idle");
            animator.setSpeed(1);
        } else {
            logger.debug("remainingDistance {}", agent.remainingDistance());
            animator.setAnimation("Walk");
            animator.setSpeed(2);
        }
    }

}