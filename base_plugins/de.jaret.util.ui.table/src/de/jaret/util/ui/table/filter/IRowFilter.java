/*
 *  File: IRowFilter.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.filter;

import de.jaret.util.misc.PropertyObservable;
import de.jaret.util.ui.table.model.IRow;

/**
 * A simple row filter for the jaret table. This is a PropertyObservable to allow the table to refresh on prop changes.
 * 
 * @author Peter Kliem
 * @version $Id: IRowFilter.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public interface IRowFilter extends PropertyObservable {
    /**
     * Check whether the row is in the resulting list of rows.
     * 
     * @param row row to check.
     * @return true if the rw is in the result.
     */
    boolean isInResult(IRow row);
}
