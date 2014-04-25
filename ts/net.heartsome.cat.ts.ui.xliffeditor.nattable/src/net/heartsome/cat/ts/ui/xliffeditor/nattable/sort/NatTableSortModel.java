package net.heartsome.cat.ts.ui.xliffeditor.nattable.sort;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.sourceforge.nattable.sort.ISortModel;
import net.sourceforge.nattable.sort.SortDirectionEnum;

/**
 * 控制排序的模型
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class NatTableSortModel implements ISortModel {

	private List<Integer> sortedColumnIndexes;
	private List<Integer> sortOrders;
	private List<SortDirectionEnum> sortDirections;

	private XLFHandler handler;

	public NatTableSortModel(XLFHandler handler) {
		sortedColumnIndexes = new ArrayList<Integer>();
		sortOrders = new ArrayList<Integer>();
		sortDirections = new ArrayList<SortDirectionEnum>();
		this.handler = handler;
	}

	public SortDirectionEnum getSortDirection(int columnIndex) {
		if (sortedColumnIndexes.contains(columnIndex)) {
			return sortDirections.get(sortOrders.indexOf(columnIndex));
		}
		return SortDirectionEnum.NONE;
	}

	public int getSortOrder(int columnIndex) {
		if (sortedColumnIndexes.contains(columnIndex)) {
			return sortOrders.indexOf(columnIndex);
		}
		return -1;
	}

	public boolean isColumnIndexSorted(int columnIndex) {
		return sortedColumnIndexes.contains(columnIndex);
	}

	public void sort(int columnIndex, SortDirectionEnum direction, boolean accumulate) {
		String columnName = null;
		XLIFFEditorImplWithNatTable xliffEditor = XLIFFEditorImplWithNatTable.getCurrent();
		if (xliffEditor == null) {
			return;
		}
		if (xliffEditor.isHorizontalLayout()) {
			if (columnIndex == xliffEditor.getSrcColumnIndex()) {
				columnName = "source";
			} else if (columnIndex == xliffEditor.getTgtColumnIndex()) {
				columnName = "target";
			}
		} else {
			if (columnIndex == VerticalNatTableConfig.SOURCE_COL_INDEX) {
				columnName = "source";
			}
		}

		if (columnName == null) {
			return;
		}
		HsMultiActiveCellEditor.commit(true);
		clear();
		sortedColumnIndexes.add(columnIndex);
		sortOrders.add(columnIndex);
		sortDirections.add(direction);

		switch (direction) {
		case NONE:
			handler.resetRowIdsToUnsorted();
			break;
		case ASC:
			handler.sort(columnName, true);
			break;
		case DESC:
			handler.sort(columnName, false);
			break;
		default:
			break;
		}
		xliffEditor.autoResize();
//		Bug #2317：选中文本段后排序，不会刷新状态栏中的序号
		xliffEditor.updateStatusLine();
		HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
	}

	public void clear() {
		sortedColumnIndexes.clear();
		sortOrders.clear();
		sortDirections.clear();
	}
}
