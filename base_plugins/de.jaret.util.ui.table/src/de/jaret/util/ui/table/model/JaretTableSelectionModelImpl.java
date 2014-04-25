/*
 *  File: JaretTableSelectionModelImpl.java 
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
import java.util.Vector;

/**
 * Implementation of the JaretTableSelectionModel.
 * 
 * @author Peter Kliem
 * @version $Id: JaretTableSelectionModelImpl.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class JaretTableSelectionModelImpl implements IJaretTableSelectionModel {
    /** listeners to inform. */
    protected List<IJaretTableSelectionModelListener> _listeners;

    /** true for allowance of full row selection. */
    protected boolean _fullRowSelectionAllowed = true;

    /** true for allowance of full column selection. */
    protected boolean _fullColumnSelectionAllowed = true;

    /** true for allowance of single cell selection. */
    protected boolean _cellSelectioAllowed = true;

    /** true if multiple selection of more than one elemnt is allowed. */
    protected boolean _multipleSelectionAllowed = true;

    /** true if only row selections are allowed. */
    protected boolean _onlyRowSelectionAllowed = false;

    /** the selection data store. */
    protected IJaretTableSelection _selection = new JaretTableSelectionImpl();

    /**
     * {@inheritDoc}
     */
    public void clearSelection() {
        List<IRow> l = new ArrayList<IRow>();
        l.addAll(_selection.getSelectedRows());
        for (IRow row : l) {
            remSelectedRow(row);
        }
        List<IColumn> c = new ArrayList<IColumn>();
        c.addAll(_selection.getSelectedColumns());
        for (IColumn col : c) {
            remSelectedColumn(col);
        }
        List<IJaretTableCell> t = new ArrayList<IJaretTableCell>();
        t.addAll(_selection.getSelectedCells());
        for (IJaretTableCell cell : t) {
            remSelectedCell(cell);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFullRowSelectionAllowed() {
        return _fullRowSelectionAllowed;
    }

    /**
     * {@inheritDoc}
     */
    public void setFullRowSelectionAllowed(boolean allowed) {
        _fullRowSelectionAllowed = allowed;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFullColumnSelectionAllowed() {
        return _fullColumnSelectionAllowed;
    }

    /**
     * {@inheritDoc}
     */
    public void setFullColumnSelectionAllowed(boolean allowed) {
        _fullColumnSelectionAllowed = allowed;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCellSelectionAllowed() {
        return _cellSelectioAllowed;
    }

    /**
     * {@inheritDoc}
     */
    public void setCellSelectionAllowed(boolean allowed) {
        _cellSelectioAllowed = allowed;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMultipleSelectionAllowed() {
        return _multipleSelectionAllowed;
    }

    /**
     * {@inheritDoc}
     */
    public void setMultipleSelectionAllowed(boolean allowed) {
        _multipleSelectionAllowed = allowed;
    }

    /**
     * {@inheritDoc}
     */
    public void addSelectedRow(IRow row) {
        if (!_selection.getSelectedRows().contains(row)) {
            _selection.addRow(row);
            fireRowSelectionAdded(row);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remSelectedRow(IRow row) {
        if (_selection.getSelectedRows().contains(row)) {
            _selection.remRow(row);
            fireRowSelectionRemoved(row);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addSelectedColumn(IColumn column) {
        if (!_selection.getSelectedColumns().contains(column)) {
            _selection.addColumn(column);
            fireColumnSelectionAdded(column);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remSelectedColumn(IColumn column) {
        if (_selection.getSelectedColumns().contains(column)) {
            _selection.remColumn(column);
            fireColumnSelectionRemoved(column);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addSelectedCell(IJaretTableCell cell) {
        if (!_selection.getSelectedCells().contains(cell)) {
            _selection.addCell(cell);
            fireCellSelectionAdded(cell);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remSelectedCell(IJaretTableCell cell) {
        if (_selection.getSelectedCells().contains(cell)) {
            _selection.remCell(cell);
            fireCellSelectionRemoved(cell);
        }
    }

    /**
     * {@inheritDoc}
     */
    public IJaretTableSelection getSelection() {
        return _selection;
    }


    
    private void fireRowSelectionAdded(IRow row) {
        if (_listeners != null) {
            for (IJaretTableSelectionModelListener listener : _listeners) {
                listener.rowSelectionAdded(row);
            }
        }
    }

    private void fireRowSelectionRemoved(IRow row) {
        if (_listeners != null) {
            for (IJaretTableSelectionModelListener listener : _listeners) {
                listener.rowSelectionRemoved(row);
            }
        }
    }

    private void fireColumnSelectionAdded(IColumn column) {
        if (_listeners != null) {
            for (IJaretTableSelectionModelListener listener : _listeners) {
                listener.columnSelectionAdded(column);
            }
        }
    }

    private void fireColumnSelectionRemoved(IColumn column) {
        if (_listeners != null) {
            for (IJaretTableSelectionModelListener listener : _listeners) {
                listener.columnSelectionRemoved(column);
            }
        }
    }

    private void fireCellSelectionAdded(IJaretTableCell cell) {
        if (_listeners != null) {
            for (IJaretTableSelectionModelListener listener : _listeners) {
                listener.cellSelectionAdded(cell);
            }
        }
    }

    private void fireCellSelectionRemoved(IJaretTableCell cell) {
        if (_listeners != null) {
            for (IJaretTableSelectionModelListener listener : _listeners) {
                listener.cellSelectionRemoved(cell);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void addTableSelectionModelListener(IJaretTableSelectionModelListener jtsm) {
        if (_listeners == null) {
            _listeners = new Vector<IJaretTableSelectionModelListener>();
        }
        _listeners.add(jtsm);

    }

    /**
     * {@inheritDoc}
     */
    public void removeTableSelectionModelListener(IJaretTableSelectionModelListener jtsm) {
        if (_listeners != null) {
            _listeners.remove(jtsm);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOnlyRowSelectionAllowed() {
        return _onlyRowSelectionAllowed;
    }

    /**
     * {@inheritDoc}
     */
    public void setOnlyRowSelectionAllowed(boolean allowed) {
        _onlyRowSelectionAllowed = allowed;
    }

}
