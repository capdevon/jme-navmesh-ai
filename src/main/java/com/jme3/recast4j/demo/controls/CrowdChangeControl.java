/*
 * The MIT License
 *
 * Copyright 2019 .
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

import com.jme3.recast4j.Detour.Crowd.Crowd;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import org.recast4j.detour.crowd.CrowdAgent;

/**
 * Changes crowds automatically. Firstly removes the CrowdAgent from the 
 * curCrowd, then updates the curCrowd variable with the new crowd and 
 * crowdAgent settings.
 * 
 * @author Robert
 */
public class CrowdChangeControl extends AbstractControl {

    private Crowd crowd;
    private Crowd curCrowd;
    private CrowdAgent crowdAgent;
    private CrowdAgent curCrowdAgent;

    public CrowdChangeControl(Crowd crowd, CrowdAgent crowdAgent) {
        this.crowd = crowd;
        this.crowdAgent = crowdAgent;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (curCrowd != crowd || curCrowdAgent != crowdAgent) {
            //When first adding control, currCrowd and curCrowdAgent will be null
            //so skip crowd interaction and just set variables.
            if (curCrowd == null) {
                curCrowd = crowd;
                curCrowdAgent = crowdAgent;
            } else {
                curCrowd.removeAgent(curCrowdAgent);
                curCrowd = crowd;
                curCrowdAgent = crowdAgent;
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    /**
     * @param crowd the crowd to set
     * @param crowdAgent
     */
    public void setCrowd(Crowd crowd, CrowdAgent crowdAgent) {
        this.crowd = crowd;
        this.crowdAgent = crowdAgent;
    }

}

