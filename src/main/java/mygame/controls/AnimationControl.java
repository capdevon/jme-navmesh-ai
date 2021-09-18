package mygame.controls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Spatial;

/**
 * Spatial must have AnimControl to use this control.
 *
 * @author capdevon
 */
public class AnimationControl extends AdapterControl {

    private static final Logger logger = LoggerFactory.getLogger(AnimationControl.class);

    private static final float DEFAULT_BLEND_TIME = 0.15f;

    private AnimControl animControl;
    private AnimChannel animChannel;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        if (spatial != null) {
            animControl = getComponentInChild(AnimControl.class);
            if (animControl != null) {
                logger.info("{} --Animations: {}", spatial, animControl.getAnimationNames());
                animChannel = animControl.createChannel();
            } else {
                logger.warn("AnimControl not found: {}", spatial);
            }
        }
    }

    public void addAnimListener(AnimEventListener listener) {
        animControl.addListener(listener);
    }

    public void removeAnimListener(AnimEventListener listener) {
        animControl.removeListener(listener);
    }

    public void setAnimation(String animName) {
        setAnimation(animName, LoopMode.Loop);
    }

    public void setAnimation(String animName, LoopMode loopMode) {
        if (animControl == null) {
            return;
        }

        if (animControl.getAnimationNames().contains(animName)) {
            if (!animName.equals(animChannel.getAnimationName())) {
                animChannel.setAnim(animName, DEFAULT_BLEND_TIME);
                animChannel.setSpeed(2);
                animChannel.setLoopMode(loopMode);
            }
        } else {
            logger.warn("Cannot find animation named: {}", animName);
        }
    }
}
