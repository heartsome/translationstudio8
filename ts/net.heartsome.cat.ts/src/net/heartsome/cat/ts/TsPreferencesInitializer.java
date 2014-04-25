package net.heartsome.cat.ts;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * TS系统级别参数初始化<br>
 * 用于初始化系统级别的参数（是否显示状态栏）
 *  
 * robert	2012-03-20
 */
public class TsPreferencesInitializer extends AbstractPreferenceInitializer {

	public TsPreferencesInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		preferenceStore.setDefault(TsPreferencesConstant.TS_statusBar_status, true);
	}

}
