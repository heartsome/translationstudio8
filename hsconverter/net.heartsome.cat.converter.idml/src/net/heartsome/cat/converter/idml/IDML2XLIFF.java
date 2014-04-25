package net.heartsome.cat.converter.idml;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.heartsome.cat.common.file.FileManager;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.idml.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * IDML 转换为 XLIFF
 * @author peason
 * @version
 * @since JDK1.6
 */
public class IDML2XLIFF implements Converter {

	private static final Logger LOGGER = LoggerFactory.getLogger(IDML2XLIFF.class);

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "idml";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("idml.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "IDML to XLIFF Conveter";

	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		IDML2XLIFFImpl impl = new IDML2XLIFFImpl();
		return impl.run(args, monitor);
	}

	public String getName() {
		return NAME_VALUE;
	}

	public String getType() {
		return TYPE_VALUE;
	}

	public String getTypeName() {
		return TYPE_NAME_VALUE;
	}

	/**
	 * IDML 转换为 XLIFF 的实现类
	 * @author peason
	 * @version
	 * @since JDK1.6
	 */
	class IDML2XLIFFImpl {

		/** IDML 中 Story 文件的前缀 */
		private static final String IDML_PREFIX = "idPkg";

		/** IDML 中 Story 文件的命名空间 */
		private static final String IDML_NAMESPACE = "http://ns.adobe.com/AdobeInDesign/idml/1.0/packaging";

		/** 源文件路径 */
		private String strSrcPath;

		/** 骨架文件路径 */
		private String strSklPath;

		/** XLIFF 文件路径 */
		private String strXLIFFPath;

		private boolean blnIsSuite;

		private String strQtToolID;

		/** 源语言 */
		private String strSrcLang;

		/** 目标语言 */
		private String strTgtLang;

		/** Catalogue 路径 */
		private String strCatalogue;

		/** 分段规则文件路径 */
		private String strSrx;

		/** 编码 */
		private String strSrcEncoding;

		private boolean blnSegByElement;

		private StringSegmenter segmenter;

		/** 存放 Spread 的名称，按实际显示页的顺序 */
		private LinkedList<String> lstSpread = new LinkedList<String>();

		/** 存放 MasterSpread 的名称，按 designmap.xml 中指定的顺序 */
		private LinkedList<String> lstMasterSpread = new LinkedList<String>();

		/** designmap.xml 中指定的 story 文件的集合 */
		private ArrayList<String> lstAllStory = new ArrayList<String>();

		/** Story 文件的集合，已排序 */
		private LinkedList<String> lstStoryBySort = new LinkedList<String>();

		/** 生成 XLIFF 文件的输出流 */
		private FileOutputStream out;

		/** 生成骨架文件的输出流 */
		private ZipOutputStream zipSklOut;

		private FileManager fileManager = new FileManager();

		/** 临时解压目录（将 IDML 文件先解压到此目录） */
		private String strTmpFolderPath;

		/** 文本段 ID 值 */
		private int segNum = 0;

