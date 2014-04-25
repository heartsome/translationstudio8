package net.heartsome.cat.ts.core.qa;

/**
 * 字数分析所用到的数据pojo类
 * @author  robert	2012-04-12
 * @version 
 * @since   JDK1.6
 */
public class WordsFABean {
	private String srcPureText;
	private String srcContent;
	/** 把所有的标记进行拼起来 */
	private String tagStr;
	private String preHash;
	private String nextHash;
	private int srcLength;
	private boolean isLocked;
	/** 跟所比较过的文本段后最大的匹配率 */
	private int thisMatchRate = 0;
	
	public WordsFABean() {
	}

	public WordsFABean(String srcPureText, String srcContent, String tagStr, String preHash, String nextHash, int srcLength, boolean isLocked) {
		this.srcPureText = srcPureText;
		this.srcContent = srcContent;
		this.tagStr = tagStr;
		this.preHash = preHash;
		this.nextHash = nextHash;
		this.srcLength = srcLength;
		this.isLocked = isLocked;
	}

	public String getSrcPureText() {
		return srcPureText;
	}

	public void setSrcPureText(String srcPureText) {
		this.srcPureText = srcPureText;
	}

	public String getSrcContent() {
		return srcContent;
	}

	public void setSrcContent(String srcContent) {
		this.srcContent = srcContent;
	}
	
	public String getTagStr() {
		return tagStr;
	}

	public void setTagStr(String tagStr) {
		this.tagStr = tagStr;
	}

	public String getPreHash() {
		return preHash;
	}

	public void setPreHash(String preHash) {
		this.preHash = preHash;
	}

	public String getNextHash() {
		return nextHash;
	}

	public void setNextHash(String nextHash) {
		this.nextHash = nextHash;
	}

	public int getSrcLength() {
		return srcLength;
	}

	public void setSrcLength(int srcLength) {
		this.srcLength = srcLength;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public int getThisMatchRate() {
		return thisMatchRate;
	}

	public void setThisMatchRate(int thisMatchRate) {
		if (thisMatchRate > this.thisMatchRate) {
			this.thisMatchRate = thisMatchRate;
		}
	}
}
