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

import com.jme3.recast4j.Recast.Utils.Flags;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.io.MeshDataWriter;
import org.recast4j.detour.io.MeshSetWriter;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilderConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteOrder;

public class RecastTest {
	
	/**
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
    	
        RecastConfigBuilder builder = new RecastConfigBuilder();
        Mesh m = new Box(50f, 0.5f, 50f);

        RecastBuilderConfigBuilder rcb = new RecastBuilderConfigBuilder(m);
        GeometryProviderBuilder gpb = new GeometryProviderBuilder(m);
        RecastBuilder rb = new RecastBuilder();

        RecastBuilderConfig bcfg = rcb.build(builder.build());
        RecastBuilder.RecastBuilderResult rbr = rb.build(gpb.build(), bcfg);

        MeshData meshData = NavMeshBuilder.createNavMeshData(new NavMeshDataCreateParamsBuilder(rbr).build(bcfg));
        NavMesh navMesh = new NavMesh(meshData, bcfg.cfg.maxVertsPerPoly, 0);
        try {
            saveToFile(meshData);
            saveToFile(navMesh);
        } catch (Exception e) {
        	e.printStackTrace();
        }

        System.out.println(Flags.ofBitmask(1, 2, 4).setFlagByBitmask(8).setFlagByBitmask(16).getValue());
        System.out.println(Flags.of(1, 2, 3).setFlag(4).setFlag(5).getValue());
    }

    private static MeshData buildBlockingRenderThread(Mesh m) {
        RecastBuilderConfig bcfg = new RecastBuilderConfigBuilder(m).build(new RecastConfigBuilder().build());
        return NavMeshBuilder.createNavMeshData(new NavMeshDataCreateParamsBuilder(new RecastBuilder().build(new GeometryProviderBuilder(m).build(), bcfg)).build(bcfg));
    }
    
	private static void saveToFile(MeshData md) throws Exception {
		MeshDataWriter mdw = new MeshDataWriter();
		File f = new File("test.md");
		System.out.println("Saving MeshData=" + f.getAbsolutePath());
		mdw.write(new FileOutputStream(f), md, ByteOrder.BIG_ENDIAN, false);
	}

	private static void saveToFile(NavMesh nm) throws Exception {
		MeshSetWriter msw = new MeshSetWriter();
		File f = new File("test.nm");
		System.out.println("Saving NavMesh=" + f.getAbsolutePath());
		msw.write(new FileOutputStream(f), nm, ByteOrder.BIG_ENDIAN, false);
	}

//    private static void saveToFile(MeshData md) throws Exception {
//        MeshDataWriter mdw = new MeshDataWriter();
//        mdw.write(new FileOutputStream(new File("test.md")),  md, ByteOrder.BIG_ENDIAN, false);
//    }
//
//    private static void saveToFile(NavMesh nm) throws Exception {
//        MeshSetWriter msw = new MeshSetWriter();
//        msw.write(new FileOutputStream(new File("test.nm")), nm, ByteOrder.BIG_ENDIAN, false);
//    }
}
