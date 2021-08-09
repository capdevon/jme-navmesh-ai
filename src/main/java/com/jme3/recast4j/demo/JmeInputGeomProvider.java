package com.jme3.recast4j.demo;

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

import com.jme3.recast4j.Recast.OffMeshLink;

/**
 *
 * @author Robert
 */
public class JmeInputGeomProvider implements InputGeomProvider {
	
    public final float[] vertices;
    public final int[] faces;
    public final float[] normals;

    final float[] bmin;
    final float[] bmax;
    final List<ConvexVolume> convexVolumes = new ArrayList<>();
    final List<OffMeshLink> offMeshConnections = new ArrayList<>();
    final List<Modification> listModifications = new ArrayList<>();

    /**
     * Constructor.
     * 
     * @param vertices
     * @param faces
     */
    public JmeInputGeomProvider(float[] vertices, int[] faces) {
        this.vertices = vertices;
        this.faces = faces;
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

    @Override
    public Iterable<TriMesh> meshes() {
        return Collections.singletonList(new TriMesh(vertices, faces));
    }

    public void calculateNormals() {
        for (int i = 0; i < faces.length; i += 3) {
            int v0 = faces[i] * 3;
            int v1 = faces[i + 1] * 3;
            int v2 = faces[i + 2] * 3;
            float[] e0 = new float[3], e1 = new float[3];
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
    
    public List<Modification> getListMods() {
        return this.listModifications;
    }

    public void addMod(Modification mod) {
        this.listModifications.add(mod);
    }

//    public void addOffMeshCon(float[] start, float[] end, float radius, int bidir, int id) {
//        org.recast4j.detour.OffMeshConnection con = new org.recast4j.detour.OffMeshConnection();
//        float[] pos = new float[6];
//        System.arraycopy(start, 0, pos, 0, start.length);
//        System.arraycopy(end, 0, pos, 3, end.length);
//        con.pos = pos;
//        con.rad = radius;
//        con.flags = bidir;
//        con.userId = id;
//        offMeshConnections.add(con);
//    }
    
    public List<OffMeshLink> getOffMeshConnections() {
        return offMeshConnections;
    }

    public void addOffMeshConnection(OffMeshLink link) {
        offMeshConnections.add(link);
    }

    public void removeOffMeshConnections(Predicate<OffMeshLink> filter) {
        offMeshConnections.retainAll(offMeshConnections.stream().filter(c -> !filter.test(c)).collect(Collectors.toList()));
    }
}
