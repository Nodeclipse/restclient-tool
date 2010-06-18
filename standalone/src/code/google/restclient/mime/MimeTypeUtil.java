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

package code.google.restclient.mime;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import org.apache.log4j.Logger;

import code.google.restclient.common.PropUtil;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;

/**
 * @author Yaduvendra.Singh
 */
public class MimeTypeUtil extends MimeUtil {

    private static final Logger LOG = Logger.getLogger(MimeTypeUtil.class);
    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    private static final String ALLOWED_MIME_TYPES = PropUtil.getProperty("allowed.mime.types");

    private static String[] allowedMimeTypes;

    static {
        if ( ALLOWED_MIME_TYPES != null ) allowedMimeTypes = ALLOWED_MIME_TYPES.split(",");
    }

    /* ** get mime type methods ** */
    public static String getMimeType(String fileName) {
        Collection<?> mimeTypes = getMimeTypeCollection(fileName);
        return getSpecificMimeType(mimeTypes).toString();
    }

    public static String getMimeType(InputStream is) {
        Collection<?> mimeTypes = getMimeTypeCollection(is);
        return getSpecificMimeType(mimeTypes).toString();
    }

    public static String getMimeType(File file) {
        Collection<?> mimeTypes = getMimeTypeCollection(file);
        return getSpecificMimeType(mimeTypes).toString();
    }

    /* ** get media type methods ** */
    public static String getMediaType(String fileName) {
        Collection<?> mimeTypes = getMimeTypeCollection(fileName);
        return getSpecificMediaType(mimeTypes);
    }

    public static String getMediaType(InputStream is) {
        Collection<?> mimeTypes = getMimeTypeCollection(is);
        return getSpecificMediaType(mimeTypes);
    }

    public static String getMediaType(File file) {
        Collection<?> mimeTypes = getMimeTypeCollection(file);
        return getSpecificMediaType(mimeTypes);
    }

    /* ** methods to check if mime type is one of the types mentioned in config file ** */
    public static boolean isMimeTypeAllowed(String fileName) {
        return checkMimeType(getMimeType(fileName));
    }

    public static boolean isMimeTypeAllowed(InputStream is) {
        return checkMimeType(getMimeType(is));
    }

    public static boolean isMimeTypeAllowed(File file) {
        return checkMimeType(getMimeType(file));
    }

    public static String[] getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public static boolean checkMimeType(String mimeType) {
        if ( allowedMimeTypes == null ) return true; // if allowed mime types not specified, allow all
        for ( String mType : allowedMimeTypes ) {
            if ( mType.equals(mimeType) ) {
                return true;
            }
        }
        return false;
    }

    /* ** helping methods ** */
    private static Collection<?> getMimeTypeCollection(String fileName) {
        registerMagicMimeDetector();
        Collection<?> mimeTypes = getMimeTypes(fileName);
        MimeType mimeType = getMostSpecificMimeType(mimeTypes);
        unregisterMagicMimeDetector();

        if ( "application/octet-stream".equals(mimeType.toString()) ) {
            if ( DEBUG_ENABLED ) LOG.debug("getMimeTypeCollection() - using extension MIME detector ... ");
            registerExtensionMimeDetector();
            mimeTypes = getMimeTypes(fileName);
            unregisterExtensionMimeDetector();
        } else if ( DEBUG_ENABLED ) LOG.debug("getMimeTypeCollection() - using stream Magic MIME detector ... ");

        return mimeTypes;
    }

    private static Collection<?> getMimeTypeCollection(InputStream is) {
        if ( DEBUG_ENABLED ) LOG.debug("getMimeTypeCollection() - using stream Magic MIME detector ... ");
        BufferedInputStream buffIS = new BufferedInputStream(is);
        registerMagicMimeDetector();
        Collection<?> mimeTypes = getMimeTypes(buffIS);
        unregisterMagicMimeDetector();

        return mimeTypes;
    }

    private static Collection<?> getMimeTypeCollection(File file) {
        if ( DEBUG_ENABLED ) LOG.debug("getMimeTypeCollection() - using stream Magic MIME detector ... ");
        registerMagicMimeDetector();
        Collection<?> mimeTypes = getMimeTypes(file);
        unregisterMagicMimeDetector();

        return mimeTypes;
    }

    private static MimeType getSpecificMimeType(Collection<?> mimeTypes) {
        MimeType mimeType = getMostSpecificMimeType(mimeTypes);
        if ( DEBUG_ENABLED ) LOG.debug("getSpecificMimeType() - returning mime type: " + mimeType);
        return mimeType;
    }

    private static String getSpecificMediaType(Collection<?> mimeTypes) {
        MimeType mimeType = getMostSpecificMimeType(mimeTypes);
        if ( DEBUG_ENABLED )
            LOG.debug("getSpecificMediaType() - returning media type: " + mimeType.getMediaType() + ", associated mimeType=" + mimeType);
        return mimeType.getMediaType();
    }

    private static void registerMagicMimeDetector() {
        registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    }

    private static void unregisterMagicMimeDetector() {
        unregisterMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    }

    private static void registerExtensionMimeDetector() {
        registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
    }

    private static void unregisterExtensionMimeDetector() {
        unregisterMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
    }

    /*
    public static void main(String[] args) {
        System.out.println("mime type: "
            + isMimeTypeAllowed("C:/tools/eclipse/workspace/galileo-classic/swt-standalone/conf/version.txt"));
    }
    */
}
