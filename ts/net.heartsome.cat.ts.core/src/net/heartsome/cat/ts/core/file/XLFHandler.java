package net.heartsome.cat.ts.core.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.xquery.XQException;

import net.heartsome.cat.common.bean.ProjectInfoBean;
import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.file.AbstractFileHandler;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.Utils;
import net.heartsome.cat.ts.core.bean.AltTransBean;
import net.heartsome.cat.ts.core.bean.Constants;
import net.heartsome.cat.ts.core.bean.FuzzyTransDataBean;
import net.heartsome.cat.ts.core.bean.NoteBean;
import net.heartsome.cat.ts.core.bean.PropBean;
import net.heartsome.cat.ts.core.bean.PropGroupBean;
import net.heartsome.cat.ts.core.bean.SplitSegInfoBean;
import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.core.bean.XliffBean;
import net.heartsome.cat.ts.core.qa.CountWord;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QATUDataBean;
import net.heartsome.cat.ts.core.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.org.tools.utils.string.StringUtilsBasic;

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
 * XLIFF 文件的操作类<br/>
 * @author John,Weachy,Leakey
 * @version
 * @since JDK1.5
 * @notice 在本类中涉及到四个容易混淆的概念：tuIndex, tuId, rowIndex, rowId。 <li>&nbsp;tuIndex: 翻译单元在XLIFF文件中的索引，从0开始。</li> <li>
 *         tuId：翻译单元Id，即trans-unit节点的id属性的值。</li> <li>&nbsp;rowIndex: 翻译单元在NatTable中的行的索引，从0开始。</li> <li>&nbsp;rowId:
 *         翻译单元在NatTable中的唯一标识。由XLIFF文件名 、源文件路径、翻译单元Id三部分拼接成，详见“net.heartsome.cat.ts.core.file.RowIdUtil”。</li>
 * @see net.heartsome.cat.ts.core.file.RowIdUtil
 */
public class XLFHandler extends AbstractFileHandler {

	/** 日志管理器 **/
	private final static Logger LOGGER = LoggerFactory.getLogger(XLFHandler.class);

	/** 文件历史访问列表。键为文件名，值为文本段的索引，空字符串值为默认值，表示第一个文本段。 **/
	private Map<String, String> accessHistory = super.createFileHistory(10, 10);

	/** 项目中文件中翻译单元计数映射表，键为项目中的XILFF文件，值为该文件翻译单元总数。 **/
	private LinkedHashMap<String, Integer> tuSizeMap = new LinkedHashMap<String, Integer>();

	/** 文件名与VTDNav对象的映射 **/
	private Hashtable<String, VTDNav> vnMap = new Hashtable<String, VTDNav>();

	public static final String hsR7NSUrl = "http://www.heartsome.net.cn/2008/XLFExtension";
	// private static final String hsR8NSUrl = "http://www.heartsome.net/2010/XLFExtension";
	public static final String hsNSPrefix = "hs";

	private Hashtable<String, String> xliffXmlnsMap = new Hashtable<String, String>(); // xliff文件的默认命名空间

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	/** 缓存的VTD导航对象 */
	private VTDNav vnRead;

	/** 可编辑文本段唯一标识的集合 */
	private ArrayList<String> rowIds = new ArrayList<String>();

	/** 缓存当前页. */
	private Map<Integer, TransUnitBean> cacheMap = new HashMap<Integer, TransUnitBean>();

	/** 上下文个数. */
	private int contextNum = 1;

	public Map<Integer, TransUnitBean> getCacheMap() {
		return cacheMap;
	}

	public static String XPATH_ALL_TU = "/xliff/file/body//trans-unit[source/text()!='' or source/*]";

	/** 过滤规则映射 */
	private static final LinkedHashMap<String, String> filterMap = new LinkedHashMap<String, String>();
	{
		// 初始化默认的过滤文本段条件
		filterMap.put(Messages.getString("file.XLFHandler.filterAll"), "");
		// filterMap.put("指定语言文本段", ""); //
		// 显示项目文件中指定的目标语言的文本段。指定语言默认为设置最多的目标语言。在项目文件中所有XLIFF文件目标语言不一致时为默认状态。
		filterMap.put(Messages.getString("file.XLFHandler.filterLocked"), "@translate='no'");
		filterMap.put(Messages.getString("file.XLFHandler.filterUnlocked"), "not(@translate='no')");
		filterMap.put(Messages.getString("file.XLFHandler.filterReview"), "@hs:needs-review='yes'");
		filterMap.put(Messages.getString("file.XLFHandler.filterSendToTM"), "@hs:send-to-tm='no'");
		filterMap.put(Messages.getString("file.XLFHandler.filterNote"), "note");

		filterMap.put(Messages.getString("file.XLFHandler.filterNull"), "not(target/* or target/text()!='')");
		filterMap.put(Messages.getString("file.XLFHandler.filterNew"), "target/@state='new'");
		filterMap.put(Messages.getString("file.XLFHandler.filterTranslated"),
				"not(@approved='yes') and target/@state='translated'");
		filterMap.put(Messages.getString("file.XLFHandler.filterUntranslated"),
				"not(target/@state='translated') and not(target/@state='signed-off')");
		filterMap.put(Messages.getString("file.XLFHandler.filterApproved"),
				"@approved='yes' and not(target/@state='signed-off')");
		filterMap.put(Messages.getString("file.XLFHandler.filterUnapproved"), "not(@approved='yes')");
		filterMap.put(Messages.getString("file.XLFHandler.filterSignedOff"), "target/@state='signed-off'");
		filterMap.put(Messages.getString("file.XLFHandler.filterUnsignedOff"), "not(target/@state='signed-off')");

		filterMap.put(Messages.getString("file.XLFHandler.filterRepeat"),
				Messages.getString("file.XLFHandler.filterRepeat")); // 仅显示当前XLIFF文件或项目文件中源文本相同的行。即包括文档内重复和文档外重复。
		if (CommonFunction.checkEdition("U")) {
			filterMap.put(Messages.getString("file.XLFHandler.filterMatch101"),
					"translate(target/@hs:quality, '%', '')=101");
		}
		filterMap.put(Messages.getString("file.XLFHandler.filterMatch100"),
				"translate(target/@hs:quality, '%', '')=100");
		filterMap.put(Messages.getString("file.XLFHandler.filterMatchlt100"),
				"translate(target/@hs:quality, '%', '')<100");
		// filterMap.put("快速翻译文本段", "alt-trans/@tool-id='" + Constant.QT_TOOLID + "'");
		// filterMap.put("自动繁殖文本段", "contains(alt-trans/@origin, 'autoFuzzy_')"); // 显示带有自动繁殖产生的匹配的文本段。

		// TODO “错误标记文本段”暂时没有实现
		// filterMap.put("错误标记文本段", ""); // 仅显示标记错误的文本段。
		// TODO “术语不一致文本段”暂时没有实现
		// filterMap.put("术语不一致文本段", ""); // 仅显示源文本对应的目标语言术语未在目标文本中出现的文本段。启用术语库时才可用。
		// filterMap.put("译文不一致文本段", "译文不一致文本段"); // 仅显示源文本相同但译文不同的文本段。
		// TODO “带修订标记文本段”暂时没有实现
		// filterMap.put("带修订标记文本段", ""); // 仅显示带有修订标记的文本段。
		if (PlatformUI.isWorkbenchRunning()) {
			LinkedHashMap<String, String> map = PreferenceStore.getMap(IPreferenceConstants.FILTER_CONDITION);
			if (map != null && !map.isEmpty()) {
				filterMap.putAll(map);
			}
		}
	}

	/**
	 * 过滤条件改变后的存储集合，存储过滤条件名称及语言名称，robert -- 2012-06-12 key1:filterName, key2:langFilterCondition.
	 */
	private Map<String, String> filterStatusMap = new HashMap<String, String>();

	/**
	 * 获取缓存的自定义过滤条件
	 * @return 缓存的自定义过滤条件
	 */
	public static LinkedHashMap<String, String> getFilterMap() {
		return filterMap;
	}

	/**
	 * 获取首选项中的自定义过滤条件
	 * @return 首选项中的自定义过滤条件
	 */
	public static LinkedHashMap<String, String> getCustomFilterMap() {
		LinkedHashMap<String, String> map = PreferenceStore.getMap(IPreferenceConstants.FILTER_CONDITION);
		if (map == null) {
			map = new LinkedHashMap<String, String>();
		}
		return map;
	}

	/**
	 * 获取首选项中的自定义过滤条件的额外数据（目前只有一个，就是选择的条件是满足所有还是满足任一一个）
	 * @return 首选项中的自定义过滤条件的额外数据
	 */
	public static LinkedHashMap<String, String> getCustomFilterAdditionMap() {
		LinkedHashMap<String, String> map = PreferenceStore.getMap(IPreferenceConstants.FILTER_CONDITION_ADDITION);
		if (map == null) {
			map = new LinkedHashMap<String, String>();
		}
		return map;
	}

	/**
	 * 获取首选中的自定义过滤条件的索引和数据信息（用于刷新界面）
	 * @return 首选中的自定义过滤条件的索引和数据信息
	 */
	public static LinkedHashMap<String, ArrayList<String[]>> getCustomFilterIndexMap() {
		LinkedHashMap<String, ArrayList<String[]>> map = PreferenceStore
				.getCustomCondition(IPreferenceConstants.FILTER_CONDITION_INDEX);
		if (map == null) {
			map = new LinkedHashMap<String, ArrayList<String[]>>();
		}
		return map;
	}

	/**
	 * 得到所有的过滤条件名称
	 * @return 所有过滤条件的名称;
	 */
	public static Set<String> getFilterNames() {
		return filterMap.keySet();
	}

	public Hashtable<String, VTDNav> getVnMap() {
		return vnMap;
	}

	/**
	 * 根据条件进行过滤
	 * @param filterName
	 *            过滤条件的名字
	 * @param langFilterCondition
	 *            语言过滤条件
	 * @return 是否成功更新;
	 */
	public boolean doFilter(String filterName, String langFilterCondition) {
		filterStatusMap.put("filterName", filterName);
		filterStatusMap.put("langFilterCondition", langFilterCondition);
		String condition = filterMap.get(filterName);
		if (condition == null) {
			condition = getCustomFilterMap().get(filterName);
		}

		if (condition != null) {
			if (condition.equals(Messages.getString("file.XLFHandler.filterRepeat"))) { // 重复文本段
				filterRepeatedSegment(langFilterCondition);
			} else if (condition.equals(Messages.getString("file.XLFHandler.differTaget"))) {
				getInconsistentTranslationsSegment(langFilterCondition);
			} else {
				String filterXPath;
				if (!"".equals(langFilterCondition)) {
					filterXPath = "/xliff/file[" + langFilterCondition + "]//descendant::trans-unit";
				} else {
					filterXPath = "/xliff/file//descendant::trans-unit";
				}
				if (!"".equals(condition)) {
					condition = " and " + condition;
				}
				filterXPath += "[(source/text()!='' or source/*)" + condition + "]";

				refreshRowIdsByFilterXPath(filterXPath);
			}
			return true;
		}
		return false;
	}

	/**
	 * 清除缓存的翻译单元
	 */
	public void resetCache() {
		this.cacheMap.clear();
	}

	public ArrayList<String> getRowIds() {
		return rowIds;
	}

	@Override
	public Map<String, Object> closeFile(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> closeFile(File file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> closeFiles(List<String> files) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> openFile(String filename) {
		return openFile(new File(filename), null);
	}

	@Override
	protected Map<String, Object> openFile(String filename, int tuCount) {
		return openFile(new File(filename), null);
	}

	@Override
	public Map<String, Object> openFile(File file) {
		return openFile(file, null);
	}

	@Override
	public Map<String, Object> openFiles(List<String> files) {
		Iterator<String> it = files.iterator();
		while (it.hasNext()) {
			String filename = it.next();
			Map<String, Object> midResult = openFile(filename, 0);
			if (midResult.get(Constant.RETURNVALUE_RESULT).equals(Constant.RETURNVALUE_RESULT_FAILURE)) {
				return midResult;
			}
		}

		return getSuccessResult();
	}

	/**
	 * 打开多个文件
	 * @param fileNames
	 *            文件名集合
	 * @param monitor
	 *            进度条
	 * @return ;
	 */
	public Map<String, Object> openFiles(ArrayList<String> fileNames, IProgressMonitor monitor) {
		ArrayList<File> files = new ArrayList<File>();
		for (String fileName : fileNames) {
			files.add(new File(fileName));
		}
		openFiles(files, monitor);
		return null;
	}

	/**
	 * 打开多个文件
	 * @param files
	 *            文件集合
	 * @param monitor
	 *            进度条
	 * @return ;
	 */
	public Map<String, Object> openFiles(List<File> files, IProgressMonitor monitor) {
		boolean isStarting = PlatformUI.getWorkbench().isStarting();
		if (isStarting) {
			monitor = null;
		} else if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			if (!isStarting) {
				int sumWorkTicks = files.size();
				monitor.beginTask(Messages.getString("file.XLFHandler.task1"), sumWorkTicks);
			}

			IProgressMonitor subMonitor = null;
			Iterator<File> it = files.iterator();
			while (it.hasNext()) {
				File file = it.next();
				if (!file.exists()) {
					if (!isStarting) {
						monitor.worked(1);
					}
					try {
						throw new FileNotFoundException();
					} catch (FileNotFoundException e) {
						LOGGER.error("", e);
						return getErrorResult(
								MessageFormat.format(Messages.getString("file.XLFHandler.logger1"),
										file.getAbsolutePath()), null);
					}
				}
				if (!isStarting) {
					subMonitor = new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
				}
				Map<String, Object> midResult = openFile(file, subMonitor);
				if (midResult.get(Constant.RETURNVALUE_RESULT).equals(Constant.RETURNVALUE_RESULT_FAILURE)) {
					return midResult;
				}
			}
		} finally {
			if (!isStarting) {
				monitor.done();
			}
		}

		return getSuccessResult();
	}

	/**
	 * 解析文件（同时操作进度条）
	 * @param file
	 * @param monitor
	 * @param totalWork
	 * @return ;
	 */
	private Map<String, Object> openFile(File file, IProgressMonitor monitor) {
		boolean isStarting = PlatformUI.getWorkbench().isStarting();
		if (isStarting) {
			monitor = null;
		} else if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			if (!isStarting) {
				monitor.beginTask(MessageFormat.format(Messages.getString("file.XLFHandler.task2"), file.getName()), 10);
			}

			String filename = file.getAbsolutePath();

			// updateXLIFFVersion(filename); // 更新 XLIFF 文件版本为 1.2
			// monitor.worked(1);

			// 解析文件并获取索引
			VTDGen vgRead = new VTDGen();
			if (vgRead.parseFile(filename, true)) {
				vnRead = vgRead.getNav();
				if (!isStarting) {
					if (monitor.isCanceled()) {
						return getErrorResult(Messages.getString("file.XLFHandler.msg1"), null);
					}
					monitor.worked(3);
				}
				try {

					// 创建临时文件
					// File tmpFile = createTmpFile();
					// XMLModifier xm = new XMLModifier(vnRead);
					// save(xm, tmpFile);
					//
					// tmpFileMap.put(filename, tmpFile.getAbsolutePath());
					// filesChangeStatus.put(filename, false);
					// monitor.worked(1);

					AutoPilot ap = new AutoPilot(vnRead);

					// 记录xliff文件命名空间
					ap.selectXPath("namespace-uri(/xliff)");
					String xmlns;
					if ((xmlns = ap.evalXPathToString()) != null) {
						xliffXmlnsMap.put(filename, xmlns);
					} else {
						String errorMsg = MessageFormat.format(Messages.getString("file.XLFHandler.msg2"), filename);
						return getErrorResult(errorMsg, null);
					}
					if (!isStarting) {
						monitor.worked(1);
					}
					ap.resetXPath();

					if (!isStarting) {
						if (monitor.isCanceled()) {
							return getErrorResult(Messages.getString("file.XLFHandler.msg1"), null);
						}
					}

					ap.selectXPath("count(" + XPATH_ALL_TU + ")");
					int countAllTU = (int) ap.evalXPathToNumber(); // 整个xliff文件中的trans-unit节点的个数
					if (!isStarting) {
						monitor.worked(6);
					}

					tuSizeMap.put(filename, countAllTU);
					vnMap.put(filename, vnRead);
				}

				// catch (ModifyException e) {
				// String errorMsg = MessageFormat.format("打开{0}文件时创建临时文件出错。",
				// filename);
				// LOGGER.error(errorMsg, e);
				// return getErrorResult(errorMsg, e);
				// } catch (IOException e) {
				// String errorMsg = MessageFormat.format("打开{0}文件时读写文件出错。",
				// filename);
				// LOGGER.error(errorMsg, e);
				// return getErrorResult(errorMsg, e);
				// } catch (NavException e) {
				// String errorMsg = "VTDNav 为 null，构建 VTDUtils 实例失败。";
				// LOGGER.error(errorMsg, e);
				// return getErrorResult(errorMsg, e);
				// }
				catch (XPathParseException e) {
					String errorMsg = Messages.getString("file.XLFHandler.logger2");
					LOGGER.error(errorMsg, e);
					return getErrorResult(errorMsg, e);
				}
				// catch (XPathEvalException e) {
				// String errorMsg = "XPath 求值时出错，定位到file节点失败。";
				// LOGGER.error(errorMsg, e);
				// return getErrorResult(errorMsg, e);
				// }

				accessHistory.put(filename, "");
			} else {
				String errorMsg = MessageFormat.format(Messages.getString("file.XLFHandler.logger3"), filename);
				LOGGER.error(errorMsg);
				return getErrorResult(errorMsg, null);
			}
		} finally {
			if (!isStarting) {
				monitor.done();
			}
		}
		return getSuccessResult();
	}

	@Override
	public Map<String, Object> saveFile(String srcFile, String tgtFile) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> saveFile(File srcFile, File tgtFile) {
		// TODO Auto-generated method stub
		return null;
	}

	// 获取错误返回值。
	private Map<String, Object> getErrorResult(String msg, Throwable e) {
		if (Constant.MODE_DEBUG == Constant.RUNNING_MODE && e != null) {
			e.printStackTrace();
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(Constant.RETURNVALUE_RESULT, Constant.RETURNVALUE_RESULT_FAILURE);
		resultMap.put(Constant.RETURNVALUE_MSG, msg);
		resultMap.put(Constant.RETURNVALUE_EXCEPTION, e);
		return resultMap;
	}

	// 获取正确返回值并记录消息日志。
	@SuppressWarnings("unused")
	private Map<String, Object> getSuccessResult(String msg) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(Constant.RETURNVALUE_RESULT, Constant.RETURNVALUE_RESULT_SUCCESSFUL);
		resultMap.put(Constant.RETURNVALUE_MSG, msg);
		return resultMap;
	}

