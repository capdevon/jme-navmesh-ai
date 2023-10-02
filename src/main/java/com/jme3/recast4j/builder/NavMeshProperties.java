package com.jme3.recast4j.builder;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author capdevon
 */
public class NavMeshProperties {

    private static final Logger logger = Logger.getLogger(NavMeshProperties.class.getName());
    
    private NavMeshProperties() {}
    
    /**
     * Export the NavMeshBuildSettings to a file.
     * 
     * @param settings
     * @param file
     */
    public static void save(NavMeshBuildSettings settings, File file) {

        logger.log(Level.INFO, "Saving File={0}", file.getAbsolutePath());
        
        try (OutputStream output = new FileOutputStream(file)) {
            Properties prop = toProperties(settings);
            String comments = "NavMeshBuildSettings properties file";
            prop.store(output, comments);
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error: Failed to save NavMeshBuildSettings!", ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Properties toProperties(T t) 
            throws IntrospectionException, ReflectiveOperationException {

        Class<T> c = (Class<T>) t.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(c, Object.class);
        Properties prop = new Properties();

        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            String name = pd.getName();
            Object o = pd.getReadMethod().invoke(t);
            if (o != null) {
                prop.setProperty(name, o.toString());
            }
        }
        
        return prop;
    }
    
    /**
     * Reads a NavMeshBuildSettings from the input file.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static NavMeshBuildSettings load(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            return load(input);
        }
    }
    
    /**
     * Reads a NavMeshBuildSettings from the input byte stream.
     * 
     * @param input
     * @return
     * @throws IOException
     */
    public static NavMeshBuildSettings load(InputStream input) throws IOException {
        
        Properties props = new Properties();
        props.load(input);

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
