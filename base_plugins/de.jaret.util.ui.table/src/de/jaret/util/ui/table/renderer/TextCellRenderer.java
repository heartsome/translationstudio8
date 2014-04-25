/*
 *  File: TextCellRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.swt.TextRenderer;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.model.ITableViewState;

/**
 * TextCellRenderer for the jaret table. Features an integrated comment marker (tooltip), Override getComment() to use
 * this. This CellRenderer may be used as the basis for a lot of toText-CellRenderers (see the DateCellRenderer)
 * 
 * @author Peter Kliem
 * @version $Id: TextCellRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class TextCellRenderer extends CellRendererBase implements ICellRenderer {
    /** size of the comment arker. */
    private static final int COMMENTMARKER_SIZE = 5;
    
    /** color of the comment marker. */
    protected Color _commentColor;

    /**
     * Create a text cell renderer for printing.
     * 
     * @param printer printer device
     */
    public TextCellRenderer(Printer printer) {
        super(printer);
    }

    /**
     * Create a text cell renderer for display.
     */
    public TextCellRenderer() {
        super(null);
        _commentColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
    }

    /**
     * {@inheritDoc}
     */
    public String getTooltip(JaretTable jaretTable, Rectangle drawingArea, IRow row, IColumn column, int x, int y) {
        if (getComment(row, column) != null && isInCommentMarkerArea(drawingArea, COMMENTMARKER_SIZE, x, y)) {
            return getComment(row, column);
        }
        return null;
    }

    /**
     * Convert the value specified by row, column to a string. This method is ideally suited to be overidden by
     * extensions of the textcellrenderer.
     * 
     * @param row row of the cell
     * @param column column of the cell
     * @return String for the value
     */
    protected String convertValue(IRow row, IColumn column) {
        Object value = column.getValue(row);
        return value != null ? value.toString() : null;
    }

    /**
     * Override for using content marker and tooltip.
     * 
     * @param row row of the cell
     * @param column column of the cell
     * @return comment as String or <code>null</code>
     */
    protected String getComment(IRow row, IColumn column) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void draw(GC gc, JaretTable jaretTable, ICellStyle cellStyle, Rectangle drawingArea, IRow row,
            IColumn column, boolean drawFocus, boolean selected, boolean printing) {
        
        drawBackground(gc, drawingArea, cellStyle, selected, printing);
        Rectangle drect = drawBorder(gc, cellStyle, drawingArea, printing);
        Rectangle rect = applyInsets(drect);

        // convert the value to a string
        String s = convertValue(row, column);

        Color fg = gc.getForeground();
        Color bg = gc.getBackground();
        Font font = gc.getFont();

       

        // draw comment marker if comment is present and not printing
        if (!printing && getComment(row, column) != null) {
            drawCommentMarker(gc, drawingArea, _commentColor, COMMENTMARKER_SIZE);
        }

        if (drawFocus) {
            drawFocus(gc, drect);
        }
        drawSelection(gc, drawingArea, cellStyle, selected, printing);

        gc.setForeground(fg);
        gc.setBackground(bg);
        gc.setFont(font);
        if (s != null) {
            if (selected && !printing) {
                gc.setBackground(SELECTIONCOLOR);
            } else {
                gc.setBackground(getBackgroundColor(cellStyle, printing));
            }
            gc.setForeground(getForegroundColor(cellStyle, printing));
            gc.setFont(getFont(cellStyle, printing, gc.getFont()));

            drawCellString(gc, rect, s, cellStyle);
            if (s.indexOf("we") != -1) {
                TextLayout textLayout = new TextLayout(gc.getDevice());
                textLayout.setText(s);
                textLayout.setFont(gc.getFont());
                textLayout.setWidth(rect.width);
                Color color = new Color(gc.getDevice(), 150, 100, 100);
        		Font font2 = new Font(gc.getDevice(), gc.getFont().getFontData()[0].getName(), gc.getFont()
        				.getFontData()[0].getHeight(), SWT.ITALIC);
        		TextStyle style = new TextStyle(font2, color, null);
        		for (int i = 1; i < s.length(); i++) {
        			int j = indexOf(s, "we", i, false);
        			if (j != -1) {
        				textLayout.setStyle(style, j, j + 3);
        			} else {
        				break;
        			}

        		}
        		gc.fillRectangle(rect);
        		textLayout.draw(gc, rect.x, rect.y);
        		gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                }
        }
    }
    
    public int indexOf(String srcStr, String str, int index, boolean isCaseSensitive) {
		if (index == 0) {
			return -1;
		}
		if (index == 1) {
			if (isCaseSensitive) {
				return srcStr.indexOf(str);
			} else {
				return srcStr.toUpperCase().indexOf(str.toUpperCase());
			}
		}
		if (isCaseSensitive) {
			return srcStr.indexOf(str, indexOf(srcStr, str, index - 1, isCaseSensitive) + str.length());
		} else {
			return srcStr.toUpperCase().indexOf(str.toUpperCase(),
					indexOf(srcStr, str, index - 1, isCaseSensitive) + str.length());
		}
	}
    /**
     * Draw the string.
     * 
     * @param gc gc
     * @param rect drawing area
     * @param s String to drw
     * @param cellStyle the cell style
     */
    private void drawCellString(GC gc, Rectangle rect, String s, ICellStyle cellStyle) {
        if (cellStyle.getMultiLine()) {
            drawCellStringMulti(gc, rect, s, cellStyle);
        } else {
            drawCellStringSingle(gc, rect, s);
        }
    }

    /**
     * Draw single line String.
     * 
     * @param gc gc
     * @param rect drawing area
     * @param s String to draw
     */
    private void drawCellStringSingle(GC gc, Rectangle rect, String s) {
        gc.drawString(s, rect.x, rect.y + 10, true);
    }

    /**
     * Draw a String in the drawing area, splitting it into multiple lines.
     * 
     * @param gc gc
     * @param rect drawing area
     * @param s String to draw
     * @param cellStyle cell style determing alignment
     */
    private void drawCellStringMulti(GC gc, Rectangle rect, String s, ICellStyle cellStyle) {
        int halign = TextRenderer.LEFT;
        if (cellStyle.getHorizontalAlignment() == ITableViewState.HAlignment.RIGHT) {
            halign = TextRenderer.RIGHT;
        } else if (cellStyle.getHorizontalAlignment() == ITableViewState.HAlignment.CENTER) {
            halign = TextRenderer.CENTER;
        }
        int valign = TextRenderer.TOP;
        if (cellStyle.getVerticalAlignment() == ITableViewState.VAlignment.BOTTOM) {
            valign = TextRenderer.BOTTOM;
        } else if (cellStyle.getVerticalAlignment() == ITableViewState.VAlignment.CENTER) {
            valign = TextRenderer.CENTER;
        }

        TextRenderer.renderText(gc, rect, true, false, s, halign, valign);
    }

    /**
     * {@inheritDoc}
     */
    public int getPreferredHeight(GC gc, ICellStyle cellStyle, int width, IRow row, IColumn column) {
        Object value = convertValue(row, column);
        Font font = gc.getFont();
        int height = -1;
        if (value != null) {
            String s = value.toString();
            gc.setFont(getFont(cellStyle, false, gc.getFont()));
            height = TextRenderer.getHeight(gc, getInnerWidth(width, cellStyle), true, s);
        }
        gc.setFont(font);
        return height + getVerticalSpacesSum(cellStyle);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        // nothing to dispose
    }

    /**
     * {@inheritDoc}
     */
    public ICellRenderer createPrintRenderer(Printer printer) {
        return new TextCellRenderer(printer);
    }

}
