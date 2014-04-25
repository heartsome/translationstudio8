/**
 * OpenOffice2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.openoffice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
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
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.xml.sax.SAXException;

/**
 * The Class OpenOffice2Xliff.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class OpenOffice2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-openoffice";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("openoffice.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "OpenOffice to XLIFF Conveter";

	// 内部实现所依赖的转换器
	/** The dependant converter. */
	private Converter dependantConverter;

	/**
	 * for test to initialize depend on converter.
	 */
	public OpenOffice2Xliff() {
		dependantConverter = Activator.getXMLConverter(Converter.DIRECTION_POSITIVE);
	}

	/**
	 * 运行时把所依赖的转换器，在初始化的时候通过构造函数注入。.
	 * @param converter
	 *            the converter
	 */
	public OpenOffice2Xliff(Converter converter) {
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
		OpenOffice2XliffImpl converter = new OpenOffice2XliffImpl();
		return converter.run(args, monitor);
	}

	/**
	 * The Class OpenOffice2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class OpenOffice2XliffImpl {

		/** The merged. */
		private Document merged;

		/** The merged root. */
		private Element mergedRoot;

		/** The zip in. */
		private ZipInputStream zipIn;

		/** The zip out. */
		private ZipOutputStream zipOut;

		/** The src file. */
		private String srcFile;

		/** The skeleton. */
		private String skeleton;

		/** The is suite. */
		private boolean isSuite;

		private String type ;
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
			file.setAttribute("datatype", type); //$NON-NLS-1$
			// file.setAttribute("original", TextUtil.cleanString(srcFile)); //$NON-NLS-1$
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

		private void setType(String args ){
			if("true".equals(args)){
				type="x-msoffice2003";
			}else{
				type=TYPE_VALUE;
			}
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
			setType(args.get("isofficefile"));
			String xliff = args.get(Converter.ATTR_XLIFF_FILE);
			skeleton = args.get(Converter.ATTR_SKELETON_FILE);
			isSuite = false;
			if (Converter.TRUE.equals(args.get(Converter.ATTR_IS_SUITE))) {
				isSuite = true;
			}
			try {
				// 把总任务分为压缩文件中的条目个数＋1;其中最后一个任务为 library3 写合并后的 xliff 文件。
				ZipFile zipFile = new ZipFile(srcFile);
				int size = zipFile.size();
				int totalTask = size + 1;
				monitor.beginTask("", totalTask);
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
					// 标识当前的条目是否委托其他转换器进行转换
					boolean isDelegation = false;
					// 检查是否取消操作
					if (monitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("openoffice.cancel"));
					}
					String messagePattern = Messages.getString("openoffice.OpenOffice2Xliff.msg1");
					String message = MessageFormat.format(messagePattern, new Object[] { entry.getName() });
					monitor.subTask(message);
					if (entry.getName().matches(".*\\.[xX][mM][lL]")) { //$NON-NLS-1$
						File f = new File(entry.getName());
						String name = f.getName();
						String tmpFileName = name.substring(0, name.lastIndexOf(".")); //$NON-NLS-1$
						if (tmpFileName.length() < 3) {
							tmpFileName += "_tmp"; //$NON-NLS-1$
						}
						File tmp = File.createTempFile(tmpFileName, ".xml");
						FileOutputStream output = new FileOutputStream(tmp.getAbsolutePath());
						byte[] buf = new byte[1024];
						int len;
						while ((len = zipIn.read(buf)) > 0) {
							output.write(buf, 0, len);
						}
						output.close();
						try {
							Map<String, String> table = new HashMap<String, String>();
							table.put(Converter.ATTR_SOURCE_FILE, tmp.getAbsolutePath());
							table.put(Converter.ATTR_XLIFF_FILE, tmp.getAbsolutePath() + ".xlf"); //$NON-NLS-1$ 
							table.put(Converter.ATTR_SKELETON_FILE, tmp.getAbsolutePath() + ".skl"); //$NON-NLS-1$ 
							table.put(Converter.ATTR_CATALOGUE, args.get(Converter.ATTR_CATALOGUE));
							table.put(Converter.ATTR_SOURCE_LANGUAGE, args.get(Converter.ATTR_SOURCE_LANGUAGE));
							table.put(Converter.ATTR_TARGET_LANGUAGE, args.get(Converter.ATTR_TARGET_LANGUAGE));
							table.put(Converter.ATTR_SOURCE_ENCODING, args.get(Converter.ATTR_SOURCE_ENCODING));
							table.put(Converter.ATTR_PROGRAM_FOLDER, args.get(Converter.ATTR_PROGRAM_FOLDER));
							table.put(Converter.ATTR_SEG_BY_ELEMENT, args.get(Converter.ATTR_SEG_BY_ELEMENT));
							table.put(Converter.ATTR_SRX, args.get(Converter.ATTR_SRX));
							table.put(Converter.ATTR_FORMAT, TYPE_VALUE);

							Converter converter = dependantConverter;
							boolean hasError = false;
							Map<String, String> res = null;
							try {
								// 委托其它转换器进行操作
								res = converter.convert(table, Progress.getSubMonitor(monitor, 1));
								isDelegation = true;
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
					// 如果当前条目没有委托其它转换器进行操作，则需要把任务的处理进度加 1
					if (!isDelegation) {
						monitor.worked(1);
					}
				}

				zipOut.close();

				// output final XLIFF

				// fixed a bug 572 by john. keep a well-format of xliff. add a empty
				// file into xliff if root have not file element.
				List<Element> files = mergedRoot.getChildren("file"); //$NON-NLS-1$
				if (files.size() == 0) {
					Element file = new Element("file", merged); //$NON-NLS-1$
					file.setAttribute("original", srcFile); //$NON-NLS-1$
					file.setAttribute("source-language", args.get("srcLang")); //$NON-NLS-1$ //$NON-NLS-2$
					file.setAttribute("datatype", type); //$NON-NLS-1$
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
					throw new OperationCanceledException(Messages.getString("openoffice.cancel"));
				}
				monitor.subTask(Messages.getString("openoffice.OpenOffice2Xliff.task2"));

				XMLOutputter outputter = new XMLOutputter();
				outputter.preserveSpace(true);
				FileOutputStream output = new FileOutputStream(xliff);
				outputter.output(merged, output);
				output.close();
				result.put(Converter.ATTR_XLIFF_FILE, xliff);

				monitor.worked(1);
			} catch(OperationCanceledException e){
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("openoffice.OpenOffice2Xliff.msg2"), e);
			} finally {
				monitor.done();
			}
			return result;
		}
	}
}
