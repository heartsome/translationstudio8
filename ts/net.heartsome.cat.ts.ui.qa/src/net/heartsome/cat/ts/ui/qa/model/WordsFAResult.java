package net.heartsome.cat.ts.ui.qa.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.ts.core.qa.QAConstant;

public class WordsFAResult {
	/** 新文本段 */
	private int newPara = 0;	
	/** 内部重复文本段 */
	private int interRepeatPara = 0;	
	/** 内部匹配文本段 */
	private int interMatchPara = 0;	
	/** 外部重复文本段  */
	private int exterRepeatPara = 0;	
	/** 外部匹配文本段 */
	private int exterMatchPara = 0;		
	
	private int lockedPara = 0;
	
	/** 新字数 */
	private int newWords = 0;
	/** 内部匹配字数 */
	private int interMatchWords = 0;
	/** 外部匹配字数 */
	private int exterMatchWords = 0;
	/** 所有外部匹配字数, key=匹配率, value=字数，针对结果显示界面的第三张表，也就是存储每一个源节点的外部匹配率 */
	private Map<Integer, Integer> allExterMatchWords = new HashMap<Integer, Integer>();
	/** 所有内部匹配字数, key=匹配率, value=字数，针对结果显示界面的第三张表，也就是存储每一个源节点的外部匹配率 */
	private Map<Integer, Integer> allInterMatchWords = new HashMap<Integer, Integer>();
	/** 锁定文本段的字数 */
	private int lockedWords = 0;
	
	public int getNewPara() {
		return newPara;
	}
	public int getInterRepeatPara() {
		return interRepeatPara;
	}
	public int getInterMatchPara() {
		return interMatchPara;
	}
	public int getExterRepeatPara() {
		return exterRepeatPara;
	}
	public int getExterMatchPara() {
		return exterMatchPara;
	}
	
	public int getNewWords() {
		return newWords;
	}
	public int getInterMatchWords() {
		return interMatchWords;
	}
	public int getExterMatchWords() {
		return exterMatchWords;
	}
	public int getLockedWords() {
		return lockedWords;
	}
	public int getLockedPara() {
		return lockedPara;
	}
	
	
	public void setNewPara(int newPara) {
		this.newPara += newPara;
	}
	public void setInterRepeatPara(int interRepeatPara) {
		this.interRepeatPara += interRepeatPara;
	}
	public void setInterMatchPara(int interMatchPara) {
		this.interMatchPara += interMatchPara;
	}
	public void setExterRepeatPara(int exterRepeatPara) {
		this.exterRepeatPara += exterRepeatPara;
	}
	public void setExterMatchPara(int exterMatchPara) {
		this.exterMatchPara += exterMatchPara;
	}
	
	public void setNewWords(int newWords) {
		this.newWords += newWords;
	}
	public void setInterMatchWords(int interMatchWords) {
		this.interMatchWords += interMatchWords;
	}
	public void setExterMatchWords(int exterMatchWords) {
		this.exterMatchWords += exterMatchWords;
	}
	public void setLockedWords(int lockedWords) {
		this.lockedWords += lockedWords;
	}
	public void setLockedPara(int lockedPara) {
		this.lockedPara += lockedPara;
	}
	
	
	
