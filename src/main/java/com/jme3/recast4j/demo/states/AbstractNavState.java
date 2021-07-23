package com.jme3.recast4j.demo.states;

import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_DOOR;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_GRASS;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_GROUND;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_JUMP;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_ROAD;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYAREA_TYPE_WATER;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.recast4j.detour.MeshData;
import org.recast4j.detour.Poly;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.recast4j.Recast.Utils.RecastUtils;
import com.jme3.recast4j.demo.utils.PathViewer;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.system.AppSettings;
import com.jme3.util.BufferUtils;

/**
 * 
 * @author capdevon
 */
public abstract class AbstractNavState extends BaseAppState {

    public PathViewer pathViewer;

    // cache fields
    public SimpleApplication app;
    public AppSettings settings;
    public AppStateManager stateManager;
    public AssetManager assetManager;
    public InputManager inputManager;
    public ViewPort viewPort;
    public Camera camera;
    public Node rootNode;
    public Node guiNode;

    protected void refreshCacheFields() {
        this.app 			= (SimpleApplication) getApplication();
        this.settings 		= app.getContext().getSettings();
        this.stateManager 	= app.getStateManager();
        this.assetManager 	= app.getAssetManager();
        this.inputManager 	= app.getInputManager();
        this.viewPort 		= app.getViewPort();
        this.camera 		= app.getCamera();
        this.rootNode 		= app.getRootNode();
        this.guiNode 		= app.getGuiNode();
    }

    protected PhysicsSpace getPhysicsSpace() {
        return getState(BulletAppState.class).getPhysicsSpace();
    }

