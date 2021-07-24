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

package com.jme3.recast4j.demo.states;

import static com.jme3.recast4j.demo.SimpleAreaMod.AREAMOD_DOOR;
import static com.jme3.recast4j.demo.SimpleAreaMod.AREAMOD_GRASS;
import static com.jme3.recast4j.demo.SimpleAreaMod.AREAMOD_GROUND;
import static com.jme3.recast4j.demo.SimpleAreaMod.AREAMOD_ROAD;
import static com.jme3.recast4j.demo.SimpleAreaMod.AREAMOD_WATER;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_DOOR;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_GRASS;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_GROUND;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_JUMP;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_ROAD;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_WATER;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYFLAGS_DISABLED;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYFLAGS_DOOR;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYFLAGS_JUMP;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYFLAGS_SWIM;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYFLAGS_WALK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.DetourCommon;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.FindPolysAroundResult;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.detour.NavMeshParams;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.Poly;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.Result;
import org.recast4j.detour.Status;
import org.recast4j.detour.StraightPathItem;
import org.recast4j.detour.Tupple2;
import org.recast4j.detour.VectorPtr;
import org.recast4j.detour.io.MeshDataWriter;
import org.recast4j.detour.io.MeshSetReader;
import org.recast4j.detour.io.MeshSetWriter;
import org.recast4j.detour.tilecache.TileCache;
import org.recast4j.detour.tilecache.TileCacheMeshProcess;
import org.recast4j.detour.tilecache.TileCacheParams;
import org.recast4j.detour.tilecache.TileCacheStorageParams;
import org.recast4j.detour.tilecache.io.TileCacheReader;
import org.recast4j.detour.tilecache.io.TileCacheWriter;
import org.recast4j.detour.tilecache.io.compress.TileCacheCompressorFactory;
import org.recast4j.recast.CompactHeightfield;
import org.recast4j.recast.Context;
import org.recast4j.recast.ContourSet;
import org.recast4j.recast.Heightfield;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.Recast;
import org.recast4j.recast.RecastArea;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastConfig;
import org.recast4j.recast.RecastConstants;
import org.recast4j.recast.RecastConstants.PartitionType;
import org.recast4j.recast.RecastContour;
import org.recast4j.recast.RecastFilter;
import org.recast4j.recast.RecastMesh;
import org.recast4j.recast.RecastMeshDetail;
import org.recast4j.recast.RecastRasterization;
import org.recast4j.recast.RecastRegion;
import org.recast4j.recast.RecastVectors;
import org.recast4j.recast.geom.TriMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.animation.Bone;
import com.jme3.animation.SkeletonControl;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.BetterDefaultQueryFilter;
import com.jme3.recast4j.Detour.DetourUtils;
import com.jme3.recast4j.Recast.NavMeshDataCreateParamsBuilder;
import com.jme3.recast4j.Recast.RecastBuilderConfigBuilder;
import com.jme3.recast4j.Recast.RecastConfigBuilder;
import com.jme3.recast4j.demo.GeometryProviderBuilder2;
import com.jme3.recast4j.demo.JmeInputGeomProvider;
import com.jme3.recast4j.demo.Modification;
import com.jme3.recast4j.demo.MyBuilderProgressListener;
import com.jme3.recast4j.demo.RecastBuilder;
import com.jme3.recast4j.demo.TileLayerBuilder;
import com.jme3.recast4j.demo.controls.DoorSwingControl;
import com.jme3.recast4j.demo.controls.PhysicsAgentControl;
import com.jme3.recast4j.demo.utils.GameObject;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseEventControl;

/**
 *
 * @author Robert
 */
public class NavState extends AbstractNavState {

    private static final Logger LOG = LoggerFactory.getLogger(NavState.class.getName());

    //This is unused in recast4j so setting it here rather than using reflection.
    private static final int DT_TILECACHE_WALKABLE_AREA = 63;

    private Node worldMap, doorNode, offMeshCon;
    private NavMesh navMesh;
    private NavMeshQuery query;
    private List<Node> characters;
    private Map<String, org.recast4j.detour.OffMeshConnection> mapOffMeshCon;
    private PartitionType m_partitionType = PartitionType.WATERSHED;
    private float maxClimb = .3f; // Should add getter for this.
    private float radius = 0.4f;  // Should add getter for this.
    private float height = 1.7f;  // Should add getter for this.

    public NavState() {
        characters = new ArrayList<>(64);
        mapOffMeshCon = new HashMap<>();
    }
    
    @Override
    protected void onEnable() {
        worldMap = (Node) rootNode.getChild("worldmap");
        offMeshCon = (Node) rootNode.getChild("offMeshCon");
        
        //====================================================================
//        //Original implementation using jme3-recast4j methods.
//        buildSolo();
//        //Solo build using jme3-recast4j methods. Implements area and flag types.
//        buildSoloModified();
//        //Solo build using recast4j methods. Implements area and flag types.
//        buildSoloRecast4j();
//        //Tile build using recast4j methods. Implements area and flag types plus
//        //offmesh connections.
//        buildTiledRecast4j();
        buildTileCache();
        //====================================================================
        
        initMouseListener();
        
        setupDoors();
    }

    private void setupDoors() {
        //If the doorNode in DemoApplication is not null, we will create doors.
        doorNode = (Node) rootNode.getChild("doorNode");

        /**
         * This check will set any doors found in the doorNode open/closed flags
         * by adding a lemur MouseEventControl to each door found that has a 
         * DoorSwingControl. The click method for the MouseEventControl will 
         * determine when and which flags to set for the door. It will notify 
         * the DoorSwingControl of which animation to play based off the 
         * determination.
         * 
         * This is an all or none setting where either the door is open or 
         * closed. 
         */
        if (doorNode != null) {

            //Gather all doors from the doorNode.
            List < Spatial > children = doorNode.getChildren();

            /**
             * Cycle through the list and add a MouseEventControl to each door
             * with a DoorSwingControl.
             */
            for (Spatial child: children) {

                DoorSwingControl swingControl = GameObject.getComponentInChild(child, DoorSwingControl.class);

                if (swingControl != null) {
                    /**
                     * We are adding the MouseEventControl to the doors hitBox not 
                     * the door. It would be easier to use the door by turning 
                     * hardware skinning off but for some reason it always 
                     * throws an exception when doing so. The hitBox is attached 
                     * to the root bones attachment node. 
                     */
                    SkeletonControl skelControl = GameObject.getComponentInChild(child, SkeletonControl.class);
                    String name = skelControl.getSkeleton().getBone(0).getName();
                    Spatial hitBox = skelControl.getAttachmentsNode(name).getChild(0);

                    addDoorMouseListener(swingControl, hitBox);
                }
            }
        }
    }

	private void addDoorMouseListener(DoorSwingControl swingControl, Spatial hitBox) {
		MouseEventControl.addListenersToSpatial(hitBox, new DefaultMouseListener() {

		    @Override
		    protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {

		        LOG.info("<========== BEGIN Door MouseEventControl ==========>");

		        /**
		         * We have the worldmap and the doors using 
		         * MouseEventControl. In certain circumstances, usually
		         * when moving and clicking, click will return target as 
		         * worldmap so we have to then use capture to get the 
		         * proper spatial.
		         */
		        if (!target.equals(hitBox)) {
		            LOG.info("Wrong target found [{}] parentName [{}].", target.getName(), target.getParent().getName());
		            LOG.info("Switching to capture [{}] capture parent [{}].",capture.getName(), capture.getParent().getName());
		            target = capture;
		        }

		        //The filter to use for this search.
		        DefaultQueryFilter filter = new BetterDefaultQueryFilter();

		        //Limit the search to only door flags.
		        int includeFlags = POLYFLAGS_DOOR;
		        filter.setIncludeFlags(includeFlags);

		        //Include everything.
		        int excludeFlags = 0;                   
		        filter.setExcludeFlags(excludeFlags);

		        /**
		         * Look for the largest radius to search for. This will 
		         * make it possible to grab only one of a double door. 
		         * The width of the door is preferred over thickness. 
		         * The idea is to only return polys within the width of 
		         * the door so in cases where there are double doors, 
		         * only the selected door will open/close. This means 
		         * doors with large widths should not be in range of 
		         * other doors or the other doors polys will be included.
		         * 
		         * Searches take place from the origin of the attachment
		         * node which should be the same as the doors origin.
		         */
		        BoundingBox bounds = (BoundingBox) target.getWorldBound();
		        //Width of door opening.
		        float maxXZ = Math.max(bounds.getXExtent(), bounds.getZExtent()) * 2;

		        Result<FindNearestPolyResult> findNearestPoly = query.findNearestPoly(target.getWorldTranslation().toArray(null), new float[] {maxXZ, maxXZ, maxXZ}, filter);
		        
		        //No obj, no go. Fail most likely result of filter setting.
		        if (!findNearestPoly.status.isSuccess() || findNearestPoly.result.getNearestRef() == 0) {
		            LOG.error("Door findNearestPoly unsuccessful or getNearestRef is not > 0.");
		            LOG.error("findNearestPoly [{}] getNearestRef [{}].", findNearestPoly.status, findNearestPoly.result.getNearestRef());
		            return;
		        }
		        
		        Result<FindPolysAroundResult> findPolysAroundCircle = query.findPolysAroundCircle(findNearestPoly.result.getNearestRef(), findNearestPoly.result.getNearestPos(), maxXZ, filter);

		        //Success
		        if (findPolysAroundCircle.status.isSuccess()) {
		            List<Long> m_polys = findPolysAroundCircle.result.getRefs();

   //                            //May need these for something else eventually.
   //                            List<Long> m_parent = result.result.getParentRefs();
   //                            List<Float> m_costs = result.result.getCosts();

		            /**
		             * Store each poly and flag in a single object and 
		             * add it to this list so we can later check they 
		             * all have the same flag.
		             */
		            List<PolyAndFlag> listPolyAndFlag = new ArrayList<>();

		            //The flags that say this door is open.
		            int open = POLYFLAGS_WALK | POLYFLAGS_DOOR ;

		            //The flags that say this door is closed, i.e. open
		            // flags and POLYFLAGS_DISABLED
		            int closed = open | POLYFLAGS_DISABLED;

		            /**
		             * We iterate through the polys looking for the open
		             * or closed flags.
		             */
		            for (long poly: m_polys) {

		                LOG.info("<========== PRE flag set Poly ID [{}] Flags [{}] ==========>", poly, navMesh.getPolyFlags(poly).result);
		                printFlags(poly);

		                /**
		                 * We look for closed or open doors and add the 
		                 * poly id and flag to set for the poly to the 
		                 * list. We will later check to see if all poly 
		                 * flags are the same and act accordingly. If 
		                 * the door is closed, we add the open flags, if 
		                 * open, add the closed flags. 
		                 */
		                if (isBitSet(closed, navMesh.getPolyFlags(poly).result)) {
		                    listPolyAndFlag.add(new PolyAndFlag(poly, open));
		                } else if (isBitSet(open, navMesh.getPolyFlags(poly).result)) {
		                    listPolyAndFlag.add(new PolyAndFlag(poly, closed));
		                }
		            }

		            /**
		             * Check that all poly flags for the door are either 
		             * all open or all closed. This prevents changing 
		             * door flags in circumstances where a user may be 
		             * allowed to block open or closed doors with in 
		             * game objects through tile caching. If the object 
		             * was placed in such a way that not all polys in a 
		             * door opening were blocked by the object, not 
		             * checking if all polys had the same flag would 
		             * allow bypassing the blocking object flag setting. 
		             */
		            boolean same = false;
		            for (PolyAndFlag obj: listPolyAndFlag) {
		                //If any flag does not match, were done.
		                if (obj.getFlag() != listPolyAndFlag.get(0).getFlag()) {
		                    LOG.info("All poly flags are not the same listPolyAndFlag.");
		                    same = false;
		                    break;
		                }
		                same = true;
		            }

		            //If all flags match set door open/closed.
		            if (same) {                                    
		                //Set all obj flags.
		                for (PolyAndFlag obj: listPolyAndFlag) {
		                    navMesh.setPolyFlags(obj.getPoly(), obj.getFlag());
		                    LOG.info("<========== POST flag set Poly ID [{}] Flags [{}] ==========>", obj.getPoly(), navMesh.getPolyFlags(obj.getPoly()).result);
		                    printFlags(obj.getPoly());
		                }

		                /**
		                 * All flags are the same so we only 
		                 * need the first object.
		                 */
		                if (listPolyAndFlag.get(0).getFlag() == (open)) {
		                    //Open doorControl.
		                    swingControl.setOpen(true);
		                } else {
		                    //Close doorControl.
		                    swingControl.setOpen(false);
		                }
		            }
		        }
		        LOG.info("<========== END Door MouseEventControl Add ==========>");
		    }
		});
	}

