package net.heartsome.cat.ts.ui.xliffeditor.nattable.config;

public class VerticalNatTableConfig {

	/** 行号的列索引 */
	public static int ID_COL_INDEX = 0;

	/** 状态的列索引 */
	public static int STATUS_COL_INDEX = 1;

	/** 源的列索引 */
	public static int SOURCE_COL_INDEX = 2;

	/** 目标的列索引 */
	public static int TARGET_COL_INDEX = 2;

	/** 跨行数 */
	public static int ROW_SPAN = 2;

	/** 在所跨行中，源所占的行 */
	public static int SOURCE_INDEX_PER_SPAN = 0;

	/** 在所跨行中，目标所占的行 */
	public static int TARGET_INDEX_PER_SPAN = 1;

	/**
	 * 是否是“目标”内容
	 * @param columnIndex
	 * @param rowIndex
	 * @return ;
	 */
	public static boolean isSource(int columnIndex, int rowIndex) {
		return columnIndex == SOURCE_COL_INDEX && rowIndex % VerticalNatTableConfig.ROW_SPAN == SOURCE_INDEX_PER_SPAN;
	}

	/**
	 * 是否是“目标”内容
	 * @param columnIndex
	 * @param rowIndex
	 * @return ;
	 */
	public static boolean isTarget(int columnIndex, int rowIndex) {
		return columnIndex == TARGET_COL_INDEX && rowIndex % VerticalNatTableConfig.ROW_SPAN == TARGET_INDEX_PER_SPAN;
	}

	/**
	 * 得到实际的行索引（由于垂直布局下一个翻译单元跨N行，NatTable的行索引也被放大了N倍，通过此方法可以得到相当于水平布局下的行索引）
	 * @param rowIndex
	 *            NatTable中的行索引
	 * @return ;
	 */
	public static int getRealRowIndex(int rowIndex) {
		return rowIndex / ROW_SPAN;
	}

	/**
	 * 得到 Source 所在行
	 * @param realRowIndex
	 * @return ;
	 */
	public static int getSourceRowIndex(int realRowIndex) {
		return realRowIndex * ROW_SPAN;
	}

	/**
	 * 得到 Target 所在行
	 * @param realRowIndex
	 * @return ;
	 */
	public static int getTargetRowIndex(int realRowIndex) {
		return realRowIndex * ROW_SPAN;
	}
}
