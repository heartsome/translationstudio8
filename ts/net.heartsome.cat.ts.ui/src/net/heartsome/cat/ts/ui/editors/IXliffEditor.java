package net.heartsome.cat.ts.ui.editors;

import java.io.File;
import java.util.List;

import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.core.file.XLFHandler;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPartSite;

public interface IXliffEditor {

	/**
	 * 改变单个单元格值
	 * @param row
	 *            行
	 * @param columnIndex
	 *            列索引，只可修改 Source 和 Target 列（可使用 {@link #getTgtColumnIndex()} 和 {@link #getSrcColumnIndex()} 得到）。
	 * @param newValue
	 *            新值 ;
	 * @param matchType
	 *            应用匹配时，匹配类型，如果不确可以为null；
	 * @param quality
	 *            应用匹配时，匹配率，如果不确定可以为null；
	 */
	void updateCell(int row, int columnIndex, String newValue, String matchType, String quality)
			throws ExecutionException;

	/**
	 * 改变多个单元格的值
	 * @param rows
	 *            多行
	 * @param columnIndex
	 *            列索引，只可修改 Source 和 Target 列（可使用 {@link #getTgtColumnIndex()} 和 {@link #getSrcColumnIndex()} 得到）。
	 * @param newValue
	 *            新值;
	 */
	void updateCells(int[] rows, int columnIndex, String newValue) throws ExecutionException;

	/**
	 * 得到 Source 所在列索引
	 * @return ;
	 */
	int getSrcColumnIndex();

	/**
	 * 得到 Target 所在列索引
	 * @return ;
	 */
	int getTgtColumnIndex();

	/**
	 * 改变当前布局 ;
	 */
	void changeLayout();

	/**
	 * 改变当前布局
	 * @param isHorizontalLayout
	 *            是否是垂直布局 ;
	 */
	void changeLayout(boolean isHorizontalLayout);

	/**
	 * 设置焦点 ;
	 */
	void setFocus();

	/**
	 * 刷新编辑器 ;
	 */
	void refresh();

	/**
	 * 得到选中的行
	 * @return ;
	 */
	int[] getSelectedRows();

	/**
	 * 得到 XLIFF 文件的处理类
	 * @return ;
	 */
	XLFHandler getXLFHandler();

	/**
	 * 得到当前的IWorkbenchPartSite
	 * @return ;
	 */
	IWorkbenchPartSite getSite();

	/**
	 * 自动调整 NatTable 大小 ;
	 */
	void autoResize();

	void autoResizeNotColumn();

	/**
	 * 重置排序 robert 2011-12-24
	 */
	void resetOrder();

	/**
	 * 根据行的索引跳转到某行
	 * @param position
	 *            ;
	 */
	void jumpToRow(int position, boolean isMultiFiles);

	/**
	 * 将一个字符串插入到光标的位置
	 * @param row
	 * @param columnIndex
	 * @param newValue
	 *            ;
	 */
	void insertCell(int row, int columnIndex, String insertText) throws ExecutionException;

	/**
	 * 标识该编辑器被关闭后是否保存，以备下次打开时重新加载 --robert
	 * @param isStore
	 *            ;
	 */
	void setStore(boolean isStore);

	/**
	 * 获取选中文本的纯文本（不包含标记），如未选中，返回 null
	 * @return ;
	 */
	String getSelectPureText();

	/**
	 * 获取源语言名称
	 * @return ;
	 */
	String getSrcColumnName();

	/**
	 * 获取目标语言名称
	 * @return ;
	 */
	String getTgtColumnName();

	/**
	 * 获取选中的源或目标的文本（标记显示为原始形式）
	 * @param src
	 *            源文本
	 * @param tgt
	 *            目标文本
	 */
	void getSelectSrcOrTgtPureText(StringBuffer src, StringBuffer tgt);

	/**
	 * 得到当前选中的行的唯一标识
	 * @return ;
	 */
	List<String> getSelectedRowIds();

	/**
	 * 是否打开了多文件（合并打开）--robert 2012-06-07
	 * @return ;
	 */
	boolean isMultiFile();

	/**
	 * 针对合并打开的nattable，获取被合并打开的文件--robert 2012-06-07
	 */
	List<File> getMultiFileList();

	/**
	 * 获取指定行号的翻译单元内容
	 * @param rowIndex
	 *            Nattbale中行的索引
	 * @return ;
	 */
	TransUnitBean getRowTransUnitBean(int rowIndex);

	/**
	 * 获取匹配结束后，根据设置应用最大匹配率的匹配或者复制源文到目标
	 * @param rowIndex
	 *            NatTable row Index
	 * @param targetContent
	 *            目标内容;
	 */
	void affterFuzzyMatchApplayTarget(int rowIndex, String targetContent, String matchType, String quality);

	/**
	 * 术语面板加载完成后，让编辑器中的源高亮显示术语, 如果Terms为null，则刷新背景色
	 * @param terms
	 *            ;
	 */
	void highlightedTerms(int rowIndex, List<String> terms);

	/**
	 * 根据是否显示非打印字符刷新 XLIFF EDITOR
	 * @param isShow
	 *            根据当前的设置是否显示非打印字符;
	 */
	void refreshWithNonprinttingCharacter(boolean isShow);

}