		/**
		 * 转换器的处理顺序如下：<br/>
		 * 1. 解压源文件到临时目录。<br/>
		 * 2. 将临时目录中除 Stories 文件夹外的其他文件及目录添加到骨架文件。<br/>
		 * 3. 解析 designmap.xml 文件，获取 Spread 文件的顺序及需要处理的 Story 文件<br/>
		 * 4. 按顺序解析 Spread 文件，从中确定 Story 文件的顺序 <br/>
		 * 5. 对第 4 步中确定的 Story 文件逐一解析，如果 Story 文件中无 Content 节点，则直接放入骨架文件；<br/>
		 * 如果有 Content 节点，则对该 Story 文件添加骨架信息后，文件名添加 .skl 后缀并放入骨架文件 <br/>
		 * 6. 删除临时解压目录 <br/>
		 * @param args
		 * @param monitor
		 * @return
		 * @throws ConverterException
		 *             ;
		 */
		public Map<String, String> run(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			monitor.beginTask("", 13);
			Map<String, String> result = new HashMap<String, String>();
			// 转换过程分为 11 部分，releaseSrcZip 占 1，createZip 占 1，initSpreadAllStory 占 1， parseSpreadFile 占2，
			// parseStoryFile 占 5，deleteFileOrFolder 占 1
			IProgressMonitor firstPartMonitor = Progress.getSubMonitor(monitor, 1);
			firstPartMonitor.beginTask(Messages.getString("idml.IDML2XLIFF.task2"), 1);
			firstPartMonitor.subTask("");
			strSrcPath = args.get(Converter.ATTR_SOURCE_FILE);
			strXLIFFPath = args.get(Converter.ATTR_XLIFF_FILE);
			strSklPath = args.get(Converter.ATTR_SKELETON_FILE);
			blnIsSuite = Converter.TRUE.equals(args.get(Converter.ATTR_IS_SUITE));
			strQtToolID = args.get(Converter.ATTR_QT_TOOLID) != null ? args.get(Converter.ATTR_QT_TOOLID)
					: Converter.QT_TOOLID_DEFAULT_VALUE;

			strSrcLang = args.get(Converter.ATTR_SOURCE_LANGUAGE);
			strTgtLang = args.get(Converter.ATTR_TARGET_LANGUAGE);
			strCatalogue = args.get(Converter.ATTR_CATALOGUE);
			String elementSegmentation = args.get(Converter.ATTR_SEG_BY_ELEMENT);
			strSrx = args.get(Converter.ATTR_SRX);
			strSrcEncoding = args.get(Converter.ATTR_SOURCE_ENCODING);
			if (elementSegmentation == null) {
				blnSegByElement = false;
			} else {
				blnSegByElement = elementSegmentation.equals(Converter.TRUE);
			}

			try {
				out = new FileOutputStream(strXLIFFPath);
				zipSklOut = new ZipOutputStream(new FileOutputStream(strSklPath));
				writeHeader();
				if (!blnSegByElement) {
					segmenter = new StringSegmenter(strSrx, strSrcLang, strCatalogue);
				}
				if (firstPartMonitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				releaseSrcZip(strSrcPath);
				firstPartMonitor.worked(1);
				firstPartMonitor.done();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				IProgressMonitor secondPartMonitor = Progress.getSubMonitor(monitor, 1);
				secondPartMonitor.beginTask(Messages.getString("idml.IDML2XLIFF.task3"), 1);
				secondPartMonitor.subTask("");
				fileManager.createZip(strTmpFolderPath, zipSklOut, strTmpFolderPath + File.separator + "Stories");
				secondPartMonitor.worked(1);
				secondPartMonitor.done();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				initSpreadAllStory(Progress.getSubMonitor(monitor, 1));
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				// 先提取母版内容
				parseSpreadFile(true, Progress.getSubMonitor(monitor, 2));
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				// 再提取文本内容
				parseSpreadFile(false, Progress.getSubMonitor(monitor, 2));
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				// 按顺序解析 Story 文件
				parseStoryFile(Progress.getSubMonitor(monitor, 5));
				lstAllStory.removeAll(lstStoryBySort);
				if (lstAllStory.size() > 0) {
					// 将剩余的 story 文件放入压缩包
					for (String strStoryPath : lstAllStory) {
						fileManager.addFileToZip(zipSklOut, strTmpFolderPath, strTmpFolderPath + File.separator
								+ strStoryPath);
					}
				}
				writeOut("</body>\n</file>\n</xliff>");
				out.close();
				zipSklOut.flush();
				zipSklOut.close();
				IProgressMonitor thirdMonitor = Progress.getSubMonitor(monitor, 1);
				thirdMonitor.beginTask(Messages.getString("idml.IDML2XLIFF.task4"), 1);
				thirdMonitor.subTask("");
				// 删除解压目录
				fileManager.deleteFileOrFolder(new File(strTmpFolderPath));
				thirdMonitor.worked(1);
				thirdMonitor.done();
				result.put(Converter.ATTR_XLIFF_FILE, strXLIFFPath);
			} catch(OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(Messages.getString("idml.IDML2XLIFF.logger1"), e);
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("idml.IDML2XLIFF.msg1"),
						e);
			} finally {
				monitor.done();
			}

			return result;
		}

		/**
		 * 加载 designmap.xml 文件，初始化 lstSpread 和 lstAllStory
		 * @throws XPathParseException
		 * @throws XPathEvalException
		 * @throws NavException
		 *             ;
		 */
		private void initSpreadAllStory(IProgressMonitor monitor) throws XPathParseException, XPathEvalException,
				NavException {
			monitor.beginTask("", 1);
			monitor.subTask(MessageFormat.format(Messages.getString("idml.IDML2XLIFF.task5"), "designmap.xml"));
			VTDGen vg = new VTDGen();
			if (vg.parseZIPFile(strSrcPath, "designmap.xml", true)) {
				VTDNav vn = vg.getNav();
				AutoPilot ap = new AutoPilot(vn);
				ap.declareXPathNameSpace(IDML_PREFIX, IDML_NAMESPACE);
				ap.selectXPath("/Document/node()");
				while (ap.evalXPath() != -1) {
					int curIndex = vn.getCurrentIndex();
					int tokenType = vn.getTokenType(curIndex);
					String name = vn.toString(curIndex);
					// 节点
					if (tokenType == 0) {
						if (name.equals("idPkg:Spread")) {
							String strSpread = vn.toString(vn.getAttrVal("src"));
							lstSpread.add(strSpread);
						} else if (name.equals("idPkg:Story")) {
							String strStory = vn.toString(vn.getAttrVal("src"));
							lstAllStory.add(strStory);
						} else if (name.equals("idPkg:MasterSpread")) {
							String strMasterSpread = vn.toString(vn.getAttrVal("src"));
							lstMasterSpread.add(strMasterSpread);
						}
					}
				}
			}
			monitor.worked(1);
			monitor.done();
		}

