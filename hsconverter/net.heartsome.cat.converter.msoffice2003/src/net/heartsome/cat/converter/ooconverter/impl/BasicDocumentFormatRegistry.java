/**
 * BasicDocumentFormatRegistry.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconverter.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.converter.ooconverter.DocumentFormatRegistry;

/**
 * The Class BasicDocumentFormatRegistry.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class BasicDocumentFormatRegistry implements DocumentFormatRegistry {

	/** The document formats. */
	private List documentFormats = new LinkedList();

	/**
	 * Adds the document format.
	 * @param documentFormat
	 *            the document format
	 */
	public void addDocumentFormat(DocumentFormat documentFormat) {
		documentFormats.add(documentFormat);
	}

	/**
	 * Gets the document formats.
	 * @return the document formats
	 */
	protected List getDocumentFormats() {
		return documentFormats;
	}

	/**
	 * Gets the format by file extension.
	 * @param extension
	 *            the file extension
	 * @return the DocumentFormat for this extension, or null if the extension is not mapped
	 */
	public DocumentFormat getFormatByFileExtension(String extension) {
		if (extension == null) {
			return null;
		}
		String lowerExtension = extension.toLowerCase();
		for (Iterator it = documentFormats.iterator(); it.hasNext();) {
			DocumentFormat format = (DocumentFormat) it.next();
			if (format.getFileExtension().equals(lowerExtension)) {
				return format;
			}
		}
		return null;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconverter.DocumentFormatRegistry#getFormatByMimeType(java.lang.String)
	 * @param mimeType
	 * @return
	 */
	public DocumentFormat getFormatByMimeType(String mimeType) {
		for (Iterator it = documentFormats.iterator(); it.hasNext();) {
			DocumentFormat format = (DocumentFormat) it.next();
			if (format.getMimeType().equals(mimeType)) {
				return format;
			}
		}
		return null;
	}
}
