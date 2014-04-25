package net.heartsome.cat.ts.ui.innertag;

import java.util.List;

import net.heartsome.cat.common.ui.innertag.InnerTag;

import org.eclipse.jface.text.ITextViewer;

public interface ISegmentViewer extends ITextViewer {

	/**
	 * 得到内部标记
	 * @return ;
	 */
	List<InnerTag> getInnerTagCacheList();

	/**
	 * 得到文本内容
	 * @return ;
	 */
	String getText();

	/**
	 * 得到纯文本内容
	 * @return ;
	 */
	String getPureText();

	/**
	 * 设置源文本
	 * @param source
	 *            源文本;
	 */
	void setSource(String source);

	/**
	 * 设置文本内容
	 * @param text
	 *            原始内容。如果此内容为目标文本，务必在此方法前调用 {@link #setSource(String)} 方法设置源文本，以便正确解析标记索引、标识出错误标记;
	 */
	void setText(String text);

	/**
	 * 得到 Source 的内部标记中最大的索引。
	 * @return Source 的内部标记中最大的索引;
	 */
	int getSourceMaxTagIndex();

	/**
	 * 得到错误标记的起始位置。
	 * @return ;
	 */
	int getErrorTagStart();

	/**
	 * 得到当前显示的所有内部标记控件。
	 * @return ;
	 */
	List<InnerTag> getCurrentInnerTags();

	/**
	 * 插入指定索引的标记
	 * @param tagIndex
	 *            要插入标记的索引
	 * @param offset
	 *            插入位置;
	 */
	void insertInnerTag(int tagIndex, int offset);

}
