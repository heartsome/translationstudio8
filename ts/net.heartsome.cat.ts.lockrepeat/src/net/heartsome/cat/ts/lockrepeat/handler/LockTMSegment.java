/**
 * PreTranslation.java
 *
 * Version information :
 *
 * Date:Dec 13, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.lockrepeat.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.TranslationUnitAnalysisResult;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.lockrepeat.resource.Messages;
import net.heartsome.cat.ts.tm.bean.TransUnitInfo2TranslationBean;
import net.heartsome.cat.ts.tm.match.TmMatcher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 锁定重复之锁定外部重复
 * @author robert	2012-05-08
 * @version
 * @since JDK1.6
 */
public class LockTMSegment {
	public static final Logger logger = LoggerFactory.getLogger(LockTMSegment.class);

	private boolean isLockedContextMatch = true; // 是否需要锁定上下文匹配
	private boolean isLockedFullMatch = true; // 是否需要锁定完全匹配

	private XLFHandler xlfHandler; // 项目中的XLIFF文件解析
	private TmMatcher tmMatcher;
	private TransUnitInfo2TranslationBean tuInfoBean = null;
	/** 上下文个数 */
	private int contextSize;
	private List<String> xlfFiles; // 项目中的XLIFF文件路径,绝对路径
	private IProject curProject;

	private final String SRCLANG = "source-language";
	private final String TAGLANG = "target-language";
	private final String XPATH_ALL_TU = "/xliff/file/body/descendant::trans-unit[source/text()!='' or source/*]";

	private final String XP_FILE = "/xliff/file";
	private static final int CONSTANT＿ONE = 1;

	private Map<String, Integer> tuNumResult;
	private Map<String, Integer> lockedFullMatchResult;
	private Map<String, Integer> lockedContextResult;
	/** 要进行锁定的文本段的rowId */
	private Map<String, List<String>> needLockRowIdMap;
	
	public LockTMSegment(XLFHandler xlfHandler, TmMatcher tmMatcher, List<String> xlfFiles, IProject curProject) {
		this.xlfHandler = xlfHandler;
		this.xlfFiles = xlfFiles;
		this.tmMatcher = tmMatcher;
		this.curProject = curProject;

		lockedFullMatchResult = new HashMap<String, Integer>();
		lockedContextResult = new HashMap<String, Integer>();
		tuNumResult = new HashMap<String, Integer>();
		needLockRowIdMap = new HashMap<String, List<String>>();
		//只查询一个数据，并且最低匹配为100
		this.tmMatcher.setCustomeMatchParameters(1, 100);
		contextSize = tmMatcher.getContextSize();
	}

