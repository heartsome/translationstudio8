/*
 *  File: CellEditorBase.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.editor;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Abstract base implementation for ICellEditors for the jaret table.
 * 
 * @author Peter Kliem
 * @version $Id: CellEditorBase.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public abstract class CellEditorBase implements ICellEditor {

    /** member storing the last requested row. */
    protected IRow _row;
    /** member storing the last requested column. */
    protected IColumn _column;
    /** member storing the requesting table. */
    protected JaretTable _table;

    /**
     * {@inheritDoc} Base implementation storing the table and row/col information.
     */
    public Control getEditorControl(JaretTable table, IRow row, IColumn column, char typedKey) {
        _table = table;
        _row = row;
        _column = column;

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        // help the garbage collector
        _table = null;
        _column = null;
        _row = null;

    }

    /**
     * {@inheritDoc} default will always return -1.
     */
    public int getPreferredHeight() {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    public boolean handleClick(JaretTable table, IRow row, IColumn column, Rectangle drawingArea, int x, int y) {
        // no action on single click
        return false;
    }

}
