package org.crossref.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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
    
    /**
     * Read a textual resource as an array of strings.
     * 
     * @param resource The resource to read
     * @return An array of strings
     */
    public static List<String> readResourceAsLines(String resource) {
        InputStream is = ResourceUtils.class.getResourceAsStream(resource);
        try {
            return readStreamLines(is);
        } catch (IOException ex) {
            throw new RuntimeException("Error opening resource: " + resource, ex);
        }
    }

    /**
     * Read a textual resource as a string.
     * 
     * @param resource The resource to read
     * @return A string
     */
    public static String readResourceAsString(String resource) {
        return readResourceAsString(resource, true);
    }

    /**
     * Read a textual resource as as string.
     * 
     * @param resource The resource to read
     * @param appendCr Flag indicating whether to retain CR/LFs
     * @return A string
     */
    public static String readResourceAsString(String resource, boolean appendCr) {
        InputStream is = ResourceUtils.class.getResourceAsStream(resource);
        try {
            return readStreamAsString(is, appendCr);
        } catch (IOException ex) {
            throw new RuntimeException("Error reading resource: " + resource, ex);
        }
    }

    /**
     * Read a file as a string.
     * 
     * @param fileName The file to read
     * @return A string
     */
    public static String readFileAsString(String fileName) {
        return readFileAsString(fileName, true);
    }

    /**
     * Read a file as an array of Strings.
     * 
     * @param fileName The file to read
     * @return An array of strings
     */
    public static List<String> readFileLines(String fileName) {
        try {
            return readStreamLines(new FileInputStream(fileName));
        } catch (IOException ex) {
            throw new RuntimeException("Error opening file: " + fileName, ex);
        }
    }

    /**
     * Read a file as a string.
     * 
     * @param fileName The file to read
     * @param appendCr Flag indicating whether to retain CR/LFs
     * @return A string
     */
    public static String readFileAsString(String fileName, boolean appendCr) {
        try {
            return readStreamAsString(new FileInputStream(fileName), appendCr);
        } catch (IOException ex) {
            throw new RuntimeException("Error opening file: " + fileName, ex);
        }
    }

    /**
     * Read a stream as a string.
     * 
     * @param is The stream to read
     * @return A string
     * @throws IOException 
     */
    public static String readStreamAsString(InputStream is) throws IOException {
        return readStreamAsString(is, true);
    }

    /**
     * Read a given input stream as a string.
     * 
     * @param is Textual input stream
     * @param appendCr Flag indicating whether to retain CR/LFs
     * @return A string
     * @throws IOException 
     */
    public static String readStreamAsString(InputStream is, boolean appendCr) throws IOException {
        StringBuilder buffer = new StringBuilder(1000);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        String line;
        while((line = in.readLine()) != null) {
            buffer.append(line);
            if (appendCr) {
                buffer.append("\n");
            }
        }

        return buffer.toString();
    }

    /**
     * Read a given input stream as an array of strings.
     * 
     * @param is Textual input stream
     * @return An array of strings
     * @throws IOException 
     */
    public static List<String> readStreamLines(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        List<String> lines = new ArrayList<>(1000);

        String line;
        while((line = in.readLine()) != null) {
            if (!StringUtils.isEmpty(line)) {
                lines.add(line);
            }
        }

        return lines;
    }
}
