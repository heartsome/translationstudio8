package net.heartsome.cat.convert.ui.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.convert.ui.resource.Messages;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;
import net.heartsome.xml.XMLOutputter;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

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
		LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger1"), start);
		IStatus result = null;
		monitor = Progress.getMonitor(monitor);
		monitor.beginTask(Messages.getString("model.ReverseConversionValidateWithLibrary3.task1"), 4);
		String xliffFile = path;
		try {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			long startTime = 0;
			if (LOGGER.isInfoEnabled()) {
				File temp = new File(xliffFile);
				if (temp.exists()) {
					LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger2"), temp.length());
				}
				startTime = System.currentTimeMillis();
				LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger3"), startTime);
			}

			// 验证所需要转换的 xliff 文件
			readXliff(xliffFile);
			monitor.worked(1);

			long endTime = 0;
			if (LOGGER.isInfoEnabled()) {
				endTime = System.currentTimeMillis();
				LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger4"), endTime);
				LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger5"), (endTime - startTime));
			}

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			if (LOGGER.isInfoEnabled()) {
				startTime = System.currentTimeMillis();
				LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger6"), startTime);
			}
			// 获取骨架文件路径
			skl = getSkeleton(path);
			monitor.worked(1);

			if (LOGGER.isInfoEnabled()) {
				endTime = System.currentTimeMillis();
				LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger7"), endTime);
				LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger8"), (endTime - startTime));
			}

			if (skl != null && !skl.equals("")) { //$NON-NLS-1$
				File sklFile = new File(skl);
				if (!sklFile.exists()) {
					return ValidationStatus.error(Messages.getString("model.ReverseConversionValidateWithLibrary3.msg1"));
				}
			}
			// 设置骨架文件的路径
			configuraion.setSkeleton(skl);

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			if (LOGGER.isInfoEnabled()) {
				startTime = System.currentTimeMillis();
				LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger9"), startTime);
			}

			createList(root);
			monitor.worked(1);

			if (LOGGER.isInfoEnabled()) {
				endTime = System.currentTimeMillis();
				LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger10"), endTime);
				LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger11"), (endTime - startTime));
			}

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			// TODO 确认为什么需要创建一个 xliff 文件副本，并需要确认什么时候删除此副本
			File tmpXLFFile = File.createTempFile("tmpXLF", ".hsxliff"); //$NON-NLS-1$ //$NON-NLS-2$
			reBuildXlf(xliffFile, tmpXLFFile);
			configuraion.setTmpXlfPath(tmpXLFFile.getAbsolutePath());

			// 设置文件类型
			configuraion.setFileType(dataType);
			result = Status.OK_STATUS;
			monitor.worked(1);
			long end = System.currentTimeMillis();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger12"), end);
				LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger13"), (end - start));
			}
		} catch (Exception e) {
			// TODO 需要针对不同的异常返回不能的提示信息
			String messagePattern = Messages.getString("model.ReverseConversionValidateWithLibrary3.msg2");
			String message = MessageFormat.format(messagePattern, new Object[] { xliffFile });
			LOGGER.error(message, e);
			result = ValidationStatus.error(Messages.getString("model.ReverseConversionValidateWithLibrary3.msg3"), e);
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
		targetLanguage = file.getAttributeValue("target-language", Messages.getString("model.ReverseConversionValidateWithLibrary3.msg4")); //$NON-NLS-1$ //$NON-NLS-2$
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
			LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger14"), startTime);
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
				tgt.setAttribute(Messages.getString("model.ReverseConversionValidateWithLibrary3.msg5"), targetLanguage); //$NON-NLS-1$
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
			LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger15"), endTime);
			LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger16"), (endTime - startTime));
		}

		XMLOutputter outputter = new XMLOutputter();
		outputter.preserveSpace(true);

		FileOutputStream out;
		out = new FileOutputStream(tmpXLFFile);

		if (LOGGER.isInfoEnabled()) {
			startTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger17"), startTime);
		}

		outputter.output(doc, out);

		if (LOGGER.isInfoEnabled()) {
			endTime = System.currentTimeMillis();
			LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger18"), endTime);
			LOGGER.info(Messages.getString("model.ReverseConversionValidateWithLibrary3.logger19"), (endTime - startTime));
		}

		out.close();
		outputter = null;
	}

	
	/**
	 * <div style='color:red;'>代替上面的 reBuildXlf 方法</div>
	 * 将要逆转换的 hsxliff 文件生成临时文件，再将这个临时文件进行处理，比如将分割的文本段拆分开来	robert	2012-11-28
	 * @param tmpXLFFile
	 */
	private void reBuildXlf(String xliffPath, File tmpXLFFile) {
		//先将所有合并的文本段进行恢复成原来的样子
		try {
			ResourceUtils.copyFile(new File(xliffPath), tmpXLFFile);
			VTDGen vg = new VTDGen();
			if (!vg.parseFile(xliffPath, true)) {
				LOGGER.error(MessageFormat.format("{0} parse error!", xliffPath));
				return;
			}
			VTDNav vn = vg.getNav();
	
			AutoPilot ap = new AutoPilot(vn);
			AutoPilot childAP = new AutoPilot(vn);
			ap.declareXPathNameSpace("hs", "http://www.heartsome.net.cn/2008/XLFExtension");
			childAP.declareXPathNameSpace("hs", "http://www.heartsome.net.cn/2008/XLFExtension");
			
			VTDUtils vu = new VTDUtils(vn);
			XMLModifier xm = new XMLModifier(vn);
			
			// 先找出所有的分割与合并信息，再依序列号从高到低依次分解，合并信息是<ph id="hs-merge0~1" splitMergeIndex="0"> 这种标记
			NavigableMap<Long, SegMergeInfoBean> infoMap = new TreeMap<Long, SegMergeInfoBean>();
			ap.selectXPath("/xliff/file/body/descendant::node()" +
					"[(name()='group' and @ts='hs-split') or (name()='ph' and contains(@id, 'hs-merge'))]");
			int idx = -1;
			while(ap.evalXPath() != -1){
				String nodeName = vn.toString(vn.getCurrentIndex());
				long index = -1;
				if ((idx = vn.getAttrVal("splitMergeIndex")) != -1) {
					index = Long.parseLong(vn.toString(idx));
				}
				boolean isMerge = false;
				// 如果是 ph 节点，那么这个就是合并信息
				if ("ph".equals(nodeName)) {
					isMerge = true;
					String phFrag = vu.getElementFragment();
					String phID = vn.toString(vn.getAttrVal("id"));
					String[] tuIds = vn.toString(vn.getAttrVal("id")).replace("hs-merge", "").split("~");
					String mergeFirstId = tuIds[0].trim();
					String mergeSecondId = tuIds[1].trim();
					System.out.println("mergeFirstId = " + mergeFirstId);
					System.out.println("mergeSecondId = " + mergeSecondId);
					infoMap.put(index, new SegMergeInfoBean(isMerge, phFrag, phID, mergeFirstId, mergeSecondId));
				}else {
					infoMap.put(index, new SegMergeInfoBean(isMerge));
				}
			}

			for(Entry<Long, SegMergeInfoBean> entry :  infoMap.descendingMap().entrySet()){
				Long index = entry.getKey();
				SegMergeInfoBean bean = entry.getValue();
				
				if (bean.isMerge()) {
					resetMerge(ap, vn, vu, xm, index, bean);
				}else {
					resetSplit(ap, childAP, vn, vu, xm, index);
				}
				vn = xm.outputAndReparse();
				xm.bind(vn);
				ap.bind(vn);
				childAP.bind(vn);
				vu.bind(vn);
			}
			xm.output(tmpXLFFile.getAbsolutePath());
		}catch (Exception e) {
			e.printStackTrace();
		}
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
	private String getSkeleton(String xlfPath) throws IOException {
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
					IFile xlfIfile = ConverterUtil.localPath2IFile(xlfPath);
					if (external != null) {
						result = external.getAttributeValue("href"); //$NON-NLS-1$
						result = result.replaceAll("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&lt;", "<"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&gt;", ">"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&apos;", "\'"); //$NON-NLS-1$ //$NON-NLS-2$
						result = result.replaceAll("&quot;", "\""); //$NON-NLS-1$ //$NON-NLS-2$
						
						result = xlfIfile.getProject().getLocation().toOSString()+result;
					} else {
						Element internal = mskl.getChild("internal-file"); //$NON-NLS-1$
						if (internal != null) {
							File tmp = File.createTempFile("internal", ".skl", new File(xlfIfile.getProject().getWorkspace().getRoot().getLocation().toOSString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
									out.write(cdataString.getBytes());
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
	
	private void resetMerge(AutoPilot ap, VTDNav vn, VTDUtils vu, XMLModifier xm, Long index, SegMergeInfoBean bean) throws Exception{
		String phFrag = bean.getPhFrag();
		String phID = bean.getPhID();
		String firstId = bean.getMergeFirstId();
		String secondId = bean.getMergeSecondId();
		
		String xpath = "/xliff/file/body/descendant::trans-unit/source[ph[@id='" + phID + "' and @splitMergeIndex='" + index + "']]";
		ap.selectXPath(xpath);
		if (ap.evalXPath() != -1) {
			String srcHeader = vu.getElementHead();
			String srcContent = vu.getElementContent();
			
			String curContent = srcContent.substring(0, srcContent.indexOf(phFrag));
			String otherSrcContent = srcContent.substring(srcContent.indexOf(phFrag) + phFrag.length(), srcContent.length());
			String curSrcFrag = srcHeader + curContent + "</source>";
			xm.remove();
			xm.insertAfterElement(curSrcFrag.getBytes("UTF-8"));
			
			setDataToOtherTU(vn, xm, true, secondId, otherSrcContent);
			
			System.out.println("-------------");
			System.out.println(curContent);
			System.out.println(otherSrcContent);
		}
		vn.pop();
		
		//处理译文
		xpath = "/xliff/file/body/descendant::trans-unit/target[ph[@id='" + phID + "' and @splitMergeIndex='" + index + "']]";
		ap.selectXPath(xpath);
		if (ap.evalXPath() != -1) {
			String tgtHeader = vu.getElementHead();
			String tgtContent = vu.getElementContent();
			
			//处理合并标识符在 g 标记内的情况，而源文中未处理的主要原因是因为在源文中，合并标识符是自动生成，且用户不能调动其位置，故而未做处理。
			int spltOffset = tgtContent.indexOf(phFrag);
			List<Map<String, String>> tagLocationList = getTagLocation(vn, tgtContent);
			String srcAddStr1 = "";
			String srcAddStr2 = "";
			for (Map<String, String> map : tagLocationList) {
				String tagHeader = map.get("tagHeader");
				String tagTail = map.get("tagTail");
				int headerIdx = Integer.parseInt(map.get("headerIdx"));
				int tailIdx = Integer.parseInt(map.get("tailIdx"));
				if (headerIdx < spltOffset && spltOffset <= tailIdx) {
					srcAddStr1 = tagTail + srcAddStr1;
					srcAddStr2 += tagHeader;
				}
			}
			
			String tgtFragment1 = tgtHeader + tgtContent.substring(0, spltOffset) + srcAddStr1 + "</target>";
			
			String otherTgtContent =  srcAddStr2 + tgtContent.substring(spltOffset + phFrag.length());
			xm.remove();
			xm.insertAfterElement(tgtFragment1.getBytes("UTF-8"));
			
			setDataToOtherTU(vn, xm, false, secondId, otherTgtContent);
			
		}
		vn.pop();
	}
	
	private void resetSplit(AutoPilot ap, AutoPilot childAP, VTDNav vn, VTDUtils vu, XMLModifier xm, Long index) throws Exception {
		String xpath = "/xliff/file/body/descendant::group[not(descendant::node()[name()='group']) and @ts='hs-split' and @splitMergeIndex='" + index + "']";
		ap.selectXPath(xpath);
		if (ap.evalXPath() != -1) {
			String id = vn.toString(vn.getAttrVal("id"));

			StringBuffer newSrcFragSB = new StringBuffer();
			StringBuffer newTgtFragSB = new StringBuffer();
			
			// 先组装源文
			vn.push();
			boolean addHeader = false;
			childAP.selectXPath("./trans-unit/source");
			while(childAP.evalXPath() != -1){
				if (!addHeader) {
					newSrcFragSB.append(vu.getElementHead() == null ? "" : vu.getElementHead());
					addHeader = true;
				}
				newSrcFragSB.append(vu.getElementContent() == null ? "" : vu.getElementContent());
			}
			if (newSrcFragSB.length() == 0) {
				newSrcFragSB = new StringBuffer("<source/>");
			}else {
				newSrcFragSB.append("</source>");
			}
			vn.pop();

			// 再组装译文
			vn.push();
			addHeader = false;
			childAP.selectXPath("./trans-unit/target");
			while(childAP.evalXPath() != -1){
				if (!addHeader) {
					newTgtFragSB.append(vu.getElementHead() == null ? "" : vu.getElementHead());
					addHeader = true;
				}
				newTgtFragSB.append(vu.getElementContent() == null ? "" : vu.getElementContent());
			}
			if (newTgtFragSB.length() == 0) {
				newTgtFragSB = new StringBuffer("<target/>");
			}else {
				newTgtFragSB.append("</target>");
			}
			vn.pop();
			
			StringBuffer newTUFrag = new StringBuffer();
			newTUFrag.append("<trans-unit id=\"" + id + "\" xml:space=\"preserve\">");
			newTUFrag.append(newSrcFragSB);
			newTUFrag.append(newTgtFragSB);
			newTUFrag.append("</trans-unit>");
			
			xm.remove();
			xm.insertAfterElement(newTUFrag.toString().getBytes("UTF-8"));
		}
	}
	
	/**
	 * 向被合并的空的 tu 中填充值
	 * @param vn
	 * @param xm
	 * @param isSrc
	 * @param secondId
	 * @param otherContent
	 * @throws Exception
	 */
	private void setDataToOtherTU(VTDNav vn, XMLModifier xm, boolean isSrc, String secondId, String otherContent) throws Exception {
		if (otherContent == null || otherContent.length() <= 0) {
			return;
		}
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		String xpath = "/xliff/file/body/descendant::trans-unit[@id='" + secondId + "']" 
			+ (isSrc ? "/source" : "/target");
		ap.selectXPath(xpath); 
		if (ap.evalXPath() != -1) {
			xm.insertAfterHead(otherContent.getBytes("UTF-8"));
		}
		vn.pop();
	}
	
	
	/**
	 * <div style='color:red'>备注：该方法是从 XLFHandler 类中拷贝的。</div>
	 * 获取每个标记 header 与 tail 在文本中的 index，此方法主要针对文本段分割，分割点在g、mrk标记里面。robert	2012-11-15
	 * @param vn
	 */
	private List<Map<String, String>> getTagLocation(VTDNav vn, String srcContent){
		List<Map<String, String>> tagLoctionList = new LinkedList<Map<String,String>>();
		
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		String xpath = "./descendant::node()";
		
		try {
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath(xpath);
			int lastIdx = 0;
			while(ap.evalXPath() != -1){
				Map<String, String> tagLocationMap = new HashMap<String, String>();
				
				String tagName = vn.toString(vn.getCurrentIndex());
				if (!("g".equals(tagName) || "mrk".equals(tagName))) {
					continue;
				}
				String tagHeader = vu.getElementHead();
				String tagTail = vu.getElementFragment().replace(tagHeader, "").replace(vu.getElementContent(), "");
				int headerIdx = srcContent.indexOf(tagHeader, lastIdx);
				int tailIdx = headerIdx + tagHeader.length() + vu.getElementContent().length();
				lastIdx = headerIdx;
				
				tagLocationMap.put("tagHeader", tagHeader);
				tagLocationMap.put("tagTail", tagTail);
				tagLocationMap.put("headerIdx", "" + headerIdx);
				tagLocationMap.put("tailIdx", "" + tailIdx);
				
				tagLoctionList.add(tagLocationMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		vn.pop();
		return tagLoctionList;
	}
}
