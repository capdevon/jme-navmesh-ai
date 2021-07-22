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

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import java.util.logging.Level;

/**
 * Used to allow access to the BCC rigidBody and turns off logging info. 
 * 
 * Serves no other uses.
 * 
 * @author Robert
 */
public class CrowdBCC extends BetterCharacterControl  { 
        
    public CrowdBCC(float radius, float height, float weight) {
        super(radius, height, weight);
        logger.setLevel(Level.SEVERE);
    }
    
    //Need to override because we extended BetterCharacterControl
    @Override
    public CrowdBCC jmeClone() {
        try {
            return (CrowdBCC) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Clone Not Supported", ex);
        }
    }
    
    public PhysicsRigidBody getPhysicsRigidBody() {
        return rigidBody;
    }
    
    public float getHeight() {
        return this.height;
    }
    
    public float getRadius() {
        return this.radius;
    }
    
    public float getMass() {
        return this.mass;
    }
}

