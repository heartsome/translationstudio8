/*
 *  File: RendererBase.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.printing.Printer;

/**
 * Base implementation for renderers that support both screen and printer rendering. It's main purpose is scaling
 * beetween screen and printer coordinates (based on 96dpi for the screen).
 * 
 * @author Peter Kliem
 * @version $Id: RendererBase.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public abstract class RendererBase {
    /** printer if used. */
    protected Printer _printer;

    /** constant for scaling: screen resolution x. */
    protected static final double SCREEN_DPI_X = 96.0;
    /** constant for scaling: screen resolution y. */
    protected static final double SCREEN_DPI_Y = 96.0;

    /** x scaling for transformation beetwenn screen and printer. */
    protected double _scaleX = 1.0;

    /** y scaling for transformation beetwenn screen and printer. */
    protected double _scaleY = 1.0;

    /** for saving gc attribute. */
    private Color _bgColor;
    /** for saving gc attribute. */
    private Color _fgColor;
    /** for saving gc attribute. */
    private int _lineWidth;
    /** for saving gc attribute. */
    private Font _font;

    /**
     * May be constructed without printer (supplying null).
     * 
     * @param printer or <code>null</code>
     */
    public RendererBase(Printer printer) {
        _printer = printer;
        if (_printer != null) {
            Point dpi = _printer.getDPI();
            _scaleX = (double) dpi.x / SCREEN_DPI_X;
            _scaleY = (double) dpi.y / SCREEN_DPI_Y;
        }
    }

    /**
     * Scale an x coordinate/size from screen to printer.
     * 
     * @param in corodinate/size to scale
     * @return scaled value
     */
    public int scaleX(int in) {
        return (int) Math.round(_scaleX * (double) in);
    }

    /**
     * Retrieve the x scale factor.
     * 
     * @return x scale factor
     */
    public double getScaleX() {
        return _scaleX;
    }

    /**
     * Scale an y coordinate/size from screen to printer.
     * 
     * @param in corodinate/size to scale
     * @return scaled value
     */
    public int scaleY(int in) {
        return (int) Math.round(_scaleY * (double) in);
    }

    /**
     * Retrieve the y scale factor.
     * 
     * @return y scale factor
     */
    public double getScaleY() {
        return _scaleY;
    }

    /**
     * Retrieve the printer device.
     * 
     * @return printer device if set <code>null</code> otherwise
     */
    public Printer getPrinter() {
        return _printer;
    }

    /**
     * Helper method saving several GC attributes to loal variables. The values can be restored with
     * <code>restoreGCAttributes</code>.
     * 
     * @param gc GC to save attributes for
     */
    protected void saveGCAttributes(GC gc) {
        _bgColor = gc.getBackground();
        _fgColor = gc.getForeground();
        _font = gc.getFont();
        _lineWidth = gc.getLineWidth();
    }

    /**
     * Helper method to restore attribute values saved with <code>saveGCAttributes</code>.
     * 
     * @param gc GC to restore attributes for
     */
    protected void restoreGCAttributes(GC gc) {
        if (_bgColor == null) {
            throw new RuntimeException("no attributes saved");
        }
        gc.setBackground(_bgColor);
        gc.setForeground(_fgColor);
        gc.setFont(_font);
        gc.setLineWidth(_lineWidth);
    }

}
