/*******************************************************************************
 * Copyright (c) 2010 Yadu.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Yadu - initial API and implementation
 ******************************************************************************/

package code.google.restclient.core;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.log4j.Logger;

import code.google.restclient.common.RCConstants;

/**
 * @author Yaduvendra.Singh
 */
public class Hitter {

    private static final Logger LOG = Logger.getLogger(Hitter.class);
    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    private static ClientConnectionManager conman = null;
    private String proxyHost = RCConstants.SYS_PROXY_ENABLED;
    private int proxyPort = -1;
    private Map<String, String> headers;
    HttpHandler handler;

    public Hitter() {
        configureSysProxy();
        headers = new LinkedHashMap<String, String>();
        handler = new HttpHandler();
    }

    public Hitter(Map<String, String> headers) {
        this();
        this.headers = headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setProxy(String host, int port) {
        proxyHost = host;
        proxyPort = port;
    }

    private void configureSysProxy() {
        try {
            // check System properties
            if ( RCConstants.SYS_PROXY_ENABLED.equals(proxyHost) ) {
                String proxyHostEnvStr = System.getProperty("https.proxyHost");
                String proxyPortENvStr = System.getProperty("https.proxyPort");
                if ( proxyHostEnvStr != null ) {
                    proxyHost = proxyHostEnvStr;
                    proxyPort = Integer.parseInt(proxyPortENvStr);
                } else {
                    proxyHost = "";
                    proxyPort = -1;
                }
            } else if ( RCConstants.SYS_PROXY_DISABLED.equals(proxyHost) ) {
                proxyHost = "";
                proxyPort = -1;
            }
        } catch ( Exception e ) {
            LOG.error("failed to get proxy from system props");
            proxyHost = "";
            proxyPort = -1;
        }
    }

    private boolean isProxyConfigured() {
        if ( proxyHost == null || "".equals(proxyHost) ) return false;
        if ( "DISABLED".equals(proxyHost) ) return false;
        if ( proxyPort == -1 ) return false;

        return true;
    }

    /*
     * Since DefaultHttpClient and its connection manager are all thread-safe, all HttpClient
     * impl configuration should be synchronized. this entire dance is here because we want to use
     * a ThreadSafeClientConnManager and not the SingleClientConnManager.
     */
    private synchronized HttpClient getHttpClient() {
        HttpClient client = null;
        HttpParams params = new SyncBasicHttpParams();

        // set proxy
        if ( isProxyConfigured() ) {
            if ( DEBUG_ENABLED ) LOG.debug("CONFIGURING PROXY TO " + proxyHost + ":" + proxyPort);
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            ConnRouteParams.setDefaultProxy(params, proxy);
        }

        // http params apply at client level so shared by all request
        // they can be overridden by params set at request level
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, RCConstants.DEFAULT_CHARSET);
        HttpProtocolParams.setUserAgent(params, RCConstants.APP_DISPLAY_NAME);
        HttpClientParams.setRedirecting(params, true);

        /*
         HttpConnectionParams.setTcpNoDelay(params, true); HttpConnectionParams.setLinger(params, 30);
         HttpConnectionParams.setConnectionTimeout(params, 30000); HttpConnectionParams.setSoTimeout(params, 30000);
         HttpProtocolParams.setUseExpectContinue(params, false); HttpClientParams.setCookiePolicy(params,
         CookiePolicy.BROWSER_COMPATIBILITY);
         */

        // Registry creation code copied from impl of DefaultHttpClient. It looks we don't have to register
        // a separate Scheme for each weird port
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), RCConstants.PLAIN_SOCKET_PORT));
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), RCConstants.SSL_SOCKET_PORT));

        if ( conman == null ) conman = new ThreadSafeClientConnManager(registry);

        if ( client == null ) {
            client = new DefaultHttpClient(conman);
            ((DefaultHttpClient) client).setParams(params);
            ((DefaultHttpClient) client).setKeepAliveStrategy(new CustomKeepAliveStrategy());
            // set custom request retry handler which doesn't allow retry for POST request
            // HttpRequestRetryHandler retryHandler = new CustomRetryHandler();
            // ((DefaultHttpClient) client).setHttpRequestRetryHandler(retryHandler);

            // client.getParams().setParameter(CoreProtocolPNames.WAIT_FOR_CONTINUE, 10000); // 10 seconds
        }

        return client;
    }

    /**
     * Common method to make GET or POST request
     */
    public HttpHandler hit(String url, String methodName, String body, Map<String, String> requestHeaders) throws Exception {

        if ( HttpGet.METHOD_NAME.equals(methodName) ) {
            if ( DEBUG_ENABLED ) LOG.debug("===> GET " + url);
            return hit(url, new HttpGet(url), requestHeaders);

        } else if ( HttpHead.METHOD_NAME.equals(methodName) ) {
            if ( DEBUG_ENABLED ) LOG.debug("===> HEAD " + url);
            return hit(url, new HttpHead(url), requestHeaders);

        } else if ( HttpDelete.METHOD_NAME.equals(methodName) ) {
            if ( DEBUG_ENABLED ) LOG.debug("===> DELETE " + url);
            return hit(url, new HttpDelete(url), requestHeaders);

        } else if ( isEntityEnclosingMethod(methodName) ) {
            if ( body == null ) body = "";
            HttpEntity entity = null;
            if ( body.startsWith("@") ) {
                String path = Pattern.compile("^@").matcher(body).replaceAll("");
                entity = new FileEntity(new File(path), ""); // second argument is contentType e.g.
                // "text/plain; charset=\"UTF-8\"");
            } else {
                entity = new StringEntity(body, RCConstants.DEFAULT_CHARSET);
            }

            if ( DEBUG_ENABLED ) {
                String dump = body;
                if ( Pattern.compile("pass", Pattern.CASE_INSENSITIVE).matcher(dump).matches() ) dump = "*PASSWORD*SCRUBBED*";
                if ( DEBUG_ENABLED ) LOG.debug("===> " + methodName.toUpperCase() + " " + url + " <=== " + dump);
            }

            return hit(url, methodName, entity, requestHeaders);
        } else {
            throw new IllegalArgumentException("hit(): Unsupported method => " + methodName);
        }
    }

    /**
     * Method to make POST request by sending file
     */
    public HttpHandler hit(String url, File body, Map<String, String> requestHeaders) throws Exception {

        FileEntity entity = new FileEntity(body, ""); // second argument is contentType e.g.
        // "text/plain; charset=\"UTF-8\"");
        return hit(url, entity, requestHeaders);
    }

    /**
     * Method to make POST request by sending input stream
     */
    public HttpHandler hit(String url, InputStream body, Map<String, String> requestHeaders) throws Exception {

        InputStreamEntity entity = new InputStreamEntity(body, -1); // content length is unknown so -1
        return hit(url, entity, requestHeaders);
    }

    public HttpHandler hit(String url, HttpEntity entity, Map<String, String> requestHeaders) throws Exception {
        return hit(url, RCConstants.POST, entity, requestHeaders);
    }

    /**
     * Method to make POST or PUT request by sending http entity (as body)
     */
    public HttpHandler hit(String url, String methodName, HttpEntity entity, Map<String, String> requestHeaders) throws Exception {

        // url = encodeUrl(url);
        if ( DEBUG_ENABLED ) LOG.debug("hit() - hitting service with entity; url=> " + url);

        if ( HttpPost.METHOD_NAME.equals(methodName) ) { // POST
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(entity);
            handler.setPostEntity(entity); // handler uses this entity to get content headers
            if ( DEBUG_ENABLED )
                LOG.debug("hit() - content type: " + httpPost.getEntity().getContentType() + "\ncontent encoding: "
                    + httpPost.getEntity().getContentEncoding() + "\ncontent length: " + httpPost.getEntity().getContentLength());
            return hit(url, httpPost, requestHeaders);
        } else if ( HttpPut.METHOD_NAME.equals(methodName) ) { // PUT
            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(entity);
            handler.setPostEntity(entity); // handler uses this entity to get content headers
            if ( DEBUG_ENABLED )
                LOG.debug("hit() - content type: " + httpPut.getEntity().getContentType() + "\ncontent encoding: "
                    + httpPut.getEntity().getContentEncoding() + "\ncontent length: " + httpPut.getEntity().getContentLength());
            return hit(url, httpPut, requestHeaders);
        }

        return null;
    }

    private HttpHandler hit(String url, HttpUriRequest request, Map<String, String> requestHeaders) throws Exception {

        if ( DEBUG_ENABLED ) LOG.debug("hit() - hitting url (params encoded) --> " + url);

        HttpResponse response = null;
        // HttpHandler handler = new HttpHandler();
        HttpClient client = getHttpClient();

        // set request headers
        Map<String, String> hs = new LinkedHashMap<String, String>();
        if ( headers != null ) hs.putAll(headers);
        if ( requestHeaders != null ) hs.putAll(requestHeaders);
        if ( !hs.isEmpty() ) {
            for ( String header : hs.keySet() )
                request.setHeader(new BasicHeader(header, hs.get(header)));
        }
        handler.setRequest(request);

        response = client.execute(request); // **** execute request ****
        handler.setResponse(response);
        return handler;

    }

    private boolean isEntityEnclosingMethod(String methodName) {
        return HttpPost.METHOD_NAME.equals(methodName) || HttpPut.METHOD_NAME.equals(methodName);
    }

    /*
    public static void main(String[] args) {
    	Hitter hitter = new Hitter();
    	//String url = "http://localhost.mlbam.com:8080/stage/v1.1/14/content/item/viewImage?fileLocation=/images/04022010/6845668/Sunset.jpg";
    	HttpHandler httpHandler = null;
    	String url = "http://localhost.mlbam.com:8080/webtest/controller";
    	try {
    		httpHandler = hitter.hit(url, "PUT", "aa", null);
    	} catch (Exception e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	LOG.debug("Response Headers: \n" + httpHandler.getResponseHeaders());
    }
    */
}
