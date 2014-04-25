/**
 * DOCConverter.java
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

import net.heartsome.cat.converter.ooconverter.impl.DocumentFormat;

/**
 * The Interface DOCConverter.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public interface DOCConverter {

	/**
	 * Convert a document.
	 * <p>
	 * Note that this method does not close <tt>inputStream</tt> and <tt>outputStream</tt>.
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
	void convert(InputStream inputStream, DocumentFormat inputFormat, OutputStream outputStream,
			DocumentFormat outputFormat) throws Exception;

	/**
	 * Convert a document.
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
	void convert(File inputFile, DocumentFormat inputFormat, File outputFile, DocumentFormat outputFormat)
			throws Exception;

	/**
	 * Convert a document. The input format is guessed from the file extension.
	 * @param inputDocument
	 *            the input document
	 * @param outputDocument
	 *            the output document
	 * @param outputFormat
	 *            the output format
	 * @throws Exception
	 *             the exception
	 */
	void convert(File inputDocument, File outputDocument, DocumentFormat outputFormat) throws Exception;

	/**
	 * Convert a document. Both input and output formats are guessed from the file extension.
	 * @param inputDocument
	 *            the input document
	 * @param outputDocument
	 *            the output document
	 * @throws Exception
	 *             the exception
	 */
	void convert(File inputDocument, File outputDocument) throws Exception;

}
