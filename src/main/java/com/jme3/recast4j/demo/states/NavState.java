/*
 * The MIT License
 *
 * Copyright 2021.
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

import static com.jme3.recast4j.demo.JmeAreaMods.AREAMOD_DOOR;
import static com.jme3.recast4j.demo.JmeAreaMods.AREAMOD_GRASS;
import static com.jme3.recast4j.demo.JmeAreaMods.AREAMOD_GROUND;
import static com.jme3.recast4j.demo.JmeAreaMods.AREAMOD_ROAD;
import static com.jme3.recast4j.demo.JmeAreaMods.AREAMOD_WATER;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_DOOR;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_GRASS;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_GROUND;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_JUMP;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_ROAD;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_WATER;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYFLAGS_DISABLED;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYFLAGS_DOOR;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYFLAGS_JUMP;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYFLAGS_SWIM;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYFLAGS_WALK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.recast4j.detour.OffMeshConnection;
import org.recast4j.detour.Poly;
import org.recast4j.detour.Result;
import org.recast4j.detour.Tupple2;
import org.recast4j.detour.VectorPtr;
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
import org.recast4j.recast.ContourSet;
import org.recast4j.recast.Heightfield;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.Recast;
import org.recast4j.recast.RecastArea;
import org.recast4j.recast.RecastBuilder;
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

import com.jme3.anim.util.AnimMigrationUtils;
import com.jme3.animation.Bone;
import com.jme3.animation.SkeletonControl;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.ai.NavMeshAgent;
import com.jme3.recast4j.ai.NavMeshAgentDebug;
import com.jme3.recast4j.ai.NavMeshHit;
import com.jme3.recast4j.ai.NavMeshPath;
import com.jme3.recast4j.ai.NavMeshPathStatus;
import com.jme3.recast4j.ai.NavMeshQueryFilter;
import com.jme3.recast4j.ai.StraightPathOptions;
import com.jme3.recast4j.demo.controls.Animator;
import com.jme3.recast4j.demo.controls.DoorSwingControl;
import com.jme3.recast4j.demo.controls.PCControl;
import com.jme3.recast4j.demo.utils.GameObject;
import com.jme3.recast4j.demo.utils.MainCamera;
import com.jme3.recast4j.detour.DetourUtils;
import com.jme3.recast4j.geom.InputGeomProviderBuilder;
import com.jme3.recast4j.geom.JmeInputGeomProvider;
import com.jme3.recast4j.geom.JmeRecastBuilder;
import com.jme3.recast4j.geom.JmeRecastVoxelization;
import com.jme3.recast4j.geom.JmeTileLayersBuilder;
import com.jme3.recast4j.geom.JmeRecastBuilderProgressListener;
import com.jme3.recast4j.geom.NavMeshModifier;
import com.jme3.recast4j.geom.OffMeshLink;
import com.jme3.recast4j.geom.Telemetry;
import com.jme3.recast4j.recast.NavMeshAssetManager;
import com.jme3.recast4j.recast.RecastConfigBuilder;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseEventControl;

/**
 *
 * @author Robert
 * @author capdevon
 */
public class NavState extends AbstractNavState {

    private static final Logger logger = LoggerFactory.getLogger(NavState.class.getName());

    private static final String MODEL_NAME = "jaime";

    private Node worldMap;
    private Node offMeshCon;
    private NavMesh navMesh;
    private NavMeshQuery navQuery;
    private Map<String, OffMeshConnection> mapOffMeshCon = new HashMap<>();

    private float agentRadius = 0.3f;
    private float agentHeight = 1.7f;
    private float agentMaxClimb = 0.3f; // > 2*ch
    private float cellSize = 0.1f;      // cs=r/2
    private float cellHeight = 0.1f;    // ch=cs/2 but not < .1f

    @Override
    protected void onEnable() {
        worldMap = (Node) rootNode.getChild("worldmap");
        offMeshCon = (Node) rootNode.getChild("offMeshCon");

        //====================================================================
        System.out.println("Building NavMesh... this may freeze your computer for a few seconds, please stand by");
        long startTime = System.currentTimeMillis();

//        //Original implementation using jme3-recast4j methods.
//        buildSolo();
//        //Solo build using jme3-recast4j methods. Implements area and flag types.
        buildSoloModified();
//        //Solo build using recast4j methods. Implements area and flag types.
//        buildSoloRecast4j();
//        //Tile build using recast4j methods. Implements area and flag types plus offmesh connections.
//        buildTiledRecast4j();
//        buildTileCache();

        long buildTime = (System.currentTimeMillis() - startTime);
        System.out.println("Building succeeded after " + buildTime + " ms");
        //====================================================================

        initWorldMouseListener();

        setupDoors();
    }

    @Override
    protected void onDisable() {
        Spatial jaime = rootNode.getChild(MODEL_NAME);
        NavMeshAgent agent = jaime.getControl(NavMeshAgent.class);
        jaime.removeControl(agent);
    }

