/**
 * LineNumberPainter.java
 *
 * Version information :
 *
 * Date:Mar 1, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.xliffeditor.nattable.painter;

import java.util.List;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.TextPainter;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class LineNumberPainter extends TextPainter {

	public LineNumberPainter() {
		super(true, false);
	}

	@Override
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return 0;
	}

	public void paintCell(LayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		Rectangle cellBounds = cell.getBounds();
		// Color backgroundColor = CellStyleUtil.getCellStyle(cell,
		// configRegistry).getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		// if (backgroundColor != null) {
		Color originalBackground = gc.getBackground();

		gc.setBackground(GUIHelper.COLOR_WIDGET_BACKGROUND);
		gc.fillRectangle(bounds);

		gc.setBackground(originalBackground);
		// }
		if (checkSplit(cell, configRegistry)) {
			// Color originalBackground = gc.getBackground();
			// gc.setBackground(GUIHelper.COLOR_RED);
			// gc.fillRectangle(cellBounds);
			// gc.setBackground(originalBackground);
			// gc.setBackgroundPattern(new Pattern(Display.getCurrent(),
			// XliffEditorGUIHelper.getImage(XliffEditorGUIHelper.ImageName.SPLITPOINT)));
			Image image = XliffEditorGUIHelper.getImage(XliffEditorGUIHelper.ImageName.SPLITPOINT);
			gc.drawImage(image, cellBounds.width / 2 - image.getBounds().width / 2, cellBounds.y + cellBounds.height
					/ 2 - image.getBounds().height / 2);
			// gc.setBackgroundPattern(null);
			//
			// }
			// else {
		}
		super.paintCell(cell, gc, bounds, configRegistry);
	}

	/**
	 * 通过LayerCell得到行号确定所要得到的TU对象，通过TU对象的各种属性确定其状态图片
	 * @param cell
	 * @param configRegistry
	 * @return ;
	 */
	protected boolean checkSplit(LayerCell cell, IConfigRegistry configRegistry) {

		int index = cell.getLayer().getRowIndexByPosition(cell.getRowPosition());

		// 添加分割点的图标，--robert
		XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();
		if (editor != null) {
			if (!editor.isHorizontalLayout()) {
				index = index / 2;
			}
			String rowId = editor.getXLFHandler().getRowId(index);
			List<String> splitPoints = editor.getSplitXliffPoints();
			if (splitPoints.indexOf(rowId) != -1) {
				return true;
			}
		}
		return false;
	}
}
