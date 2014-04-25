/*
 *  File: IndexColumn.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import de.jaret.util.ui.table.JaretTable;

/**
 * A simple column displaying the row index.
 * 
 * @author Peter Kliem
 * @version $Id: IndexColumn.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class IndexColumn extends AbstractColumn implements IColumn {
    /** id of the index col. */
    public static final String ID = "indexColumnId";
    /** table the column is for. */
    protected JaretTable _table;

    /** haeder label for the column. */
    protected String _headerLabel;
    
    /**
     * Construct an index column.
     * 
     * @param table table the indexcolumn is used for
     * @param headerLabel hedare label to use
     */
    public IndexColumn(JaretTable table, String headerLabel) {
        _table = table;
        _headerLabel = headerLabel;
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return ID;
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
    public Object getValue(IRow row) {
        return new Integer(_table.getInternalRowIndex(row));
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(IRow row, Object value) {
    }

    /**
     * {@inheritDoc}
     */
    public int compare(IRow r1, IRow r2) {
        return ((Integer) getValue(r1)).compareTo((Integer) getValue(r2));
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsSorting() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getContentClass() {
        return Integer.class;
    }
    
    /**
     * {@inheritDoc} Never editable.
     */
    public boolean isEditable() {
        return false;
    }

    /**
     * {@inheritDoc} Never editable.
     */
    public boolean isEditable(IRow row) {
        return false;
    }
}
