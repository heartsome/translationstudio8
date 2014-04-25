/*
 *  File: OptimizeRowHeightAction.java 
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
import de.jaret.util.ui.table.model.IJaretTableSelection;
import de.jaret.util.ui.table.model.IRow;

/**
 * Action that registers all selected rows for optimization of their respective heights.
 * 
 * @author Peter Kliem
 * @version $Id: OptimizeRowHeightAction.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class OptimizeRowHeightAction extends Action {
    protected JaretTable _table;

    public OptimizeRowHeightAction(JaretTable table) {
        _table = table;
    }

    @Override
    public void run() {
        IJaretTableSelection selection = _table.getSelectionModel().getSelection();
        if (!selection.isEmpty() && selection.getSelectedRows().size() > 0) {
            for (IRow row : selection.getSelectedRows()) {
                _table.optimizeHeight(row);
            }
        }
    }

    @Override
    public String getText() {
        return "Optimal row height";
    }
}
