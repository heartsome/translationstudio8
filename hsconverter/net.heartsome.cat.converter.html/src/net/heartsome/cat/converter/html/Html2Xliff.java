/**
 * Html2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.html;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.html.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The Class Html2Xliff.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Html2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "html";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("html.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "Html to XLIFF Conveter";

	/**
	 * Gets the node type.
	 * @param string
	 *            the string
	 * @return the node type
	 */
	private static String getNodeType(String string) {

		String result = ""; //$NON-NLS-1$
		if (string.startsWith("<![CDATA[")) { //$NON-NLS-1$
			return "![CDATA["; //$NON-NLS-1$
		}
		if (string.startsWith("<![IGNORE[")) { //$NON-NLS-1$
			return "![IGNORE["; //$NON-NLS-1$
		}
		if (string.startsWith("<![INCLUDE[")) { //$NON-NLS-1$
			return "![INCLUDE["; //$NON-NLS-1$
		}
		if (string.startsWith("<!--")) { //$NON-NLS-1$
			return "!--"; //$NON-NLS-1$
		}
		if (string.startsWith("<?")) { //$NON-NLS-1$
			return "?"; //$NON-NLS-1$
		}

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
		return result.toLowerCase();
	}

	/**
	 * Contains text.
	 * @param string
	 *            the string
	 * @return true, if successful
	 */
	private static boolean containsText(String string) {
		StringBuffer buffer = new StringBuffer();
		int length = string.length();
		boolean inTag = false;

		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			if (string.substring(i).startsWith("<ph")) { //$NON-NLS-1$
				inTag = true;
			}
			if (string.substring(i).startsWith("</ph>")) { //$NON-NLS-1$
				inTag = false;
				i = i + 4;
				continue;
			}
			if (!inTag) {
				buffer.append(c);
			}
		}
		return !buffer.toString().trim().equals(""); //$NON-NLS-1$
	}

	/**
	 * Normalize.
	 * @param string
	 *            the string
	 * @return the string
	 */
	private static String normalize(String string) {
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
				while (i < length - 1 && Character.isSpaceChar(string.charAt(i + 1))) {
					i++;
				}
			}
		}
		return rs;
	}

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
		Html2XliffImpl converter = new Html2XliffImpl();
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
	 * The Class Html2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Html2XliffImpl {

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

		/** The entities. */
		private Hashtable<String, String> entities;

		/** The ctypes. */
		private Hashtable<String, String> ctypes;

		/** The keep formating. */
		private Hashtable<String, String> keepFormating;

		/** The seg by element. */
		private boolean segByElement;

		/** The keep format. */
		private boolean keepFormat;

		/** The segmenter. */
		private StringSegmenter segmenter;

		/** The catalogue. */
		private String catalogue;

		/** The first. */
		private String first;

		/** The last. */
		private String last;

		/** The is suite. */
		private boolean isSuite;

		/** The qt tool id. */
		private String qtToolId;

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
			monitor.subTask(Messages.getString("html.Html2Xliff.beginTask"));
			Map<String, String> result = new HashMap<String, String>();
			inputFile = params.get(Converter.ATTR_SOURCE_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
			sourceLanguage = params.get(Converter.ATTR_SOURCE_LANGUAGE);
			targetLanguage = params.get(Converter.ATTR_TARGET_LANGUAGE);
			srcEncoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			String iniFile = params.get(Converter.ATTR_INI_FILE);

			try {
				if (iniFile == null || "".equals(iniFile)) {
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
							Messages.getString("html.Html2Xliff.msg3"));
				}
				catalogue = params.get(Converter.ATTR_CATALOGUE);

				isSuite = false;
				if (Converter.TRUE.equalsIgnoreCase(params.get(Converter.ATTR_IS_SUITE))) {
					isSuite = true;
				}

				qtToolId = params.get(Converter.ATTR_QT_TOOLID) != null ? params.get(Converter.ATTR_QT_TOOLID)
						: Converter.QT_TOOLID_DEFAULT_VALUE;

				String elementSegmentation = params.get(Converter.ATTR_SEG_BY_ELEMENT);
				if (elementSegmentation == null) {
					segByElement = false;
				} else {
					if (elementSegmentation.equals(Converter.TRUE)) {
						segByElement = true;
					} else {
						segByElement = false;
					}
				}

				if (!segByElement) {
					String initSegmenter = params.get(Converter.ATTR_SRX);
					segmenter = new StringSegmenter(initSegmenter, sourceLanguage, catalogue);
				}
				input = new FileInputStream(inputFile);
				skeleton = new FileOutputStream(skeletonFile);
				output = new FileOutputStream(xliffFile);
				writeHeader();
				monitor.worked(1);

				int size = input.available();
				byte[] array = new byte[size];
				if (size != input.read(array)) {
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
							Messages.getString("html.Html2Xliff.msg1")); //$NON-NLS-1$
				}
				String file = new String(array, srcEncoding);
				array = null;
				buildTables(iniFile, Progress.getSubMonitor(monitor, 1));
				buildList(file, Progress.getSubMonitor(monitor, 1));
				file = null;

				processList(Progress.getSubMonitor(monitor, 1));

				skeleton.close();
				writeString("</body>\n"); //$NON-NLS-1$
				writeString("</file>\n"); //$NON-NLS-1$
				writeString("</xliff>"); //$NON-NLS-1$
				input.close();
				output.close();

				result.put(Converter.ATTR_XLIFF_FILE, xliffFile);
			} catch (OperationCanceledException e) {
				// 需要把用户的取消操作异常跟其它异常区分开，并重新抛出此取消异常。
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("html.Html2Xliff.msg4"),
						e);
			} finally {
				monitor.done();
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
					"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " //$NON-NLS-1$
					+ "xmlns:hs=\"" //$NON-NLS-1$
					+ Converter.HSNAMESPACE + "\" " + //$NON-NLS-1$ 
					"xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd " //$NON-NLS-1$
					+ Converter.HSSCHEMALOCATION + "\">\n"); //$NON-NLS-1$
			if (!targetLanguage.equals("")) {
				writeString("<file original=\"" //$NON-NLS-1$
						+ cleanString(inputFile)
						+ "\" source-language=\"" //$NON-NLS-1$
						+ sourceLanguage
						+ "\" target-language=\"" + targetLanguage + "\" datatype=\"" + TYPE_VALUE + "\">\n"); //$NON-NLS-1$
			} else {
				writeString("<file original=\"" //$NON-NLS-1$
						+ cleanString(inputFile) + "\" source-language=\"" //$NON-NLS-1$
						+ sourceLanguage + "\" datatype=\"" + TYPE_VALUE + "\">\n"); //$NON-NLS-1$
			}
			writeString("<header>\n"); //$NON-NLS-1$
			writeString("   <skl>\n"); //$NON-NLS-1$
			String crc = ""; //$NON-NLS-1$
			if (isSuite) {
				crc = "crc=\"" + CRC16.crc16(TextUtil.cleanString(skeletonFile).getBytes("UTF-8")) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			writeString("      <external-file href=\"" + TextUtil.cleanString(skeletonFile) + "\" " + crc + "/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			writeString("   </skl>\n"); //$NON-NLS-1$
			writeString("   <tool tool-id=\"" + qtToolId + "\" tool-name=\"HSStudio\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			writeString("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\" >" //$NON-NLS-1$
					+ srcEncoding + "</hs:prop></hs:prop-group>\n"); //$NON-NLS-1$
			writeString("</header>\n"); //$NON-NLS-1$
			writeString("<body>\n"); //$NON-NLS-1$
		}

		/**
		 * Process list.
		 * @throws ParserConfigurationException
		 *             * @throws SAXException the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws SAXException
		 *             the SAX exception
		 */
		private void processList(IProgressMonitor monitor) throws IOException, SAXException {
			monitor.beginTask(Messages.getString("html.Html2Xliff.task2"), segments.size());
			monitor.subTask("");
			for (int i = 0; i < segments.size(); i++) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("html.cancel"));
				}
				monitor.worked(1);
				String text = segments.get(i);
				if (isTranslateable(text)) {
					extractSegment(text);
				} else {
					// send directly to skeleton
					writeSkeleton(text);
				}
			}
			monitor.done();
		}

		/**
		 * Extract segment.
		 * @param seg
		 *            the seg
		 * @throws ParserConfigurationException
		 *             * @throws SAXException the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws SAXException
		 *             the SAX exception
		 */
		private void extractSegment(String seg) throws IOException, SAXException {

			// start by making a smaller list

			List<String> miniList = new ArrayList<String>();

			int start = seg.indexOf("<"); //$NON-NLS-1$
			int end = seg.indexOf(">"); //$NON-NLS-1$
			String text = ""; //$NON-NLS-1$

			while (start != -1) {
				if (start > 0) {
					// add initial text
					miniList.add(seg.substring(0, start));
					seg = seg.substring(start);
					start = seg.indexOf("<"); //$NON-NLS-1$
					end = seg.indexOf(">"); //$NON-NLS-1$
				}
				// add the tag
				if (end < seg.length()) {
					miniList.add(seg.substring(start, end + 1));
					seg = seg.substring(end + 1);
					start = seg.indexOf("<"); //$NON-NLS-1$
					end = seg.indexOf(">"); //$NON-NLS-1$
				} else {
					miniList.add(seg);
					start = -1;
				}
			}
			if (seg.length() > 0) {
				// add trailing characters
				miniList.add(seg);
			}

			int size = miniList.size();
			int i;
			// separate initial text
			String initial = ""; //$NON-NLS-1$
			for (i = 0; i < size; i++) {
				text = miniList.get(i);
				if (!isTranslateable(text)) {
					initial = initial + text;
				} else {
					break;
				}
			}

			// get translatable
			String translatable = text;
			for (i++; i < size; i++) {
				translatable = translatable + miniList.get(i);
			}

			// get trailing text
			String trail = ""; //$NON-NLS-1$
			int j;
			for (j = size - 1; j > 0; j--) {
				String t = miniList.get(j);
				if (!isTranslateable(t)) {
					trail = t + trail;
				} else {
					break;
				}
			}

			// remove trailing from translatable
			start = translatable.lastIndexOf(trail);
			if (start != -1) {
				translatable = translatable.substring(0, start);
			}

			writeSkeleton(initial);
			String tagged = addTags(translatable);
			if (containsText(tagged)) {
				translatable = tagged;
				if (segByElement) {
					String[] frags = translatable.split("\u2029"); //$NON-NLS-1$
					for (int m = 0; m < frags.length; m++) {
						writeSegment(frags[m]);
					}
				} else {
					String[] frags = translatable.split("\u2029"); //$NON-NLS-1$
					for (int m = 0; m < frags.length; m++) {
						String[] segs = segmenter.segment(frags[m]);
						for (int h = 0; h < segs.length; h++) {
							writeSegment(segs[h]);
						}
					}
				}
			} else {
				writeSkeleton(translatable);
			}
			writeSkeleton(trail);
		}

		/**
		 * Write segment.
		 * @param segment
		 *            the segment
		 * @throws ParserConfigurationException
		 *             * @throws SAXException the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws SAXException
		 *             the SAX exception
		 */
		private void writeSegment(String segment) throws IOException, SAXException {

			segment = segment.replaceAll("\u2029", ""); //$NON-NLS-1$ //$NON-NLS-2$
			String pure = removePH(segment);
			if (pure.trim().equals("")) { //$NON-NLS-1$
				writeSkeleton(phContent(segment));
				return;
			}

			if (segment.trim().equals("")) { //$NON-NLS-1$
				writeSkeleton(segment);
				return;
			}

			first = ""; //$NON-NLS-1$
			last = ""; //$NON-NLS-1$

			segment = segmentCleanup(segment);

			writeSkeleton(first);
			tagId = 0;
			writeString("   <trans-unit id=\"" //$NON-NLS-1$
					+ segId + "\" xml:space=\"preserve\" approved=\"no\">\n" //$NON-NLS-1$
					+ "      <source xml:lang=\"" //$NON-NLS-1$
					+ sourceLanguage + "\">"); //$NON-NLS-1$
			if (keepFormat) {
				writeString(segment);
			} else {
				writeString(normalize(segment));
			}
			writeString("</source>\n   </trans-unit>\n"); //$NON-NLS-1$

			writeSkeleton("%%%" + segId++ + "%%%\n" + last); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * Segment cleanup.
		 * @param segment
		 *            the segment
		 * @return the string
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String segmentCleanup(String segment) throws SAXException, IOException {
			ByteArrayInputStream stream = new ByteArrayInputStream(("<x>" + segment + "</x>").getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			SAXBuilder b = new SAXBuilder();
			Document d = b.build(stream);
			Element e = d.getRootElement();
			List<Node> content = e.getContent();
			Iterator<Node> it = content.iterator();
			int count = 0;
			while (it.hasNext()) {
				Node n = it.next();
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					count++;
				}
			}

			if (count == 1) {
				Node n = content.get(0);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					first = phContent(new Element(n).toString());
					content.remove(0);
				} else {
					n = content.get(content.size() - 1);
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						last = phContent(new Element(n).toString());
						content.remove(content.size() - 1);
					}
				}
			}

			if (count == 2) {
				Node n = content.get(0);
				Node s = content.get(content.size() - 1);
				if (n.getNodeType() == Node.ELEMENT_NODE && s.getNodeType() == Node.ELEMENT_NODE) {
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						first = new Element(n).getText();
						content.remove(n);
						last = new Element(s).getText();
						content.remove(s);
					}
				}
			}
			e.setContent(content);
			String es = e.toString();
			return es.substring(3, es.length() - 4);
		}

		/**
		 * Ph content.
		 * @param segment
		 *            the segment
		 * @return the string
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String phContent(String segment) throws SAXException, IOException {
			ByteArrayInputStream stream = new ByteArrayInputStream(("<x>" + segment + "</x>").getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			SAXBuilder b = new SAXBuilder();
			Document d = b.build(stream);
			Element e = d.getRootElement();
			List<Node> content = e.getContent();
			Iterator<Node> it = content.iterator();
			String result = ""; //$NON-NLS-1$
			while (it.hasNext()) {
				Node n = it.next();
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					result = result + new Element(n).getText();
				}
				if (n.getNodeType() == Node.TEXT_NODE) {
					result = result + n.getNodeValue();
				}
			}
			return result;
		}

		/**
		 * Removes the ph.
		 * @param segment
		 *            the segment
		 * @return the string
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String removePH(String segment) throws SAXException, IOException {
			ByteArrayInputStream stream = new ByteArrayInputStream(("<x>" + segment + "</x>").getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			SAXBuilder b = new SAXBuilder();
			Document d = b.build(stream);
			Element e = d.getRootElement();
			List<Node> content = e.getContent();
			Iterator<Node> it = content.iterator();
			String result = ""; //$NON-NLS-1$
			while (it.hasNext()) {
				Node n = it.next();
				if (n.getNodeType() == Node.TEXT_NODE) {
					result = result + n.getNodeValue();
				}
			}
			return result;
		}

		/**
		 * Adds the tags.
		 * @param src
		 *            the src
		 * @return the string
		 */
		private String addTags(String src) {
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
				// check if we don't fall in a trap
				// from Adobe GoLive
				int errors = element.indexOf("<", 1); //$NON-NLS-1$
				if (errors != -1) {
					// there is a "<" inside quotes
					// must move manually until the end
					// of the element avoiding angle brackets
					// within quotes
					boolean exit = false;
					boolean inQuotes = false;
					StringBuffer buffer = new StringBuffer();
					buffer.append("<"); //$NON-NLS-1$
					int k = 0;
					while (!exit) {
						k++;
						char c = src.charAt(start + k);
						if (c == '\"') {
							inQuotes = !inQuotes;
						}
						buffer.append(c);
						if (!inQuotes && c == '>') {
							exit = true;
						}
					}
					element = buffer.toString();
					end = start + buffer.toString().length();
					start = end;
				}

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
		 */
		private String tag(String element) {
			String result = ""; //$NON-NLS-1$
			String type = getNodeType(element);
			if (translatableAttributes.containsKey(type)) {
				result = extractAttributes(type, element);
				if (result.indexOf("\u2029") == -1) { //$NON-NLS-1$
					String ctype = ""; //$NON-NLS-1$
					if (ctypes.containsKey(type)) {
						ctype = " ctype=\"" + ctypes.get(type) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
					}
					result = "<ph id=\"" //$NON-NLS-1$
							+ tagId++ + "\"" //$NON-NLS-1$
							+ ctype + ">" //$NON-NLS-1$
							+ cleanString(element) + "</ph>"; //$NON-NLS-1$
				}
			} else {
				String ctype = ""; //$NON-NLS-1$
				if (ctypes.containsKey(type)) {
					ctype = " ctype=\"" + ctypes.get(type) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
				}
				result = "<ph id=\"" //$NON-NLS-1$
						+ tagId++ + "\"" //$NON-NLS-1$
						+ ctype + ">" //$NON-NLS-1$
						+ cleanString(element) + "</ph>"; //$NON-NLS-1$
			}
			return result;
		}

		/**
		 * Clean string.
		 * @param s
		 *            the s
		 * @return the string
		 */
		private String cleanString(String s) {
			int control = s.indexOf("&"); //$NON-NLS-1$
			while (control != -1) {
				int sc = s.indexOf(";", control); //$NON-NLS-1$
				if (sc == -1) {
					// no semicolon, it's not an entity
					s = s.substring(0, control) + "&amp;" + s.substring(control + 1); //$NON-NLS-1$
				} else {
					// may be an entity
					String candidate = s.substring(control, sc) + ";"; //$NON-NLS-1$
					if (!(candidate.equals("&amp;") || candidate.equals("&gt;") || candidate.equals("&lt;"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						String entity = entities.get(candidate);
						if (entity != null) {
							s = s.substring(0, control) + entity + s.substring(sc + 1);
						} else {
							s = s.substring(0, control) + "%%%ph id=\"" //$NON-NLS-1$
									+ tagId++ + "\"%%%&amp;" //$NON-NLS-1$
									+ candidate.substring(1) + "%%%/ph%%%" //$NON-NLS-1$
									+ s.substring(sc + 1);
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
			s = s.replaceAll("%%%/ph%%%", "</ph>"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("%%%ph", "<ph"); //$NON-NLS-1$ //$NON-NLS-2$
			s = s.replaceAll("\"%%%&amp;", "\">&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
			return s;
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
		 * Checks if is translateable.
		 * @param string
		 *            the string
		 * @return true, if is translateable
		 */
		private boolean isTranslateable(String string) {

			keepFormat = false;

			//
			// remove CDATA sections
			//
			int startComment = string.indexOf("<![CDATA["); //$NON-NLS-1$
			int endComment = string.indexOf("]]>"); //$NON-NLS-1$
			while (startComment != -1 || endComment != -1) {
				if (startComment != -1) {
					if (endComment == -1) {
						string = string.substring(0, startComment);
					} else {
						if (startComment < endComment) {
							string = string.substring(0, startComment) + string.substring(endComment + 3);
						} else {
							string = string.substring(endComment + 3, startComment);
						}
					}
				} else {
					if (endComment != -1) {
						string = string.substring(endComment + 3);
					}
				}
				startComment = string.indexOf("<![CDATA["); //$NON-NLS-1$
				endComment = string.indexOf("]]>"); //$NON-NLS-1$
			}
			//
			// remove IGNORE sections
			//
			startComment = string.indexOf("<![IGNORE["); //$NON-NLS-1$
			endComment = string.indexOf("]]>"); //$NON-NLS-1$
			while (startComment != -1 || endComment != -1) {
				if (startComment != -1) {
					if (endComment == -1) {
						string = string.substring(0, startComment);
					} else {
						if (startComment < endComment) {
							string = string.substring(0, startComment) + string.substring(endComment + 3);
						} else {
							string = string.substring(endComment + 3, startComment);
						}
					}
				} else {
					if (endComment != -1) {
						string = string.substring(endComment + 3);
					}
				}
				startComment = string.indexOf("<![IGNORE["); //$NON-NLS-1$
				endComment = string.indexOf("]]>"); //$NON-NLS-1$
			}
			//
			// remove IGNORE sections
			//
			startComment = string.indexOf("<![INCLUDE["); //$NON-NLS-1$
			endComment = string.indexOf("]]>"); //$NON-NLS-1$
			while (startComment != -1 || endComment != -1) {
				if (startComment != -1) {
					if (endComment == -1) {
						string = string.substring(0, startComment);
					} else {
						if (startComment < endComment) {
							string = string.substring(0, startComment) + string.substring(endComment + 3);
						} else {
							string = string.substring(endComment + 3, startComment);
						}
					}
				} else {
					if (endComment != -1) {
						string = string.substring(endComment + 3);
					}
				}
				startComment = string.indexOf("<![INCLUDE["); //$NON-NLS-1$
				endComment = string.indexOf("]]>"); //$NON-NLS-1$
			}
			//
			// remove XML style comments
			//
			startComment = string.indexOf("<!--"); //$NON-NLS-1$
			endComment = string.indexOf("-->"); //$NON-NLS-1$
			while (startComment != -1 || endComment != -1) {
				if (startComment != -1) {
					if (endComment == -1) {
						string = string.substring(0, startComment);
					} else {
						if (startComment < endComment) {
							string = string.substring(0, startComment) + string.substring(endComment + 3);
						} else {
							string = string.substring(endComment + 3, startComment);
						}
					}
				} else {
					if (endComment != -1) {
						string = string.substring(endComment + 3);
					}
				}
				startComment = string.indexOf("<!--"); //$NON-NLS-1$
				endComment = string.indexOf("-->"); //$NON-NLS-1$
			}
			//
			// remove Processing Instruction
			//
			startComment = string.indexOf("<?"); //$NON-NLS-1$
			endComment = string.indexOf("?>"); //$NON-NLS-1$
			while (startComment != -1 || endComment != -1) {
				if (startComment != -1) {
					if (endComment == -1) {
						string = string.substring(0, startComment);
					} else {
						string = string.substring(0, startComment) + string.substring(endComment + 2);
					}
				} else {
					if (endComment != -1) {
						string = string.substring(endComment + 2);
					}
				}
				startComment = string.indexOf("<?"); //$NON-NLS-1$
				endComment = string.indexOf("?>"); //$NON-NLS-1$
			}
			//
			// remove C style comments
			//
			startComment = string.indexOf("/*"); //$NON-NLS-1$
			endComment = string.indexOf("*/"); //$NON-NLS-1$
			while (startComment != -1 || endComment != -1) {
				if (startComment != -1) {
					if (endComment == -1) {
						string = string.substring(0, startComment);
					} else {
						if (startComment < endComment) {
							string = string.substring(0, startComment) + string.substring(endComment + 2);
						} else {
							string = string.substring(endComment + 2, startComment);
						}
					}
				} else {
					if (endComment != -1) {
						string = string.substring(endComment + 2);
					}
				}
				startComment = string.indexOf("/*"); //$NON-NLS-1$
				endComment = string.indexOf("*/"); //$NON-NLS-1$
			}
			//
			// Start checking
			//
			int start = string.indexOf("<"); //$NON-NLS-1$
			int end = string.indexOf(">"); //$NON-NLS-1$

			String type;
			String text = ""; //$NON-NLS-1$

			while (start != -1) {
				if (start > 0) {
					text = text + cleanString(string.substring(0, start));
					string = string.substring(start);
					start = string.indexOf("<"); //$NON-NLS-1$
					end = string.indexOf(">"); //$NON-NLS-1$
				}
				type = getNodeType(string.substring(start, end));
				keepFormat = keepFormating.containsKey(type);

				if (type.equals("script") || type.equals("style")) { //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}

				// check for translatable attributes
				if (translatableAttributes.containsKey(type)) {
					return true;
				}
				if (type.startsWith("/")) { //$NON-NLS-1$
					if (translatableAttributes.containsKey(type.substring(1))) {
						return true;
					}
				}
				if (end < string.length()) {
					string = string.substring(end + 1);
				} else {
					string = string.substring(end);
				}
				start = string.indexOf("<"); //$NON-NLS-1$
				end = string.indexOf(">"); //$NON-NLS-1$
			}

			text = text + cleanString(string);

			// look for non-white space

			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (c == ' ' || c == '\n' || c == '\f' || c == '\t' || c == '\r' || c == '\u00A0' || c == '\u2007'
						|| c == '\u202F' || c == '\uFEFF' || c == '>') {
					continue;
				}
				return true;
			}

			return false;
		}

		/**
		 * Extract attributes.
		 * @param type
		 *            the type
		 * @param element
		 *            the element
		 * @return the string
		 */
		private String extractAttributes(String type, String element) {

			String ctype = ""; //$NON-NLS-1$
			if (ctypes.containsKey(type)) {
				ctype = " ctype=\"" + ctypes.get(type) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
			String result = "<ph id=\"" + tagId++ + "\"" + ctype + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			element = cleanString(element);

			Vector<String> v = translatableAttributes.get(type);

			StringTokenizer tokenizer = new StringTokenizer(element, "&= \t\n\r\f/", true); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (!v.contains(token.toLowerCase())) {
					result = result + token;
				} else {
					result = result + token;
					String s = tokenizer.nextToken();
					while (s.equals("=") || s.equals(" ")) { //$NON-NLS-1$ //$NON-NLS-2$
						result = result + s;
						s = tokenizer.nextToken();
					}
					// s contains the first word of the atttribute
					if ((s.startsWith("\"") && s.endsWith("\"") //$NON-NLS-1$ //$NON-NLS-2$
					|| s.startsWith("'") && s.endsWith("'")) //$NON-NLS-1$ //$NON-NLS-2$
							&& s.length() > 1) {
						// the value is one word and it is quoted
						result = result + s.substring(0, 1) + "</ph>\u2029" //$NON-NLS-1$
								+ s.substring(1, s.length() - 1) + "\u2029<ph id=\"" //$NON-NLS-1$
								+ tagId++ + "\">" //$NON-NLS-1$
								+ s.substring(s.length() - 1);
					} else {
						if (s.startsWith("\"") || s.startsWith("'")) { //$NON-NLS-1$ //$NON-NLS-2$
							// attribute value is quoted, but it has more than
							// one
							// word
							String quote = s.substring(0, 1);
							result = result + s.substring(0, 1) + "</ph>\u2029" + s.substring(1); //$NON-NLS-1$
							s = tokenizer.nextToken();
							do {
								result = result + s;
								s = tokenizer.nextToken();
							} while (s.indexOf(quote) == -1);
							String left = s.substring(0, s.indexOf(quote));
							String right = s.substring(s.indexOf(quote));
							result = result + left + "\u2029<ph id=\"" + tagId++ + "\">" + right; //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							// attribute is not quoted, it can only be one word
							result = result + "</ph>\u2029" + s + "\u2029<ph id=\"" + tagId++ + "\">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
			Catalogue cat = new Catalogue(catalogue);
			builder.setEntityResolver(cat);
			Document doc = builder.build(iniFile);
			Element root = doc.getRootElement();
			List<Element> tags = root.getChildren("tag"); //$NON-NLS-1$

			startsSegment = new Hashtable<String, String>();
			translatableAttributes = new Hashtable<String, Vector<String>>();
			entities = new Hashtable<String, String>();
			ctypes = new Hashtable<String, String>();
			keepFormating = new Hashtable<String, String>();

			int size = tags.size();
			monitor.beginTask(Messages.getString("html.Html2Xliff.task3"), size);
			monitor.subTask("");
			Iterator<Element> i = tags.iterator();
			while (i.hasNext()) {
				if (monitor.isCanceled()) {
					// 用户取消操作
					throw new OperationCanceledException(Messages.getString("html.cancel"));
				}
				monitor.worked(1);
				Element t = i.next();
				if (t.getAttributeValue("hard-break", "inline").equals("segment")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					startsSegment.put(t.getText(), "yes"); //$NON-NLS-1$
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

			List<Element> ents = root.getChildren("entity"); //$NON-NLS-1$
			Iterator<Element> it = ents.iterator();
			while (it.hasNext()) {
				Element e = it.next();
				entities.put("&" + e.getAttributeValue("name") + ";", e.getText()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			root = null;
			doc = null;
			builder = null;
			monitor.done();
		}

		/**
		 * Builds the list.
		 * @param file
		 *            the file
		 * @throws Exception
		 *             the exception
		 */
		private void buildList(String file, IProgressMonitor monitor) throws Exception {
			monitor.beginTask(Messages.getString("html.Html2Xliff.task4"), IProgressMonitor.UNKNOWN);
			monitor.subTask("");

			segments = new ArrayList<String>();
			int start = file.indexOf("<"); //$NON-NLS-1$
			int end = file.indexOf(">"); //$NON-NLS-1$
			String type;
			String text = ""; //$NON-NLS-1$
			if (start > 0) {
				segments.add(file.substring(0, start));
				file = file.substring(start);
				start = file.indexOf("<"); //$NON-NLS-1$
				end = file.indexOf(">"); //$NON-NLS-1$
			}
			// 由于 HTML 中可能存在诸如：<a href="http://www.hao123.com/video">更多>></a> 这种包含转义字符的文本，因此使用下面的表达式匹配 HTML 标记。
			Pattern pattern = Pattern.compile("<[^<>]+>");
			// 记录 text 在替换之前的长度，text 要将 <, > 替换成 &lt;, &gt;
			int len = 0;
			while (start != -1) {
				if (monitor.isCanceled()) {
					// 用户取消操作
					throw new OperationCanceledException(Messages.getString("html.cancel"));
				}
				if (file.substring(start, start + 3).equals("<![")) { //$NON-NLS-1$
					end = file.indexOf("]]>") + 2; //$NON-NLS-1$
				}
				if (end < start || end < 0 || start < 0) {
					throw new Exception(Messages.getString("html.Html2Xliff.msg2")); //$NON-NLS-1$
				}
				String element = file.substring(start, end + 1);

				// check if the element is OK or has strange stuff
				// from Adobe GoLive 5
				int errors = element.indexOf("<", 1); //$NON-NLS-1$
				if (errors != -1 && !element.startsWith("<![")) { //$NON-NLS-1$
					// there is a "<" inside quotes
					// must move manually until the end
					// of the element avoiding angle brackets
					// within quotes
					boolean exit = false;
					boolean inQuotes = false;
					StringBuffer buffer = new StringBuffer();
					buffer.append("<"); //$NON-NLS-1$
					int k = 0;
					while (!exit) {
						k++;
						char c = file.charAt(start + k);
						if (c == '\"') {
							inQuotes = !inQuotes;
						}
						buffer.append(c);
						if (!inQuotes && c == '>') {
							exit = true;
						}
					}
					element = buffer.toString();
					end = start + buffer.toString().length();
					start = end;
				}

				type = getNodeType(element);

				if (type.equals("![INCLUDE[") || type.equals("![IGNORE[") || type.equals("![CDATA[")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					// it's an SGML section, send it to skeleton
					// and keep looking for segments
					segments.add(text);
					text = ""; //$NON-NLS-1$
					len = 0;
					end = file.indexOf("]]>"); //$NON-NLS-1$
					segments.add(file.substring(start, end + 3));
					file = file.substring(end + 3);
					start = file.indexOf("<"); //$NON-NLS-1$
					end = file.indexOf(">"); //$NON-NLS-1$
					if (start != -1) {
						text = file.substring(0, start);
						len = text.length();
					}
					continue;
				}
				if (startsSegment.containsKey(type)) {
					segments.add(text);
					file = file.substring(len);
					start = start - len;
					end = end - len;
					text = ""; //$NON-NLS-1$
					len = text.length();
				}
				if (type.startsWith("/") && startsSegment.containsKey(type.substring(1))) { //$NON-NLS-1$
					segments.add(text);
					file = file.substring(len);
					start = start - len;
					end = end - len;
					text = ""; //$NON-NLS-1$
					len = text.length();
				}
				if (type.equals("script") || type.equals("style")) { //$NON-NLS-1$ //$NON-NLS-2$
					// send the whole element to the list
					segments.add(text);
					file = file.substring(len);
					text = ""; //$NON-NLS-1$
					len = text.length();
					if (!element.endsWith("/>")) { //$NON-NLS-1$
						end = file.toLowerCase().indexOf("/" + type + ">"); //$NON-NLS-1$ //$NON-NLS-2$
						String script = file.substring(0, end + 2 + type.length());
						if (script.indexOf("<!--") != -1 && script.indexOf("-->") == -1) { //$NON-NLS-1$ //$NON-NLS-2$
							// there are nested scripts inside this one
							end = file.toLowerCase().indexOf("/" + type + ">", file.indexOf("-->") + 3); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							script = file.substring(0, end + 2 + type.length());
						}
						segments.add(script);
						file = file.substring(end + 2 + type.length());
					} else {
						segments.add(element);
						file = file.substring(element.length());
					}

					start = file.indexOf("<"); //$NON-NLS-1$
					end = file.indexOf(">"); //$NON-NLS-1$
					if (start != -1) {
						text = file.substring(0, start);
						len = text.length();
					}
					continue;
				}
				if (type.equals("!--")) { //$NON-NLS-1$
					// it's a comment, send it to skeleton
					// and keep looking for segments
					segments.add(text);
					file = file.substring(len);
					text = ""; //$NON-NLS-1$
					len = text.length();
					end = file.indexOf("-->"); //$NON-NLS-1$
					String comment = file.substring(0, end + 3);
					segments.add(comment);
					file = file.substring(end + 3);
					start = file.indexOf("<"); //$NON-NLS-1$
					end = file.indexOf(">"); //$NON-NLS-1$
					if (start != -1) {
						text = file.substring(0, start);
						len = text.length();
					}
					continue;
				}

				text = text + element;
				len += element.length();

				if (end < file.length()) { // there may be text to extract
					Matcher matcher = pattern.matcher(file);
					int startIndex = 0;
					while (matcher.find()) {
						startIndex = matcher.start();
						if (startIndex > end) {
							break;
						}
					}
					if (startIndex > end) {
						String strTmpText = file.substring(end + 1, startIndex);
						len += strTmpText.length();
						strTmpText = strTmpText.replaceAll("<", "&lt;");
						strTmpText = strTmpText.replaceAll(">", "&gt;");
						text += strTmpText;
					}
				}

				Matcher matcher = pattern.matcher(file);
				int start2 = start;
				while (matcher.find()) {
					start = matcher.start();
					end = matcher.end();
					if (start > start2) {
						break;
					}
				}
				end--;
				if (end > file.length()) {
					end = file.length();
				}
				if (start <= start2) {
					start = -1;
				}
			}
			segments.add(text);
			monitor.done();
		}
	}

}
