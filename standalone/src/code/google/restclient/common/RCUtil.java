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

package code.google.restclient.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import code.google.restclient.init.Configurator;

/**
 * @author Yaduvendra.Singh
 */
public class RCUtil {

    private static final Logger LOG = Logger.getLogger(RCUtil.class);
    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    public static boolean isEmpty(String str) {
        if ( str != null && !"".equals(str.trim()) ) return false;
        else return true;
    }

    public static Map<String, String> getMapFromStr(String str) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        if ( str != null ) {
            String[] linesArr = str.replaceAll("\r", "").split("\n");
            if ( linesArr != null && linesArr.length > 0 ) {
                String[] keyValueArr = null;
                for ( String line : linesArr ) {
                    keyValueArr = line.trim().split("=");
                    if ( keyValueArr != null && keyValueArr.length == 2 ) {
                        map.put(keyValueArr[0].trim(), keyValueArr[1].trim());
                    }
                }
            }
        }
        return map;
    }

    public static String encode(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, RCConstants.DEFAULT_CHARSET);
    }

    public static String decode(String str) throws UnsupportedEncodingException {
        return URLDecoder.decode(str, RCConstants.DEFAULT_CHARSET);
    }

    public static String getFileContent(String filePath, boolean encode) throws UnsupportedEncodingException, IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = null;
        while ( (line = reader.readLine()) != null ) {
            sb.append("\n");
            if ( encode ) sb.append(RCUtil.encode(line));
            else sb.append(line);
        }
        return sb.toString().replaceFirst("\n", "");
    }

    /**
     * Encodes name/value pair of params passed to URL using UTF-8 encoding
     * 
     * @param url
     *            complete url with request query if any
     * @return encodedUrl
     * @throws UnsupportedEncodingException
     */
    public static String encodeUrl(String url) throws UnsupportedEncodingException {
        if ( url == null || "".equals(url) ) throw new IllegalArgumentException("Error: URL can not be null");

        String[] urlParts = url.split("\\?");
        String queryStr = (urlParts.length > 1) ? urlParts[1] : null;
        StringBuilder sb = new StringBuilder();
        if ( queryStr != null ) {
            for ( String pair : queryStr.split("&") ) {
                String[] tuple = pair.split("=");
                if ( tuple.length > 1 ) {
                    // encode name part
                    sb.append("&" + encode(tuple[0]) + "=");
                    // encode value part
                    sb.append(encode(decode(tuple[1])));
                }
            }
            queryStr = sb.toString().replaceFirst("&", "");
            url = urlParts[0] + "?" + queryStr;
        }
        return url;
    }

    public static String getMimeType(File file) {
        String mimeType = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ( (line = br.readLine()) != null ) {
                line = line.trim();
                if ( line.startsWith("<") ) {
                    mimeType = "text/xml";
                    break;
                }
            }
        } catch ( Exception e ) {
            LOG.error("getMimeType(): Error while reading file");
        } finally {
            try {
                if ( br != null ) br.close();
            } catch ( IOException e ) {
                LOG.error("getMimeType(): Error while closing file");
            }
        }
        return mimeType;
    }

    public static boolean isEntityEnclosingMethod(String methodName) {
        return RCConstants.POST.equals(methodName) || RCConstants.PUT.equals(methodName);
    }

    public static String removeMethodName(String errMsg) {
        String regex = "^[a-zA-Z_]+\\(\\)[\\s]*(: | -)"; // err msg i.e. "hit(): error occurred"
        if ( !isEmpty(errMsg) ) errMsg = errMsg.replaceFirst(regex, "");
        return errMsg;
    }

    public static InputStream getResourceAsStream(String resourceName) {
        return RCUtil.class.getClassLoader().getResourceAsStream(resourceName);
    }

    public static void cleanUpRespFiles() {
        File tempDir = Configurator.getTempRespFilesDir();
        if ( tempDir != null && tempDir.isDirectory() ) {
            File[] files = tempDir.listFiles();
            for ( File file : files ) {
                file.delete();
            }
        }
    }

    public static boolean deleteFile(File file) {
        if ( file == null ) return false;
        return file.delete();
    }

    public static boolean deleteFile(String filePath) {
        if ( isEmpty(filePath) ) return false;
        File file = new File(filePath);
        return deleteFile(file);
    }
}
