package net.heartsome.cat.ts.ui.preferencepage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 首选项的系统项设置默认值的类
 * @author peason
 * @version
 * @since JDK1.6
 */
public class SystemPreferenceInitializer extends AbstractPreferenceInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemPreferenceInitializer.class);

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(IPreferenceConstants.SYSTEM_AUTO_UPDATE, IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_WEEKLY);
		store.setDefault(IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_WEEKLY_DATE, 2);
		store.setDefault(IPreferenceConstants.SYSTEM_LANGUAGE, IPreferenceConstants.SYSTEM_LANGUAGE_WITH_EN);
		// 默认语言从产品的 ini 文件中取值
		Location configArea = Platform.getInstallLocation();
		String locale = "en";

		URL location = null;
		try {
			location = new URL(configArea.getURL().toExternalForm() + "configuration" + File.separator + "config.ini");
		} catch (MalformedURLException e) {
			// This should never happen
			LOGGER.error(Messages.getString("preferencepage.SystemPreferenceInitializer.logger1"), e);
		}

		try {
			String fileName = location.getFile();
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			boolean isNl = false;
			String line = in.readLine();
			while (line != null) {
				if (line.startsWith("osgi.nl=")) {
					isNl = true;
					locale = line.substring("osgi.nl=".length()).trim();
					break;
				}
				line = in.readLine();
			}
			in.close();
			if (!isNl) {
				locale = "en";
			}
		} catch (FileNotFoundException e) {
			LOGGER.error(Messages.getString("preferencepage.SystemPreferenceInitializer.logger1"), e);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		if (locale != null) {
			if (locale.startsWith("en")) {
				CommonFunction.setSystemLanguage("en");
				store.setValue(IPreferenceConstants.SYSTEM_LANGUAGE, IPreferenceConstants.SYSTEM_LANGUAGE_WITH_EN);
			} else if (locale.startsWith("zh")) {
				CommonFunction.setSystemLanguage("zh");
				store.setValue(IPreferenceConstants.SYSTEM_LANGUAGE, IPreferenceConstants.SYSTEM_LANGUAGE_WITH_ZH_CN);
			}
		}
		store.setDefault(IPreferenceConstants.SYSTEM_USER, System.getProperty("user.name"));
		//将用户保存到平台首选项中
		PlatformUI.getPreferenceStore().setDefault(IPreferenceConstants.SYSTEM_USER,
				store.getDefaultString(IPreferenceConstants.SYSTEM_USER));
		FontData fd = JFaceResources.getDefaultFont().getFontData()[0];
		store.setDefault(IPreferenceConstants.XLIFF_EDITOR_FONT_NAME, fd.getName());
		int fontSize = fd.getHeight();
		store.setDefault(IPreferenceConstants.XLIFF_EDITOR_FONT_SIZE, fontSize < 13 ? 13 : fontSize);
		
		store.setDefault(IPreferenceConstants.MATCH_VIEW_FONT_NAME, fd.getName());
		store.setDefault(IPreferenceConstants.MATCH_VIEW_FONT_SIZE, fontSize < 13 ? 13 : fontSize);
		
		store.setDefault(IPreferenceConstants.XLIFF_EDITOR_SHOWHIDEN_NONPRINTCHARACTER, false);
		
	}

}
