/*
 *  File: RiskCellEditor.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.examples.table.renderer;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

import de.jaret.examples.table.DummyRow;
import de.jaret.examples.table.DummyRow.Risk;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.editor.CellEditorBase;
import de.jaret.util.ui.table.editor.ICellEditor;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * CellEditor for a risk. Does only process clicks and key strokes. Keybindings are
 * <ul>
 * <li>'p': roll risk probability</li>
 * <li>'s': roll risk severity</li>
 * </ul>
 * 
 * @author Peter Kliem
 * @version $Id: RiskCellEditor.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class RiskCellEditor extends CellEditorBase implements ICellEditor {
    protected boolean _singleClick = false;

    public RiskCellEditor() {
    }

    public RiskCellEditor(boolean singleClick) {
        _singleClick = singleClick;
    }

    public Control getEditorControl(JaretTable table, IRow row, IColumn column, char typedKey) {
        if (typedKey == 'p' || typedKey == 'P') {
            rollProb(row, column);
        } else if (typedKey == 's' || typedKey == 'S') {
            rollSeverity(row, column);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void stopEditing(boolean storeInput) {
        // nothing to do
    }

    public boolean handleClick(JaretTable table, IRow row, IColumn column, Rectangle drawingArea, int x, int y) {
        boolean change = checkClick(table, row, column, drawingArea, x, y);
        return change;
    }

    boolean _forceSquare = true;

    private boolean checkClick(JaretTable table, IRow row, IColumn column, Rectangle rect, int x, int y) {
        if (_forceSquare) {
            int a = Math.min(rect.width, rect.height);
            Rectangle nrect = new Rectangle(0, 0, a, a);
            nrect.x = rect.x + (rect.width - a) / 2;
            nrect.y = rect.y + (rect.height - a) / 2;
            rect = nrect;
        }

        if (!rect.contains(x, y)) {
            return false;
        }

        int width = rect.width;
        int height = rect.height;

        int sWidth = (width - RiskRenderer.AXISOFFSET) / 3;
        int sHeight = (height - RiskRenderer.AXISOFFSET) / 3;

        int xx = x - rect.x;
        int yy = y - rect.y;
        int prob = xx / sWidth;
        int sev = yy / sHeight;

        if (prob >= 0 && sev >= 0) {
            sev = 2 - sev;
            Risk risk = ((DummyRow) row).new Risk(prob + 1, sev + 1);
            column.setValue(row, risk);
            return true;
        }

        return false;
    }

    private void rollProb(IRow row, IColumn column) {
        DummyRow.Risk risk = (Risk) column.getValue(row);
        int newProb = risk.getRiskProb() + 1;
        newProb = newProb > 3 ? 1 : newProb;
        column.setValue(row, ((DummyRow) row).new Risk(newProb, risk.getRiskSeverity()));
    }

    private void rollSeverity(IRow row, IColumn column) {
        DummyRow.Risk risk = (Risk) column.getValue(row);
        int newSev = risk.getRiskSeverity() + 1;
        newSev = newSev > 3 ? 1 : newSev;
        column.setValue(row, ((DummyRow) row).new Risk(risk.getRiskProb(), newSev));
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        super.dispose();
    }

}
