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

import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_DOOR;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_GRASS;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_GROUND;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_ROAD;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_WATER;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYFLAGS_DOOR;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYFLAGS_SWIM;
import static com.jme3.recast4j.demo.JmeAreaMods.POLYFLAGS_WALK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.detour.NavMeshParams;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.ObstacleAvoidanceQuery.ObstacleAvoidanceParams;
import org.recast4j.detour.io.MeshDataReader;
import org.recast4j.detour.io.MeshDataWriter;
import org.recast4j.detour.io.MeshSetReader;
import org.recast4j.detour.io.MeshSetWriter;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastConfig;
import org.recast4j.recast.RecastVectors;
import org.recast4j.recast.geom.InputGeomProvider;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.Crowd.Crowd;
import com.jme3.recast4j.Detour.Crowd.MovementApplicationType;
import com.jme3.recast4j.Detour.Crowd.Impl.CrowdManagerAppState;
import com.jme3.recast4j.Recast.GeometryProviderBuilder;
import com.jme3.recast4j.Recast.NavMeshDataCreateParamsBuilder;
import com.jme3.recast4j.Recast.RecastBuilderConfigBuilder;
import com.jme3.recast4j.Recast.RecastConfigBuilder;
import com.jme3.recast4j.Recast.Utils.RecastUtils;
import com.jme3.recast4j.demo.controls.CrowdDebugControl;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Torus;

/**
 * A procedural example of creating a crowd. When running this state and the gui
 * at the same time, they do not interfere with each other. Used for tutorial 
 * code examples only.
 * 
 * @author Robert
 */
public class CrowdState extends BaseAppState {

    private AssetManager assetManager;
    private Node debugNode = new Node("Debug Node");
    private NavMeshQuery query;
    private Crowd crowd;
    
    @Override
    protected void initialize(Application app) {   
    	this.assetManager = app.getAssetManager();
    	getRootNode().attachChild(debugNode);
        buildSolo();
//        buildTiled();
//        buildCrowd();
    }
    
    private Node getRootNode() {
    	return ((SimpleApplication) getApplication()).getRootNode();
    }
    
