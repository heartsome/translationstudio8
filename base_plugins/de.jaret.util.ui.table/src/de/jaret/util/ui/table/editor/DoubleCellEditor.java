/*
 *  File: DoubleCellEditor.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.editor;

import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.jaret.util.ui.DoubleField;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Cell Editor for editing double values.
 * <p>
 * Key bindings: CR, TAB: accept input and leave, ESC leave and reset to value when starting editing. Cursor up/down
 * will roll the value.
 * </p>
 * 
 * @author Peter Kliem
 * @version $Id: DoubleCellEditor.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class DoubleCellEditor extends CellEditorBase implements ICellEditor, FocusListener {
    /** text control wrapped by the doublefield. */
    protected Text _text;
    /** old value for restauration. */
    protected double _oldVal;
    /** doublefield managing the input. */
    protected DoubleField _doubleField;

    /**
     * Default constructor.
     */
    public DoubleCellEditor() {
    }

    /**
     * Convert the value retrieved from the model.
     * 
     * @param row row of the cell
     * @param column column of the cell
     * @return double value (defaulting to 0.0)
     */
    protected double convertValue(IRow row, IColumn column) {
        Object value = column.getValue(row);
        return value != null ? (Double) value : 0.0;
    }

    /**
     * Store the value in the model. If any error occurs, it will be silently ignored!
     * 
     * @param row row
     * @param column column
     */
    protected void storeValue(IRow row, IColumn column) {
        double value;
        try {
            value = _doubleField.getValue();
            _column.setValue(_row, value);
        } catch (ParseException e) {
            // ignore
        }
    }

    /**
     * Create and setup the control.
     * 
     * @param table parent for the control
     */
    private void createControl(JaretTable table) {
        if (_text == null) {
            _table = table;
            _text = new Text(table, SWT.BORDER | SWT.RIGHT);
            _doubleField = new DoubleField();
            _doubleField.setText(_text);

            _text.addKeyListener(new KeyListener() {

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

            _text.addFocusListener(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Control getEditorControl(JaretTable table, IRow row, IColumn column, char typedKey) {
        super.getEditorControl(table, row, column, typedKey);
        createControl(table);
        _oldVal = convertValue(row, column);
        if (typedKey != 0) {
            _text.setText("" + typedKey);
            _text.setSelection(1);
        } else {
            double value = convertValue(row, column);
            _doubleField.setValue(value);
            _text.selectAll();
        }
        return _text;
    }

    /**
     * {@inheritDoc}
     */
    public int getPreferredHeight() {
        if (_text == null) {
            return -1;
        }
        Point size = _text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        return size.y;
    }

    /**
     * {@inheritDoc} do nothing.
     */
    public void focusGained(FocusEvent arg0) {
    }

    /**
     * {@inheritDoc} On losing focus store the value.
     */
    public void focusLost(FocusEvent arg0) {
        _table.stopEditing(true);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        super.dispose();
        if (_text != null && !_text.isDisposed()) {
            _doubleField.setText(null);
            _text.removeFocusListener(this);
            _text.dispose();
        }

    }

    /**
     * {@inheritDoc}
     */
    public void stopEditing(boolean storeInput) {
        if (storeInput) {
            storeValue(_row, _column);
        }
        _text.setVisible(false);
    }

}
