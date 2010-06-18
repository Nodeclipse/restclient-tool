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

/**
 * @author Yaduvendra.Singh
 */
public class RCException extends Exception {

    private static final long serialVersionUID = 3839291487767397007L;

    private String message;

    public RCException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public RCException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTopStackFrame() {
        return getStackTrace()[0].toString();
    }

}
