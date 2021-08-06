package com.jme3.recast4j.Recast;

import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastConfig;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.geom.InputGeomProvider;

import com.jme3.scene.Node;

/**
 * The Purpose of this class is to simplify NavMesh building at the loss of
 * configurability.<br />
 * This provides a very standard pipeline for your needs, but it is desired to
 * roll your own whenever there are some special needs. This however provides a
 * good starting point.
 *
 * @author MeFisto94
 * @author capdevon
 */
public class SimpleNavMeshBuilder {

    /**
     * 
     * @param node
     * @param cfg
     * @return
     */
    public static NavMesh buildSolo(Node node, RecastConfig cfg) {
        // Build merged mesh.
        InputGeomProvider geomProvider = new GeometryProviderBuilder(node).build();

        // Create a RecastBuilderConfig builder with world bounds of our geometry.
        RecastBuilderConfig builderCfg = new RecastBuilderConfig(cfg, geomProvider.getMeshBoundsMin(), geomProvider.getMeshBoundsMax());

        // Build our Navmesh data using our gathered geometry and configuration.
        RecastBuilder rcBuilder = new RecastBuilder();
        RecastBuilderResult rcResult = rcBuilder.build(geomProvider, builderCfg);

        // Set the parameters needed to build our MeshData using the RecastBuilder results.
        NavMeshDataCreateParamsBuilder paramsBuilder = new NavMeshDataCreateParamsBuilder(rcResult);

        // Build the parameter object.
        NavMeshDataCreateParams params = paramsBuilder.withPolyFlagsAll(1).build(builderCfg);

        //Generate MeshData using our parameters object.
        MeshData meshData = NavMeshBuilder.createNavMeshData(params);

        //Build the NavMesh.
        NavMesh navMesh = new NavMesh(meshData, builderCfg.cfg.maxVertsPerPoly, 0);
        return navMesh;
    }
}
