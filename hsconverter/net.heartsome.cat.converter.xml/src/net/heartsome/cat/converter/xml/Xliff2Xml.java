/**
 * Xliff2Xml.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.xml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.CalculateProcessedBytes;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.util.ReverseConversionInfoLogRecord;
import net.heartsome.cat.converter.xml.resource.Messages;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The Class Xliff2Xml.
 * @author John Zhu
 */
public class Xliff2Xml implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "xml";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("xml.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to XML Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2XmlImpl converter = new Xliff2XmlImpl();
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
	 * The Class Xliff2XmlImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2XmlImpl {

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

		/** The encoding. */
		private String encoding;

		/** The catalogue. */
		private Catalogue catalogue;

		/** The entities. */
		private Hashtable<String, String> entities;

		/** The in design. */
		private boolean inDesign = false;

		/** The in attribute. */
		private boolean inAttribute;

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
			String isInDesign = params.get(Converter.ATTR_IS_INDESIGN);
			String outputFile = params.get(Converter.ATTR_TARGET_FILE);
			if (isInDesign != null) {
				inDesign = true;
			}
			encoding = params.get(Converter.ATTR_SOURCE_ENCODING);

			String attrIsPreviewMode = params.get(Converter.ATTR_IS_PREVIEW_MODE);
			/* 是否为预览翻译模式 */
			boolean isPreviewMode = attrIsPreviewMode != null && attrIsPreviewMode.equals(Converter.TRUE);

			try {
				infoLogger.logConversionFileInfo(null, null, xliffFile, sklFile);
				// 把转换过程分为两大部分共 10 个任务，其中加载 xliff 文件占 4，替换过程占 6。
				monitor.beginTask("", 10);
				monitor.subTask(Messages.getString("xml.Xliff2Xml.task1"));
				infoLogger.startLoadingXliffFile();
				catalogue = new Catalogue(catalogueFile);
				output = new FileOutputStream(outputFile);
				loadSegments();
				infoLogger.endLoadingXliffFile();
				monitor.worked(4);
				// 是否取消
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("xml.cancel"));
				}
				try {
					infoLogger.startReplacingSegmentSymbol();
					cpb = ConverterUtils.getCalculateProcessedBytes(sklFile);
					replaceMonitor = Progress.getSubMonitor(monitor, 6);
					replaceMonitor.beginTask(Messages.getString("xml.Xliff2Xml.task2"), cpb.getTotalTask());
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
								line = line.substring(line.indexOf("%%%\n") + 4); //$NON-NLS-1$
								Element segment = segments.get(code);
								if (segment != null) {
									inAttribute = segment.getAttributeValue("restype", "").equals("x-attribute"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
									ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
											MessageFormat.format(Messages.getString("xml.Xliff2Xml.msg1"), code));
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
				output = null;

				if (inDesign) {
					removeSeparators(outputFile, catalogue);
				}
				result.put(Converter.ATTR_TARGET_FILE, outputFile);
				infoLogger.endConversion();
			} catch (OperationCanceledException e) {
				throw e;
			} catch (ConverterException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("xml.Xliff2Xml.msg2"), e);
			} finally {
				monitor.done();
			}
			return result;
		}

		/**
		 * Removes the separators.
		 * @param outputFile
		 *            the output file
		 * @param catalogue2
		 *            the catalogue2
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void removeSeparators(String outputFile, Catalogue catalogue2) throws SAXException, IOException {
			SAXBuilder builder = new SAXBuilder();
			builder.setEntityResolver(catalogue2);
			Document doc = builder.build(outputFile);
			Element root = doc.getRootElement();
			recurse(root);
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			output = new FileOutputStream(outputFile);
			outputter.output(doc, output);
			output.close();
		}

		/**
		 * Extract text.
		 * @param element
		 *            the element
		 * @return the string
		 */
		private String extractText(Element element) {
			String result = ""; //$NON-NLS-1$
			List<Node> content = element.getContent();
			Iterator<Node> i = content.iterator();
			while (i.hasNext()) {
				Node n = i.next();
				switch (n.getNodeType()) {
				case Node.ELEMENT_NODE:
					Element e = new Element(n);
					String ph = extractText(e);
					result = result + ph;
					break;
				case Node.TEXT_NODE:
					if (element.getName().equals("ph")) { //$NON-NLS-1$
						result = result + n.getNodeValue();
					} else {
						if (inAttribute) {
							result = result + addEntities(n.getNodeValue()).replaceAll("\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							result = result + addEntities(n.getNodeValue());
						}
					}
					break;
				default:
					break;
				}
			}
			return result;
		}

		/**
		 * Adds the entities.
		 * @param string
		 *            the string
		 * @return the string
		 */
		private String addEntities(String string) {

			int index = string.indexOf("&"); //$NON-NLS-1$
			while (index != -1) {
				int smcolon = string.indexOf(";", index); //$NON-NLS-1$
				if (smcolon == -1) {
					// no semicolon. there is no chance it is an entity
					string = string.substring(0, index) + "&amp;" //$NON-NLS-1$
							+ string.substring(index + 1);
					index++;
				} else {
					int space = string.indexOf(" ", index); //$NON-NLS-1$
					if (space == -1) {
						String name = string.substring(index + 1, smcolon);
						if (entities.containsKey(name)) {
							// it is an entity, jump to the semicolon
							index = smcolon;
						} else {
							string = string.substring(0, index) + "&amp;" //$NON-NLS-1$
									+ string.substring(index + 1);
							index++;
						}
					} else {
						// check if space appears before the semicolon
						if (space < smcolon) {
							// not an entity!
							string = string.substring(0, index) + "&amp;" //$NON-NLS-1$
									+ string.substring(index + 1);
							index++;
						} else {
							String name = string.substring(index + 1, smcolon);
							if (entities.containsKey(name)) {
								// it is a known entity, jump to the semicolon
								index = smcolon;
							} else {
								string = string.substring(0, index) + "&amp;" //$NON-NLS-1$
										+ string.substring(index + 1);
								index++;
							}
						}
					}
				}
				if (index < string.length() && index >= 0) {
					index = string.indexOf("&", index); //$NON-NLS-1$
				} else {
					index = -1;
				}
			}
			StringTokenizer tok = new StringTokenizer(string, "<>", true); //$NON-NLS-1$
			StringBuffer buff = new StringBuffer();
			while (tok.hasMoreElements()) {
				String str = tok.nextToken();
				if (str.equals("<")) { //$NON-NLS-1$
					buff.append("&lt;"); //$NON-NLS-1$
				} else if (str.equals(">")) { //$NON-NLS-1$
					buff.append("&gt;"); //$NON-NLS-1$
				} else {
					buff.append(str);
				}
			}
			string = buff.toString();
			// now replace common text with
			// the entities declared in the DTD

			Enumeration<String> enu = entities.keys();
			while (enu.hasMoreElements()) {
				String key = enu.nextElement();
				String value = entities.get(key);
				if (!value.equals("") && !key.equals("amp") && !key.equals("lt") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						&& !key.equals("gt") && !key.equals("quot") && !key.equals("apos")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					string = replaceEntities(string, value, "&" + key + ";"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			enu = null;
			tok = null;
			buff = null;

			return string;
		}

		/**
		 * Write string.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeString(String string) throws IOException {
			writeString(string, false, encoding);
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
				throw new OperationCanceledException(Messages.getString("xml.cancel"));
			}
			// 在计算已处理的字节时，需要用 skeleton 文件的源编码进行解码
			if (!isSegment) {
				cpb.calculateProcessed(replaceMonitor, string, UTF_8);
			} else {
				replaceCode = "%%%" + replaceCode + "%%%";
				cpb.calculateProcessed(replaceMonitor, replaceCode, UTF_8);
			}
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

			entities = new Hashtable<String, String>();

			Element header = root.getChild("file").getChild("header"); //$NON-NLS-1$ //$NON-NLS-2$
			List<Element> groups = header.getChildren("hs:prop-group"); //$NON-NLS-1$
			if (groups != null) {
				Iterator<Element> g = groups.iterator();
				while (g.hasNext()) {
					Element group = g.next();
					if (group.getAttributeValue("name").equals("entities")) { //$NON-NLS-1$ //$NON-NLS-2$
						List<Element> props = group.getChildren("hs:prop"); //$NON-NLS-1$
						Iterator<Element> p = props.iterator();
						while (p.hasNext()) {
							Element prop = p.next();
							entities.put(prop.getAttributeValue("prop-type"), prop //$NON-NLS-1$
									.getText());
						}
					}
					if (group.getAttributeValue("name").equals("encoding")) { //$NON-NLS-1$ //$NON-NLS-2$
						String stored = group.getChild("hs:prop").getText(); //$NON-NLS-1$
						if (!stored.equals(encoding)) {
							encoding = stored;
						}
					}
				}
			}
		}
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
	protected static String replaceToken(String string, String token, String newText) {
		int index = string.indexOf(token);
		while (index != -1) {
			string = string.substring(0, index) + newText + string.substring(index + token.length());
			index = string.indexOf(token, index + newText.length());
		}
		return string;
	}

	/**
	 * Recurse.
	 * @param e
	 *            the e
	 */
	private static void recurse(Element e) {
		List<Node> content = e.getContent();
		for (int i = 0; i < content.size(); i++) {
			Node n = content.get(i);
			switch (n.getNodeType()) {
			case Node.TEXT_NODE:
				String text = n.getNodeValue();
				if (text.startsWith("c_")) { //$NON-NLS-1$
					text = "c_" + text.substring(2).replaceAll("_", "~sep~"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				break;
			case Node.ELEMENT_NODE:
				recurse(new Element(n));
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Replace entities.
	 * @param string
	 *            the string
	 * @param token
	 *            the token
	 * @param entity
	 *            the entity
	 * @return the string
	 */
	private static String replaceEntities(String string, String token, String entity) {
		int index = string.indexOf(token);
		while (index != -1) {
			String before = string.substring(0, index);
			String after = string.substring(index + token.length());
			// check if we are not inside an entity
			int amp = before.lastIndexOf("&"); //$NON-NLS-1$
			if (amp == -1) {
				// we are not in an entity
				string = before + entity + after;
			} else {
				boolean inEntity = true;
				for (int i = amp; i < before.length(); i++) {
					char c = before.charAt(i);
					if (Character.isWhitespace(c) || ";.@$*()[]{},/?\\\"\'+=-^".indexOf(c) != -1) { //$NON-NLS-1$
						inEntity = false;
						break;
					}
				}
				if (inEntity) {
					// check for a colon in "after"
					int colon = after.indexOf(";"); //$NON-NLS-1$
					if (colon == -1) {
						// we are not in an entity
						string = before + entity + after;
					} else {
						// verify is there is something that breaks the entity
						// before
						for (int i = 0; i < colon; i++) {
							char c = after.charAt(i);
							if (Character.isWhitespace(c) || "&.@$*()[]{},/?\\\"\'+=-^".indexOf(c) != -1) { //$NON-NLS-1$
								inEntity = false;
								break;
							}
						}
					}
				} else {
					// we are not in an entity
					string = before + entity + after;
				}
			}
			if (index < string.length()) {
				index = string.indexOf(token, index + 1);
			}
		}
		return string;
	}
}
