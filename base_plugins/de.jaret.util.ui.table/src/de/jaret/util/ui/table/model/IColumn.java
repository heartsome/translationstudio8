/*
 *  File: IColumn.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import java.util.Comparator;

/**
 * Interface for a column used in a jaret table model. The unique id is <b>only</b> used for storing view state
 * information.
 * 
 * @author Peter Kliem
 * @version $Id: IColumn.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IColumn extends Comparator<IRow> {
    /**
     * Id is used for storing the column width. It has to be unique among all columns if the use of the view state
     * persisting support will be used.
     * 
     * @return unique id.
     */
    String getId();

    /**
     * Return a textual label to be displayed as the column header label.
     * 
     * @return header label
     */
    String getHeaderLabel();

    /**
     * Should return true for a header to be painted. Note that this ia a small violation of the separation between
     * viewstate and data. However this can be tolerated.
     * 
     * @return true when a header should be painted
     */
    boolean displayHeader();

    /**
     * Retrieve the value of the column for the given row.
     * 
     * @param row the row
     * @return the column value for the given row.
     */
    Object getValue(IRow row);

    /**
     * Set the value of the coloumn for a given row.
     * 
     * @param row the row
     * @param value value to set
     */
    void setValue(IRow row, Object value);

    /**
     * Check whether the column supports sorting.
     * 
     * @return true when sorting is supported.
     */
    boolean supportsSorting();

    /**
     * To allow null values as column value and to support cell editing and displaying a column may support this method
     * for supplying the information.
     * 
     * @return the contained class or null if the information is not available.
     */
    Class<?> getContentClass();

    /**
     * To specify a content class per row this method may be implemented to reflect the appropriate class.
     * 
     * @param row row of which to get the content class
     * @return contained class or null if the information is not available.
     */
    Class<?> getContentClass(IRow row);

    /**
     * Check whether the column can be edited.
     * 
     * @return true if the values of the columns can be changed
     */
    boolean isEditable();

    /**
     * Check whether a a specific cell of the column can be edited.
     * 
     * @param row row specifying the cell in the column
     * @return true if the ell can be changed
     */
    boolean isEditable(IRow row);

    /**
     * Add a listener to listen on changes on the column.
     * 
     * @param cl listener to add
     */
    void addColumnListener(IColumnListener cl);

    /**
     * Remove a column listener.
     * 
     * @param cl listener to remove
     */
    void remColumnListener(IColumnListener cl);

}
