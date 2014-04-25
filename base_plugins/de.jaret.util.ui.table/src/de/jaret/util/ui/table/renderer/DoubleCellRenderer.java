/*
 *  File: DoubleCellRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.swt.printing.Printer;

import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * CellRenderer for double values.
 * 
 * @author Peter Kliem
 * @version $Id: DoubleCellRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class DoubleCellRenderer extends TextCellRenderer {
    /** default fraction digits. */
    protected static final int DEFAULT_FRACTION_DIGITS = 2;
    /** number format for text converson. */
    protected NumberFormat _numberFormat = DecimalFormat.getIntegerInstance();

    /**
     * Construct a double cell renderer for printing.
     * 
     * @param printer printer device
     */
    public DoubleCellRenderer(Printer printer) {
        super(printer);
        _numberFormat.setMaximumFractionDigits(DEFAULT_FRACTION_DIGITS);
        _numberFormat.setMinimumFractionDigits(DEFAULT_FRACTION_DIGITS);
    }

    /**
     * Construct a double cell renderer for use with a display.
     */
    public DoubleCellRenderer() {
        this(null);
    }

    /**
     * Retrieve the used number format.
     * 
     * @return number format
     */
    public NumberFormat getNumberFormat() {
        return _numberFormat;
    }

    /**
     * Set number format used for text conversion.
     * 
     * @param numberFormat number format
     */
    public void setNumberFormat(NumberFormat numberFormat) {
        _numberFormat = numberFormat;
    }

    /**
     * {@inheritDoc}
     */
    protected String convertValue(IRow row, IColumn column) {
        Double value = (Double) column.getValue(row);
        return value != null ? _numberFormat.format(value.doubleValue()) : null;
    }

    /**
     * {@inheritDoc}
     */
    public ICellRenderer createPrintRenderer(Printer printer) {
        DoubleCellRenderer dcr = new DoubleCellRenderer(printer);
        dcr.setNumberFormat(getNumberFormat());
        return dcr;
    }

}
