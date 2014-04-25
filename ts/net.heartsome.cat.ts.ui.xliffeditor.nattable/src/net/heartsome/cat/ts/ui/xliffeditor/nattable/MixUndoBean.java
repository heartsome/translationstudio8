/**
 * MixUndoBean.java
 *
 * Version information :
 *
 * Date:2013-5-15
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.xliffeditor.nattable;

/**
 * 由于原来的版本中有两种模式，显示模式、编辑模式，造成了每种模式使用独立的方案。后来版本中<br>
 * 取消了显示模式，要求在编辑模式中就能撤销用户做过的所有更改，此类用于联合两种撤销模式，主<br>
 * 要作用是缓存重做过程信息。
 * @author austen
 * @version 8.2.3
 */
public class MixUndoBean {
	
	/**
	 * 未保存的单元格 row
	 */
	private int unSaveRow;
	
	/**
	 * 未保存的单元格内容
	 */
	private String unSaveText;
	
	
	/**
	 * 跨越焦点的重做的步数，默认为 0
	 */
	private int crosseStep = 0;

	/** 没有全部保存情况下重做
	 *  0 未初始化
	 *  1 未保存
	 * -1 已保存
	 * */
	private int saveStatus = 0;
	
	public int getUnSaveRow() {
		return unSaveRow;
	}


	public void setUnSaveRow(int unSaveRow) {
		this.unSaveRow = unSaveRow;
	}


	public String getUnSaveText() {
		return unSaveText;
	}


	public void setUnSaveText(String unSaveText) {
		this.unSaveText = unSaveText;
	}


	public int getCrosseStep() {
		return crosseStep;
	}


	public void setCrosseStep(int crosseStep) {
		this.crosseStep = crosseStep;
	}


	public int getSaveStatus() {
		return saveStatus;
	}


	public void setSaveStatus(int saveStatus) {
		this.saveStatus = saveStatus;
	}

	
}
