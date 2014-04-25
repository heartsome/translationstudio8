/*
 *  File: AbstractSelectionProvider.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.util;

import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IJaretTableCell;
import de.jaret.util.ui.table.model.IJaretTableSelectionModelListener;
import de.jaret.util.ui.table.model.IRow;

/**
 * Abstract base for an ISelectionProvider based on a jaret Table.
 * 
 * @author Peter Kliem
 * @version $Id: AbstractSelectionProvider.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public abstract class AbstractSelectionProvider implements ISelectionProvider, IJaretTableSelectionModelListener {
    /** jaret table the selection provider listens to. */
    protected JaretTable _table;
    /** list of ISelection listeners. * */
    protected List<ISelectionChangedListener> _selectionChangeListeners;

    /**
     * Contruct an abstract selection provider.
     * 
     * @param table JaretTable to listen to
     */
    public AbstractSelectionProvider(JaretTable table) {
        _table = table;
        _table.getSelectionModel().addTableSelectionModelListener(this);
    }

    // ///////////// ISelectionProvider

    /**
     * {@inheritDoc}
     */
    public synchronized void addSelectionChangedListener(ISelectionChangedListener listener) {
        if (_selectionChangeListeners == null) {
            _selectionChangeListeners = new Vector<ISelectionChangedListener>();
        }
        _selectionChangeListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        if (_selectionChangeListeners != null) {
            _selectionChangeListeners.remove(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setSelection(ISelection selection) {
        setISelection(selection);
    }

    /**
     * {@inheritDoc}
     * Retrieve an IStructuredSelection of the current selection (will contain rows, columns and cells).
     */
    public ISelection getSelection() {
        return getISelection();
    }

    /**
     * Override this method to return an ISelectiobn appropriate for the intended use.
     * 
     * @return ISelection
     */
    protected abstract ISelection getISelection();

    /**
     * Override this method to set a selection on the table based on an ISelection.
     * 
     * @param selection ISelection to be set.
     */
    protected abstract void setISelection(ISelection selection);

    /**
     * Inform listeners about a change of selection.
     */
    private void fireSelectionChanged() {
        SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
        if (_selectionChangeListeners != null) {
            for (ISelectionChangedListener listener : _selectionChangeListeners) {
                listener.selectionChanged(event);
            }
        }
    }

    // ////////// IjaretTableModeSelectionListener

    /**
     * {@inheritDoc}
     */
    public void rowSelectionAdded(IRow row) {
        fireSelectionChanged();
    }

    /**
     * {@inheritDoc}
     */
    public void rowSelectionRemoved(IRow row) {
        fireSelectionChanged();
    }

    /**
     * {@inheritDoc}
     */
    public void cellSelectionAdded(IJaretTableCell cell) {
        fireSelectionChanged();
    }

    /**
     * {@inheritDoc}
     */
    public void cellSelectionRemoved(IJaretTableCell cell) {
        fireSelectionChanged();
    }

    /**
     * {@inheritDoc}
     */
    public void columnSelectionAdded(IColumn column) {
        fireSelectionChanged();
    }

    /**
     * {@inheritDoc}
     */
    public void columnSelectionRemoved(IColumn column) {
        fireSelectionChanged();
    }

    // //// end ISelectionProvider

}
