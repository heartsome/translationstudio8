/*
 *  File: DateCellEditor.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.editor;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.date.JaretDate;
import de.jaret.util.ui.datechooser.DateChooser;
import de.jaret.util.ui.datechooser.IDateChooserListener;
import de.jaret.util.ui.datechooser.IFieldIdentifier;
import de.jaret.util.ui.datechooser.SimpleFieldIdentifier;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Cell editor for editing dates using the jaret datechooser. Supports java.util.date and JaretDate. The fieldidentifier
 * used for the datechooser (see Javadoc there) is not locale dependant (Day/Month/year) have to be changed when used in
 * another country (or removed!).
 * <p>
 * Key bindings: TAB and CR will leave the datechooser (positive). ESC will leave the chooser resetting the date to the
 * value present when editing started.
 * </p>
 * 
 * @author Peter Kliem
 * @version $Id: DateCellEditor.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class DateCellEditor extends CellEditorBase implements ICellEditor, IDateChooserListener, FocusListener {
    /** chooser component. */
    protected DateChooser _chooser;

    /** old java.util.Date val if present. */
    protected Date _oldVal;
    /** old JaretDate value if present. */
    protected JaretDate _oldJaretDateVal;

    /** true if jaretdate is used. */
    private boolean _jaretDate;

    /**
     * Create the chooser control.
     * 
     * @param table parent table.
     */
    private void createControl(JaretTable table) {
        _table = table;
        if (_chooser == null) {
            _chooser = new DateChooser(table, SWT.NULL);
            // TODO locale dependent
            IFieldIdentifier fi = new SimpleFieldIdentifier(".", new int[] {Calendar.DAY_OF_MONTH, Calendar.MONTH,
                    Calendar.YEAR});
            _chooser.setFieldIdentifier(fi);
            _chooser.setSelectAllOnFocusGained(false);
            _chooser.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            _chooser.addFocusListener(this);
            _chooser.addDateChooserListener(this);

            _chooser.getTextField().addKeyListener(new KeyListener() {
                public void keyPressed(KeyEvent event) {
                    if (event.keyCode == SWT.TAB) {
                        _chooser.validateInput();
                        stopEditing(true);
                        event.doit = false; // do not further process
                        _table.forceFocus();
                        _table.focusRight();
                    } else if (event.keyCode == SWT.CR) {
                        _chooser.validateInput();
                        stopEditing(true);
                        event.doit = false; // do not further process
                        _table.forceFocus();
                        _table.focusDown();
                    } else if (event.keyCode == SWT.ESC) {
                        stopEditing(false);
                        restoreOldVal();
                        event.doit = false; // do not further process
                        _table.forceFocus();
                    }
                }

                public void keyReleased(KeyEvent e) {
                }
            });

            // add a traverse listener so the TAB-key won't traverse the focus out of the table
            _chooser.getTextField().addTraverseListener(new TraverseListener() {
                public void keyTraversed(TraverseEvent e) {
                    e.doit = false;
                }

            });
        }
    }

    /**
     * {@inheritDoc}
     */
    public Control getEditorControl(JaretTable table, IRow row, IColumn column, char typedKey) {
        super.getEditorControl(table, row, column, typedKey);
        createControl(table);
        if (column.getValue(row) instanceof Date) {
            _oldVal = (Date) column.getValue(row);
            _jaretDate = false;
        } else if (column.getValue(row) instanceof JaretDate) {
            _oldVal = ((JaretDate) column.getValue(row)).getDate();
            _oldJaretDateVal = (JaretDate) column.getValue(row);
            _jaretDate = true;
        }
        if (typedKey != 0) {
            _chooser.setText("" + typedKey);
            _chooser.setSelection(1);
        } else {
            _chooser.setDate(_oldVal);
        }
        _row = row;
        _column = column;

        return _chooser;
    }

    /**
     * Restore date from the the beginning of the edit action.
     * 
     */
    private void restoreOldVal() {
        if (!_jaretDate) {
            _column.setValue(_row, _oldVal);
        } else {
            _column.setValue(_row, _oldJaretDateVal);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stopEditing(boolean storeInput) {
        if (storeInput) {
            storeValue();
        }
        _chooser.setDropped(false);
        _chooser.setVisible(false);
    }

    /**
     * Store the value in the model.
     * 
     */
    private void storeValue() {
        if (!_jaretDate) {
            _column.setValue(_row, _chooser.getDate());
        } else {
            _column.setValue(_row, new JaretDate(_chooser.getDate()));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        if (_chooser != null && !_chooser.isDisposed()) {
            _chooser.removeFocusListener(this);
            _chooser.dispose();
        }
        // help the garbage collector
        _table = null;
        _column = null;
        _row = null;
    }

    /**
     * {@inheritDoc} If the users choses a date, stop editing an store the chosen date.
     */
    public void dateChosen(Date date) {
        // if a date has been chosen in the datechooser stop editing immediately
        stopEditing(true);
        _table.forceFocus();
    }

    /**
     * {@inheritDoc} No Action on intermediate changes in the chooser.
     */
    public void dateIntermediateChange(Date date) {
    }

    /**
     * {@inheritDoc} When the chooser tells us the user canceled the editing, restore the old date.
     */
    public void choosingCanceled() {
        _chooser.setDate(_oldVal);
    }

    /**
     * {@inheritDoc} nothing to do.
     */
    public void inputInvalid() {
    }
    
    /**
     * {@inheritDoc} Do nothing on focus gained.
     */
    public void focusGained(FocusEvent e) {
    }

    /**
     * {@inheritDoc} When loosing focus, stop the editing and store the value.
     */
    public void focusLost(FocusEvent e) {
        _table.stopEditing(true);
    }

}
