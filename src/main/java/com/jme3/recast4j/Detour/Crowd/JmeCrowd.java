package com.jme3.recast4j.demo.states;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

import org.recast4j.detour.NavMesh;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.recast4j.detour.crowd.ObstacleAvoidanceQuery.ObstacleAvoidanceParams;
import org.recast4j.detour.io.MeshSetWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
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
import com.jme3.recast4j.Detour.Crowd.CrowdManagerAppState;
import com.jme3.recast4j.Detour.Crowd.JmeCrowd;
import com.jme3.recast4j.Detour.Crowd.MovementType;
import com.jme3.recast4j.debug.NavMeshDebugViewer;
import com.jme3.recast4j.debug.NavPathDebugViewer;
import com.jme3.recast4j.demo.controls.CrowdDebugControl;
import com.jme3.recast4j.editor.NavMeshBuildSettings;
import com.jme3.recast4j.editor.builder.TileNavMeshBuilder;
import com.jme3.recast4j.geom.JmeGeomProviderBuilder;
import com.jme3.recast4j.geom.JmeInputGeomProvider;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

/**
 * 
 * @author capdevon
 */
public class CrowdState extends BaseAppState {

    private static final Logger LOG = LoggerFactory.getLogger(CrowdState.class.getName());

    private ViewPort viewPort;
    private AssetManager assetManager;
    private InputManager inputManager;

    private NavMeshDebugViewer nmDebugViewer;
    private NavPathDebugViewer pathViewer;
    private NavMesh navMesh;
    private JmeCrowd crowd;

    private Node worldMap = new Node("worldMap");

    @Override
    protected void initialize(Application app) {
        this.viewPort = app.getViewPort();
        this.assetManager = app.getAssetManager();
        this.inputManager = app.getInputManager();

        nmDebugViewer = new NavMeshDebugViewer(app.getAssetManager());
        pathViewer = new NavPathDebugViewer(app.getAssetManager());

        worldMap.attachChild(createFloor());
        getRootNode().attachChild(worldMap);

        buildTiled();
        buildCrowd();

        addAgent(new Vector3f(-5, 0, 0));
        addAgent(new Vector3f(-4f, 0.0f, -1f));
        addAgent(new Vector3f(-3, 0, 0));

        initKeys();
    }

    private Node getRootNode() {
        return ((SimpleApplication) getApplication()).getRootNode();
    }

    protected PhysicsSpace getPhysicsSpace() {
        return getState(BulletAppState.class).getPhysicsSpace();
    }

