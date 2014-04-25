/**
 * Xliff2Properties.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.javaproperties;

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
import net.heartsome.cat.converter.javaproperties.resource.Messages;
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
 * The Class Xliff2Properties.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Xliff2Properties implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "javalistresourcebundle";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("javaproperties.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to Java Properties Conveter";

	/**
	 * (non-Javadoc).
	 * @param args
	 *            the args
	 * @param monitor
	 *            the monitor
	 * @return the map< string, string>
	 * @throws ConverterException
	 *             the converter exception
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2PropertiesImpl converter = new Xliff2PropertiesImpl();
		return converter.run(args, monitor);
	}

	/**
	 * (non-Javadoc).
	 * @return the name
	 * @see net.heartsome.cat.converter.Converter#getName()
	 */
	public String getName() {
		return NAME_VALUE;
	}

	/**
	 * (non-Javadoc).
	 * @return the type
	 * @see net.heartsome.cat.converter.Converter#getType()
	 */
	public String getType() {
		return TYPE_VALUE;
	}

	/**
	 * (non-Javadoc).
	 * @return the type name
	 * @see net.heartsome.cat.converter.Converter#getTypeName()
	 */
	public String getTypeName() {
		return TYPE_NAME_VALUE;
	}

	/**
	 * The Class Xliff2PropertiesImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2PropertiesImpl {

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

		// 计算替换进度的对象
		private CalculateProcessedBytes cpb;

		// 替换过程的进度监视器
		private IProgressMonitor replaceMonitor;

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
			String outputFile = params.get(Converter.ATTR_TARGET_FILE);

			String attrIsPreviewMode = params.get(Converter.ATTR_IS_PREVIEW_MODE);
			/* 是否为预览翻译模式 */
			boolean isPreviewMode = attrIsPreviewMode != null && attrIsPreviewMode.equals(Converter.TRUE);

			try {
				infoLogger.logConversionFileInfo(catalogueFile, null, xliffFile, sklFile);

				// 把转换过程的进度分为 10，其中加载 catalogue 占 2，加载 xliff 文件占 3，替换过程占 5。
				monitor.beginTask("", 10);
				monitor.subTask(Messages.getString("javaproperties.Xliff2Properties.task1"));
				try {
					infoLogger.startLoadingCatalogueFile();
					if (catalogueFile != null) {
						catalogue = new Catalogue(catalogueFile);
					}
					infoLogger.endLoadingCatalogueFile();
					monitor.worked(2);
					// 是否取消操作
					if (monitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("javaproperties.cancel"));
					}

					infoLogger.startLoadingIniFile();
					output = new FileOutputStream(outputFile);
					loadSegments(Progress.getSubMonitor(monitor, 3));
					infoLogger.endLoadingXliffFile();
				} catch (OperationCanceledException e) {
					throw e;
				} catch (Exception e1) {
					if (Converter.DEBUG_MODE) {
						e1.printStackTrace();
					}

					ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
							Messages.getString("javaproperties.Xliff2Properties.msg1"), e1);
				}
				try {
					// 初始化计算替换进度的对象O
					cpb = ConverterUtils.getCalculateProcessedBytes(sklFile);
					replaceMonitor = Progress.getSubMonitor(monitor, 5);
					replaceMonitor.beginTask(Messages.getString("javaproperties.Xliff2Properties.task2"), cpb.getTotalTask());
					replaceMonitor.subTask("");
					infoLogger.startReplacingSegmentSymbol();

					input = new InputStreamReader(new FileInputStream(sklFile), "UTF-8"); //$NON-NLS-1$
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
									Element target = segment.getChild("target"); //$NON-NLS-1$
									Element source = segment.getChild("source"); //$NON-NLS-1$
									if (target != null) {
										String tgtStr = extractText(target);
										if (isPreviewMode || !"".equals(tgtStr.trim())) {
											writeString(tgtStr, true, code);
										} else {
											writeString(extractText(source), true, code);
										}
									} else {
										writeString(extractText(source), true, code);
									}
								} else {
									MessageFormat mf = new MessageFormat(Messages.getString("javaproperties.Xliff2Properties.msg0")); //$NON-NLS-1$
									Object[] args = { "" + code }; //$NON-NLS-1$
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
					infoLogger.endReplacingSegmentSymbol();

					output.close();

					result.put(Converter.ATTR_TARGET_FILE, outputFile);
					infoLogger.endConversion();
				} catch (OperationCanceledException e) {
					throw e;
				} catch (Exception e) {
					if (Converter.DEBUG_MODE) {
						e.printStackTrace();
					}

					ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
							Messages.getString("javaproperties.Xliff2Properties.msg2"), e);
				} finally {
					replaceMonitor.done();
				}
			} finally {
				// 保证 monitor 的 done 被调用
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
		 */
		private void loadSegments(IProgressMonitor monitor) throws SAXException, IOException {
			try {
				monitor.beginTask(Messages.getString("javaproperties.Xliff2Properties.msg3"), IProgressMonitor.UNKNOWN);
				monitor.subTask("");
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
					// 是否取消操作
					if (monitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("javaproperties.cancel"));
					}
					Element unit = i.next();
					segments.put(unit.getAttributeValue("id"), unit); //$NON-NLS-1$
				}
			} finally {
				monitor.done();
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
				throw new OperationCanceledException(Messages.getString("javaproperties.cancel"));
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

	/**
	 * Extract text.
	 * @param target
	 *            the target
	 * @return the string
	 */
	private static String extractText(Element target) {
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
		return cleanChars(result);
	}

	/**
	 * Clean chars.
	 * @param string
	 *            the string
	 * @return the string
	 */
	private static String cleanChars(String string) {
		String result = ""; //$NON-NLS-1$
		int size = string.length();
		for (int i = 0; i < size; i++) {
			char c = string.charAt(i);
			if (c <= 255) {
				result = result + c;
			} else {
				result = result + toHex(c);
			}
		}
		return result;
	}

	/**
	 * To hex.
	 * @param c
	 *            the c
	 * @return the string
	 */
	private static String toHex(char c) {
		String hex = Integer.toHexString(c);
		while (hex.length() < 4) {
			hex = "0" + hex; //$NON-NLS-1$
		}
		return "\\u" + hex; //$NON-NLS-1$
	}

}
