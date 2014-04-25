/**
 * AbstractOpenOfficeDocumentConverter.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconverter;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.heartsome.cat.converter.msoffice2003.resource.Messages;
import net.heartsome.cat.converter.ooconnect.OPConnection;
import net.heartsome.cat.converter.ooconverter.impl.DefaultDocumentFormatRegistry;
import net.heartsome.cat.converter.ooconverter.impl.DocumentFormat;

import org.apache.commons.io.FilenameUtils;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XRefreshable;

/**
 * The Class AbstractOpenOfficeDocumentConverter.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public abstract class AbstractOpenOfficeDocumentConverter implements DOCConverter {

	/** The default load properties. */
	@SuppressWarnings("unchecked")
	private final Map/* <String,Object> */defaultLoadProperties;

	/** The open office connection. */
	protected OPConnection openOfficeConnection;

	/** The document format registry. */
	private DocumentFormatRegistry documentFormatRegistry;

	/**
	 * Instantiates a new abstract open office document converter.
	 * @param connection
	 *            the connection
	 */
	public AbstractOpenOfficeDocumentConverter(OPConnection connection) {
		this(connection, new DefaultDocumentFormatRegistry());
	}

	/**
	 * Instantiates a new abstract open office document converter.
	 * @param openOfficeConnection
	 *            the open office connection
	 * @param documentFormatRegistry
	 *            the document format registry
	 */
	@SuppressWarnings("unchecked")
	public AbstractOpenOfficeDocumentConverter(OPConnection openOfficeConnection,
			DocumentFormatRegistry documentFormatRegistry) {
		this.openOfficeConnection = openOfficeConnection;
		this.documentFormatRegistry = documentFormatRegistry;

		defaultLoadProperties = new HashMap();
		defaultLoadProperties.put("Hidden", Boolean.TRUE); //$NON-NLS-1$
		defaultLoadProperties.put("ReadOnly", Boolean.TRUE); //$NON-NLS-1$
	}

	/**
	 * Sets the default load property.
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	@SuppressWarnings("unchecked")
	public void setDefaultLoadProperty(String name, Object value) {
		defaultLoadProperties.put(name, value);
	}

	/**
	 * Gets the default load properties.
	 * @return the default load properties
	 */
	@SuppressWarnings("unchecked")
	protected Map getDefaultLoadProperties() {
		return defaultLoadProperties;
	}

	/**
	 * Gets the document format registry.
	 * @return the document format registry
	 */
	protected DocumentFormatRegistry getDocumentFormatRegistry() {
		return documentFormatRegistry;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconverter.DOCConverter#convert(java.io.File, java.io.File)
	 * @param inputFile
	 * @param outputFile
	 * @throws Exception
	 */
	public void convert(File inputFile, File outputFile) throws Exception {
		convert(inputFile, outputFile, null);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconverter.DOCConverter#convert(java.io.File, java.io.File,
	 *      net.heartsome.cat.converter.ooconverter.impl.DocumentFormat)
	 * @param inputFile
	 * @param outputFile
	 * @param outputFormat
	 * @throws Exception
	 */
	public void convert(File inputFile, File outputFile, DocumentFormat outputFormat) throws Exception {
		convert(inputFile, null, outputFile, outputFormat);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconverter.DOCConverter#convert(java.io.InputStream,
	 *      net.heartsome.cat.converter.ooconverter.impl.DocumentFormat, java.io.OutputStream,
	 *      net.heartsome.cat.converter.ooconverter.impl.DocumentFormat)
	 * @param inputStream
	 * @param inputFormat
	 * @param outputStream
	 * @param outputFormat
	 * @throws Exception
	 */
	public void convert(InputStream inputStream, DocumentFormat inputFormat, OutputStream outputStream,
			DocumentFormat outputFormat) throws Exception {
		ensureNotNull(Messages.getString("ooconverter.AbstractOpenOfficeDocumentConverter.6"), inputStream); //$NON-NLS-1$
		ensureNotNull(Messages.getString("ooconverter.AbstractOpenOfficeDocumentConverter.7"), inputFormat); //$NON-NLS-1$
		ensureNotNull(Messages.getString("ooconverter.AbstractOpenOfficeDocumentConverter.8"), outputStream); //$NON-NLS-1$
		ensureNotNull(Messages.getString("ooconverter.AbstractOpenOfficeDocumentConverter.9"), outputFormat); //$NON-NLS-1$
		convertInternal(inputStream, inputFormat, outputStream, outputFormat);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconverter.DOCConverter#convert(java.io.File,
	 *      net.heartsome.cat.converter.ooconverter.impl.DocumentFormat, java.io.File,
	 *      net.heartsome.cat.converter.ooconverter.impl.DocumentFormat)
	 * @param inputFile
	 * @param inputFormat
	 * @param outputFile
	 * @param outputFormat
	 * @throws Exception
	 */
	public void convert(File inputFile, DocumentFormat inputFormat, File outputFile, DocumentFormat outputFormat)
			throws Exception {
		ensureNotNull(Messages.getString("ooconverter.AbstractOpenOfficeDocumentConverter.10"), inputFile); //$NON-NLS-1$
		ensureNotNull(Messages.getString("ooconverter.AbstractOpenOfficeDocumentConverter.11"), outputFile); //$NON-NLS-1$

		if (!inputFile.exists()) {
			throw new IllegalArgumentException(Messages.getString("ooconverter.AbstractOpenOfficeDocumentConverter.12") + inputFile); //$NON-NLS-1$
		}
		if (inputFormat == null) {
			inputFormat = guessDocumentFormat(inputFile);
		}
		if (outputFormat == null) {
			outputFormat = guessDocumentFormat(outputFile);
		}
		if (!inputFormat.isImportable()) {
			throw new IllegalArgumentException(
					Messages.getString("ooconverter.AbstractOpenOfficeDocumentConverter.13") + inputFormat.getName()); //$NON-NLS-1$
		}
		if (!inputFormat.isExportableTo(outputFormat)) {
			MessageFormat mf = new MessageFormat(Messages.getString("ooconverter.AbstractOpenOfficeDocumentConverter.14")); //$NON-NLS-1$
			Object[] args = new Object[] { inputFormat.getName(), outputFormat.getName() };
			String errMsg = mf.format(args);
			args = null;
			throw new IllegalArgumentException(errMsg);
		}
		convertInternal(inputFile, inputFormat, outputFile, outputFormat);
	}

	/**
	 * Convert internal.
	 * @param inputStream
	 *            the input stream
	 * @param inputFormat
	 *            the input format
	 * @param outputStream
	 *            the output stream
	 * @param outputFormat
	 *            the output format
	 * @throws Exception
	 *             the exception
	 */
	protected abstract void convertInternal(InputStream inputStream, DocumentFormat inputFormat,
			OutputStream outputStream, DocumentFormat outputFormat) throws Exception;

	/**
	 * Convert internal.
	 * @param inputFile
	 *            the input file
	 * @param inputFormat
	 *            the input format
	 * @param outputFile
	 *            the output file
	 * @param outputFormat
	 *            the output format
	 * @throws Exception
	 *             the exception
	 */
	protected abstract void convertInternal(File inputFile, DocumentFormat inputFormat, File outputFile,
			DocumentFormat outputFormat) throws Exception;

	/**
	 * Ensure not null.
	 * @param argumentName
	 *            the argument name
	 * @param argumentValue
	 *            the argument value
	 */
	private void ensureNotNull(String argumentName, Object argumentValue) {
		if (argumentValue == null) {
			throw new IllegalArgumentException(argumentName
					+ Messages.getString("ooconverter.AbstractOpenOfficeDocumentConverter.15")); //$NON-NLS-1$
		}
	}

	/**
	 * Guess document format.
	 * @param file
	 *            the file
	 * @return the document format
	 */
	private DocumentFormat guessDocumentFormat(File file) {
		String extension = FilenameUtils.getExtension(file.getName());
		DocumentFormat format = getDocumentFormatRegistry().getFormatByFileExtension(extension);
		if (format == null) {
			throw new IllegalArgumentException(Messages.getString("ooconverter.AbstractOpenOfficeDocumentConverter.16") + file); //$NON-NLS-1$
		}
		return format;
	}

	/**
	 * Refresh document.
	 * @param document
	 *            the document
	 */
	protected void refreshDocument(XComponent document) {
		XRefreshable refreshable = (XRefreshable) UnoRuntime.queryInterface(XRefreshable.class, document);
		if (refreshable != null) {
			refreshable.refresh();
		}
	}

	/**
	 * Property.
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @return the property value
	 */
	protected static PropertyValue property(String name, Object value) {
		PropertyValue property = new PropertyValue();
		property.Name = name;
		property.Value = value;
		return property;
	}

	/**
	 * To property values.
	 * @param properties
	 *            the properties
	 * @return the property value[]
	 */
	@SuppressWarnings("unchecked")
	protected static PropertyValue[] toPropertyValues(Map/* <String,Object> */properties) {
		PropertyValue[] propertyValues = new PropertyValue[properties.size()];
		int i = 0;
		for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object value = entry.getValue();
			if (value instanceof Map) {
				// recursively convert nested Map to PropertyValue[]
				Map subProperties = (Map) value;
				value = toPropertyValues(subProperties);
			}
			propertyValues[i++] = property((String) entry.getKey(), value);
		}
		return propertyValues;
	}
}
