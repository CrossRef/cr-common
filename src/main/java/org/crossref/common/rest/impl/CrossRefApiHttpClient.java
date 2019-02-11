package org.crossref.common.rest.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.crossref.common.utils.LogUtils;
import org.crossref.common.rest.api.ICrossRefApiClient;
import org.crossref.common.rest.api.IHttpClient;

/**
 * HTTP Implementation of the CrossRefApiClient interface.
 * 
 * @author joe.aparo
 */
public class CrossRefApiHttpClient implements ICrossRefApiClient {

    private final IHttpClient httpClient;
    private final Logger log = LogUtils.getLogger();
    
    /**
     * Constructor accepts generic http connection object
     * 
     * @param apiClient 
     */
    public CrossRefApiHttpClient(IHttpClient apiClient) {
        this.httpClient = apiClient;
    }

    @Override
    public String getWorks(Map<String, Object> args) throws IOException {
        log.debug("CR API Client getWorks");
        
        return httpClient.get("works", args, null);
    }
}
