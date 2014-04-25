package net.heartsome.cat.ts.ui.qa.model;

/**
 * 锁定内部重复时，保存每个文本段的纯文本以及字数。
 * @author robert	2012-10-09
 *
 */
public class LockDataBean {
	private String pureText;
	private int wordCount;
	
	public LockDataBean(){}
	
	public LockDataBean(String pureText, int wordCount){
		this.pureText = pureText;
		this.wordCount = wordCount;
	}

	public String getPureText() {
		return pureText;
	}

	public void setPureText(String pureText) {
		this.pureText = pureText;
	}

	public int getWordCount() {
		return wordCount;
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}
	
	
	

}
