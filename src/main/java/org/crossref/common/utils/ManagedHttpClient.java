package org.crossref.common.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
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

/**
 * Implements a robust http client connection based on Apache's http
 * components. The initialization code below was adapted from code 
 * provided by Apache.
 *
 * @author joe.aparo
 */
public final class ManagedHttpClient extends AbstractHttpClient {
    
    private CloseableHttpClient httpClient = null;
    private int maxConnections = 20;
    private int maxConnRequests = 5;

    /**
     * Constructor with scheme/host/port
     * 
     * @param scheme Http scheme, either "http" or "https"
     * @param host Server host name, or IP address
     * @param port Server port
     */
    public ManagedHttpClient(String scheme, String host, int port) {
        super(scheme, host, port);
    }

    /**
     * Set the maximum number of total connections.
     * @param maxConnections 
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
     /**
     * Set the maximum number of concurrent requests per connection. 
     * @param maxConnRequests 
     */
    public void setMaxConnRequests(int maxConnRequests) {
        this.maxConnRequests = maxConnRequests;
    }
    
   /**
     * Initialize the http client connection manager based on current settings.
     */
    @Override
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
        connManager.setSocketConfig(new HttpHost(getHost(), getPort()), socketConfig);
        
        // Validate connections after 1 sec of inactivity
        connManager.setValidateAfterInactivity(getActivityTimeout());

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
        connManager.setConnectionConfig(new HttpHost(getHost(), getPort()), ConnectionConfig.DEFAULT);

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(maxConnections);
        connManager.setDefaultMaxPerRoute(maxConnRequests);
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost(getHost(), getPort())), maxConnRequests);

        // Create global request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.DEFAULT)
            .setExpectContinueEnabled(true)
            .setSocketTimeout(getSocketTimeout())
            .setConnectTimeout(getConnectTimeout())
            .setConnectionRequestTimeout(getActivityTimeout())
            .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
            .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
            .build();

        // Create an HttpClient with the given custom dependencies and configuration.
        this.httpClient = HttpClients.custom()
            .setConnectionManager(connManager)
            .setDefaultRequestConfig(defaultRequestConfig)
            .disableRedirectHandling()
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
            getLogger().warn("Error closing http client connection: " + e.getMessage(), e);
        }
        
        this.httpClient = null;
    }
    
    /**
     * Fetch the content returned at the given path of the http server.
     * 
     * @param path The path, relative to the server root schemd/host/port
     * @param args Query arguments to be added to the path
     * @param callHeaders Additional headers to set for the request
     * 
     * @return A string returned from the path
     * @throws ClientProtocolException
     * @throws IOException 
     */
    @Override
    public String get(String path, Map<String, Object> args, Map<String, String> callHeaders)
        throws IOException {
        
        if (!isOpen()) {
                return null;
        }
        
        HttpGet httpget = new HttpGet(getUrlRoot() + path + formatQueryArgs(args));
        httpget.setProtocolVersion(HttpVersion.HTTP_1_1);
        
        // Add any standard and call-specific headers
        Map<String, String> commonHeaders = getCommonHeaders();
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
        Timer timer = new Timer();
        timer.start();
        CloseableHttpResponse response = httpClient.execute(httpget);        
        timer.stop();
        getLogger().debug("httpClient.execute: " + timer.elapsedMs());
        
        // Extract/return contents of call
        timer.start();
        String resp = EntityUtils.toString(response.getEntity());
        timer.stop();
        getLogger().debug("EntityUtils.toString: " + timer.elapsedMs());        
        
        response.close();
        
        return resp;
    }
    
    /**
     * Check if client is open/active
     */
    private boolean isOpen() {
        return httpClient != null;
    }
}
