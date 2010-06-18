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

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

/**
 * @author Yaduvendra.Singh
 */
public class CustomRetryHandler implements HttpRequestRetryHandler {

    private static final Logger LOG = Logger.getLogger(CustomRetryHandler.class);
    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {

        HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);

        // idempotent are those methods which returns same value irrespective of number of execution

        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
        if ( !idempotent ) {
            if ( DEBUG_ENABLED ) {
                LOG.debug("retryRequest(): not retrying because request is non-idempotent");
            }
            // Retry if the request is considered idempotent
            return false;
        }

        if ( executionCount >= 5 ) {
            if ( DEBUG_ENABLED ) {
                LOG.debug("retryRequest(): not retrying because execution count exceeds 5");
            }
            // Do not retry if over max retry count
            return false;
        }

        if ( exception instanceof NoHttpResponseException ) {
            if ( DEBUG_ENABLED ) {
                LOG.debug("retryRequest(): retrying because server dropped connection");
            }
            // Retry if the server dropped connection on us
            return true;
        }

        if ( exception instanceof SSLHandshakeException ) {
            if ( DEBUG_ENABLED ) {
                LOG.debug("retryRequest(): not retrying because of SSL handshake exception");
            }
            // Do not retry on SSL handshake exception
            return false;
        }

        if ( idempotent ) {
            if ( DEBUG_ENABLED ) {
                LOG.debug("retryRequest(): retrying because request is idempotent");
            }
            // Retry if the request is considered idempotent
            return true;
        }

        return false;
    }

}
