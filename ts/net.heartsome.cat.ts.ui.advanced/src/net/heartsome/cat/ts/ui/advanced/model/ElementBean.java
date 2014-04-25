package net.heartsome.cat.ts.ui.advanced.model;

/**
 * XML转换器配置中元素的POJO类
 * @author robert 2012-02-23
 * @version
 * @since JDK1.6
 */
public class ElementBean {
	/** 元素名 */
	private String name;
	/** 元素类型，对应生成文件属性hard-break */
	private String type;
	/** 内联类型，对应生成文件属性ctype */
	private String inlineType;
	/** 可翻译属性，对应生成文件属性attributes */
	private String transAttribute;
	/** 保留空格，对应生成文件属性keep-format */
	private String remainSpace;
	
	public ElementBean() {
	}
	
	public ElementBean(String name, String type, String inlineType, String transAttribute, String remainSpace) {
		this.name = name;
		this.type = type;
		this.inlineType = inlineType;
		this.transAttribute = transAttribute;
		this.remainSpace = remainSpace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getInlineType() {
		return inlineType;
	}

	public void setInlineType(String inlineType) {
		this.inlineType = inlineType;
	}

	public String getTransAttribute() {
		return transAttribute;
	}

	public void setTransAttribute(String transAttribute) {
		this.transAttribute = transAttribute;
	}

	public String getRemainSpace() {
		return remainSpace;
	}

	public void setRemainSpace(String remainSpace) {
		this.remainSpace = remainSpace;
	}

}
