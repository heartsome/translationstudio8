/**
 * OpenOfficeDocumentConverter.java
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.msoffice2003.resource.Messages;
import net.heartsome.cat.converter.ooconnect.OPConnection;
import net.heartsome.cat.converter.ooconnect.OPException;
import net.heartsome.cat.converter.ooconverter.impl.DocumentFormat;

import org.apache.commons.io.IOUtils;

import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;

/**
 * The Class OpenOfficeDocumentConverter.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class OpenOfficeDocumentConverter extends AbstractOpenOfficeDocumentConverter {

	// private static final Logger logger =
	// LoggerFactory.getLogger(OpenOfficeDocumentConverter.class);

	/**
	 * Instantiates a new open office document converter.
	 * @param connection
	 *            the connection
	 */
	public OpenOfficeDocumentConverter(OPConnection connection) {
		super(connection);
	}

	/**
	 * Instantiates a new open office document converter.
	 * @param connection
	 *            the connection
	 * @param formatRegistry
	 *            the format registry
	 */
	public OpenOfficeDocumentConverter(OPConnection connection, DocumentFormatRegistry formatRegistry) {
		super(connection, formatRegistry);
	}

	/**
	 * In this file-based implementation, streams are emulated using temporary files.
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
	protected void convertInternal(InputStream inputStream, DocumentFormat inputFormat, OutputStream outputStream,
			DocumentFormat outputFormat) throws Exception {
		File inputFile = null;
		File outputFile = null;
		try {
			inputFile = File.createTempFile("document", "." + inputFormat.getFileExtension()); //$NON-NLS-1$ //$NON-NLS-2$
			OutputStream inputFileStream = null;
			try {
				inputFileStream = new FileOutputStream(inputFile);
				IOUtils.copy(inputStream, inputFileStream);
			} finally {
				IOUtils.closeQuietly(inputFileStream);
			}

			outputFile = File.createTempFile("document", "." + outputFormat.getFileExtension()); //$NON-NLS-1$ //$NON-NLS-2$
			convert(inputFile, inputFormat, outputFile, outputFormat);
			InputStream outputFileStream = null;
			try {
				outputFileStream = new FileInputStream(outputFile);
				IOUtils.copy(outputFileStream, outputStream);
			} finally {
				IOUtils.closeQuietly(outputFileStream);
			}
		} catch (IOException ioException) {
			throw new OPException(Messages.getString("ooconverter.OpenOfficeDocumentConverter.4"), ioException); //$NON-NLS-1$
		} finally {
			if (inputFile != null) {
				inputFile.delete();
			}
			if (outputFile != null) {
				outputFile.delete();
			}
		}
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
	@SuppressWarnings("unchecked")
	protected void convertInternal(File inputFile, DocumentFormat inputFormat, File outputFile,
			DocumentFormat outputFormat) throws Exception {
		Map/* <String,Object> */loadProperties = new HashMap();
		loadProperties.putAll(getDefaultLoadProperties());
		loadProperties.putAll(inputFormat.getImportOptions());

		Map/* <String,Object> */storeProperties = outputFormat.getExportOptions(inputFormat.getFamily());

		synchronized (openOfficeConnection) {
			XFileIdentifierConverter fileContentProvider = openOfficeConnection.getFileContentProvider();
			String inputUrl = fileContentProvider.getFileURLFromSystemPath("", inputFile.getAbsolutePath()); //$NON-NLS-1$
			String outputUrl = fileContentProvider.getFileURLFromSystemPath("", outputFile.getAbsolutePath()); //$NON-NLS-1$

			loadAndExport(inputUrl, loadProperties, outputUrl, storeProperties);
		}
	}

	/**
	 * Load and export.
	 * @param inputUrl
	 *            the input url
	 * @param loadProperties
	 *            the load properties
	 * @param outputUrl
	 *            the output url
	 * @param storeProperties
	 *            the store properties
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	private void loadAndExport(String inputUrl, Map/* <String,Object> */loadProperties, String outputUrl,
			Map/* <String,Object> */storeProperties) throws Exception {
		XComponent document;
		// try {
		document = loadDocument(inputUrl, loadProperties);
		// } catch (ErrorCodeIOException errorCodeIOException) {
		// throw new
		// OPException("conversion failed: could not load input document; OOo errorCode: "
		// + errorCodeIOException.ErrCode, errorCodeIOException);
		// } catch (Exception otherException) {
		// throw new
		// OPException("conversion failed: could not load input document",
		// otherException);
		// }
		if (document == null) {
			throw new OPException(Messages.getString("ooconverter.OpenOfficeDocumentConverter.9")); //$NON-NLS-1$
		}

		refreshDocument(document);

		// try {
		storeDocument(document, outputUrl, storeProperties);
		// } catch (ErrorCodeIOException errorCodeIOException) {
		// throw new
		// OPException("conversion failed: could not save output document; OOo errorCode: "
		// + errorCodeIOException.ErrCode, errorCodeIOException);
		// } catch (Exception otherException) {
		// throw new
		// OPException("conversion failed: could not save output document",
		// otherException);
		// }
	}

	/**
	 * Load document.
	 * @param inputUrl
	 *            the input url
	 * @param loadProperties
	 *            the load properties
	 * @return the x component
	 * @throws IllegalArgumentException
	 *             the illegal argument exception
	 */
	@SuppressWarnings("unchecked")
	private XComponent loadDocument(String inputUrl, Map loadProperties) throws com.sun.star.io.IOException,
			IllegalArgumentException {
		XComponentLoader desktop = openOfficeConnection.getDesktopObject();
		return desktop.loadComponentFromURL(inputUrl, "_blank", 0, toPropertyValues(loadProperties)); //$NON-NLS-1$
	}

	/**
	 * Store document.
	 * @param document
	 *            the document
	 * @param outputUrl
	 *            the output url
	 * @param storeProperties
	 *            the store properties
	 */
	@SuppressWarnings("unchecked")
	private void storeDocument(XComponent document, String outputUrl, Map storeProperties)
			throws com.sun.star.io.IOException {
		try {
			XStorable storable = (XStorable) UnoRuntime.queryInterface(XStorable.class, document);
			storable.storeToURL(outputUrl, toPropertyValues(storeProperties));
		} finally {
			XCloseable closeable = (XCloseable) UnoRuntime.queryInterface(XCloseable.class, document);
			if (closeable != null) {
				try {
					closeable.close(true);
				} catch (CloseVetoException closeVetoException) {
					if (Converter.DEBUG_MODE) {
						closeVetoException.printStackTrace();
					}
				}
			} else {
				document.dispose();
			}
		}
	}

}
