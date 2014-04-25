/*
 *  File: JaretTablePrintDialog.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.print;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

import de.jaret.util.ui.table.JaretTablePrinter;

/**
 * Simple print dialog for a jaret table.
 * 
 * @author Peter Kliem
 * @version $Id: JaretTablePrintDialog.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class JaretTablePrintDialog extends Dialog {
    protected static PrinterData _printerData;
    protected int _pIdx = -1;
    protected String[] _printers;
    protected PrinterData[] _pdatas;
    protected CCombo _printerCombo;
    protected JaretTablePrintConfiguration _configuration;
    protected Button _repeatHeader;
    protected Label _pagesLabel;
    protected JaretTablePrinter _tablePrinter;

    public JaretTablePrintDialog(Shell parentShell, String printerName, JaretTablePrinter tablePrinter,
            JaretTablePrintConfiguration printConfiguration) {
        super(parentShell);
        _tablePrinter = tablePrinter;
        _configuration = printConfiguration;
        if (_configuration == null) {
            _configuration = new JaretTablePrintConfiguration("table", false, 1.0);
        }
        if (printerName == null && _printerData != null) {
            printerName = _printerData.name;
        }

        _pdatas = Printer.getPrinterList();
        _printers = new String[_pdatas.length];
        int stdIdx = -1;
        for (int i = 0; i < _pdatas.length; i++) {
            PrinterData pd = _pdatas[i];
            _printers[i] = pd.name;
            if (printerName != null && pd.name.equals(printerName)) {
                _pIdx = i;
            }
            if (pd.name.equals(Printer.getDefaultPrinterData().name)) {
                stdIdx = i;
            }
        }
        if (_pIdx == -1) {
            _printerData = Printer.getDefaultPrinterData();
            _pIdx = stdIdx;
        } else {
            _printerData = _pdatas[_pIdx];
        }
    }

    public void setRowLimit(int limit) {
        _configuration.setRowLimit(limit);
    }

    public void setColLimit(int limit) {
        _configuration.setColLimit(limit);
    }

    public JaretTablePrintConfiguration getConfiguration() {
        return _configuration;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Print");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = new Composite(parent, SWT.NULL);
        // dialogArea.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
        GridData gd1 = new GridData(GridData.FILL_BOTH);
        dialogArea.setLayoutData(gd1);
        GridLayout gl = new GridLayout();
        gl.numColumns = 1;
        dialogArea.setLayout(gl);

        createPrinterSelection(dialogArea);

        Composite parameterArea = new Composite(dialogArea, SWT.NULL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        parameterArea.setLayoutData(gd);
        createParameterArea(parameterArea);
        return dialogArea;
    }

    @Override
    protected void okPressed() {
        _printerData = _pdatas[_printerCombo.getSelectionIndex()];
        super.okPressed();
    }

    private void createPrinterSelection(Composite parent) {
        Composite area = new Composite(parent, SWT.NULL);
        area.setLayout(new RowLayout());
        // area.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));

        _printerCombo = new CCombo(area, SWT.BORDER | SWT.READ_ONLY);
        _printerCombo.setItems(_printers);
        _printerCombo.select(_pIdx);

        Button select = new Button(area, SWT.PUSH);
        select.setText("Configure");
        select.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                PrintDialog pd = new PrintDialog(Display.getCurrent().getActiveShell());
                PrinterData pdata = pd.open();
                if (pdata != null) {
                    _printerData = pdata;
                    select(_printerData);
                }
            }

            private void select(PrinterData printerData) {
                for (int i = 0; i < _pdatas.length; i++) {
                    PrinterData pd = _pdatas[i];
                    if (pd.name.equals(printerData.name)) {
                        _printerCombo.select(i);
                        break;
                    }
                }

            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }

        });

    }

    protected void createParameterArea(Composite parent) {
        GridLayout gl = new GridLayout();
        gl.numColumns = 2;
        parent.setLayout(gl);

        _repeatHeader = new Button(parent, SWT.CHECK);
        _repeatHeader.setSelection(_configuration.getRepeatHeader());
        _repeatHeader.setText("Repeat header");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        _repeatHeader.setLayoutData(gd);

        final Label scaleText = new Label(parent, SWT.RIGHT);
        scaleText.setText(getScaleText());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        scaleText.setLayoutData(gd);

        final Scale scale = new Scale(parent, SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        scale.setLayoutData(gd);
        scale.setMaximum(1000);
        scale.setMinimum(10);
        scale.setSelection((int) (_configuration.getScale() * 100));
        scale.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent ev) {
                int val = scale.getSelection();
                double s = (double) val / 100.0;
                _configuration.setScale(s);
                scaleText.setText(getScaleText());
                updateConf();
            }

            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        _pagesLabel = new Label(parent, SWT.RIGHT);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        _pagesLabel.setLayoutData(gd);
        _printerData = _pdatas[_printerCombo.getSelectionIndex()];
        Printer printer = new Printer(_printerData);
        _tablePrinter.setPrinter(printer);
        Point pages = _tablePrinter.calculatePageCount(_configuration);
        printer.dispose();
        _pagesLabel.setText(getPagesText(pages));

    }

    private String getScaleText() {
        int pc = (int) (_configuration.getScale() * 100);
        return Integer.toString(pc) + "%";
    }

    private String getPagesText(Point pages) {
        return "X: " + pages.x + " Y: " + pages.y + " (" + pages.x * pages.y + " pages)";
    }

    private void updateConf() {
        _configuration.setRepeatHeader(_repeatHeader.getSelection());
        Printer printer = new Printer(_printerData);
        _tablePrinter.setPrinter(printer);
        Point pages = _tablePrinter.calculatePageCount(_configuration);
        printer.dispose();
        _pagesLabel.setText(getPagesText(pages));
    }

    public PrinterData getPrinterData() {
        return _printerData;
    }

}
