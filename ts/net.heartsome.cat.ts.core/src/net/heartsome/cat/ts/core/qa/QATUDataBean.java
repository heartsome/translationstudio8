package net.heartsome.cat.ts.core.qa;

/**
 * 每一个　tu 的数据　bean
 * @author robert	2013-10-24
 */
public class QATUDataBean {
	private String rowId;
	private String lineNumber;
	private String srcPureText;
	private String tgtPureText;
	private String srcContent;
	private String tgtContent;
	private String fileName;
	private String srcLang;
	private String tgtLang;
	/** 当前处理文件的绝对路径 */
	private String xlfPath;
	/** 默认通过过滤器的过滤 */
	private boolean passFilter = true;

	public QATUDataBean(){
		
		
	}

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getSrcPureText() {
		return srcPureText;
	}

	public void setSrcPureText(String srcPureText) {
		this.srcPureText = srcPureText;
	}

	public String getTgtPureText() {
		return tgtPureText;
	}

	public void setTgtPureText(String tgtPureText) {
		this.tgtPureText = tgtPureText;
	}

	public String getSrcContent() {
		return srcContent;
	}

	public void setSrcContent(String srcContent) {
		this.srcContent = srcContent;
	}

	public String getTgtContent() {
		return tgtContent;
	}

	public void setTgtContent(String tgtContent) {
		this.tgtContent = tgtContent;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isPassFilter() {
		return passFilter;
	}

	public void setPassFilter(boolean passFilter) {
		this.passFilter = passFilter;
	}

	public String getSrcLang() {
		return srcLang;
	}

	public void setSrcLang(String srcLang) {
		this.srcLang = srcLang;
	}

	public String getTgtLang() {
		return tgtLang;
	}

	public void setTgtLang(String tgtLang) {
		this.tgtLang = tgtLang;
	}

	public String getXlfPath() {
		return xlfPath;
	}

	public void setXlfPath(String xlfPath) {
		this.xlfPath = xlfPath;
	}
	
	
	
	
	
	
}
