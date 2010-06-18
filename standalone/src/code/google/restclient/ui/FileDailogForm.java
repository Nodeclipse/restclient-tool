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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import code.google.restclient.client.ViewRequest;
import code.google.restclient.common.RCUtil;


/**
 * @author Yaduvendra.Singh
 */
public class FileDailogForm {

    public static void openFileDialog(Shell shell, final ViewRequest req) {
        final int VERTICAL_SPACING = 20;
        final int HORIZONTAL_SPACING = 5;
        final String FILE_PARAM_NAME = "file param name";
        Display display = shell.getDisplay();
        final Shell dialog = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("File Detail");
        FormLayout layout = new FormLayout();
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        dialog.setLayout(layout);
        dialog.setLayoutData(new FormData(225, 400));

        /* *********** Define Widgets *************** */
        Group fileTypeGrp = new Group(dialog, SWT.NONE);
        fileTypeGrp.setText("File Type");
        FormLayout fileTypeGrpLayout = new FormLayout();
        fileTypeGrpLayout.marginWidth = 5;
        fileTypeGrpLayout.marginHeight = 5;
        fileTypeGrp.setLayout(fileTypeGrpLayout);
        /*FormData data = new FormData();
        data.width = 180;
        data.left = new FormAttachment(0, 0);
        fileTypeGrp.setLayoutData(data);*/

        // final Button xmlRadio = new Button(fileTypeGrp, SWT.RADIO);
        // xmlRadio.setText("XML");
        final Button textRadio = new Button(fileTypeGrp, SWT.RADIO);
        textRadio.setText("Text");
        final Button binaryRadio = new Button(fileTypeGrp, SWT.RADIO);
        binaryRadio.setText("Binary");

        final Text filePathText = new Text(dialog, SWT.BORDER);
        filePathText.setEditable(false);
        filePathText.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        final Button fileBtn = new Button(dialog, SWT.PUSH);
        fileBtn.setText("&Browse");

        final Button multipartCheck = new Button(dialog, SWT.CHECK);
        multipartCheck.setText("&Multipart");
        final Text fileParamText = new Text(dialog, SWT.BORDER);
        fileParamText.setText(FILE_PARAM_NAME);
        fileParamText.setEnabled(false);

        final Button okBtn = new Button(dialog, SWT.PUSH);
        okBtn.setText("&OK");
        final Button cancelBtn = new Button(dialog, SWT.PUSH);
        cancelBtn.setText("&Cancel");

        /* *********** Render Widgets *************** */
        // Place file type group
        FormData data = new FormData();
        data.width = 180;
        data.left = new FormAttachment(0, 0);
        fileTypeGrp.setLayoutData(data);

        data = new FormData();
        data.left = new FormAttachment(0, 0);
        textRadio.setLayoutData(data);

        data = new FormData();
        data.left = new FormAttachment(textRadio, HORIZONTAL_SPACING, SWT.RIGHT);
        binaryRadio.setLayoutData(data);

        // Place file browse
        data = new FormData();
        data.width = 125;
        data.left = new FormAttachment(0, 0);
        data.top = new FormAttachment(fileTypeGrp, VERTICAL_SPACING, SWT.BOTTOM);
        filePathText.setLayoutData(data);

        data = new FormData();
        data.left = new FormAttachment(filePathText, HORIZONTAL_SPACING, SWT.RIGHT);
        data.top = new FormAttachment(fileTypeGrp, VERTICAL_SPACING, SWT.BOTTOM);
        data.bottom = new FormAttachment(filePathText, 0, SWT.BOTTOM);
        fileBtn.setLayoutData(data);

        // Place multipart check
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.top = new FormAttachment(filePathText, VERTICAL_SPACING, SWT.BOTTOM);
        data.bottom = new FormAttachment(fileParamText, 0, SWT.BOTTOM);
        multipartCheck.setLayoutData(data);

        data = new FormData();
        data.width = 110;
        data.left = new FormAttachment(multipartCheck, HORIZONTAL_SPACING, SWT.RIGHT);
        data.top = new FormAttachment(filePathText, VERTICAL_SPACING, SWT.BOTTOM);
        fileParamText.setLayoutData(data);

        // Place ok/cancel buttons
        data = new FormData();
        data.width = 50;
        data.right = new FormAttachment(cancelBtn, -(HORIZONTAL_SPACING), SWT.LEFT);
        data.top = new FormAttachment(multipartCheck, VERTICAL_SPACING, SWT.BOTTOM);
        okBtn.setLayoutData(data);

        data = new FormData();
        data.width = 50;
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(multipartCheck, VERTICAL_SPACING, SWT.BOTTOM);
        cancelBtn.setLayoutData(data);

        /* **** Event handling **** */
        SelectionAdapter adapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // file type radio buttons
                if ( e.getSource().equals(textRadio) ) {
                    boolean selection = textRadio.getSelection();
                    req.setTextBody(selection);
                }
                if ( e.getSource().equals(binaryRadio) ) {
                    boolean selection = binaryRadio.getSelection();
                    if ( selection ) req.setTextBody(false);
                }

                // multipart check button
                if ( e.getSource().equals(multipartCheck) ) {
                    boolean selection = multipartCheck.getSelection();
                    if ( selection ) {
                        fileParamText.setEnabled(true);
                        fileParamText.setText("");
                    } else {
                        fileParamText.setText(FILE_PARAM_NAME);
                        fileParamText.setEnabled(false);
                    }
                    req.setMultipart(selection);
                }

                // push buttons
                if ( e.getSource().equals(fileBtn) ) { // file button
                    FileDialog fileDialog = new FileDialog(dialog, SWT.NONE);
                    String selectedFile = fileDialog.open();
                    if ( selectedFile != null ) {
                        filePathText.setText(selectedFile);
                    }
                }
                if ( e.getSource().equals(okBtn) ) {
                    req.setFilePath(filePathText.getText());
                    if ( multipartCheck.getSelection() && RCUtil.isEmpty(fileParamText.getText()) ) {
                        fileParamText.setFocus();
                    }
                    if ( multipartCheck.getSelection() && !RCUtil.isEmpty(fileParamText.getText()) ) {
                        req.setFileParamName(fileParamText.getText());
                    }

                    if ( !(multipartCheck.getSelection() && RCUtil.isEmpty(fileParamText.getText())) ) {
                        dialog.close();
                        dialog.dispose();
                    }
                }
                if ( e.getSource().equals(cancelBtn) ) {
                    req.setTextBody(true);
                    req.setMultipart(false);
                    req.setFilePath(null);
                    req.setFileParamName(null);
                    req.setEncodeBody(false);

                    dialog.close();
                    dialog.dispose();
                }
            }
        };
        textRadio.addSelectionListener(adapter);
        binaryRadio.addSelectionListener(adapter);
        multipartCheck.addSelectionListener(adapter);
        fileBtn.addSelectionListener(adapter);
        okBtn.addSelectionListener(adapter);
        cancelBtn.addSelectionListener(adapter);

        dialog.setDefaultButton(okBtn);
        dialog.pack();
        dialog.open();

        while ( !dialog.isDisposed() ) {
            if ( !display.readAndDispatch() ) display.sleep();
        }
    }

}
