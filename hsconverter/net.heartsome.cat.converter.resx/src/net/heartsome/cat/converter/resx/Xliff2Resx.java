/**
 * Xliff2Resx.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.resx;

/**
 * @author Pablo
 *
 */

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
import net.heartsome.cat.converter.util.ReverseConversionInfoLogRecord;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * The Class Xliff2Resx.
 * @author John Zhu
 */
public class Xliff2Resx implements Converter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Xliff2Resx.class);

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "resx";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("resx.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to ResX Conveter";

	// 内部实现所依赖的转换器
	/** The dependant converter. */
	private Converter dependantConverter;

	/**
	 * for test to initialize depend on converter.
	 */
	public Xliff2Resx() {
		dependantConverter = Activator.getXMLConverter(Converter.DIRECTION_REVERSE);
	}

	/**
	 * 运行时把所依赖的转换器，在初始化的时候通过构造函数注入。.
	 * @param converter
	 *            the converter
	 */
	public Xliff2Resx(Converter converter) {
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
		Xliff2ResxImpl converter = new Xliff2ResxImpl();
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
	 * The Class Xliff2ResxImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2ResxImpl {

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
			ReverseConversionInfoLogRecord infoLogger = ConverterUtils.getReverseConversionInfoLogRecord();
			infoLogger.startConversion();
			Map<String, String> result = new HashMap<String, String>();
			try {
				// 把转换过程分为四大部分共 10 个任务，其中委派其它转换器进行转换占 5，读中间 xliff 文件占 2，处理所读取的 xliff 文件占 1，写 xliff 文件占 2。
				monitor.beginTask("", 10);
				// 委派进行转换
				result = dependantConverter.convert(params, Progress.getSubMonitor(monitor, 5));
				inputFile = params.get(Converter.ATTR_TARGET_FILE);
				if (inputFile == null || "".equals(inputFile)) {
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
							Messages.getString("resx.Xliff2Resx.msg1"));
				}

				File tgtFile = new File(inputFile);
				if (!tgtFile.exists()) {
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
							Messages.getString("resx.Xliff2Resx.msg1"));
				}

				String catalogue = params.get(Converter.ATTR_CATALOGUE);

				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("resx.cancel"));
				}
				monitor.subTask(Messages.getString("resx.Xliff2Resx.task2"));
				long startTime = 0;
				if (LOGGER.isInfoEnabled()) {
					startTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("resx.Xliff2Resx.logger1"), startTime);
				}
				xmlResx = openXml(inputFile, catalogue);
				long endTime = 0;
				if (LOGGER.isInfoEnabled()) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("resx.Xliff2Resx.logger2"), endTime);
					LOGGER.info(Messages.getString("resx.Xliff2Resx.logger3"), endTime - startTime);
				}
				monitor.worked(2);

				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("resx.cancel"));
				}
				monitor.subTask(Messages.getString("resx.Xliff2Resx.task3"));
				if (LOGGER.isInfoEnabled()) {
					startTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("resx.Xliff2Resx.logger4"), startTime);
				}
				Element root = xmlResx.getRootElement();
				List<Element> lstNodes = root.getChildren();
				for (int i = 0; i < lstNodes.size(); i++) {
					Element node = lstNodes.get(i);
					List<Node> lstChilds = node.getContent();
					for (int j = 0; j < lstChilds.size(); j++) {
						if (lstChilds.get(j).getNodeType() == Node.ELEMENT_NODE) {
							Element child = new Element(lstChilds.get(j));
							if (isConvNode(child)) {
								Element newChild = new Element("value", xmlResx); //$NON-NLS-1$
								newChild.clone(child, xmlResx);
								lstChilds.set(j, newChild.getElement());
							}
						}
					}
					node.setContent(lstChilds);
				}
				if (LOGGER.isInfoEnabled()) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("resx.Xliff2Resx.logger5"), endTime);
					LOGGER.info(Messages.getString("resx.Xliff2Resx.logger6"), endTime - startTime);
				}
				monitor.worked(1);

				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("resx.cancel"));
				}
				monitor.subTask(Messages.getString("resx.Xliff2Resx.task4"));
				if (LOGGER.isInfoEnabled()) {
					startTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("resx.Xliff2Resx.logger7"), startTime);
				}
				saveXml(xmlResx, inputFile);
				if (LOGGER.isInfoEnabled()) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("resx.Xliff2Resx.logger8"), endTime);
					LOGGER.info(Messages.getString("resx.Xliff2Resx.logger9"), endTime - startTime);
				}
				monitor.worked(2);
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("resx.Xliff2Resx.msg1"),
						e);
			} finally {
				monitor.done();
			}
			infoLogger.endConversion();
			return result;
		}
	}

	/**
	 * Checks if is conv node.
	 * @param node
	 *            the node
	 * @return true, if is conv node
	 */
	private static boolean isConvNode(Element node) {
		return node.getName().equals("translate"); //$NON-NLS-1$
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
	private static Document openXml(String filename, String catalogue) throws Exception {
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
	private static void saveXml(Document xmlDoc, String xmlFile) throws Exception {
		XMLOutputter outputter = new XMLOutputter();
		outputter.preserveSpace(true);
		FileOutputStream soutput = new FileOutputStream(xmlFile);
		outputter.output(xmlDoc, soutput);
		soutput.close();
	}

}
