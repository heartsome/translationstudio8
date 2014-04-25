/*
 *  File: AbstractRowFilter.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.model;

import de.jaret.util.misc.PropertyObservableBase;
import de.jaret.util.ui.table.filter.IRowFilter;

/**
 * Abstract base implementation of a RowFilter to allow easy anonymous inner classes to be constructed.
 * 
 * @author Peter Kliem
 * @version $Id: AbstractRowFilter.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public abstract class AbstractRowFilter extends PropertyObservableBase implements IRowFilter {

}
