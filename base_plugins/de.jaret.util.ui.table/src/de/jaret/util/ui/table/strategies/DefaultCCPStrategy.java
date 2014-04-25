/*
 *  File: DefaultCCPStrategy.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.strategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IJaretTableCell;
import de.jaret.util.ui.table.model.IJaretTableSelection;

/**
 * Default implementation for cut, copy, paste. See the the description of the methods for details. The implementation
 * is not yet perfect. It uses "brutal" String conversions. May be it would best to introduce converter services in the
 * table model (optional) or use methods in renderers or editors for conversion.
 * 
 * @author Peter Kliem
 * @version $Id: DefaultCCPStrategy.java,v 1.1 2012-05-07 01:34:37 jason Exp $
 */
public class DefaultCCPStrategy implements ICCPStrategy {
    /** Delimiter used when copying. */
    private static final String COPY_DELIMITER = "\t";
    /** Delimiters for separating fields in paste operations. */
    private static final String PASTE_DELIMITERS = "\t;";

    /** Clipboard instance. */
    private Clipboard _clipboard;
    /** If set to true header labels will always included in copies. */
    private boolean _includeHeadersInCopy = false;

    /**
     * Aquire clipboard.
     * 
     * @return Clipboard instance
     */
    private synchronized Clipboard getClipboard() {
        if (_clipboard == null) {
            _clipboard = new Clipboard(Display.getCurrent());
        }
        return _clipboard;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        if (_clipboard != null) {
            _clipboard.dispose();
        }
    }

    /**
     * Do the copy operation using the constant COPY_DELIMITER. Empty lines will be omitted, missing cels will be empty.
     * 
     * @param table jaret table the operation is invoked on
     */
    public void copy(JaretTable table) {
        cutOrCopy(table, false);
    }

    /**
     * Do the cut operation. Basicly a a copy and a empty operation.
     * 
     * @param table jaret table the operation is invoked on
     */
    public void cut(JaretTable table) {
        cutOrCopy(table, true);
    }

    /**
     * Do the actual copy or cut operation.
     * 
     * @param table table
     * @param cut if set to true cells we be emptied
     */
    protected void cutOrCopy(JaretTable table, boolean cut) {
        IJaretTableSelection selection = table.getSelectionModel().getSelection();
        Clipboard cb = getClipboard();
        if (!selection.isEmpty()) {
            Set<IJaretTableCell> cells = selection.getAllSelectedCells(table.getTableModel());
            int minx = -1;
            int maxx = -1;
            int miny = -1;
            int maxy = -1;
            // line is the outer map
            Map<Integer, Map<Integer, IJaretTableCell>> cellMap = new HashMap<Integer, Map<Integer, IJaretTableCell>>();
            for (IJaretTableCell cell : cells) {
                Point p = table.getCellDisplayIdx(cell);
                Map<Integer, IJaretTableCell> lineMap = cellMap.get(p.y);
                if (lineMap == null) {
                    lineMap = new HashMap<Integer, IJaretTableCell>();
                    cellMap.put(p.y, lineMap);
                }
                if (miny == -1 || p.y < miny) {
                    miny = p.y;
                }
                if (maxy == -1 || p.y > maxy) {
                    maxy = p.y;
                }
                lineMap.put(p.x, cell);
                if (minx == -1 || p.x < minx) {
                    minx = p.x;
                }
                if (maxx == -1 || p.x > maxx) {
                    maxx = p.x;
                }
            }
            StringBuilder buf = new StringBuilder();
            if (_includeHeadersInCopy) {
                for (int x = minx; x <= maxx; x++) {
                    String headerLabel = table.getColumn(x).getHeaderLabel();
                    buf.append(headerLabel);
                    buf.append(COPY_DELIMITER);
                }
                buf.append("\n");
            }
            for (int y = miny; y <= maxy; y++) {
                Map<Integer, IJaretTableCell> lineMap = cellMap.get(y);
                // empty lines are ommitted
                if (lineMap != null) {
                    for (int x = minx; x <= maxx; x++) {
                        IJaretTableCell cell = lineMap.get(x);
                        String value = null;
                        if (cell != null) {
                            Object val = cell.getColumn().getValue(cell.getRow());
                            value = val != null ? val.toString() : null;
                            if (cut) {
                                emptyCell(cell);
                            }
                        }
                        if (value != null) {
                            buf.append(value);
                        }
                        buf.append(COPY_DELIMITER);
                    }
                    buf.append("\n");
                }
            }
            TextTransfer textTransfer = TextTransfer.getInstance();
            cb.setContents(new Object[] {buf.toString()}, new Transfer[] {textTransfer});
        }
    }

    /**
     * Empty the given cell. First try null, if an exception is thrown by the modell try the empty string.
     * 
     * @param cell cell to be emptied
     */
    protected void emptyCell(IJaretTableCell cell) {
        try {
            cell.getColumn().setValue(cell.getRow(), null);
        } catch (Exception e) {
            try {
                cell.getColumn().setValue(cell.getRow(), "");
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    /**
     * Paste pastes textual context starting at the focussed cell (does not use the selection by now). Uses TAB and
     * semicolon as delimiters (Excel uses TAB, semicolon for pasting csv).
     * 
     * @param table the jaret table
     */
    public void paste(JaretTable table) {
        Clipboard cb = getClipboard();

        TextTransfer textTransfer = TextTransfer.getInstance();
        Object content = cb.getContents(textTransfer);
        if (content != null) {
            if (content instanceof String) {
                String string = (String) content;
                List<String> lines = new ArrayList<String>();
                StringTokenizer tokenizer = new StringTokenizer(string, "\n");
                while (tokenizer.hasMoreTokens()) {
                    lines.add(tokenizer.nextToken());
                }
                Point focus = table.getFocussedCellIdx();
                if (focus == null) {
                    table.setFocus();
                    focus = table.getFocussedCellIdx();
                }
                int lineOff = 0;
                for (String line : lines) {
                    tokenizer = new StringTokenizer(line, PASTE_DELIMITERS, true);
                    int colOff = 0;
                    String last = null;
                    while (tokenizer.hasMoreTokens()) {
                        String value = tokenizer.nextToken();
                        boolean ignore = false;
                        if (PASTE_DELIMITERS.indexOf(value) != -1) {
                            // delimiter
                            if (last != null && last.equals(value)) {
                                value = "";
                            } else {
                                ignore = true;
                            }
                        }
                        if (!ignore) {
                            try {
                                table.setValue(focus.x + colOff, focus.y + lineOff, value);
                            } catch (Exception e) {
                                // silently ignore -- this can happen
                            }

                            colOff++;
                        }
                        last = value;
                    }
                    lineOff++;
                }
            }
        }
    }

    /**
     * Retrieve the state of header include in the copied content.
     * 
     * @return the includeHeadersInCopy
     */
    public boolean getIncludeHeadersInCopy() {
        return _includeHeadersInCopy;
    }

    /**
     * Set includeHeaders: if set to true in copy and cut context the headline (=col headers) labels will be included.
     * 
     * @param includeHeadersInCopy the includeHeadersInCopy to set
     */
    public void setIncludeHeadersInCopy(boolean includeHeadersInCopy) {
        _includeHeadersInCopy = includeHeadersInCopy;
    }

}
