package com.jme3.recast4j.Recast;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.Poly;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.RecastConstants;

/**
 * 
 * @author capdevon
 */
public class IORecast {

    private IORecast() {}

    /**
     * 
     * @param fileName
     * @param mesh
     */
    public static void saveObj(String fileName, NavMesh mesh) {
        try {
            File file = Path.of("test-output", fileName).toFile();
            file.getParentFile().mkdirs();
            System.out.println("Saving NavMesh=" + file.getAbsolutePath());

            FileWriter fw = new FileWriter(file);
            for (int i = 0; i < mesh.getTileCount(); i++) {
                MeshTile tile = mesh.getTile(i);
                if (tile != null) {
                    for (int v = 0; v < tile.data.header.vertCount; v++) {
                        fw.write("v " +
                            tile.data.verts[v * 3] + " " +
                            tile.data.verts[v * 3 + 1] + " " +
                            tile.data.verts[v * 3 + 2] + "\n");
                    }
                }
            }

            int vertexOffset = 1;
            for (int i = 0; i < mesh.getTileCount(); i++) {
                MeshTile tile = mesh.getTile(i);
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
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param fileName
     * @param mesh
     */
    public static void saveObj(String fileName, PolyMesh mesh) {
        try {
            File file = Path.of("test-output", fileName).toFile();
            file.getParentFile().mkdirs();
            System.out.println("Saving PolyMesh=" + file.getAbsolutePath());

            FileWriter fw = new FileWriter(file);
            for (int v = 0; v < mesh.nverts; v++) {
                fw.write("v " + (mesh.bmin[0] + mesh.verts[v * 3] * mesh.cs) + " " +
                    (mesh.bmin[1] + mesh.verts[v * 3 + 1] * mesh.ch) + " " +
                    (mesh.bmin[2] + mesh.verts[v * 3 + 2] * mesh.cs) + "\n");
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
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param fileName
     * @param dmesh
     */
    public static void saveObj(String fileName, PolyMeshDetail dmesh) {
        try {
            File file = Path.of("test-output", fileName).toFile();
            file.getParentFile().mkdirs();
            System.out.println("Saving PolyMeshDetail=" + file.getAbsolutePath());

            FileWriter fw = new FileWriter(file);
            for (int v = 0; v < dmesh.nverts; v++) {
                fw.write("v " + dmesh.verts[v * 3] + " " +
                    dmesh.verts[v * 3 + 1] + " " +
                    dmesh.verts[v * 3 + 2] + "\n");
            }

            for (int m = 0; m < dmesh.nmeshes; m++) {
                int vfirst = dmesh.meshes[m * 4];
                int tfirst = dmesh.meshes[m * 4 + 2];
                for (int f = 0; f < dmesh.meshes[m * 4 + 3]; f++) {
                    fw.write("f " + (vfirst + dmesh.tris[(tfirst + f) * 4] + 1) + " " +
                        (vfirst + dmesh.tris[(tfirst + f) * 4 + 1] + 1) + " " +
                        (vfirst + dmesh.tris[(tfirst + f) * 4 + 2] + 1) + "\n");
                }
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
