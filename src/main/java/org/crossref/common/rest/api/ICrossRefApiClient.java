package org.crossref.common.rest.api;

import java.io.IOException;
import java.util.Map;

/**
 * Represents calls to the CrossRef JSON API service.
 * 
 * @author joe.aparo
 */
public interface ICrossRefApiClient {
    
    /**
     * Fetch works matching the given selection criteria.
     * 
     * @param args Selection criteria arguments
     * @return A JSON response string
     * 
     * @throws IOException 
     */
    public String getWorks(Map<String, Object> args) throws IOException;
}
