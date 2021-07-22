/*
 *  MIT License
 *  Copyright (c) 2018 MeFisto94
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
package com.jme3.recast4j.Recast;

import com.jme3.math.Vector3f;

public class OffMeshConnection {

    /** Off-mesh connection vertices. [Unit: wu] **/
    Vector3f A;
    /** Off-mesh connection vertices. [Unit: wu] **/
    Vector3f B;
    /** Off-mesh connection radii. [Unit: wu] **/
    float radius;
    /** User defined flags assigned to the off-mesh connections. **/
    int flags;

    /** User defined area ids assigned to the off-mesh connections.
     * (https://digestingduck.blogspot.com/2010/01/off-mesh-connection-progress-pt-3.html?showComment=1334596410261#c3440987512618011308)
     * Can be used to weight off-mesh-connections based on their area type (teleporter would be a different area than elevator)
     */
    int areas;

    /** The permitted travel direction of the off-mesh connections.
     *  See {@link OffMeshConnectionDirection} for more information
     */
    OffMeshConnectionDirection direction;

    /** User-Defined ID, could be used to identify this in your game world together with {@link #areas} */
    int userId;

    public Vector3f getA() {
        return A;
    }

    public void setA(Vector3f a) {
        A = a;
    }

    public Vector3f getB() {
        return B;
    }

    public void setB(Vector3f b) {
        B = b;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getAreas() {
        return areas;
    }

    public void setAreas(int areas) {
        this.areas = areas;
    }

    public OffMeshConnectionDirection getDirection() {
        return direction;
    }

    public void setDirection(OffMeshConnectionDirection direction) {
        this.direction = direction;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