		/**
		 * 按 designmap.xml 中指定 Spread 的顺序解析 Spread 文件
		 * @throws XPathParseException
		 * @throws XPathEvalException
		 * @throws NavException
		 *             ;
		 */
		private void parseSpreadFile(final boolean isParseMasterSpread, IProgressMonitor monitor)
				throws XPathParseException, XPathEvalException, NavException {
			monitor.beginTask("", isParseMasterSpread ? lstMasterSpread.size() : lstSpread.size());
			Iterator<String> it = isParseMasterSpread ? lstMasterSpread.iterator() : lstSpread.iterator();
			final ArrayList<Double> lstPageX = new ArrayList<Double>();
			// 总页数
			int pageAmount = 0;
			// 每个 Spread 所包含的页数
			int pageCount = 0;
			final LinkedHashMap<String, Double[]> mapStoryAndCoordinate = new LinkedHashMap<String, Double[]>();

			//与匿名内部类通信
			class FieldBridge {
				int pageAmount;
				int pageCount;
			}
			
			final FieldBridge spreadAttr = new FieldBridge();
			spreadAttr.pageAmount = pageAmount;
			spreadAttr.pageCount = pageCount;

			while (it.hasNext()) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				String strSpreadPath = it.next();
				monitor.subTask(MessageFormat.format(Messages.getString("idml.IDML2XLIFF.task5"), strSpreadPath));
				SAXParserFactory factory = SAXParserFactory.newInstance();
				try {
					SAXParser saxParser = factory.newSAXParser();
					lstPageX.clear();
					saxParser.parse(new File(strTmpFolderPath + File.separator + strSpreadPath), new DefaultHandler() {
						boolean hasSpreadNode = false;
						String strPageCount = null;
						String pageItemTransform = null;

						List<TextFrameAttr> textFrameList = new LinkedList<TextFrameAttr>();

						@Override
						public void startElement(String uri, String localName, String qName, Attributes attributes)
								throws SAXException {
							if (qName.trim().equalsIgnoreCase("Page") && hasSpreadNode) {
								String strItemTransform = attributes.getValue("ItemTransform");
								String strName = attributes.getValue("Name");// 页号
								if (strItemTransform == null || strName == null) {
									return;
								}
								Pattern pattern = Pattern.compile("(?<=\\D*)\\d+(?=(\\D*$))");
								Matcher matcher = pattern.matcher(strName);
								if (matcher.find()) {
									int curPageNumber = Integer.parseInt(matcher.group());
									if (curPageNumber >= spreadAttr.pageAmount
											&& (curPageNumber - spreadAttr.pageAmount) <= spreadAttr.pageCount) {
										String[] arrTransform = strItemTransform.trim().split(" ");
										if (arrTransform.length == 6) {
											double dblX = Double.parseDouble(arrTransform[4].trim());
											lstPageX.add(dblX);
										}
									}
								}
							} else if (qName.trim().equalsIgnoreCase("TextFrame") && hasSpreadNode) {
								TextFrameAttr attr = new TextFrameAttr();
								attr.itemTransform = attributes.getValue("ItemTransform");
								attr.parentStory = attributes.getValue("ParentStory");
								textFrameList.add(attr);
							} else if (qName.trim().equalsIgnoreCase(isParseMasterSpread ? "MasterSpread" : "Spread")) {
								hasSpreadNode = true;
								strPageCount = attributes.getValue("PageCount");
								spreadAttr.pageCount = Integer.parseInt(strPageCount.trim());
								pageItemTransform = attributes.getValue("ItemTransform");
							}
						}

						@Override
						public void endDocument() throws SAXException {
							if (!hasSpreadNode) {
								return;
							}
							if (lstPageX.size() == 0 && pageItemTransform != null) {
								lstPageX.add(Double.MIN_VALUE);
								String[] arrTransform = pageItemTransform.trim().split(" ");
								if (arrTransform.length == 6) {
									double dblX = Double.parseDouble(arrTransform[4].trim());
									lstPageX.add(dblX);
								}
								lstPageX.add(Double.MAX_VALUE);
							}
							Collections.sort(lstPageX);
							spreadAttr.pageAmount += spreadAttr.pageCount;
							mapStoryAndCoordinate.clear();
							for (TextFrameAttr attr : textFrameList) {
								String strStoryName = attr.parentStory;
								String strItemTransform = attr.itemTransform;
								String[] arrTransform = strItemTransform.trim().split(" ");
								if (arrTransform.length == 6) {
									double dblX = Double.parseDouble(arrTransform[4].trim());
									double dblY = Double.parseDouble(arrTransform[5].trim());
									mapStoryAndCoordinate.put(strStoryName, new Double[] { dblX, dblY });
								}
							}
							// 对 mapStoryAndCoordinate 排序
							sortMap(mapStoryAndCoordinate, lstPageX);
						}

						/**
						 * TextFrame 节点属性
						 * @author Austen
						 * @version
						 * @since JDK1.6
						 */
						class TextFrameAttr {
							/** ParentStory 属性. */
							String parentStory = null;
							/** ItemTransform 属性. */
							String itemTransform = null;
						}
					});
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
				monitor.worked(1);
			}
			monitor.done();
		}

