/*
 *  File: DefaultFillDragStrategy.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.strategies;

import java.util.List;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IJaretTableCell;

/**
 * Defaut implementation of a fill drag strategy: simply copy the content.
 * 
 * @author Peter Kliem
 * @version $Id: DefaultFillDragStrategy.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class DefaultFillDragStrategy implements IFillDragStrategy {

    /**
     * {@inheritDoc}
     */
    public void doFill(JaretTable table, IJaretTableCell firstCell, List<IJaretTableCell> cells) {
        Object value = firstCell.getColumn().getValue(firstCell.getRow());
        for (IJaretTableCell cell : cells) {
            // check whether destination cell is editable
            if (table.getTableModel().isEditable(cell.getRow(), cell.getColumn())) {
                try {
                    cell.getColumn().setValue(cell.getRow(), value);
                } catch (Exception e) {
                    // whatever happens -- ignore it
                }
            }
        }
    }

}
