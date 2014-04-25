/**
 * Xliff2Text.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */

package net.heartsome.cat.converter.text;

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

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.text.resource.Messages;
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
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The Class Xliff2Text.
 * @author John Zhu
 */
public class Xliff2Text implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "plaintext";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("text.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to Text Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2TextImpl converter = new Xliff2TextImpl();
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
	 * The Class Xliff2TextImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2TextImpl {

		private static final String UTF_8 = "UTF-8";

		/** The skl file. */
		private String sklFile;

		/** The xliff file. */
		private String xliffFile;

		/** The encoding. */
		private String encoding;

		/** The segments. */
		private Hashtable<String, Element> segments;

		/** The catalogue. */
		private Catalogue catalogue;

		/** The output. */
		private FileOutputStream output;

		/** The input. */
		private InputStreamReader input;

		/** The buffer. */
		private BufferedReader buffer;

		/** The line. */
		private String line;

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
			encoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			String catalogueFile = params.get(Converter.ATTR_CATALOGUE);

			String attrIsPreviewMode = params.get(Converter.ATTR_IS_PREVIEW_MODE);
			/* 是否为预览翻译模式 */
			boolean isPreviewMode = attrIsPreviewMode != null && attrIsPreviewMode.equals(Converter.TRUE);

			try {
				infoLogger.logConversionFileInfo(catalogueFile, null, xliffFile, sklFile);
				// 把转换过程分为三大部分共 10 个任，其中加载 catalogue 占 3，加载 xliff 文件占 3，替换占 4。
				monitor.beginTask("", 10);
				monitor.subTask(Messages.getString("text.Xliff2Text.task2"));
				infoLogger.startLoadingCatalogueFile();
				if (catalogueFile != null) {
					catalogue = new Catalogue(catalogueFile);
				}
				infoLogger.endLoadingXliffFile();
				monitor.worked(3);
				// 是否取消
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("text.cancel"));
				}

				monitor.subTask(Messages.getString("text.Xliff2Text.task3"));
				infoLogger.startLoadingXliffFile();
				String outputFile = params.get(Converter.ATTR_TARGET_FILE);
				output = new FileOutputStream(outputFile);
				loadSegments();
				infoLogger.endLoadingXliffFile();
				monitor.worked(3);
				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("text.cancel"));
				}
				IProgressMonitor replaceMonitor = Progress.getSubMonitor(monitor, 4);
				try {
					infoLogger.startReplacingSegmentSymbol();
					CalculateProcessedBytes cpb = ConverterUtils.getCalculateProcessedBytes(sklFile);
					replaceMonitor.beginTask(Messages.getString("text.Xliff2Text.task4"), cpb.getTotalTask());
					replaceMonitor.subTask("");
					input = new InputStreamReader(new FileInputStream(sklFile), UTF_8); //$NON-NLS-1$
					buffer = new BufferedReader(input);
					line = buffer.readLine();
					while (line != null) {
						// 是否取消操作
						if (replaceMonitor.isCanceled()) {
							throw new OperationCanceledException(Messages.getString("text.cancel"));
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
										String tgtStr = extractText(target);
										if (isPreviewMode || !"".equals(tgtStr.trim())) {
											if (isPreviewMode) {
												if ("".equals(tgtStr.trim())) {
													writeString(extractText(source));
												} else {
													writeString(tgtStr);
												}
											} else {
												writeString(tgtStr);
											}
										} else {
											writeString(extractText(source));
										}
									} else {
										writeString(extractText(source));
									}
								} else {
									MessageFormat mf = new MessageFormat(Messages.getString("text.Xliff2Text.msg0")); //$NON-NLS-1$
									Object[] args = new Object[] { code };
									ConverterUtils.throwConverterException(Activator.PLUGIN_ID, mf.format(args));
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
				} finally {
					replaceMonitor.done();
				}
				infoLogger.endReplacingSegmentSymbol();

				output.close();
				result.put(Converter.ATTR_TARGET_FILE, outputFile);
				infoLogger.endConversion();
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("text.Xliff2Text.msg1"), e);
			} finally {
				monitor.done();
			}
			return result;
		}

		/**
		 * Extract text.
		 * @param target
		 *            the target
		 * @return the string
		 */
		private String extractText(Element target) {
			String result = ""; //$NON-NLS-1$
			List<Node> content = target.getContent();
			Iterator<Node> i = content.iterator();
			while (i.hasNext()) {
				Node n = i.next();
				switch (n.getNodeType()) {
				case Node.ELEMENT_NODE:
					Element e = new Element(n);
					result = result + extractText(e);
					break;
				case Node.TEXT_NODE:
					result = result + n.getNodeValue();
					break;
				default:
					break;
				}
			}
			return result;
		}

		/**
		 * Load segments.
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void loadSegments() throws SAXException, IOException {

			SAXBuilder builder = new SAXBuilder();
			if (catalogue != null) {
				builder.setEntityResolver(catalogue);
			}

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
			output.write((string.replaceAll("\n", System.getProperty("line.separator"))).getBytes(encoding));
		}
	}
}
