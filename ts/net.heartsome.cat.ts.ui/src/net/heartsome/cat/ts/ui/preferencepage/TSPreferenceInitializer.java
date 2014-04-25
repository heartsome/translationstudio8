package net.heartsome.cat.ts.ui.preferencepage;

import net.heartsome.cat.common.core.CoreActivator;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.bean.ColorConfigLoader;
import net.heartsome.cat.ts.ui.bean.IColorPreferenceConstant;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * 设置 TS 应用中相关首选项页的初始值
 * @author cheney
 * @since JDK1.6
 */
public class TSPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		// 设置 colors 首选项页的初始值
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.TAG_FG_COLOR, new RGB(234, 234, 234));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.TAG_BG_COLOR, new RGB(223, 112, 0));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.WRONG_TAG_COLOR, new RGB(255, 0, 0));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.DIFFERENCE_FG_COLOR, new RGB(255, 0, 0));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.DIFFERENCE_BG_COLOR, new RGB(244, 244, 159));

		PreferenceConverter.setDefault(store, IColorPreferenceConstant.PT_COLOR, new RGB(255, 0, 0));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.QT_COLOR, new RGB(255, 204, 204));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.MT_COLOR, new RGB(171, 217, 198));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.TM_MATCH101_COLOR, new RGB(255, 255, 204));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.TM_MATCH100_COLOR, new RGB(37, 168, 204));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.TM_MATCH90_COLOR, new RGB(79, 185, 214));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.TM_MATCH80_COLOR, new RGB(114, 199, 222));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.TM_MATCH70_COLOR, new RGB(155, 215, 231));
		PreferenceConverter.setDefault(store, IColorPreferenceConstant.TM_MATCH0_COLOR, new RGB(198, 240, 251));

		PreferenceConverter.setDefault(store, IColorPreferenceConstant.HIGHLIGHTED_TERM_COLOR, new RGB(170, 255, 85));

		// 设置 net.heartsome.cat.common.core 插件中的语言代码初始值
		IPreferenceStore corePreferenceStore = new ScopedPreferenceStore(ConfigurationScope.INSTANCE, CoreActivator
				.getDefault().getBundle().getSymbolicName());
		corePreferenceStore.setDefault(IPreferenceConstants.LANGUAGECODE, LocaleService.getLanguageConfigAsString());

		// 设置选择路径对话框的初始值
		PlatformUI.getPreferenceStore()
				.setDefault(IPreferenceConstants.LAST_DIRECTORY, System.getProperty("user.home"));
		
		ColorConfigLoader.init();
	}

}
