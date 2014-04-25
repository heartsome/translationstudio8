package net.heartsome.cat.ts.core.bean;

import java.util.Hashtable;
import java.util.Vector;

import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.xml.vtdimpl.VTDUtils;

/**
 * 匹配对象。参见 XLIFF v1.2 标准中的 alt-trans 节点信息。
 */
public class AltTransBean implements IXMLBean {

	private FuzzySearchResult fuzzyResult;

	/**
	 * 匹配源文本。即 alt-trans 节点中 source 子节点的纯文本内容。
	 */
	private String srcText;

	/**
	 * 匹配源节点内容。即 alt-trans 节点中 source 子节点的全部内容，含内部标记等子节点。
	 */
	private String srcContent;

	/**
	 * 匹配目标文本。即 alt-trans 节点中 target 子节点的纯文本。
	 */
	private String tgtText;

	/**
	 * 匹配目标节点内容。即 alt-trans 节点中 target 子节点的全部内容，含内部标记等子节点。
	 */
	private String tgtContent;

	/**
	 * 匹配源节点属性。即 alt-trans 节点中 source 子节点的属性。键取自 XLIFF 标准。
	 */
	private Hashtable<String, String> srcProps;

	/**
	 * 匹配目标节点属性。即 alt-trans 节点中 target 子节点的属性。键取自 XLIFF 标准。
	 */
	private Hashtable<String, String> tgtProps;

	/**
	 * 匹配节点属性。即 alt-trans 节点的属性。键取自 XLIFF 标准。
	 */
	private Hashtable<String, String> matchProps;

	/**
	 * 匹配节点中的属性组集合。
	 */
	private Vector<PropGroupBean> propGroups;

	/**
	 * 获取当前匹配的源文本。不含内部标记等子节点内容。
	 */
	public String getSrcText() {
		return srcText;
	}

	/**
	 * 设置当前匹配的源文本。
	 * @param srcText
	 *            源文本内容。不含内部标记等子节点内容。
	 */
	public void setSrcText(String srcText) {
		this.srcText = srcText;
	}

	/**
	 * 获取当前匹配的源节点内容。含内部标记等子节点内容。
	 */
	public String getSrcContent() {
		return srcContent;
	}

	/**
	 * 设置当前匹配的源节点内容。
	 * @param srcContent
	 *            源节点内容。含内部标记等子节点内容。
	 */
	public void setSrcContent(String srcContent) {
		this.srcContent = srcContent;
	}

	/**
	 * 获取当前匹配的目标节点内容。含内部标记等子节点内容。
	 */
	public String getTgtContent() {
		return tgtContent;
	}

	/**
	 * 设置当前匹配的目标节点内容。含内部标记等子节点内容。
	 * @param tgtContent
	 *            目标节点内容。
	 */
	public void setTgtContent(String tgtContent) {
		this.tgtContent = tgtContent;
	}

	/**
	 * 获取当前匹配的目标文本。
	 */
	public String getTgtText() {
		return tgtText;
	}

	/**
	 * 设置当前匹配的目标文本。
	 */
	public void setTgtText(String tgtText) {
		this.tgtText = tgtText;
	}

	/**
	 * 获取当前匹配的源节点的全部属性。
	 */
	public Hashtable<String, String> getSrcProps() {
		return srcProps;
	}

	/**
	 * 设置当前匹配的源节点的全部属性。
	 */
	public void setSrcProps(Hashtable<String, String> srcProps) {
		this.srcProps = srcProps;
	}

	/**
	 * 获取当前匹配的目标节点的全部属性。
	 */
	public Hashtable<String, String> getTgtProps() {
		return tgtProps;
	}

	/**
	 * 设置当前匹配的目标节点的全部属性。
	 */
	public void setTgtProps(Hashtable<String, String> tgtProps) {
		this.tgtProps = tgtProps;
	}

	/**
	 * 获取当前匹配节点的全部属性。
	 */
	public Hashtable<String, String> getMatchProps() {
		return matchProps;
	}

	/**
	 * 设置当前匹配节点的全部属性。
	 */
	public void setMatchProps(Hashtable<String, String> matchProps) {
		this.matchProps = matchProps;
	}

	/**
	 * 构建一个无任何信息的匹配对象。
	 */
	public AltTransBean() {
	}

	/**
	 * 构建一个仅有源文本与匹配文本的匹配对象。
	 * @param srcText
	 *            匹配源文本。
	 * @param tgtText
	 *            匹配目标文本。
	 */
	public AltTransBean(String srcText, String tgtText) {
		this.srcText = srcText;
		this.tgtText = tgtText;
	}

