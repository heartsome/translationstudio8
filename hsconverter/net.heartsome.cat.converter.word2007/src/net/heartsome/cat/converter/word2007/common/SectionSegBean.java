package net.heartsome.cat.converter.word2007.common;
/**
 * 保存每个文本段中一个小片段的容
 * @author robert	2012-08-10
 */
public class SectionSegBean {
	private String ctype;
	private String text;
	private String style;
	private String extendNodesStr;
	private String phTagStr;
	
	public SectionSegBean () {}
	public SectionSegBean(String ctype, String text, String style, String extendNodesStr, String phTagStr){
		this.ctype = ctype;
		this.text = text;
		this.style = style;
		this.extendNodesStr = extendNodesStr;
		this.phTagStr = phTagStr;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getExtendNodesStr() {
		return extendNodesStr;
	}
	public void setExtendNodesStr(String extendNodesStr) {
		this.extendNodesStr = extendNodesStr;
	}
	public String getPhTagStr() {
		return phTagStr;
	}
	public void setPhTagStr(String phTagStr) {
		this.phTagStr = phTagStr;
	}
	public String getCtype() {
		return ctype;
	}
	public void setCtype(String ctype) {
		this.ctype = ctype;
	}


	
}
