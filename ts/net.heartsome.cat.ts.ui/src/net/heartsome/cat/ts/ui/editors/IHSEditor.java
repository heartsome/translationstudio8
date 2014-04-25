/**
 * IHSEEditor.java
 *
 * Version information :
 *
 * Date:Jan 27, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.ts.ui.editors;

import net.heartsome.cat.ts.core.bean.TransUnitBean;

/**
 * .
 * @author stone
 * @version
 * @since JDK1.6
 */
public interface IHSEditor {

	/**
	 * 通知Editor，将选中翻译单元的翻译修改为value。.
	 * @param value
	 *            the value
	 */
	void changeData(String value);

	/**
	 * 获取选中的TransUnitBean，没有选中返回null.
	 * @return the select trans unit bean
	 */
	TransUnitBean getSelectTransUnitBean();

	/**
	 * 修改编辑器的源文本和翻译排列的方式。
	 */
	void changeModel();

	/**
	 * 设置编辑器的源文本和翻译排列的方式。
	 * @param layoutModel
	 */
	void setModel(int layoutModel);

}
