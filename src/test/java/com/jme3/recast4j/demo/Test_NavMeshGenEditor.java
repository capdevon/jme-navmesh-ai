package com.jme3.recast4j.demo;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.recast4j.editor.NavMeshGenState;
import com.jme3.recast4j.editor.NavMeshEditorState;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;

/**
 *
 * @author capdevon
 */
public class Test_NavMeshGenEditor extends SimpleApplication {

    public static void main(String[] args) {
        Test_NavMeshGenEditor app = new Test_NavMeshGenEditor();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("NavMeshEditorApp");
        settings.setResolution(1280, 720);
        settings.setFrameRate(60);

        app.setSettings(settings);
        app.setPauseOnLostFocus(false);
        app.setShowSettings(false);
        app.start();
    }

    private Node scene;

    @Override
    public void simpleInitApp() {

        setDisplayStatView(false);
        configureCamera();
        loadScene();
        addLighting();

        stateManager.attach(new NavMeshGenState(scene));
        stateManager.attach(new NavMeshEditorState());
    }

    private void configureCamera() {
        flyCam.setMoveSpeed(20f);
        flyCam.setDragToRotate(true);

        cam.setLocation(Vector3f.UNIT_XYZ.mult(10));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        cam.setFrustumPerspective(45, (float) cam.getWidth() / cam.getHeight(), 0.1f, 1000f);
    }

    private void loadScene() {
        scene = (Node) assetManager.loadModel("Models/Level/recast_level.mesh.j3o");
        scene.setName("MainScene");
        scene.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(scene);
    }

    private void addLighting() {
        //Set the atmosphere of the world, lights, post processing.
        ColorRGBA skyColor = new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f);
        viewPort.setBackgroundColor(skyColor);

        AmbientLight ambient = new AmbientLight();
        rootNode.addLight(ambient);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.2f, -1, -0.3f).normalizeLocal());
        rootNode.addLight(dl);

        // Render shadows based on the directional light.
        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 2_048, 3);
        dlsf.setLight(dl);
        dlsf.setShadowIntensity(0.4f);
        dlsf.setShadowZExtend(256);
        dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);
        fpp.addFilter(new FXAAFilter());
        viewPort.addProcessor(fpp);
    }

}
