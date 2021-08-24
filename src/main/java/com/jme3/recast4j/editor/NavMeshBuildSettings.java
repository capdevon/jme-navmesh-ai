package com.jme3.recast4j.editor;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.recast4j.recast.AreaModification;
import org.recast4j.recast.RecastConstants.PartitionType;

/**
 * 
 * @author capdevon
 */
public class NavMeshBuildSettings {
	
    //Settings Explained.
    //First you should decide the size of your character "capsule". For example 
    //if you are using meters as units in your game world, a good size of human 
    //sized character might be r=0.4, h=2.0.

    //Next the voxelization cell size cs will be derived from that. Usually good 
    //value for cs is r/2 or r/3. In ourdoor environments, r/2 might be enough, 
    //indoors you sometimes want the extra precision and you might choose to use 
    //r/3 or smaller.
    
    //The voxelization cell height ch is defined separately in order to allow 
    //greater precision in height tests. Good starting point for ch is cs/2. If 
    //you get small holes where there are discontinuities in the height (steps), 
    //you may want to decrease cell height.
    
    //Next up is the character definition values. First up is walkableHeight, 
    //which defines the height of the agent in voxels, that is ceil(h/ch).
    
    //The walkableClimb defines how high steps the character can climb. In most 
    //levels I have encountered so far, this means almost waist height! Lazy 
    //level designers. If you use down projection+capsule for NPC collision 
    //detection you may derive a good value from that representation. Again this 
    //value is in voxels, remember to use ch instead of cs, ceil(maxClimb/ch).
    
    //The parameter walkableRadius defines the agent radius in voxels, 
    //ceil(r/cs). If this value is greater than zero, the navmesh will be 
    //shrunken by the agent radius. The shrinking is done in voxel 
    //representation, so some precision is lost there. This step allows simpler 
    //checks at runtime. If you want to have tight fit navmesh, use zero radius.
    
    //The parameter walkableSlopeAngle is used before voxelization to check if 
    //the slope of a triangle is too high and those polygons will be given 
    //non-walkable flag. You may tweak the triangle flags yourself too, for 
    //example if you wish to make certain objects or materials non-walkable. The 
    //parameter is in radians.
    
    //In certain cases really long outer edges may decrease the triangulation 
    //results. Sometimes this can be remedied by just tesselating the long edges. 
    //The parameter maxEdgeLen defines the max edge length in voxel coordinates. 
    //A good value for maxEdgeLen is something like walkableRadius*8. A good way 
    //to tweak this value is to first set it really high and see if your data 
    //creates long edges. If so, then try to find as big value as possible which 
    //happens to create those few extra vertices which makes the tesselation 
    //better.
    
