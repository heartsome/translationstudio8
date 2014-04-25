/**
 * TranslationTaskData.java
 *
 * Version information :
 *
 * Date:2013-9-5
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
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TranslationTaskData {

	private Object matcher;
	private TransUnitBean transUnit;
	private TransUnitInfo2TranslationBean tuInfo;
	private IXliffEditor editor;
	private int rowIndex;
	private IProject project;

	/**
	 * @param matcher
	 * @param transUnit
	 * @param tuInfo
	 * @param editor
	 */
	public TranslationTaskData(Object matcher, TransUnitBean transUnit, TransUnitInfo2TranslationBean tuInfo,
			IXliffEditor editor, int rowIndex, IProject project) {
		this.matcher = matcher;
		this.transUnit = transUnit;
		this.tuInfo = tuInfo;
		this.editor = editor;
		this.rowIndex = rowIndex;
		this.project = project;
	}

	/** @return the matcher */
	public Object getMatcher() {
		return matcher;
	}

	/**
	 * @param matcher
	 *            the matcher to set
	 */
	public void setMatcher(Object matcher) {
		this.matcher = matcher;
	}

	/** @return the transUnit */
	public TransUnitBean getTransUnit() {
		return transUnit;
	}

	/**
	 * @param transUnit
	 *            the transUnit to set
	 */
	public void setTransUnit(TransUnitBean transUnit) {
		this.transUnit = transUnit;
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

	/** @return the rowIndex */
	public int getRowIndex() {
		return rowIndex;
	}

	/**
	 * @param rowIndex
	 *            the rowIndex to set
	 */
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	/** @return the project */
	public IProject getProject() {
		return project;
	}

	/**
	 * @param project
	 *            the project to set
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

}
