package net.heartsome.cat.ts.ui.qa.fileAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.TranslationUnitAnalysisResult;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.tm.MatchQuality;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.core.qa.CountWord;
import net.heartsome.cat.ts.core.qa.FAModel;
import net.heartsome.cat.ts.core.qa.FileAnalysis;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.core.qa.WordsFABean;
import net.heartsome.cat.ts.tm.bean.TransUnitInfo2TranslationBean;
import net.heartsome.cat.ts.tm.match.TmMatcher;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.model.WordsFAResult;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字数分析，包括统计内部重复，内部匹配，外部重复，外部匹配，以及新字数的字数统计， 结果展示为字数统计，文本段统计。 monitor进度提示语句： 字数分析: 获取文件{0}所有源文本 ... ...
 * 备注：所分析的文件必须在一个项目内
 * @author robert 2011-12-08
 * 
 * 备注-robert	,此处还待优化，优化内容为重新写一个查询数据库的方法，待jason优化findmatch方法之后，优化的方面是，只查询我所需要的东西	2012-01-04
 */
public class WordsFA extends FileAnalysis {
	private FAModel model;
	private QAXmlHandler handler;
	/** 是否忽略标记 */
	private boolean ignoreTag = false;
	/** 是否忽略大小写，从记忆库首选项中获取 */
	private boolean ignoreCase = false;
	/** 罚分制度 */
	private int tagPenalty;
	/** 是否检查内部重复 */
	private boolean interRepeat;
	/** 是否检查内部匹配 */
	private boolean interMatch;
	
	/** 这是进度条的前进间隔，也就是当循环多少个trans-unit节点后前进一格，针对匹配 */
	private int workInterval = 1;
	/** 这是进度条的前进间隔，也就是当循环多少个trans-unit节点后前进一格，针对获取源文本 */
	private int getSrcTextWorkInterval = workInterval * 100;
	/**
	 * 存储所选文件的所有trans-unit节点的源文本 Map<文件名, Map<trans-unit唯一标识符,
	 * Map<source节点的内容或纯文本(QAConstant.FA_SRC_CONTENT/QAConstant.FA_SRC_PURE_TEXT), 值>>>
	 */
	private Map<String, Map<String, WordsFABean>> allSrcTextsMap = new HashMap<String, Map<String, WordsFABean>>();
	/** 新字数的最大匹配率，从首选项的记忆库设置中获取，也就是匹配率低于这个值就是新字数 */
	private int newWordsMaxMatchRate;
	/** 一个存放每个文件分析结果的集合，key值为一个文件的绝对路径(IFile.getLocation.toOSString) */
	private Map<String, WordsFAResult> WordsFAResultMap;
	/** 所有包括分析文件的容器，但不包括项目 */
	private List<IContainer> allFolderList;
	
	/** 上下文的个数，从首选项中获取(记忆库) */
	private int contextSum = 0;
	
	private IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
	/** 加权系数字符串，如internalRepeat:0.50;external101:0.50;external100:0.50;95-99:0.60; */
	private String equivStr;
	private TmMatcher tmMatcher = new TmMatcher();
	private TransUnitInfo2TranslationBean tuInfoBean;
	private IProject curProject;
	private List<String> needLockRowIdList = new ArrayList<String>();
	/** 保存内部重复的数据，以便锁定内部重复 */
	private XLIFFEditorImplWithNatTable nattble;
	private boolean isOpened;
	public final static Logger logger = LoggerFactory.getLogger(WordsFA.class.getName());

	@Override
	public int beginAnalysis(FAModel model, IProgressMonitor monitor, QAXmlHandler handler) {
		this.model = model;
		this.handler = handler;
		super.setModel(model);
		ignoreTag = preferenceStore.getBoolean(QAConstant.FA_PREF_ignoreTag);
		ignoreCase = preferenceStore.getBoolean(QAConstant.FA_PREF_ignoreCase);
		contextSum = preferenceStore.getInt(QAConstant.FA_PREF_contextNum);
		equivStr = preferenceStore.getString(QAConstant.FA_PREF_equivalent);
		tagPenalty = preferenceStore.getInt(QAConstant.FA_PREF_tagPenalty);
		initNewWordsMaxMatchRateFromWeightPage();
		
		interRepeat = preferenceStore.getBoolean(QAConstant.FA_PREF_interRepeate);
		interMatch = preferenceStore.getBoolean(QAConstant.FA_PREF_interMatch);
		
		// 备注，这里的是否忽略大小写，传到记忆库匹配时是必须变成它的相反值的，因为记忆库里的参数为是否区分大小写。
		tmMatcher.setCustomeMatchParameters(1, ignoreTag, newWordsMaxMatchRate, !ignoreCase, contextSum, tagPenalty);
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (model.getAnalysisIFileList().size() <= 0) {
			return QAConstant.QA_ZERO;
		}

		// 字数分析的进度条分为两大块，1：获取项目下所有源文本，共花一格，2：分析文件，共花四格
		int allTUSize = model.getAllTuSize();
		curProject = model.getAnalysisIFileList().get(0).getProject();
		// UNDO 所有的这里都要对进度条进行处理。
		if (allTUSize > 500) {
			workInterval = allTUSize / 500;
		}

		int matchWorkUnit = allTUSize % workInterval == 0 ? (allTUSize / workInterval) : (allTUSize / workInterval) + 1;

		monitor.beginTask("", matchWorkUnit);

		// 开始进行内部与外部匹配
		WordsFAResultMap = matching(monitor);
		tmMatcher.clearResources(); // 关闭数据库，释放资源
		
		if (WordsFAResultMap == null) {
			return QAConstant.QA_ZERO;
		}

		// 输出结果
		printWordsFAReslut();

		return QAConstant.QA_FIRST;

	}
	
	/**
	 * 根据加权字数字符串获取最低匹配率
	 * @return
	 */
	private void initNewWordsMaxMatchRateFromWeightPage(){
		int minRate = 100;
		//internalRepeat:0.50;external101:0.50;external100:0.50;95-99:0.60;
		String[] equivArray = equivStr.split(";");
		for(String singleEquiv : equivArray){
			if (singleEquiv.indexOf("internalRepeat") != -1 || singleEquiv.indexOf("external101") != -1
					|| singleEquiv.indexOf("external100") != -1) {
				continue;
			}
			String matchPair = singleEquiv.substring(0, singleEquiv.indexOf(":") );
			String minRateStr = matchPair.split("-")[0];
			if (minRateStr != null && minRateStr.matches("\\d{0,2}")) {
				if (minRate > Integer.parseInt(minRateStr)) {
					minRate = Integer.parseInt(minRateStr);
				}
			}
		}
		newWordsMaxMatchRate = minRate;
		if (newWordsMaxMatchRate <=0 ) {
			newWordsMaxMatchRate = 1;
		}
	}
	
