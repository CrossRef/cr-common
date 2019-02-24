package org.crossref.common.rest.impl;

import org.crossref.common.rest.api.ICrossRefApiClient;
import org.crossref.common.rest.api.IHttpClient;
import org.junit.Before;
import org.mockito.Mock;
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
}
