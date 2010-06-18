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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Yaduvendra.Singh
 */
public class PropUtil {

    private static final Logger LOG = Logger.getLogger(PropUtil.class);
    private static boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    private static final HashMap<String, Properties> cache = new HashMap<String, Properties>();
    private static final String DEFAULT_PROP_FILE = "rest-client";

    static {
        load(DEFAULT_PROP_FILE);
    }

    /**
     * This method loads property file even if it is already in cache map. It should be used to load property file if there is a possibility of file
     * update while application is running.
     * 
     * @param propFileName
     *            properties file name without .properties suffix
     */
    public static void load(String propFileName) {
        String completeFileName = propFileName + ".properties";
        Properties props = new Properties();
        try {
            InputStream is = RCUtil.getResourceAsStream(completeFileName);
            props.load(is);
            is.close();
            cache.put(propFileName, props);
        } catch ( IOException e ) {
            LOG.warn("load() - could not load property file " + completeFileName, e);
        }
    }

    /**
     * @param propName
     * @param defaultVal
     *            value to be returned if property propName is not found
     * @param propFileName
     *            properties file name without .properties suffix
     * @return propVal
     */
    public static String getProperty(String propName, String defaultVal, String propFileName) {
        String propVal = defaultVal;
        if ( RCUtil.isEmpty(propFileName) ) propFileName = DEFAULT_PROP_FILE;
        Properties props = cache.get(propFileName);
        if ( props == null ) load(propFileName);
        else if ( props.getProperty(propName) != null ) propVal = props.getProperty(propName);

        return propVal;
    }

    public static String getProperty(String propName, String propFileName) {
        return getProperty(propName, null, propFileName);
    }

    public static String getProperty(String propName) {
        return getProperty(propName, null, null);
    }

}
