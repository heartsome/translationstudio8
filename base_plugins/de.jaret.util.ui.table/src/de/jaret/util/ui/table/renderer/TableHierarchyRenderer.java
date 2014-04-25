/*
 *  File: TableHierarchyRenderer.java 
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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IHierarchicalTableViewState;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.model.ITableNode;
import de.jaret.util.ui.table.model.StdHierarchicalTableModel;

/**
 * A renderer for rendering the hierarchy (as a tree) of a hierarchical tree model.
 * 
 * @author Peter Kliem
 * @version $Id: TableHierarchyRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class TableHierarchyRenderer extends CellRendererBase implements IHierarchyRenderer {
    /** size of the plus/minus signs. */
    protected int SIZE = 12;

    protected int SIGNINSETS = 3;

    protected boolean _drawTree = true;

    protected int _levelWidth = 30;

    protected boolean _drawIcons = false;

    protected boolean _drawLabels = false;

    protected ILabelProvider _labelProvider = null;

    /** type of nodemarks to draw: 0 none, 1 +/-, 2 triangles. */
    protected int _nodeMarkType = 2;

    /**
     * Create the renderer for a printer device.
     * @param printer printer device
     */
    public TableHierarchyRenderer(Printer printer) {
        super(printer);
        SIZE = scaleX(SIZE);
        SIGNINSETS = scaleX(SIGNINSETS);
    }

    /**
     * Create the renderer for use with the display.
     */
    public TableHierarchyRenderer() {
        super(null);
    }

    /**
     * {@inheritDoc}
     */
    public void draw(GC gc, JaretTable jaretTable, ICellStyle cellStyle, Rectangle drawingArea, IRow row,
            IColumn column, boolean drawFocus, boolean selected, boolean printing) {
        int offx;
        offx = scaleX(_levelWidth);
        ITableNode node = (ITableNode) row;
        int level = node.getLevel();
        boolean leaf = node.getChildren().size() == 0;
        boolean expanded = ((IHierarchicalTableViewState) jaretTable.getTableViewState()).isExpanded(node);

        int x = drawingArea.x + offx * level + SIZE / 2;

        int y = drawingArea.y + (drawingArea.height - SIZE) / 2;

        if (leaf && !_drawIcons) {
            drawLeaf(gc, SIZE, x, y);
        } else if (expanded && !leaf) {
            if (_nodeMarkType == 1) {
                drawMinus(gc, SIZE, x, y);
            } else if (_nodeMarkType == 2) {
                drawTriangleDown(gc, SIZE, x, y);
            }
        } else if (!leaf) {
            if (_nodeMarkType == 1) {
                drawPlus(gc, SIZE, x, y);
            } else if (_nodeMarkType == 2) {
                drawTriangleRight(gc, SIZE, x, y);
            }
        }
        if (_nodeMarkType != 0) {
            x += SIZE + 4;
        }

        // default for drawing selection
        Rectangle labelrect = drawingArea;

        if (_labelProvider != null && (_drawIcons || _drawLabels)) {
            int labelx = x;
            labelrect = new Rectangle(x, y, 0, 0);
            if (_drawIcons) {
                Image img = _labelProvider.getImage(row);
                if (img != null) {
                    if (!printing) {
                        gc.drawImage(img, x, y);
                        labelx += img.getBounds().width;
                        labelrect.width += img.getBounds().width;
                        labelrect.height = img.getBounds().height;
                    } else {
                        gc.drawImage(img, 0, 0, img.getBounds().width, img.getBounds().height, x, y, scaleX(img
                                .getBounds().width), scaleY(img.getBounds().height));
                        labelx += scaleX(img.getBounds().width);
                        labelrect.width += scaleX(img.getBounds().width);
                        labelrect.height = scaleY(img.getBounds().height);
                    }
                }
            }
            if (_drawLabels) {
                String label = _labelProvider.getText(row);
                if (label != null) {
                    gc.drawString(label, labelx, y);

                    Point extent = gc.stringExtent(label);
                    labelrect.width += extent.x;
                    labelrect.height = Math.max(labelrect.height, extent.y);
                }
            }
        }

        // draw tree connections
        if (_drawTree) {
            // TimeBarNode node = (TimeBarNode) row;
            if (printing) {
                gc.setLineWidth(3);
            }
            gc.setLineStyle(SWT.LINE_DOT);
            int midy = drawingArea.y + ((drawingArea.height - SIZE) / 2) + SIZE / 2;
            int icoy = drawingArea.y + ((drawingArea.height - SIZE) / 2) + SIZE;
            int icox = drawingArea.x + offx * (level) + SIZE - SIZE / 2;
            int midx = drawingArea.x + +offx * (level) + SIZE;
            int beginx = drawingArea.x + offx * (level - 1) + SIZE;
            int endx = drawingArea.x + offx * (level + 1) + SIZE;

            // connection
            gc.drawLine(beginx, midy, icox, midy);

            // uplink
            gc.drawLine(beginx, drawingArea.y, beginx, midy);

            // downlink
            if ((!leaf && expanded)) {
                gc.drawLine(midx, icoy, midx, drawingArea.y + drawingArea.height);
            }

            boolean hasMoreSiblings = true;
            if (jaretTable.getTableModel() instanceof StdHierarchicalTableModel) {
                StdHierarchicalTableModel model = (StdHierarchicalTableModel) jaretTable.getTableModel();
                hasMoreSiblings = model.moreSiblings(node, node.getLevel());
            }

            // // downlink on begin
            // // if has more siblings
            // if (hasMoreSiblings) {
            // // gc.drawLine(beginx, icoy, beginx,
            // drawingArea.y+drawingArea.height);
            // }

            // level lines
            if (jaretTable.getTableModel() instanceof StdHierarchicalTableModel) {
                StdHierarchicalTableModel model = (StdHierarchicalTableModel) jaretTable.getTableModel();
                for (int i = 0; i < level; i++) {
                    if (model.moreSiblings(node, i)) {
                        x = drawingArea.x + offx * i + SIZE;
                        gc.drawLine(x, drawingArea.y, x, drawingArea.y + drawingArea.height);
                    }
                }
            }

            gc.setLineStyle(SWT.LINE_SOLID);
            gc.setLineWidth(1);
        }
        if (drawFocus) {
            drawFocus(gc, labelrect);
        }
        drawSelection(gc, labelrect, cellStyle, selected, printing);

    }

    protected void drawPlus(GC gc, int size, int x, int y) {
        gc.drawLine(x + SIGNINSETS, y + size / 2, x + size - SIGNINSETS, y + size / 2);
        gc.drawLine(x + size / 2, y + SIGNINSETS, x + size / 2, y + size - SIGNINSETS);
        gc.drawRectangle(x, y, size, size);
    }

    protected void drawMinus(GC gc, int size, int x, int y) {
        gc.drawLine(x + SIGNINSETS, y + size / 2, x + size - SIGNINSETS, y + size / 2);
        gc.drawRectangle(x, y, size, size);
    }

    protected void drawTriangleDown(GC gc, int size, int x, int y) {
        Color bg = gc.getBackground();
        gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
        int[] pArray = new int[] { x, y, x + size, y, x + size / 2, y + size - 3 };
        gc.fillPolygon(pArray);
        gc.setBackground(bg);
    }

    protected void drawTriangleRight(GC gc, int size, int x, int y) {
        Color bg = gc.getBackground();
        gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
        int[] pArray = new int[] { x, y, x + size - 3, y + size / 2, x, y + size };
        gc.fillPolygon(pArray);
        gc.setBackground(bg);
    }

    protected void drawLeaf(GC gc, int size, int x, int y) {
        Color bg = gc.getBackground();
        gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
        gc.fillOval(x + size / 2, y + size / 2, size / 2, size / 2);
        gc.setBackground(bg);
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(Rectangle drawingArea, int x, int y) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int getPreferredWidth() {
        return scaleX(SIZE + 4);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        if (_labelProvider != null) {
            _labelProvider.dispose();
        }
    }

    /**
     * @return Returns the labelProvider.
     */
    public ILabelProvider getLabelProvider() {
        return _labelProvider;
    }

    /**
     * @param labelProvider The labelProvider to set.
     */
    public void setLabelProvider(ILabelProvider labelProvider) {
        _labelProvider = labelProvider;
    }

    /**
     * @return Returns the levelWidth.
     */
    public int getLevelWidth() {
        return _levelWidth;
    }

    /**
     * @param levelWidth The levelWidth to set.
     */
    public void setLevelWidth(int levelWidth) {
        _levelWidth = levelWidth;
    }

    /**
     * @return Returns the drawIcons.
     */
    public boolean getDrawIcons() {
        return _drawIcons;
    }

    /**
     * @param drawIcons The drawIcons to set.
     */
    public void setDrawIcons(boolean drawIcons) {
        this._drawIcons = drawIcons;
    }

    /**
     * @return Returns the drawLabels.
     */
    public boolean getDrawLabels() {
        return _drawLabels;
    }

    /**
     * @param drawLabels The drawLabels to set.
     */
    public void setDrawLabels(boolean drawLabels) {
        this._drawLabels = drawLabels;
    }

    /**
     * {@inheritDoc}
     */
    public ICellRenderer createPrintRenderer(Printer printer) {
        TableHierarchyRenderer r = new TableHierarchyRenderer(printer);
        r.setDrawIcons(_drawIcons);
        r.setDrawLabels(_drawLabels);
        r.setLevelWidth(_levelWidth);
        r.setLabelProvider(_labelProvider);
        return r;
    }

    /**
     * {@inheritDoc}
     */
    public int getPreferredWidth(List<IRow> rows, IColumn column) {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    public int getPreferredHeight(IRow row, IColumn column) {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInActiveArea(IRow row, Rectangle drawingArea, int xx, int yy) {
        int offx = scaleX(_levelWidth);
        ITableNode node = (ITableNode) row;
        int level = node.getLevel();
        boolean leaf = node.getChildren().size() == 0;
        // leaves can not be toggled
        if (leaf) {
            return false;
        }

        int x = drawingArea.x + offx * level + SIZE / 2;
        int y = drawingArea.y + (drawingArea.height - SIZE) / 2;

        return x <= xx && xx <= x + SIZE && y <= yy && yy <= y + SIZE;
    }
}
