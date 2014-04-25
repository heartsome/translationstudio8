/*
 *  File: StdHierarchicalTableModel.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.jaret.util.misc.PropertyObservable;

/**
 * Implementation of a "normal" jaret table model based on a hierarchical jaret table model. The StdHierarchicalmodel
 * will listen on propchanges if the nodes are PropertyObservables.
 * 
 * @author Peter Kliem
 * @version $Id: StdHierarchicalTableModel.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class StdHierarchicalTableModel extends AbstractJaretTableModel implements IHierarchicalTableViewStateListener,
        ITableNodeListener, PropertyChangeListener {
    /**
     * Current row list = list of visible nodes.
     */
    protected List<ITableNode> _rows;

    /** the hierarchical model this "normal" model maps. */
    protected IHierarchicalJaretTableModel _hModel;

    /** the hierarchical viewstate responsible for node visibility. */
    protected IHierarchicalTableViewState _hvs;

    /** the list of columns. */
    protected List<IColumn> _cols = new ArrayList<IColumn>();

    /**
     * Construct a new stdhierarchical table model for a viewstate and a hierarchical model.
     * 
     * @param hModel hierarchical table model
     * @param hvs hierarchical viewstate
     */
    public StdHierarchicalTableModel(IHierarchicalJaretTableModel hModel, IHierarchicalTableViewState hvs) {
        _hModel = hModel;
        _hvs = hvs;
        _hvs.addHierarchicalViewstateListener(this);
        updateRowList();
    }

    /**
     * Register as propchange listener if node is observable.
     * 
     * @param node node
     */
    private void registerPropChange(ITableNode node) {
        // listen for propertychanges if we are dealing with a propertyobservable
        if (node instanceof PropertyObservable) {
            ((PropertyObservable) node).addPropertyChangeListener(this);
        }
    }

    /**
     * Deregister as propchange listener if node is observable.
     * 
     * @param node node
     */
    private void deRegisterPropChange(ITableNode node) {
        if (node instanceof PropertyObservable) {
            ((PropertyObservable) node).removePropertyChangeListener(this);
        }
    }

    /**
     * Update the internal rowlist by traversing the hierarchy.
     */
    private void updateRowList() {
        _rows = new ArrayList<ITableNode>();
        updateRowList(_rows, 0, _hModel.getRootNode(), true);
    }

    /**
     * Recursive creation of the list of rows.
     * 
     * @param rows list to be filled
     * @param level current level
     * @param node current node
     * @param visible true if visible
     */
    private void updateRowList(List<ITableNode> rows, int level, ITableNode node, boolean visible) {
        if (visible) {
            rows.add(node);
            registerPropChange(node);
        }

        node.addTableNodeListener(this);

        // set the level of the node
        node.setLevel(level);

        for (ITableNode n : node.getChildren()) {
            updateRowList(rows, level + 1, n, _hvs.isExpanded(node) && visible);
        }
    }

    /**
     * Check whether more siblings exist for a given node on a given level.
     * @param node node
     * @param level level
     * @return true if more siblings exist (even with other parent)
     */
    public boolean moreSiblings(ITableNode node, int level) {
        int idx = _rows.indexOf(node);
        if (idx == -1) {
            throw new RuntimeException();
        }
        if (node.getLevel() == level) {
            return getNextSibling(node) != null;
        } else {
            ITableNode n = node;
            for (int l = node.getLevel(); l > level + 1; l--) {
                n = getParent(n);
            }
            return getNextSibling(n) != null;
        }
    }

    /**
     * Return the next sibling of a node.
     * 
     * @param node node to get the next sibling for
     * @return the sibling or <code>null</code> if there is none
     */
    public ITableNode getNextSibling(ITableNode node) {
        ITableNode parent = getParent(node);
        if (parent == null) {
            return null;
        }
        int idx = parent.getChildren().indexOf(node);
        if (parent.getChildren().size() > idx + 1) {
            return parent.getChildren().get(idx + 1);
        } else {
            return null;
        }
    }

    /**
     * Retrieve the parent of a particular node.
     * 
     * @param node node
     * @return parent of the node or <code>null</code> if it is the root node or is not in the list of nodes (not
     * visible)
     */
    private ITableNode getParent(ITableNode node) {
        int idx = _rows.indexOf(node);
        if (idx == -1) {
            return null;
        }
        for (int i = idx - 1; i >= 0; i--) {
            ITableNode n = _rows.get(i);
            if (n.getChildren().contains(node)) {
                return n;
            }
        }
        return null;
    }

    /**
     * Check whether a node is visible.
     * 
     * @param node node to check
     * @return true if the node is visible
     */
    public boolean isVisible(ITableNode node) {
        return getIdxForNode(node) != -1;
    }

    /**
     * Get the index of a node in the list of visible nodes.
     * 
     * @param node node to check
     * @return index or -1 if not found
     */
    private int getIdxForNode(ITableNode node) {
        return _rows.indexOf(node);
    }

    /**
     * {@inheritDoc}
     */
    public IRow getRow(int rowIdx) {
        return _rows.get(rowIdx);
    }

    /**
     * {@inheritDoc}
     */
    public int getRowCount() {
        return _rows.size();
    }


    /**
     * {@inheritDoc}
     */
    public void nodeAdded(ITableNode parent, ITableNode newChild) {
        newChild.addTableNodeListener(this);
        if (_hvs.isExpanded(parent)) {
            // search the position of the new child and add the row
            Map<ITableNode, Integer> map = new HashMap<ITableNode, Integer>();
            posForNode(parent, map);
            int pos = map.get(newChild);
            _rows.add(pos, newChild);
            fireRowAdded(pos, newChild);
            // if the new child has children and is expanded, add all of its children
            List<ITableNode> toAdd = new ArrayList<ITableNode>();
            enumerateChildren(newChild, toAdd);
            pos++;
            for (ITableNode tableNode : toAdd) {
                _rows.add(pos, tableNode);
                fireRowAdded(pos, tableNode);
                pos++;
            }

        }

    }
    /**
     * Fill a list with all children of the given node that are visible.
     * 
     * @param node starting ndoe
     * @param children list to fill
     */
    private void enumerateChildren(ITableNode node, List<ITableNode> children) {
        if (node.getChildren() != null && _hvs.isExpanded(node)) {
            for (ITableNode tableNode : node.getChildren()) {
                children.add(tableNode);
                enumerateChildren(tableNode, children);
            }
        }
    }

    /**
     * Fill a map with the index positions for the underlying nodes.
     * 
     * @param node starting node
     * @param map map to fill with the indizes
     * @return "inserted" count for recursive call
     */
    private int posForNode(ITableNode node, Map<ITableNode, Integer> map) {
        int idx = getIdxForNode(node);
        int count = node.getChildren().size();
        int inserted = 0;
        for (int i = 0; i < count; i++) {
        	ITableNode n = node.getChildren().get(i);
            map.put(n, idx + 1 + inserted);
            inserted++;
            if (_hvs.isExpanded(n) && n.getChildren().size() > 0) {
                inserted += posForNode(n, map);
            }
        }
        return inserted;
    }
    
    /**
     * {@inheritDoc}
     */
    public void nodeRemoved(ITableNode parent, ITableNode removedChild) {
        removedChild.removeTableNodeListener(this);
        if (_hvs.isExpanded(parent)) {
            // remove the row of the child
            _rows.remove(removedChild);
            fireRowRemoved(removedChild);
            // remove the rows of all visible children
            List<ITableNode> toRemove = new ArrayList<ITableNode>();
            enumerateChildren(removedChild, toRemove);
            for (ITableNode tableNode : toRemove) {
                _rows.remove(tableNode);
                fireRowRemoved(tableNode);
            }
        }
    }
    
    
    

    /**
     * {@inheritDoc }Handle expansion of a node. This means adding all rows that become visible when expanding.
     */
    public void nodeExpanded(ITableNode node) {
        nodeExpanded2(node);
    }

    /**
     * Handle expansion of a node by traversing all children of the node to check whether they are visible.
     * 
     * @param node node expanded
     * @return count of nodes that became visible ()= number of insertedlines)
     */
    private int nodeExpanded2(ITableNode node) {
        int idx = getIdxForNode(node);
        int count = node.getChildren().size();
        int inserted = 0;
        for (int i = 0; i < count; i++) {
            ITableNode n = node.getChildren().get(i);
            int newIdx = idx + 1 + inserted;
            _rows.add(newIdx, n);
            inserted++;
            fireRowAdded(newIdx, n);
            registerPropChange(n);
            if (_hvs.isExpanded(n) && n.getChildren().size() > 0) {
                inserted += nodeExpanded2(n);
            }
        }
        return inserted;
    }

    /**
     * {@inheritDoc} Handle folding of a node. This means removing all rows that "disappear" with folding.
     */
    public void nodeFolded(ITableNode node) {
        int count = node.getChildren().size();
        for (int i = 0; i < count; i++) {
            ITableNode n = node.getChildren().get(i);
            if (_hvs.isExpanded(n) && n.getChildren().size() > 0) {
                nodeFolded(n);
            }
            int idx2 = getIdxForNode(n);
            // maybe the node is already hidden ...
            if (idx2 != -1) {
                _rows.remove(idx2);
            }
            fireRowRemoved(n);
            deRegisterPropChange(n);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getColumnCount() {
        return _cols.size();
    }

    /**
     * {@inheritDoc}
     */
    public IColumn getColumn(int idx) {
        return _cols.get(idx);
    }

    /**
     * Add a column to the list of columns.
     * 
     * @param column column to add
     */
    public void addColumn(IColumn column) {
        _cols.add(column);
        fireColumnAdded(_cols.size() - 1, column);
    }

    /**
     * {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof IRow) {
            fireRowChanged((IRow) evt.getSource());
        }
    }
}
