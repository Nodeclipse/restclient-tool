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

package code.google.restclient.init;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import code.google.restclient.common.PropUtil;
import code.google.restclient.common.RCConstants;
import code.google.restclient.common.RCUtil;

/**
 * @author Yaduvendra.Singh
 */
public class Configurator {

    private static File LOGS_DIR;
    private static File TEMP_RESP_FILES_DIR;

    static {
        createLogDir();
        createTempRespFilesDir();
    }

    private static void createLogDir() {
        LOGS_DIR = new File(getTempDirPath() + File.separator + "logs");
        if ( !LOGS_DIR.exists() ) LOGS_DIR.mkdirs();
    }

    public static void createTempRespFilesDir() {
        TEMP_RESP_FILES_DIR = new File(getTempDirPath() + File.separator + "resp_files");
        if ( !TEMP_RESP_FILES_DIR.exists() ) TEMP_RESP_FILES_DIR.mkdirs();
    }

    public static File getLogsDir() {
        return LOGS_DIR;
    }

    public static String getLogFilePath() throws IOException {
        return Configurator.getLogsDir().getCanonicalPath() + File.separator + "rest-client.log";
    }

    public static File getTempRespFilesDir() {
        return TEMP_RESP_FILES_DIR;
    }

    public static File getUserHomeTempDir() {
        File tempDir = new File(getTempDirPath());
        if ( !tempDir.exists() ) tempDir.mkdirs();
        return tempDir;
    }

    /**
     * @return tempDirPath - user home temporary rest-client directory path
     */
    public static String getTempDirPath() {
        return System.getProperty("user.home") + File.separator + "temp" + File.separator + "." + RCConstants.APP_DISPLAY_NAME.toLowerCase();
    }

    private static void initLog4j() throws IOException {
        Properties log4jProps = new Properties();
        InputStream is = RCUtil.getResourceAsStream("rc-log4j.properties");
        log4jProps.load(is);
        is.close();
        log4jProps.setProperty("log4j.appender.FILE.file", getLogFilePath()); // add log file path

        // PropertyConfigurator.configure(org.apache.log4j.helpers.Loader.getResource("rc-log4j.properties"));
        PropertyConfigurator.configure(log4jProps);
    }

    private static void systemCheck() throws IOException {
        if ( RCConstants.SYSTEM_CHECK ) {
            float sysJavaVer = getJavaVerFloat(System.getProperty("java.version"));
            float appJavaVer = getJavaVerFloat(PropUtil.getProperty("java.version"));
            String sysOsName = System.getProperty("os.name").toLowerCase();
            String appOsName = PropUtil.getProperty("os.name").toLowerCase();

            if ( sysJavaVer < appJavaVer || !sysOsName.startsWith(appOsName) ) {
                System.err.println("Required: \n\tMin. Java Version: " + appJavaVer + "\n\tOperating System: " + appOsName);
                System.exit(0);
            }
        }
    }

    private static float getJavaVerFloat(String versionStr) {
        float ver = (float) 0.0;
        if ( !RCUtil.isEmpty(versionStr) ) ver = Float.parseFloat(versionStr.substring(0, 3));
        return ver;
    }

    public static void init() {
        try {
            systemCheck(); // check system requirement
            initLog4j(); // initialize log4j
            getTempRespFilesDir(); // create output files directory for binary or unknown content type response
        } catch ( IOException e ) {
            // Make sure you have write permission in user home directory
            System.err.println("Warning: configuration failed.");
        }
    }

}
