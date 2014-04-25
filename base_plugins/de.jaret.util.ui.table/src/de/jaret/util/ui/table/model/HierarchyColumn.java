/*
 *  File: HierarchyColumn.java 
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
 * Dummy column for placement of the hierarchy.
 * 
 * @author Peter Kliem
 * @version $Id: HierarchyColumn.java,v 1.1 2012-05-07 01:34:36 jason Exp $
 */
public class HierarchyColumn extends AbstractColumn {

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return "hierarchycolumnID";
    }

    /**
     * {@inheritDoc}
     */
    public String getHeaderLabel() {
        return "hierarchy";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean displayHeader() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(IRow row) {
        return "hierarchy column";
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(IRow row, Object value) {
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsSorting() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getContentClass() {
        return String.class;
    }

    /**
     * {@inheritDoc}
     */
    public int compare(IRow o1, IRow o2) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEditable() {
        return false;
    }
}