    private void setupDoors() {
        //If the doorNode in DemoApplication is not null, we will create doors.
        Node doorNode = (Node) rootNode.getChild("doorNode");

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
            List<Spatial> children = doorNode.getChildren();

            /**
             * Cycle through the list and add a MouseEventControl to each door
             * with a DoorSwingControl.
             */
            for (Spatial child : children) {

                DoorSwingControl swingControl = GameObject.getComponentInChildren(child, DoorSwingControl.class);

                if (swingControl != null) {
                    /**
                     * We are adding the MouseEventControl to the doors hitBox
                     * not the door. It would be easier to use the door by
                     * turning hardware skinning off but for some reason it
                     * always throws an exception when doing so. The hitBox is
                     * attached to the root bones attachment node.
                     */
                    SkeletonControl skControl = GameObject.getComponentInChildren(child, SkeletonControl.class);
                    String name = skControl.getSkeleton().getBone(0).getName();
                    Spatial hitBox = skControl.getAttachmentsNode(name).getChild(0);

                    addDoorMouseListener(swingControl, hitBox);
                }
            }
        }
    }

    private void addDoorMouseListener(DoorSwingControl swingControl, Spatial hitBox) {
        MouseEventControl.addListenersToSpatial(hitBox, new DefaultMouseListener() {

            @Override
            protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {

                logger.info("<========== BEGIN Door MouseEventControl ==========>");

                /**
                 * We have the worldmap and the doors using MouseEventControl.
                 * In certain circumstances, usually when moving and clicking,
                 * click will return target as worldmap so we have to then use
                 * capture to get the proper spatial.
                 */
                if (!target.equals(hitBox)) {
                    logger.info("Wrong target found [{}] parentName [{}].", target.getName(), target.getParent().getName());
                    logger.info("Switching to capture [{}] capture parent [{}].", capture.getName(), capture.getParent().getName());
                    target = capture;
                }

                //The filter to use for this search.
                DefaultQueryFilter filter = new DefaultQueryFilter();

                //Limit the search to only door flags.
                int includeFlags = POLYFLAGS_DOOR;
                filter.setIncludeFlags(includeFlags);

                //Include everything.
                int excludeFlags = 0;
                filter.setExcludeFlags(excludeFlags);

                /**
                 * Look for the largest radius to search for. This will make it
                 * possible to grab only one of a double door. The width of the
                 * door is preferred over thickness. The idea is to only return
                 * polys within the width of the door so in cases where there
                 * are double doors, only the selected door will open/close.
                 * This means doors with large widths should not be in range of
                 * other doors or the other doors polys will be included.
                 *
                 * Searches take place from the origin of the attachment node
                 * which should be the same as the doors origin.
                 */
                BoundingBox bounds = (BoundingBox) target.getWorldBound();
                //Width of door opening.
                float maxXZ = Math.max(bounds.getXExtent(), bounds.getZExtent()) * 2;
                float[] center = target.getWorldTranslation().toArray(null);
                float[] halfExtents = new float[]{maxXZ, maxXZ, maxXZ};

                Result<FindNearestPolyResult> findNearestPoly = navQuery.findNearestPoly(center, halfExtents, filter);

                //No obj, no go. Fail most likely result of filter setting.
                if (!findNearestPoly.succeeded() || findNearestPoly.result.getNearestRef() == 0) {
                    logger.error("Door findNearestPoly unsuccessful or getNearestRef is not > 0.");
                    logger.error("findNearestPoly [{}] getNearestRef [{}].", findNearestPoly.status, findNearestPoly.result.getNearestRef());
                    return;
                }

                Result<FindPolysAroundResult> findPolysAroundCircle = navQuery.findPolysAroundCircle(findNearestPoly.result.getNearestRef(), findNearestPoly.result.getNearestPos(), maxXZ, filter);

                //Success
                if (findPolysAroundCircle.succeeded()) {

                    List<Long> m_polys = findPolysAroundCircle.result.getRefs();
                    //May need these for something else eventually.
                    //List<Long> m_parent = result.result.getParentRefs();
                    //List<Float> m_costs = result.result.getCosts();

                    /**
                     * Store each poly and flag in a single object and add it to
                     * this list so we can later check they all have the same
                     * flag.
                     */
                    List<PolyAndFlag> listPolyFlag = new ArrayList<>();

                    //The flags that say this door is open.
                    int openFlags = POLYFLAGS_WALK | POLYFLAGS_DOOR;

                    //The flags that say this door is closed.
                    int closedFlags = openFlags | POLYFLAGS_DISABLED;

                    /**
                     * We iterate through the polys looking for the open or
                     * closed flags.
                     */
                    for (long poly : m_polys) {

                        logger.info("PRE flag set Poly ID [{}] Flags [{}]", poly, navMesh.getPolyFlags(poly).result);
                        printFlags(poly);

                        /**
                         * We look for closed or open doors and add the poly id
                         * and flag to set for the poly to the list. We will
                         * later check to see if all poly flags are the same and
                         * act accordingly. If the door is closed, we add the
                         * open flags, if open, add the closed flags.
                         */
                        if (isBitSet(closedFlags, navMesh.getPolyFlags(poly).result)) {
                            listPolyFlag.add(new PolyAndFlag(poly, openFlags));

                        } else if (isBitSet(openFlags, navMesh.getPolyFlags(poly).result)) {
                            listPolyFlag.add(new PolyAndFlag(poly, closedFlags));
                        }
                    }

                    /**
                     * Check that all poly flags for the door are either all
                     * open or all closed. This prevents changing door flags in
                     * circumstances where a user may be allowed to block open
                     * or closed doors with in game objects through tile
                     * caching. If the object was placed in such a way that not
                     * all polys in a door opening were blocked by the object,
                     * not checking if all polys had the same flag would allow
                     * bypassing the blocking object flag setting.
                     */
                    boolean same = true;
                    for (PolyAndFlag obj : listPolyFlag) {
                        //If any flag does not match, were done.
                        if (obj.flag != listPolyFlag.get(0).flag) {
                            logger.info("All poly flags are not the same listPolyAndFlag.");
                            same = false;
                            break;
                        }
                    }

                    //If all flags match set door open/closed.
                    if (same) {
                        //Set all obj flags.
                        for (PolyAndFlag obj : listPolyFlag) {
                            navMesh.setPolyFlags(obj.poly, obj.flag);
                            logger.info("POST flag set Poly ID [{}] Flags [{}]", obj.poly, navMesh.getPolyFlags(obj.poly).result);
                            printFlags(obj.poly);
                        }

                        /**
                         * All flags are the same so we only need the first
                         * object.
                         */
                        if (listPolyFlag.get(0).flag == (openFlags)) {
                            //Open doorControl.
                            swingControl.setOpen(true);
                        } else {
                            //Close doorControl.
                            swingControl.setOpen(false);
                        }
                    }
                }
                logger.info("<========== END Door MouseEventControl Add ==========>");
            }
        });
    }

    private Node loadJaime() {
        Spatial model = assetManager.loadModel("Models/Jaime/Jaime.j3o");
        Node npc = (Node) AnimMigrationUtils.migrate(model);
        npc.setName(MODEL_NAME);
        npc.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(npc);
        
        npc.addControl(new BetterCharacterControl(agentRadius, agentHeight, 10f));
        getPhysicsSpace().add(npc);

        return npc;
    }

    private void initWorldMouseListener() {

        Node npc = loadJaime();
        npc.addControl(new Animator());
        npc.addControl(new NavMeshAgent(navMesh));
        npc.addControl(new NavMeshAgentDebug(assetManager));
        npc.addControl(new PCControl());

        int includeFlags = POLYFLAGS_WALK | POLYFLAGS_DOOR | POLYFLAGS_SWIM | POLYFLAGS_JUMP;
        int excludeFlags = POLYFLAGS_DISABLED;
        //Extents can be anything you determine is appropriate.
        float[] polyExtents = new float[]{1, 1, 1};

        NavMeshQueryFilter filter = new NavMeshQueryFilter(includeFlags, excludeFlags);
        filter.setPolyExtents(polyExtents);
        filter.setStraightPathOptions(StraightPathOptions.AllCrossings);
        // Change costs.
        filter.setAreaCost(POLYAREA_TYPE_GROUND, 1.0f);
        filter.setAreaCost(POLYAREA_TYPE_WATER, 10.0f);
        filter.setAreaCost(POLYAREA_TYPE_ROAD, 1.0f);
        filter.setAreaCost(POLYAREA_TYPE_DOOR, 1.0f);
        filter.setAreaCost(POLYAREA_TYPE_GRASS, 2.0f);
        filter.setAreaCost(POLYAREA_TYPE_JUMP, 1.5f);

        NavMeshAgent agent = npc.getControl(NavMeshAgent.class);
        agent.setQueryFilter(filter);

        MouseEventControl.addListenersToSpatial(worldMap, new DefaultMouseListener() {
            @Override
            protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {

                // First clear existing pathGeometries from the old path finding
                debugHelper.clear();

                if (event.getButtonIndex() == MouseInput.BUTTON_LEFT) {

                    Vector3f locOnMap = getLocationOnMap();
                    System.out.println("Compute path from " + npc.getWorldTranslation() + " to " + locOnMap);

                    NavMeshPath navPath = new NavMeshPath();
                    agent.calculatePath(locOnMap, navPath);

                    if (navPath.getStatus() == NavMeshPathStatus.PathComplete) {
                        agent.setPath(navPath);

                        float yOffset = .5f;
                        debugHelper.color = ColorRGBA.Green;
                        debugHelper.drawCube(npc.getWorldTranslation().add(0, yOffset, 0), .15f);
                        
                        debugHelper.color = ColorRGBA.Yellow;
                        debugHelper.drawCube(locOnMap.add(0, yOffset, 0), .15f);

                    } else {
                        System.err.println("Unable to find path");
                    }
                } else {
                    NavMeshHit hit = new NavMeshHit();
                    Vector3f targetPos = getLocationOnMap();
                    boolean blocked = agent.raycast(targetPos, hit);
                    
                    debugHelper.color = blocked ? ColorRGBA.Red : ColorRGBA.Green;
                    debugHelper.drawLine(npc.getWorldTranslation(), targetPos);
                }
            }
        });
    }

    /**
     * Returns the Location on the Map which is currently under the Cursor. For
     * this we use the Camera to project the point onto the near and far plane
     * (because we don'from have the depth information [map height]). Then we
     * can use this information to do a raycast, ideally the world is in between
     * those planes and we hit it at the correct place.
     *
     * @return The Location on the Map
     */
    public Vector3f getLocationOnMap() {
        Ray ray = MainCamera.screenPointToRay(camera, inputManager.getCursorPosition());
        CollisionResults collResults = new CollisionResults();
        worldMap.collideWith(ray, collResults);

        if (collResults.size() > 0) {
            return collResults.getClosestCollision().getContactPoint();
        } else {
            return null;
        }
    }

    /**
     * Original implementation using jme3-recast4j methods and custom
     * JmeRecastBuilder.
     */
    private void buildSolo() {
        //Clean up offMesh connections.
        offMeshCon.detachAllChildren();

        JmeInputGeomProvider m_geom = InputGeomProviderBuilder.build(worldMap);

        RecastConfig cfg = new RecastConfigBuilder()
                .withPartitionType(PartitionType.WATERSHED)
                .withWalkableAreaMod(AREAMOD_GROUND)
                .withAgentRadius(agentRadius)
                .withAgentHeight(agentHeight)
                .withCellSize(cellSize)
                .withCellHeight(cellHeight)
                .withAgentMaxClimb(agentMaxClimb)
                .withAgentMaxSlope(45f)
                .withEdgeMaxLen(2.4f) // r*8
                .withEdgeMaxError(1.3f) // 1.1 - 1.5
                .withDetailSampleDistance(8.0f) // increase if exception
                .withDetailSampleMaxError(8.0f) // increase if exception
                .withVertsPerPoly(3)
                .build();

        // Create a RecastBuilderConfig builder with world bounds of our geometry.
        RecastBuilderConfig builderCfg = new RecastBuilderConfig(cfg, m_geom.getMeshBoundsMin(), m_geom.getMeshBoundsMax());

        // Build our Navmesh data using our gathered geometry and configuration.
        JmeRecastBuilder rcBuilder = new JmeRecastBuilder();
        RecastBuilderResult rcResult = rcBuilder.build(m_geom, builderCfg);

        // Build the parameter object.
        NavMeshDataCreateParams params = getNavMeshCreateParams(m_geom,
                cellSize, cellHeight, agentHeight, agentRadius, agentMaxClimb, rcResult);

        //Generate MeshData using our parameters object.
        MeshData meshData = NavMeshBuilder.createNavMeshData(params);

        //Build the NavMesh.
        navMesh = new NavMesh(meshData, cfg.maxVertsPerPoly, 0);
        navQuery = new NavMeshQuery(navMesh);

        try {
            NavMeshAssetManager.save(meshData, makeFile("recast-solo.md"));
            NavMeshAssetManager.save(navMesh, makeFile("recast-solo.nm"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //Show wireframe. Helps with param tweaks. false = solid color.
        navMeshRenderer.drawMeshData(meshData, true);
    }

    /**
     * This example sets area type and flags based off geometry of each
     * individual mesh and uses the custom JmeRecastBuilder class with
     * jme3-recast4j wrapper methods.
     */
    private void buildSoloModified() {

        //Build merged mesh.
        JmeInputGeomProvider m_geom = InputGeomProviderBuilder.build(worldMap);
        setNavMeshModifiers(m_geom, worldMap);

        //Clean up offMesh connections.
        offMeshCon.detachAllChildren();

        RecastConfig cfg = new RecastConfigBuilder()
                .withPartitionType(RecastConstants.PartitionType.WATERSHED)
                .withWalkableAreaMod(AREAMOD_GROUND)
                .withAgentRadius(agentRadius)
                .withAgentHeight(agentHeight)
                .withCellSize(cellSize)
                .withCellHeight(cellHeight)
                .withAgentMaxClimb(agentMaxClimb)
                .withAgentMaxSlope(45f)
                .withEdgeMaxLen(2.4f) // r*8
                .withEdgeMaxError(1.3f) // 1.1 - 1.5
                .withDetailSampleDistance(8.0f) // increase if exception
                .withDetailSampleMaxError(8.0f) // increase if exception
                .withVertsPerPoly(3)
                .build();

        //Create a RecastBuilderConfig builder with world bounds of our geometry.
        RecastBuilderConfig builderCfg = new RecastBuilderConfig(cfg, m_geom.getMeshBoundsMin(), m_geom.getMeshBoundsMax());

        Telemetry telemetry = new Telemetry();
        // Rasterize input polygon soup.
        Heightfield solid = JmeRecastVoxelization.buildSolidHeightfield(m_geom, builderCfg, telemetry);
        
        JmeRecastBuilder rcBuilder = new JmeRecastBuilder();
        RecastBuilderResult rcResult = rcBuilder.build(builderCfg.borderSize, builderCfg.buildMeshDetail, m_geom, cfg, solid, telemetry);
        
        System.out.println("Telemetry:");
        telemetry.print();

        // Build the parameter object.
        NavMeshDataCreateParams params = getNavMeshCreateParams(m_geom,
                cellSize, cellHeight, agentHeight, agentRadius, agentMaxClimb, rcResult);

        updateAreaAndFlags(params);

        //Generate MeshData using our parameters object.
        MeshData meshData = NavMeshBuilder.createNavMeshData(params);

        //Build the NavMesh.
        navMesh = new NavMesh(meshData, cfg.maxVertsPerPoly, 0);
        navQuery = new NavMeshQuery(navMesh);

        //Create offmesh connections here.
        try {
            NavMeshAssetManager.save(meshData, makeFile("recast-solo-modified.md"));
            NavMeshAssetManager.save(navMesh, makeFile("recast-solo-modified.nm"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //Show wireframe. Helps with param tweaks. false = solid color.
        navMeshRenderer.drawMeshByArea(meshData, true);
    }

    /**
     * This example builds the mesh manually by using recast4j methods.
     * Implements area type and flag setting.
     */
    private void buildSoloRecast4j() {

        //Build merged mesh.
        JmeInputGeomProvider m_geom = InputGeomProviderBuilder.build(worldMap);
        setNavMeshModifiers(m_geom, worldMap);

        //Clean up offMesh connections.
        offMeshCon.detachAllChildren();

        //Get min/max bounds.
        Telemetry telemetry = new Telemetry();
        PartitionType partitionType = PartitionType.WATERSHED;

        RecastConfig cfg = new RecastConfigBuilder()
                .withPartitionType(partitionType)
                .withWalkableAreaMod(AREAMOD_GROUND)
                .withAgentRadius(agentRadius)
                .withAgentHeight(agentHeight)
                .withCellSize(cellSize)
                .withCellHeight(cellHeight)
                .withAgentMaxClimb(agentMaxClimb)
                .withAgentMaxSlope(45f)
                .withEdgeMaxLen(2.4f) // r*8
                .withEdgeMaxError(1.3f) // 1.1 - 1.5
                .withDetailSampleDistance(8.0f) // increase if exception
                .withDetailSampleMaxError(8.0f) // increase if exception
                .withVertsPerPoly(3)
                .build();

        RecastBuilderConfig builderCfg = new RecastBuilderConfig(cfg, m_geom.getMeshBoundsMin(), m_geom.getMeshBoundsMax());

        // Step 2. Rasterize input polygon soup.
        // Allocate voxel heightfield where we rasterize our input data to.
        Heightfield solid = new Heightfield(builderCfg.width, builderCfg.height, builderCfg.bmin, builderCfg.bmax, cfg.cs, cfg.ch);

        for (TriMesh geom : m_geom.meshes()) {
            float[] verts = geom.getVerts();
            int[] tris = geom.getTris();
            int ntris = tris.length / 3;

            // Allocate array that can hold triangle area types.
            // If you have multiple meshes you need to process, allocate
            // and array which can hold the max number of triangles you need to
            // process.
            
            // Find triangles which are walkable based on their slope and rasterize them.
            // If your input data is multiple meshes, you can transform them here,
            // calculate the are type for each of the meshes and rasterize them.
            
            // ** START NEW CUSTOM CODE **
            //Separate individual triangles into a arrays so we can mark Area Type.
            List<int[]> listTris = new ArrayList<>();
            int fromIndex = 0;
            for (NavMeshModifier sourceObj : m_geom.getModifications()) {
                int[] triangles = new int[sourceObj.getGeomLength()];
                System.arraycopy(tris, fromIndex, triangles, 0, sourceObj.getGeomLength());
                listTris.add(triangles);
                fromIndex += sourceObj.getGeomLength();
            }

            List<int[]> areas = new ArrayList<>();

            for (NavMeshModifier sourceObj : m_geom.getModifications()) {
                int[] m_triareas = Recast.markWalkableTriangles(
                        telemetry,
                        cfg.walkableSlopeAngle,
                        verts,
                        listTris.get(m_geom.getModifications().indexOf(sourceObj)),
                        listTris.get(m_geom.getModifications().indexOf(sourceObj)).length / 3,
                        sourceObj.getAreaModification());

                areas.add(m_triareas);
            }

            //Prepare the new array for all areas.
            int[] m_triareasAll = new int[ntris];
            int length = 0;
            //Copy all flagged areas into new array.
            for (int[] area : areas) {
                System.arraycopy(area, 0, m_triareasAll, length, area.length);
                length += area.length;
            }
            // ** END NEW CUSTOM CODE **

            RecastRasterization.rasterizeTriangles(telemetry, verts, tris, m_triareasAll, ntris, solid, cfg.walkableClimb);
        }

        // Step 3. Filter walkables surfaces.
        RecastFilter.filterLowHangingWalkableObstacles(telemetry, cfg.walkableClimb, solid);
        RecastFilter.filterLedgeSpans(telemetry, cfg.walkableHeight, cfg.walkableClimb, solid);
        RecastFilter.filterWalkableLowHeightSpans(telemetry, cfg.walkableHeight, solid);

        // Step 4. Partition walkable surface to simple regions.
        CompactHeightfield chf = Recast.buildCompactHeightfield(telemetry, cfg.walkableHeight, cfg.walkableClimb, solid);

        RecastArea.erodeWalkableArea(telemetry, cfg.walkableRadius, chf);

        // (Optional) Mark areas.
//      List<ConvexVolume> vols = geom.getConvexVolumes(); 
//      for (ConvexVolume convexVolume: vols) { 
//          RecastArea.markConvexPolyArea(telemetry, convexVolume.verts, convexVolume.hmin, convexVolume.hmax, convexVolume.areaMod, chf);
//      }
        
        if (partitionType == PartitionType.WATERSHED) {
            // Prepare for region partitioning, by calculating distance field
            // along the walkable surface.
            RecastRegion.buildDistanceField(telemetry, chf);
            // Partition the walkable surface into simple regions without holes.
            RecastRegion.buildRegions(telemetry, chf, 0, cfg.minRegionArea, cfg.mergeRegionArea);
        } else if (partitionType == PartitionType.MONOTONE) {
            // Partition the walkable surface into simple regions without holes.
            // Monotone partitioning does not need distancefield.
            RecastRegion.buildRegionsMonotone(telemetry, chf, 0, cfg.minRegionArea, cfg.mergeRegionArea);
        } else {
            // Partition the walkable surface into simple regions without holes.
            RecastRegion.buildLayerRegions(telemetry, chf, 0, cfg.minRegionArea);
        }

        // Step 5. Trace and simplify region contours.
        ContourSet contour = RecastContour.buildContours(telemetry, chf, cfg.maxSimplificationError, cfg.maxEdgeLen, RecastConstants.RC_CONTOUR_TESS_WALL_EDGES);

        // Step 6. Build polygons mesh from contours.
        PolyMesh polyMesh = RecastMesh.buildPolyMesh(telemetry, contour, cfg.maxVertsPerPoly);

        // Step 7. Create detail mesh which allows to access approximate height on each polygon.
        PolyMeshDetail detailMesh = RecastMeshDetail.buildPolyMeshDetail(telemetry, polyMesh, chf, cfg.detailSampleDist, cfg.detailSampleMaxError);

        RecastBuilderResult rcResult = new RecastBuilder().new RecastBuilderResult(solid, chf, contour, polyMesh, detailMesh);

        NavMeshDataCreateParams params = getNavMeshCreateParams(m_geom, cellSize, cellHeight, agentHeight, agentRadius, agentMaxClimb, rcResult);

        updateAreaAndFlags(params);

        MeshData meshData = NavMeshBuilder.createNavMeshData(params);
        navMesh = new NavMesh(meshData, cfg.maxVertsPerPoly, 0);
        navQuery = new NavMeshQuery(navMesh);

        System.out.println("Telemetry:");
        telemetry.print();

        //Create offmesh connections here.
        
        try {
            String filename = "recast4j-solo";
            NavMeshAssetManager.save(meshData, makeFile(filename + ".md"));
            NavMeshAssetManager.save(navMesh, makeFile(filename + ".nm"));
            NavMeshAssetManager.saveAsObj(navMesh, makeFile(filename + ".obj"));
            NavMeshAssetManager.saveAsObj(detailMesh, makeFile(filename + "_" + cfg.partitionType + "_detail.obj"));
            NavMeshAssetManager.saveAsObj(polyMesh, makeFile(filename + "_" + cfg.partitionType + ".obj"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //Show wireframe. Helps with param tweaks. false = solid color.
        navMeshRenderer.drawMeshByArea(meshData, true);
    }
    
    /**
     * This example sets area type and flags based off geometry of each
     * individual mesh and uses the custom JmeRecastBuilder class. Implements
     * offmesh connections. Uses recast4j methods for building.
     */
    private void buildTiledRecast4j() {

        //Build merged mesh.
        JmeInputGeomProvider m_geom = InputGeomProviderBuilder.build(worldMap);
        setNavMeshModifiers(m_geom, worldMap);

        setOffMeshConnections();

        //Clean up offMesh connections.
        offMeshCon.detachAllChildren();

        //Step 2. Create a Recast configuration object.
        RecastConfig cfg = new RecastConfigBuilder()
                .withPartitionType(RecastConstants.PartitionType.WATERSHED)
                .withWalkableAreaMod(AREAMOD_GROUND)
                .withAgentRadius(agentRadius)
                .withAgentHeight(agentHeight)
                .withCellSize(cellSize)
                .withCellHeight(cellHeight)
                .withAgentMaxClimb(agentMaxClimb)
                .withAgentMaxSlope(45f)
                .withEdgeMaxLen(3.2f) // r*8
                .withEdgeMaxError(1.3f) // 1.1 - 1.5
                .withDetailSampleDistance(6.0f) // increase if exception
                .withDetailSampleMaxError(6.0f) // increase if exception
                .withVertsPerPoly(3)
                .withTileSize(16)
                .build();

        // Build all tiles
        JmeRecastBuilder rcBuilder = new JmeRecastBuilder(new JmeRecastBuilderProgressListener());
        int threads = 1;
        RecastBuilderResult[][] rcResult = rcBuilder.buildTiles(m_geom, cfg, threads);
        // Add tiles to nav mesh
        int tw = rcResult.length;
        int th = rcResult[0].length;

        // Create empty nav mesh
        NavMeshParams navMeshParams = new NavMeshParams();
        RecastVectors.copy(navMeshParams.orig, m_geom.getMeshBoundsMin());
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

                NavMeshDataCreateParams params = getNavMeshCreateParams(m_geom,
                        cellSize, cellHeight, agentHeight, agentRadius, agentMaxClimb, rcResult[x][y]);

                params.tileX = x;
                params.tileY = y;

                updateAreaAndFlags(params);

                MeshData meshData = NavMeshBuilder.createNavMeshData(params);
                navMesh.addTile(meshData, 0, 0);
            }
        }

        navQuery = new NavMeshQuery(navMesh);

        //process off-mesh-connections
        processOffMeshConnections();

        try {
            File f = makeFile("recast4j-tiled.nm");

            // Native format using tiles.
            MeshSetWriter msw = new MeshSetWriter();
            msw.write(new FileOutputStream(f), navMesh, ByteOrder.BIG_ENDIAN, false);

            // Read in saved NavMesh.
            MeshSetReader msr = new MeshSetReader();
            navMesh = msr.read(new FileInputStream(f), cfg.maxVertsPerPoly);

            navQuery = new NavMeshQuery(navMesh);
            navMeshRenderer.drawNavMeshByArea(navMesh, true);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void buildTileCache() {

        //Build merged mesh.
        JmeInputGeomProvider m_geom = InputGeomProviderBuilder.build(worldMap);
        setNavMeshModifiers(m_geom, worldMap);

        setOffMeshConnections();

        //Clean up offMesh connections.
        offMeshCon.detachAllChildren();

        //Step 2. Create a Recast configuration object.
        RecastConfig cfg = new RecastConfigBuilder()
                .withPartitionType(RecastConstants.PartitionType.MONOTONE)
                .withWalkableAreaMod(AREAMOD_GROUND)
                .withAgentRadius(agentRadius)
                .withAgentHeight(agentHeight)
                .withCellSize(cellSize)
                .withCellHeight(cellHeight)
                .withAgentMaxClimb(agentMaxClimb)
                .withAgentMaxSlope(45f)
                .withEdgeMaxLen(3.2f) // r*8
                .withEdgeMaxError(1.3f) // 1.1 - 1.5
                .withDetailSampleDistance(6.0f) // increase if exception
                .withDetailSampleMaxError(6.0f) // increase if exception
                .withVertsPerPoly(3)
                .withTileSize(16)
                .build();

        boolean cCompatibility = false;

        /**
         * Layers represent heights for the tile cache. For example, a bridge
         * with an underpass would have a layer for travel under the bridge and
         * another for traveling over the bridge.
         */
        JmeTileLayersBuilder layerBuilder = new JmeTileLayersBuilder(m_geom, cfg);
        List<byte[]> layers = layerBuilder.build(ByteOrder.BIG_ENDIAN, cCompatibility, 1);

        //Build the tile cache which also builds the navMesh.
        TileCache tc = getTileCache(m_geom, cfg, ByteOrder.BIG_ENDIAN, cCompatibility);

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
                ex.printStackTrace();
            }
        }

        try {
            //Save and read back for testing.
            File f = makeFile("recast4j-tile-cache.tc");

            //Write our tile cache.
            TileCacheWriter writer = new TileCacheWriter();
            writer.write(new FileOutputStream(f), tc, ByteOrder.BIG_ENDIAN, cCompatibility);

            //Read our tile cache.
            TileCacheReader reader = new TileCacheReader();
            tc = reader.read(new FileInputStream(f), cfg.maxVertsPerPoly, new JmeTileCacheMeshProcess());

            //Get the navMesh and build a query object.
            navMesh = tc.getNavMesh();
            navQuery = new NavMeshQuery(navMesh);

            //process off-mesh-connections
            processOffMeshConnections();

            //Tile data can be null since maxTiles is not an exact science.
            int maxTiles = tc.getTileCount();

            for (int i = 0; i < maxTiles; i++) {
                MeshTile tile = tc.getNavMesh().getTile(i);
                MeshData meshData = tile.data;
                if (meshData != null) {
                    navMeshRenderer.drawMeshByArea(meshData, true);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private File makeFile(String fileName) {
        String dirName = "navmesh-test";
        File file = Path.of(dirName, fileName).toFile();
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        logger.info("Writing file={}", file.getAbsolutePath());
        return file;
    }

    private NavMeshDataCreateParams getNavMeshCreateParams(JmeInputGeomProvider m_geom,
            float m_cellSize, float m_cellHeight, float m_agentHeight, float m_agentRadius, float m_agentMaxClimb,
            RecastBuilderResult rcResult) {

        PolyMesh m_pmesh = rcResult.getMesh();
        PolyMeshDetail m_dmesh = rcResult.getMeshDetail();
        NavMeshDataCreateParams params = new NavMeshDataCreateParams();

        for (int i = 0; i < m_pmesh.npolys; ++i) {
            m_pmesh.flags[i] = 1;
        }

        params.verts = m_pmesh.verts;
        params.vertCount = m_pmesh.nverts;
        params.polys = m_pmesh.polys;
        params.polyAreas = m_pmesh.areas;
        params.polyFlags = m_pmesh.flags;
        params.polyCount = m_pmesh.npolys;
        params.nvp = m_pmesh.nvp;

        if (m_dmesh != null) {
            params.detailMeshes = m_dmesh.meshes;
            params.detailVerts = m_dmesh.verts;
            params.detailVertsCount = m_dmesh.nverts;
            params.detailTris = m_dmesh.tris;
            params.detailTriCount = m_dmesh.ntris;
        }

        params.walkableHeight = m_agentHeight;
        params.walkableRadius = m_agentRadius;
        params.walkableClimb = m_agentMaxClimb;
        params.bmin = m_pmesh.bmin;
        params.bmax = m_pmesh.bmax;
        params.cs = m_cellSize;
        params.ch = m_cellHeight;
        params.buildBvTree = true;

        params.offMeshConCount  = m_geom.getOffMeshConnections().size();
        params.offMeshConVerts  = new float[params.offMeshConCount * 6];
        params.offMeshConRad    = new float[params.offMeshConCount];
        params.offMeshConDir    = new int[params.offMeshConCount];
        params.offMeshConAreas  = new int[params.offMeshConCount];
        params.offMeshConFlags  = new int[params.offMeshConCount];
        params.offMeshConUserID = new int[params.offMeshConCount];

        for (int i = 0; i < params.offMeshConCount; i++) {
            OffMeshLink offMeshConn = m_geom.getOffMeshConnections().get(i);
            for (int j = 0; j < 6; j++) {
                params.offMeshConVerts[6 * i + j] = offMeshConn.verts[j];
            }
            params.offMeshConRad[i]     = offMeshConn.radius;
            params.offMeshConDir[i]     = offMeshConn.biDirectional ? NavMesh.DT_OFFMESH_CON_BIDIR : 0;
            params.offMeshConAreas[i]   = offMeshConn.area;
            params.offMeshConFlags[i]   = offMeshConn.flags;
            params.offMeshConUserID[i]  = offMeshConn.userID;
        }

        //System.out.println(ReflectionToStringBuilder.toString(params, ToStringStyle.MULTI_LINE_STYLE));
        return params;
    }

    private void processOffMeshConnections() {
        /**
         * Process OffMeshConnections. Since we are reading this in we do it
         * here. If we were just running with the tile cache we first created we
         * would just place this after building the tiles. Basic flow: Check
         * each mapOffMeshConnection for an index > 0. findNearestPoly() for the
         * start/end positions of the link. getTileAndPolyByRef() using the
         * returned poly reference. If both start and end are good values, set
         * the connection properties.
         */
        for (Map.Entry<String, OffMeshConnection> offMeshConn : mapOffMeshCon.entrySet()) {

            /**
             * If the OffMeshConnection id is 0, there is no paired bone for the
             * link so skip.
             */
            if (offMeshConn.getValue().userId > 0) {
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
                System.arraycopy(offMeshConn.getValue().pos, 0, startPos, 0, 3);
                //Get the end position for the link.
                float[] endPos = new float[3];
                System.arraycopy(offMeshConn.getValue().pos, 3, endPos, 0, 3);

                float[] extents = new float[]{agentRadius, agentRadius, agentRadius};

                //Find the nearest polys to start/end.
                Result<FindNearestPolyResult> startPoly = navQuery.findNearestPoly(startPos, extents, filter);
                Result<FindNearestPolyResult> endPoly = navQuery.findNearestPoly(endPos, extents, filter);

                /**
                 * Note: not isFailure() here, because isSuccess guarantees us,
                 * that the result isn't "RUNNING", which it could be if we only
                 * check it's not failure.
                 */
                if (!startPoly.succeeded() || !endPoly.succeeded()
                        || startPoly.result.getNearestRef() == 0 || endPoly.result.getNearestRef() == 0) {

                    logger.error("offmeshCon findNearestPoly unsuccessful or getNearestRef is not > 0.");
                    logger.error("Link [{}] pos {} id [{}]", offMeshConn.getKey(), Arrays.toString(offMeshConn.getValue().pos), offMeshConn.getValue().userId);
                    logger.error("findNearestPoly startPoly [{}] getNearestRef [{}]", startPoly.succeeded(), startPoly.result.getNearestRef());
                    logger.error("findNearestPoly endPoly [{}] getNearestRef [{}].", endPoly.succeeded(), endPoly.result.getNearestRef());

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
                         * endpoints. Jme requires 3 vertices per poly to build
                         * a debug mesh so we have to create a 3-vertex polygon
                         * here if using debug. The extra vertex position will
                         * be connected automatically when we add the tile back
                         * to the navmesh. For games, this would be a two vert
                         * poly.
                         *
                         * See:
                         * https://github.com/ppiastucki/recast4j/blob/3c532068d79fe0306fedf035e50216008c306cdf/detour/src/main/java/org/recast4j/detour/NavMesh.java#L406
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
                        offMeshConn.getValue().poly = poly;

                        //Shorten names and make readable. Could just call directly.
                        float[] start = startPoly.result.getNearestPos();
                        float[] end = endPoly.result.getNearestPos();

                        //Set the links position array values to nearest.
                        offMeshConn.getValue().pos = new float[]{
                            start[0], start[1], start[2], end[0], end[1], end[2]
                        };

                        //Determine what side of the tile the vertex is on.
                        offMeshConn.getValue().side = startTile == endTile ? 0xFF
                                : NavMeshBuilder.classifyOffMeshPoint(new VectorPtr(offMeshConn.getValue().pos, 3),
                                        startTile.header.bmin, startTile.header.bmax);

                        //Create new OffMeshConnection array.
                        if (startTile.offMeshCons == null) {
                            startTile.offMeshCons = new OffMeshConnection[1];
                        } else {
                            startTile.offMeshCons = Arrays.copyOf(startTile.offMeshCons, startTile.offMeshCons.length + 1);
                        }

                        //Add this connection.
                        startTile.offMeshCons[startTile.offMeshCons.length - 1] = offMeshConn.getValue();
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
    }

    private void setOffMeshConnections() {
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

                    SkeletonControl skControl = GameObject.getComponentInChildren(node, SkeletonControl.class);

                    if (skControl != null) {
                        /**
                         * Offmesh connections require two connections, a
                         * start/end vector3f and must connect to a surrounding
                         * tile. To complete a connection, start and end must be
                         * the same for each. You can supply the Vector3f
                         * manually or for example, use bones from an armature.
                         * When using bones, they should be paired and use a
                         * naming convention.
                         *
                         * In our case, we used bones and this naming
                         * convention:
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
                         * Bone 1 naming: offmesh.anything.a Bone 2 naming:
                         * offmesh.anything.b
                         *
                         * arg[0]: offmesh = same value all bones arg[1]:
                         * anything = same value paired bones arg[2]: a or b =
                         * one paired bone
                         *
                         * The value of arg[0] applies to ALL bones and dictates
                         * these are link bones.
                         *
                         * The value of arg[1] dictates these pair of bones
                         * belong together.
                         *
                         * The value of arg[2] distinguishes the paired bones
                         * from each other.
                         *
                         * Examples:
                         *
                         * offmesh.pond.a offmesh.pond.b offmesh.1.a offmesh.1.b
                         */
                        Bone[] roots = skControl.getSkeleton().getRoots();
                        for (Bone bone : roots) {
                            /**
                             * Split the name up using delimiter.
                             */
                            String[] arg = bone.getName().split("\\.");

                            if (arg[0].equals("offmesh")) {

                                //New connection.
                                OffMeshConnection link1 = new OffMeshConnection();

                                /**
                                 * The bones worldTranslation will be the start
                                 * or end Vector3f of each OffMeshConnection
                                 * object.
                                 */
                                float[] linkPos = DetourUtils.toFloatArray(node.localToWorld(bone.getModelSpacePosition(), null));

                                /**
                                 * Prepare new position array. The endpoints of
                                 * the connection.
                                 *
                                 * startPos endPos [ax, ay, az, bx, by, bz]
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
                                link1.rad = agentRadius;

                                /**
                                 * Move through link1 both directions. Only
                                 * works if both links have identical in
                                 * start/end. Set to 0 for one direction link.
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
                                 * map, set start or end determined by link1
                                 * arg[2].
                                 */
                                if (mapOffMeshCon.containsKey(link2)) {
                                    /**
                                     * Copy link1 pos to link2 pos. If link1 is
                                     * start(a) of link, copy link1 start to
                                     * link2 start. If link1 is the end(b) of
                                     * link, copy link1 end to link2 end.
                                     */
                                    System.arraycopy(link1.pos, arg[2].equals("a") ? 0 : 3,
                                            mapOffMeshCon.get(link2).pos, arg[2].equals("a") ? 0 : 3, 3);

                                    /**
                                     * Copy link2 pos to link1 pos. If link1 is
                                     * the start(a) of link, copy link2 end to
                                     * link1 end. If link1 is end(b) of link,
                                     * copy link2 start to link1 start.
                                     */
                                    System.arraycopy(mapOffMeshCon.get(link2).pos, arg[2].equals("a") ? 3 : 0,
                                            link1.pos, arg[2].equals("a") ? 3 : 0, 3);

                                    /**
                                     * OffMeshconnections with id of 0 don't get
                                     * processed later if not set here.
                                     */
                                    if (arg[2].equals("a")) {
                                        link1.userId = ++id;
                                        logger.info("OffMeshConnection [{}] id  [{}]", bone.getName(), link1.userId);
                                        logger.info("OffMeshConnection [{}] pos {}", bone.getName(), link1.pos);

                                        mapOffMeshCon.get(link2).userId = ++id;
                                        logger.info("OffMeshConnection [{}] id  [{}]", link2, mapOffMeshCon.get(link2).userId);
                                        logger.info("OffMeshConnection [{}] pos {}", link2, mapOffMeshCon.get(link2).pos);

                                    } else {
                                        mapOffMeshCon.get(link2).userId = ++id;
                                        logger.info("OffMeshConnection [{}] id  [{}]", link2, mapOffMeshCon.get(link2).userId);
                                        logger.info("OffMeshConnection [{}] pos {}", link2, mapOffMeshCon.get(link2).pos);

                                        link1.userId = ++id;
                                        logger.info("OffMeshConnection [{}] id  [{}]", bone.getName(), link1.userId);
                                        logger.info("OffMeshConnection [{}] pos {}", bone.getName(), link1.pos);
                                    }
                                }
                                //Add this bone to map.
                                mapOffMeshCon.put(bone.getName(), link1);
                            }
                        }
                    }
                }
            }
        });
    }

    //Build the tile cache.
    private TileCache getTileCache(JmeInputGeomProvider geom, RecastConfig cfg, ByteOrder order, boolean cCompatibility) {

        //This value specifies how many layers (or "floors") each navmesh tile is expected to have.
        final int EXPECTED_LAYERS_PER_TILE = 4;

        TileCacheParams params = new TileCacheParams();
        int[] twh = Recast.calcTileCount(geom.getMeshBoundsMin(), geom.getMeshBoundsMax(), cfg.cs, cfg.tileSize);
        params.ch = cellSize;
        params.cs = cellHeight;
        DetourCommon.vCopy(params.orig, geom.getMeshBoundsMin());
        params.height = cfg.tileSize;
        params.width = cfg.tileSize;
        params.walkableHeight = agentHeight;
        params.walkableRadius = agentRadius;
        params.walkableClimb = agentMaxClimb;
        params.maxSimplificationError = cfg.maxSimplificationError;
        params.maxTiles = twh[0] * twh[1] * EXPECTED_LAYERS_PER_TILE;
        params.maxObstacles = 128;

        NavMeshParams navMeshParams = new NavMeshParams();
        RecastVectors.copy(navMeshParams.orig, geom.getMeshBoundsMin());
        navMeshParams.tileWidth = cfg.tileSize * cfg.cs;
        navMeshParams.tileHeight = cfg.tileSize * cfg.cs;
        navMeshParams.maxTiles = params.maxTiles;
        navMeshParams.maxPolys = 16384;

        NavMesh navMesh = new NavMesh(navMeshParams, cfg.maxVertsPerPoly);

        TileCache tc = new TileCache(params, new TileCacheStorageParams(order, cCompatibility),
                navMesh, TileCacheCompressorFactory.get(cCompatibility), new JmeTileCacheMeshProcess());

        return tc;
    }

    /**
     * This is a mandatory class otherwise the tile cache build will not set the
     * areas. This gets call from the tc.buildNavMeshTile(ref) method.
     */
    private class JmeTileCacheMeshProcess implements TileCacheMeshProcess {

        @Override
        public void process(NavMeshDataCreateParams params) {
            updateAreaAndFlags(params);
        }
    }

    private void setNavMeshModifiers(JmeInputGeomProvider m_geom, Node root) {

        root.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Geometry geo) {

                String[] name = geo.getMaterial().getName().split("_");
                NavMeshModifier mod = null;

                switch (name[0]) {
                    case "water":
                        mod = new NavMeshModifier(geo, AREAMOD_WATER);
                        break;
                    case "road":
                        mod = new NavMeshModifier(geo, AREAMOD_ROAD);
                        break;
                    case "grass":
                        mod = new NavMeshModifier(geo, AREAMOD_GRASS);
                        break;
                    case "door":
                        mod = new NavMeshModifier(geo, AREAMOD_DOOR);
                        break;
                    default:
                        mod = new NavMeshModifier(geo, AREAMOD_GROUND);
                }

                m_geom.addModification(mod);
                System.out.println("setNavMeshArea " + mod);
            }
        });
    }

    private void updateAreaAndFlags(NavMeshDataCreateParams params) {
        final int DT_TILECACHE_WALKABLE_AREA = 63;

        for (int i = 0; i < params.polyCount; ++i) {

            if (params.polyAreas[i] == DT_TILECACHE_WALKABLE_AREA) {
                params.polyAreas[i] = POLYAREA_TYPE_GROUND;
            }

            if (params.polyAreas[i] == POLYAREA_TYPE_GROUND
                    || params.polyAreas[i] == POLYAREA_TYPE_GRASS
                    || params.polyAreas[i] == POLYAREA_TYPE_ROAD) {
                params.polyFlags[i] = POLYFLAGS_WALK;

            } else if (params.polyAreas[i] == POLYAREA_TYPE_WATER) {
                params.polyFlags[i] = POLYFLAGS_SWIM;

            } else if (params.polyAreas[i] == POLYAREA_TYPE_DOOR) {
                params.polyFlags[i] = POLYFLAGS_WALK | POLYFLAGS_DOOR;
            }
        }
    }

    /**
     * Prints any polygons found flags to the log.
     *
     * @param poly The polygon id to look for flags.
     */
    private void printFlags(long poly) {

        int flags = navMesh.getPolyFlags(poly).result;

        if (flags == 0) {
            logger.info("No flag found.");
        }
        if (isBitSet(POLYFLAGS_DOOR, flags)) {
            logger.info("POLYFLAGS_DOOR [{}]", POLYFLAGS_DOOR);
        }
        if (isBitSet(POLYFLAGS_WALK, flags)) {
            logger.info("POLYFLAGS_WALK [{}]", POLYFLAGS_WALK);
        }
        if (isBitSet(POLYFLAGS_SWIM, flags)) {
            logger.info("POLYFLAGS_SWIM [{}]", POLYFLAGS_SWIM);
        }
        if (isBitSet(POLYFLAGS_JUMP, flags)) {
            logger.info("POLYFLAGS_JUMP [{}]", POLYFLAGS_JUMP);
        }
        if (isBitSet(POLYFLAGS_DISABLED, flags)) {
            logger.info("POLYFLAGS_DISABLED [{}]", POLYFLAGS_DISABLED);
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
     * Class to hold the object id and flags for the MouseEventControl check that
     * assures all flags are the same.
     */
    private class PolyAndFlag {

        public final long poly;
        public final int flag;

        public PolyAndFlag(long poly, int flag) {
            this.poly = poly;
            this.flag = flag;
        }

    }

}
