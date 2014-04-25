package net.heartsome.cat.ts.ui.jaret.renderer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.heartsome.cat.common.util.TextUtil;

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
 * 用于对单元格中的文本设置样式
 * @author peason
 * @version
 * @since JDK1.6
 */
public class StyleTextCellRenderer extends TextCellRenderer {

	private TextLayout textLayout;

	/** 添加样式的文本 */
	private String strStyleText;

	/** 设置样式时是否区分大小写 */
	private boolean blnIsCaseSensitive;

	/** 设置样式时是否使用正则表达式 */
	private boolean blnIsApplyRegular;

	private TextStyle style;

	/**
	 * 构造方法
	 * @param strStyleText
	 *            要加样式的文本
	 * @param blnIsCaseSensitive
	 *            是否区分大小写
	 */
	public StyleTextCellRenderer(String strStyleText, boolean blnIsCaseSensitive, boolean blnIsApplyRegular,TextStyle style) {
		super();
		this.strStyleText = strStyleText;
		this.blnIsCaseSensitive = blnIsCaseSensitive;
		this.blnIsApplyRegular = blnIsApplyRegular;	
		this.style = style;
	}
	
	@Override
	public void dispose() {		
		super.dispose();
	}
	
	public void draw(GC gc, JaretTable jaretTable, ICellStyle cellStyle, Rectangle drawingArea, IRow row,
			IColumn column, boolean drawFocus, boolean selected, boolean printing) {
		super.draw(gc, jaretTable, cellStyle, drawingArea, row, column, drawFocus, selected, printing);
		// Color bg = gc.getBackground();
		// Color fg = gc.getForeground();
		Rectangle drect = drawBorder(gc, cellStyle, drawingArea, printing);
		Rectangle rect = applyInsets(drect);
		String s = convertValue(row, column);
		if (s != null && strStyleText != null && !strStyleText.equals("")) {
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
			if (blnIsApplyRegular) {
				Pattern pattern = null;
				if (blnIsCaseSensitive) {
					pattern = Pattern.compile(strStyleText);
				} else {
					pattern = Pattern.compile(strStyleText, Pattern.CASE_INSENSITIVE);
				}
				Matcher matcher = pattern.matcher(s);
				while (matcher.find()) {
					textLayout.setStyle(style, matcher.start(), matcher.end() - 1);
				}
				gc.fillRectangle(rect);
				textLayout.draw(gc, rect.x, rect.y);
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
			} else {
				int index = -1;
				if (!blnIsCaseSensitive) {
					index = s.toUpperCase().indexOf(strStyleText.toUpperCase());
				} else {
					index = s.indexOf(strStyleText);
				}
				if (index != -1) {
					for (int i = 1; i < s.length(); i++) {
						int j = TextUtil.indexOf(s, strStyleText, i, blnIsCaseSensitive);
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
	}
}
