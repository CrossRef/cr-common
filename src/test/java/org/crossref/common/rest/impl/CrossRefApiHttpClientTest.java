package org.crossref.common.rest.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.crossref.common.rest.api.ICrossRefApiClient;
import org.crossref.common.rest.api.IHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 * Test API HTTP client implementation.
 * 
 * @author joe.aparo
 */
public class CrossRefApiHttpClientTest {
    @Mock
    private IHttpClient httpClient;
    
    private ICrossRefApiClient apiClient;
    
    @Before
    public void setupMock() {
        MockitoAnnotations.initMocks(this);
        
        apiClient = new CrossRefApiHttpClient(httpClient);
    }
    
    @Test
    public void getWorks_shouldReturnResponse() {
        
        Map<String, Object> args = new HashMap<>();
        String testResponse = "Doesn't reall matter";
        
        try {
            when(httpClient.get(any(), any(), any())).thenReturn(testResponse);
            
            String response = apiClient.getWorks(args);
            
            Assert.assertTrue(!StringUtils.isEmpty(response) && response.equals(testResponse));
            
        } catch (IOException ex) {
            Logger.getLogger(CrossRefApiHttpClientTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
