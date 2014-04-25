/*
 *  File: MultiLineListExample.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.examples.table.mllist;

import java.util.Date;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.jaret.examples.table.DummyRow;
import de.jaret.util.ui.ResourceImageDescriptor;
import de.jaret.util.ui.console.ConsoleControl;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.DefaultJaretTableModel;
import de.jaret.util.ui.table.model.IJaretTableModel;
import de.jaret.util.ui.table.model.ITableViewState;
import de.jaret.util.ui.table.model.PropCol;
import de.jaret.util.ui.table.model.PropListeningTableModel;
import de.jaret.util.ui.table.renderer.DefaultBorderConfiguration;
import de.jaret.util.ui.table.renderer.DefaultCellStyle;

/**
 * Simple exmaple for demonstrating the use of the jaret table.
 * 
 * @author Peter Kliem
 * @version $Id: MultiLineListExample.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class MultiLineListExample {

    Shell _shell;
    ConsoleControl _consoleControl;
    IJaretTableModel _tableModel;

    public MultiLineListExample(IJaretTableModel tableModel) {
        _tableModel = tableModel;
        _shell = new Shell(Display.getCurrent());
        _shell.setText("jaret table multilinelist");
        createControls();
        _shell.open();
        Display display;
        display = _shell.getDisplay();
        _shell.pack();
        _shell.setSize(280, 400);

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

        _jt = new JaretTable(_shell, SWT.V_SCROLL);
        _jt.setLayoutData(gd);

        if (_tableModel == null) {
            DefaultJaretTableModel model = new PropListeningTableModel();

            model.addRow(new DummyRow("r1", "line 1", "line 2 adds more text", true, new Date(), MultiLineListExample
                    .getImageRegistry().get("icon")));
            model.addRow(new DummyRow("r2", "another first line", "line 2 adds more text", true, new Date(),
                    MultiLineListExample.getImageRegistry().get("icon")));
            model.addRow(new DummyRow("r3", "and yet another one", "line 2 adds more text", true, new Date(),
                    MultiLineListExample.getImageRegistry().get("icon")));
            model.addRow(new DummyRow("r5", "4444444444", "line 2 adds more text", true, new Date(),
                    MultiLineListExample.getImageRegistry().get("icon")));
            model.addRow(new DummyRow("r6", "555555555", "line 2 adds more text", true, new Date(),
                    MultiLineListExample.getImageRegistry().get("icon")));
            model.addRow(new DummyRow("r7", "6666666666", "line 2 adds more text", true, new Date(),
                    MultiLineListExample.getImageRegistry().get("icon")));
            model.addRow(new DummyRow("r8", "7777777777", "line 2 adds more text", true, new Date(),
                    MultiLineListExample.getImageRegistry().get("icon")));
            model.addRow(new DummyRow("r9", "88888888888", "line 2 adds more text", true, new Date(),
                    MultiLineListExample.getImageRegistry().get("icon")));

            PropCol ct1 = new PropCol("t1", "column 1", "T1");
            ct1.setEditable(false);
            model.addColumn(ct1);

            _tableModel = model;
        }

        DefaultCellStyle cs = (DefaultCellStyle) _jt.getTableViewState().getCellStyleProvider().getDefaultCellStyle()
                .copy();
        cs.setBorderConfiguration(new DefaultBorderConfiguration(0, 0, 0, 0));
        _jt.getTableViewState().getCellStyleProvider().setColumnCellStyle(_tableModel.getColumn(0), cs);
        _jt.getTableViewState().setRowHeightMode(ITableViewState.RowHeightMode.FIXED);
        // has to be replaced
        for (int i = 0; i < _tableModel.getRowCount(); i++) {
            _jt.getTableViewState().setRowHeight(_tableModel.getRow(i), 60);
        }
        // _jt.getTableViewState().setColumnResizeMode(ITableViewState.ColumnResizeMode.ALL);

        _jt.setHeaderHeight(0);
        _jt.registerCellRenderer(_tableModel.getColumn(0), new MultilineListCellRenderer());

        _jt.setTableModel(_tableModel);
        _jt.getTableViewState().setColumnWidth(_tableModel.getColumn(0), 230);

    }

    static ImageRegistry _imageRegistry;

    public static ImageRegistry getImageRegistry() {
        if (_imageRegistry == null) {
            _imageRegistry = new ImageRegistry();
            ImageDescriptor imgDesc = new ResourceImageDescriptor("/de/jaret/examples/table/mllist/icon.gif");
            _imageRegistry.put("icon", imgDesc);
            imgDesc = new ResourceImageDescriptor("/de/jaret/examples/table/keyboard.png");
            _imageRegistry.put("keyboard", imgDesc);
        }
        return _imageRegistry;
    }

    public static void main(String args[]) {
        MultiLineListExample te = new MultiLineListExample(null);
    }

}
