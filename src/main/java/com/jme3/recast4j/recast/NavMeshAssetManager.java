package com.jme3.recast4j.recast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteOrder;

import org.recast4j.detour.MeshData;
import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.Poly;
import org.recast4j.detour.io.MeshDataWriter;
import org.recast4j.detour.io.MeshSetReader;
import org.recast4j.detour.io.MeshSetWriter;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.RecastConstants;

/**
 *
 * @author capdevon
 */
public class NavMeshAssetManager {
    
    private static final boolean cCompatibility = false;
    private static final int maxVertsPerPoly = 3;

    private NavMeshAssetManager() {}

    /**
     * 
     * @param mesh
     * @param f
     * @throws IOException
     */
    public static void save(MeshData mesh, File f) throws IOException {
        MeshDataWriter writer = new MeshDataWriter();
        writer.write(new FileOutputStream(f), mesh, ByteOrder.BIG_ENDIAN, cCompatibility);
    }
    
    /**
     * 
     * @param navMesh
     * @param f
     * @throws IOException
     */
    public static void save(NavMesh navMesh, File f) throws IOException {
        MeshSetWriter writer = new MeshSetWriter();
        writer.write(new FileOutputStream(f), navMesh, ByteOrder.BIG_ENDIAN, cCompatibility);
    }

    /**
     * 
     * @param f
     * @return
     * @throws IOException
     */
    public static NavMesh load(File f) throws IOException {
        MeshSetReader reader = new MeshSetReader();
        NavMesh navMesh = reader.read(new FileInputStream(f), maxVertsPerPoly);
        return navMesh;
    }

    /**
     * Save NavMesh as Wavefront (.obj) file
     * 
     * @param navMesh
     * @param file
     * @throws IOException
     */
    public static void saveAsObj(NavMesh navMesh, File file) throws IOException {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (FileWriter fw = new FileWriter(file)) {

            for (int i = 0; i < navMesh.getTileCount(); i++) {
                MeshTile tile = navMesh.getTile(i);
                if (tile != null) {
                    for (int v = 0; v < tile.data.header.vertCount; v++) {
                        fw.write("v "
                                + tile.data.verts[v * 3] + " "
                                + tile.data.verts[v * 3 + 1] + " "
                                + tile.data.verts[v * 3 + 2] + "\n");
                    }
                }
            }

            int vertexOffset = 1;
            for (int i = 0; i < navMesh.getTileCount(); i++) {
                MeshTile tile = navMesh.getTile(i);
                if (tile != null) {
                    for (int p = 0; p < tile.data.header.polyCount; p++) {
                        fw.write("f ");
                        Poly poly = tile.data.polys[p];
                        for (int v = 0; v < poly.vertCount; v++) {
                            fw.write(poly.verts[v] + vertexOffset + " ");
                        }
                        fw.write("\n");
                    }
                    vertexOffset += tile.data.header.vertCount;
                }
            }
        }
    }

    /**
     * Save PolyMesh as Wavefront (.obj) file
     * 
     * @param mesh
     * @param file
     * @throws IOException
     */
    public static void saveAsObj(PolyMesh mesh, File file) throws IOException {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter fw = new FileWriter(file)) {

            for (int v = 0; v < mesh.nverts; v++) {
                fw.write("v " 
                        + (mesh.bmin[0] + mesh.verts[v * 3] * mesh.cs) + " "
                        + (mesh.bmin[1] + mesh.verts[v * 3 + 1] * mesh.ch) + " "
                        + (mesh.bmin[2] + mesh.verts[v * 3 + 2] * mesh.cs) + "\n");
            }

            for (int i = 0; i < mesh.npolys; i++) {
                int p = i * mesh.nvp * 2;
                fw.write("f ");
                for (int j = 0; j < mesh.nvp; ++j) {
                    int v = mesh.polys[p + j];
                    if (v == RecastConstants.RC_MESH_NULL_IDX) {
                        break;
                    }
                    fw.write((v + 1) + " ");
                }
                fw.write("\n");
            }
        }
    }

    /**
     * Save PolyMeshDetail as Wavefront (.obj) file
     * 
     * @param mesh
     * @param file
     * @throws IOException
     */
    public static void saveAsObj(PolyMeshDetail mesh, File file) throws IOException {

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter fw = new FileWriter(file)) {

            for (int v = 0; v < mesh.nverts; v++) {
                fw.write("v " 
                        + mesh.verts[v * 3] + " " 
                        + mesh.verts[v * 3 + 1] + " " 
                        + mesh.verts[v * 3 + 2] + "\n");
            }

            for (int i = 0; i < mesh.nmeshes; i++) {
                int vfirst = mesh.meshes[i * 4];
                int tfirst = mesh.meshes[i * 4 + 2];
                for (int f = 0; f < mesh.meshes[i * 4 + 3]; f++) {
                    fw.write("f " 
                            + (vfirst + mesh.tris[(tfirst + f) * 4] + 1) + " "
                            + (vfirst + mesh.tris[(tfirst + f) * 4 + 1] + 1) + " "
                            + (vfirst + mesh.tris[(tfirst + f) * 4 + 2] + 1) + "\n");
                }
            }
        }
    }

}