	/**
	 * 获取要分析的所有xliff文件的所有源文本 其值为一个键值对，rowId --> 该节点的源文本
	 * @return : Map<文件名, Map<trans-unit唯一标识符,
	 *         Map<source节点的内容或纯文本(QAConstant.FA_SRC_CONTENT/QAConstant.FA_SRC_PURE_TEXT), 值>>> 如果返回为null,则标志退出程序操作
	 */
	public Map<String, Map<String, WordsFABean>> getAllXlfSrcTexts(String srcLang, String tgtLang) {
		return handler.getAllSrcText(getSrcTextWorkInterval, ignoreTag, contextSum, srcLang, tgtLang);
	}

	/**
	 * 开始处理匹配操作 如果返回null，则是用户点击退出按钮，执行退出操作
	 */
	public Map<String, WordsFAResult> matching(IProgressMonitor monitor) {
		int matchTravelTuIndex = 0;
		// 字数统计的结果集合
		Map<String, WordsFAResult> wordsFAResultMap = new LinkedHashMap<String, WordsFAResult>();
		Map<String, ArrayList<String>> languages = handler.getLanguages();
		//先对每个文件存放一个结果集
		for(IFile iFile : model.getAnalysisIFileList()){
			String filePath = iFile.getLocation().toOSString();
			wordsFAResultMap.put(filePath, new WordsFAResult());
		}
		WordsFAResult wordFaResult;	//针对每一个文件的结果集
		
		for (Entry<String, ArrayList<String>> langEntry : languages.entrySet()) {
			String srcLanguage = langEntry.getKey();
			for (String tgtLanguage : langEntry.getValue()) {
				//针对每个文件，每种语言对获取其内容
				allSrcTextsMap = getAllXlfSrcTexts(srcLanguage.toUpperCase(), tgtLanguage.toUpperCase());
				// 如果返回的值为空，则标志用户点击了退出操作，那么退出程序
				if (allSrcTextsMap == null) {
					continue;
				}
				WordsFABean bean;
				List<TranslationUnitAnalysisResult> exterMatchResult = null;
				// 字数统计的结果集合
				for(Entry<String, Map<String, WordsFABean>> textEntry : allSrcTextsMap.entrySet()){
					String filePath = textEntry.getKey();
					IFile iFile = ResourceUtils.fileToIFile(filePath);
					// 存储匹配结果的pojo类
					wordFaResult = wordsFAResultMap.get(filePath);
					
					Map<String, WordsFABean> fileSrcTextMap = textEntry.getValue();

					monitor.setTaskName(MessageFormat.format(Messages.getString("qa.fileAnalysis.WordsFA.tip1"), iFile
							.getFullPath().toOSString()));

					Iterator<Entry<String, WordsFABean>> it = fileSrcTextMap.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, WordsFABean> entry = (Entry<String, WordsFABean>) it.next();
						matchTravelTuIndex++;

						String rowId = entry.getKey();
						bean = entry.getValue();

						String srcPureText = bean.getSrcPureText();
						int textLength = bean.getSrcLength();
						String preTextHash = bean.getPreHash();
						String nextTextHash = bean.getNextHash();
						boolean isLocked = bean.isLocked();
						String tagStr = bean.getTagStr();

						int wordsCount = CountWord.wordCount(srcPureText, srcLanguage);
						
						//若处于锁定状态，则添加到已锁定字数，然后跳出，执行下一文本段
						if (isLocked) {
							wordFaResult.setLockedPara(QAConstant.QA_FIRST);
							wordFaResult.setLockedWords(wordsCount);
							//删除该文本段，不再进行比较
							fileSrcTextMap.remove(rowId);
							it = fileSrcTextMap.entrySet().iterator();
							continue;
						}
						
						// UNDO 应先判断数据库是否可用。
						// 第一步，进行外部匹配，先封装参数。
						tuInfoBean = new TransUnitInfo2TranslationBean();
						tuInfoBean.setNextContext(bean.getNextHash());
						tuInfoBean.setPreContext(bean.getPreHash());
						tuInfoBean.setSrcFullText(bean.getSrcContent());
						tuInfoBean.setSrcLanguage(srcLanguage);
						tuInfoBean.setSrcPureText(bean.getSrcPureText());
						tuInfoBean.setTgtLangugage(tgtLanguage);
						
						exterMatchResult = tmMatcher.analysTranslationUnit(curProject, tuInfoBean);
						int exterMatchRate = 0;
						if (exterMatchResult != null && exterMatchResult.size() > 0) {
							exterMatchRate = exterMatchResult.get(0).getSimilarity();
						}
						
						if (exterMatchRate == 100) {
							// 如果锁定外部　100%　匹配，那么这些字数将被添加到锁定字数，而非外部 100% 匹配，外部101% 一样
							if (model.isLockExter100()) {
								wordFaResult.setLockedPara(QAConstant.QA_FIRST);
								wordFaResult.setLockedWords(wordsCount);
								needLockRowIdList.add(rowId);
							}else {
								wordFaResult.setExterRepeatPara(QAConstant.QA_FIRST);
								wordFaResult.setExterMatchWords(wordsCount);
								wordFaResult.setAllExterMatchWords(exterMatchRate, wordsCount);
							}
							//删除该文本段，不再进行比较
							fileSrcTextMap.remove(rowId);
							it = fileSrcTextMap.entrySet().iterator();
							if (!handler.monitorWork(monitor, matchTravelTuIndex, workInterval, false)) {
								return null;
							}
							continue;
						}
						
						if (exterMatchRate == 101) {
							if (model.isLockExter101()) {
								wordFaResult.setLockedPara(QAConstant.QA_FIRST);
								wordFaResult.setLockedWords(wordsCount);
								needLockRowIdList.add(rowId);
							}else {
								wordFaResult.setExterRepeatPara(QAConstant.QA_FIRST);
								wordFaResult.setExterMatchWords(wordsCount);
								wordFaResult.setAllExterMatchWords(exterMatchRate, wordsCount);
							}
							//删除该文本段，不再进行比较
							fileSrcTextMap.remove(rowId);
							it = fileSrcTextMap.entrySet().iterator();
							
							if (!handler.monitorWork(monitor, matchTravelTuIndex, workInterval, false)) {
								return null;
							}
							continue;
						}
						
						// 第二步，进行内部匹配
						int inteMatchRate = 0;
						// 如果要检查内部重复，那么就查找内部匹配
						if (interRepeat) {
							// 如果不进行内部模糊匹配，那么直接
							int interNewWordsMaxMatchRate = interMatch ? newWordsMaxMatchRate : 100;
							internalMatching(rowId, srcPureText, tagStr, textLength, preTextHash, nextTextHash, interNewWordsMaxMatchRate);
						}
						
						inteMatchRate = bean.getThisMatchRate() > inteMatchRate ? bean.getThisMatchRate() : inteMatchRate;
						int maxMacthRate = exterMatchRate > inteMatchRate ? exterMatchRate : inteMatchRate;
						
						if (inteMatchRate == 100 || inteMatchRate == 101) {
							if (model.isLockInterRepeat()) {
								wordFaResult.setLockedPara(QAConstant.QA_FIRST);
								wordFaResult.setLockedWords(wordsCount);
								needLockRowIdList.add(rowId);
							}else {
								wordFaResult.setInterRepeatPara(QAConstant.QA_FIRST);
								wordFaResult.setInterMatchWords(wordsCount);
								wordFaResult.setAllInterMatchWords(inteMatchRate, wordsCount);
							}
						}else if (maxMacthRate < newWordsMaxMatchRate ) {
							//最大匹配小于最小匹配时，就为新字数
							wordFaResult.setNewPara(QAConstant.QA_FIRST);
							wordFaResult.setNewWords(wordsCount);
						}else {
							if (inteMatchRate > exterMatchRate) {
								// 内部匹配
								wordFaResult.setInterMatchPara(QAConstant.QA_FIRST);
								wordFaResult.setInterMatchWords(wordsCount);
								wordFaResult.setAllInterMatchWords(inteMatchRate, wordsCount);
							}else {	//外部匹配
								wordFaResult.setExterMatchPara(QAConstant.QA_FIRST);
								wordFaResult.setExterMatchWords(wordsCount);
								wordFaResult.setAllExterMatchWords(exterMatchRate, wordsCount);
							}
						}
						//删除该文本段，不再进行比较
						fileSrcTextMap.remove(rowId);
						it = fileSrcTextMap.entrySet().iterator();
						
						if (!handler.monitorWork(monitor, matchTravelTuIndex, workInterval, false)) {
							return null;
						}
					}
					
					wordsFAResultMap.put(filePath, wordFaResult);
				}
				
				if (!handler.monitorWork(monitor, matchTravelTuIndex, workInterval, false)) {
					return null;
				}
			}
		}
		lockRepeatTU(wordsFAResultMap);
		return wordsFAResultMap;
	}

	

	
	/**
	 * 内部匹配 备注，是将一个source节点拿去跟所有的source节点进行匹配，并称这个source节点为比较者，其他所有的source节点为被比较者
	 * 关于上下文匹配，在获取分割xliff文件时就没有必要去做了，故在获取加权系数时，传入两个空值即可
	 * @param rowId
	 *            : 被比较者的trans-unit节点的唯一标识符
	 * @param srcContent
	 *            : 被比较者的trans-unit节点的source子节点的完整内容(包括标记)
	 * @param srcPureText
	 *            : 被比较者的trans-unit节点的source子节点的纯文本
	 * @param preTextHash
	 *            上文的hash值
	 * @param nextTexthash
	 *            下文的hash值
	 */
	public int internalMatching(String rowId, String srcPureText, String tagStr, int textLength,
			String preTextHash, String nextTexthash, int interNewWordsMaxMatchRate) {
		int matchRate = 0; // 匹配率
		// System.out.println("长度为="+srcContent.length());
		//System.out.println("rowId = " + rowId);
		//System.out.println("ignoreTag = " + ignoreTag);
		
		int fileSize = model.getAnalysisIFileList().size();
		List<IFile> fileList = model.getAnalysisIFileList();
		
		for (int fileIndex = 0; fileIndex < fileSize; fileIndex++) {
			IFile iFile = fileList.get(fileIndex);
			String filePath = iFile.getLocation().toOSString();
			Map<String, WordsFABean> fileSrcTextMap = allSrcTextsMap.get(filePath);

			Iterator<Entry<String, WordsFABean>> it = fileSrcTextMap.entrySet().iterator();
			WordsFABean curBean;
			while (it.hasNext()) {
				Entry<String, WordsFABean> entry = (Entry<String, WordsFABean>) it.next();
				String curRowId = entry.getKey();

				// 比较者不与自己进行比较
				if (rowId.equals(curRowId)) {
					continue;
				}

				curBean = entry.getValue();
				String curSrcPureText = curBean.getSrcPureText();

				if (!checkIsideal(ignoreCase ? srcPureText.toLowerCase() : srcPureText, 
						ignoreCase ? curSrcPureText.toLowerCase() : curSrcPureText, interNewWordsMaxMatchRate)) { 
					continue;
				}

				// long time2 = System.currentTimeMillis();
				int curMatchRate = 0;
				curMatchRate = MatchQuality.similarity(ignoreCase ? srcPureText.toLowerCase() : srcPureText, 
						ignoreCase ? curSrcPureText.toLowerCase() : curSrcPureText);
				if (!ignoreTag) {
					String curTagStr = curBean.getTagStr();
					if (!curTagStr.equals(tagStr)) {
						curMatchRate -= tagPenalty;
					}
				}

				if (curMatchRate > matchRate) {
					matchRate = curMatchRate;
				}
				
				//如果当前文本段的匹配率小于本次所比较的匹配率，则重新刷新匹配率
				curBean.setThisMatchRate(curMatchRate);
				
				// System.out.println("比较时＝ " + (System.currentTimeMillis() - time2));
			}
		}
		
		return matchRate;
	}
	

	/**
	 * 执行锁定重复文本段
	 */
	private void lockRepeatTU(Map<String, WordsFAResult> wordsFAResultMap) {
		final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
		// 首先，判断当前所处理的文件是否合并打开
		if (model.isMultiFile()) {
			if (needLockRowIdList.size() <= 0) {
				return;
			}
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IEditorInput input = new FileEditorInput(model.getMultiTempIFile());
					IEditorReference[] reference = window.getActivePage().findEditors(input,XLIFF_EDITOR_ID,
									IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
					nattble = (XLIFFEditorImplWithNatTable) reference[0].getEditor(true);
					XLFHandler thisHandler = nattble.getXLFHandler();
					thisHandler.lockFaTU(needLockRowIdList);
					nattble.redraw();
				}
			});
		}else {
			//针对已经打开的文件进行锁定
			if (needLockRowIdList.size() <= 0) {
				return;
			}
			
			final Map<String, List<String>> rowIdMap = RowIdUtil.groupRowIdByFileName(needLockRowIdList);
			for(final IFile iFile : model.getAnalysisIFileList() ){
				isOpened = false;
				final String filePath = iFile.getLocation().toOSString();
				
				if (rowIdMap.get(filePath) == null || rowIdMap.get(filePath).size() <= 0) {
					continue;
				}
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						IWorkbenchWindow window = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow();
						IEditorInput input = new FileEditorInput(iFile);
						IEditorReference[] reference = window.getActivePage()
								.findEditors(input,XLIFF_EDITOR_ID,
								IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
						if (reference.length > 0) {
							isOpened = true;
							nattble = (XLIFFEditorImplWithNatTable) reference[0]
									.getEditor(true);
							XLFHandler thisHandler = nattble.getXLFHandler();
							thisHandler.lockFaTU(rowIdMap.get(filePath));
							nattble.redraw();
						}
					}
				});
				if (!isOpened) {
					for(String rowId : rowIdMap.get(filePath)){
						handler.lockedTU(rowId);
					}
				}
			}
		}
	}
	
	/**
	 * 检查两个句子是否理想匹配，如果不是理想匹配，返回 false
	 * @return
	 */
	private static boolean checkIsideal(String x, String y, int newMatch){
		// 求匹配的算法，必须要去掉首尾的空格。
		x = x.trim();
		y = y.trim();
		// x 换成短句，y变成长句
		String temp = "";
		if (x.length() > y.length()) {
			temp = x;
			x = y;
			y = temp;
		}
		int maxLength = y.length();
		if (maxLength == 0) {
			return false;
		}
		if ((100 * x.length() / maxLength) < newMatch) {
			return false;
		}
		if (newMatch >= 100) {
			if (!x.equals(y)) {
				return false;
			}
		}
		// 如果最大长度小于 10 ，那么 MatchQuality 类的所耗时间并不长，不需要求 字符相似度
//		if (maxLength < 10) {
//			return true;
//		}
		
		// 下面是切段理想匹配
		Map<Character, Integer> xMap = new HashMap<Character, Integer>();
		for(char _char : x.toCharArray()){
			if (xMap.containsKey(_char)) {
				xMap.put(_char, xMap.get(_char) + 1);
			}else {
				xMap.put(_char, 1);
			}
		}
		
		int repeatSum = 0;
		for (char _char : y.toCharArray()) {
			if (xMap.containsKey(_char)) {
				if (xMap.get(_char) <= 1) {
					xMap.remove(_char);
				}else {
					xMap.put(_char, xMap.get(_char) - 1);
				}
				repeatSum ++;
			}
		}
		int idealMath = 100*repeatSum / maxLength;
		if (idealMath < newMatch) {
			return false;
		}
		return true;
	}

	
	