    //When the rasterized areas are converted back to vectorized representation 
    //the maxSimplificationError describes how loosely the simplification is 
    //done (the simplification is Douglas-Peucker, so this value describes the 
    //max deviation in voxels). Good values are between 1.1-1.5 (1.3 usually 
    //yield good results). If the value is less, some strair-casing starts to 
    //appear at the edges and if it is more than that, the simplification starts 
    //to cut some corners.
    //Watershed partitioning is really prone to noise in the input distance 
    //field. In order to get nicer ares, the ares are merged and small isolated 
    //areas are removed after the water shed partitioning. The parameter 
    //minRegionSize describes the minimum isolated region size that is still 
    //kept. 
    //A region is removed if the regionVoxelCount < minRegionSize*minRegionSize.
    //The triangulation process greatly benefits from small local data. The 
    //parameter mergeRegionSize controls how large regions can be still merged. 
    //If regionVoxelCount > mergeRegionSize*mergeRegionSize the region is not 
    //allowed to be merged with another region anymore.
    //Yeah, I know these last two values are a bit weirdly defined. If you are 
    //using tiled preprocess with relatively small tile size, the merge value 
    //can be really high. If you have followed the above steps, then I'd 
    //recommend using the demo values for minRegionSize and mergeRegionSize. If 
    //you see small patched missing here and there, you could lower the 
    //minRegionSize.
    //Mikko Mononen
    /**
     * ***********************************************************************
     */
    //The width/height size of tile's on the xz-plane.
    //[Limit: >= 0] [Units: vx]
    public int tileSize = 16;
    //The width and depth resolution used when sampling the source geometry. The 
    //width and depth of the cell columns that make up voxel fields.
    //Cells are laid out on the width/depth plane of voxel fields. Width is 
    //associated with the x-axis of the source geometry. Depth is associated 
    //with the z-axis.
    //A lower value allows for the generated mesh to more closely match the 
    //source geometry, but at a higher processing and memory cost.
    //Small cell size needed to allow mesh to travel up stairs.
    //Adjust m_cellSize and m_cellHeight for contour simplification exceptions.
    //[Limit: > 0] [Units: wu], outdoors = m_agentRadius/2, indoors = m_agentRadius/3, m_cellSize = 
    //m_agentRadius for very small cells.
    public float cellSize = 0.2f;
    //Height is associated with the y-axis of the source geometry.
    //A smaller value allows for the final mesh to more closely match the source 
    //geometry at a potentially higher processing cost. (Unlike cellSize, using 
    //a lower value for cellHeight does not significantly increase memory use.)
    //This is a core configuration value that impacts almost all other 
    //parameters. 
    //m_agentHeight, m_agentMaxClimb, and m_detailSampleMaxError will 
    //need to be greater than this value in order to function correctly. 
    //m_agentMaxClimb is especially susceptible to impact from the value of 
    //m_cellHeight.
    //[Limit: > 0] [Units: wu], m_cellSize/2
    public float cellHeight = 0.1f;
    //Represents the minimum floor to ceiling height that will still allow the 
    //floor area to be considered traversable. It permits detection of overhangs 
    //in the geometry that make the geometry below become un-walkable. It can 
    //also be thought of as the maximum agent height.
    //This value should be at least two times the value of m_cellHeight in order 
    //to get good results. 
    //[Limit: >= 3][Units: vx] 
    public float agentHeight = 2.0f;
    //Represents the closest any part of a mesh can get to an obstruction in the 
    //source geometry.
    //Usually this value is set to the maximum bounding radius of agents 
    //utilizing the meshes for navigation decisions.
    //This value must be greater than the m_cellSize to have an effect.
    //[Limit: >=0] [Units:vx]
    public float agentRadius = 0.5f;
    //Represents the maximum ledge height that is considered to still be 
    //traversable.
    //Prevents minor deviations in height from improperly showing as 
    //obstructions. Permits detection of stair-like structures, curbs, etc.
    //m_agentMaxClimb should be greater than two times m_cellHeight. 
    //(m_agentMaxClimb > m_cellHeight * 2) Otherwise the resolution of the voxel 
    //field may not be high enough to accurately detect traversable ledges. 
    //Ledges may merge, effectively doubling their step height. This is 
    //especially an issue for stairways. 
    //[Limit: >=0] [Units: vx], m_agentMaxClimb/m_cellHeight = voxels.
    public float agentMaxClimb = .5f;
    //The maximum slope that is considered traversable.
    //[Limits: 0 <= value < 90] [Units: Degrees]  
    public float agentMaxSlope = 45.0f;
    //The minimum region size for unconnected (island) regions.
    //[Limit: >=0] [Units: vx]
    public int regionMinSize = 8;
    //Any regions smaller than this size will, if possible, be merged with 
    //larger regions.
    //[Limit: >=0] [Units: vx] 
    public int regionMergeSize = 20;
    //The maximum length of polygon edges that represent the border of meshes.
    //Adjust to decrease dangling errors.
    //[Limit: >=0] [Units: vx], m_agentRadius * 8
    public float edgeMaxLen = 4.0f;
    //The maximum distance the edges of meshes may deviate from the source 
    //geometry.
    //A lower value will result in mesh edges following the xz-plane geometry 
    //contour more accurately at the expense of an increased triangle count.
    //1.1 takes 2x as long to generate mesh as 1.5
    //[Limit: >=0][Units: vx], 1.1 to 1.5 for best results.
    public float edgeMaxError = 1.3f;
    //The maximum number of vertices per polygon for polygons generated during 
    //the voxel to polygon conversion process.
    //[Limit: >= 3] 
    public int vertsPerPoly = 3;
    //Sets the sampling distance to use when matching the detail mesh to the 
    //surface of the original geometry.
    //Higher values result in a detail mesh that conforms more closely to the 
    //original geometry's surface at the cost of a higher final triangle count 
    //and higher processing cost.
    //The difference between this parameter and m_edgeMaxError is that this 
    //parameter operates on the height rather than the xz-plane. It also matches 
    //the entire detail mesh surface to the contour of the original geometry. 
    //m_edgeMaxError only matches edges of meshes to the contour of the original 
    //geometry. 
    //Increase to reduce dangling errors at the cost of accuracy.
    //[Limits: 0 or >= 0.9] [Units: wu] 
    public float detailSampleDist = 8.0f;
    //The maximum distance the surface of the detail mesh may deviate from the 
    //surface of the original geometry.
    //Increase to reduce dangling errors at the cost of accuracy.
    //[Limit: >=0] [Units: wu]
    public float detailSampleMaxError = 8.0f;
    
    public boolean filterLowHangingObstacles = true;
    public boolean filterLedgeSpans = true;
    public boolean filterWalkableLowHeightSpans = true;
    
    public PartitionType partitionType = PartitionType.WATERSHED;
    public AreaModification walkableAreaMod = SampleAreaModifications.SAMPLE_AREAMOD_GROUND;
    
    public boolean tiled = false;
    
    @Override
    public String toString() {
    	return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
