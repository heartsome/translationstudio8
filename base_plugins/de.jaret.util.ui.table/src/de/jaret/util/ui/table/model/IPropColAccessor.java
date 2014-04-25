/*
 *  File: IPropColAccessor.java 
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
 * Interface describing an accessor to be used together with the PropCol.
 * 
 * @author kliem
 * @version $Id: IPropColAccessor.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IPropColAccessor {
    /**
     * Retrieve the value from the base object.
     * 
     * @param base base object
     * @return value
     */
    Object getValue(Object base);

    /**
     * Set a value on the base object.
     * 
     * @param base base object
     * @param value value to be set
     */
    void setValue(Object base, Object value);
}
