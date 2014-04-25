/*
 *  File: IJaretTableSelectionModel.java 
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
 * Selection model for the jaret table. The selection models controls the slection istelf and the possible selection
 * modes.
 * 
 * @author Peter Kliem
 * @version $Id: IJaretTableSelectionModel.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IJaretTableSelectionModel {

    /**
     * Clear the selection.
     */
    void clearSelection();

    /**
     * Check whether full row selection is allowed.
     * 
     * @return true if full row selection is allowed.
     */
    boolean isFullRowSelectionAllowed();

    /**
     * Set the allowance for full row selection.
     * 
     * @param allowed true for allowed
     */
    void setFullRowSelectionAllowed(boolean allowed);

    /**
     * Check whether full column selection is allowed.
     * 
     * @return true if full column selection is allowed.
     */
    boolean isFullColumnSelectionAllowed();

    /**
     * Set the allowance for full column selection.
     * 
     * @param allowed true for allowed
     */
    void setFullColumnSelectionAllowed(boolean allowed);

    /**
     * 
     * @return true if selection of single cells is allowed
     */
    boolean isCellSelectionAllowed();

    /**
     * Set allowance for single cell selections.
     * 
     * @param allowed true for allowed 
     */
    void setCellSelectionAllowed(boolean allowed);

    /**
     * Retrieve allowance for multiple elements selectable.
     * 
     * @return true if multiple elemets should be selectable
     */
    boolean isMultipleSelectionAllowed();

    /**
     * Set the allowance for multiple selection.
     * 
     * @param allowed true for allowed
     */
    void setMultipleSelectionAllowed(boolean allowed);

    /**
     * Check whether only row selection is allowed.
     * 
     * @return true if only rows should be selectable
     */
    boolean isOnlyRowSelectionAllowed();

    /**
     * If set to true only row selection is allowed.
     * 
     * @param allowed true for only row selection
     */
    void setOnlyRowSelectionAllowed(boolean allowed);

    /**
     * Add a row to the selection.
     * 
     * @param row element to be added to the selection
     */
    void addSelectedRow(IRow row);

    /**
     * Remove a row from the selection.
     * 
     * @param row element to be removed from the selection
     */
    void remSelectedRow(IRow row);

    /**
     * Add a column to the selection.
     * 
     * @param column element to be added to the selection
     */
    void addSelectedColumn(IColumn column);

    /**
     * Remove a column from the selection.
     * 
     * @param column element to be removed from the selection
     */
    void remSelectedColumn(IColumn column);

    /**
     * Add a cell to the selection.
     * 
     * @param cell element to be added to the selection
     */
    void addSelectedCell(IJaretTableCell cell);

    /**
     * Remove a cell from the selection.
     * 
     * @param cell element to be removed from the selection
     */
    void remSelectedCell(IJaretTableCell cell);

    /**
     * retrieve the selected elements in the tabel selection structure.
     * 
     * @return selected elements
     */
    IJaretTableSelection getSelection();

    /**
     * Add a listener to listen to changes of the selection.
     * 
     * @param jtsm listener
     */
    void addTableSelectionModelListener(IJaretTableSelectionModelListener jtsm);

    /**
     * Remove a listener.
     * 
     * @param jtsm listener to be removed
     */
    void removeTableSelectionModelListener(IJaretTableSelectionModelListener jtsm);

}
