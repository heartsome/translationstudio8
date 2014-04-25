/*
 *  File: ClassImageRenderer.java 
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
 * CellRenderer rendering images corresponding to the class of the value.
 * 
 * @author Peter Kliem
 * @version $Id: ClassImageRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class ClassImageRenderer extends CellRendererBase implements ICellRenderer {
    protected Map<Class, String> _keyMap = new HashMap<Class, String>();
    private ImageRegistry _imageRegistry;

    public ClassImageRenderer(Printer printer) {
        super(printer);
    }

    public ClassImageRenderer() {
        super(null);
    }

    /**
     * Add a mapping between a class and an image descriptor.
     * 
     * @param clazz the class
     * @param key string key (has to be non null an unique for this renderer) to identfy the object
     * @param imageDescriptor image descriptor for the image
     */
    public void addClassImageDescriptorMapping(Class<?> clazz, String key, ImageDescriptor imageDescriptor) {
        getImageRegistry().put(key, imageDescriptor);
        _keyMap.put(clazz, key);
    }

    /**
     * Add a mapping between a class and an image ressource.
     * 
     * @param clazz class
     * @param key string key (has to be non null an unique for this renderer) to identfy the object
     * @param ressourceName ressource path
     */
    public void addClassRessourceNameMapping(Class<?> clazz, String key, String ressourceName) {
        ImageDescriptor imgDesc = new ResourceImageDescriptor(ressourceName, this.getClass());
        addClassImageDescriptorMapping(clazz, key, imgDesc);
    }

    /**
     * Retrieve the key for a class, checking all super classes and interfaces.
     * 
     * @param clazz class to check
     * @return key or null
     */
    protected String getKeyForClass(Class<?> clazz) {
        String result = _keyMap.get(clazz);
        if (result != null) {
            return result;
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            result = _keyMap.get(interfaces[i]);
            if (result != null) {
                return result;
            }
        }

        Class<?> sc = clazz.getSuperclass();
        if (sc != null) {
            result = getKeyForClass(sc);
        }
        return result;
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
        String key = getKeyForClass(value.getClass());

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
     * {@inheritDoc} Disposes the image registry and clears the key map to help garbage collecting.
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
        ClassImageRenderer renderer = new ClassImageRenderer(printer);
        for (Class<?> clazz : _keyMap.keySet()) {
            String key = _keyMap.get(clazz);
            ImageDescriptor imageDesc = getImageRegistry().getDescriptor(key);
            renderer.addClassImageDescriptorMapping(clazz, key, imageDesc);
        }
        return renderer;
    }

}
