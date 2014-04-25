package net.heartsome.cat.ts.core.qa;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 对xml文件(xliff,tbx,tmx)的处理，包括数据的读取存储等
 * 涉及到两个功能，一是品质检查，二是文件分析
 * 备注：若方法来自于XLFHandler,那就备注为“来自XLFHandler”；
 * @author robert	2011-11-11
 */
public class QAXmlHandler {
	private Hashtable<String, VTDNav> vnMap = new Hashtable<String, VTDNav>();
	private Hashtable<String, AutoPilot> apMap = new Hashtable<String, AutoPilot>();
	/**  xliff文件的默认命名空间 */
	private Hashtable<String, String> xliffXmlnsMap = new Hashtable<String, String>(); 
	/** 缓存的VTD导航对象 */
	private VTDNav vnRead;
	/** 日志管理器 **/
	private final static Logger logger = LoggerFactory.getLogger(QAXmlHandler.class);
	/** 文件历史访问列表。键为文件名，值为文本段的索引，空字符串值为默认值，表示第一个文本段。 **/
	private Map<String, String> accessHistory = createFileHistory(10, 10);
	/** 项目中文件中翻译单元计数映射表，键为项目中的XILFF文件，值为该文件翻译单元总数。 **/
	private LinkedHashMap<String, Integer> tuSizeMap = new LinkedHashMap<String, Integer>();
	public static String XPATH_ALL_TU = "/xliff/file/body//trans-unit[source/text()!='' or source/*]";
	private VTDGen tagVg ;
	public static final String hsR7NSUrl = "http://www.heartsome.net.cn/2008/XLFExtension";
	public static final String hsNSPrefix = "hs";
	
	public QAXmlHandler(){
		tagVg = new VTDGen();
	}
	
	public Map<String, Object> openFile(String filename) {
		return openFile(new File(filename), null);
	}
	
