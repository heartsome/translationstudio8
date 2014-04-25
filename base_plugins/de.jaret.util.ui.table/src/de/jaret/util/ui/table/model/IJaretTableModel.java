/*
 *  File: IJaretTableModel.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

/**
 * Interface for the table model used by the jaret table. The model should always provide all data. Sorting and
 * filtering is done by the jaret table displaying the data.
 * 
 * @author Peter Kliem
 * @version $Id: IJaretTableModel.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IJaretTableModel {
    /**
     * Return the number of rows in the model.
     * 
     * @return number of rows
     */
    int getRowCount();

    /**
     * Retrieve a specific row.
     * 
     * @param idx index of the row
     * @return the row
     */
    IRow getRow(int idx);

    /**
     * Retrieve the number of columns.
     * 
     * @return the number of columns.
     */
    int getColumnCount();

    /**
     * Retrieve a column specified by it's index.
     * 
     * @param idx index of the column to retrieve
     * @return column at index idx
     */
    IColumn getColumn(int idx);

    /**
     * Retrieve a column specified by it's id.
     * 
     * @param id id of the column to retrieve
     * @return column for the given id or <code>null</code> if the column coud not be found
     */
    IColumn getColumn(String id);

    /**
     * Check whether a cell is editable.
     * 
     * @param row row of the cell
     * @param column column of the cell
     * @return true for an editable cell
     */
    boolean isEditable(IRow row, IColumn column);

    /**
     * Set the value of a particular cell.
     * 
     * @param row row of the cell
     * @param column column of the cell
     * @param value the value to be stored
     */
    void setValue(IRow row, IColumn column, Object value);
    
    /**
     * Add a column. 
     * 
     * @param column column to add
     */
    void addColumn(IColumn column);

    /**
     * Add a listener listening for changes on the model.
     * 
     * @param jtml listener to add
     */
    void addJaretTableModelListener(IJaretTableModelListener jtml);

    /**
     * Remove a listener on the model.
     * 
     * @param jtml listener to remove
     */
    void removeJaretTableModelListener(IJaretTableModelListener jtml);

}
