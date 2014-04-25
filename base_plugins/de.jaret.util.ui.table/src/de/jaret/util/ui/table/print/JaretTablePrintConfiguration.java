/*
 *  File: JaretTablePrintConfiguration.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.print;

/**
 * Simple structure to control the printing using the JaretTablePrinter.
 * 
 * @author Peter Kliem
 * @version $Id: JaretTablePrintConfiguration.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class JaretTablePrintConfiguration {
    protected boolean _repeatHeader;
    protected double _scale;
    protected String _name;
    protected String _footerText;
    protected int _rowLimit = -1;
    protected int _colLimit = -1;

    public JaretTablePrintConfiguration(String name, boolean repeatHeader, double scale) {
        _name = name;
        _repeatHeader = repeatHeader;
        _scale = scale;
    }

    public JaretTablePrintConfiguration() {
        this("Table", false, 1.0);
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return _name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * @return Returns the repeatHeader.
     */
    public boolean getRepeatHeader() {
        return _repeatHeader;
    }

    /**
     * @param repeatHeader The repeatHeader to set.
     */
    public void setRepeatHeader(boolean repeatHeader) {
        _repeatHeader = repeatHeader;
    }

    /**
     * @return Returns the scale.
     */
    public double getScale() {
        return _scale;
    }

    /**
     * @param scale The scale to set.
     */
    public void setScale(double scale) {
        _scale = scale;
    }

    /**
     * @return Returns the footerText.
     */
    public String getFooterText() {
        return _footerText;
    }

    /**
     * @param footerText The footerText to set.
     */
    public void setFooterText(String footerText) {
        _footerText = footerText;
    }

    /**
     * @return Returns the colLimit.
     */
    public int getColLimit() {
        return _colLimit;
    }

    /**
     * @param colLimit The colLimit to set.
     */
    public void setColLimit(int colLimit) {
        _colLimit = colLimit;
    }

    /**
     * @return Returns the rowLimit.
     */
    public int getRowLimit() {
        return _rowLimit;
    }

    /**
     * @param rowLimit The rowLimit to set.
     */
    public void setRowLimit(int rowLimit) {
        _rowLimit = rowLimit;
    }

}