    private void initMouseListener() {
        MouseEventControl.addListenersToSpatial(worldMap, new DefaultMouseListener() {
            @Override
            protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                super.click(event, target, capture);

                // First clear existing pathGeometries from the old path finding:
                pathViewer.clearPath();

                // Clicked on the map, so params a path to:
                Vector3f locOnMap = getLocationOnMap(); // Don'from calculate three times

                if (getCharacters().size() == 1) {
                    DefaultQueryFilter filter = new BetterDefaultQueryFilter();

                    int includeFlags = POLYFLAGS_WALK | POLYFLAGS_DOOR | POLYFLAGS_SWIM | POLYFLAGS_JUMP;
                    filter.setIncludeFlags(includeFlags);

                    int excludeFlags = POLYFLAGS_DISABLED;
                    filter.setExcludeFlags(excludeFlags);

                    Node character = getCharacters().get(0);
			
		    //Extents can be anything you determine is appropriate.
                    float[] extents = new float[] { 1.0f, 1.0f, 1.0f };

                    Result<FindNearestPolyResult> startPoly = query.findNearestPoly(character.getWorldTranslation().toArray(null), extents, filter);
                    Result<FindNearestPolyResult> endPoly = query.findNearestPoly(DetourUtils.toFloatArray(locOnMap), extents, filter);

                    // Note: not isFailure() here, because isSuccess guarantees us, that the result isn't "RUNNING", which it could be if we only check it's not failure.
                    if (!startPoly.status.isSuccess() || !endPoly.status.isSuccess() ||
                        startPoly.result.getNearestRef() == 0 || endPoly.result.getNearestRef() == 0) {
                    	
                        LOG.error("Character findNearestPoly unsuccessful or getNearestRef is not > 0.");
                        LOG.error("findNearestPoly startPoly [{}] getNearestRef [{}]", startPoly.status.isSuccess(), startPoly.result.getNearestRef());
                        LOG.error("findNearestPoly endPoly [{}] getNearestRef [{}].", endPoly.status.isSuccess(), endPoly.result.getNearestRef());

                    } else {
                        LOG.info("Will walk from {} to {}", character.getWorldTranslation(), locOnMap);

                        float yOffset = .5f;
                        pathViewer.putBox(ColorRGBA.Green, character.getWorldTranslation().add(0, yOffset, 0));
                        pathViewer.putBox(ColorRGBA.Yellow, locOnMap.add(0, yOffset, 0));

                        if (event.getButtonIndex() == MouseInput.BUTTON_LEFT) {
                            findPathImmediately(character, filter, startPoly.result, endPoly.result);

                        } else if (event.getButtonIndex() == MouseInput.BUTTON_RIGHT) {
                            findPathSlicedPartial(character, filter, startPoly.result, endPoly.result);
                        }
                    }
                }
            }
        });
    }
        
    /**
     * 
     * @param character
     * @param filter
     * @param startPoly
     * @param endPoly
     */
    private void findPathImmediately(Node character, QueryFilter filter, FindNearestPolyResult startPoly, FindNearestPolyResult endPoly) {

        Result<List<Long>> path = query.findPath(startPoly.getNearestRef(), endPoly.getNearestRef(), startPoly.getNearestPos(), endPoly.getNearestPos(), filter);
        if (path.succeeded()) {

            //Set the parameters for straight path. Paths cannot exceed 256 polygons.
            int maxStraightPath = 256;
            int options = 0;

            Result<List<StraightPathItem>> straightPath = query.findStraightPath(startPoly.getNearestPos(), endPoly.getNearestPos(), path.result, maxStraightPath, options);

            if (!straightPath.result.isEmpty()) {

                List<Vector3f> wayPoints = drawPath(straightPath.result, character);

                character.getControl(PhysicsAgentControl.class).stopFollowing();
                character.getControl(PhysicsAgentControl.class).followPath(wayPoints);

            } else {
                System.err.println("Unable to find straight paths");
            }
        } else {
            System.err.println("I'm sorry, unable to find a path.....");
        }
    }

    /**
     * 
     * @param character
     * @param filter
     * @param startPoly
     * @param endPoly
     */
    private void findPathSliced(Node character, QueryFilter filter, FindNearestPolyResult startPoly, FindNearestPolyResult endPoly) {

        query.initSlicedFindPath(startPoly.getNearestRef(), endPoly.getNearestRef(), startPoly.getNearestPos(), endPoly.getNearestPos(), filter, 0);

        Result<Integer> res;
        do {
            // typically called from a control or appstate, so simulate it with a loop and sleep.
            res = query.updateSlicedFindPath(1);
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } while (res.status == Status.IN_PROGRESS);

        Result<List<Long>> slicedPath = query.finalizeSlicedFindPath();

        // @TODO: Use NavMeshSliceControl (but then how to do the Debug Graphics?)
        // @TODO: Try Partial. How would one make this logic with controls etc so it's easy?
        //query.finalizeSlicedFindPathPartial();

        if (slicedPath.succeeded()) {
            // Get the proper path from the rough polygon listing
            Result<List<StraightPathItem>> straightPath = query.findStraightPath(startPoly.getNearestPos(), endPoly.getNearestPos(), slicedPath.result, Integer.MAX_VALUE, 0);

            if (!straightPath.result.isEmpty()) {

                List<Vector3f> wayPoints = drawPath(straightPath.result, character);

                character.getControl(PhysicsAgentControl.class).stopFollowing();
                character.getControl(PhysicsAgentControl.class).followPath(wayPoints);

            } else {
                System.err.println("Unable to find straight paths");
            }
        } else {
            System.err.println("I'm sorry, unable to find a path.....");
        }
    }

    /**
     * Partial means canceling before being finished
     * @param character
     * @param filter
     * @param startPoly
     * @param endPoly
     */
    private void findPathSlicedPartial(Node character, QueryFilter filter, FindNearestPolyResult startPoly, FindNearestPolyResult endPoly) {

        query.initSlicedFindPath(startPoly.getNearestRef(), endPoly.getNearestRef(), startPoly.getNearestPos(), endPoly.getNearestPos(), filter, 0);
        Result<Integer> res = query.updateSlicedFindPath(1);
        Result<List<Long>> slicedPath = query.finalizeSlicedFindPath();

        query.initSlicedFindPath(startPoly.getNearestRef(), endPoly.getNearestRef(), startPoly.getNearestPos(), endPoly.getNearestPos(), filter, 0);
        Result<List<Long>> slicedPathPartial = query.finalizeSlicedFindPathPartial(slicedPath.result);

        // @TODO: Use NavMeshSliceControl (but then how to do the Debug Graphics?)
        // @TODO: Try Partial. How would one make this logic with controls etc so it's easy?
        //query.finalizeSlicedFindPathPartial();

        if (slicedPathPartial.succeeded()) {
            // Get the proper path from the rough polygon listing
            Result<List<StraightPathItem>> straightPath = query.findStraightPath(startPoly.getNearestPos(), endPoly.getNearestPos(), slicedPathPartial.result, Integer.MAX_VALUE, 0);

            if (!straightPath.result.isEmpty()) {

                List<Vector3f> wayPoints = drawPath(straightPath.result, character);

                character.getControl(PhysicsAgentControl.class).stopFollowing();
                character.getControl(PhysicsAgentControl.class).followPath(wayPoints);

            } else {
                System.err.println("Unable to find straight paths");
            }
        } else {
            System.err.println("I'm sorry, unable to find a path.....");
        }
    }

    private List<Vector3f> drawPath(List<StraightPathItem> straightPath, Node character) {

        List<Vector3f> wayPoints = new ArrayList<>(straightPath.size());
        Vector3f oldPos = character.getWorldTranslation();
        Vector3f offset = new Vector3f(0, .5f, 0);

        for (StraightPathItem spi : straightPath) {

            Vector3f waypoint = DetourUtils.toVector3f(spi.getPos());
            pathViewer.putLine(ColorRGBA.Orange, oldPos.add(offset), waypoint.add(offset));

            if (spi.getRef() != 0) { // if ref is 0, it's the linkB.
            	pathViewer.putBox(ColorRGBA.Blue, waypoint.add(offset));
            }

            wayPoints.add(waypoint);
            oldPos = waypoint;
        }

        return wayPoints;
    }
    
    /**
     * Returns the Location on the Map which is currently under the Cursor. 
     * For this we use the Camera to project the point onto the near and far 
     * plane (because we don'from have the depth information [map height]). Then 
     * we can use this information to do a raycast, ideally the world is in 
     * between those planes and we hit it at the correct place.
     * 
     * @return The Location on the Map
     */
	public Vector3f getLocationOnMap() {
		Ray ray = screenPointToRay(camera, inputManager.getCursorPosition());
		CollisionResults collResults = new CollisionResults();
		worldMap.collideWith(ray, collResults);

		if (collResults.size() > 0) {
			return collResults.getClosestCollision().getContactPoint();
		} else {
			return null;
		}
	}
	
	private Ray screenPointToRay(Camera camera, Vector2f click2d) {
		// Convert screen click to 3d position
		Vector3f click3d = camera.getWorldCoordinates(new Vector2f(click2d), 0f).clone();
		Vector3f dir = camera.getWorldCoordinates(new Vector2f(click2d), 1f).subtractLocal(click3d).normalizeLocal();
		// Aim the ray from the clicked spot forwards.
		Ray ray = new Ray(click3d, dir);
		return ray;
	}

    /**
     * @return the characters
     */
    public List<Node> getCharacters() {
        return characters;
    }
    
    /**
     * Original implementation using jme3-recast4j methods and custom recastBuilder.
     */
    private void buildSolo() {
        //Clean up offMesh connections.
        offMeshCon.detachAllChildren();
        
        System.out.println("Building Nav Mesh, this may freeze your computer for a few seconds, please stand by");
        long time = System.currentTimeMillis(); // Never do real benchmarking with currentTimeMillis!
        RecastBuilderConfig bcfg = new RecastBuilderConfigBuilder(worldMap).
                build(new RecastConfigBuilder()
                        .withAgentRadius(.3f)           // r
                        .withAgentHeight(1.7f)          // h
                        //cs and ch should probably be .1 at min.
                        .withCellSize(.1f)              // cs=r/3
                        .withCellHeight(.1f)            // ch=cs 
                        .withAgentMaxClimb(.3f)         // > 2*ch
                        .withAgentMaxSlope(45f)         
                        .withEdgeMaxLen(2.4f)           // r*8
                        .withEdgeMaxError(1.3f)         // 1.1 - 1.5
                        .withDetailSampleDistance(8.0f) // increase if exception
                        .withDetailSampleMaxError(8.0f) // increase if exception
                        .withVertsPerPoly(3).build());
        
        //Split up for testing.
        JmeInputGeomProvider geom = new GeometryProviderBuilder2(worldMap).build();
        RecastBuilder rcBuilder = new RecastBuilder();
        RecastBuilderResult result = rcBuilder.build(geom, builderCfg);
        
        NavMeshDataCreateParams params = new NavMeshDataCreateParamsBuilder(result).build(builderCfg);
        MeshData meshData = NavMeshBuilder.createNavMeshData(params);
        
        navMesh = new NavMesh(meshData, builderCfg.cfg.maxVertsPerPoly, 0);
        query = new NavMeshQuery(navMesh);
        
        try {
        	saveToFile(meshData);
        	saveToFile(navMesh);
        	
        } catch (Exception ex) {
            LOG.error("[{}]", ex);
        }

        //Show wireframe. Helps with param tweaks. false = solid color.
        showDebugMeshes(meshData, true);
        
        System.out.println("Building succeeded after " + (System.currentTimeMillis() - time) + " ms");
    }
    
    /**
     * This example sets area type and flags based off geometry of each 
     * individual mesh and uses the custom RecastBuilder class with 
     * jme3-recast4j wrapper methods. 
     */
    private void buildSoloModified() {
    	
    	//Build merged mesh.
        JmeInputGeomProvider geomProvider = new GeometryProviderBuilder2(worldMap).build();
        
	configureAreaMod(geomProvider);
        
        //Clean up offMesh connections.
        offMeshCon.detachAllChildren();
        
        RecastBuilderConfig bcfg = new RecastBuilderConfigBuilder(worldMap).withDetailMesh(true).
                build(new RecastConfigBuilder()
                        .withAgentRadius(.3f)           // r
                        .withAgentHeight(1.7f)          // h
                        //cs and ch should probably be .1 at min.
                        .withCellSize(.1f)              // cs=r/3
                        .withCellHeight(.1f)            // ch=cs 
                        .withAgentMaxClimb(.3f)         // > 2*ch
                        .withAgentMaxSlope(45f)         
                        .withEdgeMaxLen(2.4f)             // r*8
                        .withEdgeMaxError(1.3f)         // 1.1 - 1.5
                        .withDetailSampleDistance(8.0f) // increase to 8 if exception on level model
                        .withDetailSampleMaxError(8.0f) // increase to 8 if exception on level model
                        .withVertsPerPoly(3).build());
        
        //Split up for testing.
        RecastBuilderResult result = new RecastBuilder().build(geomProvider, bcfg);
        
        NavMeshDataCreateParamsBuilder paramsBuilder = new NavMeshDataCreateParamsBuilder(result);
        PolyMesh m_pmesh = result.getMesh();
        
        //Set Ability flags. 
		for (int i = 0; i < m_pmesh.npolys; ++i) {
			if (m_pmesh.areas[i] == POLYAREA_TYPE_GROUND 
					|| m_pmesh.areas[i] == POLYAREA_TYPE_GRASS
					|| m_pmesh.areas[i] == POLYAREA_TYPE_ROAD) {
				paramsBuilder.withPolyFlag(i, POLYFLAGS_WALK);
			} else if (m_pmesh.areas[i] == POLYAREA_TYPE_WATER) {
				paramsBuilder.withPolyFlag(i, POLYFLAGS_SWIM);
			} else if (m_pmesh.areas[i] == POLYAREA_TYPE_DOOR) {
				paramsBuilder.withPolyFlags(i, POLYFLAGS_WALK | POLYFLAGS_DOOR);
			} else if (m_pmesh.areas[i] == POLYAREA_TYPE_JUMP) {
				paramsBuilder.withPolyFlag(i, POLYFLAGS_JUMP);
			}
		}
        
        NavMeshDataCreateParams params = paramsBuilder.build(bcfg);
        
        /**
         * Must set variables for parameters walkableHeight, walkableRadius, 
         * walkableClimb manually for mesh data unless jme3-recast4j fixed.
         */
        params.walkableClimb = maxClimb; //Should add getter for this.
        params.walkableHeight = height; //Should add getter for this.
        params.walkableRadius = radius; //Should add getter for this.
            
        MeshData meshData = NavMeshBuilder.createNavMeshData(params);
        navMesh = new NavMesh(meshData, bcfg.cfg.maxVertsPerPoly, 0);
        query = new NavMeshQuery(navMesh);
        
        //Create offmesh connections here.

        try {
        	saveToFile(meshData);
        	saveToFile(navMesh);
        
        } catch (Exception ex) {
            LOG.error("[{}]", ex);
        }

        //Show wireframe. Helps with param tweaks. false = solid color.
//        showDebugMeshes(meshData, true);
        showDebugByArea(meshData, true);

    }
    
    /**
     * This example builds the mesh manually by using recast4j methods. 
     * Implements area type and flag setting.
     */
    private void buildSoloRecast4j() {

        //Build merged mesh.
        JmeInputGeomProvider geomProvider = new GeometryProviderBuilder2(worldMap).build();
        
		configureAreaMod(geomProvider);
        
        //Clean up offMesh connections.
        offMeshCon.detachAllChildren();
        
        //Get min/max bounds.
        float[] bmin = geomProvider.getMeshBoundsMin();
        float[] bmax = geomProvider.getMeshBoundsMax();
        Context m_ctx = new Context();
        
        //We could use multiple configs here based off area type list.
        RecastConfigBuilder builder = new RecastConfigBuilder();
        RecastConfig cfg = builder
            .withAgentRadius(radius)            // r
            .withAgentHeight(height)            // h
            //cs and ch should be .1 at min.
            .withCellSize(0.1f)                 // cs=r/2
            .withCellHeight(0.1f)               // ch=cs/2 but not < .1f
            .withAgentMaxClimb(maxClimb)        // > 2*ch
            .withAgentMaxSlope(45f)
            .withEdgeMaxLen(2.4f)               // r*8
            .withEdgeMaxError(1.3f)             // 1.1 - 1.5
            .withDetailSampleDistance(8.0f)     // increase if exception
            .withDetailSampleMaxError(8.0f)     // increase if exception
            .withWalkableAreaMod(AREAMOD_GROUND)
            .withVertsPerPoly(3).build();
        
        RecastBuilderConfig bcfg = new RecastBuilderConfig(cfg, bmin, bmax);
        
        Heightfield m_solid = new Heightfield(bcfg.width, bcfg.height, bcfg.bmin, bcfg.bmax, cfg.cs, cfg.ch);
        
        for (TriMesh geom : geomProvider.meshes()) {
            float[] verts = geom.getVerts();
            int[] tris = geom.getTris();
            int ntris = tris.length / 3;
            
            //Separate individual triangles into a arrays so we can mark Area Type.
            List<int[]> listTris = new ArrayList<>();
            int fromIndex = 0;
			for (Modification mod : geomProvider.getListMods()) {
				int[] triangles = new int[mod.getGeomLength()];
				System.arraycopy(tris, fromIndex, triangles, 0, mod.getGeomLength());
				listTris.add(triangles);
				fromIndex += mod.getGeomLength();
			}
            
            List<int[]> areas = new ArrayList<>();
            
			for (Modification mod : geomProvider.getListMods()) {
				int[] m_triareas = Recast.markWalkableTriangles(
						m_ctx, 
						cfg.walkableSlopeAngle, 
						verts,
						listTris.get(geomProvider.getListMods().indexOf(mod)),
						listTris.get(geomProvider.getListMods().indexOf(mod)).length / 3, 
						mod.getMod());
				
				areas.add(m_triareas);
			}            
            
            //Prepare the new array for all areas.
            int[] m_triareasAll = new int[ntris];
            int length = 0;
            //Copy all flagged areas into new array.
            for (int[] area: areas) {
                System.arraycopy(area, 0, m_triareasAll, length, area.length);
                length += area.length;
            }
            RecastRasterization.rasterizeTriangles(m_ctx, verts, tris, m_triareasAll, ntris, m_solid, cfg.walkableClimb);
        }
        
        RecastFilter.filterLowHangingWalkableObstacles(m_ctx, cfg.walkableClimb, m_solid);
        RecastFilter.filterLedgeSpans(m_ctx, cfg.walkableHeight, cfg.walkableClimb, m_solid);
        RecastFilter.filterWalkableLowHeightSpans(m_ctx, cfg.walkableHeight, m_solid);

        CompactHeightfield m_chf = Recast.buildCompactHeightfield(m_ctx, cfg.walkableHeight, cfg.walkableClimb, m_solid);

        RecastArea.erodeWalkableArea(m_ctx, cfg.walkableRadius, m_chf);
 
//        // (Optional) Mark areas.
//        List<ConvexVolume> vols = geom.getConvexVolumes(); 
//        for (ConvexVolume convexVolume: vols) { 
//            RecastArea.markConvexPolyArea(m_ctx, convexVolume.verts, convexVolume.hmin, convexVolume.hmax, convexVolume.areaMod, m_chf);
//        }

        if (m_partitionType == PartitionType.WATERSHED) {
            // Prepare for region partitioning, by calculating distance field
            // along the walkable surface.
            RecastRegion.buildDistanceField(m_ctx, m_chf);
            // Partition the walkable surface into simple regions without holes.
            RecastRegion.buildRegions(m_ctx, m_chf, 0, cfg.minRegionArea, cfg.mergeRegionArea);
        } else if (m_partitionType == PartitionType.MONOTONE) {
            // Partition the walkable surface into simple regions without holes.
            // Monotone partitioning does not need distancefield.
            RecastRegion.buildRegionsMonotone(m_ctx, m_chf, 0, cfg.minRegionArea, cfg.mergeRegionArea);
        } else {
            // Partition the walkable surface into simple regions without holes.
            RecastRegion.buildLayerRegions(m_ctx, m_chf, 0, cfg.minRegionArea);
        }

        ContourSet m_cset = RecastContour.buildContours(m_ctx, m_chf, cfg.maxSimplificationError, cfg.maxEdgeLen,
                RecastConstants.RC_CONTOUR_TESS_WALL_EDGES);

        // Build polygon navmesh from the contours.
        PolyMesh m_pmesh = RecastMesh.buildPolyMesh(m_ctx, m_cset, cfg.maxVertsPerPoly);

        //Set Ability flags.
        for (int i = 0; i < m_pmesh.npolys; ++i) {
            if (m_pmesh.areas[i] == POLYAREA_TYPE_GROUND
            ||  m_pmesh.areas[i] == POLYAREA_TYPE_GRASS
            ||  m_pmesh.areas[i] == POLYAREA_TYPE_ROAD) {
                m_pmesh.flags[i] = POLYFLAGS_WALK;
            } else if (m_pmesh.areas[i] == POLYAREA_TYPE_WATER) {
                m_pmesh.flags[i] = POLYFLAGS_SWIM;
            } else if (m_pmesh.areas[i] == POLYAREA_TYPE_DOOR) {
                m_pmesh.flags[i] = POLYFLAGS_WALK | POLYFLAGS_DOOR;
            } else if (m_pmesh.areas[i] == POLYAREA_TYPE_JUMP) {
                m_pmesh.flags[i] = POLYFLAGS_JUMP;
            }          
        }

        //Create detailed mesh for picking.
        PolyMeshDetail m_dmesh = RecastMeshDetail.buildPolyMeshDetail(m_ctx, m_pmesh, m_chf, cfg.detailSampleDist, cfg.detailSampleMaxError);
        
        NavMeshDataCreateParams params = new NavMeshDataCreateParams();
        params.verts = m_pmesh.verts;
        params.vertCount = m_pmesh.nverts;
        params.polys = m_pmesh.polys;
        params.polyAreas = m_pmesh.areas;
        params.polyFlags = m_pmesh.flags;
        params.polyCount = m_pmesh.npolys;
        params.nvp = m_pmesh.nvp;
        params.detailMeshes = m_dmesh.meshes;
        params.detailVerts = m_dmesh.verts;
        params.detailVertsCount = m_dmesh.nverts;
        params.detailTris = m_dmesh.tris;
        params.detailTriCount = m_dmesh.ntris;
        params.walkableHeight = height; //Should add getter for this.
        params.walkableRadius = radius; //Should add getter for this.
        params.walkableClimb = maxClimb; //Should add getter for this.
        params.bmin = m_pmesh.bmin;
        params.bmax = m_pmesh.bmax;
        params.cs = cfg.cs; 
        params.ch = cfg.ch;
        params.buildBvTree = true;
                
        MeshData meshData = NavMeshBuilder.createNavMeshData(params);
        navMesh = new NavMesh(meshData, params.nvp, 0);
        query = new NavMeshQuery(navMesh);
        
        //Create offmesh connections here.

        try {
        	saveToFile(meshData);
        	saveToFile(navMesh);
        
        } catch (Exception ex) {
            LOG.error("[{}]", ex);
        }

        //Show wireframe. Helps with param tweaks. false = solid color.
//        showDebugMeshes(meshData, true);
        showDebugByArea(meshData, true);

    }

    /**
     * This example sets area type and flags based off geometry of each 
     * individual mesh and uses the custom RecastBuilder class. Implements 
     * offmesh connections. Uses recast4j methods for building.
     */
    private void buildTiledRecast4j() {
    	
    	//Build merged mesh.
        JmeInputGeomProvider geomProvider = new GeometryProviderBuilder2(worldMap).build();
        
		configureAreaMod(geomProvider);
        
        //Set offmesh connections.
        offMeshCon.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            //Id will be used for OffMeshConnections.
            int id = 0;
            
            @Override
            public void visit(Node spat) { 
                /**
                 * offMeshCon has no skeleton and is instance of node so the 
                 * search will include its children. This will return with a 
                 * child SkeletonControl because of this. Add check to skip 
                 * offMeshCon.
                 */
                if (!spat.getName().equals(offMeshCon.getName())) {

                    SkeletonControl skelControl = GameObject.getComponentInChild(spat, SkeletonControl.class);

                    if (skelControl != null) {
                        /**
                        * Offmesh connections require two connections, a 
                        * start/end vector3f and must connect to a surrounding 
                        * tile. To complete a connection, start and end must be 
                        * the same for each. You can supply the Vector3f manually 
                        * or for example, use bones from an armature. When using 
                        * bones, they should be paired and use a naming convention. 
                        * 
                        * In our case, we used bones and this naming convention:
                        * 
                        * arg[0](delimiter)arg[1](delimiter)arg[2]
                        * 
                        * We set each bone origin to any vertices, in any mesh, 
                        * as long as the same string for arg[0] and arg[1] are 
                        * identical and they do not use the same vertices. We 
                        * duplicate two polygons (triangles) in the mesh or 
                        * separate meshes and add an armature(s) using a naming
                        * convention.
                        * 
                        * Naming convention for two bones: 
                        * 
                        * Bone 1 naming: offmesh.anything.a
                        * Bone 2 naming: offmesh.anything.b
                        * 
                        * arg[0]: offmesh   = same value all bones
                        * arg[1]: anything  = same value paired bones
                        * arg[2]: a or b    = one paired bone
                        * 
                        * The value of arg[0] applies to ALL bones and 
                        * dictates these are link bones.
                        * 
                        * The value of arg[1] dictates these pair of bones 
                        * belong together. 
                        * 
                        * The value of arg[2] distinguishes the paired bones 
                        * from each other.
                        * 
                        * Examples: 
                        * 
                        * offmesh.pond.a
                        * offmesh.pond.b
                        * offmesh.1.a
                        * offmesh.1.b
                        */
                        Bone[] roots = skelControl.getSkeleton().getRoots();
                        for (Bone b: roots) {
                            /**
                             * Split the name up using delimiter. 
                             */
                            String[] arg = b.getName().split("\\.");

                            if (arg[0].equals("offmesh")) {

                                //New connection.
                                org.recast4j.detour.OffMeshConnection link1 = new org.recast4j.detour.OffMeshConnection();

                                /**
                                 * The bones worldTranslation will be the start
                                 * or end Vector3f of each OffMeshConnection 
                                 * object.
                                 */
                                float[] linkPos = DetourUtils.toFloatArray(spat.localToWorld(b.getModelSpacePosition(), null));

                                /**
                                 * Prepare new position array. The endpoints of 
                                 * the connection. 
                                 * 
                                 *  startPos    endPos
                                 * [ax, ay, az, bx, by, bz]
                                 */
                                float[] pos = new float[6];

                                /**
                                 * Copy link1 current position to pos array. If
                                 * link1 is bone (a), it becomes the link start.
                                 * If (b), the link end.
                                 */
                                System.arraycopy(linkPos, 0, pos, arg[2].equals("a") ? 0:3, 3);

                                //Set link1 to new array.
                                link1.pos = pos;

                                //Player (r)adius. Links fire at (r) * 2.25.
                                link1.rad = radius;
                                
                                /**
                                 * Move through link1 both directions. Only 
                                 * works if both links have identical in start/end.
                                 * Set to 0 for one direction link.
                                 */
                                link1.flags = NavMesh.DT_OFFMESH_CON_BIDIR;

                                /**
                                 * We need to look for the bones mate. Based off 
                                 * our naming convention, this will be 
                                 * offmesh.anything."a" or "b" so we set the 
                                 * search to whatever link1 arg[2] isn't.
                                 */
                                String link2 = String.join(".", arg[0], arg[1], arg[2].equals("a") ? "b": "a");

                                /**
                                 * If the paired bone has already been added to 
                                 * map, set start or end determined by link1 arg[2].
                                 */
                                if (mapOffMeshCon.containsKey(link2)) {
                                    /**
                                     * Copy link1 pos to link2 pos. If link1 is 
                                     * start(a) of link, copy link1 start to 
                                     * link2 start. If link1 is the end(b) of 
                                     * link, copy link1 end to link2 end. 
                                     */
                                    System.arraycopy(link1.pos, arg[2].equals("a") ? 0:3, mapOffMeshCon.get(link2).pos, arg[2].equals("a") ? 0:3, 3);
                                    
                                    /**
                                     * Copy link2 pos to link1 pos. If link1 is
                                     * the start(a) of link, copy link2 end to
                                     * link1 end. If link1 is end(b) of link, 
                                     * copy link2 start to link1 start.
                                     */
                                    System.arraycopy(mapOffMeshCon.get(link2).pos, arg[2].equals("a") ? 3:0, link1.pos, arg[2].equals("a") ? 3:0, 3);

                                    /**
                                     * OffMeshconnections with id of 0 don't get 
                                     * processed later if not set here.
                                     */
                                    if (arg[2].equals("a")) {
                                        link1.userId = ++id;
                                        LOG.info("OffMeshConnection [{}] id [{}]", b.getName(), link1.userId);
                                        mapOffMeshCon.get(link2).userId = ++id;
                                        LOG.info("OffMeshConnection [{}] id [{}]", link2, mapOffMeshCon.get(link2).userId);
                                    } else {
                                        mapOffMeshCon.get(link2).userId = ++id;
                                        LOG.info("OffMeshConnection [{}] id [{}]", link2, mapOffMeshCon.get(link2).userId);
                                        link1.userId = ++id;
                                        LOG.info("OffMeshConnection [{}] id [{}]", b.getName(), link1.userId);
                                    }

                                }
                                //Add this bone to map.
                                mapOffMeshCon.put(b.getName(), link1);
                            }
                        }
                    }
                }
            }
        });        
        
        //Clean up offMesh connections.
        offMeshCon.detachAllChildren();
        
        //Step 2. Create a Recast configuration object.
        RecastConfigBuilder builder = new RecastConfigBuilder();
        //Instantiate the configuration parameters.
        RecastConfig cfg = builder
                .withAgentRadius(.3f)       // r
                .withAgentHeight(1.7f)       // h
                //cs and ch should be .1 at min.
                .withCellSize(0.1f)                 // cs=r/2
                .withCellHeight(0.1f)               // ch=cs/2 but not < .1f
                .withAgentMaxClimb(.3f)             // > 2*ch
                .withAgentMaxSlope(45f)
                .withEdgeMaxLen(3.2f)               // r*8
                .withEdgeMaxError(1.3f)             // 1.1 - 1.5
                .withDetailSampleDistance(6.0f)     // increase if exception
                .withDetailSampleMaxError(6.0f)     // increase if exception
                .withVertsPerPoly(3)
                .withTileSize(16)
                .build(); 
        
        // Build all tiles
        RecastBuilder rb = new RecastBuilder(new MyBuilderProgressListener());
        RecastBuilderResult[][] rcResult = rb.buildTiles(geomProvider, cfg, 1);
        // Add tiles to nav mesh
        int tw = rcResult.length;
        int th = rcResult[0].length;
        
        // Create empty nav mesh
        NavMeshParams navMeshParams = new NavMeshParams();
        RecastVectors.copy(navMeshParams.orig, geomProvider.getMeshBoundsMin());
        navMeshParams.tileWidth = cfg.tileSize * cfg.cs;
        navMeshParams.tileHeight = cfg.tileSize * cfg.cs;
        navMeshParams.maxTiles = tw * th;
        navMeshParams.maxPolys = 32768;
        navMesh = new NavMesh(navMeshParams, cfg.maxVertsPerPoly);
        
        for (int y = 0; y < th; y++) {
            for (int x = 0; x < tw; x++) {
                PolyMesh m_pmesh = rcResult[x][y].getMesh();
                if (m_pmesh.npolys == 0) {
                        continue;
                }

                // Update obj flags from areas. Including offmesh connections.
                for (int i = 0; i < m_pmesh.npolys; ++i) {
                    if (m_pmesh.areas[i] == POLYAREA_TYPE_GROUND
                    ||  m_pmesh.areas[i] == POLYAREA_TYPE_GRASS
                    ||  m_pmesh.areas[i] == POLYAREA_TYPE_ROAD) {
                        m_pmesh.flags[i] = POLYFLAGS_WALK;
                    } else if (m_pmesh.areas[i] == POLYAREA_TYPE_WATER) {
                        m_pmesh.flags[i] = POLYFLAGS_SWIM;
                    } else if (m_pmesh.areas[i] == POLYAREA_TYPE_DOOR) {
                        m_pmesh.flags[i] = POLYFLAGS_WALK | POLYFLAGS_DOOR;
                    }                     
                }
                
                NavMeshDataCreateParams params = new NavMeshDataCreateParams();
                
                params.verts = m_pmesh.verts;
                params.vertCount = m_pmesh.nverts;
                params.polys = m_pmesh.polys;
                params.polyAreas = m_pmesh.areas;
                params.polyFlags = m_pmesh.flags;
                params.polyCount = m_pmesh.npolys;
                params.nvp = m_pmesh.nvp;
                PolyMeshDetail dmesh = rcResult[x][y].getMeshDetail();
                params.detailMeshes = dmesh.meshes;
                params.detailVerts = dmesh.verts;
                params.detailVertsCount = dmesh.nverts;
                params.detailTris = dmesh.tris;
                params.detailTriCount = dmesh.ntris;
                params.walkableHeight = height;
                params.walkableRadius = radius;
                params.walkableClimb = maxClimb;
                params.bmin = m_pmesh.bmin;
                params.bmax = m_pmesh.bmax;
                params.cs = cfg.cs;
                params.ch = cfg.ch;
                params.tileX = x;
                params.tileY = y;
                params.buildBvTree = true;
                
                navMesh.addTile(NavMeshBuilder.createNavMeshData(params), 0, 0);
            }
        }
        
        query = new NavMeshQuery(navMesh);
        
        /**
         * Process OffMeshConnections. 
         * Basic flow: 
         * Check each mapOffMeshConnection for an index > 0. 
         * findNearestPoly() for the start/end positions of the link.
         * getTileAndPolyByRef() using the returned poly reference.
         * If both start and end are good values, set the connection properties.
         */
        Iterator<Map.Entry<String, org.recast4j.detour.OffMeshConnection>> itOffMesh = mapOffMeshCon.entrySet().iterator();
        while (itOffMesh.hasNext()) {
            Map.Entry<String, org.recast4j.detour.OffMeshConnection> next = itOffMesh.next();

            /**
             * If the OffMeshConnection id is 0, there is no paired bone for the
             * link so skip.
             */            
            if (next.getValue().userId > 0) {
                //Create a new filter for findNearestPoly
                DefaultQueryFilter filter = new DefaultQueryFilter();

                //In our case, we only need swim or walk flags.
                int include = POLYFLAGS_WALK | POLYFLAGS_SWIM;
                filter.setIncludeFlags(include);

                //No excludes.
                int exclude = 0;
                filter.setExcludeFlags(exclude);

                //Get the start position for the link.
                float[] startPos = new float[3];
                System.arraycopy(next.getValue().pos, 0, startPos, 0, 3);
                //Get the end position for the link.
                float[] endPos = new float[3];
                System.arraycopy(next.getValue().pos, 3, endPos, 0, 3);

                //Find the nearest polys to start/end.
                Result<FindNearestPolyResult> startPoly = query.findNearestPoly(startPos, new float[] {radius,radius,radius}, filter);
                Result<FindNearestPolyResult> endPoly = query.findNearestPoly(endPos, new float[] {radius,radius,radius}, filter);

                /**
                 * Note: not isFailure() here, because isSuccess guarantees us, 
                 * that the result isn't "RUNNING", which it could be if we only 
                 * check it's not failure.
                 */
                if (!startPoly.status.isSuccess() 
                ||  !endPoly.status.isSuccess() 
                ||   startPoly.result.getNearestRef() == 0 
                ||   endPoly.result.getNearestRef() == 0) {
                    LOG.error("offmeshCon findNearestPoly unsuccessful or getNearestRef is not > 0.");
                    LOG.error("Link [{}] pos {} id [{}]", next.getKey(), Arrays.toString(next.getValue().pos), next.getValue().userId);
                    LOG.error("findNearestPoly startPoly [{}] getNearestRef [{}]", startPoly.status.isSuccess(), startPoly.result.getNearestRef());
                    LOG.error("findNearestPoly endPoly [{}] getNearestRef [{}].", endPoly.status.isSuccess(), endPoly.result.getNearestRef());
                } else {
                    //Get the tile and poly from reference.
                    Result<Tupple2<MeshTile, Poly>> startTileByRef = navMesh.getTileAndPolyByRef(startPoly.result.getNearestRef());
                    Result<Tupple2<MeshTile, Poly>> endTileByRef = navMesh.getTileAndPolyByRef(endPoly.result.getNearestRef());

                    //Mesh data for the start/end tile.
                    MeshData startTile = startTileByRef.result.first.data;
                    MeshData endTile = endTileByRef.result.first.data;

                    //Both start and end poly must be vailid.
                    if (startTileByRef.result.second != null && endTileByRef.result.second != null) {
                        //We will add a new poly that will become our "link" 
                        //between start and end points so make room for it.
                        startTile.polys = Arrays.copyOf(startTile.polys, startTile.polys.length + 1);
                        //We shifted everything but haven't incremented polyCount 
                        //yet so this will become our new poly's index.
                        int poly = startTile.header.polyCount;
                        /**
                         * Off-mesh connections are stored in the navigation 
                         * mesh as special 2-vertex polygons with a single edge. 
                         * At least one of the vertices is expected to be inside 
                         * a normal polygon. So an off-mesh connection is 
                         * "entered" from a normal polygon at one of its 
                         * endpoints. Jme requires 3 vertices per poly to 
                         * build a debug mesh so we have to create a 
                         * 3-vertex polygon here if using debug. The extra 
                         * vertex position will be connected automatically 
                         * when we add the tile back to the navmesh. For 
                         * games, this would be a two vert poly.
                         * 
                         * See: https://github.com/ppiastucki/recast4j/blob/3c532068d79fe0306fedf035e50216008c306cdf/detour/src/main/java/org/recast4j/detour/NavMesh.java#L406
                         */
                        startTile.polys[poly] = new Poly(poly, 3);
                        /**
                         * Must add/create our new indices for start and end.
                         * When we add the tile, the third vert will be 
                         * generated for us. 
                         */
                        startTile.polys[poly].verts[0] = startTile.header.vertCount;
                        startTile.polys[poly].verts[1] = startTile.header.vertCount + 1;
                        //Set the poly's type to DT_POLYTYPE_OFFMESH_CONNECTION
                        //so it is not seen as a regular poly when linking.
                        startTile.polys[poly].setType(Poly.DT_POLYTYPE_OFFMESH_CONNECTION);
                        //Make room for our start/end verts.
                        startTile.verts = Arrays.copyOf(startTile.verts, startTile.verts.length + 6);
                        //Increment our poly and vert counts.
                        startTile.header.polyCount++;
                        startTile.header.vertCount += 2;
                        //Set our OffMeshLinks poly to this new poly.
                        next.getValue().poly = poly;
                        //Shorten names and make readable. Could just call directly.
                        float[] start = startPoly.result.getNearestPos();
                        float[] end = endPoly.result.getNearestPos();
                        //Set the links position array values to nearest.
                        next.getValue().pos = new float[] { start[0], start[1], start[2], end[0], end[1], end[2] };
                        //Determine what side of the tile the vertx is on.
                        next.getValue().side = startTile == endTile ? 0xFF
                                : NavMeshBuilder.classifyOffMeshPoint(new VectorPtr(next.getValue().pos, 3),
                                        startTile.header.bmin, startTile.header.bmax);
                        //Create new OffMeshConnection array.
                        if (startTile.offMeshCons == null) {
                                startTile.offMeshCons = new org.recast4j.detour.OffMeshConnection[1];
                        } else {
                                startTile.offMeshCons = Arrays.copyOf(startTile.offMeshCons, startTile.offMeshCons.length + 1);
                        }
                        
                        //Add this connection.
                        startTile.offMeshCons[startTile.offMeshCons.length - 1] = next.getValue();
                        startTile.header.offMeshConCount++;

                        //Set the polys area type and flags.
                        startTile.polys[poly].flags = POLYFLAGS_JUMP;
                        startTile.polys[poly].setArea(POLYAREA_TYPE_JUMP);

                        /**
                         * Removing and adding the tile will rebuild all the 
                         * links for the tile automatically. The number of links 
                         * is : edges + portals * 2 + off-mesh con * 2.
                         */
                        MeshData removeTile = navMesh.removeTile(navMesh.getTileRef(startTileByRef.result.first));
                        navMesh.addTile(removeTile, 0, navMesh.getTileRef(startTileByRef.result.first));
                    }
                }       
            }
        }
        
	    try {
		// Native format using tiles.
		MeshSetWriter msw = new MeshSetWriter();
		msw.write(new FileOutputStream(new File("test.nm")), navMesh, ByteOrder.BIG_ENDIAN, false);

		// Read in saved NavMesh.
		MeshSetReader msr = new MeshSetReader();
		navMesh = msr.read(new FileInputStream("test.nm"), cfg.maxVertsPerPoly);

		query = new NavMeshQuery(navMesh);
		int maxTiles = navMesh.getMaxTiles();

		// Tile data can be null since maxTiles is not an exact science.
		for (int i = 0; i < maxTiles; i++) {
		    MeshData meshData = navMesh.getTile(i).data;
		    if (meshData != null) {
			showDebugByArea(meshData, true);
		    }
		}
	    } catch (IOException ex) {
		LOG.error("[{}]", ex);
	    }
    }  
 
    private void buildTileCache() {
    	
    	//Build merged mesh.
        JmeInputGeomProvider geomProvider = new GeometryProviderBuilder2(worldMap).build();
        
	configureAreaMod(geomProvider);
        
        //Set offmesh connections.
        offMeshCon.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            //Id will be used for OffMeshConnections.
            int id = 0;
            
            @Override
            public void visit(Node node) { 
                /**
                 * offMeshCon has no skeleton and is instance of node so the 
                 * search will include its children. This will return with a 
                 * child SkeletonControl because of this. Add check to skip 
                 * offMeshCon.
                 */
                if (!node.getName().equals(offMeshCon.getName())) {

                    SkeletonControl skelControl = GameObject.getComponentInChild(node, SkeletonControl.class);

                    if (skelControl != null) {
                        /**
                        * Offmesh connections require two connections, a 
                        * start/end vector3f and must connect to a surrounding 
                        * tile. To complete a connection, start and end must be 
                        * the same for each. You can supply the Vector3f manually 
                        * or for example, use bones from an armature. When using 
                        * bones, they should be paired and use a naming convention. 
                        * 
                        * In our case, we used bones and this naming convention:
                        * 
                        * arg[0](delimiter)arg[1](delimiter)arg[2]
                        * 
                        * We set each bone origin to any vertices, in any mesh, 
                        * as long as the same string for arg[0] and arg[1] are 
                        * identical and they do not use the same vertices. We 
                        * duplicate two polygons (triangles) in the mesh or 
                        * separate meshes and add an armature(s) using a naming
                        * convention.
                        * 
                        * Naming convention for two bones: 
                        * 
                        * Bone 1 naming: offmesh.anything.a
                        * Bone 2 naming: offmesh.anything.b
                        * 
                        * arg[0]: offmesh   = same value all bones
                        * arg[1]: anything  = same value paired bones
                        * arg[2]: a or b    = one paired bone
                        * 
                        * The value of arg[0] applies to ALL bones and 
                        * dictates these are link bones.
                        * 
                        * The value of arg[1] dictates these pair of bones 
                        * belong together. 
                        * 
                        * The value of arg[2] distinguishes the paired bones 
                        * from each other.
                        * 
                        * Examples: 
                        * 
                        * offmesh.pond.a
                        * offmesh.pond.b
                        * offmesh.1.a
                        * offmesh.1.b
                        */
                        Bone[] roots = skelControl.getSkeleton().getRoots();
                        for (Bone b: roots) {
                            /**
                             * Split the name up using delimiter. 
                             */
                            String[] arg = b.getName().split("\\.");

                            if (arg[0].equals("offmesh")) {

                                //New connection.
                                org.recast4j.detour.OffMeshConnection link1 = new org.recast4j.detour.OffMeshConnection();

                                /**
                                 * The bones worldTranslation will be the start
                                 * or end Vector3f of each OffMeshConnection 
                                 * object.
                                 */
                                float[] linkPos = DetourUtils.toFloatArray(node.localToWorld(b.getModelSpacePosition(), null));

                                /**
                                 * Prepare new position array. The endpoints of 
                                 * the connection. 
                                 * 
                                 *  startPos    endPos
                                 * [ax, ay, az, bx, by, bz]
                                 */
                                float[] pos = new float[6];

                                /**
                                 * Copy link1 current position to pos array. If
                                 * link1 is bone (a), it becomes the link start.
                                 * If (b), the link end.
                                 */
					System.arraycopy(linkPos, 0, pos, arg[2].equals("a") ? 0 : 3, 3);

                                //Set link1 to new array.
                                link1.pos = pos;

                                //Player (r)adius. Links fire at (r) * 2.25.
                                link1.rad = radius;
                                
                                /**
                                 * Move through link1 both directions. Only 
                                 * works if both links have identical in start/end.
                                 * Set to 0 for one direction link.
                                 */
                                link1.flags = NavMesh.DT_OFFMESH_CON_BIDIR;

                                /**
                                 * We need to look for the bones mate. Based off 
                                 * our naming convention, this will be 
                                 * offmesh.anything."a" or "b" so we set the 
                                 * search to whatever link1 arg[2] isn't.
                                 */
					String link2 = String.join(".", arg[0], arg[1], arg[2].equals("a") ? "b" : "a");

                                /**
                                 * If the paired bone has already been added to 
                                 * map, set start or end determined by link1 arg[2].
                                 */
                                if (mapOffMeshCon.containsKey(link2)) {
					/**
					 * Copy link1 pos to link2 pos. If link1 is start(a) of link, copy link1 start
					 * to link2 start. If link1 is the end(b) of link, copy link1 end to link2 end.
					 */
					System.arraycopy(link1.pos, arg[2].equals("a") ? 0 : 3,
							mapOffMeshCon.get(link2).pos, arg[2].equals("a") ? 0 : 3, 3);

					/**
					 * Copy link2 pos to link1 pos. If link1 is the start(a) of link, copy link2 end
					 * to link1 end. If link1 is end(b) of link, copy link2 start to link1 start.
					 */
					System.arraycopy(mapOffMeshCon.get(link2).pos, arg[2].equals("a") ? 3 : 0,
							link1.pos, arg[2].equals("a") ? 3 : 0, 3);

                                    /**
                                     * OffMeshconnections with id of 0 don't get 
                                     * processed later if not set here.
                                     */
                                    if (arg[2].equals("a")) {
                                        link1.userId = ++id;
                                        LOG.info("OffMeshConnection [{}] id  [{}]", b.getName(), link1.userId);
                                        LOG.info("OffMeshConnection [{}] pos {}", b.getName(), link1.pos);
                                        mapOffMeshCon.get(link2).userId = ++id;
                                        LOG.info("OffMeshConnection [{}] id  [{}]", link2, mapOffMeshCon.get(link2).userId);
                                        LOG.info("OffMeshConnection [{}] pos {}", link2, mapOffMeshCon.get(link2).pos);
                                    } else {
                                        mapOffMeshCon.get(link2).userId = ++id;
                                        LOG.info("OffMeshConnection [{}] id  [{}]", link2, mapOffMeshCon.get(link2).userId);
                                        LOG.info("OffMeshConnection [{}] pos {}", link2, mapOffMeshCon.get(link2).pos);
                                        link1.userId = ++id;
                                        LOG.info("OffMeshConnection [{}] id  [{}]", b.getName(), link1.userId);
                                        LOG.info("OffMeshConnection [{}] pos {}", b.getName(), link1.pos);
                                    }
                                }
                                //Add this bone to map.
                                mapOffMeshCon.put(b.getName(), link1);
                            }
                        }
                    }
                }
            }
        }); 
        
        //Clean up offMesh connections.
        offMeshCon.detachAllChildren();
                
        //Step 2. Create a Recast configuration object.
        RecastConfigBuilder builder = new RecastConfigBuilder();
        //Instantiate the configuration parameters.
        RecastConfig rcConfig = builder
                .withAgentRadius(radius)            // r
                .withAgentHeight(height)            // h
                //cs and ch should be .1 at min.
                .withCellSize(0.1f)                 // cs=r/2
                .withCellHeight(0.1f)               // ch=cs/2 but not < .1f
                .withAgentMaxClimb(maxClimb)        // > 2*ch
                .withAgentMaxSlope(45f)
                .withEdgeMaxLen(3.2f)               // r*8
                .withEdgeMaxError(1.3f)             // 1.1 - 1.5
                .withDetailSampleDistance(6.0f)     // increase if exception
                .withDetailSampleMaxError(6.0f)     // increase if exception
                .withVertsPerPoly(3)
                .withPartitionType(PartitionType.MONOTONE)
                .withTileSize(16).build();

        //Build the tile cache which also builds the navMesh.
        TileCache tc = getTileCache(geomProvider, rcConfig);    
            
        /**
         * Layers represent heights for the tile cache. For example, a bridge
         * with an underpass would have a layer for travel under the bridge and 
         * another for traveling over the bridge.
         */
        TileLayerBuilder layerBuilder = new TileLayerBuilder(geomProvider, rcConfig);

        List<byte[]> layers = layerBuilder.build(ByteOrder.BIG_ENDIAN, false, 1);
        
        for (byte[] data : layers) {
            try {
                /**
                 * The way tile cache works is you have two tiles, one is for 
                 * the cache and is added here with addTile. The other is for 
                 * the NavMesh and is added with buildNavMeshTile.
                 */
                long ref = tc.addTile(data, 0);
                tc.buildNavMeshTile(ref);
                
            } catch (IOException ex) {
                LOG.error("{} {}" + NavState.class.getName(), ex);
            }
        }  
                    
        //Save and read back for testing.
        TileCacheWriter writer = new TileCacheWriter(); 
        TileCacheReader reader = new TileCacheReader();  

        try {
            //Write our file.
            writer.write(new FileOutputStream(new File("test.tc")), tc, ByteOrder.BIG_ENDIAN, false);
            //Create new tile cache.
            tc = reader.read(new FileInputStream("test.tc"), 3, new JmeTileCacheMeshProcess());

            //Get the navMesh and build a querry object.
            navMesh = tc.getNavMesh();
            query = new NavMeshQuery(navMesh); 

            /**
             * Process OffMeshConnections. Since we are reading this in we do it 
             * here. If we were just running with the tile cache we first 
             * created we would just place this after building the tiles.
             * Basic flow: 
             * Check each mapOffMeshConnection for an index > 0. 
             * findNearestPoly() for the start/end positions of the link.
             * getTileAndPolyByRef() using the returned poly reference.
             * If both start and end are good values, set the connection properties.
             */
            Iterator<Map.Entry<String, org.recast4j.detour.OffMeshConnection>> itOffMesh = mapOffMeshCon.entrySet().iterator();
            while (itOffMesh.hasNext()) {
                Map.Entry<String, org.recast4j.detour.OffMeshConnection> next = itOffMesh.next();

                /**
                 * If the OffMeshConnection id is 0, there is no paired bone for the
                 * link so skip.
                 */            
                if (next.getValue().userId > 0) {
                    //Create a new filter for findNearestPoly
                    DefaultQueryFilter filter = new DefaultQueryFilter();

                    //In our case, we only need swim or walk flags.
                    int include = POLYFLAGS_WALK | POLYFLAGS_SWIM;
                    filter.setIncludeFlags(include);

                    //No excludes.
                    int exclude = 0;
                    filter.setExcludeFlags(exclude);

                    //Get the start position for the link.
                    float[] startPos = new float[3];
                    System.arraycopy(next.getValue().pos, 0, startPos, 0, 3);
                    //Get the end position for the link.
                    float[] endPos = new float[3];
                    System.arraycopy(next.getValue().pos, 3, endPos, 0, 3);

                    //Find the nearest polys to start/end.
                    Result<FindNearestPolyResult> startPoly = query.findNearestPoly(startPos, new float[] {radius,radius,radius}, filter);
                    Result<FindNearestPolyResult> endPoly = query.findNearestPoly(endPos, new float[] {radius,radius,radius}, filter);

                    /**
                     * Note: not isFailure() here, because isSuccess guarantees us, 
                     * that the result isn't "RUNNING", which it could be if we only 
                     * check it's not failure.
                     */
					if (!startPoly.status.isSuccess() 
							|| !endPoly.status.isSuccess()
							|| startPoly.result.getNearestRef() == 0 
							|| endPoly.result.getNearestRef() == 0) {
                        LOG.error("offmeshCon findNearestPoly unsuccessful or getNearestRef is not > 0.");
                        LOG.error("Link [{}] pos {} id [{}]", next.getKey(), Arrays.toString(next.getValue().pos), next.getValue().userId);
                        LOG.error("findNearestPoly startPoly [{}] getNearestRef [{}]", startPoly.status.isSuccess(), startPoly.result.getNearestRef());
                        LOG.error("findNearestPoly endPoly [{}] getNearestRef [{}].", endPoly.status.isSuccess(), endPoly.result.getNearestRef());
                    } else {
                        //Get the tile and poly from reference.
                        Result<Tupple2<MeshTile, Poly>> startTileByRef = navMesh.getTileAndPolyByRef(startPoly.result.getNearestRef());
                        Result<Tupple2<MeshTile, Poly>> endTileByRef = navMesh.getTileAndPolyByRef(endPoly.result.getNearestRef());

                        //Mesh data for the start/end tile.
                        MeshData startTile = startTileByRef.result.first.data;
                        MeshData endTile = endTileByRef.result.first.data;

                        //Both start and end poly must be vailid.
                        if (startTileByRef.result.second != null && endTileByRef.result.second != null) {
                            //We will add a new poly that will become our "link" 
                            //between start and end points so make room for it.
                            startTile.polys = Arrays.copyOf(startTile.polys, startTile.polys.length + 1);
                            //We shifted everything but haven't incremented polyCount 
                            //yet so this will become our new poly's index.
                            int poly = startTile.header.polyCount;
                            /**
                             * Off-mesh connections are stored in the navigation 
                             * mesh as special 2-vertex polygons with a single edge. 
                             * At least one of the vertices is expected to be inside 
                             * a normal polygon. So an off-mesh connection is 
                             * "entered" from a normal polygon at one of its 
                             * endpoints. Jme requires 3 vertices per poly to 
                             * build a debug mesh so we have to create a 
                             * 3-vertex polygon here if using debug. The extra 
                             * vertex position will be connected automatically 
                             * when we add the tile back to the navmesh. For 
                             * games, this would be a two vert poly.
                             * 
                             * See: https://github.com/ppiastucki/recast4j/blob/3c532068d79fe0306fedf035e50216008c306cdf/detour/src/main/java/org/recast4j/detour/NavMesh.java#L406
                             */
                            startTile.polys[poly] = new Poly(poly, 3);
                            /**
                             * Must add/create our new indices for start and end.
                             * When we add the tile, the third vert will be 
                             * generated for us. 
                             */
                            startTile.polys[poly].verts[0] = startTile.header.vertCount;
                            startTile.polys[poly].verts[1] = startTile.header.vertCount + 1;
                            //Set the poly's type to DT_POLYTYPE_OFFMESH_CONNECTION
                            //so it is not seen as a regular poly when linking.
                            startTile.polys[poly].setType(Poly.DT_POLYTYPE_OFFMESH_CONNECTION);
                            //Make room for our start/end verts.
                            startTile.verts = Arrays.copyOf(startTile.verts, startTile.verts.length + 6);
                            //Increment our poly and vert counts.
                            startTile.header.polyCount++;
                            startTile.header.vertCount += 2;
                            //Set our OffMeshLinks poly to this new poly.
                            next.getValue().poly = poly;
                            //Shorten names and make readable. Could just call directly.
                            float[] start = startPoly.result.getNearestPos();
                            float[] end = endPoly.result.getNearestPos();
                            //Set the links position array values to nearest.
                            next.getValue().pos = new float[] { start[0], start[1], start[2], end[0], end[1], end[2] };
                            //Determine what side of the tile the vertx is on.
                            next.getValue().side = startTile == endTile ? 0xFF
                                    : NavMeshBuilder.classifyOffMeshPoint(new VectorPtr(next.getValue().pos, 3),
                                            startTile.header.bmin, startTile.header.bmax);
                            //Create new OffMeshConnection array.
				if (startTile.offMeshCons == null) {
					startTile.offMeshCons = new org.recast4j.detour.OffMeshConnection[1];
				} else {
					startTile.offMeshCons = Arrays.copyOf(startTile.offMeshCons, startTile.offMeshCons.length + 1);
				}

                            //Add this connection.
                            startTile.offMeshCons[startTile.offMeshCons.length - 1] = next.getValue();
                            startTile.header.offMeshConCount++;

                            //Set the polys area type and flags.
                            startTile.polys[poly].flags = POLYFLAGS_JUMP;
                            startTile.polys[poly].setArea(POLYAREA_TYPE_JUMP);

                            /**
                             * Removing and adding the tile will rebuild all the 
                             * links for the tile automatically. The number of links 
                             * is : edges + portals * 2 + off-mesh con * 2.
                             */
                            MeshData removeTile = navMesh.removeTile(navMesh.getTileRef(startTileByRef.result.first));
                            navMesh.addTile(removeTile, 0, navMesh.getTileRef(startTileByRef.result.first));                      
                        }
                    }       
                }
            }
            
            int maxTiles = tc.getTileCount();

            //Tile data can be null since maxTiles is not an exact science.
            for (int i = 0; i < maxTiles; i++) {
                MeshTile tile = tc.getNavMesh().getTile(i);
                MeshData meshData = tile.data;
                if (meshData != null ) {
                    showDebugByArea(meshData, true);
                }
            }
        } catch (IOException ex) {
            LOG.error("{} {}", NavState.class.getName(), ex);
        }
        
    }
    
    private void saveToFile(MeshData md) throws Exception {
        MeshDataWriter mdw = new MeshDataWriter();
        File f = new File("test.md");
        System.out.println("Saving MeshData=" + f.getAbsolutePath());
        mdw.write(new FileOutputStream(f), md, ByteOrder.BIG_ENDIAN, false);
    }

    private void saveToFile(NavMesh nm) throws Exception {
        MeshSetWriter msw = new MeshSetWriter();
        File f = new File("test.nm");
        System.out.println("Saving NavMesh=" + f.getAbsolutePath());
        msw.write(new FileOutputStream(f), nm, ByteOrder.BIG_ENDIAN, false);
    }
    
    private void configureAreaMod(JmeInputGeomProvider geomProvider) {
        worldMap.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Geometry spat) {

                int geomLength = spat.getMesh().getTriangleCount() * 3;
                String[] name = spat.getMaterial().getName().split("_");

                switch (name[0]) {
                    case "water":
                        geomProvider.addMod(new Modification(geomLength, AREAMOD_WATER));
                        break;
                    case "road":
                        geomProvider.addMod(new Modification(geomLength, AREAMOD_ROAD));
                        break;
                    case "grass":
                        geomProvider.addMod(new Modification(geomLength, AREAMOD_GRASS));
                        break;
                    case "door":
                        geomProvider.addMod(new Modification(geomLength, AREAMOD_DOOR));
                        break;
                    default:
                        geomProvider.addMod(new Modification(geomLength, AREAMOD_GROUND));
                }
            }
        });
    }
    
    /**
     * This is a mandatory class otherwise the tile cache build will not set
     * the areas. This gets call from the tc.buildNavMeshTile(ref) method.
     */
    private class JmeTileCacheMeshProcess implements TileCacheMeshProcess {

        @Override
        public void process(NavMeshDataCreateParams params) {
            // Update poly flags from areas.
            for (int i = 0; i < params.polyCount; ++i) {
                Poly p = new Poly(i, 6);

                if (params.polyAreas[i] == DT_TILECACHE_WALKABLE_AREA) {
                    params.polyAreas[i] = POLYAREA_TYPE_GROUND;
                }

                if (params.polyAreas[i] == POLYAREA_TYPE_GROUND ||
                    params.polyAreas[i] == POLYAREA_TYPE_GRASS ||
                    params.polyAreas[i] == POLYAREA_TYPE_ROAD) {
                    params.polyFlags[i] = POLYFLAGS_WALK;
                    
                } else if (params.polyAreas[i] == POLYAREA_TYPE_WATER) {
                    params.polyFlags[i] = POLYFLAGS_SWIM;
                    
                } else if (params.polyAreas[i] == POLYAREA_TYPE_DOOR) {
                    params.polyFlags[i] = POLYFLAGS_WALK | POLYFLAGS_DOOR;
                }
            }
        }
    }

    //Build the tile cache.
    private TileCache getTileCache(JmeInputGeomProvider geom, RecastConfig rcfg) {
        final int EXPECTED_LAYERS_PER_TILE = 4;
        
        TileCacheParams params = new TileCacheParams();
        int[] twh = Recast.calcTileCount(geom.getMeshBoundsMin(), geom.getMeshBoundsMax(), rcfg.cs, rcfg.tileSize);
        params.ch = rcfg.ch;
        params.cs = rcfg.cs;
        DetourCommon.vCopy(params.orig, geom.getMeshBoundsMin());
        params.height = rcfg.tileSize;
        params.width = rcfg.tileSize;
        params.walkableHeight = height;
        params.walkableRadius = radius;
        params.walkableClimb = maxClimb;
        params.maxSimplificationError = rcfg.maxSimplificationError;
        params.maxTiles = twh[0] * twh[1] * EXPECTED_LAYERS_PER_TILE;
        params.maxObstacles = 128;
        
        NavMeshParams navMeshParams = new NavMeshParams();
        RecastVectors.copy(navMeshParams.orig, geom.getMeshBoundsMin());
        navMeshParams.tileWidth = rcfg.tileSize * rcfg.cs;
        navMeshParams.tileHeight = rcfg.tileSize * rcfg.cs;
        navMeshParams.maxTiles = params.maxTiles;
        navMeshParams.maxPolys = 16384;
        
        NavMesh navMesh = new NavMesh(navMeshParams, 3);

        return new TileCache(params, new TileCacheStorageParams(ByteOrder.BIG_ENDIAN, false), navMesh, TileCacheCompressorFactory.get(false), new JmeTileCacheMeshProcess());
    }
    
    /**
     * Prints any polygons found flags to the log.
     * 
     * @param poly The polygon id to look for flags.
     */
    private void printFlags(long poly) {
        if (isBitSet(POLYFLAGS_DOOR, navMesh.getPolyFlags(poly).result)) {
            LOG.info("POLYFLAGS_DOOR [{}]", POLYFLAGS_DOOR);
        }
        if (isBitSet(POLYFLAGS_WALK, navMesh.getPolyFlags(poly).result)) {
            LOG.info("POLYFLAGS_WALK [{}]", POLYFLAGS_WALK);
        }
        if (isBitSet(POLYFLAGS_SWIM, navMesh.getPolyFlags(poly).result)) {
            LOG.info("POLYFLAGS_SWIM [{}]", POLYFLAGS_SWIM);
        }
        if (isBitSet(POLYFLAGS_JUMP, navMesh.getPolyFlags(poly).result)) {
            LOG.info("POLYFLAGS_JUMP [{}]", POLYFLAGS_JUMP);
        }
        if (isBitSet(POLYFLAGS_DISABLED, navMesh.getPolyFlags(poly).result)) {
            LOG.info("POLYFLAGS_DISABLED [{}]", POLYFLAGS_DISABLED);
        }
    }

    /**
     * Prints any flag found in the supplied flags.
     * @param flags The flags to print.
     */
    private void printFlags(int flags) {
        if (flags == 0) {
            LOG.info("No flag found.");
        }
        if (isBitSet(POLYFLAGS_DOOR, flags)) {
            LOG.info("POLYFLAGS_DOOR         [{}]", POLYFLAGS_DOOR);
        }
        if (isBitSet(POLYFLAGS_WALK, flags)) {
            LOG.info("POLYFLAGS_WALK         [{}]", POLYFLAGS_WALK);
        }
        if (isBitSet(POLYFLAGS_SWIM, flags)) {
            LOG.info("POLYFLAGS_SWIM         [{}]", POLYFLAGS_SWIM);
        }
        if (isBitSet(POLYFLAGS_JUMP, flags)) {
            LOG.info("POLYFLAGS_JUMP         [{}]", POLYFLAGS_JUMP);
        }
        if (isBitSet(POLYFLAGS_DISABLED, flags)) {
            LOG.info("POLYFLAGS_DISABLED     [{}]", POLYFLAGS_DISABLED);
        }
    }
    
    /**
     * Checks whether a bit flag is set.
     * 
     * @param flag The flag to check for.
     * @param flags The flags to check for the supplied flag.
     * @return True if the supplied flag is set for the given flags.
     */
    private boolean isBitSet(int flag, int flags) {
        return (flags & flag) == flag;
    }
    
    /**
     * Class to hold the obj id and flags for the MouseEventControl check that 
     * assures all flags are the same.
     */
    private class PolyAndFlag {
    	
        private long poly;
        private int flag;

        public PolyAndFlag(long poly, int flag) {
            this.poly = poly;
            this.flag = flag;
        }

        public long getPoly() {
            return poly;
        }

        public int getFlag() {
            return flag;
        }

    }
    
}