		// 原来的解析 Spread 的方法，
		// /**
		// * 按 designmap.xml 中指定 Spread 的顺序解析 Spread 文件
		// * @throws XPathParseException
		// * @throws XPathEvalException
		// * @throws NavException
		// * ;
		// */
		// private void parseSpreadFile(boolean isParseMasterSpread, IProgressMonitor monitor) throws
		// XPathParseException,
		// XPathEvalException, NavException {
		// monitor.beginTask("", isParseMasterSpread ? lstMasterSpread.size() : lstSpread.size());
		// Iterator<String> it = isParseMasterSpread ? lstMasterSpread.iterator() : lstSpread.iterator();
		// ArrayList<Double> lstPageX = new ArrayList<Double>();
		// // 总页数
		// int pageAmount = 0;
		// // 每个 Spread 所包含的页数
		// int pageCount;
		// LinkedHashMap<String, Double[]> mapStoryAndCoordinate = new LinkedHashMap<String, Double[]>();
		// AutoPilot ap = new AutoPilot();
		// AutoPilot apPage = new AutoPilot();
		// AutoPilot apTextFrame = new AutoPilot();
		// VTDUtils vu = new VTDUtils();
		// int i = 0;
		// while (it.hasNext()) {
		// System.out.println(i++);
		// if (monitor.isCanceled()) {
		// throw new OperationCanceledException(Messages.getString("idml.cancel"));
		// }
		// String strSpreadPath = it.next();
		// monitor.subTask(MessageFormat.format(Messages.getString("idml.IDML2XLIFF.task5"), strSpreadPath));
		// VTDGen vg = new VTDGen();
		// if (vg.parseZIPFile(strSrcPath, strSpreadPath, true)) {
		// lstPageX.clear();
		// VTDNav vn = vg.getNav();
		// ap.bind(vn);
		// ap.declareXPathNameSpace(IDML_PREFIX, IDML_NAMESPACE);
		// ap.selectXPath(isParseMasterSpread ? "/idPkg:MasterSpread/MasterSpread" : "/idPkg:Spread/Spread");
		// vu.bind(vn);
		// if (ap.evalXPath() != -1) {
		// String strPageCount = vn.toString(vn.getAttrVal("PageCount"));// 页数
		// pageCount = Integer.parseInt(strPageCount.trim());
		// int transformIndex = vn.getAttrVal("ItemTransform");
		// String pageItemTransform = null;
		// if (transformIndex != -1) {
		// pageItemTransform = vn.toString(transformIndex);
		// }
		// vn.push();
		// apPage.bind(vn);
		// apPage.selectXPath("./Page");
		// while (apPage.evalXPath() != -1) {
		// String strItemTransform = vu.getCurrentElementAttribut("ItemTransform", null);
		// String strName = vu.getCurrentElementAttribut("Name", null);// 页号
		// if (strItemTransform == null || strName == null) {
		// continue;
		// }
		// Pattern pattern = Pattern.compile("(?<=\\D*)\\d+(?=(\\D*$))");
		// Matcher matcher = pattern.matcher(strName);
		// if (matcher.find()) {
		// int curPageNumber = Integer.parseInt(matcher.group());
		// if (curPageNumber >= pageAmount && (curPageNumber - pageAmount) <= pageCount) {
		// String[] arrTransform = strItemTransform.trim().split(" ");
		// if (arrTransform.length == 6) {
		// double dblX = Double.parseDouble(arrTransform[4].trim());
		// lstPageX.add(dblX);
		// }
		// }
		// }
		// }
		// if (lstPageX.size() == 0 && pageItemTransform != null) {
		// lstPageX.add(Double.MIN_VALUE);
		// String[] arrTransform = pageItemTransform.trim().split(" ");
		// if (arrTransform.length == 6) {
		// double dblX = Double.parseDouble(arrTransform[4].trim());
		// lstPageX.add(dblX);
		// }
		// lstPageX.add(Double.MAX_VALUE);
		// }
		// Collections.sort(lstPageX);
		// pageAmount += pageCount;
		// vn.pop();
		// mapStoryAndCoordinate.clear();
		// apTextFrame.bind(vn);
		// apTextFrame.selectXPath(".//TextFrame");
		// while (apTextFrame.evalXPath() != -1) {
		// String strStoryName = vn.toString(vn.getAttrVal("ParentStory"));
		// String strItemTransform = vn.toString(vn.getAttrVal("ItemTransform"));
		// String[] arrTransform = strItemTransform.trim().split(" ");
		// if (arrTransform.length == 6) {
		// double dblX = Double.parseDouble(arrTransform[4].trim());
		// double dblY = Double.parseDouble(arrTransform[5].trim());
		// mapStoryAndCoordinate.put(strStoryName, new Double[] { dblX, dblY });
		// }
		// }
		// // 对 mapStoryAndCoordinate 排序
		// sortMap(mapStoryAndCoordinate, lstPageX);
		// }
		// }
		// monitor.worked(1);
		// }
		// monitor.done();
		// }

