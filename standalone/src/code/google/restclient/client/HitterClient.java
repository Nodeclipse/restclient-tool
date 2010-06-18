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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import code.google.restclient.common.RCConstants;
import code.google.restclient.common.RCUtil;
import code.google.restclient.core.Hitter;
import code.google.restclient.core.HttpHandler;
import code.google.restclient.exception.RCException;
import code.google.restclient.init.Configurator;
import code.google.restclient.mime.MimeTypeUtil;


/**
 * @author Yaduvendra.Singh
 */
public class HitterClient {

    private static final Logger LOG = Logger.getLogger(HitterClient.class);
    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    private static final SimpleDateFormat SDF = new SimpleDateFormat("MMddyyyyHHmmssSS");
    private volatile boolean abort = false;

    public boolean isAbort() {
        return abort;
    }

    public void setAbort(boolean abort) {
        this.abort = abort;
    }

    public void hit(ViewRequest req, ViewResponse resp) throws RCException {
        Hitter hitter = new Hitter();
        HttpHandler handler = null;
        String url = null;
        // hitter.setProxy("", -1); // set proxy taking values from UI
        try {
            url = req.getUrlToHit();
            if ( !RCUtil.isEntityEnclosingMethod(req.getMethod()) ) {
                handler = hitter.hit(url, req.getMethod(), "", req.getInputHeaders());
            } else {
                if ( req.isPostParams() ) {
                    handler = hitter.hit(url, getParamsPostEntity(req), req.getInputHeaders());
                } else if ( req.isMultipart() ) {
                    handler = hitter.hit(url, getMultipartEntity(req), req.getInputHeaders());
                } else {
                    String body = req.getBodyToPost();
                    handler = hitter.hit(url, req.getMethod(), body, req.getInputHeaders());
                }
            }
            req = prepareViewRequest(req, handler);
            resp = prepareViewResponse(resp, handler);
            if ( !handler.isReqAborted() ) handler.closeConnection();
        } catch ( UnknownHostException uhe ) {
            LOG.error("hit(): Error: Uknown host", uhe);
            throw new RCException("Error: Uknown host");
        } catch ( Exception e ) {
            LOG.error("hit(): Error occured while hiting url", e);
            throw new RCException("Error: " + RCUtil.removeMethodName(e.getMessage()));
        }
    }

