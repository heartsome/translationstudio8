package net.heartsome.cat.common.innertag.factory;

import java.util.List;

import net.heartsome.cat.common.innertag.InnerTagBean;

public interface IPlaceHolderBuilder {

	/**
	 * 得到内部标记的占位符。
	 * @param innerTagBeans
	 *            内部标记集合
	 * @param index
	 *            当前标记所在索引
	 * @return 内部标记占位符;
	 */
	String getPlaceHolder(List<InnerTagBean> innerTagBeans, int index);

	/**
	 * 根据内部标记占位符得到该内部标记在标记集合中的索引
	 * @param innerTagBeans
	 *            内部标记集合
	 * @param placeHolder
	 *            内部标记占位符
	 * @return 索引;
	 */
	int getIndex(List<InnerTagBean> innerTagBeans, String placeHolder);
}
