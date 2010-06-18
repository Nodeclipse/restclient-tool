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
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import code.google.restclient.common.RCUtil;

/**
 * @author Yaduvendra.Singh
 */
public class DecorHelper {

    public enum COLOR {
        LIGHT_GREEN(240, 255, 240), LIGHT_YELLOW(255, 250, 240), LIGHT_ORANGE(255, 228, 196), LIGHT_PINK(255, 182, 193);

        private final int r;
        private final int g;
        private final int b;

        COLOR(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public Color getColor() {
            return new Color(Display.getDefault(), r, g, b);
        }
    }

    public static Font getDefaultFont() {
        return new Font(Display.getDefault(), "Courier New", 8, SWT.NORMAL);
    }

    public static Font getFont(String fontName, int fontSize) {
        if ( RCUtil.isEmpty(fontName) ) return getDefaultFont();
        return new Font(Display.getDefault(), fontName, fontSize, SWT.NORMAL);
    }

    public static Color getColor(int swtColor) {
        return Display.getDefault().getSystemColor(swtColor);
    }

    /**
     * @param start
     * @param length
     * @param fontStyle
     *            - defined in SWT class i.e. SWT.BOLD
     * @param fontColor
     *            - one of the color enum defined in DecorHelper.COLOR
     * @return style range to be applied on some string
     */
    public static StyleRange getStyle(int start, int length, int fontStyle, COLOR fontColor) {
        StyleRange styleRange = new StyleRange();
        styleRange.start = start;
        styleRange.length = length;
        if ( fontStyle != -1 ) styleRange.fontStyle = fontStyle;
        if ( fontColor != null ) styleRange.foreground = fontColor.getColor();
        return styleRange;
    }

    /**
     * @param start
     * @param length
     * @param fontStyle
     *            - defined in SWT class i.e. SWT.BOLD
     * @param swtColor
     *            - defined in SWT class i.e. SWT.COLOR_BLUE
     * @return styleRange
     */
    public static StyleRange getStyle(int start, int length, int fontStyle, int swtColor) {
        StyleRange styleRange = new StyleRange();
        styleRange.start = start;
        styleRange.length = length;
        if ( fontStyle != -1 ) styleRange.fontStyle = fontStyle;
        if ( swtColor != -1 ) styleRange.foreground = getColor(swtColor);
        return styleRange;
    }

    /**
     * @param start
     * @param length
     * @param fontStyle
     *            - defined in SWT class i.e. SWT.BOLD
     * @param color
     *            - Color object
     * @return styleRange
     */
    public static StyleRange getStyle(int start, int length, int fontStyle, Color color) {
        StyleRange styleRange = new StyleRange();
        styleRange.start = start;
        styleRange.length = length;
        if ( fontStyle != -1 ) styleRange.fontStyle = fontStyle;
        if ( color != null ) styleRange.foreground = color;
        return styleRange;
    }

    public static StyleRange getGrayItalicStyle(String text) {
        int length = 0;
        if ( text != null ) length = text.length();
        return DecorHelper.getStyle(0, length, SWT.ITALIC, SWT.COLOR_GRAY);
    }
}
