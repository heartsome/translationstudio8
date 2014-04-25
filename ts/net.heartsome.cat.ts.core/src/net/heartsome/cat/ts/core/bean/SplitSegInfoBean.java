package net.heartsome.cat.ts.core.bean;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 分割文件的片段信息
 * @author robert	2013-10-17
 */
public class SplitSegInfoBean {
	/** 分割文件的总体字数 */
	private int totalWordNum;
	/** 分割文件的总行数 */
	private int totalTuNum;
	/** 每行 */
	private Map<String, Integer> rowWordNumMap = new LinkedHashMap<String, Integer>();
	
	public SplitSegInfoBean(int totalWordNum, int totalTuNum, Map<String, Integer> rowWordNumMap){
		this.totalWordNum = totalWordNum;
		this.totalTuNum = totalTuNum;
		this.rowWordNumMap = rowWordNumMap;
		
	}

	public int getTotalWordNum() {
		return totalWordNum;
	}
	public void setTotalWordNum(int totalWordNum) {
		this.totalWordNum = totalWordNum;
	}
	public int getTotalTuNum() {
		return totalTuNum;
	}
	public void setTotalTuNum(int totalTuNum) {
		this.totalTuNum = totalTuNum;
	}
	public Map<String, Integer> getRowWordNumMap() {
		return rowWordNumMap;
	}
	public void setRowWordNumMap(Map<String, Integer> rowWordNumMap) {
		this.rowWordNumMap = rowWordNumMap;
	}
	
	
}
