/*
 *  File: IntegerCellEditor.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Cell Editor for editing integer values using a spinner widget. Well it seems that the Spinner does not support
 * negative values ...
 * 
 * Key bindings: CR, TAB: accept input and leave, ESC leave and reset to value when starting editing
 * </p>
 * 
 * @author Peter Kliem
 * @version $Id: IntegerCellEditor.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class IntegerCellEditor extends CellEditorBase implements ICellEditor, FocusListener {
    /** spinner widgrt. */
    protected Spinner _spinner;

    /** old value. */
    private int _oldVal;

    /** min value that can be selected. */
    private int _min = Integer.MIN_VALUE;
    /** max value that can be selected. */
    private int _max = Integer.MAX_VALUE;

    /**
     * Construct an integer cell renderer with given min and max values.
     * 
     * @param min minimal value
     * @param max maximum value
     */
    public IntegerCellEditor(int min, int max) {
        _min = min;
        _max = max;
    }

    /**
     * Default construcor.
     * 
     */
    public IntegerCellEditor() {
    }

    protected int convertValue(IRow row, IColumn column) {
        Object value = column.getValue(row);
        return value != null ? (Integer) value : 0;
    }

    protected void storeValue(IRow row, IColumn column) {
        Integer value = _spinner.getSelection();
        _column.setValue(_row, value);
    }

    /**
     * Create the control.
     * 
     * @param table parent table
     */
    private void createControl(JaretTable table) {
        if (_spinner == null) {
            _table = table;
            _spinner = new Spinner(table, SWT.BORDER);

            _spinner.setMaximum(_max);
            _spinner.setMinimum(_min);

            _spinner.addTraverseListener(new TraverseListener() {
                public void keyTraversed(TraverseEvent e) {
                    e.doit = false;
                }
            });

            _spinner.addKeyListener(new KeyListener() {
                public void keyPressed(KeyEvent event) {
                    if (event.keyCode == SWT.TAB) {
                        event.doit = false;
                        stopEditing(true);
                        _table.forceFocus();
                        _table.focusRight();
                    } else if (event.keyCode == SWT.CR) {
                        event.doit = false;
                        stopEditing(true);
                        _table.forceFocus();
                        _table.focusDown();
                    } else if (event.keyCode == SWT.ESC) {
                        event.doit = false;
                        stopEditing(false);
                        _column.setValue(_row, _oldVal);
                        _table.forceFocus();
                    }

                }

                public void keyReleased(KeyEvent arg0) {
                }

            });

            _spinner.addFocusListener(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Control getEditorControl(JaretTable table, IRow row, IColumn column, char typedKey) {
        super.getEditorControl(table, row, column, typedKey);
        createControl(table);
        _oldVal = (Integer) column.getValue(row);
        if (false && typedKey != 0) {
            // _spinner.setsetText("" + typedKey);
            // _text.setSelection(1);
        } else {
            int value = convertValue(row, column);
            _spinner.setSelection(value);
        }
        return _spinner;
    }

    /**
     * {@inheritDoc}
     */
    public int getPreferredHeight() {
        if (_spinner == null) {
            return -1;
        }
        Point size = _spinner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        return size.y;
    }

    /**
     * {@inheritDoc} Do nothing on gaining focus.
     */
    public void focusGained(FocusEvent arg0) {
    }

    /**
     * {@inheritDoc} Stop and strore when focus leaves.
     */
    public void focusLost(FocusEvent arg0) {
        _table.stopEditing(true);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        super.dispose();
        if (_spinner != null && !_spinner.isDisposed()) {
            _spinner.removeFocusListener(this);
            _spinner.dispose();
        }

    }

    /**
     * {@inheritDoc}
     */
    public void stopEditing(boolean storeInput) {
        if (storeInput) {
            storeValue(_row, _column);
        }
        _spinner.setVisible(false);
    }

}
