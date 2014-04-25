/*
 *  File: ITableViewState.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import java.util.List;

import de.jaret.util.ui.table.renderer.ICellStyle;
import de.jaret.util.ui.table.renderer.ICellStyleProvider;

/**
 * View state of a jaret table. The viewstate controls the rendering of the model (i.e. row heights).
 * 
 * @author Peter Kliem
 * @version $Id: ITableViewState.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface ITableViewState {
    /**
     * Enumeration for the row height mode of a table row.
     * <ul>
     * <li>FIXED: fixed height</li>
     * <li>OPTIMAL: height will be optimal with information from the renderers. Manual resize of the row will not be
     * possible.</li>
     * <li>OPTANDVAR: like OPTIMAL. When the height of the row is changed manually the row height mode is changed to
     * VARIABLE</li>
     * <li>VARIABLE: height variable by dragging</li>
     * </ul>
     */
    static enum RowHeightMode {
        FIXED, OPTIMAL, OPTANDVAR, VARIABLE
    };

    /**
     * Enumeration for the possible resize behaviours:
     * <ul>
     * <li>NONE: resize will only have an effect on the resized column</li>
     * <li>SUBSEQUENT: resize will take/give the space from the next visible column (unless minwidth is reached)</li>
     * <li>ALLSUBSEQUENT: resize will take/give the space from all following visible columns (unless minwidth of those
     * is reached)</li>
     * <li>ALL: width is interpreted as a weight; all columns will be resized</li>
     * </ul>
     * Recommended mode is NONE since the other modes result in heavy redraw activity.
     */
    static enum ColumnResizeMode {
        NONE, SUBSEQUENT, ALLSUBSEQUENT, ALL
    };

    static enum HAlignment {
        LEFT, RIGHT, CENTER
    };

    static enum VAlignment {
        TOP, BOTTOM, CENTER
    };

    /**
     * Retrieve the current height of a row.
     * 
     * @param row row to query the height for.
     * @return height in pixel.
     */
    int getRowHeight(IRow row);

    /**
     * Set the height of a row.
     * 
     * @param row row
     * @param height height
     */
    void setRowHeight(IRow row, int height);

    /**
     * Set the row height for ALL rows.
     * 
     * @param height height
     */
    void setRowHeight(int height);

    /**
     * Get the configured minimal row heigth.
     * 
     * @return minimal row height
     */
    int getMinimalRowHeight();

    /**
     * Set the minimal row height.
     * 
     * @param minimalRowHeight value to set
     */
    void setMinimalRowHeight(int minimalRowHeight);

    /**
     * Retrieve the row heigth mode for a specific row.
     * 
     * @param row row to get the heigth mode for
     * @return the row height mode
     */
    RowHeightMode getRowHeigthMode(IRow row);

    /**
     * Set the row height mode for a specific row.
     * 
     * @param row row to set the height mode for
     * @param mode mode to set.
     */
    void setRowHeightMode(IRow row, RowHeightMode mode);

    /**
     * Set the row heigth mode for all rows and the mode to use as the default for new rows.
     * 
     * @param mode mode to be used.
     */
    void setRowHeightMode(RowHeightMode mode);

    /**
     * Retrieve the default row heigth mode.
     * 
     * @return the default row height mode.
     */
    RowHeightMode getRowHeightMode();

    /**
     * retrive the width of a column.
     * 
     * @param column column
     * @return the width in pixel
     */
    int getColumnWidth(IColumn column);

    /**
     * Set the width of a column.
     * 
     * @param column column
     * @param width width in pixel
     */
    void setColumnWidth(IColumn column, int width);

    /**
     * Retrieve the minimum column width.
     * 
     * @return the minimum width a col can be shrinked to
     */
    int getMinimalColWidth();

    /**
     * Set the minimum col width.
     * 
     * @param minimalColumnWidth width a column can be minimal sized to
     */
    void setMinimalColWidth(int minimalColumnWidth);

    /**
     * Check whether a column is visible.
     * 
     * @param column column
     * @return true if the col is visible
     */
    boolean getColumnVisible(IColumn column);

    /**
     * Set the visibility of a column.
     * 
     * @param column column
     * @param visible true for visible
     */
    void setColumnVisible(IColumn column, boolean visible);

    /**
     * Set the visibility of a column.
     * 
     * @param columnID id of the column
     * @param visible true for visible
     */
    void setColumnVisible(String columnID, boolean visible);

    /**
     * Check whether resizing of a column is allowed.
     * 
     * @param column column
     * @return true if resizing is allowed
     */
    boolean columnResizingAllowed(IColumn column);

    /**
     * Set whether resizing a column is allowed.
     * 
     * @param column column
     * @param resizingAllowed true for allow resizing
     */
    void setColumnResizingAllowed(IColumn column, boolean resizingAllowed);

    /**
     * Retrieve the mode used when resizing a column.
     * 
     * @return the current column resizing mode
     */
    ColumnResizeMode getColumnResizeMode();

    /**
     * Set the mode to use when the size of a column changes.
     * 
     * @param resizeMode the resize mode.
     */
    void setColumnResizeMode(ColumnResizeMode resizeMode);

    /**
     * Retrieve the list of columns in their display order.
     * 
     * @return List of columnsin their display order
     */
    List<IColumn> getSortedColumns();

    /**
     * Set the order of the columns.
     * 
     * @param columns ordered list of columns
     */
    void setSortedColumns(List<IColumn> columns);

    /**
     * Retrieve the position in the sorting order set.
     * 
     * @param column column
     * @return position
     */
    int getColumnSortingPosition(IColumn column);

    /**
     * Retrieve the sorting direction for a column.
     * 
     * @param column column to check the sorting direction
     * @return true for ascending, false for descending
     */
    boolean getColumnSortingDirection(IColumn column);

    /**
     * Handle the slection of a column for sorting (handle a click).
     * 
     * @param column column to add to the sorting (or to reverse its sorting direction)
     */
    void setSorting(IColumn column);

    /**
     * Retrieve the cell style provider of the viewstate.
     * 
     * @return the cell style provider
     */
    ICellStyleProvider getCellStyleProvider();

    /**
     * Retrieve the cell style for a specified cell.
     * 
     * @param row row of the cell
     * @param column column of the cell
     * @return the cellstyle for the cell
     */
    ICellStyle getCellStyle(IRow row, IColumn column);

    /**
     * Add a listener to be informed about changes on the viewstate.
     * 
     * @param tvsl listener to add
     */
    void addTableViewStateListener(ITableViewStateListener tvsl);

    /**
     * Remove a listener from the viewstate.
     * 
     * @param tvsl listener to be removed
     */
    void removeTableViewStateListener(ITableViewStateListener tvsl);
}
