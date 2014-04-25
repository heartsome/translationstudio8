package net.heartsome.cat.ts.ui.xliffeditor.nattable.qa;

import net.heartsome.cat.ts.core.file.XLFHandler;

/**
 * 自动品质检查的接口
 * @author robert	2012-05-16
 */
public interface IAutomaticQA {
	/**
	 * 开始自动品质检查
	 * @param isAddToDb
	 * @param rowId
	 * @return
	 */
	public String beginAutoQa(boolean isAddToDb, String rowId, boolean needInitQAResultViewer);
	public void setInitData(XLFHandler handler);
	/**
	 * 激活品质检查视图
	 */
	public void bringQAResultViewerToTop();
	/** 通知本次品质检查已经结束 */
	public void informQAEndFlag();
}
