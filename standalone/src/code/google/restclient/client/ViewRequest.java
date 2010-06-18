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

package code.google.restclient.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import code.google.restclient.common.RCConstants;
import code.google.restclient.common.RCUtil;
import code.google.restclient.mime.MimeTypeUtil;


/**
 * @author Yaduvendra.Singh
 */
public class ViewRequest {
    private static final Logger LOG = Logger.getLogger(ViewRequest.class);
    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    private String method;
    private String url;
    private String headersStr;
    private String paramsStr;
    private String bodyStr;
    private String fileParamName;
    private String filePath;
    private boolean isTextBody = true;
    private boolean isMultipart = false;
    private boolean isEncodeBody = false;

    private String reqLine;
    private String host;
    private int port;
    private String scheme;
    private String path;
    private String queryStrRaw;
    private String protocolVersion;
    private Map<String, String> headers = new HashMap<String, String>();

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBodyStr() {
        return bodyStr;
    }

    public void setBodyStr(String bodyStr) {
        this.bodyStr = bodyStr;
    }

    public String getFileParamName() {
        return fileParamName;
    }

    public void setFileParamName(String fileParamName) {
        this.fileParamName = fileParamName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isTextBody() {
        return isTextBody;
    }

    public void setTextBody(boolean isTextBody) {
        this.isTextBody = isTextBody;
    }

    public boolean isMultipart() {
        return isMultipart;
    }

    public void setMultipart(boolean isMultipart) {
        this.isMultipart = isMultipart;
    }

    public boolean isEncodeBody() {
        return isEncodeBody;
    }

    public void setEncodeBody(boolean encodeBody) {
        isEncodeBody = encodeBody;
    }

    public String getReqLine() {
        return reqLine;
    }

    public void setReqLine(String reqLine) {
        this.reqLine = reqLine;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQueryStrRaw() {
        return queryStrRaw;
    }

    public void setQueryStrRaw(String queryStrRaw) {
        this.queryStrRaw = queryStrRaw;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHeadersStr() {
        return headersStr;
    }

    public void setHeadersStr(String headersStr) {
        this.headersStr = headersStr;
    }

    public String getParamsStr() {
        return paramsStr;
    }

    public void setParamsStr(String paramsStr) {
        this.paramsStr = paramsStr;
    }

    public String getUrlToHit() throws UnsupportedEncodingException {
        if ( url == null || "".equals(url) ) throw new IllegalArgumentException("URL can not be null");
        String urlToHit = url;
        String paramsString = getParamsBodyStr(); // paramsStr;

        if ( !getParams().isEmpty() ) {
            if ( !RCUtil.isEntityEnclosingMethod(method)
                || (RCUtil.isEntityEnclosingMethod(method) && !isMultipart && (!isEmptyBodyStr() || !RCUtil.isEmpty(filePath))) ) {
                if ( !urlToHit.contains("?") ) urlToHit += "?";
                else paramsString = "&" + paramsString;
                urlToHit += paramsString;
            }
        }
        return RCUtil.encodeUrl(urlToHit);
    }

    public Map<String, String> getInputHeaders() throws UnsupportedEncodingException {
        return RCUtil.populateMapFromStr(headersStr);
    }

    public Map<String, String> getParams() {
        return RCUtil.populateMapFromStr(paramsStr);
    }

    public String getBodyToPost() throws IOException { // if non multipart post
        String body = null;
        if ( !RCUtil.isEmpty(filePath) ) body = "@" + filePath;
        else if ( !isEmptyBodyStr() ) body = bodyStr;
        // else if( !getParams().isEmpty() ) body = getParamsBodyStr();

        return body;
    }

    public String getParamsBodyStr() throws UnsupportedEncodingException {
        String paramsString = paramsStr;
        if ( !RCUtil.isEntityEnclosingMethod(method) || (RCUtil.isEntityEnclosingMethod(method) && !(RCUtil.isEmpty(filePath) && isEmptyBodyStr())) ) {
            // encode '&' if they are going to be part of url
            paramsString = paramsString.replaceAll("&", RCUtil.encode("&"));
        }
        paramsString = paramsString.replaceAll("\n", "&").replaceAll("\r", "");

        return paramsString;
    }

    public String getDisplayHeaderPart() {
        StringBuilder sb = new StringBuilder();
        try {
            if ( reqLine != null ) {
                // HTTP1.1 specifies to use absolute URL if not sending 'host' header. httpcomponents
                // sends 'host' header by default so not using absolute URL here
                sb.append(getShortReqLine());
                for ( String key : headers.keySet() )
                    sb.append("\n" + key + ": " + headers.get(key));
            }
        } catch ( Exception e ) {
            LOG.error("getDisplayHeaderPart(): error while preaparing req headers to display => ", e);
        }
        return sb.toString();
    }

    public String getDisplayBodyPart() {
        StringBuilder displayBodyPart = new StringBuilder();
        try {
            if ( RCUtil.isEntityEnclosingMethod(method) ) {
                if ( isMultipart ) {
                    if ( !getParams().isEmpty() ) displayBodyPart.append(getParamsBodyStr());

                    if ( !RCUtil.isEmpty(fileParamName) && !RCUtil.isEmpty(filePath) ) {
                        if ( !getParams().isEmpty() ) displayBodyPart.append("&");
                        displayBodyPart.append(fileParamName + "=");
                        if ( isTextBodyOK() && getFileSize() != 0 && getFileSize() <= 5 ) {
                            displayBodyPart.append(RCUtil.getFileContent(filePath, false));
                        } else displayBodyPart.append("[content @ " + filePath + "]");
                    }
                } else if ( !RCUtil.isEmpty(filePath) ) {
                    float size = (float) getFileSize() / 1024; // Kilo Bytes
                    if ( isTextBodyOK() && size != 0 && size <= 5 ) {
                        displayBodyPart.append(RCUtil.getFileContent(filePath, false));
                    } else displayBodyPart.append("[content @ " + filePath + "]");
                } else if ( !isEmptyBodyStr() ) displayBodyPart.append(bodyStr);
                else if ( !getParams().isEmpty() ) displayBodyPart.append(getParamsBodyStr());
            }
        } catch ( Exception e ) {
            LOG.error("getDisplayBodyPart(): error while preparing req body to display => ", e);
        }

        return displayBodyPart.toString();
    }

    private boolean isEmptyBodyStr() {
        // if( !RCUtil.isEmpty(bodyStr) && !RCConstants.BODY_TEXT.equals(bodyStr) ) return false;
        if ( !RCUtil.isEmpty(bodyStr) && !RCConstants.BODY_TEXT.equals(bodyStr) ) return false;
        else return true;
    }

    public boolean isPostParams() throws IOException {
        if ( RCUtil.isEmpty(filePath) && RCUtil.isEmpty(bodyStr) && !getParams().isEmpty() ) return true;
        else return false;
    }

    private long getFileSize() {
        File file = new File(filePath);
        long size = 0;
        if ( file.exists() ) size = file.length();
        return size;
    }

    private String getShortReqLine() {
        String reqLineShort =
            method + " " + (RCUtil.isEmpty(path) ? "/" : path) + (RCUtil.isEmpty(queryStrRaw) ? "" : "?" + queryStrRaw) + " " + protocolVersion;
        return reqLineShort;
    }

    private boolean isTextBodyOK() {
        LOG.debug("isTextBodyOK() - here ...");
        boolean isText = isTextBody;
        try {
            if ( !RCUtil.isEmpty(filePath) ) {
                File file = new File(filePath);
                String mimeType = MimeTypeUtil.getMimeType(file); // use stream first
                // use extension
                if ( "application/octet-stream".equals(mimeType) ) mimeType = MimeTypeUtil.getMimeType(filePath);
                if ( !"application/octet-stream".equals(mimeType) ) {
                    String mediaType = MimeTypeUtil.getMediaType(filePath);
                    if ( "text".equalsIgnoreCase(mediaType) ) isText = true;
                }
            }
        } catch ( Exception e ) {
            LOG.warn("isTextBodyOK(): could not use mime detector to determine mime type." + " Accepting file type selected by user");
        }
        return isText;
    }

    public void clear() {
        method = null;
        url = null;
        headersStr = null;
        paramsStr = null;
        bodyStr = null;
        fileParamName = null;
        filePath = null;
        isTextBody = false;
        isMultipart = false;
        isEncodeBody = false;

        reqLine = null;
        host = null;
        port = -1;
        scheme = null;
        path = null;
        queryStrRaw = null;
        protocolVersion = null;
        headers.clear();
    }

}
