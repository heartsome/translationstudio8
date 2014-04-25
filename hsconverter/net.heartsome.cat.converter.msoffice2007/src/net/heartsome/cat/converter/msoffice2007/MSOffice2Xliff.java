/**
 * MSOffice2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msoffice2007;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.msoffice2007.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;
import net.heartsome.xml.Attribute;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The Class MSOffice2Xliff.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class MSOffice2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-msoffice2007";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("msoffice2007.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "MS Office 2007 to XLIFF Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		MSOffice2XliffImpl converter = new MSOffice2XliffImpl();
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
	 * The Class MSOffice2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class MSOffice2XliffImpl {

		/** The qt tool id. */
		private String qtToolID = null;

		/** The src file. */
		private String srcFile;

		/** The skeleton. */
		private String skeleton;

		/** The merged. */
		private Document merged;

		/** The merged root. */
		private Element mergedRoot;

		/** The zip out. */
		private ZipOutputStream zipOut;

		/** The zip in. */
		private ZipInputStream zipIn;

		/** The input file. */
		private String inputFile;

		/** The xliff file. */
		private String xliffFile;

		/** The skeleton file. */
		private String skeletonFile;

		/** The source language. */
		private String sourceLanguage;

		/** The XLIFF file target language **/
		private String targetLanguage;

		/** The catalogue. */
		private String catalogue;

		/** The text. */
		private String text = ""; //$NON-NLS-1$

		/** The in body. */
		boolean inBody = false;

		/** The out. */
		private FileOutputStream out;

		/** The skel. */
		private FileOutputStream skel;

		/** The segnum. */
		private int segnum;

		/** The seg by element. */
		private boolean segByElement;

		/** The segmenter. */
		private StringSegmenter segmenter;

		/** The src encoding. */
		private String srcEncoding;

		/** The is suite. */
		private boolean isSuite;

		/** The srx. */
		private String srx;

		/**
		 * Convert content.
		 * @param params
		 *            the params
		 * @return the map< string, string>
		 * @throws ConverterException
		 *             the converter exception
		 */
		public Map<String, String> convertContent(Map<String, String> params) throws ConverterException {
			Map<String, String> result = new HashMap<String, String>();
			inputFile = params.get(Converter.ATTR_SOURCE_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
			segnum = 0;

			try {
				// if (segByElement == false) {
				if (!segByElement) {
					segmenter = new StringSegmenter(srx, sourceLanguage, catalogue);
				}
				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(inputFile);
				Element root = doc.getRootElement();
				out = new FileOutputStream(xliffFile);
				skel = new FileOutputStream(skeletonFile);
				writeHeader();
				recurse(root);
				writeOut("</body>\n</file>\n</xliff>"); //$NON-NLS-1$
				out.close();
				skel.close();
				result.put(Converter.ATTR_XLIFF_FILE, xliffFile);
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("msoffice2007.MSOffice2Xliff.msg1"),
						e);
			}
			return result;
		}

		/**
		 * Run.
		 * @param args
		 *            the args
		 * @param monitor
		 *            the monitor
		 * @return the map< string, string>
		 * @throws ConverterException
		 *             the converter exception
		 */
		public Map<String, String> run(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			Map<String, String> result = new HashMap<String, String>();

			srcFile = args.get(Converter.ATTR_SOURCE_FILE);
			String xliff = args.get(Converter.ATTR_XLIFF_FILE);
			skeleton = args.get(Converter.ATTR_SKELETON_FILE);
			isSuite = false;
			if (Converter.TRUE.equals(args.get(Converter.ATTR_IS_SUITE))) {
				isSuite = true;
			}
			qtToolID = args.get(Converter.ATTR_QT_TOOLID) != null ? args.get(Converter.ATTR_QT_TOOLID)
					: Converter.QT_TOOLID_DEFAULT_VALUE;

			sourceLanguage = args.get(Converter.ATTR_SOURCE_LANGUAGE);
			targetLanguage = args.get(Converter.ATTR_TARGET_LANGUAGE);
			catalogue = args.get(Converter.ATTR_CATALOGUE);
			String elementSegmentation = args.get(Converter.ATTR_SEG_BY_ELEMENT);
			srx = args.get(Converter.ATTR_SRX);
			srcEncoding = args.get(Converter.ATTR_SOURCE_ENCODING);
			if (elementSegmentation == null) {
				segByElement = false;
			} else {
				if (elementSegmentation.equals(Converter.TRUE)) {
					segByElement = true;
				} else {
					segByElement = false;
				}
			}

			try {
				// 把总任务分为压缩文件中的条目个数＋1;其中最后一个任务为 library3 写合并后的 xliff 文件。
				ZipFile zipFile = new ZipFile(srcFile);
				int size = zipFile.size();
				int totalTask = size + 1;
				monitor.beginTask(Messages.getString("msoffice2007.MSOffice2Xliff.task1"), totalTask);
				merged = new Document(null, "xliff", null, null); //$NON-NLS-1$
				mergedRoot = merged.getRootElement();
				mergedRoot.setAttribute("version", "1.2"); //$NON-NLS-1$ //$NON-NLS-2$
				mergedRoot.setAttribute("xmlns", "urn:oasis:names:tc:xliff:document:1.2"); //$NON-NLS-1$ //$NON-NLS-2$
				mergedRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
				mergedRoot.setAttribute("xmlns:hs", Converter.HSNAMESPACE); //$NON-NLS-1$
				mergedRoot
						.setAttribute(
								"xsi:schemaLocation", "urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd " + Converter.HSSCHEMALOCATION); //$NON-NLS-1$ //$NON-NLS-2$
				zipOut = new ZipOutputStream(new FileOutputStream(skeleton));
				zipIn = new ZipInputStream(new FileInputStream(srcFile));

				ZipEntry entry = null;
				while ((entry = zipIn.getNextEntry()) != null) {
					// 检查是否取消操作
					if (monitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("msoffice2007.cancel"));
					}
					String messagePattern = Messages.getString("msoffice2007.MSOffice2Xliff.task2");
					String message = MessageFormat.format(messagePattern, new Object[] { entry.getName() });
					monitor.setTaskName(message);

					if (entry.getName().matches(".*\\.[xX][mM][lL]")) { //$NON-NLS-1$
						File f = new File(entry.getName());
						String name = f.getName();
						String tmpFileName = name.substring(0, name.lastIndexOf(".")); //$NON-NLS-1$
						if (tmpFileName.length() < 3) {
							tmpFileName += "_tmp"; //$NON-NLS-1$
						}
						File tmp = File.createTempFile(tmpFileName, ".xml"); //$NON-NLS-1$ 
						FileOutputStream output = new FileOutputStream(tmp.getAbsolutePath());
						byte[] buf = new byte[1024];
						int len;
						while ((len = zipIn.read(buf)) > 0) {
							output.write(buf, 0, len);
						}
						output.close();
						try {
							Hashtable<String, String> table = new Hashtable<String, String>();
							table.put(Converter.ATTR_SOURCE_FILE, tmp.getAbsolutePath());
							table.put(Converter.ATTR_XLIFF_FILE, tmp.getAbsolutePath() + ".xlf"); //$NON-NLS-1$
							table.put(Converter.ATTR_SKELETON_FILE, tmp.getAbsolutePath() + ".skl"); //$NON-NLS-1$
							boolean hasError = false;
							Map<String, String> res = null;
							try {
								res = convertContent(table);
							} catch (ConverterException ce) {
								hasError = true;
							}

							if (res == null || res.get(Converter.ATTR_XLIFF_FILE) == null) {
								hasError = true;
							}
							if (!hasError) {
								if (countSegments(tmp.getAbsolutePath() + ".xlf") > 0) { //$NON-NLS-1$
									updateXliff(tmp.getAbsolutePath() + ".xlf", entry.getName()); //$NON-NLS-1$
									addFile(tmp.getAbsolutePath() + ".xlf"); //$NON-NLS-1$
									ZipEntry content = new ZipEntry(entry.getName() + ".skl"); //$NON-NLS-1$
									content.setMethod(ZipEntry.DEFLATED);
									zipOut.putNextEntry(content);
									FileInputStream input = new FileInputStream(tmp.getAbsolutePath() + ".skl"); //$NON-NLS-1$
									byte[] array = new byte[1024];
									while ((len = input.read(array)) > 0) {
										zipOut.write(array, 0, len);
									}
									zipOut.closeEntry();
									input.close();
								} else {
									saveEntry(entry, tmp.getAbsolutePath());
								}
								File skl = new File(tmp.getAbsolutePath() + ".skl"); //$NON-NLS-1$
								skl.delete();
								File xlf = new File(tmp.getAbsolutePath() + ".xlf"); //$NON-NLS-1$
								xlf.delete();
							} else {
								saveEntry(entry, tmp.getAbsolutePath());
							}
						} catch (Exception e) {
							if (Converter.DEBUG_MODE) {
								e.printStackTrace();
							}

							// do nothing
							saveEntry(entry, tmp.getAbsolutePath());
						}
						tmp.delete();
					} else {
						// not an XML file
						File tmp = File.createTempFile("zip", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
						FileOutputStream output = new FileOutputStream(tmp.getAbsolutePath());
						byte[] buf = new byte[1024];
						int len;
						while ((len = zipIn.read(buf)) > 0) {
							output.write(buf, 0, len);
						}
						output.close();
						saveEntry(entry, tmp.getAbsolutePath());
						tmp.delete();
					}
					monitor.worked(1);
				}

				zipOut.close();

				// output final XLIFF

				// fixed a bug 572 by john. keep a well-format of xliff. add a
				// empty
				// file into xliff if root have not file element.

				List<Element> files = mergedRoot.getChildren("file"); //$NON-NLS-1$
				if (files.size() == 0) {
					Element file = new Element("file", merged); //$NON-NLS-1$
					file.setAttribute("original", srcFile); //$NON-NLS-1$
					file.setAttribute("source-language", sourceLanguage); //$NON-NLS-1$
					file.setAttribute("datatype", TYPE_VALUE); //$NON-NLS-1$
					Element header = new Element("header", merged); //$NON-NLS-1$
					Element body = new Element("body", merged); //$NON-NLS-1$
					file.addContent(header);
					file.addContent("\n"); //$NON-NLS-1$
					file.addContent(body);
					mergedRoot.addContent(file);
					header = null;
					body = null;
					file = null;
				}
				files = null;

				// 生成 xliff 文件
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("msoffice2007.cancel"));
				}
				monitor.subTask(Messages.getString("msoffice2007.MSOffice2Xliff.task3"));
				XMLOutputter outputter = new XMLOutputter();
				outputter.preserveSpace(true);
				FileOutputStream output = new FileOutputStream(xliff);
				outputter.output(merged, output);
				output.close();
				result.put(Converter.ATTR_XLIFF_FILE, xliff);
				monitor.worked(1);
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("msoffice2007.MSOffice2Xliff.msg2"),
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
			writeOut("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"); //$NON-NLS-1$
			writeOut("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " + //$NON-NLS-1$
					"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xmlns:hs=\"" + Converter.HSNAMESPACE
					+ "\" " + //$NON-NLS-1$ 
					"xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd " //$NON-NLS-1$
					+ Converter.HSSCHEMALOCATION + "\">\n"); //$NON-NLS-1$
			if (!targetLanguage.equals("")) {
				writeOut("<file datatype=\"x-office\" original=\"" + cleanString(inputFile) + "\" source-language=\"" + sourceLanguage + "\" target-language=\"" + targetLanguage + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}else {
				writeOut("<file datatype=\"x-office\" original=\"" + cleanString(inputFile) + "\" source-language=\"" + sourceLanguage + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			writeOut("<header>\n"); //$NON-NLS-1$
			writeOut("<skl>\n"); //$NON-NLS-1$
			if (isSuite) {
				writeOut("<external-file crc=\"" + CRC16.crc16(TextUtil.cleanString(skeleton).getBytes("UTF-8")) + "\" href=\"" + cleanString(skeletonFile) + "\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				writeOut("<external-file href=\"" + cleanString(skeletonFile) + "\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			writeOut("</skl>\n"); //$NON-NLS-1$
			writeOut("   <tool tool-id=\"" + qtToolID + "\" tool-name=\"HSStudio\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			writeOut("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\">" //$NON-NLS-1$
					+ srcEncoding + "</hs:prop></hs:prop-group>\n"); //$NON-NLS-1$
			writeOut("</header>\n<body>\n"); //$NON-NLS-1$
			writeOut("\n"); //$NON-NLS-1$
		}

		/**
		 * Save entry.
		 * @param entry
		 *            the entry
		 * @param name
		 *            the name
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void saveEntry(ZipEntry entry, String name) throws IOException {
			ZipEntry content = new ZipEntry(entry.getName());
			content.setMethod(ZipEntry.DEFLATED);
			zipOut.putNextEntry(content);
			FileInputStream input = new FileInputStream(name);
			byte[] array = new byte[1024];
			int len;
			while ((len = input.read(array)) > 0) {
				zipOut.write(array, 0, len);
			}
			zipOut.closeEntry();
			input.close();
		}

		/**
		 * Write segment.
		 * @param sourceText
		 *            the source text
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws SAXException
		 *             the SAX exception
		 */
		private void writeSegment(String sourceText) throws IOException, SAXException {
			String s = "<source xml:lang=\"" + sourceLanguage + "\">" + sourceText + "</source>"; //$NON-NLS-1$ //$NON-NLS-2$
			SAXBuilder b = new SAXBuilder();
			Document d = b.build(new ByteArrayInputStream(s.getBytes("UTF-8"))); //$NON-NLS-1$
			Element source = d.getRootElement();
			List<Node> content = source.getContent();
			String start = ""; //$NON-NLS-1$
			String end = ""; //$NON-NLS-1$
			List<Element> tags = source.getChildren("ph"); //$NON-NLS-1$
			if (tags.size() == 1) {
				if (content.get(0).getNodeType() == Node.ELEMENT_NODE) {
					Element e = tags.get(0);
					start = e.getText();
					content.remove(0);
					source.setContent(content);
				} else if (content.get(content.size() - 1).getNodeType() == Node.ELEMENT_NODE) {
					Element e = tags.get(0);
					end = e.getText();
					content.remove(content.size() - 1);
					source.setContent(content);
				}
			} else if (tags.size() > 1) {
				if (content.get(0).getNodeType() == Node.ELEMENT_NODE
						&& content.get(content.size() - 1).getNodeType() == Node.ELEMENT_NODE) {
					// check if it is possible to send
					// initial and trailing tag to skeleton
					Element first = new Element(content.get(0));
					Element last = new Element(content.get(content.size() - 1));
					String test = first.getText() + last.getText();
					if (checkPairs(test)) {
						start = first.getText();
						end = last.getText();
						content.remove(content.size() - 1);
						content.remove(0);
						source.setContent(content);
					}
				}
			}

			writeSkel(start);
			if (containsText(source)) {
				List<Element> phs = source.getChildren("ph"); //$NON-NLS-1$
				for (int i = 0; i < phs.size(); i++) {
					phs.get(i).setAttribute("id", "" + (i + 1)); //$NON-NLS-1$ //$NON-NLS-2$
				}
				writeOut("<trans-unit id=\"" + segnum + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
				writeOut(source.toString());
				writeOut("\n</trans-unit>\n\n"); //$NON-NLS-1$
				writeSkel("%%%" + segnum++ + "%%%\n"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				Iterator<Node> i = source.getContent().iterator();
				while (i.hasNext()) {
					Node n = i.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						writeSkel(cleanString(n.getNodeValue()));
					} else {
						Element e = new Element(n);
						writeSkel(e.getText());
					}
				}
			}
			writeSkel(end);
		}

		/**
		 * Check pairs.
		 * @param test
		 *            the test
		 * @return true, if successful
		 */
		private boolean checkPairs(String test) {
			String[] parts = test.trim().split("<"); //$NON-NLS-1$
			Stack<String> stack = new Stack<String>();
			for (int i = 0; i < parts.length; i++) {
				if (parts[i].length() > 0) {
					String[] subparts = parts[i].split("[\\s]|[>]"); //$NON-NLS-1$
					if (subparts[0].startsWith("/")) { //$NON-NLS-1$
						if (stack.size() == 0) {
							return false;
						} else {
							String last = stack.pop();
							if (!last.equals(subparts[0].substring(1))) {
								return false;
							}
						}
					} else {
						stack.push(subparts[0]);
					}
				}
			}
			if (stack.size() == 0) {
				return true;
			}
			return false;
		}

		/**
		 * Contains text.
		 * @param source
		 *            the source
		 * @return true, if successful
		 */
		private boolean containsText(Element source) {
			List<Node> content = source.getContent();
			String string = ""; //$NON-NLS-1$
			Iterator<Node> i = content.iterator();
			while (i.hasNext()) {
				Node n = i.next();
				if (n.getNodeType() == Node.TEXT_NODE) {
					string = string + n.getNodeValue();
				}
			}
			return !string.trim().equals(""); //$NON-NLS-1$
		}

		/**
		 * Write out.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeOut(String string) throws IOException {
			out.write(string.getBytes("UTF-8")); //$NON-NLS-1$
		}

		/**
		 * Recurse.
		 * @param e
		 *            the e
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws SAXException
		 *             the SAX exception
		 */
		private void recurse(Element e) throws IOException, SAXException {
			writeSkel("<" + e.getName()); //$NON-NLS-1$
			List<Attribute> atts = e.getAttributes();
			Iterator<Attribute> at = atts.iterator();
			while (at.hasNext()) {
				Attribute a = at.next();
				writeSkel(" " + a.getName() + "=\"" + a.getValue() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			writeSkel(">"); //$NON-NLS-1$
			List<Element> children = e.getChildren();
			Iterator<Element> i = children.iterator();
			while (i.hasNext()) {
				Element child = i.next();
				if (child.getName().matches("[a-z]:p") || child.getName().matches("t")) { //$NON-NLS-1$ //$NON-NLS-2$
					recursePara(child);
				} else {
					recurse(child);
				}
			}
			writeSkel("</" + e.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * Write skel.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeSkel(String string) throws IOException {
			skel.write(string.getBytes("UTF-8")); //$NON-NLS-1$
		}

		/**
		 * Recurse para.
		 * @param e
		 *            the e
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws SAXException
		 *             the SAX exception
		 */
		private void recursePara(Element e) throws IOException, SAXException {
			// send the opening tag to skeleton
			writeSkel("<" + e.getName()); //$NON-NLS-1$
			List<Attribute> atts = e.getAttributes();
			Iterator<Attribute> ia = atts.iterator();
			while (ia.hasNext()) {
				Attribute a = ia.next();
				writeSkel(" " + a.getName() + "=\"" + a.getValue() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			writeSkel(">"); //$NON-NLS-1$
			// send initial elements that don't have text to skeleton
			List<Element> content = e.getChildren();
			int i = 0;
			for (; i < content.size(); i++) {
				Element child = content.get(i);
				if (hasTextElement(child)) {
					break;
				}
				writeSkel(child.toString());
			}

			// fixed a bug by john. can not extrace text from docx and pptx
			if (e.getName().matches("([a-z]:)?t")) { //$NON-NLS-1$
				text = cleanString(e.getText());
			} else {
				if (content.size() - i == 1) {
					recursePara(content.get(i));
				} else {
					// get the text from the remaining elements
					for (; i < content.size(); i++) {
						recursePhrase(content.get(i));
					}
				}
			}
			text = text.replaceAll("</ph><ph>", ""); //$NON-NLS-1$ //$NON-NLS-2$
			// if (segByElement == true) {
			if (segByElement) {
				writeSegment(text);
			} else {
				String[] segs = segmenter.segment(text);
				for (int h = 0; h < segs.length; h++) {
					String seg = segs[h];
					writeSegment(seg);
				}
			}
			text = ""; //$NON-NLS-1$
			// send closing tag to skeleton
			writeSkel("</" + e.getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * Checks for text element.
		 * @param e
		 *            the e
		 * @return true, if successful
		 */
		private boolean hasTextElement(Element e) {
			// fixed a bug by john. can not extrace text from docx and pptx
			if (e.getName().matches("([a-z]:)?t")) { //$NON-NLS-1$
				return true;
			}
			boolean containsText = false;
			List<Element> children = e.getChildren();
			for (int i = 0; i < children.size(); i++) {
				containsText = containsText || hasTextElement(children.get(i));
			}
			return containsText;
		}

		/**
		 * Recurse phrase.
		 * @param e
		 *            the e
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void recursePhrase(Element e) throws IOException {
			text = text + "<ph>&lt;" + e.getName(); //$NON-NLS-1$
			List<Attribute> atts = e.getAttributes();
			Iterator<Attribute> ia = atts.iterator();
			while (ia.hasNext()) {
				Attribute a = ia.next();
				text = text + " " + a.getName() + "=\"" + a.getValue() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			text = text + "&gt;</ph>"; //$NON-NLS-1$

			// fixed a bug by john. can not extrace text from docx and pptx
			if (e.getName().matches("([a-z]:)?t")) { //$NON-NLS-1$
				text = text + cleanString(e.getText());
			} else {
				List<Element> children = e.getChildren();
				for (int i = 0; i < children.size(); i++) {
					recursePhrase(children.get(i));
				}
			}
			text = text + "<ph>&lt;/" + e.getName() + " &gt;</ph>"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * Count segments.
		 * @param string
		 *            the string
		 * @return the int
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private int countSegments(String string) throws SAXException, IOException {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(string);
			Element root = doc.getRootElement();
			return root.getChild("file").getChild("body").getChildren("trans-unit").size(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		/**
		 * Adds the file.
		 * @param xliff
		 *            the xliff
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void addFile(String xliff) throws SAXException, IOException {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(xliff);
			Element root = doc.getRootElement();
			Element file = root.getChild("file"); //$NON-NLS-1$
			Element newFile = new Element("file", merged); //$NON-NLS-1$
			newFile.clone(file, merged);
			mergedRoot.addContent(newFile);
			File f = new File(xliff);
			f.delete();
		}

		/**
		 * Update xliff.
		 * @param xliff
		 *            the xliff
		 * @param original
		 *            the original
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void updateXliff(String xliff, String original) throws SAXException, IOException {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(xliff);
			Element root = doc.getRootElement();
			Element file = root.getChild("file"); //$NON-NLS-1$
			file.setAttribute("datatype", TYPE_VALUE); //$NON-NLS-1$
			file.setAttribute("original", TextUtil.cleanString(inputFile)); //$NON-NLS-1$
			Element header = file.getChild("header"); //$NON-NLS-1$
			Element elePropGroup = new Element("hs:prop-group", doc); //$NON-NLS-1$
			elePropGroup.setAttribute("name", "document"); //$NON-NLS-1$ //$NON-NLS-2$

			Element originalProp = new Element("hs:prop", doc); //$NON-NLS-1$
			originalProp.setAttribute("prop-type", "original"); //$NON-NLS-1$ //$NON-NLS-2$
			originalProp.setText(original);
			header.addContent("\n"); //$NON-NLS-1$
			elePropGroup.addContent(originalProp);

			Element srcFileProp = new Element("hs:prop", doc); //$NON-NLS-1$
			srcFileProp.setAttribute("prop-type", "sourcefile"); //$NON-NLS-1$ //$NON-NLS-2$
			srcFileProp.setText(srcFile);
			header.addContent("\n"); //$NON-NLS-1$
			elePropGroup.addContent(srcFileProp);

			header.addContent(elePropGroup);
			Element ext = header.getChild("skl").getChild("external-file"); //$NON-NLS-1$ //$NON-NLS-2$
			ext.setAttribute("href", TextUtil.cleanString(skeleton)); //$NON-NLS-1$
			if (isSuite) {
				ext.setAttribute("crc", "" + CRC16.crc16(TextUtil.cleanString(skeleton).getBytes("UTF-8"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			XMLOutputter outputter = new XMLOutputter();
			FileOutputStream output = new FileOutputStream(xliff);
			outputter.output(doc, output);
			output.close();
		}
	}

	/**
	 * Clean string.
	 * @param string
	 *            the string
	 * @return the string
	 */
	private static String cleanString(String string) {
		string = string.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		string = string.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		string = string.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		return string;
	}
}
