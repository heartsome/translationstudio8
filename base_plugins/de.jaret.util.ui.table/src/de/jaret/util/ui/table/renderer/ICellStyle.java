/*
 *  File: ICellStyle.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import de.jaret.util.misc.PropertyObservable;
import de.jaret.util.ui.table.model.ITableViewState;

/**
 * Interface describing the style of a cell.
 * 
 * @author Peter Kliem
 * @version $Id: ICellStyle.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public interface ICellStyle extends PropertyObservable {
    /** property name. */
    String HORIZONTAL_ALIGNMENT = "HorizontalAlignment";

    /** property name. */
    String VERTICAL_ALIGNMENT = "VerticalAlignment";

    /** property name. */
    String BACKGROUNDCOLOR = "BackgroundColor";

    /** property name. */
    String FOREGROUNDCOLOR = "ForegroundColor";

    /** property name. */
    String FONT = "Font";

    /** property name. */
    String BORDERCONFIGURATION = "BorderConfiguration";

    /** property name. */
    String BORDERCOLOR = "BorderColor";

    /**
     * Retrieve the border configuration.
     * 
     * @return the border configuration
     */
    IBorderConfiguration getBorderConfiguration();

    /**
     * Set the border configuration.
     * 
     * @param borderConfiguration the onfiguration to use
     */
    void setBorderConfiguration(IBorderConfiguration borderConfiguration);

    /**
     * Retrieve the border color.
     * 
     * @return border color
     */
    RGB getBorderColor();

    /**
     * Set the border color.
     * 
     * @param bordercolor border color
     */
    void setBorderColor(RGB bordercolor);

    /**
     * Retrieve the foreground color.
     * 
     * @return the foreground color
     */
    RGB getForegroundColor();

    /**
     * Set the foreground color.
     * 
     * @param foreground the fore ground colro to use
     */
    void setForegroundColor(RGB foreground);

    /**
     * Retrieve the background color.
     * 
     * @return the background color
     */
    RGB getBackgroundColor();

    /**
     * Set the background color.
     * 
     * @param background the color to use
     */
    void setBackgroundColor(RGB background);

    /**
     * Retrieve the font.
     * 
     * @return the font data for the font to use
     */
    FontData getFont();

    /**
     * Set the font.
     * 
     * @param fontdata font data of the font
     */
    void setFont(FontData fontdata);

    /**
     * Retrieve the horizontal alignment.
     * 
     * @return the horizontal alignment
     */
    ITableViewState.HAlignment getHorizontalAlignment();

    /**
     * Set the horizontal alignment.
     * 
     * @param alignment the horizontal alignment
     */
    void setHorizontalAlignment(ITableViewState.HAlignment alignment);

    /**
     * Retrieve the vertical alignment.
     * 
     * @return the vertical alignment
     */
    ITableViewState.VAlignment getVerticalAlignment();

    /**
     * Set the vertical alignemnt.
     * 
     * @param alignment the vertical alignment
     */
    void setVerticalAlignment(ITableViewState.VAlignment alignment);

    boolean getMultiLine();

    void setMultiLine(boolean multiLine);

    /**
     * Copy the cell style.
     * 
     * @return a copy of the cell style
     */
    ICellStyle copy();
}
