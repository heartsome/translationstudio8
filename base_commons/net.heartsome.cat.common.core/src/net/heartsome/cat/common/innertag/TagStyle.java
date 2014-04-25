package net.heartsome.cat.common.innertag;

import net.heartsome.cat.common.core.CoreActivator;

import org.eclipse.jface.preference.IPreferenceStore;

public enum TagStyle {

	/**
	 * 索引标记样式。仅显示标记索引。
	 */
	INDEX,

	/**
	 * 简单标记样式。显示标记名称以及索引。
	 */
	SIMPLE,

	/**
	 * 完整标记样式。显示标记全部内容以及索引。
	 */
	FULL;

	public static TagStyle curStyle;

	/**
	 * 取得下一个标记样式
	 * @return ;
	 */
	public TagStyle getNextStyle() {
		switch (this) {
		case INDEX: {
			curStyle = SIMPLE;
			return SIMPLE;
		}
		case SIMPLE: {
			curStyle = FULL;
			return FULL;
		}
		case FULL: {
			curStyle = INDEX;
			return INDEX;
		}
		default: {
			curStyle = SIMPLE;
			return SIMPLE;
		}
		}
	}

	/**
	 * 得到默认的标记样式
	 * @return ;
	 */
	public static TagStyle getDefault() {
		IPreferenceStore store = CoreActivator.getDefault().getPreferenceStore();
		int styleIndex = store.getInt("net.heartsome.cat.common.innertag.TagStyle.defaultStyle");
		if (styleIndex == 0) {
			curStyle = INDEX;
		} else if (styleIndex == 1) {
			curStyle = SIMPLE;
		} else if (styleIndex == 2) {
			curStyle = FULL;
		}
		return curStyle;
	}
	
	/**
	 * 得到默认的标记样式
	 * @param isInitCurStyle
	 * 				是否初始化 curStyle 变量(只有 XLIFF 编辑器中的 curStyle 才需要初始化，其他的视图不需要，如记忆库匹配视图)
	 * @return ;
	 */
	public static TagStyle getDefault(boolean isInitCurStyle) {
		if (isInitCurStyle) {
			return getDefault();
		} else {
			return INDEX;
		}
	}

	public static void setTagStyle(TagStyle style) {
		curStyle = style;
		IPreferenceStore store = CoreActivator.getDefault().getPreferenceStore();
		int styleIndex = -1;
		switch (style) {
			case INDEX: {
				styleIndex = 0;
				break;
			}
			case SIMPLE: {
				styleIndex = 1;
				break;
			}
			case FULL: {
				styleIndex = 2;
				break;
			}
			default: {
				styleIndex = 1;
				break;
			}
		}
		store.setValue("net.heartsome.cat.common.innertag.TagStyle.defaultStyle", styleIndex);
	}
}
