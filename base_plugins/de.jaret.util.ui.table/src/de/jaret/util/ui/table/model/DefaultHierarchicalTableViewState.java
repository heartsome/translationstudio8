/*
 *  File: DefaultHierarchicalTableViewState.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Default implementation of a hierarchical view state.
 * 
 * @author Peter Kliem
 * @version $Id: DefaultHierarchicalTableViewState.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class DefaultHierarchicalTableViewState extends DefaultTableViewState implements IHierarchicalTableViewState {
    /** listener list. */
    protected List<IHierarchicalTableViewStateListener> _listeners;

    /** map holding the node expanded states. */
    protected Map<ITableNode, Boolean> _expandedStatesMap = new HashMap<ITableNode, Boolean>();

    /**
     * {@inheritDoc}
     */
    public boolean isExpanded(ITableNode node) {
        Boolean state = _expandedStatesMap.get(node);
        return !(state == null || !state.booleanValue());
    }

    /**
     * {@inheritDoc}
     */
    public void setExpanded(ITableNode node, boolean expanded) {
        boolean state = isExpanded(node);
        if (state != expanded) {
            _expandedStatesMap.put(node, expanded);
            if (expanded) {
                fireNodeExpanded(node);
            } else {
                fireNodeFolded(node);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setExpandedRecursive(ITableNode node, boolean expanded) {
        if (node.getChildren().size() > 0) {
            setExpanded(node, expanded);
            for (ITableNode child : node.getChildren()) {
                setExpandedRecursive(child, expanded);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void addHierarchicalViewstateListener(IHierarchicalTableViewStateListener htvsListener) {
        if (_listeners == null) {
            _listeners = new Vector<IHierarchicalTableViewStateListener>();
        }
        _listeners.add(htvsListener);
    }

    /**
     * {@inheritDoc}
     */
    public void remHierarchicalViewStateListener(IHierarchicalTableViewStateListener htvsListener) {
        if (_listeners != null) {
            _listeners.remove(htvsListener);
        }
    }

    /**
     * Inform listeners about a node expansion.
     * 
     * @param node expanded node
     */
    protected void fireNodeExpanded(ITableNode node) {
        if (_listeners != null) {
            for (IHierarchicalTableViewStateListener listener : _listeners) {
                listener.nodeExpanded(node);
            }
        }
    }

    /**
     * Infor listeners about a folded node.
     * 
     * @param node node that has been folded
     */
    protected void fireNodeFolded(ITableNode node) {
        if (_listeners != null) {
            for (IHierarchicalTableViewStateListener listener : _listeners) {
                listener.nodeFolded(node);
            }
        }
    }
}
