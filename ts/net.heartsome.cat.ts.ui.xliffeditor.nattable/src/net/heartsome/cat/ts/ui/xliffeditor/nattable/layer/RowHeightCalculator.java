/**
 * RowHeightCalculator.java
 *
 * Version information :
 *
 * Date:2013-8-12
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.xliffeditor.nattable.layer;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable.BodyLayerStack;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.selection.SelectionLayer;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class RowHeightCalculator {
	private BodyLayerStack bodyLayer;
	private NatTable table;
	private final int defaultRowheight;

	public RowHeightCalculator(BodyLayerStack bodyLayer, NatTable table, int defaultRowHeight) {
		this.defaultRowheight = defaultRowHeight;
		this.bodyLayer = bodyLayer;
		this.table = table;
	}

	public int recaculateRowHeight(int rowIndex) {
		int height = bodyLayer.getBodyDataLayer().getRowHeightByPosition(rowIndex);
//		if (height == defaultRowheight) {
			height = getPreferredRowHeight(rowIndex, table.getConfigRegistry(), table.getClientArea().height);
			bodyLayer.getBodyDataLayer().setRowHeightByPositionWithoutEvent(rowIndex, height);
//		}
		return height;
	}

	private int getPreferredRowHeight(int rowPosition, IConfigRegistry configRegistry, int clientAreaHeight) {
		int maxHeight = 0;
		ICellPainter painter;
		LayerCell cell;
		SelectionLayer layer = bodyLayer.getSelectionLayer();
		for (int columnPosition = 0; columnPosition < layer.getColumnCount(); columnPosition++) {
			cell = layer.getCellByPosition(columnPosition, rowPosition);
			if (cell != null) {
				painter = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_PAINTER, cell.getDisplayMode(),
						bodyLayer.getConfigLabelsByPosition(columnPosition, rowPosition).getLabels());
				if (painter != null) {
					int preferedHeight = painter.getPreferredHeight(cell, null, configRegistry);
					maxHeight = (preferedHeight > maxHeight) ? preferedHeight : maxHeight;
				}
			}
		}

		if (maxHeight > clientAreaHeight) {
			return clientAreaHeight;
		}
		return maxHeight;
	}
}
