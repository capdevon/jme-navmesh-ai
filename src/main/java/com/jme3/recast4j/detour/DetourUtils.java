/*
 *  MIT License
 *  Copyright (c) 2021 MeFisto94
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package com.jme3.recast4j.Detour;

import com.jme3.math.Vector3f;

/**
 * Class consisting of helper methods to simplify interfacing between recast4j
 * and jMonkeyEngine
 * 
 * @author MeFisto94
 */
public class DetourUtils {
	
    private DetourUtils() {}

    /**
     * Create a new Vector3f instance from a float array.
     *
     * @param arr The float array to convert
     * @return the created vector
     */
    public static Vector3f toVector3f(float[] arr) {
        return new Vector3f(arr[0], arr[1], arr[2]);
    }

    /**
     * Fill a given Vector3f with values from a float array.
     * 
     * @param result The vector
     * @param arr    The float array to convert
     * @return
     */
    public static Vector3f toVector3f(Vector3f result, float[] arr) {
        result.x = arr[0];
        result.y = arr[1];
        result.z = arr[2];
        return result;
    }

    /**
     * Create a new float array from a Vector3f instance.
     *
     * @param v The vector to convert
     * @return the float array
     */
    public static float[] toFloatArray(Vector3f v) {
        return v.toArray(null);
    }

    /**
     * Fill a given float array with values from a Vector3f instance.
     *
     * @param result The float array
     * @param v      The vector to convert
     */
    public static float[] toFloatArray(float[] result, Vector3f v) {
        return v.toArray(result);
    }

    /**
     * <code>distance</code> calculates the distance between a and b.
     *
     * @param a the first vector
     * @param b the second vector
     * @return the distance between the two vectors.
     */
    public static float distance(float[] a, float[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        float result = (float) Math.sqrt(distanceSquared);

        return result;
    }

}
