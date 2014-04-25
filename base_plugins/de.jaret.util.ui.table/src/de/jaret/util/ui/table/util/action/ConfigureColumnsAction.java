/*
 *  File: ConfigureColumnsAction.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.util.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.ITableViewState;

/**
 * Action for configuring column display. Showing a dialog to reorder and change the visibility of rows. The action can
 * be parametrized to disallow manipulating the positions and visibility of fixed columns. The table will be
 * manipoulated instantly. Values will be saved to allow cancelling the configuration.
 * 
 * @author Peter Kliem
 * @version $Id: ConfigureColumnsAction.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class ConfigureColumnsAction extends Action {
    /** table the action is operating on. */
    protected JaretTable _table;
    /** the table viewstate. */
    protected ITableViewState _tvs;
    /** checkbox table viewer used to display the columns. */
    protected CheckboxTableViewer _chkBoxViewer;
    /** saved order for doing a proper cancel operation. */
    protected List<IColumn> _saveOrder;
    /** saved visibility for the columns for proper cancel action. */
    protected Map<IColumn, Boolean> _saveVisibility;
    /** if true fixed columns can be shifted or changed in visibility. */
    protected boolean _allowFixedColumns;

    /**
     * Construct the action.
     * 
     * @param table table to operate on
     * @param allowFixedColumns if true fixed columns can be changed in visibility and position (moving them out of the
     * fixed position)
     */
    public ConfigureColumnsAction(JaretTable table, boolean allowFixedColumns) {
        setTable(table);
        _allowFixedColumns = allowFixedColumns;
    }

    /**
     * Construct the action (allowFixedColumns defaults to true).
     * 
     * @param table table to operate on
     */
    public ConfigureColumnsAction(JaretTable table) {
        this(table, true);
    }

    /**
     * Set the table to operate on.
     * 
     * @param table table
     */
    public void setTable(JaretTable table) {
        _table = table;
        _tvs = _table.getTableViewState();
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        save();

        Dialog confColsDialog = new Dialog(Display.getCurrent().getActiveShell()) {
            @Override
            protected Control createDialogArea(Composite parent) {
                return createColumnControlPanel(parent);
            }

        };
        int result = confColsDialog.open();
        if (result == Dialog.CANCEL) {
            restore();
        }
    }

    /**
     * Save the current properties of the viewstate.
     */
    private void save() {
        _saveOrder = new ArrayList<IColumn>();
        _saveOrder.addAll(_tvs.getSortedColumns());
        _saveVisibility = new HashMap<IColumn, Boolean>();
        for (int i = 0; i < _table.getTableModel().getColumnCount(); i++) {
            IColumn col = _table.getTableModel().getColumn(i);
            _saveVisibility.put(col, _tvs.getColumnVisible(col));
        }
    }

    /**
     * Restore viewstate to previously saved state.
     */
    private void restore() {
        _tvs.setSortedColumns(_saveOrder);
        for (int i = 0; i < _table.getTableModel().getColumnCount(); i++) {
            IColumn col = _table.getTableModel().getColumn(i);
            boolean visible = _saveVisibility.get(col);
            _tvs.setColumnVisible(col, visible);
        }

    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        return "Configure columns";
    }

    /**
     * Create the dialog area. TODO can be done much nicer ... but works for the first draft
     * 
     * @param parent parent composite
     * @return initialized control
     */
    private Control createColumnControlPanel(Composite parent) {
        Composite panel = new Composite(parent, SWT.NULL);
        panel.setLayout(new RowLayout());

        Label l = new Label(panel, SWT.NULL);
        l.setText("Configure the columns");

        Table table = new Table(parent, SWT.CHECK | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
        _chkBoxViewer = new CheckboxTableViewer(table);
        _chkBoxViewer.setContentProvider(new ColTableContentProvider());
        _chkBoxViewer.setLabelProvider(new ColTableLabelProvider());

        TableColumn column = new TableColumn(_chkBoxViewer.getTable(), SWT.LEFT);
        column.setText("Column");
        column.setWidth(100);

        _chkBoxViewer.getTable().setHeaderVisible(true);
        _chkBoxViewer.setInput("x");

        final int firstColIdx = _allowFixedColumns ? 0 : _table.getFixedColumns();

        for (int i = 0; i < _table.getTableModel().getColumnCount(); i++) {
            IColumn col = _table.getTableModel().getColumn(i);
            _chkBoxViewer.setChecked(col, _tvs.getColumnVisible(col));
        }

        table.getColumn(0).pack();

        table.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    TableItem item = (TableItem) event.item;
                    IColumn col = (IColumn) item.getData();
                    int idx = _tvs.getSortedColumns().indexOf(col);
                    if (_allowFixedColumns || idx >= _table.getFixedColumns()) {
                        _tvs.setColumnVisible(col, item.getChecked());
                    } else {
                        _chkBoxViewer.setChecked(col, _tvs.getColumnVisible(col));
                    }
                }
            }
        });

        Button upButton = new Button(panel, SWT.PUSH);
        upButton.setText("up");
        upButton.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent arg0) {
                if (_chkBoxViewer.getTable().getSelectionCount() > 0) {
                    TableItem item = _chkBoxViewer.getTable().getItem(_chkBoxViewer.getTable().getSelectionIndex());
                    IColumn col = (IColumn) item.getData();
                    int idx = _tvs.getSortedColumns().indexOf(col);
                    if (idx > firstColIdx) {
                        _tvs.getSortedColumns().remove(col);
                        _tvs.getSortedColumns().add(idx - 1, col);
                        _table.updateColumnList();
                        _table.redraw();
                        _chkBoxViewer.refresh();
                    }
                }
            }

            public void widgetDefaultSelected(SelectionEvent arg0) {
            }

        });
        Button downButton = new Button(panel, SWT.PUSH);
        downButton.setText("down");
        downButton.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent arg0) {
                if (_chkBoxViewer.getTable().getSelectionCount() > 0) {
                    TableItem item = _chkBoxViewer.getTable().getItem(_chkBoxViewer.getTable().getSelectionIndex());
                    IColumn col = (IColumn) item.getData();
                    int idx = _tvs.getSortedColumns().indexOf(col);
                    if (idx < _tvs.getSortedColumns().size() - 1) {
                        _tvs.getSortedColumns().remove(col);
                        _tvs.getSortedColumns().add(idx + 1, col);
                        _table.updateColumnList();
                        _table.redraw();
                        _chkBoxViewer.refresh();
                    }
                }
            }

            public void widgetDefaultSelected(SelectionEvent arg0) {
            }

        });

        return panel;
    }

    /**
     * Content provider for the table viewer.
     * 
     * @author kliem
     * @version $Id: ConfigureColumnsAction.java,v 1.1 2012-05-07 01:34:39 jason Exp $
     */
    public class ColTableContentProvider implements IStructuredContentProvider {
        /**
         * {@inheritDoc}
         */
        public Object[] getElements(Object element) {
            Object[] kids = null;
            java.util.List l = _table.getTableViewState().getSortedColumns();
            kids = l.toArray();
            return kids;
        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {
        }

        /**
         * {@inheritDoc}
         */
        public void inputChanged(Viewer viewer, Object oldObject, Object newObject) {
        }
    }

    /**
     * Labelprovider for the table viewer.
     * 
     * @author kliem
     * @version $Id: ConfigureColumnsAction.java,v 1.1 2012-05-07 01:34:39 jason Exp $
     */
    public class ColTableLabelProvider implements ITableLabelProvider {
        /**
         * {@inheritDoc}
         */
        public String getColumnText(Object obj, int i) {
            String result;
            IColumn column = (IColumn) obj;
            int idx = _tvs.getSortedColumns().indexOf(column);
            switch (i) {
            case 0:
                result = column.getHeaderLabel();
                if (!_allowFixedColumns && idx < _table.getFixedColumns()) {
                    result+="(fixed)";
                }
                break;
            default:
                result = "error - unknow column";
                break;
            }
            return result;
        }

        /**
         * {@inheritDoc}
         */
        public void addListener(ILabelProviderListener ilabelproviderlistener) {
        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {
        }

        /**
         * {@inheritDoc}
         */
        public boolean isLabelProperty(Object obj, String s) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public void removeListener(ILabelProviderListener ilabelproviderlistener) {
        }

        /**
         * {@inheritDoc}
         */
        public Image getColumnImage(Object arg0, int arg1) {
            return null;
        }
    }

}
