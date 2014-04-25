package net.heartsome.cat.ts.fuzzytranslation.resource;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 国际化工具类
 * @author  robert	2012-08-29
 * @version 
 * @since   JDK1.6
 */
public class Messages {
	
	private static final String BUNDLE_NAME = "net.heartsome.cat.ts.fuzzytranslation.resource.fuzzyTrans";

	private static ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	public static String getString(String key) {
		try {
			return BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
}
