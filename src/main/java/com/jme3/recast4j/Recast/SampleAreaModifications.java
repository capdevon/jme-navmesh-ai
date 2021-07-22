/*
 *  MIT License
 *  Copyright (c) 2018 MeFisto94
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
package com.jme3.recast4j.Recast;

import org.recast4j.recast.AreaModification;

public class SampleAreaModifications {

	public static int SAMPLE_POLYAREA_TYPE_MASK = 0x07;
	/// Value for the kind of ceil "ground"
	public static int SAMPLE_POLYAREA_TYPE_GROUND = 0x1;
	/// Value for the kind of ceil "water"
	public static int SAMPLE_POLYAREA_TYPE_WATER = 0x2;
	/// Value for the kind of ceil "road"
	public static int SAMPLE_POLYAREA_TYPE_ROAD = 0x3;
	/// Value for the kind of ceil "grass"
	public static int SAMPLE_POLYAREA_TYPE_GRASS = 0x4;
	/// Flag for door area. Can be combined with area types and jump flag.
	public static int SAMPLE_POLYAREA_FLAG_DOOR = 0x08;
	/// Flag for jump area. Can be combined with area types and door flag.
	public static int SAMPLE_POLYAREA_FLAG_JUMP = 0x10;

	public static AreaModification SAMPLE_AREAMOD_GROUND = new AreaModification(SAMPLE_POLYAREA_TYPE_GROUND,
			SAMPLE_POLYAREA_TYPE_MASK);
	public static AreaModification SAMPLE_AREAMOD_WATER = new AreaModification(SAMPLE_POLYAREA_TYPE_WATER,
			SAMPLE_POLYAREA_TYPE_MASK);
	public static AreaModification SAMPLE_AREAMOD_ROAD = new AreaModification(SAMPLE_POLYAREA_TYPE_ROAD,
			SAMPLE_POLYAREA_TYPE_MASK);
	public static AreaModification SAMPLE_AREAMOD_GRASS = new AreaModification(SAMPLE_POLYAREA_TYPE_GRASS,
			SAMPLE_POLYAREA_TYPE_MASK);
	public static AreaModification SAMPLE_AREAMOD_DOOR = new AreaModification(SAMPLE_POLYAREA_FLAG_DOOR,
			SAMPLE_POLYAREA_FLAG_DOOR);
	public static AreaModification SAMPLE_AREAMOD_JUMP = new AreaModification(SAMPLE_POLYAREA_FLAG_JUMP,
			SAMPLE_POLYAREA_FLAG_JUMP);

//	public static final int SAMPLE_POLYFLAGS_WALK = 0x01; // Ability to walk (ground, grass, road)
//	public static final int SAMPLE_POLYFLAGS_SWIM = 0x02; // Ability to swim (water).
//	public static final int SAMPLE_POLYFLAGS_DOOR = 0x04; // Ability to move through doors.
//	public static final int SAMPLE_POLYFLAGS_JUMP = 0x08; // Ability to jump.
//	public static final int SAMPLE_POLYFLAGS_DISABLED = 0x10; // Disabled polygon
//	public static final int SAMPLE_POLYFLAGS_ALL = 0xffff; // All abilities.
}
