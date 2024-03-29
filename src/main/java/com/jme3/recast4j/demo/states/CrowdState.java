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

import com.jme3.anim.SkinningControl;
import com.jme3.anim.util.AnimMigrationUtils;
import com.jme3.app.Application;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.PhysicsCollisionObject;
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
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.builder.NavMeshBuildSettings;
import com.jme3.recast4j.builder.SampleAreaModifications;
import com.jme3.recast4j.builder.TileNavMeshBuilder;
import com.jme3.recast4j.demo.controls.Animator;
import com.jme3.recast4j.demo.controls.CrowdControl;
import com.jme3.recast4j.demo.controls.CrowdDebugControl;
import com.jme3.recast4j.demo.utils.Circle;
import com.jme3.recast4j.demo.utils.MainCamera;
import com.jme3.recast4j.detour.crowd.CrowdConfig;
import com.jme3.recast4j.detour.crowd.CrowdManagerAppState;
import com.jme3.recast4j.detour.crowd.JmeCrowd;
import com.jme3.recast4j.detour.crowd.MovementType;
import com.jme3.recast4j.detour.crowd.ObstacleAvoidanceType;
import com.jme3.recast4j.detour.crowd.SimpleCrowd;
import com.jme3.recast4j.detour.crowd.TargetProximity;
import com.jme3.recast4j.geom.InputGeomProviderBuilder;
import com.jme3.recast4j.geom.JmeInputGeomProvider;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

/**
 *
 * @author capdevon
 */
public class CrowdState extends AbstractNavState {

    private static final Logger logger = LoggerFactory.getLogger(CrowdState.class.getName());

    private NavMesh navMesh;
    private JmeCrowd jmeCrowd;
    private Node worldMap;
    
    //-------------------------------------------------------
    // Crowd Agent Settings
    private Spatial model;
    private boolean usePhysics = false;
    private float m_agentRadius = 0.3f;
    private float m_agentHeight = 1.6f;
    private float m_separationWeight = 1f;
    private int m_obstacleAvoidanceType = ObstacleAvoidanceType.GoodQuality.id;
    private float stoppingDistance = 0.1f;

    // flags
    private boolean m_anticipateTurns;
    private boolean m_optimizeVis = true;
    private boolean m_optimizeTopo = true;
    private boolean m_obstacleAvoidance;
    private boolean m_separation = true;
    //-------------------------------------------------------

    @Override
    protected void simpleInit() {
        super.simpleInit();

        worldMap = (Node) find("MainScene");

        buildTiled();
        buildCrowd();
        buildAgentGrid();
        initKeys();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    private void initKeys() {
        inputManager.addMapping("CROWD_PICK", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        //inputManager.addMapping("CROWD_PICK", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addListener(actionListener, "CROWD_PICK");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("CROWD_PICK") && !keyPressed) {
                Vector3f locOnMap = getLocationOnMap();
                if (locOnMap != null) {
                    debugHelper.clear();
                    debugHelper.color = ColorRGBA.Yellow;
                    debugHelper.drawCube(locOnMap, .15f);
                    //setTarget(locOnMap);
                    makeAgentsCircleTarget(locOnMap);
                }
            }
        }
    };
    
    public void makeAgentsCircleTarget(Vector3f target) {
        float radius = 2f;
        int i = 0;
        int activeAgents = jmeCrowd.getActiveAgents().size();
        for (CrowdAgent agent : jmeCrowd.getActiveAgents()) {
            float angle = FastMath.TWO_PI * i / activeAgents;
            float x = FastMath.cos(angle) * radius;
            float z = FastMath.sin(angle) * radius;
            jmeCrowd.setAgentTarget(agent, target.add(x, 0, z));
            i++;
        }
    }

    /**
     * Set the target for the crowd.
     *
     * @param target The target to set.
     */
    public void setTarget(Vector3f target) {
        jmeCrowd.setMoveTarget(target);
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
        
        NavMeshBuildSettings s = new NavMeshBuildSettings();
        s.agentHeight = m_agentHeight;
        s.agentRadius = m_agentRadius;
        s.cellSize = 0.1f;
        s.cellHeight = 0.1f;
        s.detailSampleDist = 6f;
        s.detailSampleMaxError = 6f;
        s.tiled = true;

        System.out.println("Building NavMesh... Please wait");

        JmeInputGeomProvider m_geom = InputGeomProviderBuilder.build(worldMap);
        TileNavMeshBuilder tileBuilder = new TileNavMeshBuilder();
        navMesh = tileBuilder.build(m_geom, s);

        navMeshRenderer.drawMeshBounds(m_geom);
        navMeshRenderer.drawNavMesh(navMesh, true);

        //saveNavMesh("test.nm");
    }

