/*
 *  File: ICellStyleListener.java 
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
 * Interface for a listener listening on style changes.
 * 
 * @author Peter Kliem
 * @version $Id: ICellStyleListener.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public interface ICellStyleListener {
    /**
     * Will be called whenever a style changed.
     * 
     * @param row row
     * @param column column
     * @param style changed style
     */
    void cellStyleChanged(IRow row, IColumn column, ICellStyle style);
}
