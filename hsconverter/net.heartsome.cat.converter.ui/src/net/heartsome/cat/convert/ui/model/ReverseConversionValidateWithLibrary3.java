package net.heartsome.cat.convert.ui.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.cat.convert.ui.wizard.Messages;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * 使用 heartsome library 3 对文件的逆转换进行验证
 * @author cheney
 * @since JDK1.6
 */
public class ReverseConversionValidateWithLibrary3 implements ConversionValidateStrategy {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReverseConversionValidateWithLibrary3.class);

	private SAXBuilder builder;
	private Document doc;
	private Element root;
	private List<Element> segments = new Vector<Element>();
	// 从 xliff 文件的 file 节点获取目标语言
	private String targetLanguage;
	// 文件类型
	private String dataType;

	// 骨架文件路径
	private String skl;

	public IStatus validate(String path, ConversionConfigBean configuraion, IProgressMonitor monitor) {
		long start = System.currentTimeMillis();
		LOGGER.info("开始分析 xliff 文件，当前时间为：{}", start);
		IStatus result = null;
		monitor = Progress.getMonitor(monitor);
		monitor.beginTask("正在分析 xliff 文件...", 4);
		String xliffFile = path;
		try {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			long startTime = 0;
			if (LOGGER.isInfoEnabled()) {
				File temp = new File(xliffFile);
				if (temp.exists()) {
					LOGGER.info("分析的 xliff 文件大小为（bytes）：{}", temp.length());
				}
				startTime = System.currentTimeMillis();
				LOGGER.info("开始加载 xliff 文件，当前时间为：{}", startTime);
			}

			// 验证所需要转换的 xliff 文件
			readXliff(xliffFile);
			monitor.worked(1);

			long endTime = 0;
			if (LOGGER.isInfoEnabled()) {
				endTime = System.currentTimeMillis();
				LOGGER.info("加载 xliff 文件结束，当前时间为：{}", endTime);
				LOGGER.info("加载 xliff 用时：{}", (endTime - startTime));
			}

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			if (LOGGER.isInfoEnabled()) {
				startTime = System.currentTimeMillis();
				LOGGER.info("开始处理获得 skeleton 文件的逻辑，当前时间为：{}", startTime);
			}
			// 获取骨架文件路径
			skl = getSkeleton();
			monitor.worked(1);

			if (LOGGER.isInfoEnabled()) {
				endTime = System.currentTimeMillis();
				LOGGER.info("获得 skeleton 文件结束，当前时间为：{}", endTime);
				LOGGER.info("获得 skeleton 文件用时：{}", (endTime - startTime));
			}

			if (skl != null && !skl.equals("")) { //$NON-NLS-1$
				File sklFile = new File(skl);
				if (!sklFile.exists()) {
					return ValidationStatus.error("xliff 文件对应的骨架文件不存在。");
				}
			}
			// 设置骨架文件的路径
			configuraion.setSkeleton(skl);

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			if (LOGGER.isInfoEnabled()) {
				startTime = System.currentTimeMillis();
				LOGGER.info("开始创建 segments 列表，当前时间为：{}", startTime);
			}

			createList(root);
			monitor.worked(1);

			if (LOGGER.isInfoEnabled()) {
				endTime = System.currentTimeMillis();
				LOGGER.info("创建 segments 列表结束，当前时间为：{}", endTime);
				LOGGER.info("创建 segments 列表用时：{}", (endTime - startTime));
			}

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			// TODO 确认为什么需要创建一个 xliff 文件副本，并需要确认什么时候删除此副本
			File tmpXLFFile = File.createTempFile("tmpXLF", ".xlf"); //$NON-NLS-1$ //$NON-NLS-2$
			reBuildXlf(tmpXLFFile);

			// 设置文件类型
			configuraion.setFileType(dataType);
			result = Status.OK_STATUS;
			monitor.worked(1);
			long end = System.currentTimeMillis();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("分析 xliff 文件结束，当前时间为：{}", end);
				LOGGER.info("分析 xliff 文件总用时：{}", (end - start));
			}
		} catch (Exception e) {
			// TODO 需要针对不同的异常返回不能的提示信息
			String messagePattern = "分析{0}失败。";
			String message = MessageFormat.format(messagePattern, new Object[] { xliffFile });
			LOGGER.error(message, e);
			result = ValidationStatus.error("分析 xliff 文件失败。", e);
		} finally {
			if (segments != null) {
				segments = null;
			}
			if (root != null) {
				root = null;
			}
			if (doc != null) {
				doc = null;
			}
			if (builder != null) {
				builder = null;
			}
			monitor.done();
		}

		return result;
	}

	/**
	 * @param xliff
	 *             xliff 文件的路径
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	private void readXliff(String xliff) throws SAXException, IOException, ParserConfigurationException {
		builder = new SAXBuilder();
		builder.setEntityResolver(new Catalogue(ConverterContext.catalogue));
		doc = builder.build(xliff);
		root = doc.getRootElement();
		Element file = root.getChild("file"); //$NON-NLS-1$
		dataType = file.getAttributeValue("datatype"); //$NON-NLS-1$
		targetLanguage = file.getAttributeValue("target-language", Messages.getString("ReverseConversionValidateWithLibrary3.0")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * 构建 xliff 文件副本
	 * @param tmpXLFFile
	 * @throws IOException
	 *             ;
	 */
	private void reBuildXlf(File tmpXLFFile) throws IOException {
		long startTime = 0;
		if (LOGGER.isInfoEnabled()) {
			startTime = System.currentTimeMillis();
			LOGGER.info("在重写 xliff 文件的过程中，对 segments 列表的处理开始,当前时间为：{}", startTime);
		}
		for (int i = 0, size = segments.size() - 1; i < size; i++) {
			Element e = segments.get(i);
			Element src = e.getChild("source"); //$NON-NLS-1$
			Element tgt = e.getChild("target"); //$NON-NLS-1$

			boolean isApproved = e.getAttributeValue("approved", "no").equalsIgnoreCase("yes"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			List<Node> srcList = src.getContent();
			Vector<Node> tmp = new Vector<Node>();
			for (int j = 0, jSize = srcList.size(); j < jSize; j++) {
				Node o = srcList.get(j);
				if (o.getNodeType() == Node.ELEMENT_NODE && o.getNodeName().equals("ph")) { //$NON-NLS-1$
					Element el = new Element(o);
					if (el.getAttributeValue("id", "").startsWith("hs-merge")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						String tmpMergeId = el.getAttributeValue("id", "").substring(8); //$NON-NLS-1$ //$NON-NLS-2$
						String[] pairId = tmpMergeId.split("~"); //$NON-NLS-1$
						srcList.remove(j);
						j--;
						jSize--;

						int idIndex = pairId[0].indexOf("-"); //$NON-NLS-1$
						if (idIndex != -1) {
							pairId[0] = pairId[0].substring(0, idIndex);
						}

						idIndex = pairId[1].indexOf("-"); //$NON-NLS-1$
						if (idIndex != -1) {
							pairId[1] = pairId[1].substring(0, idIndex);
						}

						if (!pairId[0].equals(pairId[1])) {
							pairId = null;
							break;
						}
						pairId = null;
					} else {
						srcList.remove(j);
						j--;
						jSize--;
						tmp.add(o);
					}
				} else {
					srcList.remove(j);
					j--;
					jSize--;
					tmp.add(o);
				}
			}

			src.removeAllChildren();
			src.setContent(tmp);
			tmp = null;

			if (tgt == null) {
				tgt = new Element("target", doc); //$NON-NLS-1$
				tgt.setAttribute(Messages.getString("ReverseConversionValidateWithLibrary3.1"), targetLanguage); //$NON-NLS-1$
				tgt.setAttribute("state", "new"); //$NON-NLS-1$ //$NON-NLS-2$
				List<Element> content = e.getChildren();
				Vector<Element> newContent = new Vector<Element>();
				for (int m = 0; m < content.size(); m++) {
					Element tmpEl = content.get(m);
					newContent.add(tmpEl);
					if (tmpEl.getName().equals("source")) { //$NON-NLS-1$
						newContent.add(tgt);
					}
					tmpEl = null;
				}
				e.setContent(newContent);
				newContent = null;
				content = null;
			}

			List<Node> tgtList = tgt.getContent();
			tmp = new Vector<Node>();

			for (int j = 0, jSize = tgtList.size(); j < jSize; j++) {
				Node o = tgtList.get(j);
				if (o.getNodeType() == Node.ELEMENT_NODE && o.getNodeName().equals("ph")) { //$NON-NLS-1$
					Element el = new Element(o);
					if (el.getAttributeValue("id", "").startsWith("hs-merge")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						String tmpMergeId = el.getAttributeValue("id", "").substring(8); //$NON-NLS-1$ //$NON-NLS-2$
						String[] pairId = tmpMergeId.split("~"); //$NON-NLS-1$
						tgtList.remove(j);
						j--;
						jSize--;
						int idIndex = pairId[0].indexOf("-"); //$NON-NLS-1$
						if (idIndex != -1) {
							pairId[0] = pairId[0].substring(0, idIndex);
						}

						idIndex = pairId[1].indexOf("-"); //$NON-NLS-1$
						if (idIndex != -1) {
							pairId[1] = pairId[1].substring(0, idIndex);
						}
						if (!pairId[0].equals(pairId[1])) {
							pairId = null;
							break;
						}
						pairId = null;
					} else {
						tgtList.remove(j);
						j--;
						jSize--;
						tmp.add(o);
					}
					el = null;
				} else {
					tgtList.remove(j);
					j--;
					jSize--;
					tmp.add(o);
				}
			}

			tgt.removeAllChildren();
			tgt.setContent(tmp);
			tmp = null;

			Element nextEl = segments.get(i + 1);
			if (!isApproved && srcList.size() > 0) {
				nextEl.setAttribute("approved", "no"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			Element nextSrc = nextEl.getChild("source"); //$NON-NLS-1$
			Element nextTgt = nextEl.getChild("target"); //$NON-NLS-1$

			if (nextTgt == null) {
				nextTgt = new Element("target", doc); //$NON-NLS-1$
				nextTgt.setAttribute("xml:lang", targetLanguage); //$NON-NLS-1$
				nextTgt.setAttribute("state", "new"); //$NON-NLS-1$ //$NON-NLS-2$
				List<Element> content = nextEl.getChildren();
				Vector<Element> newContent = new Vector<Element>();
				for (int m = 0; m < content.size(); m++) {
					Element tmpEl = content.get(m);
					newContent.add(tmpEl);
					if (tmpEl.getName().equals("source")) { //$NON-NLS-1$
						newContent.add(nextTgt);
					}
					tmpEl = null;
				}
				nextEl.setContent(newContent);
				newContent = null;
				content = null;
			}

			List<Node> nextSrcContent = nextSrc.getContent();
			List<Node> nextTgtContent = nextTgt.getContent();

			nextSrc.removeAllChildren();
			Vector<Node> newNextSrcContent = new Vector<Node>();
			newNextSrcContent.addAll(srcList);
			for (int j = 0, jSize = nextSrcContent.size(); j < jSize; j++) {
				newNextSrcContent.add(nextSrcContent.get(j));
			}
			nextSrc.setContent(newNextSrcContent);
			newNextSrcContent = null;

			nextTgt.removeAllChildren();
			Vector<Node> newNextTgtContent = new Vector<Node>();
			newNextTgtContent.addAll(tgtList);
			for (int j = 0, jSize = nextTgtContent.size(); j < jSize; j++) {
				newNextTgtContent.add(nextTgtContent.get(j));
			}
			nextTgt.setContent(newNextTgtContent);
			newNextTgtContent = null;
		}

		long endTime = 0;
		if (LOGGER.isInfoEnabled()) {
			endTime = System.currentTimeMillis();
			LOGGER.info("在重写 xliff 文件的过程中，对 segments 列表的处理结束,当前时间为：{}", endTime);
			LOGGER.info("在重写 xliff 文件的过程中，对 segments 列表的处理用时：{}", (endTime - startTime));
		}

		XMLOutputter outputter = new XMLOutputter();
		outputter.preserveSpace(true);

		FileOutputStream out;
		out = new FileOutputStream(tmpXLFFile);

		if (LOGGER.isInfoEnabled()) {
			startTime = System.currentTimeMillis();
			LOGGER.info("在重写 xliff 文件的过程中，开始调用 XMLOutputter 的 output 方法，当前时间为：{}", startTime);
		}

		outputter.output(doc, out);

		if (LOGGER.isInfoEnabled()) {
			endTime = System.currentTimeMillis();
			LOGGER.info("在重写 xliff 文件的这程中，调用 XMLOutputter 的 output 方法结束，当前时间为：{}", endTime);
			LOGGER.info("在重写 xliff 文件的这程中，调用 XMLOutputter 的 output 方法用时：{}", (endTime - startTime));
		}

		out.close();
		outputter = null;
	}

	/**
	 * 创建翻译节点列表
	 * @param rootPara
	 *            ;
	 */
	private void createList(Element rootPara) {
		List<Element> files = rootPara.getChildren();
		Iterator<Element> it = files.iterator();
		while (it.hasNext()) {
			Element el = it.next();
			if (el.getName().equals("trans-unit")) { //$NON-NLS-1$
				segments.add(el);
			} else {
				createList(el);
			}
		}
	}

	/**
	 * 获取骨架文件
	 * @return 骨架文件路径
	 * @throws IOException
	 *             在读取骨架文件失败时抛出 IO 异常 ;
	 */
	private String getSkeleton() throws IOException {
		String result = ""; //$NON-NLS-1$
		Element file = root.getChild("file"); //$NON-NLS-1$
		Element header = null;
		String encoding = "";

		if (file != null) {
			header = file.getChild("header"); //$NON-NLS-1$
			if (header != null) {

				// 添加源文件编码的读取
				List<Element> propGroups = header.getChildren("hs:prop-group"); //$NON-NLS-1$
				for (int i = 0; i < propGroups.size(); i++) {
					Element prop = propGroups.get(i);
					if (prop.getAttributeValue("name").equals("encoding")) { //$NON-NLS-1$ //$NON-NLS-2$
						encoding = prop.getText().trim();
						break;
					}
				}
				if (encoding.equals("utf-8")) { //$NON-NLS-1$
					encoding = "UTF-8"; //$NON-NLS-1$
				}

				Element mskl = header.getChild("skl"); //$NON-NLS-1$
				if (mskl != null) {
					Element external = mskl.getChild("external-file"); //$NON-NLS-1$
					if (external != null) {
						result = external.getAttributeValue("href"); //$NON-NLS-1$
						result = result.replaceAll("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&lt;", "<"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&gt;", ">"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&apos;", "\'"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&quot;", "\""); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						Element internal = mskl.getChild("internal-file"); //$NON-NLS-1$
						if (internal != null) {
							File tmp = File.createTempFile("internal", ".skl", new File("skl")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							tmp.deleteOnExit();
							FileOutputStream out = new FileOutputStream(tmp);
							List<Node> content = internal.getContent();
							for (int i = 0; i < content.size(); i++) {
								Node n = content.get(i);
								if (n.getNodeType() == Node.TEXT_NODE) {
									out.write(n.getNodeValue().getBytes(encoding));
								} else if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
									// fixed bub 515 by john.
									String cdataString = n.getNodeValue();
									if (cdataString.endsWith("]]")) { //$NON-NLS-1$
										cdataString += ">"; //$NON-NLS-1$
									}
									out.write(cdataString.getBytes(encoding));
								}
							}
							out.close();
							return tmp.getAbsolutePath();
						}
						return result;
					}
					external = null;
					mskl = null;
				} else {
					return result;
				}
			} else {
				return result;
			}
		} else {
			return result;
		}

		if (encoding != null) {
			if (encoding.equals("")) { //$NON-NLS-1$
				List<Element> groups = header.getChildren("hs:prop-group"); //$NON-NLS-1$
				for (int i = 0; i < groups.size(); i++) {
					Element group = groups.get(i);
					List<Element> props = group.getChildren("hs:prop"); //$NON-NLS-1$
					for (int k = 0; k < props.size(); k++) {
						Element prop = props.get(k);
						if (prop.getAttributeValue("prop-type", "").equals("encoding")) { //$NON-NLS-1$
							encoding = prop.getText();
						}
					}
				}
			}
		}
		header = null;
		file = null;
		return result;
	}
}
