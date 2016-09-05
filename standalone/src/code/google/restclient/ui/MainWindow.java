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
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import code.google.restclient.client.HitterClient;
import code.google.restclient.client.Validator;
import code.google.restclient.client.ViewRequest;
import code.google.restclient.client.ViewResponse;
import code.google.restclient.common.RCConstants;
import code.google.restclient.common.RCUtil;
import code.google.restclient.parse.Formatter;

/**
 * @author Yaduvendra.Singh
 */
public class MainWindow {

    private static final HitterClient client = new HitterClient();
    private static final ViewRequest req = new ViewRequest();
    private static final ViewResponse resp = new ViewResponse();
    private static final String REQ_PROCESS_MSG = "processing...";
    private static final String NO_REQ_PROCESS_MSG = ".                   .";
    private static final String REQ_PROCESS_THREAD = "request-process";
    private static volatile boolean abortRequest = false;
    private static int TOTAL_HORIZ_SPAN = 4;
    Thread reqProcThread; // Separate thread to process request

    Display display;
    Composite shellComposite;

    // Widgets
    Combo location; //TODO Go to location when Enter is pressed
    ToolBar toolbar;
    ToolItem itemGo, itemStop;
    CCombo httpActionCombo;
    SashForm sashForm, sashFormLeft;
    Composite headerComposite, paramsComposite, contentTypeComposite, bodyComposite, bottomComposite;
    Label processingLabel, headerLabel, paramsLabel, contentTypeLabel, bodyLabel;
    StyledText headerText, paramsText,contentTypeText,  bodyText, reqPaneText, respPaneText;
    Button textBodyButton, fileButton;
    Color whiteColor, blackColor, greenColor, pinkColor, grayColor;

    Browser browser;
    AnimatedImage animImage;

    public MainWindow() {}

