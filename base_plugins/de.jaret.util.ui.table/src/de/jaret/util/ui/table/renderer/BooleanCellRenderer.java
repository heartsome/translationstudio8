/*
 *  File: BooleanCellRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.ui.ResourceImageDescriptor;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * CellRenderer rendering a Boolean to a checkbox image (default) or any other two images.
 * 
 * @author Peter Kliem
 * @version $Id: BooleanCellRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class BooleanCellRenderer extends CellRendererBase implements ICellRenderer {
    /** rsc name for the checked state. */
    protected String _checkedRscName = "/de/jaret/util/ui/table/resource/checked.gif";
    /** default rsc name for the unchecked state. */
    protected String _uncheckedRscName = "/de/jaret/util/ui/table/resource/unchecked.gif";
    /** key for checked image in registry. */
    protected static final String CHECKED = "checked";
    /** key for unchecked image in registry. */
    protected static final String UNCHECKED = "unchecked";
    /** image registry for holding the images. */
    private ImageRegistry _imageRegistry;

    /**
     * Construct a boolean cell renderer for a printer device using default resources.
     * 
     * @param printer printer device
     */
    public BooleanCellRenderer(Printer printer) {
        super(printer);
    }

    /**
     * Construct a boolean cell renderer for the display using default resources.
     */
    public BooleanCellRenderer() {
        super(null);
    }

    /**
     * Construct a boolean cell renderer for a printer device providing resource names.
     * 
     * @param printer printer device
     * @param checkedRscName resource path for the checked image
     * @param uncheckedRscName resource path for the unchecked image
     */
    public BooleanCellRenderer(Printer printer, String checkedRscName, String uncheckedRscName) {
        super(printer);
        _checkedRscName = checkedRscName;
        _uncheckedRscName = uncheckedRscName;
    }

    /**
     * Construct a boolean cell renderer for the display providing resource names.
     * 
     * @param checkedRscName resource path for the checked image
     * @param uncheckedRscName resource path for the unchecked image
     */
    public BooleanCellRenderer(String checkedRscName, String uncheckedRscName) {
        super(null);
        _checkedRscName = checkedRscName;
        _uncheckedRscName = uncheckedRscName;
    }

    /**
     * {@inheritDoc}
     */
    public void draw(GC gc, JaretTable jaretTable, ICellStyle cellStyle, Rectangle drawingArea, IRow row,
            IColumn column, boolean drawFocus, boolean selected, boolean printing) {
        drawBackground(gc, drawingArea, cellStyle, selected, printing);
        Rectangle drect = drawBorder(gc, cellStyle, drawingArea, printing);
        Rectangle rect = applyInsets(drect);
        Object value = column.getValue(row);
        if (value instanceof Boolean) {
            Image img = null;
            if (((Boolean) value).booleanValue()) {
                img = getImageRegistry().get(CHECKED);
            } else {
                img = getImageRegistry().get(UNCHECKED);
            }
            int x = rect.x + (rect.width - scaleX(img.getBounds().width)) / 2;
            int y = rect.y + (rect.height - scaleY(img.getBounds().height)) / 2;
            gc.drawImage(img, 0, 0, img.getBounds().width, img.getBounds().height, x, y, scaleX(img.getBounds().width),
                    scaleY(img.getBounds().height));
        } else {
            // indicate error with red fill
            Color bg = gc.getBackground();
            gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA));
            gc.fillRectangle(rect);
            gc.setBackground(bg);
        }
        if (drawFocus) {
            drawFocus(gc, drect);
        }
        drawSelection(gc, drawingArea, cellStyle, selected, printing);
    }

    /**
     * {@inheritDoc}
     */
    public int getPreferredWidth(List<IRow> rows, IColumn column) {
        return getImageRegistry().get(CHECKED).getBounds().width;
    }

    /**
     * {@inheritDoc}
     */
    public int getPreferredHeight(GC gc, ICellStyle cellStyle, int width, IRow row, IColumn column) {
        return getImageRegistry().get(CHECKED).getBounds().height;
    }

    /**
     * Retrieve the image registry used by the renderer (lazy initializing).
     * 
     * @return initialized image regsitry containing the resources
     */
    private ImageRegistry getImageRegistry() {
        if (_imageRegistry == null) {
            _imageRegistry = new ImageRegistry();
            ImageDescriptor imgDesc = new ResourceImageDescriptor(_checkedRscName, this.getClass());
            _imageRegistry.put(CHECKED, imgDesc.createImage());
            imgDesc = new ResourceImageDescriptor(_uncheckedRscName, this.getClass());
            _imageRegistry.put(UNCHECKED, imgDesc.createImage());
        }
        return _imageRegistry;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        if (_imageRegistry != null) {
            _imageRegistry.dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    public ICellRenderer createPrintRenderer(Printer printer) {
        return new BooleanCellRenderer(printer, _checkedRscName, _uncheckedRscName);
    }

}
