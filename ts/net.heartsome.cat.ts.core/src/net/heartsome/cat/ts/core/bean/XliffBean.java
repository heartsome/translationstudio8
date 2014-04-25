package net.heartsome.cat.ts.core.bean;

import java.util.HashSet;
import java.util.Set;

public class XliffBean {

	/**
	 * 文件类型。XLIFF 文件 file 节点 datatype 属性。
	 */
	private String datatype;

	/**
	 * 源语言。XLIFF 文件 file 节点 source-language 属性。
	 */
	private String sourceLanguage;

	/**
	 * 目标语言。XLIFF 文件 file 节点 target-language 属性。
	 */
	private String targetLanguage;

	/**
	 * 源文件。XLIFF 文件 file 节点 original 属性（可以有多个 file 节点）。
	 */
	private Set<String> originals;

	/**
	 * 原始文件。
	 */
	private String sourceFile;

	/**
	 * 所在的 XLIFF 文件的文件名。
	 */
	private String xliffFile;

	/**
	 * 创建 XLIFF 文件对象
	 * @param datatype
	 * @param sourceFile
	 * @param sourceLanguage
	 * @param targetLanguage
	 * @param original
	 */
	public XliffBean(String datatype, String sourceFile, String sourceLanguage, String targetLanguage, String original, String xliffFile) {
		this.datatype = datatype;
		this.sourceFile = sourceFile;
		this.sourceLanguage = sourceLanguage;
		this.targetLanguage = targetLanguage;
		this.xliffFile = xliffFile;
		this.originals = new HashSet<String>();
		originals.add(original);
	}

	/**
	 * 添加 original
	 * @param original ;
	 */
	public void addOriginal(String original) {
		if (originals == null) {
			originals = new HashSet<String>();
		}
		originals.add(original);
	}

	/**
	 * 获取文件类型。
	 * @return XLIFF 文件 file 节点 datatype 属性。;
	 */
	public String getDatatype() {
		return datatype != null ? datatype : "";
	}

	/**
	 * 设置文件类型。
	 * @param datatype
	 *            XLIFF 文件 file 节点 datatype 属性;
	 */
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	/**
	 * 获取源语言。
	 * @return XLIFF 文件 file 节点 source-language 属性。
	 */
	public String getSourceLanguage() {
		return sourceLanguage != null ? sourceLanguage : "";
	}

	/**
	 * 设置源语言。
	 * @param sourceLanguage
	 *            XLIFF 文件 file 节点 source-language 属性。
	 */
	public void setSourceLanguage(String sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
	}

	/**
	 * 获取目标语言。
	 * @return XLIFF 文件 file 节点 target-language 属性。
	 */
	public String getTargetLanguage() {
		return targetLanguage != null ? targetLanguage : "";
	}

	/**
	 * 设置目标语言。
	 * @param targetLanguage
	 *            XLIFF 文件 file 节点 target-language 属性。
	 */
	public void setTargetLanguage(String targetLanguage) {
		this.targetLanguage = targetLanguage;
	}

	/**
	 * 获取原始文件。
	 * @return XLIFF 文件 file 节点 original 属性（可以有多个 file 节点）。
	 */
	public Set<String> getOriginals() {
		return originals != null ? originals : new HashSet<String>();
	}

	/**
	 * 设置原始文件。
	 * @param originals
	 *            XLIFF 文件 file 节点 original 属性（可以有多个 file 节点）。
	 */
	public void setOriginals(Set<String> originals) {
		this.originals = originals;
	}

	/**
	 * 得到源文件。
	 * @return 此 XLIFF 文件对应的源文件;
	 */
	public String getSourceFile() {
		return sourceFile != null ? sourceFile : "";
	}

	/**
	 * 设置源文件
	 * @param sourceFile
	 *            此 XLIFF 文件对应的源文件;
	 */
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	/**
	 * 得到所在的 XLIFF 文件的文件名。
	 * @return 所在的 XLIFF 文件的文件名。
	 */
	public String getXliffFile() {
		return xliffFile;
	}

	/**
	 * 设置所在的 XLIFF 文件的文件名。
	 * @param xliffFile 所在的 XLIFF 文件的文件名。
	 */
	public void setXliffFile(String xliffFile) {
		this.xliffFile = xliffFile;
	}
}
