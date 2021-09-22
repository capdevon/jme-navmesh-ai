package com.jme3.recast4j.demo.controls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Spatial;

/**
 * Spatial must have AnimControl to use this control.
 *
 * @author capdevon
 */
public class Animator extends AdapterControl {

    private static final Logger logger = LoggerFactory.getLogger(Animator.class);

    private static final float DEFAULT_BLEND_TIME = 0.15f;

    private AnimControl animControl;
    private AnimChannel animChannel;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        if (spatial != null) {
            animControl = getComponentInChild(AnimControl.class);
            requireNonNull(animControl, AnimControl.class, Animator.class);

            logger.info("{} --Animations: {}", spatial, animControl.getAnimationNames());
            animChannel = animControl.createChannel();
        }
    }

    public void setSpeed(float speed) {
        animChannel.setSpeed(speed);
    }
    
    public void setAnimation(String animName) {
        setAnimation(animName, LoopMode.Loop);
    }

    public void setAnimation(String animName, LoopMode loopMode) {
        if (animControl.getAnimationNames().contains(animName)) {
            if (!animName.equals(animChannel.getAnimationName())) {
                animChannel.setAnim(animName, DEFAULT_BLEND_TIME);
                animChannel.setLoopMode(loopMode);
            }
        } else {
            logger.warn("Cannot find animation named: {}", animName);
        }
    }

}