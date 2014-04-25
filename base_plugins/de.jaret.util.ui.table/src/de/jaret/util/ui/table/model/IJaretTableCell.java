/*
 *  File: IJaretTableCell.java 
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
 * Interface describing the location (in the data model) of a single cell.
 * 
 * @author Peter Kliem
 * @version $Id: IJaretTableCell.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IJaretTableCell {
    /**
     * Retrieve the row.
     * 
     * @return the row of the cell
     */
    IRow getRow();

    /**
     * Retrieve the column.
     * 
     * @return the column of the cell
     */
    IColumn getColumn();
}
