/**
 * IntelligentTagPrcessor.java
 *
 * Version information :
 *
 * Date:2013-9-4
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.util;

import static net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder.PATTERN;

import java.util.List;
import java.util.regex.Matcher;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagType;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.common.innertag.factory.XliffInnerTagFactory;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class IntelligentTagPrcessor {

	public static String intelligentAppendTag(String srcFullText, String targetFullText) {
		PlaceHolderEditModeBuilder plb = new PlaceHolderEditModeBuilder();
		XliffInnerTagFactory innerTagFactory = new XliffInnerTagFactory(plb);

		String targetParsedText = innerTagFactory.parseInnerTag(targetFullText);
		List<InnerTagBean> targetTags = innerTagFactory.getInnerTagBeans();
		String targetPureText = PATTERN.matcher(targetParsedText).replaceAll("");
		innerTagFactory.reset();
		innerTagFactory.parseInnerTag(srcFullText);
		List<InnerTagBean> srcTags = innerTagFactory.getInnerTagBeans();
		innerTagFactory.reset();

		if (targetTags.size() == srcTags.size()) {
			Matcher matcher = PlaceHolderEditModeBuilder.PATTERN.matcher(targetParsedText);
			int offset = 0;
			StringBuffer sb = new StringBuffer(targetParsedText);
			while (matcher.find()) {
				String placeHolder = matcher.group();
				int index = plb.getIndex(null, placeHolder);
				if (index > -1 && index < srcTags.size()) {
					InnerTagBean b = srcTags.get(index);
					String tagContent = b.getContent();
					int start = matcher.start() + offset;
					int end = matcher.end() + offset;
					sb.replace(start, end, tagContent);
					offset += tagContent.length() - 1;
				} else {
					sb.delete(0, sb.length());
					sb.append(targetFullText);
					break;
				}
			}
			targetPureText = sb.toString();
		} else if (srcTags.size() > 0) {
			if (srcTags.size() == 1) { // 只有一个标记
				InnerTagBean tagFirst = srcTags.get(0);
				String tagContent = tagFirst.getContent();
				if (srcFullText.indexOf(tagContent) == 0) {// header
					targetPureText = tagContent + targetPureText;
				} else if (srcFullText.indexOf(tagContent) + tagContent.length() == srcFullText.length()) { // tail
					targetPureText = targetPureText + tagContent;
				}
			} else {
				InnerTagBean tagFirst = srcTags.get(0);
				InnerTagBean tagLast = srcTags.get(srcTags.size() - 1);
				String firstTagCnt = tagFirst.getContent();
				String lastTagCnt = tagLast.getContent();
				if (srcFullText.indexOf(firstTagCnt) == 0) {
					// 句首标记是一个独立标记
					if (tagFirst.getType() == TagType.STANDALONE) {
						StringBuilder sb = processNextStandloneTag(srcFullText, srcTags, 0);
						sb.insert(0, firstTagCnt);
						targetPureText = sb.append(targetPureText).toString();
						if (tagLast.getType() == TagType.STANDALONE && targetPureText.indexOf(lastTagCnt) == -1
								&& srcFullText.indexOf(lastTagCnt) + lastTagCnt.length() == srcFullText
										.length()) {
							// 最后标记是一个独立标记，且在句末
							sb = processPreStandloneTag(targetPureText, srcFullText, srcTags, srcTags.size() - 1);
							sb.insert(0, targetPureText).append(lastTagCnt);
							targetPureText = sb.toString();
						}
					} else if (tagFirst.getType() == TagType.START && tagLast.getType() == TagType.END && tagFirst.getIndex() == tagLast.getIndex()) {
						// 句首是一个开始 且最后一个标记是结束
						if (srcFullText.lastIndexOf(lastTagCnt) + lastTagCnt.length() == srcFullText.length()) {
							// 最后一个标记在句尾，满足环绕条件
							if(srcTags.size() > 2){ // 有其他标记
								// header process
								StringBuilder sb = processNextStandloneTag(srcFullText, srcTags, 0);
								sb.insert(0, firstTagCnt);
								targetPureText = sb.append(targetPureText).toString();
								sb = processPreStandloneTag(targetPureText, srcFullText, srcTags, srcTags.size() - 1);
								sb.insert(0, targetPureText).append(lastTagCnt);
								targetPureText = sb.toString();
							} else {
								targetPureText = firstTagCnt + targetPureText + lastTagCnt;
							}
						}
					} else if (tagLast.getType() == TagType.STANDALONE
							&& srcFullText.indexOf(lastTagCnt) + lastTagCnt.length() == srcFullText.length()) {
						// 最后标记是一个独立标记，且在句末
						StringBuilder sb = processPreStandloneTag(targetPureText, srcFullText, srcTags, srcTags.size() - 1);
						sb.insert(0, targetPureText).append(lastTagCnt);
						targetPureText = sb.toString();						
					}
				} else if (tagLast.getType() == TagType.STANDALONE
						&& srcFullText.indexOf(lastTagCnt) + lastTagCnt.length() == srcFullText.length()) {
					// 最后一个标记独立标记且在句末
					StringBuilder sb = processPreStandloneTag(targetPureText, srcFullText, srcTags, srcTags.size() - 1);
					sb.insert(0, targetPureText).append(lastTagCnt);
					targetPureText = sb.toString();
				}
			}
		}

		return targetPureText;
	}

	private static StringBuilder processNextStandloneTag(String srcFullText, List<InnerTagBean> srcTags, int startTagsOffset) {
		StringBuilder sb = new StringBuilder();
		if(startTagsOffset >= srcTags.size()){
			return sb;
		}
		String firstTagCnt = srcTags.get(startTagsOffset).getContent();
		int i = startTagsOffset + 1;
		int offset = srcFullText.indexOf(firstTagCnt) + firstTagCnt.length();
		while (i < srcTags.size()) {
			InnerTagBean nextTag = srcTags.get(i);
			String nextTagCnt = nextTag.getContent();
			if (nextTag.getType() == TagType.STANDALONE && srcFullText.indexOf(nextTagCnt) == offset) {
				sb.append(nextTagCnt);
				offset += nextTagCnt.length();
				i++;
			} else {
				break;
			}
		}
		return sb;
	}
	
	private static StringBuilder processPreStandloneTag(String targetText, String srcFullText, List<InnerTagBean> srcTags, int startTagsOffset){
		StringBuilder sb = new StringBuilder();
		if(startTagsOffset >= srcTags.size()){
			return sb;
		}
		String lastTagCnt = srcTags.get(startTagsOffset).getContent();
		int i = startTagsOffset - 1;
		int offset = srcFullText.lastIndexOf(lastTagCnt);
		while (i >= 0) {
			InnerTagBean preTag = srcTags.get(i);
			String preTagCnt = preTag.getContent();
			if (preTag.getType() == TagType.STANDALONE && targetText.indexOf(preTagCnt) == -1 
					&& srcFullText.lastIndexOf(preTagCnt) + preTagCnt.length() == offset) {
				sb.insert(0, preTagCnt);
				offset = srcFullText.lastIndexOf(preTagCnt);
				i--;
			} else {
				break;
			}
		}
		return sb;
	}

}
