/*
 *  File: AbstractColumn.java 
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
 * Abstract base implemenation of an IColumn.
 * 
 * @author Peter Kliem
 * @version $Id: AbstractColumn.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public abstract class AbstractColumn implements IColumn {
    /** column listeners. */
    protected List<IColumnListener> _listeners;

    /**
     * Inform listeners about a value change.
     * 
     * @param row row
     * @param column column
     * @param oldValue old value
     * @param newValue new value
     */
    protected void fireValueChanged(IRow row, IColumn column, Object oldValue, Object newValue) {
        if (_listeners != null) {
            for (IColumnListener listener : _listeners) {
                listener.valueChanged(row, column, oldValue, newValue);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void addColumnListener(IColumnListener cl) {
        if (_listeners == null) {
            _listeners = new Vector<IColumnListener>();
        }
        _listeners.add(cl);
    }

    /**
     * {@inheritDoc}
     */
    public void remColumnListener(IColumnListener cl) {
        if (_listeners != null) {
            _listeners.remove(cl);
        }
    }

    /**
     * Default implementation: no difference to getContentClass(). {@inheritDoc}
     */
    public Class<?> getContentClass(IRow row) {
        return getContentClass();
    }

    /**
     * Header display always defaults to true. {@inheritDoc}
     */
    public boolean displayHeader() {
        return true;
    }

    /**
     * Deafult: cols are aditable.
     * 
     * @return true
     */
    public boolean isEditable() {
        return true;
    }

    /**
     * Default: delegate to <code>isEditable</code>. {@inheritDoc}
     */
    public boolean isEditable(IRow row) {
        return isEditable();
    }

}
