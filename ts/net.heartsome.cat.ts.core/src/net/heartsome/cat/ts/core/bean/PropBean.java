package net.heartsome.cat.ts.core.bean;

import java.util.Hashtable;

import net.heartsome.xml.vtdimpl.VTDUtils;

/**
 * 属性。参见 XLIFF v1.2 标准中的 prop 节点。不同于其它 XML 节点的属性。
 */
public class PropBean implements IXMLBean {

	/**
	 * 属性类型
	 */
	private String proptype;

	/**
	 * 属性值。即 prop 节点的文本内容。
	 */
	private String value;

	/**
	 * 属性语言代码
	 */
	private String lang;

	/**
	 * 获取属性类型。
	 */
	public String getProptype() {
		return proptype;
	}

	/**
	 * 获取属性值。无值返回空字符串。
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 获取属性语言。
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * 设置属性语言。
	 * @param lang
	 *            属性语言代码。
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}

	/**
	 * 构建一个指定属性类型、属性值及属性语言的属性对象。
	 */
	public PropBean(String proptype, String value, String lang) {
		this.proptype = proptype;
		if (value == null) {
			this.value = "";
		} else {
			this.value = value;
		}

		this.lang = lang;
	}

	/**
	 * 构建一个指定属性类型及值的对象。
	 */
	public PropBean(String proptype, String value) {
		this.proptype = proptype;
		if (value == null) {
			this.value = "";
		} else {
			this.value = value;
		}
	}

	public String toXMLString() {
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put("prop-type", proptype);
		if (lang != null) {
			props.put("xml:lang", lang);
		}
		return VTDUtils.getNodeXML("hs:prop", value, props);
	}
}
