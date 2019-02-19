package org.crossref.common.utils;

import java.io.File;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 * Defines static utility methods for working with classpath resources.
 * 
 * @author joe.aparo
 */
public final class ResourceUtils {
    
    private static final Logger logger = LogUtils.getLogger();
    
    /**
     * List all files in a given resource folder.
     * 
     * @param path The resource path to list files from
     * @return An array of file objects
     */
    public static File[] getResourceFolderFiles (String path) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            URL url = loader.getResource(path);
            String dir = url.getPath();
            return new File(dir).listFiles();
        } catch(Exception ex) {
            logger.warn("Unable to access resource folder: " + path);
            return null;
        }
    }
}
