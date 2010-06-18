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

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

/**
 * @author Yaduvendra.Singh
 */
public class CustomKeepAliveStrategy implements ConnectionKeepAliveStrategy {

    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        // Honor 'keep-alive' header if present in response
        HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
        while ( it.hasNext() ) {
            HeaderElement he = it.nextElement();
            String param = he.getName();
            String value = he.getValue();
            if ( value != null && param.equalsIgnoreCase("timeout") ) {
                try {
                    return Long.parseLong(value) * 1000;
                } catch ( NumberFormatException ignore ) {}
            }
        }

        /*
        HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        if ("www.naughty-server.com".equalsIgnoreCase(target.getHostName())) {
        	
        	return 5 * 1000;	// Keep alive for 5 seconds only
        }
        */

        return 10 * 1000; // Keep alive for 10 seconds
    }
}
