/*
 *  File: SimpleRow.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model.simple;

import de.jaret.util.ui.table.model.IRow;

/**
 * Simple row implementation based on it's index, used as a marker in the SimpleJaretTableModel.
 * 
 * @author kliem
 * @version $Id: SimpleRow.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class SimpleRow implements IRow {
    /** index of the column. */
    private int _index;

    /**
     * Constructor.
     * 
     * @param idx index
     */
    public SimpleRow(int idx) {
        _index = idx;
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return Integer.toString(_index);
    }

    /**
     * Retrieve the index of the row.
     * 
     * @return index of the row
     */
    public int getIndex() {
        return _index;
    }

    /**
     * {@inheritDoc} Equals based on the index of the row.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof SimpleRow) {
            SimpleRow r = (SimpleRow) obj;
            return _index == r.getIndex();
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc} hashcode based on the index of the column.
     */
    public int hashCode() {
        return _index;
    }

}
