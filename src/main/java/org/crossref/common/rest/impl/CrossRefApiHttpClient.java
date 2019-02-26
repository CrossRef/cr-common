package org.crossref.common.rest.impl;

import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.crossref.common.utils.LogUtils;
import org.crossref.common.rest.api.ICrossRefApiClient;
import org.crossref.common.rest.api.IHttpClient;
import org.crossref.common.utils.Timer;

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
     * @param httpClient 
     */
    public CrossRefApiHttpClient(IHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public JSONArray getWorks(Map<String, Object> args) throws IOException {
        String worksJson = httpClient.get("works", args, null);

        // Parse the response
        try {
            Timer timer = new Timer();
            timer.start();
	    JSONObject json = new JSONObject(worksJson);
            JSONArray arr = json.getJSONObject("message").optJSONArray("items");
            timer.stop();
            
            log.debug("new.JSONObject: " + timer.elapsedMs()); 
                        
            return arr;
            
	} catch (JSONException ex) {
            log.error("Error parsing API response string: " + worksJson, ex);
	    return new JSONArray();
	}
    }
}
