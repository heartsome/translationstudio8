/**
 * TmxNote.java
 *
 * Version information :
 *
 * Date:2013-1-25
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.bean;

/**
 * The TMX note node
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxNote {
	private String xmlLang;
	private String encoding;
	private String content;

	public TmxNote() {
	}
	/**
	 * @param xmlLang
	 *            Attribute
	 * @param encoding
	 *            Attribute
	 * @param content
	 *            The content of note Node
	 */
	public TmxNote(String xmlLang, String encoding, String content) {
		this.xmlLang = xmlLang;
		this.encoding = encoding;
		this.content = content;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TmxNote){
			TmxNote _obj = (TmxNote) obj;
			if(_obj.content.equals(this.content)){
				return true;
			}
		}
		return false;
	}

	/** @return the xmlLang */
	public String getXmlLang() {
		return xmlLang;
	}

	/**
	 * @param xmlLang
	 *            the xmlLang to set
	 */
	public void setXmlLang(String xmlLang) {
		this.xmlLang = xmlLang;
	}

	/** @return the encoding */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding
	 *            the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/** @return the content */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

}
