/*
 *  File: AbstractTableNode.java 
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
import java.util.List;
import java.util.Vector;

import de.jaret.util.misc.PropertyObservableBase;

/**
 * Abstract base implementation of an ITableNode.
 * 
 * @author Peter Kliem
 * @version $Id: AbstractTableNode.java,v 1.1 2012-05-07 01:34:36 jason Exp $
 */
public abstract class AbstractTableNode extends PropertyObservableBase implements ITableNode {
    /** listeners. */
    protected List<ITableNodeListener> _listeners;

    /** list of the chikdren of the node. */
    protected List<ITableNode> _children = new ArrayList<ITableNode>();

    /** level in the hierarchy. */
    protected int _level;

    // TODO remove
    /**
     * {@inheritDoc}
     */
    public List<ITableNode> getChildren() {
        return _children;
    }

    /**
     * {@inheritDoc}
     */
    public int getLevel() {
        return _level;
    }

    /**
     * {@inheritDoc}
     */
    public void setLevel(int level) {
        _level = level;
        if (_children != null) {
        	for (ITableNode node : _children) {
				node.setLevel(level+1);
			}
        }
    }

    /**
     * Add a node.
     * 
     * @param node node to add
     */
    public void addNode(ITableNode node) {
        node.setLevel(_level + 1);
        _children.add(node);
        fireNodeAdded(node);
    }

    /**
     * Remove a node.
     * 
     * @param node node to remove
     */
    public void remNode(ITableNode node) {
        if (_children.contains(node)) {
            _children.remove(node);
            fireNodeRemoved(node);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void addTableNodeListener(ITableNodeListener tnl) {
        if (_listeners == null) {
            _listeners = new Vector<ITableNodeListener>();
        }
        _listeners.add(tnl);
    }

    /**
     * {@inheritDoc}
     */
    public void removeTableNodeListener(ITableNodeListener tnl) {
        if (_listeners != null) {
            _listeners.remove(tnl);
        }
    }

    /**
     * Inform listeners about a newly added node.
     * 
     * @param node the added node
     */
    protected void fireNodeAdded(ITableNode node) {
        if (_listeners != null) {
            for (ITableNodeListener listener : _listeners) {
                listener.nodeAdded(this, node);
            }
        }
    }

    /**
     * Inform listeners about the removal of a node.
     * 
     * @param node removed node
     */
    protected void fireNodeRemoved(ITableNode node) {
        if (_listeners != null) {
            for (ITableNodeListener listener : _listeners) {
                listener.nodeRemoved(this, node);
            }
        }
    }

}