	// 获取正确返回值
	private Map<String, Object> getSuccessResult() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(Constant.RETURNVALUE_RESULT, Constant.RETURNVALUE_RESULT_SUCCESSFUL);
		return resultMap;
	}

	/**
	 * 创建一个用于编辑的临时XLIFF文件，并存放在一个隐藏的项目临时文件夹内。
	 */
	public File createTmpFile() throws IOException {
		File tmpFile = null;
		File folder = null;
		File curFolder = new File(".");

		if (Utils.OS_WINDOWS == Utils.getCurrentOS()) {
			folder = new File(curFolder.getAbsoluteFile() + Utils.getFileSeparator() + "~$temp");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			folder.deleteOnExit();

			String sets = "attrib +H \"" + folder.getAbsolutePath() + "\"";
			// 输出命令串
			LOGGER.info(sets);

			// 运行命令串
			Runtime.getRuntime().exec(sets);
		} else {
			folder = new File(curFolder.getAbsoluteFile() + Utils.getFileSeparator() + ".temp");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			folder.deleteOnExit();
		}

		tmpFile = File.createTempFile("tmp", CommonFunction.R8XliffExtension_1, folder);
		tmpFile.deleteOnExit();

		return tmpFile;
	}

	/**
	 * 获取所有翻译单元数量。
	 */
	public int countTransUnit() {
		return countTransUnit(null);
	}

	/**
	 * 获取指定文件的翻译单元数量。若参数为 null 则获取所有翻译单元数量。
	 * @param filename
	 *            要获取翻译单元数量的文件，值为 null 则获取所有翻译单元数量。
	 * @return 翻译单元数量， -1 表示未打开任何文件。
	 */
	public int countTransUnit(String filename) {

		// 未打开任何文件
		if (tuSizeMap == null) {
			return -1;
		}

		int count = 0;

		if (filename == null) { // 未指定任何文件，解析全部已打开的文件。
			Iterator<String> it = tuSizeMap.keySet().iterator();
			while (it.hasNext()) {
				int tmpCount = tuSizeMap.get(it.next());
				if (tmpCount == -1) {
					return -1;
				} else {
					count += tmpCount;
				}
			}

			return count;
		} else if (tuSizeMap.containsKey(filename)) { // 指定文件已打开，仅解析该文件。
			return tuSizeMap.get(filename);
		} else { // 指定文件未打开。
			return -1;
		}
	}

	// private int getTransUnitSize(String filename) {
	// VTDGen vg = new VTDGen();
	// if (vg.parseFile(filename, true)) {
	// VTDNav vn = vg.getNav();
	// AutoPilot ap = new AutoPilot(vn);
	// try {
	// ap.selectXPath("count(//trans-unit)");
	// } catch (XPathParseException e) {
	// LOGGER.error("XPath 表达式错误。", e);
	// return -1;
	// }
	// return (int) ap.evalXPathToNumber();
	// } else { // 解析文件失败
	// return -1;
	// }
	// }

	/**
	 * 通过翻译单元的索引得到定位到该翻译单元的XPath
	 * @param index
	 *            翻译单元索引，从 0 开始。
	 */
	// String getTUXPathByTUIndex(int index) {
	// if (index < 0) {
	// return null;
	// }
	//
	// index++;
	// int filePosition = 1;
	// for (; filePosition <= fileCount; filePosition++) {
	// int count = transUnitCount.get(filePosition - 1);
	// if (index > count) {
	// index -= count;
	// } else {
	// break;
	// }
	// }
	// // TODO 此处未考虑body节点下存在group节点的情况
	// return "//file[position()=" + filePosition +
	// "]/body/trans-unit[position()=" + index + "]";
	// // return "//file[" + i + "]/body/trans-unit[" + index + "] | //file[" +
	// // i + "]/body/group/trans-unit["a
	// // + index + "]";handler.
	// }

	public int countEditableTransUnit() {
		return rowIds.size();
	}

	/**
	 * 将RowId集合的顺序重置为在xliff文件中的原始顺序 ;
	 */
	public void resetRowIdsToUnsorted() {
		// 修改BUG 2278 robert
		doFilter(filterStatusMap.get("filterName"), filterStatusMap.get("langFilterCondition"));
	}

	/**
	 * 更新可编辑文本段唯一标识的集合（rowIds）
	 * @param filterXPath
	 *            ;
	 */
	private void refreshRowIdsByFilterXPath(String filterXPath) {
		resetCache(); // 清楚缓存的翻译单元
		rowIds.clear(); // 清除之前的可编辑文本段
		AutoPilot ap = new AutoPilot();
		for (Entry<String, VTDNav> entry : vnMap.entrySet()) {
			String fileName = entry.getKey();
			VTDNav vn = entry.getValue().duplicateNav();

			ap.bind(vn);
			ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			try {
				ap.selectXPath(filterXPath);
				while (ap.evalXPath() != -1) {
					String rowId = RowIdUtil.getRowId(vn, fileName);
					if (rowId != null) {
						rowIds.add(rowId);
					}
				}
			} catch (XPathParseException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (XPathEvalException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (NavException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 得到所有当前打开的文件包含的 RowId
	 * @return ;
	 */
	public List<String> getAllRowIds() {
		HashSet<String> allRowIds = new HashSet<String>();
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
				ap.selectXPath(XPATH_ALL_TU);
				while (ap.evalXPath() != -1) {
					String rowId = RowIdUtil.getRowId(vn, fileName);
					if (rowId != null) {
						allRowIds.add(rowId);
					}
				}
			} catch (NavException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (XPathParseException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (XPathEvalException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
		}
		return new ArrayList<String>(allRowIds);
	}

	/**
	 * 获取指定索引的翻译单元对象。
	 * @param rowIndex
	 *            NatTable中行的索引，从 0 开始。
	 */
	public TransUnitBean getTransUnit(int rowIndex) {
		if (tuSizeMap == null) {
			return null;
		}
		if (rowIds.size() <= rowIndex || rowIndex < 0) {
			return null;
		}
		String rowId = getRowId(rowIndex);
		return getTransUnit(rowId);
	}

	/**
	 * 获取指定索引的翻译单元对象。
	 * @param rowId
	 *            行的唯一标识，由三部分拼成
	 */
	public TransUnitBean getTransUnit(String rowId) {
		if (tuSizeMap == null || rowId == null) {
			return null;
		}
		String tuNode = RowIdUtil.parseRowIdToXPath(rowId);
		if (tuNode == null) {
			return null;
		}

		VTDUtils vu = null;
		VTDNav vn = getVTDNavByRowId(rowId);
		try {
			vu = new VTDUtils(vn);
		} catch (NavException e1) {
			String errorMsg = Messages.getString("file.XLFHandler.logger4");
			LOGGER.error(errorMsg, e1);
			return null;
		}

		try {
			if (vu.pilot(tuNode) != -1) { // 导航到 trans-unit 节点
				String tuid = "";
				String srcText = "";
				String srcContent = "";
				String tgtText = "";
				String tgtContent = "";
				Hashtable<String, String> srcProps = new Hashtable<String, String>();
				Hashtable<String, String> tgtProps = new Hashtable<String, String>();

				// 取翻译单元所有属性
				vn.push();
				Hashtable<String, String> tuProps = vu.getCurrentElementAttributs();
				vn.pop();
				if (tuProps != null) {
					tuid = tuProps.get("id");
				}

				AutoPilot ap = new AutoPilot(vn);
				ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
				ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
				// 取翻译单元源节点完整文本，含内部标记。
				vn.push();
				if (vu.pilot(ap, "./source") != -1) { // 导航到 Source 子节点
					// 源节点完整内容。
					srcContent = vu.getElementContent();
					// 源节点纯文本内容。
					srcText = getTUPureText(vu.getVTDNav());
					// 源节点属性集合。
					srcProps = vu.getCurrentElementAttributs();
				}
				ap.resetXPath();
				vn.pop();

				// 取翻译单元目标节点完整文本，含内部标记。
				vn.push();
				if (vu.pilot(ap, "./target") != -1) { // 导航到 Target 子节点
					// 目标节点完整内容。
					tgtContent = vu.getElementContent();
					// 目标节点纯文本内容。
					tgtText = getTUPureText(vu.getVTDNav());
					// 目标节点属性集合。
					tgtProps = vu.getCurrentElementAttributs();
				}
				vn.pop();

				// 获取所有的 alttrans 匹配节点。
				vn.push();
				Vector<AltTransBean> matches = getAltTrans(vu);
				vn.pop();

				// 构建翻译单元对象，存储节点信息
				TransUnitBean tub = new TransUnitBean(tuid, srcContent, srcText);
				tub.setTuProps(tuProps);
				tub.setSrcProps(srcProps);
				tub.setTgtContent(tgtContent);
				tub.setTgtText(tgtText);
				tub.setTgtProps(tgtProps);
				tub.setMatches(matches);
				vn.push();
				tub.setNotes(getNotes(vu));
				vn.pop();

				vn.push();
				tub.setPropgroups(getPrpoGroups(vu));
				vn.pop();

				return tub;
			}
		} catch (XPathEvalException e) {
			String errorMsg = Messages.getString("file.XLFHandler.logger5");
			LOGGER.error(errorMsg, e);
			return null;
		} catch (NavException e) {
			String errorMsg = Messages.getString("file.XLFHandler.logger6");
			LOGGER.error(errorMsg, e);
			return null;
		} catch (XPathParseException e) {
			String errorMsg = Messages.getString("file.XLFHandler.logger7");
			LOGGER.error(errorMsg, e);
			return null;
		}
		return null;
	}

	// 获取当前节点的所有批注。
	private Vector<NoteBean> getNotes(VTDUtils vu) throws XPathEvalException, NavException, XPathParseException {
		Vector<NoteBean> notes = new Vector<NoteBean>();
		VTDNav vn = vu.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		ap.selectXPath("./note");
		while (ap.evalXPath() != -1) {
			NoteBean note = new NoteBean(vu.getElementContent());
			note.setLang(vu.getCurrentElementAttribut("xml:lang", null));
			note.setFrom(vu.getCurrentElementAttribut("from", null));
			note.setPriority(vu.getCurrentElementAttribut("priority", null));
			note.setAnnotates(vu.getCurrentElementAttribut("annotates", null));
			note.setApplyCurrent(vu.getCurrentElementAttribut("hs:apply-current", "Yes"));

			notes.add(0, note);
		}

		if (notes.isEmpty()) {
			notes = null;
		}

		return notes;
	}

	public Vector<NoteBean> getNotes(String rowId) throws NavException, XPathParseException, XPathEvalException {
		Vector<NoteBean> notes = new Vector<NoteBean>();
		VTDNav vn = getVTDNavByRowId(rowId);
		VTDUtils vu = new VTDUtils(vn);
		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
		ap.selectXPath(tuXPath + "/note");
		while (ap.evalXPath() != -1) {
			NoteBean note = new NoteBean(vu.getElementContent());
			note.setLang(vu.getCurrentElementAttribut("xml:lang", null));
			note.setFrom(vu.getCurrentElementAttribut("from", null));
			note.setPriority(vu.getCurrentElementAttribut("priority", null));
			note.setAnnotates(vu.getCurrentElementAttribut("annotates", null));
			note.setApplyCurrent(vu.getCurrentElementAttribut("hs:apply-current", "Yes"));

			notes.add(0, note);
		}

		if (notes.isEmpty()) {
			notes = null;
		}

		return notes;
	}

	// public Vector<NoteBean> get

	private Vector<AltTransBean> getAltTrans(VTDUtils vu) throws XPathParseException, XPathEvalException, NavException {
		VTDNav vn = vu.getVTDNav();
		Vector<AltTransBean> result = new Vector<AltTransBean>();
		AutoPilot apAltTrans = new AutoPilot(vn);

		String xpath = "./alt-trans";
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath(xpath);
		while (ap.evalXPath() != -1) {
			AltTransBean atb = new AltTransBean();

			// 获取当前匹配节点全部属性。
			atb.setMatchProps(vu.getCurrentElementAttributs());

			// 获取源节点内容、属性及纯文本
			vn.push();
			if (vu.pilot(apAltTrans, "./source") != -1) {
				atb.setSrcContent(vu.getElementContent());
				atb.setSrcProps(vu.getCurrentElementAttributs());
				atb.setSrcText(getTUPureText(vu.getVTDNav()));
			}
			apAltTrans.resetXPath();
			vn.pop();

			// 获取目标节点内容、属性及纯文本
			vn.push();
			if (vu.pilot(apAltTrans, "./target") != -1) {
				atb.setTgtContent(vu.getElementContent());
				atb.setTgtProps(vu.getCurrentElementAttributs());
				atb.setTgtText(getTUPureText(vu.getVTDNav()));
			}
			apAltTrans.resetXPath();
			vn.pop();

			// 如果 Source 和 Target 的内容都不为 null。
			if (atb.getSrcContent() != null && atb.getTgtContent() != null) {
				// 获取匹配节点的属性组集合
				vn.push();
				atb.setPropGroups(getPrpoGroups(vu));
				vn.pop();
				result.add(atb);
			}
		}

		if (result.isEmpty()) {
			return null;
		} else {
			// 排序
			ArrayList<AltTransBean> list = new ArrayList<AltTransBean>(result);
			Collections.sort(list, new Comparator<AltTransBean>() {

				public int compare(AltTransBean o1, AltTransBean o2) {
					if (o1 == null && o2 == null) {
						return 0;
					} else if (o1 == null) {
						return 1;
					} else if (o2 == null) {
						return -1;
					}

					if (o1.getMatchProps() == null && o2.getMatchProps() == null) {
						return 0;
					} else if (o1.getMatchProps() == null) {
						return 1;
					} else if (o2.getMatchProps() == null) {
						return -1;
					}

					if (o1.getMatchProps().get("match-quality") == null
							&& o2.getMatchProps().get("match-quality") == null) {
						return 0;
					} else if (o1.getMatchProps().get("match-quality") == null) {
						return 1;
					} else if (o2.getMatchProps().get("match-quality") == null) {
						return -1;
					}

					int mq1 = Integer.parseInt(o1.getMatchProps().get("match-quality").replace("%", ""));
					int mq2 = Integer.parseInt(o2.getMatchProps().get("match-quality").replace("%", ""));
					return mq2 - mq1;
				}
			});
			return new Vector<AltTransBean>(list);
		}
	}

	// 获取当前节点下的属性组集合。
	private Vector<PropGroupBean> getPrpoGroups(VTDUtils vu) throws XPathParseException, XPathEvalException,
			NavException {
		VTDNav vn = vu.getVTDNav();
		Vector<PropGroupBean> pgs = new Vector<PropGroupBean>();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./prop-group");
		while (ap.evalXPath() != -1) {
			vn.push();
			Vector<PropBean> props = getProps(vu);
			vn.pop();
			PropGroupBean pg = new PropGroupBean(props);
			// 获取属性组名称。
			pg.setName(vu.getCurrentElementAttribut("name", null));

			pgs.add(pg);
		}

		ap.resetXPath();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		ap.selectXPath("./hs:prop-group");
		while (ap.evalXPath() != -1) {
			vn.push();
			Vector<PropBean> props = getProps(vu);
			vn.pop();
			PropGroupBean pg = new PropGroupBean(props);
			// 获取属性组名称。
			pg.setName(vu.getCurrentElementAttribut("name", null));

			pgs.add(pg);
		}

		if (pgs.isEmpty()) {
			pgs = null;
		}
		return pgs;
	}

	// 获取当前节点下的属性元素子节点。
	private Vector<PropBean> getProps(VTDUtils vu) throws XPathParseException, XPathEvalException, NavException {
		VTDNav vn = vu.getVTDNav();
		Vector<PropBean> props = new Vector<PropBean>();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./prop");
		while (ap.evalXPath() != -1) {
			String proptype = vu.getCurrentElementAttribut("prop-type", null);
			String value = vu.getElementContent();
			String lang = vu.getCurrentElementAttribut("xml:lang", null);

			PropBean prop = new PropBean(proptype, value, lang);
			props.add(prop);
		}

		ap.resetXPath();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		ap.selectXPath("./hs:prop");
		while (ap.evalXPath() != -1) {
			String proptype = vu.getCurrentElementAttribut("prop-type", null);
			String value = vu.getElementContent();
			String lang = vu.getCurrentElementAttribut("xml:lang", null);

			PropBean prop = new PropBean(proptype, value, lang);
			props.add(prop);
		}

		if (props.isEmpty()) {
			props = null;
		}
		return props;
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
			// BufferedInputStream bis = new BufferedInputStream(new
			// ByteArrayInputStream("".getBytes()));
			// iFile.appendContents(bis, true, false, null);
			// bis.close();

			return true;
		} catch (ModifyException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (TranscodeException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (CoreException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 保存修改并更新VTDNav对象
	 * @param xm
	 *            XMLModifier对象
	 * @param fileName
	 *            文件名
	 * @return 是否成功;
	 */
	public void saveAndReparse(final XMLModifier xm, final String fileName) {

		boolean isSaved = save(xm, fileName);
		if (!isSaved) {
			LOGGER.error(Messages.getString("file.XLFHandler.logger8"));
			return;
		}
		XLFHandler.this.resetCache();
		// 重新加载
		VTDGen vg = new VTDGen();
		if (vg.parseFile(fileName, true)) {
			vnMap.put(fileName, vg.getNav());
		}
	}

	/**
	 * 动态取得列名 规则：多个file中如果有相同的source-language和target-language则返回它们的属性值
	 * 多个file中如果有不同的source-language和target-language则返回“源”和“目标”
	 * @return source和target的列名集合;
	 */
	public Hashtable<String, String> getNatTableColumnName() {
		Hashtable<String, String> re = new Hashtable<String, String>(2);
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		for (VTDNav vn : vnMap.values()) {
			ap.bind(vn);
			try {
				ap.selectXPath("//file");
				boolean flag1 = true;
				boolean flag2 = true;
				String source = null;
				String target = null;
				while (ap.evalXPath() != -1) {
					if (flag1) {
						int index1 = vn.getAttrVal("source-language");
						if (index1 != -1) {
							String tempSource = vn.toNormalizedString(index1);
							if (tempSource != null) {
								if (source == null) {
									source = tempSource;
								} else {
									if (source.equals(tempSource)) {
										flag1 = true;
									} else {
										source = "源";
										flag1 = false;
									}
								}
							}
						} else {
							source = "源";
							flag1 = false;
						}
					}
					if (flag2) {
						int index2 = vn.getAttrVal("target-language");
						if (index2 != -1) {
							String tempTarget = vn.toNormalizedString(index2);
							if (tempTarget != null) {
								if (target == null) {
									target = tempTarget;
								} else {
									if (target.equals(tempTarget)) {
										flag1 = true;
									} else {
										target = "目标";
										flag2 = false;
									}
								}
							}
						} else {
							target = "目标";
							flag2 = false;
						}
					}
				}
				re.put("source", source);
				re.put("target", target);
				return re;
			} catch (XPathEvalException e) {
				String errorMsg = Messages.getString("file.XLFHandler.logger5");
				LOGGER.error(errorMsg, e);
			} catch (NavException e) {
				String errorMsg = Messages.getString("file.XLFHandler.logger6");
				LOGGER.error(errorMsg, e);
			} catch (XPathParseException e) {
				String errorMsg = Messages.getString("file.XLFHandler.logger7");
				LOGGER.error(errorMsg, e);
			}
		}
		return null;
	}

	/**
	 * 得到所有语言对
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
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (XPathEvalException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (NavException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
		}
		return languages;
	}

	/**
	 * @param args
	 *            ;
	 */
	/*
	 * public static void main(String[] args) throws XPathParseException, XPathEvalException, NavException { XLFHandler
	 * handler = new XLFHandler(); String fileName = "/data/weachy/Desktop/a1.xlf"; ArrayList<String> files = new
	 * ArrayList<String>(); files.add(fileName); handler.openFiles(files, null); String rowId =
	 * RowIdUtil.getRowId(fileName,
	 * "/data/weachy/eclipse-rcp-galileo-SR2-linux-gtk-x86_64/workspace/XliffMakerWeachyImpl/a.txt" , "1");
	 * TransUnitBean tu = handler.getTransUnit(rowId); // Vector<AltTransBean> matches = tu.getMatches(); // for
	 * (AltTransBean altTransBean : matches) { // System.out.println(altTransBean.toString()); // }
	 * System.out.println(tu.toXMLString()); }
	 */

	/**
	 * 获取指定索引的翻译单元id
	 * @param index
	 *            文本段在编辑器中索引，索引从 0 开始。
	 * @return 编辑器中唯一标识该行文本段的Id;
	 */
	public String getRowId(int rowIndex) {
		if (rowIndex > -1 && rowIds.size() > rowIndex) {
			return rowIds.get(rowIndex);
		}
		return null;
	}

	/**
	 * 得到在 NatTable 上行的索引
	 * @param rowId
	 *            行的唯一标识
	 * @return ;
	 */
	public int getRowIndex(String rowId) {
		if (rowId == null || "".equals(rowId)) {
			return -1;
		}
		return rowIds.indexOf(rowId);
	}

	/**
	 * 获取指定索引的翻译单元id集合
	 * @param rowIndexs
	 *            文本段在编辑器中索引集合，索引从 0 开始。
	 * @return 编辑器中唯一标识该行文本段的Id集合;
	 */
	public Set<String> getRowIds(int[] rowIndexs) {
		// 创建翻译单元id集合
		int size = rowIds.size();
		if (size == rowIndexs.length) {
			return new HashSet<String>(rowIds);
		} else {
			Set<String> set = new HashSet<String>();
			for (int rowIndex : rowIndexs) {
				if (rowIndex > -1 && rowIndex < size) {
					set.add(rowIds.get(rowIndex));
				}
			}
			return set;
		}
	}

	/**
	 * 通过文件名，与给定的xpath获取某个特定的tu节点的rowId，备注，只返回第一个ROWID robert 2012-04-28
	 * @param xlfPath
	 * @param xpath
	 * @return ;
	 */
	public String getRowIdByXpath(String xlfPath, String xpath) {
		VTDNav vn = vnMap.get(xlfPath);
		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		try {
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				return RowIdUtil.getRowId(vn, xlfPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 得到文档属性的信息
	 * @param fileName
	 *            文件名
	 * @return 多个文件的文档属性集合（一个 file 节点文档的属性为一个 HashMap）;
	 */
	public List<HashMap<String, String>> getDocumentInfo(String fileName) {

		ArrayList<HashMap<String, String>> fileList = new ArrayList<HashMap<String, String>>();

		VTDNav vn = vnMap.get(fileName);
		AutoPilot apFile = new AutoPilot(vn);
		try {
			apFile.selectXPath("/xliff/file");
			String[] fileAttrNames = { DocumentPropertiesKeys.ORIGINAL, DocumentPropertiesKeys.DATA_TYPE,
					DocumentPropertiesKeys.SOURCE_LANGUAGE, DocumentPropertiesKeys.TARGET_LANGUAGE };

			String[] propTypes = new String[] { DocumentPropertiesKeys.PROJECT_REF, DocumentPropertiesKeys.JOB_REF,
					DocumentPropertiesKeys.JOB_DATE, DocumentPropertiesKeys.JOB_OWNER, DocumentPropertiesKeys.CLIENT };

			VTDUtils vu = new VTDUtils(vn);
			while (apFile.evalXPath() != -1) {
				String value = "";
				HashMap<String, String> fileAttrs = new HashMap<String, String>();
				for (String attrName : fileAttrNames) {
					value = vu.getCurrentElementAttribut(attrName, "");
					fileAttrs.put(attrName, value);
				}

				AutoPilot ap = new AutoPilot(vn);

				vn.push();
				value = "";
				ap.selectXPath("./header/skl");
				if (ap.evalXPath() != -1) {
					ap.selectXPath("./external-file");
					if (ap.evalXPath() != -1) {
						value = vu.getCurrentElementAttribut("href", "");
					} else {
						ap.selectXPath("./internal-file");
						if (ap.evalXPath() != -1) {
							value = Constant.SKL_INTERNAL_FILE;
						}
					}
				}
				// vn.push();
				// ap.selectXPath("./header/skl/external-file");
				// value = "";
				// if (ap.evalXPath() != -1) {
				// int attrIdx = vn.getAttrVal("href");
				// value = attrIdx != -1 ? vn.toString(attrIdx) : "";
				// }
				fileAttrs.put(DocumentPropertiesKeys.SKL, value);
				vn.pop();

				ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);

				vn.push();
				ap.selectXPath("./header/hs:prop-group[@name='encoding']/hs:prop[@prop-type='encoding']");
				value = "";
				if (ap.evalXPath() != -1) {
					value = vn.toString(vn.getText());
				}
				fileAttrs.put(DocumentPropertiesKeys.ENCODING, value);
				vn.pop();

				for (String attrName : propTypes) {
					vn.push();
					ap.selectXPath("./header/hs:prop-group[@name=\"project\"]/hs:prop[@prop-type=\"" + attrName + "\"]");
					value = "";
					if (ap.evalXPath() != -1) {
						value = vn.toString(vn.getText());
					}
					if ("".equals(value) && DocumentPropertiesKeys.JOB_DATE.equals(attrName)) {
						value = sdf.format(new Date());
					}
					fileAttrs.put(attrName, value);
					vn.pop();
				}

				fileList.add(fileAttrs);
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return fileList;
	}

	/**
	 * 得到用户定制的信息(customer prop-group 的信息)
	 * @param fileName
	 * @return 以{ {key1, value1}, {key2, value2} }二维数组的形式返回;
	 */
	public String[][] getCustomerInfo(String fileName) {
		ArrayList<String[]> customerInfo = new ArrayList<String[]>();
		VTDNav vn = vnMap.get(fileName);
		AutoPilot ap = new AutoPilot(vn);
		try {
			VTDUtils vu = new VTDUtils(vn);
			ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			ap.selectXPath("/xliff/file/header/hs:prop-group[@name='customer']/hs:prop");
			while (ap.evalXPath() != -1) {
				String key = vu.getCurrentElementAttribut("prop-type", "");
				String value = vn.toString(vn.getText());
				String[] entry = { key, value };
				customerInfo.add(entry);
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

		return customerInfo.toArray(new String[][] {});
	}

	/**
	 * 转义“&”、“<”、“>”为“&amp;amp;”、“&amp;lt;”、“&amp;gt;”
	 * @param source
	 *            源文本，如：“&lt;html&gt;“
	 * @return 转义后的文本，如：“&amp;lt;html&amp;gt;“
	 */
	public String escapeTag(String source) {
		return source.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	/**
	 * 转义“&amp;lt;”、“&amp;gt;”、“&amp;amp;”为“<”、“>”、“&”
	 * @param source
	 *            源文本，如：“&amp;lt;html&amp;gt;“
	 * @return 转义后的文本，如：“&lt;html&gt;“
	 */
	public String resolveTag(String source) {
		return source.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
	}

	/**
	 * 修改文档属性
	 * @param fileInfo
	 *            file节点的信息
	 * @param projectInfo
	 *            project prop-group 的信息
	 * @param customerInfo
	 *            customer prop-group 的信息;
	 */
	public void updateDocumentInfo(String fileName, Map<String, String> fileInfo, Map<String, String> projectInfo,
			Map<String, String> customerInfo) {
		if (fileInfo == null || fileInfo.size() == 0) {
			return;
		}
		Object value = fileInfo.get(DocumentPropertiesKeys.ORIGINAL);
		if (value != null) {
			VTDNav vn = vnMap.get(fileName);
			AutoPilot ap = new AutoPilot(vn);
			String original = escapeTag(value.toString());

			try {
				ap.selectXPath("//file[@original='" + original + "']");
				if (ap.evalXPath() != -1) { // 定位到相应的file节点
					XMLModifier xm = new XMLModifier(vn);
					value = fileInfo.get(DocumentPropertiesKeys.TARGET_LANGUAGE);
					if (value != null) { // 修改 target-language 属性
						String targetLanguage = escapeTag(value.toString());
						int attrIdx = vn.getAttrVal("target-language");
						if (attrIdx != -1) {
							xm.updateToken(attrIdx, targetLanguage);
						} else {
							xm.insertAttribute(" target-language=\"" + targetLanguage + "\" ");
						}
					}

					ap.selectXPath("./header"); // 定位到 header 节点
					if (ap.evalXPath() == -1) {
						saveAndReparse(xm, fileName);
						return;
					}

					value = fileInfo.get(DocumentPropertiesKeys.SKL);
					if (value != null) { // 修改 skl 骨架文件路径
						String skl = escapeTag(value.toString());
						vn.push();
						ap.selectXPath("./skl/external-file");
						if (ap.evalXPath() != -1) {
							int attrIdx = vn.getAttrVal("href");
							if (attrIdx != -1) {
								xm.updateToken(attrIdx, skl);
							} else {
								xm.insertAttribute(" href=\"" + skl + "\" ");
							}
						}
						vn.pop();
					}

					ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);

					String propGroup = "";
					if (projectInfo != null && projectInfo.size() > 0) {
						vn.push();
						// 先删除之前的 project prop-group.
						ap.selectXPath("./hs:prop-group[@name='project']");
						if (ap.evalXPath() != -1) {
							xm.remove(vn.getElementFragment());
						}
						vn.pop();

						// 保存新的 project prop-group.
						propGroup += "<hs:prop-group name=\"project\">";
						for (Entry<String, String> entry : projectInfo.entrySet()) {
							propGroup += "<hs:prop prop-type=\"" + escapeTag(entry.getKey()) + "\">"
									+ escapeTag(entry.getValue()) + "</hs:prop>";
						}
						propGroup += "</hs:prop-group>";
					}

					if (customerInfo != null && customerInfo.size() > 0) {
						// 先删除之前的 customer prop-group.
						vn.push();
						ap.selectXPath("./hs:prop-group[@name='customer']");
						if (ap.evalXPath() != -1) {
							xm.remove(vn.getElementFragment());
						}
						vn.pop();

						// 保存新的 customer prop-group.
						propGroup += "<hs:prop-group name=\"customer\">";
						for (Entry<String, String> entry : customerInfo.entrySet()) {
							propGroup += "<hs:prop prop-type=\"" + escapeTag(entry.getKey()) + "\">"
									+ escapeTag(entry.getValue()) + "</hs:prop>";
						}
						propGroup += "</hs:prop-group>";
					}

					// 执行修改
					if (!"".equals(propGroup)) {
						xm.insertAfterHead(propGroup);

						ap.selectXPath("//hs:prop-group");
						if (ap.evalXPath() == -1) { // 不存在namespace“xmlns:hs”
							ap.selectXPath("/xliff");
							if (ap.evalXPath() != -1) {
								xm.insertAttribute(" xmlns:hs=\"" + hsR7NSUrl + "\" ");
							}
						}
					}

					// 保存文件并更新VTDNav对象
					saveAndReparse(xm, fileName);
				}
			} catch (XPathParseException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (XPathEvalException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (NavException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (ModifyException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 改变approved属性的值
	 * @param approvedValue
	 *            可选值：“yes”、“no”
	 * @param xm
	 *            XMLModifier对象;
	 * @throws NavException
	 * @throws UnsupportedEncodingException
	 * @throws ModifyException
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 */
	private boolean changeApproveProp(VTDNav vn, String approvedValue, XMLModifier xm) throws NavException,
			ModifyException, UnsupportedEncodingException, XPathParseException, XPathEvalException {
		if (approvedValue == null) {
			return false;
		}
		vn.push();
		boolean isChanged = false; // 当前的TransUnit的approved属性是否执行了修改
		int attrIdx = vn.getAttrVal("approved");
		if (approvedValue.equals("yes")) { // 批准
			if (attrIdx != -1) {
				String approved = vn.toString(attrIdx);

				if (!approved.equals(approvedValue)) { // approved属性值不为指定的approvedValue
					xm.updateToken(attrIdx, approvedValue);
					isChanged = true;
				}
			} else {
				xm.insertAttribute(" approved=\"" + approvedValue + "\" ");
				if (approvedValue.equals("yes")) {
					isChanged = true;
				}
			}

			String state = "translated";
			AutoPilot apState = new AutoPilot(vn);
			apState.selectXPath("./target");
			if (apState.evalXPath() != -1) { // 这里如果没有Target节点就不做处理
				attrIdx = vn.getAttrVal("state");
				if (attrIdx != -1) {
					xm.updateToken(attrIdx, state);
				} else {
					xm.insertAttribute(" state=\"" + state + "\" ");
				}
			}
		} else { // 取消批准
			if (attrIdx != -1) {
				vn.push();
				AutoPilot aptemp = new AutoPilot(vn);
				aptemp.selectXPath("./@approved");
				if (aptemp.evalXPath() != -1) { // 这里如果没有Target节点就不做处理
					xm.removeAttribute(vn.getCurrentIndex());
					isChanged = true;
				}
				vn.pop();
			}
		}

		if (isChanged) { // 当前的TransUnit的approved属性修改了,批准后为已翻译状态
			String state = "translated";
			AutoPilot apState = new AutoPilot(vn);
			apState.selectXPath("./target");
			if (apState.evalXPath() != -1) { // 这里如果没有Target节点就不做处理
				attrIdx = vn.getAttrVal("state");
				if (attrIdx != -1) {
					xm.updateToken(attrIdx, state);
				} else {
					xm.insertAttribute(" state=\"" + state + "\" ");
				}
			}
		}
		vn.pop();
		return isChanged;
	}

	/**
	 * 批准或取消批准所有的翻译单元
	 * @param approve
	 *            true：批准；false：取消批准;
	 */
	public List<String> approveAllTransUnits(boolean approve) {
		return approveAllTransUnits(approve, true);
	}

	/**
	 * 批准或取消批准所有的翻译单元
	 * @param approve
	 *            true：批准；false：取消批准;
	 */
	public List<String> approveAllTransUnits(final boolean approve, final boolean checkTargetWidth) {
		final ArrayList<String> list = new ArrayList<String>();
		handleAllSegment(new PerFileHandler() {
			public void handle(String fileName, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws ModifyException,
					XPathParseException, XPathEvalException, NavException, UnsupportedEncodingException {
				ap.selectXPath(XPATH_ALL_TU);
				while (ap.evalXPath() != -1) {
					String approvedValue = approve ? "yes" : "no";
					changeApproveProp(vu.getVTDNav(), approvedValue, xm);
				}
				saveAndReparse(xm, fileName); // 保存并更新VTDNav对象
			}
		});
		return list;
	}

	/**
	 * 批准或取消批准指定Id的翻译单元
	 * @param rowIdList
	 *            要修改的翻译单元Id的集合;
	 * @param approve
	 *            true：批准；false：取消批准;
	 * @throws XliffException
	 */
	public List<String> approveTransUnits(List<String> rowIdList, boolean approve) {
		return approveTransUnits(rowIdList, approve, true);
	}

	/**
	 * 批准或取消批准指定Id的翻译单元
	 * @param rowIdList
	 *            要修改的翻译单元Id的集合;
	 * @param approve
	 *            true：批准；false：取消批准;
	 * @throws XliffException
	 */
	public List<String> approveTransUnits(List<String> rowIdList, final boolean approve, final boolean checkTargetWidth) {
		if (rowIdList == null || rowIdList.isEmpty()) {
			return Collections.<String> emptyList();
		}
		final ArrayList<String> list = new ArrayList<String>();
		handleSomeSegment(rowIdList, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
				if (vu.pilot(ap, tuXPath) != -1) {
					String approvedValue = approve ? "yes" : "no";
					changeApproveProp(vu.getVTDNav(), approvedValue, xm);
				}
			}
		});
		return list;
	}

	/**
	 * 改变translate属性的值
	 * @param translateValue
	 *            可选值：“yes”、“no”
	 * @param xm
	 *            XMLModifier对象;
	 * @throws NavException
	 * @throws UnsupportedEncodingException
	 * @throws ModifyException
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 */
	private boolean changeTranslateProp(VTDNav vn, String translateValue, XMLModifier xm) throws NavException,
			ModifyException, UnsupportedEncodingException, XPathParseException, XPathEvalException {
		if (translateValue == null) {
			return false;
		}

		vn.push();
		boolean isChanged = false; // 当前的TransUnit的translate属性是否执行了修改
		int attrIdx = vn.getAttrVal("translate");
		if (attrIdx != -1) { // 存在translate属性
			String translate = vn.toString(attrIdx);
			if (!translate.equals(translateValue)) { // translate属性值不为指定的translateValue
				xm.updateToken(attrIdx, translateValue);
				isChanged = true;
			}
		} else {
			xm.insertAttribute(" translate=\"" + translateValue + "\" ");
			if (translateValue.equals("no")) { // 默认值为yes
				isChanged = true;
			}
		}
		vn.pop();
		return isChanged;
	}

	/**
	 * 修改所有翻译单元的translate属性值
	 * @param translateValue
	 *            translate属性的值
	 */
	private void changeAllTranslateProp(final String translateValue) {
		handleAllSegment(new PerFileHandler() {

			public void handle(String fileName, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws ModifyException,
					XPathParseException, XPathEvalException, NavException, UnsupportedEncodingException {
				ap.selectXPath(XPATH_ALL_TU);
				while (ap.evalXPath() != -1) {
					changeTranslateProp(vu.getVTDNav(), translateValue, xm);
				}
				saveAndReparse(xm, fileName); // 保存并更新VTDNav对象
			}
		});
	}

	/**
	 * 若当前目标文本段（target）内容不为空，则自动将其 state 属性值设为“translated”
	 * @param rowIdList
	 *            要修改的翻译单元Id的集合;
	 */
	public void changeSomeTranslateProp(List<String> rowIdList) {
		if (rowIdList == null) {
			return;
		}
		Map<String, List<String>> map = RowIdUtil.groupRowIdByFileName(rowIdList);

		VTDNav vn = null;
		AutoPilot ap = new AutoPilot();

		for (Entry<String, List<String>> entry : map.entrySet()) {
			String fileName = entry.getKey();
			List<String> rowIds = entry.getValue();
			vn = vnMap.get(fileName);
			ap.bind(vn);

			boolean isNew;
			for (int i = 0; i < rowIds.size(); i++) {
				ap.resetXPath();
				String rowId = rowIds.get(i);

				String tuXPath = RowIdUtil.parseRowIdToXPathWithCondition(rowId,
						"(not(source='')) and (not(target=''))"/* 当前目标文本段内容不为空 */);
				try {
					isNew = false;
					ap.selectXPath(tuXPath + "/target/@state");
					int stateIdx = -1;
					if ((stateIdx = ap.evalXPath()) != -1) {
						String state = vn.toNormalizedString(stateIdx + 1);
						if ("new".equalsIgnoreCase(state)) {
							isNew = true;
						}
					}
					if (!isNew && rowIdList.size() > i) { // state不为“new”
						rowIdList.remove(i);
					}
					// else { // state为“new”，则继续找下一个
					//
					// }
				} catch (XPathParseException e) {
					LOGGER.error("", e);
					e.printStackTrace();
				} catch (XPathEvalException e) {
					LOGGER.error("", e);
					e.printStackTrace();
				} catch (NavException e) {
					LOGGER.error("", e);
					e.printStackTrace();
				}
			}
		}

		if (rowIdList.size() > 0) {
			changeTgtPropValue(rowIdList, "state", "translated", "new");
		}
	}

	/**
	 * 修改指定Id的翻译单元的translate属性值
	 * @param translateValue
	 *            translate属性的值
	 * @param rowIdList
	 *            要修改的翻译单元Id的集合;
	 */
	private void changeSomeTranslateProp(final String translateValue, List<String> rowIdList) {
		handleSomeSegment(rowIdList, new PerSegmentHandler() {

			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
				ap.selectXPath(tuXPath);
				if (ap.evalXPath() != -1) {
					changeTranslateProp(vu.getVTDNav(), translateValue, xm);
				}
			}
		});
	}

	/**
	 * 锁定或取消锁定所有的翻译单元
	 * @param lock
	 *            true：批准；false：取消批准;
	 */
	public void lockAllTransUnits(boolean lock) {
		if (lock) {
			changeAllTranslateProp("no");
		} else {
			changeAllTranslateProp("yes");
		}
	}

	/**
	 * 锁定或取消锁定指定Id的翻译单元
	 * @param rowIdList
	 *            要修改的翻译单元Id的集合;
	 * @param lock
	 *            true：批准；false：取消批准;
	 */
	public void lockTransUnits(List<String> rowIdList, boolean lock) {
		if (lock) {
			changeSomeTranslateProp("no", rowIdList);
		} else {
			changeSomeTranslateProp("yes", rowIdList);
		}
	}

	/**
	 * 根据 XPath 修改指定的 TU 子节点的属性值（newValue 为 null 时表示移除该属性）
	 * @param xm
	 *            XMLModifier对象
	 * @param rowId
	 *            行的唯一标识，由三部分拼成
	 * @param subXPath
	 *            定位到tu下的子节点文本的xpath，例如："/target/@state"
	 * @param newValue
	 *            修改的新值，当为null时表示移除该属性
	 * @param currentValueRange
	 *            当前值的范围，<b>如果当前值不在此范围内，则不进行此次修改</b>
	 * @throws XPathParseException
	 * @throws UnsupportedEncodingException
	 * @throws ModifyException
	 * @throws NavException
	 * @throws XPathEvalException
	 */
	private void changePropValue(VTDUtils vu, AutoPilot ap, XMLModifier xm, String rowId, String subXPath,
			String newValue, String... currentValueRange) throws XPathParseException, XPathEvalException, NavException,
			ModifyException, UnsupportedEncodingException {
		String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
		String propXpath = "." + subXPath; // 可以定位TU下的任何子节点属性
		if (vu.pilot(ap, tuXPath) != -1) { // 导航到 TU 节点
			String currentValue = vu.getValue(ap, propXpath);
			if ((currentValue == null && newValue == null) || (currentValue != null && currentValue.equals(newValue))) {
				return;
			}
			if ((currentValue == null && newValue != null) && currentValueRange == null
					|| currentValueRange.length == 0 || CommonFunction.contains(currentValueRange, currentValue)) {
				vu.update(ap, xm, propXpath, newValue, VTDUtils.CREATE_IF_NOT_EXIST);
			}
		}
	}

	/**
	 * 修改翻译单元内容并保存
	 * @param rowId
	 * @param subXPath
	 * @param newValue
	 *            ;
	 */
	public void updateAndSave(String rowId, String subXPath, String newValue) {
		String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
		String fileName = RowIdUtil.getFileNameByRowId(rowId);
		VTDNav vn = vnMap.get(fileName);
		try {
			VTDUtils vu = new VTDUtils(vn);
			XMLModifier xm = vu.update(tuXPath + subXPath, newValue, VTDUtils.CREATE_IF_NOT_EXIST);
			saveAndReparse(xm, fileName);
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 删除翻译单元内容并保存
	 * @param rowId
	 * @param subXPath
	 * @param newValue
	 *            ;
	 */
	private void deleteAndSave(String rowId, String subXPath) {
		String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
		String fileName = RowIdUtil.getFileNameByRowId(rowId);
		VTDNav vn = vnMap.get(fileName);
		try {
			VTDUtils vu = new VTDUtils(vn);
			XMLModifier xm = vu.delete(tuXPath + subXPath);
			saveAndReparse(xm, fileName);
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 修改指定Source文本
	 * @param rowId
	 * @param newValue
	 *            ;
	 */
	public void changeSrcTextValue(String rowId, String newValue) {
		newValue = newValue == null ? "" : newValue;
		updateAndSave(rowId, "/source/text()", newValue);
		//bug #2850 更改 source 节点后，将非“未翻译”，“草稿”状态的数据更新为 “草稿”
		String tgtContent = getTgtContent(rowId);
		if (tgtContent != null) {//如果 如果 有 target 节点
			final String xpath = RowIdUtil.parseRowIdToXPath(rowId);
			if (xpath != null) {
				handleSomeSegment(Arrays.asList(rowId), new PerSegmentHandler() {
					public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm)
							throws XPathParseException, XPathEvalException, NavException,
							ModifyException, UnsupportedEncodingException {
							int approvedVal = vu.pilot(ap, xpath.concat("/@approved"));
							if (approvedVal != -1) {//删除 approve 节点
								xm.removeAttribute(approvedVal);
							}
							int statuId = vu.pilot(ap, xpath.concat("/target/@state"));
							if (statuId != -1) {//更新 state 节点
								xm.updateToken(statuId + 1, "new");
							}
					}
				});
			}
		}
	}

	/**
	 * 修改指定Source文本
	 * @param rowId
	 * @param newValue
	 *            ;
	 */
	public void changeSrcTextValue(List<String> rowIds, String newValue) {
		changeTextValue(rowIds, "/source", newValue);
	}

	/**
	 * 修改指定 Target 文本。如果改为 <code>null</code> 或者空字符串，则自动将 state 属性设置为 new。
	 * @param rowId
	 * @param newValue
	 *            ;
	 */
	public void changeTgtTextValue(String rowId, String newValue, final String matchType, final String quality) {
		if (newValue == null) {
			newValue = "";
		}
		// updateAndSave(rowId, "/target/text()", newValue);
		final String _newValue = newValue;
		List<String> list = Arrays.asList(rowId);
		if (newValue.equals("")) {
			handleSomeSegment(list, new PerSegmentHandler() {
				public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
						XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
					String _quality = quality;
					String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
					if (vu.pilot(ap, tuXPath) == -1) { // 导航到 TU 节点
						return;
					}
					int id = vu.getVTDNav().getAttrVal("approved");
					if (id != -1) {
						xm.removeAttribute(id - 1);
					}
					if (vu.pilot(ap, "./target") == -1) {
						vu.pilot("./source");
					} else {
						if(_quality == null || quality.equals("")){
							_quality = vu.getCurrentElementAttribut("hs:quality", null);							
						}
						xm.remove();
					}
					StringBuffer bf = new StringBuffer();
					bf.append("<target ");
					if (_quality != null && !_quality.equals("")) {
						bf.append(" hs:quality=\"").append(_quality).append("\"");
					}
					bf.append("></target>");
					xm.insertAfterElement(bf.toString());;
				}
			});
		} else {
			handleSomeSegment(list, new PerSegmentHandler() {
				public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
						XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
					String _quality = quality;
					String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
					if (vu.pilot(ap, tuXPath) == -1) { // 导航到 TU 节点
						return;
					}
					int id = vu.getVTDNav().getAttrVal("approved");
					if (id != -1) {
						xm.removeAttribute(id - 1);
					}
					boolean flg = false;
					if (vu.pilot("./target") == -1) {
						vu.pilot("./source");
					} else {
						if(_quality == null || quality.equals("")){
							_quality = vu.getCurrentElementAttribut("hs:quality", null);
							flg = true;
						}
						xm.remove();
					}
					StringBuffer bf = new StringBuffer();
					bf.append("<target ").append("state=\"new\"");
					if (!flg && matchType != null && !matchType.equals("")) {
						bf.append(" hs:matchType=\"").append(matchType).append("\"");
					}
					if (_quality != null && !_quality.equals("")) {
						bf.append(" hs:quality=\"").append(_quality).append("\"");
					}
					bf.append(">").append(_newValue).append("</target>");
					xm.insertAfterElement(bf.toString());
				}
			});
		}
	}

	/**
	 * 修改指定 Target 文本。如果改为 <code>null</code> 或者空字符串，则自动将 state 属性设置为 new。
	 * @param rowId
	 * @param newValue
	 *            ;
	 */
	public void changeTgtTextValue(List<String> rowIds, String newValue, final String matchType, final String quality) {
		if (newValue == null) {
			newValue = "";
		}
		// changeTextValue(rowIds, "/target/text()", newValue);
		final String _newValue = newValue;
		if (newValue.equals("")) {
			handleSomeSegment(rowIds, new PerSegmentHandler() {
				public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
						XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
					String _quality = quality;
					String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
					if (vu.pilot(ap, tuXPath) == -1) { // 导航到 TU 节点
						return;
					}
					int id = vu.getVTDNav().getAttrVal("approved");
					if (id != -1) {
						xm.removeAttribute(id - 1);
					}
					if (vu.pilot(ap, "./target") == -1) {
						vu.pilot("./source");
					} else {
						if(_quality == null || quality.equals("")){
							_quality = vu.getCurrentElementAttribut("hs:quality", null);							
						}
						xm.remove();
					}
					StringBuffer bf = new StringBuffer();
					bf.append("<target ");
					if (_quality != null && !_quality.equals("")) {
						bf.append(" hs:quality=\"").append(_quality).append("\"");
					}
					bf.append("></target>");
					xm.insertAfterElement(bf.toString());;
				}
			});
		} else {
			handleSomeSegment(rowIds, new PerSegmentHandler() {
				public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
						XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
					String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
					if (vu.pilot(ap, tuXPath) == -1) { // 导航到 TU 节点
						return;
					}
					int id = vu.getVTDNav().getAttrVal("approved");
					if (id != -1) {
						xm.removeAttribute(id - 1);
					}
					if (vu.pilot("./target") == -1) {
						vu.pilot("./source");
					} else {
						xm.remove();
					}
					StringBuffer bf = new StringBuffer();
					bf.append("<target ").append("state=\"new\"");
					if (matchType != null && !matchType.equals("")) {
						bf.append(" hs:matchType=\"").append(matchType).append("\"");
					}
					if (quality != null && !quality.equals("")) {
						bf.append(" hs:quality=\"").append(quality).append("\"");
					}
					bf.append(">").append(_newValue).append("</target>");
					xm.insertAfterElement(bf.toString());
				}
			});
		}
	}

	/**
	 * 修改指定 Target 文本。如果改为 <code>null</code> 或者空字符串，则自动将文本段状态置为未翻译, 否则置为new
	 * @param key
	 *            ：rowId，value：要修改的值 ;
	 */
	public void changeTgtTextValue(final Map<String, String> map, final String matchType, final String quality) {
		if (map == null || map.size() == 0) {
			return;
		}
		List<String> rowIds = Arrays.asList(map.keySet().toArray(new String[] {}));
		handleSomeSegment(rowIds, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				String _quality = quality;
				String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
				if (vu.pilot(ap, tuXPath) == -1) { // 导航到 TU 节点
					return;
				}
				int id = vu.getVTDNav().getAttrVal("approved");
				if (id != -1) {
					xm.removeAttribute(id - 1);
				}
				boolean flg = false;
				if (vu.pilot("./target") == -1) {
					vu.pilot("./source");
				} else {
					if(_quality == null || quality.equals("")){
						_quality = vu.getCurrentElementAttribut("hs:quality", null);
						flg = true;
					}
					xm.remove();
				}
				String value = map.get(rowId);
				StringBuffer bf = new StringBuffer();
				if (value == null || value.equals("")) {
					bf.append("<target ");
					if (_quality != null && !_quality.equals("")) {
						bf.append(" hs:quality=\"").append(_quality).append("\"");
					}
					bf.append("></target>");
				} else {
					bf.append("<target ").append("state=\"new\"");
					if (!flg && matchType != null && !matchType.equals("")) {
						bf.append(" hs:matchType=\"").append(matchType).append("\"");
					}
					if (_quality != null && !_quality.equals("")) {
						bf.append(" hs:quality=\"").append(_quality).append("\"");
					}
					bf.append(">").append(value).append("</target>");
				}
				xm.insertAfterElement(bf.toString());
			}
		});
		// map.clear();
	}

	public void copyAllSource2Target() {
		handleSomeSegment(rowIds, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
				if (vu.pilot(ap, tuXPath) == -1) { // 导航到 TU 节点
					return;
				}
				int id = vu.getVTDNav().getAttrVal("translate");
				if (id != -1 && vu.getVTDNav().toString(id).equalsIgnoreCase("no")) {
					return;
				}
				String srcContent = vu.getChildContent("source");
				if (vu.pilot(ap, "./target") != -1) {
					vu.getVTDNav().push();
					String tgtContent = vu.getElementContent();
					if (tgtContent != null && !tgtContent.equals("")) {
						return;
					}
					xm.remove();
					vu.getVTDNav().pop();
				}
				vu.pilot(ap, "./source");

				StringBuffer bf = new StringBuffer();
				bf.append("<target ").append("state=\"new\"").append(">");
				bf.append(srcContent);
				bf.append("</target>");
				xm.insertAfterElement(bf.toString());
			}
		});
	}

	/**
	 * 根据XPath修改指定TU子节点的文本值
	 * @param rowId
	 * @param subXPath
	 *            定位到tu下的子节点文本的xpath，例如："/target/text()"
	 * @param newValue
	 *            修改的值
	 */
	private void changeTextValue(List<String> rowIds, final String subXPath, final String newValue) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (String rowId : rowIds) {
			map.put(rowId, newValue);
		}
		changeTextValue(map, subXPath);
	}

	/**
	 * 根据 XPath 修改指定TU子节点的文本值
	 * @param map
	 *            key：rowId，value：要修改的值
	 * @param subXPath
	 *            定位到tu下的子节点文本的xpath，例如："/target/text()"
	 */
	private void changeTextValue(final Map<String, String> map, final String subXPath) {
		ArrayList<String> rowIds = new ArrayList<String>(map.keySet());
		handleSomeSegment(rowIds, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException {
				String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
				vu.update(ap, xm, tuXPath + subXPath, map.get(rowId), VTDUtils.CREATE_IF_NOT_EXIST);
			}
		});
	}

	/**
	 * 得到TU下指定节点的指定属性值
	 * @param rowIdList
	 *            用来生成Xpath的rowId集合
	 * @param subXPath
	 *            定位到tu下的子节点属性的xpath，例如："/target/@state"
	 * @return 由rowId与得到的属性值的映射map。<br/>
	 *         key: rowId; value: 属性值;
	 */
	private Map<String, String> getPropValue(List<String> rowIdList, String subXPath) {
		if (rowIdList == null) {
			return null;
		}
		if (subXPath.lastIndexOf('/') > subXPath.lastIndexOf('@')) { // 此subXPath要获取的并不是属性
			LOGGER.error(Messages.getString("file.XLFHandler.logger9"));
			return null;
		}
		Map<String, String> propValueMap = new HashMap<String, String>();

		AutoPilot ap = new AutoPilot();
		try {
			ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			int index = -1;
			for (String rowId : rowIdList) {
				VTDNav vn = getVTDNavByRowId(rowId);
				vn.push();
				ap.bind(vn);

				String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
				String propXpath = tuXPath + subXPath; // 可以定位TU下的任何子节点的属性
				ap.selectXPath(propXpath);
				if ((index = ap.evalXPath()) != -1) {
					propValueMap.put(rowId, vn.toNormalizedString(index + 1));
				} else {
					propValueMap.put(rowId, null);
				}
				vn.pop();
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return propValueMap;
	}

	/**
	 * 得到Target节点属性的值
	 * @param rowId
	 *            行的唯一标识
	 * @return ;
	 */
	public String getTgtPropValue(String rowId, String propName) {
		VTDNav vn = getVTDNavByRowId(rowId);
		String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
		try {
			VTDUtils vu = new VTDUtils(vn);
			return vu.getValue(tuXPath + "/target/@" + propName);
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 得到Target节点属性的值
	 * @param rowIdList
	 * @return ;
	 */
	public Map<String, String> getTgtPropValue(List<String> rowIdList, String propName) {
		return getPropValue(rowIdList, "/target/@" + propName);
	}

	/**
	 * 获取Trans-unit节点的属性值
	 * @param rowIdList
	 * @param propName
	 * @return ;
	 */
	public Map<String, String> getTuPropValue(List<String> rowIdList, String propName) {
		return getPropValue(rowIdList, "/trans-unit/@" + propName);
	}

	@Override
	protected Map<String, Object> openFile(File file, int tuCount) {
		// TODO 暂时不用这个实现，留空
		return null;
	}

	/**
	 * 修改指定 Xpath 的 target 节点的属性值，并限定当前值的范围，如果当前的值不在范围内，则不做修改。
	 * @param rowIdList
	 *            rowid集合
	 * @param propName
	 *            属性名
	 * @param propValue
	 *            修改的值
	 * @param currentValueRange
	 *            当前属性值的范围
	 */
	public void changeTgtPropValue(List<String> rowIdList, String propName, final String propValue,
			final String... currentValueRange) {
		if (rowIdList == null || rowIdList.size() == 0) {
			return;
		}
		final String subXPath = "/target/@" + propName;
		handleSomeSegment(rowIdList, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				changePropValue(vu, ap, xm, rowId, subXPath, propValue, currentValueRange);
			}
		});
	}

	/**
	 * 修改指定 Xpath 的 target 节点的属性值，并限定当前值的范围，如果当前的值不在范围内，则不做修改。
	 * @param map
	 *            rowId与属性值的映射
	 * @param propName
	 *            属性名
	 * @param currentValueRange
	 *            当前属性值的范围
	 */
	public void changeTgtPropValue(final Map<String, String> map, String propName, final String... currentValueRange) {
		ArrayList<String> rowIdList = new ArrayList<String>(map.keySet());

		final String subXPath = "/target/@" + propName;
		handleSomeSegment(rowIdList, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				changePropValue(vu, ap, xm, rowId, subXPath, map.get(rowId), currentValueRange);
			}
		});
	}

	/**
	 * 修改指定 Xpath 的 target 节点的属性值，并限定当前值的范围，如果当前的值不在范围内，则不做修改。
	 * @param rowIdList
	 *            rowid集合
	 * @param propName
	 *            属性名
	 * @param propValue
	 *            修改的值
	 * @param currentValueRange
	 *            当前属性值的范围
	 */
	public void changeTuPropValue(List<String> rowIdList, String propName, final String propValue) {
		if (rowIdList == null || rowIdList.size() == 0) {
			return;
		}
		final String subXPath = "/@" + propName;
		handleSomeSegment(rowIdList, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				changePropValue(vu, ap, xm, rowId, subXPath, propValue);
			}
		});
	}

	/**
	 * 改变Target的状态,signed-off需要修改approved=yes,改为new或translated需要删除approved=yes属性
	 * @param selectedRowIds
	 * @param state
	 *            ;
	 */
	public void changeTransUnitState(List<String> selectedRowIds, final String state) {
		handleSomeSegment(selectedRowIds, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
				if (vu.pilot(ap, tuXPath) == -1) { // 导航到 TU 节点
					return;
				}
				if (state.equals("translated") || state.equals("new")) { // 切换到已翻译或草稿状态
					int id = vu.getVTDNav().getAttrVal("approved");
					if (id != -1) {
						xm.removeAttribute(id - 1);
					}
				} else if (state.equals("signed-off")) { // 签发状态需要修改当前文本为批准状态
					int id = vu.getVTDNav().getAttrVal("approved");
					if (id != -1) {
						if (!"yes".equals(vu.getVTDNav().toString(id)))
							xm.updateToken(id, "yes");
					} else {
						xm.insertAttribute(" approved=\"yes\" ");
					}
				}
				if (vu.pilot(ap, "./target") != -1) {
					int id = vu.getVTDNav().getAttrVal("state");
					if (id != -1) {
						xm.updateToken(id, state);
					} else {
						xm.insertAttribute(" state=\"" + state + "\"");
					}
				}
			}
		});
	}

	/**
	 * 修改指定 Xpath 的 trans-unit 节点的属性值。
	 * @param map
	 *            rowId与属性值的映射
	 * @param propName
	 *            属性名
	 */
	public void changeTuPropValue(final Map<String, String> map, String propName) {
		ArrayList<String> rowIdList = new ArrayList<String>(map.keySet());

		final String subXPath = "/@" + propName;
		handleSomeSegment(rowIdList, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				changePropValue(vu, ap, xm, rowId, subXPath, map.get(rowId));
			}
		});
	}

	/**
	 * 删除Trans-unit节点的属性
	 * @param rowIdList
	 * @param propName
	 *            ;
	 */
	public void deleteTuProp(List<String> rowIdList, String propName) {
		if (rowIdList == null || rowIdList.size() == 0) {
			return;
		}
		final String subXPath = "/@" + propName;
		handleSomeSegment(rowIdList, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				deleteNodeProp(vu, ap, xm, rowId, subXPath);
			}
		});
	}

	/**
	 * 删除target节点的属性
	 * @param rowIdList
	 * @param propName
	 *            ;
	 */
	public void deleteTgtProp(List<String> rowIdList, String propName) {
		if (rowIdList == null || rowIdList.size() == 0) {
			return;
		}
		final String subXPath = "/target/@" + propName;
		handleSomeSegment(rowIdList, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				deleteNodeProp(vu, ap, xm, rowId, subXPath);
			}
		});
	}

	/**
	 * 删除节点的属性
	 * @param vu
	 * @param ap
	 * @param xm
	 * @param rowId
	 * @param subXPath
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 * @throws ModifyException
	 * @throws UnsupportedEncodingException
	 *             ;
	 */
	private void deleteNodeProp(VTDUtils vu, AutoPilot ap, XMLModifier xm, String rowId, String subXPath)
			throws XPathParseException, XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
		String propXpath = "." + subXPath; // 可以定位TU下的任何子节点属性
		if (vu.pilot(ap, tuXPath) != -1) { // 导航到 TU 节点
			AutoPilot aptemp = new AutoPilot(vu.getVTDNav());
			aptemp.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			aptemp.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			aptemp.selectXPath(propXpath);
			if (aptemp.evalXPath() != -1) { // 这里如果没有Target节点就不做处理
				xm.removeAttribute(vu.getVTDNav().getCurrentIndex());
			}
		}
	}

	/**
	 * 得到源的纯文本内容
	 * @param rowId
	 *            行的唯一标识
	 * @return 源的纯文本内容;
	 */
	public String getSrcPureText(String rowId) {
		String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
		VTDNav vn = getVTDNavByRowId(rowId);
		try {
			VTDUtils vu = new VTDUtils(vn);
			vn.push();
			vu.pilot(tuXPath + "/source");
			String result = getTUPureText(vu.getVTDNav());
			vn.pop();
			return result;
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 得到源的完整内容
	 * @param rowId
	 *            行的唯一标识
	 * @return 源的完整内容;
	 */
	public String getSrcContent(String rowId) {
		String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
		VTDNav vn = getVTDNavByRowId(rowId);
		try {
			VTDUtils vu = new VTDUtils(vn);
			return vu.getValue(tuXPath + "/source/text()");
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 得到目标的完整内容
	 * @param rowId
	 *            行的唯一标识
	 * @return 目标的完整内容;
	 */
	public String getTgtContent(String rowId) {
		String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
		VTDNav vn = getVTDNavByRowId(rowId);
		try {
			VTDUtils vu = new VTDUtils(vn);
			return vu.getValue(tuXPath + "/target/text()");
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return null;
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
	 * 界面上的排序方法，即点击 natable 列头进行排序的方法。robert	2013-02-18
	 * @param columnName 要排序的列名，分为源与目标
	 * @param isAsc	要排序列的排序类型 true:升序，false:降序;
	 */
	public void sort(String columnName, boolean isAsc){
		resetCache();

		HashSet<String> set = null;
		set = new HashSet<String>(rowIds);
		List<String> sortedRowIds = null;
		try {
			sortedRowIds = getSortRowIdList(set, columnName, isAsc);
			rowIds.clear();
			rowIds.ensureCapacity(sortedRowIds.size()); // 设置容量
			rowIds.addAll(sortedRowIds);

		} catch (Exception e) {
			resetRowIdsToUnsorted(); // 恢复原始顺序
			LOGGER.error("", e);
			e.printStackTrace();
		} finally {
			if (sortedRowIds != null) {
				sortedRowIds.clear();
				sortedRowIds = null;
			}
			System.gc();
		}
	}
	
	/**
	 * 获取排序的 rowId 集合	robert	2013-02-18
	 * @param textMap
	 * @param isAsc
	 * @return
	 */
	public List<String> getSortRowIdList(HashSet<String> rowIdSet, String columnName, final boolean isAsc){
		List<String> sortList = new LinkedList<String>();
		
		// 先通过界面上所显示的文本段，
		Map<String, String> textMap = new HashMap<String, String>();
		AutoPilot ap = new AutoPilot();
		AutoPilot childAP = new AutoPilot();
		for (Entry<String, VTDNav> entry : vnMap.entrySet()) {
			String filePath = entry.getKey();
			VTDNav vn = entry.getValue();
			ap.bind(vn);
			childAP.bind(vn);
			
			try {
				String xpath = XPATH_ALL_TU;
				ap.selectXPath(xpath);
				while(ap.evalXPath() != -1){
					String rowId = RowIdUtil.getRowId(vn, filePath);
					if (rowIdSet.contains(rowId)) {
						vn.push();
						childAP.selectXPath("./" + columnName);
						String tgtText = "";
						if (childAP.evalXPath() != -1) {
							tgtText = getTUPureText(vn);
						}
						// 排序时应忽略段首段末空格，忽略大小写
						textMap.put(rowId, tgtText.trim().toLowerCase());
						vn.pop();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		List<Entry<String, String>> mapList = new ArrayList<Entry<String, String>>(
				textMap.entrySet());
		//排序
		Collections.sort(mapList, new Comparator<Entry<String, String>>() {   
		    public int compare(Entry<String, String> o1, Entry<String, String> o2) {    
		    	int result = o1.getValue().compareTo(o2.getValue());
		        return isAsc ? result : -result;
		    }
		}); 
		for (Entry<String, String> entry : mapList) {
			sortList.add(entry.getKey());
		}
		return sortList;
	}

	/**
	 * 删除所有标记。
	 * @param rowIdList
	 *            rowId集合 ;
	 */
	public HashMap<String, String> removeAllTags(List<String> rowIdList) {
		final HashMap<String, String> tgtContentMap = new HashMap<String, String>();
		handleSomeSegment(rowIdList, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
				if (vu.pilot(ap, tuXPath) != -1) {
					int attrIdx = vu.getVTDNav().getAttrVal("translate");
					if (attrIdx != -1) { // 存在translate属性
						String c = vu.getVTDNav().toString(attrIdx);
						if (c != null && c.equals("no")) {
							return;
						}
					}
					vu.getVTDNav().push();
					if (vu.pilot(ap, "./target") != -1) {
						String currContent = vu.getElementContent();
						String currPure = getTUPureText(vu.getVTDNav());
						if (currContent == null || currContent.equals("") || currContent.equals(currPure)) {
							vu.getVTDNav().pop();
							return;
						}
						xm.remove();

						StringBuffer bf = new StringBuffer();
						bf.append("<target state=\"new\">");
						bf.append(currPure);
						bf.append("</target>");
						xm.insertAfterElement(bf.toString());
						vu.getVTDNav().pop();

						int apprIndx = vu.getVTDNav().getAttrVal("approved");
						if (apprIndx != -1) {
							xm.removeAttribute(apprIndx - 1);
						}
					} else {
						vu.getVTDNav().pop();
					}
					// String targetTextXpath = "./target/text()"; // 导航到 target 内容节点的 XPath
					// tgtContentMap.put(rowId, vu.getElementFragment()); // 保存之前的target节点的内容
					// String pureText = getTUPureTextByRowId(rowId, false); // 去除Target
					// pureText = pureText == null ? "" : pureText;
					// vu.getVTDNav().push();
					// vu.delete(ap, xm, targetTextXpath, VTDUtils.PILOT_TO_END);
					// vu.getVTDNav().pop();
					// vu.insert(ap, xm, targetTextXpath, pureText);
				}
			}
		});
		// changeTgtPropValue(rowIdList, "state", "new"); // 修改其 target 节点的 state
		// deleteTgtProp(rowIdList, "hs:matchType");
		// deleteTgtProp(rowIdList, "hs:quality");
		// deleteTuProp(rowIdList, "approved");

		return tgtContentMap;
	}

	/**
	 * 恢复删除的标记 ;
	 */
	public void resetRemoveAllTags(final HashMap<String, String> map) {
		ArrayList<String> rowIdList = new ArrayList<String>();
		Collections.addAll(rowIdList, map.keySet().toArray(new String[] {}));

		handleSomeSegment(rowIdList, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException, UnsupportedEncodingException {
				String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
				vu.update(ap, xm, tuXPath, map.get(rowId), VTDUtils.CREATE_IF_NOT_EXIST);
			}
		});
	}

	/**
	 * 分割文本段
	 * @param rowId
	 *            要分割的行的RowId
	 * @param spltOffset
	 *            分割的位置;
	 * @return ;
	 */
	public String splitSegment(String rowId, int spltOffset) {
		String fileName = RowIdUtil.getFileNameByRowId(rowId);
		VTDNav vn = vnMap.get(fileName);
		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		String xpath = RowIdUtil.parseRowIdToXPath(rowId);
		try {
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath(xpath);
			if (ap.evalXPath() == -1) {
				return "";
			}
			String tuid = vu.getCurrentElementAttribut("id", "");
			String xmlSpace = vu.getCurrentElementAttribut("xml:space", "preserve");
			String approved = vu.getCurrentElementAttribut("approved", null);
			String sendToTM = vu.getCurrentElementAttribut("hs:send-to-tm", null);
			String needReview = vu.getCurrentElementAttribut("hs:needs-review", null);
			String tuHead = vu.getElementHead();

			// 删除 approved 属性
			if (approved != null) {
				tuHead = tuHead.replace(" approved=\"" + approved + "\"", "");
				tuHead = tuHead.replace(" approved='" + approved + "'", "");
			}

			// tuHead2 删除 hs:send-to-tm, hs:needs-review 属性
			String tuHead2 = tuHead;
			if (sendToTM != null) {
				tuHead2 = tuHead2.replace(" hs:send-to-tm=\"" + sendToTM + "\"", "");
				tuHead2 = tuHead2.replace(" hs:send-to-tm='" + sendToTM + "'", "");
			}
			if (needReview != null) {
				tuHead2 = tuHead2.replace(" hs:needs-review=\"" + needReview + "\"", "");
				tuHead2 = tuHead2.replace(" hs:needs-review='" + needReview + "'", "");
			}

			StringBuffer tu1;
			StringBuffer tu2;
			if (tuHead.contains("id=\"" + tuid + "\"")) {
				tu1 = new StringBuffer(tuHead.replace("id=\"" + tuid + "\"", "id=\"" + tuid + "-1\""));
				tu2 = new StringBuffer(tuHead2.replace("id=\"" + tuid + "\"", "id=\"" + tuid + "-2\""));
			} else if (tuHead.contains("id='" + tuid + "'")) {
				tu1 = new StringBuffer(tuHead.replace("id='" + tuid + "'", "id=\"" + tuid + "-1\""));
				tu2 = new StringBuffer(tuHead2.replace("id='" + tuid + "'", "id=\"" + tuid + "-2\""));
			} else { // 不存在 id 属性
				return "";
			}
			String sourceFragment1 = null;
			String sourceFragment2 = null;
			String targetFragment1 = null;
			String targetFragment2 = null;
			ap.selectXPath(xpath + "/source");
			if (ap.evalXPath() != -1) {
				String sourceHead = vu.getElementHead();
				if (sourceHead != null) {
					String sourceContent = vu.getElementContent(); // source节点的内容

					// 处理光标在 g 标记内的情况 --robert 2012-11-15
					List<Map<String, String>> tagLocationList = getTagLocation(vn, sourceContent);
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

					sourceFragment1 = sourceHead + sourceContent.substring(0, spltOffset) + srcAddStr1 + "</source>";
					sourceFragment2 = sourceHead + srcAddStr2 + sourceContent.substring(spltOffset) + "</source>";
				}
			}
			ap.selectXPath(xpath + "/target");
			if (ap.evalXPath() != -1) {
				String state = vu.getCurrentElementAttribut("state", null);
				String targetHead = vu.getElementHead();
				if (targetHead != null) {
					if (state != null && !state.equalsIgnoreCase("new")) {
						targetHead = targetHead.replace(" state=\"" + state + "\"", " state=\"new\"");
						targetHead = targetHead.replace(" state='" + state + "'", " state=\"new\"");
						targetFragment1 = targetHead + vu.getElementContent() + "</target>";
					} else {
						targetFragment1 = vu.getElementFragment(); // target节点的段落
					}
					// targetFragment2 = targetHead + "</target>";
					targetFragment2 = "<target></target>";// modify by peason---- Bug #1048
				}
			}

			if (sourceFragment1 != null) {
				tu1.append(sourceFragment1);
			}
			if (targetFragment1 != null) {
				tu1.append(targetFragment1);
			}
			if (sourceFragment2 != null) {
				tu2.append(sourceFragment2);
			}
			if (targetFragment2 != null) {
				tu2.append(targetFragment2);
			}
			// 批注信息添加到分割后的第一个文本段中
			ap.selectXPath(xpath + "/note");
			while (ap.evalXPath() != -1) {
				tu1.append(vu.getElementFragment());
			}
			tu1.append("</trans-unit>");
			tu2.append("</trans-unit>");

			StringBuffer group = new StringBuffer("<group ");
			group.append("id=\"" + tuid + "\" ");
			group.append("ts=\"hs-split\" ");
			group.append("splitMergeIndex=\"" + System.nanoTime() + "\" ");
			group.append("xml:space=\"" + xmlSpace + "\">");
			group.append(tu1).append(tu2);
			group.append("</group>");

			String tuFragment = "";

			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				XMLModifier xm = new XMLModifier(vn);

				xm.insertBeforeElement(group.toString());
				tuFragment = vu.getElementFragment(); // 保存修改前的内容
				xm.remove();
				saveAndReparse(xm, fileName); // 保存并更新VTDNav对象

				int index = rowIds.indexOf(rowId);
				rowIds.remove(index); // 移除分割前的RowId
				rowIds.add(index, rowId + "-2"); // 添加分割后的RowId
				rowIds.add(index, rowId + "-1");

				int tuSize = tuSizeMap.get(fileName);
				tuSizeMap.put(fileName, tuSize + 1); // 翻译单元总数加1
			}
			return tuFragment;
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ModifyException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

		return "";
	}
	

	/**
	 * <div style='color:red'>此方法有复制到 convert.ui 插件的 ReverseConversionValidateWithLibrary3 类中。故若修改，注意保持同步 --robert 2012-11-29</div>
	 * 获取每个标记 header 与 tail 在文本中的 index，此方法主要针对文本段分割，分割点在g、mrk标记里面。robert 2012-11-15
	 * @param vn
	 */
	private List<Map<String, String>> getTagLocation(VTDNav vn, String srcContent) {
		List<Map<String, String>> tagLoctionList = new LinkedList<Map<String, String>>();

		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		String xpath = "./descendant::node()";

		try {
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath(xpath);
			int lastIdx = 0;
			while (ap.evalXPath() != -1) {
				Map<String, String> tagLocationMap = new HashMap<String, String>();

				String tagName = vn.toString(vn.getCurrentIndex());
				if (!("g".equals(tagName) || "mrk".equals(tagName) || "sub".equals(tagName))) {
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
			LOGGER.error("", e);
			e.printStackTrace();
		}

		vn.pop();
		return tagLoctionList;
	}

	/**
	 * 恢复分割的文本段
	 * @param rowId
	 *            唯一标识
	 * @param tuFragment
	 *            翻译单元的原始XML文本;
	 */
	public void resetSplitSegment(String rowId, String tuFragment) {
		String groupXPath = RowIdUtil.parseRowIdToGroupXPath(rowId);
		String fileName = RowIdUtil.getFileNameByRowId(rowId);
		VTDNav vn = vnMap.get(fileName);

		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath(groupXPath);
			if (ap.evalXPath() != -1) {
				XMLModifier xm = new XMLModifier(vn);
				xm.insertBeforeElement(tuFragment); // 添加 trans-unit 节点

				xm.remove(vn.getElementFragment()); // 删除 group 节点
				saveAndReparse(xm, fileName);

				int index = rowIds.indexOf(rowId + "-1");
				rowIds.add(index, rowId); // 添加分割前的RowId
				rowIds.remove(rowId + "-1");
				rowIds.remove(rowId + "-2"); // 移除分割后的RowId
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ModifyException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 合并两个文本段（注意：rowId1 和 rowId2 有可能不相邻，中间可能会间隔几个源文为空的文本段）
	 * @param rowId1
	 *            第一个文本段的 rowId
	 * @param rowId2
	 *            第二个文本段的 rowId;
	 */
	public String mergeSegment(String rowId1, String rowId2) {
		// if (rowIndex + 1 >= rowIds.size()) {
		// return "";
		// }
		// String rowId1 = getRowId(rowIndex);
		// String rowId2 = getRowId(rowIndex + 1);
		String fileName1 = RowIdUtil.getFileNameByRowId(rowId1);
		String fileName2 = RowIdUtil.getFileNameByRowId(rowId2);
		if (fileName1 == null || fileName2 == null || !fileName1.equals(fileName2)) {
			return "";
		}
		VTDNav vn = vnMap.get(fileName1);

		// TransUnitBean tuTop = getTransUnit(rowIndex);
		// TransUnitBean tuBottom = getTransUnit(rowIndex + 1);
		// String srcContent = tuTop.getSrcContent() + "<ph id=\"hs-merge" +
		// tuTop.getId() + "~" + tuBottom.getId()
		// + "\"/>" + tuBottom.getSrcContent();
		// tuTop.setSrcContent(srcContent);
		// String tgtContent = tuTop.getTgtContent() + "<ph id=\"hs-merge" +
		// tuTop.getId() + "~" + tuBottom.getId()
		// + "\"/>" + tuBottom.getTgtContent();
		// tuTop.setTgtContent(tgtContent);
		// tuTop.setMatches(null);
		//
		// String tuXPath1 = RowIdUtil.parseRowIdToXPath(rowId1);
		// String tuXPath2 = RowIdUtil.parseRowIdToXPath(rowId2);
		// AutoPilot ap = new AutoPilot(vn);
		//
		// XMLModifier xm = new XMLModifier(vn);
		// delete(ap, xm, fileName2, tuXPath2, true);
		// update(ap, xm, fileName1, tuXPath2, tuTop.toXMLString(), true);
		// ap.selectXPath(tuXPath2);
		//
		// saveAndReparse(xm, fileName1);

		String tuXPath1 = RowIdUtil.parseRowIdToXPath(rowId1);
		String tuXPath2 = RowIdUtil.parseRowIdToXPath(rowId2);
		String tuid1 = RowIdUtil.getTUIdByRowId(rowId1);
		String tuid2 = RowIdUtil.getTUIdByRowId(rowId2);
		// 不需加数据库
		boolean isNotSendToTM = false;
		// 是否是疑问行
		boolean isNeeds_review = false;
		boolean isAddNotSendToTm = false; // 是否添加属性“不需添加到数据库”
		boolean isAddNeeds_review = false; // 是否添加属性“疑问行”
		StringBuffer nodeSB = new StringBuffer();

		AutoPilot ap = new AutoPilot(vn);
		try {
			VTDUtils vu = new VTDUtils(vn);

			String oldElementFragment = "";
			ap.selectXPath(tuXPath1);
			String xmlSpace = null;
			if (ap.evalXPath() != -1) {
				xmlSpace = vu.getCurrentElementAttribut("xml:space", "preserve");
				oldElementFragment += vu.getElementFragment();
				// 获取当前tu节点的属性 hs:send-to-tm="no" hs:needs-review="yes"
				isNotSendToTM = "no".equals(vu.getCurrentElementAttribut("hs:send-to-tm", ""));
				isNeeds_review = "yes".equals(vu.getCurrentElementAttribut("hs:needs-review", ""));
			}
			ap.selectXPath(tuXPath2);
			if (ap.evalXPath() != -1) {
				oldElementFragment += vu.getElementFragment();
				if (!isNotSendToTM) {
					isAddNotSendToTm = "no".equals(vu.getCurrentElementAttribut("hs:send-to-tm", ""));
				}
				if (!isNeeds_review) {
					isAddNeeds_review = "yes".equals(vu.getCurrentElementAttribut("hs:needs-review", ""));
				}
				// 开始获取批注
				ap.selectXPath("./note");
				while (ap.evalXPath() != -1) {
					nodeSB.append(vu.getElementFragment());
				}
			}

			XMLModifier xm = new XMLModifier(vn);

			String sourceContent2 = "";
			String targetContent2 = "";

			ap.selectXPath(tuXPath2 + "/source");
			String srcLang = null;
			if (ap.evalXPath() != -1) {
				srcLang = vu.getCurrentElementAttribut("xml:lang", null);
				sourceContent2 = vu.getElementContent();
				sourceContent2 = sourceContent2 == null ? "" : sourceContent2;

			}
			ap.selectXPath(tuXPath2 + "/target");
			if (ap.evalXPath() != -1) {
				targetContent2 = vu.getElementContent();
				targetContent2 = targetContent2 == null ? "" : targetContent2;
			}
			
			String curTime = "" + System.nanoTime();
			ap.selectXPath(tuXPath1 + "/source");
			if (ap.evalXPath() != -1) {
				String sourceContent1 = vu.getElementContent();
				sourceContent1 = sourceContent1 == null ? "" : sourceContent1;

				String newValue = sourceContent1 + "<ph id=\"hs-merge" + tuid1 + "~" + tuid2 + "\" splitMergeIndex=\"" + curTime + "\"/>" + sourceContent2;
				vu.update(ap, xm, tuXPath1 + "/source/text()", newValue);
			}
			ap.selectXPath(tuXPath1 + "/target");
			if (ap.evalXPath() != -1) {
				String targetContent1 = vu.getElementContent();
				targetContent1 = targetContent1 == null ? "" : targetContent1;

				String newValue = targetContent1 + "<ph id=\"hs-merge" + tuid1 + "~" + tuid2 + "\" splitMergeIndex=\"" + curTime + "\"/>" + targetContent2;
				vu.update(ap, xm, tuXPath1 + "/target/text()", newValue);
			} else {
				String newValue = "<ph id=\"hs-merge" + tuid1 + "~" + tuid2 + "\" splitMergeIndex=\"" + curTime + "\"/>" + targetContent2;
				vu.insert(ap, xm, tuXPath1 + "/target/text()", newValue);
			}

			// 移除alt-trans节点
			ap.selectXPath(tuXPath1 + "/alt-trans");
			while (ap.evalXPath() != -1) {
				xm.remove();
			}
			ap.selectXPath(tuXPath2);
			if (ap.evalXPath() != -1) {
				// xm.remove();
				// Bug #1054：合并文本段时不应丢失第二个文本段的 trans-unit 节点
				String transUnit = "<trans-unit id=\"" + tuid2 + "\" xml:space=\"" + xmlSpace + "\"><target/>"
						+ "<source xml:lang=\"" + srcLang + "\"/></trans-unit>";
				vu.update(ap, xm, tuXPath2, transUnit);
			}

			// 整合并集中的属性以及批注
			ap.selectXPath(tuXPath1);
			if (ap.evalXPath() != -1) {
				// 注意添加属性时必须保持应有的空格
				String insertAttri = isAddNotSendToTm ? " hs:send-to-tm=\"no\" " : "";
				insertAttri += isAddNeeds_review ? " hs:needs-review=\"yes\" " : "";
				if (insertAttri.length() > 0) {
					xm.insertAttribute(insertAttri);
				}
				// 开始添加批注
				if (nodeSB.length() > 0) {
					xm.insertBeforeTail(nodeSB.toString().getBytes("UTF-8"));
				}
				vn.push();
				// 开始处理合并后的状态，保持草稿状态
				ap.selectXPath("./target");
				if (ap.evalXPath() != -1) {
					int index = vn.getAttrVal("state");
					if (index != -1) {
						xm.updateToken(index, "new");
					} else {
						xm.insertAttribute(" state=\"new\" ");
					}
				}
				vn.pop();
				// 删除批准状态
				int index = vn.getAttrVal("approved");
				if (index != -1) {
					xm.removeAttribute(index - 1);
				}
			}
			saveAndReparse(xm, fileName1);

			// 删除下面的行
			rowIds.remove(rowId2);
			tuSizeMap.put(fileName1, tuSizeMap.get(fileName1) - 1);
			return oldElementFragment;
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ModifyException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 合并文本段的撤销功能 robert 2012-11-06
	 */
	public void resetMergeSegment(Map<String, String> oldSegFragMap) {
		String xlfPath = "";
		VTDNav vn = null;
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		XMLModifier xm = new XMLModifier();

		try {
			// 由于合并的文本段都是同一个文件，因此此处不做 文件名 的判断
			for (Entry<String, String> entry : oldSegFragMap.entrySet()) {
				String rowId = entry.getKey();
				if ("".equals(xlfPath) || xlfPath == null || vn == null) {
					xlfPath = RowIdUtil.getFileNameByRowId(rowId);
					vn = vnMap.get(xlfPath);
					ap.bind(vn);
					xm.bind(vn);
				}

				ap.selectXPath(RowIdUtil.parseRowIdToXPath(rowId));
				if (ap.evalXPath() != -1) {
					xm.remove();
					xm.insertAfterElement(entry.getValue());
				}

			}
			saveAndReparse(xm, xlfPath);
			resetRowIdsToUnsorted();
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	/**
	 * 添加批注
	 * @param rowId
	 *            行的唯一标识
	 * @param note
	 *            批注内容;
	 */
	public void addNote(String rowId, String note) {
		String fileName = RowIdUtil.getFileNameByRowId(rowId);
		String xpath = RowIdUtil.parseRowIdToXPath(rowId);
		VTDNav vn = vnMap.get(fileName);
		try {
			VTDUtils vu = new VTDUtils(vn);
			XMLModifier xm = vu.insert(xpath + "/text()", "<note>" + note + "</note>");
			saveAndReparse(xm, fileName);
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 添加批注
	 * @param rowId
	 *            行的唯一标识
	 * @param note
	 *            批注内容;
	 */
	public void addNote(Map<String, List<String>> mapRowIdByFileName, String note, String from, boolean isApplyCurrent) {
		try {
			StringBuffer insertValue = new StringBuffer("<note");
			if (from != null) {
				insertValue.append(" from='").append(from).append("'");
			}
			if (!isApplyCurrent) {
				insertValue.append(" hs:apply-current='No'");
			}
			insertValue.append(">").append(StringUtilsBasic.checkNullStr(note)).append("</note>");
			VTDUtils vu = new VTDUtils();
			XMLModifier xm = new XMLModifier();
			Iterator<Entry<String, List<String>>> it = mapRowIdByFileName.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, List<String>> entry = it.next();
				String fileName = entry.getKey();
				VTDNav vn = vnMap.get(fileName);
				vu.bind(vn);
				xm.bind(vn);
				for (String rowId : entry.getValue()) {
					StringBuffer xpath = new StringBuffer(RowIdUtil.parseRowIdToXPath(rowId));
					xm = vu.insert(null, xm, xpath.append("/text()").toString(), insertValue.toString());
				}
				saveAndReparse(xm, fileName);
			}
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ModifyException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 修改批注
	 * @param rowId
	 *            行的唯一标识
	 * @param oldNote
	 *            旧的批注
	 * @param newNote
	 *            新的批注;
	 */
	public void updateNote(String rowId, String oldNote, String newNote) {
		newNote = newNote == null ? "" : newNote;
		updateAndSave(rowId, "/note[text()='" + oldNote + "']/text()", newNote);
	}

	public void updateNote(Map<String, List<String>> mapRowIdByFileName, String oldFrom, String oldText,
			String newFrom, String newText) {
		oldFrom = StringUtilsBasic.checkNullStr(oldFrom);
		oldText = StringUtilsBasic.checkNullStr(oldText);
		newFrom = StringUtilsBasic.checkNullStr(newFrom);
		newText = StringUtilsBasic.checkNullStr(newText);

		StringBuffer subXPath = new StringBuffer();
		if (!oldFrom.equals("")) {
			subXPath.append("@from='" + oldFrom + "' and ");
		}
		subXPath.insert(0, "/note[");
		subXPath.append("text()='");
		try {
			XMLModifier xm = new XMLModifier();
			VTDUtils vu = new VTDUtils();
			Iterator<Entry<String, List<String>>> it = mapRowIdByFileName.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, List<String>> entry = it.next();
				String fileName = entry.getKey();
				VTDNav vn = vnMap.get(fileName);
				xm.bind(vn);
				vu.bind(vn);
				for (String rowId : entry.getValue()) {
					StringBuffer sbTuXPath = new StringBuffer(RowIdUtil.parseRowIdToXPath(rowId));
					xm = vu.update(null, xm, sbTuXPath.append(subXPath).append(oldText).append("']/text()").toString(),
							newText);
					xm = vu.update(null, xm, sbTuXPath.append(subXPath).append(oldText).append("']/@from").toString(),
							newFrom, VTDUtils.CREATE_IF_NOT_EXIST);
				}
				saveAndReparse(xm, fileName);
			}
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ModifyException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

	}

	/**
	 * 删除批注
	 * @param rowId
	 *            行的唯一标识
	 * @param note
	 *            批注;
	 */
	public void deleteNote(String rowId, String note) {
		deleteAndSave(rowId, "/note[text()='" + note + "']");
	}

	/**
	 * 删除批注
	 * @param rowId
	 *            行的唯一标识
	 * @param note
	 *            批注;
	 */
	public void deleteNote(final HashMap<String, Vector<NoteBean>> mapNote) {
		HashMap<String, List<String>> mapRowIdAndXpath = new HashMap<String, List<String>>();
		Iterator<Entry<String, Vector<NoteBean>>> it = mapNote.entrySet().iterator();
		List<String> lstRowId = new ArrayList<String>();
		while (it.hasNext()) {
			Entry<String, Vector<NoteBean>> entry = (Entry<String, Vector<NoteBean>>) it.next();
			String rowId = entry.getKey();
			String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
			for (NoteBean bean : entry.getValue()) {
				String from = bean.getFrom();
				String applyCurrent = bean.getApplyCurrent();
				StringBuffer subXPath = new StringBuffer();
				if (from != null && !from.equals("")) {
					subXPath.append("@from='").append(from).append("' and ");
				}
				// 只有是所有句段时才加该属性
				if (applyCurrent != null && applyCurrent.equalsIgnoreCase("No")) {
					subXPath.append("@hs:apply-current='").append(applyCurrent).append("' and ");
				}
				subXPath.insert(0, "/note[");
				subXPath.append("text()='");
				if (bean.getApplyCurrent().equals("Yes")) {
					List<String> lstXpath = mapRowIdAndXpath.get(rowId);
					if (lstXpath == null) {
						lstXpath = new ArrayList<String>();
						mapRowIdAndXpath.put(rowId, lstXpath);
					}
					subXPath.insert(0, tuXPath);
					subXPath.append(bean.getNoteText()).append("']");
					lstXpath.add(subXPath.toString());
					if (!lstRowId.contains(rowId)) {
						lstRowId.add(rowId);
					}
				} else {
					// 删除所有句段的批注
					List<String> rowIds = getRowIds();
					if (!lstRowId.containsAll(rowIds)) {
						lstRowId.clear();
						lstRowId.addAll(rowIds);
					}
					for (String strRowId : rowIds) {
						StringBuffer strTuXPath = new StringBuffer(RowIdUtil.parseRowIdToXPath(strRowId));
						List<String> lstXpath = mapRowIdAndXpath.get(strRowId);
						if (lstXpath == null) {
							lstXpath = new ArrayList<String>();
							mapRowIdAndXpath.put(strRowId, lstXpath);
						}
						lstXpath.add(strTuXPath.append(subXPath).append(bean.getNoteText()).append("']").toString());
					}
				}
			}
		}

		XMLModifier xm = new XMLModifier();
		VTDUtils vu = new VTDUtils();
		Map<String, List<String>> map = RowIdUtil.groupRowIdByFileName(lstRowId);
		Iterator<Entry<String, List<String>>> iterator = map.entrySet().iterator();
		try {
			while (iterator.hasNext()) {
				Entry<String, List<String>> entry = (Entry<String, List<String>>) iterator.next();
				String fileName = entry.getKey();
				VTDNav vn = vnMap.get(fileName);
				xm.bind(vn);
				vu.bind(vn);
				List<String> rowIdList = entry.getValue();
				for (String rowId : rowIdList) {
					List<String> lstXpath = mapRowIdAndXpath.get(rowId);
					if (lstXpath != null) {
						AutoPilot ap = new AutoPilot(vu.getVTDNav());
						ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
						for (String xpath : lstXpath) {
							xm = vu.delete(ap, xm, xpath);
						}
					}
				}
				saveAndReparse(xm, fileName);
			}
		} catch (ModifyException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 删除所有批注
	 */
	public void deleteAllSegmentNote() {
		handleAllSegment(new PerFileHandler() {
			public void handle(String fileName, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws ModifyException,
					XPathParseException, XPathEvalException, NavException {
				ap.selectXPath(XPATH_ALL_TU + "/note");
				while (ap.evalXPath() != -1) {
					xm.remove();
				}
				saveAndReparse(xm, fileName); // 保存并更新VTDNav对象
			}
		});
	}

	/**
	 * 删除应用范围为当前文本段的批注
	 * @param rowIdList
	 *            行的唯一标识集合;
	 */
	public void deleteEditableSegmentNote(List<String> rowIds) {
		handleSomeSegment(rowIds, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException {
				String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
				if (vu.pilot(ap, tuXPath) != -1) {
					ap.selectXPath("./note[not(@hs:apply-current)]");
					while (ap.evalXPath() != -1) {
						xm.remove();
					}
				}
			}
		});
	}

	/**
	 * 得到文本段的索引
	 * @param currentRowIndex
	 * @param relativeXPath
	 * @param isNext
	 *            是否是找下一个。<br/>
	 *            <code>true</code>: 下一个；<code>false</code>: 上一个。
	 * @return 文本段的索引;
	 */
	private int getSegmentIndex(int currentRowIndex, String relativeXPath, boolean isNext) {
		int step = isNext ? 1 : -1;
		currentRowIndex += step;
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		while (currentRowIndex >= 0 && currentRowIndex < rowIds.size()) {
			String rowId = getRowId(currentRowIndex);
			VTDNav vn = getVTDNavByRowId(rowId);
			ap.bind(vn);
			String xpath = RowIdUtil.parseRowIdToXPath(rowId);
			try {
				ap.selectXPath(xpath + relativeXPath);
				if (ap.evalXPath() != -1) {
					return currentRowIndex;
				}
			} catch (XPathEvalException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (NavException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (XPathParseException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
			currentRowIndex += step;
		}
		return -1;
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
	public String getContext(VTDUtils vu, AutoPilot ap, int num, boolean isPre) {
		vu.getVTDNav().push();
		StringBuilder re = new StringBuilder();
		try {
			String xpath = isPre ? "preceding" : "following";
			xpath = xpath + "::trans-unit/source";
			ap.selectXPath(xpath);
			int i = 0;
			while (ap.evalXPath() != -1 && i < num) {
				String pureText = getTUPureText(vu.getVTDNav());
				if (pureText != null) {
					re.append("," + pureText.trim().hashCode());
				}
				i++;
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
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
	 * 得到下一带批注的文本段的索引
	 * @param currentRowIndex
	 *            当前的行索引
	 * @return 下一带批注的文本段的索引;
	 */
	public int getNextNoteSegmentIndex(int currentRowIndex) {
		return getSegmentIndex(currentRowIndex, /* "following::trans-unit[note]" */
				"[note]", true);
	}

	/**
	 * 得到上一带批注的文本段的索引
	 * @param currentRowIndex
	 *            当前的行索引
	 * @return 上一带批注的文本段的索引;
	 */
	public int getPreviousNoteSegmentIndex(int currentRowIndex) {
		return getSegmentIndex(currentRowIndex, /* "preceding::trans-unit[note]" */
				"[note]", false);
	}

	/**
	 * 得到下一带疑问的文本段的索引
	 * @param currentRowIndex
	 *            当前的行索引
	 * @return 下一带疑问的文本段的索引;
	 */
	public int getNextQuestionSegmentIndex(int currentRowIndex) {
		return getSegmentIndex(currentRowIndex, /* "following::trans-unit[@hs:needs-review='yes']" */
				"[@hs:needs-review='yes']", true);
	}

	/**
	 * 得到下一未批准文本段的索引
	 * @param currentRowIndex
	 *            当前的行索引
	 * @return 下一未批准文本段的索引;
	 */
	public int getNextUnapprovedSegmentIndex(int currentRowIndex) {
		return getSegmentIndex(currentRowIndex, /* "following::trans-unit[not(@approved='yes')]" */
				"[not(@approved='yes')]", true);
	}

	/**
	 * 得到上一未批准文本段的索引
	 * @param currentRowIndex
	 *            当前的行索引
	 * @return 上一未批准文本段的索引;
	 */
	public int getPreviousUnapprovedSegmentIndex(int currentRowIndex) {
		return getSegmentIndex(currentRowIndex, /* "preceding::trans-unit[not(@approved='yes')]" */
				"[not(@approved='yes')]", false);
	}

	/**
	 * 得到下一未翻译文本段的索引
	 * @param currentRowIndex
	 *            当前的行索引
	 * @return 下一未翻译文本段的索引;
	 */
	public int getNextUntranslatedSegmentIndex(int currentRowIndex) {
		return getSegmentIndex(currentRowIndex, /* "following::trans-unit[not(target/text()!='')]" */
				"[not(target/text()!='' or target/*)]", true);
	}

	/**
	 * 得到上一未翻译文本段的索引
	 * @param currentRowIndex
	 *            当前的行索引
	 * @return 上一未翻译文本段的索引;
	 */
	public int getPreviousUntranslatedSegmentIndex(int currentRowIndex) {
		return getSegmentIndex(currentRowIndex, /* "preceding::trans-unit[not(target/text()!='')]" */
				"[not(target/text()!='')]", false);
	}

	/**
	 * 得到下一不可翻译文本段的索引
	 * @param currentRowIndex
	 *            当前的行索引
	 * @return 下一不可翻译文本段的索引;
	 */
	public int getNextUntranslatableSegmentIndex(int currentRowIndex) {
		return getSegmentIndex(currentRowIndex, /* "following::trans-unit[@translate='no']" */
				"[@translate='no']", true);
	}

	/**
	 * 得到上一不可翻译文本段的索引
	 * @param currentRowIndex
	 *            当前的行索引
	 * @return 上一不可翻译文本段的索引;
	 */
	public int getPreviousUntranslatableSegmentIndex(int currentRowIndex) {
		return getSegmentIndex(currentRowIndex, /* "preceding::trans-unit[@translate='no']" */
				"[@translate='no']", false);
	}

	/**
	 * 得到下一模糊匹配文本段的索引
	 * @param currentRowIndex
	 *            当前的行索引
	 * @return 下一模糊匹配文本段的索引;
	 */
	public int getNextFuzzySegmentIndex(int currentRowIndex) {
		return getSegmentIndex(currentRowIndex, /*
												 * "following::trans-unit[" +
												 * "not(alt-trans/@match-quality='100' or alt-trans/@match-quality='100%' or "
												 * +
												 * "alt-trans/@match-quality='101' or alt-trans/@match-quality='101%')]"
												 */
//				"[not(alt-trans/@match-quality='100' or alt-trans/@match-quality='100%' or " + "alt-trans/@match-quality='101' or alt-trans/@match-quality='101%')]", true);
				// 修改，完成翻译并跳转到下一模糊匹配，这里要取界面显示的匹配，而否　alt-trans 中的匹配率。robert	2013-04-19
				"[not(target/@hs:quality='100' or target/@hs:quality='100%' or target/@hs:quality='101' or target/@hs:quality='101%')]", true);
	}

	/**
	 * 得到上一模糊匹配文本段的索引
	 * @param currentRowIndex
	 *            当前的行索引
	 * @return 上一模糊匹配文本段的索引;
	 */
	public int getPreviousFuzzySegmentIndex(int currentRowIndex) {
		return getSegmentIndex(currentRowIndex, /*
												 * "preceding::trans-unit[" +
												 * "not(alt-trans/@match-quality='100' or alt-trans/@match-quality='100%' or "
												 * +
												 * "alt-trans/@match-quality='101' or alt-trans/@match-quality='101%')]"
												 */
				"[not(alt-trans/@match-quality='100' or alt-trans/@match-quality='100%' or "
						+ "alt-trans/@match-quality='101' or alt-trans/@match-quality='101%')]", false);
	}

	/**
	 * 得到所有“重复文本段” robert 2012-09-21
	 * @param langFilterCondition
	 *            语言过滤条件 ;
	 */
	public ArrayList<String> getRepeatedSegment(String langFilterCondition) {
		try {
			// return SaxonSearcher.getRepeatedSegment(xliffXmlnsMap, langFilterCondition);
			return RepeatRowSearcher.getRepeateRowsForFilter(this, xliffXmlnsMap);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(Messages.getString("file.XLFHandler.logger10"), e);
			return null;
		}
	}

	/**
	 * 过滤所有“重复文本段”
	 * @param langFilterCondition
	 *            语言过滤条件 ;
	 */
	public void filterRepeatedSegment(String langFilterCondition) {
		ArrayList<String> list = getRepeatedSegment(langFilterCondition);
		if (list != null) {
			resetCache();
			rowIds.clear();
			rowIds = list;
		}
	}

	/**
	 * 得到所有“重复文本段”，除去第一条重复文本段。用于“锁定文本段时，保留第一条不锁定”等情况。 robert 2012-09-21
	 * @param langFilterCondition
	 *            语言过滤条件
	 * @return ;
	 */
	public ArrayList<String> getRepeatedSegmentExceptFirstOne(String srcLan, String tgtLan) {
		ArrayList<String> rowIds = RepeatRowSearcher.getRepeateRowsForLockInterRepeat(this, xliffXmlnsMap, srcLan,
				tgtLan);
		// String srcContentCache = null;
		// for (int i = 0; i < rowIds.size();) {
		// String srcContent = this.getSrcContent(rowIds.get(i));
		// srcContent = srcContent == null ? "" : srcContent;
		// if (!srcContent.equals(srcContentCache)) {
		// rowIds.remove(i);
		// srcContentCache = srcContent;
		// } else {
		// i++;
		// }
		// }
		return rowIds;
	}

	/**
	 * 得到所有“译文不一致文本段”
	 * @param langFilterCondition
	 *            ;
	 */
	public void getInconsistentTranslationsSegment(String langFilterCondition) {
		resetCache();
		rowIds.clear();
		try {
			rowIds = SaxonSearcher.getInconsistentTranslationsSegment(xliffXmlnsMap, langFilterCondition);
		} catch (XQException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 处理所有的文本段
	 * @param handler
	 *            单个文件的处理实现 ;
	 */
	private void handleAllSegment(PerFileHandler handler) {
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		XMLModifier xm = new XMLModifier();
		VTDUtils vu = new VTDUtils();
		VTDNav vn;
		for (Entry<String, VTDNav> entry : vnMap.entrySet()) {
			String fileName = entry.getKey();
			vn = entry.getValue();
			vn.push();
			try {
				ap.bind(vn);
				xm.bind(vn);
				vu.bind(vn);

				handler.handle(fileName, vu, ap, xm); // 针对每个文件的VTDNav对象进行操作
			} catch (ModifyException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (XPathParseException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (XPathEvalException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (NavException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} finally {
				vn.pop();
			}
		}
		ap = null;
		xm = null;
		vu = null;
	}

	/**
	 * 单个文件处理接口
	 * @author weachy
	 * @version
	 * @since JDK1.5
	 */
	interface PerFileHandler {

		/**
		 * 针对单个文件进行处理
		 * @param fileName
		 *            文件名
		 * @param vu
		 *            VTDUtils对象
		 * @param ap
		 *            AutoPilot对象
		 * @param xm
		 *            XMLModifier对象
		 * @throws ModifyException
		 * @throws XPathParseException
		 * @throws XPathEvalException
		 * @throws NavException
		 * @throws UnsupportedEncodingException
		 *             ;
		 */
		void handle(String fileName, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws ModifyException,
				XPathParseException, XPathEvalException, NavException, UnsupportedEncodingException;
	}

	/**
	 * 处理多个文本段
	 * @param rowIds
	 *            行的唯一标识
	 * @param handler
	 *            单个文本段的处理实现 ;
	 */
	private Map<String, Object> handleSomeSegment(List<String> rowIds, PerSegmentHandler handler) {
		if (rowIds == null || rowIds.isEmpty()) {
			return getSuccessResult();
		}
		VTDNav vn = null;
		VTDUtils vu = new VTDUtils();
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		XMLModifier xm = new XMLModifier();
		Map<String, List<String>> map = RowIdUtil.groupRowIdByFileName(rowIds);
		String errorMsg = null;
		Throwable error = null;
		for (Entry<String, List<String>> entry : map.entrySet()) {
			String fileName = entry.getKey();
			List<String> rowIdList = entry.getValue();
			vn = vnMap.get(fileName);
			vn.push();
			try {
				vu.bind(vn);
				ap.bind(vn);
				xm.bind(vn);
				for (String rowId : rowIdList) {
					handler.handle(rowId, vu, ap, xm);
				}
				// 保存并更新VTDNav对象
				saveAndReparse(xm, fileName);
				// return getSuccessResult(); //robert注释
			} catch (XPathParseException e) {
				errorMsg = Messages.getString("file.XLFHandler.logger11");
				error = e;
				LOGGER.error(errorMsg, e);
			} catch (XPathEvalException e) {
				errorMsg = Messages.getString("file.XLFHandler.logger12");
				error = e;
				LOGGER.error(errorMsg, e);
			} catch (NavException e) {
				errorMsg = Messages.getString("file.XLFHandler.logger12");
				error = e;
				LOGGER.error(errorMsg, e);
			} catch (ModifyException e) {
				errorMsg = Messages.getString("file.XLFHandler.logger13");
				error = e;
				LOGGER.error(errorMsg, e);
			} catch (UnsupportedEncodingException e) {
				errorMsg = Messages.getString("file.XLFHandler.logger14");
				error = e;
				LOGGER.error(errorMsg, e);
			} finally {
				vn.pop();
			}
		}
		vu = null;
		ap = null;
		xm = null;
		if (errorMsg != null) {
			return getErrorResult(Messages.getString("file.XLFHandler.msg3"), error);
		} else {
			return getSuccessResult();
		}
	}

	/**
	 * 单个文本段处理接口
	 * @author weachy
	 * @version
	 * @since JDK1.5
	 */
	interface PerSegmentHandler {

		/**
		 * 针对单个文本段进行处理
		 * @param rowId
		 *            文本段的唯一标识
		 * @param vu
		 *            VTDUtils 对象
		 * @param ap
		 *            AutoPilot对象
		 * @param xm
		 *            XMLModifier对象
		 * @throws XPathParseException
		 * @throws XPathEvalException
		 * @throws NavException
		 * @throws ModifyException
		 * @throws UnsupportedEncodingException
		 *             ;
		 */
		void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
				XPathEvalException, NavException, ModifyException, UnsupportedEncodingException;
	}

	/**
	 * 更新alt-trans节点,删除指定类型的alt-trans（以toolid区分类型），重新写入新的alt-trans 内容
	 * @param rowId
	 *            当前标识
	 * @param newAltTrans
	 *            新的alt-trans内容
	 * @param oldAltTransToolId
	 *            旧的alt-trans toolId,会删除此toolid的所有alt-trans;
	 */
	public void updateAltTrans(String rowId, List<AltTransBean> newAltTrans, List<String> oldAltTransToolId) {
		String fileName = RowIdUtil.getFileNameByRowId(rowId);

		VTDUtils vu;
		XMLModifier xm = null;
		try {
			vu = new VTDUtils(vnMap.get(fileName));
			xm = new XMLModifier(vu.getVTDNav());
			updateAltTrans(vu, xm, rowId, newAltTrans, oldAltTransToolId);
		} catch (NavException e) {
			LOGGER.error("", e);
		} catch (ModifyException e) {
			LOGGER.error("", e);
		}
		saveAndReparse(xm, fileName);

	}

	private void updateAltTrans(VTDUtils vu, XMLModifier xm, String rowId, List<AltTransBean> newAltTrans,
			List<String> oldAltTransToolId) {
		String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		try {
			ap.selectXPath(tuXPath);
			if (ap.evalXPath() != -1) {
				StringBuffer bf = new StringBuffer();
				for (String toolId : oldAltTransToolId) {
					bf.append("@tool-id='").append(toolId).append("' | ");
				}
				if (bf.length() > 0) {
					String toolId = bf.substring(0, bf.lastIndexOf("|"));
					String deleteXpath = "./alt-trans[" + toolId + "]";
					AutoPilot _ap = new AutoPilot(vu.getVTDNav());
					_ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
					_ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
					vu.delete(_ap, xm, deleteXpath, VTDUtils.PILOT_TO_END);
				}

				StringBuffer xmlBf = new StringBuffer();
				for (AltTransBean altTran : newAltTrans) {
					xmlBf.append(altTran.toXMLString());
				}
				if (xmlBf.length() > 0) {
					xm.insertBeforeTail(xmlBf.toString());
				}
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
		} catch (ModifyException e) {
			LOGGER.error("", e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("", e);
		} catch (NavException e) {
			LOGGER.error("", e);
		}
	}

	public void batchUpdateAltTrans(int[] rowIndexs, Map<Integer, List<AltTransBean>> newAltTransList,
			Map<Integer, List<String>> oldAltTransToolIdList) {
		String preFileName = "";
		XMLModifier xm = null;
		VTDUtils vu = new VTDUtils();
		for (int rowIndex : rowIndexs) {
			String rowId = getRowId(rowIndex);
			String fileName = RowIdUtil.getFileNameByRowId(rowId);
			try {
				if (preFileName.equals("")) {
					preFileName = fileName;
					vu.bind(vnMap.get(fileName));
					xm = new XMLModifier(vu.getVTDNav());
					preFileName = fileName;
				} else if (!preFileName.equals(fileName)) {
					saveAndReparse(xm, fileName);
					vu.bind(vnMap.get(fileName));
					xm = new XMLModifier(vu.getVTDNav());
					preFileName = fileName;
				}
				updateAltTrans(vu, xm, rowId, newAltTransList.get(rowIndex), oldAltTransToolIdList.get(rowIndex));
			} catch (NavException e) {
				LOGGER.error("", e);
			} catch (ModifyException e) {
				LOGGER.error("", e);
			}
		}
		saveAndReparse(xm, preFileName);
	}

	/**
	 * 删除匹配
	 * @param rowIds
	 *            行的唯一标识集合;
	 */
	public void deleteAltTrans(List<String> rowIds) {
		handleSomeSegment(rowIds, new PerSegmentHandler() {
			public void handle(String rowId, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws XPathParseException,
					XPathEvalException, NavException, ModifyException {
				String tuXPath = RowIdUtil.parseRowIdToXPath(rowId); // 根据RowId得到定位到该翻译单元的XPath
				// vu.delete(ap, xm, tuXPath + "/alt-trans", VTDUtils.PILOT_TO_END);
				if (vu.pilot(ap, tuXPath) == -1) {
					return;
				}
				ap.selectXPath("./alt-trans");
				while (ap.evalXPath() != -1) {
					xm.remove();
				}
			}
		});
	}

	/**
	 * 删除匹配
	 * @param xpath
	 *            alter-trans节点中的条件Xpath
	 */
	public void deleteAltTrans(String xpath) {
		Map<String, List<String>> map = RowIdUtil.groupRowIdByFileName(rowIds);
		for (Entry<String, List<String>> entry : map.entrySet()) {
			try {
				VTDUtils vu = new VTDUtils(vnMap.get(entry.getKey()));
				xpath = XPATH_ALL_TU + "/alt-trans[" + xpath + "]";
				XMLModifier xm = vu.delete(xpath, VTDUtils.PILOT_TO_END);
				saveAndReparse(xm, entry.getKey());
			} catch (NavException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 通过rowId和xpath删除Alt-Trans,xpath用于指定删除匹配的类型,通过too-id属性确定
	 * @param rowId
	 * @param xpath
	 *            ;
	 */
	public void deleteAltTrans(String rowId, String xpath) {
		String fileName = RowIdUtil.getFileNameByRowId(rowId);
		VTDNav vn = vnMap.get(fileName);
		VTDUtils vu;
		try {
			vu = new VTDUtils(vn);
			String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
			tuXPath = tuXPath + "/alt-trans[" + xpath + "]";
			XMLModifier xm = vu.delete(tuXPath, VTDUtils.PILOT_TO_END);
			saveAndReparse(xm, fileName);
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * @unused 删除所有文本段翻译 ;
	 */
	public void deleteAllSegmentTranslations() {
		handleAllSegment(new PerFileHandler() {
			public void handle(String fileName, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws ModifyException,
					XPathParseException, XPathEvalException, NavException {
				ap.selectXPath(XPATH_ALL_TU + "/target");
				while (ap.evalXPath() != -1) {
					xm.remove(vu.getVTDNav().getContentFragment());
				}
				saveAndReparse(xm, fileName);
				changeTgtPropValue(rowIds, "state", "new");
			}
		});
	}

	/**
	 * 获取翻译匹配率达到 <code>minMatchQuality</code> 的匹配
	 * @param minMatchQuality
	 *            最低匹配率
	 */
	public Map<String, String> getMatchOfSegments(final int minMatchQuality) {
		final HashMap<String, String> map = new HashMap<String, String>();
		handleAllSegment(new PerFileHandler() {
			public void handle(String fileName, VTDUtils vu, AutoPilot ap, XMLModifier xm) throws ModifyException,
					XPathParseException, XPathEvalException, NavException, UnsupportedEncodingException {
				ap.selectXPath("/xliff/file/body//trans-unit[translate(alt-trans/@match-quality, '%', '')>="
						+ minMatchQuality + "]");
				AutoPilot tempAp = new AutoPilot(vu.getVTDNav());
				while (ap.evalXPath() != -1) {
					String rowId = RowIdUtil.getRowId(vu.getVTDNav(), fileName);
					if (isApproved(rowId) || isLocked(rowId)) { // 已经批准或者锁定的，跳过。
						continue;
					}
					String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
					if (vu.pilot(tempAp, tuXPath) != -1) {
						String tgt = vu.getValue(tempAp, "./alt-trans[translate(@match-quality, '%', '')>="
								+ minMatchQuality + "]/target/text()");
						if (tgt != null) {
							String currentTgt = vu.getValue(tempAp, "./target/text()"); // 当前 Target 的值
							if (!tgt.equals(currentTgt)) {
								map.put(rowId, tgt);
							}
						}
					}
				}
				saveAndReparse(xm, fileName);
			}
		});
		return map;
	}

	/**
	 * 得到翻译单元的属性值
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
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			return vu.getValue(ap, tuXPath + "/@" + propName);
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 文本段是否已经批准
	 * @param rowId
	 *            行的唯一标识
	 * @return ;
	 */
	public boolean isApproved(String rowId) {
		String approved = this.getTuProp(rowId, "approved");
		return (approved != null && "yes".equalsIgnoreCase(approved));
	}

	/**
	 * 是否已经签发
	 * @param rowId
	 * @return
	 */
	public boolean isSignOff(String rowId) {
		String isSignOff = getNodeAttribute(RowIdUtil.getFileNameByRowId(rowId), RowIdUtil.parseRowIdToXPath(rowId)
				+ "/target", "state");
		return (isSignOff != null && "signed-off".equals(isSignOff));
	}

	/**
	 * 文本段是否已经锁定
	 * @param rowId
	 *            行的唯一标识
	 * @return ;
	 */
	public boolean isLocked(String rowId) {
		String translate = this.getTuProp(rowId, "translate");
		return (translate != null && "no".equalsIgnoreCase(translate));
	}

	public boolean isSendToTM(String rowId) {
		String sendToTm = this.getTuProp(rowId, "hs:send-to-tm");
		return (sendToTm != null && "no".equals(sendToTm));
	}

	/**
	 * 是否是疑问行对应
	 * @param rowId
	 * @return ;
	 */
	public boolean isNeedReview(String rowId) {
		String needReview = this.getTuProp(rowId, "hs:needs-review");
		return (needReview != null && "yes".equals(needReview));
	}

	public boolean isSignedOff(String rowId) {
		String singed = this.getTgtPropValue(rowId, "state");
		return (singed != null && "signed-off".equals(singed));
	}

	/**
	 * 是否是草稿状态
	 * @param rowId
	 * @return ;
	 */
	public boolean isDraft(String rowId) {
		String state = this.getTgtPropValue(rowId, "state");
		return state != null && "new".equals(state);
	}

	public boolean isEmptyTranslation(String rowId) {
		String state = this.getTgtPropValue(rowId, "state");
		return state == null;
	}

	/**
	 * 得到 TU 子节点的 XML 内容
	 * @param rowIds
	 *            行唯一标识的集合
	 * @return key：rowId，value：TU 子节点的 XML 内容;
	 */
	public Map<String, String> getTuNodes(List<String> rowIds) {
		if (rowIds == null || rowIds.size() == 0) {
			return null;
		}
		HashMap<String, String> nodes = new HashMap<String, String>();
		// AutoPilot ap = new AutoPilot();
		// VTDUtils vu = new VTDUtils();
		for (String rowId : rowIds) {
			nodes.put(rowId, getTUFragByRowId(rowId));
			// VTDNav vn = getVTDNavByRowId(rowId);
			// ap.bind(vn);
			// try {
			// vu.bind(vn);
			// String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
			// getTUFragByRowId(rowId)
			// String node = vu.getValue(ap, tuXPath);
			// nodes.put(rowId, node);
			// } catch (NavException e) {
			// LOGGER.error("", e);
			// e.printStackTrace();
			// }
		}
		return nodes;
	}

	/**
	 * 重置 TU 子节点的 XML 内容。
	 * @param subNodesOfTU
	 *            TU 子节点的 XML 内容
	 */
	public void resetTuNodes(Map<String, String> subNodesOfTU) {
		if (subNodesOfTU == null || subNodesOfTU.size() == 0) {
			return;
		}
		List<String> rowIdList = new ArrayList<String>(subNodesOfTU.keySet());
		Map<String, List<String>> map = RowIdUtil.groupRowIdByFileName(rowIdList);
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		XMLModifier xm = new XMLModifier();
		VTDUtils vu = new VTDUtils();
		for (Entry<String, List<String>> entry : map.entrySet()) {
			String fileName = entry.getKey();
			List<String> rowIds = entry.getValue();
			VTDNav vn = vnMap.get(fileName);
			try {
				ap.bind(vn);
				xm.bind(vn);
				vu.bind(vn);
				for (String rowId : rowIds) {
					String tgtNode = subNodesOfTU.get(rowId);
					String xpath = RowIdUtil.parseRowIdToXPath(rowId);
					xm = vu.update(ap, xm, xpath, tgtNode);
				}
				saveAndReparse(xm, fileName); // 保存并更新 VTDNav
			} catch (ModifyException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (NavException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据同一个 XLIFF 文件中的 rowId 生成 TMX 文件
	 * @param list
	 *            rowId
	 * @param creationTool
	 *            创建工具
	 * @param creationToolVersion
	 *            创建工具版本
	 * @param srcName
	 *            源语言
	 * @param monitor
	 *            进度条
	 * @return TMX 文件
	 */
	public StringBuffer getTMXFileContent(List<String> list, String creationTool, String creationToolVersion,
			String srcName, IProgressMonitor monitor) {
		if (list == null || list.size() == 0) {
			return new StringBuffer();
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(Messages.getString("file.XLFHandler.task3"), list.size());
		if (monitor.isCanceled())
			return new StringBuffer();
		StringBuffer re = new StringBuffer();
		re.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		re.append("<tmx version=\"1.4b\">");
		re.append("<header creationtool=\"" + creationTool + "\" creationtoolversion=\"" + creationToolVersion
				+ "\" srclang=\"" + srcName
				+ "\" adminlang=\"en\" datatype=\"xml\" o-tmf=\"XLIFF\" segtype=\"paragraph\">");
		re.append("</header>");
		re.append("<body>");
		VTDNav vn = getVTDNavByRowId(list.get(0));
		AutoPilot ap = new AutoPilot(vn);
		VTDUtils vu = null;
		try {
			vu = new VTDUtils(vn);
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			monitor.done();
		}

		String defaultSrcLang = "";
		String defaultTgtLang = "";
		String fileXPath = RowIdUtil.getFileXpathByRowId(list.get(0));
		if (vu.pilot(ap, fileXPath) != -1) {
			defaultSrcLang = vu.getValue(ap, "./@source-language");
			defaultTgtLang = vu.getValue(ap, "./@target-language");
		}
		if (defaultTgtLang == null) {
			defaultTgtLang = "";
		}

		for (String rowId : list) {
			if (monitor.isCanceled())
				return new StringBuffer();
			String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
			try {
				ap.selectXPath(tuXPath);
				ap.evalXPath();
			} catch (XPathParseException e) {
				LOGGER.error("", e);
				e.printStackTrace();
				monitor.done();
			} catch (XPathEvalException e) {
				LOGGER.error("", e);
				e.printStackTrace();
				monitor.done();
			} catch (NavException e) {
				LOGGER.error("", e);
				e.printStackTrace();
				monitor.done();
			}
			re.append("<tu ");
			// 添加tu的属性
			boolean hasTuId = false;
			String id = null;
			String attrNameXp = "./@*[name]";
			String attrValueXp = "./@";
			List<String> tuPropNames = vu.getValues(ap, attrNameXp);
			for (String name : tuPropNames) {
				String value = vu.getValue(ap, attrValueXp + name);
				re.append(name + "=\"" + value + "\" ");
				if (name.equals("id")) {
					id = value;
				}
				if (!hasTuId) {
					if (name.equals("tuid")) {
						hasTuId = true;
					}
				}
			}

			if (!hasTuId) {
				re.append("tuid=\"" + genTuId(rowId, id) + "\"");
			}
			re.append(">");

			String typeXp = "./prop-group/prop/@prop-type";
			List<String> typeValues = vu.getValues(ap, typeXp);
			if (typeValues != null) {
				for (String typeValue : typeValues) {
					re.append("<prop type=\"" + typeValue + "\">");
					re.append(vu.getValue(ap, "./prop-group/prop[@prop-type='" + typeValue + "']/text()"));
					re.append("</prop>");
				}
			}

			// HS的自定义属性组
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			String hsPropXp = "./hs:prop-group/prop/@prop-type";
			typeValues = vu.getValues(ap, hsPropXp);
			if (typeValues != null) {
				for (String typeValue : typeValues) {
					re.append("<prop type=\"" + typeValue + "\">");
					re.append(vu.getValue(ap, "./hs:prop-group/prop[@prop-type='" + typeValue + "']/text()"));
					re.append("</prop>");
				}
			}

			// 添加tuv(source)
			re.append("<tuv ");
			// 添加tuv(source)属性
			attrNameXp = "./source/@*[name]";
			attrValueXp = "./source/@";
			List<String> sourcePropNames = vu.getValues(ap, attrNameXp);
			if (sourcePropNames != null) {
				for (String name : sourcePropNames) {
					String value = vu.getValue(ap, attrValueXp + name);
					re.append(name + "=\"" + value + "\" ");
				}
			}

			/*
			 * source节点可能没有xml:lang属性，如果没有，则要去file节点去取source-language(Required)属性的值
			 */
			if (sourcePropNames == null || !sourcePropNames.contains("xml:lang")) {
				re.append("xml:lang=\"" + defaultSrcLang + "\"");
			}

			re.append(">");

			// 添加上下文prop
			re.append("<prop type=\"x-preContext\">");
			re.append(getContext(vu, ap, contextNum, true));
			re.append("</prop>");

			re.append("<prop type=\"x-nextContext\">");
			re.append(getContext(vu, ap, contextNum, false));
			re.append("</prop>");

			String sourceTextXp = "./source/text()";
			re.append("<seg>").append(vu.getValue(ap, sourceTextXp)).append("</seg>");
			re.append("</tuv>");

			// 添加tuv(target)
			re.append("<tuv ");
			// 添加tuv(target)属性
			attrNameXp = "./target/@*[name]";
			attrValueXp = "./target/@";
			List<String> targetPropNames = vu.getValues(ap, attrNameXp);
			if (targetPropNames != null) {
				for (String name : targetPropNames) {
					String value = vu.getValue(ap, attrValueXp + name);
					re.append(name + "=\"" + value + "\" ");
				}
			}
			/*
			 * target节点可能没有xml:lang属性，如果没有，则要去file节点去取target-language(Optional)属性的值
			 */
			if (targetPropNames == null || !targetPropNames.contains("xml:lang")) {
				re.append("xml:lang=\"" + defaultTgtLang + "\"");
			}

			re.append(">");
			String targetTextXp = "./target/text()";
			re.append("<seg>").append(vu.getValue(ap, targetTextXp)).append("</seg>");
			re.append("</tuv>");
			re.append("</tu>");
			monitor.worked(1);
		}
		re.append("</body>");
		re.append("</tmx>");
		monitor.done();
		return re;
	}

	/**
	 * 根据同一个 XLIFF 文件中的 rowId 生成 TMX 文件, 对于不添加到库中的文本段将被忽略
	 * @param list
	 *            rowId
	 * @param srcLang
	 *            源语言
	 * @param monitor
	 *            进度条
	 * @return TMX 文件
	 */
	public StringBuffer generateTMXFileContent(String systemUser, List<String> list, String srcLang, String tgtLang,
			IProgressMonitor monitor, int contextSize, IProject project) {
		if (list == null || list.size() == 0) {
			return null;
		}
		// long start = System.currentTimeMillis();
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.setTaskName(Messages.getString("file.XLFHandler.task4"));
		monitor.beginTask(Messages.getString("file.XLFHandler.task4"), list.size());
		if (monitor.isCanceled()) {
			monitor.setTaskName(Messages.getString("file.XLFHandler.task5"));
			throw new OperationCanceledException();
		}
		StringBuffer re = new StringBuffer();
		re.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		re.append("<tmx version=\"1.4b\">");
		re.append("<header creationtool=\"" + Constants.TMX_CREATIONTOOL + "\" creationtoolversion=\""
				+ Constants.TMX_CREATIONTOOLVERSION + "\" srclang=\"" + srcLang
				+ "\" adminlang=\"en\" datatype=\"xml\" o-tmf=\"XLIFF\" segtype=\"paragraph\">");
		re.append("</header>");
		re.append("<body>");
		VTDNav vn = getVTDNavByRowId(list.get(0)).duplicateNav();
		AutoPilot ap = new AutoPilot(vn);
		VTDUtils vu = null;
		try {
			vu = new VTDUtils(vn);
		} catch (NavException e) {
			LOGGER.error(Messages.getString("file.XLFHandler.logger15"), e);
		}

		String strChangeBy = systemUser;
		String strProp = getProjectProp(project);
		boolean isNull = true;
		AutoPilot ap2 = new AutoPilot(vn);
		ap2.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		for (String rowId : list) {
			if (monitor.isCanceled()) {
				re = null;
				monitor.setTaskName(Messages.getString("file.XLFHandler.task5"));
				throw new OperationCanceledException();
			}
			String tgtText = getTgtContent(rowId);
			String srcText = getSrcContent(rowId);
			if (srcText == null || srcText.trim().equals("") || tgtText == null || tgtText.trim().equals("")) {
				monitor.worked(1);
				continue;
			}
			if (isSendToTM(rowId)) {
				monitor.worked(1);
				continue;
			}
			String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
			try {
				ap.selectXPath(tuXPath);
				ap.evalXPath();
			} catch (XPathParseException e) {
				LOGGER.error(Messages.getString("file.XLFHandler.logger16"), e);
				monitor.done();
			} catch (XPathEvalException e) {
				LOGGER.error(Messages.getString("file.XLFHandler.logger16"), e);
				monitor.done();
			} catch (NavException e) {
				LOGGER.error(Messages.getString("file.XLFHandler.logger16"), e);
				monitor.done();
			}
			re.append("<tu ");
			// 添加tu的属性
			String id = vu.getValue("./@id");
			String creationDate = CommonFunction.retTMXDate();
			String changeDate = creationDate;
			re.append("tuid=\"" + genTuId(rowId, id) + "\" ");
			re.append("changedate=\"" + changeDate + "\" changeid=\"" + strChangeBy + "\" ");
			re.append("creationdate=\"" + creationDate + "\" creationid=\"" + strChangeBy + "\" >\n");

			try {
				// Fixed Bug #2290 入库时未添加批注信息 by Jason
				Vector<NoteBean> notes = getNotes(rowId);
				if (notes != null) {
					for (NoteBean bean : notes) {
						String lang = bean.getLang();
						if (lang != null && !lang.equals("")) {
							re.append("<note xml:lang='" + lang + "'>" + bean.getNoteText() + "</note>");
						} else {
							re.append("<note>" + bean.getNoteText() + "</note>");
						}
					}
				}
			} catch (NavException e1) {
				LOGGER.error("", e1);
				e1.printStackTrace();
			} catch (XPathParseException e1) {
				LOGGER.error("", e1);
				e1.printStackTrace();
			} catch (XPathEvalException e1) {
				LOGGER.error("", e1);
				e1.printStackTrace();
			}

			// 添加上下文prop
			re.append("<prop type=\"x-preContext\">");
			vn.push();
			try {
				ap2.selectXPath(tuXPath);
			} catch (XPathParseException e) {
				LOGGER.error("XPathParseException", e);
				e.printStackTrace();
			}
			re.append(getContext(vu, ap2, contextSize, true));
			re.append("</prop>");
			vn.pop();

			vn.push();
			re.append("<prop type=\"x-nextContext\">");
			ap2.resetXPath();
			re.append(getContext(vu, ap2, contextSize, false));
			re.append("</prop>");
			vn.pop();

			// 添加自定义属性
			re.append(strProp);

			re.append("<tuv xml:lang=\"" + srcLang + "\">\n");
			// String sourceTextXp = "./source/text()";
			// re.append("<seg>").append(vu.getValue(ap, sourceTextXp)).append("</seg>");
			re.append("<seg>").append(srcText.trim()).append("</seg>\n");
			re.append("</tuv>\n");

			// 添加tuv(target)
			re.append("<tuv xml:lang=\"" + tgtLang + "\">\n");
			// String targetTextXp = "./target/text()";
			// re.append("<seg>").append(vu.getValue(ap, targetTextXp)).append("</seg>");
			re.append("<seg>").append(tgtText.trim()).append("</seg>\n");
			re.append("</tuv>\n");
			re.append("</tu>\n");
			monitor.worked(1);
			isNull = false;
		}
		re.append("</body>");
		re.append("</tmx>");
		monitor.done();
		if (isNull) {
			return null;
		} else {
			return re;
		}
	}

	/**
	 * 更新记忆库
	 * @param file
	 *            xliff 文件
	 * @param srcLang
	 *            源语言，当添加所有语言时，值为 null
	 * @param tgtLang
	 *            目标语言，当添加所有语言时，值为 null
	 * @param isAddApprove
	 *            是否添加已批准文本段到记忆库
	 * @param isAddSignedOff
	 *            是否添加已签发文本段到记忆库
	 * @param isAddTranslate
	 *            是否添加已翻译文本段到记忆库
	 * @param isAddDraft
	 *            是否添加草稿状态文本段到记忆库
	 * @param isTagAddTM
	 *            是否将标记为不添加到记忆库的文本段添加到记忆库
	 * @param isAddNoState
	 *            是否添加无状态文本段到记忆库(未实现)
	 * @return 每个数组中第一个元素表示源语言，第二个元素为生成的 TMX 字符串
	 */
	public String[] generateTMXToUpdateTM(IFile file, boolean isAddApprove, boolean isAddSignedOff,
			boolean isAddTranslate, boolean isAddDraft, boolean isAddLocked, int contextSize, String systemUser) {
		// List<String[]> lstTMX = new ArrayList<String[]>();

		if (file == null) {
			return null;
		}
		String filePath = ResourceUtils.iFileToOSPath(file);
		VTDNav vn = vnMap.get(filePath);

		// 当文件未打开时，要先解析 xliff 文件
		if (vn == null) {
			VTDGen vg = new VTDGen();
			String path = ResourceUtils.iFileToOSPath(file);
			if (vg.parseFile(path, true)) {
				vn = vg.getNav();
			}
		}

		StringBuffer sbTMX = new StringBuffer();
		String srcLang = null;
		// if (srcLang == null || tgtLang == null) {
		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		String srcLanguage;
		String tgtLanguage;
		String original;
		String strTMX;
		try {
			VTDUtils vu = new VTDUtils(vn);
			vn.push();
			ap.selectXPath("/xliff//file");

			AutoPilot ap2 = new AutoPilot(vn);
			ap2.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			// sbTMX 是否添加了头部信息
			boolean isContainHeader = false;
			while (ap.evalXPath() != -1) {
				vn.push();
				srcLanguage = vu.getCurrentElementAttribut("source-language", null);
				srcLang = srcLanguage;
				tgtLanguage = vu.getCurrentElementAttribut("target-language", null);
				original = vu.getCurrentElementAttribut("original", null);

				if (srcLanguage == null) {
					continue;
				}
				if (tgtLanguage == null) {
					continue;
				}
				vn.pop();

				strTMX = generateTMXString(systemUser, filePath, srcLanguage, tgtLanguage, original, vn, isAddApprove,
						isAddSignedOff, isAddTranslate, isAddDraft, isAddLocked, contextSize, file.getProject());
				if (strTMX != null) {
					if (!isContainHeader) {
						isContainHeader = true;
						sbTMX.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
						sbTMX.append("<tmx version=\"1.4b\">");
						sbTMX.append("<header creationtool=\"" + Constants.TMX_CREATIONTOOL
								+ "\" creationtoolversion=\"" + Constants.TMX_CREATIONTOOLVERSION + "\" srclang=\""
								+ srcLanguage
								+ "\" adminlang=\"en\" datatype=\"xml\" o-tmf=\"XLIFF\" segtype=\"paragraph\">");
						sbTMX.append("</header>");
						sbTMX.append("<body>");
					}
					sbTMX.append(strTMX);
				}
			}
			if (sbTMX.length() > 0) {
				sbTMX.append("</body>");
				sbTMX.append("</tmx>");
			} else {
				return null;
			}
		} catch (XPathParseException e) {
			LOGGER.error(MessageFormat.format(Messages.getString("file.XLFHandler.logger17"), filePath), e);
		} catch (XPathEvalException e) {
			LOGGER.error(MessageFormat.format(Messages.getString("file.XLFHandler.logger17"), filePath), e);
		} catch (NavException e) {
			LOGGER.error(MessageFormat.format(Messages.getString("file.XLFHandler.logger17"), filePath), e);
		} finally {
			vn.pop();
		}

		// } else {
		// AutoPilot ap = new AutoPilot(vn);
		// VTDUtils vu = null;
		//
		// String original = "";
		// String strProjectRef = "";
		// String strJobRef = "";
		// String strJobDate = "";
		// String strJobOwner = "";
		// String strClient = "";
		// String projectXPath = "/xliff/file[@source-language='" + srcLang + "' and @target-language='" + tgtLang
		// + "']/header/hs:prop-group[@name='project']/hs:prop";
		//
		// try {
		// vn.push();
		// vu = new VTDUtils(vn);
		// ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		// ap.selectXPath(projectXPath);
		// if (ap.evalXPath() != -1) {
		// String type = vu.getValue("./@prop-type");
		// if (type.equals("projectref")) {
		// strProjectRef = vu.getElementContent();
		// } else if (type.equals("jobref")) {
		// strJobRef = vu.getElementContent();
		// } else if (type.equals("jobdate")) {
		// strJobDate = vu.getElementContent();
		// } else if (type.equals("jobowner")) {
		// strJobOwner = vu.getElementContent();
		// } else if (type.equals("client")) {
		// strClient = vu.getElementContent();
		// }
		// }
		// vn.pop();
		// vn.push();
		// ap.resetXPath();
		// ap.selectXPath("/xliff/file[@source-language='" + srcLang + "' and @target-language='" + tgtLang + "']");
		// if (ap.evalXPath() != -1) {
		// original = vu.getCurrentElementAttribut("original", null);
		// }
		// vn.pop();
		//
		// String strTMX = generateTMXString(filePath, srcLang, tgtLang, original, vn, isAddApprove,
		// isAddSignedOff, isAddTranslate, isAddDraft, contextSize, strProjectRef, strJobRef, strJobDate,
		// strJobOwner, strClient);
		// if (strTMX != null) {
		// lstTMX.add(new String[] { srcLang, strTMX });
		// }
		// } catch (XPathEvalException e) {
		// LOGGER.error("解析 XLIFF 文件 " + filePath + " 时出现错误：XPathEvalException。", e);
		// } catch (NavException e) {
		// LOGGER.error("解析 XLIFF 文件 " + filePath + " 时出现错误：NavException。", e);
		// } catch (XPathParseException e) {
		// LOGGER.error("解析 XLIFF 文件 " + filePath + " 时出现错误：XPathParseException。", e);
		// }
		// }

		return new String[] { srcLang, sbTMX.toString() };
	}

	public String generateTMXString(String systemUser, String filePath, String srcLang, String tgtLang,
			String original, VTDNav vn, boolean isAddApprove, boolean isAddSignedOff, boolean isAddTranslate,
			boolean isAddDraft, boolean isAddLocked, int contextSize, IProject project) {
		String strChangeBy = systemUser;
		StringBuffer re = new StringBuffer();

		String xpath = "/xliff/file[@original='" + original + "' and @source-language='" + srcLang
				+ "' and @target-language='" + tgtLang + "']/body//trans-unit[source/text()!='' or source/*]";
		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		AutoPilot ap2 = new AutoPilot(vn);
		ap2.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		VTDUtils vu = null;
		boolean isNull = true;
		boolean blnLocked = false;
		boolean blnDraft = false;
		boolean blnTranslate = false;
		boolean blnApproved = false;
		boolean blnSignedOff = false;
		String srcText;
		String tgtText;
		String sendToTM;
		String locked;
		String state;
		String approved;
		String id;
		String creationDate;
		String changeDate;
		// 添加自定义属性
		String strProp = getProjectProp(project);

		try {
			ap.selectXPath(xpath);
			vu = new VTDUtils(vn);
			vn.push();
			while (ap.evalXPath() != -1) {
				srcText = vu.getValue("./source/text()");
				tgtText = vu.getValue("./target/text()");
				// 源文或目标文本段为空（包括空格），不添加到记忆库
				if (srcText == null || srcText.trim().equals("") || tgtText == null || tgtText.trim().equals("")) {
					continue;
				}
				// 标记为“不添加到记忆库”状态的文本段不添加到记忆库（默认为 yes，表示添加到记忆库）。
				sendToTM = vu.getCurrentElementAttribut("hs:send-to-tm", "yes");
				if (sendToTM.equalsIgnoreCase("no")) {
					continue;
				}

				// 锁定状态
				locked = vu.getCurrentElementAttribut("translate", "yes");
				blnLocked = false;
				if (locked.equalsIgnoreCase("no")) {
					blnLocked = isAddLocked;
				}
				blnDraft = false;
				blnTranslate = false;
				blnApproved = false;
				blnSignedOff = false;

				if (locked.equalsIgnoreCase("yes")) {
					state = vu.getValue("./target/@state");
					approved = vu.getCurrentElementAttribut("approved", "no");
					// 是否添加草稿到记忆库
					if (state != null && state.equalsIgnoreCase("new") && approved.equalsIgnoreCase("no")) {
						blnDraft = isAddDraft;
					}

					// 是否添加完成翻译文本段到记忆库
					if (approved.equalsIgnoreCase("no") && state != null && state.equalsIgnoreCase("translated")) {
						blnTranslate = isAddTranslate;
					}

					// 是否添加已批准文本段到记忆库
					if (approved.equalsIgnoreCase("yes") && (state == null || !state.equalsIgnoreCase("signed-off"))) {
						blnApproved = isAddApprove;
					}

					// 是否添加已签发文本段到记忆库
					if (state != null && state.equalsIgnoreCase("signed-off")) {
						blnSignedOff = isAddSignedOff;
					}
				}

				if (!(blnLocked || blnDraft || blnTranslate || blnApproved || blnSignedOff)) {
					continue;
				}
				id = vu.getCurrentElementAttribut("id", null);
				re.append("<tu ");
				// 添加tu的属性
				creationDate = CommonFunction.retTMXDate();
				changeDate = creationDate;
				re.append("tuid=\"" + (original.hashCode() + "-" + id.hashCode()) + "\" ");
				re.append("changedate=\"" + changeDate + "\" changeid=\"" + strChangeBy + "\" ");
				re.append("creationdate=\"" + creationDate + "\" creationid=\"" + strChangeBy + "\" >\n");

				// 添加上下文prop
				re.append("<prop type=\"x-preContext\">");
				ap2.selectXPath("/xliff/file[@original='" + original + "' and @source-language='" + srcLang
						+ "' and @target-language='" + tgtLang + "']/body//trans-unit[@id='" + id + "']");
				re.append(getContext(vu, ap2, contextSize, true));
				re.append("</prop>\n");

				re.append("<prop type=\"x-nextContext\">");
				re.append(getContext(vu, ap2, contextSize, false));
				re.append("</prop>\n");
				
				re.append(strProp);

				re.append("<tuv xml:lang=\"" + srcLang + "\" >\n");
				re.append("<seg>").append(srcText.trim()).append("</seg>\n");
				re.append("</tuv>\n");

				// 添加tuv(target)
				re.append("<tuv xml:lang=\"" + tgtLang + "\">\n");
				re.append("<seg>").append(tgtText.trim()).append("</seg>\n");
				re.append("</tuv>\n");
				re.append("</tu>\n");
				isNull = false;
			}
		} catch (XPathEvalException e) {
			LOGGER.error(MessageFormat.format(Messages.getString("file.XLFHandler.logger17"), filePath), e);
		} catch (NavException e) {
			LOGGER.error(MessageFormat.format(Messages.getString("file.XLFHandler.logger17"), filePath), e);
		} catch (XPathParseException e) {
			LOGGER.error(MessageFormat.format(Messages.getString("file.XLFHandler.logger17"), filePath), e);
		} finally {
			vn.pop();
		}

		if (isNull) {
			return null;
		} else {
			return re.toString();
		}
	}
	
	/**
	 * 获取 IProject 自定义属性信息
	 * @param project
	 * @return ;
	 */
	public String getProjectProp(IProject project) {
		StringBuffer sbProp = new StringBuffer();
		ProjectInfoBean projectBean = ProjectConfigerFactory.getProjectConfiger(project).getCurrentProjectConfig();
		Map<String, String> mapField = projectBean.getMapField();
		if (mapField != null && mapField.size() > 0) {
			Iterator<Entry<String, String>> it = mapField.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = (Entry<String, String>) it.next();
				String key = entry.getKey();
				String value = entry.getValue();
				if (key != null && !key.trim().equals("") && value != null && !value.trim().equals("")) {
					sbProp.append("<prop type=\"" + key + "\">" + value + "</prop>\n");
				}
			}
		}
		Map<String, Object[]> mapAttr = projectBean.getMapAttr();
		if (mapAttr != null && mapAttr.size() > 0) {
			Iterator<Entry<String, Object[]>> it = mapAttr.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object[]> entry = (Entry<String, Object[]>) it.next();
				String key = entry.getKey();
				String strSel = (String) entry.getValue()[0];
				if (key != null && !key.trim().equals("") && strSel != null && !strSel.equals("")) {
					sbProp.append("<prop type=\"" + entry.getKey() + "\">" + strSel + "</prop>\n");
				}
			}
		}
		return sbProp.toString();
	}

	/**
	 * 根据选择的术语，语言等生成 TBX 格式的字符串
	 * @param srcLang
	 *            源语言
	 * @param tgtLang
	 *            目标语言
	 * @param srcTerm
	 *            源术语
	 * @param tgtTerm
	 *            目标术语
	 * @return 生成的 TBX 格式的字符串
	 */
	public String generateTBXWithString(String srcLang, String tgtLang, String srcTerm, String tgtTerm, String property) {
		if (StringUtilsBasic.checkNullStr(srcLang).equals("") || StringUtilsBasic.checkNullStr(tgtLang).equals("")
				|| StringUtilsBasic.checkNullStr(srcTerm).equals("")
				|| StringUtilsBasic.checkNullStr(tgtTerm).equals("")) {
			return null;
		}
		StringBuffer bf = new StringBuffer();
		bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bf.append("<martif type=\"TBX\" xml:lang=\"" + srcLang + "\">\n");
		bf.append("<martifHeader>\n");
		bf.append("<fileDesc>" + Constants.TBX_ADD_TERM_FILEDESC + "</fileDesc>\n");
		bf.append("<encodingDesc>\n");
		bf.append("<p type=\"DCSName\">" + Constants.TBX_XCS_DEFAULT + "</p>\n");
		bf.append("</encodingDesc>\n");
		bf.append("</martifHeader>\n");
		bf.append("<text>\n");
		bf.append("<body>\n");

		bf.append("<termEntry id=\"_" + System.currentTimeMillis() + "\">\n");
		bf.append("<langSet id=\"_" + (System.currentTimeMillis() + 1) + "\" xml:lang=\"" + srcLang + "\">\n");
		bf.append("<tig>\n");
		bf.append("<term>" + srcTerm.trim() + "</term>\n");
		bf.append("</tig>\n");
		bf.append("</langSet>\n");
		bf.append("<langSet id=\"_" + (System.currentTimeMillis() + 2) + "\" xml:lang=\"" + tgtLang + "\">\n");
		bf.append("<tig>\n");
		bf.append("<term>" + tgtTerm.trim() + "</term>\n");
		bf.append("</tig>\n");
		bf.append("</langSet>\n");
		if (property != null && !property.equals("")) {
			bf.append("<note id=\"" + srcLang + "," + tgtLang + "\">" + property + "</note>\n");
		}
		bf.append("</termEntry>\n");

		bf.append("</body>\n");
		bf.append("</text>\n");
		bf.append("</martif>\n");
		return bf.toString();
	}

	/**
	 * 获取tuid，规则是原文件名（带路径）的hashcode+“－”+当前TransUnitBean的id的hashcode
	 */
	private String genTuId(String rowId, String id) {
		String original = RowIdUtil.getFileNameByRowId(rowId);
		return original.hashCode() + "-" + id.hashCode();
	}

	/**
	 * 检查同一个 XLIFF 文件中指定的目标文本是否都为空
	 * @param rowIds
	 * @return true 全部为空 false 不全部为空
	 */
	public void removeNullTgtContentRowId(List<String> rowIds) {
		if (rowIds == null || rowIds.size() == 0) {
			return;
		}
		for (Iterator<String> it = rowIds.iterator(); it.hasNext();) {
			String tmp = getTgtContent(it.next());
			if (tmp == null || tmp.equals("")) {
				it.remove();
			}
		}
	}

	/**
	 * 检查提定集合中 rowId 所对应的行是否是锁定状态，如果是，则删除。--robert 2012-07-24
	 */
	public void removeLockedRowIds(List<String> rowIdList) {
		if (rowIdList == null || rowIdList.size() == 0) {
			return;
		}
		for (Iterator<String> it = rowIdList.iterator(); it.hasNext();) {
			String rowId = it.next();
			String locked = getNodeAttribute(RowIdUtil.getFileNameByRowId(rowId), RowIdUtil.parseRowIdToXPath(rowId),
					"translate");
			if ("no".equals(locked)) {
				it.remove();
			}
		}
	}

	/**
	 * 获取首选项中保存的自定义删除匹配条件，条件为alt-trans节点后的xpath
	 * @return 首选项中保存的自定义删除匹配条件
	 */
	public static LinkedHashMap<String, String> getCustomMatchFilterMap() {
		LinkedHashMap<String, String> map = PreferenceStore.getMap(IPreferenceConstants.MATCH_CONDITION);
		if (map == null) {
			map = new LinkedHashMap<String, String>();
		}
		return map;
	}

	/**
	 * 获取首选项中保存的自定义删除匹配条件额外信息 目前额外信息包含：满足所有条件、满足任一条件、标记的匹配、快速翻译匹配、自动繁殖翻译匹配、从来源中删除 这些按钮的选择情况。
	 * @return 首选项中保存的自定义删除匹配条件额外信息
	 */
	public static LinkedHashMap<String, ArrayList<String[]>> getCustomMatchFilterAdditionMap() {
		LinkedHashMap<String, ArrayList<String[]>> map = PreferenceStore
				.getCustomCondition(IPreferenceConstants.MATCH_CONDITION_ADDITION);
		if (map == null) {
			map = new LinkedHashMap<String, ArrayList<String[]>>();
		}
		return map;
	}

	/**
	 * 获取首选中的自定义删除匹配条件的索引和数据信息（用于刷新界面）
	 * @return 首选中的自定义删除匹配条件的索引和数据信息
	 */
	public static LinkedHashMap<String, ArrayList<String[]>> getCustomMatchFilterIndexMap() {
		LinkedHashMap<String, ArrayList<String[]>> map = PreferenceStore
				.getCustomCondition(IPreferenceConstants.MATCH_CONDITION_INDEX);
		if (map == null) {
			map = new LinkedHashMap<String, ArrayList<String[]>>();
		}
		return map;
	}

	/**
	 * 检查所有已读取文件的file节点是否有target-language属性
	 * @return key:filename，value: map（key：file节点的original属性
	 * @throws XPathParseException
	 * @throws NavException
	 * @throws XPathEvalException
	 */
	private Map<String, List<XliffBean>> getXliffInfo(boolean checkTgtLang) {
		Map<String, List<XliffBean>> fileNameXliffBeansMap = new HashMap<String, List<XliffBean>>();
		VTDUtils vu = new VTDUtils();
		AutoPilot subAp = new AutoPilot();
		for (Entry<String, VTDNav> entry : vnMap.entrySet()) {
			String fileName = entry.getKey();
			VTDNav vn = entry.getValue();
			try {
				vu.bind(vn);
				subAp.bind(vn);
				subAp.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
				subAp.selectXPath("./header/hs:prop-group[@name=\"document\"]/hs:prop[@prop-type=\"sourcefile\"]");

				AutoPilot ap = new AutoPilot(vn);
				String xpath;
				if (checkTgtLang) {
					xpath = "/xliff/file[not(@target-language)]";
				} else {
					xpath = "/xliff/file";
				}
				ap.selectXPath(xpath); // 不存在 target-language 属性。
				while (ap.evalXPath() != -1) {
					vn.push();

					List<XliffBean> xliffBeans = fileNameXliffBeansMap.get(fileName);
					if (xliffBeans == null) {
						xliffBeans = new ArrayList<XliffBean>();
						fileNameXliffBeansMap.put(fileName, xliffBeans);
					}

					String sourceLanguage = vu.getCurrentElementAttribut("source-language", "");
					String targetLanguage = vu.getCurrentElementAttribut("target-language", "");
					String original = vu.getCurrentElementAttribut("original", ""); // 如果是

					String sourceFile; // 源文件名
					String datatype = vu.getCurrentElementAttribut("datatype", null);
					if (datatype != null && (datatype.contains("openoffice") || datatype.contains("msoffice2007"))) {
						subAp.resetXPath(); // 重置 AutoPilot 对象的储存的信息。
						if (subAp.evalXPath() != -1) {
							sourceFile = vu.getElementContent();
						} else {
							sourceFile = original;
						}
					} else {
						sourceFile = original;
					}

					XliffBean xliff = null;
					for (XliffBean bean : xliffBeans) {
						if (bean.getSourceFile().equals(sourceFile)) {
							xliff = bean;
						}
					}
					if (xliff == null) {
						xliff = new XliffBean(datatype, sourceFile, sourceLanguage, targetLanguage, original, fileName);
						xliffBeans.add(xliff);
					} else {
						xliff.addOriginal(original);
					}

					vn.pop();
				}
			} catch (NavException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (XPathParseException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			} catch (XPathEvalException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
		}
		return fileNameXliffBeansMap;
	}

	/**
	 * 得到所有已读取文件中 不含 target-language 属性的 file 节点
	 * @return key:filename，value: map（key：file节点的original属性
	 * @throws XPathParseException
	 * @throws NavException
	 * @throws XPathEvalException
	 */
	public Map<String, List<XliffBean>> checkTargetLanguage() {
		return getXliffInfo(true);
	}

	/**
	 * 得到已读取的 XLIFF 文件信息
	 * @return key:filename，value: map（key：file节点的original属性
	 * @throws XPathParseException
	 * @throws NavException
	 * @throws XPathEvalException
	 */
	public Map<String, List<XliffBean>> getXliffInfo() {
		return getXliffInfo(false);
	}

	/**
	 * 设置指定 XLIFF 文件中指定 file 节点的语言代码信息（source-language、target-language 属性值）
	 * @param fileName
	 *            指定文件名
	 * @param XliffBeans
	 *            XliffBean 集合;
	 */
	public void updateLanguages(String fileName, List<XliffBean> xliffBeans) {
		if (xliffBeans == null || xliffBeans.isEmpty()) {
			return;
		}
		VTDNav vn = vnMap.get(fileName);
		AutoPilot ap = new AutoPilot(vn);
		AutoPilot subAp = new AutoPilot(vn);
		try {
			VTDUtils vu = new VTDUtils(vn);
			XMLModifier xm = new XMLModifier(vn);
			for (XliffBean bean : xliffBeans) {
				Set<String> originals = bean.getOriginals();
				for (String original : originals) {
					int index = vu.pilot(subAp, "/xliff/file[@original='" + original + "']");
					if (index != -1) {
						xm = vu.update(ap, xm, "./@source-language", bean.getSourceLanguage(),
								VTDUtils.CREATE_IF_NOT_EXIST);
						xm = vu.update(ap, xm, "./@target-language", bean.getTargetLanguage(),
								VTDUtils.CREATE_IF_NOT_EXIST);
					}
				}
			}
			saveAndReparse(xm, fileName);
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ModifyException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 得到 File 节点个数（此方法仅用于解析单个文件时）
	 * @return ;
	 */
	public int getFileCountInXliff(String fileName) {
		VTDNav vn = vnMap.get(fileName);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + fileName);
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("count(/xliff/file)");
			int countAllFile = (int) ap.evalXPathToNumber(); // 整个 xliff 文件中的
																// file 节点的个数
			return countAllFile;
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

		return -1;
	}

	/**
	 * 重置 ;
	 */
	public void reset() {
		this.accessHistory.clear();
		this.cacheMap.clear();
		this.tuSizeMap.clear();
		this.vnMap.clear();
		this.xliffXmlnsMap.clear();
		this.vnRead = null;
	}

	/**
	 * 检验多个 file 节点是否存在 document 属性组，以及该属性组下是否存在 original 属性（用于转换 XLIFF 的源文件为 OpenOffice 和 MSOffice2007 的情况）
	 * @param fileName
	 * @return ;
	 */
	public boolean validateMultiFileNodes(String fileName) {
		VTDNav vn = vnMap.get(fileName);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + fileName);
		try {
			AutoPilot subAp = new AutoPilot(vn);
			subAp.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			subAp.selectXPath("./header/hs:prop-group[@name=\"document\"]/hs:prop[@prop-type=\"original\"]");

			VTDUtils vu = new VTDUtils(vn);

			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/xliff/file");
			while (ap.evalXPath() != -1) {
				vn.push();
				subAp.resetXPath();
				if (subAp.evalXPath() != -1) {
					String documentOriginal = vu.getElementContent();
					if (documentOriginal == null || documentOriginal.equals("")) {
						return false;
					}
				} else {
					return false;
				}
				vn.pop();
			}
			return true;
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 存为纯文本
	 * @param fileName
	 * @param out
	 * @param elementName
	 * @return ;
	 */
	public boolean saveAsText(String fileName, String out, String elementName) {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(out));
			VTDNav vn = vnMap.get(fileName);
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath("/xliff/file");
			while (ap.evalXPath() != -1) {
				AutoPilot subAp = new AutoPilot(vn);
				subAp.selectXPath("descendant::trans-unit[source/text()!='']");
				while (subAp.evalXPath() != -1) {
					if ("target".equals(elementName)) {
						String approvedValue = vu.getValue("./@approved");
						approvedValue = approvedValue == null ? "no" : approvedValue;
						if (!"yes".equalsIgnoreCase(approvedValue)) { // 未批准
							continue;
						}
					}
					String value = vu.getValue("./" + elementName + "/text()");
					if (value != null) {
						bos.write((value + "\n").getBytes());
					}
				}
			}
			return true;
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			return false;
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			return false;
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			return false;
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			return false;
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					LOGGER.error("", e);
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 存为HTML
	 * @param fileName
	 * @param out
	 * @param elementName
	 * @return ;
	 */
	public boolean saveAsHtml(String fileName, String out, String elementName) {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(out));
			bos.write("<html>\n".getBytes()); //$NON-NLS-1$
			bos.write("  <head>\n".getBytes()); //$NON-NLS-1$
			bos.write("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n".getBytes()); //$NON-NLS-1$
			bos.write(("    <title>" + fileName + "</title>\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
			bos.write("  </head>\n".getBytes()); //$NON-NLS-1$
			bos.write("<body>\n".getBytes()); //$NON-NLS-1$

			VTDNav vn = vnMap.get(fileName);
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath("/xliff/file");
			while (ap.evalXPath() != -1) {
				AutoPilot subAp = new AutoPilot(vn);
				subAp.selectXPath("descendant::trans-unit[source/text()!='']");
				while (subAp.evalXPath() != -1) {
					if ("target".equals(elementName)) {
						String approvedValue = vu.getValue("./@approved");
						approvedValue = approvedValue == null ? "no" : approvedValue;
						if (!"yes".equalsIgnoreCase(approvedValue)) { // 未批准
							continue;
						}
					}
					String value = vu.getValue("./" + elementName + "/text()");
					if (value != null) {
						bos.write(("<p>" + value + "</p>\n").getBytes());
					}
				}
			}
			bos.write("</body>\n</html>\n".getBytes());
			return true;
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			return false;
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			return false;
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			return false;
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			return false;
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					LOGGER.error("", e);
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 验证该文件是否符合xliff标准,主要是查看其根元素是否是xliff
	 * @param xlfPath
	 *            要验证的Xliff文件路径
	 * @return robert 2011-10-19
	 */
	public boolean validateSplitXlf(String xlfPath) {
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			ap.selectXPath("/*");
			while (ap.evalXPath() != -1) {
				if ("xliff".equals(vn.toString(vn.getCurrentIndex()))) {
					return true;
				}
			}
			return false;
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * 验证该文件是否是分割后的文件 robert 2012-06-09
	 * @param xlfPath
	 * @return
	 */
	public boolean validateSplitedXlf(String xlfPath) {
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace("hs", hsR7NSUrl);
		try {
			String xpath = "/xliff/file/header/hs:splitInfos";
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				return false;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * 获取该xliff文件的最终分割信息(针对已经分割的文件)
	 * @param xlfPath
	 * @return robert 2011-10-20
	 */
	public Map<String, String> getOldSplitInfo(String xlfPath) {
		Map<String, String> oldSplitInfo = new HashMap<String, String>();
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);

		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			ap.selectXPath("/xliff/file/header/hs:splitInfos/hs:splitInfo[last()]"); // --robert split
			// ap.selectXPath("/xliff/file/header/splitInfos/splitInfo[last()]");

			while (ap.evalXPath() != -1) {
				VTDUtils vu = new VTDUtils(vn);
				oldSplitInfo = vu.getCurrentElementAttributs(hsNSPrefix, hsR7NSUrl);
			}

		} catch (Exception e) {
			LOGGER.error(Messages.getString("file.XLFHandler.logger18"), e);
			// TODO: handle exception
		}

		return oldSplitInfo;

	}

	/**
	 * 获取指定节点的头
	 * @param xlfPath
	 * @param nodeName
	 *            节点名称
	 * @param nodeXPath
	 *            节点路径
	 * @return robert 2011-10-20
	 */
	public String getNodeHeader(String xlfPath, String nodeName, String nodeXPath) {
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);

		String xliffNodeHeader = "";
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath(nodeXPath);
			VTDUtils vu = new VTDUtils(vn);
			while (ap.evalXPath() != -1) {
				xliffNodeHeader = vu.getElementHead();

			}
			xliffNodeHeader += "\n</" + nodeName + ">";

		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

		return xliffNodeHeader;
	}

	/**
	 * 获取整个节点，包括其头部，其子节点，其文本
	 * @param xlfPath
	 * @param nodeXPath
	 *            节点的xpath
	 * @return robert 2011-10-21
	 */
	public String getNodeFrag(String xlfPath, String nodeXPath) {
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);

		String xliffNodeContent = "";
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath(nodeXPath);
			VTDUtils vu = new VTDUtils(vn);
			if (ap.evalXPath() != -1) {
				xliffNodeContent = vu.getElementFragment();

			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return xliffNodeContent;
	}

	/**
	 * 获取给定rowId的完整内容，包括头与尾
	 * @param rowId
	 * @return ;
	 */
	public String getTUFragByRowId(String rowId) {
		String nodeStr = null;
		String xlfPath = RowIdUtil.getFileNameByRowId(rowId);
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);

		String xpath = RowIdUtil.parseRowIdToXPath(rowId);
		AutoPilot ap = new AutoPilot(vn);
		try {
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				nodeStr = vu.getElementFragment();
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return nodeStr;
	}

	/**
	 * 获取xliff文件的file节点的信息,保存到LinkedHashMap中， 第一个值为file节点的序列，从1开始，第二个值为该file节点下trans-unit的个数
	 * @param xlfPath
	 * @return robert 2011-10-21
	 */
	public Map<Integer, Integer> getFileInfo(String xlfPath) {
		Map<Integer, Integer> fileInfo = new LinkedHashMap<Integer, Integer>();
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		int fileIndex = 1; // 备注，这是从1开始。
		int transUnitNum = 0;
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/xliff/file");

			while (ap.evalXPath() != -1) {
				AutoPilot ap2 = new AutoPilot(vn);
				ap2.selectXPath("count(./body/trans-unit)");
				transUnitNum = (int) ap2.evalXPathToNumber();
				fileInfo.put(fileIndex, transUnitNum);
				fileIndex++;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

		return fileInfo;
	}

	/**
	 * 获取所有的trans-unit的节点总数
	 * @param xlfPath
	 * @return robert 2011-10-21
	 */
	public int getAllTransUnitNum(String xlfPath) {
		int transUnitNum = 0;
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("count(/xliff/file/body//trans-unit[source/text()!='' or source/*])");
			transUnitNum = (int) ap.evalXPathToNumber();

		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

		return transUnitNum;

	}

	/**
	 * 将数据添加到切割后新生成的文件中，并且是添加到指定节点的尾部
	 * @param newXlfPath
	 * @param data
	 *            要添加的内容
	 * @param toXpath
	 *            要添加的位置 robert 2011-10-21
	 */
	public void addDataToXlf(String newXlfPath, String data, String toXpath) {
		VTDNav vn = vnMap.get(newXlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + newXlfPath);
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath(toXpath);
			if (ap.evalXPath() != -1) {
				XMLModifier xm = new XMLModifier(vn);
				xm.insertBeforeTail((data + "\n").getBytes("UTF8"));
				// VTDUtils vUtils = new VTDUtils(vn);
				// 更新新生成的xliff文件，并重新加载并更新VTDVNav
				// vnMap.put(newXlfPath, vUtils.updateVTDNav(xm, newXlfPath));
				saveAndReparse(xm, newXlfPath);
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 向新切割的xliff文件添加分割信息
	 * @param newXlfPath
	 * @param infoMap
	 *            robert 2011-10-23
	 */
	public void addNewInfoToSplitXlf(String newXlfPath, Map<String, String> infoMap) {
		// --robert split

		VTDNav vn = vnMap.get(newXlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + newXlfPath);
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			ap.selectXPath("/xliff/file/header/hs:splitInfos");
			boolean hasSplitInfos = false;
			XMLModifier xm = new XMLModifier(vn);
			VTDUtils vUtils = new VTDUtils(vn);
			String splitInfo = "\n    <hs:splitInfo count=\"" + infoMap.get("count") + "\" " + " depth=\""
					+ infoMap.get("depth") + "\" id=\"" + infoMap.get("id") + "\" " + "index=\"" + infoMap.get("index")
					+ "\" name=\"" + infoMap.get("name") + "\" splitTime=\"" + infoMap.get("splitTime") + "\"/>";

			while (ap.evalXPath() != -1) {
				xm.insertBeforeTail(splitInfo + "\n");
				hasSplitInfos = true;
			}
			if (hasSplitInfos) {
				vnMap.put(newXlfPath, vUtils.updateVTDNav(xm, newXlfPath));
			}

			// 如果是第一次分割，那么添加相关信息
			if (!hasSplitInfos) {
				String firstSplitInfo = "\n  <hs:splitInfos original=\"" + infoMap.get("original") + "\">" + splitInfo
						+ "</hs:splitInfos>";
				AutoPilot ap1 = new AutoPilot(vn);
				ap1.selectXPath("/xliff/file/header");
				while (ap1.evalXPath() != -1) {
					xm.insertBeforeTail(firstSplitInfo + "\n");
				}
				vnMap.put(newXlfPath, vUtils.updateVTDNav(xm, newXlfPath));
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

		/*
		 * VTDNav vn = vnMap.get(newXlfPath); Assert.isNotNull(vn, "未在已解析的缓存中找到该文件：" + newXlfPath); try { AutoPilot ap =
		 * new AutoPilot(vn); ap.selectXPath("/xliff/file/header/splitInfos"); boolean hasSplitInfos = false;
		 * XMLModifier xm = new XMLModifier(vn); VTDUtils vUtils = new VTDUtils(vn); String splitInfo =
		 * "\n    <splitInfo count=\"" + infoMap.get("count") + "\" " + " depth=\"" + infoMap.get("depth") + "\" id=\""
		 * + infoMap.get("id") + "\" " + "index=\"" + infoMap.get("index") + "\" name=\"" + infoMap.get("name") +
		 * "\" splitTime=\"" + infoMap.get("splitTime") + "\"/>";
		 * 
		 * while (ap.evalXPath() != -1) { xm.insertBeforeTail(splitInfo + "\n"); hasSplitInfos = true; } if
		 * (hasSplitInfos) { vnMap.put(newXlfPath, vUtils.updateVTDNav(xm, newXlfPath)); }
		 * 
		 * // 如果是第一次分割，那么添加相关信息 if (!hasSplitInfos) { String firstSplitInfo = "\n  <splitInfos>" + splitInfo +
		 * "</splitInfos>";
		 * 
		 * AutoPilot ap1 = new AutoPilot(vn); ap1.selectXPath("/xliff/file/header"); while (ap1.evalXPath() != -1) {
		 * xm.insertBeforeTail(firstSplitInfo + "\n"); } vnMap.put(newXlfPath, vUtils.updateVTDNav(xm, newXlfPath)); } }
		 * catch (Exception e) { e.printStackTrace(); }
		 */
	}

	/**
	 * 获取指定文件的指定节点的内容(除节点头与尾之外)
	 * @param xlfPath
	 * @param nodeXpath
	 * @return robert 2011-10-26
	 */
	public String getNodeContent(String xlfPath, String nodeXpath) {
		String nodeContent = "";
		VTDNav vn = vnMap.get(xlfPath);
		vn.push(); // Add by Jason
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);

		try {
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			while (ap.evalXPath() != -1) {
				nodeContent = vUtils.getElementContent();
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		vn.pop(); // Add by Jason
		return nodeContent;
	}

	/**
	 * 删除目标文件的最后一条切割信息
	 * @param xlfPath
	 *            robert 2011-10-26
	 */
	public void deleteLastSplitInfo(String xlfPath) {
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);

		try {
			String xPath = "/xliff/file/header/hs:splitInfos/hs:splitInfo[last()]"; // --robert split
			// String xPath = "/xliff/file/header/splitInfos/splitInfo[last()]";
			VTDUtils vu = new VTDUtils(vn);
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			XMLModifier xm = vu.delete(ap, null, xPath, VTDUtils.PILOT_TO_END);
			saveAndReparse(xm, xlfPath);

		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 获取分割文件的分割源文件的名称 robert 2012-06-08
	 * @param srcXlfPath
	 * @return
	 */
	public String getSplitOriginalName(String srcXlfPath) {
		String originalFileName = null;
		VTDNav vn = vnMap.get(srcXlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + srcXlfPath);
		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		try {
			String xPath = "/xliff/file/header/hs:splitInfos";
			int index;
			ap.selectXPath(xPath);
			while (ap.evalXPath() != -1) {
				index = vn.getAttrVal("original");
				if (index != -1) {
					originalFileName = vn.toString(index);
					return originalFileName;
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return originalFileName;
	}

	/**
	 * 删除目标文件的切割信息的父节点<hs:splitInfos>
	 * @param xlfPath
	 */
	public void deleteSplitInfoParent(String xlfPath) {
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);

		try {
			String xPath = "/xliff/file/header/hs:splitInfos"; // --robert split
			// String xPath = "/xliff/file/header/splitInfos";
			VTDUtils vu = new VTDUtils(vn);
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			XMLModifier xm = vu.delete(ap, null, xPath, VTDUtils.PILOT_TO_END);
			saveAndReparse(xm, xlfPath);

		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}

	}

	/**
	 * 获取某节点的总数 robert 2011-10-26
	 * @return
	 */
	public int getNodeCount(String xlfPath, String nodeXpath) {
		int transUnitNum = 0;
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		try {
			AutoPilot ap = new AutoPilot(vn.duplicateNav());
			ap.selectXPath("count(" + nodeXpath + ")");
			transUnitNum = (int) ap.evalXPathToNumber();
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return transUnitNum;
	}

	/**
	 * 统计当前handler打开文件集已翻译个数
	 * @return ;
	 */
	public int getTranslatedCount() {
		int result = 0;
		Set<String> keySet = vnMap.keySet();
		for (String key : keySet) {
			result += getNodeCount(key,
					"/xliff/file/body/descendant::trans-unit[@approved = 'yes' and target/@state != 'translated' and target/@state != 'signed-off']");
			result += getNodeCount(key,
					"/xliff/file/body/descendant::trans-unit/target[@state = 'translated' or @state = 'signed-off']");
		}
		return result;
	}

	/**
	 * 统计当前handler打开文件集已批准个数
	 * @return ;
	 */
	public int getApprovedCount() {
		int result = 0;
		Set<String> keySet = vnMap.keySet();
		for (String key : keySet) {
			result += getNodeCount(key,
					"/xliff/file/body/descendant::trans-unit[not(@approved='yes') and target/@state='signed-off']");
			result += getNodeCount(key, "/xliff/file/body/descendant::trans-unit[@approved = 'yes']");
		}
		return result;
	}

	/**
	 * 获取指定xpath节点的所有属性,如果没有属性,则返回null
	 * @param xlfPath
	 * @param nodeXpath
	 * @return ;
	 */
	public Hashtable<String, String> getNodeAttributes(String xlfPath, String nodeXpath) {
		Hashtable<String, String> attributes = new Hashtable<String, String>();

		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		try {
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			while (ap.evalXPath() != -1) {
				attributes = vu.getCurrentElementAttributs();
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return attributes;
	}

	/**
	 * 获取某属性名的属性值
	 * @param xlfPath
	 * @param nodeXpath
	 * @param attrName
	 * @return robert 2011-11-02
	 */
	public String getNodeAttribute(String xlfPath, String nodeXpath, String attrName) {
		String attribute = "";

		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		try {
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			while (ap.evalXPath() != -1) {
				attribute = vu.getCurrentElementAttribut(attrName, "");
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return attribute;
	}

	/**
	 * 根据rowId获取trans-unit节点源文本的纯文本
	 * @param rowId
	 * @param isSrc
	 *            是否获取源文本，true:获取源文本，false：获取目标文本
	 * @return ;
	 */
	public String getTUPureTextByRowId(String rowId, boolean isSrc) {
		String xlfPath = RowIdUtil.getFileNameByRowId(rowId);
		String tuXpath = RowIdUtil.parseRowIdToXPath(rowId);
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		// /home/robert/workspace/runtime-hs_ts.product/test1/XLIFF/translate test.txt.xlf￱C:\Documents and
		// Settings\Administrator\桌面\translate test.txt￱2
		String childXpath = isSrc ? "/source" : "/target";

		try {
			ap.selectXPath(tuXpath + childXpath);
			if (ap.evalXPath() != -1) {
				return getTUPureText(vn);
			}

		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} finally {
			vn.pop();
		}
		return null;
	}
	
	public String getTUFullTextByRowId(String rowId, boolean isSrc) {
		String xlfPath = RowIdUtil.getFileNameByRowId(rowId);
		String tuXpath = RowIdUtil.parseRowIdToXPath(rowId);
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		// /home/robert/workspace/runtime-hs_ts.product/test1/XLIFF/translate test.txt.xlf￱C:\Documents and
		// Settings\Administrator\桌面\translate test.txt￱2
		String childXpath = isSrc ? "/source" : "/target";

		try {
			ap.selectXPath(tuXpath + childXpath);
			if (ap.evalXPath() != -1) {
				return new VTDUtils(vn).getElementContent();
			}

		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} finally {
			vn.pop();
		}
		return null;
	}

	/**
	 * 获取trans-unit节点下source或target节点的全文本或纯文本 robert 2011-12-14
	 * @param xlfPath
	 *            : xliff文件路径
	 * @param nodeXpath
	 *            : trans-unit子节点source或target的xpath值 如果返回null,则证明这个节点是个空节点,要么没有这个节点，要么这个节点没有值
	 * @return textMap：两个值，key1 --> fullText:全文本，key2 --> pureText：纯文本。
	 */
	public Map<String, String> getFullAndPureText(String xlfPath, String nodeXpath) {
		Map<String, String> textMap = new HashMap<String, String>();
		VTDNav vn = vnMap.get(xlfPath);
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(nodeXpath);
			if (ap.evalXPath() != -1) {
				String content = vUtils.getElementContent();
				if (content != null && !"".equals(content)) {
					textMap.put("fullText", content);
					textMap.put("pureText", getTUPureText(vn));
				}
			}

		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		vn.pop();
		return textMap;
	}

	/**
	 * 针对trans-unit节点而言，获取其下source和target节点的纯文本字符串 robert 2011-12-14 (从QAXmlHandler中拷取)
	 * @param xlfPath
	 * @param nodeXpath
	 * @return
	 */
	public String getTUPureText(VTDNav vn) {
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		String pureText = "";
		try {
			VTDUtils vUtils = new VTDUtils(vn);
			pureText = vUtils.getElementContent();
			// 如果子节点大于0，那继续处理
			if (vUtils.getChildElementsCount() > 0) {
				ap.resetXPath();
				ap.selectXPath("./*");
				while (ap.evalXPath() != -1) {
					String childNodeName = vUtils.getCurrentElementName();
					if (QAConstant.QA_mrk.equals(childNodeName)) { // 与R7相比，我去掉了一行代码：&&
																	// "term".equals(vUtils.getCurrentElementAttribut("mtype",
																	// "")
						if (vUtils.getChildElementsCount() <= 0) {
							String childFrag = vUtils.getElementFragment();
							String childContent = vUtils.getElementContent();
							childContent = childContent == null ? "" : childContent;
							pureText = pureText.replace(childFrag, childContent);
						} else {
							String childFrag = vUtils.getElementFragment();
							String childContent = getTUPureText(vn);
							childContent = childContent == null ? "" : childContent;
							pureText = pureText.replace(childFrag, childContent);
						}
					} else if (QAConstant.QA_g.equals(childNodeName) || QAConstant.QA_sub.equals(childNodeName)) {

						if (vUtils.getChildElementsCount() <= 0) {
							String childFrag = vUtils.getElementFragment();
							String childContent = vUtils.getElementContent();
							childContent = childContent == null ? "" : childContent;
							pureText = pureText.replace(childFrag, childContent);
						} else {
							String childFrag = vUtils.getElementFragment();
							String childContent = getTUPureText(vn);
							childContent = childContent == null ? "" : childContent;
							pureText = pureText.replace(childFrag, childContent);
						}
					} else {
						// ph节点的值为code data或者一个sub节点，因此，要考虑到sub节点的情况
						if (vUtils.getChildElementsCount() <= 0) {
							String childFrag = vUtils.getElementFragment();
							pureText = pureText.replace(childFrag, "");
						} else {
							String childFrag = vUtils.getElementFragment();
							String childContent = getSubNodePureText(vn);
							pureText = pureText.replace(childFrag, childContent);
						}
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} finally {
			vn.pop();
		}
		// 去掉标记以后，清除多余的空格，包括段首段末空格，两个以上的空格

		return pureText;
	}

	/**
	 * 获取ph,etp,btp,it节点下sub子节点的纯文本 //ph,etp,btp,it节点，只有其子节点sub内的文本才是翻译文本，故，去掉ph,etp,btp,it节点的纯文本 robert 2011-12-14
	 * (从QAXmlHandler中拷取)
	 * @param vn
	 * @return
	 */
	public String getSubNodePureText(VTDNav vn) {
		String subPureText = "";
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("./*");
			VTDUtils vUtils = new VTDUtils(vn);
			while (ap.evalXPath() != -1) {
				if (vUtils.getChildElementsCount() <= 0) {
					subPureText += vUtils.getElementContent();
				} else {
					subPureText += getTUPureText(vn);
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return subPureText;
	}

	/**
	 * 将当前的vn定位到指的xpath Add by Jason
	 * @param xlfPath
	 *            文件路径
	 * @param xpath
	 *            如/xliff/file/body/trans-unit[1],则将当前VN定位到第一个trans-unit节点;
	 */
	public void vnToXpath(String xlfPath, String xpath) {
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath(xpath);
			ap.evalXPath();
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 从当前xlfPath的文件中删除xpath指定的内容 Add By Jason
	 * @param xlfPath
	 *            文件
	 * @param xpath
	 *            如当前VN定位在trans-unit节点，则./target|./alt-trans 为删除所有的target节点和alt-trans节点 ;
	 */
	public void deleteNodeByXpath(String xlfPath, String xpath) {
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		try {
			VTDUtils vu = new VTDUtils(vn);
			XMLModifier xm = vu.delete(xpath, VTDUtils.PILOT_TO_END);
			saveAndReparse(xm, xlfPath);
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 将content内容插入到当前的vn所有在的元素节点之中 Add By Jason
	 */
	public void insert(String xlfPath, String content) {
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		try {
			XMLModifier xm = new XMLModifier(vn);
			xm.insertBeforeTail(content);
			saveAndReparse(xm, xlfPath);
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 锁定当前vn所在的Trans-unit Add by Jaosn
	 * @param xlfPath
	 *            当前文件
	 * @param translateValue
	 *            "yes"锁定,"no"解锁
	 * @return true设置成功,false设置失败;
	 */
	public void lockTransUnit(String xlfPath, String translateValue) {
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		try {
			XMLModifier xm = new XMLModifier(vn);
			changeTranslateProp(vn, translateValue, xm);
			saveAndReparse(xm, xlfPath); // 保存并更新VTDNav对象
		} catch (Exception e) {
			LOGGER.error(Messages.getString("file.XLFHandler.logger19"), e);
		}
	}

	/**
	 * 通过rowId获取当前翻译单元的上下文
	 * @param rowId
	 * @param num
	 *            上下文个数
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
			String tuXpath = RowIdUtil.parseRowIdToXPath(rowId);
			vu.pilot(tuXpath);
		} catch (NavException e1) {
			String errorMsg = Messages.getString("file.XLFHandler.logger4");
			LOGGER.error(errorMsg, e1);
			return null;
		}
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		result.put("x-preContext", getContext(vu, ap, num, true));
		result.put("x-nextContext", getContext(vu, ap, num, false));
		return result;
	}

	/**
	 * 针对自动品质检查，根据rowId获取当前tu节点的所有过滤后的数据，过滤条件为不包括上下文匹配，不包括完全匹配，不包括已锁文本，过滤条件在首选项中设置.包括源文本，源纯文本，目标文本，目标纯文本，源语言，目标语言 robert
	 * 2011-02-14
	 * @return ;
	 */
	public QATUDataBean getAutoQAFilteredTUText(String rowId, Map<String, Boolean> filterMap) {
		QATUDataBean tuDataBean = new QATUDataBean();
		VTDNav vn = getVTDNavByRowId(rowId).duplicateNav();
		String tuXpath = RowIdUtil.parseRowIdToXPath(rowId);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + RowIdUtil.getFileNameByRowId(rowId));

		try {
			AutoPilot ap = new AutoPilot(vn);
			AutoPilot childAp = new AutoPilot(vn);
			AutoPilot langAp = new AutoPilot(vn);
			VTDUtils vUtils = new VTDUtils(vn);
			ap.selectXPath(tuXpath);
			if (ap.evalXPath() != -1) {
				// 首先过滤，如果有不应包括的文本段，则返回一个空对象
				if (!filterTheTU(vn, filterMap)) {
					tuDataBean.setPassFilter(false);
					return tuDataBean;
				}

				vn.push();
				String srcLang = "";
				// 取出源文本的纯文本之前，先查看其内容是否为空，若为空，则返回null，没有source节点，也返回null
				childAp.selectXPath("./source");
				if (childAp.evalXPath() != -1) { // 因为标准里面只有一个source，因此此处用if不用while
					String srcContent = vUtils.getElementContent();
					// 如果源文本为空或无值，则返回null
					if (srcContent == null || "".equals(srcContent)) {
						return null;
					} else {
						tuDataBean.setSrcPureText(getTUPureText(vn));
						tuDataBean.setSrcContent(srcContent);
					}

					// 获取源语言
					int langIdx;
					if ((langIdx = vn.getAttrVal("xml:lang")) != -1) {
						srcLang = vn.toString(langIdx);
					} else {
						// 若该节点没有源语言，那么向上去查找file节点的源语言
						langAp.selectXPath("ancestor::file");
						if (langAp.evalXPath() != -1) {
							if ((langIdx = vn.getAttrVal("source-language")) != -1) {
								srcLang = vn.toString(langIdx);
							}
							langAp.resetXPath();
						}
					}
					tuDataBean.setSrcLang(srcLang);
				} else {
					return null;
				}
				childAp.resetXPath();
				vn.pop();

				// 下面获取目标文本的纯文本，在之前先检查目标文本是否为空或为空值，若是，则返回null，若没有target节点，也返回空
				childAp.selectXPath("./target");
				if (childAp.evalXPath() != -1) { // 因为标准里面只有一个target，因此此处用if不用while
					String tgtContent = vUtils.getElementContent();
					// 如果源文本为空或无值，则返回空对象
					if (tgtContent == null || "".equals(tgtContent)) {
						return tuDataBean;
					} else {
						tuDataBean.setTgtContent(tgtContent);
						tuDataBean.setTgtPureText(getTUPureText(vn));
					}

					// 获取目标语言
					String tgtLang = "";
					int langIdx;
					if ((langIdx = vn.getAttrVal("xml:lang")) != -1) {
						tgtLang = vn.toString(langIdx);
					} else {
						// 若该节点没有源语言，那么向上去查找file节点的源语言
						langAp.selectXPath("ancestor::file");
						if (langAp.evalXPath() != -1) {
							if ((langIdx = vn.getAttrVal("target-language")) != -1) {
								tgtLang = vn.toString(langIdx);
							}
						}
					}
					tuDataBean.setTgtLang(tgtLang);
				} else {
					return tuDataBean;
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return tuDataBean;
	}

	/**
	 * <div style='color:red'>注意：该方法与　类　{@link net.heartsome.cat.ts.core.qa.QAXmlHandler#filterTheTU(VTDNav, Map)}完全一样，请注意保持两者同步</div>
	 * 过滤trans-unit节点，过滤条件为，过滤掉不包括的文本段，如不包括上下文匹配，不包括完全匹配，不包括已锁文本， 如果过滤不成功，就返回false，过滤成功(即没有不应包括的文本段)，就返回true robert
	 * @param vn
	 * @param filterMap
	 *            过滤条件
	 * @return 备注：重复,从QAXmlHandler中拷取过来的。
	 */
	public boolean filterTheTU(VTDNav vn, Map<String, Boolean> filterMap) {
		vn.push();
		AutoPilot filterAp = new AutoPilot(vn);
		try {
			//检查上下文匹配	translate(alt-trans/@match-quality, '%', '')=101
			if (filterMap.get(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE)) {
				// hs:matchType="TM" hs:quality="101"
				filterAp.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
				filterAp.selectXPath("translate(target/@hs:quality, '%', '')=101");
				//如果检查到有上下文匹配，就返回false,标志过滤未通过
				if (filterAp.evalXPathToBoolean()) {
					return false;
				}
				filterAp.resetXPath();
			}
			
			//检查完全匹配	translate(alt-trans/@match-quality, '%', '')=100
			if (filterMap.get(QAConstant.QA_PREF_FULLMATCH_NOTINCLUDE)) {
				filterAp.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
				filterAp.selectXPath("translate(target/@hs:quality, '%', '')=100");
				if (filterAp.evalXPathToBoolean()) {
					return false;
				}
				filterAp.resetXPath();
			}
			
			//检查已锁定的文本段	@translate='no'
			if (filterMap.get(QAConstant.QA_PREF_LOCKED_NOTINCLUDE)) {
				filterAp.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
				filterAp.selectXPath("@translate='no'");
				if (filterAp.evalXPathToBoolean()) {
					return false;
				}
				filterAp.resetXPath();
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		vn.pop();
		return true;
	}

	/**
	 * 获取要合并打开的文件路径 robert 2012-03-27
	 * @return ;
	 */
	public List<String> getMultiFiles(IFile tempiFile) {
		String tempiFileLC = tempiFile.getLocation().toOSString();
		Map<String, Object> resultMap = openFile(tempiFileLC);
		if (resultMap == null
				|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap.get(Constant.RETURNVALUE_RESULT)) {
			return null;
		}
		List<String> mergerFileList = new ArrayList<String>();
		VTDNav vn = vnMap.get(tempiFileLC);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + tempiFileLC);
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("/mergerFiles/mergerFile/@filePath");
			int index = -1;
			while ((index = ap.evalXPath()) != -1) {
				String fileLC = vn.toString(index + 1);
				if (fileLC != null && !"".equals(fileLC)) {
					mergerFileList.add(fileLC);
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return mergerFileList;
	}

	/** burke 为修改锁定行目标文本不能修改其大小写添加 getCaseTgtContent方法 */
	/**
	 * 获取要改变大小写的target中的文本内容
	 * @param rowId
	 * @return
	 */
	public String getCaseTgtContent(String rowId) {
		AutoPilot ap = new AutoPilot();
		VTDNav vn = null;
		vn = getVTDNavByRowId(rowId);
		ap.bind(vn);
		try {
			String tuXPath = RowIdUtil.parseRowIdToXPath(rowId);
			ap.selectXPath(tuXPath);
			if (ap.evalXPath() != -1) {
				int attrIndex = vn.getAttrVal("translate");
				if (attrIndex != -1) {
					String attrValue = vn.toString(attrIndex);
					if (attrValue.equals("no")) {
						return "no";
					}
				}
			}
			ap.resetXPath();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		String tgt = getTgtContent(rowId);

		return tgt;
	}

	/**
	 * 根据ROWID获取TU节点在文件中的位置，位置是从1开始的 --robert 2012-05-07
	 * @return ;
	 */
	public Integer getTUPositionByRowId(String rowId) {
		int tuPosition = -1;
		VTDNav vn = vnMap.get(RowIdUtil.getFileNameByRowId(rowId));
		AutoPilot ap = new AutoPilot(vn);
		String xpath = RowIdUtil.parseRowIdToXPath(rowId);
		try {
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				ap.selectXPath("count(preceding::trans-unit[source/text()!='' or source/*])");
				tuPosition = (int) ap.evalXPathToNumber() + 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tuPosition;
	}

	/**
	 * 针对锁定重复文本段之锁定外部重复，获取每一个tu节点的相关信息，如纯文本，与所有文本，上下文的hash值 robert 2012-05-08
	 * @param filePath
	 *            文件路径
	 * @return 存储所选文件的所有trans-unit节点的源文本 Map<trans-unit唯一标识符, Map<source节点的内容或纯文本
	 *         (QAConstant.FA_SRC_CONTENT/QAConstant.FA_SRC_PURE_TEXT), 值>>
	 */
	public Map<String, String> getTUsrcText(String filePath, String xpath, int contextSize) {
		Map<String, String> srcTextMap = new HashMap<String, String>();
		VTDNav vn = vnMap.get(filePath);
		try {
			Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + filePath);
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vUtils = new VTDUtils(vn);
			AutoPilot childAp = new AutoPilot(vn);
			AutoPilot contextAp = new AutoPilot(vn);
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				String rowId = RowIdUtil.getRowId(vn, filePath);
				vn.push();
				childAp.selectXPath("./source");
				if (childAp.evalXPath() != -1) {
					String pureText = getTUPureText(vn);
					String content = vUtils.getElementContent();
					srcTextMap.put("content", content);
					srcTextMap.put("pureText", pureText);
				}
				vn.pop();

				// 获取上文的hash值
				vn.push();
				StringBuilder contextStr = new StringBuilder();
				contextAp.selectXPath("preceding::trans-unit/source[text()!='' or *]");
				int i = 0;
				while (contextAp.evalXPath() != -1 && i < contextSize) {
					contextStr.append("," + getTUPureText(vn).hashCode());
					i++;
				}
				contextAp.resetXPath();
				vn.pop();
				if (contextStr.length() > 0) {
					contextStr.substring(1, contextStr.length());
				}
				String preHash = contextStr.length() > 0 ? (contextStr.substring(1, contextStr.length())) : contextStr
						.toString();

				// 获取下文的hash值
				vn.push();
				contextStr = new StringBuilder();
				contextAp.selectXPath("following::trans-unit/source[text()!='' or *]");
				i = 0;
				while (contextAp.evalXPath() != -1 && i < contextSize) {
					contextStr.append("," + getTUPureText(vn).hashCode());
					i++;
				}
				contextAp.resetXPath();
				vn.pop();
				if (contextStr.length() > 0) {
					contextStr.substring(1, contextStr.length());
				}
				String nextHash = contextStr.length() > 0 ? (contextStr.substring(1, contextStr.length())) : contextStr
						.toString();
				srcTextMap.put("nextHash", nextHash);
				srcTextMap.put("preHash", preHash);
				srcTextMap.put("rowId", rowId);
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return srcTextMap;
	}

	/**
	 * 针对xliff文件分割，获取下一个rowId。 robert 2012-05-10
	 * @param xlfPath
	 * @param tagOrRowId
	 * @return
	 */
	public String getNextRowId(String xlfPath, String tagOrRowId) {
		String rowId = "";
		VTDNav vn = vnMap.get(xlfPath).duplicateNav();
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		AutoPilot ap = new AutoPilot(vn);
		String xpath = "";
		try {
			if ("start".equals(tagOrRowId)) {
				// 备注：这里的xpath语句不用添加(source/text()!='' or source/*)验证，因为要保证xliff文件的完整性。
				xpath = "/xliff/file[1]/body/descendant::trans-unit[position()='1']";
				ap.selectXPath(xpath);
				if (ap.evalXPath() != -1) {
					return RowIdUtil.getRowId(vn, xlfPath);
				}
				return null;
			} else if ("end".equals(tagOrRowId)) {
				xpath = "/xliff/file[last()]/body/descendant::trans-unit[last()]";
				ap.selectXPath(xpath);
				if (ap.evalXPath() != -1) {
					return RowIdUtil.getRowId(vn, xlfPath);
				}
				return null;
			} else {
				xpath = RowIdUtil.parseRowIdToXPath(tagOrRowId);
				ap.selectXPath(xpath);
				if (ap.evalXPath() != -1) {
					// AutoPilot followAP = new AutoPilot(vn);
					ap.resetXPath();
					ap.selectXPath("following::trans-unit");
					if (ap.evalXPath() != -1) {
						return RowIdUtil.getRowId(vn, xlfPath);
					}
				}
				return null;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return rowId;
	}

	/**
	 * 根据切割点的位置，获取file与body第一级子节点的序列号
	 * @param tagOrRowId
	 * @return
	 */
	public Map<String, Integer> getSplitNodeIdx(String xlfPath, String tagOrRowId) {
		Map<String, Integer> nodeIdxMap = new HashMap<String, Integer>();
		VTDNav vn = vnMap.get(xlfPath).duplicateNav();
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		AutoPilot ap = new AutoPilot(vn);
		AutoPilot checkAP = new AutoPilot(vn);
		String xpath = "";
		try {
			if ("start".equals(tagOrRowId)) {
				nodeIdxMap.put("fileNodeIdx", 1);
				nodeIdxMap.put("bodyChildNodeIdx", 1);
			} else if ("end".equals(tagOrRowId)) {
				xpath = "count(/xliff/file)";
				ap.selectXPath(xpath);
				int fileIdx = (int) ap.evalXPathToNumber();
				nodeIdxMap.put("fileNodeIdx", fileIdx);
				ap.resetXPath();
				xpath = "count(/xliff/file[last()]/body/node())";
				ap.selectXPath(xpath);
				int bodyChildNodeIdx = (int) ap.evalXPathToNumber();
				nodeIdxMap.put("bodyChildNodeIdx", bodyChildNodeIdx);
			} else {
				int fileIdx = -1;
				int bodyChildNodeIdx = -1;
				boolean isBodyParent = false; // 其父节点是否是body
				// 这是rowId的情况，先定位到当前节点
				ap.selectXPath(RowIdUtil.parseRowIdToXPath(tagOrRowId));
				if (ap.evalXPath() != -1) {
					// 查看其父节点是否是body
					vn.push();
					checkAP.selectXPath("parent::node()");
					if (checkAP.evalXPath() != -1) {
						int index = vn.getCurrentIndex();
						if (index != -1) {
							// 这表示，当前trans-unit节点就是body的第一级子节点
							if ("body".equals(vn.toString(index))) {
								isBodyParent = true;
							}
						} else {
							return null;
						}
					} else {
						return null;
					}
					vn.pop();

					if (isBodyParent) {
						checkAP.resetXPath();
						checkAP.selectXPath("count(preceding-sibling::node())");
						bodyChildNodeIdx = (int) checkAP.evalXPathToNumber() + 2;
						// 查看当前body节点的子节点个数是否大于bodyChildNodeIdx
						vn.push();
						checkAP.selectXPath("following-sibling::node()");
						if (checkAP.evalXPath() == -1) {
							bodyChildNodeIdx = 1;
						}
						vn.pop();
						// 定位到file父节点，去获取file节点的个数
						vn.push();
						checkAP.resetXPath();
						checkAP.selectXPath("ancestor::file");
						if (checkAP.evalXPath() != -1) {
							checkAP.resetXPath();
							checkAP.selectXPath("count(preceding-sibling::file)");
							fileIdx = (int) checkAP.evalXPathToNumber() + 1;
							// 如果当前节点是处于body节点的最后一个子节点，那么，fileIdx就应该加1
							if (bodyChildNodeIdx == 1) {
								fileIdx++;
							}
						}
						vn.pop();
					} else {
						// 如果是body的孙子节点，先定位到当前节点所在的body子节点
						vn.push();
						checkAP.resetXPath();
						checkAP.selectXPath("ancestor::node()");
						while (checkAP.evalXPath() != -1) {
							boolean isBodyParent_1 = false;
							vn.push();
							ap.resetXPath();
							ap.selectXPath("parent::node()");
							if (ap.evalXPath() != -1) {
								int index = vn.getCurrentIndex();
								if (index != -1 && "body".equals(vn.toString(index))) {
									isBodyParent_1 = true;
								}
							}
							vn.pop();

							if (isBodyParent_1) {
								ap.resetXPath();
								ap.selectXPath("count(preceding-sibling::node())");
								bodyChildNodeIdx = (int) ap.evalXPathToNumber() + 1;

								checkAP.resetXPath();
								checkAP.selectXPath("ancestor::file");
								if (checkAP.evalXPath() != -1) {
									checkAP.resetXPath();
									checkAP.selectXPath("count(preceding-sibling::file)");
									fileIdx = (int) checkAP.evalXPathToNumber() + 1;
								}
								break;
							}
						}
						vn.pop();
					}
					nodeIdxMap.put("fileNodeIdx", fileIdx);
					nodeIdxMap.put("bodyChildNodeIdx", bodyChildNodeIdx);
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return nodeIdxMap;
	}

	/**
	 * 针对xliff文件分割，获取相应的trans-unit的内容，包括其他结点，例如group。这种处理方式，针对body第一级子节点。robert 2012-05-11
	 * 备注：先通过file与body第一级子节点定位到起始位置，再通过startRowId进行判断要获取的节点。直到endRowId或file节点结束时为止。
	 * @param xlfPath
	 * @param fileIdx
	 *            要获取节点内容的当前file节点的序列号(从1开始)
	 * @param startBodyChildIdx
	 *            要获取节点内容的起始位置（针对body节点的第一级子节点，从1开始）
	 * @param startRowId
	 *            起始rowId或者标记(如notFirst，这表示为file节点的第一个位置开始)
	 * @param endRowId
	 *            结束位置的rowId
	 * @param isFirstFileNode
	 *            是否是新生成的分割文件的第一个file节点。
	 * @param isLastOfFile
	 *            是否是被分割xliff文件的最后，因为这时要处理一些在分割点之外的节点。
	 * @return
	 */
	public String getSplitTuData(String xlfPath, int fileIdx, int startBodyChildIdx, String startRowId,
			String endRowId, boolean isFirstFileNode, boolean isLastOfFile) {
		StringBuffer dataSB = new StringBuffer();
		List<String[]> removeTuList;
		boolean isEnd = false;
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		AutoPilot ap = new AutoPilot(vn);
		AutoPilot checkAp = new AutoPilot(vn);
		// 从这个tuXpath所定位的tu节点开始，到endRowId所定位的tu节点结束，或到一个file节点的尾端结束
		String tuXpath = "/xliff/file[" + fileIdx + "]/body/node()[" + startBodyChildIdx + "]";
		try {
			ap.selectXPath(tuXpath);
			if (ap.evalXPath() != -1) {
				VTDUtils vu = new VTDUtils(vn);
				// 起始结点的数据也要计算在内，也要判断有tu子节点的情况
				String firstNodeFrag = vu.getElementFragment();
				// 判断当前节点是否是tu节点，如果是，则判断是否等于startRowId，如果不是，那就进行其子节点，获取相关节点
				int index = vn.getCurrentIndex();
				// 是否结束，效果与isEnd相似，但是针对情况不同。
				boolean isTheLast = false;
				if (index != -1) {
					if ("trans-unit".equals(vn.toString(index))) {
						if (isFirstFileNode) {
							if (!RowIdUtil.getRowId(vn, xlfPath).equals(startRowId)) {
								firstNodeFrag = "";
							}
						}
						// 针对两个分割点相连的情况，判断起始rowId是否等于终止rowId，如果是，则退出程序
						if (endRowId.equals(RowIdUtil.getRowId(vn, xlfPath))) {
							isTheLast = true;
						}
					} else {
						removeTuList = new LinkedList<String[]>();
						// 开始循环tu节点
						vn.push();
						checkAp.resetXPath();
						checkAp.selectXPath("descendant::trans-unit");
						boolean isStart = false;
						while (checkAp.evalXPath() != -1) {
							// 如果这是分割后的文件的第一个file节点，那么，它的起始rowId才不会为空。
							String curRowId = RowIdUtil.getRowId(vn, xlfPath);
							if (isFirstFileNode) {
								if (!isStart && startRowId.equals(curRowId)) {
									isStart = true;
								}
								if (!isStart) {
									removeTuList.add(new String[] { "", vu.getElementFragment() });
								}
							}
							// 在没有开始，或已经结束这个区间之外的所有TU节点都要被删除，注意这个开始节点与结束节点的判断位置，因为结束点也必须包括在内
							if (isEnd) {
								// 由于文件名变更以后，rowId也会变更，故存入的格式为original与tuID
								String original = RowIdUtil.getOriginalByRowId(curRowId);
								String tuId = RowIdUtil.getTUIdByRowId(curRowId);
								removeTuList.add(new String[] { RowIdUtil.getRowId("{0}", original, tuId),
										vu.getElementFragment() });
							}
							if (!isEnd && endRowId.equals(RowIdUtil.getRowId(vn, xlfPath))) {
								isEnd = true;
							}
						}
						if (removeTuList.size() >= 1) {
							for (String[] tuRowIdAndFrag : removeTuList) {
								String tuPlaceHolder = "";
								if (!"".equals(tuRowIdAndFrag[0])) {
									tuPlaceHolder = "<hs:TuPlaceHolder rowId=\"" + tuRowIdAndFrag[0] + "\" />";
								}
								firstNodeFrag = firstNodeFrag.replace(tuRowIdAndFrag[1], tuPlaceHolder);
							}
						}
						vn.pop();
					}
				}

				dataSB.append(firstNodeFrag);
				if (isTheLast || isEnd) {
					return dataSB.toString();
				}

				// 开始向下循环每一个body的第一级子节点
				String followNodeXpath = "following-sibling::node()";
				ap.resetXPath();
				ap.selectXPath(followNodeXpath);
				while (ap.evalXPath() != -1) {
					String curNodeFrag = vu.getElementFragment();
					index = -1;
					index = vn.getCurrentIndex();
					if (index != -1) {
						String nodeName = vn.toString(vn.getCurrentIndex());
						// 如果名称等于trans-unit，那么标志这个节点就是tu节点
						if ("trans-unit".equals(nodeName)) {
							String rowId = RowIdUtil.getRowId(vn, xlfPath);
							if (endRowId.equals(rowId)) {
								// 如果这是最后一处分割节点，那么，继续循环，获取最后几个在分割点RowId之外的非TU节点
								if (!isLastOfFile) {
									dataSB.append(curNodeFrag);
									break;
								}
							}
						} else {
							// 否则，循环其子节点，查看其中是否存在子节点
							isEnd = false;
							removeTuList = new LinkedList<String[]>();
							vn.push();
							checkAp.selectXPath("descendant::trans-unit");
							while (checkAp.evalXPath() != -1) {
								String curRowId = RowIdUtil.getRowId(vn, xlfPath);
								if (isEnd) {
									String original = RowIdUtil.getOriginalByRowId(curRowId);
									String tuId = RowIdUtil.getTUIdByRowId(curRowId);
									removeTuList.add(new String[] { RowIdUtil.getRowId("{0}", original, tuId),
											vu.getElementFragment() });
								} else {
									if (endRowId.equals(curRowId)) {
										isEnd = true;
									}
								}
							}
							checkAp.resetXPath();
							vn.pop();
							if (isEnd) {
								// 如果已经到达结束点，那么删除该删除的tu节点
								for (String[] tuRowIdAndFrag : removeTuList) {
									String tuPlaceHolder = "<hs:TuPlaceHolder rowId=\"" + tuRowIdAndFrag[0] + "\" />";
									curNodeFrag = curNodeFrag.replace(tuRowIdAndFrag[1], tuPlaceHolder);
								}
								if (!isLastOfFile) {
									dataSB.append(curNodeFrag);
									break;
								}
							}
						}
					}
					dataSB.append(curNodeFrag);
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return dataSB.toString();
	}

	/**
	 * 针对合并后的xliff文件，会出现有些重复节点的情况，这个方法就是将重复节点进行整合。robert 2012-05-13
	 * 备注：要处理这些重复的节点，首选就是循环所有的占位符，这里是自定义的节点，例如<hs:TuPlaceHolder rowId="this is a rowId" />，之后通过rowId把这些文件
	 * @param xlfPath
	 */
	public void operateMergedXliff(String xlfPath) {
		VTDNav vn = vnMap.get(xlfPath);
		Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + xlfPath);
		AutoPilot ap = new AutoPilot(vn);
		AutoPilot updateAP = new AutoPilot(vn);
		AutoPilot parentAP = new AutoPilot(vn);
		try {
			XMLModifier xm = new XMLModifier(vn);
			VTDUtils vu = new VTDUtils(vn);
			// 先把把所有的占位符的RowId进行修改成当前文件的rowId
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			ap.selectXPath("/xliff/file/body//hs:TuPlaceHolder");
			while (ap.evalXPath() != -1) {
				int index = vn.getAttrVal("rowId");
				if (index != -1) {
					String rowId = vn.toString(index);
					String newRowId = MessageFormat.format(rowId, new Object[] { xlfPath });
					xm.updateToken(index, newRowId.getBytes());
				}
			}
			saveAndReparse(xm, xlfPath);

			// 现在开始，将所有的rowId取出，再取出rowId所对应的TU节点的frag。
			vn = vnMap.get(xlfPath);
			ap.bind(vn);
			updateAP.bind(vn);
			vu.bind(vn);
			xm.bind(vn);
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			ap.selectXPath("/xliff/file/body//hs:TuPlaceHolder");
			Map<String, String> rowIdMap = new LinkedHashMap<String, String>();
			while (ap.evalXPath() != -1) {
				String tuFrag = "";
				String rowId = "";
				int index = vn.getAttrVal("rowId");
				if (index != -1) {
					rowId = vn.toString(index);
					vn.push();
					updateAP.selectXPath(RowIdUtil.parseRowIdToXPath(rowId));
					if (updateAP.evalXPath() != -1) {
						tuFrag = vu.getElementFragment();
					}
					vn.pop();
				}
				// 开始存值
				if (!"".equals(tuFrag)) {
					rowIdMap.put(rowId, tuFrag);
				}
			}

			// 开始根据所有的rowId进行删除父节点
			parentAP.bind(vn);
			for (String rowId : rowIdMap.keySet()) {
				ap.selectXPath(RowIdUtil.parseRowIdToXPath(rowId));
				while (ap.evalXPath() != -1) {
					updateAP.resetXPath();
					updateAP.selectXPath("ancestor::node()");
					while (updateAP.evalXPath() != -1) {
						boolean isBodyParent = false;
						vn.push();
						parentAP.selectXPath("parent::node()");
						if (parentAP.evalXPath() != -1) {
							int index = vn.getCurrentIndex();
							if (index != -1 && "body".equals(vn.toString(index))) {
								isBodyParent = true;
							}
						}
						vn.pop();
						if (isBodyParent) {
							xm.remove(vn.getElementFragment());
							saveAndReparse(xm, xlfPath);
							vn = vnMap.get(xlfPath);
							ap.bind(vn);
							updateAP.bind(vn);
							parentAP.bind(vn);
							vu.bind(vn);
							xm.bind(vn);
							ap.resetXPath();
							break;
						}
					}
				}
			}

			// 最后，开始将每个占位符进行替换
			vn = vnMap.get(xlfPath);
			ap.bind(vn);
			updateAP.bind(vn);
			vu.bind(vn);
			xm.bind(vn);
			ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
			ap.selectXPath("/xliff/file/body//hs:TuPlaceHolder");
			while (ap.evalXPath() != -1) {
				String tuFrag = "";
				String rowId = "";
				int index = vn.getAttrVal("rowId");
				if (index != -1) {
					rowId = vn.toString(index);
					tuFrag = rowIdMap.get(rowId);
					// 先删除，再添加
					xm.remove(vn.getElementFragment());
					xm.insertAfterElement(tuFrag.getBytes());
				}
			}
			saveAndReparse(xm, xlfPath);
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 专门针对字数分析后的锁定文本段 roebrt 2012-05-21
	 * @param rowIdList
	 */
	public void lockFaTU(List<String> rowIdList) {
		if (rowIdList == null || rowIdList.size() <= 0) {
			return;
		}
		VTDNav vn = getVTDNavByRowId(rowIdList.get(0));
		try {
			XMLModifier xm = new XMLModifier(vn);
			saveAndReparse(xm, RowIdUtil.getFileNameByRowId(rowIdList.get(0)));
			lockTransUnits(rowIdList, true);
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 针对重复文本段的判断。获取所有的源文。robert 2012-09-20
	 * @return
	 */
	public Map<String, String> getAllSrcTextForRepeat(String filePath, boolean ignoreTag, String srcLang, String tgtLang) {
		Map<String, String> textMap = new LinkedHashMap<String, String>();
		try {
			VTDNav vn = vnMap.get(filePath);
			Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + filePath);
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vUtils = new VTDUtils(vn);
			AutoPilot childAp = new AutoPilot(vn);

			String xpath = "";
			if (srcLang != null && tgtLang != null) {
				String XPATH_ALL_TU_BYLANGUAGE = "/xliff/file[upper-case(@source-language)=''{0}'' and upper-case(@target-language)=''{1}'']/body/descendant::trans-unit[source/text()!='''' or source/*]";
				xpath = MessageFormat.format(XPATH_ALL_TU_BYLANGUAGE,
						new Object[] { srcLang.toUpperCase(), tgtLang.toUpperCase() });
			} else {
				xpath = "/xliff/file/body/descendant::trans-unit[source/text()!='' or source/*]";
			}
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				String srcText = null;
				String rowId = RowIdUtil.getRowId(vn, filePath);
				vn.push();
				childAp.selectXPath("./source");
				if (childAp.evalXPath() != -1) {
					// 如果忽略标记的话，就没有必要获取source节点的完整内容了
					if (!ignoreTag) {
						srcText = vUtils.getElementContent();
					} else {
						srcText = getTUPureText(vn);
					}
				}
				vn.pop();

				// 存放值
				textMap.put(rowId, srcText.trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return textMap;
	}

	/**
	 * 针对繁殖翻译。获取所有的源文。robert 2012-09-20
	 * @return
	 */
	public Map<String, FuzzyTransDataBean> getAllSrcTextForFuzzy(ArrayList<String> rowIdList, boolean ignoreTag) {
		Map<String, FuzzyTransDataBean> textMap = new LinkedHashMap<String, FuzzyTransDataBean>();
		try {
			VTDNav vn = null;
			AutoPilot ap = new AutoPilot();
			VTDUtils vUtils = new VTDUtils();
			AutoPilot childAp = new AutoPilot();

			for (String rowId : rowIdList) {
				// 标识译文是否为空，如果为空，则为true，不为空则为 false
				boolean isTargetNull = false;
				// 是否锁定
				boolean isLock = false;

				vn = getVTDNavByRowId(rowId).duplicateNav();
				ap.bind(vn);
				vUtils.bind(vn);
				childAp.bind(vn);

				vn.push();
				ap.selectXPath(RowIdUtil.parseRowIdToXPath(rowId));
				if (ap.evalXPath() != -1) {
					// 如果，当前文本段是处于锁定状态的，就不用获取
					int index = vn.getAttrVal("translate");
					if (index != -1 && "no".equalsIgnoreCase(vn.toString(index))) {
						isLock = true;
					}

					String srcText = null;
					vn.push();
					childAp.selectXPath("./source");
					if (childAp.evalXPath() != -1) {
						// 如果忽略标记的话，就没有必要获取source节点的完整内容了
						if (!ignoreTag) {
							srcText = vUtils.getElementContent();
						} else {
							srcText = getTUPureText(vn);
						}
					}
					vn.pop();

					vn.push();
					childAp.selectXPath("./target[text()!='' or *]");
					if (childAp.evalXPath() != -1) {
						isTargetNull = false;
					} else {
						isTargetNull = true;
					}
					vn.pop();

					// 存放值
					textMap.put(rowId, new FuzzyTransDataBean(srcText.trim(), isTargetNull, isLock));
				}

				vn.pop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return textMap;
	}

	/**
	 * 给当前界面所显示的文本段解锁 robert 2012-09-24
	 */
	public void unlockSegment() {
		Map<String, List<String>> groupRowIdMap = RowIdUtil.groupRowIdByFileName(rowIds);
		for (Entry<String, List<String>> entry : groupRowIdMap.entrySet()) {
			String filePath = entry.getKey();
			List<String> rowIdList = entry.getValue();
			VTDNav vn = vnMap.get(filePath);
			Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + filePath);
			AutoPilot ap = new AutoPilot(vn);

			boolean isUpdate = false;
			try {
				XMLModifier xm = new XMLModifier(vn);
				for (String rowId : rowIdList) {
					ap.selectXPath(RowIdUtil.parseRowIdToXPath(rowId));
					if (ap.evalXPath() != -1) {
						int index = vn.getAttrVal("translate");
						if (index != -1 && "no".equalsIgnoreCase(vn.toString(index))) {
							xm.removeAttribute(index - 1);
							isUpdate = true;
						}
					}
				}
				if (isUpdate) {
					saveAndReparse(xm, filePath);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 返回当前 hander 所处理的文件
	 * @return
	 */
	public List<String> getFiles(){
		List<String> fileList = new ArrayList<String>();
		for(Entry<String, VTDNav> entry : vnMap.entrySet()){
			fileList.add(entry.getKey());
		}
		return fileList;
	}
	
	/**
	 * 获取分割文件的相关信息，以用于设置分割点	--robert	2013-10-17
	 * @return
	 */
	public Map<String, Integer> getSplitFileInfoForPointSetting(String filePath){
		Map<String, Integer> rowWordNumMap = new LinkedHashMap<String, Integer>();
		try {
			VTDNav vn = vnMap.get(filePath);
			Assert.isNotNull(vn, Messages.getString("file.XLFHandler.msg4") + filePath);
			AutoPilot ap = new AutoPilot(vn);

			ap.selectXPath(XPATH_ALL_TU + "/source");
			int curTuWordNum = -1;
			String srcText = null;
			String rowID = null;
			while(ap.evalXPath() != -1){
				vn.push();
				vn.toElement(VTDNav.PARENT);
				rowID = RowIdUtil.getRowId(vn, filePath);
				vn.pop();
				// 由于担心 tu 下面有多个 source 节点，但是 rowId 是唯一的，故，总字数以及总文本段全依 map 中的数据为准.
				if (rowWordNumMap.get(rowID) != null) {
					continue;
				}
				
				srcText = getTUPureText(vn);
				curTuWordNum = CountWord.wordCount(srcText, null);
				rowWordNumMap.put(rowID, curTuWordNum);
			}
			
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		
		return rowWordNumMap;
	}

}