    private void saveNavMesh(String fileName) {
        try {
            File f = new File(fileName);
            System.out.println("Saving NavMesh=" + f.getAbsolutePath());

            boolean cCompatibility = false;
            MeshSetWriter msw = new MeshSetWriter();
            msw.write(new FileOutputStream(f), navMesh, ByteOrder.BIG_ENDIAN, cCompatibility);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadNavMesh(String fileName) {
        try {
            File f = new File(fileName);
            System.out.println("Loading NavMesh=" + f.getAbsolutePath());

            // Read in saved NavMesh.
            int maxVertsPerPoly = 3;
            MeshSetReader msr = new MeshSetReader();
            navMesh = msr.read(new FileInputStream(f), maxVertsPerPoly);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildAgentGrid() {

        Node npcsNode = new Node("npcs");
        rootNode.attachChild(npcsNode);

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
                String name = String.format("Agent_%d%d", i, j);
                Node model = createModel(name, position, npcsNode);
                addAgent(model);

                logger.info("Agent Name [{}] Position {}", model.getName(), model.getWorldTranslation());
            }
        }
    }

    private void buildCrowd() {

        int includeFlags = SampleAreaModifications.SAMPLE_POLYFLAGS_ALL;
        int excludeFlags = SampleAreaModifications.SAMPLE_POLYFLAGS_DISABLED;
        float[] areaCost = new float[]{1f, 10f, 1f, 1f, 2f, 1.5f};

        CrowdConfig config = new CrowdConfig(m_agentRadius);
        jmeCrowd = new SimpleCrowd(config, navMesh, new DefaultQueryFilter(includeFlags, excludeFlags, areaCost));

        // Add to CrowdManager.
        logger.info("usePhysics={}", usePhysics);
        jmeCrowd.setMovementType(usePhysics ? MovementType.PHYSICS_CHARACTER : MovementType.SPATIAL);
        ((TargetProximity) jmeCrowd.getProximity()).setDistanceThreshold(stoppingDistance);
        getState(CrowdManagerAppState.class).addCrowd(jmeCrowd);
    }

    private void addAgent(Node model) {

        CrowdAgentParams ap = getAgentParams(model);
        // Add agent to the crowd.
        CrowdAgent agent = jmeCrowd.createAgent(model, ap);
        if (agent != null) {

            //model.attachChild(createCircle("CollQueryRange", agent.params.collisionQueryRange, ColorRGBA.Yellow));
            model.attachChild(createCircle("TargetProximity", stoppingDistance, ColorRGBA.Red));

            model.addControl(new Animator());
            model.addControl(new CrowdControl(agent));
            // Add the debug control and set its visual and verbose state.
            model.addControl(new CrowdDebugControl(agent, assetManager));
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

    private Spatial loadModel() {
        /*
         * Load the Jaime model and convert it 
         * from the old animation system to the new one.
         */
        if (model == null) {
            Spatial sp = assetManager.loadModel("Models/Jaime/Jaime.j3o");
            model = AnimMigrationUtils.migrate(sp);

            Box box = new Box(0.3f, 0.02f, 0.02f);
            Geometry saber = new Geometry("saber", box);
            saber.move(0.4f, 0.05f, 0.01f);
            Material red = assetManager.loadMaterial("Common/Materials/RedColor.j3m");
            saber.setMaterial(red);
            /*
             * Create an attachments node for Jaime's right hand,
             * and attach the saber to that Node.
             */
            SkinningControl skinningControl = model.getControl(SkinningControl.class);
            Node n = skinningControl.getAttachmentsNode("hand.R");
            n.attachChild(saber);
        }

        return model.clone(false);
    }

    private Node createModel(String name, Vector3f position, Node parent) {

        Node npc = (Node) loadModel();
        npc.setName(name);
        //Set translation prior to adding controls.
        npc.setLocalTranslation(position);
        //Add agent to the scene.
        parent.attachChild(npc);

        if (usePhysics) {
            npc.addControl(new BetterCharacterControl(m_agentRadius, m_agentHeight, 20f));
            getPhysicsSpace().add(npc);

        } else {
            RigidBodyControl rbc = createRigidBody(m_agentRadius, m_agentHeight);
            npc.addControl(rbc);
            getPhysicsSpace().add(rbc);
        }

        return npc;
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
        rbc.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
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

        float radius = Math.max(x, z) / 2;
        float height = y * 2;

        return new float[]{radius, height};
    }

    private Geometry createCircle(String name, float radius, ColorRGBA color) {
        Circle circle = new Circle(Vector3f.ZERO, radius, 32);
        Geometry geo = new Geometry(name, circle);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geo.setMaterial(mat);
        geo.setShadowMode(ShadowMode.Off);
        return geo;
    }

}
