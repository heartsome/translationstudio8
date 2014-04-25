/**
 * TranslationMemoryTools.java
 *
 * Version information :
 *
 * Date:2012-12-3
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database;


/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public final class TranslationMemoryTools {
	
	public static String getInnerTagContent(String fullText) {
		StringBuffer sbOriginalValue = new StringBuffer(fullText);
		StringBuffer result = new StringBuffer();
		int start = -1;
		while ((start = sbOriginalValue.indexOf("<", start + 1)) > -1) {
			int end = sbOriginalValue.indexOf(">", start + 1);
			if (end > -1) {
				String xmlTag = sbOriginalValue.substring(start, end + 1); // 提取出的内部标记xml形式的文本
				String tagName = getTagName(xmlTag);

				if (xmlTag.indexOf("/>", 1) > -1) { // 独立标签
					result.append(xmlTag);
				} else if (xmlTag.indexOf("</") > -1) { // 结束标签
					result.append(xmlTag);
				} else if (xmlTag.indexOf(">") > -1) { // 开始标签
					if ("bpt".equals(tagName)) {
						int endIndex = sbOriginalValue.indexOf("</bpt>", start) + "</bpt>".length();
						xmlTag = sbOriginalValue.substring(start, endIndex);
						result.append(xmlTag);
					} else if ("ept".equals(tagName)) {
						int endIndex = sbOriginalValue.indexOf("</ept>", start) + "</ept>".length();
						xmlTag = sbOriginalValue.substring(start, endIndex);
						result.append(xmlTag);
					} else if ("ph".equals(tagName) || "it".equals(tagName)) {
						String tempTagName = "</" + tagName + ">";
						int endIndex = sbOriginalValue.indexOf(tempTagName, start) + tempTagName.length();
						xmlTag = sbOriginalValue.substring(start, endIndex);
						result.append(xmlTag);
					} else {
						result.append(xmlTag);
					}
				}
			}
		}
		return result.toString();
	}

	/**
	 * 得到标记的名称
	 * @param xmlTag
	 *            XML格式的标记
	 * @return 标记名称;
	 */
	private static String getTagName(String xmlTag) {
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
	
	public static void main(String[] args) {
		String fullText = "The Silicom Gigabit Ethernet<g dsf> PCI Express server<ph>dadfa</ph> adapters are PCI Express network interface cards that contain Multiple / Single  independent Gigabit Ethernet port/s on a PCI Express adapter.</g>";
		System.out.println(getInnerTagContent(fullText));
	}
}
