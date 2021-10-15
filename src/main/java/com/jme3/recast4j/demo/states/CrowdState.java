package com.jme3.recast4j.demo.states;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.io.MeshSetReader;
import org.recast4j.detour.io.MeshSetWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.Crowd.CrowdConfig;
import com.jme3.recast4j.Detour.Crowd.CrowdManagerAppState;
import com.jme3.recast4j.Detour.Crowd.JmeCrowd;
import com.jme3.recast4j.Detour.Crowd.MovementType;
import com.jme3.recast4j.Detour.Crowd.ObstacleAvoidanceType;
import com.jme3.recast4j.Detour.Crowd.SimpleCrowd;
import com.jme3.recast4j.demo.controls.Animator;
import com.jme3.recast4j.demo.controls.CrowdControl;
import com.jme3.recast4j.demo.controls.CrowdDebugControl;
import com.jme3.recast4j.demo.utils.Circle;
import com.jme3.recast4j.demo.utils.MainCamera;
import com.jme3.recast4j.editor.NavMeshBuildSettings;
import com.jme3.recast4j.editor.SampleAreaModifications;
import com.jme3.recast4j.editor.builder.TileNavMeshBuilder;
import com.jme3.recast4j.geom.JmeGeomProviderBuilder;
import com.jme3.recast4j.geom.JmeInputGeomProvider;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * 
 * @author capdevon
 */
public class CrowdState extends AbstractNavState {

    private static final Logger logger = LoggerFactory.getLogger(CrowdState.class.getName());

    private NavMesh navMesh;
    private JmeCrowd jmeCrowd;
    private Node worldMap;

    @Override
    protected void initialize(Application app) {
    	super.initialize(app);

        worldMap = (Node) rootNode.getChild("MainScene");

        buildTiled();
        buildCrowd();
        buildAgentGrid();
        initKeys();
    }

    @Override
    protected void cleanup(Application app) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onEnable() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onDisable() {
        // TODO Auto-generated method stub
    }

