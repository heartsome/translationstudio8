/*
 *  File: SimpleModelExample.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.examples.table;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IJaretTableModel;
import de.jaret.util.ui.table.model.ITableViewState;
import de.jaret.util.ui.table.model.simple.SimpleJaretTableModel;
import de.jaret.util.ui.table.util.action.JaretTableActionFactory;

/**
 * Simple exmaple for demonstrating the use of the jaret table.
 * 
 * @author Peter Kliem
 * @version $Id: SimpleModelExample.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class SimpleModelExample {
    // private static final int NUMCOLS = 5;
    // private static final int NUMROWS = 5;
    private static final int NUMCOLS = 100;
    private static final int NUMROWS = 200;
    Shell _shell;
    IJaretTableModel _tableModel;

    public SimpleModelExample(IJaretTableModel tableModel) {
        _tableModel = tableModel;
        _shell = new Shell(Display.getCurrent());
        _shell.setText("simple jaret table example");
        createControls();
        _shell.open();
        Display display;
        display = _shell.getDisplay();
        _shell.pack();
        _shell.setSize(1000, 700);

        /*
         * do the event loop until the shell is closed to block the call
         */
        while (_shell != null && !_shell.isDisposed()) {
            try {
                if (!display.readAndDispatch())
                    display.sleep();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        display.update();
    }

    JaretTable _jt;

    /**
     * Create the controls that compose the console test.
     * 
     */
    protected void createControls() {
        GridLayout gl = new GridLayout();
        gl.numColumns = 1;
        _shell.setLayout(gl);

        GridData gd = new GridData(GridData.FILL_BOTH);

        _jt = new JaretTable(_shell, SWT.V_SCROLL | SWT.H_SCROLL);
        _jt.setLayoutData(gd);

        if (_tableModel == null) {
            SimpleJaretTableModel model = new SimpleJaretTableModel();

            for (int x = 0; x <= NUMCOLS; x++) {
                model.setHeaderLabel(x, "" + x);
                for (int y = 0; y <= NUMROWS; y++) {
                    model.setValueAt(x, y, x + "/" + y);
                }
            }

            _tableModel = model;
        }

        _jt.setTableModel(_tableModel);

        // set rowheight mode to variable .. optimal would be quite expensive on each col resize
        _jt.getTableViewState().setRowHeightMode(ITableViewState.RowHeightMode.VARIABLE);

        for (int i = 0; i < NUMCOLS; i++) {
            IColumn col = _tableModel.getColumn(i);
            _jt.getTableViewState().setColumnWidth(col, 40);
        }

        JaretTableActionFactory af = new JaretTableActionFactory();

        MenuManager mm = new MenuManager();
        mm.add(af.createStdAction(_jt, JaretTableActionFactory.ACTION_CONFIGURECOLUMNS));
        _jt.setHeaderContextMenu(mm.createContextMenu(_jt));

        MenuManager rm = new MenuManager();
        rm.add(af.createStdAction(_jt, JaretTableActionFactory.ACTION_OPTROWHEIGHT));
        rm.add(af.createStdAction(_jt, JaretTableActionFactory.ACTION_OPTALLROWHEIGHTS));
        _jt.setRowContextMenu(rm.createContextMenu(_jt));

        TableControlPanel ctrlPanel = new TableControlPanel(_shell, SWT.NULL, _jt);

    }

    public static void main(String args[]) {
        SimpleModelExample te = new SimpleModelExample(null);
    }

}