    private void buildSolo() {
        Geometry boxGeo = createFloor();
        
        //Step 1. Gather our geometry.
        InputGeomProvider geomProvider = new GeometryProviderBuilder(boxGeo).build();
        
        //Step 2. Create a Recast configuration object.
        RecastConfigBuilder builder = new RecastConfigBuilder();
        
        //Instantiate the configuration parameters.
        RecastConfig cfg = builder
                .withAgentRadius(0.4f)              // r
                .withAgentHeight(2.0f)              // h
                //cs and ch should be .1 at min.
                .withCellSize(0.2f)                 // cs=r/2
                .withCellHeight(0.1f)               // ch=cs/2 but not < .1f
                .withAgentMaxClimb(0.3f)            // > 2*ch
                .withAgentMaxSlope(45f)
                .withEdgeMaxLen(3.2f)               // r*8
                .withEdgeMaxError(1.3f)             // 1.1 - 1.5
                .withDetailSampleDistance(6.0f)     // increase if exception
                .withDetailSampleMaxError(5.0f)     // increase if exception
                .withVertsPerPoly(3).build();       
        
        //Create a RecastBuilderConfig builder with world bounds of our geometry.
        RecastBuilderConfigBuilder rcb = new RecastBuilderConfigBuilder(boxGeo);
        
        //Build the configuration object using our cfg. 
        RecastBuilderConfig builderCfg = rcb.withDetailMesh(true).build(cfg);
        
        //Step 3. Build our Navmesh data using our gathered geometry and configuration.
        RecastBuilder rcBuilder = new RecastBuilder();
        RecastBuilderResult rcResult = rcBuilder.build(geomProvider, builderCfg);
        
        //Set the parameters needed to build our MeshData using the RecastBuilder results.
        NavMeshDataCreateParamsBuilder paramBuilder = new NavMeshDataCreateParamsBuilder(rcResult);
        
        //Update poly flags from areas. Set any flags here.
        PolyMesh pmesh = rcResult.getMesh();
        
        for (int i = 0; i < pmesh.npolys; ++i) {
            if (pmesh.areas[i] == POLYAREA_TYPE_GROUND
              || pmesh.areas[i] == POLYAREA_TYPE_GRASS
              || pmesh.areas[i] == POLYAREA_TYPE_ROAD) {
                paramBuilder.withPolyFlag(i, POLYFLAGS_WALK);
            } else if (pmesh.areas[i] == POLYAREA_TYPE_WATER) {
                paramBuilder.withPolyFlag(i, POLYFLAGS_SWIM);
            } else if (pmesh.areas[i] == POLYAREA_TYPE_DOOR) {
                paramBuilder.withPolyFlags(i, POLYFLAGS_WALK | POLYFLAGS_DOOR);
            }
        }
        //Build the parameter object. 
        NavMeshDataCreateParams params = paramBuilder.build(builderCfg);
        //Step 4. Generate MeshData using our parameters object.
        MeshData meshData = NavMeshBuilder.createNavMeshData(params);
        //Step 5. Build the NavMesh.
        NavMesh navMesh = new NavMesh(meshData, builderCfg.cfg.maxVertsPerPoly, 0);
        
        try {
            //Step 6. Save our work. Using compressed format.
            MeshDataWriter mdw = new MeshDataWriter();
            mdw.write(new FileOutputStream(new File("myMeshData.md")),  meshData, ByteOrder.BIG_ENDIAN, false);
            
            //Or the native format using tiles.
            MeshSetWriter msw = new MeshSetWriter();
            msw.write(new FileOutputStream(new File("myNavMesh.nm")), navMesh, ByteOrder.BIG_ENDIAN, false);
            
        }  catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void buildTiled() {
        //We need to know these variables for the tile NavMeshDataCreateParams
        //object. 
        float agentHeight = 2.0f;
        float agentRadius = 0.4f;
        float agentMaxClimb = 0.3f;
                
        Geometry boxGeo = createFloor();
        
        //Step 1. Gather our geometry.
        InputGeomProvider geomProvider = new GeometryProviderBuilder(boxGeo).build();
        
        //Step 2. Create a Recast configuration object.
        RecastConfigBuilder builder = new RecastConfigBuilder();
        
        //Instantiate the configuration parameters.
        RecastConfig cfg = builder
                .withAgentRadius(agentRadius)       // r
                .withAgentHeight(agentHeight)       // h
                //cs and ch should be .1 at min.
                .withCellSize(0.2f)                 // cs=r/2
                .withCellHeight(0.1f)               // ch=cs/2 but not < .1f
                .withAgentMaxClimb(agentMaxClimb)   // > 2*ch
                .withAgentMaxSlope(45f)
                .withEdgeMaxLen(3.2f)               // r*8
                .withEdgeMaxError(1.3f)             // 1.1 - 1.5
                .withDetailSampleDistance(6.0f)     // increase if exception
                .withDetailSampleMaxError(5.0f)     // increase if exception
                .withVertsPerPoly(3)
                .withTileSize(32).build();          // set tile size

        //Build all tiles
        RecastBuilder rcBuilder = new RecastBuilder();
        RecastBuilderResult[][] rcResult = rcBuilder.buildTiles(geomProvider, cfg, 1);
        
        //Set the parameters needed to build our MeshData using the RecastBuilder results.
        int tw = rcResult.length;
        int th = rcResult[0].length;
        
        //Create empty nav mesh.
        NavMeshParams navMeshParams = new NavMeshParams();
        RecastVectors.copy(navMeshParams.orig, geomProvider.getMeshBoundsMin());
        navMeshParams.tileWidth = cfg.tileSize * cfg.cs;
        navMeshParams.tileHeight = cfg.tileSize * cfg.cs;
        navMeshParams.maxTiles = tw * th;
        navMeshParams.maxPolys = 32768;
        
        NavMesh navMesh = new NavMesh(navMeshParams, cfg.maxVertsPerPoly);
        
        //Add tiles to nav mesh
        for (int y = 0; y < th; y++) {
            for (int x = 0; x < tw; x++) {
                PolyMesh pmesh = rcResult[x][y].getMesh();
                if (pmesh.npolys == 0) {
                    continue;
                }

                // Update poly flags from areas.
                for (int i = 0; i < pmesh.npolys; ++i) {
                    if (pmesh.areas[i] == POLYAREA_TYPE_GROUND ||
                        pmesh.areas[i] == POLYAREA_TYPE_GRASS ||
                        pmesh.areas[i] == POLYAREA_TYPE_ROAD) {
                        pmesh.flags[i] = POLYFLAGS_WALK;
                    } else if (pmesh.areas[i] == POLYAREA_TYPE_WATER) {
                        pmesh.flags[i] = POLYFLAGS_SWIM;
                    } else if (pmesh.areas[i] == POLYAREA_TYPE_DOOR) {
                        pmesh.flags[i] = POLYFLAGS_WALK | POLYFLAGS_DOOR;
                    }
                    if (pmesh.areas[i] > 0) {
                        pmesh.areas[i]--;
                    }
                }
                // Create empty parameters object to set params.
                NavMeshDataCreateParams params = new NavMeshDataCreateParams();
                params.verts = pmesh.verts;
                params.vertCount = pmesh.nverts;
                params.polys = pmesh.polys;
                params.polyAreas = pmesh.areas;
                params.polyFlags = pmesh.flags;
                params.polyCount = pmesh.npolys;
                params.nvp = pmesh.nvp;
                // Save detail mesh data.
                PolyMeshDetail dmesh = rcResult[x][y].getMeshDetail();
                params.detailMeshes = dmesh.meshes;
                params.detailVerts = dmesh.verts;
                params.detailVertsCount = dmesh.nverts;
                params.detailTris = dmesh.tris;
                params.detailTriCount = dmesh.ntris;
                params.walkableHeight = agentHeight;
                params.walkableRadius = agentRadius;
                params.walkableClimb = agentMaxClimb;
                params.bmin = pmesh.bmin;
                params.bmax = pmesh.bmax;
                params.cs = cfg.cs;
                params.ch = cfg.ch;
                params.tileX = x;
                params.tileY = y;
                params.buildBvTree = true;
                // add tile to navMesh.
                navMesh.addTile(NavMeshBuilder.createNavMeshData(params), 0, 0);
            }
        }
        
        try {
            //Native format using tiles.
            MeshSetWriter msw = new MeshSetWriter();
            msw.write(new FileOutputStream(new File("myNavMesh.nm")), navMesh, ByteOrder.BIG_ENDIAN, false);
            
            //Or read in saved NavMesh.
            MeshSetReader msr = new MeshSetReader();
            NavMesh navMeshFromSaved = msr.read(new FileInputStream("myNavMesh.nm"), 3);

            int maxTiles = navMeshFromSaved.getMaxTiles();
            for (int i = 0; i < maxTiles; i++) {
                MeshData meshdata = navMeshFromSaved.getTile(i).data;
                if (meshdata != null ) {
                    showDebugMeshes(meshdata, true);
                }
            }
            
        }  catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void buildCrowd() {
        try {
            //Read in saved MeshData and build new NavMesh.
            MeshDataReader mdr = new MeshDataReader();       
            MeshData savedMeshData = mdr.read(new FileInputStream("myMeshData.md"), 3);
            NavMesh navMeshFromData = new NavMesh(savedMeshData, 3, 0);
            showDebugMeshes(savedMeshData, true);
            
            //Or read in saved NavMesh.
            MeshSetReader msr = new MeshSetReader();
            NavMesh navMeshFromSaved = msr.read(new FileInputStream("myNavMesh.nm"), 3);
            
            //Create the query object for pathfinding in this Crowd. 
            query = new NavMeshQuery(navMeshFromSaved);
            //Start crowd.
            crowd = new Crowd(MovementApplicationType.DIRECT, 100, .3f, navMeshFromSaved);
            //Add to CrowdManager.
            getState(CrowdManagerAppState.class).getCrowdManager().addCrowd(crowd);
            
            //Add OAP.
            ObstacleAvoidanceParams oap = new ObstacleAvoidanceParams();
            oap.velBias = 0.5f;
            oap.adaptiveDivs = 5;
            oap.adaptiveRings = 2;
            oap.adaptiveDepth = 1;
            crowd.setObstacleAvoidanceParams(0, oap);
            oap = new ObstacleAvoidanceParams();
            oap.velBias = 0.5f;
            oap.adaptiveDivs = 5;
            oap.adaptiveRings = 2;
            oap.adaptiveDepth = 2;
            crowd.setObstacleAvoidanceParams(1, oap);
            oap = new ObstacleAvoidanceParams();
            oap.velBias = 0.5f;
            oap.adaptiveDivs = 7;
            oap.adaptiveRings = 2;
            oap.adaptiveDepth = 3;
            crowd.setObstacleAvoidanceParams(2, oap);
            oap = new ObstacleAvoidanceParams();
            oap.velBias = 0.5f;
            oap.adaptiveDivs = 7;
            oap.adaptiveRings = 3;
            oap.adaptiveDepth = 3;
            crowd.setObstacleAvoidanceParams(3, oap);
        } catch (IOException | InstantiationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {      
//        addAgent(new Vector3f(-5, 0, 0));
//        addAgent(new Vector3f(-4f, 0.0f, -1f));
//        addAgent(new Vector3f(-3, 0, 0));         
    }

    @Override
    protected void onDisable() {
    }
    
    @Override
    public void update(float tpf) {
    }
    
    /**
     * Set the target for the crowd.
     * 
     * @param target The target to set.
     */
    public void setTarget(Vector3f target) {
        /*
        //Get the query extent for this crowd.
        float[] ext = crowd.getQueryExtents();

        //Locate the nearest poly ref/pos.
        Result<FindNearestPolyResult> nearest = query.findNearestPoly(DetourUtils.toFloatArray(target), ext, new BetterDefaultQueryFilter());

        if (!nearest.status.isSuccess() || nearest.result.getNearestRef() == 0) {
            LOG.info("getNearestRef() can't be 0. ref [{}]", nearest.result.getNearestRef());
        } else {
            //Sets all agent targets at same time.
            crowd.requestMoveToTarget(DetourUtils.createVector3f(nearest.result.getNearestPos()), nearest.result.getNearestRef());
        }*/
        crowd.requestMoveToTarget(target);
    }
    
    private void addAgent(Vector3f location) {
        
        //Load the spatial that will represent the agent.
        Node agent = (Node) assetManager.loadModel("Models/Jaime/Jaime.j3o");
        //Set translation prior to adding controls.
        agent.setLocalTranslation(location);
        //If we have a physics Crowd we need a physics compatible control to apply
        //movement and direction to the spatial.
        //agent.addControl((new BetterCharacterControl(0.3f, 1.5f, 20f)));
        //getState(BulletAppState.class).getPhysicsSpace().add(agent);
        
        //Add agent to the scene.
        getRootNode().attachChild(agent);
        
        int updateFlags = CrowdAgentParams.DT_CROWD_OPTIMIZE_TOPO | CrowdAgentParams.DT_CROWD_OPTIMIZE_VIS;
        //Build the params object.
        CrowdAgentParams ap = new CrowdAgentParams();
        ap.radius                   = 0.03f;
        ap.height                   = 1.5f;
        ap.maxAcceleration          = 8.0f;
        ap.maxSpeed                 = 3.5f;
        ap.collisionQueryRange      = 12.0f;
        ap.pathOptimizationRange    = 30.0f;
        ap.separationWeight         = 2.0f;
        ap.updateFlags              = updateFlags;
        ap.obstacleAvoidanceType    = 0;
        
        //Were going to use a debug move control so setup geometry for later use.
        Torus halo = new Torus(16, 16, 0.1f, 0.3f);
        Geometry haloGeom = new Geometry("halo", halo);
        Material haloMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        haloGeom.setMaterial(haloMat);
        haloGeom.setLocalTranslation(0, ap.height + 0.5f, 0);
        Quaternion pitch90 = new Quaternion();
        pitch90.fromAngleAxis(FastMath.PI/2, new Vector3f(1,0,0));
        haloGeom.setLocalRotation(pitch90);

        //Add agent to the crowd.
        CrowdAgent createAgent = crowd.createAgent(agent.getWorldTranslation(), ap);
        //Set the spatial for the agent.
        crowd.setSpatialForAgent(createAgent, agent);        
        //Add the debug control and set its visual and verbose state.
        CrowdDebugControl dmc = new CrowdDebugControl(crowd, createAgent, haloGeom.clone());
        dmc.setVisual(true); 
        dmc.setVerbose(false);                    
        agent.addControl(dmc);
    }
    
    private Geometry createFloor() {
    	Box boxMesh = new Box(20f,.1f,20f); 
        Geometry boxGeo = new Geometry("Colored Box", boxMesh); 
        Material boxMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"); 
        boxMat.setBoolean("UseMaterialColors", true); 
        boxMat.setColor("Ambient", ColorRGBA.LightGray); 
        boxMat.setColor("Diffuse", ColorRGBA.LightGray); 
        boxGeo.setMaterial(boxMat); 
        getRootNode().attachChild(boxGeo);
        return boxGeo;
    }
    
    private void showDebugMeshes(MeshData meshData, boolean wireframe) {
        Geometry dgeom = new Geometry("DebugMeshDetailed", RecastUtils.getDebugMesh(meshData.detailMeshes, meshData.detailVerts, meshData.detailTris));
        Material matGreen = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matGreen.setColor("Color", ColorRGBA.Green);
        dgeom.setMaterial(matGreen);
        dgeom.move(0, 0.25f, 0);

        Geometry sgeom = new Geometry("DebugMeshSimple", RecastUtils.getDebugMesh(meshData));
        Material matRed = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matRed.setColor("Color", ColorRGBA.Red);
        matRed.getAdditionalRenderState().setWireframe(wireframe);
        sgeom.setMaterial(matRed);
        sgeom.move(0, 0.125f, 0);

        debugNode.attachChild(sgeom);
        debugNode.attachChild(dgeom);
        
        System.out.println("VertCount Regular Mesh: " + sgeom.getVertexCount());
        System.out.println("VertCount Detailed Mesh: " + dgeom.getVertexCount());
    }
    
}
