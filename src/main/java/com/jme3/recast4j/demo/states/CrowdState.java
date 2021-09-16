package com.jme3.recast4j.demo.states;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.ObstacleAvoidanceQuery.ObstacleAvoidanceParams;
import org.recast4j.detour.io.MeshSetWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.Crowd.CrowdConfig;
import com.jme3.recast4j.Detour.Crowd.CrowdManagerAppState;
import com.jme3.recast4j.Detour.Crowd.JmeCrowd;
import com.jme3.recast4j.Detour.Crowd.MovementType;
import com.jme3.recast4j.Detour.Crowd.ObstacleAvoidanceType;
import com.jme3.recast4j.demo.controls.CrowdDebugControl;
import com.jme3.recast4j.editor.NavMeshBuildSettings;
import com.jme3.recast4j.editor.SampleAreaModifications;
import com.jme3.recast4j.editor.builder.TileNavMeshBuilder;
import com.jme3.recast4j.geom.JmeGeomProviderBuilder;
import com.jme3.recast4j.geom.JmeInputGeomProvider;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

/**
 * 
 * @author capdevon
 */
public class CrowdState extends AbstractNavState {

    private static final Logger logger = LoggerFactory.getLogger(CrowdState.class.getName());

    private NavMesh navMesh;
    private JmeCrowd jmeCrowd;

    private Node worldMap = new Node("worldMap");
    private Node npcsNode = new Node("npcs");

    @Override
    protected void initialize(Application app) {
    	super.initialize(app);

        worldMap.attachChild(createFloor());
        rootNode.attachChild(worldMap);
        rootNode.attachChild(npcsNode);

        buildTiled();
        buildCrowd();
        buildAgentGrid();
        initKeys();
    }

    @Override
    protected void cleanup(Application app) {}

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}

    private void initKeys() {
        inputManager.addMapping("CROWD_PICK", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        //inputManager.addMapping("CROWD_PICK", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("TOGGLE_PHYSX_DEBUG", new KeyTrigger(KeyInput.KEY_0));
        inputManager.addListener(actionListener, "CROWD_PICK", "TOGGLE_PHYSX_DEBUG");
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
            } else if (name.equals("TOGGLE_PHYSX_DEBUG") && !keyPressed) {
                boolean debugEnabled = getState(BulletAppState.class).isDebugEnabled();
                getState(BulletAppState.class).setDebugEnabled(!debugEnabled);
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

    private Geometry createFloor() {
        Box box = new Box(40f, .1f, 40f);
        box.scaleTextureCoordinates(new Vector2f(10, 10)); //5x5
        Geometry geo = new Geometry("Colored Box", box);

        //Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        //mat.setBoolean("UseMaterialColors", true);
        //mat.setColor("Ambient", ColorRGBA.LightGray);
        //mat.setColor("Diffuse", ColorRGBA.LightGray);

        Material mat = new Material(assetManager, Materials.LIGHTING);
        Texture texture = assetManager.loadTexture("Textures/Level/default_grid.png");
        texture.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap", texture);

        geo.setMaterial(mat);

        CollisionShape shape = CollisionShapeFactory.createMeshShape(geo);
        RigidBodyControl rgb = new RigidBodyControl(shape, 0);
        geo.addControl(rgb);
        getPhysicsSpace().add(rgb);

        return geo;
    }

    private void buildTiled() {
        JmeInputGeomProvider m_geom = new JmeGeomProviderBuilder(worldMap).build();
        NavMeshBuildSettings s = new NavMeshBuildSettings();
        s.agentHeight = m_agentHeight;
        s.agentRadius = m_agentRadius;
        s.tiled = true;

        TileNavMeshBuilder tileBuilder = new TileNavMeshBuilder();
        navMesh = tileBuilder.build(m_geom, s);

        nmDebugViewer.drawMeshBounds(m_geom);
        nmDebugViewer.drawNavMesh(navMesh, true);

        //saveToFile(navMesh, "test.nm");
    }

    private void saveToFile(NavMesh nm, String fileName) {
        try {
            MeshSetWriter msw = new MeshSetWriter();
            File f = new File(fileName);
            System.out.println("Saving NavMesh=" + f.getAbsolutePath());
            msw.write(new FileOutputStream(f), nm, ByteOrder.BIG_ENDIAN, false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void buildAgentGrid() {

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
        jmeCrowd = new JmeCrowd(config, navMesh, __ -> new DefaultQueryFilter(includeFlags, excludeFlags, areaCost));

        // Setup local avoidance params to different qualities.
        ObstacleAvoidanceParams params = new ObstacleAvoidanceParams();

        // Low (11)
        params.velBias = 0.5f;
        params.adaptiveDivs = 5;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 1;
        jmeCrowd.setObstacleAvoidanceParams(ObstacleAvoidanceType.LowQuality.getId(), params);

        // Medium (22)
        params.velBias = 0.5f;
        params.adaptiveDivs = 5;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 2;
        jmeCrowd.setObstacleAvoidanceParams(ObstacleAvoidanceType.MedQuality.getId(), params);

        // Good (45)
        params.velBias = 0.5f;
        params.adaptiveDivs = 7;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 3;
        jmeCrowd.setObstacleAvoidanceParams(ObstacleAvoidanceType.GoodQuality.getId(), params);

        // High (66)
        params.velBias = 0.5f;
        params.adaptiveDivs = 7;
        params.adaptiveRings = 3;
        params.adaptiveDepth = 3;
        jmeCrowd.setObstacleAvoidanceParams(ObstacleAvoidanceType.HighQuality.getId(), params);

        // Add to CrowdManager.
        jmeCrowd.setMovementType(usePhysics ? MovementType.PHYSICS_CHARACTER : MovementType.SPATIAL);
        getState(CrowdManagerAppState.class).addCrowd(jmeCrowd);
    }

    //-------------------------------------------------------
    // Crowd Agent Settings
    boolean usePhysics = true;
    float m_agentRadius = 0.3f;
    float m_agentHeight = 1.7f;
    float m_separationWeight = 2f;
    int m_obstacleAvoidanceType = ObstacleAvoidanceType.GoodQuality.getId();

    // flags
    boolean m_anticipateTurns;
    boolean m_optimizeVis = true;
    boolean m_optimizeTopo = true;
    boolean m_obstacleAvoidance;
    boolean m_separation = true;
    //-------------------------------------------------------

    private void addAgent(Spatial model) {

        CrowdAgentParams ap = getAgentParams(model);
        // Add agent to the crowd.
        CrowdAgent agent = jmeCrowd.createAgent(model, ap);
        if (agent != null) {
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

}
