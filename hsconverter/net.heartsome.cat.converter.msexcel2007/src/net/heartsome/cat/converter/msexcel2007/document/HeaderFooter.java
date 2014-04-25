/**
 * HeaderFooter.java
 *
 * Version information :
 *
 * Date:2012-8-9
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007.document;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class HeaderFooter {
	
	private String type;

	private String content;

	public HeaderFooter(String type, String content) {
		this.type = type;
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
