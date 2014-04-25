/**
 * Xliff2Html.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.html;

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
import net.heartsome.cat.converter.html.resource.Messages;
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
 * The Class Xliff2Html.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Xliff2Html implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "html";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("html.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to Html Conveter";

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
		Xliff2HtmlImpl converter = new Xliff2HtmlImpl();
		// 需要添加指定 ini file
		String iniDir = args.get(Converter.ATTR_INIDIR);
		String iniFile = iniDir + System.getProperty("file.separator") + "init_html.xml";
		args.put(Converter.ATTR_INI_FILE, iniFile);
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
	 * The Class Xliff2HtmlImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2HtmlImpl {

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

		/** The encoding. */
		private String encoding;

		/** The entities. */
		private Hashtable<String, String> entities;

		/** The catalogue. */
		private Catalogue catalogue;

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

			// 在转换过程中，记录转换的相关信息
			ReverseConversionInfoLogRecord infoLogger = ConverterUtils.getReverseConversionInfoLogRecord();
			infoLogger.startConversion();

			Map<String, String> result = new HashMap<String, String>();

			sklFile = params.get(Converter.ATTR_SKELETON_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			encoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			String iniFile = params.get(Converter.ATTR_INI_FILE);
			String catalogueFile = params.get(Converter.ATTR_CATALOGUE);
			String outputFile = params.get(Converter.ATTR_TARGET_FILE);

			String attrIsPreviewMode = params.get(Converter.ATTR_IS_PREVIEW_MODE);
			/* 是否为预览翻译模式 */
			boolean isPreviewMode = attrIsPreviewMode != null && attrIsPreviewMode.equals(Converter.TRUE);

			if (iniFile == null || "".equals(iniFile)) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("html.Xliff2Html.msg1"));
			}
			try {
				infoLogger.logConversionFileInfo(catalogueFile, iniFile, xliffFile, sklFile);

				// 经过对不同大小的文件进行测试后，可以对转换过程进行如下划分。把整个转换过程分为 10，其中加载 catalogue 文件占 2，加载 ini文件占 1，加载 xliff 文件占 3，替换过程占 4。
				monitor.beginTask("", 10);

				monitor.subTask(Messages.getString("html.Xliff2Html.task2"));

				infoLogger.startLoadingCatalogueFile();

				if (catalogueFile != null) {
					catalogue = new Catalogue(catalogueFile);
				}

				infoLogger.endLoadingCatalogueFile();

				monitor.worked(2);
				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("html.cancel"));
				}

				monitor.subTask(Messages.getString("html.Xliff2Html.task3"));
				infoLogger.startLoadingIniFile();

				loadEntities(iniFile);

				infoLogger.endLoadingIniFile();
				monitor.worked(1);
				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("html.cancel"));
				}

				infoLogger.startLoadingXliffFile();

				output = new FileOutputStream(outputFile);
				loadSegments(Progress.getSubMonitor(monitor, 3));

				infoLogger.endLoadingXliffFile();

				infoLogger.startReplacingSegmentSymbol();

				try {
					// 初始化计算替换进度的对象
					cpb = ConverterUtils.getCalculateProcessedBytes(sklFile);
					replaceMonitor = Progress.getSubMonitor(monitor, 4);
					replaceMonitor.beginTask(Messages.getString("html.Xliff2Html.task4"), cpb.getTotalTask());
					replaceMonitor.subTask("");

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
								line = line.substring(line.indexOf("%%%\n") + 4); //$NON-NLS-1$
								Element segment = segments.get(code);
								if (segment != null) {
									// 替换字符
									String replaceCode = code;

									Element target = segment.getChild("target"); //$NON-NLS-1$
									Element source = segment.getChild("source"); //$NON-NLS-1$
									if (target != null) {
										String tgtStr = extractText(target);
										if (isPreviewMode || !"".equals(tgtStr.trim())) {
											writeString(tgtStr, true, replaceCode);
										} else {
											writeString(extractText(source), true, replaceCode);
										}
									} else {
										writeString(extractText(source), true, replaceCode);
									}
								} else {
									ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
											MessageFormat.format(Messages.getString("html.Xliff2Html.msg2"), code));
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
					// 保证 monitor 的 done 方法被调用
					replaceMonitor.done();
				}

				infoLogger.endReplacingSegmentSymbol();

				result.put(Converter.ATTR_TARGET_FILE, outputFile);

				infoLogger.endConversion();
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("html.Xliff2Html.msg3"), e);
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						// ignore the exception
					}
				}
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
			return addEntities(result);
		}

		/**
		 * Load entities.
		 * @param iniFile
		 *            the ini file
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void loadEntities(String iniFile) throws SAXException, IOException {
			SAXBuilder builder = new SAXBuilder();
			if (catalogue != null) {
				builder.setEntityResolver(catalogue);
			}
			Document doc = builder.build(iniFile);
			Element root = doc.getRootElement();

			entities = new Hashtable<String, String>();

			List<Element> ents = root.getChildren("entity"); //$NON-NLS-1$
			Iterator<Element> it = ents.iterator();
			while (it.hasNext()) {
				Element e = it.next();
				entities.put(e.getText(), "&" + e.getAttributeValue("name") + ";"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			root = null;
			doc = null;
			builder = null;
		}

		/**
		 * Adds the entities.
		 * @param text
		 *            the text
		 * @return the string
		 */
		private String addEntities(String text) {
			StringBuffer result = new StringBuffer();
			boolean inTag = false;
			int start = text.indexOf("<"); //$NON-NLS-1$
			int end = text.indexOf(">"); //$NON-NLS-1$
			if (end != -1) {
				if (start == -1) {
					inTag = true;
				} else {
					if (end < start) {
						inTag = true;
					}
				}
			}
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (c == '<') {
					inTag = true;
				}
				if (c == '>') {
					inTag = false;
				}
				if (!inTag && entities.containsKey("" + c)) { //$NON-NLS-1$
					if (c == '&' && text.charAt(i + 1) == '#') {
						// check if it is an escaped entity
						// like: &amp;#x2018;
						int scolon = text.indexOf(";", i); //$NON-NLS-1$
						if (scolon == -1) {
							// not an escaped entity
							result.append(entities.get("" + c)); //$NON-NLS-1$
						} else {
							// check for space before the semicolon
							int space = text.indexOf(" ", i); //$NON-NLS-1$
							if (space == -1) {
								// no space before the semicolon
								// it is an escaped entity
								result.append(c);
							} else {
								if (space > scolon) {
									// space is after semicolon
									// it is an escaped entity
									result.append(c);
								} else {
									// not an escaped entity
									result.append(entities.get("" + c)); //$NON-NLS-1$
								}
							}
						}
					} else {
						result.append(entities.get("" + c)); //$NON-NLS-1$
					}
				} else {
					result.append(c);
				}
			}
			return result.toString();
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
				throw new OperationCanceledException(Messages.getString("html.cancel"));
			}
			if (!isSegment) {
				cpb.calculateProcessed(replaceMonitor, string, encoding);
			} else {
				replaceCode = "%%%" + replaceCode + "%%%";
				cpb.calculateProcessed(replaceMonitor, replaceCode, encoding);
			}
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
				// 在加载 xliff 文件时，构建 dom tree 的进度无法控制，所以在此取任务总量为未知。
				monitor.beginTask(Messages.getString("html.Xliff2Html.msg4"), IProgressMonitor.UNKNOWN);
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
						throw new OperationCanceledException(Messages.getString("html.cancel"));
					}
					Element unit = i.next();
					segments.put(unit.getAttributeValue("id"), unit); //$NON-NLS-1$
				}
			} finally {
				monitor.done();
			}
		}
	}

}
