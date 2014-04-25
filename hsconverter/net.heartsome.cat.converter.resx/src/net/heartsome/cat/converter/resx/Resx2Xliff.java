/**
 * Resx2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.resx;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.resx.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.w3c.dom.Node;

/**
 * The Class Resx2Xliff.
 * @author John Zhu
 */
public class Resx2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "resx";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("resx.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "ResX to XLIFF Conveter";

	// 内部实现所依赖的转换器
	/** The dependant converter. */
	private Converter dependantConverter;

	/**
	 * for test to initialize depend on converter.
	 */
	public Resx2Xliff() {
		dependantConverter = Activator.getXMLConverter(Converter.DIRECTION_POSITIVE);
	}

	/**
	 * 运行时把所依赖的转换器，在初始化的时候通过构造函数注入。.
	 * @param converter
	 *            the converter
	 */
	public Resx2Xliff(Converter converter) {
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
		Resx2XliffImpl converter = new Resx2XliffImpl();
		args.put(Converter.ATTR_FORMAT, TYPE_VALUE);
		args.put(Converter.ATTR_IS_RESX, Converter.TRUE);
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
	 * The Class Resx2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Resx2XliffImpl {

		/** The xml resx. */
		private Document xmlResx; // Temporary xml for the conversion

		/** The input file. */
		private String inputFile;

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
				// 把转换过程分为四部分：构建源文件的 dom tree，处理内容节点，输出临时的 xml 文件，委托其它转换器对 xml 文件进行转换。
				monitor.beginTask("", 4);
				inputFile = params.get(Converter.ATTR_SOURCE_FILE);
				String catalogue = params.get(Converter.ATTR_CATALOGUE);

				// 检查是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("resx.cancel"));
				}
				monitor.subTask(Messages.getString("resx.Resx2Xliff.task2"));
				xmlResx = openXml(inputFile, catalogue);
				monitor.worked(1);

				Element root = xmlResx.getRootElement();
				List<Element> lstNodes = root.getChildren();
				// 处理各个内容节点
				IProgressMonitor subMonitor = Progress.getSubMonitor(monitor, 1);
				subMonitor.beginTask(Messages.getString("resx.Resx2Xliff.task3"), lstNodes.size());
				subMonitor.subTask("");
				for (int i = 0; i < lstNodes.size(); i++) {
					// 检查是否取消操作
					if (subMonitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("resx.cancel"));
					}
					Element node = lstNodes.get(i);
					if (node.getName().equals("data")) { //$NON-NLS-1$
						if (isTrans(node) && !isSkipCommand(node)) {
							List<Node> lstChilds = node.getContent();
							for (int j = 0; j < lstChilds.size(); j++) {
								Node childNode = lstChilds.get(j);
								if (childNode.getNodeType() == Node.ELEMENT_NODE) {
									Element eChild = new Element(childNode);
									if (eChild.getName().equals("value")) { //$NON-NLS-1$
										Element newChild = new Element("translate", xmlResx); //$NON-NLS-1$
										newChild.clone(eChild, xmlResx);
										lstChilds.set(j, newChild.getElement());
									}
								}
							}
							node.setContent(lstChilds);
						}
					}
					subMonitor.worked(1);
				}
				subMonitor.done();

				File tempFile = File.createTempFile("tempResx", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
				inputFile = tempFile.getAbsolutePath();

				// 输出 xliff 文件
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("resx.cancel"));
				}
				monitor.subTask(Messages.getString("resx.Resx2Xliff.task4"));
				saveXml(xmlResx, inputFile);
				monitor.worked(1);
				params.put(Converter.ATTR_SOURCE_FILE, inputFile);
				params.put(Converter.ATTR_IS_RESX, Converter.TRUE);
				result = dependantConverter.convert(params, Progress.getSubMonitor(monitor, 1));
				tempFile.delete();
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("resx.Resx2Xliff.msg1"), e);
			} finally {
				monitor.done();
			}
			return result;
		}

		/**
		 * Open xml.
		 * @param filename
		 *            the filename
		 * @param catalogue
		 *            the catalogue
		 * @return the document
		 * @throws Exception
		 *             the exception
		 */
		private Document openXml(String filename, String catalogue) throws Exception {
			SAXBuilder builder = new SAXBuilder();
			builder.setEntityResolver(new Catalogue(catalogue));
			return builder.build(filename);
		}

		// Save the xml to a file
		/**
		 * Save xml.
		 * @param xmlDoc
		 *            the xml doc
		 * @param xmlFile
		 *            the xml file
		 * @throws Exception
		 *             the exception
		 */
		private void saveXml(Document xmlDoc, String xmlFile) throws Exception {
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			FileOutputStream soutput = new FileOutputStream(xmlFile);
			outputter.output(xmlDoc, soutput);
			soutput.close();
		}
	}

	/*
	 * Each data row contains a name, and value. The row also contains a type or mimetype. Type corresponds to a .NET
	 * class that support text/value conversion. Classes that don't support this are serialized and stored with the
	 * mimetype set.
	 */
	/**
	 * Checks if is trans.
	 * @param node
	 *            the node
	 * @return true, if is trans
	 */
	private static boolean isTrans(Element node) {
		if (node.getAttribute("mimetype") != null) { //$NON-NLS-1$
			if (!node.getAttributeValue("mimetype").trim().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if is skip command.
	 * @param node
	 *            the node
	 * @return true, if is skip command
	 */
	private static boolean isSkipCommand(Element node) {
		// Search for the _skip command in comment tag
		List<Element> children = node.getChildren("comment"); //$NON-NLS-1$
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).getText().equalsIgnoreCase("_skip")) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}
}