		/**
		 * 对集合 map 进行排序
		 * @param map
		 *            要排序的集合
		 * @param lstPageX
		 *            Spread 中 Page 的横坐标集合（已按从小到大的顺序排序）
		 */
		private void sortMap(LinkedHashMap<String, Double[]> map, final ArrayList<Double> lstPageX) {
			List<Map.Entry<String, Double[]>> lstMap = new ArrayList<Map.Entry<String, Double[]>>(map.entrySet());
			Collections.sort(lstMap, new Comparator<Map.Entry<String, Double[]>>() {

				public int compare(Entry<String, Double[]> o1, Entry<String, Double[]> o2) {
					Double[] arrDbl1 = o1.getValue();
					Double[] arrDbl2 = o2.getValue();
					boolean isSamePage = false;
					if (lstPageX.size() == 1) {
						isSamePage = true;
					} else {
						for (int i = 0; i < lstPageX.size() - 1; i++) {
							double dbl1 = lstPageX.get(i);
							double dbl2 = lstPageX.get(i + 1);
							if (arrDbl1[0] >= dbl1 && arrDbl1[0] <= dbl2 && arrDbl2[0] >= dbl1 && arrDbl2[0] <= dbl2) {
								isSamePage = true;
								break;
							}
						}
					}
					// 在同一页
					if (isSamePage) {
						// 先比较纵坐标，纵坐标相同再比较横坐标，由于 y 轴的正方向向下，因此此处为 arrDbl2[1] - arrDbl1[1]
						if (arrDbl1[1] != arrDbl2[1]) {
							return arrDbl1[1] > arrDbl2[1] ? 1 : -1;
						} else {
							return arrDbl1[0] > arrDbl2[0] ? 1 : -1;
						}
					} else {
						// 未在同一页时只需比较横坐标
						return arrDbl1[0] > arrDbl2[0] ? 1 : -1;
					}
				}
			});
			for (Entry<String, Double[]> entry : lstMap) {
				String strStorySuffix = entry.getKey();
				String strStoryModule = lstAllStory.get(0);
				String strStoryPath = strStoryModule.substring(0, strStoryModule.lastIndexOf("_") + 1) + strStorySuffix
						+ strStoryModule.substring(strStoryModule.lastIndexOf("."));
				if (lstAllStory.contains(strStoryPath) && !lstStoryBySort.contains(strStoryPath)) {
					lstStoryBySort.add(strStoryPath);
				}
			}
		}

		int gId = 0;

