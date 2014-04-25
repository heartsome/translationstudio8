/**
 * TransUnitInfoBean.java
 *
 * Version information :
 *
 * Date:2012-4-27
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.tm.bean;

/**
 * 翻译单元信息封装，用于匹配时所需翻译单元信息的封装
 * @author jason
 * @version
 * @since JDK1.5
 */
public class TransUnitInfo2TranslationBean {

	private String srcFullText = "";

	private String srcPureText = "";

	// 翻译单元源文语言代码
	private String srcLanguage = "";

	// 翻译单元目标语言代码
	private String tgtLangugage = "";

	// 上文
	private String preContext = "";

	// 下文
	private String nextContext = "";
	
	/**
	 * 重置当前翻译单元信息
	 *  ;
	 */
	public void resetTuInfo(){
		srcFullText = "";
		srcPureText = "";
		// 翻译单元源文语言代码
		srcLanguage = "";
		// 翻译单元目标语言代码
		tgtLangugage = "";
		// 上文
		preContext = "";
		// 下文
		nextContext = "";
	}

	/** @return the srcFullText */
	public String getSrcFullText() {
		return srcFullText;
	}

	/**
	 * @param srcFullText
	 *            翻译单元完整的文本内容
	 */
	public void setSrcFullText(String srcFullText) {
		this.srcFullText = srcFullText;
	}

	/** @return 翻译单元源文纯文本内容 */
	public String getSrcPureText() {
		return srcPureText;
	}

	/**
	 * @param srcPureText
	 *            the srcPureText to set
	 */
	public void setSrcPureText(String srcPureText) {
		this.srcPureText = srcPureText;
	}

	/** @return the srcLanguage */
	public String getSrcLanguage() {
		return srcLanguage;
	}

	/**
	 * @param srcLanguage
	 *            the srcLanguage to set
	 */
	public void setSrcLanguage(String srcLanguage) {
		this.srcLanguage = srcLanguage;
	}

	/** @return the tgtLangugage */
	public String getTgtLangugage() {
		return tgtLangugage;
	}

	/**
	 * @param tgtLangugage
	 *            the tgtLangugage to set
	 */
	public void setTgtLangugage(String tgtLangugage) {
		this.tgtLangugage = tgtLangugage;
	}

	/** @return the preContext */
	public String getPreContext() {
		return preContext;
	}

	/**
	 * @param preContext
	 *            the preContext to set
	 */
	public void setPreContext(String preContext) {
		this.preContext = preContext;
	}

	/** @return the nextContext */
	public String getNextContext() {
		return nextContext;
	}

	/**
	 * @param nextContext
	 *            the nextContext to set
	 */
	public void setNextContext(String nextContext) {
		this.nextContext = nextContext;
	}

}
