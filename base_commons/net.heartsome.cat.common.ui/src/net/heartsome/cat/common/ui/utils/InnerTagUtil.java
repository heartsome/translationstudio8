package net.heartsome.cat.common.ui.utils;

import static net.heartsome.cat.common.ui.innertag.PlaceHolderNormalModeBuilder.PATTERN;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.innertag.TagType;
import net.heartsome.cat.common.innertag.factory.IInnerTagFactory;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.common.innertag.factory.XliffInnerTagFactory;
import net.heartsome.cat.common.ui.innertag.PlaceHolderNormalModeBuilder;

import org.eclipse.jface.text.Position;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

/**
 * 内部标记工具类，本类只操作文本，标记着色的部分请参看 {@link innertagC}
 * <p>
 * 此类中有几个概念需要注意：
 * <li>XmlTag: XML 格式的标记</li>
 * <li>StyledTag: 显示在 UI 上的标记样式</li>
 * <li>XmlValue: 源语言或目标语言的 XML 格式的值（XLIFF 文件中的值）</li>
 * <li>DisplayValue: 源语言或目标语言的添加了标记样式的显示文本（UI 上的显示值）</li>
 * </p>
 * @author weachy
 * @since JDK1.5
 */
public class InnerTagUtil {

	private static PlaceHolderNormalModeBuilder placeHolderCreater = new PlaceHolderNormalModeBuilder();;

	private InnerTagUtil() {
		// Color borderColor = new Color(Display.getCurrent(), 0, 255, 255);
		// Color textBgColor = new Color(Display.getCurrent(), 0, 205, 205);
		// Color inxBgColor = new Color(Display.getCurrent(), 0, 139, 139);
		// Color textFgColor = new Color(Display.getCurrent(), 0, 104, 139);
		// Color inxFgColor = borderColor;

		// Font font = new Font(Display.getDefault(), "宋体", 10, SWT.NORMAL);
	}

	/** 标记字体 */
	public static Font tagFont = getTagFont();

	private static Font getTagFont() {
		return new Font(Display.getDefault(), "Arial", 8, SWT.BOLD);
	}

	/**
	 * 将带内部标记的文本由XML格式转换为显示格式的文本
	 * @param originalValue
	 *            原始的带内部标记的XML格式的文本
	 * @return ;
	 */
	public static TreeMap<String, InnerTagBean> parseXmlToDisplayValue(StringBuffer originalValue, TagStyle style) {
		// 得到标签映射map（key: 内部标记；value: 内部标记实体）
		TreeMap<String, InnerTagBean> tags = new TreeMap<String, InnerTagBean>(new Comparator<String>() {
			public int compare(String str1, String str2) {
				int num1 = InnerTagUtil.getStyledTagNum(str1);
				int num2 = InnerTagUtil.getStyledTagNum(str2);
				if (num1 == num2) {
					return str1.indexOf(String.valueOf(num1)) - str2.indexOf(String.valueOf(num1));
				}
				return num1 - num2;
			}
		});
		if (originalValue == null || originalValue.length() == 0) {
			return tags;
		}

		placeHolderCreater.setStyle(style);

		IInnerTagFactory innerTagFactory = new XliffInnerTagFactory(originalValue.toString(), placeHolderCreater);
		originalValue.replace(0, originalValue.length(), innerTagFactory.getText()); // 提取标记之后的文本。

		List<InnerTagBean> innerTagBeans = innerTagFactory.getInnerTagBeans();

		if (innerTagBeans != null && innerTagBeans.size() > 0) {
			for (int i = 0; i < innerTagBeans.size(); i++) {
				String placeHolder = placeHolderCreater.getPlaceHolder(innerTagBeans, i);
				tags.put(placeHolder, innerTagBeans.get(i));
				// originalValue.replace(innerTagBean.getOffset(), innerTagBean.getOffset(), innerTag.toString());
			}
		}

		return tags;
	}

	public static final char INVISIBLE_CHAR = '\u200A'; // 不可见字符（此字符为：不换行空格）

	/**
	 * 根据 source 的内容显示内部标记
	 * @param originalValue
	 *            原始值
	 * @param srcOriginalValue
	 *            Source 值
	 * @return ;
	 */
	public static Map<String, InnerTagBean> parseXmlToDisplayValueFromSource(String source, StringBuffer originalValue,
			TagStyle style) {
		// 得到标签映射map（key: 内部标记；value: 内部标记实体）
		TreeMap<String, InnerTagBean> tags = new TreeMap<String, InnerTagBean>(new Comparator<String>() {
			public int compare(String str1, String str2) {
				int num1 = InnerTagUtil.getStyledTagNum(str1);
				int num2 = InnerTagUtil.getStyledTagNum(str2);
				if (num1 == num2) {
					return str1.indexOf(String.valueOf(num1)) - str2.indexOf(String.valueOf(num1));
				}
				return num1 - num2;
			}
		});
		if (originalValue == null || originalValue.length() == 0) {
			return tags;
		}

		placeHolderCreater.setStyle(style);

		IInnerTagFactory innerTagFactory = new XliffInnerTagFactory(source, placeHolderCreater);
		List<InnerTagBean> sourceInnerTagBeans = innerTagFactory.getInnerTagBeans();

		if (sourceInnerTagBeans != null && sourceInnerTagBeans.size() > 0) {
			int index = -1;
			for (int i = 0; i < sourceInnerTagBeans.size(); i++) {
				InnerTagBean innerTagBean = sourceInnerTagBeans.get(i);
				String placeHolder = placeHolderCreater.getPlaceHolder(sourceInnerTagBeans, i);
				tags.put(placeHolder, innerTagBean);

				// String xml1 = FindReplaceDocumentAdapter.escapeForRegExPattern(entry.getValue());
				String xml = innerTagBean.getContent();
				if ((index = originalValue.indexOf(xml, index)) != -1) { // 替换 Source 中存在的标记
					originalValue.replace(index, index + xml.length(), placeHolder);
					index += placeHolder.length();
				}
			}
		}

		String target = innerTagFactory.parseInnerTag(originalValue.toString()); // 替换目标文本中的错误标记
		originalValue.replace(0, originalValue.length(), target);

		return tags;
	}

