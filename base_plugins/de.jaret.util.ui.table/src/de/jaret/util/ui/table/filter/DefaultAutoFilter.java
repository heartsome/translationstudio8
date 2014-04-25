/*
 *  File: DefaultAutoFilter.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.filter;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;

import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Default implementation of the IAutofilter interface rendering a combobox with a simple selection mechanism.
 * 
 * @author kliem
 * @version $Id: DefaultAutoFilter.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class DefaultAutoFilter extends AbstractAutoFilter implements IAutoFilter, SelectionListener {
    /** maximal length of an object list in a autofilter combox (-1 for unlimited). */
    private static final int MAX_AUTOFILTERLENGTH = -1;

    /** text: to be externalized. */
    protected static final String TEXT_FILTER_ALL = "(all)";
    /** text: to be externalized. */
    protected static final String TEXT_FILTER_EMPTY = "(empty)";
    /** text: to be externalized. */
    protected static final String TEXT_FILTER_NONEMPTY = "(non-empty)";

    /** control used for the filter -> coombobox. */
    protected CCombo _combo;

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        if (_combo != null) {
            _combo.dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Control getControl() {
        return _combo;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInResult(IRow row) {
        String filter = _combo.getText();
        Object value = _column.getValue(row);
        String valString = value != null ? value.toString() : "";
        if (!filter.equals(TEXT_FILTER_ALL)) {
            if (filter.equals(TEXT_FILTER_EMPTY) && valString.trim().length() > 0) {
                return false;
            }
            if (filter.equals(TEXT_FILTER_NONEMPTY) && valString.trim().length() == 0) {
                return false;
            }
            if (!filter.equals(TEXT_FILTER_ALL) && !filter.equals(TEXT_FILTER_EMPTY)
                    && !filter.equals(TEXT_FILTER_NONEMPTY)) {
                if (!filter.equals(valString)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void update() {
        if (_combo == null) {
            _combo = new CCombo(_table, SWT.BORDER | SWT.READ_ONLY);
            _combo.addSelectionListener(this);
        }
        IColumn col = _column;

        Set<String> colFilterStrings = getColFilterStrings(col, MAX_AUTOFILTERLENGTH);

        String[] items = new String[colFilterStrings.size() + 3];
        int idx = 0;
        items[idx++] = TEXT_FILTER_ALL;
        items[idx++] = TEXT_FILTER_EMPTY;
        items[idx++] = TEXT_FILTER_NONEMPTY;
        for (String s : colFilterStrings) {
            items[idx++] = s;
        }

        _combo.setItems(items);
        _combo.select(0);
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        _combo.select(0);
        firePropertyChange("FILTER", null, "x");
    }
    
    /**
     * {@inheritDoc}
     */
    public void widgetDefaultSelected(SelectionEvent e) {
    }

    /**
     * {@inheritDoc} Inform everyone the filter changed.
     */
    public void widgetSelected(SelectionEvent e) {
        firePropertyChange("FILTER", null, "x");
    }

}
