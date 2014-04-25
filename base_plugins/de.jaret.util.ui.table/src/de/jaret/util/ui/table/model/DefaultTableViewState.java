/*
 *  File: DefaultTableViewState.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import de.jaret.util.ui.table.renderer.DefaultCellStyleProvider;
import de.jaret.util.ui.table.renderer.ICellStyle;
import de.jaret.util.ui.table.renderer.ICellStyleListener;
import de.jaret.util.ui.table.renderer.ICellStyleProvider;

/**
 * Default implementation of a TableViewState for the jaret table.
 * 
 * @author Peter Kliem
 * @version $Id: DefaultTableViewState.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class DefaultTableViewState implements ITableViewState, ICellStyleListener {
    /** map of row configurations. */
    protected Map<IRow, RowConfiguration> _rowConfiguations = new HashMap<IRow, RowConfiguration>();

    /** map of column configurations. */
    protected Map<IColumn, ColumnConfiguration> _colConfigurations = new HashMap<IColumn, ColumnConfiguration>();

    /** listener list for the tableviewstate listeners. */
    protected List<ITableViewStateListener> _listeners;

    protected int _minimalRowHeight = 10;

    protected int _maximalRowHeight = -1; // undefined

    protected int _defaultRowHeight = 22;

    protected int _minimalColumnWidth = 10;

    protected int _maximalColumnWidth = -1; // undefined

    protected int _defaultColumnWidth = 100;

    /** the sorted list (for display) of columns. */ 
    protected List<IColumn> _sortedColumns;

    /** the cell style provider used. */
    protected ICellStyleProvider _cellStyleProvider;

    /** The colummn resize mode used by the table. */
    protected ColumnResizeMode _columnResizeMode;

    /** Default row height mode for new rows. */
    protected RowHeightMode _defaultRowHeightMode = RowHeightMode.OPTANDVAR;

    /**
     * Constructor.
     * 
     */
    public DefaultTableViewState() {
        _cellStyleProvider = new DefaultCellStyleProvider();
        _cellStyleProvider.addCellStyleListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public int getRowHeight(IRow row) {
        RowConfiguration configuration = getRowConfiguration(row);
        return configuration.rowHeight;
    }

    /**
     * {@inheritDoc}
     */
    public void setRowHeight(IRow row, int height) {
        RowConfiguration configuration = getRowConfiguration(row);
        if (configuration.rowHeight != height) {
            configuration.rowHeight = height;
            fireRowHeightChanged(row, height);
        }
    }

    /**
     * {@inheritDoc} 
     */
    public void setRowHeight(int height) {
        _defaultRowHeight = height;
        // set for all rows known
        for (RowConfiguration rconfig : _rowConfiguations.values()) {
            rconfig.rowHeight = height;
        }
        // TODO may be optimized 
        for (IRow row : _rowConfiguations.keySet()) {
            fireRowHeightChanged(row, height);
        }
        
    }
    
    
    /**
     * {@inheritDoc}
     */
    public RowHeightMode getRowHeigthMode(IRow row) {
        RowConfiguration configuration = getRowConfiguration(row);
        return configuration.heightMode;
    }

    /**
     * {@inheritDoc}
     */
    public void setRowHeightMode(IRow row, RowHeightMode mode) {
        RowConfiguration configuration = getRowConfiguration(row);
        if (configuration.heightMode != mode) {
            configuration.heightMode = mode;
            fireRowHeightModeChanged(row, mode);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setRowHeightMode(RowHeightMode mode) {
        _defaultRowHeightMode = mode;
        // set for all rows known
        for (RowConfiguration rconfig : _rowConfiguations.values()) {
            rconfig.heightMode = mode;
        }
        for (IRow row : _rowConfiguations.keySet()) {
            fireRowHeightModeChanged(row, mode);
        }
    }

    /**
     * {@inheritDoc}
     */
    public RowHeightMode getRowHeightMode() {
        return _defaultRowHeightMode;
    }

    private RowConfiguration getRowConfiguration(IRow row) {
        RowConfiguration configuration = _rowConfiguations.get(row);
        if (configuration == null) {
            configuration = getDefaultRowConfiguration();
            _rowConfiguations.put(row, configuration);
        }
        return configuration;
    }

    private RowConfiguration getDefaultRowConfiguration() {
        RowConfiguration configuration = new RowConfiguration();
        configuration.rowHeight = _defaultRowHeight;
        configuration.heightMode = _defaultRowHeightMode;
        return configuration;
    }

    private ColumnConfiguration getColumnConfiguration(IColumn col) {
        ColumnConfiguration configuration = _colConfigurations.get(col);
        if (configuration == null) {
            configuration = getDefaultColumnConfiguration(col.getId());
            _colConfigurations.put(col, configuration);
        }
        return configuration;
    }

    private ColumnConfiguration getColumnConfiguration(String columnId) {
        for (ColumnConfiguration colConf : _colConfigurations.values()) {
            if (colConf.id.equals(columnId)) {
                return colConf;
            }
        }
        return getDefaultColumnConfiguration(columnId);
    }

    private ColumnConfiguration getDefaultColumnConfiguration(String colId) {
        ColumnConfiguration configuration = new ColumnConfiguration();
        configuration.id = colId;
        configuration.resizable = true;
        configuration.columnWidth = _defaultColumnWidth;
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    public int getColumnWidth(IColumn column) {
        ColumnConfiguration configuration = getColumnConfiguration(column);
        return (int) Math.round(configuration.columnWidth);
    }

    /**
     * {@inheritDoc}
     */
    public void setColumnWidth(IColumn column, int width) {
        ColumnConfiguration configuration = getColumnConfiguration(column);
        if (configuration.columnWidth != width) {
            int oldVal = (int) Math.round(configuration.columnWidth);
            if (_columnResizeMode == ColumnResizeMode.SUBSEQUENT) {
                IColumn sCol = getSubsequentColumn(column);
                if (sCol == null) {
                    // no subsequent col -> do it
                    configuration.columnWidth = width;
                    fireColumnWidthChanged(column, width);
                } else {
                    int max = getColumnWidth(sCol) - _minimalColumnWidth;
                    int delta = width - oldVal;
                    delta = delta > max ? max : delta;
                    configuration.columnWidth += delta;
                    ColumnConfiguration sConfiguration = getColumnConfiguration(sCol);
                    sConfiguration.columnWidth -= delta;
                    fireColumnWidthsChanged();
                }
            } else if (_columnResizeMode == ColumnResizeMode.ALLSUBSEQUENT || _columnResizeMode == ColumnResizeMode.ALL) {
                List<IColumn> sCols = null;
                if (_columnResizeMode == ColumnResizeMode.ALLSUBSEQUENT) {
                    sCols = getSubsequentColumns(column);
                } else {
                    sCols = getAllVisibleCols(column);
                }
                if (sCols.size() == 0) {
                    // no subsequent cols -> do it
                    configuration.columnWidth = width;
                    fireColumnWidthChanged(column, width);
                } else {
                    int max = 0;
                    for (IColumn c : sCols) {
                        max += getColumnWidth(c) - _minimalColumnWidth;
                    }
                    int delta = width - oldVal;
                    delta = delta > max ? max : delta;
                    configuration.columnWidth += delta;
                    double distDelta = (double) delta / (double) sCols.size();
                    for (IColumn c : sCols) {
                        ColumnConfiguration sConfiguration = getColumnConfiguration(c);
                        sConfiguration.columnWidth -= distDelta;
                    }
                    fireColumnWidthsChanged();
                }
            } else { // mode NONE
                configuration.columnWidth = width;
                fireColumnWidthChanged(column, width);
            }
        }

    }

    /**
     * Creates a list of al visible cols without the given column.
     * 
     * @param without the column to omit
     * @return list of visible columns without a given one
     */
    private List<IColumn> getAllVisibleCols(IColumn without) {
        List<IColumn> result = new ArrayList<IColumn>();
        int idx = _sortedColumns.indexOf(without);
        for (int i = 0; i < _sortedColumns.size(); i++) {
            if (idx != i && getColumnVisible(_sortedColumns.get(i))) {
                result.add(_sortedColumns.get(i));
            }
        }
        return result;
    }

    /**
     * Retrieve the next column after the given one.
     * 
     * @param column reference column
     * @return next column in the sorted columns or <code>null</code> if the given column is the last
     */
    private IColumn getSubsequentColumn(IColumn column) {
        List<IColumn> l = getSubsequentColumns(column);
        if (l.size() > 0) {
            return l.get(0);
        }
        return null;
    }

    /**
     * Retrieve the list of columns behind the given column.
     * 
     * @param column reference column
     * @return list of columns behind the given column in the sorted columns
     */
    private List<IColumn> getSubsequentColumns(IColumn column) {
        List<IColumn> result = new ArrayList<IColumn>();
        int idx = _sortedColumns.indexOf(column);
        if (idx == -1) {
            return result;
        }
        for (int i = idx + 1; i < _sortedColumns.size(); i++) {
            if (getColumnVisible(_sortedColumns.get(i))) {
                result.add(_sortedColumns.get(i));
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getColumnVisible(IColumn column) {
        ColumnConfiguration configuration = getColumnConfiguration(column);
        return configuration.visible;
    }

    /**
     * {@inheritDoc}
     */
    public void setColumnVisible(IColumn column, boolean visible) {
        ColumnConfiguration configuration = getColumnConfiguration(column);
        if (configuration.visible != visible) {
            configuration.visible = visible;
            fireColumnVisibilityChanged(column, visible);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setColumnVisible(String columnId, boolean visible) {
        ColumnConfiguration configuration = getColumnConfiguration(columnId);
        if (configuration.visible != visible) {
            configuration.visible = visible;
            // TODO fireColumnVisibilityChanged(column, visible);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<IColumn> getSortedColumns() {
        if (_sortedColumns == null) {
            _sortedColumns = new ArrayList<IColumn>();
        }
        return _sortedColumns;
    }

    /**
     * {@inheritDoc}
     */
    public void setSortedColumns(List<IColumn> sortedColumns) {
        _sortedColumns = sortedColumns;
        fireColumnOrderChanged();
    }

    /**
     * {@inheritDoc}
     */
    public int getColumnSortingPosition(IColumn column) {
        ColumnConfiguration configuration = getColumnConfiguration(column);
        return configuration.sortingPosition;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getColumnSortingDirection(IColumn column) {
        ColumnConfiguration configuration = getColumnConfiguration(column);
        return configuration.sortingDirection;
    }

    /**
     * {@inheritDoc}
     */
    public void setSorting(IColumn column) {
        ColumnConfiguration conf = getColumnConfiguration(column);
        if (conf.sortingPosition == 0) {
            addShiftSorting();
            conf.sortingPosition = 1;
        } else {
            if (conf.sortingDirection) {
                conf.sortingDirection = false;
            } else {
                conf.sortingDirection = true;
                remSorting(conf.sortingPosition);
                conf.sortingPosition = 0;
            }
        }
        fireSortingChanged();
    }

    private void addShiftSorting() {
        for (ColumnConfiguration cconf : _colConfigurations.values()) {
            if (cconf.sortingPosition > 0) {
                cconf.sortingPosition++;
            }
        }
    }

    private void remSorting(int pos) {
        for (ColumnConfiguration cconf : _colConfigurations.values()) {
            if (cconf.sortingPosition > pos) {
                cconf.sortingPosition--;
            }
        }
    }

    /**
     * Simple helper class holding the configuration for a row.
     * 
     * @author Peter Kliem
     * @version $Id: DefaultTableViewState.java,v 1.1 2012-05-07 01:34:37 jason Exp $
     */
    public class RowConfiguration {
        /** rowheightmode. */
        public RowHeightMode heightMode;
        /** actual row height. */
        public int rowHeight;
    }

    /**
     * Simple helper class holding the onfiguration information for a column.
     * 
     * @author Peter Kliem
     * @version $Id: DefaultTableViewState.java,v 1.1 2012-05-07 01:34:37 jason Exp $
     */
    public class ColumnConfiguration {
        /** width of the column. */
        public double columnWidth;
        /** resize allowance. */
        public boolean resizable = true;
        /** true when visible. */
        public boolean visible = true;
        /** id of the column. */
        public String id;
        /** position in sorting. */
        public int sortingPosition;
        /** sorting direction if sorting. */
        public boolean sortingDirection = true; // ascending as default
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void addTableViewStateListener(ITableViewStateListener tvsl) {
        if (_listeners == null) {
            _listeners = new Vector<ITableViewStateListener>();
        }
        _listeners.add(tvsl);
    }

    /**
     * {@inheritDoc}
     */
    public void removeTableViewStateListener(ITableViewStateListener tvsl) {
        if (_listeners != null) {
            _listeners.remove(tvsl);
        }
    }

    /**
     * Inform listeners about a change in the height of a row.
     * 
     * @param row row
     * @param newHeight new row height
     */
    protected void fireRowHeightChanged(IRow row, int newHeight) {
        if (_listeners != null) {
            for (ITableViewStateListener listener : _listeners) {
                listener.rowHeightChanged(row, newHeight);
            }
        }
    }

    protected void fireRowHeightModeChanged(IRow row, RowHeightMode newMode) {
        if (_listeners != null) {
            for (ITableViewStateListener listener : _listeners) {
                listener.rowHeightModeChanged(row, newMode);
            }
        }
    }

    protected void fireColumnWidthChanged(IColumn column, int newWidth) {
        if (_listeners != null) {
            for (ITableViewStateListener listener : _listeners) {
                listener.columnWidthChanged(column, newWidth);
            }
        }
    }

    protected void fireColumnWidthsChanged() {
        if (_listeners != null) {
            for (ITableViewStateListener listener : _listeners) {
                listener.columnWidthsChanged();
            }
        }
    }

    protected void fireColumnVisibilityChanged(IColumn column, boolean visible) {
        if (_listeners != null) {
            for (ITableViewStateListener listener : _listeners) {
                listener.columnVisibilityChanged(column, visible);
            }
        }
    }

    protected void fireSortingChanged() {
        if (_listeners != null) {
            for (ITableViewStateListener listener : _listeners) {
                listener.sortingChanged();
            }
        }
    }

    protected void fireColumnOrderChanged() {
        if (_listeners != null) {
            for (ITableViewStateListener listener : _listeners) {
                listener.columnOrderChanged();
            }
        }
    }

    protected void fireCellStyleChanged(IRow row, IColumn column, ICellStyle cellStyle) {
        if (_listeners != null) {
            for (ITableViewStateListener listener : _listeners) {
                listener.cellStyleChanged(row, column, cellStyle);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getMinimalRowHeight() {
        return _minimalRowHeight;
    }

    /**
     * {@inheritDoc}
     */
    public void setMinimalRowHeight(int minimalRowHeight) {
        _minimalRowHeight = minimalRowHeight;
    }

    /**
     * {@inheritDoc}
     */
    public int getMinimalColWidth() {
        return _minimalColumnWidth;
    }

    /**
     * {@inheritDoc}
     */
    public void setMinimalColWidth(int minimalColumnWidth) {
        _minimalColumnWidth = minimalColumnWidth;
    }

    /**
     * {@inheritDoc}
     */
    public ICellStyleProvider getCellStyleProvider() {
        return _cellStyleProvider;
    }

    /**
     * {@inheritDoc}
     */
    public void setCellStyleProvider(ICellStyleProvider cellStyleProvider) {
        if (_cellStyleProvider != null) {
            _cellStyleProvider.remCellStyleListener(this);
        }
        _cellStyleProvider = cellStyleProvider;
        _cellStyleProvider.addCellStyleListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public ICellStyle getCellStyle(IRow row, IColumn column) {
        return _cellStyleProvider.getCellStyle(row, column);
    }

    /**
     * {@inheritDoc} inform listeners abou the received cell style change.
     */
    public void cellStyleChanged(IRow row, IColumn column, ICellStyle style) {
        fireCellStyleChanged(row, column, style);
    }

    /**
     * {@inheritDoc}
     */
    public boolean columnResizingAllowed(IColumn column) {
        ColumnConfiguration conf = getColumnConfiguration(column);
        return conf.resizable;
    }

    /**
     * {@inheritDoc}
     */
    public void setColumnResizingAllowed(IColumn column, boolean resizingAllowed) {
        ColumnConfiguration conf = getColumnConfiguration(column);
        conf.resizable = resizingAllowed;
    }

    /**
     * @return Returns the columnResizeMode.
     */
    public ColumnResizeMode getColumnResizeMode() {
        return _columnResizeMode;
    }

    /**
     * @param columnResizeMode The columnResizeMode to set.
     */
    public void setColumnResizeMode(ColumnResizeMode columnResizeMode) {
        _columnResizeMode = columnResizeMode;
    }

}
