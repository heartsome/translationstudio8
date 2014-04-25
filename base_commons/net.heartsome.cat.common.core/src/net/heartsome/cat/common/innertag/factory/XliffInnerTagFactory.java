package net.heartsome.cat.common.innertag.factory;

import static net.heartsome.cat.common.innertag.TagType.END;
import static net.heartsome.cat.common.innertag.TagType.STANDALONE;
import static net.heartsome.cat.common.innertag.TagType.START;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.innertag.TagType;

public class XliffInnerTagFactory implements IInnerTagFactory {
	private static final List<String> standaloneTags = Arrays.asList("x", "bx", "ex", "ph");

	private static final List<String> normalTags = Arrays.asList("g", "bpt", "ept", "ph", "it", "mrk", "sub");

	private ArrayList<InnerTagBean> beans = new ArrayList<InnerTagBean>();

	private String text;

	private IPlaceHolderBuilder placeHolderCreater;

	private Stack<Integer> indexStack = new Stack<Integer>(); // 索引集合

	private int start = -1;

	private int maxIndex = 0;

	private boolean HasStartTag = false;

	/**
	 * 内部标记工厂默认实现
	 * @param xml
	 *            XML文本
	 */
	public XliffInnerTagFactory(String xml) {
		this(xml, new DefaultPlaceHolderBuilder());
	}

	/**
	 * 内部标记工厂默认实现
	 * @param placeHolderCreater
	 *            占位符创建器
	 */
	public XliffInnerTagFactory(IPlaceHolderBuilder placeHolderCreater) {
		this(null, placeHolderCreater);
	}

	/**
	 * 内部标记工厂默认实现
	 * @param xml
	 *            XML文本
	 * @param placeHolderCreater
	 *            占位符创建器
	 */
	public XliffInnerTagFactory(String xml, IPlaceHolderBuilder placeHolderCreater) {
		this.placeHolderCreater = placeHolderCreater;
		this.text = parseInnerTag(xml);
	}

	public void reset() {
		beans.clear();
		text = null;
		indexStack.clear(); // 索引集合

		start = -1;
		maxIndex = 0;
		HasStartTag = false;
		targetFlg = false;
	}

	public String getText() {
		return text;
	}

	public List<InnerTagBean> getInnerTagBeans() {
		ArrayList<InnerTagBean> innerTagBeans = new ArrayList<InnerTagBean>();
		innerTagBeans.addAll(beans);
		return innerTagBeans;
	}
	
	private boolean targetFlg = false;
	public String parseInnerTag(String xml, boolean isEditMode) {

		if (xml == null || xml.length() == 0) {
			return "";
		}
		if (!indexStack.empty()) {
			indexStack.clear();
		}

		StringBuffer sbOriginalValue = new StringBuffer(xml);
		
		int beanSize = beans.size();
		this.start = -1; // 起始索引
		if (beans.size() > 0) {
			targetFlg = true;
			int index = -1;
			for (int i = 0; i < beans.size(); i++) {
				InnerTagBean bean = beans.get(i);
				if (bean.getType() != TagType.STANDALONE) {
					continue;
				}
				String content = bean.getContent();
				index = sbOriginalValue.indexOf(content);
				if (index > -1 && (isEditMode || TagStyle.curStyle != TagStyle.FULL)) {
					String placeHolder = placeHolderCreater.getPlaceHolder(beans, i);
					if(sbOriginalValue.indexOf(placeHolder) != -1){
						// Bug #3044 不能正确显示插入的标记
						// 已经存在，说明这个标记在译文中是重复的。
						continue;
					}
					sbOriginalValue.replace(index, index + content.length(), placeHolder);
				} else if (TagStyle.curStyle == TagStyle.FULL) {
					this.start = index + content.length();
				}
			}
			beanSize = beans.size();
		} else {
			beanSize = -1;
		}

		while ((start = sbOriginalValue.indexOf("<", start + 1)) > -1) {
			int end = sbOriginalValue.indexOf(">", start + 1);
			if (end > -1) {
				String xmlTag = sbOriginalValue.substring(start, end + 1); // 提取出的内部标记xml形式的文本
				String tagName = getTagName(xmlTag);

				sbOriginalValue.replace(start, end + 1, xmlTag);
				if (xmlTag.indexOf("/>", 1) > -1) { // 独立标签
					if (standaloneTags.contains(tagName) || normalTags.contains(tagName)) {
						if ("bx".equals(tagName)) {
							addInnerTagBean(START, sbOriginalValue, xmlTag, tagName);
						} else if ("ex".equals(tagName)) {
							addInnerTagBean(END, sbOriginalValue, xmlTag, tagName);
						} else {
							addInnerTagBean(STANDALONE, sbOriginalValue, xmlTag, tagName);
						}
					}
				} else if (xmlTag.indexOf("</") > -1) { // 结束标签
					if (normalTags.contains(tagName)) {
						addInnerTagBean(END, sbOriginalValue, xmlTag, tagName);
					}
				} else if (xmlTag.indexOf(">") > -1) { // 开始标签
					if (normalTags.contains(tagName)) {
						if ("bpt".equals(tagName)) {
							int endIndex = sbOriginalValue.indexOf("</bpt>", start) + "</bpt>".length();
							xmlTag = sbOriginalValue.substring(start, endIndex);
							sbOriginalValue.replace(start, endIndex, xmlTag);
							addInnerTagBean(START, sbOriginalValue, xmlTag, tagName);
						} else if ("ept".equals(tagName)) {
							int endIndex = sbOriginalValue.indexOf("</ept>", start) + "</ept>".length();
							xmlTag = sbOriginalValue.substring(start, endIndex);
							sbOriginalValue.replace(start, endIndex, xmlTag);
							addInnerTagBean(END, sbOriginalValue, xmlTag, tagName);
						} else if ("ph".equals(tagName) || "it".equals(tagName)) {
							String tempTagName = "</" + tagName + ">";
							int endIndex = sbOriginalValue.indexOf(tempTagName, start) + tempTagName.length();
							xmlTag = sbOriginalValue.substring(start, endIndex);
							sbOriginalValue.replace(start, endIndex, xmlTag);
							addInnerTagBean(STANDALONE, sbOriginalValue, xmlTag, tagName);
						} else {
							addInnerTagBean(START, sbOriginalValue, xmlTag, tagName);
						}
					}
				}
			}
		}

		if (beanSize > 0) { // 设置为错误标记
			for (int i = beanSize; i < beans.size(); i++) {
				beans.get(i).setWrongTag(true);
			}
		}
		targetFlg = false;
		return sbOriginalValue.toString();

	}

