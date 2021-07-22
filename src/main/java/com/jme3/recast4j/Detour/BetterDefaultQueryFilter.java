/*
 *  MIT License
 *  Copyright (c) 2019 MeFisto94
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
package com.jme3.recast4j.Detour;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.MeshTile;
import org.recast4j.detour.Poly;

/**
 * For documentation, see {@link DefaultQueryFilter}.
 * Changes: When a Polygon has no flag and the filter is set to include all (which is the default case most of the time),
 * accept the polygon instead of rejecting it. Because that otherwise leads to useless code setting the flag to 0x1, just
 * so that the filter accepts them.
 */
public class BetterDefaultQueryFilter extends DefaultQueryFilter {
    protected int m_excludeFlags;
    protected int m_includeFlags;

    public BetterDefaultQueryFilter() {
        super();
        // they are private...
        this.m_includeFlags = 0xffff;
        this.m_excludeFlags = 0;
    }

    public BetterDefaultQueryFilter(int includeFlags, int excludeFlags, float[] areaCost) {
        super(includeFlags, excludeFlags, areaCost);
        this.m_includeFlags = includeFlags;
        this.m_excludeFlags = excludeFlags;
    }

    @Override
    public boolean passFilter(long ref, MeshTile tile, Poly poly) {
        if (poly.flags == 0x0 && m_includeFlags == 0xFFFF && m_excludeFlags == 0) {
            return true; // Keep unflagged polys, but only if we include all AND exclude nothing (as you have no chance
            // to exclude them with exclusion flags otherwise)
        }
        // super: return (poly.flags & m_includeFlags) != 0 && (poly.flags & m_excludeFlags) == 0;
        return super.passFilter(ref, tile, poly);
    }
}
