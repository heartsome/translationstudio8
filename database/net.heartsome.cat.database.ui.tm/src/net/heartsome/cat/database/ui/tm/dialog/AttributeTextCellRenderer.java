package net.heartsome.cat.database.ui.tm.dialog;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.swt.ColorManager;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.renderer.ICellStyle;
import de.jaret.util.ui.table.renderer.TextCellRenderer;

/**
 * 相关搜索属性列的 TextCellRenderer
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class AttributeTextCellRenderer extends TextCellRenderer {
	
	private TextLayout textLayout;
	
	private TextStyle style;
	
	private HashMap<IRow, ArrayList<int[]>> mapStyle;
	
	public AttributeTextCellRenderer(HashMap<IRow, ArrayList<int[]>> mapStyle, TextStyle style) {
		super();
		this.mapStyle = mapStyle;
		this.style = style;
	}
	
	public void draw(GC gc, JaretTable jaretTable, ICellStyle cellStyle, Rectangle drawingArea, IRow row,
			IColumn column, boolean drawFocus, boolean selected, boolean printing) {
		super.draw(gc, jaretTable, cellStyle, drawingArea, row, column, drawFocus, selected, printing);
		// Color bg = gc.getBackground();
		// Color fg = gc.getForeground();
		Rectangle drect = drawBorder(gc, cellStyle, drawingArea, printing);
		Rectangle rect = applyInsets(drect);
		String s = convertValue(row, column);
		if (s != null && mapStyle != null && mapStyle.containsKey(row)) {
			if (selected && !printing) {
				Color color = ColorManager.getColorManager(Display.getCurrent()).getColor(new RGB(218, 218, 218));
				gc.setBackground(color);
			} else {
				gc.setBackground(getBackgroundColor(cellStyle, printing));
			}
			if (textLayout == null) {
				textLayout = new TextLayout(gc.getDevice());
				jaretTable.getParent().addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						textLayout.dispose();
					}
				});
			}
			textLayout.setOrientation(jaretTable.getOrientation());
			textLayout.setText(s);
			textLayout.setFont(gc.getFont());
			textLayout.setWidth(rect.width);
			ArrayList<int[]> lstIndex = mapStyle.get(row);
			if (lstIndex != null) {
				for (int[] arrIndex : lstIndex) {
					if (arrIndex != null && arrIndex.length == 2) {
						textLayout.setStyle(style, arrIndex[0], arrIndex[1] - 1);
					}
				}
				gc.fillRectangle(rect);
				textLayout.draw(gc, rect.x, rect.y);
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
			}
		}
	}
}
