package com.jme3.recast4j.geom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.recast4j.recast.Context;
import org.recast4j.recast.Heightfield;
import org.recast4j.recast.Recast;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastConfig;
import org.recast4j.recast.RecastRasterization;
import org.recast4j.recast.geom.ChunkyTriMesh.ChunkyTriMeshNode;
import org.recast4j.recast.geom.TriMesh;

/**
 * 
 * @author capdevon
 */
public class JmeRecastVoxelization {
	
	/**
	 * 
	 * @param geomProvider
	 * @param builderCfg
	 * @param ctx
	 * @return
	 */
    public static Heightfield buildSolidHeightfield(JmeInputGeomProvider geomProvider, RecastBuilderConfig builderCfg, Context ctx) {
    	
        RecastConfig cfg = builderCfg.cfg;

        // Allocate voxel heightfield where we rasterize our input data to.
        Heightfield solid = new Heightfield(builderCfg.width, builderCfg.height, builderCfg.bmin, builderCfg.bmax, cfg.cs, cfg.ch);

        // Allocate array that can hold triangle area types.
        // If you have multiple meshes you need to process, allocate
        // and array which can hold the max number of triangles you need to
        // process.

        // Find triangles which are walkable based on their slope and rasterize
        // them.
        // If your input data is multiple meshes, you can transform them here,
        // calculate
        // the are type for each of the meshes and rasterize them.
        for (TriMesh geom : geomProvider.meshes()) {
            float[] verts = geom.getVerts();
            int[] tris = geom.getTris();
            int ntris = tris.length / 3;
            boolean tiled = cfg.tileSize > 0;
            
            /**
             * Sort triangle indices into group arrays using the supplied 
             * Modification geometry length so we can mark Area Type.
             * 
             * This listAreaTris will hold a copy of each areas indices as a 
             * separate array.
             * 
             * Each array has a AreaModification that can be accessed from the
             * Modification getMod() method using the index of the array held
             * in listAreaTris.
             */            
            List<int[]> listTriIndices = new ArrayList<>();
            int fromIndex = 0;

            for (NavMeshBuildSource sourceObj : geomProvider.getModifications()) {
                int[] triangles = new int[sourceObj.getGeomLength()];
                System.arraycopy(tris, fromIndex, triangles, 0, sourceObj.getGeomLength());
                listTriIndices.add(triangles);
                fromIndex += sourceObj.getGeomLength();
            }
            
            if (tiled) {
                float[] tbmin = new float[2];
                float[] tbmax = new float[2];
                tbmin[0] = builderCfg.bmin[0];
                tbmin[1] = builderCfg.bmin[2];
                tbmax[0] = builderCfg.bmax[0];
                tbmax[1] = builderCfg.bmax[2];
                
                List<ChunkyTriMeshNode> nodes = geom.getChunksOverlappingRect(tbmin, tbmax);
                
                for (ChunkyTriMeshNode node : nodes) {
                    int[] node_tris = node.tris;
                    int node_ntris = node_tris.length / 3;
                    
                    if (!listTriIndices.isEmpty()) {
                        /**
                         * With listTriIndices we have arrays of indices whose 
                         * position/index in listTriIndices matches the index of 
                         * geomProviders list of Modifications position/index. 
                         * 
                         * We cycle through each nodes triangles, in the same 
                         * order they are found, looking for a matching triangle 
                         * in one of the arrays.
                         *
                         * We mark any found triangles area using the index of 
                         * the array found in listTriIndices and add them to the
                         * list of marked triangles.
                         * 
                         * Last, we merge all marked triangles into one array for 
                         * Rasterization.
                         */
                        List<Integer> listMarkedTris = new ArrayList<>();
                        int[] nodeTri = new int[3];
                        int[] areaTri = new int[3];

                        for (int i = 0; i < node_ntris; i++) {

                            //Create a triangle from the node.
                            nodeTri[0] = node_tris[i*3];
                            nodeTri[1] = node_tris[i*3+1];
                            nodeTri[2] = node_tris[i*3+2];

                            //Cycle through each array.
                            for (int[] areaTris: listTriIndices) {
                                /**
                                 * If no triangle is found in this array of 
                                 * indices we will move onto the next array, and 
                                 * the next, until we find a match.
                                 */
                                boolean found = false;

                                //Cycle through each areas indices.
                                for (int j = 0; j < areaTris.length/3; j++) {

                                    //Create triangle from the array.
                                    areaTri[0] = areaTris[j*3];
                                    areaTri[1] = areaTris[j*3+1];
                                    areaTri[2] = areaTris[j*3+2];

                                    /**
                                     * If we find a matching triangle in this 
                                     * array, we are done.
                                     */
                                    if (Arrays.equals(nodeTri, areaTri)) {
                                        found = true;
                                        break;
                                    }
                                }

                                /**
                                 * We found that nodeTri matched areaTri so mark 
                                 * Area Type which is represented by its 
                                 * areaTris index. 
                                 * 
                                 * This is a single triangle we are passing to 
                                 * markWalkableTriangles. 
                                 * 
                                 * We then break out of the loop to advance to 
                                 * the next node triangle to check. If no match 
                                 * was found for this group, we check the next 
                                 * array and continue the search. 
                                 */
                                if (found) {
                                    //Mark single triangle.
                                    int[] m_triareas = Recast.markWalkableTriangles(
                                            ctx, 
                                            cfg.walkableSlopeAngle, 
                                            verts, 
                                            nodeTri, 
                                            nodeTri.length/3,
                                            geomProvider.getModifications().get(listTriIndices.indexOf(areaTris)).getAreaModification());

                                    /**
                                     * Add marked triangle to the listMarkedTris.
                                     * We passed in a single triangle so there 
                                     * is only one element in the array.
                                     */
                                    listMarkedTris.add(m_triareas[0]);
                                    break;
                                }
                            }
                        }

                        //Prepare a new array to combine all marked triangles.
                        int[] mergeArea = new int[node_ntris];
                        //Copy each marked triangle into the new array.
                        for (int i = 0; i < mergeArea.length; i++) {
                            mergeArea[i] = listMarkedTris.get(i);
                        }   

                        RecastRasterization.rasterizeTriangles(ctx, verts, node_tris, mergeArea, node_ntris, solid, cfg.walkableClimb);
                        
                    } else {
                        //Mark all triangles  with a single AreaModification from cfg.
                        int[] m_triareas = Recast.markWalkableTriangles(ctx, cfg.walkableSlopeAngle, verts, node_tris, node_ntris, cfg.walkableAreaMod);
                        RecastRasterization.rasterizeTriangles(ctx, verts, node_tris, m_triareas, node_ntris, solid, cfg.walkableClimb);
                    }
                }
            } else {

                if (!listTriIndices.isEmpty()) {
                    /**
                     * Set the Area Type for each triangle. Since this is one 
                     * mesh, the AreaModification can be applied directly to the 
                     * array found in listTriIndices.
                     */
                    List<int[]> listMarkedTris = new ArrayList<>();
                    for (NavMeshBuildSource sourceObj : geomProvider.getModifications()) {
                        int[] m_triareas = Recast.markWalkableTriangles(ctx, 
                                cfg.walkableSlopeAngle, 
                                verts, 
                                listTriIndices.get(geomProvider.getModifications().indexOf(sourceObj)), 
                                listTriIndices.get(geomProvider.getModifications().indexOf(sourceObj)).length/3, 
                                sourceObj.getAreaModification());
                        listMarkedTris.add(m_triareas);
                    }                 

                    //Prepare a new array to combine all marked triangles.
                    int[] mergeArea = new int[ntris];
                    int length = 0;
                    //Copy each marked triangle into the new array.
                    for (int[] area: listMarkedTris) {
                        System.arraycopy(area, 0, mergeArea, length, area.length);
                        length += area.length;
                    }

                    RecastRasterization.rasterizeTriangles(ctx, verts, tris, mergeArea, ntris, solid, cfg.walkableClimb);

                } else {
                    //Mark all triangles  with a single AreaModification from cfg.                    
                    int[] m_triareas = Recast.markWalkableTriangles(ctx, cfg.walkableSlopeAngle, verts, tris, ntris, cfg.walkableAreaMod);
                    RecastRasterization.rasterizeTriangles(ctx, verts, tris, m_triareas, ntris, solid, cfg.walkableClimb);
                }   
            }
        }
        
        return solid;
    }

}
