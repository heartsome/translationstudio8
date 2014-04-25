/*
 *  File: JaretTableSelectionImpl.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the JaretTableSelection.
 * 
 * @author Peter Kliem
 * @version $Id: JaretTableSelectionImpl.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class JaretTableSelectionImpl implements IJaretTableSelection {
    /** selected rows. */
    protected List<IRow> _rows = new ArrayList<IRow>();
    /** selected columns. */
    protected List<IColumn> _columns = new ArrayList<IColumn>();
    /** selected cells. */
    protected List<IJaretTableCell> _cells = new ArrayList<IJaretTableCell>();

    /**
     * {@inheritDoc}
     */
    public void clear() {
        _rows.clear();
        _columns.clear();
        _cells.clear();
    }

    /**
     * {@inheritDoc}
     */
    public List<IRow> getSelectedRows() {
        return _rows;
    }

    /**
     * {@inheritDoc}
     */
    public List<IColumn> getSelectedColumns() {
        return _columns;
    }

    /**
     * {@inheritDoc}
     */
    public List<IJaretTableCell> getSelectedCells() {
        return _cells;
    }

    /**
     * {@inheritDoc}
     */
    public void addRow(IRow row) {
        _rows.add(row);
    }

    /**
     * {@inheritDoc}
     */
    public void remRow(IRow row) {
        _rows.remove(row);
    }

    /**
     * {@inheritDoc}
     */
    public void addColumn(IColumn column) {
        _columns.add(column);
    }

    /**
     * {@inheritDoc}
     */
    public void remColumn(IColumn column) {
        _columns.remove(column);
    }

    /**
     * {@inheritDoc}
     */
    public void addCell(IJaretTableCell cell) {
        _cells.add(cell);
    }

    /**
     * {@inheritDoc}
     */
    public void remCell(IJaretTableCell cell) {
        _cells.remove(cell);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return _rows.size() == 0 && _columns.size() == 0 && _cells.size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    public Set<IJaretTableCell> getAllSelectedCells(IJaretTableModel model) {
        Set<IJaretTableCell> set = new HashSet<IJaretTableCell>();
        for (IRow row : _rows) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                JaretTableCellImpl cell = new JaretTableCellImpl(row, model.getColumn(i));
                set.add(cell);
            }
        }
        for (IColumn col : _columns) {
            for (int i = 0; i < model.getRowCount(); i++) {
                JaretTableCellImpl cell = new JaretTableCellImpl(model.getRow(i), col);
                set.add(cell);
            }

        }
        set.addAll(_cells);
        return set;
    }

}
