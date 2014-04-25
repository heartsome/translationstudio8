package net.heartsome.cat.ts.ui.xliffeditor.nattable.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

/**
 * rowId存取器
 * @author weachy
 * @version
 * @since JDK1.5
 */
public interface IRowIdAccessor {

	/**
	 * 获取指定行的RowId
	 * @param rowPosition
	 *            行的位置
	 * @return RowId;
	 */
	Serializable getRowIdByPosition(int rowPosition);

	/**
	 * 获取指定范围的多行的RowId集合
	 * @param rowPosition
	 *            起始行的位置
	 * @param length
	 *            行个数
	 * @return RowId集合;
	 */
	Set<? extends Serializable> getRowIdsByPositionRange(int rowPosition, int length);

	/**
	 * 得到当前所有显示行的RowId集合
	 * @return 当前所有显示行的RowId集合;
	 */
	ArrayList<? extends Serializable> getRowIds();
}
