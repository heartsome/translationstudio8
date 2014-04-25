/*
 *  File: DefaultBorderConfiguration.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import java.util.StringTokenizer;

/**
 * Default implementation of a BorderConfiguration.
 * 
 * @author Peter Kliem
 * @version $Id: DefaultBorderConfiguration.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class DefaultBorderConfiguration implements IBorderConfiguration {
    /** left border. */
    protected int _borderLeft;
    /** right border. */
    protected int _borderRight;
    /** top border. */
    protected int _borderTop;
    /** bottom border. */
    protected int _borderBottom;

    /**
     * Construct a border configuration.
     * 
     * @param left left border
     * @param right right border
     * @param top top border
     * @param bottom bottom border
     */
    public DefaultBorderConfiguration(int left, int right, int top, int bottom) {
        _borderBottom = bottom;
        _borderLeft = left;
        _borderRight = right;
        _borderTop = top;
    }

    /**
     * Construct a borderconfiguration form a comma separated string holding the values (no error hndling).
     * 
     * @param str csv string
     */
    public DefaultBorderConfiguration(String str) {
        StringTokenizer tokenizer = new StringTokenizer(str, ",");
        _borderLeft = Integer.parseInt(tokenizer.nextToken());
        _borderRight = Integer.parseInt(tokenizer.nextToken());
        _borderTop = Integer.parseInt(tokenizer.nextToken());
        _borderBottom = Integer.parseInt(tokenizer.nextToken());
    }

    /**
     * {@inheritDoc}
     */
    public DefaultBorderConfiguration copy() {
        DefaultBorderConfiguration bc = new DefaultBorderConfiguration(_borderLeft, _borderRight, _borderTop,
                _borderBottom);
        return bc;
    }

    /**
     * {@inheritDoc} Produces a string that can be passed to the constructor as a csv string.
     */
    public String toString() {
        return _borderLeft + "," + _borderRight + "," + _borderTop + "," + _borderBottom;
    }

    /**
     * @return Returns the borderBottom.
     */
    public int getBorderBottom() {
        return _borderBottom;
    }

    /**
     * @param borderBottom The borderBottom to set.
     */
    public void setBorderBottom(int borderBottom) {
        _borderBottom = borderBottom;
    }

    /**
     * @return Returns the borderLeft.
     */
    public int getBorderLeft() {
        return _borderLeft;
    }

    /**
     * @param borderLeft The borderLeft to set.
     */
    public void setBorderLeft(int borderLeft) {
        _borderLeft = borderLeft;
    }

    /**
     * @return Returns the borderRight.
     */
    public int getBorderRight() {
        return _borderRight;
    }

    /**
     * @param borderRight The borderRight to set.
     */
    public void setBorderRight(int borderRight) {
        _borderRight = borderRight;
    }

    /**
     * @return Returns the borderTop.
     */
    public int getBorderTop() {
        return _borderTop;
    }

    /**
     * @param borderTop The borderTop to set.
     */
    public void setBorderTop(int borderTop) {
        _borderTop = borderTop;
    }

}