		/**
		 * 解析 Story 文件
		 * @throws Exception
		 *             ;
		 */
		private void parseStoryFile(IProgressMonitor monitor) throws Exception {
			monitor.beginTask("", lstStoryBySort.size());
			Iterator<String> it = lstStoryBySort.iterator();
			// 标识 Story 文件是否有修改。
			boolean isUpdate = false;
			int xId;
			boolean isModify = false;
			// 存储删除的节点，在修改完成后要重新添加到原来的位置
			HashMap<String, String> mapDelete = new HashMap<String, String>();
			VTDGen vg = new VTDGen();
			XMLModifier xm = new XMLModifier();
			AutoPilot ap = new AutoPilot();
			ap.declareXPathNameSpace(IDML_PREFIX, IDML_NAMESPACE);
			VTDUtils vu = new VTDUtils();
			while (it.hasNext()) {
				String strStoryPath = it.next();
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("idml.cancel"));
				}
				monitor.subTask(MessageFormat.format(Messages.getString("idml.IDML2XLIFF.task5"), strStoryPath));
				vg.clear();
				if (vg.parseZIPFile(strSrcPath, strStoryPath, true)) {
					isUpdate = false;
					VTDNav vn = vg.getNav();
					xm.bind(vn);
					vu.bind(vn);
					ap.bind(vn);
					ap.selectXPath("/idPkg:Story/Story/descendant::ParagraphStyleRange/CharacterStyleRange/node()[name()='Rectangle' or name()='Group']");
					xId = 0;
					// 新建一个以 x 为扩展名的文件，文件名与 strStoryPath 的文件名相同
					File tmpFile = new File(strTmpFolderPath + File.separator
							+ strStoryPath.substring(0, strStoryPath.lastIndexOf(".")) + ".x");
					tmpFile.createNewFile();
					BufferedWriter fos = new BufferedWriter(new FileWriter(tmpFile, true));
					fos.append("<root>\n");
					while (ap.evalXPath() != -1) {
						String strRectangle = vu.getElementFragment();
						String strReplaceText = "<x id=\"" + xId + "\">\n" + strRectangle + "\n</x>\n";
						fos.append(strReplaceText);
						xm.remove();
						xm.insertAfterElement("<x id=\"" + xId + "\"/>");
						isUpdate = true;
						xId++;
					}
					fos.append("</root>");
					fos.close();
					if (isUpdate) {
						vn = xm.outputAndReparse();
						xm.bind(vn);
						ap.bind(vn);
						vu.bind(vn);
						fileManager.addFileToZip(zipSklOut, strTmpFolderPath, tmpFile.getAbsolutePath());
					}

					isModify = false;
					ap.selectXPath("/idPkg:Story/Story/descendant::node()[name()='HiddenText' or (name()='Change' and @ChangeType='DeletedText')]");
					mapDelete.clear();
					xId = 0;
					while (ap.evalXPath() != -1) {
						String strDel = vu.getElementFragment();
						xm.remove();
						String strText = "%%%%" + xId++ + "%%%%";
						xm.insertAfterElement(strText);
						mapDelete.put(strText, strDel);
						isModify = true;
						isUpdate = true;
					}
					if (isModify) {
						vn = xm.outputAndReparse();
						xm.bind(vn);
						ap.bind(vn);
						vu.bind(vn);
					}

					// 选取 ParagraphStyleRange 节点，只选取第一层
					ap.selectXPath("/idPkg:Story/Story/descendant::ParagraphStyleRange[not(ancestor::node()[name()='ParagraphStyleRange']) and descendant::node()[name()='Content']]");
					while (ap.evalXPath() != -1) {
						String paragraphElement = vu.getElementFragment();
						String paraModified = handlePara(paragraphElement, mapDelete);
						if (!paragraphElement.equals(paraModified)) {
							xm.remove();
							xm.insertAfterElement(paraModified);
							isUpdate = true;
						}
					}

					if (isUpdate) {
						// Story 文件有修改时，文件名添加 .skl 后缀并放入压缩包
						saveStoryFile(xm, strStoryPath);
					} else {
						// Story 文件无修改时，将源文件放入压缩包，文件名未改变。
						fileManager.addFileToZip(zipSklOut, strTmpFolderPath, strTmpFolderPath + File.separator
								+ strStoryPath);
					}
				}
				monitor.worked(1);
			}
			monitor.done();
		}

		public String handlePara(String para, HashMap<String, String> mapDelete) throws Exception {
			StringBuffer sb = new StringBuffer();
			List<Integer> lstDepth = new ArrayList<Integer>();
			VTDGen vg = new VTDGen();
			vg.setDoc(para.getBytes());
			vg.parse(true);
			VTDNav vn = vg.getNav();
			XMLModifier xm = new XMLModifier(vn);
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vu = new VTDUtils(vn);
			boolean isFind = false;
			int curDepth = 0;
			ArrayList<String> lstSegment = new ArrayList<String>();
			ArrayList<Integer> lstIndex = new ArrayList<Integer>();
			ap.selectXPath("/ParagraphStyleRange/descendant::node()");
			StringBuffer sbSkl = new StringBuffer();
			StringBuffer sbXLF = new StringBuffer();
			StringBuffer sbContent = new StringBuffer();
			AutoPilot ap2 = new AutoPilot(vn);
			AutoPilot childAp = new AutoPilot(vn);
			while (ap.evalXPath() != -1) {
				String nodeName = vu.getCurrentElementName();
				curDepth = vn.getCurrentDepth();
				// 跳过 ParagraphStyleRange 节点
				if (nodeName.equalsIgnoreCase("ParagraphStyleRange")) {
					lstDepth.add(curDepth);
					// vn.pop();
					// 一个文本段结束，要进行分段
					if (sb.length() > 0) {
						vn.push();
						updateParaAndXLF(sb, vn, lstSegment, sbSkl, sbContent, sbXLF, xm, ap2, childAp, lstIndex);
						vn.pop();
					}
					continue;
				} else {
					isFind = false;
					Iterator<Integer> it = lstDepth.iterator();
					while (it.hasNext()) {
						Integer depth = (Integer) it.next();
						if (curDepth >= depth) {
							isFind = true;
							it.remove();
						}
					}
					if (isFind || nodeName.equalsIgnoreCase("Br") || nodeName.equalsIgnoreCase("Table")) {
						// 一个文本段结束，要进行分段
						if (sb.length() > 0) {
							vn.push();
							updateParaAndXLF(sb, vn, lstSegment, sbSkl, sbContent, sbXLF, xm, ap2, childAp, lstIndex);
							vn.pop();
						}
					}
					if (nodeName.equalsIgnoreCase("Content")) {
						String seg = vu.getElementContent();
						sb.append(seg);
						lstSegment.add(seg);
						lstIndex.add(vn.getCurrentIndex());
					}
				}
			}
			if (sb.length() > 0) {
				vn.push();
				updateParaAndXLF(sb, vn, lstSegment, sbSkl, sbContent, sbXLF, xm, ap2, childAp, lstIndex);
				vn.pop();
			}

			if (mapDelete.size() > 0) {
				childAp.bind(vn);
				ap.selectXPath("/ParagraphStyleRange/descendant::node()[text()!='' and starts-with(normalize-space(text()),'%%%%') and ends-with(normalize-space(text()),'%%%%')]");
				while (ap.evalXPath() != -1) {
					String strId = vu.getElementPureText().trim();
					String strPrimary = mapDelete.get(strId);
					vn.push();
					childAp.selectXPath("./text()");
					if (childAp.evalXPath() != -1) {
						xm.updateToken(vn.getCurrentIndex(), strPrimary);
					}
					vn.pop();
				}
			}

			vn = xm.outputAndReparse();
			vu.bind(vn);
			return vu.getElementFragment();
		}

		public void updateParaAndXLF(StringBuffer sb, VTDNav vn, ArrayList<String> lstSegment, StringBuffer sbSkl,
				StringBuffer sbContent, StringBuffer sbXLF, XMLModifier xm, AutoPilot ap2, AutoPilot childAp,
				ArrayList<Integer> lstIndex) throws Exception {
			ParagraphManagement paraManagement = new ParagraphManagement();
			String string = paraManagement.toHexString(sb.toString().replaceAll("\r\n", "").replaceAll("\n", ""));
			if (string.toUpperCase().replaceAll("FFFE", "").replaceAll("FEFF", "").replaceAll("2028", "").trim()
					.equals("")) {
				sbSkl.delete(0, sbSkl.length());
				sbXLF.delete(0, sbXLF.length());
				sbContent.delete(0, sbContent.length());
				sb.delete(0, sb.length());
				lstSegment.clear();
				lstIndex.clear();
				return;
			}
			String[] arrSeg = segmenter.segment(sb.toString());
			arr: for (String segment : arrSeg) {
				// 修改 XLIFF 和骨架
				Iterator<String> it = lstSegment.iterator();
				int index = -1;
				while (it.hasNext()) {
					String seg = (String) it.next();
					index++;
					if (segment.equals(seg)) {
						// 一个 trans-unit : <source><g id=''>segment</g></source>
						sbXLF.append("<g id=\"" + gId + "\">" + insertPhTag(segment) + "</g>");
						writeSegment(sbXLF.toString(), gId);
						sbXLF.delete(0, sbXLF.length());
						// 骨架：sbSkl.append(###id###), 然后写入文件;sbSkl = 0 sbContent.append(segment) sbContent=0
						sbSkl.append("###" + gId++ + "###");
						sbContent.append(segment);

						ap2.selectXPath("/ParagraphStyleRange/descendant::node()[text()!='' and text()="
								+ VTDUtils.dealEscapeQuotes(sbContent.toString()) + "]");
						while (ap2.evalXPath() != -1) {
							int curIndex = vn.getCurrentIndex();
							boolean isFind = false;
							for (Iterator<Integer> iterator = lstIndex.iterator(); iterator.hasNext();) {
								Integer item = iterator.next();
								if (item == curIndex) {
									iterator.remove();
									isFind = true;
								}
							}
							if (!isFind) {
								continue;
							}
							vn.push();
							childAp.selectXPath("./text()");
							if (childAp.evalXPath() != -1) {
								xm.updateToken(vn.getCurrentIndex(), sbSkl.toString());
							}
							vn.pop();
							break;
						}

						sbSkl.delete(0, sbSkl.length());
						sbContent.delete(0, sbContent.length());
						it.remove();
						continue arr;
					} else if (segment.startsWith(seg)) {
						sbXLF.append("<g id=\"" + gId + "\">" + insertPhTag(seg) + "</g>");
						// 骨架：sbSkl.append(###id###), 然后写入文件并sbSkl = 0 sbContent.append(seg) sbContent=0
						sbSkl.append("###" + gId++ + "###");
						sbContent.append(seg);

						ap2.selectXPath("/ParagraphStyleRange/descendant::node()[text()!='' and text()="
								+ VTDUtils.dealEscapeQuotes(sbContent.toString()) + "]");
						while (ap2.evalXPath() != -1) {
							int curIndex = vn.getCurrentIndex();
							boolean isFind = false;
							for (Iterator<Integer> iterator = lstIndex.iterator(); iterator.hasNext();) {
								Integer item = iterator.next();
								if (item == curIndex) {
									iterator.remove();
									isFind = true;
								}
							}
							if (!isFind) {
								continue;
							}
							vn.push();
							childAp.selectXPath("./text()");
							if (childAp.evalXPath() != -1) {
								xm.updateToken(vn.getCurrentIndex(), sbSkl.toString());
							}
							vn.pop();
							break;
						}

						sbSkl.delete(0, sbSkl.length());
						sbContent.delete(0, sbContent.length());
						segment = segment.substring(seg.length());
						it.remove();
						index--;
						continue;
					} else if (seg.startsWith(segment)) {
						// sbXLF.append("<g id=''>segment</g>"), 然后写入 XLIFF 文件
						sbXLF.append("<g id=\"" + gId + "\">" + insertPhTag(segment) + "</g>");
						writeSegment(sbXLF.toString(), gId);
						sbXLF.delete(0, sbXLF.length());
						// 骨架：sbSkl.append(###id###); sbContent.append(segment)
						sbSkl.append("###" + gId++ + "###");
						sbContent.append(segment);
						lstSegment.set(index, seg.substring(segment.length()));
						continue arr;
					}
				}
			}
			sbSkl.delete(0, sbSkl.length());
			sbXLF.delete(0, sbXLF.length());
			sbContent.delete(0, sbContent.length());
			sb.delete(0, sb.length());
			lstSegment.clear();
			lstIndex.clear();
		}

		private String insertPhTag(String input) {
			// 匹配 Content 子节点的正则表达式
			Pattern pattern = Pattern.compile("<[^<>]+>");
			Matcher matcher = pattern.matcher(input);
			int start = 0;
			int end = 0;
			StringBuffer sbText = new StringBuffer();
			while (matcher.find()) {
				start = matcher.start();
				sbText.append(input.substring(end, start)).append("<ph>")
						.append(TextUtil.cleanSpecialString(matcher.group())).append("</ph>");
				end = matcher.end();
			}
			sbText.append(input.substring(end, input.length()));
			return sbText.toString();
		}

		/**
		 * 保存 Story 文件的修改并将其添加到压缩包
		 * @param xm
		 * @param storyName
		 *            Story 文件在源文件中的相对路径
		 * @throws Exception
		 *             ;
		 */
		private void saveStoryFile(XMLModifier xm, String storyName) throws Exception {
			File tempFile = new File(strTmpFolderPath + File.separator + storyName + ".skl");
			FileOutputStream fos = new FileOutputStream(tempFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			xm.output(bos); // 写入文件
			bos.close();
			fileManager.addFileToZip(zipSklOut, strTmpFolderPath, tempFile.getAbsolutePath());
		}

		/**
		 * 将 strSrc 写入 XLIFF 文件
		 * @param strSrc
		 * @throws IOException
		 *             ;
		 */
		private void writeSegment(String strSrc, int id) throws IOException {
			// 去掉只有一个G标签的情况
			if (getGTagCount(strSrc) == 1) {
				writeOut("<trans-unit id=\"" + segNum++ + "\" gid=\"" + id + "\">\n");
				strSrc = strSrc.replaceAll("<g.+?>|</g>", "");
			} else {
				writeOut("<trans-unit id=\"" + segNum++ + "\">\n");
			}
			// int phId = 1;
			// 将源文件中的 &apos; 替换为 '，因为在 XLIFF 中 &apos; 不是转义字符，而 InDesign 中是，在逆转换时，要注意将 ' 替换为 &apos;
			strSrc = strSrc.replaceAll("&apos;", "'");
			writeOut("<source xml:lang=\"" + strSrcLang + "\">" + strSrc + "</source>\n");
			writeOut("</trans-unit>\n\n"); //$NON-NLS-1$
		}

		/**
		 * 判断只有一个G标签的情况
		 * @param strSrc
		 * @return ;
		 */
		private int getGTagCount(String strSrc) {
			int i = 0;
			if (strSrc.startsWith("<g") && strSrc.endsWith("</g>")) {
				Pattern p = Pattern.compile("</g>");
				Matcher m = p.matcher(strSrc);

				while (m.find()) {
					i++;
				}
			}
			return i;
		}

		/**
		 * 写 XLIFF 的头节点内容
		 * @throws IOException
		 *             ;
		 */
		private void writeHeader() throws IOException {
			writeOut("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"); //$NON-NLS-1$
			writeOut("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " + //$NON-NLS-1$
					"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xmlns:hs=\"" + Converter.HSNAMESPACE
					+ "\" " + //$NON-NLS-1$ 
					"xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd " //$NON-NLS-1$
					+ Converter.HSSCHEMALOCATION + "\">\n"); //$NON-NLS-1$
			if (!strTgtLang.equals("")) {
				writeOut("<file datatype=\"" + TYPE_VALUE + "\" original=\"" + TextUtil.cleanSpecialString(strSrcPath) + "\" source-language=\"" + strSrcLang + "\" target-language=\"" + strTgtLang + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				writeOut("<file datatype=\"" + TYPE_VALUE + "\" original=\"" + TextUtil.cleanSpecialString(strSrcPath) + "\" source-language=\"" + strSrcLang + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			writeOut("<header>\n"); //$NON-NLS-1$
			writeOut("<skl>\n"); //$NON-NLS-1$
			if (blnIsSuite) {
				writeOut("<external-file crc=\"" + CRC16.crc16(TextUtil.cleanString(strSklPath).getBytes("UTF-8")) + "\" href=\"" + TextUtil.cleanSpecialString(strSklPath) + "\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				writeOut("<external-file href=\"" + TextUtil.cleanSpecialString(strSklPath) + "\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			writeOut("</skl>\n"); //$NON-NLS-1$
			writeOut("   <tool tool-id=\"" + strQtToolID + "\" tool-name=\"HSStudio\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			writeOut("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\">" //$NON-NLS-1$
					+ strSrcEncoding + "</hs:prop></hs:prop-group>\n"); //$NON-NLS-1$
			writeOut("</header>\n<body>\n"); //$NON-NLS-1$
			writeOut("\n"); //$NON-NLS-1$
		}

		private void writeOut(String string) throws IOException {
			out.write(string.getBytes("UTF-8")); //$NON-NLS-1$
		}

		/**
		 * 将 source 所代表的压缩包解压到临时目录 strTmpFolderPath
		 * @param source
		 * @throws IOException
		 *             ;
		 */
		private void releaseSrcZip(String source) throws IOException {
			String sysTemp = System.getProperty("java.io.tmpdir");
			String dirName = "tmpidmlfolder" + System.currentTimeMillis();
			File dir = new File(sysTemp + File.separator + dirName);
			dir.mkdirs();
			strTmpFolderPath = dir.getAbsolutePath();
			fileManager.releaseZipToFile(source, strTmpFolderPath);
		}
	}
}
