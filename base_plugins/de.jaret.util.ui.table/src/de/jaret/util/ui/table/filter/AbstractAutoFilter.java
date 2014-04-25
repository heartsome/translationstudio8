/*
 *  File: AbstractAutoFilter.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.filter;

import java.util.HashSet;
import java.util.Set;

import de.jaret.util.misc.PropertyObservableBase;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * An abstract base that can be used when implementing autofilters.
 * 
 * @author kliem
 * @version $Id: AbstractAutoFilter.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public abstract class AbstractAutoFilter extends PropertyObservableBase implements IAutoFilter {
    /** table of the autofilter. */
    protected JaretTable _table;

    /** column of the autofilter. */
    protected IColumn _column;

    /**
     * {@inheritDoc}
     */
    public void setColumn(IColumn column) {
        _column = column;
    }

    /**
     * {@inheritDoc}
     */
    public void setTable(JaretTable table) {
        _table = table;
    }

    /**
     * Get all possible filter strings for the autofilter comboboxes.
     * 
     * @param col the column to look at
     * @param maxLength the maxmium length of the individual strings. If a string is larger than that it wil be
     * truncated, -1 for no truncation
     * @return set of strings, shortened if necessary
     */
    protected Set<String> getColFilterStrings(IColumn col, int maxLength) {
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < _table.getRowCount(); i++) {
            IRow row = _table.getRow(i);
            Object val = col.getValue(row);
            if (val != null) {
                String valStr = val.toString();
                if (maxLength != -1 && valStr.length() > maxLength) {
                    // TODO mark for proper use in filters
                    valStr = valStr.substring(0, maxLength - 1);
                }
                result.add(valStr);
            }
        }

        return result;
    }

}
