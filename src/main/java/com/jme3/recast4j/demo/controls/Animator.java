package com.jme3.recast4j.demo.controls;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.anim.AnimComposer;
import com.jme3.anim.SkinningControl;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;

import static com.jme3.recast4j.demo.utils.GameObject.getComponentInChildren;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author capdevon
 */
public class Animator extends AbstractControl {

    private static final Logger logger = LoggerFactory.getLogger(Animator.class);

    private SkinningControl skControl;
    private AnimComposer animComposer;
    private String currAnimName;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        if (spatial != null) {
            skControl = getComponentInChildren(spatial, SkinningControl.class);
            Objects.requireNonNull(skControl, "SkinningControl not found: " + spatial);

            animComposer = getComponentInChildren(spatial, AnimComposer.class);
            Objects.requireNonNull(animComposer, "AnimComposer not found: " + spatial);

            configureAnimClips();
        }
    }

    private void configureAnimClips() {
        for (String name : animComposer.getAnimClipsNames()) {
            logger.info("{}, make action [{}]", spatial, name);
            animComposer.action(name);
        }
    }

    public void setHWSkinning(boolean enabled) {
        skControl.setHardwareSkinningPreferred(enabled);
    }

    public void setSpeed(float speed) {
        animComposer.setGlobalSpeed(speed);
    }

    public void setAnimation(String animName) {
        if (!animName.equals(currAnimName)) {
            animComposer.setCurrentAction(animName);
            currAnimName = animName;
        }
    }

    public String getCurrentAnimName() {
        return currAnimName;
    }

    @Override
    protected void controlUpdate(float tpf) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // TODO Auto-generated method stub
    }

}
