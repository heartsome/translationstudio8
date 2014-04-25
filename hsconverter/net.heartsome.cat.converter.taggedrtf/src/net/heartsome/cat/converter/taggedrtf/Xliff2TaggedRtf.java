/**
 * Xliff2TaggedRtf.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.taggedrtf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.taggedrtf.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.util.ReverseConversionInfoLogRecord;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The Class Xliff2TaggedRtf.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Xliff2TaggedRtf implements Converter {
	private static final Logger LOGGER = LoggerFactory.getLogger(Xliff2TaggedRtf.class);

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-trtf";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("taggedrtf.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to Tagged RTF Conveter";

	// 内部实现所依赖的转换器
	/** The dependant converter. */
	private Converter dependantConverter;

	/**
	 * for test to initialize depend on converter.
	 */
	public Xliff2TaggedRtf() {
		dependantConverter = Activator.getRtfConverter(Converter.DIRECTION_REVERSE);
	}

	/**
	 * 运行时把所依赖的转换器，在初始化的时候通过构造函数注入。.
	 * @param converter
	 *            the converter
	 */
	public Xliff2TaggedRtf(Converter converter) {
		dependantConverter = converter;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2TaggedRtfImpl converter = new Xliff2TaggedRtfImpl();
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
	 * The Class Xliff2TaggedRtfImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2TaggedRtfImpl {

		/** The skeleton. */
		private String skeleton;

		/** The xliff. */
		private String xliff;

		/** The skl doc. */
		private Document sklDoc;

		/** The skl root. */
		private Element sklRoot;

		/** The segments. */
		private Hashtable<String, Element> segments;

		/** The tw4win mark. */
		private String tw4winMark;

		/** The source start. */
		private Element sourceStart;

		/** The source end. */
		private Element sourceEnd;

		/** The target start. */
		private Element targetStart;

		/** The target end. */
		private Element targetEnd;

		/** The tags. */
		private String tags;

		/** The close. */
		private String close;

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
			try {
				// 把转换过程分为四大部分共 10 个任务，其中加载文件占 2，文件处理占 2，重写 xml 文件占 2，委派其它转换器进行转换占 4。
				monitor.beginTask("", 10);
				monitor.subTask(Messages.getString("taggedrtf.Xliff2TaggedRtf.task2"));
				infoLogger.startLoadingXliffFile();
				xliff = params.get(Converter.ATTR_XLIFF_FILE);
				skeleton = params.get(Converter.ATTR_SKELETON_FILE) + ".tg.skl";

				infoLogger.logConversionFileInfo(null, null, xliff, skeleton);

				loadFiles();
				infoLogger.endLoadingXliffFile();
				monitor.worked(2);

				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.subTask(Messages.getString("taggedrtf.Xliff2TaggedRtf.task3"));
				long startTime = 0;
				if (LOGGER.isInfoEnabled()) {
					startTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("taggedrtf.Xliff2TaggedRtf.logger1"), startTime);
				}
				processFiles();
				long endTime = 0;
				if (LOGGER.isInfoEnabled()) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("taggedrtf.Xliff2TaggedRtf.logger2"), endTime);
					LOGGER.info(Messages.getString("taggedrtf.Xliff2TaggedRtf.logger3"), endTime - startTime);
				}
				monitor.worked(2);

				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.subTask(Messages.getString("taggedrtf.Xliff2TaggedRtf.task4"));
				if (LOGGER.isInfoEnabled()) {
					startTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("taggedrtf.Xliff2TaggedRtf.logger4"), startTime);
				}
				File nskl = new File(skeleton + ".xlf");
				FileOutputStream output = new FileOutputStream(nskl.getAbsolutePath());
				XMLOutputter outputter = new XMLOutputter();
				outputter.preserveSpace(true);
				outputter.output(sklDoc, output);
				output.close();
				if (LOGGER.isInfoEnabled()) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("taggedrtf.Xliff2TaggedRtf.logger5"), endTime);
					LOGGER.info(Messages.getString("taggedrtf.Xliff2TaggedRtf.logger6"), endTime - startTime);
				}
				monitor.worked(2);
				params.put(Converter.ATTR_XLIFF_FILE, nskl.getAbsolutePath());
				params.put(Converter.ATTR_SKELETON_FILE, getSkeleton());
				// 委派其它转换器进行转换
				result = dependantConverter.convert(params, Progress.getSubMonitor(monitor, 4));
				nskl.delete();
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
				if (e instanceof ConverterException) {
					throw (ConverterException)e;
				} else {
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("taggedrtf.Xliff2TaggedRtf.msg1"), e);
				}
			} finally {
				monitor.done();
			}
			infoLogger.endConversion();
			return result;
		}

		/**
		 * Gets the skeleton.
		 * @return the skeleton
		 */
		private String getSkeleton() {
			String result = ""; //$NON-NLS-1$
			Element file = sklRoot.getChild("file"); //$NON-NLS-1$
			if (file != null) {
				Element header = file.getChild("header"); //$NON-NLS-1$
				if (header != null) {
					Element mskl = header.getChild("skl"); //$NON-NLS-1$
					if (mskl != null) {
						Element external = mskl.getChild("external-file"); //$NON-NLS-1$
						if (external != null) {
							result = external.getAttributeValue("href"); //$NON-NLS-1$
						}
					}
				}
			}
			return result;
		}

		/**
		 * Process files.
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void processFiles() throws SAXException, IOException {
			Element body = sklRoot.getChild("file").getChild("body"); //$NON-NLS-1$ //$NON-NLS-2$
			List<Element> units = body.getChildren("trans-unit"); //$NON-NLS-1$
			Iterator<Element> i = units.iterator();
			while (i.hasNext()) {
				Element unit = i.next();
				Element segment = segments.get(unit.getAttributeValue("id")); //$NON-NLS-1$
				//FIX  Bug #3417 :Tagged RTF 转换器：转换为目标文件时无法获取译文
				//if (segment.getAttributeValue("approved", "no").equalsIgnoreCase("yes")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					buildTarget(unit, segment);
				//	unit.setAttribute("approved", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
				//}
			}
		}

		/**
		 * Builds the target.
		 * @param original
		 *            the original
		 * @param translated
		 *            the translated
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void buildTarget(Element original, Element translated) throws SAXException, IOException {
			Element source = original.getChild("source"); //$NON-NLS-1$
			Element target = original.getChild("target"); //$NON-NLS-1$
			if (target == null) {
				target = new Element("target", sklDoc); //$NON-NLS-1$
				original.addContent(target);
			}
			int phId = 1000;
			if (TaggedRtf2Xliff.find(source.toString(), "{0&gt;", 0) != -1) { //$NON-NLS-1$
				// this segment was already formatted by Trados
				buildMissingTags(translated);

				List<Node> content = sourceStart.getContent();
				Iterator<Node> it = content.iterator();
				while (it.hasNext()) {
					Node n = it.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						String s = n.getNodeValue();
						Element open = new Element("ph", sklDoc); //$NON-NLS-1$
						open.setAttribute("id", "" + phId++); //$NON-NLS-1$ //$NON-NLS-2$
						if (s.matches("^[0-9a-zA-Z\\s].*")) { //$NON-NLS-1$
							open.setText("\\v "); //$NON-NLS-1$
						} else {
							open.setText("\\v"); //$NON-NLS-1$
						}
						target.addContent(open);
						target.addContent(s);
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = new Element(n);
						Element tag = new Element(e.getName(), sklDoc);
						tag.clone(e, sklDoc);
						target.addContent(tag);
					}
				}

				content = translated.getChild("source").getContent(); //$NON-NLS-1$
				it = content.iterator();
				while (it.hasNext()) {
					Node n = it.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						String s = n.getNodeValue();
						Element open = new Element("ph", sklDoc); //$NON-NLS-1$
						open.setAttribute("id", "" + phId++); //$NON-NLS-1$ //$NON-NLS-2$
						if (s.matches("^[0-9a-zA-Z\\s].*")) { //$NON-NLS-1$
							open.setText("\\v "); //$NON-NLS-1$
						} else {
							open.setText("\\v"); //$NON-NLS-1$
						}
						target.addContent(open);
						target.addContent(s);
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = new Element(n);
						Element tag = new Element(e.getName(), sklDoc);
						tag.clone(e, sklDoc);
						String s = tag.getText();
						if (s.matches("^[0-9a-zA-Z\\s].*")) { //$NON-NLS-1$
							tag.setText("\\v " + s); //$NON-NLS-1$
						} else {
							tag.setText("\\v" + s); //$NON-NLS-1$
						}
						target.addContent(tag);
					}
				}
				content = sourceEnd.getContent();
				it = content.iterator();
				while (it.hasNext()) {
					Node n = it.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						String s = n.getNodeValue();
						Element open = new Element("ph", sklDoc); //$NON-NLS-1$
						open.setAttribute("id", "" + phId++); //$NON-NLS-1$ //$NON-NLS-2$
						if (s.matches("^[0-9a-zA-Z\\s].*")) { //$NON-NLS-1$
							open.setText("\\v "); //$NON-NLS-1$
						} else {
							open.setText("\\v"); //$NON-NLS-1$
						}
						target.addContent(open);
						target.addContent(s);
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = new Element(n);
						Element tag = new Element(e.getName(), sklDoc);
						tag.clone(e, sklDoc);
						target.addContent(tag);
					}
				}

				Element percent = new Element("ph", sklDoc); //$NON-NLS-1$
				percent.setText("\\v0{" + tw4winMark + " <\\}100\\{>}"); //$NON-NLS-1$ //$NON-NLS-2$
				target.addContent(percent);
				percent.setAttribute("id", "" + phId++); //$NON-NLS-1$ //$NON-NLS-2$

				content = targetStart.getContent();
				it = content.iterator();
				while (it.hasNext()) {
					Node n = it.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						String s = n.getNodeValue();
						Element open = new Element("ph", sklDoc); //$NON-NLS-1$
						open.setAttribute("id", "" + phId++); //$NON-NLS-1$ //$NON-NLS-2$
						if (s.matches("^[0-9a-zA-Z\\s].*")) { //$NON-NLS-1$
							open.setText("\\v0 "); //$NON-NLS-1$
						} else {
							open.setText("\\v0"); //$NON-NLS-1$
						}
						target.addContent(open);
						target.addContent(s);
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = new Element(n);
						Element tag = new Element(e.getName(), sklDoc);
						tag.clone(e, sklDoc);
						target.addContent(tag);
					}
				}

				content = translated.getChild("target").getContent(); //$NON-NLS-1$
				it = content.iterator();
				while (it.hasNext()) {
					Node n = it.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						String s = n.getNodeValue();
						target.addContent(s);
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = new Element(n);
						Element tag = new Element(e.getName(), sklDoc);
						tag.clone(e, sklDoc);
						target.addContent(tag);
					}
				}

				content = targetEnd.getContent();
				it = content.iterator();
				while (it.hasNext()) {
					Node n = it.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						target.addContent(n.getNodeValue());
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = new Element(n);
						Element tag = new Element(e.getName(), sklDoc);
						tag.clone(e, sklDoc);
						target.addContent(tag);
					}
				}

			} else {
				// It is necessary to add Trados formatting
				// and hide source text

				Element start = new Element("ph", sklDoc); //$NON-NLS-1$
				start.setText("{" + tw4winMark + " \\{0>}"); //$NON-NLS-1$ //$NON-NLS-2$
				start.setAttribute("id", "" + phId++); //$NON-NLS-1$ //$NON-NLS-2$
				target.addContent(start);

				tags = ""; //$NON-NLS-1$

				List<Node> content = source.getContent();
				Iterator<Node> it = content.iterator();
				while (it.hasNext()) {
					Node n = it.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						String s = n.getNodeValue();
						Element open = new Element("ph", sklDoc); //$NON-NLS-1$
						open.setAttribute("id", "" + phId++); //$NON-NLS-1$ //$NON-NLS-2$
						if (s.matches("^[0-9a-zA-Z\\s]")) { //$NON-NLS-1$
							open.setText("\\v "); //$NON-NLS-1$
						} else {
							open.setText("\\v"); //$NON-NLS-1$
						}
						target.addContent(open);
						target.addContent(s);
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = new Element(n);
						checkTags(e);
						Element tag = new Element(e.getName(), sklDoc);
						tag.clone(e, sklDoc);
						String s = tag.getText();
						if (s.matches("^[0-9a-zA-Z\\s].*")) { //$NON-NLS-1$
							tag.setText("\\v " + s); //$NON-NLS-1$
						} else {
							tag.setText("\\v" + s); //$NON-NLS-1$
						}
						target.addContent(tag);
					}
				}

				closeTags();

				Element percent = new Element("ph", sklDoc); //$NON-NLS-1$
				percent.setText(close + "\\v0{" + tw4winMark + " <\\}100\\{>}"); //$NON-NLS-1$ //$NON-NLS-2$
				percent.setAttribute("id", "" + phId++); //$NON-NLS-1$ //$NON-NLS-2$
				target.addContent(percent);

				Element transTarget = translated.getChild("target"); //$NON-NLS-1$
				if(null == transTarget){
					return ;
				}
				List<Node> transContent = transTarget.getContent();
				it = transContent.iterator();
				boolean skip = false;
				while (it.hasNext()) {
					Node n = it.next();
					if (n.getNodeType() == Node.TEXT_NODE) {
						String s = n.getNodeValue();
						Element open = new Element("ph", sklDoc); //$NON-NLS-1$
						open.setAttribute("id", "" + phId++); //$NON-NLS-1$ //$NON-NLS-2$
						if (s.matches("^[0-9a-zA-Z\\s].*")) { //$NON-NLS-1$
							open.setText("\\v0 "); //$NON-NLS-1$
						} else {
							open.setText("\\v0"); //$NON-NLS-1$
						}
						target.addContent(open);
						target.addContent(s);
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element e = new Element(n);
						if (e.toString().indexOf(tw4winMark) != -1) {
							skip = true;
						} else {
							skip = false;
						}
						Element tag = new Element(e.getName(), sklDoc);
						tag.clone(e, sklDoc);
						target.addContent(tag);
					}
				}
				if (!skip) {
					Element last = new Element("ph", sklDoc); //$NON-NLS-1$
					last.setAttribute("id", "" + phId++); //$NON-NLS-1$ //$NON-NLS-2$
					last.setText("{" + tw4winMark + " <0\\}}"); //$NON-NLS-1$ //$NON-NLS-2$
					target.addContent(last);
				}

			}
		}

		/**
		 * Close tags.
		 */
		private void closeTags() {
			StringTokenizer tk = new StringTokenizer(tags, "\\{}", true); //$NON-NLS-1$
			String result = ""; //$NON-NLS-1$
			int balance = 0;
			while (tk.hasMoreTokens()) {
				String token = tk.nextToken();
				if (token.equals("\\")) { //$NON-NLS-1$
					if (tk.hasMoreTokens()) {
						token = token + tk.nextToken();
					} else {
						// TODO check why this happens
						System.out.println(tags);
					}
				}
				if (token.equals("{")) { //$NON-NLS-1$
					balance++;
				} else if (token.equals("}")) { //$NON-NLS-1$
					balance--;
				}
				if (token.equals("}")) { //$NON-NLS-1$
					int index = result.lastIndexOf("{"); //$NON-NLS-1$
					if (index != -1) {
						result = result.substring(0, index + 1);
						continue;
					}
					result = ""; //$NON-NLS-1$
					continue;
				}
				result = result + token;
			}
			close = ""; //$NON-NLS-1$
			if (result.indexOf("\\super") != -1 || result.indexOf("\\sub") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
				close = close + "\\nosupersub"; //$NON-NLS-1$
			}
			if (result.indexOf("\\b\\") != -1 || result.indexOf("\\b ") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
				close = close + "\\b0"; //$NON-NLS-1$
			}
			if (result.indexOf("\\i\\") != -1 || result.indexOf("\\i ") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
				close = close + "\\i0"; //$NON-NLS-1$
			}
			if (result.indexOf("\\ul\\") != -1 || result.indexOf("\\ul ") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
				close = close + "\\ul0"; //$NON-NLS-1$
			}
			if (result.indexOf("\\strike") != -1) { //$NON-NLS-1$
				close = close + "\\strike0"; //$NON-NLS-1$
			}
			if (balance > 0) {
				for (int i = 0; i < balance; i++) {
					close = close + "}"; //$NON-NLS-1$
				}
			}
			if (balance < 0) {
				balance = -1 * balance;
				for (int i = 0; i < balance; i++) {
					close = close + "{"; //$NON-NLS-1$
				}
			}
		}

		/**
		 * Check tags.
		 * @param e
		 *            the e
		 */
		private void checkTags(Element e) {
			tags = tags + e.getText();
		}

		/**
		 * Builds the missing tags.
		 * @param tu
		 *            the tu
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void buildMissingTags(Element tu) throws SAXException, IOException {
			List<Element> groups = tu.getChildren("hs:prop-group"); //$NON-NLS-1$
			Iterator<Element> it = groups.iterator();
			while (it.hasNext()) {
				Element group = it.next();
				if (group.getAttributeValue("name", "").equals("tags")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					List<Element> props = group.getChildren();
					Iterator<Element> j = props.iterator();
					while (j.hasNext()) {
						Element prop = j.next();
						SAXBuilder builder = new SAXBuilder();
						String text = "<?xml version=\"1.0\"?>\n<tag>" + prop.getText() + "</tag>"; //$NON-NLS-1$ //$NON-NLS-2$
						ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes("UTF-8")); //$NON-NLS-1$
						Document doc = builder.build(stream);
						Element root = doc.getRootElement();
						if (prop.getAttributeValue("prop-type").equals("sourceStart")) { //$NON-NLS-1$ //$NON-NLS-2$
							sourceStart = new Element("tags", sklDoc); //$NON-NLS-1$
							sourceStart.clone(root, sklDoc);
						}
						if (prop.getAttributeValue("prop-type").equals("sourceEnd")) { //$NON-NLS-1$ //$NON-NLS-2$
							sourceEnd = new Element("tags", sklDoc); //$NON-NLS-1$
							sourceEnd.clone(root, sklDoc);
						}
						if (prop.getAttributeValue("prop-type").equals("targetStart")) { //$NON-NLS-1$ //$NON-NLS-2$
							targetStart = new Element("tags", sklDoc); //$NON-NLS-1$
							targetStart.clone(root, sklDoc);
						}
						if (prop.getAttributeValue("prop-type").equals("targetEnd")) { //$NON-NLS-1$ //$NON-NLS-2$
							targetEnd = new Element("tags", sklDoc); //$NON-NLS-1$
							targetEnd.clone(root, sklDoc);
						}
					}
				}
			}

		}

		/**
		 * Load files.
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @throws ConverterException 
		 */
		private void loadFiles() throws ConverterException {
			SAXBuilder builder = new SAXBuilder();
			try {
				sklDoc = builder.build(skeleton);
			} catch (Exception e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("taggedrtf.Xliff2TaggedRtf.msg2"), e);
			}
			sklRoot = sklDoc.getRootElement();

			try {
				Document xlfDoc = builder.build(xliff);
				Element xlfRoot = xlfDoc.getRootElement();
				Element body = xlfRoot.getChild("file").getChild("body"); //$NON-NLS-1$ //$NON-NLS-2$
				List<Element> units = body.getChildren("trans-unit"); //$NON-NLS-1$
				Iterator<Element> i = units.iterator();
				segments = new Hashtable<String, Element>();
				while (i.hasNext()) {
					Element unit = i.next();
					segments.put(unit.getAttributeValue("id"), unit); //$NON-NLS-1$
				}

				List<Element> properties = xlfRoot.getChild("file").getChild("header").getChildren("hs:prop-group"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				i = properties.iterator();
				while (i.hasNext()) {
					Element group = i.next();
					if (group.getAttributeValue("name", "").equals("tags")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						List<Element> taglist = group.getChildren("hs:prop"); //$NON-NLS-1$
						Iterator<Element> t = taglist.iterator();
						while (t.hasNext()) {
							Element prop = t.next();
							if (prop.getAttributeValue("prop-type").equals("Mark")) { //$NON-NLS-1$ //$NON-NLS-2$
								tw4winMark = prop.getText();
							}
						}
					}
				}
			} catch (Exception e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("taggedrtf.Xliff2TaggedRtf.msg3"), e);
			}
		}
	}

}
