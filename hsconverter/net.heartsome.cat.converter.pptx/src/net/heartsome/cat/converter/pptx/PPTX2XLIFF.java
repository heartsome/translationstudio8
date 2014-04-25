package net.heartsome.cat.converter.pptx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipOutputStream;

import net.heartsome.cat.common.file.FileManager;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.pptx.preference.Constants;
import net.heartsome.cat.converter.pptx.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * PPTX 转换为 XLIFF
 * @author peason
 * @version
 * @since JDK1.6
 */
public class PPTX2XLIFF implements Converter {

	private static final Logger LOGGER = LoggerFactory.getLogger(PPTX2XLIFF.class);

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "pptx";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("pptx.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "MS Office PowerPoint 2007 to XLIFF Conveter";

	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		PPTX2XLIFFImpl impl = new PPTX2XLIFFImpl();
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
	 * PPTX 转换为 XLIFF 的实现类
	 * @author peason
	 * @version
	 * @since JDK1.6
	 */
	class PPTX2XLIFFImpl {

		/** SlideMaster 文件中 a 的前缀 */
		private static final String PREFIX_A = "a";

		/** SlideMaster 文件中 a 的命名空间 */
		private static final String NAMESPACE_A = "http://schemas.openxmlformats.org/drawingml/2006/main";

		/** SlideMaster 文件中 p 的前缀 */
		private static final String PREFIX_P = "p";

		/** SlideMaster 文件中 a 的命名空间 */
		private static final String NAMESPACE_P = "http://schemas.openxmlformats.org/presentationml/2006/main";

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

		/** 生成 XLIFF 文件的输出流 */
		private FileOutputStream out;

		/** 生成骨架文件的输出流 */
		private ZipOutputStream zipSklOut;

		private FileManager fileManager = new FileManager();

		/** 临时解压目录（将源文件先解压到此目录） */
		private String strTmpFolderPath;

		/** 文本段 ID 值 */
		private int segNum = 0;

		/** key 为 slideMasterN.xml 文件名，value 中第一个值为 ph 节点的 type 属性值，第二个为横坐标，第三个为纵坐标 */
		private HashMap<String, ArrayList<String[]>> mapSldMasterPH = new HashMap<String, ArrayList<String[]>>();

		/** key 为 slideLayoutN.xml 文件名，value 中第一个值为 ph 节点的 type 属性值，第二个为横坐标，第三个为纵坐标 */
		private HashMap<String, ArrayList<String[]>> mapSldLayoutPH = new HashMap<String, ArrayList<String[]>>();

		/** key 为 slideMasterN.xml 文件名，value 为 slideLayoutN.xml 文件名 */
		private HashMap<String, ArrayList<String>> mapLayout = new HashMap<String, ArrayList<String>>();

		/** slide 文件名集合，已按实际显示的顺序排序 */
		private ArrayList<String> lstSlide = new ArrayList<String>();

		/** 是否萃取备注内容，此属性在首选项中设置（备注中的页眉页脚不受此变量控制） */
		private boolean blnIsFilterNote;

		/**
		 * 转换器的处理流程如下： <br/>
		 * 1. 解压源文件到临时目录 <br/>
		 * 2. 创建骨架文件压缩包，并将除 ppt/notesSlides 和 ppt/slides 文件夹外的其他文件放入压缩包 <br/>
		 * 3. 解析 ppt/slideMasters 下的 slideMasterN.xml 文件（如果存在的话），查找 ph 不是 dt 和 sldNum 的节点，取出其 type, idx 属性值和坐标值<br/>
		 * (key 为 slideMasterN.xml 文件名，value 为 type, idx 属性值和坐标值的集合) <br/>
		 * 4. 解析 ppt/slideMasters/_rels 下的 rels 文件，取出其包含的 slideLayoutN.xml 文件名(key 为 slideMasterN.xml 文件名，<br/>
		 * value 为 slideLayoutN.xml 文件名) <br/>
		 * 5. 解析 ppt/presentation.xml 文件和 ppt/_rels/presentation.xml.rels 文件，确定 slideN.xml 文件的顺序。 <br/>
		 * 6. 按顺序解析 slideN.xml.rels 文件，取出 slideLayoutN.xml 和 noteSlideN.xml 文件名，然后解析对应的 slideN.xml 文件，<br/>
		 * 并放入骨架信息，每解析完一个 slideN.xml 文件后就去 _rels 查找相应的 rels 文件并解析，从中确定对应的 noteSlideN.xml 文件 <br/>
		 * 7. 解析第 6 步确定的 noteSlideN.xml 文件，查找 ph 的 type 属性值为 body, hdr, ftr 的节点，从中取出文本信息 <br/>
		 * 8. 重复 6, 7 步 <br/>
		 * 9. 删除临时目录 <br/>
		 * @param args
		 * @param monitor
		 * @return
		 * @throws ConverterException
		 *             ;
		 */
		public Map<String, String> run(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
			Map<String, String> result = new HashMap<String, String>();
			monitor = Progress.getMonitor(monitor);
			monitor.beginTask("", 13);
			// 转换过程分为 13 部分，releaseSrcZip 占 1，createZip 占 1，parseSlideMaster 占 2，parseSlideMasterRels 占
			// 2，getSlideAndSort 占 1，
			// parseSlideBySort 占 5，deleteFileOrFolder 占 1
			IProgressMonitor firstMonitor = Progress.getSubMonitor(monitor, 1);
			firstMonitor.beginTask(Messages.getString("pptx.PPTX2XLIFF.task1"), 1);
			firstMonitor.subTask("");
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			blnIsFilterNote = store.getBoolean(Constants.PPTX_FILTER);
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

				if (firstMonitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}

				// 第 1 步
				releaseSrcZip(strSrcPath);

				firstMonitor.worked(1);
				firstMonitor.done();
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}

				IProgressMonitor secondMonitor = Progress.getSubMonitor(monitor, 1);
				secondMonitor.beginTask(Messages.getString("pptx.PPTX2XLIFF.task2"), 1);
				secondMonitor.subTask("");

				List<String> lstExcludeFolder = new ArrayList<String>();
				lstExcludeFolder.add(strTmpFolderPath + File.separator + "ppt" + File.separator + "notesSlides");
				lstExcludeFolder.add(strTmpFolderPath + File.separator + "ppt" + File.separator + "slides");
				// 第 2 步
				fileManager.createZip(strTmpFolderPath, zipSklOut, lstExcludeFolder);

				File notesSlideRelsDic = new File(strTmpFolderPath + File.separator + "ppt" + File.separator
						+ "notesSlides" + File.separator + "_rels");
				if (notesSlideRelsDic.isDirectory()) {
					for (File notesSlideRelsFile : notesSlideRelsDic.listFiles()) {
						if (notesSlideRelsFile.isFile()) {
							fileManager.addFileToZip(zipSklOut, strTmpFolderPath, notesSlideRelsFile.getAbsolutePath());
						}
					}
				}
				File slideRelsDic = new File(strTmpFolderPath + File.separator + "ppt" + File.separator + "slides"
						+ File.separator + "_rels");
				if (slideRelsDic.isDirectory()) {
					for (File slideRelsFile : slideRelsDic.listFiles()) {
						if (slideRelsFile.isFile()) {
							fileManager.addFileToZip(zipSklOut, strTmpFolderPath, slideRelsFile.getAbsolutePath());
						}
					}
				}

				secondMonitor.worked(1);
				secondMonitor.done();
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}

				// 第 3 步
				parseSlideMaster(Progress.getSubMonitor(monitor, 2));
				parseSlideLayout();
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}

