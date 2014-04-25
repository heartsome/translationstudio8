/*
 *  File: RiskRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.examples.table.renderer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import de.jaret.examples.table.DummyRow;
import de.jaret.examples.table.DummyRow.Risk;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.renderer.CellRendererBase;
import de.jaret.util.ui.table.renderer.ICellRenderer;
import de.jaret.util.ui.table.renderer.ICellStyle;

/**
 * Fun renderer rendering a risk as a grid.
 * 
 * @TODO Printing
 * 
 * @author Peter Kliem
 * @version $Id: RiskRenderer.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class RiskRenderer extends CellRendererBase implements ICellRenderer {
    /** offset fo rthe axis rendering. */
    public static final int AXISOFFSET = 0;
    private boolean _forceSquare = true;

    private Color _red;
    private Color _redInactive;
    private Color _green;
    private Color _greenInactive;
    private Color _yellow;
    private Color _yellowInactive;

    private Color _black;

    // private static final RGB INACTIVE_GREEN = new RGB(201, 255, 205);
    // private static final RGB INACTIVE_YELLOW = new RGB(255, 255, 200);
    // private static final RGB INACTIVE_RED = new RGB(255, 201, 205);
    private static final RGB INACTIVE_GREEN = new RGB(220, 255, 220);
    private static final RGB INACTIVE_YELLOW = new RGB(255, 255, 220);
    private static final RGB INACTIVE_RED = new RGB(255, 220, 220);

    private Color[][] _inactiveColors;
    private Color[][] _colors;

    public RiskRenderer(Printer printer) {
        super(printer);
        if (printer != null) {
            _yellow = printer.getSystemColor(SWT.COLOR_YELLOW);
            _green = printer.getSystemColor(SWT.COLOR_GREEN);
            _red = printer.getSystemColor(SWT.COLOR_RED);
            _black = printer.getSystemColor(SWT.COLOR_BLACK);
            _redInactive = new Color(printer, INACTIVE_RED);
            _yellowInactive = new Color(printer, INACTIVE_YELLOW);
            _greenInactive = new Color(printer, INACTIVE_GREEN);
        }
        Color[][] inactiveColors = { { _yellowInactive, _redInactive, _redInactive },
                { _greenInactive, _yellowInactive, _redInactive }, { _greenInactive, _greenInactive, _yellowInactive } };
        _inactiveColors = inactiveColors;
        Color[][] colors = { { _yellow, _red, _red }, { _green, _yellow, _red }, { _green, _green, _yellow } };
        _colors = colors;
    }

    public RiskRenderer() {
        super(null);
        _yellow = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
        _green = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
        _red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
        _black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
        _redInactive = new Color(Display.getCurrent(), INACTIVE_RED);
        _yellowInactive = new Color(Display.getCurrent(), INACTIVE_YELLOW);
        _greenInactive = new Color(Display.getCurrent(), INACTIVE_GREEN);
        _black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
        Color[][] inactiveColors = { { _yellowInactive, _redInactive, _redInactive },
                { _greenInactive, _yellowInactive, _redInactive }, { _greenInactive, _greenInactive, _yellowInactive } };
        _inactiveColors = inactiveColors;
        Color[][] colors = { { _yellow, _red, _red }, { _green, _yellow, _red }, { _green, _green, _yellow } };
        _colors = colors;
    }

    public void draw(GC gc, JaretTable jaretTable, ICellStyle cellStyle, Rectangle drawingArea, IRow row,
            IColumn column, boolean drawFocus, boolean selected, boolean printing) {
        drawBackground(gc, drawingArea, cellStyle, selected, printing);
        Rectangle drect = drawBorder(gc, cellStyle, drawingArea, printing);
        Rectangle rect = applyInsets(drect);
        DummyRow.Risk risk = (Risk) column.getValue(row);
        if (_forceSquare) {
            int a = Math.min(rect.width, rect.height);
            Rectangle nrect = new Rectangle(0, 0, a, a);
            nrect.x = rect.x + (rect.width - a) / 2;
            nrect.y = rect.y + (rect.height - a) / 2;
            rect = nrect;
        }

        Color bg = gc.getBackground();

        int width = rect.width;
        int height = rect.height;

        int sWidth = (width - AXISOFFSET) / 3;
        int sHeight = (height - AXISOFFSET) / 3;

        // x = prob
        // y = severity
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                Color c = getColor(risk, x, y);
                gc.setBackground(c);
                int oX = AXISOFFSET + x * sWidth + rect.x;
                int oY = rect.y + height - AXISOFFSET - (y + 1) * sHeight;
                gc.fillRectangle(oX, oY, sWidth, sHeight);
                gc.drawRectangle(oX, oY, sWidth, sHeight);
                if (risk != null && risk.getRiskProb() - 1 == x && risk.getRiskSeverity() - 1 == y) {
                    gc.drawLine(oX, oY, oX + sWidth, oY + sHeight);
                    gc.drawLine(oX, oY + sHeight, oX + sWidth, oY);
                }
            }
        }

        // // axises
        // // x
        // gc.drawLine(rect.x + AXISOFFSET, rect.y + height - AXISOFFSET, rect.x + 3 * sWidth + AXISOFFSET, rect.y +
        // height - AXISOFFSET);
        // // y
        // gc.drawLine(rect.x + AXISOFFSET, rect.y + height - AXISOFFSET, rect.x + AXISOFFSET, rect.y + height -
        // AXISOFFSET - 3 * sHeight);

        gc.setBackground(bg);

        if (drawFocus) {
            drawFocus(gc, drect);
        }
        drawSelection(gc, drawingArea, cellStyle, selected, printing);

    }

    private Color getColor(Risk risk, int prob, int sev) {
        if (risk != null && risk.getRiskProb() - 1 == prob && risk.getRiskSeverity() - 1 == sev) {
            return _colors[2 - prob][sev];
        } else {
            return _inactiveColors[2 - prob][sev];
        }
    }

    public void dispose() {
        if (_yellowInactive != null) {
            _yellowInactive.dispose();
        }
        if (_redInactive != null) {
            _redInactive.dispose();
        }
        if (_greenInactive != null) {
            _greenInactive.dispose();
        }
    }

    public ICellRenderer createPrintRenderer(Printer printer) {
        return new RiskRenderer(printer);
    }
}