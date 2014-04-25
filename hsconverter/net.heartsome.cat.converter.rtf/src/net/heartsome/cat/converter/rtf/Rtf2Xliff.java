/**
 * Rtf2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.rtf;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.rtf.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The Class Rtf2Xliff.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Rtf2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "rtf";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("rtf.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "RTF to XLIFF Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Rtf2XliffImpl converter = new Rtf2XliffImpl();
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
	 * The Class Rtf2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Rtf2XliffImpl {

		// check text hidden in tags using the XML inLocalisation sample

		/** The input. */
		private FileInputStream input;

		/** The output. */
		private FileOutputStream output;

		/** The skeleton. */
		private FileOutputStream skeleton;

		/** The content. */
		private String content;

		/** The header. */
		private String header;

		/** The groups. */
		private Vector<String> groups;

		/** The main text. */
		private String mainText;

		/** The styles. */
		private Vector<String> styles;

		/** The default cpg. */
		private String defaultCpg;

		/** The default font. */
		private String defaultFont;

		/** The default lang. */
		private String defaultLang;

		/** The default uc. */
		private String defaultUC;

		/** The in loch. */
		private boolean inLOCH;

		/** The in hich. */
		private boolean inHICH;

		/** The in dbch. */
		private boolean inDBCH;

		/** The src encoding. */
		private String srcEncoding;

		/** The font table. */
		Hashtable<String, String> fontTable;

		/** The charsets. */
		Hashtable<String, String> charsets;

		/** The fonts. */
		private Vector<String> fonts;

		/** The status. */
		Hashtable<String, Object> status;

		/** The stack. */
		Stack<Hashtable<String, Object>> stack;

		/** The ignore. */
		Hashtable<String, String> ignore;

		/** The skip. */
		private int skip;

		/** The default af. */
		private String defaultAF;

		/** The default status. */
		private Hashtable<String, Object> defaultStatus;

		/** The tw4win term. */
		private boolean tw4winTerm = false;

		/** The tw4win mark. */
		private boolean tw4winMark = false;

		/** The tw4win error. */
		private boolean tw4winError = false;

		/** The tw4win popup. */
		private boolean tw4winPopup = false;

		/** The tw4win jump. */
		private boolean tw4winJump = false;

		/** The tw4win external. */
		private boolean tw4winExternal = false;

		/** The tw4win internal. */
		private boolean tw4winInternal = false;

		/** The do_not_translate. */
		private boolean do_not_translate = false;

		/** The win external. */
		private String winExternal = ""; //$NON-NLS-1$

		/** The win internal. */
		private String winInternal = ""; //$NON-NLS-1$

		/** The win mark. */
		private String winMark = ""; //$NON-NLS-1$

		/** The not_translate. */
		private String not_translate = ""; //$NON-NLS-1$

		/** The internal value. */
		private String internalValue = ""; //$NON-NLS-1$

		/** The external value. */
		private String externalValue = ""; //$NON-NLS-1$

		/** The no translate value. */
		private String noTranslateValue = ""; //$NON-NLS-1$

		/** The color list. */
		private Vector<String> colorList;

		/** The color group. */
		private int colorGroup;

		/** The font group. */
		private int fontGroup;

		/** The style group. */
		private int styleGroup;

		/** The style fonts. */
		private Hashtable<String, String> styleFonts;

		/** The source language. */
		private String sourceLanguage;

		private String targetLanguage;

		/** The tagged rtf. */
		private boolean taggedRTF;

		/** The seg by element. */
		private boolean segByElement;

		/** The seg id. */
		int segId;

		/** The breaks. */
		private Hashtable<String, String> breaks;

		/** The segments. */
		private Vector<String> segments;

		/** The start. */
		private String start;

		/** The end. */
		private String end;

		/** The meat. */
		private String meat;

		/** The in external. */
		private boolean inExternal;

		/** The segmenter. */
		private StringSegmenter segmenter;

		/** The skeleton file. */
		private String skeletonFile;

		/** The xliff file. */
		private String xliffFile;

		/** The mark style. */
		private Object markStyle;

		/** The input file. */
		private String inputFile;

		/** The symbols. */
		private Hashtable<Integer, String> symbols;

		/** The ignorable fonts. */
		private Hashtable<String, String> ignorableFonts;

		/** The default cf. */
		private String defaultCF = "1"; //$NON-NLS-1$

		/** The cf table. */
		private Hashtable<String, String> cfTable;

		/** The qt tool id. */
		private String qtToolID;

		/** The is suite. */
		private boolean isSuite;

		/** The data type. */
		private String dataType;

		/** The program foler. */
		private String programFoler;

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
			Map<String, String> result = new HashMap<String, String>();
			try {
				// 把转换过程分为 10 部分：parseMainText 方法之前的部分占 1， parseMainText 方法中的处理占 5，pase 方法占 2，剩余部分占 2。
				monitor.beginTask("", 10);
				// 再把 parseMainText 之前的任务进一步进行细分，分为 4 部分：读取文件内容，构建文件的组内容，解析组内容，处理组内容。
				IProgressMonitor firstPartMonitor = Progress.getSubMonitor(monitor, 1);
				firstPartMonitor.beginTask("", 4);
				dataType = params.get(Converter.ATTR_FORMAT);
				if (dataType == null) {
					dataType = TYPE_VALUE;
				}
				inputFile = params.get(Converter.ATTR_SOURCE_FILE);
				xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
				skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
				sourceLanguage = params.get(Converter.ATTR_SOURCE_LANGUAGE);
				targetLanguage = params.get(Converter.ATTR_TARGET_LANGUAGE);
				srcEncoding = params.get(Converter.ATTR_SOURCE_ENCODING);
				programFoler = params.get(Converter.ATTR_PROGRAM_FOLDER);
				String catalogue = params.get(Converter.ATTR_CATALOGUE);
				String elementSegmentation = params.get(Converter.ATTR_SEG_BY_ELEMENT);
				String tagged = params.get(Converter.ATTR_IS_TAGGEDRTF);
				if (tagged != null && tagged.equalsIgnoreCase(Converter.TRUE)) {
					taggedRTF = true;
				} else {
					taggedRTF = false;
				}

				if (elementSegmentation == null) {
					segByElement = false;
				} else {
					if (elementSegmentation.equalsIgnoreCase(Converter.TRUE)) {
						segByElement = true;
					} else {
						segByElement = false;
					}
				}

				isSuite = false;
				if (Converter.TRUE.equals(params.get(Converter.ATTR_IS_SUITE))) {
					isSuite = true;
				}
				qtToolID = params.get(Converter.ATTR_QT_TOOLID) != null ? params.get(Converter.ATTR_QT_TOOLID)
						: Converter.QT_TOOLID_DEFAULT_VALUE;

				segId = 0;
				if (!segByElement || taggedRTF) {
					String initSegmenter = params.get(Converter.ATTR_SRX);
					segmenter = new StringSegmenter(initSegmenter, sourceLanguage, catalogue);
				}

				// read the file into a String

				// 检查是否取消操作
				if (firstPartMonitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("rtf.cancel"));
				}
				firstPartMonitor.subTask(Messages.getString("rtf.Rtf2Xliff.task3"));
				input = new FileInputStream(inputFile);
				int size = input.available();
				byte[] array = new byte[size];
				input.read(array);
				content = new String(array);
				array = null;
				input.close();

				firstPartMonitor.worked(1);

				// get the header
				StringBuffer buffer = new StringBuffer();
				buffer.append(content.charAt(0));
				int i = 1;
				char c = 0;
				while (i < size && (c = content.charAt(i++)) != '{') {
					buffer.append(c);
				}
				header = buffer.toString();

				stack = new Stack<Hashtable<String, Object>>();

				processHeader();

				// build all groups

				styles = new Vector<String>();

				// 检查是否取消操作
				if (firstPartMonitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("rtf.cancel"));
				}
				firstPartMonitor.subTask(Messages.getString("rtf.Rtf2Xliff.task4"));
				buildGroups();
				buildCharsets();
				loadSymbols();
				firstPartMonitor.worked(1);

				System.out.println("Groups built"); //$NON-NLS-1$

				// parse individual groups

				Hashtable<String, String> mainGroups = new Hashtable<String, String>();
				mainText = ""; //$NON-NLS-1$

				firstPartMonitor.subTask(Messages.getString("rtf.Rtf2Xliff.task5"));
				for (i = 0; i < groups.size(); i++) {
					// 检查是否取消操作
					if (firstPartMonitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("rtf.cancel"));
					}
					String group = groups.get(i);
					if (group.indexOf("\\stylesheet") != -1) { //$NON-NLS-1$
						buildStyleList(group);
						styleGroup = i;
						if (taggedRTF) {
							addStyles();
							System.out.println("Styles added"); //$NON-NLS-1$
						}
						parseStyles();
						if (taggedRTF) {
							internalValue = getValue(winInternal);
							externalValue = getValue(winExternal);
							noTranslateValue = getValue(not_translate);
						}
					}
					if (group.indexOf("\\fonttbl") != -1) { //$NON-NLS-1$
						buildFontList(group);
						fontGroup = i;
					}
					if (group.indexOf("\\colortbl") != -1) { //$NON-NLS-1$
						buildColorList(group);
						colorGroup = i;
					}

					if (group.indexOf("\\fonttbl") != -1 //$NON-NLS-1$
							|| group.indexOf("\\filetbl") != -1 //$NON-NLS-1$
							|| group.indexOf("\\colortbl") != -1 //$NON-NLS-1$
							|| group.indexOf("\\stylesheet") != -1 //$NON-NLS-1$
							|| group.indexOf("\\listtable") != -1 //$NON-NLS-1$
							|| group.indexOf("\\revtable") != -1 //$NON-NLS-1$
							|| group.indexOf("\\rsidtable") != -1 //$NON-NLS-1$
							|| group.indexOf("\\generator") != -1 //$NON-NLS-1$
							|| group.indexOf("\\info") != -1 //$NON-NLS-1$
							|| group.indexOf("\\colorschememapping") != -1 //$NON-NLS-1$
							|| group.indexOf("\\latentstyles") != -1 //$NON-NLS-1$
							|| group.indexOf("\\pgptbl") != -1 //$NON-NLS-1$
							|| group.indexOf("\\operator") != -1 //$NON-NLS-1$
							|| group.indexOf("\\xmlnstbl") != -1 //$NON-NLS-1$
							|| group.indexOf("\\fchars") != -1 //$NON-NLS-1$
							|| group.indexOf("\\lchars") != -1 //$NON-NLS-1$
							|| group.indexOf("\\pgptbl") != -1 //$NON-NLS-1$
							|| group.indexOf("\\listtext") != -1 //$NON-NLS-1$
							|| group.indexOf("\\wgrffmtfilter") != -1 //$NON-NLS-1$
							|| group.indexOf("\\themedata") != -1 //$NON-NLS-1$
							|| group.indexOf("\\datastore") != -1 //$NON-NLS-1$
							|| group.indexOf("\\defchp") != -1 //$NON-NLS-1$
							|| group.indexOf("\\defpap") != -1 //$NON-NLS-1$
							|| group.indexOf("\\defshp") != -1 //$NON-NLS-1$
							|| group.indexOf("\\shp") != -1 //$NON-NLS-1$
							|| group.indexOf("\\pgdsctbl") != -1) { //$NON-NLS-1$

						// should be ignored, unless it contains text
						if (group.indexOf("\\par") != -1 //$NON-NLS-1$
								|| group.indexOf("\\cell") != -1 //$NON-NLS-1$
								|| group.indexOf("\\row") != -1 //$NON-NLS-1$
								|| group.indexOf("\\nestcell") != -1 //$NON-NLS-1$
								|| group.indexOf("\\dobypara") != -1) { //$NON-NLS-1$						
							mainText = mainText + group;
							mainGroups.put("" + i, ""); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else {
						mainText = mainText + group;
						mainGroups.put("" + i, ""); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				firstPartMonitor.worked(1);

				System.out.println("Groups parsed"); //$NON-NLS-1$

				fillIgnore();

				output = new FileOutputStream(xliffFile);
				skeleton = new FileOutputStream(skeletonFile);

				writeSkl(header + "\n"); //$NON-NLS-1$

				writeHeader();
				System.out.println("Header written"); //$NON-NLS-1$

				firstPartMonitor.subTask(Messages.getString("rtf.Rtf2Xliff.task6"));
				for (int h = 0; h < groups.size() - 1; h++) {
					// 是否取消操作
					if (firstPartMonitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("rtf.cancel"));
					}

					if (mainGroups.containsKey("" + h)) { //$NON-NLS-1$
						continue;
					}
					if (h == fontGroup) {
						writeSkl("{\\fonttbl \n"); //$NON-NLS-1$
						for (int k = 0; k < fonts.size(); k++) {
							writeSkl(fonts.get(k) + "\n"); //$NON-NLS-1$
						}
						writeSkl("}\n\n"); //$NON-NLS-1$
						continue;
					}
					if (h == colorGroup) {
						writeSkl("{\\colortbl;"); //$NON-NLS-1$
						for (int k = 0; k < colorList.size(); k++) {
							writeSkl(colorList.get(k) + ";"); //$NON-NLS-1$
						}
						writeSkl("}\n\n"); //$NON-NLS-1$
						continue;
					}
					if (h == styleGroup) {
						writeSkl("{\\stylesheet \n"); //$NON-NLS-1$
						for (int k = 0; k < styles.size(); k++) {
							writeSkl(styles.get(k) + "\n"); //$NON-NLS-1$
						}
						writeSkl("}\n\n"); //$NON-NLS-1$
						continue;
					}
					String group = groups.get(h);
					writeSkl(group);
					writeSkl("\n\n"); //$NON-NLS-1$
				}
				firstPartMonitor.worked(1);
				firstPartMonitor.done();

				mainText = parseMainText(Progress.getSubMonitor(monitor, 5));
				parse(mainText, Progress.getSubMonitor(monitor, 2));
				System.out.println("Processing " + segments.size() + " segments"); //$NON-NLS-1$ //$NON-NLS-2$
				checkSegments(Progress.getSubMonitor(monitor, 1));
				writeSkl("\n}\n"); //$NON-NLS-1$

				writeStr("</body>\n"); //$NON-NLS-1$
				writeStr("</file>\n"); //$NON-NLS-1$
				writeStr("</xliff>"); //$NON-NLS-1$

				output.close();
				skeleton.close();

				result.put(Converter.ATTR_XLIFF_FILE, xliffFile);
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				if (taggedRTF) {
					if (e instanceof ConverterException) {
						throw (ConverterException)e;
					} else {
						ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("rtf.Rtf2Xliff.msg3"),
								e);
					}
				} else {
					if (e instanceof ConverterException) {
						throw (ConverterException)e;
					} else {
						ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("rtf.Rtf2Xliff.msg4"), e);
					}
				}
			} finally {
				monitor.done();
			}
			return result;
		}

		/**
		 * Load symbols.
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void loadSymbols() throws SAXException, IOException {
			symbols = new Hashtable<Integer, String>();
			SAXBuilder b = new SAXBuilder();
			Document d = b.build(programFoler + "ini/symbol.xml"); //$NON-NLS-1$
			Element r = d.getRootElement();
			List<Element> l = r.getChildren();
			Iterator<Element> i = l.iterator();
			while (i.hasNext()) {
				Element e = i.next();
				symbols.put(new Integer(e.getAttributeValue("code")), e.getText()); //$NON-NLS-1$
			}
			i = null;
			l = null;
			r = null;
			d = null;
			b = null;
		}

		/**
		 * Parses the group.
		 * @param group
		 *            the group
		 * @return the string
		 */
		private String parseGroup(String group, IProgressMonitor monitor) {
			int size = group.length();
			// 如果 size 大于 10000,则把总任务数按比例缩小 100 倍；如果 size 大于 100000,则把总任务数按比例缩小 1000 倍。
			int scale = 1;
			if (size > 100000) {
				scale = 1000;
			} else if (size > 10000) {
				scale = 100;
			}
			int totalTask = size / scale;
			monitor.beginTask("", totalTask);
			monitor.subTask(Messages.getString("rtf.Rtf2Xliff.task7"));
			Stack<String> localStack = new Stack<String>();
			String buff = ""; //$NON-NLS-1$
			int count = 0;
			for (int i = 0; i < group.length(); i++) {
				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("rtf.cancel"));
				}
				char c = group.charAt(i);
				if (c == '{') {
					localStack.push(buff);
					buff = ""; //$NON-NLS-1$
				}
				buff = buff + c;
				if (c == '}') {
					String clean = cleanGroup(buff);
					buff = localStack.pop();
					if (buff.matches(".*\\\\[a-zA-Z]+") && clean.matches("[a-zA-z0-9].*")) { //$NON-NLS-1$ //$NON-NLS-2$
						buff = buff + " "; //$NON-NLS-1$
					}
					if (buff.matches(".*\\\\[a-zA-Z]+[0-9]+") && clean.matches("[0-9].*")) { //$NON-NLS-1$ //$NON-NLS-2$
						buff = buff + " "; //$NON-NLS-1$
					}
					buff = buff + clean;
				}
				count++;
				int temp = count / scale;
				count %= scale;
				if (temp > 0) {
					monitor.worked(temp);
				}
			}
			monitor.done();
			return buff;
		}

		/**
		 * Clean group.
		 * @param buff
		 *            the buff
		 * @return the string
		 */
		private String cleanGroup(String buff) {

			if (buff.indexOf("\\ltrch") != -1 && buff.indexOf("\\rtlch") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
				buff = removeControl(buff, "\\ltrch"); //$NON-NLS-1$
				buff = removeControl(buff, "\\rtlch"); //$NON-NLS-1$
			}
			if (buff.indexOf("\\fcs0") != -1 && buff.indexOf("\\fcs1") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
				buff = removeControl(buff, "\\fcs"); //$NON-NLS-1$
			}

			if (buff.matches("\\{(\\\\*)?\\\\.*\\}")) { //$NON-NLS-1$
				// contains text and controls
				// braces should be preserved
				return buff;
			}
			if (buff.matches("\\{.*\\}")) { //$NON-NLS-1$
				// contains text without control words
				// braces should be removed
				return buff.substring(1, buff.length() - 1);
			}
			return buff;
		}

		/**
		 * Removes the control.
		 * @param string
		 *            the string
		 * @param ctrl
		 *            the ctrl
		 * @return the string
		 */
		private String removeControl(String string, String ctrl) {
			int index = string.indexOf(ctrl);
			while (index != -1) {
				String left = string.substring(0, index);
				if (left.endsWith("\\*")) { //$NON-NLS-1$
					left = left.substring(0, left.length() - 2);
				}
				String right = string.substring(index + ctrl.length());
				if (right.matches("^[0-9]+\\s[a-zA-Z0-9\\s].*")) { //$NON-NLS-1$
					right = right.replaceAll("^[0-9]+\\s", ""); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					right = right.replaceAll("^[0-9]+", ""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (left.matches(".*\\\\[a-zA-Z]+") && right.matches("^[a-zA-Z0-9\\s].*")) { //$NON-NLS-1$ //$NON-NLS-2$
					right = " " + right; //$NON-NLS-1$
				} else if (left.matches(".*\\\\[a-zA-Z]+[0-9]+") && right.matches("[0-9\\s].*")) { //$NON-NLS-1$ //$NON-NLS-2$
					right = " " + right; //$NON-NLS-1$
				}
				string = left + right;
				index = string.indexOf(ctrl);
			}
			return string;
		}

		/**
		 * Parses the styles.
		 * @throws ConverterException 
		 */
		private void parseStyles() throws ConverterException {
			styleFonts = new Hashtable<String, String>();
			cfTable = new Hashtable<String, String>();
			for (int i = 0; i < styles.size(); i++) {
				String style = styles.get(i);
				StringTokenizer tk = new StringTokenizer(style, "\\{} \t", true); //$NON-NLS-1$
				String control = ""; //$NON-NLS-1$
				String value = ""; //$NON-NLS-1$
				while (tk.hasMoreElements()) {
					String token = tk.nextToken();
					if (token.length() == 1) {
						continue;
					}
					String ctl = getControl("\\" + token); //$NON-NLS-1$
					if (ctl.equals("s") || // paragraph style //$NON-NLS-1$
							ctl.equals("cs") || // character style //$NON-NLS-1$
							ctl.equals("ts") || // table style //$NON-NLS-1$
							ctl.equals("ds")) { // section style //$NON-NLS-1$

						control = getValue("\\" + token); //$NON-NLS-1$
					}
					if (ctl.equals("f")) { //$NON-NLS-1$
						value = getValue("\\" + token); //$NON-NLS-1$
					}
				}
				if (!control.equals("") && !value.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
					if (fontTable.containsKey(value)) {
						styleFonts.put(control, value);
					} else {
						MessageFormat mf = new MessageFormat(taggedRTF ? Messages.getString("rtf.Rtf2Xliff.msg5") : Messages.getString("rtf.Rtf2Xliff.msg1")); //$NON-NLS-1$
						Object[] args = { value };
						String msg = mf.format(args);
						System.err.println(msg);
						args = null;
						fontTable.put(control, "0"); //$NON-NLS-1$
//						throw new ConverterException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg));
					}
				}
				cfTable.put(getStyle(style), getValue("cf", style)); //$NON-NLS-1$
			}
		}

		/**
		 * Builds the charsets.
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void buildCharsets() throws SAXException, IOException {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(programFoler + "ini/rtf_encodings.xml"); //$NON-NLS-1$
			charsets = new Hashtable<String, String>();
			Element root = doc.getRootElement();
			List<Element> list = root.getChildren("encoding"); //$NON-NLS-1$
			Iterator<Element> it = list.iterator();
			while (it.hasNext()) {
				Element e = it.next();
				charsets.put(e.getAttributeValue("codePage"), getEncoding(e.getText().trim())); //$NON-NLS-1$
			}
		}

		/**
		 * Builds the font list.
		 * @param group
		 *            the group
		 * @throws Exception
		 *             the exception
		 */
		private void buildFontList(String group) throws Exception {
			int upr = group.indexOf("\\upr"); //$NON-NLS-1$
			int ud = group.indexOf("\\ud"); //$NON-NLS-1$
			if (upr != -1 && ud != -1) {
				group = group.substring(upr + 4, ud);
				group = group.substring(0, group.lastIndexOf("}")); //$NON-NLS-1$
			}
			fontTable = new Hashtable<String, String>();
			ignorableFonts = new Hashtable<String, String>();
			fonts = new Vector<String>();
			int level = 0;
			int i = group.indexOf("{", group.indexOf("\\fonttbl")); //$NON-NLS-1$ //$NON-NLS-2$
			StringBuffer buffer = new StringBuffer();
			while (i < group.length()) {
				char c = group.charAt(i++);
				if (c == '\r' || c == '\n') {
					continue;
				}
				buffer.append(c);
				if (c == '{') {
					level++;
				}
				if (c == '}') {
					level--;
				}
				if (level == 0) {
					String font = buffer.toString().trim();
					if (!font.trim().equals("")) { //$NON-NLS-1$
						font = parseFont(font);
					}
					fonts.add(font);
					buffer = null;
					buffer = new StringBuffer();
				}
			}
		}

		/**
		 * Parses the font.
		 * @param font
		 *            the font
		 * @return the string
		 */
		private String parseFont(String font) {
			StringTokenizer tk = new StringTokenizer(font, "\\{} \t", true); //$NON-NLS-1$
			String control = ""; //$NON-NLS-1$
			String value = ""; //$NON-NLS-1$
			while (tk.hasMoreElements()) {
				String token = tk.nextToken();
				if (token.length() == 1) {
					continue;
				}
				if (getControl("\\" + token).equals("f")) { //$NON-NLS-1$ //$NON-NLS-2$
					control = getValue("\\" + token); //$NON-NLS-1$
				}
				if (getControl("\\" + token).equals("fcharset")) { //$NON-NLS-1$ //$NON-NLS-2$
					value = getValue("\\" + token); //$NON-NLS-1$
				}
			}
			if (!control.equals("") && !value.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				if (value.equals("2")) { //$NON-NLS-1$
					if (font.indexOf("Symbol") != -1) { //$NON-NLS-1$
						ignorableFonts.put(control, ""); //$NON-NLS-1$
					} else {
						font = font.replace("fcharset2", "fcharset0"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				if (charsets.containsKey(value)) {
					fontTable.put(control, value);
				} else {
					MessageFormat mf = new MessageFormat(Messages.getString("rtf.Rtf2Xliff.msg2")); //$NON-NLS-1$
					Object[] args = { value };
					System.err.println(mf.format(args));
					args = null;
					fontTable.put(control, "0"); //$NON-NLS-1$
				}
			}
			return font;
		}

		/**
		 * Process header.
		 */
		private void processHeader() {
			defaultUC = "0"; //$NON-NLS-1$
			defaultFont = ""; //$NON-NLS-1$
			defaultLang = ""; //$NON-NLS-1$
			defaultCpg = "1252"; //$NON-NLS-1$
			defaultAF = ""; //$NON-NLS-1$

			StringTokenizer tk = new StringTokenizer(header, "\\", true); //$NON-NLS-1$
			while (tk.hasMoreTokens()) {
				String token = tk.nextToken();
				if (token.equals("\\")) { //$NON-NLS-1$
					token = token + tk.nextToken();
				}
				String ctrlString = getControl(token);
				if (ctrlString.equals("ansicpg")) { //$NON-NLS-1$
					defaultCpg = getValue(token);

					// fixed a bug 921 by john.
					String tmpEncoding = getEncoding(defaultCpg);
					if (tmpEncoding != null) {
						srcEncoding = tmpEncoding;
					}
				}
				if (ctrlString.equals("deff")) { //$NON-NLS-1$
					defaultFont = getValue(token);
				}
				if (ctrlString.equals("deflang")) { //$NON-NLS-1$
					defaultLang = getValue(token);
				}
				if (ctrlString.equals("adeff")) { //$NON-NLS-1$
					System.out.println("Default afont: " + getValue(token)); //$NON-NLS-1$
				}
				if (ctrlString.equals("uc")) { //$NON-NLS-1$
					defaultUC = getValue(token);
				}
			}
			status = new Hashtable<String, Object>();
			status.put("defaultUC", defaultUC); //$NON-NLS-1$
			status.put("defaultFont", defaultFont); //$NON-NLS-1$
			status.put("defaultLang", defaultLang); //$NON-NLS-1$
			status.put("srcEncoding", srcEncoding); //$NON-NLS-1$
			status.put("defaultCpg", defaultCpg); //$NON-NLS-1$
			status.put("defaultCF", defaultCF); //$NON-NLS-1$
			status.put("defaultAF", defaultAF); //$NON-NLS-1$
			status.put("inLOCH", new Boolean(inLOCH)); //$NON-NLS-1$
			status.put("inHICH", new Boolean(inHICH)); //$NON-NLS-1$
			status.put("inDBCH", new Boolean(inDBCH)); //$NON-NLS-1$

			stack.push(status);
			defaultStatus = status;
		}

		/**
		 * Gets the encoding.
		 * @param encoding
		 *            the encoding
		 * @return the encoding
		 */
		private String getEncoding(String encoding) {
			String[] codes = TextUtil.getPageCodes();
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("windows-" + encoding) != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
			if (encoding.equals("10000")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("macroman") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("10001")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("shift_jis") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("10006")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("macgreek") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("10007")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("maccyrillic") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("10029")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("maccentraleurope") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("10079")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("maciceland") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("10081")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("macturkish") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("65000")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("utf-7") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("650001")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("utf-8") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("932")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("shift_jis") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("936")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("gb2312") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("949")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("euc-kr") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("950")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("big5") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equals("1361")) { //$NON-NLS-1$
				for (int h = 0; h < codes.length; h++) {
					if (codes[h].toLowerCase().indexOf("johab") != -1) { //$NON-NLS-1$
						return codes[h];
					}
				}
			}
			if (encoding.equalsIgnoreCase("Symbol")) { //$NON-NLS-1$
				return "Symbol"; //$NON-NLS-1$
			}
			if (defaultStatus != null) {
				return (String) defaultStatus.get("srcEncoding"); //$NON-NLS-1$
			}
			return null;
		}

		/**
		 * Parses the main text.
		 * @return the string
		 * @throws Exception
		 *             the exception
		 */
		private String parseMainText(IProgressMonitor monitor) throws Exception {
			// 此处把任务分为 10 个部分：第一个 while 循环占 1，每二个 for 循环占 1，最后的 paseGroup 方法占 8。
			monitor.beginTask("", 10);
			System.out.println("Replacing braces..."); //$NON-NLS-1$

			mainText = replaceToken(mainText, "\\{", "\uE008"); //$NON-NLS-1$ //$NON-NLS-2$
			mainText = replaceToken(mainText, "\\}", "\uE007"); //$NON-NLS-1$ //$NON-NLS-2$
			mainText = replaceToken(mainText, "\\\\", "\uE011"); //$NON-NLS-1$ //$NON-NLS-2$

			System.out.println("Braces replaced"); //$NON-NLS-1$

			StringTokenizer tk = new StringTokenizer(mainText, "{}\\", true); //$NON-NLS-1$

			System.out.println("Processing " + tk.countTokens() + " tokens"); //$NON-NLS-1$ //$NON-NLS-2$

			// 对每一部分的内容再进行细分
			int tokenSize = tk.countTokens();
			IProgressMonitor subMonitor1 = Progress.getSubMonitor(monitor, 1);
			subMonitor1.beginTask("", tokenSize);
			subMonitor1.subTask(Messages.getString("rtf.Rtf2Xliff.task8"));
			Vector<String> v = new Vector<String>();
			while (tk.hasMoreElements()) {
				// 是否取消操作
				if (subMonitor1.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("rtf.cancel"));
				}

				String token = tk.nextToken();
				if (token.equals("\\")) { //$NON-NLS-1$
					token = token + tk.nextToken();
				}
				if (token.equals("\\*")) { //$NON-NLS-1$
					token = token + tk.nextToken() + tk.nextToken();
				}
				if (token.startsWith("\\'")) { //$NON-NLS-1$
					if (token.length() == 4) {
						v.add(token);
					} else {
						v.add(token.substring(0, 4));
						v.add(token.substring(4));
					}
				} else if (token.startsWith("\\")) { //$NON-NLS-1$
					String ctl = getControl(token);
					String value = getValue(token);
					String s = token.substring(0, token.indexOf(ctl)) + ctl + value;
					v.add(s);
					String remainder = token.substring(s.length());
					if (remainder.startsWith(" ")) { //$NON-NLS-1$
						if (!remainder.matches("^\\s[a-zA-Z0-9\\s].*")) { //$NON-NLS-1$
							remainder = remainder.substring(1);
						} else if (s.matches(".*[0-9]")) { //$NON-NLS-1$
							remainder = remainder.substring(1);
						}
					}
					if (!remainder.equals("")) { //$NON-NLS-1$
						v.add(remainder);
					}
				} else {
					v.add(token);
				}
				subMonitor1.worked(1);
			}
			subMonitor1.done();

			String result = ""; //$NON-NLS-1$
			int level = 0;
			System.out.println("Processing " + v.size() + " frag...");

			// 对第二部分的同样进行细分
			IProgressMonitor subMonitor2 = Progress.getSubMonitor(monitor, 1);
			subMonitor2.beginTask("", v.size());
			subMonitor2.subTask(Messages.getString("rtf.Rtf2Xliff.task9"));

			for (int i = 0; i < v.size(); i++) {
				// 是否取消操作
				if (subMonitor2.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("rtf.cancel"));
				}

				String frag = v.get(i);
				frag = replaceToken(frag, "\\emdash ", "" + '\u2014'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\endash ", "" + '\u2013'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\lquote ", "" + '\u2018'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\rquote ", "" + '\u2019'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\ldblquote ", "" + '\u201C'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\rdblquote ", "" + '\u201D'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\tab ", "" + '\u0009'); //$NON-NLS-1$ //$NON-NLS-2$

				// replace the same characters again, without the space
				frag = replaceToken(frag, "\\emdash", "" + '\u2014'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\endash", "" + '\u2013'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\lquote", "" + '\u2018'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\rquote", "" + '\u2019'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\ldblquote", "" + '\u201C'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\rdblquote", "" + '\u201D'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\tab", "" + '\u0009'); //$NON-NLS-1$ //$NON-NLS-2$

				// remove special spaces and replace with Unicode version
				frag = replaceToken(frag, "\\enspace", "" + '\u2002'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\emspace", "" + '\u2003'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\qmspace", "" + '\u2005'); //$NON-NLS-1$ //$NON-NLS-2$
				frag = replaceToken(frag, "\\~", "" + '\u00A0'); // non breaking //$NON-NLS-1$ //$NON-NLS-2$
				// space
				// //$NON-NLS-1$
				// //$NON-NLS-2$

				// Hyphens
				frag = replaceToken(frag, "\\_", "" + '\u2011'); // non breaking //$NON-NLS-1$ //$NON-NLS-2$
				// hyphen
				// //$NON-NLS-1$
				// //$NON-NLS-2$
				frag = replaceToken(frag, "\\-", "" + '\u00AD'); // soft hyphen //$NON-NLS-1$ //$NON-NLS-2$
				// //$NON-NLS-1$
				// //$NON-NLS-2$

				if (frag.equals("{")) { //$NON-NLS-1$
					saveCurrStatus();
					level++;
				} else if (frag.equals("}")) { //$NON-NLS-1$
					level--;
					restoreStatus();
				}
				if (frag.startsWith("\\\'")) { //$NON-NLS-1$
					String run = frag;
					while (v.get(i + 1).startsWith("\\\'")) { //$NON-NLS-1$
						run = run + v.get(i + 1);
						i++;
					}
					frag = decode(run);
					if (frag.trim().length() == 1 && skip > 0 && result.charAt(result.length() - 1) == frag.charAt(0)) {
						frag = ""; //$NON-NLS-1$
					}
					skip--;
				} else if (frag.startsWith("\\")) { //$NON-NLS-1$
					String ctl = getControl(frag);
					String remainder = getReminder(frag);
					if (ctl.equals("loch")) { //$NON-NLS-1$
						inDBCH = false;
						inLOCH = true;
						inHICH = false;
						frag = remainder;
					} else if (ctl.equals("hich")) { //$NON-NLS-1$
						inDBCH = false;
						inLOCH = false;
						inHICH = true;
						frag = remainder;
					} else if (ctl.equals("dbch")) { //$NON-NLS-1$
						inDBCH = true;
						inLOCH = false;
						inHICH = false;
						frag = remainder;
					} else if (ctl.equals("uc")) { //$NON-NLS-1$
						defaultUC = getValue(frag);
						frag = remainder;
					} else if (ctl.equals("u")) { //$NON-NLS-1$
						frag = decodeU(getValue(frag)) + remainder;
						skip = Integer.parseInt(defaultUC);
					} else if (ctl.equals("f")) { //$NON-NLS-1$
						String value = getValue(frag);
						if (!fontTable.containsKey(value)) {
							value = defaultFont;
						}
						defaultCpg = fontTable.get(value);
						srcEncoding = charsets.get(defaultCpg);
						if (value.equals(defaultFont) || ignorableFonts.containsKey(value)) {
							frag = remainder;
						}
						defaultFont = value;
					} else if (ctl.equals("af")) { //$NON-NLS-1$
						String value = getValue(frag);
						if (!value.equals(defaultAF)) {
							defaultAF = value;
						}
						frag = remainder;
					} else if (ctl.equals("cf")) { //$NON-NLS-1$
						String value = getValue(frag);
						if (value.equals(defaultCF)) {
							frag = remainder;
						}
						defaultCF = value;
					} else if (ctl.equals("s") || //$NON-NLS-1$
							ctl.equals("cs") || //$NON-NLS-1$
							ctl.equals("ts") || //$NON-NLS-1$
							ctl.equals("ds")) { //$NON-NLS-1$

						String style = getValue(frag);
						if (styleFonts.containsKey(style)) {
							String value = styleFonts.get(style);
							defaultCpg = fontTable.get(value);
							srcEncoding = charsets.get(defaultCpg);
							defaultFont = value;
						}
						if (cfTable.containsKey(style)) {
							defaultCF = cfTable.get(style);
						} else {
							defaultCF = "1"; //$NON-NLS-1$
						}
					} else if (ctl.equals("pard")) { //$NON-NLS-1$
						resetStatus();
					} else if (ignore.containsKey(ctl)) {
						String value = getValue(frag);
						if (value.equals("")) { //$NON-NLS-1$
							value = ctl;
						}
						frag = remainder;
					}
					if (frag.matches("[0-9].*")) { //$NON-NLS-1$
						if (result.matches(".*\\\\[a-z]+[0-9]+")) { //$NON-NLS-1$
							frag = " " + frag; //$NON-NLS-1$
						}
					} else if (frag.matches("[a-zA-Z].*")) { //$NON-NLS-1$
						if (result.matches(".*\\\\[a-z]+")) { //$NON-NLS-1$
							frag = " " + frag; //$NON-NLS-1$
						}
					}

				} else {
					// plain text
					if (skip > 0 && frag.startsWith(" ?")) { //$NON-NLS-1$
						frag = frag.substring(2);
						skip--;
					}
					if (skip > 0 && frag.startsWith("?")) { //$NON-NLS-1$
						frag = frag.substring(1);
						skip--;
					}
				}
				if (frag.matches("[0-9].*")) { //$NON-NLS-1$
					if (result.matches(".*\\\\[a-z]+[0-9]*")) { //$NON-NLS-1$
						frag = " " + frag; //$NON-NLS-1$
					}
				} else if (frag.matches("[a-zA-Z].*")) { //$NON-NLS-1$
					if (result.matches(".*\\\\[a-z]+")) { //$NON-NLS-1$
						frag = " " + frag; //$NON-NLS-1$
					}
				}
				result = result + frag;
				subMonitor2.worked(1);
			}
			subMonitor2.done();
			// 此 paseGroup 方法非常耗时，经过测试，文件大小为 1867519 bytes（约 1.8m) 的 rtf 文件，在 paseGroup 方法中需热循环 1793397 次。
			String temp = parseGroup(result, Progress.getSubMonitor(monitor, 8));
			monitor.done();

			return temp;
		}

		/**
		 * Gets the reminder.
		 * @param token
		 *            the token
		 * @return the reminder
		 */
		private String getReminder(String token) {
			String control = getControl(token);
			token = token.substring(token.indexOf(control) + control.length());
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < token.length(); i++) {
				char c = token.charAt(i);
				if ((c >= '0' && c <= '9') || c == '-') {
					buffer.append(c);
				} else {
					if (c == ' ') {
						buffer.append(c);
					}
					break;
				}
			}
			return token.substring(buffer.toString().length());
		}

		/**
		 * Reset status.
		 */
		private void resetStatus() {
			defaultUC = (String) defaultStatus.get("defaultUC"); //$NON-NLS-1$
			defaultFont = (String) defaultStatus.get("defaultFont"); //$NON-NLS-1$
			defaultAF = (String) defaultStatus.get("defaultAF"); //$NON-NLS-1$
			defaultLang = (String) defaultStatus.get("defaultLang"); //$NON-NLS-1$
			srcEncoding = (String) defaultStatus.get("srcEncoding"); //$NON-NLS-1$
			defaultCpg = (String) defaultStatus.get("defaultCpg"); //$NON-NLS-1$
			defaultCF = (String) defaultStatus.get("defaultCF"); //$NON-NLS-1$
		}

		/**
		 * Decode.
		 * @param string
		 *            the string
		 * @return the string
		 * @throws UnsupportedEncodingException
		 *             the unsupported encoding exception
		 */
		private String decode(String string) throws UnsupportedEncodingException {
			String remainder = ""; //$NON-NLS-1$
			if (string.indexOf(" ") != -1) { //$NON-NLS-1$
				remainder = string.substring(string.indexOf(" ") + 1); //$NON-NLS-1$
			}
			string = string.replaceAll("\'", ""); //$NON-NLS-1$ //$NON-NLS-2$
			StringTokenizer tk = new StringTokenizer(string, "\\"); //$NON-NLS-1$
			String converted = ""; //$NON-NLS-1$
			if (inDBCH) {
				int size = tk.countTokens();
				byte[] array = new byte[size];
				int j = 0;
				while (tk.hasMoreTokens()) {
					String s = tk.nextToken();
					array[j++] = (byte) Integer.parseInt(s, 16);
				}
				if (!srcEncoding.equals("Symbol")) { //$NON-NLS-1$
					converted = new String(array, srcEncoding);
				} else {
					converted = new String(array, getEncoding("1252")); //$NON-NLS-1$
				}
			} else if (inHICH) {
				while (tk.hasMoreTokens()) {
					String s = tk.nextToken();
					if (!srcEncoding.equals("Symbol")) { //$NON-NLS-1$
						byte[] array = new byte[1];
						array[0] = (byte) Integer.parseInt(s, 16);
						converted = converted + new String(array, srcEncoding);
					} else {
						converted = converted + symbols.get(new Integer(Integer.parseInt(s, 16)));
					}
				}
			} else {
				// inLOCH
				if (!srcEncoding.equals("Symbol")) { //$NON-NLS-1$
					while (tk.hasMoreTokens()) {
						String s = tk.nextToken();
						byte b = (byte) Integer.parseInt(s, 16);
						byte[] array = new byte[2];
						if (isLeadByte(b)) {
							// it is a leading byte, get next one and convert
							array[0] = b;
							array[1] = (byte) Integer.parseInt(tk.nextToken(), 16);
						} else {
							array[0] = b;
							array[1] = 0;
						}
						String ss = ""; //$NON-NLS-1$
						try {
							ss = new String(array, srcEncoding);
						} catch (Exception e) {
							ss = new String(array, getEncoding("1252")); //$NON-NLS-1$
						}
						converted = converted + ss.charAt(0);
					}
				} else {
					while (tk.hasMoreTokens()) {
						String s = tk.nextToken();
						converted = converted + symbols.get(Integer.parseInt(s, 16));
					}
				}
			}
			converted = replaceToken(converted, "{", "\uE008"); //$NON-NLS-1$ //$NON-NLS-2$
			converted = replaceToken(converted, "}", "\uE007"); //$NON-NLS-1$ //$NON-NLS-2$
			converted = replaceToken(converted, "\\", "\uE011"); //$NON-NLS-1$ //$NON-NLS-2$

			return converted + remainder;
		}

		/**
		 * Restore status.
		 */
		private void restoreStatus() {
			if (!stack.isEmpty()) {
				status = stack.pop();
				defaultUC = (String) status.get("defaultUC"); //$NON-NLS-1$
				defaultFont = (String) status.get("defaultFont"); //$NON-NLS-1$
				defaultAF = (String) status.get("defaultAF"); //$NON-NLS-1$
				defaultLang = (String) status.get("defaultLang"); //$NON-NLS-1$
				srcEncoding = (String) status.get("srcEncoding"); //$NON-NLS-1$
				defaultCpg = (String) status.get("defaultCpg"); //$NON-NLS-1$
				inLOCH = ((Boolean) status.get("inLOCH")).booleanValue(); //$NON-NLS-1$
				inHICH = ((Boolean) status.get("inHICH")).booleanValue(); //$NON-NLS-1$
				inDBCH = ((Boolean) status.get("inDBCH")).booleanValue(); //$NON-NLS-1$
			} else {
				status = new Hashtable<String, Object>();
				status.put("defaultUC", defaultUC); //$NON-NLS-1$
				status.put("defaultFont", defaultFont); //$NON-NLS-1$
				status.put("defaultLang", defaultLang); //$NON-NLS-1$
				status.put("srcEncoding", srcEncoding); //$NON-NLS-1$
				status.put("defaultCpg", defaultCpg); //$NON-NLS-1$
				status.put("defaultAF", defaultAF); //$NON-NLS-1$
				status.put("inLOCH", new Boolean(inLOCH)); //$NON-NLS-1$
				status.put("inHICH", new Boolean(inHICH)); //$NON-NLS-1$
				status.put("inDBCH", new Boolean(inDBCH)); //$NON-NLS-1$
			}
		}

		/**
		 * Save curr status.
		 */
		private void saveCurrStatus() {
			status = new Hashtable<String, Object>();
			status.put("defaultUC", defaultUC); //$NON-NLS-1$
			status.put("defaultFont", defaultFont); //$NON-NLS-1$
			status.put("defaultLang", defaultLang); //$NON-NLS-1$
			status.put("srcEncoding", srcEncoding); //$NON-NLS-1$
			status.put("defaultCpg", defaultCpg); //$NON-NLS-1$
			status.put("defaultAF", defaultAF); //$NON-NLS-1$
			status.put("inLOCH", new Boolean(inLOCH)); //$NON-NLS-1$
			status.put("inHICH", new Boolean(inHICH)); //$NON-NLS-1$
			status.put("inDBCH", new Boolean(inDBCH)); //$NON-NLS-1$

			stack.push(status);
		}

		/**
		 * Write str.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeStr(String string) throws IOException {
			string = replaceToken(string, "\uE008", "{"); //$NON-NLS-1$ //$NON-NLS-2$
			string = replaceToken(string, "\uE007", "}"); //$NON-NLS-1$ //$NON-NLS-2$
			string = replaceToken(string, "\uE011", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$

			output.write(string.getBytes("UTF-8")); //$NON-NLS-1$
		}

		/**
		 * Builds the groups.
		 */
		private void buildGroups() {
			groups = new Vector<String>();
			int level = 0;
			int i = header.length();
			StringBuffer buffer = new StringBuffer();
			int size = content.length();
			while (i < size) {
				char c = content.charAt(i++);
				if (c != '\r' && c != '\n') {
					buffer.append(c);
				}
				if (c == '{') {
					level++;
				}
				if (c == '}') {
					level--;
				}
				if (level == 0) {
					groups.add(buffer.toString());
					buffer = null;
					buffer = new StringBuffer();
				}
			}
			groups.add(content.substring(i));
			content = null;
		}

		/**
		 * Builds the style list.
		 * @param group
		 *            the group
		 * @throws Exception
		 *             the exception
		 */
		private void buildStyleList(String group) throws Exception {
			int level = 0;
			int i = group.indexOf("{", 1); //$NON-NLS-1$
			StringBuffer buffer = new StringBuffer();
			while (i < group.length()) {
				char c = group.charAt(i++);
				if (c == '\n' || c == '\r') {
					continue;
				}
				buffer.append(c);
				if (c == '{') {
					level++;
				}
				if (c == '}') {
					level--;
				}
				if (level == 0) {
					String style = buffer.toString().trim();
					if (style.indexOf("tw4winMark") != -1) { //$NON-NLS-1$
						tw4winMark = true;
						winMark = getStyle(style)
								+ "\\v\\cf" + getValue("cf", style) + "\\sub\\f" + getValue("f", style) + "\\fs24"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					}
					if (style.indexOf("tw4winError") != -1) { //$NON-NLS-1$
						tw4winError = true;
					}
					if (style.indexOf("tw4winPopup") != -1) { //$NON-NLS-1$
						tw4winPopup = true;
					}
					if (style.indexOf("tw4winJump") != -1) { //$NON-NLS-1$
						tw4winJump = true;
					}
					if (style.indexOf("tw4winExternal") != -1) { //$NON-NLS-1$
						tw4winExternal = true;
						winExternal = getStyle(style)
								+ "\\cf" + getValue("cf", style) + "\\f" + getValue("f", style) + "\\lang1024"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					}
					if (style.indexOf("tw4winInternal") != -1) { //$NON-NLS-1$
						tw4winInternal = true;
						winInternal = getStyle(style)
								+ "\\cf" + getValue("cf", style) + "\\f" + getValue("f", style) + "\\lang1024"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						markStyle = "\\v\\cf" + getValue("cf", style) + "\\sub\\f" + getValue("f", style) + "\\fs24"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					}
					if (style.indexOf("tw4winTerm") != -1) { //$NON-NLS-1$
						tw4winTerm = true;
					}
					if (style.indexOf("DO_NOT_TRANSLATE") != -1) { //$NON-NLS-1$
						do_not_translate = true;
						not_translate = getStyle(style);
					}
					styles.add(style);
					buffer = null;
					buffer = new StringBuffer();
				}
			}
		}

		/**
		 * Gets the value.
		 * @param token
		 *            the token
		 * @return the value
		 */
		private String getValue(String token) {
			String control = getControl(token);
			if (control.equals("'")) { //$NON-NLS-1$
				return token.substring(token.indexOf("'"), token.indexOf("'") + 2); //$NON-NLS-1$ //$NON-NLS-2$
			}
			token = token.substring(token.indexOf(control) + control.length());
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < token.length(); i++) {
				char c = token.charAt(i);
				if ((c >= '0' && c <= '9') || c == '-') {
					buffer.append(c);
				} else {
					break;
				}
			}
			return buffer.toString();
		}

		/**
		 * Gets the control.
		 * @param token
		 *            the token
		 * @return the control
		 */
		private String getControl(String token) {
			if (token.trim().length() < 2 || "{\\".indexOf(token.trim().charAt(0)) == -1) { //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			}
			StringBuffer buffer = new StringBuffer();
			for (int i = 1; i < token.length(); i++) {
				char c = token.charAt(i);
				if (c == '\\' || c == '*' || c == '{') {
					continue;
				}
				if (c == '\'') {
					return "'"; //$NON-NLS-1$
				}
				if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
					break;
				}
				buffer.append(c);
			}
			return buffer.toString();
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
				string = before + newText + after;
				index = string.indexOf(token, index + newText.length());
			}
			return string;
		}

		/**
		 * Decode u.
		 * @param current
		 *            the current
		 * @return the string
		 */
		private String decodeU(String current) {
			String run = ""; //$NON-NLS-1$
			int i = 0;

			for (i = 0; i < current.length(); i++) {
				if (isDigit(current.charAt(i)) || current.charAt(i) == '-') {
					break;
				}
			}
			if (current.charAt(i) == '-') {
				run = "-"; //$NON-NLS-1$
				i++;
			}
			for (; i < current.length(); i++) {
				if (isDigit(current.charAt(i))) {
					run = run + current.charAt(i);
				} else {
					// avoid trailing spaces if any
					break;
				}
			}
			int value = Integer.parseInt(run);
			if (value < 0) {
				value = value + 65536;
			}
			if (!srcEncoding.equals("Symbol")) { //$NON-NLS-1$
				return "" + new Character((char) value); //$NON-NLS-1$
			}
			if (value > 0xF000) {
				value = value - 0xF000;
			}
			return symbols.get(new Integer(value));
		}

		/**
		 * Checks if is digit.
		 * @param c
		 *            the c
		 * @return true, if is digit
		 */
		private boolean isDigit(char c) {
			return c >= '0' && c <= '9';
		}

		/**
		 * Fill ignore.
		 */
		private void fillIgnore() {
			ignore = new Hashtable<String, String>();
			ignore.put("insrsid", ""); //$NON-NLS-1$ //$NON-NLS-2$
			ignore.put("charrsid", ""); //$NON-NLS-1$ //$NON-NLS-2$
			ignore.put("langfe", ""); //$NON-NLS-1$ //$NON-NLS-2$
			ignore.put("lang", ""); //$NON-NLS-1$ //$NON-NLS-2$
			ignore.put("langnp", ""); //$NON-NLS-1$ //$NON-NLS-2$
			ignore.put("sectrsid", ""); //$NON-NLS-1$ //$NON-NLS-2$
			ignore.put("pararsid", ""); //$NON-NLS-1$ //$NON-NLS-2$
			ignore.put("tblrsid", ""); //$NON-NLS-1$ //$NON-NLS-2$
			ignore.put("delrsid", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * Builds the color list.
		 * @param group
		 *            the group
		 */
		private void buildColorList(String group) {
			colorList = new Vector<String>();
			String list = group.substring(group.indexOf("colortbl") + "colortbl".length()).trim(); //$NON-NLS-1$ //$NON-NLS-2$
			list = list.substring(list.indexOf("\\"), list.indexOf("}")).trim(); //$NON-NLS-1$ //$NON-NLS-2$
			StringTokenizer tk = new StringTokenizer(list, ";"); //$NON-NLS-1$
			while (tk.hasMoreTokens()) {
				String color = tk.nextToken().trim();
				colorList.add(color);
			}
		}

		/**
		 * Adds the styles.
		 */
		private void addStyles() {
			String tw4winMarkColor = "\\red128\\green0\\blue128"; //$NON-NLS-1$
			String tw4winErrorColor = "\\red0\\green255\\blue0"; //$NON-NLS-1$
			String tw4winPopupColor = "\\red0\\green128\\blue0"; //$NON-NLS-1$
			String tw4winJumpColor = "\\red0\\green128\\blue128"; //$NON-NLS-1$
			String tw4winExternalColor = "\\red128\\green128\\blue128"; //$NON-NLS-1$
			String tw4winInternalColor = "\\red255\\green0\\blue0"; //$NON-NLS-1$
			String tw4winTermColor = "\\red0\\green0\\blue255"; //$NON-NLS-1$
			String doNotTranslateColor = "\\red128\\green0\\blue0"; //$NON-NLS-1$

			int code = getNextFreestyle();
			int font = getMaxFont();
			fonts.add("{\\f" + font + "\\fmodern\\fprq1 {\\*\\panose 02070309020205020404}\\fcharset0 Courier New;}"); //$NON-NLS-1$ //$NON-NLS-2$
			fontTable.put("" + font, "0"); //$NON-NLS-1$ //$NON-NLS-2$
			int color = 0;
			String style = ""; //$NON-NLS-1$

			if (!tw4winMark) {
				color = getColor(tw4winMarkColor);
				winMark = "\\cs" + code + "\\v\\cf" + color //$NON-NLS-1$ //$NON-NLS-2$
						+ "\\sub\\f" + font + "\\fs24"; //$NON-NLS-1$ //$NON-NLS-2$
				style = "{\\*\\cs" + code++ + " \\additive \\v\\cf" + color //$NON-NLS-1$ //$NON-NLS-2$
						+ "\\sub\\f" + font + "\\fs24 tw4winMark;}"; //$NON-NLS-1$ //$NON-NLS-2$
				styles.add(style);
				tw4winMark = true;
			}
			if (!tw4winError) {
				color = getColor(tw4winErrorColor);
				style = "{\\*\\cs" + code++ + " \\additive \\cf" + color + "\\fs40\\f" + font //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ "\\fs24 tw4winError;}"; //$NON-NLS-1$
				styles.add(style);
				tw4winError = true;
			}
			if (!tw4winPopup) {
				color = getColor(tw4winPopupColor);
				style = "{\\*\\cs" + code++ + " \\additive \\f" + font + "\\cf" + color + " tw4winPopup;}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				styles.add(style);
				tw4winPopup = true;
			}
			if (!tw4winJump) {
				color = getColor(tw4winJumpColor);
				style = "{\\*\\cs" + code++ + " \\additive \\f" + font + "\\cf" + color + " tw4winJump;}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				styles.add(style);
				tw4winJump = true;
			}
			if (!tw4winExternal) {
				winExternal = "\\cs" + code + "\\cf" + color + "\\f" + font //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ "\\lang1024"; //$NON-NLS-1$
				color = getColor(tw4winExternalColor);
				style = "{\\*\\cs" + code++ + " \\additive \\cf" + color + "\\f" + font //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ "\\lang1024 tw4winExternal;}"; //$NON-NLS-1$
				styles.add(style);
				tw4winExternal = true;
			}
			if (!tw4winInternal) {
				color = getColor(tw4winInternalColor);
				winInternal = "\\cs" + code + "\\cf" + color + "\\f" + font //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ "\\lang1024"; //$NON-NLS-1$
				style = "{\\*\\cs" + code++ + " \\additive \\cf" + color + "\\f" + font //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ "\\lang1024 tw4winInternal;}"; //$NON-NLS-1$
				styles.add(style);
				tw4winInternal = true;
			}
			if (!tw4winTerm) {
				color = getColor(tw4winTermColor);
				style = "{\\*\\cs" + code++ + " \\additive \\cf" + color + " tw4winTerm;}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				styles.add(style);
				tw4winTerm = true;
			}
			if (!do_not_translate) {
				color = getColor(doNotTranslateColor);
				style = "{\\*\\cs" + code++ + " \\additive \\cf" + color + "\\f" + font //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ "\\lang1024 DO_NOT_TRANSLATE;}"; //$NON-NLS-1$
				styles.add(style);
				do_not_translate = true;
			}

		}

		/**
		 * Gets the color.
		 * @param color
		 *            the color
		 * @return the color
		 */
		private int getColor(String color) {
			for (int i = 0; i < colorList.size(); i++) {
				if (color.equals(colorList.get(i))) {
					return i + 1;
				}
			}
			colorList.add(color);
			return colorList.size();
		}

		/**
		 * Gets the next freestyle.
		 * @return the next freestyle
		 */
		private int getNextFreestyle() {
			int max = -1;
			for (int i = 0; i < styles.size(); i++) {
				String string = styles.get(i);
				StringTokenizer tk = new StringTokenizer(string, "\\", true); //$NON-NLS-1$
				while (tk.hasMoreTokens()) {
					String token = tk.nextToken();
					if (token.equals("\\")) { //$NON-NLS-1$
						if (!tk.hasMoreTokens()) {
							break;
						}
						token = token + tk.nextToken();
					}
					if (getControl(token).equals("s") //$NON-NLS-1$
							|| getControl(token).equals("cs") //$NON-NLS-1$
							|| getControl(token).equals("ds") //$NON-NLS-1$
							|| getControl(token).equals("ts")) { //$NON-NLS-1$

						int value = Integer.parseInt(getValue(token).trim());
						if (value > max) {
							max = value;
						}
					}
				}
			}
			return max + 1;
		}

		/**
		 * Gets the max font.
		 * @return the max font
		 */
		private int getMaxFont() {
			int max = -1;
			for (int i = 0; i < fonts.size(); i++) {
				String string = fonts.get(i);
				StringTokenizer tk = new StringTokenizer(string, "\\", true); //$NON-NLS-1$
				while (tk.hasMoreTokens()) {
					String token = tk.nextToken();
					if (token.equals("\\")) { //$NON-NLS-1$
						token = token + tk.nextToken();
					}
					if (getControl(token).equals("f")) { //$NON-NLS-1$
						int value = Integer.parseInt(getValue(token).trim());
						if (value > max) {
							max = value;
						}
					}
				}
			}
			return max + 1;
		}

		/**
		 * Checks if is lead byte.
		 * @param aByte
		 *            the a byte
		 * @return true, if is lead byte
		 */
		private boolean isLeadByte(byte aByte) {
			if (srcEncoding.equals(charsets.get("128"))) { //$NON-NLS-1$
				// Shift-JIS
				if ((aByte >= 0x81) && (aByte <= 0x9F)) {
					return true;
				}
				if ((aByte >= 0xE0) && (aByte <= 0xEE)) {
					return true;
				}
				if ((aByte >= 0xFA) && (aByte <= 0xFC)) {
					return true;
				}
			}
			if (srcEncoding.equals(charsets.get("134"))) { //$NON-NLS-1$
				// 936: Chinese Simplified (GB2312)
				if ((aByte >= 0xA1) && (aByte <= 0xA9)) {
					return true;
				}
				if ((aByte >= 0xB0) && (aByte <= 0xF7)) {
					return true;
				}
			}
			if (srcEncoding.equals(getEncoding("949"))) { //$NON-NLS-1$
				// 949: Korean
				if ((aByte >= 0x81) && (aByte <= 0xC8)) {
					return true;
				}
				if ((aByte >= 0xCA) && (aByte <= 0xFD)) {
					return true;
				}
			}
			if (srcEncoding.equals(charsets.get("136"))) { //$NON-NLS-1$
				// 950: Chinese Traditional (Big5)
				if ((aByte >= 0xA1) && (aByte <= 0xC6)) {
					return true;
				}
				if ((aByte >= 0xC9) && (aByte <= 0xF9)) {
					return true;
				}
			}
			// All other encoding: No lead bytes
			return false;
		}

		/**
		 * Parses the.
		 * @param group
		 *            the group
		 */
		private void parse(String group, IProgressMonitor monitor) {

			System.out.println("Initializing rules"); //$NON-NLS-1$
			initBreaks();

			segments = new Vector<String>();

			StringTokenizer tk = new StringTokenizer(group, "\\{}", true); //$NON-NLS-1$
			System.out.println("Processing " + tk.countTokens() + " tokens"); //$NON-NLS-1$ //$NON-NLS-2$
			String segment = ""; //$NON-NLS-1$
			String token = ""; //$NON-NLS-1$

			int tokenSize = tk.countTokens();
			monitor.beginTask(Messages.getString("rtf.Rtf2Xliff.task10"), tokenSize);
			monitor.subTask("");

			while (tk.hasMoreTokens()) {
				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("rtf.cancel"));
				}

				token = tk.nextToken();
				if (token.equals("\\")) { //$NON-NLS-1$
					token = token + tk.nextToken();
				}
				if (token.equals("{")) { //$NON-NLS-1$
					token = token + tk.nextToken();
				}
				if (token.equals("}")) { //$NON-NLS-1$
					skip = 0;
				}
				if (token.equals("{\\")) { //$NON-NLS-1$
					token = token + tk.nextToken();
				}
				if (token.equals("\\*") || token.equals("{\\*")) { //$NON-NLS-1$ //$NON-NLS-2$
					token = token + tk.nextToken() + tk.nextToken();
				}
				String ctrl = getControl(token);
				if (ctrl.equals("footnote")) { //$NON-NLS-1$
					// skip until the end of the footnote
					// ignore any breaking token
					segment = segment + token.substring(0, token.indexOf("\\footnote")); //$NON-NLS-1$
					token = token.substring(token.indexOf("\\footnote")); //$NON-NLS-1$
					int level = 1;
					boolean canBreak = false;
					do {
						if (token.equals("{")) { //$NON-NLS-1$
							level++;
						}
						if (token.equals("}")) { //$NON-NLS-1$
							level--;
						}
						if (level == 0) {
							canBreak = true;
						}
						segment = segment + token;
						token = tk.nextToken();
					} while (!canBreak); // == false);
					ctrl = getControl(token);
				}
				if (breaks.containsKey(ctrl)) {
					if (token.startsWith("{")) { //$NON-NLS-1$
						segment = segment + token.substring(0, token.indexOf("\\")); //$NON-NLS-1$
						token = token.substring(token.indexOf("\\")); //$NON-NLS-1$
					}
					segments.add(segment);
					segment = ""; //$NON-NLS-1$
				}
				segment = segment + token;
				monitor.worked(1);
			}
			monitor.done();

			segments.add(segment);
		}

		/**
		 * Inits the breaks.
		 */
		private void initBreaks() {
			breaks = new Hashtable<String, String>();
			breaks.put("par", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("pard", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("row", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("cell", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("nestcell", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("dobypara", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("sectd", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("header", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("footer", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("headerf", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("footerf", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("headerl", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("headerr", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("footerl", ""); //$NON-NLS-1$ //$NON-NLS-2$
			breaks.put("footerr", ""); //$NON-NLS-1$ //$NON-NLS-2$

			if (!taggedRTF) {
				breaks.put("line", ""); //$NON-NLS-1$ //$NON-NLS-2$
				// breaks.put("tab","");
			}

			// breaks.put("do","");
			// breaks.put("sp","");
		}

		/**
		 * Check segments.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws SAXException
		 *             the SAX exception
		 */
		private void checkSegments(IProgressMonitor monitor) throws IOException, SAXException {
			monitor.beginTask(Messages.getString("rtf.Rtf2Xliff.task11"), segments.size());
			monitor.subTask("");
			for (int i = 0; i < segments.size(); i++) {
				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("rtf.cancel"));
				}
				String segment = segments.get(i);
				String trimmed = segment.trim();
				if (inExternal && trimmed.startsWith("\\pard")) { //$NON-NLS-1$
					inExternal = false;
				}
				processSegment(segment);
				monitor.worked(1);
			}
			monitor.done();
		}

		/**
		 * Write skl.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeSkl(String string) throws IOException {
			string = replaceToken(string, "\uE008", "\\{"); //$NON-NLS-1$ //$NON-NLS-2$
			string = replaceToken(string, "\uE007", "\\}"); //$NON-NLS-1$ //$NON-NLS-2$
			string = replaceToken(string, "\uE011", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
			string = replaceToken(string, "\u0009", "\\tab "); //$NON-NLS-1$ //$NON-NLS-2$
			string = restoreControls(string);
			skeleton.write(string.getBytes("UTF-8")); //$NON-NLS-1$
		}

		/**
		 * Write skeleton.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeSkeleton(String string) throws IOException {
			string = replaceToken(string, "\uE008", "\\{"); //$NON-NLS-1$ //$NON-NLS-2$
			string = replaceToken(string, "\uE007", "\\}"); //$NON-NLS-1$ //$NON-NLS-2$
			string = replaceToken(string, "\uE011", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
			string = replaceToken(string, "\u0009", "\\tab "); //$NON-NLS-1$ //$NON-NLS-2$
			string = restoreControls(string);
			skeleton.write(clean(string).getBytes("UTF-8")); //$NON-NLS-1$
		}

		/**
		 * Restore controls.
		 * @param value
		 *            the value
		 * @return the string
		 */
		private String restoreControls(String value) {
			value = replaceToken(value, "" + '\u2002', "\\enspace "); //$NON-NLS-1$ //$NON-NLS-2$
			value = replaceToken(value, "" + '\u2003', "\\emspace "); //$NON-NLS-1$ //$NON-NLS-2$
			value = replaceToken(value, "" + '\u2005', "\\qmspace "); //$NON-NLS-1$ //$NON-NLS-2$
			value = replaceToken(value, "" + '\u2014', "\\emdash "); //$NON-NLS-1$ //$NON-NLS-2$
			value = replaceToken(value, "" + '\u2013', "\\endash "); //$NON-NLS-1$ //$NON-NLS-2$
			value = replaceToken(value, "" + '\u2018', "\\lquote "); //$NON-NLS-1$ //$NON-NLS-2$
			value = replaceToken(value, "" + '\u2019', "\\rquote "); //$NON-NLS-1$ //$NON-NLS-2$
			value = replaceToken(value, "" + '\u201C', "\\ldblquote "); //$NON-NLS-1$ //$NON-NLS-2$
			value = replaceToken(value, "" + '\u201D', "\\rdblquote "); //$NON-NLS-1$ //$NON-NLS-2$
			value = replaceToken(value, "" + '\u00A0', "\\~"); //$NON-NLS-1$ //$NON-NLS-2$
			value = replaceToken(value, "" + '\u2011', "\\_"); //$NON-NLS-1$ //$NON-NLS-2$
			value = replaceToken(value, "" + '\u00AD', "\\-"); //$NON-NLS-1$ //$NON-NLS-2$
			return value;
		}

		/**
		 * Process segment.
		 * @param fragment
		 *            the fragment
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws SAXException
		 *             the SAX exception
		 */
		private void processSegment(String fragment) throws IOException, SAXException {

			String string = XMLOutputter.validChars(TextUtil.cleanString(fragment));
			String segment = ""; //$NON-NLS-1$
			boolean inPh = false;
			StringTokenizer tk = new StringTokenizer(string, "\\{}", true); //$NON-NLS-1$
			while (tk.hasMoreElements()) {
				String token = tk.nextToken();
				if (token.equals("\\")) { //$NON-NLS-1$
					token = token + tk.nextToken();
				}
				if (token.equals("\\*")) { //$NON-NLS-1$
					token = token + tk.nextToken() + tk.nextToken();
				}
				if (!inPh && (token.startsWith("\\") || token.startsWith("{") || token.startsWith("}"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					segment = segment + "<ph>"; //$NON-NLS-1$
					inPh = true;
				}
				if (taggedRTF
						&& (inExternal || getControl(token).equals("cs") && ((getValue(token).equals(externalValue)) || (getValue(token).equals(noTranslateValue))))) { //$NON-NLS-1$
					// skip Trados external tags, until '}' is found
					if (inPh && !inExternal) {
						segment = segment + "</ph><ph>"; //$NON-NLS-1$
					}

					inExternal = true;
					segment = segment + token;
					String s = ""; //$NON-NLS-1$
					if (tk.hasMoreTokens()) {
						s = tk.nextToken();
					}
					while (!s.equals("}") && tk.hasMoreTokens()) { //$NON-NLS-1$
						String clean = clean(s);
						segment = segment + clean;
						s = tk.nextToken();
					}
					if (s.equals("}")) { //$NON-NLS-1$
						inExternal = false;
					}
					if (!tk.hasMoreTokens() && inExternal) {
						segment = segment + clean(s);
						s = ""; //$NON-NLS-1$
					}
					token = s;
					if (token.equals("\\")) { //$NON-NLS-1$
						token = token + tk.nextToken();
					}
					if (token.equals("\\*")) { //$NON-NLS-1$
						token = token + tk.nextToken() + tk.nextToken();
					}
				}
				if (taggedRTF && getControl(token).equals("cs") && getValue(token).equals(internalValue)) { //$NON-NLS-1$
					// skip Trados internal tags, until '}' is found
					if (inPh) {
						int idx = segment.lastIndexOf("{"); //$NON-NLS-1$
						int phStarts = segment.lastIndexOf("<ph>") + 4; //$NON-NLS-1$
						String tag = segment.substring(phStarts);
						if (idx != -1 && idx > phStarts && (tag.startsWith("\\par") //$NON-NLS-1$
								|| tag.startsWith("\\cell") //$NON-NLS-1$
								|| tag.startsWith("\\row") //$NON-NLS-1$
								|| tag.startsWith("\\nestcell") //$NON-NLS-1$
						|| tag.startsWith("\\dobypara") //$NON-NLS-1$
								)) {
							segment = segment.substring(0, idx) + "</ph><ph>" + segment.substring(idx); //$NON-NLS-1$
						}
					}
					segment = segment + token;
					String s = tk.nextToken();
					while (!s.equals("}") && tk.hasMoreTokens()) { //$NON-NLS-1$
						segment = segment + clean(s);
						s = tk.nextToken();
					}
					if (!tk.hasMoreTokens()) {
						segment = segment + clean(s);
						s = ""; //$NON-NLS-1$
					}
					token = s;
					if (token.equals("\\")) { //$NON-NLS-1$
						token = token + tk.nextToken();
					}
					if (token.equals("\\*")) { //$NON-NLS-1$
						token = token + tk.nextToken() + tk.nextToken();
					}
				}
				if (getControl(token).equals("field") //$NON-NLS-1$
						|| getControl(token).equals("listtext") //$NON-NLS-1$
						|| getControl(token).equals("do") //$NON-NLS-1$
						|| getControl(token).equals("sp")) { //$NON-NLS-1$
					// skip field definition, until '}' is found
					// check if there are fonts to preserve
					segment = segment + token;
					if (tk.hasMoreTokens()) {
						String s = tk.nextToken();
						int level = 1;
						if (s.equals("{")) { //$NON-NLS-1$
							level++;
						}
						if (s.equals("}")) { //$NON-NLS-1$
							level--;
						}
						while (!(s.equals("}") && level <= 0) && tk.hasMoreTokens()) { //$NON-NLS-1$
							s = replaceToken(s, "\uE008", "\\{"); //$NON-NLS-1$ //$NON-NLS-2$
							s = replaceToken(s, "\uE007", "\\}"); //$NON-NLS-1$ //$NON-NLS-2$
							s = replaceToken(s, "\uE011", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
							s = replaceToken(s, "\u0009", "\\tab "); //$NON-NLS-1$ //$NON-NLS-2$

							segment = segment + clean(s);
							s = tk.nextToken();
							if (s.equals("\\")) { //$NON-NLS-1$
								s = s + tk.nextToken();
							}
							if (s.equals("\\*")) { //$NON-NLS-1$
								s = s + tk.nextToken() + tk.nextToken();
							}
							if (s.equals("{")) { //$NON-NLS-1$
								level++;
							}
							if (s.equals("}")) { //$NON-NLS-1$
								level--;
							}
							s = replaceToken(s, "\uE011", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						token = s;
						if (!(token.startsWith("\\") || token.startsWith("}") || token.startsWith("{")) && segment.endsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							token = "\\" + s; //$NON-NLS-1$
							segment = segment.substring(0, segment.length() - 1);
						}
						if (token.equals("\\")) { //$NON-NLS-1$
							token = token + tk.nextToken();
						}
						if (token.equals("\\*")) { //$NON-NLS-1$
							token = token + tk.nextToken() + tk.nextToken();
						}
					}
				}
				if (getControl(token).equals("pict")) { //$NON-NLS-1$
					// skip the picture data
					int level = 1;
					while (level > 0) {
						segment = segment + token;
						token = tk.nextToken();
						if (token.equals("{")) { //$NON-NLS-1$
							level++;
						}
						if (token.equals("}")) { //$NON-NLS-1$
							level--;
						}
					}
					if (token.equals("\\")) { //$NON-NLS-1$
						token = token + tk.nextToken();
					}
					if (token.equals("\\*")) { //$NON-NLS-1$
						token = token + tk.nextToken() + tk.nextToken();
					}
				}
				if (getControl(token).equals("datafield") //$NON-NLS-1$
						|| getControl(token).equals("bkmkstart") //$NON-NLS-1$
						|| getControl(token).equals("bkmkend")) { //$NON-NLS-1$

					// skip field data, until '}' is found
					segment = segment + token;
					String s = tk.nextToken();
					while (!s.equals("}") && tk.hasMoreTokens()) { //$NON-NLS-1$
						segment = segment + clean(s);
						s = tk.nextToken();
					}
					token = s;
					if (token.equals("\\")) { //$NON-NLS-1$
						token = token + tk.nextToken();
					}
					if (token.equals("\\*")) { //$NON-NLS-1$
						token = token + tk.nextToken() + tk.nextToken();
					}
				}
				if (token.startsWith("\\")) { //$NON-NLS-1$
					String text = removeCtrl(token);
					String ctl = getControl(token);
					if (ctl.equals("pntxta") //$NON-NLS-1$
							|| ctl.equals("pntxtb") //$NON-NLS-1$
							|| ctl.equals("sv") //$NON-NLS-1$
							|| ctl.equals("sn")) { //$NON-NLS-1$

						// this control needs a parameter
						if (text.matches("^\\s[a-zA-Z0-9\\s].*")) { //$NON-NLS-1$
							// has a space in front of the parameter
							text = text.substring(2);
						} else {
							text = text.substring(1);
						}
					}
					if (text.trim().equals("")) { //$NON-NLS-1$
						segment = segment + token;
					} else {
						segment = segment + token.substring(0, token.indexOf(text)) + "</ph>" + text; //$NON-NLS-1$
						inPh = false;
					}
				} else if (token.startsWith("{") || token.startsWith("}")) { //$NON-NLS-1$ //$NON-NLS-2$
					String text = token.substring(1);
					if (text.trim().equals("")) { //$NON-NLS-1$
						segment = segment + token;
					} else {
						segment = segment + token.charAt(0) + "</ph>" + text; //$NON-NLS-1$
						inPh = false;
					}
				} else {
					if (inPh) {
						segment = segment + "</ph>"; //$NON-NLS-1$
						inPh = false;
					}
					segment = segment + token;
				}
			}
			if (inPh) {
				segment = segment + "</ph>"; //$NON-NLS-1$
			}

			if (containsText(segment)) {
				writeSegment(segment);
			} else {
				writeSkl(Xliff2Rtf.cleanChars(fragment));
			}
		}

		/**
		 * Clean.
		 * @param source
		 *            the source
		 * @return the string
		 */
		private String clean(String source) {
			String clean = ""; //$NON-NLS-1$
			int from = source.indexOf("blipuid"); //$NON-NLS-1$
			if (from != -1) {
				StringTokenizer tk = new StringTokenizer(source, "\\{}", true); //$NON-NLS-1$
				while (tk.hasMoreTokens()) {
					String token = tk.nextToken();
					if (token.length() < 1000) {
						clean = clean + Xliff2Rtf.clean(token);
					} else {
						clean = clean + token;
					}
				}
			} else {
				return Xliff2Rtf.clean(source);
			}

			return clean;
		}

		/**
		 * Contains text.
		 * @param segment
		 *            the segment
		 * @return true, if successful
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private boolean containsText(String segment) throws SAXException, IOException {
			SAXBuilder builder = new SAXBuilder();
			String text = "<?xml version=\"1.0\"?>\n<segment>" + segment //$NON-NLS-1$
					+ "</segment>"; //$NON-NLS-1$
			ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes("UTF-8")); //$NON-NLS-1$
			Document doc = builder.build(stream);
			List<Node> rootContent = doc.getRootElement().getContent();
			Iterator<Node> it = rootContent.iterator();
			while (it.hasNext()) {
				Node n = it.next();
				if (n.getNodeType() == Node.TEXT_NODE) {
					String s = n.getNodeValue();
					if (taggedRTF) {
						s = removeTradosTag(s);
					}
					if (!s.trim().equals("")) { //$NON-NLS-1$
						s = s.trim();
						for (int i = 0; i < s.length(); i++) {
							char c = s.charAt(i);
							if (c != '.' && c != ',' && c != '(' && c != ')' && c != '-'
									&& c != '\u2022' && c != '\u00B7' && c != '\u2219' && c != '\u25D8'
									&& c != '\u25E6') {
								return true;
							}
						}
					}
				}
			}
			return false;
		}

		/**
		 * Removes the trados tag.
		 * @param s
		 *            the s
		 * @return the string
		 */
		private String removeTradosTag(String s) {
			s = replaceToken(s, '\uE008' + "0>", ""); //$NON-NLS-1$ //$NON-NLS-2$
			s = replaceToken(s, "<0" + '\uE007', ""); //$NON-NLS-1$ //$NON-NLS-2$
			int from = s.indexOf("<" + '\uE007'); //$NON-NLS-1$
			int to = s.indexOf('\uE008' + ">"); //$NON-NLS-1$
			if (from != -1 && to != -1) {
				s = s.substring(0, from) + s.substring(to + 2);
			}
			return s;
		}

		/**
		 * Removes the ctrl.
		 * @param token
		 *            the token
		 * @return the string
		 */
		private String removeCtrl(String token) {
			int i = 1;
			// skip ctrl word
			for (; i < token.length(); i++) {
				char c = token.charAt(i);
				if (c == '\\' || c == '*' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
					continue;
				}
				break;
			}
			// skip numeric argument
			for (; i < token.length(); i++) {
				char c = token.charAt(i);
				if (c == '-' || (c >= '0' && c <= '9')) {
					continue;
				}
				break;
			}
			StringBuffer buffer = new StringBuffer();
			for (; i < token.length(); i++) {
				char c = token.charAt(i);
				buffer.append(c);
			}
			String result = buffer.toString();
			if (result.length() == 0) {
				return ""; //$NON-NLS-1$
			}
			if (Character.isSpaceChar(result.charAt(0))) {
				return result.substring(1);
			}
			return result;
		}

		/**
		 * Write segment.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws SAXException
		 *             the SAX exception
		 */
		private void writeSegment(String string) throws IOException, SAXException {
			if (segByElement) {
				if (taggedRTF) {
					if (find(string, "\uE008" + "0&gt;", 0) != -1) { //$NON-NLS-1$ //$NON-NLS-2$
						// segmented by trados
						String[] segs = splitTagged(string);
						for (int i = 0; i < segs.length; i++) {
							if (containsText(segs[i])) {
								if (find(segs[i], "\uE008" + "0&gt;", 0) != -1) { //$NON-NLS-1$ //$NON-NLS-2$
									writeStr(tag(segs[i]));
									writeSkeleton("%%%" + segId++ + "%%%\n"); //$NON-NLS-1$ //$NON-NLS-2$
								} else {
									tidy(segs[i]);
									writeSkeleton(Xliff2Rtf.cleanChars(start));
									writeStr(tag(meat));
									writeSkeleton("%%%" + segId++ + "%%%\n"); //$NON-NLS-1$ //$NON-NLS-2$
									writeSkeleton(Xliff2Rtf.cleanChars(end));
								}
							} else {
								writeSkeleton(Xliff2Rtf.cleanChars(unTag(segs[i])));
							}
						}
					} else {
						// not presegmented, segment by sentence
						String[] segs = segmenter.segment(string);
						for (int i = 0; i < segs.length; i++) {
							if (containsText(segs[i])) {
								tidy(segs[i]);
								writeSkeleton(Xliff2Rtf.cleanChars(start));
								writeStr(tag(meat));
								writeSkeleton("%%%" + segId++ + "%%%\n"); //$NON-NLS-1$ //$NON-NLS-2$
								writeSkeleton(Xliff2Rtf.cleanChars(end));
							} else {
								writeSkeleton(unTag(Xliff2Rtf.cleanChars(segs[i])));
							}
						}
					}
				} else {
					// segment by paragraph
					tidy(string);
					writeSkeleton(Xliff2Rtf.cleanChars(start));
					writeStr(tag(meat));
					writeSkeleton("%%%" + segId++ + "%%%\n"); //$NON-NLS-1$ //$NON-NLS-2$
					writeSkeleton(Xliff2Rtf.cleanChars(end));
				}
			} else {
				// segment by sentence
				String[] segs = segmenter.segment(string);
				for (int i = 0; i < segs.length; i++) {
					if (containsText(segs[i])) {
						tidy(segs[i]);
						writeSkeleton(Xliff2Rtf.cleanChars(start));
						writeStr(tag(meat));
						writeSkeleton("%%%" + segId++ + "%%%\n"); //$NON-NLS-1$ //$NON-NLS-2$
						writeSkeleton(Xliff2Rtf.cleanChars(end));
					} else {
						writeSkeleton(unTag(Xliff2Rtf.cleanChars(segs[i])));
					}
				}
			}
		}

		/**
		 * Find.
		 * @param text
		 *            the text
		 * @param token
		 *            the token
		 * @param from
		 *            the from
		 * @return the int
		 */
		public int find(String text, String token, int from) {
			int length = text.length();
			for (int i = from; i < length; i++) {
				String remaining = text.substring(i);
				if (remaining.startsWith("<ph")) { //$NON-NLS-1$
					int ends = remaining.indexOf("</ph>"); //$NON-NLS-1$
					if (ends != -1) {
						remaining = remaining.substring(ends + 5);
						i = i + ends + 5;
					}
				}
				String trimmed = removePh(remaining);
				if (trimmed.startsWith(token)) {
					return i;
				}
			}
			return -1;
		}

		/**
		 * Removes the ph.
		 * @param string
		 *            the string
		 * @return the string
		 */
		private String removePh(String string) {
			String result = ""; //$NON-NLS-1$
			int starts = string.indexOf("<ph"); //$NON-NLS-1$
			while (starts != -1) {
				result = result + string.substring(0, starts);
				string = string.substring(starts);
				int ends = string.indexOf("</ph>"); //$NON-NLS-1$
				if (ends != -1) {
					string = string.substring(ends + 5);
				}
				starts = string.indexOf("<ph"); //$NON-NLS-1$
			}
			return result + string;
		}

		/**
		 * Tidy.
		 * @param string
		 *            the string
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void tidy(String string) throws SAXException, IOException {
			if (!taggedRTF) {
				string = string.replaceAll("</ph><ph>", ""); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				string = replaceToken(string, "<ph>}</ph><ph>", "<ph>}"); //$NON-NLS-1$ //$NON-NLS-2$
				string = replaceToken(string, "</ph><ph>{</ph>", "{</ph>"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			start = ""; //$NON-NLS-1$
			end = ""; //$NON-NLS-1$
			meat = string;

			SAXBuilder builder = new SAXBuilder();
			String text = "<?xml version=\"1.0\"?>\n<segment>" + string //$NON-NLS-1$
					+ "</segment>"; //$NON-NLS-1$
			ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes("UTF-8")); //$NON-NLS-1$
			Document doc = builder.build(stream);
			Element root = doc.getRootElement();
			List<Node> rootContent = root.getContent();
			Iterator<Node> it = rootContent.iterator();
			int tags = 0;
			while (it.hasNext()) {
				Node n = it.next();
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					tags++;
				}
			}
			if (tags == 0) {
				return;
			}
			if (tags == 1) {
				Node n = rootContent.get(0);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					// starts with a tag
					Element e = new Element(n);
					if (!(taggedRTF && e.toString().indexOf(winInternal) != -1)) {
						start = e.getText();
						rootContent.remove(n);
						root.setContent(rootContent);
						meat = root.toString().substring(9, root.toString().length() - 10);
					}
					return;
				}
				n = rootContent.get(rootContent.size() - 1);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					// ends with a tag
					Element e = new Element(n);
					if (!(taggedRTF && e.toString().indexOf(winInternal) != -1)) {
						end = e.getText();
						rootContent.remove(n);
						root.setContent(rootContent);
						meat = root.toString().substring(9, root.toString().length() - 10);
					}
					return;
				}
			}
			Node n1 = rootContent.get(0);
			Node n2 = rootContent.get(rootContent.size() - 1);
			if (tags == 2 && n1.getNodeType() == Node.ELEMENT_NODE && n2.getNodeType() == Node.ELEMENT_NODE) {
				// starts and ends with a tag, no tags in the middle
				Element e1 = new Element(n1);
				start = e1.getText();
				Element e2 = new Element(n2);
				end = e2.getText();
				rootContent.remove(n1);
				rootContent.remove(n2);
				root.setContent(rootContent);
				meat = root.toString().substring(9, root.toString().length() - 10);
				return;
			}
			if (n1.getNodeType() == Node.ELEMENT_NODE) {
				// remove initial tag
				Element e1 = new Element(n1);
				if (!(taggedRTF && e1.toString().indexOf(winInternal) != -1)) {
					start = e1.getText();
					rootContent.remove(n1);
					root.setContent(rootContent);
					meat = root.toString().substring(9, root.toString().length() - 10);
				}
			}
		}

		/**
		 * Tag.
		 * @param string
		 *            the string
		 * @return the string
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String tag(String string) throws SAXException, IOException {
			if (!taggedRTF && string.indexOf("</ph><ph>") != -1) { //$NON-NLS-1$
				string = string.replaceAll("</ph><ph>", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			SAXBuilder builder = new SAXBuilder();
			String text = "<?xml version=\"1.0\"?>\n<source xml:lang=\"" + sourceLanguage + "\">" + string //$NON-NLS-1$ //$NON-NLS-2$
					+ "</source>"; //$NON-NLS-1$
			ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes("UTF-8")); //$NON-NLS-1$
			Document doc = builder.build(stream);
			Element root = doc.getRootElement();
			List<Node> rootContent = root.getContent();
			int k = 0;
			Iterator<Node> it = rootContent.iterator();
			Vector<Node> v = new Vector<Node>();
			while (it.hasNext()) {
				Node n = it.next();
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element e = new Element(n);
					e.setAttribute("id", "" + k++); //$NON-NLS-1$ //$NON-NLS-2$
					v.add(n);
				} else {
					v.add(n);
				}
			}
			root.setContent(v);
			return "   <trans-unit id=\"" //$NON-NLS-1$
					+ segId + "\" xml:space=\"preserve\" approved=\"no\">\n      " + root.toString() //$NON-NLS-1$
					+ "\n   </trans-unit>\n"; //$NON-NLS-1$
		}

		/**
		 * Split tagged.
		 * @param text
		 *            the text
		 * @return the string[]
		 */
		private String[] splitTagged(String text) {
			Vector<String> strings = new Vector<String>();

			int index = find(text, "\uE008" + "0&gt;", 1); //$NON-NLS-1$ //$NON-NLS-2$
			while (index != -1) {
				String candidate = text.substring(0, index);
				int ends = find(candidate, "&lt;0" + "\uE007", 0); //$NON-NLS-1$ //$NON-NLS-2$
				while (ends != -1) {
					if (!candidate.substring(0, ends + 6).endsWith("0" + "\uE007")) { //$NON-NLS-1$ //$NON-NLS-2$
						ends += candidate.substring(ends).indexOf("0" + "\uE007") + 2; //$NON-NLS-1$ //$NON-NLS-2$
						strings.add(candidate.substring(0, ends));
						candidate = candidate.substring(ends);
					} else {
						strings.add(candidate.substring(0, ends + 6));
						candidate = candidate.substring(ends + 6);
					}
					ends = find(candidate, "&lt;0" + "\uE007", 1); //$NON-NLS-1$ //$NON-NLS-2$
				}
				strings.add(candidate);
				text = text.substring(index);
				index = find(text, "\uE008" + "0&gt;", 1); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!text.endsWith("0" + "\uE007")) { //$NON-NLS-1$ //$NON-NLS-2$
				index = text.lastIndexOf("0" + "\uE007"); //$NON-NLS-1$ //$NON-NLS-2$
				if (index != -1) {
					strings.add(text.substring(0, index + 2));
					text = text.substring(index + 2);
				}
			}
			strings.add(text);

			String[] result = new String[strings.size()];
			for (int i = 0; i < strings.size(); i++) {
				result[i] = strings.get(i);
			}
			return result;
		}

		/**
		 * Un tag.
		 * @param string
		 *            the string
		 * @return the string
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String unTag(String string) throws SAXException, IOException {
			SAXBuilder builder = new SAXBuilder();
			String text = "<?xml version=\"1.0\"?>\n<tag>" + string + "</tag>"; //$NON-NLS-1$ //$NON-NLS-2$
			ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes("UTF-8")); //$NON-NLS-1$
			Document doc = builder.build(stream);
			Element root = doc.getRootElement();
			List<Node> rootContent = root.getContent();
			Iterator<Node> it = rootContent.iterator();
			String result = ""; //$NON-NLS-1$
			while (it.hasNext()) {
				Node n = it.next();
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element e = new Element(n);
					result = result + e.getText();
				} else if (n.getNodeType() == Node.TEXT_NODE) {
					result = result + n.getNodeValue();
				}
			}
			return result;
		}

		/**
		 * Gets the style.
		 * @param style
		 *            the style
		 * @return the style
		 */
		private String getStyle(String style) {
			StringTokenizer tk = new StringTokenizer(style, "\\", true); //$NON-NLS-1$
			String token = tk.nextToken();
			if (token.equals("{")) { //$NON-NLS-1$
				token = tk.nextToken();
			}
			if (token.equals("\\")) { //$NON-NLS-1$
				token = token + tk.nextToken();
			}
			if (token.equals("\\*")) { //$NON-NLS-1$
				// Skip the \\*
				token = tk.nextToken() + tk.nextToken();
			}
			return token.trim();
		}

		/**
		 * Write header.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeHeader() throws IOException {
			writeStr("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
			writeStr("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " + //$NON-NLS-1$
					"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xmlns:hs=\"" + Converter.HSNAMESPACE
					+ "\" " + //$NON-NLS-1$ 
					"xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd " //$NON-NLS-1$
					+ Converter.HSSCHEMALOCATION + "\">\n"); //$NON-NLS-1$
			if (!"".equals(targetLanguage)) {
				writeStr("<file original=\"" //$NON-NLS-1$
						+ TextUtil.cleanString(inputFile)
						+ "\" source-language=\"" //$NON-NLS-1$
						+ sourceLanguage
						+ "\" target-language=\"" + targetLanguage + "\" datatype=\"" + dataType + "\">\n"); //$NON-NLS-1$
			} else {
				writeStr("<file original=\"" //$NON-NLS-1$
						+ TextUtil.cleanString(inputFile) + "\" source-language=\"" //$NON-NLS-1$
						+ sourceLanguage + "\" datatype=\"" + dataType + "\">\n"); //$NON-NLS-1$
			}
			writeStr("<header>\n"); //$NON-NLS-1$
			writeStr("   <skl>\n"); //$NON-NLS-1$
			String crc = ""; //$NON-NLS-1$
			if (isSuite) {
				crc = "crc=\"" + CRC16.crc16(TextUtil.cleanString(skeletonFile).getBytes("UTF-8")) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			writeStr("      <external-file href=\"" + TextUtil.cleanString(skeletonFile) + "\" " + crc + "/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			writeStr("   </skl>\n"); //$NON-NLS-1$
			writeStr("<tool tool-id=\"" + qtToolID + "\" tool-name=\"HSStudio\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			writeStr("   <hs:prop-group name=\"encoding\">\n   <hs:prop prop-type=\"encoding\" >" //$NON-NLS-1$
					+ srcEncoding + "</hs:prop>\n</hs:prop-group>\n"); //$NON-NLS-1$
			if (taggedRTF) {
				writeStr("<hs:prop-group name=\"tags\">\n" //$NON-NLS-1$
						+ "   <hs:prop prop-type=\"Internal\" >" //$NON-NLS-1$
						+ winInternal + "</hs:prop>\n" //$NON-NLS-1$
						+ "   <hs:prop prop-type=\"External\" >" //$NON-NLS-1$
						+ winExternal + "</hs:prop>\n" //$NON-NLS-1$
						+ "   <hs:prop prop-type=\"Mark\" >" //$NON-NLS-1$
						+ winMark + "</hs:prop>\n" //$NON-NLS-1$
						+ "   <hs:prop prop-type=\"style\" >" //$NON-NLS-1$
						+ markStyle + "</hs:prop>\n" //$NON-NLS-1$
						+ "</hs:prop-group>\n"); //$NON-NLS-1$
			}
			writeStr("</header>\n"); //$NON-NLS-1$
			writeStr("<body>\n"); //$NON-NLS-1$
		}

		/**
		 * Gets the value.
		 * @param control
		 *            the control
		 * @param style
		 *            the style
		 * @return the value
		 */
		private String getValue(String control, String style) {
			StringTokenizer tk = new StringTokenizer(style, "\\", true); //$NON-NLS-1$
			while (tk.hasMoreTokens()) {
				String token = tk.nextToken();
				if (token.equals("\\")) { //$NON-NLS-1$
					token = token + tk.nextToken();
				}
				if (token.equals("\\*")) { //$NON-NLS-1$
					token = token + tk.nextToken() + tk.nextToken();
				}
				if (getControl(token).equals(control)) {
					return getValue(token);
				}
			}
			return "1"; //$NON-NLS-1$
		}
	}
}
