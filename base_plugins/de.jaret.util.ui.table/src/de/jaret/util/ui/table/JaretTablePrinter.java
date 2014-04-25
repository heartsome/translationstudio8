/*
 *  File: JaretTablePrinter.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;

import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.print.JaretTablePrintConfiguration;
import de.jaret.util.ui.table.renderer.ICellRenderer;
import de.jaret.util.ui.table.renderer.ICellStyle;
import de.jaret.util.ui.table.renderer.ITableHeaderRenderer;

/**
 * <p>
 * Print utility for the jaret table. The table printer depends on implemented print functionality in the configured
 * renderers. It "connects" directly to the jaret table thus no headless printing is possible.
 * </p>
 * <p>
 * The Table printer should be disposed after using!
 * </p>
 * TODO this is a first hack.
 * 
 * @author Peter Kliem
 * @version $Id: JaretTablePrinter.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class JaretTablePrinter {
    /** borders in cm. */
    protected double _borderTop = 1;

    protected double _borderBottom = 1;

    protected double _borderLeft = 1;

    protected double _borderRight = 1;

    protected Rectangle _printingRect;

    protected Printer _printer;

    protected double _scaleX;

    protected double _scaleY;

    protected ITableHeaderRenderer _headerRenderer;

    protected JaretTable _table;

    protected int _pageHeight;

    protected int _pageWidth;

    protected int _footerHeight;

    protected double _scale = 1.0;

    public JaretTablePrinter(Printer printer, JaretTable table) {
        _table = table;
        setPrinter(printer);
    }

    public void setPrinter(Printer printer) {
        if (_printer != null) {
            _printer.dispose();
        }
        _printer = printer;
        if (printer != null) {
            Point dpi = _printer.getDPI();
            _scaleX = (double) dpi.x / 96.0;
            _scaleY = (double) dpi.y / 96.0;
        }

    }

    public int scaleX(int in) {
        return (int) Math.round(_scaleX * (double) in * _scale);
    }

    public double getScaleX() {
        return _scaleX;
    }

    public int scaleY(int in) {
        return (int) Math.round(_scaleY * (double) in * _scale);
    }

    public Printer getPrinter() {
        return _printer;
    }

    protected int pixelForCmX(double cm) {
        Point dpi = _printer.getDPI();
        double inch = cm / 2.54;
        return (int) (dpi.x * inch);
    }

    protected int pixelForCmY(double cm) {
        Point dpi = _printer.getDPI();
        double inch = cm / 2.54;
        return (int) (dpi.y * inch);
    }

    /**
     * Calculate the number of pages generated when printing.
     * 
     * @param configuration
     * @return
     */
    public Point calculatePageCount(JaretTablePrintConfiguration configuration) {
        _scale = configuration.getScale();
        _pageHeight = _printer.getClientArea().height - pixelForCmY(_borderTop + _borderBottom);
        _pageWidth = _printer.getClientArea().width - pixelForCmX(_borderLeft + _borderRight);

        int tHeight;
        if (configuration.getRowLimit() == -1) {
            tHeight = _table.getTotalHeight();
        } else {
            tHeight = _table.getTotalHeight(configuration.getRowLimit());
        }
        int tWidth;
        if (configuration.getColLimit() == -1) {
            tWidth = _table.getTotalWidth();
        } else {
            tWidth = _table.getTotalWidth(configuration.getColLimit());
        }

        int pagesx = (scaleX(tWidth) / _pageWidth) + 1;
        int pagesy = (scaleY(tHeight) / (_pageHeight - _footerHeight)) + 1;

        int headerheight = _table.getDrawHeader() ? _table.getHeaderHeight() : 0;
        headerheight = configuration.getRepeatHeader() ? headerheight + headerheight * (pagesy - 1) : headerheight;

        // corrected pagesy
        pagesy = (scaleY(tHeight + headerheight) / (_pageHeight - _footerHeight)) + 1;

        return new Point(pagesx, pagesy);

    }

    public void print(JaretTablePrintConfiguration configuration) {
        _printingRect = new Rectangle(pixelForCmX(_borderLeft), pixelForCmY(_borderTop), _pageWidth, _pageHeight);

        _headerRenderer = _table.getHeaderRenderer().getPrintRenderer(_printer);
        Point pages = calculatePageCount(configuration);
        int pagesx = pages.x;
        int pagesy = pages.y;

        _printer.startJob(configuration.getName() != null ? configuration.getName() : "jarettable");

        GC gc = new GC(_printer);
        Font oldfont = gc.getFont();
        FontData fontdata = new FontData("Arial", (int) (8.0 * _scale), SWT.NULL);
        Font printerFont = new Font(_printer, fontdata);
        gc.setFont(printerFont);

        for (int px = 0; px < pagesx; px++) {
            int startx = (int) ((px * _pageWidth) / (_scaleX * _scale));
            IColumn column = _table.getColumnForAbsX(startx);
            int offx = startx - _table.getAbsBeginXForColumn(column);
            int beginColIdx = _table.getColIdxForAbsX(startx);
            // System.out.println("PX "+px+" startx "+startx+" offx "+offx+" beginColIdx "+beginColIdx);
            int rIdx = 0;
            for (int py = 0; py < pagesy; py++) {
                int y = 0;
                String footerText = configuration.getFooterText() != null ? configuration.getFooterText() : "";
                footerText += "(" + (px + 1) + "/" + pagesx + "," + (py + 1) + "/" + pagesy + ")";
                _printer.startPage();

                int starty = (int) (py * ((_pageHeight - _footerHeight - (configuration.getRepeatHeader() ? scaleY(_table
                        .getHeaderHeight())
                        : 0)) / (_scaleY * _scale)));
                rIdx = py == 0 ? 0 : rIdx;// _table.getRowIdxForAbsY(starty);
                Rectangle clipSave = gc.getClipping();

                if (starty == 0 || configuration.getRepeatHeader()) {
                    // draw header
                    // draw headers table area
                    int x = -offx;
                    int cIdx = beginColIdx;
                    while (scaleX(x) < _pageWidth && cIdx < _table.getColumnCount()
                            && (configuration.getColLimit() == -1 || cIdx <= configuration.getColLimit())) {
                        IColumn col = _table.getColumn(cIdx);
                        int colwidth = _table.getTableViewState().getColumnWidth(col);
                        int xx = x > 0 ? x : 0;
                        int clipWidth = x > 0 ? colwidth : colwidth - offx;
                        if (!_headerRenderer.disableClipping()) {
                            gc.setClipping(scaleX(xx) + pixelForCmX(_borderLeft), pixelForCmY(_borderTop),
                                    scaleX(clipWidth), scaleY(_table.getHeaderHeight()));
                            gc.setClipping(gc.getClipping().intersection(_printingRect));
                        }

                        drawHeader(gc, scaleX(x) + pixelForCmX(_borderLeft), scaleX(colwidth), col);

                        x += colwidth;
                        cIdx++;
                    }
                    y += _table.getHeaderHeight();
                    gc.setClipping(clipSave);
                }

                // normal table area

                gc.setClipping(_printingRect);

                while (scaleY(y) < _pageHeight && rIdx < _table.getRowCount()
                        && (configuration.getRowLimit() == -1 || rIdx <= configuration.getRowLimit())) {
                    IRow row = _table.getRow(rIdx);
                    int rHeight = _table.getTableViewState().getRowHeight(row);
                    // do not draw a row that does not fit on th page
                    if (scaleY(y) + scaleY(rHeight) > _pageHeight) {
                        break;
                    }
                    int x = -offx;
                    int cIdx = beginColIdx;
                    while (scaleX(x) < _pageWidth && cIdx < _table.getColumnCount()
                            && (configuration.getColLimit() == -1 || cIdx <= configuration.getColLimit())) {
                        IColumn col = _table.getColumn(cIdx);
                        int colwidth = _table.getTableViewState().getColumnWidth(col);
                        Rectangle area = new Rectangle(scaleX(x) + pixelForCmX(_borderLeft), scaleY(y)
                                + pixelForCmY(_borderTop), scaleX(colwidth), scaleY(rHeight));
                        drawCell(gc, area, row, col);
                        x += colwidth;
                        cIdx++;
                    }
                    y += rHeight;
                    rIdx++;
                }

                gc.setClipping(clipSave);
                drawFooter(gc, footerText);
                _printer.endPage();
            }
        }
        _printer.endJob();
        printerFont.dispose();
        gc.setFont(oldfont);
        gc.dispose();
    }

    /**
     * TODO creation and disposal of the cell renderers for printing is ... well should be changed!
     * 
     * @param gc
     * @param area
     * @param row
     * @param col
     */
    private void drawCell(GC gc, Rectangle area, IRow row, IColumn col) {
        ICellStyle bc = _table.getTableViewState().getCellStyle(row, col);
        ICellRenderer cellRenderer = _table.getCellRenderer(row, col).createPrintRenderer(_printer);
        if (cellRenderer != null) {
            cellRenderer.draw(gc, _table, bc, area, row, col, false, false, true);
        }
        cellRenderer.dispose();
    }

    private void drawFooter(GC gc, String footer) {
        Point extent = gc.textExtent(footer);
        int y = _printer.getClientArea().height - _footerHeight - pixelForCmY(_borderBottom);
        // gc.drawLine(0,y,_pageWidth, y);
        // gc.drawLine(0,y+extent.y,_pageWidth, y+extent.y);
        gc.drawString(footer, pixelForCmX(_borderLeft), y);

    }

    private void drawHeader(GC gc, int x, int colwidth, IColumn col) {
        Rectangle area = new Rectangle(x, pixelForCmY(_borderTop), colwidth, scaleY(_table.getHeaderHeight()));
        int sortingPos = _table.getTableViewState().getColumnSortingPosition(col);
        boolean sortingDir = _table.getTableViewState().getColumnSortingDirection(col);
        _headerRenderer.draw(gc, area, col, sortingPos, sortingDir, true);
    }

    public void dispose() {
        if (_headerRenderer != null) {
            _headerRenderer.dispose();
        }
    }
}
