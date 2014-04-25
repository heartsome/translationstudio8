package net.heartsome.cat.common.ui.innertag;

import static net.heartsome.cat.common.ui.utils.InnerTagUtil.INVISIBLE_CHAR;

import java.util.List;
import java.util.regex.Pattern;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.innertag.TagType;
import net.heartsome.cat.common.innertag.factory.IPlaceHolderBuilder;

import org.eclipse.core.runtime.Assert;

public class PlaceHolderNormalModeBuilder implements IPlaceHolderBuilder {

	private static String BLANK_CHARACTER = "\u2006";

	public static Pattern PATTERN = Pattern.compile(BLANK_CHARACTER + "(" + INVISIBLE_CHAR + "\\d+)?" + INVISIBLE_CHAR
			+ "?((x|bx|ex|ph|g|bpt|ept|ph|it|mrk|sub)"+
				"|(<(x|bx|ex|ph|bpt|ept|ph|it|mrk|sub)\\s*(\\w*\\s*=\\s*('|\")(.|\n)*('|\"))*>?(.|\n)*<?/(x|bx|ex|ph|bpt|ept|ph|it|mrk|sub)?>)"+
				"|(<g(\\s*\\w*\\s*=\\s*('|\")(.|\n)*('|\"))*>)|</g>)?"
			+ INVISIBLE_CHAR + "?(\\d+" + INVISIBLE_CHAR + ")?" + BLANK_CHARACTER+"?");

	private TagStyle style;
	
	/**
	 * 得到内部标记的占位符。
	 * @param innerTagBeans
	 *            内部标记集合（不能为null）
	 * @param index
	 *            当前标记所在索引
	 * @return 内部标记占位符;
	 */
	public String getPlaceHolder(List<InnerTagBean> innerTagBeans, int index) {
		Assert.isNotNull(innerTagBeans);

		InnerTagBean innerTagBean = innerTagBeans.get(index);
		int tagIndex = innerTagBean.getIndex();
		StringBuffer tagContent = new StringBuffer();
		StringBuffer innerTag = new StringBuffer();
		if (innerTagBean.getType() == TagType.START) {
			StringBuffer id = createStyledTagStartIndex(tagIndex);
			
			if (style == TagStyle.SIMPLE || style == TagStyle.INDEX) {
				innerTag.append(id);
				if (style == TagStyle.SIMPLE) {
					tagContent.append(INVISIBLE_CHAR).append(innerTagBean.getName()).append(INVISIBLE_CHAR);
					innerTag.append(tagContent); // 形如“|_1| g_|” （“_”代指不可见字符）
				}
			} else if (style == TagStyle.FULL) {
				tagContent.append(INVISIBLE_CHAR).append(innerTagBean.getContent()).append(INVISIBLE_CHAR);
				innerTag.append(id).append(tagContent);// 形如“|_1| g_|” （“_”代指不可见字符）
			}
		} else if (innerTagBean.getType() == TagType.END) {
			StringBuffer id = createStyledTagEndIndex(tagIndex);
			if (style == TagStyle.SIMPLE || style == TagStyle.INDEX) {
				if (style == TagStyle.SIMPLE) {
					tagContent.append(INVISIBLE_CHAR).append(innerTagBean.getName()).append(INVISIBLE_CHAR);
					innerTag.append(tagContent); // 形如“|_1| g_|” （“_”代指不可见字符）
				}
				innerTag.append(id);
			} else if (style == TagStyle.FULL) {
				tagContent.append(INVISIBLE_CHAR).append(innerTagBean.getContent()).append(INVISIBLE_CHAR);
				innerTag.append(tagContent).append(id);
			}
		} else if (innerTagBean.getType() == TagType.STANDALONE) {
			StringBuffer id = createStyledTagStartIndex(tagIndex);
			StringBuffer endId = createStyledTagEndIndex(tagIndex);
			if (style == TagStyle.SIMPLE || style == TagStyle.INDEX) {
				innerTag.append(id);
				if (style == TagStyle.SIMPLE) {
					tagContent.append(INVISIBLE_CHAR).append(innerTagBean.getName()).append(INVISIBLE_CHAR);
					innerTag.append(tagContent).append(endId);// 形如“|_1| ph |1_|”;（“_”代指不可见字符）
				}
			} else if (style == TagStyle.FULL) {
				tagContent.append(INVISIBLE_CHAR).append(innerTagBean.getContent()).append(INVISIBLE_CHAR);
				innerTag.append(id).append(tagContent).append(endId);
			}
		}
		return innerTag.insert(0, BLANK_CHARACTER).append(BLANK_CHARACTER).toString();
	}

	/**
	 * 此方法暂时无使用价值，无实现。
	 * @see net.heartsome.cat.common.innertag.factory.IPlaceHolderBuilder#getIndex(java.lang.String)
	 */
	public int getIndex(List<InnerTagBean> innerTagBeans, String placeHolder) {
		// TODO 显示模式下此方法暂时用不到，不做实现
		return -1;
	}

	/**
	 * 得到开始部分的标记索引
	 * @param index
	 *            标记索引号
	 * @return ;
	 */
	public static StringBuffer createStyledTagStartIndex(int index) {
		return new StringBuffer().append(INVISIBLE_CHAR).append(index);
	}

	/**
	 * 得到结尾部分的标记索引
	 * @param index
	 *            标记索引号
	 * @return ;
	 */
	public static StringBuffer createStyledTagEndIndex(int index) {
		return new StringBuffer().append(index).append(INVISIBLE_CHAR);
	}
	
	public void setStyle(TagStyle style) {
		this.style = style;
	}
}
