/**
 * Xml2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.xml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.xml.resource.Messages;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;
import net.heartsome.xml.Attribute;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.w3c.dom.CDATASection;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDParser;

/**
 * The Class Xml2Xliff.
 * @author John Zhu
 */
public class Xml2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "xml";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("xml.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XML to XLIFF Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xml2XliffImpl converter = new Xml2XliffImpl();
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
	 * The Class Xml2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xml2XliffImpl {

		/** The input file. */
		private String inputFile;

		/** The xliff file. */
		private String xliffFile;

		/** The skeleton file. */
		private String skeletonFile;

		/** The source language. */
		private String sourceLanguage;

		private String targetLanguage;

		/** The src encoding. */
		private String srcEncoding;

		/** The input. */
		private FileInputStream input;

		/** The output. */
		private FileOutputStream output;

		/** The skeleton. */
		private FileOutputStream skeleton;

		/** The seg id. */
		private int segId;

		/** The tag id. */
		private int tagId;

		/** The segments. */
		private List<String> segments;

		/** The starts segment. */
		private Hashtable<String, String> startsSegment;

		/** The translatable attributes. */
		private Hashtable<String, Vector<String>> translatableAttributes;

		/** The inline. */
		private Hashtable<String, String> inline;

		/** The ctypes. */
		private Hashtable<String, String> ctypes;

		/** The keep formating. */
		private Hashtable<String, String> keepFormating;

		/** The seg by element. */
		private boolean segByElement;

		/** The segmenter. */
		private StringSegmenter segmenter;

		/** The catalogue. */
		private String catalogue;

		/** The program folder. */
		private String programFolder;

		/** The root element. */
		private String rootElement;

		/** The entities. */
		private Hashtable<String, String> entities;

		/** The entities map. */
		private String entitiesMap;

		/** The root. */
		private Element root;

		/** The text. */
		private String text;

		/** The stack. */
		private Stack<String> stack;

		/** The translatable. */
		private String translatable = ""; //$NON-NLS-1$

		/** The in design. */
		private boolean inDesign = false;

		/** The ignore. */
		private Hashtable<String, String> ignore;

		// /** The generic. */
		// private boolean generic;

		/** The in attribute. */
		private boolean inAttribute;

		/** The is resx. */
		private boolean isResx;

		/** The start text. */
		private String startText;

		/** The end text. */
		private String endText;

		/** The is suite. */
		private boolean isSuite;

		/** The qt tool id. */
		private String qtToolID;

		/** The format. */
		private String format;

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
			// 把转换分成四个部分：写 xliff 文件头，构建 tables，构建列表，处理列表。
			monitor.beginTask("", 4);
			monitor.subTask(Messages.getString("xml.Xml2Xliff.task1"));
			Map<String, String> result = new HashMap<String, String>();
			segId = 1;
			stack = new Stack<String>();

			inputFile = params.get(Converter.ATTR_SOURCE_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
			sourceLanguage = params.get(Converter.ATTR_SOURCE_LANGUAGE);
			targetLanguage = params.get(Converter.ATTR_TARGET_LANGUAGE);
			srcEncoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			catalogue = params.get(Converter.ATTR_CATALOGUE);
			programFolder = params.get(Converter.ATTR_PROGRAM_FOLDER);
			if (programFolder == null) {
				programFolder = Converter.PROGRAM_FOLDER_DEFAULT_VALUE;
			}
			String elementSegmentation = params.get(Converter.ATTR_SEG_BY_ELEMENT);
			String initSegmenter = params.get(Converter.ATTR_SRX);
			String isInDesign = params.get(Converter.ATTR_IS_INDESIGN);
			format = params.get(Converter.ATTR_FORMAT);
			if (format == null || "".equals(format)) {
				format = TYPE_VALUE;
			}

			isSuite = false;
			if (Converter.TRUE.equalsIgnoreCase(params.get(Converter.ATTR_IS_SUITE))) {
				isSuite = true;
			}

			qtToolID = params.get(Converter.ATTR_QT_TOOLID) != null ? params.get(Converter.ATTR_QT_TOOLID)
					: Converter.QT_TOOLID_DEFAULT_VALUE;

			if (isInDesign != null) {
				inDesign = true;
			} else {
				inDesign = false;
			}

			if (params.get(Converter.ATTR_IS_RESX) != null) {
				isResx = true;
			} else {
				isResx = false;
			}

			// 如果根据文件找不相应的 ini file，则自动生成一个。
			// String isGeneric = params.get(Converter.ATTR_IS_GENERIC);
			// if (isGeneric != null && isGeneric.equals(Converter.TRUE)) {
			// generic = true;
			// } else {
			// generic = false;
			// }

			try {
				boolean autoConfiguration = false;
				String iniFile = getIniFile(inputFile);
				File f = new File(iniFile);
				if (!f.exists()) {
					// if (!generic) {
					// ConverterUtils.throwConverterException(Activator.PLUGIN_ID, MessageFormat.format(Messages
					// .getString("Xml2Xliff.10"), f.getName()));
					// }
					AutoConfiguration autoConfigurator = new AutoConfiguration();
					autoConfigurator.run(inputFile, f.getAbsolutePath(), catalogue);
					autoConfiguration = true;
				}

				if (elementSegmentation == null) {
					segByElement = false;
				} else {
					if (Converter.TRUE.equals(elementSegmentation)) {
						segByElement = true;
					} else {
						segByElement = false;
					}
				}

				if (!segByElement) {
					segmenter = new StringSegmenter(initSegmenter, sourceLanguage, catalogue);
				}

				String detected = getEncoding(inputFile);
				if (!srcEncoding.equals(detected)) {
					System.out.println("Changed encoding to " + detected); //$NON-NLS-1$
					srcEncoding = detected;
				}

				input = new FileInputStream(inputFile);
				skeleton = new FileOutputStream(skeletonFile);
				output = new FileOutputStream(xliffFile);
				writeHeader();
				monitor.worked(1);

				int size = input.available();
				byte[] array = new byte[size];
				if (size != input.read(array)) {
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("xml.Xml2Xliff.msg1"));
				}
				String file = new String(array, srcEncoding);
				// remove xml declaration and doctype
				int begin = file.indexOf("<" + rootElement); //$NON-NLS-1$
				if (begin != -1) {
					if (file.charAt(0) == '<') {
						writeSkeleton(file.substring(0, begin));
					} else {
						writeSkeleton(file.substring(1, begin));
					}
					file = file.substring(begin);
				}
				file = null;
				array = null;
				buildTables(iniFile, Progress.getSubMonitor(monitor, 1));
				if (autoConfiguration) {
					f.delete();
				}

				buildList(Progress.getSubMonitor(monitor, 1));

				processList(Progress.getSubMonitor(monitor, 1));

				skeleton.close();
				writeString("</body>\n"); //$NON-NLS-1$
				writeString("</file>\n"); //$NON-NLS-1$
				writeString("</xliff>"); //$NON-NLS-1$
				input.close();
				output.close();

				result.put(Converter.ATTR_XLIFF_FILE, xliffFile);
			} catch(OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("xml.Xml2Xliff.msg2"), e);
			} finally {
				monitor.done();
			}

			return result;
		}

		/**
		 * Gets the ini file.
		 * @param fileName
		 *            the file name
		 * @return the ini file
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws ParserConfigurationException
		 *             the parser configuration exception
		 * @throws ConverterException
		 *             the converter exception
		 */
		public String getIniFile(String fileName) throws SAXException, IOException, ParserConfigurationException,
				ConverterException {
			SAXBuilder builder = new SAXBuilder();
			Catalogue cat = new Catalogue(catalogue);
			builder.setEntityResolver(cat);
			/*
			 * set expandEntities to false if automatic inclusion of referenced documents is not desired. It is enabled
			 * by default, but may change later upon request.
			 */
			// builder.expandEntities(false);
			Document doc = builder.build(fileName);
			entities = new Hashtable<String, String>();

			NamedNodeMap map = doc.getEntities();

			entitiesMap = ""; //$NON-NLS-1$
			if (map != null) {
				int ents = map.getLength();
				for (int i = 0; i < ents; i++) {
					Node n = map.item(i);
					String value = n.getNodeValue();
					if (value == null) {
						Node first = n.getFirstChild();
						if (first != null) {
							value = first.getNodeValue();
						}
					}
					if (value != null) {
						entities.put("&" + n.getNodeName() + ";", value); //$NON-NLS-1$ //$NON-NLS-2$
					}
					n = null;
				}
				Enumeration<String> en = entities.keys();
				while (en.hasMoreElements()) {
					String key = en.nextElement();
					entitiesMap = entitiesMap + "      <prop prop-type=\"" //$NON-NLS-1$
							+ key.substring(1, key.length() - 1) + "\">" //$NON-NLS-1$
							+ cleanEntity(entities.get(key)) + "</prop>\n"; //$NON-NLS-1$
				}
				map = null;
			}
			// Add predefined standard entities
			entities.put("&gt;", ">"); //$NON-NLS-1$ //$NON-NLS-2$
			entities.put("&lt;", "<"); //$NON-NLS-1$ //$NON-NLS-2$
			entities.put("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
			entities.put("&apos;", "'"); //$NON-NLS-1$ //$NON-NLS-2$
			entities.put("&quot;", "\""); //$NON-NLS-1$ //$NON-NLS-2$

			root = doc.getRootElement();
			rootElement = root.getName();
			// check for ResX before anything else
			// this requires a fixed ini name
			if (root.getName().equals("root")) { //$NON-NLS-1$
				List<Element> dataElements = root.getChildren("data"); //$NON-NLS-1$
				if (dataElements.size() > 0) {
					for (int i = 0; i < dataElements.size(); i++) {
						Element g = (dataElements.get(i)).getChild("translate"); //$NON-NLS-1$
						if (g != null) {
							isResx = true;
							break;
						}
					}
					if (isResx) {
						return programFolder + "ini/config_resx.xml"; //$NON-NLS-1$
					}
				}
			}
			String pub = doc.getPublicId();
			String sys = doc.getSystemId();
			if (sys != null) {
				// remove path from systemId
				if (sys.indexOf("/") != -1 && sys.lastIndexOf("/") < sys.length()) { //$NON-NLS-1$ //$NON-NLS-2$
					sys = sys.substring(sys.lastIndexOf("/") + 1); //$NON-NLS-1$
				}
				if (sys.indexOf("\\") != -1 && sys.lastIndexOf("/") < sys.length()) { //$NON-NLS-1$ //$NON-NLS-2$
					sys = sys.substring(sys.lastIndexOf("\\") + 1); //$NON-NLS-1$
				}
			}

			Document d = builder.build(catalogue);
			Element r = d.getRootElement();
			List<Element> dtds = r.getChildren("dtd"); //$NON-NLS-1$
			Iterator<Element> i = dtds.iterator();
			while (i.hasNext()) {
				Element dtd = i.next();
				if (pub != null) {
					if (dtd.getAttributeValue("publicId", "").equals(pub)) { //$NON-NLS-1$ //$NON-NLS-2$
						String s = getRootElement(dtd.getText());
						if (s != null) {
							System.out
									.println("Detected configuration file using PUBLIC ID : ini/config_" + s + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
							return programFolder + "ini/config_" + s + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
				if (sys != null) {
					if (dtd.getAttributeValue("systemId", "").equals(sys)) { //$NON-NLS-1$ //$NON-NLS-2$
						String s = getRootElement(dtd.getText());
						if (s != null) {
							System.out
									.println("Detected configuration file using SYSTEM ID : ini/config_" + s + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
							return programFolder + "ini/config_" + s + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
					if (dtd.getAttributeValue("systemId", "").endsWith(sys)) { //$NON-NLS-1$ //$NON-NLS-2$
						String s = getRootElement(dtd.getText());
						if (s != null) {
							System.out
									.println("Detected configuration file using DTD name in SYSTEM ID : ini/config_" + s + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
							return programFolder + "ini/config_" + s + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			}

			if (rootElement.indexOf(":") != -1) { //$NON-NLS-1$
				System.out.println("Detected configuration file using namespace: " //$NON-NLS-1$
						+ rootElement.substring(0, rootElement.indexOf(":"))); //$NON-NLS-1$
				return programFolder + "ini/config_" //$NON-NLS-1$
						+ rootElement.substring(0, rootElement.indexOf(":")) //$NON-NLS-1$
						+ ".xml"; //$NON-NLS-1$
			}

			System.out.println("Detected configuration file using root element: " + rootElement); //$NON-NLS-1$
			return programFolder + "ini/config_" + rootElement + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$

		}

		/**
		 * Clean entity.
		 * @param s
		 *            the s
		 * @return the string
		 */
		private String cleanEntity(String s) {
			int control = s.indexOf("&"); //$NON-NLS-1$
			while (control != -1) {
				int sc = s.indexOf(";", control); //$NON-NLS-1$
				if (sc == -1) {
					// no semicolon, it's not an entity
					s = s.substring(0, control) + "&amp;" //$NON-NLS-1$
							+ s.substring(control + 1);
				} else {
					// may be an entity
					String candidate = s.substring(control, sc) + ";"; //$NON-NLS-1$
					if (!candidate.equals("&amp;")) { //$NON-NLS-1$
						String entity = entities.get(candidate);
						if (entity != null) {
							s = s.substring(0, control) + entity + s.substring(sc + 1);
						} else if (candidate.startsWith("&#x")) { //$NON-NLS-1$
							// it's a character in hexadecimal format
							int c = Integer.parseInt(candidate.substring(3, candidate.length() - 1), 16);
							s = s.substring(0, control) + (char) c + s.substring(sc + 1);
						} else if (candidate.startsWith("&#")) { //$NON-NLS-1$
							// it's a character
							int c = Integer.parseInt(candidate.substring(2, candidate.length() - 1));
							s = s.substring(0, control) + (char) c + s.substring(sc + 1);
						} else {
							s = s.substring(0, control) + "&amp;" //$NON-NLS-1$
									+ s.substring(control + 1);
						}
					}
				}
				if (control < s.length()) {
					control++;
				}
				control = s.indexOf("&", control); //$NON-NLS-1$
			}

			control = s.indexOf("<"); //$NON-NLS-1$
			while (control != -1) {
				s = s.substring(0, control) + "&lt;" + s.substring(control + 1); //$NON-NLS-1$
				if (control < s.length()) {
					control++;
				}
				control = s.indexOf("<", control); //$NON-NLS-1$
			}

			control = s.indexOf(">"); //$NON-NLS-1$
			while (control != -1) {
				s = s.substring(0, control) + "&gt;" + s.substring(control + 1); //$NON-NLS-1$
				if (control < s.length()) {
					control++;
				}
				control = s.indexOf(">", control); //$NON-NLS-1$
			}
			return s;
		}

		/**
		 * Gets the root element.
		 * @param string
		 *            the string
		 * @return the root element
		 * @throws ConverterException
		 *             the converter exception
		 */
		private String getRootElement(String string) throws ConverterException {
			String result = null;
			File dtd = new File(string);
			try {
				DTDParser parser = new DTDParser(dtd);
				DTD d = parser.parse(true);
				if (d != null) {
					if (d.rootElement != null) {
						result = d.rootElement.getName();
					}
				}
			} catch (IOException e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("xml.Xml2Xliff.msg3"), e);
			}
			return result;
		}

		/**
		 * Write header.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeHeader() throws IOException {
			writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
			writeString("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " + //$NON-NLS-1$
					"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xmlns:hs=\"" + Converter.HSNAMESPACE
					+ "\" " + //$NON-NLS-1$ 
					"xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd " //$NON-NLS-1$
					+ Converter.HSSCHEMALOCATION + "\">\n"); //$NON-NLS-1$
			if (!"".equals(targetLanguage)) {
				writeString("<file original=\"" + cleanString(inputFile) + "\" source-language=\"" //$NON-NLS-1$ //$NON-NLS-2$
						+ sourceLanguage
						+ "\" target-language=\"" + targetLanguage + "\" datatype=\"" + format + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				writeString("<file original=\"" + cleanString(inputFile) + "\" source-language=\"" //$NON-NLS-1$ //$NON-NLS-2$
						+ sourceLanguage + "\" datatype=\"" + format + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			writeString("<header>\n"); //$NON-NLS-1$
			writeString("   <skl>\n"); //$NON-NLS-1$
			String crc = ""; //$NON-NLS-1$
			if (isSuite) {
				crc = "crc=\"" + CRC16.crc16(TextUtil.cleanString(skeletonFile).getBytes("UTF-8")) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			writeString("      <external-file href=\"" + TextUtil.cleanString(skeletonFile) + "\" " + crc + "/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			writeString("   </skl>\n"); //$NON-NLS-1$
			writeString("   <tool tool-id=\"" + qtToolID + "\" tool-name=\"HSStudio\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			writeString("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\">" //$NON-NLS-1$
					+ srcEncoding + "</hs:prop></hs:prop-group>\n"); //$NON-NLS-1$
			if (!entitiesMap.equals("")) { //$NON-NLS-1$
				writeString("   <hs:prop-group name=\"entities\">\n" + entitiesMap //$NON-NLS-1$
						+ "   </hs:prop-group>\n"); //$NON-NLS-1$
			}
			writeString("</header>\n"); //$NON-NLS-1$
			writeString("<body>\n"); //$NON-NLS-1$
		}

		/**
		 * Process list.
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void processList(IProgressMonitor monitor) throws IOException, SAXException {
			// 设置任务数量
			monitor.beginTask(Messages.getString("xml.Xml2Xliff.task2"), segments.size());
			monitor.subTask("");
			for (int i = 0; i < segments.size(); i++) {
				// 检查是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("xml.cancel"));
				}
				monitor.worked(1);

				String txt = segments.get(i);
				if (txt.startsWith("" + '\u007F' + "" + '\u007F')) { //$NON-NLS-1$ //$NON-NLS-2$
					// send directly to skeleton
					writeSkeleton(txt.substring(2));
					continue;
				}
				if (txt.startsWith("" + '\u0080')) { //$NON-NLS-1$
					inAttribute = true;
					txt = txt.substring(1);
				} else {
					inAttribute = false;
				}
				if (inDesign && !txt.trim().equals("")) { //$NON-NLS-1$
					if (txt.startsWith("c_") && !txt.substring(2).trim().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
						writeSkeleton("c_"); //$NON-NLS-1$
						
						txt = txt.substring(2);
						txt.replaceAll("~sep~", "_"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						writeSkeleton(txt);
						continue;
					}
				}
				
				if (segByElement) {
			
					writeSegment(txt);
				} else {
					// 将单纯的格式信息提前写入骨架以减少标记。
					int inx = txt.indexOf("?>");
					while (inDesign && txt.startsWith("<?") && inx != -1) {
						writeSkeleton(txt.substring(0, inx + 2));
						txt = txt.substring(inx + 2);
						inx = txt.indexOf("?>");
					}
					String[] segs = segmenter.segment(txt);
					for (int h = 0; h < segs.length; h++) {
						String seg = segs[h];
						boolean isStartWith2028 = seg.startsWith("" + '\u2028');
						boolean isStartWith2029 = seg.startsWith("" + '\u2029');
						while (isStartWith2028 || isStartWith2029) {
							if (isStartWith2028) {
								writeSkeleton("" + '\u2028'); //$NON-NLS-1$
							} else {
								writeSkeleton("" + '\u2029'); //$NON-NLS-1$
							}
							seg = seg.substring(1);

							isStartWith2028 = seg.startsWith("" + '\u2028');
							isStartWith2029 = seg.startsWith("" + '\u2029');
						}
						writeSegment(seg);
					}
				}
			}
			monitor.done();
		}

		/**
		 * Write segment.
		 * @param segment
		 *            the segment
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeSegment(String segment) throws IOException, SAXException {
			tagId = 0;
			String restype = ""; //$NON-NLS-1$
			String tagged = addTags(segment);
			if (!containsText(tagged)) {
				writeSkeleton(segment);
				return;
			}
			if (inAttribute) {
				restype = " restype=\"x-attribute\""; //$NON-NLS-1$
			}
			String seg = "   <trans-unit id=\"" + segId //$NON-NLS-1$
					+ "\" xml:space=\"preserve\" approved=\"no\" " //$NON-NLS-1$
					+ restype + ">\n" //$NON-NLS-1$
					+ "      <source xml:lang=\"" + sourceLanguage + "\">" //$NON-NLS-1$ //$NON-NLS-2$
					+ tagged + "</source>\n   </trans-unit>\n"; //$NON-NLS-1$

			writeString(tidy(seg));

			writeSkeleton(startText + "%%%" + segId++ + "%%%\n" + endText); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * Tidy.
		 * @param seg
		 *            the seg
		 * @return the string
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String tidy(String seg) throws SAXException, IOException {
			startText = ""; //$NON-NLS-1$
			endText = ""; //$NON-NLS-1$
			List<Node> start = new ArrayList<Node>();
			List<Node> end = new ArrayList<Node>();
			List<Node> txt = new ArrayList<Node>();
			SAXBuilder b = new SAXBuilder();
			Document d = b.build(new ByteArrayInputStream(seg.getBytes("utf-8"))); //$NON-NLS-1$
			Element r = d.getRootElement();
			Element s = r.getChild("source"); //$NON-NLS-1$
			if (s.getChildren().size() == 0) {
				return seg;
			}
			List<Node> content = s.getContent();
			int i = 0;
			// skip initial tags
			for (i = 0; i < content.size(); i++) {
				Node n = content.get(i);
				if (n.getNodeType() == Node.TEXT_NODE) {
					if (!n.getNodeValue().trim().equals("")) { //$NON-NLS-1$
						// txt.add(n);
						break;
					}
					start.add(n);
				} else {
					start.add(n);
				}
			}
			// skip text nodes
			for (; i < content.size(); i++) {
				Node n = content.get(i);
				if (n.getNodeType() != Node.TEXT_NODE) {
					// end.add(n);
					break;
				}
				txt.add(n);
			}
			// skip final tags
			for (; i < content.size(); i++) {
				Node n = content.get(i);
				if (n.getNodeType() == Node.TEXT_NODE) {
					if (!n.getNodeValue().trim().equals("")) { //$NON-NLS-1$
						break;
					}
					end.add(n);
				} else {
					end.add(n);
				}
			}
			if (i == content.size()) {
				// OK, tags are only at start and end
				for (i = 0; i < start.size(); i++) {
					Node n = start.get(i);
					if (n.getNodeType() == Node.TEXT_NODE) {
						startText += n.getNodeValue();
					}
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						startText += (new Element(n).getText()).toString();
					}
				}
				for (i = 0; i < end.size(); i++) {
					Node n = end.get(i);
					if (n.getNodeType() == Node.TEXT_NODE) {
						endText += n.getNodeValue();
					}
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						endText += (new Element(n).getText()).toString();
					}
				}
				s.setContent(txt);
			}
			return r.toString();
		}

		/**
		 * Contains text.
		 * @param tagged
		 *            the tagged
		 * @return true, if contains text
		 */
		private boolean containsText(String tagged) {
			int start = tagged.indexOf("<ph"); //$NON-NLS-1$
			int end = tagged.indexOf("</ph>"); //$NON-NLS-1$
			while (start != -1) {
				tagged = tagged.substring(0, start) + tagged.substring(end + 5);
				start = tagged.indexOf("<ph"); //$NON-NLS-1$
				end = tagged.indexOf("</ph>"); //$NON-NLS-1$
			}
			tagged = tagged.trim();
			if (tagged.length() == 0) {
				return false;
			}else{
				return true;
			}
// 修改将数字以及其他的字符显示出来			
//			for (int i = 0; i < tagged.length(); i++) {
//				int c = tagged.charAt(i);
//				if ("$€￡￥￠(){}0123456789%.,-‐‑‒—―".indexOf(c) == -1) { //$NON-NLS-1$
//					return true;
//				}
//				/*
//				 * if (c < 8192 || c > 8303) { return true; }
//				 */
//			}
//			return false;
		}

		/**
		 * Normalize.
		 * @param string
		 *            the string
		 * @return the string
		 */
		private String normalize(String string) {
			string = string.replace('\n', ' ');
			string = string.replace('\t', ' ');
			string = string.replace('\r', ' ');
			string = string.replace('\f', ' ');
			String rs = ""; //$NON-NLS-1$
			int length = string.length();
			for (int i = 0; i < length; i++) {
				char ch = string.charAt(i);
				if (!Character.isSpaceChar(ch)) {
					rs = rs + ch;
				} else {
					rs = rs + " "; //$NON-NLS-1$
					while (i < (length - 1) && Character.isSpaceChar(string.charAt(i + 1))) {
						i++;
					}
				}
			}
			return rs;
		}

		/**
		 * Adds the tags.
		 * @param src
		 *            the src
		 * @return the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String addTags(String src) throws IOException {
			String result = ""; //$NON-NLS-1$
			int start = src.indexOf("<"); //$NON-NLS-1$
			int end = src.indexOf(">"); //$NON-NLS-1$

			while (start != -1) {
				if (start > 0) {
					result = result + cleanString(src.substring(0, start));
					src = src.substring(start);
					start = src.indexOf("<"); //$NON-NLS-1$
					end = src.indexOf(">"); //$NON-NLS-1$
				}
				String element = src.substring(start, end + 1);
				// if(element.length() == 0){
				// break;
				// }
				String tagged = tag(element);
				result = result + tagged;

				src = src.substring(end + 1);

				start = src.indexOf("<"); //$NON-NLS-1$
				end = src.indexOf(">"); //$NON-NLS-1$
			}
			result = result + cleanString(src);
			return result;
		}

		/**
		 * Tag.
		 * @param element
		 *            the element
		 * @return the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String tag(String element) throws IOException {
			String result = ""; //$NON-NLS-1$
			String type = getType(element);
			if (translatableAttributes.containsKey(type)) {
				result = extractAttributes(type, element);
			} else {
				String ctype = ""; //$NON-NLS-1$
				if (ctypes.containsKey(type)) {
					ctype = " ctype=\"" + ctypes.get(type) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
				}
				result = "<ph id=\"" + tagId++ + "\"" + ctype + ">" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ cleanString(element) + "</ph>"; //$NON-NLS-1$
			}
			return result;
		}

		/**
		 * Clean string.
		 * @param s
		 *            the s
		 * @return the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String cleanString(String s) throws IOException {
			int control = s.indexOf("&"); //$NON-NLS-1$
			while (control != -1) {
				int sc = s.indexOf(";", control); //$NON-NLS-1$
				if (sc == -1) {
					// no semicolon, it's not an entity
					s = s.substring(0, control) + "&amp;" //$NON-NLS-1$
							+ s.substring(control + 1);
				} else {
					// may be an entity
					String candidate = s.substring(control, sc) + ";"; //$NON-NLS-1$
					if (!candidate.equals("&amp;")) { //$NON-NLS-1$
						String entity = entities.get(candidate);
						if (entity != null) {
							s = s.substring(0, control) + entity + s.substring(sc + 1);
						} else {
							s = s.substring(0, control) + "%%%ph id=\"" + tagId++ //$NON-NLS-1$
									+ "\"%%%&amp;" + candidate.substring(1) //$NON-NLS-1$
									+ "%%%/ph%%%" + s.substring(sc + 1); //$NON-NLS-1$
						}
					}

					// remarked by john. fixed a bug 1107 by john.
					// } else {
					// // it is an "&amp;"
					//                	s = s.substring(0, control) + "&amp;" //$NON-NLS-1$
					// + s.substring(control + 1);
					// }
				}
				if (control < s.length()) {
					control++;
				}
				control = s.indexOf("&", control); //$NON-NLS-1$
			}

			control = s.indexOf("<"); //$NON-NLS-1$
			while (control != -1) {
				s = s.substring(0, control) + "&lt;" + s.substring(control + 1); //$NON-NLS-1$
				if (control < s.length()) {
					control++;
				}
				control = s.indexOf("<", control); //$NON-NLS-1$
			}

			control = s.indexOf(">"); //$NON-NLS-1$
			while (control != -1) {
				s = s.substring(0, control) + "&gt;" + s.substring(control + 1); //$NON-NLS-1$
				if (control < s.length()) {
					control++;
				}
				control = s.indexOf(">", control); //$NON-NLS-1$
			}
			s = s.replaceAll("%%%/ph%%%", "</ph>"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("%%%ph", "<ph"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("\"%%%&amp;", "\">&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
			return XMLOutputter.validChars(s);
		}

		/**
		 * Write skeleton.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeSkeleton(String string) throws IOException {
			skeleton.write(string.getBytes("UTF-8")); //$NON-NLS-1$
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
		 * Extract attributes.
		 * @param type
		 *            the type
		 * @param element
		 *            the element
		 * @return the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String extractAttributes(String type, String element) throws IOException {

			String ctype = ""; //$NON-NLS-1$
			if (ctypes.containsKey(type)) {
				ctype = " ctype=\"" + ctypes.get(type) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
			String result = "<ph id=\"" + tagId++ + "\"" + ctype + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			element = cleanString(element);

			Vector<String> v = translatableAttributes.get(type);

			StringTokenizer tokenizer = new StringTokenizer(element, "&= \t\n\r\f/", //$NON-NLS-1$
					true);
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (!v.contains(token)) {
					result = result + token;
				} else {
					result = result + token;
					String s = tokenizer.nextToken();
					while (s.equals("=") || s.equals(" ")) { //$NON-NLS-1$ //$NON-NLS-2$
						result = result + s;
						s = tokenizer.nextToken();
					}
					// s contains the first word of the attribute
					if (((s.startsWith("\"") && s.endsWith("\"")) || (s //$NON-NLS-1$ //$NON-NLS-2$
							.startsWith("'") && s.endsWith("'"))) //$NON-NLS-1$ //$NON-NLS-2$
							&& s.length() > 1) {
						// the value is one word and it is quoted
						result = result + s.substring(0, 1) + "</ph>" //$NON-NLS-1$
								+ s.substring(1, s.length() - 1) + "<ph id=\"" //$NON-NLS-1$
								+ tagId++ + "\">" + s.substring(s.length() - 1); //$NON-NLS-1$
					} else {
						if (s.startsWith("\"") || s.startsWith("'")) { //$NON-NLS-1$ //$NON-NLS-2$
							// attribute value is quoted, but it has more than
							// one
							// word
							String quote = s.substring(0, 1);
							result = result + s.substring(0, 1) + "</ph>" //$NON-NLS-1$
									+ s.substring(1);
							s = tokenizer.nextToken();
							do {
								result = result + s;
								if (tokenizer.hasMoreElements()) {
									s = tokenizer.nextToken();
								}
							} while (s.indexOf(quote) == -1);
							String left = s.substring(0, s.indexOf(quote));
							String right = s.substring(s.indexOf(quote));
							result = result + left + "<ph id=\"" + tagId++ + "\">" //$NON-NLS-1$ //$NON-NLS-2$
									+ right;
						} else {
							// attribute is not quoted, it can only be one word
							result = result + "</ph>" + s + "<ph id=\"" + tagId++ //$NON-NLS-1$ //$NON-NLS-2$
									+ "\">"; //$NON-NLS-1$
						}
					}
				}
			}
			result = result + "</ph>"; //$NON-NLS-1$
			return result;
		}

		/**
		 * Builds the tables.
		 * @param iniFile
		 *            the ini file
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws ParserConfigurationException
		 *             the parser configuration exception
		 */
		private void buildTables(String iniFile, IProgressMonitor monitor) throws SAXException, IOException,
				ParserConfigurationException {

			SAXBuilder builder = new SAXBuilder();
			builder.setEntityResolver(new Catalogue(catalogue));
			Document doc = builder.build(iniFile);
			Element rt = doc.getRootElement();
			List<Element> tags = rt.getChildren("tag"); //$NON-NLS-1$

			startsSegment = new Hashtable<String, String>();
			translatableAttributes = new Hashtable<String, Vector<String>>();
			ignore = new Hashtable<String, String>();
			ctypes = new Hashtable<String, String>();
			keepFormating = new Hashtable<String, String>();
			inline = new Hashtable<String, String>();

			// 设置任务数量
			int size = tags.size();
			monitor.beginTask(Messages.getString("xml.Xml2Xliff.task3"), size);
			monitor.subTask("");

			Iterator<Element> i = tags.iterator();
			while (i.hasNext()) {
				// 检查取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("xml.cancel"));
				}
				monitor.worked(1);

				Element t = i.next();
				if (t.getAttributeValue("hard-break", "inline").equals("yes") || t.getAttributeValue("hard-break", "inline").equals("segment")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					startsSegment.put(t.getText(), "yes"); //$NON-NLS-1$
				} else if (t.getAttributeValue("hard-break", "inline").equals("no") || t.getAttributeValue("hard-break", "inline").equals("inline")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					inline.put(t.getText(), "yes"); //$NON-NLS-1$
				} else {
					ignore.put(t.getText(), "yes"); //$NON-NLS-1$
				}
				if (t.getAttributeValue("keep-format", "no").equals("yes")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					keepFormating.put(t.getText(), "yes"); //$NON-NLS-1$
				}
				String attributes = t.getAttributeValue("attributes", ""); //$NON-NLS-1$ //$NON-NLS-2$
				if (!attributes.equals("")) { //$NON-NLS-1$
					StringTokenizer tokenizer = new StringTokenizer(attributes, ";"); //$NON-NLS-1$
					int count = tokenizer.countTokens();
					Vector<String> v = new Vector<String>(count);
					for (int j = 0; j < count; j++) {
						v.add(tokenizer.nextToken());
					}
					translatableAttributes.put(t.getText(), v);
				}
				String ctype = t.getAttributeValue("ctype", ""); //$NON-NLS-1$ //$NON-NLS-2$
				if (!ctype.equals("")) { //$NON-NLS-1$
					ctypes.put(t.getText(), ctype);
				}
				t = null;
			}
			tags = null;
			rt = null;
			doc = null;
			builder = null;
			monitor.done();
		}

		/**
		 * Builds the list.
		 * @throws Exception
		 *             the exception
		 */
		private void buildList(IProgressMonitor monitor) throws Exception {
			monitor.beginTask(Messages.getString("xml.Xml2Xliff.task4"), IProgressMonitor.UNKNOWN);
			monitor.subTask("");
			segments = new ArrayList<String>();
			text = ""; //$NON-NLS-1$
			Node n = root.getElement();
			parseNode(n);
			segments.add(text);
			monitor.done();
		}

		/**
		 * Parses the node.
		 * @param n
		 *            the n
		 * @throws Exception
		 *             the exception
		 */
		private void parseNode(Node n) throws Exception {
			switch (n.getNodeType()) {
			case Node.ATTRIBUTE_NODE:
				throw new Exception(Messages.getString("xml.Xml2Xliff.msg4") + n); //$NON-NLS-1$
			case Node.CDATA_SECTION_NODE:
				segments.add(text);
				CDATASection cdata = (CDATASection) n;
				segments.add("" + '\u007F' + '\u007F' + "<![CDATA[" + cdata.getNodeValue() + "]]>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				translatable = ""; //$NON-NLS-1$
				text = ""; //$NON-NLS-1$
				break;
			case Node.COMMENT_NODE:
				segments.add(text);
				segments.add("" + '\u007F' + '\u007F' + "<!--" + n.getNodeValue() + "-->"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				translatable = ""; //$NON-NLS-1$
				text = ""; //$NON-NLS-1$
				break;
			case Node.DOCUMENT_FRAGMENT_NODE:
				throw new Exception(Messages.getString("xml.Xml2Xliff.msg5") + n); //$NON-NLS-1$
			case Node.DOCUMENT_NODE:
				throw new Exception(Messages.getString("xml.Xml2Xliff.msg6") + n); //$NON-NLS-1$
			case Node.DOCUMENT_TYPE_NODE:
				throw new Exception(Messages.getString("xml.Xml2Xliff.msg7") + n); //$NON-NLS-1$
			case Node.ELEMENT_NODE:
				Element e = new Element(n);
				if (startsSegment.containsKey(e.getName())) {
					segments.add(text);
					text = ""; //$NON-NLS-1$
					translatable = ""; //$NON-NLS-1$
					stack = null;
					stack = new Stack<String>();
					stack.push(e.getName());
					if (!keepFormating.containsKey(e.getName())
							&& !e.getAttributeValue("xml:space", "default").equals("preserve")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						normalizeElement(e);
					}
				}
				if (ignore.containsKey(e.getName())) {
					segments.add(text);
					segments.add("" + '\u007F' + "" + '\u007F' + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
					text = ""; //$NON-NLS-1$
					translatable = ""; //$NON-NLS-1$
					stack = null;
					stack = new Stack<String>();
					return;
				}
				if (stack.size() == 0 && e.getChildren().size() == 0
						&& !translatableAttributes.containsKey(e.getName())) {
					if (inline.containsKey(e.getName()) && !e.getText().equals("")) { //$NON-NLS-1$
						segments.add(text);
						text = ""; //$NON-NLS-1$
						translatable = ""; //$NON-NLS-1$
						stack = null;
						stack = new Stack<String>();
						stack.push(e.getName());
						if (!keepFormating.containsKey(e.getName())
								&& !e.getAttributeValue("xml:space", "default").equals("preserve")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							normalizeElement(e);
						}
					} else {
						segments.add(text);
						text = ""; //$NON-NLS-1$
						translatable = ""; //$NON-NLS-1$
						segments.add('\u007F' + "" + '\u007F' + e.toString()); //$NON-NLS-1$
						break;
					}
				}
				if (stack.size() > 0 && !startsSegment.containsKey(e.getName())) {
					stack.push(e.getName());
				}
				List<Attribute> attributes = e.getAttributes();
				text = text + "<" + e.getName(); //$NON-NLS-1$
				if (attributes.size() > 0) {
					for (int i = 0; i < attributes.size(); i++) {
						Attribute a = attributes.get(i);
						Vector<String> attr = translatableAttributes.get(e.getName());
						if (translatableAttributes.containsKey(e.getName()) && attr.contains(a.getName())) {
							//
							// This is a translatable attribute and all quotes
							// should be escaped at reverse conversion time
							//
							if (!text.startsWith("" + '\u007F' + '\u007F')) { //$NON-NLS-1$
								text = "" + '\u007F' + '\u007F' + text; //$NON-NLS-1$
							}
							segments.add(text + " " + a.getName() + "=\""); //$NON-NLS-1$ //$NON-NLS-2$
							segments.add('\u0080' + cleanAttribute(a.getValue()));
							segments.add("" + '\u007F' + '\u007F' + "\""); //$NON-NLS-1$ //$NON-NLS-2$
							text = ""; //$NON-NLS-1$
							translatable = ""; //$NON-NLS-1$
						} else {
							if (!inline.containsKey(e.getName())
									&& !text.startsWith("" + '\u007F' + '\u007F') && translatable.length() == 0) { //$NON-NLS-1$
								text = "" + '\u007F' + '\u007F' + text; //$NON-NLS-1$
							}
							text = text + " " + a.getName() + "=\"" + cleanAttribute(a.getValue()) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
				}
				List<Node> content = e.getContent();
				if (content.size() == 0) {
					if (text.equals("")) { //$NON-NLS-1$
						text = "" + '\u007F' + '\u007F' + "/>"; //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						text = text + "/>"; //$NON-NLS-1$
					}
				} else {
					if (!inline.containsKey(e.getName())) {
						if (!text.equals("")) { //$NON-NLS-1$
							segments.add(text + ">"); //$NON-NLS-1$
							text = ""; //$NON-NLS-1$
						} else {
							segments.add("" + '\u007F' + '\u007F' + ">"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						translatable = ""; //$NON-NLS-1$
					} else {
						if (!text.equals("")) { //$NON-NLS-1$
							text = text + ">"; //$NON-NLS-1$
						} else {
							segments.add("" + '\u007F' + '\u007F' + ">"); //$NON-NLS-1$ //$NON-NLS-2$
							translatable = ""; //$NON-NLS-1$
						}
					}
					for (int i = 0; i < content.size(); i++) {
						parseNode(content.get(i));
					}
					if (startsSegment.containsKey(e.getName())) {
						segments.add(text);
						text = ""; //$NON-NLS-1$
						translatable = ""; //$NON-NLS-1$
					}
					if (!text.equals("")) { //$NON-NLS-1$
						text = text + "</" + e.getName() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						segments.add("" + '\u007F' + '\u007F' + "</" + e.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
				if (stack.size() > 0) {
					stack.pop();
				}

				break;
			case Node.ENTITY_NODE:
				throw new Exception(Messages.getString("xml.Xml2Xliff.msg8") + n); //$NON-NLS-1$
			case Node.ENTITY_REFERENCE_NODE:
				EntityReference er = (EntityReference) n;
				String name = "&" + er.getNodeName() + ';'; //$NON-NLS-1$
				if (entities.containsKey(name)) {
					text = text + entities.get(name);
				} else {
					text = text + name;
				}
				break;
			case Node.NOTATION_NODE:
				throw new Exception(Messages.getString("xml.Xml2Xliff.msg9") + n); //$NON-NLS-1$
			case Node.PROCESSING_INSTRUCTION_NODE:
				ProcessingInstruction pi = (ProcessingInstruction) n;
				if (inDesign && !translatable.trim().equals("")) { //$NON-NLS-1$
					text = text + "<?" + pi.getNodeName() + " " + pi.getData() + "?>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					segments.add(text);
					segments.add("" + '\u007F' + '\u007F' + "<?" + pi.getNodeName() + " " + pi.getData() + "?>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					text = ""; //$NON-NLS-1$
					translatable = ""; //$NON-NLS-1$
				}
				break;
			case Node.TEXT_NODE:
				String value = n.getNodeValue();
				value = value.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
				value = value.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
				value = value.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
				text = text + value;
				if (value.trim().length() > 0) {
					translatable = translatable + value;
				}
				if (value.trim().length() > 0 && text.startsWith("" + '\u007F' + '\u007F')) { //$NON-NLS-1$
					for (int j = 0; j < stack.size(); j++) {
						if (startsSegment.containsKey(stack.get(j))) {
							text = text.substring(2);
							break;
						}
					}
				}

				break;
			default:
				break;
			}
		}

		/**
		 * Normalize element.
		 * @param e
		 *            the e
		 */
		private void normalizeElement(Element e) {
			List<Node> l = e.getContent();
			Iterator<Node> i = l.iterator();
			List<Node> normal = new Vector<Node>();
			while (i.hasNext()) {
				Node n = i.next();
				if (n.getNodeType() == Node.TEXT_NODE) {
					String value = n.getNodeValue();
					value = normalize(value);
					n.setNodeValue(value);
				}
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element e1 = new Element(n);
					if (!keepFormating.containsKey(e1.getName())
							&& !e1.getAttributeValue("xml:space", "default").equals("preserve")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						normalizeElement(e1);
						n = e1.getElement();
					}
				}
				normal.add(n);
			}
			e.setContent(normal);
		}

		/**
		 * Clean attribute.
		 * @param value
		 *            the value
		 * @return the string
		 */
		private String cleanAttribute(String value) {
			if (stack.size() > 1 && !text.startsWith("" + '\u007F' + '\u007F')) { //$NON-NLS-1$
				// this is an inline element and will be placed in <ph>
				value = value.replaceAll("&", "&amp;amp;"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				value = value.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			value = value.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
			value = value.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
			value = value.replaceAll("\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
			return value;
		}

		/**
		 * Gets the type.
		 * @param string
		 *            the string
		 * @return the type
		 */
		private String getType(String string) {

			String result = ""; //$NON-NLS-1$
			if (string.startsWith("<![CDATA[")) {return "![CDATA[";} //$NON-NLS-1$ //$NON-NLS-2$
			if (string.startsWith("<!--")) {return "!--";} //$NON-NLS-1$ //$NON-NLS-2$
			if (string.startsWith("<?")) {return "?";} //$NON-NLS-1$ //$NON-NLS-2$

			if (!string.equals("")) { //$NON-NLS-1$
				// skip initial "<"
				string = string.substring(1);
			}

			for (int i = 0; i < string.length(); i++) {
				char c = string.charAt(i);
				if (c == ' ' || c == '\n' || c == '\f' || c == '\t' || c == '\r' || c == '>') {
					break;
				}
				result = result + c;
			}
			if (result.endsWith("/") && result.length() > 1) { //$NON-NLS-1$
				result = result.substring(0, result.length() - 1);
			}
			return result;
		}

		/**
		 * Gets the encoding.
		 * @param fileName
		 *            the file name
		 * @return the encoding
		 * @throws Exception
		 *             the exception
		 */
		public String getEncoding(String fileName) throws Exception {
			// check if there is a BOM (byte order mark)
			// at the start of the document
			FileInputStream inputStream = new FileInputStream(fileName);
			byte[] array = new byte[2];
			inputStream.read(array);
			inputStream.close();
			byte[] lt = "<".getBytes(); //$NON-NLS-1$
			byte[] feff = { -1, -2 };
			byte[] fffe = { -2, -1 };
			if (array[0] != lt[0]) {
				// there is a BOM, now check the order
				if (array[0] == fffe[0] && array[1] == fffe[1]) {
					return "UTF-16BE"; //$NON-NLS-1$
				}
				if (array[0] == feff[0] && array[1] == feff[1]) {
					return "UTF-16LE"; //$NON-NLS-1$
				}
			}
			// check declared encoding
			// return UTF-8 as default
			String result = "UTF-8"; //$NON-NLS-1$
			FileReader in = new FileReader(fileName);
			BufferedReader buffer = new BufferedReader(in);
			String line = buffer.readLine();
			in.close();
			if (line.startsWith("<?")) { //$NON-NLS-1$
				line = line.substring(2, line.indexOf("?>")); //$NON-NLS-1$
				line = line.replaceAll("\'", "\""); //$NON-NLS-1$ //$NON-NLS-2$
				StringTokenizer tokenizer = new StringTokenizer(line);
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if (token.startsWith("encoding")) { //$NON-NLS-1$
						result = token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			return result;
		}
	}
}