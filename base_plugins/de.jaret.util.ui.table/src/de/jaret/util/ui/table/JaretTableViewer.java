/*
 *  File: JaretTableViewer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table;

import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * JFace Structured viewer for the jaret table (minimal implementation).
 * 
 * @author Peter Kliem
 * @version $Id: JaretTableViewer.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class JaretTableViewer extends StructuredViewer {
    protected JaretTable _table;

    public JaretTableViewer(JaretTable table) {
        _table = table;
    }

    @Override
    protected Widget doFindInputItem(Object element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Widget doFindItem(Object element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
        // TODO Auto-generated method stub

    }

    @Override
    protected List getSelectionFromWidget() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void internalRefresh(Object element) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reveal(Object element) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void setSelectionToWidget(List l, boolean reveal) {
        // TODO Auto-generated method stub

    }

    @Override
    public Control getControl() {
        // TODO Auto-generated method stub
        return null;
    }

}
