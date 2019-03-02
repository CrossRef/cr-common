package org.crossref.common.utils;

import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * Implements a basic HTTP connection. In this case,
 * a client connection is created anew on each call.
 * 
 * @author joe.aparo
 */


public class UnmanagedHttpClient extends AbstractHttpClient {

    public UnmanagedHttpClient(String scheme, String host, int port) {
        super(scheme, host, port);
    }

    @Override
    public String get(String path, Map<String, Object> args, Map<String, String> headers) throws IOException {
        RequestConfig.Builder requestBuilder = RequestConfig.custom()
            .setConnectTimeout(getConnectTimeout())
            .setSocketTimeout(getSocketTimeout())
            .setConnectionRequestTimeout(getActivityTimeout());
        
        HttpClientBuilder clientBuilder = HttpClientBuilder.create()
            .setDefaultRequestConfig(requestBuilder.build());
        
        HttpClient httpClient = clientBuilder.build();
        HttpGet httpget = new HttpGet(getUrlRoot() + path + formatQueryArgs(args));
        
        // Add any standard and call-specific headers
        Map<String, String> commonHeaders = getCommonHeaders();
        if (commonHeaders != null) {
            for (Map.Entry<String, String> e : commonHeaders.entrySet()) {
                httpget.addHeader(e.getKey(), e.getValue());
            } 
        }
        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                httpget.addHeader(e.getKey(), e.getValue());
            }
        }

        // Make the http call
        Timer timer = new Timer();
        timer.start();
        HttpResponse response = httpClient.execute(httpget);        
        timer.stop();
        getLogger().debug("httpClient.execute: " + timer.elapsedMs());
        
        // Extract/return contents of call
        timer.start();
        String resp = EntityUtils.toString(response.getEntity());
        timer.stop();
        getLogger().debug("EntityUtils.toString: " + timer.elapsedMs());        
        
        return resp;
    }
    
}
