/*
 *  File: ITableViewStateListener.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import de.jaret.util.ui.table.model.ITableViewState.RowHeightMode;
import de.jaret.util.ui.table.renderer.ICellStyle;

/**
 * Interface for listening to changes on the viewstate.
 * 
 * @author Peter Kliem
 * @version $Id: ITableViewStateListener.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface ITableViewStateListener {
    /**
     * Height of row changed.
     * 
     * @param row row
     * @param newHeight new height
     */
    void rowHeightChanged(IRow row, int newHeight);

    /**
     * Row height mode changed.
     * 
     * @param row row
     * @param newHeightMode new height mode
     */
    void rowHeightModeChanged(IRow row, RowHeightMode newHeightMode);

    /**
     * Column width changed.
     * 
     * @param column column
     * @param newWidth new width
     */
    void columnWidthChanged(IColumn column, int newWidth);

    /**
     * Called when more than one column width has changed.
     * 
     */
    void columnWidthsChanged();

    /**
     * Called when the visibility of a column changed.
     * 
     * @param column column
     * @param visible true column is now visible false otherwise
     */
    void columnVisibilityChanged(IColumn column, boolean visible);

    /**
     * Called when the sorting order for rows indicated on columns changed.
     */
    void sortingChanged();

    /**
     * Called whenever a cellstyle has been changed.
     * 
     * @param row row
     * @param column column
     * @param cellStyle new or changed cell style
     */
    void cellStyleChanged(IRow row, IColumn column, ICellStyle cellStyle);

    /**
     * Called when the ordering of the columns changed.
     * 
     */
    void columnOrderChanged();

}
