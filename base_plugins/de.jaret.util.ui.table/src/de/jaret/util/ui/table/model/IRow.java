/*
 *  File: IRow.java 
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
 * Row interface for rows used with jaret table models. The unique id is <b>only</b> used for persisting view state
 * information (a feature most users do appreciate).
 * 
 * @author Peter Kliem
 * @version $Id: IRow.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IRow {
    /**
     * Used for storing the row height (identification purposes).
     * 
     * @return a unique id
     */
    String getId();
}
