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

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

/**
 * @author Yaduvendra.Singh
 */
public class ByteArrayResponseHandler extends CustomResponseHandler<byte[]> {

    public byte[] handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        statusLine = response.getStatusLine();
        statusCode = statusLine.getStatusCode();
        reasonPhrase = statusLine.getReasonPhrase();
        protocolVersion = statusLine.getProtocolVersion();
        allHeaders = response.getAllHeaders();

        HttpEntity respEntity = response.getEntity();
        try {
            if ( respEntity != null ) return EntityUtils.toByteArray(respEntity);
            else return null;
        } finally {
            EntityUtils.consume(respEntity);
        }
    }

}
