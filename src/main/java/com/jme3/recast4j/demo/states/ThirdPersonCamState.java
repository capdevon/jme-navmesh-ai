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
package com.jme3.recast4j.demo.states;

import java.util.Objects;
import java.util.logging.Logger;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.CameraInput;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author Robert
 */
public class ThirdPersonCamState extends BaseAppState {

    private static final Logger logger = Logger.getLogger(ThirdPersonCamState.class.getName());

    private Camera cam;
    private InputManager inputManager;

    @Override
    protected void initialize(Application app) {
        this.cam = app.getCamera();
        this.inputManager = app.getInputManager();

        Node character = (Node) find("jaime");
        addHeadNode(character);

        logger.info("ThirdPersonCamState initialized");
    }
    
    /**
     * Finds a GameObject by name and returns it.
     * 
     * @param childName
     * @return
     */
    public Spatial find(String childName) {
        Spatial child = getRootNode().getChild(childName);
        String errorMsg = String.format("The spatial %s could not be found", childName);
        return Objects.requireNonNull(child, errorMsg);
    }
    
    public Node getRootNode() {
        return ((SimpleApplication) getApplication()).getRootNode();
    }
    
    public Node getGuiNode() {
        return ((SimpleApplication) getApplication()).getGuiNode();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }
    
    /**
     * Create 3rd person view.
     * @param character
     */
    private void addHeadNode(Node character) {
        
        BoundingBox bounds = (BoundingBox) character.getWorldBound();
        Node head = new Node("HeadNode");
        character.attachChild(head);
        
        //offset head node using spatial bounds to pos head level
        head.setLocalTranslation(0, bounds.getYExtent() * 2, 0);
        cam.lookAtDirection(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);
        
        //use offset head node as target for cam to follow
        ChaseCamera chaseCam = new ChaseCamera(cam, head, inputManager);
        //duplicate blender rotation
        chaseCam.setInvertVerticalAxis(true);
        chaseCam.setDefaultHorizontalRotation(1.57f);
        chaseCam.setRotationSpeed(4f);
        chaseCam.setMinDistance(bounds.getYExtent() * 2);
        chaseCam.setMaxDistance(25);
        chaseCam.setDefaultDistance(10);
        //prevent camera rotation below head
        chaseCam.setDownRotateOnCloseViewOnly(false);  
        
        //Set arrow keys to rotate view.
        //Uses default mouse scrolling to zoom.
        chaseCam.setToggleRotationTrigger(
                new KeyTrigger(KeyInput.KEY_W), 
                new KeyTrigger(KeyInput.KEY_A),
                new KeyTrigger(KeyInput.KEY_S), 
                new KeyTrigger(KeyInput.KEY_D));

        inputManager.addMapping(CameraInput.CHASECAM_UP, new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(CameraInput.CHASECAM_MOVELEFT, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(CameraInput.CHASECAM_DOWN, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(CameraInput.CHASECAM_MOVERIGHT, new KeyTrigger(KeyInput.KEY_D));
    }
}
