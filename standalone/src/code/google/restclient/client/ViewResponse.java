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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Yaduvendra.Singh
 */
public class ViewResponse {
    private static final Logger LOG = Logger.getLogger(ViewResponse.class);
    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    private String statusLine;
    private String url;
    private Map<String, String> headers = new HashMap<String, String>();
    private String bodyStr;
    private File bodyFile;
    private String contentType;

    public String getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
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

    public File getBodyFile() {
        return bodyFile;
    }

    public void setBodyFile(File bodyFile) {
        this.bodyFile = bodyFile;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDisplayHeaderPart() {
        StringBuilder sb = new StringBuilder();
        if ( statusLine != null ) {
            sb.append(statusLine);
            for ( String key : headers.keySet() )
                sb.append("\n" + key + ": " + headers.get(key));
        }
        return sb.toString();
    }

    public String getDisplayBodyPart() {
        String displayBodyPart = null;
        try {
            if ( bodyStr != null ) displayBodyPart = bodyStr;
            else if ( bodyFile != null ) displayBodyPart = "[content @ " + bodyFile.getCanonicalPath() + "]";
        } catch ( IOException e ) {
            LOG.error("prepareDisplayBodyPart(): error while getting path of output file => ", e);
        }
        return displayBodyPart;
    }

    /*
    public String getDisplayHtml() {
        String displayBodyPart = getDisplayBodyPart();

        if ( displayBodyPart != null && bodyFile != null ) {
            String filePath = displayBodyPart.replace("[content @ ", "").replace("]", "");
            if ( filePath != null && contentType != null && contentType.startsWith("image") ) {
                displayBodyPart = "<img src=\"" + filePath + "\">";
            } else displayBodyPart = "[content @ " + "<a href=\"file:///" + filePath + "\" >" + filePath + "</a>]";
        }
        return displayBodyPart;
    }
    */

    public String getBodyFilePath() {
        try {
            if ( bodyFile != null ) return bodyFile.getCanonicalPath();
        } catch ( IOException e ) {
            LOG.warn("getBodyFilePath() - could not find response body file", e);
        }
        return null;
    }

    public void clear() {
        statusLine = null;
        url = null;
        headers.clear();
        bodyStr = null;
        contentType = null;
        bodyFile = null;
    }

}
