package net.heartsome.cat.ts.core.bean;

import net.heartsome.xml.vtdimpl.VTDUtils;

/**
 * 批注对象。参见 XLIFF v1.2 标准中的 note 节点。
 * 
 * */
public class NoteBean implements IXMLBean {

	public static final String ANNOTATES_SOURCE = "source";
	public static final String ANNOTATES_TARGET = "target";
	public static final String ANNOTATES_GENERAL = "general";

	public static final String PRIORITY_MAX = "10";
	public static final String PRIORITY_DEFALUE = "1";

	/**
	 * 批注从属的节点。如 source, target 或是 general。 general 为默认值。
	 * */
	private String annotates;

	/**
	 * 优先级。未设置则为 null。最小为 1，最大为 10。
	 * */
	private String priority;

	/**
	 * 批注来源。如 translator，翻译或 John。
	 * 
	 * */
	private String from;

	/**
	 * 批注语言。
	 * */
	private String lang;

	/**
	 * 批注文本。
	 * */
	private String noteText;
	
	/**
	 * 应用当前文本段还是所有句段，值为"Yes" 时表示应用当前文本段；"No"表示应用所有文本段
	 */
	private String applyCurrent;

	/**
	 * 获取该批注的从属节点属性。
	 * */
	public String getAnnotates() {
		return annotates;
	}

	/**
	 * 设置该批注的从属节点值，如非定义的常量值，则取默认值。
	 * 
	 * @param annotates
	 *            从属节点。
	 * */
	public void setAnnotates(String annotates) {
		if (ANNOTATES_GENERAL.equalsIgnoreCase(annotates)
				|| ANNOTATES_SOURCE.equalsIgnoreCase(annotates)
				|| ANNOTATES_TARGET.equalsIgnoreCase(annotates)) {
			this.annotates = NoteBean.ANNOTATES_GENERAL;
		} else {
			this.annotates = annotates;
		}
	}

	/**
	 * 获取该批注的优先级。如未定义则为 null。
	 * */
	public String getPriority() {
		return priority;
	}

	/**
	 * 设置该批注的优先级，取值范围为 1 至 10，超过最大值时取 10。
	 * 
	 * @param iPriority
	 *            优先级。
	 * */
	public void setPriority(String priority) {
		try {
			int iPriority = Integer.parseInt(priority);

			if (iPriority > 10) {
				iPriority = 10;
			}

			if (iPriority < 1) {
				iPriority = 1;
			}
			this.priority = String.valueOf(iPriority);
		} catch (NumberFormatException nfe) {
			this.priority = NoteBean.PRIORITY_DEFALUE;
		}
	}

	/**
	 * 获取批注来源。
	 * 
	 * */
	public String getFrom() {
		return from;
	}

	/**
	 * 设置批注来源。如未定义则为 null。
	 * 
	 * */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * 获取该批注的语言。如未定义则为 null。
	 * */
	public String getLang() {
		return lang;
	}

	/**
	 * 设置该批注的语言。
	 * */
	public void setLang(String lang) {
		this.lang = lang;
	}

	/**
	 * 获取该批注的文本。无批注文本则返回空字符串。
	 * */
	public String getNoteText() {
		return noteText;
	}

	/**
	 * 设置该批注的文本。如为 null 则设置为空字符串。
	 * 
	 * @param noteText
	 *            批注文本。
	 * */
	public void setNoteText(String noteText) {
		if (noteText == null) {
			this.noteText = "";
		} else {
			this.noteText = noteText;
		}
	}

	/**
	 * 构建一个指定文本的批注。如为 null 则构建一个文本为空字符串的批注对象。
	 * */
	public NoteBean(String noteText) {
		if (noteText == null) {
			noteText = "";
		} else {
			this.noteText = noteText;
		}
	}

	public String toXMLString() {
		return VTDUtils.getNodeXML("note", noteText, null);
	}

	public String getApplyCurrent() {
		return applyCurrent;
	}

	public void setApplyCurrent(String applyCurrent) {
		this.applyCurrent = applyCurrent;
	}

}
