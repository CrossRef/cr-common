package org.crossref.common.rest.api;

import java.io.IOException;
import java.util.Map;

/**
 * Represents a generic HTTP client interface;
 * 
 * @author joe.aparo
 */
public interface IHttpClient {
    /**
     * Generic method for invoking an HTTP get.
     * 
     * @param path The resource path, relative from the client root, to get.
     * @param args Query arguments to be appended to the get request URL
     * @param headers Optional headers to be passed in to the call
     * 
     * @return A String response
     * @throws IOException 
     */
    String get(String path, Map<String, Object> args, Map<String, String> headers) throws IOException;
}