	public Map<Integer, Integer> getAllExterMatchWords() {
		return allExterMatchWords;
	}
	/** 给外部匹配结果传值 */
	public void setAllExterMatchWords(int matchRate, int allExterMatchWords) {
		if (this.allExterMatchWords.containsKey(matchRate)) {
			int oldPara = this.allExterMatchWords.get(matchRate);
			this.allExterMatchWords.put(matchRate, allExterMatchWords + oldPara);
		}else {
			this.allExterMatchWords.put(matchRate, allExterMatchWords);
		}
	}	
	/** 给外部匹配结果传值 */
	public void setAllExterMatchWords(Map<Integer, Integer> allExterMathWords){
		Iterator<Entry<Integer, Integer>> it = allExterMathWords.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer, Integer> entry = it.next();
			int matchRate = entry.getKey();
			int words = entry.getValue();
			setAllExterMatchWords(matchRate, words);
		}
	}
	
	public Map<Integer, Integer> getAllInterMatchWords() {
		return allInterMatchWords;
	}
	/** 给内部匹配结果传值 */
	public void setAllInterMatchWords(int matchRate, int allInterMatchWords) {
		if (this.allInterMatchWords.containsKey(matchRate)) {
			int oldPara = this.allInterMatchWords.get(matchRate);
			this.allInterMatchWords.put(matchRate, allInterMatchWords + oldPara);
		}else {
			this.allInterMatchWords.put(matchRate, allInterMatchWords);
		}
	}
	/** 给内部匹配结果传值 */
	public void setAllInterMatchWords(Map<Integer, Integer> allInterMatchWords){
		Iterator<Entry<Integer, Integer>> it = allInterMatchWords.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer, Integer> entry = it.next();
			int matchRate = entry.getKey();
			int words = entry.getValue();
			setAllInterMatchWords(matchRate, words);
		}
	}
	
	/**
	 * 获取字数分析的所有字数，为新字数，内部匹配字数，外部匹配字数，锁定字数之和
	 * @return
	 */
	public int getTotalWords(){
		return newWords + interMatchWords + exterMatchWords + lockedWords;
	}
	
	/**
	 * 获取字数分析的所有文本段数，为新文本段，内部匹配文本段，外部匹配文本段，内部重复文本段，外部重复文本段，锁定文本段之和
	 * @return
	 */
	public int getTotalPara(){
		return newPara + interMatchPara + exterMatchPara + interRepeatPara + exterRepeatPara + lockedPara;
	}
	
	
	/**
	 * 获取所有外部匹配区间段的总数字
	 * @param matchPair	匹配率段，如95－99,也可能传值为100或101,
	 * @return
	 */
	public int getExterMatch(String matchPair){
		if (matchPair.indexOf("-") < 0) {
			return allExterMatchWords.get(Integer.parseInt(matchPair)) == null ? 0 : allExterMatchWords.get(Integer.parseInt(matchPair));
		}
		
		int minMatch = Integer.parseInt(matchPair.substring(0, matchPair.indexOf("-")));
		int maxMatch = Integer.parseInt(matchPair.substring(matchPair.indexOf("-") + 1, matchPair.length()));
		
		int totalWords = 0;
		Iterator<Entry<Integer, Integer>> it = allExterMatchWords.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer, Integer> entry = it.next();
			int matchRate = entry.getKey();
			if (matchRate >= minMatch && matchRate <= maxMatch) {
				totalWords += entry.getValue();
			}
		}
		return totalWords;
	}
	
	/**
	 * 获取所有内部匹配区间段的总数字
	 * @param matchPair	匹配率段，如84-94, 或 100, 101
	 * @return
	 */
	public int getInterMatch(String matchPair){
		if (matchPair.indexOf("-") < 0) {
			return allInterMatchWords.get(Integer.parseInt(matchPair)) == null ? 0 : allInterMatchWords.get(Integer.parseInt(matchPair));
		}
		
		int minMatch = Integer.parseInt(matchPair.substring(0, matchPair.indexOf("-")));
		int maxMatch = Integer.parseInt(matchPair.substring(matchPair.indexOf("-") + 1, matchPair.length()));
		
		int totalWords = 0;
		Iterator<Entry<Integer, Integer>> it = allInterMatchWords.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer, Integer> entry = it.next();
			int matchRate = entry.getKey();
			if (matchRate >= minMatch && matchRate <= maxMatch) {
				totalWords += entry.getValue();
			}
		}
		return totalWords;
	}
	
	/**
	 * 获取加权字数
	 * @param eqalStr
	 *            加权系数的组合字符串，如："internalRepeat:0.50;external101:0.50;external100:0.50;95-99:0.60;85-94:0.70"
	 * @return ;
	 */
	public int getEqalWords(String equivStr) {
		float eqalWords = 0;	//加权系数
		int matchWords = 0;
		int totalWords = 0;
		String[] equivArray = equivStr.split(";");
		for (int i = 0; i < equivArray.length; i++) {
			String matchPair = equivArray[i].substring(0, equivArray[i].indexOf(":") );
			float equiv = Float.parseFloat(equivArray[i].substring(equivArray[i].indexOf(":") + 1 , equivArray[i].length()));
			int interMathWords = 0;
			int exterMathWords = 0;
			// 这是内部重复
			if (QAConstant._InternalRepeat.equals(matchPair)) {
				interMathWords = getInterMatch("100-101");
			}else if (QAConstant._External101.equals(matchPair)) {
				exterMathWords = getExterMatch("101");
			}else if (QAConstant._External100.equals(matchPair)) {
				exterMathWords = getExterMatch("100");
			}else {
				interMathWords = getInterMatch(matchPair);
				exterMathWords = getExterMatch(matchPair);
			}
			eqalWords += interMathWords * equiv;
			eqalWords += exterMathWords * equiv;
			matchWords += (interMathWords + exterMathWords);
		}
		//加权总字数等于内部与外部匹配的加权字数，与无匹配区间的匹配值，与新字数的和
		float result = newWords + (interMatchWords + exterMatchWords - matchWords) + eqalWords;
		totalWords = new BigDecimal(result).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
		return totalWords;
	}

}
