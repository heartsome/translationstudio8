/*
 *  File: LabelProviderRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.printing.Printer;

import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Cell renderer rendering an object using an ILabelProvider (uses text only).
 * 
 * @author kliem
 * @version $Id: LabelProviderRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class LabelProviderRenderer extends TextCellRenderer {
    /** Label provider tat will be used. */
    protected ILabelProvider _labelProvider;

    /**
     * Construct a label provider renderer for a printer.
     * 
     * @param printer printer device
     */
    public LabelProviderRenderer(Printer printer) {
        super(printer);
    }

    /**
     * Construct a label provider renderer.
     */
    public LabelProviderRenderer() {
        this(null);
    }

    
    /**
     * {@inheritDoc} Use the label provider to convert value to String.
     */
    protected String convertValue(IRow row, IColumn column) {
        if (_labelProvider == null) {
            // error: handle gracefully
            return "no label provider set";
        }
        Object value = column.getValue(row);
        return _labelProvider.getText(value);
    }
    
    /**
     * Retrieve the label provider used.
     * 
     * @return the label provider
     */
    public ILabelProvider getLabelProvider() {
        return _labelProvider;
    }

    /**
     * Set the label provider to be used by the renderer.
     * 
     * @param labelProvider label provider to be used
     */
    public void setLabelProvider(ILabelProvider labelProvider) {
        _labelProvider = labelProvider;
    }

    /**
     * {@inheritDoc}
     */
    public ICellRenderer createPrintRenderer(Printer printer) {
        LabelProviderRenderer lpr = new LabelProviderRenderer(printer);
        lpr.setLabelProvider(getLabelProvider());
        return lpr;
    }

}
