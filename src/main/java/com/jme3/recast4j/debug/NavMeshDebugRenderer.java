package com.jme3.recast4j.debug;

//import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_DOOR;
//import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_GRASS;
//import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_GROUND;
//import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_JUMP;
//import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_ROAD;
//import static com.jme3.recast4j.demo.JmeAreaMods.POLYAREA_TYPE_WATER;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.recast4j.detour.MeshData;
import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.Poly;
import org.recast4j.recast.geom.InputGeomProvider;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.detour.DetourUtils;
import com.jme3.recast4j.recast.utils.RecastUtils;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.debug.WireBox;
import com.jme3.util.BufferUtils;

/**
 * 
 * @author capdevon
 */
public class NavMeshDebugViewer {
	
    // Asset manager
    protected AssetManager assetManager;
    // Node for attaching debug geometries
    public Node debugNode = new Node("NavMeshDebugViewer");

    public NavMeshDebugViewer(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
	
    public void clear() {
        debugNode.detachAllChildren();
    }
	
    /**
     * Render all the debug geometries to the specified view port.
     */
    public void show(RenderManager rm, ViewPort vp) {
        debugNode.updateLogicalState(0f);
        debugNode.updateGeometricState();
        rm.renderScene(debugNode, vp);
    }
    
    public void drawMeshBounds(InputGeomProvider geomProvider) {
    	drawMeshBounds(geomProvider.getMeshBoundsMin(), geomProvider.getMeshBoundsMax());
    }
    
    public void drawMeshBounds(NavMesh mesh) {
    	float[][] meshBounds = getNavMeshBounds(mesh);
    	drawMeshBounds(meshBounds[0], meshBounds[1]);
    }
    
    protected void drawMeshBounds(float[] bmin, float[] bmax) {
        Vector3f min = DetourUtils.toVector3f(bmin);
        Vector3f max = DetourUtils.toVector3f(bmax);

        BoundingBox bbox = new BoundingBox(min, max);
        Geometry geo = WireBox.makeGeometry(bbox);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        geo.setMaterial(mat);
        geo.setShadowMode(ShadowMode.Off);
        debugNode.attachChild(geo);
    }

    protected float[][] getNavMeshBounds(NavMesh mesh) {
        float[] bmin = new float[] { Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY };
        float[] bmax = new float[] { Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY };
        for (int t = 0; t < mesh.getMaxTiles(); ++t) {
            MeshTile tile = mesh.getTile(t);
            if (tile != null && tile.data != null) {
                for (int i = 0; i < tile.data.verts.length; i += 3) {
                    bmin[0] = Math.min(bmin[0], tile.data.verts[i]);
                    bmin[1] = Math.min(bmin[1], tile.data.verts[i + 1]);
                    bmin[2] = Math.min(bmin[2], tile.data.verts[i + 2]);
                    bmax[0] = Math.max(bmax[0], tile.data.verts[i]);
                    bmax[1] = Math.max(bmax[1], tile.data.verts[i + 1]);
                    bmax[2] = Math.max(bmax[2], tile.data.verts[i + 2]);
                }
            }
        }
        return new float[][] { bmin, bmax };
    }
    
    public void drawNavMesh(NavMesh navMesh, boolean wireframe) {
        int maxTiles = navMesh.getMaxTiles();
        for (int i = 0; i < maxTiles; i++) {
            MeshTile tile = navMesh.getTile(i);
            MeshData meshData = tile.data;
            if (meshData != null) {
                drawMeshData(meshData, wireframe);
            }
        }
    }
    
    public void drawNavMeshByArea(NavMesh navMesh, boolean wireframe) {
        int maxTiles = navMesh.getMaxTiles();
        for (int i = 0; i < maxTiles; i++) {
            MeshTile tile = navMesh.getTile(i);
            MeshData meshData = tile.data;
            if (meshData != null) {
                drawMeshByArea(meshData, wireframe);
            }
        }
    }

    /**
     * Displays a debug mesh
     * 
     * @param meshData  MeshData to process.
     * @param wireframe display as solid or wire frame.
     */
    public void drawMeshData(MeshData meshData, boolean wireframe) {

        Geometry dgeom = new Geometry("DebugMeshDetailed", RecastUtils.getDebugMesh(meshData.detailMeshes, meshData.detailVerts, meshData.detailTris));
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Green);
        dgeom.setMaterial(mat1);
        dgeom.move(0, 0.25f, 0);

        Geometry sgeom = new Geometry("DebugMeshSimple", RecastUtils.getDebugMesh(meshData));
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Cyan);
        mat2.getAdditionalRenderState().setWireframe(wireframe);
        sgeom.setMaterial(mat2);
        sgeom.move(0, 0.125f, 0);
        
