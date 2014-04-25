/*
 *  File: OptimizeAllRowHeightsAction.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.util.action;

import org.eclipse.jface.action.Action;

import de.jaret.util.ui.table.JaretTable;

/**
 * Action that registers all rows of the model for optimization of the row height.
 * 
 * @author Peter Kliem
 * @version $Id: OptimizeAllRowHeightsAction.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class OptimizeAllRowHeightsAction extends Action {
    /** tbale the action has been constructed for. */
    protected JaretTable _table;

    /**
     * Construct the action.
     * 
     * @param table table to operate on
     */
    public OptimizeAllRowHeightsAction(JaretTable table) {
        _table = table;
    }

    /**
     * {@inheritDoc} call optimize height for all rows of the table.
     */
    public void run() {
        for (int i = 0; i < _table.getTableModel().getRowCount(); i++) {
            _table.optimizeHeight(_table.getTableModel().getRow(i));
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        return "Optimal row heights for all rows";
    }
}
