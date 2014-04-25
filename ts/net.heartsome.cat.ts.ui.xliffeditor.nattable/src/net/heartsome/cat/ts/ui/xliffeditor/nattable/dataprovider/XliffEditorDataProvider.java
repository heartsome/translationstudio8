package net.heartsome.cat.ts.ui.xliffeditor.nattable.dataprovider;

import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.UpdateDataBean;
import net.sourceforge.nattable.data.IColumnAccessor;
import net.sourceforge.nattable.data.IRowDataProvider;

public class XliffEditorDataProvider<T> implements IRowDataProvider<T> {
	private XLFHandler handler;

	private static final int CACHE_SIZE = 200;

	public XLFHandler getHandler() {
		return handler;
	}

	private IColumnAccessor<T> columnAccessor;

	public XliffEditorDataProvider(XLFHandler handler, IColumnAccessor<T> columnAccessor) {
		this.handler = handler;
		this.columnAccessor = columnAccessor;
	}

	@SuppressWarnings("unchecked")
	public T getRowObject(final int rowIndex) {
		T obj = (T) handler.getCacheMap().get(rowIndex);
		if (obj == null) {
			obj = (T) handler.getTransUnit(rowIndex);
			cache(rowIndex, obj); // 缓存操作
		}
		return obj;
	}

	/**
	 * 缓存当前页
	 * @param key
	 * @param value
	 *            ;
	 */
	private void cache(final int key, T value) {
		if (handler.getCacheMap().size() > CACHE_SIZE) {
			// 清空缓存
			handler.resetCache();
		} else {
			handler.getCacheMap().put(key, (TransUnitBean) value);
		}
	}

	public int indexOfRowObject(T rowObject) {
		// TODO 暂时用不到，无实现
		return 0;
	}

	public int getColumnCount() {
		return columnAccessor.getColumnCount();
	}

	public Object getDataValue(int columnIndex, int rowIndex) {
		if (columnIndex == 0) {
			return rowIndex + 1;
		} else if (columnIndex == 1) { // 获取Source列的值
//			T obj = (T) handler.getCacheMap().get(rowIndex);
//			if (obj != null) {
//				return ((TransUnitBean) obj).getSrcContent();
//			} else {
//				String rowId = handler.getRowId(rowIndex);
//				return handler.getSrcContent(rowId);
//			}
			T obj = getRowObject(rowIndex);				
			if (obj != null) {
				return ((TransUnitBean) obj).getSrcContent();
			} else {
				return null;
			}
		} else if (columnIndex == 3) { // 获取Target列的值
//			T obj = (T) handler.getCacheMap().get(rowIndex);
//			if (obj != null) {
//				return ((TransUnitBean) obj).getTgtContent();
//			} else {
//				String rowId = handler.getRowId(rowIndex);
//				return handler.getTgtContent(rowId);
//			}
			T obj = getRowObject(rowIndex);				
			if (obj != null) {
				return ((TransUnitBean) obj).getTgtContent();
			} else {
				return null;
			}
		} else {
			return columnAccessor.getDataValue(getRowObject(rowIndex), columnIndex);
		}
	}

	public int getRowCount() {
		return handler.countEditableTransUnit();
	}

	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		if (columnIndex == 1) { // 修改Source列
			setSrcValue(rowIndex, newValue);
		}
		if (columnIndex == 3) { // 修改Target列
			setTgtValue(rowIndex, newValue);
		}
	}

	protected void setSrcValue(int rowIndex, Object newValue) {
		handler.changeSrcTextValue(handler.getRowId(rowIndex), ((UpdateDataBean) newValue).getText());
	}

	protected void setTgtValue(int rowIndex, Object newValue) {
		UpdateDataBean bean = (UpdateDataBean) newValue;
		handler.changeTgtTextValue(handler.getRowId(rowIndex), bean.getText(), bean.getMatchType(), bean.getQuality());
	}

}
