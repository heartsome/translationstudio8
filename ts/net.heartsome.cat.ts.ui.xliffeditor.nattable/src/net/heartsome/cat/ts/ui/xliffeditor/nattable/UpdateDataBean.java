/**
 * UpdateDataBean.java
 *
 * Version information :
 *
 * Date:2012-6-27
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.xliffeditor.nattable;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class UpdateDataBean {
	private String text;
	private String matchType;
	private String quality;

	public UpdateDataBean() {
		this.text = "";
		this.matchType = null;
		this.quality = null;
	}

	public UpdateDataBean(String text, String matchType, String quality) {
		this.text = text;
		this.matchType = matchType;
		this.quality = quality;
	}

	/** @return the text */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/** @return the matchType */
	public String getMatchType() {
		return matchType;
	}

	/**
	 * @param matchType
	 *            the matchType to set
	 */
	public void setMatchType(String matchType) {
		this.matchType = matchType;
	}

	/** @return the quality */
	public String getQuality() {
		return quality;
	}

	/**
	 * @param quality
	 *            the quality to set
	 */
	public void setQuality(String quality) {
		this.quality = quality;
	}
	
	@Override
	public String toString() {
		return text;
	}

}