	/**
	 * 构建一个无匹配节点属性的匹配对象。
	 * @param srcText
	 *            匹配源文本。
	 * @param tgtText
	 *            匹配目标文本。
	 * @param srcProps
	 *            匹配源节点的全部属性。
	 * @param tgtProps
	 *            匹配目标节点的全部属性。
	 */
	public AltTransBean(String srcText, String tgtText, Hashtable<String, String> srcProps,
			Hashtable<String, String> tgtProps) {
		this.srcText = srcText;
		this.tgtText = tgtText;
		this.srcProps = srcProps;
		this.tgtProps = tgtProps;
	}

	/**
	 * 构建一个匹配对象。
	 * @param srcContent
	 *            匹配源文本。
	 * @param tgtContent
	 *            匹配目标文本。
	 * @param srcProps
	 *            匹配源节点的全部属性。
	 * @param tgtProps
	 *            匹配目标节点的全部属性。
	 * @param matchProps
	 *            匹配节点的全部属性。
	 */
	public AltTransBean(String srcContent, String tgtContent, Hashtable<String, String> srcProps,
			Hashtable<String, String> tgtProps, Hashtable<String, String> matchProps) {
		this.srcContent = srcContent;
		this.tgtContent = tgtContent;
		this.srcProps = srcProps;
		this.tgtProps = tgtProps;
		this.matchProps = matchProps;
	}

	/**
	 * 构建一个指定语言及文本匹配对象。
	 * @param srcText
	 *            匹配源文本。
	 * @param tgtText
	 *            匹配目标文本。
	 * @param srcLang
	 *            匹配源节点的语言属性。
	 * @param tgtLang
	 *            匹配目标节点的语言属性。
	 */
	public AltTransBean(String srcText, String tgtText, String srcLang, String tgtLang) {
		srcProps = new Hashtable<String, String>();
		srcProps.put("xml:lang", srcLang);
		tgtProps = new Hashtable<String, String>();
		tgtProps.put("xml:lang", srcLang);
		this.srcText = srcText;
		this.tgtText = tgtText;
	}

	/**
	 * 构建一个指定语言、文本以及翻译匹配来源的匹配对象。
	 * @param srcText
	 *            匹配源文本。
	 * @param tgtText
	 *            匹配目标文本。
	 * @param srcLang
	 *            匹配源节点的语言属性。
	 * @param tgtLang
	 *            匹配目标节点的语言属性。
	 * @param origin
	 *            匹配节点的翻译匹配来源属性。
	 * @param tooleId
	 *            匹配节点的翻译工具标识
	 */
	public AltTransBean(String srcText, String tgtText, String srcLang, String tgtLang, String origin, String tooleId) {
		srcProps = new Hashtable<String, String>();
		srcProps.put("xml:lang", srcLang);
		tgtProps = new Hashtable<String, String>();
		tgtProps.put("xml:lang", srcLang);
		matchProps = new Hashtable<String, String>();
		matchProps.put("origin", origin);
		matchProps.put("tool-id", tooleId);
		this.srcText = srcText;
		this.tgtText = tgtText;
	}

	/**
	 * 获取匹配源语言属性。无此属性返回 null。
	 */
	public String getSrcLang() {
		if (srcProps == null) {
			return null;
		} else {
			return srcProps.get("xml:lang");
		}
	}

	/**
	 * 获取匹配目标语言属性。无此属性返回 null。
	 */
	public String getTgtLang() {
		if (tgtProps == null) {
			return null;
		} else {
			return tgtProps.get("xml:lang");
		}
	}

	/**
	 * 获取匹配来源属性。无此属性返回 null。
	 */
	public String getMatchOrigin() {
		if (matchProps == null) {
			return null;
		} else {
			return matchProps.get("origin");
		}
	}

	/**
	 * 设置匹配的属性组集合。
	 * @param propGroups
	 *            属性组集合。
	 */
	public void setPropGroups(Vector<PropGroupBean> propGroups) {
		this.propGroups = propGroups;
	}

	/**
	 * 获取匹配的属性组集合。
	 */
	public Vector<PropGroupBean> getPropGroups() {
		return propGroups;
	}

	/** @return the fuzzyResult */
	public FuzzySearchResult getFuzzyResult() {
		return fuzzyResult;
	}

	/**
	 * @param fuzzyResult
	 *            the fuzzyResult to set
	 */
	public void setFuzzyResult(FuzzySearchResult fuzzyResult) {
		this.fuzzyResult = fuzzyResult;
	}

	public String toXMLString() {
		String srcXML = VTDUtils.getNodeXML("source", srcContent, srcProps);
		String tgtXML = VTDUtils.getNodeXML("target", tgtContent, tgtProps);
		String propGroupsXML = "";
		if (propGroups != null && !propGroups.isEmpty()) {
			for (PropGroupBean propGroupBean : propGroups) {
				propGroupsXML += propGroupBean.toXMLString();
			}
		}
		return VTDUtils.getNodeXML("alt-trans", srcXML + tgtXML + propGroupsXML, matchProps);
	}

}
