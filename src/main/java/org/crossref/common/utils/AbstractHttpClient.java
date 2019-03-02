package org.crossref.common.utils;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.crossref.common.rest.api.IHttpClient;

/**
 * Base class for HTTP client implementations.
 * 
 * @author joe.aparo
 */
public abstract class AbstractHttpClient implements IHttpClient {
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    private static final int DEFAULT_ACTIVITY_TIMEOUT = 30000;
    
    private final Logger logger = LogUtils.getLogger();
    private String scheme = null;
    private String host = null;
    private int port = 0;
    private String urlRoot = null;
    private final Map<String, String> commonHeaders = new HashMap<>();
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    private int activityTimeout = DEFAULT_ACTIVITY_TIMEOUT;
    
    /**
     * Constructor with scheme/host/port
     * 
     * @param scheme Http scheme, either "http" or "https"
     * @param host Server host name, or IP address
     * @param port Server port. A value of zero means no port
     */
    public AbstractHttpClient(String scheme, String host, int port) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        
        StringBuilder buf = new StringBuilder();
        buf.append(this.scheme).append("://").append(this.host);
        if (port > 0) {
            buf.append(":").append(port);
        }
        buf.append("/");
        
        urlRoot = buf.toString();
    }

    /**
     * Placeholder for common initialization phase.
     */
    public void initialize() {
    }
    
    /**
     * Get base logger.
     * @return A logger
     */
    protected Logger getLogger() {
        return logger;
    }
    
    /**
     * Get the configured host.
     * @return A host name or ip
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Get the configured http scheme.
     * @return Either "http" or "https"
     */
    public String getScheme() {
        return scheme;
    }
    
    /**
     * Get the configured host port.
     * 
     * @return A server port. A value of zero means no port.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Get the computed root url based on scheme/host/port. The
     * value ends with a forward slash.
     * 
     * @return A root url, e.g. http://foo.com:9000/
     */
    public String getUrlRoot() {
        return urlRoot;
    }
    
    /**
     * Set the connection timeout in MS.
     * @param timeout Timeout value
     */
    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }

    /**
     * Get configured connection timeout.
     * @return Timeout in MS
     */
    public int getConnectTimeout() {
        return this.connectTimeout;
    }
    
    /**
     * Set the socket timeout in MS.
     * @param timeout Timeout value
     */
    public void setSocketTimeout(int timeout) {
        this.socketTimeout = timeout;
    }
    
    /**
     * Get configured socket timeout.
     * @return Timeout in MS
     */

    public int getSocketTimeout() {
        return this.socketTimeout;
    }
    
    /**
     * Set the activity timeout in MS.
     * @param timeout Timeout value
     */
    public void setActivityTimeout(int timeout) {
        this.activityTimeout = timeout;
    }
    
    /**
     * Get configured activity timeout.
     * @return Timeout in MS
     */
    public int getActivityTimeout() {
        return this.activityTimeout;
    }

    /**
     * Set common headers to be included in all requests.
     * @param commonHeaders 
     */
    public void setCommonHeaders(Map<String, String> commonHeaders) {
        this.commonHeaders.clear();
        if (commonHeaders != null) {
            this.commonHeaders.putAll(commonHeaders);
        }
    }
    
    public Map<String, String> getCommonHeaders() {
        return new HashMap<String, String>(this.commonHeaders);
    }
    
    /**
     * Build a query string based on the given map of values.
     * URLEncode string values in the process.
     * 
     * @param args Name/value pairs to form request string from.
     * @return A formatted url query string
     */
    protected String formatQueryArgs(Map<String, Object> args) {
        StringBuilder sb = new StringBuilder("");
		
        for (Map.Entry<String, Object> e : args.entrySet()) {
            sb.append((sb.length() >  0) ? '&' : '?')
            .append(e.getKey()).append("=")
            .append(EncodeUtils.urlEncode(e.getValue().toString()));          
        }

        return sb.toString();
    }

}