	/**
	 * 根据构建参数执行预翻译 ;
	 * @return false：标识用户点击退出按钮，不再执行
	 */
	public boolean executeTranslation(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		
		//首先获取所有文件的tu节点的总数
		int allTuSize = 0;
		for (String xlfPath : xlfFiles) {
			int curTuSize = xlfHandler.getNodeCount(xlfPath, XPATH_ALL_TU);
			//初始化结果集
			tuNumResult.put(xlfPath, curTuSize);
			if (lockedFullMatchResult.get(xlfPath) == null) {
				lockedFullMatchResult.put(xlfPath, 0);
			}
			if (lockedContextResult.get(xlfPath) == null) {
				lockedContextResult.put(xlfPath, 0);
			}
			if (needLockRowIdMap.get(xlfPath) == null) {
				needLockRowIdMap.put(xlfPath, new ArrayList<String>());
			}
			allTuSize += curTuSize;
		}
		
		monitor.beginTask(Messages.getString("translation.LockTMSegment.task1"), allTuSize);
		boolean canTmMatch = tmMatcher.checkTmMatcher(curProject);
		if (!canTmMatch) {
			monitorWork(monitor, allTuSize);
			return true;
		}
		
		Map<String, String> srcTextMap = null;
		for (String xlfPath : xlfFiles) {
			int fileNodeSum = xlfHandler.getNodeCount(xlfPath, XP_FILE);
			for (int fileNodeIdx = CONSTANT＿ONE; fileNodeIdx <= fileNodeSum; fileNodeIdx++) {
				String source_lan = xlfHandler.getNodeAttribute(xlfPath, "/xliff/file[" + fileNodeIdx + "]", SRCLANG);
				String target_lan = xlfHandler.getNodeAttribute(xlfPath, "/xliff/file[" + fileNodeIdx + "]", TAGLANG);
				if (source_lan == null || source_lan.equals("")) {
					continue;
				}
				if (target_lan == null || target_lan.equals("")) {
					continue;
				}
				//获取每一个tu节点的相关信息
				int curFileNodeTuSize = xlfHandler.getNodeCount(xlfPath, "/xliff/file[" + fileNodeIdx
						+ "]/body/descendant::trans-unit[source/text()!='' or source/*]");
				for (int tuNodeIdx = CONSTANT＿ONE; tuNodeIdx <= curFileNodeTuSize; tuNodeIdx++) {
					String tuXpath = "/xliff/file[" + fileNodeIdx + "]/descendant::trans-unit[" + tuNodeIdx + "]";
					srcTextMap = xlfHandler.getTUsrcText(xlfPath, tuXpath, contextSize);
					if (srcTextMap == null || srcTextMap.size() < 0 ) {
						return true;
					}
					searchTmAndLockTu(xlfPath, source_lan, target_lan, srcTextMap);
					if (!monitorWork(monitor, allTuSize)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * 查询数据库的匹配并且锁定文本段
	 * @param xlfPath
	 * @param source_lan
	 * @param target_lan
	 * @param srcTextMap
	 */
	private void searchTmAndLockTu(String xlfPath, String source_lan, String target_lan, Map<String, String> srcTextMap) {
		tuInfoBean = new TransUnitInfo2TranslationBean();
		String srcContent = srcTextMap.get("content");
		if (srcContent == null || "".equals(srcContent)) {
			return;
		}
		tuInfoBean.setNextContext(srcTextMap.get("nextHash"));
		tuInfoBean.setPreContext(srcTextMap.get("preHash"));
		tuInfoBean.setSrcFullText(srcContent);
		tuInfoBean.setSrcLanguage(source_lan);
		tuInfoBean.setSrcPureText(srcTextMap.get("pureText"));
		tuInfoBean.setTgtLangugage(target_lan);
		
		int a = 1;
		List<TranslationUnitAnalysisResult> tmResult = tmMatcher.analysTranslationUnit(curProject, tuInfoBean);
		if (tmResult != null && tmResult.size() > 0) {
			int similarity = tmResult.get(0).getSimilarity();
			if (isLockedContextMatch && similarity == 101) {
				xlfHandler.lockTransUnit(xlfPath, "no");
				Integer lockedNum = lockedContextResult.get(xlfPath);
				if (lockedNum == null) {
					lockedContextResult.put(xlfPath, 1);
				} else {
					lockedContextResult.put(xlfPath, lockedNum + 1);
				}
				needLockRowIdMap.get(xlfPath).add(srcTextMap.get("rowId"));
			} else if (isLockedFullMatch && similarity == 100) {
				xlfHandler.lockTransUnit(xlfPath, "no");
				Integer lockedNum = lockedFullMatchResult.get(xlfPath);
				if (lockedNum == null) {
					lockedFullMatchResult.put(xlfPath, 1);
				} else {
					lockedFullMatchResult.put(xlfPath, lockedNum + 1);
				}
				needLockRowIdMap.get(xlfPath).add(srcTextMap.get("rowId"));
			}
			a ++;
		}
	}

	/**
	 * 设置是否锁定上下文匹配,即101%匹配
	 * @param isLockedContextMatch
	 *            the isLocaledContextMatch to set
	 */
	public void setLockedContextMatch(boolean isLockedContextMatch) {
		this.isLockedContextMatch = isLockedContextMatch;
	}

	/**
	 * 设置是否锁定完全匹配,即100%匹配
	 * @param isLockedFullMatch
	 *            the isLocaledFullMatch to set
	 */
	public void setLockedFullMatch(boolean isLockedFullMatch) {
		this.isLockedFullMatch = isLockedFullMatch;
	}

	/** @return the lockedFullMatchResult */
	public Map<String, Integer> getLockedFullMatchResult() {
		return lockedFullMatchResult;
	}

	/** @return the lockedContextResult */
	public Map<String, Integer> getLockedContextResult() {
		return lockedContextResult;
	}

	/** @return the tuNumResult */
	public Map<String, Integer> getTuNumResult() {
		return tuNumResult;
	}

	public Map<String, List<String>> getNeedLockRowIdMap() {
		return needLockRowIdMap;
	}

	public boolean monitorWork(IProgressMonitor monitor, int stepValue){
		if (monitor.isCanceled()) {
			return false;
		}
		monitor.worked(stepValue);
		return true;
	}
}
