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

package code.google.restclient.exception;

import java.io.InputStream;

/**
 * @author Yaduvendra.Singh
 */
public class HitterException extends Exception {

    private static final long serialVersionUID = -2750978502199146817L;
    private String url;
    private int code;
    private InputStream is;

    public HitterException(String message, Throwable cause) {
        super(message, cause);
    }

    public HitterException(String url, int code, String message, Throwable cause) {
        this(message, cause);
        this.url = url;
        this.code = code;
    }

    public HitterException(String url, int code) {
        this.url = url;
        this.code = code;
    }

    public HitterException(String url, int code, InputStream is) {
        this.url = url;
        this.code = code;
        this.is = is;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return "FAILED TO HIT " + url + " : " + code;
    }

    public InputStream getIs() {
        return is;
    }

    public void setIs(InputStream is) {
        this.is = is;
    }
}
