/*
 *  File: IColumnListener.java 
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
 * Interface for listening on value changes on a specific cell in a column.
 * 
 * @author Peter Kliem
 * @version $Id: IColumnListener.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IColumnListener {
    /**
     * Called when a value in a column changed.
     * 
     * @param row the row
     * @param column the column
     * @param oldValue the old value
     * @param newValue the new value
     */
    void valueChanged(IRow row, IColumn column, Object oldValue, Object newValue);
}
