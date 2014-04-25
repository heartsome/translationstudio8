package net.heartsome.cat.ts.ui.qa.model;

import java.text.NumberFormat;

/**
 * 编辑进度分析结果封装类
 * @author robert	2011-12-14
 */
public class EditProgressFAResult {
	/** 未批准文本段 */
	private int notApprovedParas;
	/** 已批准文本段 */
	private int approvedParas;
	/** 未批准字数 */
	private int notApprovedWords;
	/** 已批准字数 */
	private int approvedWords;
	/** 锁定文本段的段数 */
	private int lockedParas = 0;
	/** 锁定文本段的字数 */
	private int lockedWords = 0;
	
	public EditProgressFAResult(){ }

	public EditProgressFAResult(int notApprovedParas, int approvedParas,
			int lockedParas, int notApprovedWords, int approvedWords, int lockedWords) {
		this.notApprovedParas = notApprovedParas;
		this.approvedParas = approvedParas;
		this.lockedParas = lockedParas;
		this.notApprovedWords = notApprovedWords;
		this.approvedWords = approvedWords;
		this.lockedWords = lockedWords;
	}

	public int getNotApprovedParas() {
		return notApprovedParas;
	}
	public int getApprovedParas() {
		return approvedParas;
	}
	public int getLockedParas() {
		return lockedParas;
	}
	public int getNotApprovedWords() {
		return notApprovedWords;
	}
	public int getApprovedWords() {
		return approvedWords;
	}
	public int getLockedWords() {
		return lockedWords;
	}

	public void setNotApprovedParas(int notApprovedParas) {
		this.notApprovedParas += notApprovedParas;
	}
	public void setApprovedParas(int approvedParas) {
		this.approvedParas += approvedParas;
	}
	public void setLockedParas(int lockedParas) {
		this.lockedParas += lockedParas;
	}
	public void setNotApprovedWords(int notApprovedWords) {
		this.notApprovedWords += notApprovedWords;
	}
	public void setApprovedWords(int approvedWords) {
		this.approvedWords += approvedWords;
	}
	public void setLockedWords(int lockedWords) {
		this.lockedWords += lockedWords;
	}
	
	/**
	 * 获取所有字数
	 * @return
	 */
	public int getTotalWords(){
		return notApprovedWords + approvedWords;
	}
	
	/**
	 * 获取未批准字数的比例
	 * @return	如25.00%
	 */
	public String getNotApprovedWordsRatio(){
		float ratio = (float)notApprovedWords / (notApprovedWords + approvedWords);
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		
		return format.format(ratio * 100) + "%";
	}
	
	/**
	 * 获取已批准字数的比例
	 * @return	如25.00%
	 */
	public String getApprovedWordsRatio(){
		float ratio = (float)approvedWords / (notApprovedWords + approvedWords);
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		return format.format(ratio * 100) + "%";
	}
	
	/**
	 * 获取所有文本段
	 * @return
	 */
	public int getTotalParas(){
		return notApprovedParas + approvedParas;
	}
	
	/**
	 * 获取未批准文本段的比例
	 * @return	如25.00%
	 */
	public String getNotApprovedParasRatio(){
		float ratio = (float)notApprovedParas / (notApprovedParas + approvedParas);
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		return format.format(ratio * 100) + "%";
	}
	
	/**
	 * 获取已批准文本段的比例
	 * @return	如25.00%
	 */
	public String getApprovedParasRatio(){
		float ratio = (float)approvedParas / (notApprovedParas + approvedParas);
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		return format.format(ratio * 100) + "%";
	}

}
