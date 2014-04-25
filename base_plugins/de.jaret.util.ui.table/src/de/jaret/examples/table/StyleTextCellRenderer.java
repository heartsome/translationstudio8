package de.jaret.examples.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.renderer.ICellStyle;
import de.jaret.util.ui.table.renderer.TextCellRenderer;

public class StyleTextCellRenderer extends TextCellRenderer {

	private TextLayout textLayout;

	/** 添加样式的文本 */
	private String strStyleText;

	/** 设置样式时是否区分大小写 */
	private boolean blnIsCaseSensitive;

	private TextStyle style;
	
	public StyleTextCellRenderer(String strStyleText, boolean blnIsCaseSensitive) {
        super();
        this.strStyleText = strStyleText;
        this.blnIsCaseSensitive = blnIsCaseSensitive;
    }

	public void draw(GC gc, JaretTable jaretTable, ICellStyle cellStyle, Rectangle drawingArea, IRow row,
			IColumn column, boolean drawFocus, boolean selected, boolean printing) {
		super.draw(gc, jaretTable, cellStyle, drawingArea, row, column, drawFocus, selected, printing);
		Rectangle drect = drawBorder(gc, cellStyle, drawingArea, printing);
		Rectangle rect = applyInsets(drect);
		String s = convertValue(row, column);
		if (s != null && strStyleText != null) {
			int index = -1;
			if (blnIsCaseSensitive) {
				index = s.toUpperCase().indexOf(strStyleText.toUpperCase());
			} else {
				index = s.indexOf(strStyleText);
			}
			if (index != -1) {
				if (textLayout == null) {
					textLayout = new TextLayout(gc.getDevice());
					jaretTable.getParent().addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							textLayout.dispose();
						}
					});
				}
				textLayout.setText(s);
				textLayout.setFont(gc.getFont());
				textLayout.setWidth(rect.width);
				if (style == null) {
					final Color color = new Color(gc.getDevice(), 150, 100, 100);
					final Font font = new Font(gc.getDevice(), gc.getFont().getFontData()[0].getName(), gc.getFont()
							.getFontData()[0].getHeight(), SWT.ITALIC);
					style = new TextStyle(font, color, null);
					jaretTable.getParent().addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							color.dispose();
							font.dispose();
						}
					});
				}
				for (int i = 1; i < strStyleText.length(); i++) {
					int j = indexOf(s, strStyleText, i, blnIsCaseSensitive);
					if (j != -1) {
						textLayout.setStyle(style, j, j + strStyleText.length() - 1);
					} else {
						break;
					}

				}
				gc.fillRectangle(rect);
				textLayout.draw(gc, rect.x, rect.y);
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
			}
		}
	}
	public int indexOf(String srcStr, String str, int index, boolean isCaseSensitive) {
		if (index == 0) {
			return -1;
		}
		if (index == 1) {
			if (isCaseSensitive) {
				return srcStr.indexOf(str);
			} else {
				return srcStr.toUpperCase().indexOf(str.toUpperCase());
			}
		}
		if (isCaseSensitive) {
			return srcStr.indexOf(str, indexOf(srcStr, str, index - 1, isCaseSensitive) + str.length());
		} else {
			return srcStr.toUpperCase().indexOf(str.toUpperCase(),
					indexOf(srcStr, str, index - 1, isCaseSensitive) + str.length());
		}
	}
}
