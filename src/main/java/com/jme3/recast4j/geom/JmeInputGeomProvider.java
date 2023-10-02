package com.jme3.recast4j.geom;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.recast4j.recast.AreaModification;
import org.recast4j.recast.ConvexVolume;
import org.recast4j.recast.RecastVectors;
import org.recast4j.recast.geom.InputGeomProvider;
import org.recast4j.recast.geom.TriMesh;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import jme3tools.optimize.GeometryBatchFactory;

/**
 *
 * @author capdevon
 */
public class JmeInputGeomProvider implements InputGeomProvider {
	
    private final float[] vertices;
    private final int[] faces;
    private final float[] normals;

    private final float[] bmin;
    private final float[] bmax;
    private final List<ConvexVolume> convexVolumes = new ArrayList<>();
    private final List<OffMeshLink> offMeshConnections = new ArrayList<>();
    private final List<NavMeshModifier> listModifications = new ArrayList<>();

    /**
     * 
     * @param geometries
     */
    public JmeInputGeomProvider(List<Geometry> geometries) {
        Mesh mesh = new Mesh();
        GeometryBatchFactory.mergeGeometries(geometries, mesh);
        
        vertices = getVertices(mesh);
        faces = getIndices(mesh);
        normals = new float[faces.length];
        calculateNormals();
        
        bmin = new float[3];
        bmax = new float[3];
        RecastVectors.copy(bmin, vertices, 0);
        RecastVectors.copy(bmax, vertices, 0);
        for (int i = 1; i < vertices.length / 3; i++) {
            RecastVectors.min(bmin, vertices, i * 3);
            RecastVectors.max(bmax, vertices, i * 3);
        }
    }

    @Override
    public float[] getMeshBoundsMin() {
        return bmin;
    }

    @Override
    public float[] getMeshBoundsMax() {
        return bmax;
    }
    
    @Override
    public Iterable<TriMesh> meshes() {
        return Collections.singletonList(new TriMesh(vertices, faces));
    }

    @Override
    public List<ConvexVolume> convexVolumes() {
        return convexVolumes;
    }
    
    public void addConvexVolume(float[] verts, float minh, float maxh, AreaModification areaMod) {
        ConvexVolume vol = new ConvexVolume();
        vol.hmin = minh;
        vol.hmax = maxh;
        vol.verts = verts;
        vol.areaMod = areaMod;
        convexVolumes.add(vol);
    }
    
    public void clearConvexVolumes() {
    	convexVolumes.clear();
    }
    
    public List<NavMeshModifier> getModifications() {
        return this.listModifications;
    }

    public void addModification(NavMeshModifier mod) {
        this.listModifications.add(mod);
    }
    
    public List<OffMeshLink> getOffMeshConnections() {
        return offMeshConnections;
    }

    public void addOffMeshConnection(OffMeshLink link) {
        offMeshConnections.add(link);
    }

    public void removeOffMeshConnections(Predicate<OffMeshLink> filter) {
        offMeshConnections.retainAll(offMeshConnections.stream().filter(c -> !filter.test(c)).collect(Collectors.toList()));
    }
    
    private float[] getVertices(Mesh mesh) {
        FloatBuffer buffer = mesh.getFloatBuffer(VertexBuffer.Type.Position);
        return BufferUtils.getFloatArray(buffer);
    }

    private int[] getIndices(Mesh mesh) {
        int[] indices = new int[3];
        int[] triangles = new int[mesh.getTriangleCount() * 3];

        for (int i = 0; i < mesh.getTriangleCount(); i++) {
            mesh.getTriangle(i, indices);
            triangles[3 * i] = indices[0];
            triangles[3 * i + 1] = indices[1];
            triangles[3 * i + 2] = indices[2];
        }
        return triangles;
    }

    private void calculateNormals() {
        for (int i = 0; i < faces.length; i += 3) {
            int v0 = faces[i] * 3;
            int v1 = faces[i + 1] * 3;
            int v2 = faces[i + 2] * 3;
            float[] e0 = new float[3];
            float[] e1 = new float[3];
            for (int j = 0; j < 3; ++j) {
                e0[j] = vertices[v1 + j] - vertices[v0 + j];
                e1[j] = vertices[v2 + j] - vertices[v0 + j];
            }
            normals[i] = e0[1] * e1[2] - e0[2] * e1[1];
            normals[i + 1] = e0[2] * e1[0] - e0[0] * e1[2];
            normals[i + 2] = e0[0] * e1[1] - e0[1] * e1[0];
            float d = (float) Math.sqrt(normals[i] * normals[i] + normals[i + 1] * normals[i + 1] + normals[i + 2] * normals[i + 2]);
            if (d > 0) {
                d = 1.0f / d;
                normals[i] *= d;
                normals[i + 1] *= d;
                normals[i + 2] *= d;
            }
        }
    }
}
