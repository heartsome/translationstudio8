package net.heartsome.cat.ts.ui.qa.resource;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "net.heartsome.cat.ts.ui.qa.resource.qa"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {
		// Do nothing
	}

	public static String getString(String key) {
		try {
			if (RESOURCE_BUNDLE.getString(key) == null || "".equals(RESOURCE_BUNDLE.getString(key).trim())) {
				System.out.println("---错误：没有赋值=" + key);
			}
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
}
