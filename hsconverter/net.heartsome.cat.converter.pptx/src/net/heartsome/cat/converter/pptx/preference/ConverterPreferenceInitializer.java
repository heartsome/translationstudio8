package net.heartsome.cat.converter.pptx.preference;

import net.heartsome.cat.converter.pptx.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 设置首选项的 PowerPoint 2007 默认值类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ConverterPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(Constants.PPTX_FILTER, false);
	}

}