    private UrlEncodedFormEntity getParamsPostEntity(ViewRequest req) throws RCException {
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        Map<String, String> params = req.getParams();
        for ( String paramName : params.keySet() ) {
            formparams.add(new BasicNameValuePair(paramName, params.get(paramName)));
        }
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, RCConstants.DEFAULT_CHARSET);
            return entity;
        } catch ( UnsupportedEncodingException e ) {
            throw new RCException("getParamsPostEntity(): Could not prepare params post entity due to unsupported encoding", e);
        }
    }

    private MultipartEntity getMultipartEntity(ViewRequest req) throws RCException {
        MultipartEntity reqEntity = new MultipartEntity();
        Map<String, String> params = req.getParams();
        String paramValue = null;
        StringBody stringBody = null;

        try {
            for ( String paramName : params.keySet() ) {
                paramValue = params.get(paramName);
                stringBody = new StringBody(paramValue, Charset.forName(RCConstants.DEFAULT_CHARSET));
                reqEntity.addPart(paramName, stringBody);
            }
            String fileParamName = req.getFileParamName();
            File selectedFile = new File(req.getFilePath());
            // String mimeType = MimeTypeUtil.getMimeType(selectedFile);
            // FileBody fileBody = new FileBody(selectedFile, mimeType, ""); // last argument is charset
            if ( selectedFile.exists() && !RCUtil.isEmpty(fileParamName) ) {
                FileBody fileBody = new FileBody(selectedFile);
                reqEntity.addPart(fileParamName, fileBody);
            }
        } catch ( UnsupportedEncodingException e ) {
            throw new RCException("getMultipartEntity(): Could not prepare multipart entity due to unsupported encoding", e);
        }
        return reqEntity;
    }

    private ViewRequest prepareViewRequest(ViewRequest req, HttpHandler handler) {
        if ( handler != null && req != null ) {
            req.setReqLine(handler.getRequestLine());
            // req.setUrl(handler.getUrl());
            req.setHeaders(handler.getRequestHeaders());
            URI uri = handler.getUri();
            if ( uri != null ) {
                req.setHost(uri.getHost());
                req.setPort(uri.getPort());
                req.setPath(uri.getRawPath());
                req.setScheme(uri.getScheme());
                req.setQueryStrRaw(uri.getRawQuery());
            }
            req.setProtocolVersion(handler.getProtocolVersion());
        }
        return req;
    }

    private ViewResponse prepareViewResponse(ViewResponse resp, HttpHandler handler) throws RCException {
        if ( handler != null && resp != null ) {
            resp.setStatusLine(handler.getStatusLine());
            resp.setUrl(handler.getUrl());
            resp.setHeaders(handler.getResponseHeaders());
            String contentType = handler.getResponseContentType();
            resp.setContentType(contentType);

            String respStr = null;
            if ( isTextOrXmlResponse(handler) ) {
                respStr = streamToString(handler);
                resp.setBodyStr(respStr);
            } else resp.setBodyFile(streamToFile(handler, contentType));

            // writing xml response to file as well as keeping it as body string. This is just for showing
            // xml in browser with syntax highlighting until there is syntax highlighter for response pane
            if ( !RCUtil.isEmpty(respStr) && isXmlResponse(handler) ) {
                resp.setBodyFile(stringToFile(respStr, contentType));
            }
        }
        return resp;
    }

    private String streamToString(HttpHandler handler) throws RCException {
        InputStream is = handler.getResponseStream();
        if ( is == null ) return null;
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        try {
            while ( (line = reader.readLine()) != null ) {
                if ( abort ) {
                    handler.abort();
                    abort = false;
                    break;
                }
                sb.append(line);
            }
        } catch ( IOException e ) {
            throw new RCException("streamToString(): error occurred while converting response stream to string", e);
        } finally {
            try {
                if ( reader != null && !handler.isReqAborted() ) reader.close();
            } catch ( IOException e ) {
                throw new RCException("streamToString(): error occurred while closing input stream", e);
            }

        }
        return sb.toString();
    }

    private File streamToFile(HttpHandler handler, String contentType) throws RCException {
        InputStream is = handler.getResponseStream();
        if ( is == null ) return null;
        String timeStamp = SDF.format(new Date());
        File tmpFile = new File(Configurator.getTempRespFilesDir(), "output_" + timeStamp + "." + RCConstants.TEMP_FILE_EXT);
        File outputFile = null;
        FileOutputStream fos = null;
        byte[] buf = new byte[2 * 1024];
        int len;
        try {
            fos = new FileOutputStream(tmpFile);
            while ( (len = is.read(buf)) != -1 ) {
                if ( abort ) {
                    handler.abort();
                    abort = false;
                    break;
                }
                fos.write(buf, 0, len);
            }
            fos.flush();
        } catch ( Exception e ) {
            throw new RCException("streamToFile(): error occurred while writing to file", e);
        } finally {
            try {
                if ( fos != null ) fos.close();
                outputFile = changeFileExtension(tmpFile, contentType);
            } catch ( IOException e ) {
                throw new RCException("streamToFile(): error occurred while closing input/output stream", e);
            }
        }
        return outputFile;
    }

    private File stringToFile(String str, String contentType) throws RCException {

        if ( RCUtil.isEmpty(str) ) return null;
        String timeStamp = SDF.format(new Date());
        File tmpFile = new File(Configurator.getTempRespFilesDir(), "output_" + timeStamp + "." + RCConstants.TEMP_FILE_EXT);
        File outputFile = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(tmpFile);
            fw.write(str);
            fw.flush();
        } catch ( Exception e ) {
            throw new RCException("stringToFile(): error occurred while writing to file", e);
        } finally {
            try {
                if ( fw != null ) fw.close();
                outputFile = changeFileExtension(tmpFile, contentType);
            } catch ( IOException e ) {
                throw new RCException("stringToFile(): error occurred while closing file writer", e);
            }
        }
        return outputFile;
    }

    private File changeFileExtension(File tmpFile, String contentType) throws IOException {
        // change extension of file to its media type
        String ext = "";
        if ( RCUtil.isEmpty(contentType) ) contentType = MimeTypeUtil.getMimeType(tmpFile);

        ext = getSubType(contentType);
        if ( ext.contains("xml") ) ext = "xml";
        /*
        if ( contentType != null && contentType.indexOf("/") > 0 ) { // i.e. application/ssml+xml; charset=UTF-8
            ext = contentType.substring(contentType.indexOf("/") + 1);
            if ( ext.indexOf(";") > 0 ) ext = ext.substring(0, ext.indexOf(";")).trim();
            if ( ext.contains("xml") ) ext = "xml";
        }
        */
        if ( !RCUtil.isEmpty(ext) ) {
            String filePath = tmpFile.getCanonicalPath();
            String newFileName = filePath.replaceFirst("\\." + RCConstants.TEMP_FILE_EXT + "$", "." + ext);
            File outputFile = new File(newFileName);
            boolean success = tmpFile.renameTo(outputFile);
            if ( success ) return outputFile;
        }
        return tmpFile;
    }

    /**
     * Returns true if response is of text or xml type or if content type is null
     */
    private boolean isTextOrXmlResponse(HttpHandler handler) {
        String contentType = handler.getResponseContentType();
        if ( contentType != null ) { // check for extra text content types specified in config file
            if ( contentType.indexOf(";") > 0 ) {
                contentType = contentType.substring(0, contentType.indexOf(";")).trim();
            }
            String[] extraTextContTypes = RCConstants.EXTRA_TEXT_CONTENT_TYPES.split(",");
            for ( String extraContType : extraTextContTypes ) {
                if ( extraContType.equalsIgnoreCase(contentType) ) return true;
            }
        }
        if ( contentType != null && !contentType.startsWith("text") ) return false;
        return true;
    }

    private boolean isXmlResponse(HttpHandler handler) {
        String contentType = handler.getResponseContentType();
        String subType = getSubType(contentType);
        if ( subType != null ) {
            if ( contentType.startsWith("text") && subType.contains("xml") ) return true;
            // check for extra text content types specified in config file
            String[] extraTextContTypes = RCConstants.EXTRA_TEXT_CONTENT_TYPES.split(",");
            for ( String extraContType : extraTextContTypes ) {
                String xSubType = extraContType.substring(extraContType.indexOf("/") + 1);
                if ( xSubType.equalsIgnoreCase(subType) ) return true;
            }
        }
        return false;
    }

    private String getSubType(String contentType) {
        // process content type which is generally in format "type/subtype; charset=XYZ"
        // i.e. application/ssml+xml; charset=UTF-8
        String subType = null;
        if ( contentType != null && contentType.indexOf("/") > 0 ) {
            // remove type
            subType = contentType.substring(contentType.indexOf("/") + 1);
            // remove charset
            if ( subType.indexOf(";") > 0 ) subType = subType.substring(0, subType.indexOf(";")).trim();
        }
        if ( DEBUG_ENABLED ) LOG.debug("getSubType() - returning sub type " + subType);
        return subType;
    }

    /*
    	public static void main(String[] args) {
    		HitterClient client = new HitterClient();
    		ViewRequest req = new ViewRequest();
    		ViewResponse resp = new ViewResponse();
    		req.setUrl("http://localhost.mlbam.com:8080/stage/v1.1/14/content/item/read/6845668?test= you # me");
    		//req.setUrl("http://localhost.mlbam.com:8080/stage/v1.1/14/content/item/viewImage?fileLocation=/images/04022010/6845668/Sunset.jpg");
    		req.setHeadersStr("fingerprint=offline-fingerprint\nidentitypointid=9364");
    		req.setMethod("GET");
    		req.setParamsStr("output=json");
    		//client.setAbort();
    		try {
    			client.hit(req, resp);
    		} catch (RCException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		System.out.println("************ Request ****************");
    		System.out.println("Request Headers: \n" + req.getDisplayHeaderPart());
    		System.out.println("Request Body: \n" + req.getDisplayBodyPart());
    		
    		System.out.println("************ Response ****************");
    		System.out.println("ViewResponse Headers:\n" + resp.getDisplayHeaderPart());
    		System.out.println("ViewResponse Body:\n" + resp.getDisplayBodyPart());
    		System.out.println("ViewResponse Body File Path:\n" + resp.getBodyFile());
    	}
    */
}
