package net.heartsome.cat.converter.ttx;

public class TextBean {
	private int tagId;
	private String text;
	/** 这个属性是用于逆转换的，也就是文本段所处在节点的内容，这个节点一般是df */
	private String parentFrag;
	/** 这个属性是用于逆转换的，是否有标记 */
	private boolean hasTag;
	/** 标识此文本段是否是纯文本还是标记 */
	private boolean isText;
	
	
	public TextBean(){}
	
	public TextBean(int tagId, String text, boolean isText){
		this.tagId = tagId;
		this.text = text;
		this.isText = isText;
	}
	public int getTagId() {
		return tagId;
	}
	public void setTagId(int tagId) {
		this.tagId = tagId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getParentFrag() {
		return parentFrag;
	}
	public void setParentFrag(String parentFrag) {
		this.parentFrag = parentFrag;
	}
	public boolean isHasTag() {
		return hasTag;
	}
	public void setHasTag(boolean hasTag) {
		this.hasTag = hasTag;
	}
	public boolean isText() {
		return isText;
	}
	public void setText(boolean isText) {
		this.isText = isText;
	}

}
