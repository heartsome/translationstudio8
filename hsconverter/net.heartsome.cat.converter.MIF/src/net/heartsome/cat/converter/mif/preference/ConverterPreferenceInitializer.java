package net.heartsome.cat.converter.mif.preference;

import net.heartsome.cat.converter.mif.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 设置首选项 Adobe FrameMaker 的默认值类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ConverterPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(Constants.FRAMEMAKER_FILTER, false);
	}

}
