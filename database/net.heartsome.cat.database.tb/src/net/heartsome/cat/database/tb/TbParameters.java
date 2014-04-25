/**
 * TBParameters.java
 *
 * Version information :
 *
 * Date:2012-5-7
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.tb;

import net.heartsome.cat.database.bean.TBPreferenceConstants;
import net.heartsome.cat.database.ui.tb.Activator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TbParameters implements IPropertyChangeListener {

	private IPreferenceStore ps;
	private int tbUpdateStrategy = 0;

	private static TbParameters instance;

	public static TbParameters getInstance() {
		if (instance == null) {
			instance = new TbParameters();
		}
		return instance;
	}

	private TbParameters() {
		ps = Activator.getDefault().getPreferenceStore();
		ps.addPropertyChangeListener(this);
		loadPerference();
	}

	public void propertyChange(PropertyChangeEvent event) {
		loadPerference();
	}

	private void loadPerference() {
		tbUpdateStrategy = ps.getInt(TBPreferenceConstants.TB_UPDATE);
	}

	/** @return the tbUpdateStrategy */
	public int getTbUpdateStrategy() {
		return tbUpdateStrategy;
	}

}
