package net.heartsome.cat.ts.core.bean;

/**
 * 繁殖翻译用到数据存储 pojo 类。
 * @author robert	2012-10-15
 */
public class FuzzyTransDataBean {
	/** 源文，有可能是纯文本，有可能是所有文本 */
	private String srcText;
	/** 译文是否非空，即是否翻译 */
	private boolean isTgtNull;
	/** 是否锁定 */
	private boolean isLock;
	
	
	public FuzzyTransDataBean (){}

	public FuzzyTransDataBean (String srcText, boolean isTgtNull, boolean isLock){
		this.srcText = srcText;
		this.isTgtNull = isTgtNull;
		this.isLock = isLock;
	}

	public String getSrcText() {
		return srcText;
	}

	public void setSrcText(String srcText) {
		this.srcText = srcText;
	}

	public boolean isTgtNull() {
		return isTgtNull;
	}

	public void setTgtNull(boolean isTgtNull) {
		this.isTgtNull = isTgtNull;
	}

	public boolean isLock() {
		return isLock;
	}

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}
	
	
}
