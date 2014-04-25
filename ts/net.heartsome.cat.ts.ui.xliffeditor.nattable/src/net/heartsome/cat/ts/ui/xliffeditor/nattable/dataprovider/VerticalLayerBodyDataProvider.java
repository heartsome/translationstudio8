package net.heartsome.cat.ts.ui.xliffeditor.nattable.dataprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.sourceforge.nattable.data.IColumnAccessor;
import net.sourceforge.nattable.data.ISpanningDataProvider;
import net.sourceforge.nattable.layer.cell.DataCell;

public class VerticalLayerBodyDataProvider<T> extends XliffEditorDataProvider<T> implements ISpanningDataProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(XLIFFEditorImplWithNatTable.class);

	public VerticalLayerBodyDataProvider(XLFHandler handler, IColumnAccessor<T> columnAccessor) {
		super(handler, columnAccessor);
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		if (columnIndex == VerticalNatTableConfig.ID_COL_INDEX) { // ID 列
			if (rowIndex % VerticalNatTableConfig.ROW_SPAN == 0) { // 第一列要显示ID的地方
				return rowIndex / VerticalNatTableConfig.ROW_SPAN + 1;
			}
		} else if (columnIndex == VerticalNatTableConfig.STATUS_COL_INDEX) { // 状态列
			return "flag";
		} else { // Source和Target列
			if (VerticalNatTableConfig.isSource(columnIndex, rowIndex)) {
				return ((TransUnitBean) getRowObject(rowIndex)).getSrcContent();
			} else if (VerticalNatTableConfig.isTarget(columnIndex, rowIndex)) {
				return ((TransUnitBean) getRowObject(rowIndex)).getTgtContent();
			}
		}
		return null;
	}

	@Override
	public int getRowCount() {
		return super.getRowCount() * VerticalNatTableConfig.ROW_SPAN;
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		if (columnIndex != VerticalNatTableConfig.SOURCE_COL_INDEX) {
			LOGGER.debug(Messages.getString("dataprovider.VerticalLayerBodyDataProvider.logger1"));
		}
		if (rowIndex % 2 == 0) {
			setSrcValue(rowIndex / VerticalNatTableConfig.ROW_SPAN, newValue);
		} else {
			setTgtValue(rowIndex / VerticalNatTableConfig.ROW_SPAN, newValue);
		}
	}

	@Override
	public T getRowObject(int rowIndex) {
		return super.getRowObject(rowIndex / VerticalNatTableConfig.ROW_SPAN);
	}

	@Override
	public int indexOfRowObject(T rowObject) {
		// TODO 暂时用不到，无实现
//		LOGGER.debug("此方法尚未实现。");
		return 0;
	}

	public DataCell getCellByPosition(int columnPosition, int rowPosition) {
		int rowSpan = 1;
		// TODO 用索引来判断 Position，可能会有问题
		if (columnPosition == VerticalNatTableConfig.ID_COL_INDEX
				|| columnPosition == VerticalNatTableConfig.STATUS_COL_INDEX) {
			rowSpan = VerticalNatTableConfig.ROW_SPAN;
			rowPosition = rowPosition / VerticalNatTableConfig.ROW_SPAN * VerticalNatTableConfig.ROW_SPAN;
		}
		return new DataCell(columnPosition, rowPosition, 1, rowSpan);
	}

}
