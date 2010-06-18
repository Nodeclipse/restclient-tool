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

package code.google.restclient.ui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;

import code.google.restclient.common.RCUtil;

/**
 * @author Yaduvendra.Singh
 */
public class ImageHelper {

    /**
     * @param item
     *            - any widget on which image is to be set
     * @param imagePath
     *            - image path in classpath. It is read as stream out of classpath
     * @param altItem
     *            - alternate widget name to be set if image could not be loaded
     */
    public static void addImage(Item item, String imagePath, String altItem) {
        try {
            if ( !RCUtil.isEmpty(imagePath) ) {
                Image image = new Image(Display.getDefault(), RCUtil.getResourceAsStream(imagePath));
                item.setImage(image);
            }
        } catch ( Exception e ) {
            if ( !RCUtil.isEmpty(altItem) ) item.setText(altItem);
        }
    }

    /**
     * @param shell
     *            - any shell on which image is to be set as logo icon
     * @param imagePath
     *            - image path in classpath. It is read as stream out of classpath
     */
    public static void addImage(Shell shell, String imagePath) {
        try {
            if ( !RCUtil.isEmpty(imagePath) ) {
                Image image = new Image(Display.getDefault(), RCUtil.getResourceAsStream(imagePath));
                shell.setImage(image);
            }
        } catch ( Exception e ) {
            // TODO could not set logo icon to shell
        }
    }

}
