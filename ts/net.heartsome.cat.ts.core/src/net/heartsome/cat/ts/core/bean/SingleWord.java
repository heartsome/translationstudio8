package net.heartsome.cat.ts.core.bean;

/**
 * 用于实时拼写检查的 pojo 类，保存单个单词（包括标记在内），以及在文本段中的起始位置
 * @author robert	2012-01-21
 */
public class SingleWord {
	/** 带标记的单词 */
	private String word;
	/** 不带标记的纯单词 */
	private String pureWord;
	/** 该单词在文本中的起始位置 */
	private int start;
	/** 该单词的长度（包括标记） */
	private int length;

	
	public SingleWord(){
		
	}
	
	public SingleWord(String word, String pureWord, int start, int length){
		this.word = word;
		this.pureWord = pureWord;
		this.start = start;
		this.length = length;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getPureWord() {
		return pureWord;
	}

	public void setPureWord(String pureWord) {
		this.pureWord = pureWord;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}


	/**
	 * 将传入的纯文本单词与当前 pojo 类的纯文本单词相比较。看是否想等
	 * @param pureWord
	 * @return
	 */
	public boolean equalPureText(String pureWord){
		if (this.pureWord.equals(pureWord)) {
			return true;
		}
		return false;
	}
	
	
}
