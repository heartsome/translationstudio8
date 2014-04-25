/**
 * XliffEditorParameter.java
 *
 * Version information :
 *
 * Date:2013-4-22
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.bean;

import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class XliffEditorParameter implements IPropertyChangeListener {

	private boolean isShowNonpirnttingCharacter;

	private IPreferenceStore ps;
	private static XliffEditorParameter instance;

	public static XliffEditorParameter getInstance() {
		if (instance == null) {
			instance = new XliffEditorParameter();
		}
		return instance;
	}

	private XliffEditorParameter() {
		Activator ac = Activator.getDefault();
		if (ac != null) {
			ps = Activator.getDefault().getPreferenceStore();
			if (ps != null) {
				ps.addPropertyChangeListener(this);
				loadPreferenceValues();
			}
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (ps != null) {
			loadPreferenceValues();
		}
	}

	private void loadPreferenceValues() {
		isShowNonpirnttingCharacter = ps.getBoolean(IPreferenceConstants.XLIFF_EDITOR_SHOWHIDEN_NONPRINTCHARACTER);
	}

	public boolean isShowNonpirnttingCharacter() {
		return isShowNonpirnttingCharacter;
	}

	public void setShowNonpirnttingCharacter(boolean isShowNonpirnttingCharacter) {
		this.isShowNonpirnttingCharacter = isShowNonpirnttingCharacter;
	}

}
