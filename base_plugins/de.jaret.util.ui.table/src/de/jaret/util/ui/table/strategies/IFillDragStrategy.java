/*
 *  File: IFillDragStrategy.java 
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
 * Interface describing a stragey used when cells should be filled after a fill drag.
 * 
 * @author Peter Kliem
 * @version $Id: IFillDragStrategy.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IFillDragStrategy {
    /**
     * Do a fill operation fro the first cell to the other cells.
     * 
     * @param table table requesting the operation
     * @param firstCell originating cell
     * @param cells cells to be filled
     */
    void doFill(JaretTable table, IJaretTableCell firstCell, List<IJaretTableCell> cells);
}
