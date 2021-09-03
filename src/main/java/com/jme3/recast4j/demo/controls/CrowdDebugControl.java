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

import org.recast4j.detour.crowd.CrowdAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.math.ColorRGBA;
import com.jme3.recast4j.Detour.Crowd.JmeCrowd;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.AbstractControl;

/**
 * A debugging control that displays visual, verbose or both debug information 
 * about an agents MoveRequestState inside the crowd. 
 * 
 * @author Robert
 */
public class CrowdDebugControl extends AbstractControl {

    private static final Logger LOG = LoggerFactory.getLogger(CrowdDebugControl.class.getName());
    
    private CrowdAgent agent;
    private JmeCrowd crowd;
    private Geometry halo;
    private ColorRGBA curColor;
    private boolean visual;
    private boolean verbose;
    private float timer;
    private float refreshTime = 1f;
    
    /**
     * This control will display a visual, verbose, or both representation of an 
     * agents MoveRequestState while inside the given crowd.
     *      White   = isForming
     *      Magenta = isMoving / MoveRequestState.DT_CROWDAGENT_TARGET_VALID
     *      Cyan    = hasNoTarget / MoveRequestState.DT_CROWDAGENT_TARGET_NONE
     *      Black   = none of the above
     * 
     * @param crowd The crowd the agent is a member of.
     * @param agent The agent to look for inside the crowd.
     * @param halo A Geometry that will be used as the visual representation for
     * the agents MoveRequestState.
     */
    public CrowdDebugControl(JmeCrowd crowd, CrowdAgent agent, Geometry halo) {
        this.crowd = crowd;
        this.agent = agent;
        this.halo = halo;
    }

    @Override
    protected void controlUpdate(float tpf) {
        timer += tpf;
        if (timer > refreshTime) {
        	
            if (visual) {
                if (crowd.hasValidTarget(agent)) {
                	setColor(ColorRGBA.Green);
                } else if (crowd.hasNoTarget(agent)) {
                	setColor(ColorRGBA.Blue);
                } else {
                	setColor(ColorRGBA.Red);
                }
            }
            
            if (verbose) {
                LOG.info("<========== BEGIN CrowdDebugControl [{}] index [{}] ==========>", spatial.getName(), agent.idx);
                LOG.info("isActive              [{}]", agent.active);
                LOG.info("MoveRequestState      [{}]", agent.targetState );
                LOG.info("CrowdAgentState       [{}]", agent.state);
                LOG.info("<========== END   CrowdDebugControl [{}] index [{}] ==========>", spatial.getName(), agent.idx);
            }
            
            timer = 0;
        }
    }
    
    protected void setColor(ColorRGBA c) {
    	if (curColor != c) {
            halo.getMaterial().setColor("Color", c);
            curColor = c;
        }
    }

    @Override 
    public void setSpatial(Spatial spatial) {   
        super.setSpatial(spatial);
        //Add the halo to the spatial.
        if (spatial != null){       
            ((Node) spatial).attachChild(halo);
        } else {
            halo.removeFromParent(); //Must remove when control removed.
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    /**
     * If true, the halo is not culled.
     * 
     * @return The visual state of the halo.
     */
    public boolean isVisual() {
        return visual;
    }

    /**
     * Sets the cullHint of the halo to inherit if true, otherwise always culled.
     * 
     * @param visual the visual to set.
     */
	public void setVisual(boolean visual) {
		this.halo.setCullHint(visual ? CullHint.Inherit : CullHint.Always);
		this.visual = visual;
	}

    /**
     * If true, logging is on. 
     * 
     * @return Whether logging is on or off.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Turns logging on or off.
     * 
     * @param verbose True for logging.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * @param agent the agent to set
     */
    public void setAgent(CrowdAgent agent) {
        this.agent = agent;
    }

    /**
     * @param crowd the crowd to set
     */
    public void setCrowd(JmeCrowd crowd) {
        this.crowd = crowd;
    }

}

