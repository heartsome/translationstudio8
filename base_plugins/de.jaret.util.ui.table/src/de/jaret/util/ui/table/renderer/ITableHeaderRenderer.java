/*
 *  File: ITableHeaderRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;

import de.jaret.util.ui.table.model.IColumn;

/**
 * Interface describing a header renderer for the jaret table.
 * 
 * @author Peter Kliem
 * @version $Id: ITableHeaderRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public interface ITableHeaderRenderer {
    /**
     * Draw a table header.
     * 
     * @param gc GC to be used
     * @param rectangle rectangle to draw within
     * @param column the column for which the header is painted.
     * @param sortingPosition if the column is part of the sorting set this indicates the sorting order position. A
     * value of 0 means no sorting.
     * @param sortDir if sorting this indicates the sorting direction. <code>true</code> means ascending.
     * @param printing true if the draw operation is for a printer
     */
    void draw(GC gc, Rectangle rectangle, IColumn column, int sortingPosition, boolean sortDir, boolean printing);

    /**
     * If this method returns <code>true</code> the gc for drawing will not be limited by a clipping rect. This is
     * useful for slanted header texts but should be used with the appropriate care.
     * 
     * @return true if the rendering should not be clipped.
     */
    boolean disableClipping();

    /**
     * Check whether a click hits the area reserved for sorting indication.
     * 
     * @param drawingArea drawing aea of the header
     * @param column column
     * @param x x coordinat of the click
     * @param y y coordinate of the click
     * @return true if the click is in the area that should be active for sorting
     */
    boolean isSortingClick(Rectangle drawingArea, IColumn column, int x, int y);

    /**
     * Create a table header renderer for printing.
     * 
     * @param printer the printer that will be used
     * @return a configured header renderer for printing.
     */
    ITableHeaderRenderer getPrintRenderer(Printer printer);

    /**
     * Dispose any resources allocated.
     * 
     */
    void dispose();

}
