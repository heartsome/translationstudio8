/**
 * Xliff2JavaScript.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.javascript;

import java.io.BufferedReader;
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
import net.heartsome.cat.converter.javascript.resource.Messages;
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
import org.xml.sax.SAXException;

/**
 * The Class Xliff2JavaScript.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Xliff2JavaScript implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "javascript";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("javascript.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to JavaScript Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2JavaScriptImpl converter = new Xliff2JavaScriptImpl();
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
	 * The Class Xliff2JavaScriptImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2JavaScriptImpl {

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

		// 计算替换进度的对象
		private CalculateProcessedBytes cpb;

		// 替换过程的进度监视器
		private IProgressMonitor replaceMonitor;

		// skeleton 文件编码
		private String encoding;

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

			sklFile = params.get(Converter.ATTR_SKELETON_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			catalogue = params.get(Converter.ATTR_CATALOGUE);
			String outputFile = params.get(Converter.ATTR_TARGET_FILE);

			encoding = params.get(Converter.ATTR_SOURCE_ENCODING);

			String attrIsPreviewMode = params.get(Converter.ATTR_IS_PREVIEW_MODE);
			/* 是否为预览翻译模式 */
			boolean isPreviewMode = attrIsPreviewMode != null && attrIsPreviewMode.equals(Converter.TRUE);

			try {
				infoLogger.logConversionFileInfo(catalogue, null, xliffFile, sklFile);
				// 把转换过程分为二大部分其 10 个任务，其中加载 xliff 文件占 4，替换过程占 6。
				monitor.beginTask("", 10);
				monitor.subTask(Messages.getString("javascript.Xliff2JavaScript.task2"));
				infoLogger.startLoadingXliffFile();
				output = new FileOutputStream(outputFile);
				loadSegments();
				infoLogger.endLoadingXliffFile();
				monitor.worked(4);
				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("javascript.cancel"));
				}

				try {
					infoLogger.startReplacingSegmentSymbol();
					cpb = ConverterUtils.getCalculateProcessedBytes(sklFile);
					replaceMonitor = Progress.getSubMonitor(monitor, 6);
					replaceMonitor.beginTask(Messages.getString("javascript.Xliff2JavaScript.task3"), cpb.getTotalTask());
					replaceMonitor.subTask("");
					input = new InputStreamReader(new FileInputStream(sklFile), UTF_8); //$NON-NLS-1$
					buffer = new BufferedReader(input);

					line = buffer.readLine();
					while (line != null) {
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
									// 替换字符
									String replaceCode = code;

									Element target = segment.getChild("target"); //$NON-NLS-1$
									Element source = segment.getChild("source"); //$NON-NLS-1$
									if (target != null) {
										String tgtStr = target.getText();
										if (isPreviewMode || !"".equals(tgtStr.trim())) {
											writeString(tgtStr, true, replaceCode);
										} else {
											writeString(source.getText(), true, replaceCode);
										}
									} else {
										writeString(source.getText(), true, replaceCode);
									}
								} else {
									ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
											MessageFormat.format(Messages.getString("javascript.Xliff2JavaScript.msg1"), code));
								}

								index = line.indexOf("%%%"); //$NON-NLS-1$
								if (index == -1) {
									writeString(line);
								}
							} // end while
						} else {
							//
							// non translatable portion
							//
							writeString(line);
						}

						line = buffer.readLine();
					}
					infoLogger.endReplacingSegmentSymbol();
				} finally {
					replaceMonitor.done();
				}
				output.close();

				result.put(Converter.ATTR_TARGET_FILE, outputFile);
				infoLogger.endConversion();
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("javascript.Xliff2JavaScript.msg2"), e);
			} finally {
				monitor.done();
			}

			return result;
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
		 * Write string.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeString(String string) throws IOException {
			writeString(string, false, null);
		}

		/**
		 * Write string.
		 * @param string
		 *            the string
		 * @param isSegment
		 *            标识当前所写内容，ture 标识当前所写内容为 segment，false 标识当前所定内容为原骨架文件中的内容。
		 * @param replaceCode
		 *            skeleton 文件中的segment 标识符
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeString(String string, boolean isSegment, String replaceCode) throws IOException {
			byte[] bytes = string.getBytes(encoding);
			output.write(bytes);
			// 是否取消操作
			if (replaceMonitor.isCanceled()) {
				throw new OperationCanceledException(Messages.getString("javascript.cancel"));
			}
			// 在计算已处理的字节时，需要用 skeleton 文件的源编码进行解码
			if (!isSegment) {
				cpb.calculateProcessed(replaceMonitor, string, UTF_8);
			} else {
				replaceCode = "%%%" + replaceCode + "%%%";
				cpb.calculateProcessed(replaceMonitor, replaceCode, UTF_8);
			}
		}

	}

}