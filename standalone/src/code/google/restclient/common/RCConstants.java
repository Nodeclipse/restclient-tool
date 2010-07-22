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

/**
 * @author Yaduvendra.Singh
 */
public class RCConstants {

    // App
    public static final String APP_NAME = PropUtil.getProperty("app.name");
    public static final String APP_VERSION = PropUtil.getProperty("app.version");
    public static final String APP_DISPLAY_NAME = PropUtil.getProperty("app.display.name");
    public static final String APP_FULL_NAME = APP_DISPLAY_NAME + "/" + APP_VERSION;
    public static final boolean SYSTEM_CHECK = new Boolean(PropUtil.getProperty("system.check"));

    // HTTP config
    public static final String SYS_PROXY_ENABLED = "SYS_PROXY_ENABLED";
    public static final String SYS_PROXY_DISABLED = "SYS_PROXY_DISABLED";
    public static final int PLAIN_SOCKET_PORT = 80;
    public static final int SSL_SOCKET_PORT = 443;
    public static final boolean DISABLE_HOST_NAME_VERIFIER = new Boolean(PropUtil.getProperty("disable.host.name.verifier"));
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String HEAD = "HEAD";
    public static final String DELETE = "DELETE";
    public static final String PUT = "PUT";
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String OCTET_MIME_TYPE = "application/octet-stream";
    public static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";

    // Intro texts
    public static final String HEADER_TEXT = "Headers on separate lines";
    public static final String PARAMS_TEXT = "Params on separate lines";
    public static final String BODY_TEXT = "Raw body content";
    public static final String REQUEST_DETAIL = "Request Detail";
    public static final String RESPONSE_DETAIL = "Response Detail";

    // Mime properties
    public static final String EXTRA_TEXT_CONTENT_TYPES = PropUtil.getProperty("extra.text.content.types");

    public static final boolean SHOW_TEXT_FILE_BODY = true;
    public static String URL_ERR_MSG = "URL is not properly formatted";
    public static String TEMP_FILE_EXT = "tmp";

}
