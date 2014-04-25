package net.heartsome.cat.common.innertag.factory;

import java.util.List;
import java.util.regex.Pattern;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.util.UnicodeConverter;

public class PlaceHolderEditModeBuilder implements IPlaceHolderBuilder {

	/** Unicode 前缀 */
	private static final String UNICODE_PREFIX = "\\ua0";

	/** 最小范围 */
	public static final char MIN = '\ua000';

	/** 最大范围 */
	public static final char MAX = '\ua099';

	/** 内部标记正则表达式 */
	public static final Pattern PATTERN = Pattern.compile("[" + MIN + "-" + MAX + "]");

	/**
	 * 由占位符得到索引
	 * @param innerTagBeans
	 *            此参数可忽略（null）。
	 * @param placeHolder
	 *            占位符
	 * @return 索引;
	 */
	public int getIndex(List<InnerTagBean> innerTagBeans, String placeHolder) {
		String text = UnicodeConverter.convert(placeHolder);
		if (text.length() == 6) {
			try {
				return Integer.parseInt(text.substring(4, 6), 16);
			} catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * 根据索引得到占位符
	 * @param innerTagBeans
	 *            此参数可忽略（null）。
	 * @param index
	 *            索引
	 * @return 占位符;
	 */
	public String getPlaceHolder(List<InnerTagBean> innerTagBeans, int index) {
		String hexString = Integer.toHexString(index);
		if (hexString.length() == 1) {
			hexString = "0" + hexString;
		}
		String placeHolderString = UNICODE_PREFIX + hexString;
		return UnicodeConverter.revert(placeHolderString);
	}
}
