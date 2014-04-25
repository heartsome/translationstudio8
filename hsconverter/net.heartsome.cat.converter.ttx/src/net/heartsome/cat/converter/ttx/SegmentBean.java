package net.heartsome.cat.converter.ttx;


/**
 * 专门针对 ArrangeTTX 类所需要用的数据进行封装的 POJO 类。
 * @author robert	2012-07-25
 */
public class SegmentBean {
	/** 一个文本段（即要进行翻译的文本段）所处在节点的父节点的内容，通常指 <df> 节点，若无 <df> ，则其内容为其自己。 */
	private String parentNodeFrag;
	/** 这个文本段的父节点是否包括标记 */
	private boolean hasTag;
	/** 文本段的内容 */
	private String segment;
	/** <ut> 标记，这个标记的内容有可能是paragram, 也有可能是 cf 节点。 */
	private String tagStr;
	
	public SegmentBean(){}

	public String getParentNodeFrag() {
		return parentNodeFrag;
	}
	public void setParentNodeFrag(String parentNodeFrag) {
		this.parentNodeFrag = parentNodeFrag;
	}
	public boolean isHasTag() {
		return hasTag;
	}
	public void setHasTag(boolean hasTag) {
		this.hasTag = hasTag;
	}
	public String getSegment() {
		return segment;
	}
	public void setSegment(String segment) {
		this.segment = segment;
	}
	public String getTagStr() {
		return tagStr;
	}
	public void setTagStr(String tagStr) {
		this.tagStr = tagStr;
	}
	
}
