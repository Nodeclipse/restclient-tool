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

package code.google.restclient.core;

import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;

/**
 * @author Yaduvendra.Singh
 * @param <T>
 */
public abstract class CustomResponseHandler<T> implements ResponseHandler<T> {
    protected int statusCode;
    protected String reasonPhrase;
    protected ProtocolVersion protocolVersion;
    protected StatusLine statusLine;
    protected Header[] allHeaders;

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }

    public Header[] getAllHeaders() {
        return allHeaders;
    }

}
