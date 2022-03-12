package com.jme3.recast4j.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.animation.SkeletonControl;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.audio.AudioListenerState;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.recast4j.demo.controls.DoorSwingControl;
import com.jme3.recast4j.demo.states.LemurConfigState;
import com.jme3.recast4j.demo.states.NavState;
import com.jme3.recast4j.demo.states.ThirdPersonCamState;
import com.jme3.recast4j.demo.utils.GameObject;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture2D;
import com.jme3.water.WaterFilter;

/**
 * 
 * @author capdevon
 */
public class DemoApplication extends SimpleApplication {
	
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class.getName());
    
    private final Quaternion YAW180 = new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(0,1,0));
    private Node worldMap;
    private Node doorNode;
    private Node offMeshCon;
    private BulletAppState bullet;
    
    public DemoApplication() {
        super(new StatsAppState(),
                new AudioListenerState(),
                new DebugKeysAppState(),
                new NavState(),
                new LemurConfigState(),
                new ThirdPersonCamState()
        );
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        DemoApplication app = new DemoApplication();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("jme3-recast4j - DemoApplication");
        settings.setResolution(1280, 720);

        app.setSettings(settings);
        app.setPauseOnLostFocus(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {  
        initPhysics();
        setupWorld();
        addLighting();
//        loadNavMeshBox();
//        loadNavMeshDune();
        loadNavMeshLevel();
//        loadPond();
//        loadPondSurface();
//        loadCrate();
        
        configureInput();
    }
    
    /**
     * Configure keyboard input during startup.
     */
    private void configureInput() {
        inputManager.addMapping("TOGGLE_PHYSX_DEBUG", new KeyTrigger(KeyInput.KEY_0));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (isPressed) {
                    boolean debugEnabled = bullet.isDebugEnabled();
                    bullet.setDebugEnabled(!debugEnabled);
                }
            }
        }, "TOGGLE_PHYSX_DEBUG");
    }
    
    private void initPhysics() {
        bullet = new BulletAppState();
        // Performance is better when threading in parallel
        bullet.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bullet);
        bullet.setDebugEnabled(false);
    }
    
    private void setupWorld() {
        worldMap = new Node("worldmap");
        worldMap.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(worldMap);

        offMeshCon = new Node("offMeshCon");
        rootNode.attachChild(offMeshCon);
    }
    
    /**
     * Add lighting and shadows.
     */
    private void addLighting() {
        //Set the atmosphere of the world, lights, and post processing.
        ColorRGBA skyColor = new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f);
        viewPort.setBackgroundColor(skyColor);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.2f, -1, -0.3f).normalizeLocal());
        sun.setName("sun");
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.25f, 0.25f, 0.25f, 1));
        ambient.setName("ambient");
        rootNode.addLight(ambient);

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 4096, 2);
        dlsf.setLight(sun);
        dlsf.setShadowIntensity(0.4f);
        dlsf.setShadowZExtend(256);

        FXAAFilter fxaa = new FXAAFilter();

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);
        fpp.addFilter(fxaa);
        //fpp.addFilter(setupWater());
        viewPort.addProcessor(fpp);
    }
    
    private void loadNavMeshBox() {

        Box box = new Box(8f, 1f, 8f);
        Geometry floor = new Geometry("Floor", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", ColorRGBA.Red);
        mat.setColor("Ambient", ColorRGBA.White);
        mat.setBoolean("UseMaterialColors", true);
        floor.setMaterial(mat);

        CollisionShape shape = CollisionShapeFactory.createMeshShape(floor);
        RigidBodyControl rbc = new RigidBodyControl(shape, 0);
        floor.addControl(rbc);

        bullet.getPhysicsSpace().add(rbc);
        worldMap.attachChild(floor);
    }

//    private void loadNavMeshDune() {
//        worldMap = (Geometry)assetManager.loadModel("Models/dune.j3o");
//        // @TODO: Dune.j3o does not have normals and thus no neat lighting.
//        TangentBinormalGenerator.generate(worldMap.getMesh());
//    }

    /**
     * Loads the room scene and adds, rotates, and moves the doors into place.
     */
    private void loadNavMeshLevel() {  
        Node level = (Node) assetManager.loadModel("Models/Level/recast_level.mesh.j3o"); 
        level.addControl(new RigidBodyControl(0));
        bullet.getPhysicsSpace().add(level);
        worldMap.attachChild(level);
        
        /**
         * Create door node here since NavState checks the door node for null 
         * to avoid trying to add MouseEventControl when no door node is used. 
         * Like when loading different scenes such as the pond. The nodes name 
         * is used for locating it as a child of rootNode.
         */
        doorNode = new Node("doorNode");
        rootNode.attachChild(doorNode);

        /**
         * Creating doors in blender with their origin at (0,0,0) is required.
         * The findPolysAroundCircle method in MouseEventControl uses the doors 
         * origin to localize the search for door polys so doors must be moved 
         * into their final position. If you apply location in blender, then 
         * findPolysAroundCircle would start the search from (0,0,0).
         */
        loadDoor(1, new Vector3f(-.39f, 0f, -10.03f), null);
        loadDoor(2, new Vector3f(-15.49f, 2.7f, -2.23f), null);
        loadDoor(3, new Vector3f(-21.49f, 2.7f, -2.23f), null);
        loadDoor(4, new Vector3f(-22.51f, 2.7f, 2.23f), YAW180);
        loadDoor(5, new Vector3f(-16.51f, 2.7f, 2.23f), YAW180);
    }

    /**
     * Adds a door to the scene at the given location and rotation.
     * 
     * @param location Where to move the door to.
     * @param rotation The doors rotation. A null value will keep the doors 
     * current rotation.
     */
    private void loadDoor(int id, Vector3f location, Quaternion rotation) {
        
        /**
         * gltf loader test. This works but gltf doesn't when it comes to 
         * exporting animations created in blender. Imported animations into 
         * blender that are then exported do work however.
         */
//        GltfModelKey modelKey = new GltfModelKey("Textures/Level/Door.gltf");
//        ExtrasLoader extras = new GltfUserDataLoader();
//        modelKey.setExtrasLoader(extras);
        
        //Load a door.
        Node door = (Node) assetManager.loadModel("Models/Level/Door.mesh.j3o");
        door.setName("door-" + id);
        
        SkeletonControl skelControl = GameObject.getComponentInChildren(door, SkeletonControl.class);
        /**
         * Couldn't get hardware skinning to turn off which would allow the 
         * bounding box to move with the door as it opens or closes so added
         * a hitBox to the root bone instead.
         */
        if (skelControl != null) {
            //Create a box shape with the same dimensions as the door.
            BoundingBox bbox = (BoundingBox) door.getWorldBound();
            Box boxMesh = new Box(bbox.getXExtent(), bbox.getYExtent(), bbox.getZExtent());

            //The geometry for the door.
            Geometry boxGeo = new Geometry("hitBox-" + id, boxMesh); 

            //The material.
            Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); 
            boxMat.setColor("Color", ColorRGBA.Green);
            boxMat.getAdditionalRenderState().setWireframe(true);
            boxGeo.setMaterial(boxMat); 
            //Toggle visibility.
            //boxGeo.setCullHint(Spatial.CullHint.Always);

            //Center hitBox to door.
            boxGeo.setLocalTranslation(bbox.getCenter());

            /**
             * Create a node that will use the same origin as the root
             * bone which has the same origin as the door. This will 
             * keep the searches in MouseEventControl localized to this 
             * door.
             */
            Node collisionNode = new Node("collisionNode-" + id);
            collisionNode.attachChild(boxGeo);

            String rootBone = skelControl.getSkeleton().getBone(0).getName();
            //Our root bone for the animations.
            skelControl.getAttachmentsNode(rootBone).attachChild(collisionNode);
            //Add our animation swing control and attach to rootNode.
            door.addControl(new DoorSwingControl());
        }
        
        /**
         * Creating doors in blender with their origin at (0,0,0) is required.
         * The findPolysAroundCircle method in MouseEventControl uses the doors 
         * origin to localize the search for door polys so doors must be moved 
         * into their final position. If you apply location in blender, then 
         * findPolysAroundCircle would start the search from (0,0,0).
         */ 
        door.setLocalTranslation(location);
        
        //Some doors need rotating.
        if (rotation != null) {
            door.setLocalRotation(rotation);
        }

        doorNode.attachChild(door);
    }      
        
    private void loadFish() {
        Node fish = (Node) assetManager.loadModel("Models/Fish/Fish1.j3o");
        fish.setName("fish");
        fish.setLocalTranslation(-8f, -.2f, 0f);
//        fish.addControl(new BetterCharacterControl(.2f, .4f, 1f));
//        fish.addControl(new PhysicsAgentControl());
//        bullet.getPhysicsSpace().add(fish);
        rootNode.attachChild(fish);
    }
    
    private void loadPond() {
        Node pond = (Node) assetManager.loadModel("Models/Pond/pond.mesh.j3o"); 
        pond.setName("pond");
        pond.addControl(new RigidBodyControl(0));
        bullet.getPhysicsSpace().add(pond);
        worldMap.attachChild(pond);
        
        //Add offmesh connection
        Node pond_offmesh = (Node) assetManager.loadModel("Models/Pond/offmesh/pond_offmesh.mesh.j3o");
        offMeshCon.attachChild(pond_offmesh);
    }

    private void loadPondSurface() {
        Node surface = (Node) assetManager.loadModel("Models/Pond/Water/water_surface.mesh.j3o");
        surface.setName("water");
        Vector3f pos = surface.getLocalTranslation();
        surface.setLocalTranslation(pos.x, 4f, pos.z);
        
        surface.addControl(new RigidBodyControl(0));
        bullet.getPhysicsSpace().add(surface);
        worldMap.attachChild(surface);
        
        //Add water offmesh connection.
        Node water_offmesh = (Node) assetManager.loadModel("Models/Pond/Water/offmesh/water_offmesh.mesh.j3o");
        water_offmesh.setLocalTranslation(surface.getLocalTranslation());
        offMeshCon.attachChild(water_offmesh);
    }
    
    private void loadCrate() {
        Node crate = (Node) assetManager.loadModel("Models/Crate/crate.mesh.j3o");
        crate.setName("crate");
        crate.setLocalTranslation(4.0f, 0.0f, 0.0f);
        
        crate.addControl(new RigidBodyControl(0));
        bullet.getPhysicsSpace().add(crate);
        worldMap.attachChild(crate);
        
        //Add crate offmesh connection.
        Node crate_offmesh = (Node) assetManager.loadModel("Models/Crate/offmesh/crate_offmesh.mesh.j3o");
        crate_offmesh.setLocalTranslation(crate.getLocalTranslation());
        offMeshCon.attachChild(crate_offmesh);
    }
    
    private WaterFilter setupWater() {
        //Water Filter
        WaterFilter waterPond = new WaterFilter(rootNode, new Vector3f(0.5f, -0.5f, -0.5f));

        //foam
        waterPond.setUseFoam(false);
        waterPond.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
        waterPond.setFoamIntensity(0.4f);
        waterPond.setFoamHardness(0.3f);
        waterPond.setFoamExistence(new Vector3f(0.8f, 8f, 1f));
        //light reflection
        waterPond.setReflectionDisplace(50);
        waterPond.setRefractionConstant(0.25f);
        waterPond.setRefractionStrength(0.2f);
        //water color
        waterPond.setColorExtinction(new Vector3f(30, 50, 70));
        waterPond.setWaterColor(new ColorRGBA().setAsSrgb(0.0078f, 0.3176f, 0.5f, 1.0f));
        waterPond.setDeepWaterColor(new ColorRGBA().setAsSrgb(0.0039f, 0.00196f, 0.145f, 1.0f));
        waterPond.setWaterTransparency(0.12f);
        //underwater
        waterPond.setCausticsIntensity(0.4f);
        waterPond.setUnderWaterFogDistance(80);
        //waves
        waterPond.setUseRipples(true);
        waterPond.setSpeed(0.75f);
        waterPond.setWaterHeight(-.1f);
        waterPond.setMaxAmplitude(0.3f);
        waterPond.setWaveScale(0.008f);
        //translation and shorline
        waterPond.setCenter(new Vector3f(-7.6f, -1f, 0));
        waterPond.setRadius(6.75f);
        waterPond.setShapeType(WaterFilter.AreaShape.Circular);
        
        return waterPond;
    }

}
