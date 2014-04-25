package net.heartsome.cat.convert.ui.model;


/**
 * 文本段分割与合并信息的pojo类，此类主要用于在逆转换时，将分割与合并后的文件进行相关处理时的数据单元
 * @author robert	2012-11-29
 */
public class SegMergeInfoBean {
	/** 当前信息是否是属于合并的 */
	private boolean isMerge;
	private String phFrag;
	private String phID;
	private String mergeFirstId;
	private String mergeSecondId;
	
	public SegMergeInfoBean(boolean isMerge){
		this.isMerge = isMerge;
	}
	
	public SegMergeInfoBean(boolean isMerge, String phFrag, String phID, String mergeFirstId, String mergeSecondId){
		this.isMerge = isMerge;
		this.phFrag = phFrag;
		this.phID = phID;
		this.mergeFirstId = mergeFirstId;
		this.mergeSecondId = mergeSecondId;
	}
	
	public boolean isMerge() {
		return isMerge;
	}
	public void setMerge(boolean isMerge) {
		this.isMerge = isMerge;
	}
	public String getPhFrag() {
		return phFrag;
	}
	public void setPhFrag(String phFrag) {
		this.phFrag = phFrag;
	}
	public String getPhID() {
		return phID;
	}
	public void setPhID(String phID) {
		this.phID = phID;
	}
	public String getMergeFirstId() {
		return mergeFirstId;
	}
	public void setMergeFirstId(String mergeFirstId) {
		this.mergeFirstId = mergeFirstId;
	}
	public String getMergeSecondId() {
		return mergeSecondId;
	}
	public void setMergeSecondId(String mergeSecondId) {
		this.mergeSecondId = mergeSecondId;
	}
	
	
	
}
