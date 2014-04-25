/*
 *  File: IJaretTableSelection.java 
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
import java.util.Set;

/**
 * Interface describing the selection in a jaret table. The selection is composed of full selected rows and columns and
 * of a list of selected cells. If the selection contains full selected rows or columns, the cells of the rows/columns
 * are not included in the list of single cells.
 * 
 * @author Peter Kliem
 * @version $Id: IJaretTableSelection.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IJaretTableSelection {
    /**
     * Retrieve selected rows.
     * 
     * @return selected rows.
     */
    List<IRow> getSelectedRows();

    /**
     * Retrieve selected Columns.
     * 
     * @return selecetd columns
     */
    List<IColumn> getSelectedColumns();

    /**
     * Retrieve cells that have been selected seperately.
     * 
     * @return List of JaretTableCells selected seperately
     */
    List<IJaretTableCell> getSelectedCells();

    /**
     * Retrieve a set of all selected cells (union of all cells in selected rows and columns plus. the cells selected
     * seperately)
     * 
     * @param model is needed to determine all cells.
     * @return Set of all selected cells.
     */
    Set<IJaretTableCell> getAllSelectedCells(IJaretTableModel model);

    /**
     * Add a row to the selection.
     * 
     * @param row the row to add
     */
    void addRow(IRow row);

    /**
     * Remove row from selection.
     * 
     * @param row row to remove
     */
    void remRow(IRow row);

    /**
     * Add a acolumn to the selection.
     * 
     * @param column column to add
     */
    void addColumn(IColumn column);

    /**
     * Remove column from the selection.
     * 
     * @param column col to remove
     */
    void remColumn(IColumn column);

    /**
     * Add a cell to the selection.
     * 
     * @param cell cell to add
     */
    void addCell(IJaretTableCell cell);

    /**
     * Remove a cell from the selection.
     * 
     * @param cell cell to remove
     */
    void remCell(IJaretTableCell cell);

    /**
     * Check if something is selected.
     * 
     * @return true if the selection is empty
     */
    boolean isEmpty();

    /**
     * Clear the selection.
     * 
     */
    void clear();
}
