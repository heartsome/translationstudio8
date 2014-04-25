package net.heartsome.cat.ts.core.bean;

/**
 * 分割　xliff 文件时设置分割点所用到的片段　bean
 * @author robert	2013-10-16
 */
public class SegPointBean {
	/** 当前片段所在行的的唯一标识符 */
	private String rowId;
	/** 当前片段的总字数 */
	private int wordNumber;
	
	public SegPointBean(int wordNumber){
		this.wordNumber = wordNumber;
	}
	
	public SegPointBean(String rowId){
		this.rowId = rowId;
	}
	
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	public int getWordNumber() {
		return wordNumber;
	}
	public void setWordNumber(int wordNumber) {
		this.wordNumber = wordNumber;
	}
	
	
}