    private void initKeys() {
        inputManager.addMapping("crowd pick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        //        inputManager.addMapping("crowd pick", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addListener(actionListener, "crowd pick");
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("crowd pick") && !keyPressed) {
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
     * Returns the Location on the Map which is currently under the Cursor. For this
     * we use the Camera to project the point onto the near and far plane (because
     * we don'from have the depth information [map height]). Then we can use this
     * information to do a raycast, ideally the world is in between those planes and
     * we hit it at the correct place.
     * 
     * @return The Location on the Map
     */
    private Vector3f getLocationOnMap() {
        Ray ray = screenPointToRay(getApplication().getCamera(), inputManager.getCursorPosition());
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
        s.tiled = true;

        TileNavMeshBuilder builder = new TileNavMeshBuilder();

        navMesh = builder.build(m_geom, s);

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

    private void buildCrowd() {
        // Start crowd.
        crowd = new JmeCrowd(100, .3f, navMesh);
        crowd.setMovementType(MovementType.BETTER_CHARACTER_CONTROL);
        // Add to CrowdManager.
        getState(CrowdManagerAppState.class).getCrowdManager().addCrowd(crowd);

        // Setup local avoidance params to different qualities.
        // Use mostly default settings, copy from dtCrowd.
        ObstacleAvoidanceParams params = new ObstacleAvoidanceParams(crowd.getObstacleAvoidanceParams(0));

        // Low (11)
        params.velBias = 0.5f;
        params.adaptiveDivs = 5;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 1;
        crowd.setObstacleAvoidanceParams(0, params);

        // Medium (22)
        params.velBias = 0.5f;
        params.adaptiveDivs = 5;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 2;
        crowd.setObstacleAvoidanceParams(1, params);

        // Good (45)
        params.velBias = 0.5f;
        params.adaptiveDivs = 7;
        params.adaptiveRings = 2;
        params.adaptiveDepth = 3;
        crowd.setObstacleAvoidanceParams(2, params);

        // High (66)
        params.velBias = 0.5f;
        params.adaptiveDivs = 7;
        params.adaptiveRings = 3;
        params.adaptiveDepth = 3;
        crowd.setObstacleAvoidanceParams(3, params);
    }

    @Override
    protected void cleanup(Application app) {}

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}

    @Override
    public void render(RenderManager rm) {
        nmDebugViewer.show(rm, viewPort);
        pathViewer.show(rm, viewPort);
    }

    /**
     * Set the target for the crowd.
     * 
     * @param target The target to set.
     */
    public void setTarget(Vector3f target) {
        crowd.requestMoveToTarget(target);
    }

    public float agentRadius = 0.3f;
    public float agentHeight = 1.7f;
    public boolean m_anticipateTurns;
    public boolean m_optimizeVis = true;
    public boolean m_optimizeTopo = true;
    public boolean m_obstacleAvoidance;
    public boolean m_separation = true;

    private void addAgent(Vector3f location) {

        Node model = createModel(location);

        CrowdAgentParams ap = getAgentParams(model);
        // Add agent to the crowd.
        CrowdAgent agent = crowd.createAgent(model.getWorldTranslation(), ap);
        // Set the spatial for the agent.
        //crowd.setSpatialForAgent(agent, model);
        // Add the debug control and set its visual and verbose state.
        CrowdDebugControl cwDebug = new CrowdDebugControl(crowd, agent, createTorus(agentHeight));
        cwDebug.setVisual(true);
        cwDebug.setVerbose(false);
        model.addControl(cwDebug);
    }

    private CrowdAgentParams getAgentParams(Spatial model) {
        CrowdAgentParams ap = new CrowdAgentParams();
        ap.radius = agentRadius;
        ap.height = agentHeight;
        ap.maxAcceleration = 8.0f;
        ap.maxSpeed = 2;
        ap.collisionQueryRange = ap.radius * 12.0f;
        ap.pathOptimizationRange = ap.radius * 30.0f;
        ap.updateFlags = getUpdateFlags();
        ap.obstacleAvoidanceType = 2;
        ap.separationWeight = 2f;
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

    private Node createModel(Vector3f position) {

        //Load the spatial that will represent the agent.
        Node model = (Node) assetManager.loadModel("Models/Jaime/Jaime.j3o");
        model.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        //Set translation prior to adding controls.
        model.setLocalTranslation(position);
        //Add agent to the scene.
        getRootNode().attachChild(model);

        //If we have a physics Crowd we need a physics compatible control to apply
        //movement and direction to the spatial.
        model.addControl(new BetterCharacterControl(agentRadius, agentHeight, 20f));
        getPhysicsSpace().add(model);

        //RigidBodyControl rbc = createRigidBody(agentRadius, agentHeight);
        //model.addControl(rbc);
        //getPhysicsSpace().add(rbc);

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

    private Geometry createTorus(float yHeight) {
        // Were going to use a debug move control so setup geometry for later use.
        Sphere sphere = new Sphere(16, 16, 0.2f);
        Geometry geo = new Geometry("halo", sphere);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        geo.setMaterial(mat);
        geo.setLocalTranslation(0, yHeight + 0.5f, 0);
        geo.setShadowMode(RenderQueue.ShadowMode.Off);

        return geo;
    }

}
