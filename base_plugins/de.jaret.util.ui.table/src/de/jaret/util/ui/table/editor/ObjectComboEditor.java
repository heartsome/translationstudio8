/*
 *  File: ObjectComboEditor.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.editor;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Editor using a ComboBox for selecting one of several objects supplied to the editor at creation time. A label
 * provider is used for toString conversion.
 * 
 * @author Peter Kliem
 * @version $Id: ObjectComboEditor.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class ObjectComboEditor extends CellEditorBase implements ICellEditor, FocusListener {
    /** combox widget. */
    protected Combo _combo;
    /** old value. */
    protected Object _oldVal;
    /** list of items displayed. */
    protected String[] _stringItems;
    /** label provider used. */
    protected ILabelProvider _labelProvider;
    /** if true allow null as a possible selection. */
    protected boolean _allowNull = true;
    /** the text displayed for <code>null</code>. */
    protected String _nullText = "";
    /** object list for selection. */
    protected List<? extends Object> _itemList;

    /**
     * Construct a new ObjectComboEditor with a list of selectabel Objects and an ILabelprovider.
     * 
     * @param list list of Objects that may be selected.
     * @param labelProvider label provider to be used or <code>null</code>. In the latter case a simple toString
     * label provider will be used.
     * @param allowNull if true null will always be a possible value in the comboBox
     * @param nullText string to be displyed for the null value if allowed
     */
    public ObjectComboEditor(List<? extends Object> list, ILabelProvider labelProvider, boolean allowNull,
            String nullText) {
        _labelProvider = labelProvider;
        if (_labelProvider == null) {
            _labelProvider = new ToStringLabelProvider();
        }
        _allowNull = allowNull;
        _nullText = nullText;
        _itemList = list;

        if (_itemList != null) {
            initItems();
        }

    }

    protected void initItems() {
        _stringItems = _allowNull ? new String[_itemList.size() + 1] : new String[_itemList.size()];

        int i = 0;
        if (_allowNull) {
            _stringItems[0] = _nullText;
            i = 1;
        }

        for (Object o : _itemList) {
            _stringItems[i++] = _labelProvider.getText(o);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Control getEditorControl(JaretTable table, IRow row, IColumn column, char typedKey) {
        super.getEditorControl(table, row, column, typedKey);

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
            _combo.setItems(_stringItems);
        }

        Object value = column.getValue(row);
        _oldVal = value;

        int selIdx = -1;
        if (_allowNull && value == null) {
            selIdx = 0;
        } else {
            selIdx = _itemList.indexOf(value);
            selIdx = _allowNull ? selIdx + 1 : selIdx;
        }

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
                if (_allowNull && selIdx == 0) {
                    selection = null;
                } else {
                    selIdx = _allowNull ? selIdx - 1 : selIdx;
                    selection = _itemList.get(selIdx);
                }
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
     * {@inheritDoc} Nothing to do on gaining focus.
     */
    public void focusGained(FocusEvent arg0) {
    }

    /**
     * {@inheritDoc} Store and end editing when focus is taken away.
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

    /**
     * Simple Labelprovider just using the toString method of any supplied object.
     * 
     * @author Peter Kliem
     * @version $Id: ObjectComboEditor.java,v 1.1 2012-05-07 01:34:38 jason Exp $
     */
    public class ToStringLabelProvider implements ILabelProvider {

        /**
         * {@inheritDoc}
         */
        public Image getImage(Object element) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public String getText(Object element) {
            return element.toString();
        }

        /**
         * {@inheritDoc}
         */
        public void addListener(ILabelProviderListener listener) {
        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {
        }

        /**
         * {@inheritDoc}
         */
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public void removeListener(ILabelProviderListener listener) {
        }

    }

}
