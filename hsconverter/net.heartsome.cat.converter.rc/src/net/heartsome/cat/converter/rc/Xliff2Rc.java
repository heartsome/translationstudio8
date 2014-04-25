/**
 * Xliff2Rc.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.rc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.rc.resource.Messages;
import net.heartsome.cat.converter.util.CalculateProcessedBytes;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.util.ReverseConversionInfoLogRecord;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * The Class Xliff2Rc.
 * @author John Zhu
 */
public class Xliff2Rc implements Converter {
	private static final Logger LOGGER = LoggerFactory.getLogger(Xliff2Rc.class);

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "winres";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("rc.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to RC Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2RcImpl converter = new Xliff2RcImpl();
		return converter.run(args, monitor);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getName()
	 * @return
	 */
	public String getName() {
		return NAME_VALUE;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getType()
	 * @return
	 */
	public String getType() {
		return TYPE_VALUE;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getTypeName()
	 * @return
	 */
	public String getTypeName() {
		return TYPE_NAME_VALUE;
	}

	/**
	 * The Class Xliff2RcImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2RcImpl {

		// 骨架文件的字符编码
		private static final String UTF_8 = "UTF-8";

		/** The input. */
		private InputStreamReader input;

		/** The buffer. */
		private BufferedReader buffer;

		/** The skl file. */
		private String sklFile;

		/** The xliff file. */
		private String xliffFile;

		/** The line. */
		private String line;

		/** The segments. */
		private Hashtable<String, Element> segments;

		/** The output. */
		private FileOutputStream output;

		/** The catalogue. */
		private String catalogue;

		/** The dlg text. */
		private Hashtable<String, Object> dlgText;

		/** The dest temp. */
		private String destTemp;

		/** The encoding. */
		private String encoding;

		/** The output file. */
		private String outputFile;

		private boolean isInfoEnabled = LOGGER.isInfoEnabled();

		/**
		 * Run.
		 * @param params
		 *            the params
		 * @param monitor
		 *            the monitor
		 * @return the map< string, string>
		 * @throws ConverterException
		 *             the converter exception
		 */
		public Map<String, String> run(Map<String, String> params, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			ReverseConversionInfoLogRecord infoLogger = ConverterUtils.getReverseConversionInfoLogRecord();
			infoLogger.startConversion();
			Map<String, String> result = new HashMap<String, String>();
			dlgText = new Hashtable<String, Object>();

			sklFile = params.get(Converter.ATTR_SKELETON_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			catalogue = params.get(Converter.ATTR_CATALOGUE);
			encoding = params.get(Converter.ATTR_SOURCE_ENCODING);

			String attrIsPreviewMode = params.get(Converter.ATTR_IS_PREVIEW_MODE);
			/* 是否为预览翻译模式 */
			boolean isPreviewMode = attrIsPreviewMode != null && attrIsPreviewMode.equals(Converter.TRUE);

			try {
				// 把转换过程分为四个部分共 7 个任务，其中加载 xliff 文件占 1，处理 dlgInitExists 占 2，替换过程占 2，处理 dlgInitLengths 占 2。
				monitor.beginTask("", 7);
				infoLogger.logConversionFileInfo(catalogue, null, xliffFile, sklFile);
				monitor.subTask(Messages.getString("rc.Xliff2Rc.task2"));
				infoLogger.startLoadingXliffFile();
				File tempFile = File.createTempFile("tempRC", ".temp"); //$NON-NLS-1$ //$NON-NLS-2$
				destTemp = tempFile.getAbsolutePath();
				output = new FileOutputStream(destTemp);
				loadSegments();
				infoLogger.endLoadingXliffFile();
				monitor.worked(1);

				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("rc.cancel"));
				}
				monitor.subTask(Messages.getString("rc.Xliff2Rc.task3"));
				long startTime = 0;
				if (isInfoEnabled) {
					startTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("rc.Xliff2Rc.logger1"), startTime);
				}
				dlgInitExists(params);
				long endTime = 0;
				if (isInfoEnabled) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("rc.Xliff2Rc.logger2"), endTime);
					LOGGER.info(Messages.getString("rc.Xliff2Rc.logger3"), endTime - startTime);
				}
				monitor.worked(2);

				IProgressMonitor replaceMonitor = Progress.getSubMonitor(monitor, 2);
				try {
					// 是否取消操作
					if (replaceMonitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("rc.cancel"));
					}
					infoLogger.startReplacingSegmentSymbol();
					CalculateProcessedBytes cpb = ConverterUtils.getCalculateProcessedBytes(sklFile);
					replaceMonitor.beginTask(Messages.getString("rc.Xliff2Rc.task4"), cpb.getTotalTask());
					replaceMonitor.subTask("");
					input = new InputStreamReader(new FileInputStream(sklFile), UTF_8); //$NON-NLS-1$
					buffer = new BufferedReader(input);

					line = buffer.readLine();
					while (line != null) {
						// 是否取消操作
						if (replaceMonitor.isCanceled()) {
							throw new OperationCanceledException(Messages.getString("rc.cancel"));
						}
						cpb.calculateProcessed(replaceMonitor, line, UTF_8);

						line = line + "\n"; //$NON-NLS-1$

						if (line.indexOf("%%%") != -1) { //$NON-NLS-1$
							//
							// contains translatable text
							//
							int index = line.indexOf("%%%"); //$NON-NLS-1$
							while (index != -1) {
								String start = line.substring(0, index);
								writeString(start);
								line = line.substring(index + 3);
								String code = line.substring(0, line.indexOf("%%%")); //$NON-NLS-1$
								line = line.substring(line.indexOf("%%%") + 3); //$NON-NLS-1$
								Element segment = segments.get(code);
								if (segment != null) {
									Element target = segment.getChild("target"); //$NON-NLS-1$
									Element source = segment.getChild("source"); //$NON-NLS-1$
									if (target != null) {
										String tgtStr = target.getText();
										if (isPreviewMode || !"".equals(tgtStr.trim())) { //$NON-NLS-1$
											writeString(converDlgInit(tgtStr, code));
										} else {
											// process source
											writeString(converDlgInit(source.getText(), code));
										}
									} else {
										// process source
										writeString(converDlgInit(source.getText(), code));
									}
								} else {
									ConverterUtils.throwConverterException(Activator.PLUGIN_ID, MessageFormat.format(
											Messages.getString("rc.Xliff2Rc.msg1"), code));
								}

								index = line.indexOf("%%%"); //$NON-NLS-1$
								if (index == -1) {
									writeString(line);
								}
							} // end while

						} else {
							writeString(line);
						}

						line = buffer.readLine();
					}
					infoLogger.endReplacingSegmentSymbol();
				} finally {
					replaceMonitor.done();
				}
				output.close();
				buffer.close();
				monitor.subTask(Messages.getString("rc.Xliff2Rc.task5"));
				if (isInfoEnabled) {
					startTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("rc.Xliff2Rc.logger4"), startTime);
				}
				dlgInitLengths(params);
				if (isInfoEnabled) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("rc.Xliff2Rc.logger5"), endTime);
					LOGGER.info(Messages.getString("rc.Xliff2Rc.logger6"), endTime - startTime);
				}
				monitor.worked(2);
				tempFile.delete();
				result.put(Converter.ATTR_TARGET_FILE, outputFile);
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("rc.Xliff2Rc.msg2"), e);
			} finally {
				monitor.done();
			}
			infoLogger.endConversion();
			return result;
		}

		/**
		 * Conver dlg init.
		 * @param word
		 *            the word
		 * @param code
		 *            the code
		 * @return the string
		 * @throws Exception
		 *             the exception
		 */
		private String converDlgInit(String word, String code) throws Exception {
			if (dlgText.containsKey(code)) {
				return decode(word, code);
			}

			return new String("\"" + word + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * Decode.
		 * @param word
		 *            the word
		 * @param code
		 *            the code
		 * @return the string
		 * @throws Exception
		 *             the exception
		 */
		private String decode(String word, String code) throws Exception {
			dlgText.remove(code);
			byte[] utf16Bytes = word.getBytes("UTF-8"); //$NON-NLS-1$
			String byteWord = ""; //$NON-NLS-1$
			Byte par = new Byte("00"); //$NON-NLS-1$
			int length = utf16Bytes.length;

			for (int i = 0; i < length; i = i + 1) {
				if (i % 2 == 0) {
					par = new Byte(utf16Bytes[i]);
				} else {
					Byte impar = new Byte(utf16Bytes[i]);
					byteWord = byteWord
							+ " 0x" + Integer.toHexString(impar.intValue()) + Integer.toHexString(par.intValue()) + ", "; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			if (utf16Bytes.length % 2 == 0) {
				byteWord = byteWord + "\"\\000\" "; //$NON-NLS-1$
			} else {
				if (length > 0) {
					byteWord = byteWord + " 0x00" + Integer.toHexString(par.intValue()) + ", "; //$NON-NLS-1$ //$NON-NLS-2$
				}
				byteWord = byteWord + " "; //$NON-NLS-1$
			}
			length++;
			dlgText.put(code, "" + length);
			return byteWord;
		}

		/**
		 * Load segments.
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws ParserConfigurationException
		 *             the parser configuration exception
		 */
		private void loadSegments() throws SAXException, IOException, ParserConfigurationException {

			SAXBuilder builder = new SAXBuilder();
			builder.setEntityResolver(new Catalogue(catalogue));

			Document doc = builder.build(xliffFile);
			Element root = doc.getRootElement();
			Element body = root.getChild("file").getChild("body"); //$NON-NLS-1$ //$NON-NLS-2$
			List<Element> units = body.getChildren("trans-unit"); //$NON-NLS-1$
			Iterator<Element> i = units.iterator();

			segments = new Hashtable<String, Element>();

			while (i.hasNext()) {
				Element unit = i.next();
				segments.put(unit.getAttributeValue("id"), unit); //$NON-NLS-1$
			}
		}

		/**
		 * Write string encoded.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeStringEncoded(String string) throws IOException {
			output.write(string.getBytes(encoding));
		}

		/**
		 * Write string.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeString(String string) throws IOException {
			output.write(string.getBytes("UTF-8")); //$NON-NLS-1$
		}

		/**
		 * Dlg init exists.
		 * @param params
		 *            the params
		 * @throws Exception
		 *             the exception
		 */
		private void dlgInitExists(Map<String, String> params) throws Exception {
			input = new InputStreamReader(new FileInputStream(sklFile), "UTF-8"); //$NON-NLS-1$
			buffer = new BufferedReader(input);

			line = buffer.readLine();
			while (line != null) {

				if (line.indexOf("###") != -1) { //$NON-NLS-1$
					// contains dlginit length
					int index = line.indexOf("###"); //$NON-NLS-1$
					while (index != -1) {
						line = line.substring(index + 3);
						String code = line.substring(0, line.indexOf("###")); //$NON-NLS-1$
						line = line.substring(line.indexOf("###") + 3); //$NON-NLS-1$
						Element segment = segments.get(code);
						if (segment != null) {
							dlgText.put(code, new Integer(0));
						} else {
							ConverterUtils.throwConverterException(Activator.PLUGIN_ID, MessageFormat.format(
									Messages.getString("rc.Xliff2Rc.msg1"), code));
						}
						index = line.indexOf("###"); //$NON-NLS-1$
					} // end while
				}
				line = buffer.readLine();
			}
		}

		/**
		 * Dlg init lengths.
		 * @param params
		 *            the params
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void dlgInitLengths(Map<String, String> params) throws IOException {
			outputFile = params.get(Converter.ATTR_TARGET_FILE);
			output = new FileOutputStream(outputFile);
			input = new InputStreamReader(new FileInputStream(destTemp), "UTF-8"); //$NON-NLS-1$
			buffer = new BufferedReader(input);

			line = buffer.readLine();
			while (line != null) {
				line = line + "\n"; //$NON-NLS-1$

				if (line.indexOf("###") != -1) { //$NON-NLS-1$
					// contains dlginit length
					int index = line.indexOf("###"); //$NON-NLS-1$
					while (index != -1) {
						String start = line.substring(0, index);
						writeStringEncoded(start);
						line = line.substring(index + 3);
						String code = line.substring(0, line.indexOf("###")); //$NON-NLS-1$
						line = line.substring(line.indexOf("###") + 3); //$NON-NLS-1$
						writeStringEncoded((String) dlgText.get(code));
						index = line.indexOf("###"); //$NON-NLS-1$
						if (index == -1) {
							writeStringEncoded(line);
						}
					} // end while

				} else {
					writeStringEncoded(line);
				}
				line = buffer.readLine();
			}
			output.close();
		}
	}
}