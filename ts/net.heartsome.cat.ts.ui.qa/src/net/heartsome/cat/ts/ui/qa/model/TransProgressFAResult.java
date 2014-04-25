package net.heartsome.cat.ts.ui.qa.model;

import java.text.NumberFormat;

/**
 * 文件分析的翻译进度分析结果pojo类
 * @author robert	2011-12-13
 */
public class TransProgressFAResult {
	/** 未翻译文本段数，是指翻译单元无 target 子节点或该节点内容为空的文本段的个数 */
	private int notTransPara = 0;
	/** 已翻译文本段数，是指翻译单元有 target 子节点并且该节点内容不为空的文本段； */
	private int translatedPara = 0;
	/** 未翻译字数，是指翻译单元无 target 子节点或该节点内容为空的文本段的字数； */
	private int notTransWords = 0;
	/** 已经翻译字数，翻译单元有 target 子节点并且该节点内容不为空的文本段的字数； */
	private int translatedWords = 0;
	/** 锁定文本段的段数 */
	private int lockedPara = 0;
	/** 锁定文本段的字数 */
	private int lockedWords = 0;
	
	public TransProgressFAResult(){}
	
	public TransProgressFAResult(int notTransPara, int translatedPara, int lockedPara,
			int notTransWords, int translatedWords, int lockedWords) {
		this.notTransPara = notTransPara;
		this.translatedPara = translatedPara;
		this.lockedPara = lockedPara;
		this.notTransWords = notTransWords;
		this.translatedWords = translatedWords;
		this.lockedWords = lockedWords;
	}
	
	public int getNotTransPara() {
		return notTransPara;
	}
	public int getTranslatedPara() {
		return translatedPara;
	}
	public int getNotTransWords() {
		return notTransWords;
	}
	public int getTranslatedWords() {
		return translatedWords;
	}
	public int getLockedWords() {
		return lockedWords;
	}
	public int getLockedPara() {
		return lockedPara;
	}


	public void setNotTransPara(int notTransPara) {
		this.notTransPara += notTransPara;
	}
	public void setTranslatedPara(int translatedPara) {
		this.translatedPara += translatedPara;
	}
	public void setNotTransWords(int notTransWords) {
		this.notTransWords += notTransWords;
	}
	public void setTranslatedWords(int translatedWords) {
		this.translatedWords += translatedWords;
	}
	public void setLockedWords(int lockedWords) {
		this.lockedWords += lockedWords;
	}
	public void setLockedPara(int lockedPara) {
		this.lockedPara += lockedPara;
	}
	
	/**
	 * 获取所有字数
	 * @return
	 */
	public int getTotalWords(){
		return notTransWords + translatedWords;
	}
	
	/**
	 * 获取未翻译字数的比例
	 * @return	如25.00%
	 */
	public String getNotTransWordsRatio(){
		float ratio = (float)notTransWords / (notTransWords + translatedWords);
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		
		return format.format(ratio * 100) + "%";
	}
	
	/**
	 * 获取已翻译字数的比例
	 * @return	如25.00%
	 */
	public String getTransWordsRatio(){
		float ratio = (float)translatedWords / (notTransWords + translatedWords);
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		return format.format(ratio * 100) + "%";
	}
	
	/**
	 * 获取所有文本段
	 * @return
	 */
	public int getTotalParas(){
		return notTransPara + translatedPara;
	}
	
	/**
	 * 获取未翻译文本段的比例
	 * @return	如25.00%
	 */
	public String getNotTransParasRatio(){
		float ratio = (float)notTransPara / (notTransPara + translatedPara);
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		return format.format(ratio * 100) + "%";
	}
	
	/**
	 * 获取已翻译文本段的比例
	 * @return	如25.00%
	 */
	public String getTransParasRatio(){
		float ratio = (float)translatedPara / (notTransPara + translatedPara);
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		return format.format(ratio * 100) + "%";
	}
}
