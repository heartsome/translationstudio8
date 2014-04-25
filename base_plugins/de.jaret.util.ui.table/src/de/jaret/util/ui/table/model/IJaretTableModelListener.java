/*
 *  File: IJaretTableModelListener.java 
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
 * Listener for listening to table model changes on a jaret table model.
 * 
 * @author Peter Kliem
 * @version $Id: IJaretTableModelListener.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IJaretTableModelListener {
    /**
     * Called if there has been a change in the row data.
     * 
     * @param row row that changed.
     */
    void rowChanged(IRow row);

    /**
     * Called when a row has been removed from the model.
     * 
     * @param row removed row.
     */
    void rowRemoved(IRow row);

    /**
     * Called when a row has been added to the model.
     * 
     * @param idx index of the added row.
     * @param row row that has been added.
     */
    void rowAdded(int idx, IRow row);

    /**
     * Called when a column has been added to the table model.
     * 
     * @param idx index of the new column.
     * @param column the new column.
     */
    void columnAdded(int idx, IColumn column);

    /**
     * Called when a column has been removed from the model.
     * 
     * @param column the removed row.
     */
    void columnRemoved(IColumn column);

    /**
     * Called when a column changed.
     * 
     * @param column changed column.
     */
    void columnChanged(IColumn column);

    /**
     * The value of the specified cell changed.
     * 
     * @param row of the cell
     * @param column of the cell
     */
    void cellChanged(IRow row, IColumn column);

    /**
     * All table data has been invalidated.
     * 
     */
    void tableDataChanged();

}
