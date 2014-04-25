/*
 *  File: DefaultCellStyleProvider.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IJaretTableCell;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.model.ITableViewState;
import de.jaret.util.ui.table.model.JaretTableCellImpl;
import de.jaret.util.ui.table.model.ITableViewState.HAlignment;
import de.jaret.util.ui.table.model.ITableViewState.VAlignment;

/**
 * A Default implementation of a CellStyleProvider. It will register itself with every cell style as a property change
 * listener.
 * 
 * @author Peter Kliem
 * @version $Id: DefaultCellStyleProvider.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class DefaultCellStyleProvider implements ICellStyleProvider, PropertyChangeListener {
    /** map storing the row cell styles. */
    protected Map<IRow, ICellStyle> _rowMap = new HashMap<IRow, ICellStyle>();
    /** map storing the column cell styles. */
    protected Map<IColumn, ICellStyle> _columnMap = new HashMap<IColumn, ICellStyle>();
    /** map combintaion storing the style of a cell . */
    protected Map<IRow, Map<IColumn, ICellStyle>> _cellMap = new HashMap<IRow, Map<IColumn, ICellStyle>>();

    /** the listener list. */
    protected List<ICellStyleListener> _listeners;

    /** the default cell style. */
    protected ICellStyle _defaultCellStyle;
    /** the default cell style aligned right. */
    protected ICellStyle _defaultCellStyleAlignRight;
    /** style stategy. */
    protected IStyleStrategy _styleStrategy;

    /**
     * Constructor.
     */
    public DefaultCellStyleProvider() {
        IBorderConfiguration borderConf = new DefaultBorderConfiguration(1, 1, 1, 1);
        _defaultCellStyle = new DefaultCellStyle(null, null, borderConf, null);
        _defaultCellStyle.addPropertyChangeListener(this);
        _defaultCellStyleAlignRight = new DefaultCellStyle(null, null, borderConf, null);
        _defaultCellStyleAlignRight.setHorizontalAlignment(ITableViewState.HAlignment.RIGHT);
        _defaultCellStyleAlignRight.addPropertyChangeListener(this);
    }

    /**
     * {@inheritDoc} TODO include a strategy for priority row/column.
     */
    public ICellStyle getCellStyle(IRow row, IColumn column) {
        ICellStyle style = null;
        style = getCellSpecificStyle(row, column, false);
        if (style == null) {
            style = _rowMap.get(row);
        }
        if (style == null) {
            style = _columnMap.get(column);
        }
        if (style == null) {
            Class<?> clazz = column.getContentClass(row);
            if (clazz != null
                    && (clazz.equals(Double.class) || clazz.equals(Integer.class) || clazz.equals(Float.class)
                            || clazz.equals(Double.TYPE) || clazz.equals(Integer.TYPE) || clazz.equals(Float.TYPE))) {
                style = _defaultCellStyleAlignRight;
            } else {
                style = _defaultCellStyle;
            }
        }
        if (_styleStrategy != null) {
            style = _styleStrategy.getCellStyle(row, column, style, _defaultCellStyle);
        }

        return style;
    }

    /**
     * {@inheritDoc}
     */
    public void setRowCellStyle(IRow row, ICellStyle style) {
        ICellStyle old = _rowMap.get(row);
        if (old != null) {
            old.removePropertyChangeListener(this);
        }
        _rowMap.put(row, style);
        if (style != null) {
            style.addPropertyChangeListener(this);
        }
        fireCellStyleChanged(row, null, style);
    }

    /**
     * {@inheritDoc}
     */
    public ICellStyle getRowCellStyle(IRow row, boolean create) {
        ICellStyle style = null;
        style = _rowMap.get(row);
        if (style != null) {
            return style;
        }

        if (style == null && !create) {
            return _defaultCellStyle;
        } else {
            style = _defaultCellStyle.copy();
            setRowCellStyle(row, style);
            return style;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setColumnCellStyle(IColumn column, ICellStyle style) {
        ICellStyle old = _columnMap.get(column);
        if (old != null) {
            old.removePropertyChangeListener(this);
        }
        _columnMap.put(column, style);
        if (style != null) {
            style.addPropertyChangeListener(this);
        }
        fireCellStyleChanged(null, column, style);
    }

    /**
     * {@inheritDoc}
     */
    public ICellStyle getColumnCellStyle(IColumn column, boolean create) {
        ICellStyle style = null;
        style = _columnMap.get(column);
        if (style != null) {
            return style;
        }
        if (style == null && !create) {
            return _defaultCellStyle;
        } else {
            // System.out.println("creating");
            style = _defaultCellStyle.copy();
            setColumnCellStyle(column, style);
            return style;
        }
    }

    /**
     * {@inheritDoc}
     */
    public ICellStyle getCellSpecificStyle(IRow row, IColumn column, boolean create) {
        ICellStyle style = null;
        Map<IColumn, ICellStyle> cMap = _cellMap.get(row);
        if (cMap != null) {
            style = cMap.get(column);
        }
        if (style == null && create) {
            style = _defaultCellStyle.copy();
            setCellStyle(row, column, style);
        }
        return style;
    }

    /**
     * {@inheritDoc}
     */
    public void setCellStyle(IRow row, IColumn column, ICellStyle style) {
        ICellStyle oldStyle = getCellSpecificStyle(row, column, false);
        if (oldStyle != null) {
            oldStyle.removePropertyChangeListener(this);
        }
        Map<IColumn, ICellStyle> cMap = _cellMap.get(row);
        if (cMap == null) {
            cMap = new HashMap<IColumn, ICellStyle>();
            _cellMap.put(row, cMap);
        }
        cMap.put(column, style);
        style.addPropertyChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public ICellStyle getDefaultCellStyle() {
        return _defaultCellStyle;
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaultCellStyle(ICellStyle cellStyle) {
        _defaultCellStyle.removePropertyChangeListener(this);
        _defaultCellStyle = cellStyle;
        cellStyle.addPropertyChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void addCellStyleListener(ICellStyleListener csl) {
        if (_listeners == null) {
            _listeners = new ArrayList<ICellStyleListener>();
        }
        _listeners.add(csl);
    }

    /**
     * {@inheritDoc}
     */
    public void remCellStyleListener(ICellStyleListener csl) {
        if (_listeners != null) {
            _listeners.remove(csl);
        }
    }

    /**
     * Inform listeners about a cell style change.
     * 
     * @param row row affected
     * @param column olumn affected
     * @param cellStyle new style
     */
    protected void fireCellStyleChanged(IRow row, IColumn column, ICellStyle cellStyle) {
        if (_listeners != null) {
            for (ICellStyleListener listener : _listeners) {
                listener.cellStyleChanged(row, column, cellStyle);
            }
        }
    }

    /**
     * Retrieve all cells that have a certain style. TODO check performance
     * 
     * @param style the style to search
     * @return list of cels the style applies to
     */
    protected List<IJaretTableCell> getStyleLocations(ICellStyle style) {
        List<IJaretTableCell> result = new ArrayList<IJaretTableCell>();
        if (_columnMap.containsValue(style)) {
            for (IColumn col : _columnMap.keySet()) {
                if (_columnMap.get(col) == style) {
                    result.add(new JaretTableCellImpl(null, col));
                }
            }
        }
        if (_rowMap.containsValue(style)) {
            for (IRow row : _rowMap.keySet()) {
                if (_rowMap.get(row) == style) {
                    result.add(new JaretTableCellImpl(row, null));
                }
            }
        }

        for (IRow row : _cellMap.keySet()) {
            Map<IColumn, ICellStyle> cmap = _cellMap.get(row);
            if (cmap != null) {
                for (IColumn col : cmap.keySet()) {
                    ICellStyle cs = cmap.get(col);
                    if (style == cs) {
                        result.add(new JaretTableCellImpl(row, col));
                    }
                }
            }
        }

        return result;
    }

    /**
     * {@inheritDoc} Listens to all styles and fires style changed for every location a style is used in.
     */
    public void propertyChange(PropertyChangeEvent event) {
        ICellStyle style = (ICellStyle) event.getSource();
        List<IJaretTableCell> locs = getStyleLocations(style);
        for (IJaretTableCell loc : locs) {
            fireCellStyleChanged(loc.getRow(), loc.getColumn(), (ICellStyle) style);
        }
    }

    /**
     * {@inheritDoc}
     */
    public IStyleStrategy getStyleStrategy() {
        return _styleStrategy;
    }

    /**
     * {@inheritDoc}
     */
    public void setStyleStrategy(IStyleStrategy startegy) {
        _styleStrategy = startegy;
    }

    // ////////// convenience methods
    /**
     * {@inheritDoc}
     */
    public void setBackground(IRow row, RGB background) {
        ICellStyle style = getRowCellStyle(row, true);
        style.setBackgroundColor(background);
        setRowCellStyle(row, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setBackground(IColumn column, RGB background) {
        ICellStyle style = getColumnCellStyle(column, true);
        style.setBackgroundColor(background);
        setColumnCellStyle(column, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setBackground(IRow row, IColumn column, RGB background) {
        ICellStyle style = getCellSpecificStyle(row, column, true);
        style.setBackgroundColor(background);
        setCellStyle(row, column, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setForeground(IRow row, RGB foreground) {
        ICellStyle style = getRowCellStyle(row, true);
        style.setForegroundColor(foreground);
        setRowCellStyle(row, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setForeground(IColumn column, RGB foreground) {
        ICellStyle style = getColumnCellStyle(column, true);
        style.setForegroundColor(foreground);
        setColumnCellStyle(column, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setForeground(IRow row, IColumn column, RGB foreground) {
        ICellStyle style = getCellSpecificStyle(row, column, true);
        style.setForegroundColor(foreground);
        setCellStyle(row, column, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setHorizontalAlignment(IRow row, HAlignment alignment) {
        ICellStyle style = getRowCellStyle(row, true);
        style.setHorizontalAlignment(alignment);
        setRowCellStyle(row, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setHorizontalAlignment(IColumn column, HAlignment alignment) {
        ICellStyle style = getColumnCellStyle(column, true);
        style.setHorizontalAlignment(alignment);
        setColumnCellStyle(column, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setHorizontalAlignment(IRow row, IColumn column, HAlignment alignment) {
        ICellStyle style = getCellSpecificStyle(row, column, true);
        style.setHorizontalAlignment(alignment);
        setCellStyle(row, column, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setVerticalAlignment(IRow row, VAlignment alignment) {
        ICellStyle style = getRowCellStyle(row, true);
        style.setVerticalAlignment(alignment);
        setRowCellStyle(row, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setVerticalAlignment(IColumn column, VAlignment alignment) {
        ICellStyle style = getColumnCellStyle(column, true);
        style.setVerticalAlignment(alignment);
        setColumnCellStyle(column, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setVerticalAlignment(IRow row, IColumn column, VAlignment alignment) {
        ICellStyle style = getCellSpecificStyle(row, column, true);
        style.setVerticalAlignment(alignment);
        setCellStyle(row, column, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setFont(IRow row, FontData fontdata) {
        ICellStyle style = getRowCellStyle(row, true);
        style.setFont(fontdata);
        setRowCellStyle(row, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setFont(IColumn column, FontData fontdata) {
        ICellStyle style = getColumnCellStyle(column, true);
        style.setFont(fontdata);
        setColumnCellStyle(column, style);
    }

    /**
     * {@inheritDoc}
     */
    public void setFont(IRow row, IColumn column, FontData fontdata) {
        ICellStyle style = getCellSpecificStyle(row, column, true);
        style.setFont(fontdata);
        setCellStyle(row, column, style);
    }

}
