package net.heartsome.cat.ts.core.qa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import net.heartsome.cat.ts.core.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Shell;

/**
 * 文件分析的model类
 * @author robert	2011-12-07
 */
public class FAModel {
	/** 要进行分析的文件的集合 */
	private List<IFile> analysisIFileList = new ArrayList<IFile>();
	/** 存储文件分析的三个分析项（字数分析，翻译进度分析，编辑进度分析）的名称，类名的集合 */
	private Map<String, Map<String, String>> analysisItemMap = new HashMap<String, Map<String,String>>();
	private Shell shell;
	/** 要分析的文件的所有trans-unit节点的个数总和 */
	private int allTuSize;
	
	/** 是否锁定外部101％的匹配 */
	private boolean isLockExter101;
	/** 是否锁定外部100％的匹配 */
	private boolean isLockExter100;
	/** 是否锁定内部重复 */
	private boolean isLockInterRepeat;
	/** 标识当前所处理的文件是否是合并打开的文件 */
	private boolean isMultiFile;
	/** 如果当前所处理的文件是合并打开的，那这个就是保存这个合并打开的文件的 */
	private IFile multiTempIFile;
	/** 分析出错的文件 */
	private List<IFile> errorIFileList = new LinkedList<IFile>();
	/** 分析文件的总数 */
	private int subFileNum = 0;
	
	
	public FAModel(){
		//字数分析
		Map<String, String> analysisItem = new HashMap<String, String>();
		analysisItem.put(QAConstant.FA_ITEM_NAME, Messages.getString("qa.FAModel.WordsFA"));
		analysisItem.put(QAConstant.FA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.fileAnalysis.WordsFA");
		analysisItemMap.put(QAConstant.FA_WORDS_ANALYSIS, analysisItem);
		
		//翻译进度分析
		analysisItem = new HashMap<String, String>();
		analysisItem.put(QAConstant.FA_ITEM_NAME, Messages.getString("qa.FAModel.TranslationProgressFA"));
		analysisItem.put(QAConstant.FA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.fileAnalysis.TranslationProgressFA");
		analysisItemMap.put(QAConstant.FA_TRANSLATION_PROGRESS_ANALYSIS, analysisItem);
		
		//编辑进度分析
		analysisItem = new HashMap<String, String>();
		analysisItem.put(QAConstant.FA_ITEM_NAME, Messages.getString("qa.FAModel.EditProgressFA"));
		analysisItem.put(QAConstant.FA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.fileAnalysis.EditProgressFA");
		analysisItemMap.put(QAConstant.FA_EDITING_PROGRESS_ANALYSIS, analysisItem);
		
	}
	public List<IFile> getAnalysisIFileList() {
		return analysisIFileList;
	}
	public void setAnalysisIFileList(List<IFile> analysisIFileList) {
		this.analysisIFileList = analysisIFileList;
	}
	public Map<String, Map<String, String>> getAnalysisItemMap() {
		return analysisItemMap;
	}
	public Shell getShell() {
		return shell;
	}
	public void setShell(Shell shell) {
		this.shell = shell;
	}
	public int getAllTuSize() {
		return allTuSize;
	}
	public void setAllTuSize(int allTuSize) {
		this.allTuSize = allTuSize;
	}
	public boolean isLockExter101() {
		return isLockExter101;
	}
	public void setLockExter101(boolean isLockExter101) {
		this.isLockExter101 = isLockExter101;
	}
	public boolean isLockExter100() {
		return isLockExter100;
	}
	public void setLockExter100(boolean isLockExter100) {
		this.isLockExter100 = isLockExter100;
	}
	public boolean isLockInterRepeat() {
		return isLockInterRepeat;
	}
	public void setLockInterRepeat(boolean isLockInterRepeat) {
		this.isLockInterRepeat = isLockInterRepeat;
	}
	public boolean isMultiFile() {
		return isMultiFile;
	}
	public void setMultiFile(boolean isMultiFile) {
		this.isMultiFile = isMultiFile;
	}
	public IFile getMultiTempIFile() {
		return multiTempIFile;
	}
	public void setMultiTempIFile(IFile multiTempIFile) {
		this.multiTempIFile = multiTempIFile;
	}
	public List<IFile> getErrorIFileList() {
		return errorIFileList;
	}
	public void setErrorIFileList(List<IFile> errorIFileList) {
		this.errorIFileList = errorIFileList;
	}
	public int getSubFileNum() {
		return subFileNum;
	}
	public void setSubFileNum(int subFileNum) {
		this.subFileNum = subFileNum;
	}
	

}
