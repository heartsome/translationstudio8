/*
 *  File: DefaultCellStyle.java 
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

import de.jaret.util.misc.PropertyObservableBase;
import de.jaret.util.ui.table.model.ITableViewState;
import de.jaret.util.ui.table.model.ITableViewState.HAlignment;
import de.jaret.util.ui.table.model.ITableViewState.VAlignment;

/**
 * Default implementation of ICellStyle.
 * 
 * @author Peter Kliem
 * @version $Id: DefaultCellStyle.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class DefaultCellStyle extends PropertyObservableBase implements ICellStyle {
    /** the border configuraion. */
    protected IBorderConfiguration _borderConfiguration;
    /** foreground color as RGB (no color ressource here). */
    protected RGB _foregroundColor;
    /** background color as RGB (no color ressource here). */
    protected RGB _backgroundColor;
    /** border color as RGB (no color ressource here). */
    protected RGB _borderColor;
    /** the font data to use (no font ressource here). */
    protected FontData _font;
    /** horizontal alignement. */
    protected ITableViewState.HAlignment _hAlignment = ITableViewState.HAlignment.LEFT;
    /** vertical alignment. */
    protected ITableViewState.VAlignment _vAlignment = ITableViewState.VAlignment.TOP;
    /** should allow multinline.*/
    protected boolean _multiLine = true;

    /**
     * Construct a new default cell style.
     * 
     * @param foregroundColor forgeround
     * @param backgroundColor background
     * @param borderConfiguration border config
     * @param font fontdata 
     */
    public DefaultCellStyle(RGB foregroundColor, RGB backgroundColor, IBorderConfiguration borderConfiguration,
            FontData font) {
        _foregroundColor = foregroundColor;
        _backgroundColor = backgroundColor;
        _borderConfiguration = borderConfiguration;
        _font = font;
    }

    /**
     * {@inheritDoc}
     */
    public DefaultCellStyle copy() {
        DefaultCellStyle cs = new DefaultCellStyle(_foregroundColor, _backgroundColor, _borderConfiguration.copy(),
                _font);
        cs.setHorizontalAlignment(_hAlignment);
        cs.setVerticalAlignment(_vAlignment);
        return cs;
    }

    /**
     * @return Returns the backgroundColor.
     */
    public RGB getBackgroundColor() {
        return _backgroundColor;
    }

    /**
     * @param backgroundColor The backgroundColor to set.
     */
    public void setBackgroundColor(RGB backgroundColor) {
        if (isRealModification(_backgroundColor, backgroundColor)) {
            RGB oldVal = _backgroundColor;
            _backgroundColor = backgroundColor;
            firePropertyChange(BACKGROUNDCOLOR, oldVal, backgroundColor);
        }
    }

    /**
     * @return Returns the borderColor.
     */
    public RGB getBorderColor() {
        return _borderColor;
    }

    /**
     * {@inheritDoc}
     */
    public void setBorderColor(RGB borderColor) {
        if (isRealModification(_borderColor, borderColor)) {
            RGB oldVal = _borderColor;
            _borderColor = borderColor;
            firePropertyChange(BORDERCOLOR, oldVal, borderColor);
        }
    }

    /**
     * @return Returns the borderConfiguration.
     */
    public IBorderConfiguration getBorderConfiguration() {
        return _borderConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    public void setBorderConfiguration(IBorderConfiguration borderConfiguration) {
        if (isRealModification(_borderConfiguration, borderConfiguration)) {
            IBorderConfiguration oldVal = _borderConfiguration;
            _borderConfiguration = borderConfiguration;
            firePropertyChange(BORDERCONFIGURATION, oldVal, borderConfiguration);
        }
    }

    /**
     * @return Returns the font.
     */
    public FontData getFont() {
        return _font;
    }

    /**
     * @param font The font to set.
     */
    public void setFont(FontData font) {
        if (isRealModification(_font, font)) {
            FontData oldVal = _font;
            _font = font;
            firePropertyChange(FONT, oldVal, font);
        }
    }

    /**
     * @return Returns the foregroundColor.
     */
    public RGB getForegroundColor() {
        return _foregroundColor;
    }

    /**
     * @param foregroundColor The foregroundColor to set.
     */
    public void setForegroundColor(RGB foregroundColor) {
        if (isRealModification(_foregroundColor, foregroundColor)) {
            RGB oldVal = _foregroundColor;
            _foregroundColor = foregroundColor;
            firePropertyChange(FOREGROUNDCOLOR, oldVal, foregroundColor);
        }
    }

    /**
     * {@inheritDoc}
     */
    public HAlignment getHorizontalAlignment() {
        return _hAlignment;
    }

    /**
     * {@inheritDoc}
     */
    public void setHorizontalAlignment(HAlignment alignment) {
        if (!_hAlignment.equals(alignment)) {
            HAlignment oldVal = _hAlignment;
            _hAlignment = alignment;
            firePropertyChange(HORIZONTAL_ALIGNMENT, oldVal, alignment);
        }
    }

    /**
     * {@inheritDoc}
     */
    public VAlignment getVerticalAlignment() {
        return _vAlignment;
    }

    /**
     * {@inheritDoc}
     */
    public void setVerticalAlignment(VAlignment alignment) {
        if (!_vAlignment.equals(alignment)) {
            VAlignment oldVal = _vAlignment;
            _vAlignment = alignment;
            firePropertyChange(VERTICAL_ALIGNMENT, oldVal, alignment);
        }
    }

    /**
     * @return Returns the multiLine.
     */
    public boolean getMultiLine() {
        return _multiLine;
    }

    /**
     * @param multiLine The multiLine to set.
     */
    public void setMultiLine(boolean multiLine) {
        _multiLine = multiLine;
    }

}
