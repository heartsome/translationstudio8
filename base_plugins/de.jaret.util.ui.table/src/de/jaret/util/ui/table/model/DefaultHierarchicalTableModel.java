/*
 *  File: DefaultHierarchicalTableModel.java 
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
 * Default implementation of a hierarchical jaret table model.
 * 
 * @author Peter Kliem
 * @version $Id: DefaultHierarchicalTableModel.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class DefaultHierarchicalTableModel implements IHierarchicalJaretTableModel {
    /** the root node. */
    protected ITableNode _root;

    /**
     * Construct the model.
     * 
     * @param root the root node
     */
    public DefaultHierarchicalTableModel(ITableNode root) {
        _root = root;
    }

    /**
     * {@inheritDoc}
     */
    public ITableNode getRootNode() {
        return _root;
    }

}
