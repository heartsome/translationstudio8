/*
 *  File: TableHierarchicalExample.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.examples.table;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

import de.jaret.util.ui.ResourceImageDescriptor;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.DefaultHierarchicalTableModel;
import de.jaret.util.ui.table.model.HierarchyColumn;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IHierarchicalJaretTableModel;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.model.ITableNode;
import de.jaret.util.ui.table.model.PropCol;
import de.jaret.util.ui.table.model.StdHierarchicalTableModel;
import de.jaret.util.ui.table.renderer.TableHierarchyRenderer;
import de.jaret.util.ui.table.util.action.JaretTableActionFactory;

/**
 * Simple example showing the hierarchical model. Shows a very simple darg and drop handling allowing to move single
 * rows in the table.
 * 
 * @author kliem
 * @version $Id: TableHierarchicalExample.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class TableHierarchicalExample {

    /**
     * If set to true, simple node drag and drop will be enabled. This is disabledin the example by default, since it is
     * far from perfect.
     */
    private static final boolean SUPPORT_DND = false;

    private Shell _shell;
    private JaretTable _jt;

    public TableHierarchicalExample(IHierarchicalJaretTableModel hierarchicalModel) {
        _shell = new Shell(Display.getCurrent());
        _shell.setText("jaret table hierarchical example");
        createControls(hierarchicalModel);
        _shell.open();
        Display display;
        display = _shell.getDisplay();
        _shell.pack();
        _shell.setSize(400, 700);

        /*
         * do the event loop until the shell is closed to block the call
         */
        while (_shell != null && !_shell.isDisposed()) {
            try {
                if (!display.readAndDispatch())
                    display.sleep();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        display.update();
    }

    /**
     * Create the controls that compose the console test.
     * 
     */
    protected void createControls(IHierarchicalJaretTableModel hierarchicalModel) {
        GridLayout gl = new GridLayout();
        gl.numColumns = 1;
        _shell.setLayout(gl);

        GridData gd = new GridData(GridData.FILL_BOTH);

        _jt = new JaretTable(_shell, SWT.V_SCROLL | SWT.H_SCROLL);
        _jt.setLayoutData(gd);

        IHierarchicalJaretTableModel hmodel = hierarchicalModel;

        if (hierarchicalModel == null) {

            ITableNode root = new DummyTableNode("tn1", "tn1", "Root", "This the root node");
            ITableNode r1 = new DummyTableNode("tn11", "tn12", "1", "Child 1 of the root");
            ITableNode r2 = new DummyTableNode("tn12", "tn12", "2", "Child 2 of the root");
            ITableNode r3 = new DummyTableNode("tn13", "tn13", "3", "Child 3 of the root");
            root.addNode(r1);
            root.addNode(r2);
            root.addNode(r3);

            r1.addNode(new DummyTableNode("tn111", "tn111", "1", "A second level child"));
            r1.addNode(new DummyTableNode("tn112", "tn112", "2", "Another second level child"));

            ITableNode n1 = new DummyTableNode("tn131", "tn131", "1", "A second level child");
            r3.addNode(n1);
            ITableNode n2 = new DummyTableNode("tn132", "tn132", "2", "Another second level child");
            r3.addNode(n2);

            n1.addNode(new DummyTableNode("tn1311", "tn1311", "1", "A third level child"));
            n1.addNode(new DummyTableNode("tn1312", "tn1312", "2", "Another third level child"));

            DefaultHierarchicalTableModel dhmodel = new DefaultHierarchicalTableModel(root);
            hmodel = dhmodel;

            if (SUPPORT_DND) {
                // init the simple drag and drop handling
                initDND(_jt, _shell);
            }
        }

        _jt.setTableModel(hmodel);
        StdHierarchicalTableModel model = (StdHierarchicalTableModel) _jt.getTableModel();
        IColumn hcol = new HierarchyColumn();
        // create and setup hierarchy renderer
        final TableHierarchyRenderer hierarchyRenderer = new TableHierarchyRenderer();
        hierarchyRenderer.setLabelProvider(new LabelProvider());
        hierarchyRenderer.setDrawIcons(true);
        hierarchyRenderer.setDrawLabels(true);
        _jt.registerCellRenderer(hcol, hierarchyRenderer);
        model.addColumn(hcol);

        model.addColumn(new PropCol("b1", "column 1", "B1"));
        model.addColumn(new PropCol("t1", "column 2", "T1"));
        model.addColumn(new PropCol("t2", "column 3", "T2"));
        model.addColumn(new PropCol("t3", "column 4", "T3"));

        JaretTableActionFactory af = new JaretTableActionFactory();

        MenuManager mm = new MenuManager();
        mm.add(af.createStdAction(_jt, JaretTableActionFactory.ACTION_CONFIGURECOLUMNS));
        _jt.setHeaderContextMenu(mm.createContextMenu(_jt));

        MenuManager rm = new MenuManager();
        rm.add(af.createStdAction(_jt, JaretTableActionFactory.ACTION_OPTROWHEIGHT));
        rm.add(af.createStdAction(_jt, JaretTableActionFactory.ACTION_OPTALLROWHEIGHTS));
        _jt.setRowContextMenu(rm.createContextMenu(_jt));

        TableControlPanel ctrlPanel = new TableControlPanel(_shell, SWT.NULL, _jt);

        Label l = new Label(_shell, SWT.NONE);
        l.setText("Level width:");
        final Scale levelWidthScale = new Scale(_shell, SWT.HORIZONTAL);
        levelWidthScale.setMaximum(40);
        levelWidthScale.setMinimum(0);
        levelWidthScale.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ev) {
                int val = levelWidthScale.getSelection();
                hierarchyRenderer.setLevelWidth(val);
                _jt.redraw();
            }
        });

    }

    public static void main(String args[]) {
        TableHierarchicalExample te = new TableHierarchicalExample(null);
    }

    public class LabelProvider implements ILabelProvider {

        public Image getImage(Object element) {
            DummyTableNode node = (DummyTableNode) element;
            return node.getB1() ? getImageRegistry().get("true") : getImageRegistry().get("false");
        }

        public String getText(Object element) {
            DummyTableNode node = (DummyTableNode) element;
            return node.getId();
        }

        public void addListener(ILabelProviderListener listener) {
        }

        public void dispose() {
            if (_imageRegistry != null) {
                _imageRegistry.dispose();
            }
        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {
        }

        ImageRegistry _imageRegistry;

        public ImageRegistry getImageRegistry() {
            if (_imageRegistry == null) {
                _imageRegistry = new ImageRegistry();
                ImageDescriptor imgDesc = new ResourceImageDescriptor("/de/jaret/examples/table/true.gif");
                _imageRegistry.put("true", imgDesc);
                imgDesc = new ResourceImageDescriptor("/de/jaret/examples/table/false.gif");
                _imageRegistry.put("false", imgDesc);
            }
            return _imageRegistry;
        }
    }

    IRow _draggedRow;
    ITableNode _parentTableNode;

    /**
     * Init a simple drag and drop operation for moving rows in the table.
     * 
     * @param table
     * @param parent
     */
    private void initDND(final JaretTable table, Composite parent) {
        // support move only
        int operations = DND.DROP_MOVE;
        final DragSource source = new DragSource(table, operations);

        // Provide data in Text format
        Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
        source.setTransfer(types);

        source.addDragListener(new DragSourceListener() {
            public void dragStart(DragSourceEvent event) {

                // check whether drag occured on the hierarchy column
                IColumn column = table.colForX(event.x);
                if (column != null && table.isHierarchyColumn(column)) { // TODO check whether a resize may have
                    // higher priority
                    // possible row drag
                    IRow row = table.rowForY(event.y);
                    if (row != null) {
                        // row hit, start row drag
                        _draggedRow = row;

                        // capture the data for internal use
                        // row drag: use row at starting position
                        _parentTableNode = getParent(table.getHierarchicalModel().getRootNode(), (ITableNode) row);

                    } else {
                        event.doit = false;
                    }

                }
            }

            public void dragSetData(DragSourceEvent event) {
                // Provide the data of the requested type.
                if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
                    if (_draggedRow != null) {
                        event.data = "row: " + _draggedRow.getId();
                    }
                }
            }

            public void dragFinished(DragSourceEvent event) {
                // for this simple case we do all the manipulations in the drop
                // target
                // this is more of a hack ...
                _draggedRow = null;
            }

        });

        // ////////////////////
        // Drop target

        // moved to the drop target
        operations = DND.DROP_MOVE;
        final DropTarget target = new DropTarget(table, operations);

        // Receive data in Text
        final TextTransfer textTransfer = TextTransfer.getInstance();
        types = new Transfer[] {textTransfer};
        target.setTransfer(types);

        target.addDropListener(new DropTargetListener() {
            public void dragEnter(DropTargetEvent event) {
            }

            public void dragOver(DropTargetEvent event) {
                // event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;

                if (_draggedRow != null) {
                    // no drag over effect right now
                }
            }

            public void dragOperationChanged(DropTargetEvent event) {
            }

            public void dragLeave(DropTargetEvent event) {
            }

            public void dropAccept(DropTargetEvent event) {
            }

            public void drop(DropTargetEvent event) {
                // this simple drop implementation takes care of the complete
                // operation
                // this is kind of a hack ...
                if (textTransfer.isSupportedType(event.currentDataType)) {
                    String text = (String) event.data;
                    System.out.println("DROP: " + text);

                    if (_draggedRow != null) {
                        int destY = Display.getCurrent().map(null, table, event.x, event.y).y;
                        int destX = Display.getCurrent().map(null, table, event.x, event.y).x;

                        IRow overRow = table.rowForY(destY);
                        if (overRow != null) {
                            System.out.println("over row " + overRow.getId());
                            // this is an action from the drag source listener
                            // ...
                            // this has to be done right here because otherwise
                            // the node would be at two places
                            // at the same time causing some redraw trouble ...
                            _parentTableNode.remNode((ITableNode) _draggedRow);
                            ITableNode node = (ITableNode) overRow;
                            node.addNode((ITableNode) _draggedRow);
                        }
                    }
                }
            }

        });

        // Dispose listener on parent of timebar viewer to dispose the
        // dragsource and dragtarget BEFORE the timebar
        // viewer
        // this prevents an exception beeing thrown by SWT
        parent.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                source.dispose();
                target.dispose();
            }
        });

    }

    private ITableNode getParent(ITableNode root, ITableNode draggedRow) {
        if (root.getChildren().contains(draggedRow)) {
            return root;
        } else {
            for (ITableNode node : root.getChildren()) {
                ITableNode result = getParent(node, draggedRow);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

}
