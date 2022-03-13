/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.recast4j.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author capdevon
 */
public class NavMeshProperties {

    private NavMeshProperties() {}

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static NavMeshBuildSettings fromFile(File file) throws IOException {

        System.out.println("--Loading File=" + file.getAbsolutePath());

        try (FileInputStream fis = new FileInputStream(file)) {

            Properties props = new Properties();
            props.load(fis);

            NavMeshBuildSettings settings = new NavMeshBuildSettings();
            settings.tiled                  = getBoolean(props, "tiled");
            settings.tileSize               = getInteger(props, "tileSize");
            settings.cellSize               = getFloat(props, "cellSize");
            settings.cellHeight             = getFloat(props, "cellHeight");
            settings.agentHeight            = getFloat(props, "agentHeight");
            settings.agentRadius            = getFloat(props, "agentRadius");
            settings.agentMaxClimb          = getFloat(props, "agentMaxClimb");
            settings.agentMaxSlope          = getFloat(props, "agentMaxSlope");
            settings.regionMinSize          = getInteger(props, "regionMinSize");
            settings.regionMergeSize        = getInteger(props, "regionMergeSize");
            settings.edgeMaxLen             = getFloat(props, "edgeMaxLen");
            settings.edgeMaxError           = getFloat(props, "edgeMaxError");
            settings.vertsPerPoly           = getInteger(props, "vertsPerPoly");
            settings.detailSampleDist       = getFloat(props, "detailSampleDist");
            settings.detailSampleMaxError   = getFloat(props, "detailSampleMaxError");

            return settings;
        }
    }

    private static float getFloat(Properties props, String key) {
        return Float.parseFloat(props.getProperty(key));
    }

    private static int getInteger(Properties props, String key) {
        return Integer.parseInt(props.getProperty(key));
    }

    private static boolean getBoolean(Properties props, String key) {
        return Boolean.parseBoolean(props.getProperty(key));
    }

    private static String getString(Properties props, String key) {
        return props.getProperty(key);
    }
}
