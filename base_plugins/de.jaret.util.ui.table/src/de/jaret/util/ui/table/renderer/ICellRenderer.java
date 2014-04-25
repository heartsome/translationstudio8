/*
 *  File: ICellRenderer.java 
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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Interface for a cell renderer for a jaret table.
 * 
 * @author Peter Kliem
 * @version $Id: ICellRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public interface ICellRenderer {
    /**
     * Draw a single cell. The draw method should be null safe (handling null as the cell value).
     * 
     * @param gc GC to paint on
     * @param jaretTable table the rendering is for
     * @param cellStyle style of the cell
     * @param drawingArea rectangle to draw within
     * @param row row of the cell to paint
     * @param column column of the cell to paint
     * @param drawFocus true if a focus mark should be drawn
     * @param selected true if the cell is currently selected
     * @param printing true if the render operation is for a printer
     */
    void draw(GC gc, JaretTable jaretTable, ICellStyle cellStyle, Rectangle drawingArea, IRow row, IColumn column,
            boolean drawFocus, boolean selected, boolean printing);

    /**
     * Calculate the preferred width for the column.
     * 
     * @param rows the rows currently displayed by the table
     * @param column the column for which the preferred width is to be calculated
     * @return the preferred width or -1 for no special preferred width.
     */
    int getPreferredWidth(List<IRow> rows, IColumn column);

    /**
     * Calculate the preferred height of a specific cell.
     * 
     * @param gc GC that will used
     * @param cellStyle cell style of the cell
     * @param width width of the column (thus of the cell)
     * @param row row
     * @param column column
     * @return the preferred height or -1 for no special preferred height
     */
    int getPreferredHeight(GC gc, ICellStyle cellStyle, int width, IRow row, IColumn column);

    /**
     * Provide a tooltip text for display.
     * 
     * @param jaretTable table that is asking
     * @param drawingArea area of the cell rendering
     * @param row row
     * @param column column
     * @param x mouse x coordinate (absolute within drawing area)
     * @param y mouse y coordinate (abs within drawing area)
     * @return tootip text or <code>null</code> if no tooltip is to be shown
     */
    String getTooltip(JaretTable jaretTable, Rectangle drawingArea, IRow row, IColumn column, int x, int y);

    /**
     * Create a renderer connfigured for printing.
     * 
     * @param printer printer to use
     * @return a configured renderer for printing
     */
    ICellRenderer createPrintRenderer(Printer printer);

    /**
     * If there are resources to free - this is the place.
     * 
     */
    void dispose();

}
