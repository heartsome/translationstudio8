package net.heartsome.cat.ts.ui.preferencepage.translation;

import net.heartsome.cat.ts.ui.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 为首选项的翻译/预翻译设置默认值的类
 * @author peason
 * @version
 * @since JDK1.6
 */
public class TranslationInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(ITranslationPreferenceConstants.AUTO_ADAPT_SPACE_POSITION, false);
		store.setDefault(ITranslationPreferenceConstants.COPY_SOURCE_TO_TARGET, false);
		store.setDefault(ITranslationPreferenceConstants.AUTO_QUICK_TRANSLATION, true);
		store.setDefault(ITranslationPreferenceConstants.AUTO_APPLY_TM_MATCH, false);
		store.setDefault(ITranslationPreferenceConstants.SKIP_NOT_TRANSLATE_TEXT, true);

		store.setDefault(ITranslationPreferenceConstants.PATH_OF_OPENOFFICE, "");
		store.setDefault(ITranslationPreferenceConstants.PORT_OF_OPENOFFICE, 9000);
		store.setDefault(ITranslationPreferenceConstants.ENABLED_OF_OPENOFFICE, false);

	}

}
