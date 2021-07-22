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
package com.jme3.recast4j.demo;

import org.recast4j.recast.AreaModification;

public class SimpleAreaMod {

	public static final int POLYAREA_TYPE_MASK 		= 0x07;
	public static final int POLYAREA_TYPE_GROUND 	= 0x1;
	public static final int POLYAREA_TYPE_WATER 	= 0x2;
	public static final int POLYAREA_TYPE_ROAD 		= 0x3;
	public static final int POLYAREA_TYPE_DOOR 		= 0x4;
	public static final int POLYAREA_TYPE_GRASS 	= 0x5;
	public static final int POLYAREA_TYPE_JUMP 		= 0x6;

	public static final AreaModification AREAMOD_GROUND = new AreaModification(POLYAREA_TYPE_GROUND, POLYAREA_TYPE_MASK);
	public static final AreaModification AREAMOD_WATER 	= new AreaModification(POLYAREA_TYPE_WATER, POLYAREA_TYPE_MASK);
	public static final AreaModification AREAMOD_ROAD 	= new AreaModification(POLYAREA_TYPE_ROAD, POLYAREA_TYPE_MASK);
	public static final AreaModification AREAMOD_GRASS 	= new AreaModification(POLYAREA_TYPE_GRASS, POLYAREA_TYPE_MASK);
	public static final AreaModification AREAMOD_DOOR 	= new AreaModification(POLYAREA_TYPE_DOOR, POLYAREA_TYPE_DOOR);
	public static final AreaModification AREAMOD_JUMP 	= new AreaModification(POLYAREA_TYPE_JUMP, POLYAREA_TYPE_JUMP);

	public static final int POLYFLAGS_WALK = 0x01; 		// Ability to walk (ground, grass, road)
	public static final int POLYFLAGS_SWIM = 0x02; 		// Ability to swim (water).
	public static final int POLYFLAGS_DOOR = 0x04; 		// Ability to move through doors.
	public static final int POLYFLAGS_JUMP = 0x08; 		// Ability to jump.
	public static final int POLYFLAGS_DISABLED = 0x10; 	// Disabled polygon
	public static final int POLYFLAGS_ALL = 0xffff; 	// All abilities.

}
