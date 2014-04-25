/*
 *  File: IHierarchicalTableViewState.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

/**
 * Interface describing a hierarchical tabel viewstate. Extends the falt table viewstate.
 * 
 * @author Peter Kliem
 * @version $Id: IHierarchicalTableViewState.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IHierarchicalTableViewState extends ITableViewState {
    /**
     * Check whether a node is expanded.
     * 
     * @param node node to check
     * @return true for expanded
     */
    boolean isExpanded(ITableNode node);

    /**
     * Set the expanded state for a single node.
     * 
     * @param node node
     * @param expanded true for expanded
     */
    void setExpanded(ITableNode node, boolean expanded);

    /**
     * Set the expanded state for a node and all of it's children.
     * 
     * @param node node to begin with
     * @param expanded expanded state
     */
    void setExpandedRecursive(ITableNode node, boolean expanded);

    /**
     * Add a view state listener.
     * 
     * @param htvsListener listener to add
     */
    void addHierarchicalViewstateListener(IHierarchicalTableViewStateListener htvsListener);

    /**
     * Remove a view state listener.
     * 
     * @param htvsListener listener to remove
     */
    void remHierarchicalViewStateListener(IHierarchicalTableViewStateListener htvsListener);
}
