/*
 *  File: SimpleColumn.java 
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

import de.jaret.util.ui.table.model.AbstractColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Simple implementation of a column for use in the SimpleJaretTableModel.
 * 
 * @author kliem
 * @version $Id: SimpleColumn.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class SimpleColumn extends AbstractColumn {
    /** Map holding the column values. */
    private Map<Integer, Object> _values = new HashMap<Integer, Object>();
    /** header label. */
    private String _headerLabel = "";
    /** index of the column. */
    private int _index;
    /**
     * Tablemodel the column is part of.
     */
    private SimpleJaretTableModel _model;

    /**
     * Construct a column.
     * 
     * @param index index
     * @param model table model the column is part of
     */
    public SimpleColumn(int index, SimpleJaretTableModel model) {
        _index = index;
        _model = model;
    }

    /**
     * Allow setting of the header label.
     * 
     * @param label the label
     */
    public void setHeaderLabel(String label) {
        _headerLabel = label;
    }

    /**
     * {@inheritDoc} Always assume different classes and rely on the object tht is the value.
     */
    public Class<?> getContentClass() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getHeaderLabel() {
        return _headerLabel;
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return Integer.toString(_index);
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(IRow row) {
        return _values.get(((SimpleRow) row).getIndex());
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(IRow row, Object value) {
        Object oldVal = getValue(row);
        _values.put(((SimpleRow) row).getIndex(), value);
        fireValueChanged(row, this, oldVal, value);
        _model.cellChanged(row, this, value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsSorting() {
        return true;
    }

    /**
     * {@inheritDoc} Sorting set null &lt; non-null, non comparables are equal.
     */
    @SuppressWarnings("unchecked")
    public int compare(IRow o1, IRow o2) {
        Object v1 = getValue(o1);
        Object v2 = getValue(o2);
        if (v1 == null && v2 != null) {
            return -1;
        }
        if (v2 == null && v1 != null) {
            return 1;
        }
        if (v1 instanceof Comparable) {
            Comparable c1 = (Comparable) v1;
            try {
                return c1.compareTo(v2);
            } catch (Exception e) {
                // ignore
            }
        }

        return 0;
    }

}
