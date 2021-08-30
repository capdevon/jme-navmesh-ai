package com.jme3.recast4j.ai;

import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.Poly;
import org.recast4j.detour.QueryFilter;

/**
 * 
 * @author capdevon
 */
public class NavMeshQueryFilter implements QueryFilter {

	//This is a bitfield.
    protected int m_excludeFlags = 0;
    //This is a bitfield.
    protected int m_includeFlags = 0xffff;
    protected final float[] m_areaCost = new float[NavMesh.DT_MAX_AREAS];
    protected float[] m_polyExtents = new float[] {2, 4, 2};
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

    @Override
    public boolean passFilter(long ref, MeshTile tile, Poly poly) {
        return (poly.flags & m_includeFlags) != 0 && (poly.flags & m_excludeFlags) == 0;
    }

    @Override
    public float getCost(float[] pa, float[] pb, long prevRef, MeshTile prevTile, Poly prevPoly, long curRef,
        MeshTile curTile, Poly curPoly, long nextRef, MeshTile nextTile, Poly nextPoly) {
        return vDist(pa, pb) * m_areaCost[curPoly.getArea()];
    }

    public int getIncludeFlags() {
        return m_includeFlags;
    }

    public void setIncludeFlags(int flags) {
        m_includeFlags = flags;
    }

    public int getExcludeFlags() {
        return m_excludeFlags;
    }

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
     * @param areaIndex - Index to retrieve the cost for.
     * @return float - The cost multiplier for the supplied area index.
     */
    public float getAreaCost(int areaIndex) {
        if (areaIndex < 0 || areaIndex >= m_areaCost.length) {
            String msg = String.format("The valid range is [0:{0}]", m_areaCost.length - 1);
            throw new ArrayIndexOutOfBoundsException(msg);
        }
        return m_areaCost[areaIndex];
    }

    /**
     * Sets the pathfinding cost multiplier for this filter for a given area type.
     * 
     * @param areaIndex - The area index to set the cost for.
     * @param cost - The cost for the supplied area index.
     */
    public void setAreaCost(int areaIndex, float cost) {
        m_areaCost[areaIndex] = cost;
    }

    /// Returns the distance between two points.
    /// @param[in] v1 A point. [(x, y, z)]
    /// @param[in] v2 A point. [(x, y, z)]
    /// @return The distance between the two points.
    private float vDist(float[] v1, float[] v2) {
        float dx = v2[0] - v1[0];
        float dy = v2[1] - v1[1];
        float dz = v2[2] - v1[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

}
