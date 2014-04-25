/*
 *  File: ITableNode.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import java.util.List;

/**
 * Interface describing a table row in a hierarchy of rows.
 * 
 * @author Peter Kliem
 * @version $Id: ITableNode.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface ITableNode extends IRow {

    /**
     * Retriev all children of the node.
     * 
     * @return chrildren of the node
     */
    List<ITableNode> getChildren();

    /**
     * Retrieve the level in the tree.
     * 
     * @return level in the tree.
     */
    int getLevel();

    /**
     * Tell the node it's level.
     * 
     * @TODO remove
     * @param level level of the node
     */
    void setLevel(int level);

    /**
     * Add a node as a child.
     * 
     * @param node child to be added.
     */
    void addNode(ITableNode node);

    /**
     * Remove a child node.
     * 
     * @param node node to remove.
     */
    void remNode(ITableNode node);

    /**
     * Add a listener to listen for node changes.
     * 
     * @param tnl listener to add
     */
    void addTableNodeListener(ITableNodeListener tnl);

    /**
     * Remove a listener registered for node changes.
     * 
     * @param tnl listener to remove
     */
    void removeTableNodeListener(ITableNodeListener tnl);
}
