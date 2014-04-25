/**
 * StreamOpenOfficeDocumentConverter.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.msoffice2003.resource.Messages;
import net.heartsome.cat.converter.ooconnect.OPConnection;
import net.heartsome.cat.converter.ooconnect.OPException;
import net.heartsome.cat.converter.ooconverter.impl.DocumentFormat;

import org.apache.commons.io.IOUtils;

import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
import com.sun.star.lib.uno.adapter.OutputStreamToXOutputStreamAdapter;
import com.sun.star.uno.UnoRuntime;

/**
 * The Class StreamOpenOfficeDocumentConverter.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class StreamOpenOfficeDocumentConverter extends AbstractOpenOfficeDocumentConverter {

	/**
	 * Instantiates a new stream open office document converter.
	 * @param connection
	 *            the connection
	 */
	public StreamOpenOfficeDocumentConverter(OPConnection connection) {
		super(connection);
	}

	/**
	 * Instantiates a new stream open office document converter.
	 * @param connection
	 *            the connection
	 * @param formatRegistry
	 *            the format registry
	 */
	public StreamOpenOfficeDocumentConverter(OPConnection connection, DocumentFormatRegistry formatRegistry) {
		super(connection, formatRegistry);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconverter.AbstractOpenOfficeDocumentConverter#convertInternal(java.io.File,
	 *      net.heartsome.cat.converter.ooconverter.impl.DocumentFormat, java.io.File,
	 *      net.heartsome.cat.converter.ooconverter.impl.DocumentFormat)
	 * @param inputFile
	 * @param inputFormat
	 * @param outputFile
	 * @param outputFormat
	 * @throws Exception
	 */
	protected void convertInternal(File inputFile, DocumentFormat inputFormat, File outputFile,
			DocumentFormat outputFormat) throws Exception {
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = new FileInputStream(inputFile);
			outputStream = new FileOutputStream(outputFile);
			convert(inputStream, inputFormat, outputStream, outputFormat);
		} catch (FileNotFoundException fileNotFoundException) {
			throw new IllegalArgumentException(fileNotFoundException.getMessage());
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconverter.AbstractOpenOfficeDocumentConverter#convertInternal(java.io.InputStream,
	 *      net.heartsome.cat.converter.ooconverter.impl.DocumentFormat, java.io.OutputStream,
	 *      net.heartsome.cat.converter.ooconverter.impl.DocumentFormat)
	 * @param inputStream
	 * @param inputFormat
	 * @param outputStream
	 * @param outputFormat
	 */
	@SuppressWarnings("unchecked")
	protected void convertInternal(InputStream inputStream, DocumentFormat inputFormat, OutputStream outputStream,
			DocumentFormat outputFormat) {
		Map/* <String,Object> */exportOptions = outputFormat.getExportOptions(inputFormat.getFamily());

		try {
			synchronized (openOfficeConnection) {
				loadAndExport(inputStream, inputFormat.getImportOptions(), outputStream, exportOptions);
			}
		} catch (OPException openOfficeException) {
			throw openOfficeException;
		} catch (Throwable throwable) {
			throw new OPException(Messages.getString("ooconverter.StreamOpenOfficeDocumentConverter.1"), throwable); //$NON-NLS-1$
		}
	}

	/**
	 * Load and export.
	 * @param inputStream
	 *            the input stream
	 * @param importOptions
	 *            the import options
	 * @param outputStream
	 *            the output stream
	 * @param exportOptions
	 *            the export options
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	private void loadAndExport(InputStream inputStream, Map/* <String,Object> */importOptions,
			OutputStream outputStream, Map/* <String,Object> */exportOptions) throws Exception {
		XComponentLoader desktop = openOfficeConnection.getDesktopObject();

		Map/* <String,Object> */loadProperties = new HashMap();
		loadProperties.putAll(getDefaultLoadProperties());
		loadProperties.putAll(importOptions);
		// doesn't work using InputStreamToXInputStreamAdapter; probably because
		// it's not XSeekable
		// property("InputStream", new
		// InputStreamToXInputStreamAdapter(inputStream))
		loadProperties.put("InputStream", new ByteArrayToXInputStreamAdapter(IOUtils.toByteArray(inputStream))); //$NON-NLS-1$

		XComponent document = desktop.loadComponentFromURL(
				"private:stream", "_blank", 0, toPropertyValues(loadProperties)); //$NON-NLS-1$ //$NON-NLS-2$
		if (document == null) {
			throw new OPException(Messages.getString("ooconverter.StreamOpenOfficeDocumentConverter.6")); //$NON-NLS-1$
		}

		refreshDocument(document);

		Map/* <String,Object> */storeProperties = new HashMap();
		storeProperties.putAll(exportOptions);
		storeProperties.put("OutputStream", new OutputStreamToXOutputStreamAdapter(outputStream)); //$NON-NLS-1$

		try {
			XStorable storable = (XStorable) UnoRuntime.queryInterface(XStorable.class, document);
			storable.storeToURL("private:stream", toPropertyValues(storeProperties)); //$NON-NLS-1$
		} finally {
			document.dispose();
		}
	}
}