    public void render(Composite parent) {
        shellComposite = parent;
        display = parent.getDisplay();
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = TOTAL_HORIZ_SPAN;
        shellComposite.setLayout(gridLayout);

        createWidgets();

        shellComposite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if ( animImage != null ) animImage.releaseThread();
                releaseReqProcThread();
                RCUtil.cleanUpRespFiles(); // cleanup temporary response files
                // Clean up.
                if ( animImage != null ) animImage.dispose();
                if ( whiteColor != null ) whiteColor.dispose();
                if ( blackColor != null ) blackColor.dispose();
                if ( greenColor != null ) greenColor.dispose();
                if ( pinkColor != null ) pinkColor.dispose();
                if ( grayColor != null ) grayColor.dispose();
            }
        });
    }

    public void open() {
        Display display = new Display();
        MainWindow window = new MainWindow();
        Shell shell = window.openShell(display);

        while ( !shell.isDisposed() ) {
            if ( !display.readAndDispatch() ) display.sleep();
        }
        display.dispose();
    }

    private Shell openShell(Display display) {
        this.display = display;
        shellComposite = new Shell(display);
        Shell shell = shellComposite.getShell();
        shell.setText(RCConstants.APP_DISPLAY_NAME);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = TOTAL_HORIZ_SPAN;
        shellComposite.setLayout(gridLayout);
        ImageHelper.addImage(shell, "icons/logo.gif");

        createWidgets();

        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                if ( animImage != null ) animImage.releaseThread();
                releaseReqProcThread();
                e.doit = true;
                RCUtil.cleanUpRespFiles(); // cleanup temporary response files
            }
        });

        shellComposite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                // Clean up.
                if ( animImage != null ) animImage.dispose();
                if ( whiteColor != null ) whiteColor.dispose();
                if ( blackColor != null ) blackColor.dispose();
                if ( greenColor != null ) greenColor.dispose();
                if ( pinkColor != null ) pinkColor.dispose();
                if ( grayColor != null ) grayColor.dispose();
            }
        });

        shell.open();
        return shell;
    }

    private void createWidgets() {
        // Address bar combo using standard SWT
        GridData data = new GridData();
        location = new Combo(shellComposite, SWT.BORDER);

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        location.setLayoutData(data);

        // Tool bar buttons
        toolbar = new ToolBar(shellComposite, SWT.NONE);
        itemGo = new ToolItem(toolbar, SWT.PUSH);
        itemGo.setToolTipText("Go");
        ImageHelper.addImage(itemGo, "icons/go.gif", "&Go");
        itemStop = new ToolItem(toolbar, SWT.PUSH);
        itemStop.setToolTipText("Stop");
        itemStop.setEnabled(false); // Initially disabled
        ImageHelper.addImage(itemStop, "icons/stop.gif", "&Stop");

        data = new GridData();
        data.horizontalSpan = 1;
        toolbar.setLayoutData(data);

        processingLabel = new Label(shellComposite, SWT.NONE);
        processingLabel.setText(NO_REQ_PROCESS_MSG);
        data = new GridData();
        data.horizontalSpan = 1;
        processingLabel.setLayoutData(data);

        /*
        Canvas canvas = new Canvas(shellComposite, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 1;
        canvas.setLayoutData(data);
        animImage = new AnimatedImage(canvas, "icons/loader.gif");
        animImage.drawImage(false);
        */

        // Creating combo drop down using custom SWT
        httpActionCombo = new CCombo(shellComposite, SWT.BORDER);
        httpActionCombo.add("GET");
        httpActionCombo.add("POST");
        httpActionCombo.add("PUT");
        httpActionCombo.add("DELETE");
        httpActionCombo.add("HEAD");
        httpActionCombo.add("OPTIONS");
        httpActionCombo.add("TRACE");
        httpActionCombo.select(0);
        httpActionCombo.setEditable(false);
        whiteColor = new Color(Display.getCurrent(), 255, 255, 255);
        httpActionCombo.setBackground(whiteColor);

        data = new GridData();
        data.horizontalSpan = 1;
        httpActionCombo.setLayoutData(data);

        // Create sash form
        sashForm = new SashForm(shellComposite, SWT.HORIZONTAL);
        sashForm.SASH_WIDTH = 4; // Change the width of the sashes
        sashForm.setBackground(shellComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE)); // Change the color used to
        // paint
        // the sashes
        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.horizontalSpan = TOTAL_HORIZ_SPAN;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        sashForm.setLayoutData(data);

        /* ** Create first inner SashForm ** */
        sashFormLeft = new SashForm(sashForm, SWT.VERTICAL);
        // Change the width of the sashes
        sashFormLeft.SASH_WIDTH = 2;
        // Change the color used to paint the sashes
        sashFormLeft.setBackground(shellComposite.getDisplay().getSystemColor(SWT.COLOR_GRAY));

        // Create styled texts on composites placed on left sash form
        // Headers
        headerComposite = new Composite(sashFormLeft, SWT.NONE);
        headerComposite.setLayout(new GridLayout());
        headerLabel = new Label(headerComposite, SWT.NONE);
        headerLabel.setText("Headers");

        headerText = new StyledText(headerComposite, SWT.V_SCROLL);
        headerText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        headerText.setWordWrap(true);
        headerText.setBackground(DecorHelper.COLOR.LIGHT_GREEN.getColor());
        headerText.setFont(DecorHelper.getDefaultFont()); // set font
        headerText.setText(RCConstants.HEADER_TEXT);
        headerText.setStyleRange(DecorHelper.getGrayItalicStyle(RCConstants.HEADER_TEXT));

        // Params
        paramsComposite = new Composite(sashFormLeft, SWT.NONE);
        paramsComposite.setLayout(new GridLayout());
        paramsLabel = new Label(paramsComposite, SWT.NONE);
        paramsLabel.setText("Params");

        paramsText = new StyledText(paramsComposite, SWT.V_SCROLL);
        paramsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        paramsText.setWordWrap(true);
        paramsText.setBackground(DecorHelper.COLOR.LIGHT_GREEN.getColor());
        paramsText.setFont(DecorHelper.getDefaultFont()); // set font
        paramsText.setText(RCConstants.PARAMS_TEXT);
        paramsText.setStyleRange(DecorHelper.getGrayItalicStyle(RCConstants.CONTENT_TYPE_TEXT));
        
        // contentType
        contentTypeComposite = new Composite(sashFormLeft, SWT.NONE);
        contentTypeComposite.setLayout(new GridLayout());
        contentTypeLabel = new Label(contentTypeComposite, SWT.NONE);
        contentTypeLabel.setText("ContentType");

        contentTypeText = new StyledText(contentTypeComposite, SWT.V_SCROLL);
        contentTypeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        contentTypeText.setWordWrap(true);
        contentTypeText.setBackground(DecorHelper.COLOR.LIGHT_GREEN.getColor());
        contentTypeText.setFont(DecorHelper.getDefaultFont()); // set font
        contentTypeText.setText(RCConstants.PARAMS_TEXT);
        contentTypeText.setStyleRange(DecorHelper.getGrayItalicStyle(RCConstants.PARAMS_TEXT));

        // Body
        bodyComposite = new Composite(sashFormLeft, SWT.NONE);
        bodyComposite.setLayout(new GridLayout(3, false));
        bodyLabel = new Label(bodyComposite, SWT.NONE);
        bodyLabel.setText("Body");
        data = new GridData();
        data.horizontalAlignment = SWT.LEFT;
        data.horizontalSpan = 1;
        bodyLabel.setLayoutData(data);

        textBodyButton = new Button(bodyComposite, SWT.CHECK);
        textBodyButton.setText("&Use body text");
        data = new GridData();
        data.horizontalAlignment = SWT.RIGHT;
        data.horizontalSpan = 1;
        textBodyButton.setLayoutData(data);

        fileButton = new Button(bodyComposite, SWT.PUSH);
        fileButton.setText("&File");
        data = new GridData();
        data.horizontalAlignment = SWT.RIGHT;
        data.horizontalSpan = 1;
        fileButton.setLayoutData(data);

        bodyText = new StyledText(bodyComposite, SWT.V_SCROLL);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.horizontalSpan = 3;
        bodyText.setLayoutData(data);
        bodyText.setWordWrap(true);
        bodyText.setEnabled(false);
        bodyText.setBackground(DecorHelper.COLOR.LIGHT_PINK.getColor());
        bodyText.setFont(DecorHelper.getDefaultFont()); // set font
        bodyText.setText(RCConstants.BODY_TEXT);
        bodyText.setStyleRange(DecorHelper.getGrayItalicStyle(RCConstants.BODY_TEXT));
        /* ** First inner SashForm ends here ** */

        // Request Pane
        reqPaneText = new StyledText(sashForm, SWT.V_SCROLL); // | SWT.H_SCROLL);
        reqPaneText.setWordWrap(true);
        reqPaneText.setEditable(false);
        reqPaneText.setBackground(DecorHelper.COLOR.LIGHT_YELLOW.getColor());
        reqPaneText.setFont(DecorHelper.getDefaultFont()); // set font
        reqPaneText.setText(RCConstants.REQUEST_DETAIL);
        reqPaneText.setStyleRange(DecorHelper.getGrayItalicStyle(RCConstants.REQUEST_DETAIL));

        // Response Pane
        respPaneText = new StyledText(sashForm, SWT.V_SCROLL); // | SWT.H_SCROLL);
        respPaneText.setWordWrap(true);
        respPaneText.setEditable(false);
        respPaneText.setBackground(DecorHelper.COLOR.LIGHT_YELLOW.getColor());
        respPaneText.setFont(DecorHelper.getDefaultFont()); // set font
        respPaneText.setText(RCConstants.RESPONSE_DETAIL);
        respPaneText.setStyleRange(DecorHelper.getGrayItalicStyle(RCConstants.RESPONSE_DETAIL));

        // Browser
        try {
            browser = new Browser(sashForm, SWT.NONE);
        } catch ( SWTError e ) {
            System.err.println("Could not instantiate Browser: " + e.getMessage());
            return;
        }
        // TODO test text
        browser.setText("<marquee behavior=\"alternate\" scrolldelay=\"600\"> Browser </marquee>");

        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        browser.setLayoutData(data);

        bottomComposite = new Composite(shellComposite, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        bottomComposite.setLayout(gridLayout);

        addListeners();
    }

    /* **** Event Handling Starts Here **** */
    private void addListeners() {
        // Selection listener
        SelectionListener selListener = getSelectionListener();
        fileButton.addSelectionListener(selListener);
        httpActionCombo.addSelectionListener(selListener);
        itemGo.addSelectionListener(selListener);
        itemStop.addSelectionListener(selListener);
        textBodyButton.addSelectionListener(selListener);

        // Key press and focus events
        KeyListener keyListener = getTextKeyListener();
        FocusListener focusListener = getTextFocusListener();

        headerText.addKeyListener(keyListener);
        headerText.addFocusListener(focusListener);

        paramsText.addKeyListener(keyListener);
        paramsText.addFocusListener(focusListener);
        
        contentTypeText.addKeyListener(keyListener);
        contentTypeText.addFocusListener(focusListener);

        bodyText.addKeyListener(keyListener);
        bodyText.addFocusListener(focusListener);

        reqPaneText.addKeyListener(keyListener);
        reqPaneText.addFocusListener(focusListener);

        respPaneText.addKeyListener(keyListener);
        respPaneText.addFocusListener(focusListener);
    }

    private SelectionListener getSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if ( event.getSource().equals(fileButton) ) { // file button
                    FileDailogForm.openFileDialog(shellComposite.getShell(), req);
                    if ( !RCUtil.isEmpty(req.getFilePath()) ) {
                        httpActionCombo.select(1);
                        enableBody(bodyText, textBodyButton, false); // Disable body
                    }
                }
                if ( event.getSource().equals(httpActionCombo) ) { // enable body pane if POST is selected
                    boolean selection = RCUtil.isEntityEnclosingMethod(httpActionCombo.getText());
                    if ( selection ) req.setFilePath(null);
                    enableBody(bodyText, textBodyButton, selection);
                }
                if ( event.getSource().equals(itemGo) ) { // Go button
                    processRequest();
                }
                if ( event.getSource().equals(itemStop) ) { // Stop button
                    client.setAbort(true);
                    abortRequest = true;
                    switchGoStop(true);
                }
                if ( event.getSource().equals(textBodyButton) ) { // Use text body button
                    boolean selection = textBodyButton.getSelection();
                    if ( selection ) {
                        req.setFilePath(null);
                        httpActionCombo.select(1); // Select POST
                    } else httpActionCombo.select(0); // Select GET
                    enableBody(bodyText, textBodyButton, selection);
                }
            }
        };
    }

    private KeyListener getTextKeyListener() { // CTRL+A (Select all)
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                StyledText textWidget = (StyledText) event.widget;
                if ( event.stateMask == SWT.CTRL && event.keyCode == 97 ) {
                    textWidget.selectAll();
                }
            }
        };
    }

    private FocusListener getTextFocusListener() { // Clear intro text
        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                StyledText textWidget = (StyledText) event.widget; // TextBox
                String text = textWidget.getText();
                if ( RCConstants.HEADER_TEXT.equals(text) || RCConstants.PARAMS_TEXT.equals(text) || RCConstants.BODY_TEXT.equals(text)
                    || RCConstants.REQUEST_DETAIL.equals(text) || RCConstants.RESPONSE_DETAIL.equals(text) ) {
                    textWidget.setText("");
                    textWidget.setStyleRange(null);
                }
            }
        };
    }

    /* **** Event Handling Ends Here **** */

    /* **** Helper Methods **** */
    private void addLocationUrl(Combo location, String newUrl) {
        boolean urlFound = false;
        for ( String url : location.getItems() ) {
            if ( url.equals(newUrl) ) urlFound = true;
        }
        if ( !urlFound ) location.add(newUrl, 0);
    }

    private void processRequest() {
        String url = Validator.validateUrl(location.getText());
        location.setText(url); // Reflect validated url
        if ( url != null ) {
            // Prepare req
            req.setUrl(url);
            req.setMethod(httpActionCombo.getText());
            req.setHeadersStr(headerText.getText());
            req.setParamsStr(paramsText.getText());
            req.setContentType(paramsText.getText());
            if ( bodyText.getEnabled() && !RCConstants.BODY_TEXT.equals(bodyText.getText()) ) {
                req.setBodyStr(bodyText.getText());
            }
            addLocationUrl(location, url); // Add new url to location drop down

            // Clear previous data
            reqPaneText.setText("");
            respPaneText.setText("");
            browser.setText("");
            client.setAbort(false);
            abortRequest = false;

            // Hit url
            reqProcThread = new Thread(REQ_PROCESS_THREAD) {
                @Override
                public void run() {
                    try {
                        switchGoStop(false);
                        client.hit(req, resp); // ** Hitting URL **
                        display.syncExec(new Runnable() { // execute as sync otherwise "invalid thread access" error
                                   @Override
                                   public void run() {
                                       String hostUrl = req.getScheme() + "://" + req.getHost() + (req.getPort() == -1 ? "" : ":" + req.getPort());

                                       // Populating request pane
                                       populateReqPan(reqPaneText);
                                       req.clear(); // Clear req object for next request

                                       if ( !abortRequest ) {
                                           // Populating response pane
                                           populateRespPan(respPaneText);
                                           // Populating browser
                                           populateBrowser(browser, resp.getDisplayBodyPart(), resp.getBodyFilePath(), hostUrl);
                                       }
                                       resp.clear(); // Clear resp object for next response
                                   }
                               });
                    } catch ( final Exception ex ) {
                        display.syncExec(new Runnable() {
                            @Override
                            public void run() {
                                populateReqPan(reqPaneText); // Populate request even if response failed with error
                                req.clear();
                                respPaneText.setText(ex.getMessage());
                                resp.clear();
                            }
                        });
                    } finally {
                        switchGoStop(true);
                    }
                }
            };
            reqProcThread.start();
        }
    }

    /**
     * This must be called under dispose listener of its parent shellComposite to stop animation thread
     */
    public void releaseReqProcThread() {
        client.setAbort(true); // let thread end processing prematurely
        if ( reqProcThread != null ) {
            // wait for the thread to die before disposing the shellComposite.
            while ( reqProcThread.isAlive() ) {
                if ( !display.readAndDispatch() ) display.sleep();
            }
        }
    }

    private void populateReqPan(StyledText reqPanText) {
        String headerPart = req.getDisplayHeaderPart();
        String bodyPart = req.getDisplayBodyPart();
        if ( headerPart == null ) headerPart = "";
        if ( bodyPart == null ) bodyPart = "";

        if ( !RCUtil.isEmpty(headerPart) ) {
            if ( !RCUtil.isEmpty(bodyPart) ) {
                String prettyBodyTxt = getPrettyTxt(bodyPart);
                if ( !RCUtil.isEmpty(prettyBodyTxt) ) bodyPart = prettyBodyTxt;
                StyleRange reqPanStyle = DecorHelper.getStyle(headerPart.length(), bodyPart.length() + 2, -1, SWT.COLOR_BLUE);
                reqPanText.setText(headerPart + "\n\n" + bodyPart);
                reqPanText.setStyleRange(reqPanStyle);
            } else reqPanText.setText(headerPart);
        }

    }

    private void populateRespPan(StyledText respPanText) {
        String headerPart = resp.getDisplayHeaderPart();
        String bodyPart = resp.getDisplayBodyPart();
        if ( headerPart == null ) headerPart = "";
        if ( bodyPart == null ) bodyPart = "";

        if ( !RCUtil.isEmpty(headerPart) ) {
            if ( !RCUtil.isEmpty(bodyPart) ) {
                String prettyBodyTxt = getPrettyTxt(bodyPart);
                if ( !RCUtil.isEmpty(prettyBodyTxt) ) bodyPart = prettyBodyTxt;
                StyleRange respPanStyle = DecorHelper.getStyle(headerPart.length(), bodyPart.length() + 2, -1, SWT.COLOR_BLUE);
                respPanText.setText(headerPart + "\n\n" + bodyPart);
                respPanText.setStyleRange(respPanStyle);
            } else respPanText.setText(headerPart);
        }

    }

    private String getPrettyTxt(String text) {
        // Pretty XML
        if ( text.trim().startsWith("<?xml") ) return Formatter.getIndentedXml(text, 2);
        // Pretty Json
        if ( text.trim().startsWith("{") ) return Formatter.getIndentedJson(text, 2);
        return "";
    }

    private void populateBrowser(Browser browser, String bodyPart, String filePath, String hostUrl) {
        // First clear browser of any old content
        browser.setUrl("");
        browser.setText("");
        // Populate browser
        if ( !RCUtil.isEmpty(filePath) ) browser.setUrl("file:///" + filePath);
        else {
            String baseTag = "<base href=\"" + hostUrl + "\">";
            if ( !RCUtil.isEmpty(bodyPart) ) {
                bodyPart = bodyPart.replaceFirst("(?i)<head>", "<head>" + baseTag);
                browser.setText(bodyPart);
            }
        }
    }

    private void switchGoStop(final boolean enableGo) {
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                if ( itemGo != null && itemStop != null ) {
                    if ( enableGo ) {
                        itemGo.setEnabled(true);
                        itemStop.setEnabled(false);
                        processingLabel.setText(NO_REQ_PROCESS_MSG);
                    } else {
                        itemGo.setEnabled(false);
                        itemStop.setEnabled(true);
                        processingLabel.setText(REQ_PROCESS_MSG);
                    }
                }

            }
        });
    }

    private void enableBody(StyledText bodyText, Button bodyTextCheck, boolean selection) {

        if ( selection ) {
            greenColor = DecorHelper.COLOR.LIGHT_GREEN.getColor();
            blackColor = DecorHelper.getColor(SWT.COLOR_BLACK);
            String text = bodyText.getText();
            bodyText.setEnabled(true);
            bodyText.setBackground(greenColor);
            if ( !RCConstants.BODY_TEXT.equals(text) ) {
                bodyText.setStyleRange(DecorHelper.getStyle(0, bodyText.getText().length(), SWT.NORMAL, blackColor));
            }
            bodyTextCheck.setSelection(true); // Enable body check box
        } else {
            pinkColor = DecorHelper.COLOR.LIGHT_PINK.getColor();
            grayColor = DecorHelper.getColor(SWT.COLOR_GRAY);
            bodyText.setEnabled(false);
            bodyText.setStyleRange(DecorHelper.getStyle(0, bodyText.getText().length(), SWT.READ_ONLY, grayColor));
            bodyText.setBackground(pinkColor);
            bodyTextCheck.setSelection(false); // Disable body check box
        }
    }

    public static void main(String[] args) {
        new MainWindow().open();
    }

}
