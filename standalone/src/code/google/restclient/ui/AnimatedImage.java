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

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import code.google.restclient.common.RCUtil;

/**
 * @author Yaduvendra.Singh
 */
public class AnimatedImage {
    Display display;
    Shell shell;
    boolean animate = true;
    Button btnTest;
    Image image;
    String imageResource; // file path in classpath
    ImageLoader imageLoader = new ImageLoader();
    Canvas imageCanvas;
    GC imageCanvasGC;
    Color imageCanvasBGColor;
    Color shellBGColor;
    Thread animateThread;

    public AnimatedImage() {}

    public AnimatedImage(Canvas canvas, String imageResource) {
        imageCanvas = canvas;
        shell = imageCanvas.getShell();
        display = shell.getDisplay();
        this.imageResource = imageResource;

        // Create a GC for drawing, and hook the listener to dispose it.
        imageCanvasGC = new GC(imageCanvas);
        imageCanvas.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                imageCanvasGC.dispose();
            }
        });
    }

    public boolean isAnimate() {
        return animate;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    public static void main(String[] args) {
        Display display = new Display();
        AnimatedImage animatedImage = new AnimatedImage();
        Shell shell = animatedImage.openShell(display);

        while ( !shell.isDisposed() ) {
            if ( !display.readAndDispatch() ) display.sleep();
        }
        display.dispose();
    }

    private Shell openShell(Display disp) {
        display = disp;
        shell = new Shell(display);

        createWidgets();
        testImageDraw();

        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                releaseThread();
                e.doit = true;
            }
        });

        shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                // Clean up.
                dispose();
            }
        });

        shell.open();
        return shell;
    }

    /**
     * This must be called under dispose listener of its parent shell to stop animation thread
     */
    public void releaseThread() {
        animate = false; // stop any animation in progress
        if ( animateThread != null ) {
            // wait for the thread to die before disposing the shell.
            while ( animateThread.isAlive() ) {
                if ( !display.readAndDispatch() ) display.sleep();
            }
        }
    }

    public void dispose() {
        if ( image != null ) image.dispose();
    }

    private void createWidgets() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        shell.setLayout(layout);

        btnTest = new Button(shell, SWT.PUSH);
        btnTest.setText("Button");

        addListeners();
    }

    private void addListeners() {
        SelectionAdapter selAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selEvent) {
                if ( selEvent.getSource().equals(btnTest) ) {
                    animate = false;
                }
            }
        };

        btnTest.addSelectionListener(selAdapter);
    }

    public void drawImage(final boolean useImageBGColor) {
        InputStream is = RCUtil.getResourceAsStream(imageResource);

        if ( is != null ) { // if image is readable
            final ImageData[] imageDataArr = imageLoader.load(is);
            if ( imageDataArr.length == 1 ) {
                imageCanvas.addPaintListener(new PaintListener() {
                    public void paintControl(PaintEvent event) {
                        image = new Image(display, imageLoader.data[0]); // image of first frame
                        event.gc.drawImage(image, 0, 0);
                    }
                });
                imageCanvas.redraw();
            }
            if ( imageDataArr.length > 1 ) { // animation
                animateThread = new Thread("Animation") {
                    @Override
                    public void run() {
                        loopImage(imageDataArr, useImageBGColor);
                    }
                };
                animateThread.start();
            }
        }
    }

    public void drawImage(Canvas canvas, String imageResource, final boolean useImageBGColor) {
        imageCanvas = canvas;
        imageCanvasGC = new GC(imageCanvas);
        InputStream is = RCUtil.getResourceAsStream(imageResource);

        if ( is != null ) { // if image is readable
            final ImageData[] imageDataArr = imageLoader.load(is);
            if ( imageDataArr.length == 1 ) {
                imageCanvas.addPaintListener(new PaintListener() {
                    public void paintControl(PaintEvent event) {
                        image = new Image(display, imageLoader.data[0]); // image of first frame
                        event.gc.drawImage(image, 0, 0);
                    }
                });
                imageCanvas.redraw();
            }
            if ( imageDataArr.length > 1 ) { // animation
                animateThread = new Thread("Animation") {
                    @Override
                    public void run() {
                        loopImage(imageDataArr, useImageBGColor);
                    }
                };
                animateThread.start();
            }
        }
    }

    private void loopImage(ImageData[] imageDataArr, boolean useImageBGColor) {
        int imageDataIndex = 0;
        ImageData imageFrame = imageDataArr[imageDataIndex];
        image = new Image(display, imageFrame);

        // Create an off-screen image to draw on, and a GC to draw with. Both are disposed after the animation.
        Image offScreenImage = new Image(display, imageLoader.logicalScreenWidth, imageLoader.logicalScreenHeight);
        GC offScreenImageGC = new GC(offScreenImage);

        try {
            // Use syncExec to get the background color of the imageCanvas and shell.
            display.syncExec(new Runnable() {
                public void run() {
                    imageCanvasBGColor = imageCanvas.getBackground();
                    shellBGColor = shell.getBackground();
                }
            });

            // Fill the off-screen image with the background color of the canvas.
            offScreenImageGC.setBackground(imageCanvasBGColor);
            offScreenImageGC.fillRectangle(0, 0, imageLoader.logicalScreenWidth, imageLoader.logicalScreenHeight);

            // Draw the current image onto the off-screen image.
            offScreenImageGC.drawImage(image, 0, 0, imageFrame.width, imageFrame.height, imageFrame.x, imageFrame.y, imageFrame.width,
                                       imageFrame.height);

            int repeatCount = imageLoader.repeatCount;
            while ( animate && (imageLoader.repeatCount == 0 || repeatCount > 0) ) {
                switch ( imageFrame.disposalMethod ) {
                    case SWT.DM_FILL_BACKGROUND:
                        // Fill with the background color before drawing.
                        Color bgColor = null;
                        if ( useImageBGColor && imageLoader.backgroundPixel != -1 ) {
                            bgColor = new Color(display, imageFrame.palette.getRGB(imageLoader.backgroundPixel));
                        }
                        offScreenImageGC.setBackground(bgColor != null ? bgColor : shellBGColor);
                        offScreenImageGC.fillRectangle(imageFrame.x, imageFrame.y, imageFrame.width, imageFrame.height);
                        if ( bgColor != null ) bgColor.dispose();
                        break;
                    case SWT.DM_FILL_PREVIOUS:
                        // Restore the previous image before drawing.
                        offScreenImageGC.drawImage(image, 0, 0, imageFrame.width, imageFrame.height, imageFrame.x, imageFrame.y, imageFrame.width,
                                                   imageFrame.height);
                        break;
                }

                // Get the next image data.
                imageDataIndex = (imageDataIndex + 1) % imageDataArr.length;
                imageFrame = imageDataArr[imageDataIndex];
                image.dispose();
                image = new Image(display, imageFrame);

                // Draw the new image data.
                offScreenImageGC.drawImage(image, 0, 0, imageFrame.width, imageFrame.height, imageFrame.x, imageFrame.y, imageFrame.width,
                                           imageFrame.height);

                // Draw the off-screen image to the screen.
                imageCanvasGC.drawImage(offScreenImage, 0, 0);

                // Sleep for the specified delay time before drawing again.
                try {
                    Thread.sleep(visibleDelay(imageFrame.delayTime * 10));
                } catch ( InterruptedException e ) {}

                // If we have just drawn the last image in the set,
                // then decrement the repeat count.
                if ( imageDataIndex == imageDataArr.length - 1 ) repeatCount--;
            }
        } finally {
            offScreenImage.dispose();
            offScreenImageGC.dispose();
        }
    }

    /*
     * Return the specified number of milliseconds.
     * If the specified number of milliseconds is too small
     * to see a visual change, then return a higher number.
     */
    static int visibleDelay(int ms) {
        if ( ms < 20 ) return ms + 30;
        if ( ms < 30 ) return ms + 10;
        return ms;
    }

    private void testImageDraw() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        canvas.setLayoutData(data);

        drawImage(canvas, "icons/loader.gif", false);
    }
}
