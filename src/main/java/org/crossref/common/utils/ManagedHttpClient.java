package org.crossref.common.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParser;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.LineParser;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.crossref.common.rest.api.IHttpClient;

/**
 * Implements a robust http client connection based on Apache's http
 * components. The initialization code below was adapted from code 
 * provided by Apache.
 *
 * @author joe.aparo
 */
public final class ManagedHttpClient implements IHttpClient {
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    private static final int DEFAULT_ACTIVITY_TIMEOUT = 30000;
    private static final String URL_ROOT_FMT = "%s://%s";
    
    private CloseableHttpClient httpClient = null;
    private String scheme = null;
    private String host = null;
    private int port = 0;
    private Map<String, String> commonHeaders = new HashMap<>();
    String urlRoot = null;
    int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    int activityTimeout = DEFAULT_ACTIVITY_TIMEOUT;

    Logger log = LogUtils.getLogger();
    
    /**
     * Constructor with scheme/host/port
     * 
     * @param scheme Http scheme, either "http" or "https"
     * @param host Server host name, or IP address
     * @param port Server port
     */
    public ManagedHttpClient(String scheme, String host, int port) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.commonHeaders = commonHeaders;
        
        StringBuilder buf = new StringBuilder();
        buf.append(this.scheme).append("://").append(this.host);
        if (port > 0) {
            buf.append(":").append(port);
        }
        buf.append("/");
        
        urlRoot = buf.toString();
        
