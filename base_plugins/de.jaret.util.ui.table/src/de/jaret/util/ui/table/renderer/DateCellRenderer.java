/*
 *  File: DateCellRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.swt.printing.Printer;

import de.jaret.util.date.JaretDate;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Cell renderer for a date. Can also render a JaretDate.
 * 
 * @author Peter Kliem
 * @version $Id: DateCellRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class DateCellRenderer extends TextCellRenderer {
    /** dateformat used to format the date to text. */
    protected DateFormat _dateformat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    /**
     * Construct a date cell renderer for a printer.
     * 
     * @param printer priner device
     */
    public DateCellRenderer(Printer printer) {
        super(printer);
    }

    /**
     * Construct a date cell renderer.
     */
    public DateCellRenderer() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    protected String convertValue(IRow row, IColumn column) {
        Object value = column.getValue(row);
        if (value instanceof Date) {
            Date date = (Date) value;
            return _dateformat.format(date);
        } else if (value instanceof JaretDate) {
            Date date = ((JaretDate) value).getDate();
            return _dateformat.format(date);
        }
        return "";
    }

    /**
     * Retrive the used date format.
     * 
     * @return Returns the dateformat.
     */
    public DateFormat getDateformat() {
        return _dateformat;
    }

    /**
     * Set the dateformat used for text transformation.
     * 
     * @param dateformat The dateformat to set.
     */
    public void setDateformat(DateFormat dateformat) {
        _dateformat = dateformat;
    }

    /**
     * {@inheritDoc}
     */
    public ICellRenderer createPrintRenderer(Printer printer) {
        DateCellRenderer renderer = new DateCellRenderer(printer);
        renderer.setDateformat(getDateformat());
        return renderer;
    }

}
