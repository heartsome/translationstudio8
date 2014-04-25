/**
 * Xliff2Mif.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.mif;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.mif.resource.Messages;
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
 * The Class Xliff2Mif.
 * @author John Zhu
 */
public class Xliff2Mif implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "mif";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("mif.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to MIF Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2MifImpl converter = new Xliff2MifImpl();
		String iniDir = args.get(Converter.ATTR_INIDIR);
		String iniFile = iniDir + System.getProperty("file.separator") + "init_mif.xml";
		args.put(Converter.ATTR_INI_FILE, iniFile);
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
	 * The Class Xliff2MifImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2MifImpl {

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

		// /** The charmap. */
		// private Hashtable<String, String> charmap;

		/** The catalogue. */
		private String catalogue;

		/** The ini file. */
		private String iniFile;

		// 计算替换进度的对象
		private CalculateProcessedBytes cpb;

		// 替换过程的进度监视器
		private IProgressMonitor replaceMonitor;

		// skeleton 文件编码
		private String encoding;

		/**
		 * This method receives a Hashtable that must contain the following data: xliff: String with the name of the
		 * XLIFF file to convert to MIF backfile: String with the name of the MIF file to generate skeleton: String with
		 * the name of the skeleton to use at reverse conversion tgtLanguage: String with the language code of the MIF
		 * file.
		 * @param monitor
		 *            the monitor
		 * @param params
		 *            the params
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
			iniFile = params.get(Converter.ATTR_INI_FILE);
			String outputFile = params.get(Converter.ATTR_TARGET_FILE);
			encoding = params.get(Converter.ATTR_SOURCE_ENCODING);

			// String attrIsPreviewMode = params.get(Converter.ATTR_IS_PREVIEW_MODE);
			/* 是否为预览翻译模式 */
			// boolean isPreviewMode = attrIsPreviewMode != null && attrIsPreviewMode.equals(Converter.TRUE);

			try {
				infoLogger.logConversionFileInfo(catalogue, iniFile, xliffFile, sklFile);

				// 把转换过程任务分为三大部分 10 任务，其中加载 xliff 文件占 3，加载 ini 文件占 3，替换过程占 4。
				monitor.beginTask(Messages.getString("mif.Xliff2Mif.task1"), 10);
				monitor.subTask(Messages.getString("mif.Xliff2Mif.task2"));
				infoLogger.startLoadingXliffFile();
				output = new FileOutputStream(outputFile);
				loadSegments();
				infoLogger.endLoadingXliffFile();
				monitor.worked(3);
				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}

				monitor.subTask(Messages.getString("mif.Xliff2Mif.task3"));
				infoLogger.startLoadingIniFile();
				// loadCharMap();
				infoLogger.endLoadingIniFile();
				monitor.worked(3);
				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}

				try {
					infoLogger.startReplacingSegmentSymbol();
					cpb = ConverterUtils.getCalculateProcessedBytes(sklFile);
					replaceMonitor = Progress.getSubMonitor(monitor, 4);
					replaceMonitor.beginTask(Messages.getString("mif.Xliff2Mif.task4"), cpb.getTotalTask());
					input = new InputStreamReader(new FileInputStream(sklFile), UTF_8); //$NON-NLS-1$
					buffer = new BufferedReader(input);
					line = buffer.readLine();
					Pattern pattern = Pattern.compile("(?<=%%%)[0-9]+(?=%%%)");
					while (line != null) {
						line = line + "\n";
						Matcher mat = pattern.matcher(line);						
						if (mat.find()) {
							do {
								String code = line.substring(mat.start(), mat.end());
								Element segment = segments.get(code);
								if (segment != null) {
									Element target = segment.getChild("target");
									Element source = segment.getChild("source");
									if (line.toLowerCase().indexOf("mtext") != -1) {
										if (target != null) {
											String tgtStr = commonProcess(target);
											if (!"".equals(tgtStr.trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												// process target
												line = line.replace("%%%" + code + "%%%", tgtStr);
												writeString(line, true, code);
											} else {
												// process source
												line = line.replace("%%%" + code + "%%%", commonProcess(source));
												writeString(line, true, code);
											}
										} else {
											// process source
											line = line.replace("%%%" + code + "%%%", commonProcess(source));
											writeString(line, true, code);
										}
									} else {
										if (target != null) {
											String tgtStr = process(target, code);
											if (!"".equals(tgtStr.trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												// process target
												line = line.replace("%%%" + code + "%%%", tgtStr);
												writeString(line, true, code);
											} else {
												// process source
												line = line.replace("%%%" + code + "%%%", process(source, code));
												writeString(line, true, code);
											}
										} else {
											// process source
											line = line.replace("%%%" + code + "%%%", process(source, code));
											writeString(line, true, code);
										}
									}
								} else {
									ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
											MessageFormat.format(Messages.getString("mif.Xliff2Mif.msg1"), code));
								}
							} while (mat.find());
						} else {							
							// non translatable portion							
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
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils
						.throwConverterException(Activator.PLUGIN_ID, Messages.getString("mif.Xliff2Mif.msg2"), e);
			} finally {
				monitor.done();
			}
			return result;
		}

		/**
		 * Convert the escape character '\>' '\q' '\Q' '\\'
		 * @param c
		 * @return ;
		 */
		private String cleanString(String s) {
			int control = s.indexOf("\\");
			while (control != -1) {
				int t = control + 1;
				if (t < s.length() - 1 && s.charAt(t) != '>' && s.charAt(t) != '\\' && s.charAt(t) != 'x') {
					s = s.substring(0, control) + "\\\\" + s.substring(control + 1);
				}
				if (control < s.length()) {
					control++;
				}
				control = s.indexOf("\\", control + 1);
			}
			s = s.replace("`", "\\Q");
			s = s.replace("'", "\\q");
			s = s.replace("\u0009", "\\t");
			return s;
		}

		/*
		 * /** Load char map.
		 * 
		 * @throws SAXException the SAX exception
		 * 
		 * @throws IOException Signals that an I/O exception has occurred.
		 *//*
			 * private void loadCharMap() throws SAXException, IOException { SAXBuilder cbuilder = new SAXBuilder();
			 * Document cdoc = cbuilder.build(iniFile); charmap = new Hashtable<String, String>(); Element croot =
			 * cdoc.getRootElement(); List<Element> codes = croot.getChildren("char"); //$NON-NLS-1$ Iterator<Element>
			 * it = codes.iterator(); while (it.hasNext()) { Element e = it.next(); charmap.put(e.getText(),
			 * e.getAttributeValue("code")); //$NON-NLS-1$ e = null; } it = null; codes = null; cdoc = null; cbuilder =
			 * null; }
			 */

		/**
		 * Process.
		 * @param e
		 *            the e
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String process(Element e, String segmentSymbolCode) throws IOException {
			String result = ""; //$NON-NLS-1$
			List<Node> content = e.getContent();
			Iterator<Node> i = content.iterator();
			while (i.hasNext()) {
				Node n = i.next();
				switch (n.getNodeType()) {
				case Node.TEXT_NODE:
					result += "<String `" + cleanString(n.getNodeValue()) + "'>"; //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case Node.ELEMENT_NODE:
					Element el = new Element(n);
					String r = "";
					if (el.getName().equals("ph")) { //$NON-NLS-1$
						String c = el.getText();
						r = c;
						int sos = c.indexOf("%%%");
						while (sos != -1) {
							sos += 3;
							int eos = c.indexOf("%%%", sos);
							String code = c.substring(sos, eos);
							eos += 3;
							Element segment = segments.get(code);
							if (segment != null) {
								Element target = segment.getChild("target");
								Element source = segment.getChild("source");
								String t = "";
								if (target != null) {
									t = commonProcess(target);
									if (t.length() == 0) {
										t = commonProcess(source);
									}
								} else {
									t = commonProcess(source);
								}
								r = r.replace("%%%" + code + "%%%", t);
							}
							sos = c.indexOf("%%%", eos);
						}
						result += r; //$NON-NLS-1$
					}
					break;
				default:
					break;
				}
			}
			i = null;
			content = null;
			return result;
		}

		private String commonProcess(Element e) throws IOException {
			String result = ""; //$NON-NLS-1$
			List<Node> content = e.getContent();
			Iterator<Node> i = content.iterator();
			while (i.hasNext()) {
				Node n = i.next();
				switch (n.getNodeType()) {
				case Node.TEXT_NODE:
					result += cleanString(n.getNodeValue());
					break;
				case Node.ELEMENT_NODE:
					Element el = new Element(n);
					if (el.getName().equals("ph")) { //$NON-NLS-1$
						result += el.getText(); //$NON-NLS-1$
					}
					break;
				default:
					break;
				}
			}
			i = null;
			content = null;
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
				throw new OperationCanceledException(Messages.getString("preference.cancel"));
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
