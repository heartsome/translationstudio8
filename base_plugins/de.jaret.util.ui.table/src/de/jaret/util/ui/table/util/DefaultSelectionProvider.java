/*
 *  File: DefaultSelectionProvider.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IJaretTableCell;
import de.jaret.util.ui.table.model.IJaretTableSelection;
import de.jaret.util.ui.table.model.IRow;

/**
 * Default implementation of a SelectionProvider based on the jarettable. This will simply put rows, columns and cells in
 * a structured selection.
 * 
 * @author Peter Kliem
 * @version $Id: DefaultSelectionProvider.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class DefaultSelectionProvider extends AbstractSelectionProvider {

    /**
     * Create a default selection provider.
     * 
     * @param table JaretTable providing the selection
     */
    public DefaultSelectionProvider(JaretTable table) {
        super(table);
    }

    /**
     * {@inheritDoc}. Returns a structured selection containig rows and columns and cells that have been selected.
     */
    @SuppressWarnings("unchecked")
    protected ISelection getISelection() {
        IJaretTableSelection selection = _table.getSelectionModel().getSelection();
        if (selection != null && !selection.isEmpty()) {
            List list = new ArrayList();
            for (IRow row : selection.getSelectedRows()) {
                list.add(row);
            }
            for (IColumn col : selection.getSelectedColumns()) {
                list.add(col);
            }
            for (IJaretTableCell cell : selection.getSelectedCells()) {
                list.add(cell);
            }
            StructuredSelection sselection = new StructuredSelection(list);
            return sselection;
        }
        return new StructuredSelection();
    }

    /**
     * {@inheritDoc}
     */
    protected void setISelection(ISelection selection) {
        // TODO Auto-generated method stub
    }

}
