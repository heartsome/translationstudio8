/**
 * MatchData.java
 *
 * Version information :
 *
 * Date:2013-4-24
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.translation.view;

import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.tm.bean.TransUnitInfo2TranslationBean;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;

import org.eclipse.core.resources.IProject;

/**
 * 此封装用于存储需要在库中查询的翻译单元，当编辑器选择事件发生后，将数据存储在这里，等待匹配线程获取并从库中查询匹配
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class MatchData {
	/** 当前需要匹配的翻译单元 */
	private TransUnitBean transUnit;
	/** translation unit 所有在项目 */
	private IProject project;
	/** 在编辑器中对应的 RowId */
	private String rowId;

	/** 当前编辑器 */
	private IXliffEditor editor;
	/** TU INFO for matcher */
	private TransUnitInfo2TranslationBean tuInfo;

	public MatchData(TransUnitBean transUnit, TransUnitInfo2TranslationBean tuInfo, IProject project, String rowId,
			IXliffEditor editor) {
		this.transUnit = transUnit;
		this.tuInfo = tuInfo;
		this.project = project;
		this.rowId = rowId;
		this.editor = editor;
	}

	public TransUnitBean getTransUnit() {
		return transUnit;
	}

	public void setTransUnit(TransUnitBean transUnit) {
		this.transUnit = transUnit;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	/** @return the editor */
	public IXliffEditor getEditor() {
		return editor;
	}

	/**
	 * @param editor
	 *            the editor to set
	 */
	public void setEditor(IXliffEditor editor) {
		this.editor = editor;
	}

	/** @return the tuInfo */
	public TransUnitInfo2TranslationBean getTuInfo() {
		return tuInfo;
	}

	/**
	 * @param tuInfo
	 *            the tuInfo to set
	 */
	public void setTuInfo(TransUnitInfo2TranslationBean tuInfo) {
		this.tuInfo = tuInfo;
	}

}