//-------------------------------------------------------------------------------------------//
//---------------------------------下面的代码主要是报表部份---------------------------------------//
//-------------------------------------------------------------------------------------------//
	
	/**
	 * 输出字数统计结果到结果窗体中
	 * @param WordsFAResultMap
	 */
	public void printWordsFAReslut() {
		String htmlPath = createFAResultHtml();
		try {
			model.getAnalysisIFileList().get(0).getProject().getFolder("Intermediate").getFolder("Report").refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		
		final FileEditorInput input = new FileEditorInput(ResourceUtils.fileToIFile(htmlPath));
		if (PlatformUI.getWorkbench().isClosing()) {
			return;
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, QAConstant.FA_HtmlBrowserEditor, true);
				} catch (PartInitException e) {
					logger.error(Messages.getString("qa.fileAnalysis.WordsFA.log5"), e);
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 生成字数分析结果html文件
	 * @return html文件的路径
	 */
	public String createFAResultHtml() {
		allFolderList = new LinkedList<IContainer>();
		Date createDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String createTime = formatter.format(createDate);
		formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String htmlNameTime = formatter.format(createDate);

		IProject curProject = model.getAnalysisIFileList().get(0).getProject();
		String htmlName = "WordLog" + htmlNameTime + ".html";
		String htmlPath = curProject.getLocation().append("Intermediate").append("Report").append(htmlName).toOSString();
		
		File htmlFile = new File(htmlPath);
		if (!htmlFile.getParentFile().exists()) {
			htmlFile.getParentFile().mkdirs();
		}

		FileOutputStream output;
		try {
			output = new FileOutputStream(htmlPath);
			output.write(QAConstant.FA_HtmlDoctype.getBytes("UTF-8"));
			output.write("<html>\n".getBytes("UTF-8"));

			String headerNode = QAConstant.FA_htmlHeader;
			headerNode = headerNode.replace("###Title###", Messages.getString("qa.fileAnalysis.WordsFA.name1"));

			output.write(headerNode.getBytes("UTF-8"));
			output.write("\t<body>\n".getBytes("UTF-8"));
			output.write(("<p class=\"title\">"+Messages.getString("qa.all.fa.WordsFA")+"</p>").getBytes("UTF-8"));
			
			// ----------------<<<<<<start-- 下面是相关信息提示部份---------------------
			output.write("\t<div>\n".getBytes("UTF-8"));
			output.write("\t\t<table class=\"infoTableStyle\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n".getBytes("UTF-8"));
			
			// 记忆库
			String title = Messages.getString("qa.fa.info.tm");
			ProjectConfiger projectConfiger = ProjectConfigerFactory.getProjectConfiger(curProject);
			List<DatabaseModelBean> tmBeanList = projectConfiger.getAllTmDbs();
			StringBuffer tmInfoSB = new StringBuffer();
			if (tmBeanList.size() > 0) {
				for(DatabaseModelBean bean : tmBeanList){
					tmInfoSB.append("\t\t\t<div style=\"margin-bottom: 2px;\">");
					String dbType = bean.getDbType();
					if("MySQL 5.x".equals(dbType)){
						dbType = "MySQL";
					} else if ("MsSQL2005".equals(dbType)){
						dbType = "SQL Server";
					}
					
					tmInfoSB.append(dbType);
					String location = "";
					if ("Internal DB".equals(dbType) || "SQLite".equals(dbType)) {
						location = bean.getItlDBLocation();
					}else {
						location = bean.getHost() + ":" + bean.getPort();
						if (":".equals(location)) {
							location = "";
						}
					}
					if (location != null && !"".equals(location.trim()) && !" : ".equals(location)) {
						tmInfoSB.append(" ( " + location + " ) ");
					}
					tmInfoSB.append(": ");
					tmInfoSB.append(MessageFormat.format("{0}" + bean.getDbName() + "{1}", 
							new Object[]{bean.isDefault() ? "<b>" : "", bean.isDefault() ? "</b>" : ""}));
					tmInfoSB.append("</div>\n");
				}
			}else {
				tmInfoSB.append("\t\t\t<div style=\"margin-bottom: 2px;\">");
				tmInfoSB.append("N/A");
				tmInfoSB.append("</div>\n");
			}
			
			String infoStr = QAConstant.FA_Report_Info.replace("###Title###", title).replace("###Content###", tmInfoSB.toString());
			output.write(infoStr.getBytes("UTF-8"));
			
			// 最底匹配率
			title = Messages.getString("qa.fa.info.newWordMatch");
			String content = newWordsMaxMatchRate + "%";
			infoStr = QAConstant.FA_Report_Info.replace("###Title###", title).replace("###Content###", content);
			output.write(infoStr.getBytes("UTF-8"));
			
			// 分析文件总数
			title = Messages.getString("qa.fa.info.fileSum");
			content = "" + model.getSubFileNum();
			infoStr = QAConstant.FA_Report_Info.replace("###Title###", title).replace("###Content###", content);
			output.write(infoStr.getBytes("UTF-8"));
			
			// 分析失败文件
			title = Messages.getString("qa.fa.info.errorFiles");
			StringBuffer errorFileSB = new StringBuffer();
			errorFileSB.append("\t\t\t<div style=\"margin-bottom: 2px;\">");
			errorFileSB.append(model.getErrorIFileList().size());
			errorFileSB.append("</div>");
			for(IFile iFile : model.getErrorIFileList()){
				errorFileSB.append("\t\t\t<div style=\"margin-bottom: 2px;\">");
				errorFileSB.append(iFile.getFullPath().toOSString());
				errorFileSB.append("</div>");
			}
			infoStr = QAConstant.FA_Report_Info.replace("###Title###", title).replace("###Content###", errorFileSB.toString());
			output.write(infoStr.getBytes("UTF-8"));
			
			// 报告生成时间
			title = Messages.getString("qa.fa.info.createTime");
			infoStr = QAConstant.FA_Report_Info.replace("###Title###", title).replace("###Content###", createTime);
			output.write(infoStr.getBytes("UTF-8"));
			output.write("</table></div><br>\n".getBytes("UTF-8"));
			// ---------------->>>>>>end-- 报表信息提示部分结束---------------------
			
			
			int paddLeft = 6;
			String folderId = curProject.getFullPath().toOSString();
			getAllFolder(curProject, allFolderList);
			// 向所有的文件夹传值
			setDataToFolder(curProject);
			// 首先写下项目
			WordsFAResult proFaResult = WordsFAResultMap.get(curProject.getLocation().toOSString());
			
			// －－－－－－－－－－－－－－数据库匹配率区间---------------------
			output.write(("\t\t<div class=\"legendStyle\"><b>"
					+ Messages.getString("qa.fileAnalysis.WordsFA.wordsFA") + "</b></div>").getBytes("UTF-8"));
			StringBuffer exterFAdata = new StringBuffer();

			//"100-101:0.5;89-99:0.4"
			String[] equivArray = equivStr.split(";");
			
			int length = equivArray.length;
			// 是否有 上下文匹配
			boolean hasExter101 = equivStr.indexOf("external101") != -1;
			int interMathNum = 0;
			if (interRepeat && !interMatch) {
				interMathNum = 1;
			}else if(interRepeat && interMatch) {
				interMathNum = length - (hasExter101 ? 2 : 1);
			}
			int columLength = length - 1 + interMathNum + 3 + 4;	//要显示列的总数（文件列是普通列的三倍）
			float width = (float)100 / columLength;
			
			//开始创建表头
			exterFAdata.append("\t<table class='tableStyle' cellpadding='0' cellspacing='1'> \n");
			// 表头
			exterFAdata.append("\t\t<tr>\n");
			exterFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='" + 3 * width + "%'>"
					+ Messages.getString("qa.all.fa.fileName") + "</td>\n");
			exterFAdata.append("\t\t\t<td class='headerTd' colSpan='" + (length - 1) + "' width='" + (length - 1)
					* width + "%'>" + Messages.getString("qa.fileAnalysis.WordsFA.exterMatch") + "</td>\n");
			if (interRepeat) {
				exterFAdata.append("\t\t\t<td class='headerTd' colSpan='" + interMathNum + "' width='" + interMathNum
						* width + "%'>" + Messages.getString("qa.fileAnalysis.WordsFA.interMatch") + "</td>\n");
			}
			exterFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='" + width + "%'>"
					+ Messages.getString("qa.fileAnalysis.WordsFA.newWordsNum") + "</td>\n");
			exterFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='" + width + "%'>"
					+ Messages.getString("qa.fileAnalysis.WordsFA.equivWordsNum") + "</td>\n");
			exterFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='" + width + "%'>"
					+ Messages.getString("qa.fileAnalysis.WordsFA.lockWordsNum") + "</td>\n");
			exterFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='" + width + "%'>"
					+ Messages.getString("qa.fileAnalysis.WordsFA.wordsSum") + "</td>\n");
			exterFAdata.append("\t\t</tr>\n");
			
			exterFAdata.append("\t\t<tr>\n");
			
			//这是创建显示结果的外部匹配部份
			for (int i = 0; i < length; i++) {
				String matchPair = equivArray[i].substring(0, equivArray[i].indexOf(":") );
				String className = "headerTd";
				if (QAConstant._External101.equals(matchPair)) {
					exterFAdata.append("\t\t\t<td class='" + className + "' width='" + width + "%'>"
							+ Messages.getString("qa.fileAnalysis.WordsFA.contentMath") + "</td>\n");
				}else if (QAConstant._External100.equals(matchPair)) {
					exterFAdata.append("\t\t\t<td class='" + className + "' width='"+width+"%'>"+ "100%" +"</td>\n");
				}else if (!QAConstant._InternalRepeat.equals(matchPair)) {
					matchPair = matchPair.substring(0, matchPair.indexOf("-")) + "%" + matchPair.substring(matchPair.indexOf("-"), matchPair.length()) + "%";
					exterFAdata.append("\t\t\t<td class='" + className + "' width='"+width+"%'>"+ matchPair +"</td>\n");
				}
			}
			//这是创建显示结果的内部匹配部份
			for (int i = 0; i < length; i++) {
				String matchPair = equivArray[i].substring(0, equivArray[i].indexOf(":") );
				String className = "headerTd";
				if (interRepeat && QAConstant._InternalRepeat.equals(matchPair)) {
					exterFAdata.append("\t\t\t<td class='" + className + "' width='" + width + "%'>"
							+ Messages.getString("qa.fileAnalysis.WordsFA.contentRepeat") + "</td>\n");
				}else if (interMatch && matchPair.indexOf("external") == -1) {
					matchPair = matchPair.substring(0, matchPair.indexOf("-")) + "%" + matchPair.substring(matchPair.indexOf("-"), matchPair.length()) + "%";
					exterFAdata.append("\t\t\t<td class='" + className + "' width='"+width+"%'>"+ matchPair +"</td>\n");
				}
			}
			exterFAdata.append("\t\t</tr>\n");
			
			// 首先写下项目
			exterFAdata.append("<tr onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n" +
					"<td class='folderTd' style='padding-left: 6'><a href='javascript:void(0)' id='"
					+ folderId + "' name='allExter' class='link'  "
					+ "title='" + Messages.getString("qa.all.fa.clickToShrink") + "' onclick='clickFolder(id, name)' >" +
							"<span id='" + folderId + "_span'>-</span> " + curProject.getName() + "</a></td>\n");
			
			//项目的外部匹配部份
			for (int i = 0; i < length; i++) {
				String matchPair = equivArray[i].substring(0, equivArray[i].indexOf(":") );
				String className = "folderTd";
				if (QAConstant._External101.equals(matchPair)) {
					exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ proFaResult.getExterMatch("101") +"</td>\n");
				}else if (QAConstant._External100.equals(matchPair)) {
					exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ proFaResult.getExterMatch("100") +"</td>\n");
				}else if (!QAConstant._InternalRepeat.equals(matchPair)) {
					exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ proFaResult.getExterMatch(matchPair) +"</td>\n");
				}
			}
			//项目的内部匹配部份
			for (int i = 0; i < length; i++) {
				String matchPair = equivArray[i].substring(0, equivArray[i].indexOf(":") );
				String className = "folderTd";
				if (interRepeat && QAConstant._InternalRepeat.equals(matchPair)) {
					exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ 
							proFaResult.getInterMatch("100-101") +"</td>\n");
				}else if (interMatch && matchPair.indexOf("external") == -1) {
					exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ 
							proFaResult.getInterMatch(matchPair) +"</td>\n");
				}
			}
			//新字数，锁定字数，总字数
			exterFAdata.append("\t\t\t<td class='folderTd' align='right'>"+ proFaResult.getNewWords() +"</td>\n");
			exterFAdata.append("\t\t\t<td class='folderTd' align='right'>"+ proFaResult.getEqalWords(equivStr) +"</td>\n");
			exterFAdata.append("\t\t\t<td class='folderTd' align='right'>"+ proFaResult.getLockedWords() +"</td>\n");
			exterFAdata.append("\t\t\t<td class='folderTd' align='right'>"+ proFaResult.getTotalWords() +"</td>\n");
			exterFAdata.append("</tr>");
			
			allExterMatchSetInputData(curProject, exterFAdata, paddLeft, equivArray);
			
			exterFAdata.append("\t</table>\n");
			output.write(exterFAdata.toString().getBytes("UTF-8"));

			// －－－－－－－－－－－－－－文本段---------------------
			
			output.write("<br/>\n".getBytes("UTF-8"));
			output.write(("\t\t\t<div class=\"legendStyle\"><b>"
					+ Messages.getString("qa.fileAnalysis.WordsFA.paragraph") + "</b></div>").getBytes("UTF-8"));
			StringBuffer paraFAdata = new StringBuffer();
			int paraTableColumnNum = 4 + 3;
			if (interRepeat && !interMatch) {
				paraTableColumnNum = 5 + 3;		// 文件名列占两个单位的长度
			}else if (interRepeat && interMatch) {
				paraTableColumnNum = 6 + 3;
			}
			float paraCloumnWidth = (float)100 / paraTableColumnNum;

			// 创建一个表
			paraFAdata.append("\t<table class='tableStyle' cellpadding='0' cellspacing='1'> \n");
			// 表头
			paraFAdata.append("\t\t<tr>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='" + 2 * paraCloumnWidth + "%'>" + Messages.getString("qa.all.fa.fileName")
					+ "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='" + paraCloumnWidth + "%'>"
					+ Messages.getString("qa.fileAnalysis.WordsFA.newPara") + "</td>\n");
			if (interMatch) {
				paraFAdata.append("\t\t\t<td class='headerTd' width='" + paraCloumnWidth + "%'>"
						+ Messages.getString("qa.fileAnalysis.WordsFA.interMatchPara") + "</td>\n");
			}
			paraFAdata.append("\t\t\t<td class='headerTd' width='" + paraCloumnWidth + "%'>"
					+ Messages.getString("qa.fileAnalysis.WordsFA.exterMatchPara") + "</td>\n");
			if (interRepeat) {
				paraFAdata.append("\t\t\t<td class='headerTd' width='" + paraCloumnWidth + "%'>"
						+ Messages.getString("qa.fileAnalysis.WordsFA.interRepeatPara") + "</td>\n");
			}
			paraFAdata.append("\t\t\t<td class='headerTd' width='" + paraCloumnWidth + "%'>"
					+ Messages.getString("qa.fileAnalysis.WordsFA.exterRepeatPara") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='" + paraCloumnWidth + "%'>"
					+ Messages.getString("qa.fileAnalysis.WordsFA.lockedPara") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='" + paraCloumnWidth + "%'>"
					+ Messages.getString("qa.fileAnalysis.WordsFA.paraSum") + "</td>\n");
			paraFAdata.append("\t\t</tr>\n");

			// 首先写下项目
			paddLeft = 6;
			paraFAdata.append("<tr onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n" +
					"<td class='folderTd' style='padding-left: 6'><a href='javascript:void(0)' id='"
							+ folderId + "' name='paras' class='link'  "
							+ "title='" + Messages.getString("qa.all.fa.clickToShrink") + "' onclick='clickFolder(id, name)' ><span id='" + folderId
							+ "_span'>-</span> " + curProject.getName() + "</a></td>\n"
							+ "<td class='folderTd' align='right'>" + proFaResult.getNewPara() + "</td>\n"
							+ (interMatch ? "<td class='folderTd' align='right'>" + proFaResult.getInterMatchPara() + "</td>\n" : "")
							+ "<td class='folderTd' align='right'>" + proFaResult.getExterMatchPara() + "</td>\n"
							+ (interRepeat ? "<td class='folderTd' align='right'>" + proFaResult.getInterRepeatPara() + "</td>\n" : "")
							+ "<td class='folderTd' align='right'>" + proFaResult.getExterRepeatPara() + "</td>\n"
							+ "<td class='folderTd' align='right'>" + proFaResult.getLockedPara() + "</td>\n"
							+ "<td class='folderTd' align='right'>" + proFaResult.getTotalPara() + "</td>\n"
							+ "</tr>");
			paraSetInputData(curProject, paraFAdata, paddLeft);

			paraFAdata.append("\t</table>\n");
			output.write(paraFAdata.toString().getBytes("UTF-8"));
