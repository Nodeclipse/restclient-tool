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

import org.apache.log4j.Logger;

import code.google.restclient.common.RCConstants;
import code.google.restclient.ui.MainWindow;


/**
 * @author Yaduvendra.Singh
 */
public class Launcher {

    public static void main(String[] args) {
        Configurator.init();
        Logger log = Logger.getLogger(Launcher.class);
        log.info("Configuration initialization successful. Launching " + RCConstants.APP_DISPLAY_NAME + " ...");
        new MainWindow().open();
    }
}
