/*
 *  File: TextCellEditor.java 
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Cell Editor for editing strings. Supports single and multiple line edits. For multiple line usage there are several
 * options:
 * <ul>
 * <li>resize: when true the input will be growing with the input text up to maxRows rows</li>
 * <li>maxRows: max height of input filed when resizing</li>
 * </ul>
 * <p>
 * Key bindings: CR, TAB: accept input and leave, ALT+CR insert CR, ESC leave and reset to value when starting editing
 * </p>
 * 
 * @author Peter Kliem
 * @version $Id: TextCellEditor.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class TextCellEditor extends CellEditorBase implements ICellEditor, FocusListener {
    protected boolean _multi = true;

    /** control used for editing. */
    protected Text _text;

    private String _oldVal;

    private int _maxrows = 6;

    public TextCellEditor(boolean multi) {
        _multi = multi;
    }

    protected String convertValue(IRow row, IColumn column) {
        Object value = column.getValue(row);
        return value != null ? value.toString() : null;
    }

    protected void storeValue(IRow row, IColumn column) {
        String value = _text.getText();
        _column.setValue(_row, value);
    }

    /**
     * Create the control to be used when editing.
     * 
     * @param table table is the parent control
     */
    private void createControl(JaretTable table) {
        if (_text == null) {
            _table = table;
            if (!_multi) {
                _text = new Text(table, SWT.BORDER);
            } else {
                _text = new Text(table, SWT.BORDER | SWT.MULTI | SWT.WRAP);
            }
            _text.addKeyListener(new KeyListener() {

                public void keyPressed(KeyEvent event) {
                    if ((event.stateMask & SWT.ALT) != 0 && event.keyCode == SWT.CR) {
                        event.doit = false;
                        _text.insert("\n");
                    } else if (event.keyCode == SWT.TAB) {
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
                    } else {
                        if (_multi) {
                            // System.out.println("lines "+_text.getLineCount());
                            // System.out.println("lineheight "+_text.getLineHeight());
                            // int lheight = _text.getLineHeight();
                            // int lcount = _text.getLineCount();
                            // TODO
                            if (true || _text.getLineCount() * _text.getLineHeight() < _text.getSize().y) {
                                Point newSize = new Point(_text.getSize().x, getPreferredHeight());
                                _text.setSize(newSize);
                            }
                        }
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
        _oldVal = (String) column.getValue(row);
        if (typedKey != 0) {
            _text.setText("" + typedKey);
            _text.setSelection(1);
        } else {
            String value = convertValue(row, column);
            _text.setText(value != null ? value : "");
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
        int lheight = _text.getLineHeight();
        int lcount = _text.getLineCount();
        if (lcount > _maxrows + 1) {
            lcount = _maxrows;
        }
        return (lcount + 1) * lheight;

    }

    public void focusGained(FocusEvent arg0) {
    }

    public void focusLost(FocusEvent arg0) {
        _table.stopEditing(true);
    }

    public void dispose() {
        super.dispose();
        if (_text != null && !_text.isDisposed()) {
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

    /**
     * @return the maxrows
     */
    public int getMaxrows() {
        return _maxrows;
    }

    /**
     * @param maxrows the maxrows to set
     */
    public void setMaxrows(int maxrows) {
        _maxrows = maxrows;
    }

}
