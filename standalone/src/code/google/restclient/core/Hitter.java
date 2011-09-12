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

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.log4j.Logger;

import code.google.restclient.common.RCConstants;
import code.google.restclient.common.RCUtil;

/**
 * @author Yaduvendra.Singh
 */
public class Hitter {

    private static final Logger LOG = Logger.getLogger(Hitter.class);
    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    private static HttpClient client = null;
    private static ClientConnectionManager conman = null;
    private String proxyHost = RCConstants.SYS_PROXY_ENABLED;
    private int proxyPort = -1;
    private Map<String, String> headers;

    // Dummy object for synchronizing getHttpClient()
    private final Object lock;

    public Hitter() {
        configureSysProxy();
        headers = new LinkedHashMap<String, String>();
        lock = new Object();
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

    /**
     * Method to make POST or PUT request by sending http entity (as body)
     */
    public void hit(String url, String methodName, HttpHandler handler, Map<String, String> requestHeaders) throws Exception {

        if ( DEBUG_ENABLED ) LOG.debug("hit() - method => " + methodName + ", url => " + url);

        if ( HttpGet.METHOD_NAME.equals(methodName) ) {
            if ( DEBUG_ENABLED ) LOG.debug("hit() - ===> GET " + url);
            hit(url, new HttpGet(url), handler, requestHeaders);
        } else if ( HttpHead.METHOD_NAME.equals(methodName) ) {
            if ( DEBUG_ENABLED ) LOG.debug("hit() - ===> HEAD " + url);
            hit(url, new HttpHead(url), handler, requestHeaders);
        } else if ( HttpDelete.METHOD_NAME.equals(methodName) ) {
            if ( DEBUG_ENABLED ) LOG.debug("hit() - ===> DELETE " + url);
            hit(url, new HttpDelete(url), handler, requestHeaders);
        } else if ( HttpOptions.METHOD_NAME.equals(methodName) ) {
            if ( DEBUG_ENABLED ) LOG.debug("hit() - ===> OPTIONS " + url);
            hit(url, new HttpOptions(url), handler, requestHeaders);
        } else if ( HttpTrace.METHOD_NAME.equals(methodName) ) {
            if ( DEBUG_ENABLED ) LOG.debug("hit() - ===> TRACE " + url);
            hit(url, new HttpTrace(url), handler, requestHeaders);
        } else if ( HttpPost.METHOD_NAME.equals(methodName) ) { // POST
            if ( DEBUG_ENABLED ) LOG.debug("hit() - ===> POST " + url);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(handler.getReqBodyEntity());
            hit(url, httpPost, handler, requestHeaders);
        } else if ( HttpPut.METHOD_NAME.equals(methodName) ) { // PUT
            if ( DEBUG_ENABLED ) LOG.debug("hit() - ===> PUT " + url);
            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(handler.getReqBodyEntity());
            hit(url, httpPut, handler, requestHeaders);
        } else {
            throw new IllegalArgumentException("hit(): Unsupported method => " + methodName);
        }
    }

    private HttpHandler hit(String url, HttpUriRequest request, HttpHandler handler, Map<String, String> requestHeaders) throws Exception {

        if ( DEBUG_ENABLED ) LOG.debug("hit() - hitting url (params encoded) --> " + url);

        HttpResponse response = null;
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

    /*
     * Since DefaultHttpClient and its connection manager are all thread-safe, all HttpClient
     * impl configuration should be synchronized. this entire dance is here because we want to use
     * a ThreadSafeClientConnManager and not the SingleClientConnManager.
     */
    private HttpClient getHttpClient() throws KeyManagementException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        // Early exit: already configured. Want all the gets to be fast so not synchronizing it
        if ( client != null ) return client;

        synchronized ( lock ) {
            HttpParams params = new SyncBasicHttpParams();

            // Set proxy
            if ( isProxyConfigured() ) {
                if ( DEBUG_ENABLED ) LOG.debug("CONFIGURING PROXY TO => " + proxyHost + ":" + proxyPort);
                HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                ConnRouteParams.setDefaultProxy(params, proxy);
            }

            // http params apply at client level so shared by all request. They can be overridden by params set at request level.
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, RCConstants.DEFAULT_CHARSET);
            HttpProtocolParams.setUserAgent(params, RCConstants.APP_DISPLAY_NAME);
            HttpClientParams.setRedirecting(params, true);

            /*
             HttpConnectionParams.setTcpNoDelay(params, true);
             HttpConnectionParams.setLinger(params, 30);
             HttpConnectionParams.setConnectionTimeout(params, 30000);
             HttpConnectionParams.setSoTimeout(params, 30000);
             HttpProtocolParams.setUseExpectContinue(params, false);
             HttpClientParams.setCookiePolicy(params,
             CookiePolicy.BROWSER_COMPATIBILITY);
             */

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(getPlainScheme());
            registry.register(getSSLScheme(isDisabledCertVerifier(), isDisabledHostVerifier()));

            if ( conman == null ) conman = new ThreadSafeClientConnManager(registry);

            if ( client == null ) {
                client = new DefaultHttpClient(conman);
                ((DefaultHttpClient) client).setParams(params);
                ((DefaultHttpClient) client).setKeepAliveStrategy(new CustomKeepAliveStrategy());
                /*
                // set custom request retry handler which doesn't allow retry for POST request
                HttpRequestRetryHandler retryHandler = new CustomRetryHandler();
                ((DefaultHttpClient) client).setHttpRequestRetryHandler(retryHandler);
                client.getParams().setParameter(CoreProtocolPNames.WAIT_FOR_CONTINUE, 10000); // 10 seconds
                */

            }

            return client;
        }
    }

