/*
 *  File: TableExample.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.examples.table;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.jaret.examples.table.renderer.RiskCellEditor;
import de.jaret.examples.table.renderer.RiskRenderer;
import de.jaret.util.ui.ResourceImageDescriptor;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.editor.IntegerCellEditor;
import de.jaret.util.ui.table.editor.ObjectComboEditor;
import de.jaret.util.ui.table.model.DefaultJaretTableModel;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IJaretTableModel;
import de.jaret.util.ui.table.model.ITableViewState;
import de.jaret.util.ui.table.model.PropCol;
import de.jaret.util.ui.table.model.PropListeningTableModel;
import de.jaret.util.ui.table.renderer.BarCellRenderer;
import de.jaret.util.ui.table.renderer.DefaultCellStyle;
import de.jaret.util.ui.table.renderer.ObjectImageRenderer;
import de.jaret.util.ui.table.renderer.SmileyCellRenderer;
import de.jaret.util.ui.table.util.action.JaretTableActionFactory;

/**
 * Simple example for demonstrating the use of the jaret table.
 * 
 * @author Peter Kliem
 * @version $Id: TableExample.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class TableExample {

    Shell _shell;
    IJaretTableModel _tableModel;

    public TableExample(IJaretTableModel tableModel) {
        _tableModel = tableModel;
        _shell = new Shell(Display.getCurrent());
        _shell.setText("jaret table example");
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
            DefaultJaretTableModel model = new PropListeningTableModel();

            model.addRow(new DummyRow("r1", "The quick brown fox jumps over the crazy dog.", "Mars", true, new Date(),
                    TableExample.getImageRegistry().get("splash")));
            model.addRow(new DummyRow("r2", "Dogma i am god", "Venus", true, new Date()));
            model
                    .addRow(new DummyRow(
                            "r3",
                            "Wenn der kleine Blindtext einmal gro� und wichtig geworden ist, wird er bedeutungsschwanger die Welt erorbern.",
                            "Jupiter", true, new Date(), TableExample.getImageRegistry().get("keyboard")));
            model.addRow(new DummyRow("r4", "wewe we wew e we we we w ewe", "Uranus", true, new Date()));
            model.addRow(new DummyRow("r5", "sdjg sd jhgd dsh hjsgfjhgdf", "Pluto", true, new Date()));
            model.addRow(new DummyRow("r6", "wewe we wew e we we we w ewe", "Earth", true, new Date()));
            model.addRow(new DummyRow("r7", "wewe we wew e we we we w ewe", "Mars", false, new Date()));
            model.addRow(new DummyRow("r8", "wewe we wew e we we we w ewe", "Sun", false, new Date()));
            model.addRow(new DummyRow("r9", "wewe we wew e we we we w ewe", "Earth", true, new Date()));
            model.addRow(new DummyRow("ra", "wewe we wew e we we we w ewe", "Saturn", true, new Date()));
            model.addRow(new DummyRow("rb", "wewe we wew e we we we w ewe", "Saturn", true, new Date()));
            model.addRow(new DummyRow("rc", "wewe we wew e we we we w ewe", "Pluto", true, new Date()));
            model.addRow(new DummyRow("rd", "wewe we wew e we we we w ewe", "Jupiter", true, new Date()));
            model.addRow(new DummyRow("re", "This is the last row in the sort order of the model!", "Mars", true,
                    new Date()));

            IColumn ct1 = new PropCol("t1", "column 1", "T1");
            model.addColumn(ct1);
            model.addColumn(new PropCol("d1", "Date", "D1"));
            model.addColumn(new PropCol("t2", "column 2", "T2"));
            model.addColumn(new PropCol("t3", "column 3", "T3"));
            model.addColumn(new PropCol("b1", "column 4", "B1"));
            model.addColumn(new PropCol("i1", "column 5", "Img"));
            model.addColumn(new PropCol("integer", "column 6", "Integer", Integer.class));
            model.addColumn(new PropCol("integer2", "Integer", "Integer", Integer.class));
            model.addColumn(new PropCol("integer3", "Smiley", "Integer", Integer.class));
            model.addColumn(new PropCol("Risk", "Risk", "Risk"));
            model.addColumn(new PropCol("RiskProb", "RProb", "RiskProb"));
            model.addColumn(new PropCol("RiskSeverity", "RSeverity", "RiskSeverity"));
            model.addColumn(new PropCol("Enum", "EnumTest", "EnumProperty"));
            model.addColumn(new PropCol("double", "Double", "Adouble"));
            model.addColumn(new PropCol("x1", "ComboEdit", "X1"));
            model.addColumn(new PropCol("Enum2", "EnumImage", "EnumProperty"));

            _tableModel = model;
        }

        DefaultCellStyle cs = (DefaultCellStyle) _jt.getTableViewState().getCellStyleProvider().getDefaultCellStyle()
                .copy();
        cs.setHorizontalAlignment(ITableViewState.HAlignment.RIGHT);
        _jt.getTableViewState().getCellStyleProvider().setColumnCellStyle(_tableModel.getColumn(0), cs);
        _jt.getTableViewState().getCellStyleProvider().setColumnCellStyle(_tableModel.getColumn(7), cs);
        _jt.getTableViewState().getCellStyleProvider().setColumnCellStyle(_tableModel.getColumn(10), cs);

        _jt.registerCellRenderer(_tableModel.getColumn(2), new StyleTextCellRenderer("we", false));
        _jt.registerCellRenderer(_tableModel.getColumn(6), new BarCellRenderer());
        _jt.registerCellRenderer(_tableModel.getColumn(8), new SmileyCellRenderer());
        // risk renderer and editor
        _jt.registerCellRenderer(DummyRow.Risk.class, new RiskRenderer());
        _jt.registerCellEditor(DummyRow.Risk.class, new RiskCellEditor());
        // risk values 1 to 3
        _jt.registerCellEditor(_tableModel.getColumn(10), new IntegerCellEditor(1, 3));
        _jt.registerCellEditor(_tableModel.getColumn(11), new IntegerCellEditor(1, 3));

        ObjectImageRenderer oiRenderer = new ObjectImageRenderer();
        oiRenderer.addObjectRessourceNameMapping(DummyRow.TestEnum.ENUMVAL1, "1",
                "/de/jaret/examples/table/warning.gif");
        oiRenderer.addObjectRessourceNameMapping(DummyRow.TestEnum.ENUMVAL2, "2", "/de/jaret/examples/table/error.gif");
        oiRenderer.addObjectRessourceNameMapping(DummyRow.TestEnum.ENUMVAL3, "3",
                "/de/jaret/examples/table/information.gif");
        _jt.registerCellRenderer(_tableModel.getColumn(15), oiRenderer);

        List<Object> l = new ArrayList<Object>();
        l.add("first text");
        l.add("second text");
        l.add("third text");
        ObjectComboEditor oce = new ObjectComboEditor(l, null, true, "this is null");
        _jt.registerCellEditor(((DefaultJaretTableModel) _tableModel).getColumn("x1"), oce);

        _jt.setTableModel(_tableModel);

        
        
        // register autofilters
        _jt.registerAutoFilterForClass(Integer.class, SampleIntegerAutoFilter.class);
        _jt.registerAutoFilterForColumn(_tableModel.getColumn(2), SampleTextAutoFilter.class);
        
        
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

    static ImageRegistry _imageRegistry;

    public static ImageRegistry getImageRegistry() {
        if (_imageRegistry == null) {
            _imageRegistry = new ImageRegistry();
            ImageDescriptor imgDesc = new ResourceImageDescriptor("/de/jaret/examples/table/splash.bmp");
            _imageRegistry.put("splash", imgDesc);
            imgDesc = new ResourceImageDescriptor("/de/jaret/examples/table/keyboard.png");
            _imageRegistry.put("keyboard", imgDesc);
        }
        return _imageRegistry;
    }

    public static void main(String args[]) {
        TableExample te = new TableExample(null);
    }

}
