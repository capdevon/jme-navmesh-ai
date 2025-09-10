package com.jme3.recast4j.ai;

import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.Poly;
import org.recast4j.detour.QueryFilter;

/**
 * Defines polygon filtering and traversal costs for navigation mesh query
 * operations.
 *
 * The Default Implementation
 *
 * At construction: 
 * All area costs default to 1.0. 
 * All flags are included and none are excluded.
 *
 * If a polygon has both an include and an exclude flag, it will be excluded.
 *
 * The way filtering works, a navigation mesh polygon must have at least one
 * flag set to ever be considered by a query. So a polygon with no flags will
 * never be considered.
 *
 * Setting the include flags to 0 will result in all polygons being excluded.
 * 
 * @author capdevon
 */
public class NavMeshQueryFilter implements QueryFilter {

    //This is a bitfield.
    protected int m_excludeFlags = 0;
    //This is a bitfield.
    protected int m_includeFlags = 0xffff;
    protected final float[] m_areaCost = new float[NavMesh.DT_MAX_AREAS];
    protected float[] m_polyExtents = new float[] { 2, 4, 2 };
    protected StraightPathOptions m_straightPathOptions = StraightPathOptions.None;

    public NavMeshQueryFilter() {
        for (int i = 0; i < m_areaCost.length; ++i) {
            m_areaCost[i] = 1.0f;
        }
    }

    public NavMeshQueryFilter(int includeFlags, int excludeFlags) {
        this();
        this.m_includeFlags = includeFlags;
        this.m_excludeFlags = excludeFlags;
    }

    /**
     * Returns true if the polygon can be visited. (I.e. Is traversable.)
     *
     * @param ref  The reference id of the polygon test.
     * @param tile The tile containing the polygon.
     * @param poly The polygon to test.
     * @return
     */
    @Override
    public boolean passFilter(long ref, MeshTile tile, Poly poly) {
        return (poly.flags & m_includeFlags) != 0 && (poly.flags & m_excludeFlags) == 0;
    }

    /**
     *
     * @param pa       The start position on the edge of the previous and current polygon.
     * @param pb       The end position on the edge of the current and next polygon.
     * @param prevRef  The reference id of the previous polygon. [opt]
     * @param prevTile The tile containing the previous polygon. [opt]
     * @param prevPoly The previous polygon. [opt]
     * @param curRef   The reference id of the current polygon.
     * @param curTile  The tile containing the current polygon.
     * @param curPoly  The current polygon.
     * @param nextRef  The reference id of the next polygon. [opt]
     * @param nextTile The tile containing the next polygon. [opt]
     * @param nextPoly The next polygon. [opt]
     * @return
     */
    @Override
    public float getCost(float[] pa, float[] pb, long prevRef, MeshTile prevTile, Poly prevPoly, long curRef,
        MeshTile curTile, Poly curPoly, long nextRef, MeshTile nextTile, Poly nextPoly) {
        return vDist(pa, pb) * m_areaCost[curPoly.getArea()];
    }

    /**
     * Any polygons that include one or more of these flags will be included in
     * the operation.
     *
     * @return Returns the include flags for the filter.
     */
    public int getIncludeFlags() {
        return m_includeFlags;
    }

    /**
     * Sets the include flags for the filter.
     *
     * @param flags The new flags.
     */
    public void setIncludeFlags(int flags) {
        m_includeFlags = flags;
    }

    /**
     * Any polygons that include one ore more of these flags will be excluded
     * from the operation.
     *
     * @return Returns the exclude flags for the filter.
     */
    public int getExcludeFlags() {
        return m_excludeFlags;
    }

    /**
     * Sets the exclude flags for the filter.
     *
     * @param flags The new flags.
     */
    public void setExcludeFlags(int flags) {
        m_excludeFlags = flags;
    }

    public float[] getPolyExtents() {
        return m_polyExtents;
    }

    public void setPolyExtents(float[] halfExtents) {
        this.m_polyExtents = halfExtents;
    }

    public StraightPathOptions getStraightPathOptions() {
        return m_straightPathOptions;
    }

    public void setStraightPathOptions(StraightPathOptions options) {
        this.m_straightPathOptions = options;
    }

    /**
     * Returns the area cost multiplier for the given area type for this filter. 
     * The default value is 1.
     * 
     * @param areaIndex Index to retrieve the cost for.
     * @return float The cost multiplier for the supplied area index.
     */
    public float getAreaCost(int areaIndex) {
        if (areaIndex < 0 || areaIndex >= m_areaCost.length) {
            String msg = String.format("The valid range is [0:%d]", m_areaCost.length - 1);
            throw new ArrayIndexOutOfBoundsException(msg);
        }
        return m_areaCost[areaIndex];
    }

    /**
     * Sets the pathfinding cost multiplier for this filter for a given area type.
     * 
     * @param areaIndex The area index to set the cost for.
     * @param cost The cost for the supplied area index.
     */
    public void setAreaCost(int areaIndex, float cost) {
        m_areaCost[areaIndex] = cost;
    }

    /**
     * Returns the distance between two points.
     * @param v1 A point. [(x, y, z)]
     * @param v2 A point. [(x, y, z)]
     * @return The distance between the two points.
     */
    private float vDist(float[] v1, float[] v2) {
        float dx = v2[0] - v1[0];
        float dy = v2[1] - v1[1];
        float dz = v2[2] - v1[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

}
