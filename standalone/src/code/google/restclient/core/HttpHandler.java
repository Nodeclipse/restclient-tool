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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import code.google.restclient.common.RCConstants;
import code.google.restclient.common.RCUtil;
import code.google.restclient.exception.RCException;

/**
 * Purpose of this bean is to keep information of actual request and response with its members HttpUriRequest, HttpResponse, request entity
 * (HttpEntity), response entity (HttpEntity) where reqBodyEntity is set by HitterClient before invoking Hitter.hit()
 * 
 * @author Yaduvendra.Singh
 */
public class HttpHandler {

    private HttpUriRequest request;
    private HttpResponse response;
    private HttpEntity respEntity;
    private HttpEntity reqBodyEntity;

    public HttpUriRequest getRequest() {
        return request;
    }

    public void setRequest(HttpUriRequest request) {
        this.request = request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
        if ( response != null ) respEntity = response.getEntity();
    }

    public HttpEntity getReqBodyEntity() {
        return reqBodyEntity;
    }

    public void setReqBodyEntity(HttpEntity reqBodyEntity) {
        this.reqBodyEntity = reqBodyEntity;
    }

    /* *********** Request Elements ************ */
    public String getRequestLine() {
        if ( request != null && request.getRequestLine() != null ) return request.getRequestLine().toString();
        return null;
    }

    public Map<String, String> getRequestHeaders() {
        Map<String, String> reqHeaders = new LinkedHashMap<String, String>();
        if ( request != null ) {
            Header[] headers = request.getAllHeaders();
            for ( Header header : headers ) {
                if ( !RCUtil.isEmpty(header.getName()) ) {
                    reqHeaders.put(header.getName(), header.getValue());
                }
            }
        }
        // Include headers which are added by http components by default. Additional headers to display in req pane
        if ( reqBodyEntity != null ) {
            reqHeaders.put("Content-Length", "" + reqBodyEntity.getContentLength());
            reqHeaders.put("Content-Type", reqBodyEntity.getContentType().getValue());
        }
        String host = getUri().getHost();
        String port = getUri().getPort() == -1 ? "" : ":" + getUri().getPort();
        reqHeaders.put("Host", host + port);
        // With current connection scheme configured in Hitter.getHttpClient()
        reqHeaders.put("Connection", "Keep-Alive");
        // It is set to http params in Hitter.getHttpClient()
        reqHeaders.put("User-Agent", RCConstants.APP_DISPLAY_NAME);

        return reqHeaders;
    }

    public URI getUri() {
        if ( request != null ) return request.getURI();
        return null;
    }

    public String getUrl() throws RCException {
        try {
            if ( getUri() != null ) return getUri().toURL().toString();
        } catch ( MalformedURLException e ) {
            throw new RCException("getUrl(): error while converting uri to url", e);
        }
        return null;
    }

    public String getProtocolVersion() {
        if ( request != null ) return request.getProtocolVersion().toString();
        return null;
    }

    public void abort() {
        if ( request != null ) request.abort();
    }

    public boolean isReqAborted() {
        if ( request != null ) return request.isAborted();
        return true;
    }

    /* *********** Response Elements ************ */

    public String getStatusLine() {
        if ( response != null && response.getStatusLine() != null ) return response.getStatusLine().toString();
        return null;
    }

    public String getResponseContentType() {
        if ( respEntity != null ) {
            Header header = respEntity.getContentType();
            if ( header != null ) return header.getValue();
        }
        return null;
    }

    public long getResponseContentLength() {
        if ( respEntity != null ) {
            return respEntity.getContentLength();
        }
        return -1L;
    }

    public String getResponseContentEncoding() {
        if ( respEntity != null ) {
            Header header = respEntity.getContentEncoding();
            if ( header != null ) return header.getValue();
        }
        return null;
    }

    public Map<String, String> getResponseHeaders() {
        Map<String, String> respHeaders = new HashMap<String, String>();
        if ( response != null ) {
            Header[] headers = response.getAllHeaders();
            for ( Header header : headers ) {
                if ( !RCUtil.isEmpty(header.getName()) ) respHeaders.put(header.getName(), header.getValue());
            }
        }
        return respHeaders;
    }

    public InputStream getResponseStream() throws RCException {
        try {
            if ( respEntity != null ) return respEntity.getContent();

        } catch ( Exception e ) {
            throw new RCException("getResponseStream(): error while reading response", e);
        }
        return null;
    }

    /**
     * This method should be called to release connection after calling getResponseStream()
     * 
     * @throws RCException
     */
    public void closeConnection() throws RCException {
        try {
            EntityUtils.consume(respEntity);
        } catch ( Exception e ) {
            throw new RCException("closeConnection(): error while closing connection", e);
        }
    }

}
