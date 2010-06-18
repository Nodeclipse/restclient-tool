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

package code.google.restclient.eclipse.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import code.google.restclient.init.Configurator;
import code.google.restclient.ui.MainWindow;

/**
 * @author Yaduvendra.Singh
 *
 */
public class LaunchView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "code.google.restclient.eclipse.views.LaunchView";

	@Override
	public void createPartControl(Composite parent) {
		Configurator.init();
		MainWindow window = new MainWindow();
		window.render(parent);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}
}
