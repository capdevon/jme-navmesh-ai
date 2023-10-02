package com.jme3.recast4j.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.recast4j.detour.NavMesh;
import org.recast4j.detour.io.MeshSetWriter;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.recast4j.debug.NavMeshDebugRenderer;
import com.jme3.recast4j.editor.builder.SoloNavMeshBuilder;
import com.jme3.recast4j.editor.builder.TileNavMeshBuilder;
import com.jme3.recast4j.geom.InputGeomProviderBuilder;
import com.jme3.recast4j.geom.JmeInputGeomProvider;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;

/**
 *
 * @author capdevon
 */
public class NavMeshGeneratorState extends BaseAppState {

    private static final Logger logger = Logger.getLogger(NavMeshGeneratorState.class.getName());

    private final SoloNavMeshBuilder soloNavMeshBuilder = new SoloNavMeshBuilder();
    private final TileNavMeshBuilder tileNavMeshBuilder = new TileNavMeshBuilder();

    private Node worldMap;
    private JmeInputGeomProvider m_geom;
    private NavMeshDebugRenderer nmDebugViewer;
    private ViewPort viewPort;

    /**
     * Constructor.
     *
     * @param worldMap
     */
    public NavMeshGeneratorState(Node worldMap) {
        this.worldMap = worldMap;
    }

    @Override
    protected void initialize(Application app) {
        m_geom = InputGeomProviderBuilder.build(worldMap);
        nmDebugViewer = new NavMeshDebugRenderer(app.getAssetManager());
        viewPort = app.getViewPort();
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

    @Override
    public void render(RenderManager rm) {
        nmDebugViewer.show(rm, viewPort);
    }

    public void generateNavMesh(NavMeshBuildSettings settingsUI) {
        try {
            System.out.println(settingsUI);

            NavMesh navMesh = null;
            long startTime = System.currentTimeMillis();

            if (settingsUI.tiled) {
                navMesh = tileNavMeshBuilder.build(m_geom, settingsUI);
            } else {
                navMesh = soloNavMeshBuilder.build(m_geom, settingsUI);
            }

            long endTime = System.currentTimeMillis() - startTime;
            System.out.println("Build NavMesh succeeded after: " + endTime + " ms");

            nmDebugViewer.clear();
            nmDebugViewer.drawNavMeshByArea(navMesh, true);
            nmDebugViewer.drawMeshBounds(m_geom);

            saveToFile(worldMap.getName(), navMesh);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param fileName
     * @param navMesh
     * @throws IOException
     */
    private void saveToFile(String fileName, NavMesh navMesh) throws IOException {
        File file = Path.of("navmesh-generated", fileName + ".navmesh").toFile();
        file.getParentFile().mkdirs();
        System.out.println("Saving NavMesh=" + file.getAbsolutePath());

        boolean cCompatibility = false;
        MeshSetWriter msw = new MeshSetWriter();
        msw.write(new FileOutputStream(file), navMesh, ByteOrder.BIG_ENDIAN, cCompatibility);
    }

}
