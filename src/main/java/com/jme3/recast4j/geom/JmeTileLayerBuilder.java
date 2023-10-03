/*
 * The MIT License
 *
 * Copyright 2021.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * MODELS/DUNE.J3O:
 * Converted from http://quadropolis.us/node/2584 [Public Domain according to the Tags of this Map]
 */
package com.jme3.recast4j.geom;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.recast4j.detour.DetourCommon;
import org.recast4j.detour.tilecache.AbstractTileLayersBuilder;
import org.recast4j.detour.tilecache.TileCacheBuilder;
import org.recast4j.detour.tilecache.TileCacheLayerHeader;
import org.recast4j.recast.HeightfieldLayerSet;
import org.recast4j.recast.HeightfieldLayerSet.HeightfieldLayer;
import org.recast4j.recast.Recast;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastConfig;

/**
 * Uses the JmeInputGeomProvider to build layers for the tile cache. Calls the
 * RecastBuilder that accepts the JmeInputGeomProvider to build the
 * HeightFiield.
 * 
 * @author Robert
 */
public class JmeTileLayerBuilder extends AbstractTileLayersBuilder {

    private JmeInputGeomProvider geom;
    private RecastConfig cfg;
    private int tw;
    private int th;

    public JmeTileLayerBuilder(JmeInputGeomProvider geom, RecastConfig cfg) {
        this.geom = geom;
        this.cfg = cfg;
        float[] bmin = geom.getMeshBoundsMin();
        float[] bmax = geom.getMeshBoundsMax();
        int[] twh = Recast.calcTileCount(bmin, bmax, cfg.cs, cfg.tileSize);
        tw = twh[0];
        th = twh[1];
    }

    public List<byte[]> build(ByteOrder order, boolean cCompatibility, int threads) {
        return build(order, cCompatibility, threads, tw, th);
    }

    public int getTw() {
        return tw;
    }

    public int getTh() {
        return th;
    }

    @Override
    public List<byte[]> build(int tx, int ty, ByteOrder order, boolean cCompatibility) {
        HeightfieldLayerSet lset = getHeightfieldSet(tx, ty);

        List<byte[]> result = new ArrayList<>();
        if (lset != null) {
            TileCacheBuilder builder = new TileCacheBuilder();
            for (int i = 0; i < lset.layers.length; ++i) {
                HeightfieldLayer layer = lset.layers[i];

                // Store header
                TileCacheLayerHeader header = new TileCacheLayerHeader();
                header.magic = TileCacheLayerHeader.DT_TILECACHE_MAGIC;
                header.version = TileCacheLayerHeader.DT_TILECACHE_VERSION;

                // Tile layer location in the navmesh.
                header.tx = tx;
                header.ty = ty;
                header.tlayer = i;
                DetourCommon.vCopy(header.bmin, layer.bmin);
                DetourCommon.vCopy(header.bmax, layer.bmax);

                // Tile info.
                header.width = layer.width;
                header.height = layer.height;
                header.minx = layer.minx;
                header.maxx = layer.maxx;
                header.miny = layer.miny;
                header.maxy = layer.maxy;
                header.hmin = layer.hmin;
                header.hmax = layer.hmax;
                result.add(builder.compressTileCacheLayer(header, layer.heights, layer.areas, layer.cons, order, cCompatibility));
            }
        }
        return result;
    }

    protected HeightfieldLayerSet getHeightfieldSet(int tx, int ty) {
        JmeRecastBuilder rcBuilder = new JmeRecastBuilder();
        float[] bmin = geom.getMeshBoundsMin();
        float[] bmax = geom.getMeshBoundsMax();
        RecastBuilderConfig builderCfg = new RecastBuilderConfig(cfg, bmin, bmax, tx, ty, true);
        return rcBuilder.buildLayers(geom, builderCfg);
    }
}