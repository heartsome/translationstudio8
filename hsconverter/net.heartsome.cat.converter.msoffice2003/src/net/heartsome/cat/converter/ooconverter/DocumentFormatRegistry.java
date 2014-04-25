/**
 * DocumentFormatRegistry.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconverter;

import net.heartsome.cat.converter.ooconverter.impl.DocumentFormat;

/**
 * The Interface DocumentFormatRegistry.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public interface DocumentFormatRegistry {

	/**
	 * Gets the format by file extension.
	 * @param extension
	 *            the extension
	 * @return the format by file extension
	 */
	DocumentFormat getFormatByFileExtension(String extension);

	/**
	 * Gets the format by mime type.
	 * @param extension
	 *            the extension
	 * @return the format by mime type
	 */
	DocumentFormat getFormatByMimeType(String extension);

}
