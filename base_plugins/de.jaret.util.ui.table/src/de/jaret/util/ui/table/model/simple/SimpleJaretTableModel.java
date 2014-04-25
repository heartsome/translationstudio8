/*
 *  File: SimpleJaretTableModel.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model.simple;

import java.util.HashMap;
import java.util.Map;

import de.jaret.util.ui.table.model.AbstractJaretTableModel;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Imlpementation of a very simple table model for the jaret table. The model is formed of hash maps holding objects,
 * allowing storage of values at arbitrary indizes.
 * 
 * @author kliem
 * @version $Id: SimpleJaretTableModel.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class SimpleJaretTableModel extends AbstractJaretTableModel {
    /** map holding the columns. */
    private Map<Integer, SimpleColumn> _cols = new HashMap<Integer, SimpleColumn>();
    /** map holding the rows. */
    private Map<Integer, SimpleRow> _rows = new HashMap<Integer, SimpleRow>();
    /** current maximum of the col indizes. */
    private int _colCount = -1;
    /** current maximum of the row indizes. */
    private int _rowCount = -1;

    /**
     * Set a value.
     * 
     * @param colIdx index of the column (x)
     * @param rowIdx index of the row (y)
     * @param value value
     */
    public void setValueAt(int colIdx, int rowIdx, Object value) {
        IColumn col = getColumn(colIdx);
        IRow row = getRow(rowIdx);
        col.setValue(row, value);
    }

    /**
     * Get a value.
     * 
     * @param colIdx index of the column (x)
     * @param rowIdx index of the row (y)
     * @return value at the given index or <code>null</code> if none present
     */
    public Object getValueAt(int colIdx, int rowIdx) {
        IColumn col = getColumn(colIdx);
        return col.getValue(getRow(rowIdx));
    }

    /**
     * Set the header label for a column.
     * 
     * @param colIdx index
     * @param label label to set
     */
    public void setHeaderLabel(int colIdx, String label) {
        ((SimpleColumn) getColumn(colIdx)).setHeaderLabel(label);
    }

    /**
     * {@inheritDoc}
     */
    public IColumn getColumn(int idx) {
        SimpleColumn col = _cols.get(idx);
        if (col == null) {
            col = new SimpleColumn(idx, this);
            _cols.put(idx, col);
            // fireColumnAdded(idx, col);
            _colCount = Math.max(_colCount, idx);
        }
        return col;
    }

    /**
     * {@inheritDoc}
     */
    public int getColumnCount() {
        return _colCount + 1;
    }

    /**
     * {@inheritDoc}
     */
    public IRow getRow(int idx) {
        SimpleRow row = _rows.get(idx);
        if (row == null) {
            row = new SimpleRow(idx);
            _rows.put(idx, row);
            // fireRowAdded(idx, row);
            _rowCount = Math.max(_rowCount, idx);
        }
        return row;
    }

    /**
     * {@inheritDoc}
     */
    public int getRowCount() {
        return _rowCount + 1;
    }

    /**
     * {@inheritDoc}
     */
    public void cellChanged(IRow row, SimpleColumn column, Object value) {
        fireCellChanged(row, column);
    }

    /**
     * {@inheritDoc} addColumn is not implementable for the SimpleModel; does nothing.
     */
    public void addColumn(IColumn column) {
        // do nothing
    }
    
    
}
