/*
 * The MIT License
 *
 * Copyright 2021.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * MODELS/DUNE.J3O:
 * Converted from http://quadropolis.us/node/2584 [Public Domain according to the Tags of this Map]
 */
package com.jme3.recast4j.demo.controls;

import java.io.IOException;
import java.util.Objects;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.recast4j.demo.utils.GameObject;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * Controls the swing animation of doors.
 * 
 * @author Robert
 * @author capdevon
 */
public class DoorSwingControl extends AbstractControl implements AnimEventListener {

    private AnimChannel animChannel;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial == null) {
            return;
        }

        if (animChannel == null) {
            AnimControl animControl = GameObject.getComponentInChild(spatial, AnimControl.class);
            Objects.requireNonNull(animControl, "AnimControl not found: " + spatial);

            animChannel = animControl.createChannel();
            animControl.addListener(this);
        }
    }

    /**
     * @param open
     */
    public void setOpen(boolean open) {
        String animName = (open) ? "open" : "close";
        setAnimation(animName);
    }

    protected void setAnimation(String animName) {
        if (animChannel != null) {
            animChannel.setAnim(animName);
            animChannel.setLoopMode(LoopMode.DontLoop);
        }
    }

    @Override
    protected void controlUpdate(float tpf) {}

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        DoorSwingControl control = new DoorSwingControl();
        // TODO: copy parameters to new Control
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        // TODO: load properties of this Control, e.g.
        // this.value = in.readFloat("name", defaultValue);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
        // TODO: save properties of this Control, e.g.
        // out.write(this.value, "name", defaultValue);
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        System.out.println("onAnimChange: " + spatial + ", AnimName: " + animName);
    }

}
