package net.heartsome.cat.database.ui.resource;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 国际化工具类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public final class Messages {

	/** The Constant BUNDLE_NAME. */
	private static final String BUNDLE_NAME = "net.heartsome.cat.database.ui.resource.message"; //$NON-NLS-1$

	/** The Constant RESOURCE_BUNDLE. */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Instantiates a new messages.
	 */
	private Messages() {
		// Do nothing
	}

	/**
	 * Gets the string.
	 * @param key
	 *            the key
	 * @return the string
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
