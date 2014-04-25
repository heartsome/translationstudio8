/*
 *  File: ICellEditor.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.editor;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Interface for a cell editor to be used in the jaret table.
 * 
 * @author Peter Kliem
 * @version $Id: ICellEditor.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public interface ICellEditor {
    /**
     * Provide the Control for editing the value at row/column. <b>Important:</b> make shure _not_ to create a new
     * control with every call!
     * <p>
     * This method may return <code>null</code> indicating that the editor will not supply a control.
     * </p>
     * 
     * @param table the table requesting the editor
     * @param row row
     * @param column column
     * @param typedKey the character typed when invoking the editor (may be 0 if the editor was invoked without typing
     * any key)
     * @return configured Control (parent has to be the table)
     */
    Control getEditorControl(JaretTable table, IRow row, IColumn column, char typedKey);

    /**
     * End editing.
     * 
     * @param storeInput if true the editor shall save the current input.
     */
    void stopEditing(boolean storeInput);

    /**
     * Handle a click on the cell. This could handle the whole edit for single click editors. The return value controls
     * whether the click will be used for regular selection after handling.
     * 
     * @param table the jaret table calling
     * @param row row
     * @param column column
     * @param drawingArea the rectangle of the cell
     * @param x clicked coordinate x
     * @param y clicked coordinate y
     * @return true if the click has been handled
     */
    boolean handleClick(JaretTable table, IRow row, IColumn column, Rectangle drawingArea, int x, int y);

    /**
     * Dispose whatever resouces have been allocated.
     * 
     */
    void dispose();

    /**
     * If the renderer *wishes* to be sized not the height of the cell, this method may be used to announce the
     * preferred height of the control. A value of -1 signals no preference.
     * 
     * @return preferred height or -1 for no preference.
     */
    int getPreferredHeight();
}
