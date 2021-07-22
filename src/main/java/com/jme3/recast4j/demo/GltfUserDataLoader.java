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

package com.jme3.recast4j.demo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.gltf.ExtrasLoader;
import com.jme3.scene.plugins.gltf.GltfLoader;
import java.util.Map.Entry;

/**
 * Gltf loader for Blender user data.
 * 
 * @author Robert
 */
public class GltfUserDataLoader implements ExtrasLoader {

    @Override
    public Object handleExtras(GltfLoader loader, String parentName, JsonElement parent, JsonElement extras, Object input) {
        // if its a geometry, we want to add all the geometry userdata 
        if (input instanceof Spatial) {
            if (extras.isJsonObject()) {
                JsonObject ext = extras.getAsJsonObject();
                for(Entry<String, JsonElement> element : ext.entrySet()) {
                    Spatial spatial = (Spatial) input;
                    if (element.getKey().equals("open")) {
                        spatial.setUserData(element.getKey(), element.getValue().getAsBoolean());
                    }
                }
            }
        }
        return input;
    }
}
