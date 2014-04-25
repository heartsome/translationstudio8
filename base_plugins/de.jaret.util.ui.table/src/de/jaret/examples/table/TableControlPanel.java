/*
 *  File: TableControlPanel.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.examples.table;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.JaretTablePrinter;
import de.jaret.util.ui.table.model.AbstractRowFilter;
import de.jaret.util.ui.table.model.AbstractRowSorter;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IJaretTableModel;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.model.ITableViewState;
import de.jaret.util.ui.table.model.simple.SimpleJaretTableModel;
import de.jaret.util.ui.table.print.JaretTablePrintConfiguration;
import de.jaret.util.ui.table.print.JaretTablePrintDialog;
import de.jaret.util.ui.table.renderer.DefaultTableHeaderRenderer;
import de.jaret.util.ui.table.renderer.ICellStyle;
import de.jaret.util.ui.table.renderer.IStyleStrategy;
import de.jaret.util.ui.table.strategies.DefaultCCPStrategy;

/**
 * Simple controlpanel for a jaret table (demonstration and test).
 * 
 * @author Peter Kliem
 * @version $Id: TableControlPanel.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class TableControlPanel extends Composite {

    private JaretTable _table;

    public TableControlPanel(Composite arg0, int arg1, JaretTable table) {
        super(arg0, arg1);
        _table = table;
        createControls();
    }

    /**
     * @param panel
     */
    private void createControls() {
        RowLayout rl = new RowLayout();
        rl.type = SWT.HORIZONTAL;
        this.setLayout(rl);

        Composite col1 = new Composite(this, SWT.NULL);
        rl = new RowLayout();
        rl.type = SWT.VERTICAL;
        col1.setLayout(rl);
        Composite col2 = new Composite(this, SWT.NULL);
        rl = new RowLayout();
        rl.type = SWT.VERTICAL;
        col2.setLayout(rl);
        Composite col3 = new Composite(this, SWT.NULL);
        rl = new RowLayout();
        rl.type = SWT.VERTICAL;
        col3.setLayout(rl);

        final Button autoFilterCheck = new Button(col1, SWT.CHECK);
        autoFilterCheck.setText("AutoFilter");
        autoFilterCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                _table.setAutoFilterEnable(autoFilterCheck.getSelection());
            }
        });

        final Button drawHeaderCheck = new Button(col1, SWT.CHECK);
        drawHeaderCheck.setSelection(_table.getDrawHeader());
        drawHeaderCheck.setText("Draw header");
        drawHeaderCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                _table.setDrawHeader(drawHeaderCheck.getSelection());
            }
        });

        final Button fillDragCheck = new Button(col1, SWT.CHECK);
        fillDragCheck.setSelection(_table.isSupportFillDragging());
        fillDragCheck.setText("Support fill dragging");
        fillDragCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                _table.setSupportFillDragging(fillDragCheck.getSelection());
            }
        });

        Button b = new Button(col2, SWT.PUSH);
        b.setText("Print");
        b.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                print();
            }
        });
        final Scale headerRotationScale = new Scale(col2, SWT.HORIZONTAL);
        headerRotationScale.setMaximum(90);
        headerRotationScale.setMinimum(0);
        headerRotationScale.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ev) {
                int val = headerRotationScale.getSelection();
                ((DefaultTableHeaderRenderer) _table.getHeaderRenderer()).setRotation(val);
                if (val > 0) {
                    _table.setHeaderHeight(50);
                } else {
                    _table.setHeaderHeight(18);
                }
                _table.redraw();
            }
        });

        final Button allowHeaderResizeCheck = new Button(col1, SWT.CHECK);
        allowHeaderResizeCheck.setSelection(_table.getDrawHeader());
        allowHeaderResizeCheck.setText("Allow header resize");
        allowHeaderResizeCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                _table.setHeaderResizeAllowed(allowHeaderResizeCheck.getSelection());
            }

        });
        final Button allowRowResizeCheck = new Button(col1, SWT.CHECK);
        allowRowResizeCheck.setSelection(_table.getDrawHeader());
        allowRowResizeCheck.setText("Allow row resize");
        allowRowResizeCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                _table.setRowResizeAllowed(allowRowResizeCheck.getSelection());
            }
        });

        final Button allowColResizeCheck = new Button(col1, SWT.CHECK);
        allowColResizeCheck.setSelection(_table.getDrawHeader());
        allowColResizeCheck.setText("Allow column resize");
        allowColResizeCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                _table.setColumnResizeAllowed(allowColResizeCheck.getSelection());
            }

        });

        Label l = new Label(col2, SWT.NULL);
        l.setText("Fixed columns");

        final Combo fixedColCombo = new Combo(col2, SWT.BORDER | SWT.READ_ONLY);
        fixedColCombo.setItems(new String[] {"0", "1", "2", "3", "4"});
        fixedColCombo.select(0);
        fixedColCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                _table.setFixedColumns(fixedColCombo.getSelectionIndex());
            }
        });

        l = new Label(col2, SWT.NULL);
        l.setText("Fixed rows");

        final Combo fixedRowCombo = new Combo(col2, SWT.BORDER | SWT.READ_ONLY);
        fixedRowCombo.setItems(new String[] {"0", "1", "2", "3", "4"});
        fixedRowCombo.select(0);
        fixedRowCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                _table.setFixedRows(fixedRowCombo.getSelectionIndex());
            }
        });

        final Button resizeRestrictionCheck = new Button(col1, SWT.CHECK);
        resizeRestrictionCheck.setSelection(_table.getResizeRestriction());
        resizeRestrictionCheck.setText("Restrict resizing to headers/row headers");
        resizeRestrictionCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                _table.setResizeRestriction(resizeRestrictionCheck.getSelection());
            }
        });

        final Button excludeFixedRowsCheck = new Button(col1, SWT.CHECK);
        excludeFixedRowsCheck.setSelection(_table.getExcludeFixedRowsFromSorting());
        excludeFixedRowsCheck.setText("Exclude fixed rows from sorting");
        excludeFixedRowsCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                _table.setExcludeFixedRowsFromSorting(excludeFixedRowsCheck.getSelection());
            }

        });

        final Button rowFilterCheck = new Button(col1, SWT.CHECK);
        rowFilterCheck.setSelection(false);
        rowFilterCheck.setText("Set rowfilter (even char count on col2)");
        rowFilterCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                boolean sel = rowFilterCheck.getSelection();
                if (sel) {
                    _table.setRowFilter(new AbstractRowFilter() {

                        public boolean isInResult(IRow row) {
                            return ((DummyRow) row).getT2() != null && ((DummyRow) row).getT2().length() % 2 == 0;
                        }

                    });
                } else {
                    _table.setRowFilter(null);
                }

            }
        });

        final Button rowSorterCheck = new Button(col1, SWT.CHECK);
        rowSorterCheck.setSelection(false);
        rowSorterCheck.setText("Set rowsorter (char count on col3)");
        rowSorterCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                boolean sel = rowSorterCheck.getSelection();
                if (sel) {
                    _table.setRowSorter(new AbstractRowSorter() {

                        public int compare(IRow o1, IRow o2) {
                            int c1 = ((DummyRow) o1).getT3() != null ? ((DummyRow) o1).getT3().length() : 0;
                            int c2 = ((DummyRow) o2).getT3() != null ? ((DummyRow) o2).getT3().length() : 0;

                            return c1 - c2;
                        }

                    });
                } else {
                    _table.setRowSorter(null);
                }

            }

        });

        final Button onlyRowSelectionCheck = new Button(col1, SWT.CHECK);
        onlyRowSelectionCheck.setSelection(false);
        onlyRowSelectionCheck.setText("Only row selection allowed");
        onlyRowSelectionCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                boolean sel = onlyRowSelectionCheck.getSelection();
                _table.getSelectionModel().setOnlyRowSelectionAllowed(sel);
                _table.getSelectionModel().clearSelection();
            }
        });

        final Button optimizeScrollingCheck = new Button(col1, SWT.CHECK);
        optimizeScrollingCheck.setSelection(_table.getOptimizeScrolling());
        optimizeScrollingCheck.setText("Optimize scrolling");
        optimizeScrollingCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                boolean sel = optimizeScrollingCheck.getSelection();
                _table.setOptimizeScrolling(sel);
            }
        });

        /**
         * Style strategy coloring the background of odd row indizes. The implementation is brute force creating
         * tons of objects underway ... so be careful.
         */
        final IStyleStrategy _styleStrategy = new IStyleStrategy() {

            public ICellStyle getCellStyle(IRow row, IColumn column, ICellStyle incomingStyle,
                    ICellStyle defaultCellStyle) {
                if (_table.getInternalRowIndex(row) % 2 == 0) {
                    return incomingStyle;
                } else {
                    ICellStyle s = incomingStyle.copy();
                    s.setBackgroundColor(new RGB(230, 230, 230));
                    return s;
                }
            }

        };

        final Button bgColoringCheck = new Button(col1, SWT.CHECK);
        bgColoringCheck.setSelection(_table.getTableViewState().getCellStyleProvider().getStyleStrategy() != null);
        bgColoringCheck.setText("BG coloring (IStyleStrategy)");
        bgColoringCheck.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                boolean sel = bgColoringCheck.getSelection();
                if (!sel) {
                    _table.getTableViewState().getCellStyleProvider().setStyleStrategy(null);
                    _table.redraw();
                } else {
                    _table.getTableViewState().getCellStyleProvider().setStyleStrategy(_styleStrategy);
                    _table.redraw();
                }
            }
        });

        Button b2 = new Button(col2, SWT.PUSH);
        b2.setText("Spawn new window");
        b2.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                // hack
                if (_table.getHierarchicalModel() == null) {
                    if (_table.getTableModel() instanceof SimpleJaretTableModel) {
                        new SimpleModelExample(_table.getTableModel());
                    } else {
                        new TableExample(_table.getTableModel());
                    }
                } else {
                    new TableHierarchicalExample(_table.getHierarchicalModel());
                }
            }

        });

        b2 = new Button(col2, SWT.PUSH);
        b2.setText("Start changing bars");
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                for (int i = 0; i < _table.getTableModel().getRowCount(); i++) {
                    Runnable r = new Changer(_table.getTableModel(), i);
                    Thread t = new Thread(r);
                    t.start();
                }
            }
        });

        b2 = new Button(col3, SWT.PUSH);
        b2.setText("Set heightmode OPTIMAL");
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                _table.getTableViewState().setRowHeightMode(ITableViewState.RowHeightMode.OPTIMAL);
            }
        });

        b2 = new Button(col3, SWT.PUSH);
        b2.setText("Set heightmode OPTANDVAR");
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                _table.getTableViewState().setRowHeightMode(ITableViewState.RowHeightMode.OPTANDVAR);
            }
        });

        b2 = new Button(col3, SWT.PUSH);
        b2.setText("Set heightmode VARIABLE");
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                _table.getTableViewState().setRowHeightMode(ITableViewState.RowHeightMode.VARIABLE);
            }
        });

        b2 = new Button(col3, SWT.PUSH);
        b2.setText("Set heightmode FIXED");
        b2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                _table.getTableViewState().setRowHeightMode(ITableViewState.RowHeightMode.FIXED);
            }
        });

        l = new Label(col3, SWT.NULL);
        l.setText("Column resize mode");
        final Combo colModeCombo = new Combo(col3, SWT.BORDER | SWT.READ_ONLY);
        colModeCombo.setItems(new String[] {"NONE", "SUBSEQUENT", "ALLSUBSEQUENT", "ALL"});
        colModeCombo.select(0);
        colModeCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                String sel = colModeCombo.getText();
                _table.getTableViewState().setColumnResizeMode(ITableViewState.ColumnResizeMode.valueOf(sel));
            }
        });

        b2 = new Button(col3, SWT.PUSH);
        b2.setText("Clipboard info");
        b2.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                Clipboard cb = new Clipboard(Display.getCurrent());

                System.out.println("Clipboard info");
                TextTransfer textTransfer = TextTransfer.getInstance();
                Object content = cb.getContents(textTransfer);
                if (content != null) {
                    System.out.println("TEXT: " + content.getClass() + ":" + content.toString());
                }

                RTFTransfer rtfTransfer = RTFTransfer.getInstance();
                content = cb.getContents(rtfTransfer);
                if (content != null) {
                    System.out.println("RTF: " + content.getClass() + ":" + content.toString());
                }

                HTMLTransfer htmlTransfer = HTMLTransfer.getInstance();
                content = cb.getContents(htmlTransfer);
                if (content != null) {
                    System.out.println("HTML: " + content.getClass() + ":" + content.toString());
                }
            }

        });

        final Button includeColHeadingsWhenCopying = new Button(col3, SWT.CHECK);
        includeColHeadingsWhenCopying.setText("Include col header when copying");
        if (_table.getCcpStrategy() instanceof DefaultCCPStrategy) {
            DefaultCCPStrategy stategy = (DefaultCCPStrategy) _table.getCcpStrategy();
            includeColHeadingsWhenCopying.setSelection(stategy.getIncludeHeadersInCopy());
            includeColHeadingsWhenCopying.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent arg0) {
                    boolean sel = includeColHeadingsWhenCopying.getSelection();
                    DefaultCCPStrategy stategy = (DefaultCCPStrategy) _table.getCcpStrategy();
                    stategy.setIncludeHeadersInCopy(sel);
                }
            });
        } else {
            includeColHeadingsWhenCopying.setEnabled(false);
        }

    }

    public class Changer implements Runnable {
        IJaretTableModel _model;
        int _idx;

        public Changer(IJaretTableModel model, int idx) {
            _model = model;
            _idx = idx;
        }

        public void run() {
            DummyRow r = (DummyRow) _model.getRow(_idx);
            while (r.getInteger() < 100) {
                System.out.println("Index " + _idx + " val " + r.getInteger());
                r.setInteger(r.getInteger() + 1);
                try {
                    Thread.sleep((long) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void print() {
        JaretTablePrinter jtp = new JaretTablePrinter(null, _table);
        JaretTablePrintDialog pDialog = new JaretTablePrintDialog(Display.getCurrent().getActiveShell(), null, jtp,
                null);

        pDialog.open();
        if (pDialog.getReturnCode() == Dialog.OK) {
            PrinterData pdata = pDialog.getPrinterData();
            JaretTablePrintConfiguration conf = pDialog.getConfiguration();
            Printer printer = new Printer(pdata);
            jtp.setPrinter(printer);
            jtp.print(conf);

            printer.dispose();
        }
        jtp.dispose();

    }

}
