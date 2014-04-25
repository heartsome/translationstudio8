package net.heartsome.cat.ts.ui.qa.model;

/**
 * 品质检查结果pojo类
 * @author  robert	2012-04-20
 * @version 
 * @since   JDK1.6
 */
public class QAResultBean {
	/** 品质检查级别，若为0，则为错误，若为1，则为警告 */
	private int level;
	/** 品质检查类型 取值为 QA_TERM, QA_PARAGRAPH 等等 */
	private String qaType;
	private String qaTypeText;
	/** 合并的id ,若他们 id 相同，则表示可以进行合并 */
	private String mergeId;
	private String fileName;
	/** 错误行号 */
	private String lineNumber;
	private String srcContent;
	private String tgtContent;
	private String rowId;
	/** 品质检查级别，若为0，则为错误，若为1，则为警告 */
	/** 是否是自动拼写检查 */
	private boolean isAutoQA = false;
	
	
	public QAResultBean (){
	}
	
	public QAResultBean(int level, String qaType, String qaTypeText,
			String mergeId, String fileName, String lineNumber,
			String srcContent, String tgtContent, String rowId) {
		
		this.level = level;
		this.qaType = qaType;
		this.qaTypeText = qaTypeText;
		this.mergeId = mergeId;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.srcContent = srcContent;
		this.tgtContent = tgtContent;
		this.rowId = rowId;
	}


	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getQaType() {
		return qaType;
	}

	public void setQaType(String qaType) {
		this.qaType = qaType;
	}

	public String getQaTypeText() {
		return qaTypeText;
	}

	public void setQaTypeText(String qaTypeText) {
		this.qaTypeText = qaTypeText;
	}

	public String getMergeId() {
		return mergeId;
	}

	public void setMergeId(String mergeId) {
		this.mergeId = mergeId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
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

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public boolean isAutoQA() {
		return isAutoQA;
	}

	public void setAutoQA(boolean isAutoQA) {
		this.isAutoQA = isAutoQA;
	}
	

}