        initialize();
    }

    /**
     * Set the connection timeout in MS.
     * @param timeout Timeout value
     */
    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }

    /**
     * Set the socket timeout in MS.
     * @param timeout Timeout value
     */
    public void setSocketTimeout(int timeout) {
        this.socketTimeout = timeout;
    }

    /**
     * Set the activity timeout in MS.
     * @param timeout Timeout value
     */
    public void setActivityTimeout(int timeout) {
        this.activityTimeout = timeout;
    }
    
    /**
     * Set common headers to be included in all requests.
     * @param commonHeaders 
     */
    public void setCommonHeaders(Map<String, String> commonHeaders) {
        commonHeaders.clear();
        if (commonHeaders != null) {
            this.commonHeaders.putAll(commonHeaders);
        }
    }
    
    /**
     * Initialize the http client connection manager based on current settings.
     */
    public void initialize() {
        // Already open
        if (isOpen()) {
            return;
        }
		
        // Hook into custom message parser / writer to tailor the way HTTP
        // messages are parsed from and written out to the data stream.
        HttpMessageParserFactory<HttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {
            @Override
            public HttpMessageParser<HttpResponse> create(
                SessionInputBuffer buffer, MessageConstraints constraints) {
                LineParser lineParser = new BasicLineParser() {

                    @Override
                    public Header parseHeader(final CharArrayBuffer buffer) {
                        try {
                            return super.parseHeader(buffer);
                        } catch (ParseException ex) {
                            return new BasicHeader(buffer.toString(), null);
                        }
                    }

                };
                
                return new DefaultHttpResponseParser(
                    buffer, lineParser, DefaultHttpResponseFactory.INSTANCE, constraints) {

                    @Override
                    protected boolean reject(final CharArrayBuffer line, int count) {
                        // try to ignore all garbage preceding a status line infinitely
                        return false;
                    }
                };
            }
        };
        
        HttpMessageWriterFactory<HttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();

        // Use a custom connection factory to customize the process of
        // initialization of outgoing HTTP connections. Beside standard connection
        // configuration parameters HTTP connection factory can define message
        // parser / writer routines to be employed by individual connections.
        HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
                requestWriterFactory, responseParserFactory);

        // SSL context for secure connections can be created either based on
        // system or application specific properties.
        SSLContext sslcontext = SSLContexts.createSystemDefault();

        // Create a registry of custom connection socket factories for supported protocol schemes.
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE)
            .register("https", new SSLConnectionSocketFactory(sslcontext))
            .build();

        // Use custom DNS resolver to override the system DNS resolution.
        DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                if (host.equalsIgnoreCase("localhost")) {
                    return new InetAddress[] { InetAddress.getByAddress(new byte[] {127, 0, 0, 1}) };
                } else {
                    return super.resolve(host);
                }
            }
        };

        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry, connFactory, dnsResolver);

        // Create socket configuration
        SocketConfig socketConfig = SocketConfig.custom()
            .setTcpNoDelay(true)
            .build();
        
        // Configure the connection manager to use socket configuration either
        // by default or for a specific host.
        connManager.setDefaultSocketConfig(socketConfig);
        connManager.setSocketConfig(new HttpHost(host, port), socketConfig);
        
        // Validate connections after 1 sec of inactivity
        connManager.setValidateAfterInactivity(activityTimeout);

        // Create message constraints
        MessageConstraints messageConstraints = MessageConstraints.custom()
            .setMaxHeaderCount(200)
            .setMaxLineLength(2000)
            .build();
        
        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setMalformedInputAction(CodingErrorAction.IGNORE)
            .setUnmappableInputAction(CodingErrorAction.IGNORE)
            .setCharset(Consts.UTF_8)
            .setMessageConstraints(messageConstraints)
            .build();
        
        // Configure the connection manager to use connection configuration either
        // by default or for a specific host.
        connManager.setDefaultConnectionConfig(connectionConfig);
        connManager.setConnectionConfig(new HttpHost(host, port), ConnectionConfig.DEFAULT);

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(5);
        connManager.setDefaultMaxPerRoute(1);
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost(host, port)), 1);

        // Create global request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.DEFAULT)
            .setExpectContinueEnabled(true)
            .setSocketTimeout(socketTimeout)
            .setConnectTimeout(connectTimeout)
            .setConnectionRequestTimeout(activityTimeout)
            .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
            .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
            .build();

        // Create an HttpClient with the given custom dependencies and configuration.
        this.httpClient = HttpClients.custom()
            .setConnectionManager(connManager)
            .setDefaultRequestConfig(defaultRequestConfig)
            .build();        
    }
    
    /**
     * Close the http client connection manager.
     */
    public void terminate() {
        if (!isOpen()) {
            return;
        }

        try {
            this.httpClient.close();
        } catch (IOException e) {
            log.warn("Error closing http client connection: " + e.getMessage(), e);
        }
        
        this.httpClient = null;
    }
    
    /**
     * Fetch the content returned at the given path of the http server.
     * 
     * @param path The path, relative to the server root schemd/host/port
     * @return A string returned from the path
     * @throws ClientProtocolException
     * @throws IOException 
     */
    public String get(String path, Map<String, Object> args, Map<String, String> callHeaders)
        throws IOException {
        
        if (!isOpen()) {
                return null;
        }
        
        String[] validArgs = {"rows", "query.bibliographic"};

        HttpGet httpget = new HttpGet(urlRoot + path + formatQueryArgs(args, validArgs));
        
        // Add any standard and call-specific headers
        if (commonHeaders != null) {
            for (Entry<String, String> e : commonHeaders.entrySet()) {
                httpget.addHeader(e.getKey(), e.getValue());
            } 
        }
        if (callHeaders != null) {
            for (Entry<String, String> e : callHeaders.entrySet()) {
                httpget.addHeader(e.getKey(), e.getValue());
            }
        }

        // Make the http call
        CloseableHttpResponse response = httpClient.execute(httpget);
        
        // Extract/return contents of call
        return EntityUtils.toString(response.getEntity());
    }
    
    /**
     * Check if client is open/active
     */
    private boolean isOpen() {
        return httpClient != null;
    }
    
    protected String formatQueryArgs(Map<String, Object> args, String[] valid) {
        StringBuilder sb = new StringBuilder("");
		
        for (String key : valid) {
            if (args.containsKey(key)) {
                try {
                    sb.append((sb.length() >  0) ? '&' : '?')
                    .append(key).append("=")
                    .append(URLEncoder.encode(args.get(key).toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // Warn and fail quietly
                    log.warn("Error encoding value: " + args.get(key));
                }
            }
        }

        return sb.toString();
    }
}
