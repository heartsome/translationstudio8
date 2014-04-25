/*
 *  File: ImageCellRenderer.java 
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * CellRenderer rendering an image.
 * 
 * @author Peter Kliem
 * @version $Id: ImageCellRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class ImageCellRenderer extends CellRendererBase implements ICellRenderer {

    /**
     * Construct an image cell renderer for use with a printer.
     * 
     * @param printer printer
     */
    public ImageCellRenderer(Printer printer) {
        super(printer);
    }

    /**
     * Construct an image cell renderer for display use.
     * 
     */
    public ImageCellRenderer() {
        super(null);
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
        if (value != null) {
            if (value instanceof Image) {
                Image img = (Image) value;
                int x = rect.x + (rect.width - img.getBounds().width) / 2;
                int y = rect.y + (rect.height - img.getBounds().height) / 2;
                gc.drawImage(img, x, y);
            } else {
                // indicate error with red fill
                Color bg = gc.getBackground();
                gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
                gc.fillRectangle(rect);
                gc.setBackground(bg);
            }
        }
        if (drawFocus) {
            drawFocus(gc, drawingArea);
        }
        drawSelection(gc, drawingArea, cellStyle, selected, printing);

    }

    // public int getPreferredWidth(List<IRow> rows, IColumn column) {
    // int max = 0;
    // for (IRow row : rows) {
    // Image img = (Image)column.getValue(row);
    // if (img.getBounds().width > max) {
    // max = img.getBounds().width;
    // }
    // }
    // return max;
    // }
    // public int getPreferredHeight(IRow row, IColumn column) {
    // Image img = (Image)column.getValue(row);
    // return img.getBounds().height;
    // }
    //

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        // nothing to dispose
    }

    /**
     * {@inheritDoc}
     */
    public ICellRenderer createPrintRenderer(Printer printer) {
        return new ImageCellRenderer(printer);
    }

}
