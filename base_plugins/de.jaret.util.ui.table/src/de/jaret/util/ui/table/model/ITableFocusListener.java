/*
 *  File: ITableFocusListener.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import de.jaret.util.ui.table.JaretTable;

/**
 * Interface for listeners listening to the focus move on a jaret table.
 * 
 * @author Peter Kliem
 * @version $Id: ITableFocusListener.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface ITableFocusListener {
    /**
     * Focus has been moved or has been initially set.
     * 
     * @param source JaretTable that is the source for the event.
     * @param row new focussed row.
     * @param column new focussed column.
     */
    void tableFocusChanged(JaretTable source, IRow row, IColumn column);
}
