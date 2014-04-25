/*
 *  File: ICellStyleProvider.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.model.ITableViewState;

/**
 * Interface for a cell style supplier. The cell style provider is responsible for storing the individual cell styles.
 * It is possible to define a single style for a row, a column or a specific cell. A default cell style is used whenever
 * no specific cell style has been set.
 * <p>
 * The interface operates on cell styles. In some cases this is quite inconvenient so som econvience methods have been
 * added to support direct setting of the properties.
 * </p>
 * 
 * @author Peter Kliem
 * @version $Id: ICellStyleProvider.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public interface ICellStyleProvider {
    /**
     * Set a style strategy to be involved when delivering styles.
     * 
     * @param startegy strategy to use
     */
    void setStyleStrategy(IStyleStrategy startegy);

    /**
     * Retrieve a style strategy if set.
     * 
     * @return the strategy or <code>null</code>
     */
    IStyleStrategy getStyleStrategy();

    /**
     * Retrieve the cell style for a cell. This method should not create CellStyle objects.
     * 
     * @param row row of the cell
     * @param column col of the cell
     * @return cell style for the specified cell
     */
    ICellStyle getCellStyle(IRow row, IColumn column);

    /**
     * Get the cell style defined for a single cell. Should create a new CellStyle object if create is true.
     * 
     * @param row row of the cell
     * @param column column of the cell
     * @param create true will signal to create a new style object if necessary
     * @return cell style for the cell
     */
    ICellStyle getCellSpecificStyle(IRow row, IColumn column, boolean create);

    /**
     * Retrieve the cell style for a column.
     * 
     * @param column column
     * @param create if true and no style has been set for the column, create a copy of the default cell style
     * @return the cellstyle for the colun which might be the default cell style if create is set to false
     */
    ICellStyle getColumnCellStyle(IColumn column, boolean create);

    /**
     * Set the cell style for a column.
     * 
     * @param column column
     * @param style style
     */
    void setColumnCellStyle(IColumn column, ICellStyle style);

    /**
     * Retrieve the cell style for a row.
     * 
     * @param row row
     * @param create if true and no style has been set for the row, create a copy of the default cell style
     * @return the cellstyle for the row which might be the default cell style if create is set to false
     */
    ICellStyle getRowCellStyle(IRow row, boolean create);

    /**
     * Set the cell style for a row.
     * 
     * @param row row
     * @param style cell style
     */
    void setRowCellStyle(IRow row, ICellStyle style);

    /**
     * Set the cell style to use for a specific cell.
     * 
     * @param row row of the cell
     * @param column column of the cell
     * @param style style to use
     */
    void setCellStyle(IRow row, IColumn column, ICellStyle style);

    /**
     * Retrieve the default cell style used for cells where no style has been set. If the returned cell style is
     * modified, this applies for all default styled cells.
     * 
     * @return the default cell style.
     */
    ICellStyle getDefaultCellStyle();

    /**
     * Set the default cell style that is used whenever no specific style has been set for a cell, column or row.
     * 
     * @param cellStyle cell style to use as the default cell style
     */
    void setDefaultCellStyle(ICellStyle cellStyle);

    /**
     * Add a listener to listen on cell style changes.
     * 
     * @param csl listener
     */
    void addCellStyleListener(ICellStyleListener csl);

    /**
     * Remove a cell sytle listener.
     * 
     * @param csl listener to remove
     */
    void remCellStyleListener(ICellStyleListener csl);

    /**
     * Convenience method for setting the background of a row. This method will manipulate or create a style.
     * 
     * @param row row
     * @param background background color
     */
    void setBackground(IRow row, RGB background);

    /**
     * Convenience method for setting the background of a column. This method will manipulate or create a style.
     * 
     * @param column column
     * @param background background color
     */
    void setBackground(IColumn column, RGB background);

    /**
     * Convenience method for setting the background of a cell. This method will manipulate or create a style.
     * 
     * @param row row of th cell
     * @param column column of the cell
     * @param background background color
     */
    void setBackground(IRow row, IColumn column, RGB background);

    /**
     * Convenience method for setting the foreground of a row. This method will manipulate or create a style.
     * 
     * @param row row
     * @param foreground background color
     */
    void setForeground(IRow row, RGB foreground);

    /**
     * Convenience method for setting the foreground of a column. This method will manipulate or create a style.
     * 
     * @param column column
     * @param foreground foreground color
     */
    void setForeground(IColumn column, RGB foreground);

    /**
     * Convenience method for setting the foreground of a cell. This method will manipulate or create a style.
     * 
     * @param row row of th cell
     * @param column column of the cell
     * @param foreground foreground color
     */
    void setForeground(IRow row, IColumn column, RGB foreground);

    /**
     * Convenience method for setting the horizontal alignment. The method will create a cell style for the element or
     * manipulate the already set style.
     * 
     * @param row row
     * @param hAlignment horizontal alignment
     */
    void setHorizontalAlignment(IRow row, ITableViewState.HAlignment hAlignment);

    /**
     * Convenience method for setting the horizontal alignment. The method will create a cell style for the element or
     * manipulate the already set style.
     * 
     * @param column column
     * @param hAlignment horizontal alignment
     */
    void setHorizontalAlignment(IColumn column, ITableViewState.HAlignment hAlignment);

    /**
     * Convenience method for setting the horizontal alignment. The method will create a cell style for the element or
     * manipulate the already set style.
     * 
     * @param row row of th cell
     * @param column column of the cell
     * @param hAlignment horizontal alignment
     */
    void setHorizontalAlignment(IRow row, IColumn column, ITableViewState.HAlignment hAlignment);

    /**
     * Convenience method for setting the vertical alignment. The method will create a cell style for the element or
     * manipulate the already set style.
     * 
     * @param row row
     * @param vAlignment vertical alignment
     */
    void setVerticalAlignment(IRow row, ITableViewState.VAlignment vAlignment);

    /**
     * Convenience method for setting the vertical alignment. The method will create a cell style for the element or
     * manipulate the already set style.
     * 
     * @param column column
     * @param vAlignment vertical alignment
     */
    void setVerticalAlignment(IColumn column, ITableViewState.VAlignment vAlignment);

    /**
     * Convenience method for setting the vertical alignment. The method will create a cell style for the element or
     * manipulate the already set style.
     * 
     * @param row row of th cell
     * @param column column of the cell
     * @param vAlignment vertical alignment
     */
    void setVerticalAlignment(IRow row, IColumn column, ITableViewState.VAlignment vAlignment);

    /**
     * Convenience method for setting the font. The method will create a cell style for the element or manipulate the
     * already set style.
     * 
     * @param row row
     * @param fontdata font data for the font to use
     */
    void setFont(IRow row, FontData fontdata);

    /**
     * Convenience method for setting the font. The method will create a cell style for the element or manipulate the
     * already set style.
     * 
     * @param column column
     * @param fontdata font data for the font to use
     */
    void setFont(IColumn column, FontData fontdata);

    /**
     * Convenience method for setting the font. The method will create a cell style for the element or manipulate the
     * already set style.
     * 
     * @param row row of th cell
     * @param column column of the cell
     * @param fontdata font data for the font to use
     */
    void setFont(IRow row, IColumn column, FontData fontdata);

}
