/*
 *  File: IStyleStrategy.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Interface for a strategy that can be added to a cell style provider to determine styles on the fly based on the
 * content of the element (such as coloring the background of even rows).
 * 
 * @author kliem
 * @version $Id: IStyleStrategy.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public interface IStyleStrategy {
    /**
     * This method is called before a cell style is delivered to the jaret table (by getCellStyle(row, col) in the cell
     * style provider). It gets the cell style regulary determined by the provider and the default cell style. It can
     * then replace that style according to the strategy. The strategy should not alter the incoming style since this
     * alters all cells using that style.
     * 
     * @param row row
     * @param column column
     * @param incomingStyle the determined cell style
     * @param defaultCellStyle the defalt cell style used by the provider
     * @return cellstyle to be used by the table
     */
    ICellStyle getCellStyle(IRow row, IColumn column, ICellStyle incomingStyle, ICellStyle defaultCellStyle);
}
