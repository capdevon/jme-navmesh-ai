package com.jme3.recast4j.demo.states;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

/**
 * 
 * @author capdevon
 */
public class TogglePhysicsDebugState extends BaseAppState implements ActionListener {

    private static final String TOGGLE_PHYSICS_DEBUG = "TOGGLE_PHYSICS_DEBUG";

    private BulletAppState bulletAppState;
    private InputManager inputManager;

    @Override
    protected void initialize(Application app) {
        this.bulletAppState = getState(BulletAppState.class, true);
        this.inputManager = app.getInputManager();
    }

    @Override
    protected void cleanup(Application app) {}

    @Override
    protected void onEnable() {
        inputManager.addMapping(TOGGLE_PHYSICS_DEBUG, new KeyTrigger(KeyInput.KEY_0));
        inputManager.addListener(this, TOGGLE_PHYSICS_DEBUG);
    }

    @Override
    protected void onDisable() {
        inputManager.deleteMapping(TOGGLE_PHYSICS_DEBUG);
        inputManager.removeListener(this);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(TOGGLE_PHYSICS_DEBUG) && isPressed) {
            boolean debug = bulletAppState.isDebugEnabled();
            bulletAppState.setDebugEnabled(!debug);
        }
    }

}