				// 第 4 步
				parseSlideMasterRels(Progress.getSubMonitor(monitor, 2));
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}

				// 第 5 步
				getSlideAndSort(Progress.getSubMonitor(monitor, 1));

				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}

				// 第 6, 7 步
				parseSlideBySort(Progress.getSubMonitor(monitor, 5));
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("preference.cancel"));
				}

				writeOut("</body>\n</file>\n</xliff>");
				out.close();
				zipSklOut.flush();
				zipSklOut.close();

				IProgressMonitor thirdMonitor = Progress.getSubMonitor(monitor, 1);
				thirdMonitor.beginTask(Messages.getString("pptx.PPTX2XLIFF.task3"), 1);
				thirdMonitor.subTask("");
				// 第 9 步
				thirdMonitor.worked(1);
				thirdMonitor.done();
				result.put(Converter.ATTR_XLIFF_FILE, strXLIFFPath);
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(Messages.getString("pptx.PPTX2XLIFF.logger1"), e);
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("pptx.PPTX2XLIFF.msg1"),
						e);
			} finally {
				fileManager.deleteFileOrFolder(new File(strTmpFolderPath));
				monitor.done();
			}

			return result;
		}

		/**
		 * 解析 ppt/slideMasters/_rels 目录下的 rels 文件，确定 slideMasterN.xml 与 slideLayoutN.xml 的对应关系
		 * @throws XPathParseException
		 * @throws XPathEvalException
		 * @throws NavException
		 *             ;
		 */
		private void parseSlideMasterRels(IProgressMonitor monitor) throws XPathParseException, XPathEvalException,
				NavException {
			File relsFile = new File(strTmpFolderPath + File.separator + "ppt" + File.separator + "slideMasters"
					+ File.separator + "_rels");
			if (relsFile.isDirectory()) {
				File[] arrRelFile = relsFile.listFiles();
				monitor.beginTask("", arrRelFile.length);
				VTDGen vg = new VTDGen();
				AutoPilot ap = new AutoPilot();
				VTDUtils vu = new VTDUtils();
				for (File relFile : arrRelFile) {
					String name = relFile.getName();
					monitor.subTask(MessageFormat.format(Messages.getString("pptx.PPTX2XLIFF.task4"), name));
					if (relFile.isFile() && name.toLowerCase().endsWith(".rels")) {
						if (vg.parseFile(relFile.getAbsolutePath(), true)) {
							ArrayList<String> lstLayout = new ArrayList<String>();
							VTDNav vn = vg.getNav();
							ap.bind(vn);
							vu.bind(vn);
							ap.selectXPath("/Relationships/Relationship");
							while (ap.evalXPath() != -1) {
								String strTarget = vu.getCurrentElementAttribut("Target", "");
								if (strTarget.indexOf("slideLayout") != -1) {
									strTarget = strTarget.substring(strTarget.lastIndexOf("/") + 1);
									lstLayout.add(strTarget);
								}
							}
							mapLayout.put(name.substring(0, name.lastIndexOf(".")), lstLayout);
						}
					}
					monitor.worked(1);
				}
			}
			monitor.done();
		}

		/**
		 * 解析 ppt/slideMasters 目录下的 slideMasterN.xml 文件，查找 ph 中 type 不是 dt 和 sldNum 的节点，<br/>
		 * 取出其 type, idx 属性值和坐标值，因为有的幻灯片中节点的坐标是存放在此文件中的
		 * @throws Exception
		 */
		private void parseSlideMaster(IProgressMonitor monitor) throws Exception {
			File slideMasterDic = new File(strTmpFolderPath + File.separator + "ppt" + File.separator + "slideMasters");
			if (slideMasterDic.isDirectory()) {
				VTDGen vg = new VTDGen();
				VTDUtils vu = new VTDUtils();
				AutoPilot ap = new AutoPilot();
				ap.declareXPathNameSpace(PREFIX_A, NAMESPACE_A);
				ap.declareXPathNameSpace(PREFIX_P, NAMESPACE_P);
				File[] arrSlideMasterFile = slideMasterDic.listFiles();
				monitor.beginTask("", arrSlideMasterFile.length);
				for (File slideMasterFile : arrSlideMasterFile) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("preference.cancel"));
					}
					monitor.subTask(MessageFormat.format(Messages.getString("pptx.PPTX2XLIFF.task4"),
							slideMasterFile.getName()));
					if (slideMasterFile.isFile() && slideMasterFile.getName().toLowerCase().endsWith(".xml")) {
						if (vg.parseFile(slideMasterFile.getAbsolutePath(), true)) {
							String name = slideMasterFile.getName();
							VTDNav vn = vg.getNav();
							vu.bind(vn);
							ap.bind(vn);
							ap.selectXPath("/p:sldMaster/p:cSld/p:spTree//p:sp[descendant::p:ph[not(@type='dt') and not(@type='sldNum')]]");
							ArrayList<String[]> lstFld = new ArrayList<String[]>();
							while (ap.evalXPath() != -1) {
								
								String strType = getElementAttribute(".//p:ph", "type", vn);
								String idx = getElementAttribute(".//p:ph", "idx", vn);
								if (strType == null && idx == null) {
									continue;
								}

								String strX = getElementAttribute(".//a:xfrm/a:off", "x", vn);
								String strY = getElementAttribute(".//a:xfrm/a:off", "y", vn);
								if (strX != null && strY != null) {
									lstFld.add(new String[] { strType, idx, strX, strY });
								}
							}
							if (lstFld.size() > 0) {
								mapSldMasterPH.put(name, lstFld);
							}
						}
					}
					monitor.worked(1);
				}
			}
			monitor.done();
		}
		
		/**
		 * 获取当前元素节点的某个属性值。如果当前节点没有该属性，返回默认值。
		 * @exception NavException
		 */
		public String getCurrentElementAttribut(String AttributName, String defaultValue, VTDNav vn) throws NavException {
			int index = vn.getAttrVal(AttributName);
			return index != -1 ? vn.toString(index) : defaultValue;
		}
		
		
		public String getElementAttribute(String elementXPath, String attributeName, VTDNav vn) throws XPathParseException,
				XPathEvalException, NavException {
			String text = null;
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace(PREFIX_A, NAMESPACE_A);
			ap.declareXPathNameSpace(PREFIX_P, NAMESPACE_P);
			ap.selectXPath(elementXPath);
			vn.push();
			if (ap.evalXPath() != -1) {
				int inx = vn.getAttrVal(attributeName);
				if (inx != -1) {
					text = vn.toString(inx);
				}
			}
			vn.pop();
			return text;
		}
		

		/**
		 * 获取 ppt/slides 目录下的 slideN.xml 文件并排序 ;
		 * @throws NavException
		 * @throws XPathParseException
		 * @throws XPathEvalException
		 */
		private void getSlideAndSort(IProgressMonitor monitor) throws NavException, XPathParseException,
				XPathEvalException {
			monitor.beginTask(Messages.getString("pptx.PPTX2XLIFF.task5"), 2);
			monitor.subTask("");
			VTDGen vg = new VTDGen();
			AutoPilot ap = new AutoPilot();
			ap.declareXPathNameSpace(PREFIX_P, NAMESPACE_P);
			VTDUtils vu = new VTDUtils();
			ArrayList<String> lstRId = new ArrayList<String>();
			
			
			
			if (vg.parseFile(strTmpFolderPath + File.separator + "ppt" + File.separator + "presentation.xml", true)) {
				VTDNav vn = vg.getNav();
				ap.bind(vn);
				vu.bind(vn);
				ap.selectXPath("/p:presentation/p:sldIdLst/p:sldId");
				while (ap.evalXPath() != -1) {
					String strRid = vu.getCurrentElementAttribut("r:id", null);
					if (strRid != null) {
						lstRId.add(strRid);
					}
				}
			}
			monitor.worked(1);
			if (lstRId.size() > 0) {
				if (vg.parseFile(strTmpFolderPath + File.separator + "ppt" + File.separator + "_rels" + File.separator
						+ "presentation.xml.rels", true)) {
					VTDNav vn = vg.getNav();
					ap.bind(vn);
					vu.bind(vn);
					StringBuffer sbCondition = new StringBuffer();
					for (int i = 0; i < lstRId.size(); i++) {
						if (i == 0) {
							sbCondition.append("Relationship[@Id='" + lstRId.get(i) + "']");
						} else {
							sbCondition.append("|Relationship[@Id='" + lstRId.get(i) + "']");
						}
					}
					ap.selectXPath("/Relationships/" + sbCondition.toString() + "");
					while (ap.evalXPath() != -1) {
						String strSlidePath = vu.getCurrentElementAttribut("Target", null);
						if (strSlidePath != null) {
							lstSlide.add(strSlidePath.substring(strSlidePath.lastIndexOf("/") + 1));
						}
					}
				}
			}
			monitor.worked(1);
			monitor.done();
		}

		/**
		 * 解析 slideLayoutN.xml 文件，获取坐标值，因为有的幻灯片中节点的坐标是存放在此文件中的
		 * @throws NavException
		 * @throws XPathParseException
		 * @throws XPathEvalException
		 */
		private void parseSlideLayout() throws NavException, XPathParseException, XPathEvalException {
			File slideLayoutDic = new File(strTmpFolderPath + File.separator + "ppt" + File.separator + "slideLayouts");
			if (slideLayoutDic.isDirectory()) {
				VTDGen vg = new VTDGen();
				AutoPilot ap = new AutoPilot();
				ap.declareXPathNameSpace(PREFIX_A, NAMESPACE_A);
				ap.declareXPathNameSpace(PREFIX_P, NAMESPACE_P);
				VTDUtils vu = new VTDUtils();
				File[] arrSlideLayoutFile = slideLayoutDic.listFiles();
				for (File slideLayoutFile : arrSlideLayoutFile) {
					if (slideLayoutFile.isFile() && slideLayoutFile.getName().toLowerCase().endsWith(".xml")) {
						if (vg.parseFile(slideLayoutFile.getAbsolutePath(), true)) {
							String name = slideLayoutFile.getName();
							VTDNav vn = vg.getNav();
							ap.bind(vn);
							vu.bind(vn);
							ap.selectXPath("/p:sldLayout/p:cSld/p:spTree//p:sp[descendant::p:ph[not(@type='dt') and not(@type='sldNum')]]");
							ArrayList<String[]> lstPH = new ArrayList<String[]>();
							while (ap.evalXPath() != -1) {
								String strType = getElementAttribute(".//p:ph", "type", vn);
								String idx = getElementAttribute(".//p:ph", "idx", vn);
								if (strType == null && idx == null) {
									continue;
								}

								String strX = getElementAttribute(".//a:xfrm/a:off", "x", vn);
								String strY = getElementAttribute(".//a:xfrm/a:off", "y", vn);
								if (strX != null && strY != null) {
									lstPH.add(new String[] { strType, idx, strX, strY });
								}
							}
							if (lstPH.size() > 0) {
								mapSldLayoutPH.put(name, lstPH);
							}
						}
					}
				}
			}
		}

		private void parseSlideBySort(IProgressMonitor monitor) throws Exception {
			monitor.beginTask(Messages.getString("pptx.PPTX2XLIFF.task6"), lstSlide.size());
			monitor.subTask("");
			VTDGen vg = new VTDGen();
			AutoPilot apRels = new AutoPilot();
			AutoPilot apSlide = new AutoPilot();
			apSlide.declareXPathNameSpace(PREFIX_A, NAMESPACE_A);
			apSlide.declareXPathNameSpace(PREFIX_P, NAMESPACE_P);
			AutoPilot apParent = new AutoPilot();
			apParent.declareXPathNameSpace(PREFIX_A, NAMESPACE_A);
			apParent.declareXPathNameSpace(PREFIX_P, NAMESPACE_P);
			VTDUtils vu = new VTDUtils();
			StringBuffer sbContent = new StringBuffer();
			XMLModifier xm = new XMLModifier();
			boolean isUpdate = false;
			
			List<Integer> removedIndexList = new ArrayList<Integer>();
			for (final String slideName : lstSlide) {
				removedIndexList.clear();
				
				isUpdate = false;
				String strSlideLayout = null;
				String strNotesSlide = null;
				// 先解析对应的 rels 文件
				if (vg.parseFile(strTmpFolderPath + File.separator + "ppt" + File.separator + "slides" + File.separator
						+ "_rels" + File.separator + slideName + ".rels", true)) {
					VTDNav vn = vg.getNav();
					apRels.bind(vn);
					vu.bind(vn);
					apRels.selectXPath("/Relationships/Relationship");
					while (apRels.evalXPath() != -1) {
						String strTarget = getCurrentElementAttribut("Target", null, vn);
						if (strTarget != null) {
							if (strSlideLayout == null && strTarget.indexOf("slideLayout") != -1) {
								strSlideLayout = strTarget.substring(strTarget.lastIndexOf("/") + 1);
							}
							if (strNotesSlide == null && strTarget.indexOf("notesSlide") != -1) {
								strNotesSlide = strTarget.substring(strTarget.lastIndexOf("/") + 1);
							}
						}
						if (strSlideLayout != null && strNotesSlide != null) {
							break;
						}
					}
				}
				final String slideLayoutName = strSlideLayout;
				String strSlideMaster = null;
				if (strSlideLayout != null && mapLayout.size() > 0) {
					Iterator<Entry<String, ArrayList<String>>> it = mapLayout.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, ArrayList<String>> entry = (Entry<String, ArrayList<String>>) it.next();
						ArrayList<String> lstLayout = entry.getValue();
						if (lstLayout.contains(strSlideLayout)) {
							strSlideMaster = entry.getKey();
						}
					}
				}
				final String slideMasterName = strSlideMaster;
				// 解析 slideN.xml 文件
				vg.clear();
				String strSlideFilePath = strTmpFolderPath + File.separator + "ppt" + File.separator + "slides"
						+ File.separator + slideName;
				vg = new VTDGen();
				if (vg.parseFile(strSlideFilePath, true)) {
					ArrayList<String[]> lstCoordinate = new ArrayList<String[]>();
					VTDNav vn = vg.getNav();
					apSlide.bind(vn);
					vu.bind(vn);
					xm.bind(vn);
					apParent.bind(vn);
					apSlide.selectXPath("/p:sld/p:cSld/p:spTree//p:sp[descendant::node()[name()='a:t']] "
							+ "| /p:sld/p:cSld/p:spTree//p:graphicFrame[descendant::node()[name()='a:t']]");
					// 标记是否有坐标值
					boolean isContainOff = true;
					while (apSlide.evalXPath() != -1) {
						String strX = getElementAttribute(".//p:xfrm/a:off | .//a:xfrm/a:off", "x", vn);
						String strY = getElementAttribute(".//p:xfrm/a:off | .//a:xfrm/a:off", "y", vn);
						String strType = getElementAttribute(".//p:ph", "type", vn);
						String strIdx = getElementAttribute(".//p:ph", "idx", vn);
						String name = vu.getCurrentElementName();
						vn.push();
						String parentX = null;
						String parentY = null;
						if (name.equals("p:sp")) {
							// 如果 p:sp 的父节点为 p:grpSp，则要将 p:grpSp/p:grpSpPr/a:xfrm/a:off 的 x，y 值取出，因为 p:grpSpPr
							// 的坐标是相对整个幻灯片的坐标，而 p:sp 的坐标则是相对 p:grpSpPr 的。
							apParent.selectXPath("parent::node()[name()='p:grpSp']");
							if (apParent.evalXPath() != -1) {
								parentX = getElementAttribute("./p:grpSpPr/a:xfrm/a:off", "x", vn);
								parentY = getElementAttribute("./p:grpSpPr/a:xfrm/a:off", "y", vn);
							}
						}
						vn.pop();
						if (strX == null && strY == null) {
							// ph 节点无坐标
							if (strType != null) {
								if (!strType.equals("dt") && !strType.equals("sldNum")) {
									isContainOff = false;
								}
							} else {
								isContainOff = false;
							}
						}
						if ((strType == null || (!strType.equals("dt") && !strType.equals("sldNum")))) {
							lstCoordinate.add(new String[] { strType, strIdx, strX, strY, parentX, parentY });
						}
					}
					removeDuplicateWithOrder(lstCoordinate);
					if (isContainOff) {
						Collections.sort(lstCoordinate, new Comparator<String[]>() {
							public int compare(String[] o1, String[] o2) {
								int x1 = Integer.parseInt(o1[2]);
								int y1 = Integer.parseInt(o1[3]);
								int parentX1 = 0;
								int parentY1 = 0;
								int parentX2 = 0;
								int parentY2 = 0;
								if (o1[4] != null) {
									parentX1 = Integer.parseInt(o1[4]);
								}
								if (o1[5] != null) {
									parentY1 = Integer.parseInt(o1[5]);
								}
								int x2 = Integer.parseInt(o2[2]);
								int y2 = Integer.parseInt(o2[3]);
								if (o2[4] != null) {
									parentX2 = Integer.parseInt(o2[4]);
								}
								if (o2[5] != null) {
									parentY2 = Integer.parseInt(o2[5]);
								}
								if (o1[4] != null && o1[5] != null && o2[4] != null && o2[5] != null) {
									if (parentY1 != parentY2) {
										return parentY1 - parentY2;
									} else if (parentX1 != parentX2) {
										return parentX1 - parentX2;
									} else {
										if (y1 != y2) {
											return y1 - y2;
										} else {
											return x1 - x2;
										}
									}
								} else if (o1[4] != null && o1[5] != null) {
									if (parentY1 != y2) {
										return parentY1 - y2;
									} else {
										return parentX1 - x2;
									}
								} else if (o2[4] != null && o2[5] != null) {
									if (y1 != parentY2) {
										return y1 - parentY2;
									} else {
										return x1 - parentX2;
									}
								} else {
									if (y1 != y2) {
										return y1 - y2;
									} else {
										return x1 - x2;
									}
								}
							}
						});
					} else {
						Collections.sort(lstCoordinate, new Comparator<String[]>() {
							public int compare(String[] o1, String[] o2) {
								int x1 = o1[2] == null ? 0 : Integer.parseInt(o1[2]);
								int y1 = o1[3] == null ? 0 : Integer.parseInt(o1[3]);
								int x2 = o2[2] == null ? 0 : Integer.parseInt(o2[2]);
								int y2 = o2[3] == null ? 0 : Integer.parseInt(o2[3]);
								ArrayList<String[]> lstLayoutPH = mapSldLayoutPH.get(slideLayoutName);
								if (lstLayoutPH == null || lstLayoutPH.size() == 0) {
									lstLayoutPH = mapSldMasterPH.get(slideMasterName);
								}
								if (o1[0] != null && o1[0].equals("ftr")) {
									y1 = Integer.MAX_VALUE;
								} else {
									if (lstLayoutPH != null) {
										for (String[] arrLayoutPH : lstLayoutPH) {
											if (o1[0] != null && arrLayoutPH[0] != null && arrLayoutPH[0].equals(o1[0])) {
												x1 = Integer.parseInt(arrLayoutPH[2]);
												y1 = Integer.parseInt(arrLayoutPH[3]);
												break;
											} else if (o1[1] != null && arrLayoutPH[1] != null
													&& arrLayoutPH[1].equals(o1[1])) {
												x1 = Integer.parseInt(arrLayoutPH[2]);
												y1 = Integer.parseInt(arrLayoutPH[3]);
												break;
											}
										}
									}
								}
								if (o2[0] != null && o2[0].equals("ftr")) {
									y2 = Integer.MAX_VALUE;
								} else {
									if (lstLayoutPH != null) {
										for (String[] arrLayoutPH : lstLayoutPH) {
											if (o2[0] != null && arrLayoutPH[0] != null && arrLayoutPH[0].equals(o2[0])) {
												x2 = Integer.parseInt(arrLayoutPH[2]);
												y2 = Integer.parseInt(arrLayoutPH[3]);
												break;
											} else if (o2[1] != null && arrLayoutPH[1] != null
													&& arrLayoutPH[1].equals(o2[1])) {
												x2 = Integer.parseInt(arrLayoutPH[2]);
												y2 = Integer.parseInt(arrLayoutPH[3]);
												break;
											}
										}
									}
								}
								if (y1 != y2) {
									return y1 - y2;
								} else {
									return x1 - x2;
								}
							}
						});
					}
					
					for (String[] arrCoordinate : lstCoordinate) {
						StringBuffer sbCondition = new StringBuffer("[descendant::node()[name()='a:t']");
						if (arrCoordinate[0] != null && arrCoordinate[1] != null) {
							sbCondition.append(" and descendant::node()[name()='p:ph' and @type='" + arrCoordinate[0]
									+ "' and @idx='" + arrCoordinate[1] + "']");
						} else if (arrCoordinate[0] != null) {
							sbCondition.append(" and descendant::node()[name()='p:ph' and @type='" + arrCoordinate[0]
									+ "']");
						} else if (arrCoordinate[1] != null) {
							sbCondition.append(" and descendant::node()[name()='p:ph' and @idx='" + arrCoordinate[1]
									+ "']");
						}

						if (arrCoordinate[2] != null && arrCoordinate[3] != null) {
							sbCondition.append(" and descendant::node()[name()='a:off' and @x='" + arrCoordinate[2]
									+ "' and @y='" + arrCoordinate[3] + "']");
						}
						if (arrCoordinate[4] != null && arrCoordinate[5] != null) {
							// p:sp 的父节点为 p:grpSp，判断 p:grpSp/p:grpSpPr/a:xfrm/a:off 的 x,y 值， p:sp 与 p:grpSp 是同胞
							sbCondition
									.append(" and parent::node()[name()='p:grpSp'] and (preceding-sibling::node()[name()='p:grpSpPr' and a:xfrm/a:off[@x='"
											+ arrCoordinate[4]
											+ "' and @y='"
											+ arrCoordinate[5]
											+ "']] or following-sibling::node()[name()='p:grpSpPr' and a:xfrm/a:off[@x='"
											+ arrCoordinate[4] + "' and @y='" + arrCoordinate[5] + "']])");
						}
						sbCondition.append("]");
						String xpath = "/p:sld/p:cSld/p:spTree//p:sp" + sbCondition + "/p:txBody/a:p "
								+ "| /p:sld/p:cSld/p:spTree//p:graphicFrame" + sbCondition + "//a:txBody/a:p";
						
						apSlide.selectXPath(xpath);
						while (apSlide.evalXPath() != -1) {
							sbContent.delete(0, sbContent.length());
							String strFragment = vu.getElementFragment();
							if (strFragment.indexOf("<a:t>") == -1) {
								continue;
							}
							String strText = strFragment.substring(strFragment.indexOf("<a:t>") + "<a:t>".length(),
									strFragment.lastIndexOf("</a:t>"));
							sbContent.append(strFragment.substring(0, strFragment.indexOf("<a:t>") + "<a:t>".length()))
									.append(strFragment.substring(strFragment.lastIndexOf("</a:t>")));
							if (blnSegByElement) {
								writeSegment(strText);
								sbContent.insert(sbContent.lastIndexOf("</a:t>"), "%%%" + segNum++ + "%%%");
							} else {
								String[] segs = segmenter.segment(strText);
								for (int h = 0; h < segs.length; h++) {
									String seg = segs[h];
									writeSegment(seg);
									sbContent.insert(sbContent.lastIndexOf("</a:t>"), "%%%" + segNum++ + "%%%");
								}
							}
							
							
							int currentIndex = vn.getCurrentIndex();
							if (!removedIndexList.contains(currentIndex)) {
								xm.remove(vn.getElementFragment());
								xm.insertAfterElement(sbContent.toString().getBytes());
								
								isUpdate = true;
								removedIndexList.add(currentIndex);
							}
							
						}
					}
					if (isUpdate) {
						xm.output(strSlideFilePath + ".skl");
						fileManager.addFileToZip(zipSklOut, strTmpFolderPath, strSlideFilePath + ".skl");
						isUpdate = false;
					} else {
						fileManager.addFileToZip(zipSklOut, strTmpFolderPath, strSlideFilePath);
					}
				}
				if (strNotesSlide != null) {
					String strNotesSlidePath = strTmpFolderPath + File.separator + "ppt" + File.separator
							+ "notesSlides" + File.separator + strNotesSlide;
					if (vg.parseFile(strNotesSlidePath, true)) {
						VTDNav vn = vg.getNav();
						apSlide.bind(vn);
						vu.bind(vn);
						xm.bind(vn);
						StringBuffer sbXpath = new StringBuffer();
						// notesSlideN.xml 文件中按照 type 为 body, hdr, ftr 的顺序查找，如果过滤备注文本，则是按照 type 为 hdr, ftr 的顺序查找。
						if (!blnIsFilterNote) {
							sbXpath.append("/p:notes/p:cSld/p:spTree/p:sp[p:nvSpPr/p:nvPr/p:ph[@type='body']] | ");
						}
						sbXpath.append("/p:notes/p:cSld/p:spTree/p:sp[p:nvSpPr/p:nvPr/p:ph[@type='hdr']] | /p:notes/p:cSld/p:spTree/p:sp[p:nvSpPr/p:nvPr/p:ph[@type='ftr']]");
						apSlide.selectXPath(sbXpath.toString());

						while (apSlide.evalXPath() != -1) {
							sbContent.delete(0, sbContent.length());
							String strFragment = vu.getElementFragment();
							if (strFragment.indexOf("<a:t>") == -1) {
								continue;
							}
							String strText = strFragment.substring(strFragment.indexOf("<a:t>") + "<a:t>".length(),
									strFragment.lastIndexOf("</a:t>"));
							sbContent.append(strFragment.substring(0, strFragment.indexOf("<a:t>") + "<a:t>".length()))
									.append(strFragment.substring(strFragment.lastIndexOf("</a:t>")));
							if (blnSegByElement) {
								writeSegment(strText);
								sbContent.insert(sbContent.lastIndexOf("</a:t>"), "%%%" + segNum++ + "%%%");
							} else {
								String[] segs = segmenter.segment(strText);
								for (int h = 0; h < segs.length; h++) {
									String seg = segs[h];
									writeSegment(seg);
									sbContent.insert(sbContent.lastIndexOf("</a:t>"), "%%%" + segNum++ + "%%%");
								}
							}
							xm.remove(vn.getElementFragment());
							xm.insertAfterElement(sbContent.toString().getBytes());
							isUpdate = true;
						}

						if (isUpdate) {
							xm.output(strNotesSlidePath + ".skl");
							fileManager.addFileToZip(zipSklOut, strTmpFolderPath, strNotesSlidePath + ".skl");
							isUpdate = false;
						} else {
							fileManager.addFileToZip(zipSklOut, strTmpFolderPath, strNotesSlidePath);
						}
					}
				}
				monitor.worked(1);
			}
			monitor.done();
		}

		/**
		 * 移除 list 中的重复数据
		 * @param list
		 *            ;
		 */
		public void removeDuplicateWithOrder(List<String[]> list) {
			for (int i = 0; i < list.size() - 1; i++) {
				for (int j = list.size() - 1; j > i; j--) {
					String[] arr1 = list.get(j);
					String[] arr2 = list.get(i);
					boolean isEqual = true;
					for (int len = 0; len < (arr1.length > arr2.length ? arr2.length : arr1.length); len++) {
						if (!cleanNull(arr1[len]).equals(cleanNull(arr2[len]))) {
							isEqual = false;
							break;
						}
					}
					if (isEqual) {
						list.remove(j);
					}
				}
			}
		}

		public String cleanNull(String string) {
			return string == null ? "" : string;
		}

		/**
		 * 将 strText 写入 XLIFF 文件
		 * @param strText
		 * @throws IOException
		 *             ;
		 */
		private void writeSegment(String strText) throws IOException {
			writeOut("<trans-unit id=\"" + segNum + "\">\n");
			String phText = "<ph>";
			String phEndText = "</ph>";
			int phId = 1;
			StringBuffer sbText = new StringBuffer(strText);
			int index = 0;
			int endIndex = 0;
			while (true) {
				if (index >= 0) {
					index = sbText.indexOf("</a:t>", index == 0 ? index : index + 1);
					if (index != -1) {
						sbText.insert(index, phText);
						index += phText.length();
						endIndex = sbText.indexOf("<a:t>", index == 0 ? index : index + 1);
						sbText.insert(endIndex + "<a:t>".length(), phEndText);
						String subText = TextUtil.cleanSpecialString(sbText.substring(index,
								endIndex + "<a:t>".length()));
						sbText.replace(index, endIndex + "<a:t>".length(), subText);
						index = endIndex;
					}
				} else {
					break;
				}
			}
			strText = sbText.toString();
			strText = strText.replaceAll(phEndText + phText, "");
			while (strText.indexOf(phText) != -1) {
				strText = strText.replaceFirst(phText, "<ph id=\"" + phId++ + "\">");
			}
			writeOut("<source xml:lang=\"" + strSrcLang + "\">" + strText + "</source>\n");
			writeOut("</trans-unit>\n\n");
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
			String dirName = "tmpPPTXfolder" + System.currentTimeMillis();
			File dir = new File(sysTemp + File.separator + dirName);
			dir.mkdirs();
			strTmpFolderPath = dir.getAbsolutePath();
			fileManager.releaseZipToFile(source, strTmpFolderPath);
		}
	}
}
