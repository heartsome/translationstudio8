/*
 *  File: ICCPStrategy.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.strategies;

import de.jaret.util.ui.table.JaretTable;

/**
 * Interface describing the strategies used for Cut, Copy and Paste.
 * 
 * @author Peter Kliem
 * @version $Id: ICCPStrategy.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public interface ICCPStrategy {
    /**
     * Do the cut operation.
     * 
     * @param table table the operation should be performed on
     */
    void cut(JaretTable table);

    /**
     * Do the copy operation.
     * 
     * @param table table the operation should be performed on
     */
    void copy(JaretTable table);

    /**
     * Do the paste operation.
     * 
     * @param table table the operation should be performed on
     */
    void paste(JaretTable table);

    /**
     * If there is something to dispose ... most probably the clipboard instance.
     * 
     */
    void dispose();
}
