/*
 *  File: IBorderConfiguration.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

/**
 * Interface describing a border to be rendered around a cell.
 * 
 * @author Peter Kliem
 * @version $Id: IBorderConfiguration.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public interface IBorderConfiguration {
    /**
     * Get left border width.
     * 
     * @return border width in pixels
     */
    int getBorderLeft();

    /**
     * Get right border width.
     * 
     * @return border width in pixels
     */
    int getBorderRight();

    /**
     * Get top border width.
     * 
     * @return border width in pixels
     */
    int getBorderTop();

    /**
     * Get bottom border width.
     * 
     * @return border width in pixels
     */
    int getBorderBottom();

    /**
     * Produce a copy of this border configuration.
     * 
     * @return copy of the configuration
     */
    IBorderConfiguration copy();
}
