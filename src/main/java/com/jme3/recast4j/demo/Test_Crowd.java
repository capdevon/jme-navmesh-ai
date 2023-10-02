package com.jme3.recast4j.demo;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.demo.states.CrowdState;
import com.jme3.recast4j.demo.states.TogglePhysicsDebugState;
import com.jme3.recast4j.detour.crowd.CrowdManagerAppState;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

/**
 * 
 * @author capdevon
 */
public class Test_Crowd extends SimpleApplication {

    private BulletAppState physics;

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        Test_Crowd app = new Test_Crowd();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);

        app.setSettings(settings);
        app.setPauseOnLostFocus(false);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initPhysics();
        setupScene();
        addLighting();
        configureCamera();
        
        stateManager.attach(new CrowdManagerAppState());
        stateManager.attach(new CrowdState());
    }
    
    /**
     * Configure the Camera during startup.
     */
    private void configureCamera() {
        flyCam.setMoveSpeed(20);
        flyCam.setDragToRotate(true);
        cam.setLocation(new Vector3f(0, 6, 12));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    /**
     * Configure physics during startup.
     */
    private void initPhysics() {
        physics = new BulletAppState();
        stateManager.attach(physics);
        stateManager.attach(new TogglePhysicsDebugState());
    }

    private void setupScene() {
        Node scene = new Node("MainScene");
        rootNode.attachChild(scene);

        Box box = new Box(40f, .1f, 40f);
        box.scaleTextureCoordinates(new Vector2f(12, 12));
        Geometry floor = new Geometry("Floor", box);

        Material mat = new Material(assetManager, Materials.LIGHTING);
        Texture texture = assetManager.loadTexture("Textures/Ground/default_grid.png");
        texture.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap", texture);
        floor.setMaterial(mat);

        CollisionShape shape = CollisionShapeFactory.createMeshShape(floor);
        RigidBodyControl rbc = new RigidBodyControl(shape, 0);
        floor.addControl(rbc);
        scene.attachChild(floor);
        physics.getPhysicsSpace().add(rbc);
    }

    /**
     * Add lighting and shadows.
     */
    private void addLighting() {
        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // Set the viewport's background color to light blue.
        ColorRGBA skyColor = new ColorRGBA(0.1f, 0.2f, 0.4f, 1f);
        viewPort.setBackgroundColor(skyColor);

        Vector3f lightDir = new Vector3f(-7f, -3f, -5f).normalizeLocal();
        DirectionalLight sun = new DirectionalLight(lightDir);
        sun.setName("sun");
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.25f, 0.25f, 0.25f, 1));
        ambient.setName("ambient");
        rootNode.addLight(ambient);

        // Render shadows based on the directional light.
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 2_048, 3);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
        dlsr.setEdgesThickness(5);
        dlsr.setLight(sun);
        dlsr.setShadowIntensity(0.4f);
        viewPort.addProcessor(dlsr);
    }
    
}