	/**
	 * 将带内部标记的文本由XML格式转换为显示格式的文本
	 * @param xml
	 *            原始的带内部标记的XML格式的文本
	 * @return ;
	 */
	public String parseInnerTag(String xml) {
		return parseInnerTag(xml, false);
	}

	boolean exist = false;

	/**
	 * @param tagType
	 * @param text
	 * @param tagContent
	 * @param tagName
	 *            ;
	 */
	private void addInnerTagBean(TagType tagType, StringBuffer text, String tagContent, String tagName) {
		/* 在文本中插入索引 */
		int index = -1;
		if (tagType == START) {
			HasStartTag = true;
			if(targetFlg){
			for (int i = 0; i < beans.size(); ++i) {
				InnerTagBean bean = beans.get(i);
				if (bean.getType() != TagType.START) {
					continue;
				}
				if (bean.getContent().equals(tagContent)) {
					String placeHolder = placeHolderCreater.getPlaceHolder(beans, i);
					if(text.indexOf(placeHolder) != -1){
						// 已经存在，继续寻找。
						continue;
					}
					text.replace(start, start + tagContent.length(), placeHolder);
					indexStack.push(bean.getIndex());
					exist = true;
					return;
				}
			}
			}
			maxIndex++;
			indexStack.push(maxIndex);
			index = maxIndex;
		} else if (tagType == END) {
			if (!HasStartTag) {
				maxIndex++;
				indexStack.push(maxIndex);
			}
			HasStartTag = false;
			if (!indexStack.empty()) {
				index = indexStack.pop();
				if (exist && targetFlg) {
					for (int i = 0; i < beans.size(); ++i) {
						InnerTagBean bean = beans.get(i);
						if (bean.getIndex() == index && bean.getType() == TagType.END) {
							String placeHolder = placeHolderCreater.getPlaceHolder(beans, i);
							text.replace(start, start + tagContent.length(), placeHolder);
							if (!indexStack.isEmpty()) {
								HasStartTag = true;
							}
							return;
						}
					}
				}
			}
			if (!indexStack.isEmpty()) {
				HasStartTag = true;
			}
		} else if (tagType == STANDALONE) {
			maxIndex++;
			index = maxIndex;
		}

		if (index > -1) {
			InnerTagBean bean = new InnerTagBean(index, tagName, tagContent, tagType);
			beans.add(bean);

			String placeHolder = placeHolderCreater.getPlaceHolder(beans, beans.size() - 1);
			text.replace(start, start + tagContent.length(), placeHolder);
			// 显示完整标记时，start 计算错误，因此添加下行语句
			start += placeHolder.length() - 1;
		}
	}

	/**
	 * 得到标记的名称
	 * @param xmlTag
	 *            XML格式的标记
	 * @return 标记名称;
	 */
	private String getTagName(String xmlTag) {
		if (xmlTag.indexOf("</") > -1) { // 结束标记
			return xmlTag.substring(2, xmlTag.length() - 1);
		}
		int end = xmlTag.indexOf("/>", 1); // 独立标记
		if (end == -1) {
			end = xmlTag.length() - 1; // 开始标记
		}
		int tempIndex = xmlTag.indexOf(" ", 1);
		if (tempIndex > -1 && tempIndex < end) {
			end = tempIndex;
		}
		return xmlTag.substring(1, end);
	}
}
