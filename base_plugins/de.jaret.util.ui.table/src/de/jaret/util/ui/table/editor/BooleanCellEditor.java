/*
 *  File: BooleanCellEditor.java 
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
 * BooleanCellEditor is not a real editor. It toggles on double click, optional on click and on a typed SPACE.
 * 
 * @author Peter Kliem
 * @version $Id: BooleanCellEditor.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class BooleanCellEditor extends CellEditorBase implements ICellEditor {
    /** single clickk attribute: if true react on single clicks. */
    protected boolean _singleClick = false;

    /**
     * Default constructor.
     * 
     */
    public BooleanCellEditor() {
    }

    /**
     * Constructor including the singelClick property.
     * 
     * @param singleClick if true the editor will react on single clicks in the cell
     */
    public BooleanCellEditor(boolean singleClick) {
        _singleClick = singleClick;
    }

    /**
     * {@inheritDoc}
     */
    public Control getEditorControl(JaretTable table, IRow row, IColumn column, char typedKey) {
        if (typedKey == ' ') {
            toggle(row, column);
        } else if (typedKey == 0) {
            toggle(row, column);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void stopEditing(boolean storeInput) {
        // nothing to do
    }

    /** selection area width and height. */
    private static final int SELECTION_DELTA = 16;

    /**
     * {@inheritDoc}
     */
    public boolean handleClick(JaretTable table, IRow row, IColumn column, Rectangle drawingArea, int x, int y) {
        if (_singleClick) {
            Rectangle rect = new Rectangle(drawingArea.x + (drawingArea.width - SELECTION_DELTA) / 2, drawingArea.y
                    + (drawingArea.height - SELECTION_DELTA) / 2, SELECTION_DELTA, SELECTION_DELTA);
            if (rect.contains(x, y)) {
                toggle(row, column);
                return true;
            }
        }
        return false;
    }

    /**
     * Toggle the boolean value.
     * 
     * @param row row of the cell
     * @param column column of the cell
     */
    private void toggle(IRow row, IColumn column) {
        Object value = column.getValue(row);
        if (value instanceof Boolean) {
            column.setValue(row, ((Boolean) value).booleanValue() ? Boolean.FALSE : Boolean.TRUE);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        super.dispose();
    }

}
