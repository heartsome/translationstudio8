/*
 *  File: IAutoFilter.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.filter;

import org.eclipse.swt.widgets.Control;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;

/**
 * Interface describing an autofilter to be used within a jaret table. An autofilter will be instantiated for every
 * column it is used on. So it is absolutely necessary that an autofilter can be instantiated by a default constructor.
 * 
 * @author kliem
 * @version $Id: IAutoFilter.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public interface IAutoFilter extends IRowFilter {

    /**
     * Tell the autofilter which table he serves. This will be called after instantiating the autofilter.
     * 
     * @param table table the autofilter is used with
     */
    void setTable(JaretTable table);

    /**
     * Tell the autofilter on which column it works. This method will be called once after the filter has been
     * instantiated.
     * 
     * @param column column for the autofilter
     */
    void setColumn(IColumn column);

    /**
     * Get the control representing the autofilter (most probably a combo box).
     * 
     * @return configured control representing the filter
     */
    Control getControl();

    /**
     * Update/create the control and internal state.
     */
    void update();
    
    /**
     * Remove any selection (usually revert to a setting that filters nothing).
     */
    void reset();
    
    /**
     * Dispose all resources.
     */
    void dispose();

}
