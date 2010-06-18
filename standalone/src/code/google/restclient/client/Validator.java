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

import code.google.restclient.common.RCConstants;
import code.google.restclient.common.RCUtil;

/**
 * @author Yaduvendra.Singh
 */
public class Validator {

    public static String validateUrl(String url) {
        if ( RCUtil.isEmpty(url) ) {
            RCConstants.URL_ERR_MSG = "Empty URL";
            return null;
        }
        if ( !url.startsWith("http://") && !url.startsWith("https://") ) return "http://" + url;
        return url;
    }

    /* ******** Error Message Getters ******** */
    public static String getUrlErrMsg() {
        return RCConstants.URL_ERR_MSG;
    }

}
