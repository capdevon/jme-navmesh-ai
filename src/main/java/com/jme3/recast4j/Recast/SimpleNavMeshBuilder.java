package com.jme3.recast4j.Recast;

import com.jme3.scene.Node;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastConfig;

/**
 * The Purpose of this class is to simplify NavMesh building at the loss of configurability.<br />
 * This provides a very standard pipeline for your needs, but it is desired to roll your own whenever
 * there are some special needs. This however provides a good starting point.
 *
 * @author MeFisto94
 */
public class SimpleNavMeshBuilder {

    public static MeshData build(Node node, RecastConfig cfg) {
        RecastBuilderConfig bCfg = new RecastBuilderConfigBuilder(node).build(cfg);
        return
            NavMeshBuilder.createNavMeshData(
                new NavMeshDataCreateParamsBuilder(
                    new RecastBuilder().build(
                        new GeometryProviderBuilder(node).build(), bCfg
                    )
                ).build(bCfg)
            );
    }
}