        sgeom.setShadowMode(ShadowMode.Off);
        dgeom.setShadowMode(ShadowMode.Off);

        debugNode.attachChild(sgeom);
        debugNode.attachChild(dgeom);
    }

    /**
     * Displays a debug mesh based off the area type of the poly.
     * 
     * @param meshData  MeshData to process.
     * @param wireframe display as solid or wire frame.
     */
    public void drawMeshByArea(MeshData meshData, boolean wireframe) {
        //sortVertsByArea(meshData, POLYAREA_TYPE_GROUND, wireframe);
        //sortVertsByArea(meshData, POLYAREA_TYPE_WATER, wireframe);
        //sortVertsByArea(meshData, POLYAREA_TYPE_ROAD, wireframe);
        //sortVertsByArea(meshData, POLYAREA_TYPE_DOOR, wireframe);
        //sortVertsByArea(meshData, POLYAREA_TYPE_GRASS, wireframe);
        //sortVertsByArea(meshData, POLYAREA_TYPE_JUMP, wireframe);

        Set<Integer> areaTypes = new HashSet<>();
        for (Poly poly : meshData.polys) {
            areaTypes.add(poly.getArea());
        }

        areaTypes.forEach(t -> sortVertsByArea(meshData, t, wireframe));
    }

    /**
     * Sorts the vertices of MeshData, based off the area type of a polygon, and
     * creates one mesh with geometry and material and adds it to the debug node.
     * 
     * @param meshData  MeshData to parse.
     * @param areaType  The are type to sort the vertices by.
     * @param wireframe Display mesh as solid or wire frame.
     */
    private void sortVertsByArea(MeshData meshData, int areaType, boolean wireframe) {

        ArrayList<Float> listVerts = new ArrayList<>();

        /**
         * If the poly area type equals the supplied area type, add vertices to
         * listVerts.
         */
        for (Poly poly : meshData.polys) {
            if (poly.getArea() == areaType) {
                for (int idx : poly.verts) {
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
            ColorRGBA areaColor = getAreaColor(areaType);
            for (int i = 0; i < indexes.length; i++) {
                colorArray[colorIndex++] = areaColor.getRed();
                colorArray[colorIndex++] = areaColor.getGreen();
                colorArray[colorIndex++] = areaColor.getBlue();
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
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setBoolean("VertexColor", true);

            // Set wireframe or solid.
            mat.getAdditionalRenderState().setWireframe(wireframe);
            geo.setMaterial(mat);
            // Move to just above surface.
            geo.move(0, 0.125f, 0);
            geo.setShadowMode(ShadowMode.Off);

            // Add to root node.
            debugNode.attachChild(geo);
        }
    }

    /**
     * Creates a color based off the area type.
     * 
     * @param area The area color desired.
     * @return A ColorRGBA based off the supplied area type.
     */
//    private ColorRGBA getAreaColor(int area) {
//
//        switch (area) {
//            // Ground (1): light blue
//            case POLYAREA_TYPE_GROUND:
//                return new ColorRGBA(0.0f, 0.75f, 1.0f, 1.0f);
//            // Water (2): blue
//            case POLYAREA_TYPE_WATER:
//                return ColorRGBA.Blue;
//            // Road (3): brown
//            case POLYAREA_TYPE_ROAD:
//                return new ColorRGBA(0.2f, 0.08f, 0.05f, 1);
//            // Door (4): magenta
//            case POLYAREA_TYPE_DOOR:
//                return ColorRGBA.Magenta;
//            // Grass (5): green
//            case POLYAREA_TYPE_GRASS:
//                return ColorRGBA.Green;
//            // Jump (6): yellow
//            case POLYAREA_TYPE_JUMP:
//                return ColorRGBA.Yellow;
//            // Unexpected : red
//            default:
//                return ColorRGBA.Red;
//        }
//    }
    
    private int bit(int a, int b) {
        return (a & (1 << b)) >> b;
    }

    private ColorRGBA getAreaColor(int i) {
        if (i == 0)
            return new ColorRGBA(0, 0.75f, 1.0f, 0.5f);

        int r = (bit(i, 4) + bit(i, 1) * 2 + 1) * 63;
        int g = (bit(i, 3) + bit(i, 2) * 2 + 1) * 63;
        int b = (bit(i, 5) + bit(i, 0) * 2 + 1) * 63;
        return new ColorRGBA((float) r / 255.0f, (float) g / 255.0f, (float) b / 255.0f, 0.5f);
    }

}
