package net.heartsome.cat.converter.wordfast3;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 标记的 pojo 类
 * @author robert	2012-12-19
 */
public class TagBean {
	private String content;
	private String frag;
	private Map<String, String> attributesMap = new LinkedHashMap<String, String>();
	
	public TagBean(){}
	
	public TagBean(String content, String frag, Map<String, String> attributesMap){
		this.content = content;
		this.frag = frag;
		this.attributesMap = attributesMap;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFrag() {
		return frag;
	}

	public void setFrag(String frag) {
		this.frag = frag;
	}

	public Map<String, String> getAttributesMap() {
		return attributesMap;
	}

	public void setAttributesMap(Map<String, String> attributesMap) {
		this.attributesMap = attributesMap;
	}
	
	
	
}
