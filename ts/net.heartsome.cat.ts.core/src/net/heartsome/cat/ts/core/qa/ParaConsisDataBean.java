package net.heartsome.cat.ts.core.qa;

/**
 * 品质检查 文件段一致性检查
 * @author robert
 */
public class ParaConsisDataBean {
	/** 行号 */
	private int lineNumber;
	/** 源文的纯文本 */
	private String srcPureText;
	/** 源文的所有文本 */
	private String srcContent;
	/** 译文的纯文本 */
	private String tgtPureText;
	/** 译文的所有文本 */
	private String tgtContent;
	
	public ParaConsisDataBean (){}
	
	public ParaConsisDataBean (int lineNumber, String srcPureText, String srcContent, String tgtPureText, String tgtContent){
		this.lineNumber = lineNumber;
		this.srcPureText = srcPureText;
		this.srcContent = srcContent;
		this.tgtContent = tgtContent;
		this.tgtPureText = tgtPureText;
	}


	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
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

	public String getTgtPureText() {
		return tgtPureText;
	}

	public void setTgtPureText(String tgtPureText) {
		this.tgtPureText = tgtPureText;
	}

	public String getTgtContent() {
		return tgtContent;
	}

	public void setTgtContent(String tgtContent) {
		this.tgtContent = tgtContent;
	}
	
}
