/*
 *  File: DefaultJaretTableModel.java 
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
import java.util.List;

/**
 * Default Jaret table model.
 * 
 * @author Peter Kliem
 * @version $Id: DefaultJaretTableModel.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class DefaultJaretTableModel extends AbstractJaretTableModel implements IJaretTableModel {
    /** rows. */
    protected List<IRow> _rows = new ArrayList<IRow>();
    /** columns. */
    protected List<IColumn> _cols = new ArrayList<IColumn>();

    /**
     * {@inheritDoc}
     */
    public int getRowCount() {
        return _rows.size();
    }

    /**
     * {@inheritDoc}
     */
    public IRow getRow(int idx) {
        return _rows.get(idx);
    }

    /**
     * Add a row to the model.
     * 
     * @param row row to add
     */
    public void addRow(IRow row) {
        _rows.add(row);
        fireRowAdded(_rows.size() - 1, row);
    }

    /**
     * Add a row to the model at a specified index.
     * 
     * @param index index the row will be inserted
     * @param row row to add
     */
    public void addRow(int index, IRow row) {
        _rows.add(index, row);
        fireRowAdded(index, row);
    }

    /**
     * Remove row from the model.
     * 
     * @param row row to remove
     */
    public void remRow(IRow row) {
        if (_rows.contains(row)) {
            _rows.remove(row);
            fireRowRemoved(row);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getColumnCount() {
        return _cols.size();
    }

    /**
     * {@inheritDoc}
     */
    public IColumn getColumn(int idx) {
        return _cols.get(idx);
    }

    /**
     * Retrieve a column by it's ID.
     * 
     * @param id id of the column to look for
     * @return the column or <code>null</code> if no Column with the id could be found.
     */
    public IColumn getColumn(String id) {
        for (IColumn col : _cols) {
            if (col.getId().equals(id)) {
                return col;
            }
        }
        return null;
    }

    /**
     * Add a column to the model.
     * 
     * @param column col to add
     */
    public void addColumn(IColumn column) {
        _cols.add(column);
        fireColumnAdded(_cols.size() - 1, column);
    }

    /**
     * Remove a column from the model.
     * 
     * @param column col to remove
     */
    public void remColumn(IColumn column) {
        if (_cols.contains(column)) {
            _cols.remove(column);
            fireColumnRemoved(column);
        }
    }

}
