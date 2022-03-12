package com.jme3.recast4j.demo.controls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.anim.AnimComposer;
import com.jme3.scene.Spatial;

/**
 * Spatial must have AnimComposer to use this control.
 *
 * @author capdevon
 */
public class Animator extends AdapterControl {

    private static final Logger logger = LoggerFactory.getLogger(Animator.class);

    private AnimComposer animComposer;
    private String currAnim;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        if (spatial != null) {
            animComposer = getComponentInChildren(AnimComposer.class);
            requireNonNull(animComposer, AnimComposer.class, Animator.class);

            logger.info("{} --Animations: {}", spatial, animComposer.getAnimClipsNames());
            for (String name: animComposer.getAnimClipsNames()) {
                animComposer.action(name);
            }
        }
    }

    public void setSpeed(float speed) {
        animComposer.setGlobalSpeed(speed);
    }

    public void setAnimation(String animName) {
        if (!animName.equals(currAnim)) {
            animComposer.setCurrentAction(animName);
            currAnim = animName;
        }
    }

}
