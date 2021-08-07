package mygame.controls;

import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    private static final Logger LOGGER = Logger.getLogger(AnimationControl.class.getName());
    
    private static final float DEFAULT_BLEND_TIME = 0.15f;

    private AnimControl animControl;
    private AnimChannel animChannel;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        if (spatial != null) {
            animControl = getComponentInChild(AnimControl.class);
            if (animControl != null) {
	            System.out.println(spatial.getName() + " --Animations: " + animControl.getAnimationNames());
	            animChannel = animControl.createChannel();
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
                LOGGER.log(Level.INFO, "onAnimChanged: {0}", animName);
            }
        } else {
            LOGGER.log(Level.WARNING, "Cannot find animation named: {0}", animName);
        }
    }
}