//			output.write("\t\t</fieldset><br/>\n".getBytes("UTF-8"));
			
/*			String htmlPathDiv = "<div style='width:100%;font-size:14;color:blue;'>"
					+ Messages.getString("qa.all.fa.fileLocation")
					+ curProject.getFullPath().append("Report").append(htmlName).toOSString() + "</div>";
			output.write(htmlPathDiv.getBytes("UTF-8"));*/

			output.write("\t</body>\n".getBytes("UTF-8"));
			output.write("</html>".getBytes("UTF-8"));
			output.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.fileAnalysis.WordsFA.log6"), e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.fileAnalysis.WordsFA.log7"), e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.fileAnalysis.WordsFA.log8"), e);
		}

		return htmlPath;

	}

	/**
	 * 将文件下所有的子文件（直接或间接子文件）的值加到文件夹中
	 */
	public void setDataToFolder(IProject curProject) {
		// 先存放项目相关的信息，所有的文件都存放在该项目中的，因此直接遍历所有的文件
		WordsFAResult proFaResult = new WordsFAResult();
		for (int i = 0; i < model.getAnalysisIFileList().size(); i++) {
			IFile ifile = model.getAnalysisIFileList().get(i);
			WordsFAResult faResult = WordsFAResultMap.get(ifile.getLocation().toOSString());

			proFaResult.setNewPara(faResult.getNewPara());
			proFaResult.setInterRepeatPara(faResult.getInterRepeatPara());
			proFaResult.setInterMatchPara(faResult.getInterMatchPara());
			proFaResult.setExterRepeatPara(faResult.getExterRepeatPara());
			proFaResult.setExterMatchPara(faResult.getExterMatchPara());
			proFaResult.setLockedPara(faResult.getLockedPara());

			proFaResult.setNewWords(faResult.getNewWords());
			proFaResult.setInterMatchWords(faResult.getInterMatchWords());
			proFaResult.setExterMatchWords(faResult.getExterMatchWords());
			proFaResult.setLockedWords(faResult.getLockedWords());
			
			proFaResult.setAllExterMatchWords(faResult.getAllExterMatchWords());
			proFaResult.setAllInterMatchWords(faResult.getAllInterMatchWords());
		}
		WordsFAResultMap.put(curProject.getLocation().toOSString(), proFaResult);

		// 先遍历所有的文件夹
		for (int index = 0; index < allFolderList.size(); index++) {
			IContainer container = allFolderList.get(index);
			WordsFAResult folderFaResult = new WordsFAResult();

			// 循环所有的已经分析完的文件
			for (int i = 0; i < model.getAnalysisIFileList().size(); i++) {
				IFile ifile = model.getAnalysisIFileList().get(i);
				IContainer iFileParent = ifile.getParent();
				while (iFileParent != null) {
					if (iFileParent.equals(container)) {
						WordsFAResult faResult = WordsFAResultMap.get(ifile.getLocation().toOSString());

						folderFaResult.setNewPara(faResult.getNewPara());
						folderFaResult.setInterRepeatPara(faResult.getInterRepeatPara());
						folderFaResult.setInterMatchPara(faResult.getInterMatchPara());
						folderFaResult.setExterRepeatPara(faResult.getExterRepeatPara());
						folderFaResult.setExterMatchPara(faResult.getExterMatchPara());
						folderFaResult.setLockedPara(faResult.getLockedPara());

						folderFaResult.setNewWords(faResult.getNewWords());
						folderFaResult.setInterMatchWords(faResult.getInterMatchWords());
						folderFaResult.setExterMatchWords(faResult.getExterMatchWords());
						folderFaResult.setLockedWords(faResult.getLockedWords());
						
						//所有外部匹配的值
						folderFaResult.setAllExterMatchWords(faResult.getAllExterMatchWords());
						//所有内部匹配的值
						folderFaResult.setAllInterMatchWords(faResult.getAllInterMatchWords());
						break;
					} else {
						iFileParent = iFileParent.getParent();
					}
				}
			}
			WordsFAResultMap.put(container.getLocation().toOSString(), folderFaResult);
		}
	}
	
	/**
	 * 创建第一张表的数据，新字数，内部匹配，外部匹配，锁定字数的统计
	 * @param exterFAdata
	 */
	public void allExterMatchSetInputData(IContainer curContainer, StringBuffer exterFAdata, int paddLeft, String[] equivArray){
		paddLeft += 10;
		int length = equivArray.length;
		// 先判断该容器中是否有直接子文件为本次分析文件
		if (hasFAIFiles(curContainer)) {
			// 每个文件的具体数据
			for (int fIndex = 0; fIndex < model.getAnalysisIFileList().size(); fIndex++) {
				IFile curIFile = model.getAnalysisIFileList().get(fIndex);
				if (curIFile.getParent().equals(curContainer)) {
					WordsFAResult faResult = WordsFAResultMap.get(curIFile.getLocation().toOSString());

					exterFAdata.append("\t\t<tr id='" + curContainer.getFullPath().toOSString() + "' name='allExter' " +
							"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n");
					exterFAdata.append("\t\t\t<td class='fileTd' style='padding-left: " + paddLeft + "'>"
							+ curIFile.getName() + "</td>\n");// 文件名
					//创建外部匹配部份
					for (int i = 0; i < length; i++) {
						String matchPair = equivArray[i].substring(0, equivArray[i].indexOf(":") );
						String className = "fileTd";
						if (QAConstant._External101.equals(matchPair)) {
							exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ faResult.getExterMatch("101") +"</td>\n");
						}else if (QAConstant._External100.equals(matchPair)) {
							exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ faResult.getExterMatch("100") +"</td>\n");
						}else if (!QAConstant._InternalRepeat.equals(matchPair)) {
							exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ faResult.getExterMatch(matchPair) +"</td>\n");
						}
					}
					//创建内部匹配部份 
					for (int i = 0; i < length; i++) {
						String matchPair = equivArray[i].substring(0, equivArray[i].indexOf(":") );
						String className = "fileTd";
						
						if (interRepeat && QAConstant._InternalRepeat.equals(matchPair)) {
							exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ 
									faResult.getInterMatch("100-101") +"</td>\n");
						}else if (interMatch && matchPair.indexOf("external") == -1) {
							exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ 
									faResult.getInterMatch(matchPair) +"</td>\n");
						}
					}
					//创建新字数，锁定字数，总字数三列
					exterFAdata.append("\t\t\t<td class='fileTd' align='right'>"+ faResult.getNewWords() +"</td>\n");
					exterFAdata.append("\t\t\t<td class='fileTd' align='right'>"+ faResult.getEqalWords(equivStr) +"</td>\n");
					exterFAdata.append("\t\t\t<td class='fileTd' align='right'>"+ faResult.getLockedWords() +"</td>\n");
					exterFAdata.append("\t\t\t<td class='fileTd' align='right'>"+ faResult.getTotalWords() +"</td>\n");
					
					exterFAdata.append("\t\t</tr>\n");
				}
			}
		}
		// 遍历所有含有分析文件的容器，找出当前容器的子容器，
		for (int index = 0; index < allFolderList.size(); index++) {
			IContainer childContainer = allFolderList.get(index);
			if (childContainer.getParent().equals(curContainer)) {
				WordsFAResult faResult = WordsFAResultMap.get(childContainer.getLocation().toOSString());
				String folderId = childContainer.getFullPath().toOSString();
				exterFAdata.append("<tr id='" + curContainer.getFullPath().toOSString() + "' name='allExter' " +
						"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n"
						+ "<td class='folderTd' ><a href='javascript:void(0)' id='" + folderId
						+ "' name='allExter' class='link' style='padding-left: " + paddLeft + "'"
						+ "title='" + Messages.getString("qa.all.fa.clickToShrink") + "' onclick='clickFolder(id, name)'>" +
								"<span id='" + folderId + "_span'>-</span> "
						+ childContainer.getName() + "</a></td>\n" );
				//创建外部匹配部份		
				for (int i = 0; i < length; i++) {
					String matchPair = equivArray[i].substring(0, equivArray[i].indexOf(":") );
					String className = "folderTd";
					
					if (QAConstant._External101.equals(matchPair)) {
						exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ faResult.getExterMatch("101") +"</td>\n");
					}else if (QAConstant._External100.equals(matchPair)) {
						exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ faResult.getExterMatch("100") +"</td>\n");
						// UNDO 这里的这个判断是否正确？
					}else if (!QAConstant._InternalRepeat.equals(matchPair)) {
						exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ faResult.getExterMatch(matchPair) +"</td>\n");
					}
				}		
				//创建内部匹配部 
				for (int i = 0; i < length; i++) {
					String matchPair = equivArray[i].substring(0, equivArray[i].indexOf(":") );
					String className = "folderTd";
					
					if (interRepeat && QAConstant._InternalRepeat.equals(matchPair)) {
						exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ 
								faResult.getInterMatch("100-101") +"</td>\n");
					}else if (interMatch && matchPair.indexOf("external") == -1) {
						exterFAdata.append("\t\t\t<td class='" + className + "' align='right'>"+ 
								faResult.getInterMatch(matchPair) +"</td>\n");
					}
				}		
				//创建新字数，锁定字数，新字数三列
				exterFAdata.append("\t\t\t<td class='folderTd' align='right'>"+ faResult.getNewWords() +"</td>\n");
				exterFAdata.append("\t\t\t<td class='folderTd' align='right'>"+ faResult.getEqalWords(equivStr) +"</td>\n");
				exterFAdata.append("\t\t\t<td class='folderTd' align='right'>"+ faResult.getLockedWords() +"</td>\n");
				exterFAdata.append("\t\t\t<td class='folderTd' align='right'>"+ faResult.getTotalWords() +"</td>\n");
				
				exterFAdata.append("</tr>\n");
				allExterMatchSetInputData(childContainer, exterFAdata, paddLeft , equivArray);
			}
		}
		
	}
	
	/**
	 * 创建第二张表的数据，关于文本段的统计
	 * @param curContainer
	 * @param paraFAdata
	 * @param paddLeft ;
	 */
	public void paraSetInputData(IContainer curContainer, StringBuffer paraFAdata, int paddLeft) {
		paddLeft += 10;
		// 先判断该容器中是否有直接子文件为本次分析文件
		if (hasFAIFiles(curContainer)) {
			// 每个文件的具体数据
			for (int fIndex = 0; fIndex < model.getAnalysisIFileList().size(); fIndex++) {
				IFile curIFile = model.getAnalysisIFileList().get(fIndex);
				if (curIFile.getParent().equals(curContainer)) {
					WordsFAResult faResult = WordsFAResultMap.get(curIFile.getLocation().toOSString());

					paraFAdata.append("\t\t<tr id='" + curContainer.getFullPath().toOSString() + "' name='paras' " +
							"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n");
					paraFAdata.append("\t\t\t<td class='fileTd' style='padding-left: " + paddLeft + "'>" + curIFile.getName() + "</td>\n");// 文件名
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getNewPara() + "</td>\n"); // 新文本段
					if (interMatch) {
						paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getInterMatchPara() + "</td>\n"); // 内部匹配文本段
					}
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getExterMatchPara() + "</td>\n"); // 外部匹配文本段
					if (interRepeat) {
						paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getInterRepeatPara() + "</td>\n"); // 内部重复文本段
					}
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getExterRepeatPara() + "</td>\n"); // 外部重复文本段
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getLockedPara() + "</td>\n"); // 锁定文本段
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getTotalPara() + "</td>\n"); // 总段数
					paraFAdata.append("\t\t</tr>\n");
				}
			}
		}
		// 遍历所有含有分析文件的容器，找出当前容器的子容器，
		for (int index = 0; index < allFolderList.size(); index++) {
			IContainer childContainer = allFolderList.get(index);
			if (childContainer.getParent().equals(curContainer)) {
				WordsFAResult faResult = WordsFAResultMap.get(childContainer.getLocation().toOSString());
				String folderId = childContainer.getFullPath().toOSString();
				paraFAdata.append("<tr id='" + curContainer.getFullPath().toOSString() + "' name='paras' " +
						"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n"
						+ "<td class='folderTd' ><a href='javascript:void(0)' id='" + folderId + "' name='paras' class='link' style='padding-left: " + paddLeft + "'"
						+ "title='" + Messages.getString("qa.all.fa.clickToShrink") + "' onclick='clickFolder(id, name)'>" + "<span id='" + folderId + "_span'>-</span> "
						+ childContainer.getName() + "</a></td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getNewPara() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + (interMatch ? faResult.getInterMatchPara() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" : "") + faResult.getExterMatchPara() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + (interRepeat ? faResult.getInterRepeatPara() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" : "") + faResult.getExterRepeatPara() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getLockedPara() + "</td>\n"
						+ "<td class='folderTd' align='right'>" + faResult.getTotalPara() + "</td>\n"
						+ "</tr>\n");
				paraSetInputData(childContainer, paraFAdata, paddLeft);
			}
		}
	}

	public static void main(String[] args) {
		String text = "this is a test string for test count time.";

		
		long time1 = System.currentTimeMillis();
		int count = 0;
		for (int i = 0; i < 100000; i++) {
			count += CountWord.wordCount(text, "en-us");
		}
		
		System.out.println("时间为 = " + (System.currentTimeMillis() - time1));
		System.out.println("字数为 = " + count);
		
	}
	
}