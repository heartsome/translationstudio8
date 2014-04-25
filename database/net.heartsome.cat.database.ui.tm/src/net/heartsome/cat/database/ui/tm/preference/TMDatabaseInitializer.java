package net.heartsome.cat.database.ui.tm.preference;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.database.bean.TMPreferenceConstants;
import net.heartsome.cat.database.ui.tm.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 对记忆库，术语库设置默认值常量
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class TMDatabaseInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(TMPreferenceConstants.CASE_SENSITIVE, false);
		store.setDefault(TMPreferenceConstants.IGNORE_MARK, true);
		if (CommonFunction.checkEdition("U")) {
			store.setDefault(TMPreferenceConstants.CONTEXT_MATCH, 1);
		} else {
			store.setDefault(TMPreferenceConstants.CONTEXT_MATCH, 0);
		}
		store.setDefault(TMPreferenceConstants.MAX_MATCH_NUMBER, 5);
		store.setDefault(TMPreferenceConstants.TAG_PENALTY, 2); //标记罚分
		
		store.setDefault(TMPreferenceConstants.MIN_MATCH, "70");
		
		store.setDefault(TMPreferenceConstants.MATCH_PERCENTAGE_SORT_WITH_EQUAL, TMPreferenceConstants.DEFAULT_DB_PRECEDENCE);
		store.setDefault(TMPreferenceConstants.TM_UPDATE, TMPreferenceConstants.TM_ALWAYS_ADD);
		
		// 初始化新建向导记住信息
		store.setDefault(TMPreferenceConstants.TM_RM_DBTYPE, "");
		store.setDefault(TMPreferenceConstants.TM_RM_INSTANCE, "");
		store.setDefault(TMPreferenceConstants.TM_RM_SERVER, "");
		store.setDefault(TMPreferenceConstants.TM_RM_PORT, "");
		store.setDefault(TMPreferenceConstants.TM_RM_PATH, "");
		store.setDefault(TMPreferenceConstants.TM_RM_USERNAME, "");
	}

}
