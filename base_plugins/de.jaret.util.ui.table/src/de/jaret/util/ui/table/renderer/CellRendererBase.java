/*
 *  File: CellRendererBase.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.swt.ColorManager;
import de.jaret.util.swt.FontManager;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Base implementation for cell renderers that support both screen and printer rendering. This base implementation
 * contains some useful methods so that it is highly recommended to base all renderer implementations on this base.
 * 
 * @author Peter Kliem
 * @version $Id: CellRendererBase.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public abstract class CellRendererBase extends RendererBase implements ICellRenderer {
    /** selection color for overlay (non printing only). */
    protected static final Color SELECTIONCOLOR = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

    /** alpha value used when drawing default selection. */
    private static final int SELECTIONALPHA = 150;

    /** insets used when drawing the focus. */
    protected static final int FOCUSINSETS = 2;

    /** default background color. */
    protected static final RGB WHITERGB = new RGB(255, 255, 255);
    /** default foreground color. */
    protected static final RGB BLACKRGB = new RGB(0, 0, 0);

    /** cell inset used by the convenience methods. */
    protected int _inset = 2;

    /**
     * May be constructed without printer (supplying null).
     * 
     * @param printer or <code>null</code>
     */
    public CellRendererBase(Printer printer) {
        super(printer);
    }

    /**
     * {@inheritDoc} Default implementation: no prferred width.
     */
    public int getPreferredWidth(List<IRow> rows, IColumn column) {
        return -1;
    }

    /**
     * {@inheritDoc} Default implementation returning: no information.
     */
    public int getPreferredHeight(GC gc, ICellStyle cellStyle, int width, IRow row, IColumn column) {
        return -1;
    }

    /**
     * {@inheritDoc} Default: no tooltip.
     */
    public String getTooltip(JaretTable jaretTable, Rectangle drawingArea, IRow row, IColumn column, int x, int y) {
        return null;
    }

    /**
     * Target inner width (width - borders - insets).
     * 
     * @param width width
     * @param cellStyle cell style
     * @return target inner width
     */
    protected int getInnerWidth(int width, ICellStyle cellStyle) {
        int sum = _inset * 2 + cellStyle.getBorderConfiguration().getBorderLeft()
                + cellStyle.getBorderConfiguration().getBorderRight() - 1;
        return width - sum;
    }

    /**
     * Calculate the sum of all vertical spaces that could be spplied.
     * 
     * @param cellStyle cell style
     * @return sum of all vertical spaces
     */
    protected int getVerticalSpacesSum(ICellStyle cellStyle) {
        return _inset * 2 + cellStyle.getBorderConfiguration().getBorderTop()
                + cellStyle.getBorderConfiguration().getBorderBottom() - 1;
    }

    /**
     * Draw focus marking. Should be called with the corrected drawing area.
     * 
     * @param gc GC
     * @param drawingArea corrected drawing area
     */
    protected void drawFocus(GC gc, Rectangle drawingArea) {
        Color bg = gc.getBackground();
        Color fg = gc.getForeground();
        gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
        gc.drawFocus(drawingArea.x + FOCUSINSETS, drawingArea.y + FOCUSINSETS, drawingArea.width - 2 * FOCUSINSETS,
                drawingArea.height - 2 * FOCUSINSETS - 1);
// gc.drawRectangle(drawingArea.x + 2, drawingArea.y + 2, drawingArea.width - 4, drawingArea.height - 3);
        gc.setForeground(fg);
        gc.setBackground(bg);
    }

    /**
     * Calculate the resulting rectangle after applying the insets.
     * 
     * @param rect cell drawing area
     * @return corrected rectangle
     */
    protected Rectangle applyInsets(Rectangle rect) {
        int d = _inset;
        return new Rectangle(rect.x + d, rect.y + d, rect.width - 2 * d, rect.height - 2 * d);
    }

    /**
     * Draw the border for the cell according to the cell style.
     * 
     * @param gc GC
     * @param cellStyle th style
     * @param drawingArea the drawing area of the cell
     * @param printing true marks operation for a printer
     * @return the corrected drawing area (the thickness of the border has been substracted)
     */
    protected Rectangle drawBorder(GC gc, ICellStyle cellStyle, Rectangle drawingArea, boolean printing) {
        IBorderConfiguration borderConfiguration = cellStyle.getBorderConfiguration();
        int x = drawingArea.x;
        int y = drawingArea.y;
        int width = drawingArea.width;
        int height = drawingArea.height;

        Color fg = gc.getForeground();
        gc.setForeground(getBorderColor(cellStyle, printing));

        int lineWidth = gc.getLineWidth();
        if (borderConfiguration.getBorderLeft() > 0) {
            int lw = scaleX(borderConfiguration.getBorderLeft());
            gc.setLineWidth(lw);
            gc.drawLine(drawingArea.x, drawingArea.y, drawingArea.x, drawingArea.y + drawingArea.height);
            x += lw;
            width -= lw;
        }
        if (borderConfiguration.getBorderRight() > 0) {
            int lw = scaleX(borderConfiguration.getBorderRight());
            gc.setLineWidth(lw);
            gc.drawLine(drawingArea.x + drawingArea.width, drawingArea.y, drawingArea.x + drawingArea.width,
                    drawingArea.y + drawingArea.height);
            width -= lw;
        }
        if (borderConfiguration.getBorderTop() > 0) {
            int lw = scaleY(borderConfiguration.getBorderTop());
            gc.setLineWidth(lw);
            gc.drawLine(drawingArea.x, drawingArea.y, drawingArea.x + drawingArea.width - 1, drawingArea.y);
            y += lw;
            height -= lw;
        }
        if (borderConfiguration.getBorderBottom() > 0) {
            int lw = scaleY(borderConfiguration.getBorderBottom());
            gc.setLineWidth(lw);
            gc.drawLine(drawingArea.x, drawingArea.y + drawingArea.height, drawingArea.x + drawingArea.width,
                    drawingArea.y + drawingArea.height);
            height -= lw;
        }
        gc.setLineWidth(lineWidth);
        gc.setForeground(fg);

        return new Rectangle(x, y, width, height);

    }

    /**
     * Draw the cell background.
     * 
     * @param gc GC
     * @param area cell drawing area
     * @param style cell style
     * @param selected true for selected
     * @param printing true if printing
     */
    protected void drawBackground(GC gc, Rectangle area, ICellStyle style, boolean selected, boolean printing) {
        Color c = gc.getBackground();
        Color bg;
        bg = getBackgroundColor(style, printing);
        gc.setBackground(bg);
        gc.fillRectangle(area);
        gc.setBackground(c);
    }

    /**
     * Draws a cell selection by overlaying alpha blended area using SELECTIONCOLOR.
     * 
     * @param gc GC
     * @param area area of the cell
     * @param style cellstyle
     * @param selected true if selecetd
     * @param printing true if printing - no selection will be drawn when printing
     */
    protected void drawSelection(GC gc, Rectangle area, ICellStyle style, boolean selected, boolean printing) {
        Color c = gc.getBackground();
        Color bg;

        if (selected && !printing) {
            bg = SELECTIONCOLOR;
            gc.setBackground(bg);
            int alpha = gc.getAlpha();
            gc.setAlpha(SELECTIONALPHA);
            gc.fillRectangle(area);
            gc.setAlpha(alpha);
            gc.setBackground(c);
        }
    }

    /**
     * Draw a marker in upper left corner for indicating a cell comment.
     * 
     * @param gc GC
     * @param area drawing area
     * @param color color of the marker
     * @param size size of the marker
     */
    protected void drawCommentMarker(GC gc, Rectangle area, Color color, int size) {
        Color bg = gc.getBackground();
        gc.setBackground(color);
        gc.fillRectangle(area.x + area.width - size, area.y, size, size);
        gc.setBackground(bg);
    }

    /**
     * Check whether a position is in the area of the commetn marker.
     * 
     * @param area drawing area of the cell
     * @param size size of the marker
     * @param x x coordinate to check
     * @param y y coordinate to check
     * @return true if the position is in the area of the marker
     */
    protected boolean isInCommentMarkerArea(Rectangle area, int size, int x, int y) {
        Rectangle r = new Rectangle(area.x + area.width - size, area.y, size, size);
        return r.contains(x, y);
    }

    /**
     * Get the background color according to a cell style.
     * 
     * @param style cell style
     * @param printing true for printing
     * @return the color
     */
    protected Color getBackgroundColor(ICellStyle style, boolean printing) {
        Device device = printing ? _printer : Display.getCurrent();
        ColorManager cm = ColorManager.getColorManager(device);
        Color bg = cm.getColor(style.getBackgroundColor() != null ? style.getBackgroundColor() : WHITERGB);
        return bg;
    }

    /**
     * Get the foreground color according to the cell style.
     * 
     * @param style cell style
     * @param printing true for printing
     * @return the foreground color
     */
    protected Color getForegroundColor(ICellStyle style, boolean printing) {
        Device device = printing ? _printer : Display.getCurrent();
        ColorManager cm = ColorManager.getColorManager(device);
        Color bg = cm.getColor(style.getForegroundColor() != null ? style.getForegroundColor() : BLACKRGB);
        return bg;
    }

    /**
     * Get the border color according to the cell style.
     * 
     * @param style cell style
     * @param printing true for printing
     * @return the border color
     */
    protected Color getBorderColor(ICellStyle style, boolean printing) {
        Device device = printing ? _printer : Display.getCurrent();
        ColorManager cm = ColorManager.getColorManager(device);
        Color bg = cm.getColor(style.getBorderColor() != null ? style.getBorderColor() : new RGB(0, 0, 0));
        return bg;
    }

    /**
     * Retrieve the font accrding to the cell style.
     * 
     * @param style cell style
     * @param printing true for printing
     * @param defaultFont a default font used if no font can be retrieved
     * @return font according to style or default font
     */
    protected Font getFont(ICellStyle style, boolean printing, Font defaultFont) {
        Device device = printing ? _printer : Display.getCurrent();
        FontManager fm = FontManager.getFontManager(device);
        Font f = style.getFont() != null ? fm.getFont(style.getFont()) : defaultFont;
        return f;
    }

}
