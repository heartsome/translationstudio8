/*
 *  File: ObjectImageRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * CellRenderer rendering object instances (i.e. enums) to images.
 * 
 * @author Peter Kliem
 * @version $Id: ObjectImageRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class ObjectImageRenderer extends CellRendererBase implements ICellRenderer {
    protected Map<Object, String> _keyMap = new HashMap<Object, String>();
    private ImageRegistry _imageRegistry;

    public ObjectImageRenderer(Printer printer) {
        super(printer);
    }

    public ObjectImageRenderer() {
        super(null);
    }

    /**
     * Add a mapping between an object instance and an image descriptor.
     * 
     * @param o object instance
     * @param key string key (has to be non null an unique for this renderer) to identfy the object
     * @param imageDescriptor image descriptor for the image
     */
    public void addObjectImageDescriptorMapping(Object o, String key, ImageDescriptor imageDescriptor) {
        getImageRegistry().put(key, imageDescriptor);
        _keyMap.put(o, key);
    }

    /**
     * Add a mapping between object instance and an image ressource.
     * 
     * @param o object instance
     * @param key string key (has to be non null an unique for this renderer) to identfy the object
     * @param ressourceName ressource path
     */
    public void addObjectRessourceNameMapping(Object o, String key, String ressourceName) {
        ImageDescriptor imgDesc = new ResourceImageDescriptor(ressourceName, this.getClass());
        addObjectImageDescriptorMapping(o, key, imgDesc);
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
        String key = _keyMap.get(value);

        if (key != null) {
            Image img = null;
            img = getImageRegistry().get(key);
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
     * 
     * @TODO
     */
    public int getPreferredWidth(List<IRow> rows, IColumn column) {
        return -1;// return getImageRegistry().get(CHECKED).getBounds().width;
    }

    /**
     * {@inheritDoc} TODO
     */
    public int getPreferredHeight(GC gc, ICellStyle cellStyle, int width, IRow row, IColumn column) {
        return -1;// getImageRegistry().get(CHECKED).getBounds().height;
    }

    /**
     * Retrieve the image registry instance.
     * 
     * @return ImageRegistry
     */
    private synchronized ImageRegistry getImageRegistry() {
        if (_imageRegistry == null) {
            _imageRegistry = new ImageRegistry();
        }
        return _imageRegistry;
    }

    /**
     * {@inheritDoc} Disposes the image registry and clears the map with object instances to help garbage collecting.
     */
    public void dispose() {
        if (_imageRegistry != null) {
            _imageRegistry.dispose();
        }
        _keyMap.clear();
    }

    /**
     * {@inheritDoc}
     */
    public ICellRenderer createPrintRenderer(Printer printer) {
        ObjectImageRenderer renderer = new ObjectImageRenderer(printer);
        for (Object o : _keyMap.keySet()) {
            String key = _keyMap.get(o);
            ImageDescriptor imageDesc = getImageRegistry().getDescriptor(key);
            renderer.addObjectImageDescriptorMapping(o, key, imageDesc);
        }
        return renderer;
    }

}
