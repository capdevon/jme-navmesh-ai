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
import org.recast4j.recast.RecastConstants.PartitionType;

public final class RecastConfigBuilder {

    private PartitionType partitionType = PartitionType.WATERSHED;
    private float cellSize = 0.3f;
    private float cellHeight = 0.2f;
    private float agentHeight = 2.0f;
    private float agentRadius = 0.6f;
    private float agentMaxClimb = 0.9f;
    private float agentMaxSlope = 45f;
    private int regionMinSize = 8;
    private int regionMergeSize = 20;
    private float edgeMaxLen = 12f;
    private float edgeMaxError = 1.3f;
    private int vertsPerPoly = 3;
    private float detailSampleDist = 6.0f;
    private float detailSampleMaxError = 1.0f;
    private int tileSize = 0;
    private AreaModification walkableAreaMod = JmeAreaMods.AREAMOD_GROUND;

    /**
     * We have values which we can derive from others using a formula.
     * When such a value is modified, we have to disable auto-deriveal or else
     * we'll overwrite what the user is trying to do
     */
    private boolean modifiedCalculatedValue = false;

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
     * Sets the radius of the typical agent. This should represent the median of all
     * agents and extreme outliers shall have their own NavMesh. Default: 0.6f
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
     * Default: 3
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
        cellSize = agentRadius / 2f;        // r/2
        cellHeight = cellSize / 2f;         // cs/2
        agentMaxClimb = 2f * cellHeight;    // > 2*ch
        edgeMaxLen = 8f * agentRadius;      // r*8
        return this;
    }
    
    /**
     * Build the RecastConfig Instance.<br>
     * Note: This will automatically calculate CellSize, CellHeight and others if
     * you didn't set any of those.<br>
     * If you do not want this behavior, use {@link #build(boolean)} with false.
     * 
     * @return RecastConfig Instance
     */
    public RecastConfig build() {
        if (modifiedCalculatedValue) {
            return build(false);
        } else {
            return build(true);
        }
    }

    /**
     * Build the RecastConfig Instance.<br>
     * 
     * @param deriveValues Whether to derive some values from formulas by calling deriveValues for you.
     * @see #deriveValues()
     * @return RecastConfig Instance
     */
    public RecastConfig build(boolean deriveValues) {
        if (deriveValues) {
            deriveValues();
        }

        return new RecastConfig(
                partitionType, 
                cellSize, 
                cellHeight, 
                agentHeight, 
                agentRadius, 
                agentMaxClimb, 
                agentMaxSlope,
                regionMinSize, 
                regionMergeSize, 
                edgeMaxLen, 
                edgeMaxError, 
                vertsPerPoly, 
                detailSampleDist, 
                detailSampleMaxError,
                tileSize, 
                walkableAreaMod);
    }

}