	/**
	 * 将InnerTag转换回XML格式的标记
	 * @param text
	 * @return ;
	 */
	public static String parseDisplayToXmlValue(Map<String, InnerTagBean> tags, String text) {
		StringBuffer sb = new StringBuffer(text);
		for (Entry<String, InnerTagBean> entry : tags.entrySet()) {
			String innerTag = entry.getKey();
			String xmlTag = entry.getValue().getContent();
			int index = -1;
			while ((index = sb.indexOf(innerTag, index + 1)) > -1) {
				sb.replace(index, index + innerTag.length(), xmlTag);
			}
		}
		return sb.toString();
	}

	/**
	 * 转义“&lt;”、“&gt;”为“&amp;lt;”、“&amp;gt;”
	 * @param source
	 *            源文本
	 * @return 转义后的文本;
	 */
	public static String escapeTag(String source) {
		return source.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	/**
	 * 转义“&amp;lt;”、“&amp;gt;”为“&lt;”、“&gt;”
	 * @param source
	 *            源文本
	 * @return 转义后的文本;
	 */
	public static String resolveTag(String source) {
		return source.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
	}

	/**
	 * 得到内部标记的类型。
	 * @param innerTag
	 *            内部标记
	 * @return ;
	 */
	public static TagType getStyledTagType(String innerTag) {
		int num = getStyledTagNum(innerTag);
		String tagStartIndex = PlaceHolderNormalModeBuilder.createStyledTagStartIndex(num).toString();
		String tagEndIndex = PlaceHolderNormalModeBuilder.createStyledTagEndIndex(num).toString();
		if (innerTag.startsWith(tagStartIndex) && innerTag.endsWith(tagEndIndex)) {
			return TagType.STANDALONE;
		} else if (innerTag.startsWith(tagStartIndex)) {
			return TagType.START;
		} else if (innerTag.endsWith(tagEndIndex)) {
			return TagType.END;
		}
		return null;
	}

	/**
	 * 得到内部标记索引号。
	 * @param innerTag
	 *            内部标记
	 * @return ;
	 */
	public static int getStyledTagNum(String innerTag) {
		int res = 0;
		for (int i = 0; i < innerTag.length(); i++) {
			char ch = innerTag.charAt(i);
			if (Character.isDigit(ch)) {
				res = res * 10 + Integer.parseInt(String.valueOf(ch));
			} else {
				if (res > 0) {
					return res;
				}
			}
		}
		return -1;
	}

	/**
	 * 获取忽略标记的值
	 * @param displayValue
	 *            带标记样式的文本。
	 * @return ;
	 */
	public static String getDisplayValueWithoutTags(String displayValue) {
		StringBuffer t = new StringBuffer(displayValue);
		Position[] tagRanges = getStyledTagRanges(displayValue);
		for (int i = tagRanges.length - 1; i >= 0; i--) {
			Position tagRange = tagRanges[i];
			t.delete(tagRange.getOffset(), tagRange.getOffset() + tagRange.getLength());
		}
		return t.toString();
	}

	// private static Pattern PATTERN = Pattern.compile("(" + INVISIBLE_CHAR + "\\d+)?" + INVISIBLE_CHAR
	// + "(x|bx|ex|g|bpt|ept|mrk|sub|ph|it)" + INVISIBLE_CHAR + "(\\d+" + INVISIBLE_CHAR + ")?");

	/**
	 * 得到一段带标记样式的文本中，指定位置上的标记的索引范围。
	 * @param tagStyledText
	 *            带标记样式的文本。
	 * @param offset
	 * @return 若指定位置上不存在标记，则返回 null;
	 */
	public static Position getStyledTagRange(String tagStyledText, int offset) {
		Matcher m = PATTERN.matcher(tagStyledText);
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			if (start < offset && offset < end) {
				return new Position(start, end - start);
			}
		}
		return null;
	}

	/**
	 * 得到一段带标记样式的文本中所有标记的索引范围。
	 * @param tagStyledText
	 *            带标记样式的文本。
	 * @return ;
	 */
	public static Position[] getStyledTagRanges(String tagStyledText) {
		Matcher m =  PlaceHolderEditModeBuilder.PATTERN.matcher(tagStyledText);
		ArrayList<Position> positions = new ArrayList<Position>();
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			positions.add(new Position(start, end - start));
		}
		return positions.toArray(new Position[] {});
	}

	/**
	 * 得到一段带标记样式的文本中所有的标记。
	 * @param tagStyledText
	 *            带标记样式的文本。
	 * @return ;
	 */
	public static String[] getStyledTags(String tagStyledText) {
		Matcher m =  PlaceHolderEditModeBuilder.PATTERN.matcher(tagStyledText);
		ArrayList<String> tags = new ArrayList<String>();
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			tags.add(tagStyledText.substring(start, end));
		}
		return tags.toArray(new String[] {});
	}
}
