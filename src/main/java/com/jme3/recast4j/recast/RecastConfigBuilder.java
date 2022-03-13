/*
 *  MIT License
 *  Copyright (c) 2021 MeFisto94
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package com.jme3.recast4j.recast;

import org.recast4j.recast.AreaModification;
import org.recast4j.recast.RecastConfig;
import org.recast4j.recast.RecastConstants;

import com.jme3.recast4j.demo.JmeAreaMods;

public class RecastConfigBuilder {

    protected RecastConstants.PartitionType partitionType;
    protected float cellSize;
    protected float cellHeight;
    protected float agentHeight;
    protected float agentRadius;
    protected float agentMaxClimb;
    protected float agentMaxSlope;
    protected int regionMinSize;
    protected int regionMergeSize;
    protected float edgeMaxLen;
    protected float edgeMaxError;
    protected int vertsPerPoly;
    protected float detailSampleDist;
    protected float detailSampleMaxError;
    protected int tileSize;
    protected AreaModification walkableAreaMod;

    /**
     * We have values which we can derive from others using a formula.
     * When such a value is modified, we have to disable auto-deriveal or else
     * we'll overwrite what the user is trying to do
     */
    boolean modifiedCalculatedValue = false;

    public RecastConfigBuilder(RecastConfigBuilder rcb) {
        this.partitionType = rcb.partitionType;
        this.cellSize = rcb.cellSize;
        this.cellHeight = rcb.cellHeight;
        this.agentHeight = rcb.agentHeight;
        this.agentRadius = rcb.agentRadius;
        this.agentMaxClimb = rcb.agentMaxClimb;
        this.agentMaxSlope = rcb.agentMaxSlope;
        this.regionMinSize = rcb.regionMinSize;
        this.regionMergeSize = rcb.regionMergeSize;
        this.edgeMaxLen = rcb.edgeMaxLen;
        this.edgeMaxError = rcb.edgeMaxError;
        this.vertsPerPoly = rcb.vertsPerPoly;
        this.detailSampleDist = rcb.detailSampleDist;
        this.detailSampleMaxError = rcb.detailSampleMaxError;
        this.tileSize = rcb.tileSize;
        this.walkableAreaMod = rcb.walkableAreaMod;
        this.modifiedCalculatedValue = rcb.modifiedCalculatedValue;
    }

    public RecastConfigBuilder() {
        // Set default values here
        agentHeight = 2.0f;
        agentRadius = 0.6f;
        cellHeight = 0.2f;
        cellSize = 0.3f;
        agentMaxClimb = 0.9f;
        edgeMaxError = 1.3f;
        edgeMaxLen = 12f;
        regionMergeSize = 20;
        regionMinSize = 8;
        detailSampleDist = 6.0f;
        detailSampleMaxError = 1.0f;
        agentMaxSlope = 45f;
        vertsPerPoly = 6;
        tileSize = 0;
        partitionType = RecastConstants.PartitionType.WATERSHED;
        walkableAreaMod = JmeAreaMods.AREAMOD_GROUND;
    }

    /**
     * WATERSHED Watershed, Classic Recast partitioning method generating the nicest tessellation.
     *           partitions the heightfield into nice regions without holes or overlaps
     *           there are some corner cases where this method produces holes and overlaps:
     *           holes may appear when a small obstacle is close to a large open area (triangulation can handle this)
     *           overlaps may occur if you have narrow spiral corridors (i.e stairs), this make triangulation to fail
     *           generally the best choice if you precompute the navmesh, use this if you have large open areas
     *
     * MONOTONE Monotone, Fastest navmesh generation method, partitions the heightfield into regions without holes and
     *           overlaps (guaranteed), may create long thin polygons, which sometimes causes paths with detours.
     *
     * LAYERS Layers, Reasonably fast method that produces better triangles than monotone partitioning.
     *
     * Default: WATERSHED
     *
     * @param type The PartitionType to use
     * @return this
     */
    public RecastConfigBuilder withPartitionType(RecastConstants.PartitionType type) {
        this.partitionType = type;
        return this;
    }

    /**
     * Rasterized cell size
     * Default: 0.3f
     * @param cellSize the cell size
     * @return this
     */
    public RecastConfigBuilder withCellSize(float cellSize) {
        this.cellSize = cellSize;
        modifiedCalculatedValue = true;
        return this;
    }

    /**
     * Rasterized cell height
     * Default: 0.2f
     * @param cellHeight the cell height
     * @return this
     */
    public RecastConfigBuilder withCellHeight(float cellHeight) {
        this.cellHeight = cellHeight;
        modifiedCalculatedValue = true;
        return this;
    }

    /**
     * Sets the minimum height where the agent can still walk.
     * Default: 2.0f
     * @param agentHeight The height of the typical agent
     * @return this
     */
    public RecastConfigBuilder withAgentHeight(float agentHeight) {
        this.agentHeight = agentHeight;
        return this;
    }


    /**
     * Sets the radius of the typical agent. This should represent the median of all agents and extreme outliers shall
     * have their own NavMesh.
     * Default: 0.6f
     * @param agentRadius The Radius of the typical Agent
     * @return this
     */
    public RecastConfigBuilder withAgentRadius(float agentRadius) {
        this.agentRadius = agentRadius;
        return this;
    }

    /**
     * Maximum height between grid cells the agent can climb
     * Default: 0.9f
     * @param agentMaxClimb the maximum height the agent can climb
     * @return this
     */

    public RecastConfigBuilder withAgentMaxClimb(float agentMaxClimb) {
        this.agentMaxClimb = agentMaxClimb;
        modifiedCalculatedValue = true;
        return this;
    }

    /**
     * Maximum walkable slope angle (in degrees!)
     * Default: 45f
     * @param agentMaxSlope the maximum walkable angle
     * @return this
     */
    public RecastConfigBuilder withAgentMaxSlope(float agentMaxSlope) {
        this.agentMaxSlope = agentMaxSlope;
        return this;
    }

    /**
     * Minimum regions size (smaller regions will be deleted)
     * Default: 8
     * @param regionMinSize minimal region size for deletion
     * @return this
     */
    public RecastConfigBuilder withRegionMinSize(int regionMinSize) {
        this.regionMinSize = regionMinSize;
        return this;
    }

    /**
     * Minimum regions size (smaller regions will be merged)
     * Default: 20
     * @param regionMergeSize minimal region size for merging
     * @return this
     */
    public RecastConfigBuilder withRegionMergeSize(int regionMergeSize) {
        this.regionMergeSize = regionMergeSize;
        return this;
    }

    /**
     * Maximum contour edge length
     * Default: 12f
     * @param edgeMaxLen contour edge length
     * @return this
     */
    public RecastConfigBuilder withEdgeMaxLen(float edgeMaxLen) {
        this.edgeMaxLen = edgeMaxLen;
        modifiedCalculatedValue = true;
        return this;
    }

    /**
     * Maximum distance error from contour to cells
     * Default: 1.3f
     * @param edgeMaxError max error
     * @return this
     */
    public RecastConfigBuilder withEdgeMaxError(float edgeMaxError) {
        this.edgeMaxError = edgeMaxError;
        return this;
    }

    /**
     * Max number of vertices per polygon
     * Default: 6
     * @param vertsPerPoly maximum number of vertices
     * @return this
     */
    public RecastConfigBuilder withVertsPerPoly(int vertsPerPoly) {
        this.vertsPerPoly = vertsPerPoly;
        return this;
    }

    /**
     * Detail mesh sample spacing
     * Default: 6f
     * @param detailSampleDist the spacing
     * @return this
     */
    public RecastConfigBuilder withDetailSampleDistance(float detailSampleDist) {
        this.detailSampleDist = detailSampleDist;
        return this;
    }

    /**
     * Detail mesh simplification max sample error
     * Default: 1f
     * @param detailSampleMaxError sample error
     * @return this
     */
    public RecastConfigBuilder withDetailSampleMaxError(float detailSampleMaxError) {
        this.detailSampleMaxError = detailSampleMaxError;
        return this;
    }

    public RecastConfigBuilder withTileSize(int tileSize) {
        this.tileSize = tileSize;
        return this;
    }

    public RecastConfigBuilder withWalkableAreaMod(AreaModification walkableAreaMod) {
        this.walkableAreaMod = walkableAreaMod;
        return this;
    }

    /**
     * CellSize, CellHeight, AgentMaxClimb and EdgeMaxLength can be derived from
     * some formulas. Calling this method will do that for you (and thus also
     * overwrite the values manually set!)
     *
     * @return this
     */
    public RecastConfigBuilder deriveValues() {
        cellSize = agentRadius / 2f; // r / 2
        cellHeight = cellSize / 2f; // cs / 2
        agentMaxClimb = 2f * cellHeight; // > 2 * ch
        edgeMaxLen = 8f * agentRadius; // r*8
        return this;
    }

    public RecastConfigBuilder clone() {
        return new RecastConfigBuilder(this);
    }

    /**
     * Build the RecastConfig Instance.<br>
     * @param deriveValues Whether to derive some values from formulas by calling deriveValues for you.
     * @see #deriveValues()
     * @return
     */
    public RecastConfig build(boolean deriveValues) {
        if (deriveValues) {
            deriveValues();
        }

        return new RecastConfig(partitionType, cellSize, cellHeight, agentHeight, agentRadius, agentMaxClimb, agentMaxSlope,
                regionMinSize, regionMergeSize, edgeMaxLen, edgeMaxError, vertsPerPoly, detailSampleDist, detailSampleMaxError,
                tileSize, walkableAreaMod);
    }

    /**
     * Build the RecastConfig Instance.<br>
     * Note: This will automatically calculate CellSize, CellHeight and others if you didn't set any of those.<br>
     * If you do not want this behavior, use {@link #build(boolean)} with false.
     * @return
     */
    public RecastConfig build() {
        if (modifiedCalculatedValue) {
            return build(false);
        } else {
            return build(true);
        }
    }
}
