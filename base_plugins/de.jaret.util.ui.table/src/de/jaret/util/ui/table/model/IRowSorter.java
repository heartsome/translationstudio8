/*
 *  File: IRowSorter.java 
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

import de.jaret.util.misc.PropertyObservable;

/**
 * A comparator for IRow to be set on the table. It is a PropertyObeservable to allow the table to react on changes with
 * a refresh.
 * 
 * @author Peter Kliem
 * @version $Id: IRowSorter.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface IRowSorter extends Comparator<IRow>, PropertyObservable {
}
