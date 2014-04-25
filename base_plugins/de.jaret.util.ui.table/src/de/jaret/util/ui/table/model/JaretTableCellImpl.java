/*
 *  File: JaretTableCellImpl.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

/**
 * Implementation of the IJaretTableCell.
 * 
 * @author Peter Kliem
 * @version $Id: JaretTableCellImpl.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class JaretTableCellImpl implements IJaretTableCell {
    /** the row. */
    protected IRow _row;

    /** the column. */
    protected IColumn _column;

    /**
     * Construct a table cell instance.
     * 
     * @param row the row
     * @param column the column
     */
    public JaretTableCellImpl(IRow row, IColumn column) {
        _column = column;
        _row = row;
    }

    /**
     * @return Returns the column.
     */
    public IColumn getColumn() {
        return _column;
    }

    /**
     * @return Returns the row.
     */
    public IRow getRow() {
        return _row;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof IJaretTableCell)) {
            return false;
        }
        IJaretTableCell cell = (IJaretTableCell) o;
        return _row.equals(cell.getRow()) && _column.equals(cell.getColumn());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return _row.hashCode() * _column.hashCode();
    }
}
