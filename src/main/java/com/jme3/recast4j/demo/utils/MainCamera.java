package com.jme3.recast4j.demo.utils;

import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * 
 * @author capdevon
 */
public class MainCamera {

    /**
     * Returns a ray going from camera through a screen point.
     * usage is:
     * <pre>
     *     Ray ray = MainCamera.screenPointToRay(cam, inputManager.getCursorPosition());
     * </pre>
     */
    public static Ray screenPointToRay(Camera cam, Vector2f click2d) {
        // Convert screen click to 3d position
        Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d), 0).clone();
        Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d), 1).subtractLocal(click3d).normalizeLocal();
        // Aim the ray from the clicked spot forwards.
        Ray ray = new Ray(click3d, dir);
        return ray;
    }

}