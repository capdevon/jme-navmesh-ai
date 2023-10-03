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
     * Z value for the far clipping plane (in screen coordinates)
     */
    private static final float farZ = 1f;
    /**
     * Z value for the near clipping plane (in screen coordinates)
     */
    private static final float nearZ = 0f;

    /**
     * Returns a ray going from camera through a screen point.
     * usage is:
     * <pre>
     *     Ray ray = MainCamera.screenPointToRay(cam, inputManager.getCursorPosition());
     * </pre>
     */
    public static Ray screenPointToRay(Camera cam, Vector2f screenXY) {
        // Convert screen click to 3D position
        Vector3f nearPos = cam.getWorldCoordinates(screenXY, nearZ);
        Vector3f farPos = cam.getWorldCoordinates(screenXY, farZ);
        Vector3f dir = farPos.subtract(nearPos).normalizeLocal();
        // Aim the ray from the clicked spot forwards.
        Ray ray = new Ray(nearPos, dir);
        return ray;
    }

}