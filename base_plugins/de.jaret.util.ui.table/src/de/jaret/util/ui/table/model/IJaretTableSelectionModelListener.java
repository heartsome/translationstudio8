/*
 *  File: IJaretTableSelectionModelListener.java 
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
 * Listener for listening on selection changes on a jaret table selection.
 * 
 * @author Peter Kliem
 * @version $Id: IJaretTableSelectionModelListener.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IJaretTableSelectionModelListener {
    /**
     * Called whenever a row has been added to a selection.
     * 
     * @param row row added.
     */
    void rowSelectionAdded(IRow row);

    /**
     * Called whenever a row has been removed from the selection.
     * 
     * @param row row removed.
     */
    void rowSelectionRemoved(IRow row);

    /**
     * Called whenever a cell has been added to a selection.
     * 
     * @param cell cell added
     */
    void cellSelectionAdded(IJaretTableCell cell);

    /**
     * Called whenever a cell has been removed from the selection.
     * 
     * @param cell cell removed
     */
    void cellSelectionRemoved(IJaretTableCell cell);

    /**
     * Called whenever a column has been added to a selection.
     * 
     * @param column column added
     */
    void columnSelectionAdded(IColumn column);

    /**
     * Called whenever a column has been removed from the selection.
     * 
     * @param column column removed
     */
    void columnSelectionRemoved(IColumn column);

}