    private void initKeys() {
        inputManager.addMapping("CROWD_PICK", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        //inputManager.addMapping("CROWD_PICK", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addListener(actionListener, "CROWD_PICK");
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("CROWD_PICK") && !keyPressed) {
                Vector3f locOnMap = getLocationOnMap();
                if (locOnMap != null) {
                    pathViewer.clearPath();
                    pathViewer.putBox(ColorRGBA.Yellow, locOnMap);
                    setTarget(locOnMap);
                } 
            }
        }
    };
    
    /**
     * Set the target for the crowd.
     * 
     * @param target The target to set.
     */
    public void setTarget(Vector3f target) {
    	jmeCrowd.setMoveTarget(target);
    }

    /**
     * Returns the Location on the Map which is currently under the Cursor. For this
     * we use the Camera to project the point onto the near and far plane (because
     * we don'from have the depth information [map height]). Then we can use this
     * information to do a raycast, ideally the world is in between those planes and
     * we hit it at the correct place.
     * 
     * @return The Location on the Map
     */
    private Vector3f getLocationOnMap() {
        Ray ray = MainCamera.screenPointToRay(camera, inputManager.getCursorPosition());
        CollisionResults collResults = new CollisionResults();
        worldMap.collideWith(ray, collResults);

        if (collResults.size() > 0) {
            return collResults.getClosestCollision().getContactPoint();
        } else {
            return null;
        }
    }

    private void buildTiled() {
        JmeInputGeomProvider m_geom = new JmeGeomProviderBuilder(worldMap).build();
        NavMeshBuildSettings s = new NavMeshBuildSettings();
        s.agentHeight = m_agentHeight;
        s.agentRadius = m_agentRadius;
        s.cellSize = 0.1f;
        s.cellHeight = 0.1f;
        s.detailSampleDist = 6f;
        s.detailSampleMaxError = 6f;
        s.tiled = true;

        System.out.println("Building NavMesh... Please wait");
        
        TileNavMeshBuilder tileBuilder = new TileNavMeshBuilder();
        navMesh = tileBuilder.build(m_geom, s);

        nmDebugViewer.drawMeshBounds(m_geom);
        nmDebugViewer.drawNavMesh(navMesh, true);

        //saveNavMesh("test.nm");
    }

    private void saveNavMesh(String fileName) {
        try {
            File f = new File(fileName);
            System.out.println("Saving NavMesh=" + f.getAbsolutePath());

            MeshSetWriter msw = new MeshSetWriter();
            msw.write(new FileOutputStream(f), navMesh, ByteOrder.BIG_ENDIAN, false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadNavMesh(String fileName) {
        try {
            File f = new File(fileName);
            System.out.println("Loading NavMesh=" + f.getAbsolutePath());
            int maxVertsPerPoly = 3;

            // Read in saved NavMesh.
            MeshSetReader msr = new MeshSetReader();
            navMesh = msr.read(new FileInputStream(f), maxVertsPerPoly);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void buildAgentGrid() {
    	
        Node npcsNode = new Node("npcs");
        rootNode.attachChild(npcsNode);

        //addAgent(createModel("Agent1", new Vector3f(-5, 0, 0), npcsNode));
        //addAgent(createModel("Agent2", new Vector3f(-4, 0, -1), npcsNode));
        //addAgent(createModel("Agent3", new Vector3f(-3, 0, 0), npcsNode));

        int size = 3;
        Vector3f center = new Vector3f(0, 0, 0);
        float distance = 1;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {

                // Set the start position for each node
                float x = center.getX() + i * distance;
                float y = center.getY();
                float z = center.getZ() + j * distance;
                Vector3f position = new Vector3f(x, y, z);

                // Add Agent
                String name = String.format("Agent_r%d_c%d", i, j);
                Node model = createModel(name, position, npcsNode);
                addAgent(model);

                logger.info("Agent Name [{}] Position {}", model.getName(), model.getWorldTranslation());
            }
        }
    }

    private void buildCrowd() {

        int includeFlags = SampleAreaModifications.SAMPLE_POLYFLAGS_ALL;
        int excludeFlags = SampleAreaModifications.SAMPLE_POLYFLAGS_DISABLED;
        float[] areaCost = new float[] { 1f, 10f, 1f, 1f, 2f, 1.5f };

        CrowdConfig config = new CrowdConfig(m_agentRadius);
        jmeCrowd = new SimpleCrowd(config, navMesh, new DefaultQueryFilter(includeFlags, excludeFlags, areaCost));
        
        // Add to CrowdManager.
        logger.info("usePhysics={}", usePhysics);
        jmeCrowd.setMovementType(usePhysics ? MovementType.PHYSICS_CHARACTER : MovementType.SPATIAL);
        getState(CrowdManagerAppState.class).addCrowd(jmeCrowd);
    }

    //-------------------------------------------------------
    // Crowd Agent Settings
    boolean usePhysics = true;
    float m_agentRadius = 0.3f;
    float m_agentHeight = 1.6f;
    float m_separationWeight = 1f;
    int m_obstacleAvoidanceType = ObstacleAvoidanceType.GoodQuality.id;

    // flags
    boolean m_anticipateTurns;
    boolean m_optimizeVis = true;
    boolean m_optimizeTopo = true;
    boolean m_obstacleAvoidance;
    boolean m_separation = true;
    //-------------------------------------------------------

    private void addAgent(Node model) {

        CrowdAgentParams ap = getAgentParams(model);
        // Add agent to the crowd.
        CrowdAgent agent = jmeCrowd.createAgent(model, ap);
        if (agent != null) {

            //model.attachChild(createCircle("CollQueryRange", agent.params.collisionQueryRange, ColorRGBA.Yellow));
            model.attachChild(createCircle("TargetProximity", 1, ColorRGBA.Red));

            model.addControl(new Animator());
            model.addControl(new CrowdControl(agent));

            // Add the debug control and set its visual and verbose state.
            CrowdDebugControl cwDebug = new CrowdDebugControl(agent, assetManager);
            cwDebug.setVisual(true);
            cwDebug.setVerbose(false);
            model.addControl(cwDebug);
        }
    }

    private CrowdAgentParams getAgentParams(Spatial model) {
        CrowdAgentParams ap = new CrowdAgentParams();
        ap.radius = m_agentRadius;
        ap.height = m_agentHeight;
        ap.maxAcceleration = 8.0f;
        ap.maxSpeed = 2;
        ap.collisionQueryRange = ap.radius * 12.0f;
        ap.pathOptimizationRange = ap.radius * 30.0f;
        ap.updateFlags = getUpdateFlags();
        ap.obstacleAvoidanceType = m_obstacleAvoidanceType;
        ap.separationWeight = m_separationWeight;
        ap.userData = model;
        return ap;
    }

    private int getUpdateFlags() {
        int updateFlags = 0;
        
        if (m_anticipateTurns) {
            updateFlags |= CrowdAgentParams.DT_CROWD_ANTICIPATE_TURNS;
        }
        if (m_optimizeVis) {
            updateFlags |= CrowdAgentParams.DT_CROWD_OPTIMIZE_VIS;
        }
        if (m_optimizeTopo) {
            updateFlags |= CrowdAgentParams.DT_CROWD_OPTIMIZE_TOPO;
        }
        if (m_obstacleAvoidance) {
            updateFlags |= CrowdAgentParams.DT_CROWD_OBSTACLE_AVOIDANCE;
        }
        if (m_separation) {
            updateFlags |= CrowdAgentParams.DT_CROWD_SEPARATION;
        }
        return updateFlags;
    }

    private Node createModel(String name, Vector3f position, Node parent) {

        //Load the spatial that will represent the agent.
        Node model = (Node) assetManager.loadModel("Models/Jaime/Jaime.j3o");
        model.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        model.setName(name);
        //Set translation prior to adding controls.
        model.setLocalTranslation(position);
        //Add agent to the scene.
        parent.attachChild(model);

        if (usePhysics) {
            model.addControl(new BetterCharacterControl(m_agentRadius, m_agentHeight, 20f));
            getPhysicsSpace().add(model);

        } else {
            RigidBodyControl rbc = createRigidBody(m_agentRadius, m_agentHeight);
            model.addControl(rbc);
            getPhysicsSpace().add(rbc);
        }

        return model;
    }

    private RigidBodyControl createRigidBody(float radius, float height) {
        //BetterCharacterControl bcc = new BetterCharacterControl(radius, height, 1f);
        //CollisionShape collShape = bcc.getRigidBody().getCollisionShape()

        CapsuleCollisionShape capsule = new CapsuleCollisionShape(radius, (height - (2 * radius)));
        CompoundCollisionShape collShape = new CompoundCollisionShape();
        Vector3f position = new Vector3f(0, (height / 2f), 0);
        collShape.addChildShape(capsule, position);

        // Setup root motion physics control
        RigidBodyControl rbc = new RigidBodyControl(collShape);
        // Kinematic mode must be enabled so character is not influenced by physics
        rbc.setKinematic(true);
        // Apply spatial transform to the collision shape
        rbc.setKinematicSpatial(true);

        return rbc;
    }

    private float[] calcBounds(Spatial sp) {
        // Auto calculate based on bounds.
        BoundingBox bounds = (BoundingBox) sp.getWorldBound();
        float x = bounds.getXExtent();
        float z = bounds.getZExtent();
        float y = bounds.getYExtent();

        float xz = x < z ? x : z;
        float radius = xz / 2;
        float height = y * 2;

        return new float[] { radius, height };
    }
    
    private Geometry createCircle(String name, float radius, ColorRGBA color) {
        Circle circle = new Circle(Vector3f.ZERO, radius, 32);
        Geometry geo = new Geometry(name, circle);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geo.setMaterial(mat);
        geo.setShadowMode(RenderQueue.ShadowMode.Off);
        return geo;
    }

}
