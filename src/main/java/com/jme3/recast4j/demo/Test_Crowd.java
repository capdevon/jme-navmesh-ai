package com.jme3.recast4j.demo;

import com.jme3.app.ChaseCameraAppState;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.recast4j.Detour.Crowd.CrowdManagerAppState;
import com.jme3.recast4j.demo.states.CrowdState;
import com.jme3.recast4j.demo.states.LemurConfigState;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;

/**
 * 
 * @author capdevon
 */
public class Test_Crowd extends SimpleApplication {

    private BulletAppState bullet;

    public Test_Crowd() {
        super(new StatsAppState(),
            new AudioListenerState(),
            new DebugKeysAppState(),
            new CrowdManagerAppState(),
            new LemurConfigState(),
            new CrowdState()
        );
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        Test_Crowd app = new Test_Crowd();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("jme3-recast4j - Test_Crowd");
        settings.setResolution(1280, 720);

        app.setSettings(settings);
        app.setPauseOnLostFocus(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initPhysics();
        setupWorld();
        setupCamera();
    }

    private void initPhysics() {
        bullet = new BulletAppState();
        // Performance is better when threading in parallel
        bullet.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bullet);
        bullet.setDebugEnabled(true);
    }

    private void setupWorld() {
        //Set the atmosphere of the world, lights, camera, post processing.
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.2f, -1, -0.3f).normalizeLocal());
        sun.setName("sun");
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.25f, 0.25f, 0.25f, 1));
        ambient.setName("ambient");
        rootNode.addLight(ambient);

        DirectionalLightShadowFilter shadowFilter = new DirectionalLightShadowFilter(assetManager, 4096, 2);
        shadowFilter.setLight(sun);
        shadowFilter.setShadowIntensity(0.4f);
        shadowFilter.setShadowZExtend(256);

        FXAAFilter fxaa = new FXAAFilter();

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(shadowFilter);
        fpp.addFilter(fxaa);
        viewPort.addProcessor(fpp);
    }

    private void setupCamera() {
        // disable the default 1st-person flyCam!
        //flyCam.setEnabled(false);

        Node target = new Node("MainCamera");
        //target.move(0, 1, 0);

        ChaseCameraAppState chaseCam = new ChaseCameraAppState();
        chaseCam.setTarget(target);
        stateManager.attach(chaseCam);
        chaseCam.setInvertHorizontalAxis(true);
        chaseCam.setInvertVerticalAxis(true);
        chaseCam.setZoomSpeed(0.5f);
        chaseCam.setMinDistance(1);
        chaseCam.setMaxDistance(20);
        chaseCam.setDefaultDistance(chaseCam.getMaxDistance());
        chaseCam.setMinVerticalRotation(-FastMath.HALF_PI);
        chaseCam.setRotationSpeed(3);
        chaseCam.setDefaultVerticalRotation(0.3f);
    }

}
