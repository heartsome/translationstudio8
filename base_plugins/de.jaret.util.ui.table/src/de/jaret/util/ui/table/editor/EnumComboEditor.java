/*
 *  File: EnumComboEditor.java 
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Editor for a field with an enum as type. Naturally uses a combobox.
 * 
 * @author Peter Kliem
 * @version $Id: EnumComboEditor.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class EnumComboEditor extends CellEditorBase implements ICellEditor, FocusListener {
    /** combobox widget. */
    protected Combo _combo;
    /** old value. */
    protected Object _oldVal;
    /** list of selectable items in the combobox. */
    protected Object[] _items;

    /**
     * {@inheritDoc}
     */
    public Control getEditorControl(JaretTable table, IRow row, IColumn column, char typedKey) {
        super.getEditorControl(table, row, column, typedKey);

        _items = new Object[] {};

        if (_combo == null) {
            _combo = new Combo(table, SWT.BORDER | SWT.READ_ONLY);
            _combo.addKeyListener(new KeyListener() {

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
            _combo.addFocusListener(this);

        }
        Class<?> clazz = column.getContentClass(row);

        if (clazz != null && Enum.class.isAssignableFrom(clazz)) {
            _items = clazz.getEnumConstants();
        } else {
            _items = new Object[] {};
        }

        Object value = column.getValue(row);
        _oldVal = value;

        int selIdx = -1;
        String[] stringItems = new String[_items.length];
        for (int i = 0; i < _items.length; i++) {
            stringItems[i] = _items[i].toString();
            if (value != null && value.equals(_items[i])) {
                selIdx = i;
            }
        }
        _combo.setItems(stringItems);

        if (selIdx != -1) {
            _combo.select(selIdx);
        }

        return _combo;
    }

    /**
     * {@inheritDoc}
     */
    public void stopEditing(boolean storeInput) {
        if (storeInput) {
            int selIdx = _combo.getSelectionIndex();
            Object selection = null;
            if (selIdx != -1) {
                selection = _items[selIdx];
            }
            _column.setValue(_row, selection);
        }
        _combo.setVisible(false);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        super.dispose();
        if (_combo != null && !_combo.isDisposed()) {
            _combo.dispose();
        }
    }

    /**
     * {@inheritDoc} Do nothing on gaining focus.
     */
    public void focusGained(FocusEvent arg0) {
    }

    /**
     * {@inheritDoc} Stop editing and store the value.
     */
    public void focusLost(FocusEvent arg0) {
        stopEditing(true);
    }

    /**
     * {@inheritDoc}
     */
    public int getPreferredHeight() {
        if (_combo != null) {
            Point size = _combo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            return size.y;
        }
        return -1;
    }
}
