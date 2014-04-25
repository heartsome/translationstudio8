/**
 * DocumentFormat.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconverter.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a document format ("OpenDocument Text" or "PDF"). Also contains its available export filters.
 */
public class DocumentFormat {

	/** The Constant FILTER_NAME. */
	private static final String FILTER_NAME = "FilterName"; //$NON-NLS-1$

	/** The name. */
	private String name;

	/** The family. */
	private DocumentFamily family;

	/** The mime type. */
	private String mimeType;

	/** The file extension. */
	private String fileExtension;

	/** The export options. */
	@SuppressWarnings("unchecked")
	private Map exportOptions = new HashMap();

	/** The import options. */
	@SuppressWarnings("unchecked")
	private Map importOptions = new HashMap();

	/**
	 * Instantiates a new document format.
	 */
	public DocumentFormat() {
		// empty constructor needed for XStream deserialization
	}

	/**
	 * Instantiates a new document format.
	 * @param name
	 *            the name
	 * @param mimeType
	 *            the mime type
	 * @param extension
	 *            the extension
	 */
	public DocumentFormat(String name, String mimeType, String extension) {
		this.name = name;
		this.mimeType = mimeType;
		this.fileExtension = extension;
	}

	/**
	 * Instantiates a new document format.
	 * @param name
	 *            the name
	 * @param family
	 *            the family
	 * @param mimeType
	 *            the mime type
	 * @param extension
	 *            the extension
	 */
	public DocumentFormat(String name, DocumentFamily family, String mimeType, String extension) {
		this.name = name;
		this.family = family;
		this.mimeType = mimeType;
		this.fileExtension = extension;
	}

	/**
	 * Gets the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the family.
	 * @return the family
	 */
	public DocumentFamily getFamily() {
		return family;
	}

	/**
	 * Gets the mime type.
	 * @return the mime type
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Gets the file extension.
	 * @return the file extension
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * Gets the export filter.
	 * @param family
	 *            the family
	 * @return the export filter
	 */
	private String getExportFilter(DocumentFamily family) {
		return (String) getExportOptions(family).get(FILTER_NAME);
	}

	/**
	 * Checks if is importable.
	 * @return true, if is importable
	 */
	public boolean isImportable() {
		return family != null;
	}

	/**
	 * Checks if is export only.
	 * @return true, if is export only
	 */
	public boolean isExportOnly() {
		return !isImportable();
	}

	/**
	 * Checks if is exportable to.
	 * @param otherFormat
	 *            the other format
	 * @return true, if is exportable to
	 */
	public boolean isExportableTo(DocumentFormat otherFormat) {
		return otherFormat.isExportableFrom(this.family);
	}

	/**
	 * Checks if is exportable from.
	 * @param family
	 *            the family
	 * @return true, if is exportable from
	 */
	public boolean isExportableFrom(DocumentFamily family) {
		return getExportFilter(family) != null;
	}

	/**
	 * Sets the export filter.
	 * @param family
	 *            the family
	 * @param filter
	 *            the filter
	 */
	public void setExportFilter(DocumentFamily family, String filter) {
		getExportOptions(family).put(FILTER_NAME, filter);
	}

	/**
	 * Sets the export option.
	 * @param family
	 *            the family
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public void setExportOption(DocumentFamily family, String name, Object value) {
		Map options = (Map) exportOptions.get(family);
		if (options == null) {
			options = new HashMap();
			exportOptions.put(family, options);
		}
		options.put(name, value);
	}

	/**
	 * Gets the export options.
	 * @param family
	 *            the family
	 * @return the export options
	 */
	public Map getExportOptions(DocumentFamily family) {
		Map options = (Map) exportOptions.get(family);
		if (options == null) {
			options = new HashMap();
			exportOptions.put(family, options);
		}
		return options;
	}

	/**
	 * Sets the import option.
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public void setImportOption(String name, Object value) {
		importOptions.put(name, value);
	}

	/**
	 * Gets the import options.
	 * @return the import options
	 */
	public Map getImportOptions() {
		if (importOptions != null) {
			return importOptions;
		} else {
			return Collections.EMPTY_MAP;
		}
	}
}