    private Scheme getPlainScheme() {
        return new Scheme("http", RCConstants.PLAIN_SOCKET_PORT, PlainSocketFactory.getSocketFactory());
    }

    private Scheme getSSLScheme(boolean disableVerifyCert, boolean disableVerifyHost) throws NoSuchAlgorithmException, KeyManagementException,
                    UnrecoverableKeyException, KeyStoreException {
        SSLSocketFactory sslFactory = null;

        if ( isUseSelfSignedCertVerifier() ) {
            TrustSelfSignedStrategy selfSignedStrategy = new TrustSelfSignedStrategy();

            // AllowAllHostnameVerifier doesn't verify host names contained in SSL certificate. It should not be set in
            // production environment. It may allow man in middle attack. Other host name verifiers for specific needs
            // are StrictHostnameVerifier and BrowserCompatHostnameVerifier.
            if ( disableVerifyHost ) sslFactory = new SSLSocketFactory(selfSignedStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            else sslFactory = new SSLSocketFactory(selfSignedStrategy); // BROWSER_COMPATIBLE_HOSTNAME_VERIFIER is used by default

        } else {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            if ( disableVerifyCert ) sslContext.init(null, new TrustManager[] { getNoCheckTrustManager() }, null);
            else sslContext.init(null, null, null);

            if ( disableVerifyHost ) sslFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            else sslFactory = new SSLSocketFactory(sslContext);
        }

        return new Scheme("https", RCConstants.SSL_SOCKET_PORT, sslFactory);
    }

    private TrustManager getNoCheckTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // Do nothing
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // Do nothing
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
    }

    /**
     * First checks property disable.ssl.cert.verifier as system property if not found it uses configured one.
     * 
     * @return
     */
    private boolean isDisabledCertVerifier() {
        String disable = RCUtil.getSSLOverrideProperty("disable.ssl.cert.verifier");
        if ( disable == null ) return RCConstants.DISABLE_SSL_CERT_VERIFIER;
        else return new Boolean(disable);
    }

    /**
     * First checks property disable.host.name.verifier as system property if not found it uses configured one.
     * 
     * @return
     */
    private boolean isDisabledHostVerifier() {
        String disable = RCUtil.getSSLOverrideProperty("disable.host.name.verifier");
        if ( disable == null ) return RCConstants.DISABLE_HOST_NAME_VERIFIER;
        else return new Boolean(disable);
    }

    /**
     * First checks property disable.host.name.verifier as system property if not found it uses configured one.
     * 
     * @return
     */
    private boolean isUseSelfSignedCertVerifier() {
        String disable = RCUtil.getSSLOverrideProperty("use.self.signed.cert.verifier");
        if ( disable == null ) return RCConstants.USE_SELF_SIGNED_CERT_VERIFIER;
        else return new Boolean(disable);
    }

    /*
    public static void main(String[] args) {
        Hitter hitter = new Hitter();
        String url = "https://localhost.mlbam.com:8443/";
        HttpHandler httpHandler = new HttpHandler();

        try {
            hitter.hit(url, "GET", httpHandler, null);
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LOG.debug("Response Headers: \n" + httpHandler.getResponseHeaders());
        System.out.println("Response Headers: \n" + httpHandler.getResponseHeaders());
    }
    */

}
