package com.jme3.recast4j.demo.controls;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractNavMeshControl extends AbstractControl {

    protected BetterCharacterControl characterControl;
    protected AnimControl animControl;
    protected AnimChannel animChannel;
    protected float moveSpeed = 2f;
    protected boolean startWalking;
    protected int currPathIndex;
    protected List<Vector3f> pathList = new ArrayList<>();

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {

            this.characterControl = spatial.getControl(BetterCharacterControl.class);
            Objects.requireNonNull(characterControl, "BetterCharacterControl not found: " + spatial);

            this.animControl = spatial.getControl(AnimControl.class);
            Objects.requireNonNull(animControl, "AnimControl not found: " + spatial);

            animChannel = animControl.createChannel();
            walk(false);
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (startWalking) {
            moveToWaypoint(); // Start walking for the first time
            startWalking = false;
        }
    }

    protected void moveToWaypoint() {
        Vector3f dir = pathList.get(currPathIndex).subtract(spatial.getWorldTranslation()).setY(0);
        dir.normalizeLocal();
        System.out.println("Approaching " + pathList.get(currPathIndex) + " Direction: " + dir);
        characterControl.setViewDirection(dir);
        characterControl.setWalkDirection(dir.multLocal(moveSpeed));
        walk(true);
    }

    public void followPath(List<Vector3f> pathList) {
        this.pathList = pathList;
        currPathIndex = 0;
        if (!pathList.isEmpty()) {
            startWalking = true; // This assures walking will start.
            //moveToWaypoint(); // Start walking for the first time
        }
    }

    public void stopFollowing() {
        System.out.println("Stop Walking");
        characterControl.setWalkDirection(Vector3f.ZERO);
        walk(false);
        pathList.clear();
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}

    protected void walk(boolean walking) {
        if (walking) {
            animChannel.setAnim("Walk");
            animChannel.setSpeed(moveSpeed);
        } else {
            animChannel.setAnim("Idle");
        }
    }

    protected boolean isPathListDone() {
        // e.g. index 2 -> size >= 3
        return currPathIndex + 1 > pathList.size();
    }
}
