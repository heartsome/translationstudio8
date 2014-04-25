/**
 * Xliff2OpenOffice.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.openoffice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.openoffice.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.util.ReverseConversionInfoLogRecord;
import net.heartsome.util.StringConverter;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Xliff2OpenOffice.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Xliff2OpenOffice implements Converter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Xliff2OpenOffice.class);

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-openoffice";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("openoffice.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to OpenOffice Conveter";

	// 内部实现所依赖的转换器
	/** The dependant converter. */
	private Converter dependantConverter;

	/**
	 * for test to initialize depend on converter.
	 */
	public Xliff2OpenOffice() {
		dependantConverter = Activator.getXMLConverter(Converter.DIRECTION_REVERSE);
	}

	/**
	 * 运行时把所依赖的转换器，在初始化的时候通过构造函数注入。.
	 * @param converter
	 *            the converter
	 */
	public Xliff2OpenOffice(Converter converter) {
		dependantConverter = converter;
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
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2OpenOfficeImpl converter = new Xliff2OpenOfficeImpl();
		return converter.run(args, monitor);
	}

	/**
	 * The Class Xliff2OpenOfficeImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2OpenOfficeImpl {

		/** The files table. */
		private Hashtable<String, String> filesTable;

		/** The is embedded. */
		private boolean isEmbedded = false;

		/** The catalogue. */
		private String catalogue;

		private boolean isInfoEnabled = LOGGER.isInfoEnabled();

		/**
		 * Builds the doc.
		 * @param filename
		 *            the filename
		 * @return the document
		 * @throws Exception
		 *             the exception
		 */
		public Document buildDoc(String filename) throws Exception {
			SAXBuilder builder = new SAXBuilder();
			builder.setEntityResolver(new Catalogue(catalogue));
			Document doc = builder.build(filename);
			return doc;
		}

		/**
		 * Creates the empty doc.
		 * @param filename
		 *            the filename
		 * @return the document
		 * @throws Exception
		 *             the exception
		 */
		public Document createEmptyDoc(String filename) throws Exception {
			File file = new File(filename);
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8"); //$NON-NLS-1$
			BufferedWriter bw = new BufferedWriter(osw);

			bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"); //$NON-NLS-1$
			bw.write("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" \n" //$NON-NLS-1$
					+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" //$NON-NLS-1$
					+ "xmlns:hs=\"" + Converter.HSNAMESPACE + "\" \n" //$NON-NLS-1$ //$NON-NLS-2$
					+ "xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd \n" //$NON-NLS-1$
					+ Converter.HSSCHEMALOCATION + "\">\n"); //$NON-NLS-1$
			bw.write("</xliff>\n"); //$NON-NLS-1$

			bw.flush();
			bw.close();
			osw.close();
			fos.close();
			bw = null;
			osw = null;
			fos = null;

			return buildDoc(filename);
		}

		/**
		 * Save file.
		 * @param element
		 *            the element
		 * @throws Exception
		 *             the exception
		 */
		private void saveFile(Element element) throws Exception {
			File xliff = File.createTempFile("tmp", ".xlf"); //$NON-NLS-1$ //$NON-NLS-2$ 
			Document doc = createEmptyDoc(xliff.getAbsolutePath());
			Element root = doc.getRootElement();
			root.setAttribute("version", "1.2"); //$NON-NLS-1$ //$NON-NLS-2$
			Element file = new Element("file", doc); //$NON-NLS-1$
			file.clone(element, doc);
			root.addContent(file);

			List<Element> groups = file.getChild("header").getChildren("hs:prop-group"); //$NON-NLS-1$ //$NON-NLS-2$
			Iterator<Element> i = groups.iterator();
			while (i.hasNext()) {
				Element group = i.next();
				if (group.getAttributeValue("name").equals("document")) { //$NON-NLS-1$ //$NON-NLS-2$
					filesTable.put(group.getChild("hs:prop").getText(), xliff.getAbsolutePath()); //$NON-NLS-1$
				}
			}
			if (file.getChild("header").getChild("skl").getChild("external-file") == null) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				// embedded skeleton
				file.getChild("header").getChild("skl").addContent(new Element("external-file", doc)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				isEmbedded = true;
			}
			file
					.getChild("header").getChild("skl").getChild("external-file").setAttribute("href", xliff.getAbsolutePath() + ".skl"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			XMLOutputter outputter = new XMLOutputter();
			FileOutputStream output = new FileOutputStream(xliff.getAbsolutePath());
			outputter.output(doc, output);
			output.close();
			outputter = null;
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
			ReverseConversionInfoLogRecord infoLogger = ConverterUtils.getReverseConversionInfoLogRecord();
			infoLogger.startConversion();
			Map<String, String> result = new HashMap<String, String>();
			String xliffFile = args.get(Converter.ATTR_XLIFF_FILE);
			String outputFile = args.get(Converter.ATTR_TARGET_FILE);
			catalogue = args.get(Converter.ATTR_CATALOGUE);
			filesTable = new Hashtable<String, String>();
			try {
				// 把转换过程分为三部分共 20 个任务，其中分离出各个 xliff 文件占 8，分离出 skeleton 文件占 2，生成目标文件占 10。
				monitor.beginTask("", 20);
				infoLogger.logConversionFileInfo(catalogue, null, xliffFile, null);

				IProgressMonitor separateMonitor = Progress.getSubMonitor(monitor, 8);
				long startTime = 0;
				try {
					if (isInfoEnabled) {
						startTime = System.currentTimeMillis();
						LOGGER.info(Messages.getString("openoffice.Xliff2OpenOffice.logger1"), startTime);
					}
					SAXBuilder builder = new SAXBuilder();
					Document doc = builder.build(xliffFile);
					Element root = doc.getRootElement();
					List<Element> files = root.getChildren("file"); //$NON-NLS-1$

					separateMonitor.beginTask(Messages.getString("openoffice.Xliff2OpenOffice.task2"), files.size());
					separateMonitor.subTask("");
					Iterator<Element> it = files.iterator();
					while (it.hasNext()) {
						saveFile(it.next());
						// 是否取消操作
						if (separateMonitor.isCanceled()) {
							throw new OperationCanceledException(Messages.getString("openoffice.cancel"));
						}
						separateMonitor.worked(1);
					}
				} finally {
					separateMonitor.done();
				}
				long endTime = 0;
				if (isInfoEnabled) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("openoffice.Xliff2OpenOffice.logger2"), endTime);
					LOGGER.info(Messages.getString("openoffice.Xliff2OpenOffice.logger3"), endTime - startTime);
				}

				monitor.subTask(Messages.getString("openoffice.Xliff2OpenOffice.task3"));
				if (isInfoEnabled) {
					startTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("openoffice.Xliff2OpenOffice.logger4"), startTime);
				}
				String skeleton = args.get(Converter.ATTR_SKELETON_FILE);
				if (isEmbedded) {
					File t = File.createTempFile("tmp", ".skl"); //$NON-NLS-1$ //$NON-NLS-2$
					StringConverter.decodeFile(skeleton, t.getAbsolutePath());
					skeleton = t.getAbsolutePath();
				}
				if (isInfoEnabled) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("openoffice.Xliff2OpenOffice.logger5"), endTime);
					LOGGER.info(Messages.getString("openoffice.Xliff2OpenOffice.logger6"), endTime - startTime);
				}
				monitor.worked(2);

				IProgressMonitor conversionMonitor = Progress.getSubMonitor(monitor, 10);
				try {
					ZipFile zipFile = new ZipFile(skeleton);
					int totalTask = zipFile.size();
					conversionMonitor.beginTask("", totalTask);
					if (isInfoEnabled) {
						startTime = System.currentTimeMillis();
						LOGGER.info(Messages.getString("openoffice.Xliff2OpenOffice.logger7"), startTime);
					}
					ZipInputStream in = new ZipInputStream(new FileInputStream(skeleton));
					ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
					ZipEntry entry = null;
					String messagePattern = Messages.getString("openoffice.Xliff2OpenOffice.msg1");
					while ((entry = in.getNextEntry()) != null) {
						// 标识是否委派其它转换器进行了处理
						boolean isDelegate = false;
						// 是否取消
						if (conversionMonitor.isCanceled()) {
							throw new OperationCanceledException(Messages.getString("openoffice.cancel"));
						}
						String message = MessageFormat.format(messagePattern, new Object[] { entry.getName() });
						conversionMonitor.subTask(message);

						if (entry.getName().matches(".*\\.[xX][mM][lL]\\.skl")) { //$NON-NLS-1$
							String name = entry.getName().substring(0, entry.getName().lastIndexOf(".skl")); //$NON-NLS-1$
							File tmp = new File(filesTable.get(name) + ".skl"); //$NON-NLS-1$
							FileOutputStream output = new FileOutputStream(tmp.getAbsolutePath());
							byte[] buf = new byte[1024];
							int len;
							while ((len = in.read(buf)) > 0) {
								output.write(buf, 0, len);
							}
							output.close();
							Hashtable<String, String> table = new Hashtable<String, String>();
							table.put(Converter.ATTR_XLIFF_FILE, filesTable.get(name));
							table.put(Converter.ATTR_TARGET_FILE, filesTable.get(name) + ".xml");
							table.put(Converter.ATTR_CATALOGUE, catalogue);
							table.put(Converter.ATTR_SKELETON_FILE, filesTable.get(name) + ".skl");
							Map<String, String> res = dependantConverter.convert(table, Progress.getSubMonitor(
									separateMonitor, 1));
							isDelegate = true;
							if (res.get(Converter.ATTR_TARGET_FILE) == null) {
								ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
										Messages.getString("openoffice.Xliff2OpenOffice.msg2"));
							}
							ZipEntry content = new ZipEntry(name);
							content.setMethod(ZipEntry.DEFLATED);
							out.putNextEntry(content);
							FileInputStream input = new FileInputStream(filesTable.get(name) + ".xml"); //$NON-NLS-1$
							while ((len = input.read(buf)) > 0) {
								out.write(buf, 0, len);
							}
							out.closeEntry();
							input.close();
							tmp.delete();
							File xml = new File(filesTable.get(name) + ".xml"); //$NON-NLS-1$
							xml.delete();
							File xlf = new File(filesTable.get(name));
							xlf.delete();
						} else {
							File tmp = File.createTempFile("entry", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
							FileOutputStream output = new FileOutputStream(tmp.getAbsolutePath());
							byte[] buf = new byte[1024];
							int len;
							while ((len = in.read(buf)) > 0) {
								output.write(buf, 0, len);
							}
							output.close();
							ZipEntry content = new ZipEntry(entry.getName());
							content.setMethod(ZipEntry.DEFLATED);
							out.putNextEntry(content);
							FileInputStream input = new FileInputStream(tmp.getAbsolutePath());
							while ((len = input.read(buf)) > 0) {
								out.write(buf, 0, len);
							}
							out.closeEntry();
							input.close();
							tmp.delete();
						}
						if (!isDelegate) {
							conversionMonitor.worked(1);
						}
					}
					out.close();
				} finally {
					conversionMonitor.done();
				}
				if (isInfoEnabled) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("openoffice.Xliff2OpenOffice.logger8"), endTime);
					LOGGER.info(Messages.getString("openoffice.Xliff2OpenOffice.logger9"), endTime - startTime);
				}
				if (isEmbedded) {
					File f = new File(skeleton);
					f.delete();
				}
				result.put(Converter.ATTR_TARGET_FILE, outputFile);
			} catch (OperationCanceledException e) {
				throw e;
			} catch (ConverterException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("openoffice.Xliff2OpenOffice.msg2"), e);
			} finally {
				monitor.done();
			}
			infoLogger.endConversion();
			return result;
		}
	}

}
