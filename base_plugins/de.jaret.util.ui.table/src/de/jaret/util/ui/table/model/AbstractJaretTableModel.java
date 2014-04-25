/*
 *  File: AbstractJaretTableModel.java 
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
import java.util.Vector;

/**
 * Abstract base implementation of a JaretTableModel.
 * 
 * @author Peter Kliem
 * @version $Id: AbstractJaretTableModel.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public abstract class AbstractJaretTableModel implements IJaretTableModel {
    /** registered listeners. */
    protected List<IJaretTableModelListener> _listeners;

    /**
     * {@inheritDoc} Simple default implementation.
     */
    public IColumn getColumn(String id) {
        for (int i = 0; i < getColumnCount(); i++) {
            if (getColumn(i).getId() != null && getColumn(i).getId().equals(id)) {
                return getColumn(i);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(IRow row, IColumn column, Object value) {
        column.setValue(row, value);
    }

    /**
     * {@inheritDoc} Delegates to the column.
     */
    public boolean isEditable(IRow row, IColumn column) {
        return column.isEditable(row);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void addJaretTableModelListener(IJaretTableModelListener jtml) {
        if (_listeners == null) {
            _listeners = new Vector<IJaretTableModelListener>();
        }
        _listeners.add(jtml);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void removeJaretTableModelListener(IJaretTableModelListener jtml) {
        if (_listeners != null) {
            _listeners.remove(jtml);
        }
    }

    /**
     * Inform listeners about an added row.
     * 
     * @param idx index of the row
     * @param row the row
     */
    protected void fireRowAdded(int idx, IRow row) {
        if (_listeners != null) {
            for (int i = 0; i < _listeners.size(); i++) {
                IJaretTableModelListener listener = _listeners.get(i);
                listener.rowAdded(idx, row);
            }
        }
    }

    /**
     * Inform listeners about a removed row.
     * 
     * @param row the removed row
     */
    protected void fireRowRemoved(IRow row) {
        if (_listeners != null) {
            for (int i = 0; i < _listeners.size(); i++) {
                IJaretTableModelListener listener = _listeners.get(i);
                listener.rowRemoved(row);
            }
        }
    }

    /**
     * Inform listeners abou a changed row.
     * 
     * @param row the changed row
     */
    protected void fireRowChanged(IRow row) {
        if (_listeners != null) {
            for (int i = 0; i < _listeners.size(); i++) {
                IJaretTableModelListener listener = _listeners.get(i);
                listener.rowChanged(row);
            }
        }
    }

    /**
     * Inform listeners about an added column.
     * 
     * @param idx index
     * @param column column
     */
    protected void fireColumnAdded(int idx, IColumn column) {
        if (_listeners != null) {
            for (IJaretTableModelListener listener : _listeners) {
                listener.columnAdded(idx, column);
            }
        }
    }

    /**
     * Inform listeners about a removed column.
     * 
     * @param column the now missing column
     */
    protected void fireColumnRemoved(IColumn column) {
        if (_listeners != null) {
            for (IJaretTableModelListener listener : _listeners) {
                listener.columnRemoved(column);
            }
        }
    }

    /**
     * Inform listeners about a changed column.
     * 
     * @param column changed col
     */
    protected void fireColumnChanged(IColumn column) {
        if (_listeners != null) {
            for (IJaretTableModelListener listener : _listeners) {
                listener.columnChanged(column);
            }
        }
    }

    /**
     * Inform listeners about a changed cell.
     * 
     * @param row row of the cell
     * @param column olumn of the cell
     */
    protected void fireCellChanged(IRow row, IColumn column) {
        if (_listeners != null) {
            for (IJaretTableModelListener listener : _listeners) {
                listener.cellChanged(row, column);
            }
        }
    }

    /**
     * Inform listeners about a general change of the model data.
     * 
     */
    protected void fireTableDataChanged() {
        if (_listeners != null) {
            for (IJaretTableModelListener listener : _listeners) {
                listener.tableDataChanged();
            }
        }
    }

}
