/*
 *  File: JaretTable.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

import de.jaret.util.date.JaretDate;
import de.jaret.util.misc.PropertyObservable;
import de.jaret.util.misc.PropertyObservableBase;
import de.jaret.util.ui.table.editor.BooleanCellEditor;
import de.jaret.util.ui.table.editor.DateCellEditor;
import de.jaret.util.ui.table.editor.DoubleCellEditor;
import de.jaret.util.ui.table.editor.EnumComboEditor;
import de.jaret.util.ui.table.editor.ICellEditor;
import de.jaret.util.ui.table.editor.IntegerCellEditor;
import de.jaret.util.ui.table.editor.TextCellEditor;
import de.jaret.util.ui.table.filter.DefaultAutoFilter;
import de.jaret.util.ui.table.filter.IAutoFilter;
import de.jaret.util.ui.table.filter.IRowFilter;
import de.jaret.util.ui.table.model.DefaultHierarchicalTableViewState;
import de.jaret.util.ui.table.model.DefaultTableViewState;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IHierarchicalJaretTableModel;
import de.jaret.util.ui.table.model.IHierarchicalTableViewState;
import de.jaret.util.ui.table.model.IJaretTableCell;
import de.jaret.util.ui.table.model.IJaretTableModel;
import de.jaret.util.ui.table.model.IJaretTableModelListener;
import de.jaret.util.ui.table.model.IJaretTableSelection;
import de.jaret.util.ui.table.model.IJaretTableSelectionModel;
import de.jaret.util.ui.table.model.IJaretTableSelectionModelListener;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.model.IRowSorter;
import de.jaret.util.ui.table.model.ITableFocusListener;
import de.jaret.util.ui.table.model.ITableNode;
import de.jaret.util.ui.table.model.ITableViewState;
import de.jaret.util.ui.table.model.ITableViewStateListener;
import de.jaret.util.ui.table.model.JaretTableCellImpl;
import de.jaret.util.ui.table.model.JaretTableSelectionModelImpl;
import de.jaret.util.ui.table.model.StdHierarchicalTableModel;
import de.jaret.util.ui.table.model.ITableViewState.RowHeightMode;
import de.jaret.util.ui.table.renderer.BooleanCellRenderer;
import de.jaret.util.ui.table.renderer.DateCellRenderer;
import de.jaret.util.ui.table.renderer.DefaultTableHeaderRenderer;
import de.jaret.util.ui.table.renderer.DoubleCellRenderer;
import de.jaret.util.ui.table.renderer.ICellRenderer;
import de.jaret.util.ui.table.renderer.ICellStyle;
import de.jaret.util.ui.table.renderer.IHierarchyRenderer;
import de.jaret.util.ui.table.renderer.ITableHeaderRenderer;
import de.jaret.util.ui.table.renderer.ImageCellRenderer;
import de.jaret.util.ui.table.renderer.TableHierarchyRenderer;
import de.jaret.util.ui.table.renderer.TextCellRenderer;
import de.jaret.util.ui.table.strategies.DefaultCCPStrategy;
import de.jaret.util.ui.table.strategies.DefaultFillDragStrategy;
import de.jaret.util.ui.table.strategies.ICCPStrategy;
import de.jaret.util.ui.table.strategies.IFillDragStrategy;

/**
 * Custom drawn table widget for the SWT Toolkit. Always consider using the native table widget!
 * <p>
 * The JaretTable features:
 * </p>
 * <ul>
 * <li>Flexible rendering of all elements</li>
 * <li>CellEditor support</li>
 * <li>Flat (table) and hierarchical (tree) support</li>
 * <li>Separation between data model and viewstate</li>
 * <li>row filtering and sorting without model modification</li>
 * <li>simple auto filter</li>
 * <li>drag fill support </li>
 * <li></li>
 * <li></li>
 * </ul>
 * 
 * Keyboard controls <table>
 * <tr>
 * <td><b>Key</b></td>
 * <td><b>Function</b></td>
 * </tr>
 * <tr>
 * <td>Shift+Click</td>
 * <td>Move focus</td>
 * </tr>
 * <tr>
 * <td>Arrows</td>
 * <td>Move focus</td>
 * </tr>
 * <tr>
 * <td>Shift-Arrow</td>
 * <td>Select Current cell, shift focus and select newly focussed cell and the rectangle of cells between first and
 * current cell</td>
 * </tr>
 * </table>
 * 
 * @author Peter Kliem
 * @version $Id: JaretTable.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class JaretTable extends Canvas implements ITableViewStateListener, IJaretTableModelListener,
        IJaretTableSelectionModelListener, PropertyChangeListener, PropertyObservable {
    /** true for measuring paint time. */
    private static final boolean DEBUGPAINTTIME = false;

    /** pixel for row resize/selection if no fixed rows are present. */
    private static final int SELDELTA = 4;

    /** size of the marker for fill dragging. */
    private static final int FILLDRAGMARKSIZE = 4;

    /** default height for the header. */
    private static final int DEFAULTHEADERHEIGHT = 16;

    /** default value for the minimal header height. */
    private static final int DEFAULTMINHEADERHEIGHT = 10;

    /** button that is the poputrigger. */
    private static final int POPUPTRIGGER = 3;

    /** name of the bound property. */
    public static final String PROPERTYNAME_HEADERHEIGHT = "HeaderHeight";
    /** name of the bound property. */
    public static final String PROPERTYNAME_FIRSTROWIDX = "FirstRowIdx";
    /** name of the bound property. */
    public static final String PROPERTYNAME_FIRSTROWPIXELOFFSET = "FirstRowPixelOffset";
    /** name of the bound property. */
    public static final String PROPERTYNAME_ROWSORTER = "RowSorter";
    /** name of the bound property. */
    public static final String PROPERTYNAME_ROWFILTER = "RowFilter";
    /** Pseudo propertyname on which property change is fired whenever the sorting changes. */
    public static final String PROPERTYNAME_SORTING = "Sorting";
    /** Pseudo propertyname on which property change is fired whenever the filtering changes. */
    public static final String PROPERTYNAME_FILTERING = "Filtering";
    /** name of the bound property. */
    public static final String PROPERTYNAME_AUTOFILTERENABLE = "AutoFilterEnable";

    // scroll positions of the main table area
    /** Index of the first row displayed (may be only a half display). */
    protected int _firstRowIdx = 0;
    /** Pixel offset of the display of the first row. */
    protected int _firstRowPixelOffset = 0;
    /** Index of the first row displayed. */
    protected int _firstColIdx = 0;
    /** pixel offset of the firs displayed column. */
    protected int _firstColPixelOffset = 0;

    /** number of fixed columns. */
    protected int _fixedColumns = 0;
    /** number of fixed rows. */
    protected int _fixedRows = 0;

    /** cell renderer map for columns. */
    protected Map<IColumn, ICellRenderer> _colCellRendererMap = new HashMap<IColumn, ICellRenderer>();

    /** cell renderer map for classes. */
    protected Map<Class<?>, ICellRenderer> _colClassRendererMap = new HashMap<Class<?>, ICellRenderer>();

    /** cell editor map for columns. */
    protected Map<IColumn, ICellEditor> _colCellEditorMap = new HashMap<IColumn, ICellEditor>();

    /** cell editor map for classes. */
    protected Map<Class<?>, ICellEditor> _colClassEditorMap = new HashMap<Class<?>, ICellEditor>();

    /** configuration: support fill dragging. */
    protected boolean _supportFillDragging = true;

    /** fill drag strategy. * */
    protected IFillDragStrategy _fillDragStrategy = new DefaultFillDragStrategy();

    /** Strategy for handling cut copy paste. */
    protected ICCPStrategy _ccpStrategy = new DefaultCCPStrategy();

    /** table model. */
    protected IJaretTableModel _model;

    /** hierarchical table model if used. */
    protected IHierarchicalJaretTableModel _hierarchicalModel;

    /** table viewstate. */
    protected ITableViewState _tvs = new DefaultTableViewState();

    /** List of rows actually diplayed (filtered and ordered). */
    protected List<IRow> _rows = new ArrayList<IRow>();

    /** row filter. */
    protected IRowFilter _rowFilter;

    /** row sorter. */
    protected IRowSorter _rowSorter;

    /** List of columns actually displayed. */
    protected List<IColumn> _cols = new ArrayList<IColumn>();

    /** Rectangle the headers are painted in. */
    protected Rectangle _headerRect;

    /** height of the headers. */
    protected int _headerHeight = DEFAULTHEADERHEIGHT;

    /** minimal height for the header. */
    protected int _minHeaderHeight = DEFAULTMINHEADERHEIGHT;

    /** if true headers will be drawn. */
    protected boolean _drawHeader = true;

    /** rectangle the main table is drawn into (withou fixedcolRect and without fixedRowRect!). */
    protected Rectangle _tableRect;

    /** Renderer used to render the headers. */
    protected ITableHeaderRenderer _headerRenderer = new DefaultTableHeaderRenderer();

    /** Rectangle in which the fixed columns will be painted. */
    protected Rectangle _fixedColRect;

    /** Rectangle in which the fixed rows will be painted. */
    protected Rectangle _fixedRowRect;

    /** cache for the drag marker location. */
    protected Rectangle _dragMarkerRect;

    /** Rectangle the autofilter elements (combos) are placed. */
    protected Rectangle _autoFilterRect;

    /** if true autofilters are enabled and present. */
    protected boolean _autoFilterEnabled = false;

    /** Instance of the interbal RowFilter that makes up the autofilter. */
    protected AutoFilter _autoFilter = new AutoFilter(this);

    /** map containing the actual instantiated autofilters for the different columns. */
    protected Map<IColumn, IAutoFilter> _autoFilterMap = new HashMap<IColumn, IAutoFilter>();

    /** map containing the autofilter classes to use for dedicated content classes. */
    protected Map<Class<?>, Class<? extends IAutoFilter>> _autoFilterClassMap = new HashMap<Class<?>, Class<? extends IAutoFilter>>();

    /** map containing teh autofiletr classes to use for specified columns. */
    protected Map<IColumn, Class<? extends IAutoFilter>> _autoFilterColumnMap = new HashMap<IColumn, Class<? extends IAutoFilter>>();

    /** if true header resizing is allowed. */
    private boolean _headerResizeAllowed = true;

    /** if true row resizes are allowed. */
    private boolean _rowResizeAllowed = true;

    /** if true column resizes are allowed. */
    private boolean _columnResizeAllowed = true;

    /**
     * If true, resizing is only allowed in header and fixed columns (for rows) and the leftmost SELDELTA pixels of
     * eachrow.
     */
    protected boolean _resizeRestriction = false;

    /** if true fixed rows will not be affected by sorting operations. */
    protected boolean _excludeFixedRowsFromSorting = true;

    /** global flag for allowing sorting of the table. */
    protected boolean _allowSorting = true;

    // focus control
    /** row of the focussed cell or null. */
    protected IRow _focussedRow = null;

    /** column of the focussed cell or null. */
    protected IColumn _focussedColumn = null;

    /** Listz of listeners interested in changes of the focussed cell. */
    protected List<ITableFocusListener> _tableFocusListeners;

    /** selection model used by the table. */
    protected IJaretTableSelectionModel _selectionModel = new JaretTableSelectionModelImpl();

    // editing
    /** cell editor used to edit a cell. will be nun null when editiing. */
    protected ICellEditor _editor; // if != null editing in progress

    /** control of the editor. */
    protected Control _editorControl = null;

    /** row that is edited. */
    protected IRow _editorRow;

    /** context menu used on table headers. */
    protected Menu _headerContextMenu;

    /** context menu used for rows. */
    protected Menu _rowContextMenu;

    /** Delegate to handle property change listener support. */
    protected PropertyChangeSupport _propertyChangeSupport;

    /** row information cache. */
    protected List<RowInfo> _rowInfoCache = null;
    /** column information cache. */
    protected List<ColInfo> _colInfoCache = new ArrayList<ColInfo>();

    /**
     * Simple struct for storing row information.
     * 
     * @author Peter Kliem
     * @version $Id: JaretTable.java,v 1.1 2012-05-07 01:34:37 jason Exp $
     */
    public class RowInfo {
        /** beginning y coordinate. */
        public int y;
        /** row reference. */
        public IRow row;
        /** height of the row. */
        public int height;
        /** fixed row flag. */
        public boolean fixed = false;

        /**
         * Construct a row info instance.
         * 
         * @param rowIn row reference
         * @param yIn begin y
         * @param heightIn height of the row
         * @param fixedIn true if the row is a fixed row
         */
        public RowInfo(IRow rowIn, int yIn, int heightIn, boolean fixedIn) {
            this.row = rowIn;
            this.y = yIn;
            this.height = heightIn;
            this.fixed = fixedIn;
        }
    }

    /**
     * Simple struct for storing column information.
     * 
     * @author Peter Kliem
     * @version $Id: JaretTable.java,v 1.1 2012-05-07 01:34:37 jason Exp $
     */
    public class ColInfo {
        /** begin x coordinate. */
        public int x;

        /** column reference. */
        public IColumn column;

        /** actual width of the column. */
        public int width;

        /**
         * Construct a col info instance.
         * 
         * @param columnIn col ref
         * @param xIn begin x
         * @param widthIn width in pixel
         */
        public ColInfo(IColumn columnIn, int xIn, int widthIn) {
            this.column = columnIn;
            this.x = xIn;
            this.width = widthIn;
        }
    }

    /**
     * Constructor for a new JaretTable widget.
     * 
     * @param parent parent composite
     * @param style style bits (use HSCROLL, VSCROLL)
     */
    public JaretTable(Composite parent, int style) {
        // no background painting
        super(parent, style | SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);

        addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent event) {
                onPaint(event);
            }
        });

        addMouseListener(new MouseListener() {

            public void mouseDoubleClick(MouseEvent me) {
                mouseDouble(me.x, me.y);
            }

            public void mouseDown(MouseEvent me) {
                forceFocus();
                mousePressed(me.x, me.y, me.button == POPUPTRIGGER, me.stateMask);
            }

            public void mouseUp(MouseEvent me) {
                mouseReleased(me.x, me.y, me.button == POPUPTRIGGER);
            }
        });

        addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent me) {
                if ((me.stateMask & SWT.BUTTON1) != 0) {
                    mouseDragged(me.x, me.y, me.stateMask);
                } else {
                    mouseMoved(me.x, me.y, me.stateMask);
                }
            }
        });

        addMouseTrackListener(new MouseTrackListener() {
            public void mouseEnter(MouseEvent arg0) {
            }

            public void mouseExit(MouseEvent arg0) {
                // on exit set standard cursor
                if (Display.getCurrent().getActiveShell() != null) {
                    Display.getCurrent().getActiveShell().setCursor(
                            Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
                }
            }

            public void mouseHover(MouseEvent me) {
                setToolTipText(getToolTipText(me.x, me.y));
            }
        });

        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
                onDispose(event);
            }
        });

        // key listener for keyboard control
        addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent event) {
                handleKeyPressed(event);
            }

            public void keyReleased(KeyEvent arg0) {
            }
        });

        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Resize:
                    updateScrollBars();
                    break;
                default:
                    // do nothing
                    break;
                }
            }
        };
        addListener(SWT.Resize, listener);

        ScrollBar verticalBar = getVerticalBar();
        if (verticalBar != null) {
            verticalBar.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    handleVerticalScroll(event);
                }
            });
        }

        ScrollBar horizontalBar = getHorizontalBar();
        if (horizontalBar != null) {
            horizontalBar.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    handleHorizontalScroll(event);
                }
            });
        }

        _tableRect = getClientArea();
        _fixedColRect = getClientArea();

        // updateYScrollBar();

        // register default cell renderers, editors and autofilters
        registerDefaultRenderers();
        registerDefaultEditors();
        registerDefaultAutofilters();

        // register with the viewstate
        _tvs.addTableViewStateListener(this);

        // register with the selection model
        _selectionModel.addTableSelectionModelListener(this);

        setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

        _propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Dispose whatever there is to dispose (renderers and so on).
     * 
     * @param event dispose event
     */
    private void onDispose(DisposeEvent event) {
        // header renderer
        if (_headerRenderer != null) {
            _headerRenderer.dispose();
        }
        // cell renderers
        for (ICellRenderer renderer : _colCellRendererMap.values()) {
            renderer.dispose();
        }
        for (ICellRenderer renderer : _colClassRendererMap.values()) {
            renderer.dispose();
        }
        // cell editors
        for (ICellEditor editor : _colCellEditorMap.values()) {
            editor.dispose();
        }
        for (ICellEditor editor : _colClassEditorMap.values()) {
            editor.dispose();
        }
        // autofilters
        for (IAutoFilter autoFilter : _autoFilterMap.values()) {
            autoFilter.dispose();
        }
        if (_rowSorter != null) {
            _rowSorter.removePropertyChangeListener(this);
        }
        if (_rowFilter != null) {
            _rowFilter.removePropertyChangeListener(this);
        }
        if (_ccpStrategy != null) {
            _ccpStrategy.dispose();
        }
    }

    /**
     * Register all default renderers.
     * 
     */
    private void registerDefaultRenderers() {
        ICellRenderer cellRenderer = new TextCellRenderer();
        registerCellRenderer(void.class, cellRenderer);
        registerCellRenderer(String.class, cellRenderer);
        registerCellRenderer(Image.class, new ImageCellRenderer());
        cellRenderer = new BooleanCellRenderer();
        registerCellRenderer(Boolean.class, cellRenderer);
        registerCellRenderer(Boolean.TYPE, cellRenderer);
        cellRenderer = new DateCellRenderer();
        registerCellRenderer(Date.class, cellRenderer);
        registerCellRenderer(JaretDate.class, cellRenderer);
        cellRenderer = new DoubleCellRenderer();
        registerCellRenderer(Double.class, cellRenderer);
        registerCellRenderer(Double.TYPE, cellRenderer);
    }

    /**
     * Register all default editors.
     * 
     */
    private void registerDefaultEditors() {
        registerCellEditor(String.class, new TextCellEditor(true));
        registerCellEditor(Boolean.class, new BooleanCellEditor(true));
        registerCellEditor(Date.class, new DateCellEditor());
        registerCellEditor(JaretDate.class, new DateCellEditor());
        registerCellEditor(Enum.class, new EnumComboEditor());
        registerCellEditor(Integer.class, new IntegerCellEditor());
        registerCellEditor(Integer.TYPE, new IntegerCellEditor());
        registerCellEditor(Double.class, new DoubleCellEditor());
        registerCellEditor(Double.TYPE, new DoubleCellEditor());
    }

    /**
     * Regsiter the default autofilters.
     */
    private void registerDefaultAutofilters() {
        registerAutoFilterForClass(String.class, DefaultAutoFilter.class);
    }

    /**
     * Register a cell renderer for rendering objects of class clazz.
     * 
     * @param clazz class the renderer should be applied for
     * @param cellRenderer renderer to use for clazz
     */
    public void registerCellRenderer(Class<?> clazz, ICellRenderer cellRenderer) {
        _colClassRendererMap.put(clazz, cellRenderer);
    }

    /**
     * Register a cell renderer for a column.
     * 
     * @param column column the renderer should be used on
     * @param cellRenderer renderer to use
     */
    public void registerCellRenderer(IColumn column, ICellRenderer cellRenderer) {
        _colCellRendererMap.put(column, cellRenderer);
    }

    /**
     * Retrieve the cell renderer for a cell.
     * 
     * @param row row row of the cell
     * @param column column column of the cell
     * @return cell renderer
     */
    protected ICellRenderer getCellRenderer(IRow row, IColumn column) {
        // first check column specific renderers
        ICellRenderer renderer = null;
        renderer = _colCellRendererMap.get(column);
        if (renderer == null) {
            // try class map
            Object value = column.getValue(row);
            if (value != null) {
                renderer = getCellRendererFromMap(value.getClass());
            }
            if (renderer == null) {
                // nothing? -> default
                renderer = _colClassRendererMap.get(void.class);
            }
        }
        return renderer;
    }

    /**
     * Register a cell editor for objects of class clazz.
     * 
     * @param clazz class of objeects the editor should be used for
     * @param cellEditor editor to use
     */
    public void registerCellEditor(Class<?> clazz, ICellEditor cellEditor) {
        _colClassEditorMap.put(clazz, cellEditor);
    }

    /**
     * Register a cell editor for a column.
     * 
     * @param column column the editor should be used for
     * @param cellEditor editor to use
     */
    public void registerCellEditor(IColumn column, ICellEditor cellEditor) {
        _colCellEditorMap.put(column, cellEditor);
    }

    /**
     * Retrieve the cell editor for a cell.
     * 
     * @param row row of the cell
     * @param column col of the cell
     * @return cell editor or <code>null</code> if no editor can be found
     */
    private ICellEditor getCellEditor(IRow row, IColumn column) {
        // first check column specific renderers
        ICellEditor editor = null;
        editor = _colCellEditorMap.get(column);
        if (editor == null) {
            // try class map
            Object value = column.getValue(row);
            if (value != null) {
                editor = getCellEditorFromMap(value.getClass());
            } else if (column.getContentClass(row) != null) {
                editor = getCellEditorFromMap(column.getContentClass(row));
            } else if (column.getContentClass() != null) {
                editor = getCellEditorFromMap(column.getContentClass());
            }
        }
        return editor;
    }

    /**
     * Retrieve a cell editor for a given class. Checks all interfaces and all superclasses.
     * 
     * @param clazz class in queston
     * @return editor or null
     */
    private ICellEditor getCellEditorFromMap(Class<?> clazz) {
        ICellEditor result = null;
        result = _colClassEditorMap.get(clazz);
        if (result != null) {
            return result;
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> c : interfaces) {
            result = _colClassEditorMap.get(c);
            if (result != null) {
                return result;
            }
        }

        Class<?> sc = clazz.getSuperclass();

        while (sc != null) {
            result = _colClassEditorMap.get(sc);
            if (result != null) {
                return result;
            }
            // interfaces of the superclass
            Class<?>[] scinterfaces = sc.getInterfaces();
            for (Class<?> c : scinterfaces) {
                result = _colClassEditorMap.get(c);
                if (result != null) {
                    return result;
                }
            }
            sc = sc.getSuperclass();
        }

        return result;
    }

    /**
     * Retrieve a cell renderer for a given class. Checks all interfaces and all superclasses.
     * 
     * @param clazz class in queston
     * @return renderer or null
     */
    private ICellRenderer getCellRendererFromMap(Class<?> clazz) {
        ICellRenderer result = null;
        result = _colClassRendererMap.get(clazz);
        if (result != null) {
            return result;
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> c : interfaces) {
            result = _colClassRendererMap.get(c);
            if (result != null) {
                return result;
            }
        }

        Class<?> sc = clazz.getSuperclass();

        while (sc != null) {
            result = _colClassRendererMap.get(sc);
            if (result != null) {
                return result;
            }
            // interfaces of the superclass
            Class<?>[] scinterfaces = sc.getInterfaces();
            for (Class<?> c : scinterfaces) {
                result = _colClassRendererMap.get(c);
                if (result != null) {
                    return result;
                }
            }
            sc = sc.getSuperclass();
        }

        return result;
    }

    /**
     * Register an autofilter implementing class to be used on columns that announce a specific content class.
     * 
     * @param clazz content clazz thet triggers the use of the filter
     * @param autoFilterClass class implementing the IAutoFilter interface that will be used
     */
    public void registerAutoFilterForClass(Class<?> clazz, Class<? extends IAutoFilter> autoFilterClass) {
        _autoFilterClassMap.put(clazz, autoFilterClass);
    }

    /**
     * Regsiter an autofilter implementing class for use with a specific column.
     * 
     * @param column column
     * @param autoFilterClass class of autofilter that will be used
     */
    public void registerAutoFilterForColumn(IColumn column, Class<? extends IAutoFilter> autoFilterClass) {
        _autoFilterColumnMap.put(column, autoFilterClass);
    }

    /**
     * Get the autofiletr class to be used on a column.
     * 
     * @param column column
     * @return class or <code>null</code> if none could be determined
     */
    protected Class<? extends IAutoFilter> getAutoFilterClass(IColumn column) {
        Class<? extends IAutoFilter> result = _autoFilterColumnMap.get(column);
        if (result != null) {
            return result;
        }
        Class<?> contentClass = column.getContentClass();
        if (contentClass != null) {
            result = getAutoFilterClassForClass(contentClass);
        }
        // nothing found so long -> use default (String) filter
        if (result == null) {
            result = _autoFilterClassMap.get(String.class);
        }
        return result;
    }

    /**
     * Retrieve autofilter class for a given content class. Takes interfaces and superclasses into account.
     * 
     * @param clazz content class
     * @return class to be used or <code>null</code> if none could be determined
     */
    private Class<? extends IAutoFilter> getAutoFilterClassForClass(Class<?> clazz) {
        Class<? extends IAutoFilter> result = null;
        result = _autoFilterClassMap.get(clazz);
        if (result != null) {
            return result;
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> c : interfaces) {
            result = _autoFilterClassMap.get(c);
            if (result != null) {
                return result;
            }
        }

        Class<?> sc = clazz.getSuperclass();

        while (sc != null) {
            result = _autoFilterClassMap.get(sc);
            if (result != null) {
                return result;
            }
            // interfaces of the superclass
            Class<?>[] scinterfaces = sc.getInterfaces();
            for (Class<?> c : scinterfaces) {
                result = _autoFilterClassMap.get(c);
                if (result != null) {
                    return result;
                }
            }
            sc = sc.getSuperclass();
        }

        return result;
    }

    // ////// mouse handling
    /** currently dragged row (dragging height). */
    protected RowInfo _heightDraggedRowInfo = null;

    /** currently dragged (width) column or null. */
    protected ColInfo _widthDraggedColumn = null;

    /** true if the header height is beeing dragged. */
    protected boolean _headerDragged = false;

    /**
     * Handle mouse double click.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    private void mouseDouble(int x, int y) {
        if (_tableRect.contains(x, y) || (_fixedColumns > 0 && _fixedColRect.contains(x, y))
                || (_fixedRows > 0 && _fixedRowRect.contains(x, y))) {
            setFocus(x, y);
            startEditing(rowForY(y), colForX(x), (char) 0);
        }
    }

    /**
     * Handle mouse pressed.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param popuptrigger true if the button pressed was the popup trigger
     * @param stateMask statemask from the event
     */
    private void mousePressed(int x, int y, boolean popuptrigger, int stateMask) {
        // check for location over drag marker and start fill drag if necessary
        if (_dragMarkerRect != null && _dragMarkerRect.contains(x, y)) {
            _isFillDrag = true;
            _firstFillDragSelect = _selectedIdxRectangle;
            return;
        }

        IRow row = rowByBottomBorder(y);
        if (row != null
                && _rowResizeAllowed
                && (_tvs.getRowHeigthMode(row) == RowHeightMode.VARIABLE || _tvs.getRowHeigthMode(row) == RowHeightMode.OPTANDVAR)
                && (!_resizeRestriction || Math.abs(x - _tableRect.x) <= SELDELTA || (_fixedColRect != null && _fixedColRect
                        .contains(x, y)))) {
            _heightDraggedRowInfo = getRowInfo(row);
            return;
        } else {
            IColumn col = colByRightBorder(x);
            if (col != null && _columnResizeAllowed && _tvs.columnResizingAllowed(col)
                    && (!_resizeRestriction || _headerRect == null || _headerRect.contains(x, y))) {
                _widthDraggedColumn = getColInfo(col);
                return;
            }
        }
        // check header drag
        if (_headerResizeAllowed && Math.abs(_headerRect.y + _headerRect.height - y) <= SELDELTA) {
            _headerDragged = true;
            return;
        }
        // handle mouse press for selection
        boolean doSelect = true;
        // check focus set
        if (_tableRect.contains(x, y) || (_fixedColumns > 0 && _fixedColRect.contains(x, y))
                || (_fixedRows > 0 && _fixedRowRect.contains(x, y))) {
            setFocus(x, y);
            doSelect = !handleEditorSingleClick(x, y);
        }
        // check hierarchy
        if (_tableRect.contains(x, y) || (_fixedColumns > 0 && _fixedColRect.contains(x, y))
                || (_fixedRows > 0 && _fixedRowRect.contains(x, y))) {
            IRow xrow = rowForY(y);
            IColumn xcol = colForX(x);
            if (xrow != null && xcol != null && isHierarchyColumn(xrow, xcol)) {
                Rectangle rect = getCellBounds(xrow, xcol);
                IHierarchyRenderer hrenderer = (IHierarchyRenderer) getCellRenderer(xrow, xcol);
                if (hrenderer.isInActiveArea(xrow, rect, x, y)) {
                    toggleExpanded(xrow);
                }
            }
        }
        // check header sorting clicks
        IColumn xcol = colForX(x);
        if (_allowSorting && _headerRect.contains(x, y)
                && _headerRenderer.isSortingClick(getHeaderDrawingArea(xcol), xcol, x, y)) {
            _tvs.setSorting(xcol);
        } else if (doSelect) {
            // selection can be intercepted by editor clicks
            handleSelection(x, y, stateMask, false);
        }
    }

    /**
     * Toggle the expanded state of a row.
     * 
     * @param row row to toggle
     */
    private void toggleExpanded(IRow row) {
        IHierarchicalTableViewState hvs = (IHierarchicalTableViewState) _tvs;
        hvs.setExpanded((ITableNode) row, !hvs.isExpanded((ITableNode) row));
    }

    /**
     * Determine whether the column is the hierrarchy column. This is accomplished by looking at the cell renderer
     * class.
     * 
     * @param row row
     * @param col column
     * @return true if hte adressed cell is part of the hierarchy column
     */
    private boolean isHierarchyColumn(IRow row, IColumn col) {
        if (row == null || col == null) {
            return false;
        }
        return getCellRenderer(row, col) instanceof TableHierarchyRenderer;
    }

    /**
     * Check whether a column is the hierarchy column.
     * 
     * @param column column to check
     * @return <code>true</code> if the column is the hierarchy column
     */
    public boolean isHierarchyColumn(IColumn column) {
    	if (column == null) {
    		return false;
    	}
    	return isHierarchyColumn(_rows.get(0), column);
    }
    
    /**
     * Retrieve the rectangle in which the header of a column is drawn.
     * 
     * @param col column
     * @return drawing rectangle for the header
     */
    private Rectangle getHeaderDrawingArea(IColumn col) {
        int x = getColInfo(col).x;
        Rectangle r = new Rectangle(x, _tableRect.y, _tvs.getColumnWidth(col), _tableRect.height);
        return r;
    }

    /**
     * Check whether a click is a row selection.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return true for click on the left margin of the table or in the fixed column area
     */
    private boolean isRowSelection(int x, int y) {
        return (Math.abs(x - _tableRect.x) <= SELDELTA || (_fixedColRect != null && _fixedColRect.contains(x, y)));
    }

    /**
     * Check whether a click is a column selection.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return true for coordinate in header or fixed rows
     */
    private boolean isColumnSelection(int x, int y) {
        return (_headerRect != null && _headerRect.contains(x, y))
                || (_fixedRowRect != null && _fixedRowRect.contains(x, y));
    }

    protected int _firstCellSelectX = -1;
    protected int _firstCellSelectY = -1;
    protected int _lastCellSelectX = -1;
    protected int _lastCellSelectY = -1;

    /**
     * marker flag for drag operation: fill drag.
     */
    protected boolean _isFillDrag = false;

    /** first col selected in drag. */
    protected int _firstColSelectIdx = -1;
    /** last col selected in drag or as standard col selection. */
    protected int _lastColSelectIdx = -1;
    int _lastKeyColSelectIdx = -1;
    int _firstKeyColSelectIdx = -1;

    /** first row selected in drag. */
    protected int _firstRowSelectIdx = -1;
    /** last row selected in drag. */
    protected int _lastRowSelectIdx = -1;
    int _lastKeyRowSelectIdx = -1;
    int _firstKeyRowSelectIdx = -1;

    /** last cell idx selected by shift-arrow. */
    protected Point _lastKeySelect = null;
    /** first cell idx selected by shift-arrow. */
    protected Point _firstKeySelect = null;

    /** enum for the selection type (intern). */
    private enum SelectType {
        NONE, CELL, COLUMN, ROW
    };

    /** index rectangle of selected cells whenn a fill drag starts. */
    Rectangle _firstFillDragSelect = null;
    /** true if the fill drag is horizontal (fill along the x axis), false for vertical fill drag. */
    private boolean _horizontalFillDrag;

    /** type of the last selection, used for handling keyboard selection. */
    protected SelectType _lastSelectType = SelectType.NONE;

    /**
     * Handle selection operations.
     * 
     * @param x x
     * @param y y
     * @param stateMask key state mask
     * @param dragging true when dragging
     */
    private void handleSelection(int x, int y, int stateMask, boolean dragging) {
        // a mouse select always ends a shift-arrow select
        _lastKeySelect = null;
        _firstKeySelect = null;
        _firstKeyColSelectIdx = -1;
        _lastKeyColSelectIdx = -1;
        _firstKeyRowSelectIdx = -1;
        _lastKeyRowSelectIdx = -1;

        IRow row = rowForY(y);
        int rowIdx = row != null ? _rows.indexOf(row) : -1;
        IColumn col = colForX(x);
        int colIdx = getColumnIdx(col);

        // check fill dragging
        if (dragging && _isFillDrag) {
            if (_selectionModel.isCellSelectionAllowed() && _tableRect.contains(x, y)) {
                if (col != null && row != null) {
                    if (_firstCellSelectX == -1) {
                        _firstCellSelectX = colIdx;
                        _firstCellSelectY = rowIdx;
                    }
                    if (Math.abs(_firstCellSelectX - colIdx) > Math.abs(_firstCellSelectY - rowIdx)) {
                        rowIdx = _firstCellSelectY;
                        row = rowForIdx(rowIdx);
                        _horizontalFillDrag = false;
                    } else {
                        colIdx = _firstCellSelectX;
                        col = colForIdx(colIdx);
                        _horizontalFillDrag = true;
                    }
                    ensureSelectionContainsRegion(_firstFillDragSelect, colIdx, rowIdx, _lastCellSelectX,
                            _lastCellSelectY);
                    // ensureSelectionContainsRegion(_firstCellSelectX, _firstCellSelectY, colIdx, rowIdx,
                    // _lastCellSelectX, _lastCellSelectY);
                    _lastCellSelectX = colIdx;
                    _lastCellSelectY = rowIdx;
                    // a newly selected cell will always be the focussed cell (causes scrolling this cell to be
                    // completely visible)
                    setFocus(row, col);
                }
            }

            return;
        }

        // check row selection
        if (row != null && _selectionModel.isFullRowSelectionAllowed()
                && (isRowSelection(x, y) || _selectionModel.isOnlyRowSelectionAllowed() || _firstRowSelectIdx != -1)) {
            if (_firstRowSelectIdx == -1) {
                _firstRowSelectIdx = rowIdx;
            }
            if ((stateMask & SWT.CONTROL) != 0) {
                if (!_selectionModel.getSelection().getSelectedRows().contains(row)) {
                    _selectionModel.addSelectedRow(row);
                } else {
                    _selectionModel.remSelectedRow(row);
                }
                _lastSelectType = SelectType.ROW;
            } else if (dragging) {
                ensureSelectionContainsRowRegion(_firstRowSelectIdx, rowIdx, _lastRowSelectIdx);
                _lastRowSelectIdx = rowIdx;
                _lastSelectType = SelectType.ROW;
            } else {
                _selectionModel.clearSelection();
                _selectionModel.addSelectedRow(row);
                _lastSelectType = SelectType.ROW;
            }
            _lastRowSelectIdx = rowIdx;
            return;
        }
        // check column selection
        if (_selectionModel.isFullColumnSelectionAllowed() && (isColumnSelection(x, y) || _firstColSelectIdx != -1)) {
            if (_firstColSelectIdx == -1) {
                _firstColSelectIdx = colIdx;
            }
            if ((stateMask & SWT.CONTROL) != 0) {
                if (!_selectionModel.getSelection().getSelectedColumns().contains(col)) {
                    _selectionModel.addSelectedColumn(col);
                } else {
                    _selectionModel.remSelectedColumn(col);
                }
                _lastSelectType = SelectType.COLUMN;
            } else if (dragging) {
                ensureSelectionContainsColRegion(_firstColSelectIdx, colIdx, _lastColSelectIdx);
                _lastColSelectIdx = colIdx;
                _lastSelectType = SelectType.COLUMN;
            } else {
                _selectionModel.clearSelection();
                _selectionModel.addSelectedColumn(col);
                _lastSelectType = SelectType.COLUMN;
            }
            _lastColSelectIdx = colIdx;
            return;
        }
        // check cell selection
        if (_selectionModel.isCellSelectionAllowed() && _tableRect.contains(x, y)) {
            if (col != null && row != null) {
                IJaretTableCell cell = new JaretTableCellImpl(row, col);
                if (_firstCellSelectX == -1) {
                    _firstCellSelectX = colIdx;
                    _firstCellSelectY = rowIdx;
                }
                if ((stateMask & SWT.CONTROL) != 0) {
                    if (!_selectionModel.getSelection().getSelectedCells().contains(cell)) {
                        _selectionModel.addSelectedCell(cell);
                    } else {
                        _selectionModel.remSelectedCell(cell);
                    }
                    _lastSelectType = SelectType.CELL;
                } else if (dragging) {
                    ensureSelectionContainsRegion(_firstCellSelectX, _firstCellSelectY, colIdx, rowIdx,
                            _lastCellSelectX, _lastCellSelectY);
                    _lastCellSelectX = colIdx;
                    _lastCellSelectY = rowIdx;
                    _lastSelectType = SelectType.CELL;
                } else {
                    _selectionModel.clearSelection();
                    _selectionModel.addSelectedCell(cell);
                    _lastSelectType = SelectType.CELL;
                }
                // a newly selected cell will always be the focussed cell (causes scrolling this cell to be completely
                // visible)
                setFocus(row, col);
            }
        }

    }

    /**
     * Ensures that the selection contains the rows from firstIdx to rowIdx. If the range firstIdx to lastIdx is larger
     * than the region the other rows will be removed from the selection.
     * 
     * @param firstRowSelectIdx first selected row index
     * @param rowIdx current selected row index
     * @param lastRowSelectIdx may be -1 for no last selection
     */
    private void ensureSelectionContainsRowRegion(int firstRowSelectIdx, int rowIdx, int lastRowSelectIdx) {
        int first = Math.min(firstRowSelectIdx, rowIdx);
        int end = Math.max(firstRowSelectIdx, rowIdx);
        for (int i = first; i <= end; i++) {
            IRow row = rowForIdx(i);
            if (!_selectionModel.getSelection().getSelectedRows().contains(row)) {
                _selectionModel.addSelectedRow(row);
            }
        }
        if (lastRowSelectIdx != -1) {
            int f = Math.min(firstRowSelectIdx, lastRowSelectIdx);
            int e = Math.max(firstRowSelectIdx, lastRowSelectIdx);
            for (int i = f; i <= e; i++) {
                if (i < first || i > end) {
                    IRow row = rowForIdx(i);
                    _selectionModel.remSelectedRow(row);
                }
            }
        }
    }

    /**
     * Ensures that the selection contains the columns from firstIdx to colIdx. If the range firstIdx to lastIdx is
     * larger than the region the other columns will be removed from the selection.
     * 
     * @param firstColIdx first selected column index
     * @param colIdx current selected column index
     * @param lastColSelectIdx may be -1 for no last selection
     */
    private void ensureSelectionContainsColRegion(int firstColIdx, int colIdx, int lastColSelectIdx) {
        int first = Math.min(firstColIdx, colIdx);
        int end = Math.max(firstColIdx, colIdx);
        for (int i = first; i <= end; i++) {
            IColumn col = colForIdx(i);
            if (!_selectionModel.getSelection().getSelectedColumns().contains(col)) {
                _selectionModel.addSelectedColumn(col);
            }
        }
        if (lastColSelectIdx != -1) {
            int f = Math.min(firstColIdx, lastColSelectIdx);
            int e = Math.max(firstColIdx, lastColSelectIdx);
            for (int i = f; i <= e; i++) {
                if (i < first || i > end) {
                    IColumn col = colForIdx(i);
                    _selectionModel.remSelectedColumn(col);
                }
            }
        }
    }

    /**
     * Ensures the selection contains the cells in the rectangle given by first*, *Idx. If the rectangle given by
     * first*, last* is larger than the other rectangle is is ensured that the additional cells are not in the
     * selection.
     * 
     * @param firstCellSelectX begin x index of selected cell rectangle
     * @param firstCellSelectY begin y index of selected cell rectangle
     * @param colIdx end x index of selected cell rectangle
     * @param rowIdx end y index of selected cell rectangle
     * @param lastCellSelectX may be -1 for no last selection
     * @param lastCellSelectY may be -1 for no last selection
     */
    private void ensureSelectionContainsRegion(int firstCellSelectX, int firstCellSelectY, int colIdx, int rowIdx,
            int lastCellSelectX, int lastCellSelectY) {
        int firstx = Math.min(firstCellSelectX, colIdx);
        int endx = Math.max(firstCellSelectX, colIdx);
        int firsty = Math.min(firstCellSelectY, rowIdx);
        int endy = Math.max(firstCellSelectY, rowIdx);

        for (int x = firstx; x <= endx; x++) {
            for (int y = firsty; y <= endy; y++) {
                IJaretTableCell cell = new JaretTableCellImpl(rowForIdx(y), colForIdx(x));
                if (!_selectionModel.getSelection().getSelectedCells().contains(cell)) {
                    _selectionModel.addSelectedCell(cell);
                }
            }
        }

        // last sel rect
        if (lastCellSelectX != -1) {
            int lfx = Math.min(firstCellSelectX, lastCellSelectX);
            int lex = Math.max(firstCellSelectX, lastCellSelectX);
            int lfy = Math.min(firstCellSelectY, lastCellSelectY);
            int ley = Math.max(firstCellSelectY, lastCellSelectY);

            for (int x = lfx; x <= lex; x++) {
                for (int y = lfy; y <= ley; y++) {
                    if (!(x >= firstx && x <= endx && y >= firsty && y <= endy)) {
                        IJaretTableCell cell = new JaretTableCellImpl(rowForIdx(y), colForIdx(x));
                        _selectionModel.remSelectedCell(cell);
                    }
                }
            }
        }

    }

    /**
     * Ensures the selection contains the cells in the rectangle given by firstIdxRect, *Idx. If the rectangle given by
     * first*, last* is larger than the other rectangle is is ensured that the additional cells are not in the
     * selection.
     * 
     * @param firstIdxRect rectangle containing the indizes of the originating rect
     * @param colIdx new end x index for the selected rectangle
     * @param rowIdx new end y index for teh selecetd rectangle
     * @param lastCellSelectX may be -1 for no last selection
     * @param lastCellSelectY may be -1 for no last selection
     */
    private void ensureSelectionContainsRegion(Rectangle firstIdxRect, int colIdx, int rowIdx, int lastCellSelectX,
            int lastCellSelectY) {

        int firstx = Math.min(firstIdxRect.x, colIdx);
        int endx = Math.max(firstIdxRect.x + firstIdxRect.width - 1, colIdx);
        int firsty = Math.min(firstIdxRect.y, rowIdx);
        int endy = Math.max(firstIdxRect.y + firstIdxRect.height - 1, rowIdx);

        for (int x = firstx; x <= endx; x++) {
            for (int y = firsty; y <= endy; y++) {
                IJaretTableCell cell = new JaretTableCellImpl(rowForIdx(y), colForIdx(x));
                if (!_selectionModel.getSelection().getSelectedCells().contains(cell)) {
                    _selectionModel.addSelectedCell(cell);
                }
            }
        }

        // last sel rect
        if (lastCellSelectX != -1 && lastCellSelectY != -1) {
            int lfx = Math.min(firstIdxRect.x, lastCellSelectX);
            int lex = Math.max(firstIdxRect.x + firstIdxRect.width - 1, lastCellSelectX);
            int lfy = Math.min(firstIdxRect.y, lastCellSelectY);
            int ley = Math.max(firstIdxRect.y + firstIdxRect.height - 1, lastCellSelectY);

            for (int x = lfx; x <= lex; x++) {
                for (int y = lfy; y <= ley; y++) {
                    if (!(x >= firstx && x <= endx && y >= firsty && y <= endy)) {
                        IJaretTableCell cell = new JaretTableCellImpl(rowForIdx(y), colForIdx(x));
                        _selectionModel.remSelectedCell(cell);
                    }
                }
            }
        }

    }

    /**
     * Handle drag of mouse.
     * 
     * @param x x coordinate of pointer
     * @param y y coordinate of pointer
     * @param stateMask keyStatemask
     */
    private void mouseDragged(int x, int y, int stateMask) {
        if (_isFillDrag) {
            handleSelection(x, y, stateMask, true);
        } else if (_heightDraggedRowInfo != null) {
            int newHeight = y - _heightDraggedRowInfo.y;
            if (newHeight < _tvs.getMinimalRowHeight()) {
                newHeight = _tvs.getMinimalRowHeight();
            }
            _tvs.setRowHeight(_heightDraggedRowInfo.row, newHeight);
            // setting the row heigth on an OPTVAR ror converts this to variable
            if (_tvs.getRowHeigthMode(_heightDraggedRowInfo.row) == RowHeightMode.OPTANDVAR) {
                _tvs.setRowHeightMode(_heightDraggedRowInfo.row, RowHeightMode.VARIABLE);
            }

        } else if (_widthDraggedColumn != null) {
            int newWidth = x - _widthDraggedColumn.x;
            if (newWidth < _tvs.getMinimalColWidth()) {
                newWidth = _tvs.getMinimalColWidth();
            }
            if (_tvs.getColumnWidth(_widthDraggedColumn.column) != newWidth) {
                _tvs.setColumnWidth(_widthDraggedColumn.column, newWidth);
            }
        } else if (_headerDragged) {
            int newHeight = y - _headerRect.y;
            if (newHeight < _minHeaderHeight) {
                newHeight = _minHeaderHeight;
            }
            setHeaderHeight(newHeight);
        } else {
            handleSelection(x, y, stateMask, true);
        }
    }

    /**
     * Handle mouse move. This is mostly: modifying the appearance of the cursor.
     * 
     * @param x x coordinate of pointer
     * @param y y coordinate of pointer
     * @param stateMask keyStatemask
     */
    private void mouseMoved(int x, int y, int stateMask) {
        Display display = Display.getCurrent();
        Shell activeShell = display != null ? display.getActiveShell() : null;

        // check for location over drag marker
        if (_dragMarkerRect != null && _dragMarkerRect.contains(x, y)) {
            if (activeShell != null) {
                // MAYBE other cursor for differentiation?
                activeShell.setCursor(display.getSystemCursor(SWT.CURSOR_SIZEALL));
            }
            return;
        }

        // check for location over lower border of row
        IRow row = rowByBottomBorder(y);
        if (row != null
                && _rowResizeAllowed
                && (_tvs.getRowHeigthMode(row) == RowHeightMode.VARIABLE || _tvs.getRowHeigthMode(row) == RowHeightMode.OPTANDVAR)
                && (!_resizeRestriction || Math.abs(x - _tableRect.x) <= SELDELTA || (_fixedColRect != null && _fixedColRect
                        .contains(x, y)))) {
            if (activeShell != null) {
                activeShell.setCursor(display.getSystemCursor(SWT.CURSOR_SIZENS));
            }
            return;
        } else {
            IColumn col = colByRightBorder(x);
            if (col != null && _columnResizeAllowed && _tvs.columnResizingAllowed(col)
                    && (!_resizeRestriction || _headerRect == null || _headerRect.contains(x, y))) {
                if (activeShell != null) {
                    activeShell.setCursor(display.getSystemCursor(SWT.CURSOR_SIZEW));
                }
                return;
            }
        }
        // check header drag symboling
        if (_headerRect != null && _headerResizeAllowed && Math.abs(_headerRect.y + _headerRect.height - y) <= SELDELTA) {
            if (activeShell != null) {
                activeShell.setCursor(display.getSystemCursor(SWT.CURSOR_SIZENS));
            }
            return;
        }

        if (Display.getCurrent().getActiveShell() != null) {
            Display.getCurrent().getActiveShell().setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
        }

    }

    /**
     * Handle the release of a mouse button.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param popUpTrigger true if the buttonm is the popup trigger
     */
    private void mouseReleased(int x, int y, boolean popUpTrigger) {

        if (_isFillDrag) {
            handleFill();
        }

        _heightDraggedRowInfo = null;
        _widthDraggedColumn = null;
        _headerDragged = false;

        _firstCellSelectX = -1;
        _firstCellSelectY = -1;
        _lastCellSelectX = -1;
        _lastCellSelectY = -1;

        _firstColSelectIdx = -1;
        // _lastColSelectIdx = -1;
        _firstRowSelectIdx = -1;
        // _lastRowSelectIDx = -1;

        _isFillDrag = false;

        if (_headerRect.contains(x, y) && popUpTrigger) {
            displayHeaderContextMenu(x, y);
        } else if (popUpTrigger && isRowSelection(x, y)) {
            displayRowContextMenu(x, y);
        }

    }

    /**
     * Handle the end of a fill drag.
     * 
     */
    private void handleFill() {
        if (_fillDragStrategy != null) {
            if (_horizontalFillDrag) {
                // horizontal base rect
                for (int i = _firstFillDragSelect.x; i < _firstFillDragSelect.x + _firstFillDragSelect.width; i++) {
                    IJaretTableCell firstCell = getCellForIdx(i, _firstFillDragSelect.y);
                    List<IJaretTableCell> cells = getSelectedCellsVertical(i);
                    cells.remove(firstCell);
                    _fillDragStrategy.doFill(this, firstCell, cells);
                }
            } else {
                // vertical base rect
                for (int i = _firstFillDragSelect.y; i < _firstFillDragSelect.y + _firstFillDragSelect.height; i++) {
                    IJaretTableCell firstCell = getCellForIdx(_firstFillDragSelect.x, i);
                    List<IJaretTableCell> cells = getSelectedCellsHorizontal(i);
                    cells.remove(firstCell);
                    _fillDragStrategy.doFill(this, firstCell, cells);
                }
            }
        }
    }

    /**
     * Get all cells that are selected at the x idx given.
     * 
     * @param x x idx
     * @return list of selecetd cells with x idx == x
     */
    private List<IJaretTableCell> getSelectedCellsVertical(int x) {
        List<IJaretTableCell> cells = new ArrayList<IJaretTableCell>();
        List<IJaretTableCell> s = getSelectionModel().getSelection().getSelectedCells();
        for (IJaretTableCell cell : s) {
            Point p = getCellDisplayIdx(cell);
            if (p.x == x) {
                cells.add(cell);
            }
        }
        return cells;
    }

    /**
     * Get all cells that are selected at the y idx given.
     * 
     * @param y y idx
     * @return list of selecetd cells at idx y == y
     */
    private List<IJaretTableCell> getSelectedCellsHorizontal(int y) {
        List<IJaretTableCell> cells = new ArrayList<IJaretTableCell>();
        List<IJaretTableCell> s = getSelectionModel().getSelection().getSelectedCells();
        for (IJaretTableCell cell : s) {
            Point p = getCellDisplayIdx(cell);
            if (p.y == y) {
                cells.add(cell);
            }
        }
        return cells;
    }

    /**
     * Supply tooltip text for a position in the table.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return tooltip text or <code>null</code>
     */
    private String getToolTipText(int x, int y) {
        IJaretTableCell cell = getCell(x, y);
        if (cell != null) {
            Rectangle bounds = getCellBounds(cell);
            ICellRenderer renderer = getCellRenderer(cell.getRow(), cell.getColumn());
            if (renderer != null) {
                String tt = renderer.getTooltip(this, bounds, cell.getRow(), cell.getColumn(), x, y);
                if (tt != null) {
                    return tt;
                }
            }
        }
        return null;
    }

    // /////// end mouse handling

    // // keyboard handling
    /**
     * Handle any key presses.
     * 
     * @param event key event
     */
    private void handleKeyPressed(KeyEvent event) {
        if ((event.stateMask & SWT.SHIFT) != 0 && Character.isISOControl(event.character)) {
            switch (event.keyCode) {
            case SWT.ARROW_RIGHT:
                selectRight();
                break;
            case SWT.ARROW_LEFT:
                selectLeft();
                break;
            case SWT.ARROW_DOWN:
                selectDown();
                break;
            case SWT.ARROW_UP:
                selectUp();
                break;

            default:
                // do nothing
                break;
            }
        } else if ((event.stateMask & SWT.CONTROL) != 0 && Character.isISOControl(event.character)) {
            // TODO keybindings hard coded is ok for now
            // System.out.println("keycode "+event.keyCode);
            switch (event.keyCode) {
            case 'c':
                copy();
                break;
            case 'x':
                cut();
                break;
            case 'v':
                paste();
                break;
            case 'a':
                selectAll();
                break;

            default:
                // do nothing
                break;
            }

        } else {
            _lastKeySelect = null;
            _firstKeySelect = null;

            switch (event.keyCode) {
            case SWT.ARROW_RIGHT:
                focusRight();
                break;
            case SWT.ARROW_LEFT:
                focusLeft();
                break;
            case SWT.ARROW_DOWN:
                focusDown();
                break;
            case SWT.ARROW_UP:
                focusUp();
                break;
            case SWT.TAB:
                focusRight();
                break;
            case SWT.F2:
//                startEditing(_focussedRow, _focussedColumn, (char) 0);
                break;

            default:
                if (event.character == ' ' && isHierarchyColumn(_focussedRow, _focussedColumn)) {
                    toggleExpanded(_focussedRow);
                } else if (!Character.isISOControl(event.character)) {
//                    startEditing(event.character);
                }
                // do nothing
                break;
            }
        }

    }

    /**
     * Enlarge selection to the right.
     */
    private void selectRight() {
        IJaretTableCell cell = getFocussedCell();
        if (_lastSelectType == SelectType.CELL && cell != null) {
            int cx = getColumnIdx(cell.getColumn());
            int cy = getRowIdx(cell.getRow());
            if (_lastKeySelect == null) {
                _lastKeySelect = new Point(-1, -1);
            }
            if (_firstKeySelect == null) {
                _firstKeySelect = new Point(cx, cy);
                _selectionModel.clearSelection();
            }

            focusRight();
            cell = getFocussedCell();
            cx = getColumnIdx(cell.getColumn());
            cy = getRowIdx(cell.getRow());

            ensureSelectionContainsRegion(_firstKeySelect.x, _firstKeySelect.y, cx, cy, _lastKeySelect.x,
                    _lastKeySelect.y);

            _lastSelectType = SelectType.CELL;
            _lastKeySelect = new Point(cx, cy);
        } else if (_lastSelectType == SelectType.COLUMN && (_lastColSelectIdx != -1 || _lastKeyColSelectIdx != -1)) {
            if (_firstKeyColSelectIdx == -1) {
                _firstKeyColSelectIdx = _lastColSelectIdx;
            }
            int colIdx = _lastKeyColSelectIdx != -1 ? _lastKeyColSelectIdx + 1 : _firstKeyColSelectIdx + 1;
            if (colIdx > _cols.size() - 1) {
                colIdx = _cols.size() - 1;
            }
            ensureSelectionContainsColRegion(_firstKeyColSelectIdx, colIdx, _lastKeyColSelectIdx);
            _lastKeyColSelectIdx = colIdx;
        }
    }

    /**
     * Enlarge selection to the left.
     */
    private void selectLeft() {
        IJaretTableCell cell = getFocussedCell();
        if (_lastSelectType == SelectType.CELL && cell != null) {
            if (cell != null) {
                int cx = getColumnIdx(cell.getColumn());
                int cy = getRowIdx(cell.getRow());
                if (_lastKeySelect == null) {
                    _lastKeySelect = new Point(-1, -1);
                }
                if (_firstKeySelect == null) {
                    _firstKeySelect = new Point(cx, cy);
                    _selectionModel.clearSelection();
                }

                focusLeft();

                cell = getFocussedCell();
                cx = getColumnIdx(cell.getColumn());
                cy = getRowIdx(cell.getRow());

                ensureSelectionContainsRegion(_firstKeySelect.x, _firstKeySelect.y, cx, cy, _lastKeySelect.x,
                        _lastKeySelect.y);
                _lastSelectType = SelectType.CELL;
                _lastKeySelect = new Point(cx, cy);
            }
        } else if (_lastSelectType == SelectType.COLUMN && (_lastColSelectIdx != -1 || _lastKeyColSelectIdx != -1)) {
            if (_firstKeyColSelectIdx == -1) {
                _firstKeyColSelectIdx = _lastColSelectIdx;
            }
            int colIdx = _lastKeyColSelectIdx != -1 ? _lastKeyColSelectIdx - 1 : _firstKeyColSelectIdx - 1;
            if (colIdx < 0) {
                colIdx = 0;
            }
            ensureSelectionContainsColRegion(_firstKeyColSelectIdx, colIdx, _lastKeyColSelectIdx);
            _lastKeyColSelectIdx = colIdx;
        }
    }

    /**
     * Enlarge selection downwards.
     */
    private void selectDown() {
        IJaretTableCell cell = getFocussedCell();
        if (_lastSelectType == SelectType.CELL && cell != null) {
            if (cell != null) {
                int cx = getColumnIdx(cell.getColumn());
                int cy = getRowIdx(cell.getRow());
                if (_lastKeySelect == null) {
                    _lastKeySelect = new Point(-1, -1);
                }
                if (_firstKeySelect == null) {
                    _firstKeySelect = new Point(cx, cy);
                    _selectionModel.clearSelection();
                }

                focusDown();

                cell = getFocussedCell();
                cx = getColumnIdx(cell.getColumn());
                cy = getRowIdx(cell.getRow());

                ensureSelectionContainsRegion(_firstKeySelect.x, _firstKeySelect.y, cx, cy, _lastKeySelect.x,
                        _lastKeySelect.y);

                _lastSelectType = SelectType.CELL;
                _lastKeySelect = new Point(cx, cy);
            }
        } else if (_lastSelectType == SelectType.ROW && (_lastRowSelectIdx != -1 || _lastKeyRowSelectIdx != -1)) {
            if (_firstKeyRowSelectIdx == -1) {
                _firstKeyRowSelectIdx = _lastRowSelectIdx;
            }
            int rowIdx = _lastKeyRowSelectIdx != -1 ? _lastKeyRowSelectIdx + 1 : _firstKeyRowSelectIdx + 1;
            if (rowIdx > _rows.size() - 1) {
                rowIdx = _rows.size() - 1;
            }
            ensureSelectionContainsRowRegion(_firstKeyRowSelectIdx, rowIdx, _lastKeyRowSelectIdx);
            _lastKeyRowSelectIdx = rowIdx;
        }

    }

    /**
     * Enlarge selection upwards.
     */
    private void selectUp() {
        IJaretTableCell cell = getFocussedCell();
        if (_lastSelectType == SelectType.CELL && cell != null) {
            if (cell != null) {
                int cx = getColumnIdx(cell.getColumn());
                int cy = getRowIdx(cell.getRow());
                if (_lastKeySelect == null) {
                    _lastKeySelect = new Point(-1, -1);
                }
                if (_firstKeySelect == null) {
                    _firstKeySelect = new Point(cx, cy);
                    _selectionModel.clearSelection();
                }

                focusUp();

                cell = getFocussedCell();
                cx = getColumnIdx(cell.getColumn());
                cy = getRowIdx(cell.getRow());

                ensureSelectionContainsRegion(_firstKeySelect.x, _firstKeySelect.y, cx, cy, _lastKeySelect.x,
                        _lastKeySelect.y);

                _lastSelectType = SelectType.CELL;
                _lastKeySelect = new Point(cx, cy);
            }
        } else if (_lastSelectType == SelectType.ROW && (_lastRowSelectIdx != -1 || _lastKeyRowSelectIdx != -1)) {
            if (_firstKeyRowSelectIdx == -1) {
                _firstKeyRowSelectIdx = _lastRowSelectIdx;
            }
            int rowIdx = _lastKeyRowSelectIdx != -1 ? _lastKeyRowSelectIdx - 1 : _firstKeyRowSelectIdx - 1;
            if (rowIdx < 0) {
                rowIdx = 0;
            }
            ensureSelectionContainsRowRegion(_firstKeyRowSelectIdx, rowIdx, _lastKeyRowSelectIdx);
            _lastKeyRowSelectIdx = rowIdx;
        }
    }

    /**
     * Retrieve the currently focussed cell.
     * 
     * @return the focussed cell or <code>null</code> if no cell is focussed
     */
    public IJaretTableCell getFocussedCell() {
        if (_focussedColumn != null && _focussedRow != null) {
            return new JaretTableCellImpl(_focussedRow, _focussedColumn);
        }
        return null;
    }

    /**
     * Retrieve the indizes of the currently focussed cell (idx in the filtered, sorted or whatever table).
     * 
     * @return Point x = column, y = row or <code>null</code> if no cell is focussed
     */
    public Point getFocussedCellIdx() {
        if (_focussedColumn != null && _focussedRow != null) {
            return new Point(getColumnIdx(_focussedColumn), getRowIdx(_focussedRow));
        }
        return null;
    }

    /**
     * Retrieve the display coordinates for a table cell.
     * 
     * @param cell cell to get he coordinates for
     * @return Point x = colIdx, y = rowIdx
     */
    public Point getCellDisplayIdx(IJaretTableCell cell) {
        return new Point(getColumnIdx(cell.getColumn()), getRowIdx(cell.getRow()));
    }

    /**
     * Convenience method for setting a value at a displayed position in the table. NOTE: this method does call the the
     * set method of the model directly, so be aware that the model may protest by throwing a runtime exception or just
     * ignore the new value.
     * 
     * @param colIdx column index
     * @param rowIdx row index
     * @param value value to set
     */
    public void setValue(int colIdx, int rowIdx, Object value) {
        IColumn col = getColumn(colIdx);
        IRow row = getRow(rowIdx);
        col.setValue(row, value);
    }

    /**
     * {@inheritDoc} will get call to transfer focus to the table. The mthod will focus the left/uppermost cell
     * displayed. If no rows and columns are present no cell will get the focus.
     */
    public boolean setFocus() {
        super.setFocus();
        if (_focussedRow == null && _rows != null && _rows.size() > 0 && _cols.size() > 0) {
            setFocus(_rows.get(_firstRowIdx), _cols.get(_firstColIdx));
        }
        return true;
    }

    /**
     * Ensures there is a focussed cell and uses the cell at 0,0 if no cell is focussed.
     * 
     */
    private void ensureFocus() {
        if (_focussedRow == null) {
            _focussedRow = _rows.get(0);
        }
        if (_focussedColumn == null) {
            _focussedColumn = _cols.get(0);
        }
    }

    /**
     * Move the focus left.
     */
    public void focusLeft() {
        ensureFocus();
        int idx = _cols.indexOf(_focussedColumn);
        if (idx > 0) {
            setFocus(_focussedRow, _cols.get(idx - 1));
        }
    }

    /**
     * Move the focus right.
     */
    public void focusRight() {
        ensureFocus();
        int idx = _cols.indexOf(_focussedColumn);
        if (idx < _cols.size() - 1) {
            setFocus(_focussedRow, _cols.get(idx + 1));
        }
    }

    /**
     * Move the focus up.
     */
    public void focusUp() {
        ensureFocus();
        int idx = _rows.indexOf(_focussedRow);
        if (idx > 0) {
            setFocus(_rows.get(idx - 1), _focussedColumn);
        }
    }

    /**
     * Move the focus down.
     */
    public void focusDown() {
        ensureFocus();
        int idx = _rows.indexOf(_focussedRow);
        if (idx < _rows.size() - 1) {
            setFocus(_rows.get(idx + 1), _focussedColumn);
        }
    }

    /**
     * Set the focussed cell by coordinates.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    private void setFocus(int x, int y) {
        IRow row = rowForY(y);
        IColumn col = colForX(x);
        if (col != null && row != null) {
            setFocus(row, col);
        }
    }

    /**
     * Check whether editing of a cell is in progress.
     * 
     * @return true when editing a cell
     */
    public boolean isEditing() {
        return _editor != null;
    }

    /**
     * Handle a single mouseclick by passing it to the cell editor if present.
     * 
     * @param x x coordinate of the click
     * @param y y coordinate of the click
     * @return true if the editor handled the click
     */
    private boolean handleEditorSingleClick(int x, int y) {
        IRow row = rowForY(y);
        IColumn col = colForX(x);
        if (col != null && row != null) {
            ICellEditor editor = getCellEditor(row, col);
            if (editor != null) {
                Rectangle area = getCellBounds(row, col);
                return editor.handleClick(this, row, col, area, x, y);
            }
        }
        return false;
    }

    /**
     * Start editing after a keystroke on a cell.
     * 
     * @param typedKey the typed key
     */
    private void startEditing(char typedKey) {
        if (_focussedRow != null && _focussedColumn != null) {
            startEditing(_focussedRow, _focussedColumn, typedKey);
        }
    }

    /**
     * Start editing of a specified cell if it is editable.
     * 
     * @param row row
     * @param col column
     * @param typedKey key typed
     */
    public void startEditing(IRow row, IColumn col, char typedKey) {
        if (isEditing()) {
            stopEditing(true);
        }
        if (!_model.isEditable(row, col)) {
            return;
        }
        clearSelection();
        if (row != null && col != null) {
            _editor = getCellEditor(row, col);
            if (_editor != null) {
                _editorControl = _editor.getEditorControl(this, row, col, typedKey);
                if (_editorControl != null) {
                    Rectangle bounds = getCellBounds(row, col);
                    // TODO borderwidth
                    bounds.x += 1;
                    bounds.width -= 1;
                    bounds.y += 1;
                    if (_editor.getPreferredHeight() == -1 || _editor.getPreferredHeight() < bounds.height) {
                        bounds.height -= 1;
                    } else {
                        bounds.height = _editor.getPreferredHeight();
                    }
                    _editorControl.setBounds(bounds);
                    _editorControl.setVisible(true);
                    _editorControl.forceFocus();
                }
                _editorRow = row;
            } else {
                // System.out.println("no cell editor found!");
            }
        }
        if (_editorControl == null) {
            stopEditing(true);
        }
    }

    /**
     * Clear the selection.
     */
    private void clearSelection() {
        _selectionModel.clearSelection();
    }

    /**
     * Stop editing if in progress.
     * 
     * @param storeValue if true the value of the editor is stored.
     */
    public void stopEditing(boolean storeValue) {
        if (isEditing()) {
            _editor.stopEditing(storeValue);
            if (storeValue && (_tvs.getRowHeigthMode(_editorRow) == ITableViewState.RowHeightMode.OPTIMAL)
                    || _tvs.getRowHeigthMode(_editorRow) == ITableViewState.RowHeightMode.OPTANDVAR) {
                optimizeHeight(_editorRow);
            }
            _editorRow = null;
            _editor = null;
        }
    }

    /**
     * Set the focussed cell.
     * 
     * @param row row
     * @param col column
     */
    private void setFocus(IRow row, IColumn col) {
        if (_focussedRow != row || _focussedColumn != col) {
            IRow oldRow = _focussedRow;
            IColumn oldCol = _focussedColumn;

            _focussedRow = row;
            _focussedColumn = col;

            if (isCompleteVisible(_focussedRow, _focussedColumn)) {
                redraw(_focussedRow, _focussedColumn);
                if (oldRow != null && oldCol != null) {
                    redraw(oldRow, oldCol);
                }
            } else {
                scrollToVisible(_focussedRow, _focussedColumn); // includes redrawing
            }
            fireTableFocusChanged(row, col);
        }
    }

    /**
     * Calculate the preferred height of a row. Only visibl columns are taken into account.
     * 
     * @param gc Graphics context
     * @param row row to calculate the height for
     * @return preferred height or -1 if no preferred height can be determined
     */
    private int getPreferredRowHeight(GC gc, IRow row) {
        int result = -1;
        for (IColumn column : _cols) {
            if (_tvs.getColumnVisible(column)) {
                ICellRenderer renderer = getCellRenderer(row, column);
                ICellStyle cellStyle = _tvs.getCellStyle(row, column);
                int ph = renderer.getPreferredHeight(gc, cellStyle, _tvs.getColumnWidth(column), row, column);
                if (ph > result) {
                    result = ph;
                }
            }
        }
        return result;
    }

    /** list of rows that will be optimized before the next drawing using the gc at hand. */
    protected Collection<IRow> _rowsToOptimize = Collections.synchronizedCollection(new HashSet<IRow>());

    /**
     * Register a row for height optimization in the next redrwa (redraw triggered by this method).
     * 
     * @param row row to optimize height for
     */
    public void optimizeHeight(IRow row) {
        _rowsToOptimize.add(row);
        syncedRedraw();
    }

    /**
     * Remove a row from the list to optimize.
     * 
     * @param row row to remove from the list
     */
    private void doNotOptimizeHeight(IRow row) {
        _rowsToOptimize.remove(row);
    }

    /**
     * Register a list of rows for heigt optimization.
     * 
     * @param rows list of rows to optimize
     */
    public void optimizeHeight(List<IRow> rows) {
        _rowsToOptimize.addAll(rows);
        syncedRedraw();
    }

    /**
     * Calculates and sets the row height for all rows waiting to be optimized.
     * 
     * @param gc GC for calculation of the heights
     */
    private void doRowHeightOptimization(GC gc) {
        for (IRow row : _rowsToOptimize) {
            int h = getPreferredRowHeight(gc, row);
            if (h != -1) {
                _tvs.setRowHeight(row, h);
            }
        }
        _rowsToOptimize.clear();
    }

    /**
     * Scroll the addressed cell so, that is it completely visible.
     * 
     * @param row row of the cell
     * @param column column of the cell
     */
    public void scrollToVisible(IRow row, IColumn column) {
        // first decide: above the visible area or below?
        int rIdx = _rows.indexOf(row);
        int cellY = getAbsBeginYForRowIdx(rIdx) - getFixedRowsHeight();
        int shownY = getAbsBeginYForRowIdx(_firstRowIdx) + _firstRowPixelOffset - getFixedRowsHeight();
        if (cellY < shownY) {
            if (getVerticalBar() != null) {
                getVerticalBar().setSelection(cellY);
            }
        } else {
            int cellHeight = _tvs.getRowHeight(row);
            if (getVerticalBar() != null) {
                getVerticalBar().setSelection(cellY + cellHeight - _tableRect.height);
            }
        }
        // handleVerticalScroll(null);
        // now left/right
        int cIdx = _cols.indexOf(column);
        int cellX = getAbsBeginXForColIdx(cIdx) - getFixedColumnsWidth();
        int shownX = getAbsBeginXForColIdx(_firstColIdx) - getFixedColumnsWidth();
        if (cellX < shownX) {
            if (getHorizontalBar() != null) {
                getHorizontalBar().setSelection(cellX);
            }
        } else {
            int cellWidth = _tvs.getColumnWidth(column);
            if (getHorizontalBar() != null) {
                getHorizontalBar().setSelection(cellX + cellWidth - _tableRect.width);
            }
        }
        updateScrollBars();
        redraw();
    }

    /**
     * Return true, if the adressed cell is completely (i.e. not clipped) visible.
     * 
     * @param row row of the cell
     * @param column column of the cell
     * @return true if the cell is completely visible
     */
    private boolean isCompleteVisible(IRow row, IColumn column) {
        RowInfo rInfo = getRowInfo(row);
        if (rInfo == null) {
            return false;
        }
        ColInfo cInfo = getColInfo(column);
        if (cInfo == null) {
            return false;
        }
        Rectangle b = getCellBounds(rInfo, cInfo);
        if (!(_tableRect.contains(b.x, b.y) && _tableRect.contains(b.x + b.width, b.y + b.height))) {
            if (_fixedColumns == 0 && _fixedRows == 0) {
                return false;
            } else {
                // may be in a fixed area
                if (_fixedColumns > 0 && _fixedColRect.contains(b.x, b.y)
                        && _fixedColRect.contains(b.x + b.width, b.y + b.height)) {
                    return true;
                }
                if (_fixedRows > 0 && _fixedRowRect.contains(b.x, b.y)
                        && _fixedRowRect.contains(b.x + b.width, b.y + b.height)) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether a row is currently displayed.
     * 
     * @param row row to check
     * @return true if the row is displayed.
     */
    public boolean isDisplayed(IRow row) {
        RowInfo rInfo = getRowInfo(row);
        return rInfo != null;
    }

    /**
     * Check whether a column is currently displayed.
     * 
     * @param column column to check
     * @return true if the column is displayed.
     */
    public boolean isDisplayed(IColumn column) {
        ColInfo cInfo = getColInfo(column);
        return cInfo != null;
    }

    /**
     * Retrieve row by y coordinate of the bottom border.
     * 
     * @param y y coordinate
     * @return a row identified by the bottom delimiter or <code>null</code>
     */
    private IRow rowByBottomBorder(int y) {
        for (RowInfo info : getRowInfos()) {
            int by = info.y + info.height;
            if (Math.abs(by - y) <= SELDELTA) {
                return info.row;
            }
        }
        return null;
    }

    /**
     * Retrieve the row info for a row.
     * 
     * @param row row to get the info for
     * @return info or <code>null</code>
     */
    protected RowInfo getRowInfo(IRow row) {
        for (RowInfo info : getRowInfos()) {
            if (info.row.equals(row)) {
                return info;
            }
        }
        return null;
    }

    /**
     * Retrieve a column by its right border.
     * 
     * @param x x coordinate
     * @return the column or <code>null</code>
     */
    private IColumn colByRightBorder(int x) {
        if (_colInfoCache != null) {
            for (ColInfo info : _colInfoCache) {
                int bx = info.x + info.width;
                if (Math.abs(bx - x) <= SELDELTA) {
                    return info.column;
                }
            }
        }
        return null;
    }

    /**
     * Retrieve cached col info for a row.
     * 
     * @param col column to get the info for
     * @return colInfo or <code>null</code>
     */
    private ColInfo getColInfo(IColumn col) {
        if (_colInfoCache != null) {
            for (ColInfo info : _colInfoCache) {
                if (info.column.equals(col)) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * Get the bounding rectangle for a cell.
     * 
     * @param row row of the cell
     * @param column column of the cell
     * @return the bounding rectangle or <code>null</code> if the cell is not visible
     */
    private Rectangle getCellBounds(IRow row, IColumn column) {
        RowInfo rInfo = getRowInfo(row);
        ColInfo cInfo = getColInfo(column);
        if (rInfo == null || cInfo == null) {
            return null;
        }
        return getCellBounds(rInfo, cInfo);
    }

    /**
     * Get the bounding rectangle for a cell.
     * 
     * @param rInfo row info for the row
     * @param cInfo column info for the row
     * @return the bounding rectangle
     */
    private Rectangle getCellBounds(RowInfo rInfo, ColInfo cInfo) {
        return new Rectangle(cInfo.x, rInfo.y, cInfo.width, rInfo.height);
    }

    /**
     * Retrieve the bounds for a cell.
     * 
     * @param cell cell
     * @return cell bounds or null if the cell is not displayed
     */
    private Rectangle getCellBounds(IJaretTableCell cell) {
        return getCellBounds(cell.getRow(), cell.getColumn());
    }

    /**
     * Get the bounding rect for a row.
     * 
     * @param row row
     * @return bounding rect or <code>null</code> if the row is not visible
     */
    private Rectangle getRowBounds(IRow row) {
        RowInfo rInfo = getRowInfo(row);
        return rInfo == null ? null : getRowBounds(rInfo);
    }

    /**
     * Get bounding rect for a row by the rowinfo.
     * 
     * @param info row info
     * @return bounding rect
     */
    private Rectangle getRowBounds(RowInfo info) {
        int bx = _fixedColumns == 0 ? _tableRect.x : _fixedColRect.x;
        int width = _fixedColumns == 0 ? _tableRect.width : _fixedColRect.width + _tableRect.width;
        return new Rectangle(bx, info.y, width, info.height);
    }

    /**
     * Get the bounding rect for a column.
     * 
     * @param column column
     * @return bounding rect or <code>null</code> if the column is not visible
     */
    public Rectangle getColumnBounds(IColumn column) {
        ColInfo cInfo = getColInfo(column);

        return cInfo != null ? getColumnBounds(cInfo) : null;
    }

    /**
     * Get the bounds for a column.
     * 
     * @param info column info
     * @return bounding rectangle
     */
    private Rectangle getColumnBounds(ColInfo info) {
        int by = _fixedRows == 0 ? _tableRect.y : _fixedRowRect.y;
        int height = _fixedRows == 0 ? _tableRect.height : _fixedRowRect.height + _tableRect.height;
        return new Rectangle(info.x, by, info.width, height);
    }

    /**
     * Redraw a cell. Method can be called from any thread.
     * 
     * @param row row of the cell
     * @param column column of the cell
     */
    private void redraw(IRow row, IColumn column) {
        Rectangle r = getCellBounds(row, column);
        if (r != null) {
            syncedRedraw(r.x, r.y, r.width, r.height);
        }
    }

    /**
     * Redraw a row. Method can be called from any thread.
     * 
     * @param row row to be painted
     */
    private void redraw(IRow row) {
        Rectangle r = getRowBounds(row);
        if (r != null) {
            syncedRedraw(r.x, r.y, r.width, r.height);
        }
    }

    /**
     * Redraw a column. Method can be called from any thread.
     * 
     * @param column column to be repainted
     */
    private void redraw(IColumn column) {
        Rectangle r = getColumnBounds(column);
        if (r != null) {
            syncedRedraw(r.x, r.y, r.width, r.height);
        }
    }

    /**
     * Redraw a region. This method can be called from any thread.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param width width of the region
     * @param height height of the region
     */
    private void syncedRedraw(final int x, final int y, final int width, final int height) {
        Runnable r = new Runnable() {
            public void run() {
                if (!isDisposed()) {
                    redraw(x, y, width, height, true);
                }
            }
        };
        Display.getCurrent().syncExec(r);
    }

    /**
     * Redraw complete area. Safe to call from any thread.
     * 
     */
    private void syncedRedraw() {
        Runnable r = new Runnable() {
            public void run() {
                if (!isDisposed()) {
                    redraw();
                }
            }
        };
        Display.getCurrent().syncExec(r);
    }

    // / end redra methods

    /**
     * Update the scrollbars.
     */
    protected void updateScrollBars() {
        updateYScrollBar();
        updateXScrollBar();
    }

    /**
     * Update the horiontal scrollbar.
     */
    private void updateXScrollBar() {
        if (Display.getCurrent() != null) {
            Display.getCurrent().syncExec(new Runnable() {
                public void run() {
                    ScrollBar scroll = getHorizontalBar();
                    // scroll may be null
                    if (scroll != null) {
                        _oldHorizontalScroll = -1; // make sure no optimization will be applied
                        scroll.setMinimum(0);
                        scroll.setMaximum(getTotalWidth() - getFixedColumnsWidth());
                        scroll.setThumb(getWidth() - getFixedColumnsWidth());
                        scroll.setIncrement(50); // increment for arrows
                        scroll.setPageIncrement(getWidth()); // page increment areas
                    }
                }
            });
        }
    }

    /** last horizontal scroll value for scroll optimization. */
    private int _oldHorizontalScroll = -1;
    /** last vertical scroll value for scroll optimization. */
    private int _oldVerticalScroll = -1;

    /** if true use optimized scrolling. */
    private boolean _optimizeScrolling = true;

    /**
     * Handle a change of the horizontal scrollbar.
     * 
     * @param event SelectionEvent
     */
    private void handleHorizontalScroll(SelectionEvent event) {
        int value = getHorizontalBar().getSelection() + getFixedColumnsWidth();
        int colIdx = getColIdxForAbsX(value);
        int offset = value - getAbsBeginXForColIdx(colIdx);
        int diff = _oldHorizontalScroll - value;

        if (Math.abs(diff) > _tableRect.width / 2 || _oldHorizontalScroll == -1 || !_optimizeScrolling) {
            _firstColIdx = colIdx;
            _firstColPixelOffset = offset;
            redraw();
        } else {
            if (diff > 0) {
                scroll(_tableRect.x + diff, 0, _tableRect.x, 0, _tableRect.width - diff, getHeight(), false);
            } else {
                diff = -diff;
                scroll(_tableRect.x, 0, _tableRect.x + diff, 0, _tableRect.width - diff, getHeight(), false);
            }
            _firstColIdx = colIdx;
            _firstColPixelOffset = offset;
        }
        _oldHorizontalScroll = value;
    }

    /**
     * Update the vertical scrollbar if present.
     */
    public void updateYScrollBar() {
        if (Display.getCurrent() != null) {
            Display.getCurrent().syncExec(new Runnable() {
                public void run() {
                    ScrollBar scroll = getVerticalBar();
                    // scroll may be null
                    if (scroll != null) {
                        _oldVerticalScroll = -1; // guarantee a clean repaint
                        scroll.setMinimum(0);
                        scroll.setMaximum(getTotalHeight() - getFixedRowsHeight());
                        int height = getHeight();
                        if (_tableRect != null) {
                            height = _tableRect.height;
                        }
                        scroll.setThumb(height); // - getFixedRowsHeight() - getHeaderHeight());
                        scroll.setIncrement(50); // increment for arrows
                        scroll.setPageIncrement(getHeight()); // page increment areas
                        scroll.setSelection(getAbsBeginYForRowIdx(_firstRowIdx) + _firstRowPixelOffset
                                + getFixedRowsHeight());
                    }
                }
            });
        }
    }

    /**
     * Handle a selection on the vertical scroll bar (a vertical scroll).
     * 
     * @param event selection
     */
    private void handleVerticalScroll(SelectionEvent event) {
        int value = getVerticalBar().getSelection() + getFixedRowsHeight();
        int rowidx = getRowIdxForAbsY(value);
        int offset = value - getAbsBeginYForRowIdx(rowidx);
        int oldFirstIdx = _firstRowIdx;
        int oldPixelOffset = _firstRowPixelOffset;

        int diff = _oldVerticalScroll - value;

        if (Math.abs(diff) > _tableRect.height / 2 || _oldVerticalScroll == -1 || !_optimizeScrolling) {
            update();
            _firstRowIdx = rowidx;
            _firstRowPixelOffset = offset;
            _rowInfoCache = null; // kill the cache
            redraw();
        } else {
            Rectangle completeArea = new Rectangle(_fixedColRect.x, _tableRect.y, _fixedColRect.width
                    + _tableRect.width, _tableRect.height);

            if (diff > 0) {
                scroll(0, completeArea.y + diff, 0, completeArea.y, getWidth(), completeArea.height - diff, false);
            } else {
                diff = -diff;
                scroll(0, completeArea.y, 0, completeArea.y + diff, getWidth(), completeArea.height - diff, false);
            }
            _firstRowIdx = rowidx;
            _firstRowPixelOffset = offset;
            _rowInfoCache = null; // kill the cache
        }
        _oldVerticalScroll = value;
        firePropertyChange(PROPERTYNAME_FIRSTROWIDX, oldFirstIdx, _firstRowIdx);
        firePropertyChange(PROPERTYNAME_FIRSTROWPIXELOFFSET, oldPixelOffset, _firstRowPixelOffset);
    }

    /**
     * Get the absolute begin x for a column.
     * 
     * @param colIdx index of the column (in the displayed columns)
     * @return the absolute x coordinate
     */
    public int getAbsBeginXForColIdx(int colIdx) {
        int x = 0;
        for (int idx = 0; idx < colIdx; idx++) {
            IColumn col = _cols.get(idx);
            int colWidth = _tvs.getColumnWidth(col);
            x += colWidth;
        }
        return x;
    }

    /**
     * Get the absolute begin x for a column.
     * 
     * @param column the column
     * @return the absolute x coordinate
     */
    public int getAbsBeginXForColumn(IColumn column) {
        return getAbsBeginXForColIdx(_cols.indexOf(column));
    }

    /**
     * Retrieve the beginning x coordinate for a column.
     * 
     * @param column column
     * @return beginning x coordinate for drawing that column
     */
    private int xForCol(IColumn column) {
        int x = getAbsBeginXForColIdx(_cols.indexOf(column));
        int begin = getAbsBeginXForColIdx(_firstColIdx) + _firstColPixelOffset;
        return x - begin + _fixedColRect.width;
    }

    /**
     * Return the (internal) index of the column corresponding to the given x coordinate value (absolute value taking
     * all visible columns into account).
     * 
     * @param absX absolute x coordinate
     * @return the column index in the internal list of columns (or -1 if none could be determined)
     */
    public int getColIdxForAbsX(int absX) {
        int idx = 0;
        int x = 0;
        while (x <= absX && idx < _cols.size()) {
            IColumn col = _cols.get(idx);
            int colWidth = _tvs.getColumnWidth(col);
            x += colWidth;
            idx++;
        }
        return idx < _cols.size() ? idx - 1 : -1;
    }

    /**
     * Return the column for an absolute x coordinate.
     * 
     * @param absX absolute x coordinate
     * @return the column for the coordinate
     */
    public IColumn getColumnForAbsX(int absX) {
        return _cols.get(getColIdxForAbsX(absX));
    }

    /**
     * Return the absolute y coordinate for the given row (given by index).
     * 
     * @param rowidx index of the row
     * @return absolute y coordinate (of all rows) for the row
     */
    private int getAbsBeginYForRowIdx(int rowidx) {
        int y = 0;
        for (int idx = 0; idx < rowidx; idx++) {
            IRow row = _rows.get(idx);
            int rowHeight = _tvs.getRowHeight(row);
            y += rowHeight;
        }
        return y;
    }

    /**
     * Get row index for an absolute y coordinate (thought on the full height table with all rows).
     * 
     * @TODO optimize
     * @param absY absolute y position (thought on the full table height)
     * @return index of the corresponding row
     */
    public int getRowIdxForAbsY(int absY) {
        int idx = 0;
        int y = 0;
        while (y <= absY) {
            IRow row = _rows.get(idx);
            int rowHeight = _tvs.getRowHeight(row);
            y += rowHeight;
            idx++;
        }
        return idx - 1;
    }

    /**
     * Retrie internal index of gicen row.
     * 
     * @param row row
     * @return internal index or -1 if the row is not in the internal list
     */
    private int getRowIdx(IRow row) {
        return row != null ? _rows.indexOf(row) : -1;
    }

    /**
     * Retrieve the row corresponding to a specified y coordinate.
     * 
     * @param y y
     * @return row for that y ycoordinate or <code>null</code> if no row could be determined.
     */
    public IRow rowForY(int y) {
        if ((y < _tableRect.y || y > _tableRect.y + _tableRect.height) && _fixedRows == 0) {
            return null;
        }

        for (RowInfo rInfo : getRowInfos()) {
            if (y >= rInfo.y && y < rInfo.y + rInfo.height) {
                return rInfo.row;
            }
        }
        return null;
    }

    /**
     * Retrive the list of currently availbale RowInfo etries.
     * 
     * @return list of rowinfos
     */
    private List<RowInfo> getRowInfos() {
        if (_rowInfoCache == null) {
            fillRowInfoCache();
        }
        return _rowInfoCache;
    }

    /**
     * Fill the cache of row infos for the current viewconfiguration.
     */
    private void fillRowInfoCache() {
        if (_rowInfoCache != null) {
            return;
        }
        _rowInfoCache = new ArrayList<RowInfo>();

        // fixed row area
        int y = 0;
        if (_fixedRows > 0) {
            y = _fixedRowRect.y;
            for (int rIdx = 0; rIdx < _fixedRows; rIdx++) {
                IRow row = _rows.get(rIdx);
                int rHeight = _tvs.getRowHeight(row);
                _rowInfoCache.add(new RowInfo(row, y, rHeight, true));
                y += rHeight;
            }

        }
        // normal table area
        int rIdx = _firstRowIdx;
        y = _tableRect.y - _firstRowPixelOffset;

        while (y < getHeight() && rIdx < _rows.size()) {
            IRow row = _rows.get(rIdx);
            int rHeight = _tvs.getRowHeight(row);
            _rowInfoCache.add(new RowInfo(row, y, rHeight, false));
            y += rHeight;
            rIdx++;
        }
    }

    /**
     * Retrieve a row from the internal list of rows.
     * 
     * @param idx index in the internal list
     * @return the row for idx
     */
    private IRow rowForIdx(int idx) {
        return _rows.get(idx);
    }

    /**
     * Retrieve the column corresponding to a x coordinate.
     * 
     * @param x x
     * @return the corresponding column or <code>null</code> if none could be determined
     */
    public IColumn colForX(int x) {
        if ((x < _tableRect.x || x > _tableRect.x + _tableRect.width) && _fixedColumns == 0) {
            return null;
        }
        for (ColInfo cInfo : _colInfoCache) {
            if (x >= cInfo.x && x < cInfo.x + cInfo.width) {
                return cInfo.column;
            }
        }
        return null;
    }

    /**
     * Get the column for a given index.
     * 
     * @param idx index
     * @return tghe column
     */
    private IColumn colForIdx(int idx) {
        return _cols.get(idx);
    }

    /**
     * Get the index of a given column.
     * 
     * @param column column
     * @return the index of the column or -1 if no index could be given
     */
    private int getColumnIdx(IColumn column) {
        return column != null ? _cols.indexOf(column) : -1;
    }

    /**
     * Retrieve TableXCell for given pixel coordinates.
     * 
     * @param x pixel coordinate x
     * @param y pixel coordinate y
     * @return table cel if found or <code>null</code> if no cell can be found
     */
    public IJaretTableCell getCell(int x, int y) {
        if (_tableRect.contains(x, y)) {
            IRow row = rowForY(y);
            IColumn col = colForX(x);
            if (row == null || col == null) {
                return null;
            }
            return new JaretTableCellImpl(row, col);
        }
        return null;
    }

    /**
     * Retrieve a table cell for given index coordinates.
     * 
     * @param colIdx column index (X)
     * @param rowIdx row index (Y)
     * @return table cell
     */
    public IJaretTableCell getCellForIdx(int colIdx, int rowIdx) {
        return new JaretTableCellImpl(rowForIdx(rowIdx), colForIdx(colIdx));
    }

    /**
     * Set a table model to be displayed by the jaret table.
     * 
     * @param model the table model to be displayed.
     */
    public void setTableModel(IJaretTableModel model) {
        if (_model != null) {
            _model.removeJaretTableModelListener(this);
        }
        _model = model;
        _model.addJaretTableModelListener(this);

        _hierarchicalModel = null;

        // update the sorted columnlist
        List<IColumn> cList = new ArrayList<IColumn>();
        for (int i = 0; i < model.getColumnCount(); i++) {
            cList.add(model.getColumn(i));
        }
        _tvs.setSortedColumns(cList);

        updateColumnList();
        registerRowsForOptimization();
        updateRowList();

        updateYScrollBar();
        updateXScrollBar();
        redraw();
    }

    /**
     * Set a hierarchical table model. This will internally create a StdHierrahicalTableModel that is a normal
     * TbaleModel incluuding only the expanded rows.
     * 
     * @param hmodel hierarchical model to display
     */
    public void setTableModel(IHierarchicalJaretTableModel hmodel) {
        if (_model != null) {
            _model.removeJaretTableModelListener(this);
        }
        if (_tvs != null) {
            _tvs.removeTableViewStateListener(this);
        }
        _tvs = new DefaultHierarchicalTableViewState();
        _tvs.addTableViewStateListener(this);
        _model = new StdHierarchicalTableModel(hmodel, (IHierarchicalTableViewState) _tvs);
        _model.addJaretTableModelListener(this);

        _hierarchicalModel = hmodel;

        updateColumnList();
        registerRowsForOptimization();
        updateRowList();
        updateColumnList();
        updateYScrollBar();
        updateXScrollBar();
        redraw();
    }

    /**
     * Retrieve a hierarchical model if set.
     * 
     * @return hierarchical model or <code>null</code>
     */
    public IHierarchicalJaretTableModel getHierarchicalModel() {
        return _hierarchicalModel;
    }

    /**
     * Retrieve the displayed table model.
     * 
     * @return the table model
     */
    public IJaretTableModel getTableModel() {
        return _model;
    }

    /**
     * Add a column to the underlying table model. Model has to be set for that operation or an IllegalStateException
     * will be thrown.
     * 
     * @param column column to be added
     */
    public void addColumn(IColumn column) {
        if (_model != null) {
            _model.addColumn(column);
        } else {
            throw new IllegalStateException("model has to be set for the operation.");
        }
    }

    /**
     * Registers all rows in the model for optimization that have a mode indicating optimal height.
     * 
     */
    private void registerRowsForOptimization() {
        if (_model != null) {
            for (int i = 0; i < _model.getRowCount(); i++) {
                IRow row = _model.getRow(i);
                if (_tvs.getRowHeigthMode(row) == ITableViewState.RowHeightMode.OPTANDVAR
                        || _tvs.getRowHeigthMode(row) == ITableViewState.RowHeightMode.OPTIMAL) {
                    optimizeHeight(row);
                }
            }
        }
    }

    /**
     * Update the internal rowlist according to filter and sorter.
     * 
     */
    private void updateRowList() {
        _rows = new ArrayList<IRow>();
        if (_model != null) {
            for (int i = 0; i < _model.getRowCount(); i++) {
                IRow row = _model.getRow(i);
                if (i < _fixedRows) {
                    // fixed rows are exluded from filtering
                    _rows.add(row);
                } else if (_rowFilter == null || (_rowFilter != null && _rowFilter.isInResult(row))) {
                    if (!_autoFilterEnabled || _autoFilter.isInResult(row)) {
                        if (!_rows.contains(row)) {
                            _rows.add(row);
                        }
                    }
                }
            }
        }

        // sort either by column sort order or by a set row sorter
        RowComparator comparator = new RowComparator();
        IRowSorter rs = null;
        if (comparator.canSort()) {
            rs = comparator;
        } else {
            rs = _rowSorter;
        }
        if (rs != null) {
            List<IRow> fixedRows = new ArrayList<IRow>();
            // the exclusion of the fixed rows may be solved more elegant ...
            if (_excludeFixedRowsFromSorting) {
                for (int i = 0; i < _fixedRows; i++) {
                    fixedRows.add(_rows.remove(0));
                }
            }
            Collections.sort(_rows, rs);
            if (_excludeFixedRowsFromSorting) {
                for (int i = fixedRows.size() - 1; i >= 0; i--) {
                    _rows.add(0, fixedRows.get(i));
                }
            }
        }
        // to be sure no one misses this
        updateYScrollBar();
    }

    /**
     * Get the index of the given row in the internal, fileterd list of rows.
     * 
     * @param row row to retrieve the index for
     * @return index of the row or -1 if the row is not in filtered list of rows
     */
    public int getInternalRowIndex(IRow row) {
        return _rows.indexOf(row);
    }

    /**
     * Comparator based on the sorting settings of the columns.
     * 
     * @author Peter Kliem
     * @version $Id: JaretTable.java,v 1.1 2012-05-07 01:34:37 jason Exp $
     */
    public class RowComparator extends PropertyObservableBase implements IRowSorter {
        /** arary of Row comparators (IColumns are Comparators for rows!). */
        private List<Comparator<IRow>> _comparators = new ArrayList<Comparator<IRow>>();

        /**
         * Construct it. Initializes itself with the columns.
         */
        public RowComparator() {
            IColumn[] arr = new IColumn[_cols.size()];
            int max = 0;
            for (IColumn col : _cols) {
                int sortP = _tvs.getColumnSortingPosition(col);
                if (sortP > 0) {
                    arr[sortP] = col;
                    if (sortP > max) {
                        max = sortP;
                    }
                }
            }
            for (int i = 1; i <= max; i++) {
                _comparators.add(arr[i]);
            }

        }

        /**
         * Check whether the comparator is able to sort.
         * 
         * @return true if comparators are present
         */
        public boolean canSort() {
            return _comparators.size() > 0;
        }

        /**
         * {@inheritDoc}
         */
        public int compare(IRow r1, IRow r2) {
            for (Comparator<IRow> comp : _comparators) {
                int res = comp.compare(r1, r2);
                res = _tvs.getColumnSortingDirection((IColumn) comp) ? res : -res;
                if (res != 0) {
                    return res;
                }
            }
            return 0;
        }

    }

    /**
     * Update the internal column list. Should be called whenever a column changes visibility or the column order has
     * been changed.
     * 
     */
    public void updateColumnList() {
        _cols = new ArrayList<IColumn>();
        // these are columns to take into account
        for (int i = 0; i < _model.getColumnCount(); i++) {
            if (i < _tvs.getSortedColumns().size()) {
                IColumn col = _tvs.getSortedColumns().get(i);
                if (_tvs.getColumnVisible(col)) {
                    _cols.add(col);
                }
            }
        }
        // if not all columns have been in the sorted columns - add the other columns
        for (int i = 0; i < _model.getColumnCount(); i++) {
            IColumn col = _model.getColumn(i);
            if (!_cols.contains(col) && _tvs.getColumnVisible(col)) {
                _cols.add(col);
            }
        }
    }

    /**
     * Handling of the paint event -> do the painting.
     * 
     * @param event PaintEvent
     */
    private void onPaint(PaintEvent event) {
        if (event.width == 0 || event.height == 0) {
            return;
        }
// System.out.println("Paint event "+event);
        long time = System.currentTimeMillis();
        // kill the cache
        _rowInfoCache = null;
        GC gc = event.gc; // gc for painting

        // do rowheight optimizations for registered rows
        doRowHeightOptimization(gc);

        // do the actual painting
        paint(gc, getWidth(), getHeight());
        if (DEBUGPAINTTIME) {
            System.out.println("time " + (System.currentTimeMillis() - time) + " ms");
        }
    }

    /**
     * Calculate the layout of the table area rectangles.
     * 
     * @param width width of the table
     * @param height height of the table
     */
    private void preparePaint(int width, int height) {
        if (_drawHeader) {
            _headerRect = new Rectangle(0, 0, width, _headerHeight);
        } else {
            _headerRect = new Rectangle(0, 0, 0, 0);
        }

        if (_autoFilterEnabled) {
            // preferred height of the autofilters
            int autoFilterHeight = getPreferredAutoFilterHeight();
            _autoFilterRect = new Rectangle(0, _headerRect.y + _headerRect.height, _headerRect.width, autoFilterHeight);
            _tableRect = new Rectangle(0, _autoFilterRect.y + _autoFilterRect.height, width, height
                    - _autoFilterRect.height);
        } else {
            _tableRect = new Rectangle(0, _headerRect.y + _headerRect.height, width, height - _headerRect.height);
        }

        // do we have fixed cols? correct other rects and calc fixed col rect
        if (_fixedColumns > 0) {
            int fWidth = getFixedColumnsWidth();
            _headerRect.x = _headerRect.x + fWidth;
            _headerRect.width = _headerRect.width - fWidth;

            if (_autoFilterEnabled) {
                _autoFilterRect.x = _headerRect.x;
                _autoFilterRect.width = _headerRect.width;
            }

            _tableRect.x = _headerRect.x;
            _tableRect.width = _headerRect.width;

            _fixedColRect = new Rectangle(0, _tableRect.y, fWidth, _tableRect.height);

        } else {
            _fixedColRect = new Rectangle(_tableRect.x, _tableRect.y, 0, _tableRect.height);
        }

        // do we have fixed rows? correct other rects nd setup the fixed row rect
        if (_fixedRows > 0) {
            int fHeight = getFixedRowsHeight();
            if (_autoFilterEnabled) {
                _fixedRowRect = new Rectangle(0, _autoFilterRect.y + _autoFilterRect.height, width,
                        getFixedRowsHeight());
            } else {
                _fixedRowRect = new Rectangle(0, _headerRect.y + _headerRect.height, width, getFixedRowsHeight());
            }

            _tableRect.y = _tableRect.y + fHeight;
            _tableRect.height = _tableRect.height - fHeight;
            if (_fixedColumns > 0) {
                _fixedColRect.y = _tableRect.y;
                _fixedColRect.height = _tableRect.height;
            }
        } else {
            // ensure fixed Row rect is available
            _fixedRowRect = new Rectangle(_tableRect.x, _tableRect.y, _tableRect.width, 0);
        }

    }

    /**
     * Retrieve the maximum preferred height of the autofilter controls.
     * 
     * @return preferred height for the autofilters
     */
    private int getPreferredAutoFilterHeight() {
        int result = 0;
        for (IAutoFilter af : _autoFilterMap.values()) {
            int height = af.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
            if (height > result) {
                result = height;
            }
        }
        return result;
    }

    /**
     * The main paint method.
     * 
     * @param gc GC
     * @param width width of the control
     * @param height height of the control
     */
    public void paint(GC gc, int width, int height) {
        preparePaint(width, height);

        // clear bg
        Color bg = gc.getBackground();
        gc.setBackground(getBackground());
        gc.fillRectangle(gc.getClipping());
        gc.setBackground(bg);

        drawHeader(gc, width, height);
        drawTableArea(gc, width, height);
        // set the bounds for the output filters
        setUpAutoFilter(gc);

        // additional rendering
        if (_isFillDrag) {
            drawFillDragBorder(gc);
        }

    }

    /**
     * Setup the autofilter components.
     * 
     * @param gc GC
     */
    private void setUpAutoFilter(GC gc) {
        if (_autoFilterEnabled) {
            for (IColumn column : _cols) {
                IAutoFilter af = _autoFilterMap.get(column);
                ColInfo cInfo = getColInfo(column);
                if (af != null && cInfo == null) {
                    af.getControl().setVisible(false);
                } else {
                    if (af != null) {
                        af.getControl().setVisible(true);
                        af.getControl().setBounds(cInfo.x, _autoFilterRect.y, cInfo.width, _autoFilterRect.height);
                    }
                }
            }
        } else {
            for (IColumn column : _cols) {
                IAutoFilter af = _autoFilterMap.get(column);
                if (af != null) {
                    af.getControl().setVisible(false);
                }
            }
        }
    }

    /**
     * Draw the table header.
     * 
     * @param gc gc
     * @param width width of the table
     * @param height height of the table
     */
    private void drawHeader(GC gc, int width, int height) {
        if (_headerRenderer != null && _drawHeader) {
            // draw headers for fixed columns
            for (int cIdx = 0; cIdx < _fixedColumns; cIdx++) {
                int x = getAbsBeginXForColIdx(cIdx);
                IColumn col = _cols.get(cIdx);
                int colwidth = _tvs.getColumnWidth(col);
                // fixed cols may render wherever they want if they disable clipping
                if (!_headerRenderer.disableClipping()) {
                    gc.setClipping(x, _headerRect.y, colwidth, _headerRect.height);
                }
                // column may indicate that no header should be painted
                if (col.displayHeader()) {
                    drawHeader(gc, x, colwidth, col);
                }
            }

            // draw headers table area
            int x = -_firstColPixelOffset;
            x += _tableRect.x;
            int cIdx = _firstColIdx;
            while (x < getWidth() && cIdx < _cols.size()) {
                IColumn col = _cols.get(cIdx);
                int colwidth = _tvs.getColumnWidth(col);
                int xx = x > _headerRect.x ? x : _headerRect.x;
                int clipWidth = x > _headerRect.x ? colwidth : colwidth - _firstColPixelOffset;
                if (!_headerRenderer.disableClipping()) {
                    gc.setClipping(xx, _headerRect.y, clipWidth, _headerRect.height);
                } else if (_fixedColumns > 0) {
                    // if fixed columns are present the header renderer of ordinary columns may not interfere with the
                    // fixed region
                    gc.setClipping(xx, _headerRect.y, _tableRect.width - xx, _headerRect.height);
                }

                // column may indicate that no header should be painted
                if (col.displayHeader()) {
                    drawHeader(gc, x, colwidth, col);
                }

                x += colwidth;
                cIdx++;
            }
        }

    }

    /**
     * Render the header for one column.
     * 
     * @param gc gc
     * @param x starting x
     * @param colwidth width
     * @param col column
     */
    private void drawHeader(GC gc, int x, int colwidth, IColumn col) {
        Rectangle area = new Rectangle(x, _headerRect.y, colwidth, _headerRect.height);
        int sortingPos = _tvs.getColumnSortingPosition(col);
        boolean sortingDir = _tvs.getColumnSortingDirection(col);
        _headerRenderer.draw(gc, area, col, sortingPos, sortingDir, false);
    }

    /**
     * Convenience method to check whether a certain cell is selected.
     * 
     * @param row row of the cell
     * @param column column of the cell
     * @return true if the cell is selected (by itself, a row of a column selection)
     */
    public boolean isSelected(IRow row, IColumn column) {
        if (_selectionModel.getSelection().getSelectedRows().contains(row)) {
            return true;
        }
        if (_selectionModel.getSelection().getSelectedColumns().contains(column)) {
            return true;
        }
        IJaretTableCell cell = new JaretTableCellImpl(row, column);
        if (_selectionModel.getSelection().getSelectedCells().contains(cell)) {
            return true;
        }
        return false;
    }

    /**
     * Draw the main table area including fixed rows and columns.
     * 
     * @param gc gc
     * @param width width of the area
     * @param height height of the area
     */
    private void drawTableArea(GC gc, int width, int height) {
        _colInfoCache.clear();
        boolean colCacheFilled = false;

        Rectangle clipSave = gc.getClipping();

        // iterate over all rows in the row info cache
        for (RowInfo rowInfo : getRowInfos()) {
            int y = rowInfo.y;
            IRow row = rowInfo.row;
            int rHeight = _tvs.getRowHeight(row);

            int yclip = y;
            if (rowInfo.fixed && yclip < _fixedRowRect.y) {
                yclip = _fixedRowRect.y;
            } else if (!rowInfo.fixed && yclip < _tableRect.y) {
                yclip = _tableRect.y;
            }

            int x = 0;
            // fixed columns
            if (_fixedColumns > 0) {
                x = _fixedColRect.x;
                for (int cIdx = 0; cIdx < _fixedColumns; cIdx++) {
                    IColumn col = _cols.get(cIdx);
                    int colwidth = _tvs.getColumnWidth(col);
                    if (!colCacheFilled) {
                        _colInfoCache.add(new ColInfo(col, x, colwidth));
                    }
                    // clipping is extended by 1 for border drawing
                    gc.setClipping(x, yclip, colwidth + 1, rHeight + 1);
                    Rectangle area = new Rectangle(x, y, colwidth, rHeight);

                    drawCell(gc, area, row, col);

                    x += colwidth;
                }
            }

            // columns in normal table area
            x = _tableRect.x - _firstColPixelOffset;
            int cIdx = _firstColIdx;
            while (x < getWidth() && cIdx < _cols.size()) {
                IColumn col = _cols.get(cIdx);
                int colwidth = _tvs.getColumnWidth(col);
                if (!colCacheFilled) {
                    _colInfoCache.add(new ColInfo(col, x, colwidth));
                }
                int xx = x > _tableRect.x ? x : _tableRect.x;
                int clipWidth = x > _tableRect.x ? colwidth : colwidth - _firstColPixelOffset;
                // clipping is extended by 1 for border drawing
                gc.setClipping(xx, yclip, clipWidth + 1, rHeight + 1);
                Rectangle area = new Rectangle(x, y, colwidth, rHeight);

                drawCell(gc, area, row, col);

                x += colwidth;
                cIdx++;
            }
            colCacheFilled = true;
        }

        // TODO this is a workaround for the autofilter to be rendered correctly if the
        // filter result is no rows
        if (!colCacheFilled) {
            // fixed cols
            int x = 0;
            if (_fixedColumns > 0) {
                for (int i = 0; i < _fixedColumns; i++) {
                    IColumn col = _cols.get(i);
                    int colwidth = _tvs.getColumnWidth(col);
                    _colInfoCache.add(new ColInfo(col, x, colwidth));
                    x += colwidth;
                }
            }

            x = -_firstColPixelOffset;
            x += _tableRect.x;
            int cIdx = _firstColIdx;
            while (x < getWidth() && cIdx < _cols.size()) {
                IColumn col = _cols.get(cIdx);
                int colwidth = _tvs.getColumnWidth(col);
                _colInfoCache.add(new ColInfo(col, x, colwidth));
                x += colwidth;
                cIdx++;
            }

        }

        // draw extra lines to separate fixed areas
        if (_fixedColRect != null && _fixedColumns > 0) {
            int maxY = _fixedColRect.y + _fixedColRect.height - 1;
            if (_rows != null && _rows.size() > 0) {
                IRow lastRow = _rows.get(_rows.size() - 1);
                Rectangle bounds = getRowBounds(lastRow);
                if (bounds != null) {
                    maxY = bounds.y + bounds.height;
                }
            }
            gc.setClipping(new Rectangle(0, 0, width, height));
            int fx = _fixedColRect.x + _fixedColRect.width - 1;
            gc.drawLine(fx, _fixedRowRect.y, fx, maxY);
            gc.setClipping(clipSave);
        }

        if (_fixedRowRect != null && _fixedRows > 0) {
            int maxX = _fixedRowRect.x + _fixedRowRect.width - 1;
            if (_cols != null && _cols.size() > 0) {
                IColumn lastCol = _cols.get(_cols.size() - 1);
                int mx = xForCol(lastCol) + _tvs.getColumnWidth(lastCol);
                maxX = mx;
            }
            gc.setClipping(new Rectangle(0, 0, width, height));
            int fy = _fixedRowRect.y + _fixedRowRect.height - 1;
            gc.drawLine(_fixedRowRect.x, fy, maxX, fy);
            gc.setClipping(clipSave);
        }

    }

    /**
     * Draw a single cell. Drawing is accomplished by the associated cell renderer. However the mark for fill dragging
     * is drawn by this method.
     * 
     * @param gc gc
     * @param area drawing area the cell takes up
     * @param row row of the cell
     * @param col olumn of the cell
     */
    private void drawCell(GC gc, Rectangle area, IRow row, IColumn col) {
        ICellStyle bc = _tvs.getCellStyle(row, col);
        ICellRenderer cellRenderer = getCellRenderer(row, col);

        boolean hasFocus = false;
        if (_focussedRow == row && _focussedColumn == col) { // == is appropriate: these are really the same objects!
            hasFocus = true;
        }
        boolean isSelected = isSelected(row, col);
        cellRenderer.draw(gc, this, bc, area, row, col, hasFocus, isSelected, false);
        if (_supportFillDragging && isSelected && isDragMarkerCell(row, col)) {
            drawFillDragMark(gc, area);
        }
    }

    /** if a rectangular area is selected, this holds the rectangle ofth eindizes. */
    private Rectangle _selectedIdxRectangle = null;

    /**
     * Retrieve the index rectangle of selected cells.
     * 
     * @return rectangel made from the indizes of the selected cells if a rectangular area is selected (all cells)
     */
    private Rectangle getSelectedRectangle() {
        IJaretTableSelection selection = getSelectionModel().getSelection();
        if (!selection.isEmpty() && _selectedIdxRectangle == null) {
            Set<IJaretTableCell> cells = selection.getAllSelectedCells(getTableModel());
            int minx = -1;
            int maxx = -1;
            int miny = -1;
            int maxy = -1;
            // line is the outer map
            Map<Integer, Map<Integer, IJaretTableCell>> cellMap = new HashMap<Integer, Map<Integer, IJaretTableCell>>();
            for (IJaretTableCell cell : cells) {
                Point p = getCellDisplayIdx(cell);
                Map<Integer, IJaretTableCell> lineMap = cellMap.get(p.y);
                if (lineMap == null) {
                    lineMap = new HashMap<Integer, IJaretTableCell>();
                    cellMap.put(p.y, lineMap);
                }
                if (miny == -1 || p.y < miny) {
                    miny = p.y;
                }
                if (maxy == -1 || p.y > maxy) {
                    maxy = p.y;
                }
                lineMap.put(p.x, cell);
                if (minx == -1 || p.x < minx) {
                    minx = p.x;
                }
                if (maxx == -1 || p.x > maxx) {
                    maxx = p.x;
                }
            }
            // check if all cells are selected
            boolean everythingSelected = true;
            for (int y = miny; y <= maxy && everythingSelected; y++) {
                Map<Integer, IJaretTableCell> lineMap = cellMap.get(y);
                if (lineMap != null) {
                    for (int x = minx; x <= maxx; x++) {
                        IJaretTableCell cell = lineMap.get(x);
                        if (cell == null) {
                            everythingSelected = false;
                            break;
                        }
                    }
                } else {
                    everythingSelected = false;
                    break;
                }
            }
            if (everythingSelected) {
                _selectedIdxRectangle = new Rectangle(minx, miny, maxx - minx + 1, maxy - miny + 1);
            } else {
                _selectedIdxRectangle = null;
            }
        }
        return _selectedIdxRectangle;
    }

    /**
     * Check whether a cell is the cell that should currently be marked with the drag fill marker.
     * 
     * @param row row of the cell
     * @param col column of the cell
     * @return true if it is the cell carrying the drag mark
     */
    private boolean isDragMarkerCell(IRow row, IColumn col) {
        Rectangle selIdxRect = getSelectedRectangle();
        if (selIdxRect != null) {
            if (selIdxRect.width == 1 || selIdxRect.height == 1) {
                int x = getColumnIdx(col);
                int y = getRowIdx(row);
                if (x == selIdxRect.x + selIdxRect.width - 1 && y == selIdxRect.y + selIdxRect.height - 1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Draws the fill drag mark.
     * 
     * @param gc GC
     * @param area drawing area of the cell carrying the marker
     */
    private void drawFillDragMark(GC gc, Rectangle area) {
        Color bg = gc.getBackground();
        gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
        _dragMarkerRect = new Rectangle(area.x + area.width - FILLDRAGMARKSIZE,
                area.y + area.height - FILLDRAGMARKSIZE, FILLDRAGMARKSIZE, FILLDRAGMARKSIZE);
        gc.fillRectangle(_dragMarkerRect);
        gc.setBackground(bg);
    }

    /**
     * Draws a thicker border around the fill drag area.
     * 
     * @param gc GC
     */
    private void drawFillDragBorder(GC gc) {
        // TODO
        // if (_firstCellSelectX != -1) {
        // IRow row = rowForIdx(_firstCellSelectX);
        // IColumn column = colForIdx(_firstCellSelectY);
        // Rectangle firstCellBounds = getCellBounds(row, column);
        // if (firstCellBounds == null) {
        // firstCellBounds = new Rectangle(-1, -1, -1, -1);
        // if (getRowBounds(row) != null) {
        // firstCellBounds = getRowBounds(row);
        // } else if (getColumnBounds(column)!=null) {
        // firstCellBounds = getColumnBounds(column);
        // } else
        // }
        // }
    }

    /**
     * Set the header drawing height.
     * 
     * @param newHeight height in pixel.
     */
    public void setHeaderHeight(int newHeight) {
        if (newHeight != _headerHeight) {
            int oldVal = _headerHeight;
            _headerHeight = newHeight;
            redraw();
            firePropertyChange(PROPERTYNAME_HEADERHEIGHT, oldVal, _headerHeight);
        }
    }

    /**
     * Retrieve the header height.
     * 
     * @return header height (pixel)
     */
    public int getHeaderHeight() {
        return _headerHeight;
    }

    /**
     * Total height of all possibly displayed rows (filter applied!).
     * 
     * @return sum of all rowheigths
     */
    public int getTotalHeight() {
        if (_rows != null) {
            int h = 0;
            for (IRow row : _rows) {
                h += _tvs.getRowHeight(row);
            }
            return h;
        } else {
            return 0;
        }
    }

    /**
     * Total height of the first n rows.
     * 
     * @param numRows number of rows to sum up the heights of
     * @return sum of the first first n rowheights
     */
    public int getTotalHeight(int numRows) {
        if (_rows != null) {
            int h = 0;
            for (int i = 0; i < numRows; i++) {
                IRow row = _rows.get(i);
                h += _tvs.getRowHeight(row);
            }
            return h;
        } else {
            return 0;
        }
    }

    /**
     * Retrieve total width of all possibly displayed columns.
     * 
     * @return sum of colwidhts
     */
    public int getTotalWidth() {
        if (_cols != null) {
            int width = 0;
            for (IColumn col : _cols) {
                width += _tvs.getColumnWidth(col);
            }
            return width;
        } else {
            return 0;
        }
    }

    /**
     * Retrieve total width of the first n columns.
     * 
     * @param n number of colums to take into account
     * @return sum of the first n column withs
     */
    public int getTotalWidth(int n) {
        if (_cols != null) {
            int width = 0;
            for (int i = 0; i < n; i++) {
                IColumn col = _cols.get(i);
                width += _tvs.getColumnWidth(col);
            }
            return width;
        } else {
            return 0;
        }
    }

    /**
     * Calculate the width of all fixed columns.
     * 
     * @return the sum of the individual widths of the fixed columns
     */
    private int getFixedColumnsWidth() {
        int w = 0;
        for (int i = 0; i < _fixedColumns; i++) {
            w += _tvs.getColumnWidth(_cols.get(i));
        }
        return w;
    }

    /**
     * Calculate the height of all fixed rows.
     * 
     * @return sum of the individual heights of the fixed rows
     */
    private int getFixedRowsHeight() {
        int h = 0;
        for (int i = 0; i < _fixedRows; i++) {
            h += _tvs.getRowHeight(_rows.get(i));
        }
        return h;
    }

    /**
     * Retrieve the width of the control.
     * 
     * @return width in pixel
     */
    public int getWidth() {
        return getClientArea().width;
    }

    /**
     * Retrieve the height of the control.
     * 
     * @return height in pixel
     */
    public int getHeight() {
        return getClientArea().height;
    }

    /**
     * Retrieve the table viewstate.
     * 
     * @return the tvs.
     */
    public ITableViewState getTableViewState() {
        return _tvs;
    }

    /**
     * Set a TableViewState.
     * 
     * @param tvs The tvs to set.
     */
    public void setTableViewState(ITableViewState tvs) {
        if (_tvs != null) {
            _tvs.removeTableViewStateListener(this);
        }
        _tvs = tvs;
        _tvs.addTableViewStateListener(this);
    }

    // ///////// TableViewStateListener
    /**
     * {@inheritDoc}
     */
    public void rowHeightChanged(IRow row, int newHeight) {
        Rectangle r = getRowBounds(row);
        if (r != null) {
            int height = getHeight() - r.y;
            redraw(r.x, r.y, r.width, height, true);
            _rowInfoCache = null;
        }
        updateYScrollBar();
    }

    /**
     * {@inheritDoc}
     */
    public void rowHeightModeChanged(IRow row, RowHeightMode newHeightMode) {
        if (isDisplayed(row)) {
            if (newHeightMode == RowHeightMode.OPTANDVAR || newHeightMode == RowHeightMode.OPTIMAL) {
                optimizeHeight(row);
            }
            redraw();
        }
        // tweak: if the default height mode qualifies the row for height optimization it will be optimized since it is
        // registered
        // so it has to be removed if the mode changes before drawing
        if (newHeightMode != RowHeightMode.OPTANDVAR && newHeightMode != RowHeightMode.OPTIMAL) {
            doNotOptimizeHeight(row);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void columnWidthChanged(IColumn column, int newWidth) {
        registerRowsForOptimization(); // row heights may change for opt/optvar
        Rectangle r = getColumnBounds(column);
        if (_headerRect != null) {
            int y = _drawHeader ? _headerRect.y : r.y;
            int width = getWidth() - r.x;
            int height = _drawHeader ? _headerRect.height + r.height : r.height;
            if (_autoFilterEnabled) {
                height += _autoFilterRect.height;
            }
            redraw(r.x, y, width, height, true);
        }
        updateXScrollBar();
    }

    /**
     * {@inheritDoc}
     */
    public void columnWidthsChanged() {
        registerRowsForOptimization(); // row heights may change for opt/optvar
        redraw();
        updateXScrollBar();
    }

    // private boolean isColumnResizePossible(IColumn column, int oldWidth, int newWidth) {
    // if (_tvs.getColumnResizeMode() == TableViewState.ColumnResizeMode.NONE) {
    // return true;
    // }
    // int delta = newWidth - oldWidth;
    // if (_tvs.getColumnResizeMode() == TableViewState.ColumnResizeMode.SUBSEQUENT) {
    // int idx = _cols.indexOf(column);
    // if (idx > _cols.size()-1) {
    // return false;
    // }
    // IColumn subsequent = _cols.get(idx+1);
    // if (_tvs.getColumnWidth(subsequent) - delta > _tvs.getMinimalColWidth()) {
    // return true;
    // }
    // return false;
    // }
    // return false;
    // }

    /**
     * {@inheritDoc}
     */
    public void columnVisibilityChanged(IColumn column, boolean visible) {
        updateColumnList();
        updateXScrollBar();
        redraw();
    }

    /**
     * {@inheritDoc}
     */
    public void sortingChanged() {
        updateRowList();
        redraw();
        // fire the general sorting change
        firePropertyChange(PROPERTYNAME_SORTING, null, "x");
    }

    /**
     * {@inheritDoc}
     */
    public void columnOrderChanged() {
        updateColumnList();
        redraw();
    }

    /**
     * {@inheritDoc}
     */
    public void cellStyleChanged(IRow row, IColumn column, ICellStyle style) {
        if (column == null) {
            redraw(row);
        } else if (row == null) {
            redraw(column);
        } else {
            redraw(row, column);
        }
    }

    // //// End tableviewstatelistener

    /**
     * Set the enabled state for the autofilter.
     * 
     * @param enable true for enabling the autofilter
     */
    public void setAutoFilterEnable(boolean enable) {
        if (_autoFilterEnabled != enable) {
            _autoFilterEnabled = enable;
            if (enable) {
                updateAutoFilter();
            }
            redraw();
            updateYScrollBar();
            preparePaint(getWidth(), getHeight());
            firePropertyChange(PROPERTYNAME_AUTOFILTERENABLE, !enable, enable);
        }
    }

    /**
     * Retrieve the autofilter state.
     * 
     * @return true for anabled autofilter
     */
    public boolean getAutoFilterEnable() {
        return _autoFilterEnabled;
    }

    /**
     * Create and/or update autofilters.
     * 
     */
    private void updateAutoFilter() {
        if (_autoFilterEnabled) {
            // check combining autofilter
            if (_autoFilter == null) {
                _autoFilter = new AutoFilter(this);
            }
            // create autofilter instances and controls if necessary
            for (IColumn column : _cols) {
                if (_autoFilterMap.get(column) == null) {
                    IAutoFilter af = createAutoFilter(column);
                    if (af != null) {
                        af.addPropertyChangeListener(_autoFilter);
                        _autoFilterMap.put(column, af);
                    }
                }
            }

            // update the filters and register them with the combining internal autofilter row filter
            for (IColumn column : _cols) {
                IAutoFilter af = _autoFilterMap.get(column);
                if (af != null) { // might be null in case of errors
                    af.update();
                }
            }

        }
    }

    /**
     * Instantiate an autofilter instance for the given column.
     * 
     * @param column column
     * @return instantiated autofilter or <code>null</code> if any error occurs during instantiation
     */
    private IAutoFilter createAutoFilter(IColumn column) {
        Class<? extends IAutoFilter> clazz = getAutoFilterClass(column);
        if (clazz == null) {
            return null;
        }
        IAutoFilter result = null;
        try {
            Constructor<? extends IAutoFilter> constructor = clazz.getConstructor();
            result = constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace(); // TODO
            return null;
        }

        result.setTable(this);
        result.setColumn(column);
        return result;
    }

    /**
     * Retrieve the state of header drawing.
     * 
     * @return true when headers are drawn.
     */
    public boolean getDrawHeader() {
        return _drawHeader;
    }

    /**
     * If set to true, the header row will be drawn.
     * 
     * @param drawHeader true: draw the header
     */
    public void setDrawHeader(boolean drawHeader) {
        if (_drawHeader != drawHeader) {
            _drawHeader = drawHeader;
            redraw();
        }
    }

    /**
     * @return Returns the headerRenderer.
     */
    public ITableHeaderRenderer getHeaderRenderer() {
        return _headerRenderer;
    }

    /**
     * Set a header renderer.
     * 
     * @param headerRenderer The headerRenderer to set.
     */
    public void setHeaderRenderer(ITableHeaderRenderer headerRenderer) {
        _headerRenderer = headerRenderer;
        redraw();
    }

    /**
     * @return Returns the columnResizeAllowed.
     */
    public boolean isColumnResizeAllowed() {
        return _columnResizeAllowed;
    }

    /**
     * Set whether column resizing is allowed.
     * 
     * @param columnResizeAllowed true for allowing col resizing.
     */
    public void setColumnResizeAllowed(boolean columnResizeAllowed) {
        _columnResizeAllowed = columnResizeAllowed;
    }

    /**
     * @return Returns the headerResizeAllowed.
     */
    public boolean isHeaderResizeAllowed() {
        return _headerResizeAllowed;
    }

    /**
     * @param headerResizeAllowed The headerResizeAllowed to set.
     */
    public void setHeaderResizeAllowed(boolean headerResizeAllowed) {
        _headerResizeAllowed = headerResizeAllowed;
    }

    /**
     * @return Returns the rowResizeAllowed.
     */
    public boolean isRowResizeAllowed() {
        return _rowResizeAllowed;
    }

    /**
     * @param rowResizeAllowed The rowResizeAllowed to set.
     */
    public void setRowResizeAllowed(boolean rowResizeAllowed) {
        _rowResizeAllowed = rowResizeAllowed;
    }

    /**
     * Set the first row displayed.
     * 
     * @param idx index of the first row to be displayed.
     * @param pixeloffset the pixeloffset of the first row
     */
    public void setFirstRow(int idx, int pixeloffset) {
        if (_firstRowIdx != idx || _firstRowPixelOffset != pixeloffset) {
            int oldFirstIdx = _firstRowIdx;
            int oldPixelOffset = _firstRowPixelOffset;

            _firstRowIdx = idx;
            _firstRowPixelOffset = pixeloffset;
            _rowInfoCache = null; // kill the cache
            updateYScrollBar();
            redraw();
            firePropertyChange(PROPERTYNAME_FIRSTROWIDX, oldFirstIdx, idx);
            firePropertyChange(PROPERTYNAME_FIRSTROWPIXELOFFSET, oldPixelOffset, pixeloffset);
        }
    }

    /**
     * Internal row filter pooling the results of the autofilters.
     * 
     * @author Peter Kliem
     * @version $Id: JaretTable.java,v 1.1 2012-05-07 01:34:37 jason Exp $
     */
    private class AutoFilter extends PropertyObservableBase implements IRowFilter, PropertyChangeListener {
        /** the table instance of the filter. */
        private JaretTable _table;

        public AutoFilter(JaretTable table) {
            _table = table;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isInResult(IRow row) {
            boolean result = true;
            for (IColumn column : _cols) {
                IAutoFilter af = _autoFilterMap.get(column);
                if (af != null) {
                    result = result && af.isInResult(row);
                }
                if (!result) {
                    break;
                }
            }
            return result;
        }

        /**
         * {@inheritDoc} Whenever a filter signals change update everything.
         */
        public void propertyChange(PropertyChangeEvent evt) {
            updateRowList();
            setFirstRow(_fixedRows, 0);
            redraw();
            // genarl filtering change signalling
            _table.firePropertyChange(PROPERTYNAME_FILTERING, null, "x");
        }
    }

    /**
     * If a header context is present, display it at x,y.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public void displayHeaderContextMenu(int x, int y) {
        if (_headerContextMenu != null) {
            dispContextMenu(_headerContextMenu, x, y);
        }
    }

    /**
     * If a row context is present, display it at x,y.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public void displayRowContextMenu(int x, int y) {
        if (_rowContextMenu != null) {
            dispContextMenu(_rowContextMenu, x, y);
        }
    }

    // todo move to utils
    private void dispContextMenu(Menu contextMenu, int x, int y) {
        Shell shell = Display.getCurrent().getActiveShell();
        Point coords = Display.getCurrent().map(this, shell, x, y);
        contextMenu.setLocation(coords.x + shell.getLocation().x, coords.y + shell.getLocation().y);
        contextMenu.setVisible(true);
    }

    /**
     * Set a context menu to be displayed on the header area.
     * 
     * @param headerCtxMenu menu to display on the header or <code>null</code> to disable
     */
    public void setHeaderContextMenu(Menu headerCtxMenu) {
        _headerContextMenu = headerCtxMenu;
    }

    /**
     * Retrieve a context menu that has been set on the header.
     * 
     * @return context menu or <code>null</code>
     */
    public Menu getHeaderContextMenu() {
        return _headerContextMenu;
    }

    /**
     * Set a context menu to be displayed on rows.
     * 
     * @param rowCtxMenu context menu or <code>null</code> to disable
     */
    public void setRowContextMenu(Menu rowCtxMenu) {
        _rowContextMenu = rowCtxMenu;
    }

    /**
     * Retrieve a context menu that has been set for the rows.
     * 
     * @return context menu or <code>null</code>
     */
    public Menu getRowContextMenu() {
        return _rowContextMenu;
    }

    /**
     * @return Returns the fixedColumns.
     */
    public int getFixedColumns() {
        return _fixedColumns;
    }

    /**
     * Set the numerb of fixed columns. Fixed columns are excluded from vertial scrolling. Row resizing can be
     * restricted to the area of the fixed columns.
     * 
     * @param fixedColumns The fixedColumns to set.
     */
    public void setFixedColumns(int fixedColumns) {
        if (_fixedColumns != fixedColumns) {
            _fixedColumns = fixedColumns;
            _firstColIdx = fixedColumns;
            _firstColPixelOffset = 0;
            redraw();
        }
    }

    /**
     * @return Returns the fixedRows.
     */
    public int getFixedRows() {
        return _fixedRows;
    }

    /**
     * Set the number of rows to be fixed (excluded from scrolling and autofiltering; optionally from sorting).
     * 
     * @param fixedRows The fixedRows to set.
     */
    public void setFixedRows(int fixedRows) {
        if (_fixedRows != fixedRows) {
            _fixedRows = fixedRows;
            _firstRowIdx = fixedRows;
            _firstRowPixelOffset = 0;
            _rowInfoCache = null;
            redraw();
        }
    }

    // /////////// table model listener

    /**
     * {@inheritDoc}
     */
    public void rowChanged(IRow row) {
        if (_tvs.getRowHeigthMode(row) == ITableViewState.RowHeightMode.OPTIMAL
                || _tvs.getRowHeigthMode(row) == ITableViewState.RowHeightMode.OPTANDVAR) {
            optimizeHeight(row);
        }
        redraw(row);
    }

    /**
     * {@inheritDoc}
     */
    public void rowRemoved(IRow row) {
        // MAYBE could be further optimized
        updateRowList();
        if (isDisplayed(row)) {
            syncedRedraw();
        }
        updateYScrollBar();
    }

    /**
     * {@inheritDoc}
     */
    public void rowAdded(int idx, IRow row) {
        updateRowList();
        syncedRedraw();
        updateYScrollBar();
    }

    /**
     * {@inheritDoc}
     */
    public void columnAdded(int idx, IColumn column) {
        _tvs.getSortedColumns().add(column);
        updateColumnList();
        syncedRedraw();
        updateXScrollBar();
    }

    /**
     * {@inheritDoc}
     */
    public void columnRemoved(IColumn column) {
        // MAYBE optimize
        _tvs.getSortedColumns().remove(column);
        updateColumnList();
        if (isDisplayed(column)) {
            syncedRedraw();
        }
        updateXScrollBar();
    }

    /**
     * {@inheritDoc}
     */
    public void columnChanged(IColumn column) {
        redraw(column);
    }

    /**
     * {@inheritDoc}
     */
    public void cellChanged(IRow row, IColumn column) {
        redraw(row, column);
    }

    /**
     * {@inheritDoc}
     */
    public void tableDataChanged() {
        // TODO optimze row heights
        updateRowList();
        syncedRedraw();
    }

    // end table model listener

    /**
     * Retrieve the flag controlling whether fixed rows are excluded from sorting.
     * 
     * @return if true fixed rows will not be affected by sorting operations
     */
    public boolean getExcludeFixedRowsFromSorting() {
        return _excludeFixedRowsFromSorting;
    }

    /**
     * If set to true, fixed rows are exluded from sorting.
     * 
     * @param excludeFixedRowsFromSorting true for exclude fixed rows from sorting.
     */
    public void setExcludeFixedRowsFromSorting(boolean excludeFixedRowsFromSorting) {
        _excludeFixedRowsFromSorting = excludeFixedRowsFromSorting;
    }

    /**
     * Get the state of the resize restriction flag. If true, resizing is only allowed in header and fixed columns (for
     * rows) and the leftmost SELDELTA pixels of eachrow.
     * 
     * @return Returns the resizeRestriction.
     */
    public boolean getResizeRestriction() {
        return _resizeRestriction;
    }

    /**
     * If set to true resizing of columns will only be allowed in the header area. Row resizing will be allowed on fixed
     * columns and on the first SEL_DELTA pixels of the leftmost column when restricted.
     * 
     * @param resizeRestriction The resizeRestriction to set.
     */
    public void setResizeRestriction(boolean resizeRestriction) {
        _resizeRestriction = resizeRestriction;
    }

    // //////// selection listener

    /**
     * {@inheritDoc}
     */
    public void rowSelectionAdded(IRow row) {
        _selectedIdxRectangle = null;
        redraw(row);
    }

    /**
     * {@inheritDoc}
     */
    public void rowSelectionRemoved(IRow row) {
        _selectedIdxRectangle = null;
        redraw(row);
    }

    /**
     * {@inheritDoc}
     */
    public void cellSelectionAdded(IJaretTableCell cell) {
        _selectedIdxRectangle = null;
        redraw(cell.getRow(), cell.getColumn());
    }

    /**
     * {@inheritDoc}
     */
    public void cellSelectionRemoved(IJaretTableCell cell) {
        _selectedIdxRectangle = null;
        redraw(cell.getRow(), cell.getColumn());
    }

    /**
     * {@inheritDoc}
     */
    public void columnSelectionAdded(IColumn column) {
        _selectedIdxRectangle = null;
        redraw(column);
    }

    /**
     * {@inheritDoc}
     */
    public void columnSelectionRemoved(IColumn column) {
        _selectedIdxRectangle = null;
        redraw(column);
    }

    // end selection listener

    /**
     * Retrieve the selectionmodel used by the table.
     * 
     * @return the selection model
     */
    public IJaretTableSelectionModel getSelectionModel() {
        return _selectionModel;
    }

    /**
     * Set the selection model to be used by the table.
     * 
     * @param jts the selection model to be used (usually the default implementation)
     */
    public void setSelectionModel(IJaretTableSelectionModel jts) {
        if (_selectionModel != null) {
            _selectionModel.removeTableSelectionModelListener(this);
        }
        _selectionModel = jts;
        _selectionModel.addTableSelectionModelListener(this);
    }

    /**
     * Current number of displayed columns.
     * 
     * @return number of displayed columns
     */
    public int getColumnCount() {
        return _cols.size();
    }

    /**
     * Retrieve column by the display idx.
     * 
     * @param idx display idx
     * @return column
     */
    public IColumn getColumn(int idx) {
        return _cols.get(idx);
    }

    /**
     * Convenience method to retrieve a column by it's id from the model.
     * 
     * @param id id of the column
     * @return column or <code>null</code>
     */
    public IColumn getColumn(String id) {
        for (int i = 0; i < _model.getColumnCount(); i++) {
            if (_model.getColumn(i).getId().equals(id)) {
                return _model.getColumn(i);
            }
        }
        return null;
    }

    /**
     * Get the number of displayed rows (after filtering!).
     * 
     * @return number of displayed rows
     */
    public int getRowCount() {
        return _rows.size();
    }

    /**
     * Get a row by the display idx.
     * 
     * @param idx index in the list of displayed rows.
     * @return row
     */
    public IRow getRow(int idx) {
        return _rows.get(idx);
    }

    /**
     * @return Returns the rowFilter.
     */
    public IRowFilter getRowFilter() {
        return _rowFilter;
    }

    /**
     * Set a row filter on the table.
     * 
     * @param rowFilter The rowFilter to set.
     */
    public void setRowFilter(IRowFilter rowFilter) {
        IRowFilter oldVal = _rowFilter;
        if (_rowFilter != null) {
            _rowFilter.removePropertyChangeListener(this);
        }
        _rowFilter = rowFilter;
        if (_rowFilter != null) {
            _rowFilter.addPropertyChangeListener(this);
        }
        updateRowList();
        updateAutoFilter(); // update autofilter (just in case it is enabled)
        redraw();
        firePropertyChange(PROPERTYNAME_ROWFILTER, oldVal, _rowFilter);
        // general change of filtering
        firePropertyChange(PROPERTYNAME_FILTERING, null, "x");
    }

    /**
     * @return Returns the rowSorter.
     */
    public IRowSorter getRowSorter() {
        return _rowSorter;
    }

    /**
     * Set a row sorter. A row sorter will be overruled by sorting setup on columns.
     * 
     * @param rowSorter The rowSorter to set.
     */
    public void setRowSorter(IRowSorter rowSorter) {
        IRowSorter oldValue = _rowSorter;
        if (_rowSorter != null) {
            _rowSorter.removePropertyChangeListener(this);
        }
        _rowSorter = rowSorter;
        if (_rowSorter != null) {
            _rowSorter.addPropertyChangeListener(this);
        }
        updateRowList();
        redraw();
        // fire the change for the sorter object
        firePropertyChange(PROPERTYNAME_ROWSORTER, oldValue, _rowSorter);
        // fire the general sorting change
        firePropertyChange(PROPERTYNAME_SORTING, null, "x");
    }

    /**
     * Add a listener to listen for focus changes in the table (focussed cell).
     * 
     * @param tfl listener
     */
    public synchronized void addTableFocusListener(ITableFocusListener tfl) {
        if (_tableFocusListeners == null) {
            _tableFocusListeners = new Vector<ITableFocusListener>();
        }
        _tableFocusListeners.add(tfl);
    }

    /**
     * Remove a registered listener.
     * 
     * @param tfl listener
     */
    public synchronized void remTableFocusListener(ITableFocusListener tfl) {
        if (_tableFocusListeners != null) {
            _tableFocusListeners.remove(tfl);
        }
    }

    /**
     * Inform focus listeners about a change of the focussed cell.
     * 
     * @param row row of the focussed cell
     * @param column column of the focussed cell
     */
    private void fireTableFocusChanged(IRow row, IColumn column) {
        if (_tableFocusListeners != null) {
            for (ITableFocusListener listener : _tableFocusListeners) {
                listener.tableFocusChanged(this, row, column);
            }
        }
    }

    // ************ property change listener
    /**
     * {@inheritDoc} The table eitselflistens for prop changes of the rowSorter and the rowFilter.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(_rowSorter)) {
            updateRowList();
            updateAutoFilter(); // update autofilter (just in case it is enabled)
            redraw();
            firePropertyChange(PROPERTYNAME_SORTING, null, "x");
        } else if (evt.getSource().equals(_rowFilter)) {
            updateRowList();
            updateAutoFilter(); // update autofilter (just in case it is enabled)
            redraw();
            firePropertyChange(PROPERTYNAME_FILTERING, null, "x");
        }

    }

    // ************ property change listener

    /**
     * Retrieve the used startegy when performing a fill drag.
     * 
     * @return the fillDragStrategy
     */
    public IFillDragStrategy getFillDragStrategy() {
        return _fillDragStrategy;
    }

    /**
     * Set the strategy used when perfoming a fill drag.
     * 
     * @param fillDragStrategy the fillDragStrategy to set. Must be non null.
     */
    public void setFillDragStrategy(IFillDragStrategy fillDragStrategy) {
        if (fillDragStrategy == null) {
            throw new IllegalArgumentException("FillDragStrategy must not be NULL");
        }
        _fillDragStrategy = fillDragStrategy;
    }

    /**
     * Retrieve whether fill dragging is activated.
     * 
     * @return the supportFillDragging
     */
    public boolean isSupportFillDragging() {
        return _supportFillDragging;
    }

    /**
     * Set fill drag activation.
     * 
     * @param supportFillDragging the supportFillDragging to set
     */
    public void setSupportFillDragging(boolean supportFillDragging) {
        _supportFillDragging = supportFillDragging;
        _dragMarkerRect = null;
        redraw();
    }

    /**
     * @return the iccpStrategy
     */
    public ICCPStrategy getCcpStrategy() {
        return _ccpStrategy;
    }

    /**
     * Set the strategy to perform cut, copy, paste operations. Setting the strategy to <code>null</code> causes
     * deactivation of ccp.
     * 
     * @param ccpStrategy the iccpStrategy to set or <code>null</code> to deactivat ccp
     */
    public void setCcpStrategy(ICCPStrategy ccpStrategy) {
        _ccpStrategy = ccpStrategy;
    }

    /**
     * Do a cut operation. Implementation is supplied by the CCPStrategy.
     * 
     */
    public void cut() {
        if (_ccpStrategy != null) {
            _ccpStrategy.cut(this);
        }
    }

    /**
     * Do a copy operation. Implementation is supplied by the CCPStrategy.
     * 
     */
    public void copy() {
        if (_ccpStrategy != null) {
            _ccpStrategy.copy(this);
        }
    }

    /**
     * Do a paste operation. Implementation is supplied by the CCPStrategy.
     * 
     */
    public void paste() {
        if (_ccpStrategy != null) {
            _ccpStrategy.paste(this);
        }
    }

    /**
     * Select all cells by selectiong all displayed (not filtered) columns.
     * 
     */
    public void selectAll() {
        getSelectionModel().clearSelection();
        for (IColumn col : _cols) {
            getSelectionModel().addSelectedColumn(col);
        }
    }

    /**
     * Retrieve whether scroll opotimizations are active.
     * 
     * @return true if scrolling is done optimized
     */
    public boolean getOptimizeScrolling() {
        return _optimizeScrolling;
    }

    /**
     * Set whether to use optimized scrolling by copying content. Defaults to false (since it causes trouble when
     * running on Linux or OSX).
     * 
     * @param optimizeScrolling true for optimizing
     */
    public void setOptimizeScrolling(boolean optimizeScrolling) {
        _optimizeScrolling = optimizeScrolling;
    }

    /**
     * Retrieve the height used to render the autofilters.
     * 
     * @return height of the autofilter rectangle
     */
    public int getAutoFilterHeight() {
        return _autoFilterEnabled ? _autoFilterRect.height : 0;
    }

    /**
     * {@inheritDoc}
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        _propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        _propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void firePropertyChange(String propName, Object oldVal, Object newVal) {
        if (_propertyChangeSupport != null) {
            _propertyChangeSupport.firePropertyChange(propName, oldVal, newVal);
        }
    }

    /**
     * Retrieve the pixel offset the first row is scrolled.
     * 
     * @return pixel ofset of the first row
     */
    public int getFirstRowPixelOffset() {
        return _firstRowPixelOffset;
    }

    /**
     * Retrive the index of the first row displayed in the scrolled area of the table.
     * 
     * @return index of the first row displayed
     */
    public int getFirstRowIdx() {
        return _firstRowIdx;
    }

    /**
     * Check whether sorting the table is allowed.
     * 
     * @return true if sorting is allowed
     */
    public boolean getAllowSorting() {
        return _allowSorting;
    }

    /**
     * Set the global allowance for sorting. This defaults to true.
     * 
     * @param allowSorting true to allow sorting
     */
    public void setAllowSorting(boolean allowSorting) {
        _allowSorting = allowSorting;
    }

    /**
     * Get access to the internal row list. This is for special purposes (like synchronizing models) only. Use with
     * care!
     * 
     * @return the internal list of rows
     */
    public List<IRow> getInternalRowList() {
        return _rows;
    }

}