	public Map<String, Object> openFile(String filename, IProgressMonitor monitor) {
		return openFile(new File(filename), monitor);
	}
	/**
	 * 解析文件（同时操作进度条）
	 * @param file
	 * @param monitor
	 * @param totalWork
	 * @return ;
	 */
	private Map<String, Object> openFile(File file, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask(MessageFormat.format(Messages.getString("qa.QAXmlHandler.task1"), file.getName()), 10);
			String filename = file.getAbsolutePath();
			
			// 解析文件并获取索引
			VTDGen vgRead = new VTDGen();
			if (vgRead.parseFile(filename, true)) {
				vnRead = vgRead.getNav();
				if (monitor.isCanceled()) {
					return getReturnResult();
				}
				
				monitor.worked(3);
				try {
					AutoPilot ap = new AutoPilot(vnRead);
					apMap.put(filename, ap);

					// 记录xliff文件命名空间
		//			ap.selectXPath("namespace-uri(/xliff)");
		//			String xmlns;
		//			if ((xmlns = ap.evalXPathToString()) != null) {
		//				xliffXmlnsMap.put(filename, xmlns);
		//			} else {
		//				String errorMsg = "文件“" + filename + "”，不是合法的 XLIFF 文件！";
		//				return getErrorResult(errorMsg, null);
		//			}
					
					monitor.worked(1);
					ap.resetXPath();

					if (monitor.isCanceled()) {
						return getReturnResult();
					}

					ap.selectXPath("count(" + XPATH_ALL_TU + ")");
					int countAllTU = (int) ap.evalXPathToNumber(); // 整个xliff文件中的trans-unit节点的个数
					
					monitor.worked(6);

					tuSizeMap.put(filename, countAllTU);
					vnMap.put(filename, vnRead);
				}
				
				catch (XPathParseException e) {
					String errorMsg = Messages.getString("qa.QAXmlHandler.logger1");
					logger.error(errorMsg, e);
					return getErrorResult(errorMsg, e);
				}
				accessHistory.put(filename, "");
			} else {
				String errorMsg = MessageFormat.format(Messages.getString("qa.QAXmlHandler.logger2"), filename);
				logger.error(errorMsg);
				return getErrorResult(errorMsg, null);
			}
		} finally {
			monitor.done();
		}
		return getSuccessResult();
	}
	
	// 获取错误返回值。
	private Map<String, Object> getErrorResult(String msg, Throwable e) {
		if (QAConstant.MODE_DEBUG == QAConstant.RUNNING_MODE && e != null) {
			e.printStackTrace();
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(QAConstant.RETURNVALUE_RESULT, QAConstant.RETURNVALUE_RESULT_FAILURE);
		resultMap.put(QAConstant.RETURNVALUE_MSG, msg);
		resultMap.put(QAConstant.RETURNVALUE_EXCEPTION, e);
		return resultMap;
	}
	// 获取正确返回值并记录消息日志。
	@SuppressWarnings("unused")
	private Map<String, Object> getSuccessResult(String msg) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(QAConstant.RETURNVALUE_RESULT, QAConstant.RETURNVALUE_RESULT_SUCCESSFUL);
		resultMap.put(QAConstant.RETURNVALUE_MSG, msg);
		return resultMap;
	}

	// 获取正确返回值
	private Map<String, Object> getSuccessResult() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(QAConstant.RETURNVALUE_RESULT, QAConstant.RETURNVALUE_RESULT_SUCCESSFUL);
		return resultMap;
	}
	
	/**
	 * 点击进度条退出程序后的返回值
	 * @return
	 */
	private Map<String, Object> getReturnResult(){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(QAConstant.RETURNVALUE_RESULT, QAConstant.RETURNVALUE_RESULT_RETURN);
		return resultMap;
	}
	
	public LinkedHashMap<String, Integer> getTuSizeMap() {
		return tuSizeMap;
	}
	
	/**
	 * 创建文件历史访问列表。
	 * @param initSize 容器初始化大小。
	 * @param maxSize 容器最大大小。
	 * @return 返回一个同步的有序的文件历史访问列表容器。<br/>
	 * key 	 为历史访问文件路径。<br/>
	 * value 为历史访问文件关闭时焦点所在的文本段或是术语的索引。
	 * */
	public Map<String, String> createFileHistory(final int initSize,
			final int maxSize) {
		return Collections.synchronizedMap(new LinkedHashMap<String, String>(
				initSize, 0.75f, true) {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("rawtypes")
			public boolean removeEldestEntry(Map.Entry entry) {
				return size() > maxSize;
			}
		});
	}
	
	/**
	 * 验证vn与ap是否存在，否则进行提示
	 */
	public void validNull(VTDNav vn, AutoPilot ap, String xmlPath){
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xmlPath);
		Assert.isNotNull(ap, Messages.getString("qa.QAXmlHandler.msg1") + xmlPath);
	}
	
	
	protected boolean saveAndReparse(XMLModifier xm, String fileName) {
		boolean isSaved = save(xm, fileName);
		if (!isSaved) {
			logger.debug(Messages.getString("qa.QAXmlHandler.logger3"));
			return false;
		}
		// 重新加载
		VTDGen vg = new VTDGen();
		if (vg.parseFile(fileName, true)) {
			vnMap.put(fileName, vg.getNav());
			return true;
		}
		return false;
	}

	/**
	 * 保存文件
	 * @param xm
	 *            XMLModifier对象
	 * @param fileName
	 *            文件名
	 * @return 是否保存成功;
	 */
	private boolean save(XMLModifier xm, String fileName) {
		return save(xm, new File(fileName));
	}
	
	/**
	 * 保存文件
	 * @param xm
	 *            XMLModifier对象
	 * @param fileName
	 *            文件名
	 * @return 是否保存成功;
	 */
	private boolean save(XMLModifier xm, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			xm.output(bos); // 写入文件
			bos.close();
			fos.close();

			IPath path = URIUtil.toPath(file.toURI());
			IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
			if (iFile != null) {
				iFile.refreshLocal(IResource.DEPTH_ZERO, null); // 同步导航视图和文件系统
			}
			return true;
		} catch (ModifyException e) {
			e.printStackTrace();
			logger.error(MessageFormat.format(Messages.getString("qa.QAXmlHandler.logger4"), file.getAbsolutePath()), e);
		} catch (TranscodeException e) {
			e.printStackTrace();
			logger.error(MessageFormat.format(Messages.getString("qa.QAXmlHandler.logger4"), file.getAbsolutePath()), e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(MessageFormat.format(Messages.getString("qa.QAXmlHandler.logger5"), file.getAbsolutePath()), e);
		} catch (CoreException e) {
			e.printStackTrace();
			logger.error(MessageFormat.format(Messages.getString("qa.QAXmlHandler.logger6"), file.getAbsolutePath()), e);
		}

		return false;
	}
	/**
	 * 文本段是否已经锁定,来自XLFHandler
	 * @param rowId
	 *            行的唯一标识
	 * @return ;
	 */
	public boolean isLocked(String rowId) {
		String translate = this.getTuProp(rowId, "translate");
		return (translate != null && "no".equalsIgnoreCase(translate));
	}
	/**
	 * 得到翻译单元的属性值	,来自XLFHandler
	 * @param rowId
	 *            行的唯一标识
	 * @param propName
	 *            属性名
	 * @return 属性值;
	 */
	public String getTuProp(String rowId, String propName) {
		VTDNav vn = getVTDNavByRowId(rowId);
		String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
		try {
			VTDUtils vu = new VTDUtils(vn);
			return vu.getValue(tuXPath + "/@" + propName);
		} catch (NavException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger7"), e);
		}
		return null;
	}
	
	/**
	 * 得到当前所解析文件的所有TU节点的总和
	 * @return ;
	 */
	public int getAllTUSize(){
		int tuSize = 0;
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		for (VTDNav vn : vnMap.values()) {
			ap.bind(vn);
			try {
				ap.selectXPath("count(/xliff/file/body//trans-unit)");
				tuSize += ap.evalXPathToNumber();
			}catch (Exception e) {
				logger.error(Messages.getString("qa.QAXmlHandler.logger8"), e);
			}
		}
		return tuSize;
	}
	
	/**
	 * 得到所有语言对 备注：重复，从XLFHandler.java中拷取
	 * @return 语言对的Map<br/>
	 *         key: 源语言；value: 对应的目标语言（可以是多个）
	 */
	public Map<String, ArrayList<String>> getLanguages() {
		TreeMap<String, ArrayList<String>> languages = new TreeMap<String, ArrayList<String>>();
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		VTDUtils vu = new VTDUtils();
		for (VTDNav vn : vnMap.values()) {
			ap.bind(vn);
			try {
				vu.bind(vn);
				ap.selectXPath("/xliff/file");
				while (ap.evalXPath() != -1) {
					String srcLanguage = vu.getCurrentElementAttribut("source-language", null);
					String tgtLanguage = vu.getCurrentElementAttribut("target-language", null);

					if (srcLanguage == null) {
						// TODO 该file节点不存在“source-language”属性，提醒添加
						continue;
					}
					if (tgtLanguage == null) {
						// TODO 该file节点不存在“target-language”属性，提醒添加
						continue;
					}
					ArrayList<String> tgtLanguages = languages.get(srcLanguage);
					if (tgtLanguages == null) {
						tgtLanguages = new ArrayList<String>();
						languages.put(srcLanguage, tgtLanguages);
					}
					if (!tgtLanguages.contains(tgtLanguage)) { // 未包含，就添加进去
						tgtLanguages.add(tgtLanguage);
					}
				}
			} catch (XPathParseException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.QAXmlHandler.logger9"), e);
			} catch (XPathEvalException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.QAXmlHandler.logger10"), e);
			} catch (NavException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.QAXmlHandler.logger11"), e);
			}
		}
		return languages;
	}
	
	/**
	 * 通过源与目标语言得到所有当前打开的文件包含的 RowId
	 * ///xliff/file//descendant::trans-unit[(source/text()!='' or source/*)]
	 * @return ;
	 */
	public List<String> getAllRowIdsByLanguages(String srcLanguage, String tgtLanguage) {
		List<String> allRowIds = new LinkedList<String>();
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		VTDUtils vu = new VTDUtils();
		for (Entry<String, VTDNav> entry : vnMap.entrySet()) {
			String fileName = entry.getKey();
			VTDNav vn = entry.getValue();
			ap.bind(vn);
			try {
				vu.bind(vn);
				// 查询相同目标与相同源文本的tu
				String XPATH_ALL_TU_BYLANGUAGE = "/xliff/file[upper-case(@source-language)=''{0}'' and upper-case(@target-language)=''{1}'']/body/descendant::trans-unit[(source/text()!='''' or source/*)]";
				String xpath = MessageFormat.format(XPATH_ALL_TU_BYLANGUAGE, new Object[] { srcLanguage, tgtLanguage });
				
				ap.selectXPath(xpath);
				while (ap.evalXPath() != -1) {
					String rowId = RowIdUtil.getRowId(vn, fileName);
					if (rowId != null) {
						allRowIds.add(rowId);
					}
				}
			} catch (NavException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.QAXmlHandler.logger11"), e);
			} catch (XPathParseException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.QAXmlHandler.logger9"), e);
			} catch (XPathEvalException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.QAXmlHandler.logger10"), e);
			}
		}
		return allRowIds;
	}
	
	
	/**
	 * 获取某节点的总数
	 * @return
	 */
	public int getNodeCount(String xlfPath, String nodeXpath){
		int nodeNum = 0;
		VTDNav vn = vnMap.get(xlfPath);
		AutoPilot ap = apMap.get(xlfPath);
		validNull(vn, ap, xlfPath);
		
		try {
			ap.selectXPath("count(" + nodeXpath + ")");
			nodeNum = (int) ap.evalXPathToNumber();
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger9"), e);
		}
		
		return nodeNum;
	}
	
	/**
	 * 获取trans-unit节点过滤后的值，过滤条件为不包括上下文匹配，不包括完全匹配，不包括已锁文本，过滤条件在首选项中设置
	 * @return	Map<String, String >	两个键值对，srcPureText --> 源文本的纯文本，tarPureText --> 目标文本的纯文本
	 * 如果返回的是null，则标志source节点无内容，这对与行号就不自增
	 */
	public Map<String, String> getFilteredTUPureText(String xlfPath, String nodeXpath, Map<String, Boolean> filterMap){
		Map<String, String> filteredTuPureTextMap = new HashMap<String, String>();
		VTDNav vn = vnMap.get(xlfPath);
		AutoPilot ap = new AutoPilot(vn);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xlfPath);
		AutoPilot childAp = new AutoPilot(vn);
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			while (ap.evalXPath() != -1) {
				
				vn.push();
				//取出源文本的纯文本之前，先查看其内容是否为空，若为空，则返回，没有source节点，也返回null
				childAp.selectXPath("./source");
				if (childAp.evalXPath() != -1) {	//因为标准里面只有一个source，因此此处用if不用while
					String srcContent = vUtils.getElementContent();
					//如果源文本为空或无值，则返回null
					if (srcContent == null || "".equals(srcContent)) {
						return null;
					}else {
						filteredTuPureTextMap.put("srcPureText", getTUPureText(vn));
					}
				}else {
					return null;
				}
				childAp.resetXPath();
				vn.pop();
				
				//首先过滤，如果有不应包括的文本段，则返回空
				if (!filterTheTU(vn, filterMap)) {
					return filteredTuPureTextMap;
				}
				
				//下面获取目标文本的纯文本，在之前先检查目标文本是否为空或为空值，若是，则返回null，若没有target节点，也返回空
				childAp.selectXPath("./target");
				if (childAp.evalXPath() != -1) {	//因为标准里面只有一个target，因此此处用if不用while
					String tarContent = vUtils.getElementContent();
					//如果源文本为空或无值，则返回null
					if (tarContent == null || "".equals(tarContent)) {
						return filteredTuPureTextMap;
					}else {
						filteredTuPureTextMap.put("tarPureText", getTUPureText(vn));
					}
				}else {
					return filteredTuPureTextMap;
				}
				childAp.resetXPath();
			}
			
		} catch (Exception e) {
			logger.error(Messages.getString("qa.QAXmlHandler.logger12"), e);
		}
		
		return filteredTuPureTextMap;
	}
	
	public VTDNav getVTDNav(String xmlLocation){
		VTDNav vn = vnMap.get(xmlLocation);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xmlLocation);
		return vn;
	}
	
	/**
	 * 获取trans-unit节点过滤后的值，过滤条件为不包括上下文匹配，不包括完全匹配，不包括已锁文本，过滤条件在首选项中设置
	 * @return Map<String, String > 六个键值对，srcPureText --> 源文本的纯文本，tarPureText --> 目标文本的纯文本， srcContent -->
	 *         源文本的内容，tarContent --> 目标文本的内容 , srcTag --> 源文本的标记, tarTag --> 目标文本的标记;
	 *         如果返回的是null，则标志source节点无内容，这对与行号就不自增
	 * @param xlfPath	
	 * @param nodeXpath	
	 * @param filterMap	过滤条件
	 * @return ;
	 */
	public QATUDataBean getFilteredTUText(String xlfPath, String nodeXpath, Map<String, Boolean> filterMap){
		QATUDataBean bean = new QATUDataBean();
		VTDNav vn = vnMap.get(xlfPath);
		AutoPilot ap = new AutoPilot(vn);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xlfPath);
		AutoPilot childAp = new AutoPilot(vn);
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			while (ap.evalXPath() != -1) {
				String rowId = RowIdUtil.getRowId(vn, xlfPath);
				bean.setRowId(rowId);
				
				vn.push();
				//取出源文本的纯文本之前，先查看其内容是否为空，若为空，则返回null，没有source节点，也返回null
				childAp.selectXPath("./source");
				if (childAp.evalXPath() != -1) {	//因为标准里面只有一个source，因此此处用if不用while
					String srcContent = vUtils.getElementContent();
					//如果源文本为空或无值，则返回null
					if (srcContent == null || "".equals(srcContent)) {
						return null;
					}else {
						bean.setSrcPureText(getTUPureText(vn));
						bean.setSrcContent(srcContent);
					}
				}else {
					return null;
				}
				childAp.resetXPath();
				vn.pop();
				
				//首先过滤，如果有不应包括的文本段，则返回一个空对象
				if (!filterTheTU(vn, filterMap)) {
					bean.setPassFilter(false);
					return bean;
				}
				
				//下面获取目标文本的纯文本，在之前先检查目标文本是否为空或为空值，若是，则返回null，若没有target节点，也返回空
				childAp.selectXPath("./target");
				if (childAp.evalXPath() != -1) {	//因为标准里面只有一个target，因此此处用if不用while
					String tarContent = vUtils.getElementContent();
					//如果源文本为空或无值，则返回空对象
					if (tarContent == null || "".equals(tarContent)) {
						bean.setTgtContent(tarContent);
						return bean;
					}else {
						bean.setTgtContent(tarContent);
						bean.setTgtPureText(getTUPureText(vn));
					}
				}else {
					return bean;
				}
				childAp.resetXPath();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger13"), e);
		}
		
		return bean;
	}
	
	/**
	 * 针对合并打开的文本段一致性检查的情况，获取trans-unit节点过滤后的值，过滤条件为不包括上下文匹配，不包括完全匹配，不包括已锁文本，过滤条件在首选项中设置
	 * @param xlfPath	
	 * @param nodeXpath	
	 * @param filterMap	过滤条件
	 * @return ;
	 */
	public Map<String, ParaConsisDataBean> getFilteredTUTextForMultiParaConsis(List<String> rowIdList, Map<String, Boolean> filterMap,
			boolean checkSameSource, boolean checkSameTarget, boolean srcIgnoreTag, boolean tarIgnoreTag){
		Map<String, ParaConsisDataBean> filteredTuTextMap = new HashMap<String, ParaConsisDataBean>();
		try {
			
			//为了提高系统性能，这里是否获取其纯文本，要进行一定的验证。
			//检查项有两个，即相同源文不同译文，相同译文不同源文，如果某项不检查，那么它的忽略标记为false
			if (!checkSameSource) {
				srcIgnoreTag = false;
			}
			if (!checkSameTarget) {
				tarIgnoreTag = false;
			}
			
			for(String rowId : rowIdList){
				String xlfPath = RowIdUtil.getFileNameByRowId(rowId);
				VTDNav vn = vnMap.get(xlfPath);
				AutoPilot ap = new AutoPilot(vn);
				Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xlfPath);
				AutoPilot childAp = new AutoPilot(vn);
				
				
				VTDUtils vUtils = new VTDUtils(vn);
				ap.selectXPath(RowIdUtil.parseRowIdToXPath(rowId));
				if (ap.evalXPath() != -1) {
					ParaConsisDataBean dataBean = new ParaConsisDataBean();
					
					vn.push();
					//取出源文本的纯文本之前，先查看其内容是否为空，若为空，则返回null，没有source节点，也返回null
					childAp.selectXPath("./source");
					if (childAp.evalXPath() != -1) {	//因为标准里面只有一个source，因此此处用if不用while
						String srcContent = vUtils.getElementContent();
						//如果源文本为空或无值，则返回null
						if (srcContent == null || "".equals(srcContent)) {
							continue;
						}else {
							//两个检查项中的忽略标记，若有一项为true，那么就必须获取纯文本
							if (srcIgnoreTag || tarIgnoreTag) {
								dataBean.setSrcContent(srcContent.trim());
								dataBean.setSrcPureText(getTUPureText(vn).trim());
							}else {
								dataBean.setSrcContent(srcContent.trim());
							}
						}
					}else {
						continue;
					}
					childAp.resetXPath();
					vn.pop();
					
					//首先过滤，如果有不应包括的文本段，则返回一个空对象
					if (!filterTheTU(vn, filterMap)) {
						continue;
					}
					
					vn.push();
					//下面获取目标文本的纯文本，在之前先检查目标文本是否为空或为空值，若是，则返回null，若没有target节点，也返回空
					childAp.selectXPath("./target");
					if (childAp.evalXPath() != -1) {	//因为标准里面只有一个target，因此此处用if不用while
						String tgtContent = vUtils.getElementContent();
						//如果源文本为空或无值，则返回空对象
						if (tgtContent == null || "".equals(tgtContent)) {
							continue;
						}else {
							//两个检查项中的忽略标记，若有一项为true，那么就必须获取纯文本
							if (srcIgnoreTag || tarIgnoreTag) {
								dataBean.setTgtContent(tgtContent.trim());
								dataBean.setTgtPureText(getTUPureText(vn).trim());
							}else {
								dataBean.setTgtContent(tgtContent.trim());
							}
						}
					}else {
						continue;
					}
					dataBean.setLineNumber(rowIdList.indexOf(rowId) + 1);
					vn.pop();
					filteredTuTextMap.put(rowId, dataBean);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger13"), e);
		}
		
		return filteredTuTextMap;
	}
	
	
	/**
	 * <div style='color:red'>注意：该方法与　类　{@link net.heartsome.cat.ts.core.file.XLFHandler#filterTheTU(VTDNav, Map)}完全一样，请注意保持两者同步</div>
	 * 过滤trans-unit节点，过滤条件为，过滤掉不包括的文本段，如不包括上下文匹配，不包括完全匹配，不包括已锁文本，
	 * 如果过滤不成功，就返回false，过滤成功(即没有不应包括的文本段)，就返回true
	 * @param vn
	 * @param filterMap		过滤条件
	 * @return
	 */
	public boolean filterTheTU(VTDNav vn, Map<String, Boolean> filterMap){
		vn.push();
		AutoPilot filterAp = new AutoPilot(vn);
		filterAp.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		try {
			//检查上下文匹配	translate(alt-trans/@match-quality, '%', '')=101
			if (filterMap.get(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE)) {
				// hs:matchType="TM" hs:quality="101"
				filterAp.selectXPath("translate(target/@hs:quality, '%', '')=101");
				//如果检查到有上下文匹配，就返回false,标志过滤未通过
				if (filterAp.evalXPathToBoolean()) {
					return false;
				}
				filterAp.resetXPath();
			}
			
			//检查完全匹配	translate(alt-trans/@match-quality, '%', '')=100
			if (filterMap.get(QAConstant.QA_PREF_FULLMATCH_NOTINCLUDE)) {
				filterAp.selectXPath("translate(target/@hs:quality, '%', '')=100");
				if (filterAp.evalXPathToBoolean()) {
					return false;
				}
				filterAp.resetXPath();
			}
			
			//检查已锁定的文本段	@translate='no'
			if (filterMap.get(QAConstant.QA_PREF_LOCKED_NOTINCLUDE)) {
				filterAp.selectXPath("@translate='no'");
				if (filterAp.evalXPathToBoolean()) {
					return false;
				}
				filterAp.resetXPath();
			}
		} catch (Exception e) {
			logger.error(Messages.getString("qa.QAXmlHandler.logger9"), e);
		}
		vn.pop();
		return true;

	}
	
	/**
	 * 针对trans-unit节点而言，获取其下source和target节点的纯文本字符串
	 * @param xlfPath
	 * @param nodeXpath
	 * @return	如果返回null,则证明这个节点是个空节点,要么没有这个节点，要么这个节点没有值
	 */
	public String getTUPureText(String xlfPath, String nodeXpath){
		VTDNav vn = vnMap.get(xlfPath);
		AutoPilot ap = apMap.get(xlfPath);
		validNull(vn, ap, xlfPath);
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			while (ap.evalXPath() != -1) {
				String content = vUtils.getElementContent();
				if (content == null || "".equals(content)) {
					return null;
				}
				return getTUPureText(vn);
			}
			
		} catch (Exception e) {
			logger.error(Messages.getString("qa.QAXmlHandler.logger12"), e);
		}
		return null;
		
	}
	
	/**
	 * 针对trans-unit节点而言，获取其下source和target节点的纯文本字符串
	 * @param xlfPath
	 * @param nodeXpath
	 * @return	
	 */
	public String getTUPureText(VTDNav vn){
		AutoPilot ap = new AutoPilot(vn);
		String pureText = "";
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			pureText = vUtils.getElementContent();
			//如果子节点大于0，那继续处理
			if (vUtils.getChildElementsCount() > 0) {
				ap.resetXPath();
				ap.selectXPath("./*");
				while (ap.evalXPath() != -1) {
					String childNodeName = vUtils.getCurrentElementName();
					if (QAConstant.QA_mrk.equals(childNodeName) 
							&& "term".equals(vUtils.getCurrentElementAttribut("mtype", ""))) {
						if (vUtils.getChildElementsCount() <= 0) {
							String childFrag = vUtils.getElementFragment();
							String childContent = vUtils.getElementContent();
							childContent = childContent == null ? "" : childContent;
							pureText = pureText.replace(childFrag, childContent);
						}else {
							String childFrag = vUtils.getElementFragment();
							String childContent = getTUPureText(vn);
							childContent = childContent == null ? "" : childContent;
							pureText = pureText.replace(childFrag, childContent);
						}
					}else if (QAConstant.QA_g.equals(childNodeName) || QAConstant.QA_sub.equals(childNodeName)) {
						if (vUtils.getChildElementsCount() <= 0) {
							String childFrag = vUtils.getElementFragment();
							String childContent = vUtils.getElementContent();
							childContent = childContent == null ? "" : childContent;
							pureText = pureText.replace(childFrag, childContent);
						}else {
							String childFrag = vUtils.getElementFragment();
							String childContent = getTUPureText(vn);
							pureText = pureText.replace(childFrag, childContent);
						}
					}else {
						//ph节点的值为code data或者一个sub节点，因此，要考虑到sub节点的情况
						if (vUtils.getChildElementsCount() <= 0) {
							String childFrag = vUtils.getElementFragment();
							pureText = pureText.replace(childFrag, "");
						}else {
							String childFrag = vUtils.getElementFragment();
							String childContent = getSubNodePureText(vn);
							pureText = pureText.replace(childFrag, childContent);
						}
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger12"), e);
		}
		
		return pureText;
	}
	
	/**
	 * 获取ph,etp,btp,it节点下sub子节点的纯文本
	 * //ph,etp,btp,it节点，只有其子节点sub内的文本才是翻译文本，故，去掉ph,etp,btp,it节点的纯文本
	 * @param vn
	 * @return
	 */
	public String getSubNodePureText(VTDNav vn){
		String subPureText = "";
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("./*");
			VTDUtils vUtils = new VTDUtils(vn);
			while (ap.evalXPath() != -1) {
				if (vUtils.getChildElementsCount() <= 0) {
					subPureText += " " + vUtils.getElementContent();
				}else {
					subPureText += " " + getTUPureText(vn);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger14"), e);
		}
		return subPureText;
	}
	
	/**
	 * 获取过滤后的trans-unit节点下source与target的纯文本或文本内容（根据是否忽略标记，针对文本段一致性检查）。
	 * 过滤条件为:过滤掉不包括的文本段，如不包括上下文匹配，不包括完全匹配，不包括已锁文本，过滤条件在首选项中设置。
	 * @param xlfPath			xliff文件的路径
	 * @param nodeXpath			节点的xpath
	 * @param filterMap			过滤条件的集合，其值从首选项中获取
	 * @param checkSameSource	是否检查相同源文不同译文
	 * @param CheckSameTarget	是否检查相同译文不同源文
	 * @param srcIgnoreTag		相同源文不同译文比较时是否忽略标记
	 * @param tarIgnoreTag		相同译文不同源文比较时是否忽略标记
	 * @return	tuContentMap，这是一个有可能四个值的键值对，如
	 * srcPureText --> 源节点的纯文本
	 * srcContent  --> 源节点的内容（即带标记）
	 * tarPureText --> 目标节点的纯文本
	 * tarContent  --> 目标节点的内容（即带标记）
	 */
	public Map<String, ParaConsisDataBean> getFilteredTUPureTextOrContent(String xlfPath, Map<String, Boolean> filterMap,
				boolean checkSameSource, boolean checkSameTarget, boolean srcIgnoreTag, boolean tarIgnoreTag){
		
		Map<String, ParaConsisDataBean> tuContentMap = new HashMap<String, ParaConsisDataBean>();
		VTDNav vn = vnMap.get(xlfPath);
		AutoPilot ap = new AutoPilot(vn);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xlfPath);
		AutoPilot childAp = new AutoPilot(vn);
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath("/xliff/file/body/descendant::trans-unit[source/text()!='' or source/*]");
			
			//为了提高系统性能，这里是否获取其纯文本，要进行一定的验证。
			//检查项有两个，即相同源文不同译文，相同译文不同源文，如果某项不检查，那么它的忽略标记为false
			if (!checkSameSource) {
				srcIgnoreTag = false;
			}
			
			if (!checkSameTarget) {
				tarIgnoreTag = false;
			}
			
			int lineNumber = 0;
			while (ap.evalXPath() != -1) {
				lineNumber ++;
				vn.push();
				ParaConsisDataBean dataBean = new ParaConsisDataBean();
				//取出源文本的纯文本之前，先查看其内容是否为空，若为空，则返回，没有source节点，也返回null
				childAp.selectXPath("./source");
				if (childAp.evalXPath() != -1) {	//因为标准里面只有一个source，因此此处用if不用while
					String srcContent = vUtils.getElementContent();
					//如果源文本为空或无值，则返回null
					if (srcContent == null || "".equals(srcContent)) {
						continue;
					}else {
						//两个检查项中的忽略标记，若有一项为true，那么就必须获取纯文本
						if (srcIgnoreTag || tarIgnoreTag) {
							dataBean.setSrcContent(srcContent.trim());
							dataBean.setSrcPureText(getTUPureText(vn).trim());
						}else {
							dataBean.setSrcContent(srcContent.trim());
						}
					}
				}else {
					continue;
				}
				childAp.resetXPath();
				vn.pop();
				
				//首先过滤，如果有不应包括的文本段，则返回空
				if (!filterTheTU(vn, filterMap)) {
					continue;
				}
				
				vn.push();
				//下面获取目标文本的纯文本，在之前先检查目标文本是否为空或为空值，若是，则返回null，若没有target节点，也返回空
				childAp.selectXPath("./target");
				if (childAp.evalXPath() != -1) {	//因为标准里面只有一个target，因此此处用if不用while
					String tgtContent = vUtils.getElementContent();
					//如果源文本为空或无值，则返回null
					if (tgtContent == null || "".equals(tgtContent)) {
						continue;
					}else {
						//两个检查项中的忽略标记，若有一项为true，那么就必须获取纯文本
						if (srcIgnoreTag || tarIgnoreTag) {
							dataBean.setTgtContent(tgtContent.trim());
							dataBean.setTgtPureText(getTUPureText(vn).trim());
						}else {
							dataBean.setTgtContent(tgtContent.trim());
						}
					}
				}else {
					continue;
				}
				vn.pop();
				childAp.resetXPath();
				dataBean.setLineNumber(lineNumber);
				tuContentMap.put(RowIdUtil.getRowId(vn, xlfPath), dataBean);
			}
		}catch (Exception e) {
			logger.error(Messages.getString("qa.QAXmlHandler.logger15"), e);
		}
		return tuContentMap;
	}
	
	/**
	 * 获取trans-unit节点过滤后的source和target节点的文本内容（不去掉标记），过滤条件为不包括上下文匹配，不包括完全匹配，不包括已锁文本，过滤条件在首选项中设置
	 * @return	Map<String, String >	两个键值对，srcContent --> 源文本的文本，tarContent --> 目标文本的文本
	 * 如果返回的是null，则标志source节点无内容，这对与行号就不自增
	 */
	public Map<String, String> getFilteredTUContent(String xlfPath, String nodeXpath, Map<String, Boolean> filterMap){
		Map<String, String> tuTextMap = new HashMap<String, String>();
		
		VTDNav vn = vnMap.get(xlfPath);
		AutoPilot ap = new AutoPilot(vn);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xlfPath);
		AutoPilot childAp = new AutoPilot(vn);
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			while (ap.evalXPath() != -1) {
				vn.push();
				//取出源文本的纯文本之前，先查看其内容是否为空，若为空，则返回，没有source节点，也返回null
				childAp.selectXPath("./source");
				if (childAp.evalXPath() != -1) {	//因为标准里面只有一个source，因此此处用if不用while
					String srcContent = vUtils.getElementContent();
					//如果源文本为空或无值，则返回null
					if (srcContent == null || "".equals(srcContent)) {
						return null;
					}
				}else {
					return null;
				}
				childAp.resetXPath();
				vn.pop();
				
				//首先过滤，如果有不应包括的文本段，则返回空
				if (!filterTheTU(vn, filterMap)) {
					return tuTextMap;
				}
				
				//下面获取目标文本的纯文本，在之前先检查目标文本是否为空或为空值，若是，则返回null，若没有target节点，也返回空
				childAp.selectXPath("./target");
				if (childAp.evalXPath() != -1) {	//因为标准里面只有一个target，因此此处用if不用while
					String tarContent = vUtils.getElementContent();
					//如果源文本为空或无值，则返回null
					if (tarContent == null || "".equals(tarContent)) {
						return tuTextMap;
					}else {
						//两个检查项中的忽略标记，若有一项为true，那么就必须获取纯文本
						tuTextMap.clear();
					}
				}else {
					return tuTextMap;
				}
				childAp.resetXPath();
			}
		}catch (Exception e) {
			logger.error(Messages.getString("qa.QAXmlHandler.logger13"), e);
		}
		return tuTextMap;
	}
	
	
	/**
	 * 根据需求获取trans-unit下source或target的纯文本，或者整体内容
	 * @return	如果返回null,则证明这个节点是个空节点,要么没有这个节点，要么这个节点没有值
	 */
	public String getTUPureTextOrContent(String xlfPath, String nodeXpath, boolean ignoreTag){
		VTDNav vn = vnMap.get(xlfPath);
		AutoPilot ap = apMap.get(xlfPath);
		validNull(vn, ap, xlfPath);
		try {
			VTDUtils vUtils = new VTDUtils(vn);

			ap.selectXPath(nodeXpath);
			while (ap.evalXPath() != -1) {
				String content = vUtils.getElementContent();
				if (content == null || "".equals(content)) {
					return null;
				}
				// 如果忽略标记，就返回纯文本，否则返回整体内容
				if (ignoreTag) {
					return getTUPureText(vn);
				}
				return content;
			}
		} catch (NavException e) {
			e.printStackTrace();
			logger.error("", e);
		} catch (XPathParseException e) {
			e.printStackTrace();
			logger.error("", e);
		} catch (XPathEvalException e) {
			e.printStackTrace();
			logger.error("", e);
		}
		
		return null;
		
	}
	
	/**
	 * 获取一个节点的纯文本，将所有的标记除外，包括标记里面的内容
	 * @return
	 */
	public String getNodePureText(String xlfPath, String nodeXpath){
		String pureText = "";
		VTDNav vn = vnMap.get(xlfPath);
		AutoPilot ap = apMap.get(xlfPath);
		validNull(vn, ap, xlfPath);
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			while (ap.evalXPath() != -1) {
				pureText = vUtils.getElementPureText();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger14"), e);
		}
		return pureText;
	}
	
	/**
	 * 获取指定文件的指定节点的内容(除节点头与尾之外)
	 * @param xlfPath
	 * @param nodeXpath
	 * @return
	 * robert	2011-10-26
	 */
	public String getNodeContent(String xlfPath, String nodeXpath){
		String nodeContent = "";
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xlfPath);
		
		try {
			AutoPilot ap = apMap.get(xlfPath);
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			while (ap.evalXPath() != -1) {
				nodeContent = vUtils.getElementContent();
			}
		} catch (Exception e) {
			logger.error(Messages.getString("qa.QAXmlHandler.logger16"), e);
			e.printStackTrace();
		}
		return nodeContent;
	}
	
	/**
	 * 获取指定节点的值(针对节点值为文本段)
	 * @param xmlPath
	 * @param nodeXpath
	 * @return ;
	 */
	public String getNodeText(String xmlPath, String nodeXpath, String defaultValue){
		String aspellCommand = "";
		VTDNav vn = vnMap.get(xmlPath);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xmlPath);
		AutoPilot ap = new AutoPilot(vn);
		
		try {
			ap.selectXPath(nodeXpath);
			if (ap.evalXPath() != -1) {
				int commandIndex = vn.getText();
				if (commandIndex != -1) {
					aspellCommand = vn.toString(commandIndex);
				}else {
					return defaultValue;
				}
			}else {
				return defaultValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger16"), e);
		}
		return aspellCommand;
	}
	
	/**
	 * 获取某属性名的属性值
	 * @param xlfPath
	 * @param nodeXpath
	 * @param attrName
	 * @return
	 * robert	2011-11-02
	 */
	public String getNodeAttribute(String xlfPath, String nodeXpath, String attrName){
		String attribute = "";
		
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xlfPath);
		try {
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			if (ap.evalXPath() != -1) {
				attribute = vu.getCurrentElementAttribut(attrName, "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger17"), e);
		}
		return attribute;
	}
	
	/**
	 * 获取整个节点，包括其头部，其子节点，其文本
	 * @param xlfPath
	 * @param nodeXPath	节点的xpath
	 * @return
	 * robert	2011-10-21
	 */
	public String getNodeFrag(String xlfPath, String nodeXPath){
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xlfPath);
		
		String xliffNodeContent = "";
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath(nodeXPath);
			VTDUtils vu = new VTDUtils(vn);
			if (ap.evalXPath() != -1) {
				xliffNodeContent = vu.getElementFragment();
				
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger9"), e);
		} catch (NavException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger11"), e);
		} catch (XPathEvalException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger10"), e);
		}
		return xliffNodeContent;
	}
	
	/**
	 * 备注：--nonUse
	 * 获取经过过滤后的trans-unit节点的标记,过滤条件为，过滤掉不包括的文本段，如不包括上下文匹配，不包括完全匹配，不包括已锁文本，这些条件在首选项中设置。
	 * 若返回的为null, 则标志没有source节点或者source节点值为空
	 * @return	filteredTuTagMap	两个键值对，srcTag --> 源文本的标签，tarTag --> 目标文本的标签
	 */
	public Map<String, List<Map<String, String>>> getFilteredTUTags(String xlfPath, String nodeXpath, Map<String, Boolean> filterMap){
		Map<String, List<Map<String, String>>> filteredTuTagMap = new HashMap<String, List<Map<String, String>>>();
		VTDNav vn = vnMap.get(xlfPath);
		AutoPilot ap = new AutoPilot(vn);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xlfPath);
		AutoPilot childAp = new AutoPilot(vn);
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			while (ap.evalXPath() != -1) {
				
				vn.push();
				//取出源文本的纯文本之前，先查看其内容是否为空，若为空，则返回，没有source节点，也返回null
				childAp.selectXPath("./source");
				if (childAp.evalXPath() != -1) {	//因为标准里面只有一个source，因此此处用if不用while
					String srcContent = vUtils.getElementContent();
					//如果源文本为空或无值，则返回null
					if (srcContent == null || "".equals(srcContent)) {
						return null;
					}else {
						filteredTuTagMap.put("srcTag", getTUTag(vn));
					}
				}else {
					return null;
				}
				childAp.resetXPath();
				vn.pop();
				
				//首先过滤，如果有不应包括的文本段，则返回空
				if (!filterTheTU(vn, filterMap)) {
					return filteredTuTagMap;
				}
				
				//下面获取目标文本的纯文本，在之前先检查目标文本是否为空或为空值，若是，则返回null，若没有target节点，也返回空
				childAp.selectXPath("./target");
				if (childAp.evalXPath() != -1) {	//因为标准里面只有一个target，因此此处用if不用while
					String tarContent = vUtils.getElementContent();
					//如果源文本为空或无值，则返回null
					if (tarContent == null || "".equals(tarContent)) {
						return filteredTuTagMap;
					}else {
						filteredTuTagMap.put("tarTag", getTUTag(vn));
					}
				}else {
					return filteredTuTagMap;
				}
				childAp.resetXPath();
			}
		} catch (Exception e) {
			logger.error(Messages.getString("qa.QAXmlHandler.logger18"), e);
		}
		
		return filteredTuTagMap;
	} 
	
	/**
	 * 获取trans-unit节点下source与target节点的标记信息，如果节点不存在，或者为空，则返回null
	 * @param xlfPath
	 * @param nodeXPath
	 * @return
	 */
	public List<Map<String, String>> getTUTag(String xlfPath, String nodeXPath){
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + xlfPath);
		try {
			AutoPilot ap = apMap.get(xlfPath);
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(nodeXPath);
			while (ap.evalXPath() != -1) {
				String nodeContent = vUtils.getElementContent();
				if (nodeContent == null || "".equals(nodeContent)) {
					return null;
				}
				//开始获取所有的标记
				return getTUTag(vn);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger18"), e);
		}
		return null;
	}
	
	/**
	 * 给定一个文本段，获取其中的所有标记，但是所给出的字符串必须是一个xml格式的文本段，如<target>asd fas sa s ad asd </target>，像这种就不得行：asd fasdf asd fsa
	 * @param content
	 * @return ;
	 */
	public List<Map<String, String>> getTUTag(String content) {
		try {
			//tagVg = new VTDGen();
			tagVg.setDoc(content.getBytes());
			tagVg.parse(false);

			return getTUTag(tagVg.getNav());

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		return null;
	}
	
	/**
	 * {@link #getTUTag(String, String)} 
	 * <div style='color:red'>该方法与 getTuTag 方法类似，如修改应保持一致</div>
	 * 获取标记，返回结果是一个字符串
	 * @param vn
	 * @return
	 */
	private String getTuTagStr(VTDNav vn){
		StringBuffer sb = new StringBuffer();
		
		try {
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vUtils = new VTDUtils(vn);
			//如果没有相关标记，退出程序
			if (vUtils.getChildElementsCount() < 0) {
				return sb.toString();
			}
			ap.selectXPath("./*");
			//如果子节点大于0，那继续处理
			if (vUtils.getChildElementsCount() > 0) {
				while (ap.evalXPath() != -1) {
					String childNodeName = vUtils.getCurrentElementName();
					if (QAConstant.QA_mrk.equals(childNodeName) ) {	
						String tagStr = vUtils.getElementHead() + "</" + childNodeName + ">";
						sb.append(deleteBlank(tagStr));
						
						//如果该节点下还有子节点标记，再获取其子节点标记
						if (vUtils.getChildElementsCount() > 0) {
							sb.append(getTuTagStr(vn));
						}
					}else if (QAConstant.QA_g.equals(childNodeName) || QAConstant.QA_sub.equals(childNodeName)) {
						String tagStr = vUtils.getElementHead() + "</" + childNodeName + ">";
						sb.append(deleteBlank(tagStr));
						
						if (vUtils.getChildElementsCount() > 0) {
							sb.append(getTuTagStr(vn));
						}
					}else {
						//ph节点的值为code data或者一个sub节点，因此，要考虑到sub节点的情况
						if (vUtils.getChildElementsCount() <= 0) {
							//其他节点，比如ph,etp,btp,it内都没有可翻译文本，故获取其整体文本段即可
							String childFrag = vUtils.getElementHead() + vUtils.getElementContent() + "</" + childNodeName + ">";
							sb.append(deleteBlank(childFrag));
						}else {
							//先将该标记的纯文本获取出来
							String childFrag = vUtils.getElementHead() + vUtils.getElementContent() + "</" + childNodeName + ">";
							String childContent = vUtils.getElementContent();
							String pureCode = "";
							
							//获取该节点的纯文本
							vn.push();
							String txtNode = "./text()";
							AutoPilot childAp = new AutoPilot(vn);
							childAp.selectXPath(txtNode);
							int txtIndex = -1;
							while ((txtIndex = childAp.evalXPath()) != -1) {
								pureCode += (" " + vn.toString(txtIndex));
							}
							vn.pop();
							
							String tagStr = childFrag.replace(childContent == null ? "" : childContent, pureCode == null ? "" : pureCode);
							
							sb.append(deleteBlank(tagStr));
							
							//下面添加该节点内的子节点sub标记
							sb.append(getSubNodeTagStr(vn));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger18"), e);
		}
		
		return sb.toString();
	}
	
	
	/**
	 * {@link #getTuTagStr(String, String)} 
	 * <div style='color:red'>该方法与 getTuTagStr 方法类似，如修改应保持一致</div>
	 * 获取tu节点下的标记
	 * @param ap
	 * @param vn
	 * @return
	 */
	public List<Map<String, String>> getTUTag(VTDNav vn){
		List<Map<String, String>> tagList = new LinkedList<Map<String, String>>();
		Map<String, String> tagMap;
		try {
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vUtils = new VTDUtils(vn);
			//如果没有相关标记，退出程序
			if (vUtils.getChildElementsCount() <= 0) {
				return tagList;
			}
			ap.selectXPath("./*");
			//如果子节点大于0，那继续处理
			if (vUtils.getChildElementsCount() > 0) {
				while (ap.evalXPath() != -1) {
					String childNodeName = vUtils.getCurrentElementName();
					if (QAConstant.QA_mrk.equals(childNodeName) ) {	
						String tagStr = vUtils.getElementHead() + "</" + childNodeName + ">";
						tagMap  = new HashMap<String, String>();
						tagMap.put(QAConstant.QA_TAGNAME, childNodeName);
						tagMap.put(QAConstant.QA_TAGCONTENT, deleteBlank(tagStr));
						tagList.add(tagMap);
						
						//如果该节点下还有子节点标记，再获取其子节点标记
						if (vUtils.getChildElementsCount() > 0) {
							List<Map<String, String>> childTagList = getTUTag(vn);
							for (int index = 0; index < childTagList.size(); index++) {
								tagList.add(childTagList.get(index));
							}
						}
					}else if (QAConstant.QA_g.equals(childNodeName) || QAConstant.QA_sub.equals(childNodeName)) {
						String tagStr = vUtils.getElementHead() + "</" + childNodeName + ">";
						tagMap  = new HashMap<String, String>();
						tagMap.put(QAConstant.QA_TAGNAME, childNodeName);
						tagMap.put(QAConstant.QA_TAGCONTENT, deleteBlank(tagStr));
						tagList.add(tagMap);
						
						if (vUtils.getChildElementsCount() > 0) {
							List<Map<String, String>> childTagList = getTUTag(vn);
							for (int index = 0; index < childTagList.size(); index++) {
								tagList.add(childTagList.get(index));
							}
						}
					}else {
						//ph节点的值为code data或者一个sub节点，因此，要考虑到sub节点的情况
						if (vUtils.getChildElementsCount() <= 0) {
							//其他节点，比如ph,etp,btp,it内都没有可翻译文本，故获取其整体文本段即可
							String childFrag = vUtils.getElementHead() + vUtils.getElementContent() + "</" + childNodeName + ">";
							tagMap  = new HashMap<String, String>();
							tagMap.put(QAConstant.QA_TAGNAME, childNodeName);
							tagMap.put(QAConstant.QA_TAGCONTENT, deleteBlank(childFrag));
							tagList.add(tagMap);
						}else {
							//先将该标记的纯文本获取出来
							String childFrag = vUtils.getElementHead() + vUtils.getElementContent() + "</" + childNodeName + ">";
							String childContent = vUtils.getElementContent();
							String pureCode = "";
							
							//获取该节点的纯文本
							vn.push();
							String txtNode = "./text()";
							AutoPilot childAp = new AutoPilot(vn);
							childAp.selectXPath(txtNode);
							int txtIndex = -1;
							while ((txtIndex = childAp.evalXPath()) != -1) {
								pureCode += (" " + vn.toString(txtIndex));
							}
							vn.pop();
							
							String tagStr = childFrag.replace(childContent == null ? "" : childContent, pureCode == null ? "" : pureCode);
							
							tagMap  = new HashMap<String, String>();
							tagMap.put(QAConstant.QA_TAGNAME, childNodeName);
							tagMap.put(QAConstant.QA_TAGCONTENT, deleteBlank(tagStr));
							tagList.add(tagMap);
							
							//下面添加该节点内的子节点sub标记
							List<Map<String, String>> childTagList = getSubNodeTag(vn);
							for (int index = 0; index < childTagList.size(); index++) {
								tagList.add(childTagList.get(index));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger18"), e);
		}
		return tagList;
	}
	
	/**
	 * {@link #getSubNodeTag(String, String)} 
	 * <div style='color:red'>该方法与 getSubNodeTag 方法类似，如修改应保持一致</div>
	 * 获取ph,etp,btp,it节点的子节点sub的标记
	 * @param vn
	 * @return
	 */
	public String getSubNodeTagStr(VTDNav vn){
		vn.push();
		StringBuffer sb = new StringBuffer();
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("./*");
			VTDUtils vUtils = new VTDUtils(vn);
			
			while (ap.evalXPath() != -1) {
				//先将该标记进行存储
				String nodeFrag = vUtils.getElementFragment();
				String nodeContent = vUtils.getElementContent();
				String tagStr = nodeFrag.replace(nodeContent == null ? "" : nodeContent, "");
				
				sb.append(deleteBlank(tagStr));
				
				//如果该sub节点还有子节点，那么再添加它的子节点标记
				if (vUtils.getChildElementsCount() > 0) {
					sb.append(getTuTagStr(vn));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger18"), e);
		}
		
		vn.pop();
		return sb.toString();
	}
	
	/**
	 * {@link #getSubNodeTagStr(String, String)} 
	 * <div style='color:red'>该方法与 getSubNodeTagStr 方法类似，如修改应保持一致</div>
	 * 获取ph,etp,btp,it节点的子节点sub的标记
	 * @param vn
	 * @return
	 */
	public List<Map<String, String>> getSubNodeTag(VTDNav vn){
		vn.push();
		List<Map<String, String>> subNodeTagList = new LinkedList<Map<String,String>>();
		Map<String, String> subTagMap;
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("./*");
			VTDUtils vUtils = new VTDUtils(vn);
			
			while (ap.evalXPath() != -1) {
				//理论上这应该是sub节点，但是以防万一，还是现场获取标记结点
				String nodeName = vUtils.getCurrentElementName();
				
				//先将该标记进行存储
				String nodeFrag = vUtils.getElementFragment();
				String nodeContent = vUtils.getElementContent();
				String tagStr = nodeFrag.replace(nodeContent == null ? "" : nodeContent, "");
				
				subTagMap = new HashMap<String, String>();
				subTagMap.put(QAConstant.QA_TAGNAME, nodeName);
				subTagMap.put(QAConstant.QA_TAGCONTENT, deleteBlank(tagStr));
				subNodeTagList.add(subTagMap);
				
				//如果该sub节点还有子节点，那么再添加它的子节点标记
				if (vUtils.getChildElementsCount() > 0) {
					List<Map<String, String>> subChildNodeTag = getTUTag(vn);
					for (int index = 0; index < subChildNodeTag.size(); index++) {
						subNodeTagList.add(subChildNodeTag.get(index));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger18"), e);
		}
		vn.pop();
		return subNodeTagList;
	}
	
	/**
	 * 删除多余的空格
	 * @return
	 */
	public String deleteBlank(String text){
		while (text.indexOf(QAConstant.QA_TWO_BLANK) >= 0) {
			text = text.replace(QAConstant.QA_TWO_BLANK, QAConstant.QA_ONE_BLANK);
		}
		return text;
	}
	
	/**
	 * 从.nonTransElement文件里面获取非译元素
	 * @param filePath
	 * @param nodeXpath
	 * @return
	 */
	public List<Map<String, String>> getNonTransElements(String filePath, String nodeXpath){
		List<Map<String, String>> list = new LinkedList<Map<String,String>>();
		VTDNav vn = vnMap.get(filePath);
		AutoPilot ap = new AutoPilot(vn);
		validNull(vn, ap, filePath);
		
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			AutoPilot _ap = new AutoPilot(vUtils.getVTDNav());
			_ap.selectXPath("./*");
			while (ap.evalXPath() != -1) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("id", vn.toString(vn.getAttrVal("id")));
				while (_ap.evalXPath() != -1) {
					String nodeName = vUtils.getCurrentElementName();
					if ("name".equals(nodeName)) {
						map.put("name", vUtils.getElementContent());
					}
					if ("content".equals(nodeName)) {
						map.put("content", vUtils.getElementContent());
					}else if ("regular".equals(vUtils.getCurrentElementName())) {
						map.put("regular", vUtils.getElementContent());
					}
				}
				_ap.resetXPath();
				list.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger19"), e);
		}
		return list;
	}
	
	/**
	 * 只获取非译元素的正则表达式
	 * @param filePath
	 * @return
	 */
	public List<String> getNonTransElementsRegex(String filePath){
		List<String> regexList = new ArrayList<String>();
		VTDNav vn = vnMap.get(filePath);
		AutoPilot ap = new AutoPilot(vn);
		validNull(vn, ap, filePath);
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath("/nonTrans/element/regular");
			while (ap.evalXPath() != -1) {
				regexList.add(vUtils.getElementContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger19"), e);
		}
		return regexList;
	}
	
	
	/**
	 * 将数据添加到文件中，并且是添加到指定节点的尾部
	 * @param newXlfPath
	 * @param data	要添加的内容
	 * @param toXpath	要添加的位置
	 */
	public boolean addDataToXml(String filePath, String toXpath, String data){
		VTDNav vn = vnMap.get(filePath);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + filePath);
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath(toXpath);
			if (ap.evalXPath() != -1) {
				XMLModifier xm = new XMLModifier(vn);
				xm.insertBeforeTail((data + "\n").getBytes("UTF-8"));
				//更新新生成的xliff文件，并重新加载并更新VTDVNav
				return saveAndReparse(xm, filePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		return false;
	}
	
	
	/**
	 * 删除指定的节点，且并重新解析
	 * @param filePath
	 * @param nodeXpath
	 * @return
	 */
	public boolean deleteNode(String filePath, String nodeXpath){
		VTDNav vn = vnMap.get(filePath);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + filePath);
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath(nodeXpath);
			if (ap.evalXPath() != -1) {
				VTDUtils vu = new VTDUtils(vn);
				XMLModifier xm = vu.update(nodeXpath, "");
				return saveAndReparse(xm, filePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		
		return false;
	}
	
	/**
	 * 根据指定的 xpaht 删除所有的节点
	 * @param filePath
	 * @param nodeXpath
	 * @return
	 */
	public boolean deleteAllNode(String filePath, String nodeXpath){
		VTDNav vn = vnMap.get(filePath);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + filePath);
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath(nodeXpath);
			XMLModifier xm = new XMLModifier(vn);
			boolean hasRemoved = false;
			while (ap.evalXPath() != -1) {
				xm.remove();
				hasRemoved = true;
			}
			if (hasRemoved) {
				return saveAndReparse(xm, filePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		
		return false;
		
	}
	
	/**
	 * 针对字数分析，获取所选文件的源文本
	 * @param filePath
	 *            文件路径
	 * @param traversalTuIndex
	 *            遍历的tu节点序列，是用于进度条的推进
	 * @param workInterval
	 *            进度条前进间隔
	 * @param ignoreTag
	 *            获取源文本时是否忽略标记
	 * @param contextSum
	 *            上下文匹配时的上下文个数，在首选项中设置
	 * @return 存储所选文件的所有trans-unit节点的源文本 Map<trans-unit唯一标识符,
	 *         Map<source节点的内容或纯文本(QAConstant.FA_SRC_CONTENT/QAConstant.FA_SRC_PURE_TEXT), 值>>
	 */
	public Map<String, Map<String, WordsFABean>> getAllSrcText(int workInterval, boolean ignoreTag, int contextSum, 
			String srcLang, String tgtLang) {
		Map<String, Map<String, WordsFABean>> allFileSrcTextMap = new LinkedHashMap<String, Map<String, WordsFABean>>();
		try {
			for (Entry<String, VTDNav> entry : vnMap.entrySet()) {
				Map<String, WordsFABean> srcTextMap = new LinkedHashMap<String, WordsFABean>();
				
				String filePath = entry.getKey();
				VTDNav vn = entry.getValue();
				Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + filePath);
				
				AutoPilot ap = new AutoPilot(vn);
				VTDUtils vUtils = new VTDUtils(vn);
				AutoPilot childAp = new AutoPilot(vn);
				AutoPilot contextAp = new AutoPilot(vn);
				
				StringBuilder contextStr;
				String XPATH_ALL_TU_BYLANGUAGE = "/xliff/file[upper-case(@source-language)=''{0}'' and upper-case(@target-language)=''{1}'']/body/descendant::trans-unit[source/text()!='''' or source/*]";
				String xpath = MessageFormat.format(XPATH_ALL_TU_BYLANGUAGE, new Object[] { srcLang, tgtLang });
				ap.selectXPath(xpath);
				while (ap.evalXPath() != -1) {
					String srcPureText = null;
					String srcContent = null;
					String tagStr = null;
					String preHash;
					String nextHash;
					int srcLength = 0;
					boolean isLocked;
					String rowId = RowIdUtil.getRowId(vn, filePath);
					vn.push();
					childAp.selectXPath("./source");
					if (childAp.evalXPath() != -1) {
						srcPureText = getTUPureText(vn);
						srcContent = vUtils.getElementContent();
						srcLength = srcPureText.trim().length();
						if (!ignoreTag) {
							tagStr = getTuTagStr(vn);
						}
					}
					vn.pop();
					
					// 获取上文的hash值
					vn.push();
					contextStr = new StringBuilder();
					contextAp.selectXPath("preceding::trans-unit/source[text()!='' or *]");
					int i = 0;
					while (contextAp.evalXPath() != -1 && i < contextSum) {
						contextStr.append("," + getTUPureText(vn).trim().hashCode());
						i++;
					}
					contextAp.resetXPath();
					vn.pop();
					preHash = contextStr.length() > 0 ? (contextStr.substring(1, contextStr.length())) : contextStr.toString();
	
					// 获取下文的hash值
					vn.push();
					contextStr = new StringBuilder();
					contextAp.selectXPath("following::trans-unit/source[text()!='' or *]");
					i = 0;
					while (contextAp.evalXPath() != -1 && i < contextSum) {
						contextStr.append("," + getTUPureText(vn).trim().hashCode());
						i++;
					}
					contextAp.resetXPath();
					vn.pop();
					nextHash = contextStr.length() > 0 ? (contextStr.substring(1, contextStr.length())) : contextStr.toString();
					
					//获取是否锁定translate="no"即为锁定，其他情况都算未锁定
					vn.push();
					if (vn.getAttrVal("translate") == -1) {
						isLocked = false;
					}else {
						if ("no".equals(vn.toString(vn.getAttrVal("translate")))) {
							isLocked = true;
						}else {
							isLocked = false;
						}
					}
					vn.pop();
					
					//存放值
					srcTextMap.put(rowId, new WordsFABean(srcPureText, srcContent, tagStr, preHash, nextHash, srcLength, isLocked));
				}
				
				allFileSrcTextMap.put(filePath, srcTextMap);
				
			}
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
		}
		
		return allFileSrcTextMap;
	}

	/**
	 * 通过rowId获取当前翻译单元的上下文
	 * @param rowId 
	 * @param num 上下文个数
	 * @return ;
	 */
	public Map<String, String> getTransUnitContext(String rowId, int num) {

		Map<String, String> result = new HashMap<String, String>();
		if (tuSizeMap == null || rowId == null) {
			return null;
		}

		VTDUtils vu = null;
		VTDNav vn = getVTDNavByRowId(rowId);
		try {
			vu = new VTDUtils(vn);
		} catch (NavException e1) {
			String errorMsg = Messages.getString("qa.QAXmlHandler.logger21");
			logger.error(errorMsg, e1);
			return null;
		}
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		result.put("x-preContext", getContext(vu, ap, num, true));
		result.put("x-nextContext", getContext(vu, ap, num, false));
		return result;
	}
	/**
	 * 得到VTDNav对象
	 * @param rowId
	 *            行的唯一标识
	 * @return VTDNav对象;
	 */
	private VTDNav getVTDNavByRowId(String rowId) {
		String fileName = RowIdUtil.getFileNameByRowId(rowId);
		return vnMap.get(fileName);
	}
	
	/**
	 * 得到指定 TU 上/下文
	 * @param rowId
	 *            指定 TU 的 rowId
	 * @param num
	 *            取上/下文个数
	 * @param isPre
	 *            是否是上文
	 * @return 上/下文
	 * @throws NavException
	 */
	private String getContext(VTDUtils vu, AutoPilot ap, int num, boolean isPre) {
		vu.getVTDNav().push();
		StringBuilder re = new StringBuilder();
		try {
			String xpath = isPre ? "preceding" : "following";
			xpath = xpath + "::trans-unit/source";
			ap.selectXPath(xpath);
			int i = 0;
			while (ap.evalXPath() != -1 && i < num) {
				re.append("," + getTUPureText(vu.getVTDNav()).hashCode());
				i++;
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger9"), e);
		} catch (XPathEvalException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger10"), e);
		} catch (NavException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger11"), e);
		} finally {
			vu.getVTDNav().pop();
		}
		if (re.length() > 0) {
			return re.substring(1, re.length());
		} else {
			return "";
		}
	}
	
	/**
	 * 锁定指定的trans-unit节点
	 * @param rowId
	 * @return ;
	 */
	public boolean lockedTU(String rowId){
		VTDNav vn = getVTDNavByRowId(rowId);
		XMLModifier xm;
		boolean isChanged = false; // 当前的TransUnit的translate属性是否执行了修改
		String filePath = RowIdUtil.getFileNameByRowId(rowId);
		try {
			xm = new XMLModifier(vn);
			String tuXpath = RowIdUtil.parseRowIdToXPath(rowId);
			
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath(tuXpath);
			if (ap.evalXPath() != -1) {
				int attrIdx = vn.getAttrVal("translate");
				if (attrIdx != -1) { // 存在translate属性
					String translate = vn.toString(attrIdx);
					if (!translate.equals("no")) { // translate属性值不为指定的translateValue
						xm.updateToken(attrIdx, "no");
						isChanged = true;
						saveAndReparse(xm, filePath);
					}
				} else {
					xm.insertAttribute(" translate=\"no\" ");
					isChanged = true;
					saveAndReparse(xm, filePath);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.QAXmlHandler.logger22"), e);
		}
		return isChanged;
	}
	
	/**
	 * 针对文件分析的翻译进度分析，获取每个节点的未翻译文本段数、字数，已翻译文本段数、字数。 
	 * 如果返回为null，则标志用户退出分析
	 * 备注：此处的算法与逻辑，要与XLIFFEditorImplWithNatTable中的 updateStatusLine方法保持一致
	 * @param filePath
	 *            要处理的文件的路径
	 * @param monitor
	 *            进度条
	 * @param ignoreTag
	 *            是否忽略标记
	 * @param workInterval
	 *            进度条前进一格的间隔
	 * @param traversalTuIndex
	 *            循环trans-unit节点的序列号
	 * @return transProgDataMap，下面是其健值对详解 key: notTransPara --> 未翻译文本段数 key: translatedPara --> 已翻译文本段数 key:
	 *         notTransWords --> 未翻译字数 key: translatedWords --> 已翻译字数
	 */
	public Map<String, Integer> getTransProgressData(String filePath, IProgressMonitor monitor, int workInterval,
			int traversalTuIndex) {
		Map<String, Integer> transProgDataMap = new HashMap<String, Integer>();

		int notTransPara = 0;
		int translatedPara = 0;
		int lockedPara = 0;
		int notTransWords = 0;
		int translatedWords = 0;
		int lockedWords = 0;
		try {
			VTDNav vn = vnMap.get(filePath);
			Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + filePath);
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vUtils = new VTDUtils(vn);
			AutoPilot sourceAp = new AutoPilot(vn);
			sourceAp.selectXPath("./source[text()!='' or ./*]");

			AutoPilot targetAp = new AutoPilot(vn);
			targetAp.selectXPath("./target");

			String srcLang = vUtils.getElementAttribute("/xliff/file[1]", "source-language");
			ap.selectXPath(XPATH_ALL_TU);
			while (ap.evalXPath() != -1) {
				traversalTuIndex++;
				int curWordsNum = 0;
				int inx = vn.getAttrVal("approved");
				String attriApproved = "";
				if (inx != -1) {
					attriApproved = vn.toString(inx);
				}
				//判断是否锁定
				boolean isLocked = false;
				inx = vn.getAttrVal("translate");
				if (inx != -1 && "no".equals(vn.toString(inx))) {
					isLocked = true;
				}

				// 根据是否忽略标志，获取源文本的字数
				vn.push();
				if (sourceAp.evalXPath() != -1) {
					String sourceText = getTUPureText(vn);
					curWordsNum = CountWord.wordCount(sourceText, srcLang);
				}
				sourceAp.resetXPath();
				vn.pop();

				// 查询该trans-unit节点下是否有target节点或者target节点的内容是否为空
				vn.push();
				if (targetAp.evalXPath() != -1) {
					String attriState = "";
					int stateInx = vn.getAttrVal("state");
					if (stateInx != -1) {
						attriState = vn.toString(stateInx);
					}
					if ("yes".equals(attriApproved)
							&& (!"translated".equals(attriState) || !"signed-off".equals(attriState))) {
						translatedPara++;
						translatedWords += curWordsNum;
					} else {
						if ("translated".equals(attriState) || "signed-off".equals(attriState)) {
							translatedPara++;
							translatedWords += curWordsNum;
						} else {
							notTransWords += curWordsNum;
							notTransPara++;
						}
					}
				} else {
					notTransWords += curWordsNum;
					notTransPara++;
				}
				targetAp.resetXPath();
				vn.pop();
				if (isLocked) {
					lockedPara ++;
					lockedWords += curWordsNum;
				}

				if (!monitorWork(monitor, traversalTuIndex, workInterval, false)) {
					return null;
				}
			}
		} catch (Exception e) {
			if (!monitorWork(monitor, traversalTuIndex, workInterval, false)) {
				return null;
			}
			e.printStackTrace();
			logger.error(MessageFormat.format(Messages.getString("qa.QAXmlHandler.logger23"), filePath), e);
		}

		transProgDataMap.put("notTransPara", notTransPara);
		transProgDataMap.put("translatedPara", translatedPara);
		transProgDataMap.put("lockedPara", lockedPara);
		transProgDataMap.put("notTransWords", notTransWords);
		transProgDataMap.put("translatedWords", translatedWords);
		transProgDataMap.put("lockedWords", lockedWords);

		return transProgDataMap;
	}
	
	/**
	 * 针对文件分析的翻译进度分析，获取每个节点的未翻译文本段数、字数，已翻译文本段数、字数。
	 * 如果返回为null，则标志用户退出分析
	 * @param filePath		要处理的文件的路径
	 * @param monitor		进度条
	 * @param ignoreTag		是否忽略标记
	 * @param workInterval	进度条前进一格的间隔
	 * @param traversalTuIndex	循环trans-unit节点的序列号
	 * @return	editProgDataMap，下面是其健值对详解
	 * key: notApprovedParas --> 未批准文本段数
	 * key: approvedParas --> 已批准文本段数
	 * key: notApprovedWords --> 未批准字数
	 * key: approvedWords --> 已批准字数
	 */
	public Map<String, Integer> getEditProgressData(String filePath, IProgressMonitor monitor, int workInterval, int traversalTuIndex){
		Map<String, Integer> editProgDataMap = new HashMap<String, Integer>();
		int notApprovedParas = 0;
		int approvedParas = 0;
		int lockedParas = 0;
		int notApprovedWords = 0;
		int approvedWords = 0;
		int lockedWords = 0;
		try {
			VTDNav vn = vnMap.get(filePath);
			Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + filePath);
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vUtils = new VTDUtils(vn);
			
			AutoPilot sourceAp = new AutoPilot(vn);
			sourceAp.selectXPath("./source[text()!='' or ./*]");
			String srcLang = vUtils.getElementAttribute("/xliff/file[1]", "source-language");
			
			ap.selectXPath(XPATH_ALL_TU);
			while (ap.evalXPath() != -1) {
				traversalTuIndex ++;
				int curWordsNum = 0;
				
				//判断是否锁定
				boolean isLocked = false;
				int inx = vn.getAttrVal("translate");
				if (inx != -1 && "no".equals(vn.toString(inx))) {
					isLocked = true;
				}
				
				//根据是否忽略标记，获取源文本的字数
				vn.push();
				if (sourceAp.evalXPath() != -1) {
					String sourceText = getTUPureText(vn);
					curWordsNum = CountWord.wordCount(sourceText, srcLang);
				}
				sourceAp.resetXPath();
				vn.pop();
				
				String approved = "";
				int approveIndex = vn.getAttrVal("approved");
				if (approveIndex != -1) {
					approved = vn.toString(approveIndex);
				}
				
				if ("yes".equals(approved)) {
					approvedParas ++;
					approvedWords += curWordsNum;
				}else {
					notApprovedParas ++;
					notApprovedWords += curWordsNum;
				}
				
				if (!monitorWork(monitor, traversalTuIndex, workInterval, false)) {
					return null;
				}
				
				if (isLocked) {
					lockedParas ++;
					lockedWords += curWordsNum;
				}
			}
		}catch (Exception e) {
			if (!monitorWork(monitor, traversalTuIndex, workInterval, false)) {
				return null;
			}
			e.printStackTrace();
			logger.error(MessageFormat.format(Messages.getString("qa.QAXmlHandler.logger24"), filePath), e);
		}
		
		editProgDataMap.put("notApprovedParas", notApprovedParas);
		editProgDataMap.put("approvedParas", approvedParas);
		editProgDataMap.put("lockedParas", lockedParas);
		editProgDataMap.put("notApprovedWords", notApprovedWords);
		editProgDataMap.put("approvedWords", approvedWords);
		editProgDataMap.put("lockedWords", lockedWords);
		
		return editProgDataMap;
	}
	
	/**
	 * 获取拼写检查时所用到的所以已经添加的字典语种语言
	 * @return ;
	 */
	public Hashtable<String, String> getDictionaries(String dictionaryXmlPath){
		Hashtable<String, String> dictionaries = new Hashtable<String, String>();
		VTDNav vn = vnMap.get(dictionaryXmlPath);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + dictionaryXmlPath);
		
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/dictionaries/dictionary");
			while (ap.evalXPath() != -1) {
				int langIndex = vn.getAttrVal("xml:lang");
				if (langIndex != -1) {
					dictionaries.put(vn.toString(langIndex), vn.toString(vn.getText()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		return dictionaries;
	}
	
	/**
	 * 获取aspell拼写检查器的词典语言
	 * @param aspellConfigFile	aspell配置文件路径
	 * @return ;
	 */
	public Hashtable<String, String> getAspellDictionaries(String aspellConfigFile) {
		Hashtable<String, String> dictionaries = new Hashtable<String, String>();
		VTDNav vn = vnMap.get(aspellConfigFile);
		Assert.isNotNull(vn, Messages.getString("qa.QAXmlHandler.msg1") + aspellConfigFile);

		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/aspell/aspellDictionaries/*");
			while (ap.evalXPath() != -1) {
				dictionaries.put(vn.toString(vn.getCurrentIndex()), vn.toString(vn.getText()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		return dictionaries;
	}
	
	/**
	 * 进度条前进处理方法，针对遍历tu节点总数不是workInterval的倍数情况下，程序运行要结束时，就前进一格。
	 * 如果是在程序运行中，就判断是tu节点遍历序列号是否是workInterval的倍数，若是，则前进一格
	 * @param monitor			进度条实例
	 * @param traversalTuIndex	遍历的序列号
	 * @param last				是否是程序运行的结尾处
	 * 若返回false，则标志退出程序，不再执行
	 */
	public boolean monitorWork(IProgressMonitor monitor, int traversalTuIndex, int workInterval, boolean last){
		if (last) {
			if (traversalTuIndex % workInterval != 0) {
				/*try {
					Thread.sleep(500);
				} catch (Exception e) {
				}*/
				if (monitor.isCanceled()) {
					return false;
				}
				monitor.worked(1);
			}
		}else {
			if (traversalTuIndex % workInterval == 0) {
				/*try {
					Thread.sleep(500);
				} catch (Exception e) {
				}*/
				if (monitor.isCanceled()) {
					return false;
				}
				monitor.worked(1);
			}
		}
		return true;
	}
	
	/**
	 * 获取 hunspell 所支持的语言	2013-01-15
	 * @param xmlPath
	 * @return
	 */
	public Map<String, String> getHunspellAvailableLang(String xmlPath){
		Map<String, String> langMap = new HashMap<String, String>();
		VTDGen vg = new VTDGen();
		if (!vg.parseFile(xmlPath, true)) {
			logger.error("Hunspell 语言管理文件破损，无法解析。");
			return null;
		}
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		AutoPilot childAP = new AutoPilot(vn);
		String xpath = "/config/language";
		try {
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				String code = null;
				String dictionary = null;

				vn.push();
				childAP.selectXPath("./isoCode");
				if(childAP.evalXPath() != -1){
					if (vn.getText() != -1) {
						code = vn.toRawString(vn.getText());
					}
				}
				vn.pop();
				
				vn.push();
				childAP.selectXPath("./dict");
				if (childAP.evalXPath() != -1) {
					if (vn.getText() != -1) {
						dictionary = vn.toRawString(vn.getText());
					}
				}
				vn.pop();
				
				if (code != null && dictionary != null && !"".equals(code) && !"".equals(dictionary)) {
					langMap.put(code, dictionary);
				}
			}
		} catch (Exception e) {
			logger.error("Hunspell 语言管理文件内容获取失败！", e);
		}
		return langMap;
	}
	
	/**
	 * 获取 aspell 配置文件的词典情况
	 * @return String[]{2}, 第一个值为 语言， 第二个值为 词典
	 */
	public List<String[]> getAspellDicConfig(String xmlPath) throws Exception{
		List<String[]> dicList = new LinkedList<String[]>();
		
		VTDNav vn = vnMap.get(xmlPath);
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("/aspell/aspellDictionaries/*");
		while (ap.evalXPath() != -1) {
			String lang = vn.toString(vn.getCurrentIndex());
			String dic = vn.toString(vn.getText());
			dicList.add(new String[]{lang, dic});
		}
		
		return dicList;
	} 
	
	/**
	 * 将语言与词典对添加到 Aspell 配置文件中
	 * @throws Exception
	 */
	public void addAspellConfig(String xmlPath, String lang, String dic, boolean isUpdate) throws Exception{
		VTDNav vn = vnMap.get(xmlPath);
		XMLModifier xm = null;
		VTDUtils vu = new VTDUtils(vn);
		if (isUpdate) {
			String xpath = "/aspell/aspellDictionaries/" + lang + "/text()";
			xm = vu.update(xpath, dic);
		} else {
			String xpath = "/aspell/aspellDictionaries/text()";
			String insertValue = "\n<" + lang + ">" + dic + "</" + lang + ">\n";
			xm = vu.insert(xpath, insertValue);
		}
		vu.bind(xm.outputAndReparse());
		saveAndReparse(xm, xmlPath);
	}
	
	/**
	 * 删除 Aspell 配置文件的内容
	 * @param xmlPath
	 * @param lang
	 * @param dic
	 */
	public void removeAspellConfig(String xmlPath, String lang) throws Exception{
		VTDNav vn = vnMap.get(xmlPath);
		
		AutoPilot ap = new AutoPilot(vn);
		XMLModifier xm = new XMLModifier(vn);
		String xpath = "/aspell/aspellDictionaries/" + lang + "";
		ap.selectXPath(xpath);
		if (ap.evalXPath() != -1) {
			xm.remove();
			saveAndReparse(xm, xmlPath);
		}
	}
	
	/**
	 * 当 spellPage 点击确定时，保存 aspell 配置的部份信息
	 * @param xmlPath
	 * @param isUtf8 界面上是否选中了 utf-8 的 button。
	 */
	public void saveAspellConfig(String xmlPath, String commandLine, boolean isUtf8) throws Exception{
		VTDNav vn = vnMap.get(xmlPath);
		VTDUtils vu = new VTDUtils(vn);
		XMLModifier xm = null;
		
		String commandPath = getNodeText(xmlPath, "/aspell/commandLine", null);

		if (commandPath == null && commandLine != null) {
			String insertValue = "\n<commandLine>" + commandLine + "</commandLine>\n";
			xm = vu.insert("/aspell/text()", insertValue);
		} else if (commandPath != null && commandLine != null) {
			if (!commandLine.equals(commandPath)) {
				xm = vu.update("/aspell/commandLine/text()", commandLine);
			}
		} else if (commandPath != null && commandLine == null) {
			xm = vu.delete("/aspell/commandLine");
		}
		if (xm != null) {
			vu.bind(xm.outputAndReparse());
		}
		
		String utf8File = getNodeText(xmlPath, "/aspell/utf8", null);
		String utf8 = isUtf8 ? "yes" : "no";
		if (utf8File == null) {
			String insertValue = "\n<utf8>" + utf8 + "</utf8>\n";
			xm = vu.insert("/aspell/text()", insertValue);
		} else {
			if (!utf8File.equals(utf8)) {
				xm = vu.update("/aspell/utf8/text()", utf8);
			}
		}
		if (xm != null) {
			saveAndReparse(xm, xmlPath);
		}
		
	}
	
	public static void main(String[] args) {
////		try {
////			VTDGen vg = new VTDGen();
////			String xmlPath = "/home/robert/Desktop/test.xml";
////			vg.parseFile(xmlPath, true);
////			VTDNav vn = vg.getNav();
////			AutoPilot ap = new AutoPilot(vn);
////			XMLModifier xm = new XMLModifier(vn);
////			
////			String xpath = "/root/xliff/file/descendant::node()";
////			ap.selectXPath(xpath);
////			while(ap.evalXPath() != -1){
////				System.out.println(vn.toString(vn.getCurrentIndex()));
////				if ("body".equals(vn.toString(vn.getCurrentIndex()))) {
////					System.out.println("删除了");
////					xm.remove();
////				}
////				if ("trans-unit".equals(vn.toString(vn.getCurrentIndex()))) {
////					xm.remove();
////					xm.insertAfterElement("<b>bbbbbbbbbbbbbbbb</b>");
////				}
////			}
////			xm.output(xmlPath);
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
//		
//		
//		
//		 try {
//				String content = "<test> <a type='a'>aaa</a> <a type='b'>aaa</a>  <b>bbb</b> <c>cccc</c> </test>";
//				VTDGen vg = new VTDGen();
//				vg.setDoc(content.getBytes());
//				vg.parse(true);
//				VTDUtils vu = new VTDUtils(vg.getNav());
//				XMLModifier xm = vu.delete("/test/a[@type='a']");
//				VTDNav vn = xm.outputAndReparse();
//				System.out.println(vn.toString(vn.getCurrentIndex()));
////				FileOutputStream fos =  new FileOutputStream("/home/robert/Desktop/mmmm.xml");
////				xm.output(fos);
////				fos.close();
//				System.out.println(content);
//				
//				//VTDUtils vUtils = new VTDUtils(vn);
//				vu.bind(vn);
//				System.out.println(vu.getElementFragment());
//
////				AutoPilot ap = new AutoPilot(vu.getVTDNav());
////				ap.selectXPath("/test/a[@type='b']");
////				while (ap.evalXPath() != -1) {
////					System.out.println(vu.getElementFragment());
////				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		String ttxPath = "/home/robert/Desktop/test.xml";
//		QAXmlHandler qa = new QAXmlHandler();
//		
////		qa.test(ttxPath);
//		qa.test_XLiff();
	}
	
	
	
}
