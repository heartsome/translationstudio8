/*
 *  File: MultilineListCellRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.examples.table.mllist;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import de.jaret.examples.table.DummyRow;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.renderer.CellRendererBase;
import de.jaret.util.ui.table.renderer.ICellRenderer;
import de.jaret.util.ui.table.renderer.ICellStyle;

public class MultilineListCellRenderer extends CellRendererBase implements ICellRenderer {
    Font boldFont;
    Font normalFont;

    public MultilineListCellRenderer(Printer printer) {
        super(printer);
    }

    public MultilineListCellRenderer() {
        super(null);
        boldFont = new Font(Display.getCurrent(), "Arial", 10, SWT.BOLD);
        normalFont = new Font(Display.getCurrent(), "Arial", 10, SWT.NORMAL);
    }

    public void draw(GC gc, JaretTable jaretTable, ICellStyle cellStyle, Rectangle drawingArea, IRow row,
            IColumn column, boolean drawFocus, boolean selected, boolean printing) {
        drawBackground(gc, drawingArea, cellStyle, selected, printing);
        Rectangle drect = drawBorder(gc, cellStyle, drawingArea, printing);
        Rectangle rect = applyInsets(drect);
        DummyRow dr = (DummyRow) row;

        Image img = dr.getImg();
        int x = rect.x + 4;
        int y = rect.y + (rect.height - img.getBounds().height) / 2;
        gc.drawImage(img, x, y);

        Font save = gc.getFont();

        gc.setFont(boldFont);
        gc.drawString(dr.getT2(), rect.x + 70, y + 5);
        gc.setFont(normalFont);
        gc.drawString(dr.getT3(), rect.x + 70, y + 25);

        gc.setFont(save);
        if (drawFocus) {
            drawFocus(gc, drawingArea);
        }
        drawSelection(gc, drawingArea, cellStyle, selected, printing);

    }

    public ICellRenderer createPrintRenderer(Printer printer) {
        // TODO Auto-generated method stub
        return null;
    }

    public void dispose() {
        boldFont.dispose();
        normalFont.dispose();
    }

}
