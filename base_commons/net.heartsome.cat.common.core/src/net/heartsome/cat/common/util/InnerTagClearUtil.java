/**
 * InnerTagClearUtil.java
 *
 * Version information :
 *
 * Date:2013-10-21
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 可在 XLIFF 中使用、而不能在 TMX 中使用的内部标记有：<br/>
 * <g> <bx/> <ex/> <x/> <mrk><br/>
 * 可在 TMX 中使用、而不能在 XLIFF 中使用的内部标记有： <br/>
 * <ut> <hi><br/>
 * 主要清理TMX 与Xliff 文本的内部标记不兼容的情况
 * @author yule
 * @version
 * @since JDK1.6
 */
public class InnerTagClearUtil {

	private static Pattern XLIFF_CLEAR_PATTERN = Pattern
			.compile("<ex.+?/>|<x.+?/>|<bx.+?/>|<mrk.+?>|</mrk>|<g.+?>|</g>");

	private static Pattern TMX_CLEAR_PATTERN = Pattern.compile("<ut.*?>.*</ut>|<hi.+?>|</hi>");

	private InnerTagClearUtil() {
		// single instance
	}

	/**
	 * 清理Xliff的内部标记，清理后的文本供TMX使用
	 * @param xliffXmlContent
	 *            : Xliff源或者目标文本的FullText
	 * @return ;
	 */
	public static String clearXliffTag4Tmx(String xliffXmlContent) {
		if (null == xliffXmlContent) {
			return null;
		}
		Matcher matcher = XLIFF_CLEAR_PATTERN.matcher(xliffXmlContent);
		return matcher.replaceAll("");
	}

	/**
	 * 清理TMX的内部标记，清理后的文本供Xliff使用
	 * @param tmxXmlContent
	 * @return ;
	 */
	public static String clearTmx4Xliff(String tmxXmlContent) {
		if (null == tmxXmlContent) {
			return null;
		}
		Matcher matcher = TMX_CLEAR_PATTERN.matcher(tmxXmlContent);
		return matcher.replaceAll("");
	}


}
