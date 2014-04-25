/**
 * TBDatabaseInitializer.java
 *
 * Version information :
 *
 * Date:2012-5-2
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.tb.preference;

import net.heartsome.cat.database.bean.TBPreferenceConstants;
import net.heartsome.cat.database.ui.tb.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;

/**
 * @author  jason
 * @version 
 * @since   JDK1.6
 */
public class TBDatabaseInitializer extends AbstractPreferenceInitializer {

	/** (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		//		设置术语库默认更新策略
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(TBPreferenceConstants.TB_UPDATE, TBPreferenceConstants.TB_REPEAT_OVERWRITE);
		
		
		// 初始化新建向导记住信息
		store.setDefault(TBPreferenceConstants.TB_RM_DBTYPE, "");
		store.setDefault(TBPreferenceConstants.TB_RM_INSTANCE, "");
		store.setDefault(TBPreferenceConstants.TB_RM_SERVER, "");
		store.setDefault(TBPreferenceConstants.TB_RM_PORT, "");
		store.setDefault(TBPreferenceConstants.TB_RM_PATH, "");
		store.setDefault(TBPreferenceConstants.TB_RM_USERNAME, "");
		store.setDefault(TBPreferenceConstants.TB_CASE_SENSITIVE, true);
		PlatformUI.getPreferenceStore().setDefault(TBPreferenceConstants.TB_CASE_SENSITIVE,true);
	}

}
