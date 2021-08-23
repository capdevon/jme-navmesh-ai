/*
 *  MIT License
 *  Copyright (c) 2018 MeFisto94
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package com.jme3.recast4j.editor;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;

/**
 * 
 * @author capdevon
 */
public class NavMeshEditorApp extends SimpleApplication {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
    	NavMeshEditorApp app = new NavMeshEditorApp();
    	AppSettings settings = new AppSettings(true);
        settings.setTitle("NavMeshEditorApp");
        settings.setResolution(1280, 720);

        app.setSettings(settings);
        app.setPauseOnLostFocus(false);
        app.start();
    }
    
    private Node worldMap;

	@Override
	public void simpleInitApp() {
		
		cam.setFrustumPerspective(45, (float) cam.getWidth() / cam.getHeight(), 0.1f, 1000f);
		
		flyCam.setMoveSpeed(20f);
		flyCam.setDragToRotate(true);
		
		cam.setLocation(Vector3f.UNIT_XYZ.mult(10));
		cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
	    
		setupWorld();
		
		stateManager.attach(new NavMeshGeneratorState(worldMap));
		stateManager.attach(new NavMeshUI());
	}
	
	private void setupWorld() {
    	//Set the atmosphere of the world, lights, camera, post processing.
    	viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));
    	
    	worldMap = new Node("MainScene");
        worldMap.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        
        Node level = (Node) assetManager.loadModel("Models/Level/recast_level.mesh.j3o"); 
        worldMap.attachChild(level);
        
        rootNode.attachChild(worldMap);

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

}