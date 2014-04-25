/**
 * Xliff2Rtf.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.rtf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.rtf.resource.Messages;
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
 * The Class Xliff2Rtf.
 * @author John Zhu
 */
public class Xliff2Rtf implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "rtf";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("rtf.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to RTF Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2RtfImpl converter = new Xliff2RtfImpl();
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
	 * The Class Xliff2RtfImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2RtfImpl {

		private static final String UTF_8 = "UTF-8";

		/** The skl file. */
		private String sklFile;

		/** The xliff file. */
		private String xliffFile;

		/** The catalogue. */
		private Catalogue catalogue;

		/** The output. */
		private FileOutputStream output;

		/** The input. */
		private InputStreamReader input;

		/** The buffer. */
		private BufferedReader buffer;

		/** The segments. */
		private Hashtable<String, Element> segments;

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
			String catalogueFile = params.get(Converter.ATTR_CATALOGUE);
			String outputFile = params.get(Converter.ATTR_TARGET_FILE);

			String attrIsPreviewMode = params.get(Converter.ATTR_IS_PREVIEW_MODE);
			/* 是否为预览翻译模式 */
			boolean isPreviewMode = attrIsPreviewMode != null && attrIsPreviewMode.equals(Converter.TRUE);

			try {
				// 把转换过程分为三大部分共 10 个任务，其中加载 catalogue 占 2，加载 xliff 文件占 3，替换过程占 5。
				monitor.beginTask("", 10);
				infoLogger.logConversionFileInfo(catalogueFile, null, xliffFile, sklFile);
				try {
					monitor.subTask(Messages.getString("rtf.Xliff2Rtf.task2"));
					infoLogger.startLoadingCatalogueFile();
					if (catalogueFile != null) {
						catalogue = new Catalogue(catalogueFile);
					}
					infoLogger.endLoadingCatalogueFile();
					monitor.worked(2);
					// 是否取消操作
					if (monitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("rtf.cancel"));
					}

					monitor.subTask(Messages.getString("rtf.Xliff2Rtf.task3"));
					infoLogger.startLoadingXliffFile();
					output = new FileOutputStream(outputFile);
					loadSegments();
					infoLogger.endLoadingXliffFile();
					monitor.worked(3);
				} catch (OperationCanceledException e) {
					throw e;
				} catch (Exception e1) {
					if (Converter.DEBUG_MODE) {
						e1.printStackTrace();
					}
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("rtf.Xliff2Rtf.msg1"), e1);
				}
				IProgressMonitor replaceMonitor = Progress.getSubMonitor(monitor, 5);
				try {
					infoLogger.startReplacingSegmentSymbol();
					CalculateProcessedBytes cpb = ConverterUtils.getCalculateProcessedBytes(sklFile);
					replaceMonitor.beginTask(Messages.getString("rtf.Xliff2Rtf.task4"), cpb.getTotalTask());
					replaceMonitor.subTask("");
					input = new InputStreamReader(new FileInputStream(sklFile), UTF_8); //$NON-NLS-1$
					buffer = new BufferedReader(input);
					line = buffer.readLine();
					while (line != null) {
						// 是否取消操作
						if (replaceMonitor.isCanceled()) {
							throw new OperationCanceledException(Messages.getString("rtf.cancel"));
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
								line = line.substring(line.indexOf("%%%\n") + 4); //$NON-NLS-1$
								Element segment = segments.get(code);
								if (segment != null) {
									Element target = segment.getChild("target"); //$NON-NLS-1$
									Element source = segment.getChild("source"); //$NON-NLS-1$
									if (target != null) {
										String tgtStr = cleanChars(extractText(target));
										if (isPreviewMode || !"".equals(tgtStr.trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//											writeString(tgtStr);
											if (isPreviewMode) {
												if ("".equals(tgtStr.trim())) {
													writeString(cleanChars(extractText(source)));
												} else {
													writeString(tgtStr);
												}
											} else {
												writeString(tgtStr);
											}
										} else {
											writeString(cleanChars(extractText(source)));
										}
									} else {
										writeString(cleanChars(extractText(source)));
									}
								} else {
									MessageFormat mf = new MessageFormat(Messages.getString("rtf.Xliff2Rtf.msg0")); //$NON-NLS-1$
									Object[] args = { "" + code }; //$NON-NLS-1$
									String errMsg = mf.format(args);
									args = null;
									throw new Exception(errMsg);
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
				} catch (OperationCanceledException e) {
					throw e;
				} catch (Exception e) {
					if (Converter.DEBUG_MODE) {
						e.printStackTrace();
					}
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("rtf.Xliff2Rtf.msg1"), e);
				} finally {
					replaceMonitor.done();
				}
			} finally {
				monitor.done();
			}
			result.put(Converter.ATTR_TARGET_FILE, outputFile);
			infoLogger.endConversion();
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
			string = replaceToken(string, "\\par", "\n\\par"); //$NON-NLS-1$ //$NON-NLS-2$
			string = replaceToken(string, "\\line", "\n\\line"); //$NON-NLS-1$ //$NON-NLS-2$
			output.write(string.getBytes("UTF-8")); //$NON-NLS-1$
		}

		/**
		 * Extract text.
		 * @param target
		 *            the target
		 * @return the string
		 * @throws UnsupportedEncodingException
		 *             the unsupported encoding exception
		 */
		private String extractText(Element target) throws UnsupportedEncodingException {
			String result = ""; //$NON-NLS-1$
			List<Node> content = target.getContent();
			Iterator<Node> it = content.iterator();
			while (it.hasNext()) {
				Node n = it.next();
				switch (n.getNodeType()) {
				case Node.ELEMENT_NODE:
					Element e = new Element(n);
					if (e.getName().equals("ph")) { //$NON-NLS-1$
						result = result + e.getText();
					} else {
						result = result + extractText(e);
					}
					break;
				case Node.TEXT_NODE:
					String value = n.getNodeValue();
					if (!n.getParentNode().getNodeName().equals("ph")) { //$NON-NLS-1$
						value = replaceToken(value, "\\", "\\\'5C"); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u2002', "\\enspace "); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u2003', "\\emspace "); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u2005', "\\qmspace "); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u2014', "\\emdash "); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u2013', "\\endash "); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u2018', "\\lquote "); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u2019', "\\rquote "); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u201C', "\\ldblquote "); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u201D', "\\rdblquote "); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "{", "\\{"); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "}", "\\}"); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u0009', "\\tab "); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u00A0', "\\~"); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u2011', "\\_"); //$NON-NLS-1$ //$NON-NLS-2$
						value = replaceToken(value, "" + '\u00AD', "\\-"); //$NON-NLS-1$ //$NON-NLS-2$
						value = clean(value);
					} else {
						value = replaceToken(value, "" + '\u00BB', "\\\'"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					if (result.matches(".*\\\\[a-z]+([0-9]+)?") && value.matches("^[a-zA-Z0-9\\s].*")) { //$NON-NLS-1$ //$NON-NLS-2$
						value = " " + value; //$NON-NLS-1$
					}
					result = result + value;
					break;
				default:
					break;
				}
			}
			return result;
		}

		/**
		 * Replace token.
		 * @param string
		 *            the string
		 * @param token
		 *            the token
		 * @param newText
		 *            the new text
		 * @return the string
		 */
		String replaceToken(String string, String token, String newText) {

			int index = string.indexOf(token);
			while (index != -1) {
				String before = string.substring(0, index);
				String after = string.substring(index + token.length());
				if (token.endsWith(" ") && after.length() > 0 && Character.isSpaceChar(after.charAt(0))) { //$NON-NLS-1$
					after = after.substring(1);
				}
				string = before + newText + after;
				index = string.indexOf(token, index + newText.length());
			}
			return string;
		}
	}

	/**
	 * Clean.
	 * @param string
	 *            the string
	 * @return the string
	 */
	protected static String clean(String string) {
		String clean = ""; //$NON-NLS-1$
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			if (c <= '\u007F') {
				clean = clean + c;
			} else if (c == '\uE007') {
				clean = clean + "}"; //$NON-NLS-1$
			} else if (c == '\uE008') {
				clean = clean + "{"; //$NON-NLS-1$
			} else if (c == '\uE011') {
				clean = clean + "\\\\"; //$NON-NLS-1$
			} else {
				// Forget about conversion rules that use \' because there
				// can
				// be encoding issues, specially when handling Mac text.
				// Do the OpenOffice trick for all extended characters
				// instead.
				clean = clean + "\\uc0\\u" + (int) c + " "; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return clean;
	}

	/**
	 * Clean chars.
	 * @param string
	 *            the string
	 * @return the string
	 */
	protected static String cleanChars(String string) {
		String clean = ""; //$NON-NLS-1$
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			if (c <= '\u007F' || c == '\uE007' || c == '\uE008' || c == '\uE011') {
				clean = clean + c;
			} else {
				// Forget about conversion rules that use \' because there
				// can
				// be encoding issues, specially when handling Mac text.
				// Do the OpenOffice trick for all extended characters
				// instead.
				clean = clean + "\\uc0\\u" + (int) c + " "; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return clean;
	}
}
