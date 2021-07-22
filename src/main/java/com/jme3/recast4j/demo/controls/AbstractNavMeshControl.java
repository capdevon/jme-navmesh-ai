package com.jme3.recast4j.demo.controls;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNavMeshControl extends AbstractControl {

	protected BetterCharacterControl characterControl;
	protected List<Vector3f> pathList;
	protected int currentIndex;
	protected AnimChannel walkChannel;
	protected static final float walkspeed = 2f;
	protected boolean startWalking;

	public AbstractNavMeshControl() {
		this.pathList = new ArrayList<>();
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (characterControl == null) {
			characterControl = spatial.getControl(BetterCharacterControl.class);
		}

		if (characterControl == null) {
			/**
			 * While we could automatically use .move() without BCC, it's a potential source
			 * of bugs as people might want a Character but forgot about the Control.
			 */
			throw new IllegalStateException("Cannot be used without a BetterCharacterControl");
		}

		if (walkChannel == null && spatial.getControl(AnimControl.class) != null) {
			walkChannel = spatial.getControl(AnimControl.class).createChannel();
		}

		if (startWalking) {
			moveToWaypoint(); // Start walking for the first time
			startWalking = false;
		}
	}

	protected void moveToWaypoint() {
		Vector3f dir = pathList.get(currentIndex).subtract(spatial.getWorldTranslation()).setY(0f).normalizeLocal();
		System.out.println("Approaching " + pathList.get(currentIndex) + " Direction: " + dir);
		characterControl.setViewDirection(dir);
		characterControl.setWalkDirection(dir.multLocal(walkspeed));
		walk(true);
	}

	public void followPath(List<Vector3f> pathList) {
		this.pathList = pathList;
		currentIndex = 0;
		if (!pathList.isEmpty()) {
			startWalking = true; // This assures walking will start.
//            moveToWaypoint(); // Start walking for the first time
		}
	}

	public void stopFollowing() {
		System.out.println("Stop Walking");
		characterControl.setWalkDirection(Vector3f.ZERO);
		walk(false);
		pathList.clear();
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	public void walk(boolean walking) {
		if (walkChannel != null) {
			if (walking) {
				walkChannel.setAnim("Walk");
				walkChannel.setSpeed(walkspeed);
			} else {
				walkChannel.reset(true);
			}
		}
	}

	protected boolean isPathListDone() {
		// e.g. index 2 -> size >= 3
		return currentIndex + 1 > pathList.size();
	}
}
