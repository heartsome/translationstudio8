/*
 *  File: IHierarchyRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import org.eclipse.swt.graphics.Rectangle;

import de.jaret.util.ui.table.model.IRow;

/**
 * Interface specifying extensions to the ICellRenderer interface necessary for hierarchy handling.
 * 
 * @author Peter Kliem
 * @version $Id: IHierarchyRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public interface IHierarchyRenderer extends ICellRenderer {
    /**
     * Should return true if a click on the coordinates x,y should toggle expanded state.
     * 
     * @param row row
     * @param drawingarea drawing area of the hierarchy section of the row
     * @param x x coordinate to check
     * @param y y coordinate to check
     * @return true if the click is in the acive area
     */
    boolean isInActiveArea(IRow row, Rectangle drawingarea, int x, int y);
}
