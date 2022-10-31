package com.jme3.recast4j.demo.controls;

import java.util.Objects;

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
            Objects.requireNonNull(agent, "NavMeshAgent not found: " + spatial);

            this.animator = getComponent(Animator.class);
            Objects.requireNonNull(animator, "Animator not found: " + spatial);
        }
    }

    @Override
    public void controlUpdate(float tpf) {
        if (agent.remainingDistance() < agent.stoppingDistance && !agent.pathPending()) {
            animator.setAnimation("Idle");
            animator.setSpeed(1);
        } else {
            logger.debug("remainingDistance {}", agent.remainingDistance());
            animator.setAnimation("Walk");
            animator.setSpeed(2);
        }
    }

}
