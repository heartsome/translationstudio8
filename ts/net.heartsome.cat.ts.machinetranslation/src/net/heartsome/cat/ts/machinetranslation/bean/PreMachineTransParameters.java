/**
 * PreTransParameters.java
 *
 * Version information :
 *
 * Date:2012-5-8
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.machinetranslation.bean;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class PreMachineTransParameters {

	/**
	 * 是否使用goolge翻译
	 */
	private boolean isGoogleTranslate;

	/**
	 * 是否使用bing翻译
	 */
	private boolean isBingTranslate;

	/**
	 * 是否忽略完全匹配
	 */
	private boolean isIgnoreExactMatch;

	/**
	   * 是否忽略锁定
	   */
	private boolean isIgnoreLock;
	
	/**
	  * 机器翻译首选项值
	  */
	private  PrefrenceParameters pefrenceParameters;
	 /** @return the isGoogleTranslate */
	public boolean isGoogleTranslate() {
		return isGoogleTranslate;
	}
	/** @param isGoogleTranslate the isGoogleTranslate to set */
	public void setGoogleTranslate(boolean isGoogleTranslate) {
		this.isGoogleTranslate = isGoogleTranslate;
	}
	/** @return the isBingTranslate */
	public boolean isBingTranslate() {
		return isBingTranslate;
	}
	/** @param isBingTranslate the isBingTranslate to set */
	public void setBingTranslate(boolean isBingTranslate) {
		this.isBingTranslate = isBingTranslate;
	}
	/** @return the isIgnoreExactMatch */
	public boolean isIgnoreExactMatch() {
		return isIgnoreExactMatch;
	}
	/** @param isIgnoreExactMatch the isIgnoreExactMatch to set */
	public void setIgnoreExactMatch(boolean isIgnoreExactMatch) {
		this.isIgnoreExactMatch = isIgnoreExactMatch;
	}
	/** @return the isIgnoreLock */
	public boolean isIgnoreLock() {
		return isIgnoreLock;
	}
	/** @param isIgnoreLock the isIgnoreLock to set */
	public void setIgnoreLock(boolean isIgnoreLock) {
		this.isIgnoreLock = isIgnoreLock;
	}
	/** @return the pefrenceParameters */
	public PrefrenceParameters getPefrenceParameters() {
		return PrefrenceParameters.getInstance();
	}
	/** @param pefrenceParameters the pefrenceParameters to set */
	public void setPefrenceParameters(PrefrenceParameters pefrenceParameters) {
		this.pefrenceParameters = pefrenceParameters;
	}
	
}