    @Override
    protected void initialize(Application app) {
        // TODO Auto-generated method stub
        refreshCacheFields();
        pathViewer = new PathViewer(assetManager);
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

    @Override
    public void render(RenderManager rm) {
        pathViewer.show(rm, viewPort);
    }

    protected void showDebugMeshes(MeshData meshData, boolean wireframe) {
        Material matRed = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matRed.setColor("Color", ColorRGBA.Red);
        if (wireframe) {
            matRed.getAdditionalRenderState().setWireframe(true);
        }

        Material matGreen = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matGreen.setColor("Color", ColorRGBA.Green);

        // navMesh.getTile(0).data == meshData (in this particular case)
        Geometry gDetailed = new Geometry("DebugMeshDetailed", RecastUtils.getDebugMesh(meshData.detailMeshes, meshData.detailVerts, meshData.detailTris));
        Geometry g = new Geometry("DebugMeshSimple", RecastUtils.getDebugMesh(meshData));
        g.setMaterial(matRed);
        gDetailed.setMaterial(matGreen);
        //		System.out.println("VertCount Regular Mesh: " + g.getVertexCount());
        //		System.out.println("VertCount Detailed Mesh: " + gDetailed.getVertexCount());
        g.move(0, 0.125f, 0);
        gDetailed.move(0, 0.25f, 0);

        rootNode.attachChild(g);
        rootNode.attachChild(gDetailed);
    }

    /**
     * Displays a debug mesh based off the area type of the poly.
     * 
     * @param meshData MeshData to process.
     * @param wireFrame display as solid or wire frame. 
     */
    protected void showDebugByArea(MeshData meshData, boolean wireFrame) {
        sortVertsByArea(meshData, POLYAREA_TYPE_GROUND, wireFrame);
        sortVertsByArea(meshData, POLYAREA_TYPE_WATER, wireFrame);
        sortVertsByArea(meshData, POLYAREA_TYPE_ROAD, wireFrame);
        sortVertsByArea(meshData, POLYAREA_TYPE_DOOR, wireFrame);
        sortVertsByArea(meshData, POLYAREA_TYPE_GRASS, wireFrame);
        sortVertsByArea(meshData, POLYAREA_TYPE_JUMP, wireFrame);
    }

    /**
     * Sorts the vertices of MeshData, based off the area type of a polygon, and 
     * creates one mesh with geometry and material and adds it to the root node.
     * 
     * @param meshData MeshData to parse.
     * @param areaType The are type to sort the vertices by.
     * @param wireFrame Display mesh as solid or wire frame.
     */
    protected void sortVertsByArea(MeshData meshData, int areaType, boolean wireFrame) {

        ArrayList<Float> listVerts = new ArrayList<>();

        /**
         * If the poly area type equals the supplied area type, add vertice to
         * listVerts.
         */
        for (Poly p : meshData.polys) {
            if (p.getArea() == areaType) {
                for (int idx: p.verts) {
                    // Triangle so idx + 0-2.
                    float vertX = meshData.verts[idx * 3];
                    listVerts.add(vertX);
                    float vertY = meshData.verts[idx * 3 + 1];
                    listVerts.add(vertY);
                    float vertZ = meshData.verts[idx * 3 + 2];
                    listVerts.add(vertZ);
                }
            }
        }

        // If the list is empty, do nothing.
        if (!listVerts.isEmpty()) {
            // Prepare to add found verts from listVerts.
            float[] verts = new float[listVerts.size()];

            // Populate the verts array.
            for (int i = 0; i < verts.length; i++) {
                verts[i] = listVerts.get(i);
            }

            // Create the mesh FloatBuffer.
            FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(verts);

            /**
             * As always, there are three vertices per index so set size accordingly.
             */
            int[] indexes = new int[verts.length / 3];

            /**
             * Since we populated the listVerts by order found, indices will be in order
             * from 0 to verts.length -1.
             */
            for (int i = 0; i < indexes.length; i++) {
                indexes[i] = i;
            }

            // Create the index buffer.
            IntBuffer indexBuffer = BufferUtils.createIntBuffer(indexes);
            // Prepare to set vertex colors based off area type.
            int colorIndex = 0;
            // Create the float array for the color buffer.
            float[] colorArray = new float[indexes.length * 4];

            // Populate the colorArray based off area type.
            for (int i = 0; i < indexes.length; i++) {
                colorArray[colorIndex++] = areaToColorRGBA(areaType).getRed();
                colorArray[colorIndex++] = areaToColorRGBA(areaType).getGreen();
                colorArray[colorIndex++] = areaToColorRGBA(areaType).getBlue();
                colorArray[colorIndex++] = 1.0f;
            }

            // Set the buffers for the mesh.
            Mesh mesh = new Mesh();
            mesh.setBuffer(VertexBuffer.Type.Position, 3, floatBuffer);
            mesh.setBuffer(VertexBuffer.Type.Index, 3, indexBuffer);
            mesh.setBuffer(VertexBuffer.Type.Color, 4, colorArray);
            mesh.updateBound();

            // Build the geometry for the mesh.
            Geometry geo = new Geometry("ColoredMesh", mesh);
            Material mat = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setBoolean("VertexColor", true);

            // Set wireframe or solid.
            mat.getAdditionalRenderState().setWireframe(wireFrame);
            geo.setMaterial(mat);
            // Move to just above surface.
            geo.move(0, 0.125f, 0);

            // Add to root node.
            rootNode.attachChild(geo);
        }
    }

    /**
     * Creates a color based off the area type.
     * 
     * @param area The area color desired.
     * @return A RGBA color based off the supplied area type.
     */
    protected ColorRGBA areaToColorRGBA(int area) {

        //Ground (1): light blue
        if (area == POLYAREA_TYPE_GROUND) {
            return new ColorRGBA(0.0f, 0.75f, 1.0f, 1.0f);
        }
        //Water (2): blue
        else if (area == POLYAREA_TYPE_WATER) {
            return ColorRGBA.Blue;
        }
        //Road (3): brown
        else if (area == POLYAREA_TYPE_ROAD) {
            return new ColorRGBA(0.2f, 0.08f, 0.05f, 1);
        }
        //Door (4): cyan
        else if (area == POLYAREA_TYPE_DOOR) {
            return ColorRGBA.Magenta;
        }
        //Grass (5): green
        else if (area == POLYAREA_TYPE_GRASS) {
            return ColorRGBA.Green;
        }
        //Jump (6): yellow
        else if (area == POLYAREA_TYPE_JUMP) {
            return ColorRGBA.Yellow;
        }
        //Unexpected : red
        else {
            return ColorRGBA.Red;
        }
    }

}
