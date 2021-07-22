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

package com.jme3.recast4j.demo.states.tutorial;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.DetourUtils;
import static com.jme3.recast4j.demo.SimpleAreaMod.*;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.Result;
import org.recast4j.detour.StraightPathItem;

/**
 * This is purely a non working tutorial example. Used for code demo only.
 * 
 * @author Robert
 */
public class PathState extends BaseAppState {

    private final NavMeshQuery query;
    private Spatial spatial;
    
    public PathState(NavMeshQuery query) {
        this.query = query;
    }
    
    @Override
    protected void initialize(Application app) {
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        //Use the DefaultQueryFilter so can set flags.        
        DefaultQueryFilter filter = new DefaultQueryFilter();
        
        int includeFlags = POLYFLAGS_WALK | POLYFLAGS_DOOR;
        filter.setIncludeFlags(includeFlags);
        
        int excludeFlags = POLYFLAGS_DISABLED;
        filter.setExcludeFlags(excludeFlags);
        
        //The target.
        Vector3f locOnMap = getLocationOnMap();
        
        //Extents can be anything you determine is appropriate.
        float[] extents = new float[] { 2f, 4f, 2f };
        
        //Spatials current position.
        float[] start = spatial.getWorldTranslation().toArray(null);
        
        //Convert to Recast4j native format.
        float[] end = DetourUtils.toFloatArray(locOnMap);
        
        //Get closet poly for start position.
        Result<FindNearestPolyResult> findPolyStart = query.findNearestPoly(start, extents, filter);
        //Get the closest poly for end position.
        Result<FindNearestPolyResult> findPolyEnd = query.findNearestPoly(end, extents, filter);
        
        //Get the references for the found polygons.
        long startRef = findPolyStart.result.getNearestRef();
        long endRef = findPolyEnd.result.getNearestRef();
        
        //Get the points inside the polygon.
        float[] startPos = findPolyStart.result.getNearestPos();
        float[] endPos = findPolyEnd.result.getNearestPos();

        //@TODO: Check validity using findPolyStart.status.isSuccess()

        //Get list of polys along the path.
        Result<List<Long>> path = query.findPath(startRef, endRef, startPos, endPos, filter);
        
        //Set the parameters for straight path. Paths cannot exceed 256 polygons.
        int maxStraightPath = 256;
        int options = 0;
        
        //Calculate corners within the path corridor.
        Result<List<StraightPathItem>> pathStr = query.findStraightPath(startPos, endPos, path.result, maxStraightPath, options);
        
        //Provide list for waypoints.
        List<Vector3f> wayPoints = new ArrayList<>(pathStr.result.size());
        
        //Add waypoints to the list
        for (StraightPathItem p: pathStr.result) {
            Vector3f vector = DetourUtils.toVector3f(p.getPos());
            wayPoints.add(vector);
        }
    }

    //Empty method to allow compiling.
    private Vector3f getLocationOnMap() {
        return new Vector3f();
    }
    
    @Override
    protected void onDisable() {
    }
    
}
