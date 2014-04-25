/*
 *  File: SmileyCellRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Fun renderer rendering an integer as a smiley.
 * 
 * @TODO Printing
 * 
 * @author Peter Kliem
 * @version $Id: SmileyCellRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class SmileyCellRenderer extends CellRendererBase implements ICellRenderer {
    private BoundedRangeModel _brModel = new DefaultBoundedRangeModel(50, 0, 0, 100);
    private boolean _eyeBrows = true;
    private boolean _colorChange = true;
    /** if true, rendering will be alwa limited to a maximal square, forcing the smiley to be a circle. */
    private boolean _forceCircle = true;

    private Color _neutral;
    private Color _positive;
    private Color _negative;
    private Color _currentColor;
    private Color _black;

    private double _currentValue;

    public SmileyCellRenderer(Printer printer) {
        super(printer);
        if (printer != null) {
            _neutral = printer.getSystemColor(SWT.COLOR_YELLOW);
            _positive = printer.getSystemColor(SWT.COLOR_GREEN);
            _negative = printer.getSystemColor(SWT.COLOR_RED);
            _black = printer.getSystemColor(SWT.COLOR_BLACK);

        }
    }

    public SmileyCellRenderer() {
        super(null);
        _neutral = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
        _positive = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
        _negative = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
        _black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
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
        if (value != null) {
            saveGCAttributes(gc);
            int val = ((Integer) value).intValue();
            _brModel.setValue(val);
            calcSmileFactor();

            if (_forceCircle) {
                int a = Math.min(rect.width, rect.height);
                Rectangle nrect = new Rectangle(0, 0, a, a);
                nrect.x = rect.x + (rect.width - a) / 2;
                nrect.y = rect.y + (rect.height - a) / 2;
                rect = nrect;
            }

            int width = rect.width;
            int height = rect.height;
            int offx = rect.x;
            int offy = rect.y;
            float foffx = (float) offx;
            float foffy = (float) offy;

            int lineWidth = height / 40;
            if (!_colorChange) {
                gc.setBackground(_neutral);
            } else {
                if (_currentValue >= 0) { // positive
                    gc.setBackground(calcColor(_positive, printing));
                } else { // negative
                    gc.setBackground(calcColor(_negative, printing));
                }
            }

            Device device = printing ? _printer : Display.getCurrent();
            Path p = new Path(device);
            p.addArc(foffx + 0 + lineWidth / 2, foffy + 0 + lineWidth / 2, width - 1 - lineWidth, height - 1
                    - lineWidth, 0, 360);
            gc.fillPath(p);
            gc.setForeground(_black);
            gc.setLineWidth(lineWidth);
            gc.drawPath(p);
            p.dispose();
            // eyes
            int y = height / 3;
            int x1 = width / 3;
            int x2 = width - width / 3;
            int r = width / 30;
            // eyes have a minimal size
            if (r == 0) {
                r = 1;
            }
            gc.setBackground(_black);
            gc.fillOval(offx + x1 - r, offy + y - r, 2 * r, 2 * r);
            gc.fillOval(offx + x2 - r, offy + y - r, 2 * r, 2 * r);
            // eye brows
            if (_eyeBrows) {
                gc.setLineWidth(lineWidth / 2);
                int ebWidth = width / 10;
                int yDist = height / 13;
                int yOff = (int) (_currentValue * (double) height / 30);
                int xShift = (int) (_currentValue * (double) width / 90);
                p = new Path(device);
                p.moveTo(foffx + x1 - ebWidth / 2 + xShift, foffy + y - yDist + yOff);
                p.lineTo(foffx + x1 + ebWidth / 2 - xShift, foffy + y - yDist - yOff);
                gc.drawPath(p);
                p.dispose();

                p = new Path(device);
                p.moveTo(foffx + x2 - ebWidth / 2 + xShift, foffy + y - yDist - yOff);
                p.lineTo(foffx + x2 + ebWidth / 2 - xShift, foffy + y - yDist + yOff);
                gc.drawPath(p);
                p.dispose();
            }
            // mouth
            gc.setLineWidth(lineWidth);
            x1 = (int) (width / 4.5);
            x2 = width - x1;
            y = height - height / 3;
            int midX = width / 2;
            int offset = (int) (_currentValue * (double) height / 3);
            p = new Path(Display.getCurrent());
            p.moveTo(foffx + x1, foffy + y);
            p.quadTo(foffx + midX, foffy + y + offset, foffx + x2, foffy + y);
            gc.drawPath(p);
            p.dispose();
            restoreGCAttributes(gc);
        }
        if (drawFocus) {
            drawFocus(gc, drect);
        }
        drawSelection(gc, drawingArea, cellStyle, selected, printing);

    }

    /**
     * Scales the BoundedRangeModel to [-1, 1]
     */
    private void calcSmileFactor() {
        int range = _brModel.getMaximum() - _brModel.getMinimum();
        int mid = _brModel.getMinimum() + range / 2;
        int value = _brModel.getValue();
        _currentValue = (double) (value - mid) / (double) (range / 2);
        // due to rounding errors the smileFactor may be over 1
        if (_currentValue > 1) {
            _currentValue = 1;
        } else if (_currentValue < -1) {
            _currentValue = -1;
        }
    }

    /**
     * Calculates the color beetween _neutral and the specified color
     * 
     * @param destColor
     * @return the mixed color
     */
    private Color calcColor(Color destColor, boolean printing) {
        int rDiff = destColor.getRed() - _neutral.getRed();
        int gDiff = destColor.getGreen() - _neutral.getGreen();
        int bDiff = destColor.getBlue() - _neutral.getBlue();
        double factor = Math.abs(_currentValue);
        int r = (int) ((double) rDiff * factor);
        int g = (int) ((double) gDiff * factor);
        int b = (int) ((double) bDiff * factor);

        if (_currentColor != null) {
            _currentColor.dispose();
        }
        if (!printing) {
            _currentColor = new Color(Display.getCurrent(), _neutral.getRed() + r, _neutral.getGreen() + g, _neutral
                    .getBlue()
                    - b);
        } else {
            _currentColor = new Color(_printer, _neutral.getRed() + r, _neutral.getGreen() + g, _neutral.getBlue() - b);
        }
        return _currentColor;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        if (_currentColor != null) {
            _currentColor.dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    public ICellRenderer createPrintRenderer(Printer printer) {
        // return new SmileyCellRenderer(printer);
        // TODO there is a printing problem with using path on a printer, so use a text cell renderer instead
        return new TextCellRenderer(printer);
    }
}
