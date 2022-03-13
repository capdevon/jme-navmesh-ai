package com.jme3.recast4j.Recast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteOrder;

import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.Poly;
import org.recast4j.detour.io.MeshSetReader;
import org.recast4j.detour.io.MeshSetWriter;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.RecastConstants;

/**
 *
 * @author capdevon
 */
public class IORecast {

    private IORecast() {}

    public static void saveNavMesh(String fileName, NavMesh navMesh) throws IOException {
        File f = new File(fileName);
        saveNavMesh(f, navMesh);
    }

    public static void saveNavMesh(File file, NavMesh navMesh) throws IOException {
        boolean cCompatibility = false;
        MeshSetWriter msw = new MeshSetWriter();
        msw.write(new FileOutputStream(file), navMesh, ByteOrder.BIG_ENDIAN, cCompatibility);
    }

    public static NavMesh loadNavMesh(String fileName) throws IOException {
        File f = new File(fileName);
        return loadNavMesh(f);
    }

    public static NavMesh loadNavMesh(File file) throws IOException {
        int maxVertsPerPoly = 3;
        MeshSetReader msr = new MeshSetReader();
        NavMesh navMesh = msr.read(new FileInputStream(file), maxVertsPerPoly);
        return navMesh;
    }

    /**
     * Save NavMesh as .obj file
     *
     * @param fileName
     * @param navMesh
     */
    public static void saveObj(String fileName, NavMesh navMesh) {
        try {
            FileWriter fw = new FileWriter(new File(fileName));

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

            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save PolyMesh as .obj file
     *
     * @param fileName
     * @param mesh
     */
    public static void saveObj(String fileName, PolyMesh mesh) {
        try {
            FileWriter fw = new FileWriter(new File(fileName));

            for (int v = 0; v < mesh.nverts; v++) {
                fw.write("v " + (mesh.bmin[0] + mesh.verts[v * 3] * mesh.cs) + " "
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

            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save PolyMeshDetail as .obj file
     *
     * @param fileName
     * @param dmesh
     */
    public static void saveObj(String fileName, PolyMeshDetail dmesh) {
        try {
            FileWriter fw = new FileWriter(new File(fileName));

            for (int v = 0; v < dmesh.nverts; v++) {
                fw.write("v " + dmesh.verts[v * 3] + " "
                        + dmesh.verts[v * 3 + 1] + " "
                        + dmesh.verts[v * 3 + 2] + "\n");
            }

            for (int m = 0; m < dmesh.nmeshes; m++) {
                int vfirst = dmesh.meshes[m * 4];
                int tfirst = dmesh.meshes[m * 4 + 2];
                for (int f = 0; f < dmesh.meshes[m * 4 + 3]; f++) {
                    fw.write("f " + (vfirst + dmesh.tris[(tfirst + f) * 4] + 1) + " "
                            + (vfirst + dmesh.tris[(tfirst + f) * 4 + 1] + 1) + " "
                            + (vfirst + dmesh.tris[(tfirst + f) * 4 + 2] + 1) + "\n");
                }
            }

            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
