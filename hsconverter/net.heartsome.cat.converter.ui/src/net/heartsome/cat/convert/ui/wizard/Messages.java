package net.heartsome.cat.convert.ui.wizard;

/*
 * Created on Jul 6, 2004
 *
 */

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Rodolfo M. Raya Copyright (c) 2004 Heartsome Holdings Pte Ltd http://www.heartsome.net
 */
public final class Messages {
	private static final String BUNDLE_NAME = "net.heartsome.cat.convert.ui.wizard.conversion_new"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * 私有构造函数
	 */
	private Messages() {
		// private constructor
	}

	/**
	 * 根据 key，获得相应的 value 值
	 * @param key
	 * @return ;
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}