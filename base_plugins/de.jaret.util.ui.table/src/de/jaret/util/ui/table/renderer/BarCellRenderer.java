/*
 *  File: BarCellRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * CellRenderer rendering bar according to the cell value that is expected to be of type Integer. Min and max can be
 * configured.
 * 
 * @author Peter Kliem
 * @version $Id: BarCellRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class BarCellRenderer extends CellRendererBase implements ICellRenderer {
    /** min value. */
    protected int _min = 0;

    /** max value. */
    protected int _max = 100;

    /** color for rendering. */
    protected Color _barColor;

    /**
     * Constructor for BarCellRenderer.
     * 
     * @param printer Printer or <code>null</code>
     */
    public BarCellRenderer(Printer printer) {
        super(printer);
        if (printer != null) {
            _barColor = printer.getSystemColor(SWT.COLOR_DARK_RED);
        }
    }

    /**
     * Default constructor.
     * 
     */
    public BarCellRenderer() {
        super(null);
        _barColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);
    }

    /**
     * @return Returns the max.
     */
    public int getMax() {
        return _max;
    }

    /**
     * @param max The max to set.
     */
    public void setMax(int max) {
        this._max = max;
    }

    /**
     * @return Returns the min.
     */
    public int getMin() {
        return _min;
    }

    /**
     * @param min The min to set.
     */
    public void setMin(int min) {
        this._min = min;
    }

    /**
     * {@inheritDoc}
     */
    public void draw(GC gc, JaretTable jaretTable, ICellStyle cellStyle, Rectangle drawingArea, IRow row,
            IColumn column, boolean drawFocus, boolean selected, boolean printing) {
        drawBackground(gc, drawingArea, cellStyle, selected, printing);
        Rectangle drect = drawBorder(gc, cellStyle, drawingArea, printing);
        Rectangle rect = applyInsets(drect);
        Object value = column.getValue(row);
        if (value instanceof Integer) {
            int val = ((Integer) value).intValue();
            double pixPer = (double) rect.width / (double) (_max - _min);
            int correctedValue = val - _min;
            int drawingWidth = (int) (correctedValue * pixPer);

            Color bg = gc.getBackground();
            gc.setBackground(_barColor);
            gc.fillRectangle(rect.x, rect.y, drawingWidth, rect.height);

            gc.setBackground(bg);
        } else {
            // indicate error with red fill
            Color bg = gc.getBackground();
            gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
            gc.fillRectangle(rect);
            gc.setBackground(bg);
        }
        if (drawFocus) {
            drawFocus(gc, drawingArea);
        }
        drawSelection(gc, drawingArea, cellStyle, selected, printing);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
    }

    /**
     * {@inheritDoc}
     */
    public ICellRenderer createPrintRenderer(Printer printer) {
        return new BarCellRenderer(printer);
    }

}
