package net.heartsome.cat.ts.ui.qa;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.qa.ParaConsisDataBean;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QATUDataBean;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.model.QAResultBean;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 文本段一致性检查，包括：
 * 相同源文，不同译文；相同译文，不同源文，
 * 检查时，要从首选项中获取的参数项是：忽略标记，忽略大小写
 * 进度条提示格式为	MessageFormat.format("检查{0}文件，术语一致性检查：术语比较...）
 * 进度条推进方法：设一个间隔值workInterval,先计算出要循环的所有的tu节点的数量，每workInterval个tu节点，推进一格
 * @author robert	2011-11-16
 */
public class ParagraphConsistenceQA extends QARealization {
	/** 相同源文不同译文比较时是否忽略大小写，从首选项中获取 */
	private boolean srcIgnoreCase ;
	/** 相同源文不同译文比较时是否忽略标记，从首选项中获取 */
	private boolean srcIgnoreTag ;
	/** 相同译文不同源文比较时是否忽略大小写，从首选项中获取 */
	private boolean tgtIgnoreCase ;
	/** 相同译文不同源文比较时是否忽略大小写，从首选项中获取 */
	private boolean tgtIgnoreTag;
	/** 是否检查相同源文，不同译文的情况，从首选项中获取 */
	private boolean checkSameSource;
	/** 是否检查相同译文，不同源文的情况，从首选项中获取 */
	private boolean checkSameTarget;
	/** 品质检查的过滤条件，如不包括上下文匹配文本段，完全匹配文本段，已锁定文本段 */
	private Map<String, Boolean> filterMap;
	/** 保存当前正在处理的文件路径 */
	private String curXlfPath = "";
	/** 针对每一个文件（或者合并打开时的一个语言对）获取整个要进行比较的数据，以便进行处理 */
	private Map<String, ParaConsisDataBean> dataMap = new HashMap<String, ParaConsisDataBean>();
	/** 数据经过处理后，会把源文相同的文本段的rowId放到一起，以便于集中处理 */
	private List<List<String>> srcRowIdList = new ArrayList<List<String>>();
	/** 数据经过处理后，会把源文相同的文本段的rowId放到一起，以便于集中处理 */
	private List<List<String>> tgtRowIdList = new ArrayList<List<String>>();
	/** 合并打开的文本段一致性查，是否初始化数据 */
	private boolean isMultiInit = false;

	private int level;
	
	public ParagraphConsistenceQA(){
		//filterMap = model.getNotInclude();
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		
		checkSameSource = preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_SAMESOURCE);
		checkSameTarget = preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_SAMETARGET);
		
		srcIgnoreTag = preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_SRC_IGNORCETAG);
		srcIgnoreCase = preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_SRC_IGNORCECASE);
		
		tgtIgnoreTag = preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_TAR_IGNORCETAG);
		tgtIgnoreCase = preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_TAR_IGNORCECASE);
		
		level = preferenceStore.getInt(QAConstant.QA_PREF_para_TIPLEVEL);
	}
	
	public void setParentQaResult(QAResult qaResult){
		super.setQaResult(qaResult);
	}
	
	@Override
	public String startQA(QAModel model, IProgressMonitor monitor, IFile iFile, QAXmlHandler xmlHandler,
			QATUDataBean tuDataBean) {
		
		if (tuDataBean.getTgtContent() == null || "".equals(tuDataBean.getTgtContent())) {
			return "";
		}
		
		
		String xlfPath = tuDataBean.getXlfPath();
		String rowId = tuDataBean.getRowId();
		filterMap = (filterMap == null ? model.getNotInclude() : filterMap);

		init(model, xlfPath, xmlHandler);
	
		//如果是针对合并打开的品质检查，使用以下方法
		if (model.isMuliFiles()) {
			compareNext(rowId);
		}else {
			compareNext(rowId);
		}
		
		return "";
	}

	/**
	 * 初始化相关数据
	 */
	public void init(QAModel model, String xlfPath, QAXmlHandler xmlHandler){
		if (!model.isMuliFiles()) {
			if ("".equals(curXlfPath) || !curXlfPath.equals(xlfPath)) {
				curXlfPath = xlfPath;
				dataMap.clear();
				dataMap = xmlHandler.getFilteredTUPureTextOrContent(xlfPath, filterMap, checkSameSource, checkSameTarget, srcIgnoreTag, tgtIgnoreTag);
				
				try {
					analysisSameSrc();
					analysisSameTgt();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("", e);
				}
			}
		}else {
			if (!isMultiInit) {
				dataMap = xmlHandler.getFilteredTUTextForMultiParaConsis(model.getRowIdsList(), filterMap, checkSameSource, checkSameTarget, srcIgnoreTag, tgtIgnoreTag);
				try {
					analysisSameSrc();
					analysisSameTgt();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("", e);
				}
				isMultiInit = true;
			}
		}
	}
	
	/**
	 * 将相同源文的所有文本段进行整合，放到一块去方便进行处理。
	 */
	private void analysisSameSrc() throws Exception{
		if (!checkSameSource) {
			return;
		}
		
		List<Entry<String, ParaConsisDataBean>> mapList = new ArrayList<Entry<String, ParaConsisDataBean>>(dataMap.entrySet());

		//排序
		Collections.sort(mapList, new Comparator<Entry<String, ParaConsisDataBean>>() {   
		    public int compare(Entry<String, ParaConsisDataBean> o1, Entry<String, ParaConsisDataBean> o2) { 
		    	String str1 = srcIgnoreTag ? o1.getValue().getSrcPureText() : o1.getValue().getSrcContent();
		    	String str2 = srcIgnoreTag ? o2.getValue().getSrcPureText() : o2.getValue().getSrcContent();
		        return srcIgnoreCase ? str1.compareToIgnoreCase(str2) : str1.compareTo(str2);
		    }
		}); 
		
		for (int i = 0; i < mapList.size(); i++) {
			Entry<String, ParaConsisDataBean> entry = mapList.get(i);
			String srcText = srcIgnoreTag ? entry.getValue().getSrcPureText() : entry.getValue().getSrcContent();
			ArrayList<String> resultList = new ArrayList<String>();
			boolean isSame = false;
			for (int j = i + 1; j < mapList.size(); j++) {
				Entry<String, ParaConsisDataBean> curEntry = mapList.get(j);
				String curSrcText = srcIgnoreTag ? curEntry.getValue().getSrcPureText() : curEntry.getValue().getSrcContent();
				if (srcText.length() != curSrcText.length()) {
					continue;
				}
				if (srcIgnoreCase ? srcText.equalsIgnoreCase(curSrcText) : srcText.equals(curSrcText)) {
					if (!isSame) {
						resultList.add(entry.getKey());
					}
					isSame = true;
					resultList.add(curEntry.getKey());
					mapList.remove(j);
					j --;
				}
			}
			if (resultList.size() > 0) {
				// 对 resultList 以行号进行排序
				Collections.sort(resultList, new Comparator<String>() {   
				    public int compare(String rowId1, String rowId2) { 
				    	return dataMap.get(rowId1).getLineNumber() - dataMap.get(rowId2).getLineNumber();
				    }
				}); 
				srcRowIdList.add(resultList);
			}
		}
	}
	
	/**
	 * 将相同译文的所有文本段进行整合，放到一块去方便进行处理。
	 */
	private void analysisSameTgt(){
		if (!checkSameTarget) {
			return;
		}
		
		List<Entry<String, ParaConsisDataBean>> mapList = new ArrayList<Entry<String, ParaConsisDataBean>>(dataMap.entrySet());

		//排序
		Collections.sort(mapList, new Comparator<Entry<String, ParaConsisDataBean>>() {   
		    public int compare(Entry<String, ParaConsisDataBean> o1, Entry<String, ParaConsisDataBean> o2) { 
		    	String str1 = tgtIgnoreTag ? o1.getValue().getTgtPureText() : o1.getValue().getTgtContent();
		    	String str2 = tgtIgnoreTag ? o2.getValue().getTgtPureText() : o2.getValue().getTgtContent();
		        return tgtIgnoreCase ? str1.compareToIgnoreCase(str2) : str1.compareTo(str2);
		    }
		}); 
		
		for (int i = 0; i < mapList.size(); i++) {
			Entry<String, ParaConsisDataBean> entry = mapList.get(i);
			String tgtText = tgtIgnoreTag ? entry.getValue().getTgtPureText() : entry.getValue().getTgtContent();
			ArrayList<String> resultList = new ArrayList<String>();
			boolean isSame = false;
			for (int j = i + 1; j < mapList.size(); j++) {
				Entry<String, ParaConsisDataBean> curEntry = mapList.get(j);
				String curTgtText = tgtIgnoreTag ? curEntry.getValue().getTgtPureText() : curEntry.getValue().getTgtContent();
				if (tgtText.length() != curTgtText.length()) {
					continue;
				}
				if (tgtIgnoreCase ? tgtText.equalsIgnoreCase(curTgtText) : tgtText.equals(curTgtText)) {
					if (!isSame) {
						resultList.add(entry.getKey());
					}
					isSame = true;
					resultList.add(curEntry.getKey());
					mapList.remove(j);
					j --;
				}
			}
			if (resultList.size() > 0) {
				// 对 resultList 以行号进行排序
				Collections.sort(resultList, new Comparator<String>() {   
				    public int compare(String rowId1, String rowId2) { 
				    	return dataMap.get(rowId1).getLineNumber() - dataMap.get(rowId2).getLineNumber();
				    }
				}); 
				
				tgtRowIdList.add(resultList);
			}
		}
	}
	
	
	/**
	 * 与其他文本段进行比较，获取相关值
	 * @param rowId
	 */
	private void compareNext(String rowId){
		// 先比较相同源文不同译文的情况
		String sameSrcQaTypeText = Messages.getString("qa.ParagraphConsistenceQA.name1");
		String mergeRowId = CommonFunction.createUUID();
		for (int i = 0; i < srcRowIdList.size(); i++) {
			// 如果该文本段存在与其他文本段的相同源文不同译文，那么就进行处理
			List<String> curList = srcRowIdList.get(i);
			if (curList.contains(rowId)) {
				// 循环比较，找出源文相同，译文不同的文本段
				List<Integer> sameSourceData = new LinkedList<Integer>();
				for(String rowId_1 : curList){
					sameSourceData.clear();
					ParaConsisDataBean bean_1 = dataMap.get(rowId_1);
					String tgtText_1 = srcIgnoreTag ? bean_1.getTgtPureText() : bean_1.getTgtContent();
					for (String rowId_2 : curList) {
						if (rowId_2.equals(rowId_1)) {
							continue;
						}
						ParaConsisDataBean bean_2 = dataMap.get(rowId_2);
						String tgtText_2 = srcIgnoreTag ? bean_2.getTgtPureText() : bean_2.getTgtContent();
						if (!checkEquals(tgtText_2, tgtText_1, srcIgnoreCase)) {
							sameSourceData.add(bean_2.getLineNumber());
						}
					}
					if (sameSourceData.size() > 0) {
						int lineNum = bean_1.getLineNumber();
//						String notSameTip = "";
//						for (int k = 0; k < sameSourceData.size(); k++) {
//							int k_lineNum = sameSourceData.get(k);
//							notSameTip += MessageFormat.format(Messages.getString("qa.ParagraphConsistenceQA.tip1"), k_lineNum);
//						}
//						notSameTip = notSameTip.length() > 0 ? notSameTip.substring(0, notSameTip.length() - 1): notSameTip;
//						
//						String errorTip = Messages.getString("qa.ParagraphConsistenceQA.tip2");
//						errorTip += Messages.getString("qa.ParagraphConsistenceQA.tip3");
//						errorTip += notSameTip;
//						errorTip += Messages.getString("qa.ParagraphConsistenceQA.tip4");
						
						super.printQAResult(new QAResultBean(level,
								QAConstant.QA_PARAGRAPH, sameSrcQaTypeText, mergeRowId,
								new File(RowIdUtil.getFileNameByRowId(rowId_1)).getName(), lineNum + "", 
								bean_1.getSrcContent(), bean_1.getTgtContent(), rowId_1));
					}
				}
				srcRowIdList.remove(curList);
				i --;
			}
		}
		
		// 再比较相同译文不同源文的情况
		mergeRowId = CommonFunction.createUUID();
		String sameTgtQaTypeText = Messages.getString("qa.ParagraphConsistenceQA.name2");
		for (int i = 0; i < tgtRowIdList.size(); i++) {
			// 如果该文本段存在与其他文本段的相同源文不同译文，那么就进行处理
			List<String> curList = tgtRowIdList.get(i);
			if (curList.contains(rowId)) {
				// 循环比较，找出源文相同，译文不同的文本段
				List<Integer> sameTargetData = new LinkedList<Integer>();
				for(String rowId_1 : curList){
					sameTargetData.clear();
					ParaConsisDataBean bean_1 = dataMap.get(rowId_1);
					String srcText_1 = tgtIgnoreTag ? bean_1.getSrcPureText() : bean_1.getSrcContent();
					for (String rowId_2 : curList) {
						if (rowId_2.equals(rowId_1)) {
							continue;
						}
						ParaConsisDataBean bean_2 = dataMap.get(rowId_2);
						String srcText_2 = tgtIgnoreTag ? bean_2.getSrcPureText() : bean_2.getSrcContent();
						if (!checkEquals(srcText_2, srcText_1, tgtIgnoreCase)) {
							sameTargetData.add(bean_2.getLineNumber());
						}
					}
					if (sameTargetData.size() > 0) {
						int lineNum = bean_1.getLineNumber();
//						String notSameTip = "";
//						for (int k = 0; k < sameTargetData.size(); k++) {
//							int k_lineNum = sameTargetData.get(k);
//							notSameTip += MessageFormat.format(Messages.getString("qa.ParagraphConsistenceQA.tip1"), k_lineNum);
//						}
//						notSameTip = notSameTip.length() > 0 ? notSameTip.substring(0, notSameTip.length() - 1): notSameTip;
//						
//						String errorTip = Messages.getString("qa.ParagraphConsistenceQA.tip2");
//						errorTip += Messages.getString("qa.ParagraphConsistenceQA.tip3");
//						errorTip += notSameTip;
//						errorTip += Messages.getString("qa.ParagraphConsistenceQA.tip5");
						
						super.printQAResult(new QAResultBean(level,
								QAConstant.QA_PARAGRAPH, sameTgtQaTypeText, mergeRowId,
								new File(RowIdUtil.getFileNameByRowId(rowId_1)).getName(), "" + lineNum, 
								bean_1.getSrcContent(), bean_1.getTgtContent(), rowId_1));
						
					}
				}
				tgtRowIdList.remove(curList);
				i --;
			}
		}
	}

	
	/**
	 * 判断两个文本段是否相等
	 * @param text1
	 * @param text2
	 * @return
	 */
	public boolean checkEquals(String text1, String text2, boolean ignoreCase){
		if (ignoreCase) {
			return text1.equalsIgnoreCase(text2);
		}else {
			return text1.equals(text2);
		}
	}
	
	/**
	 * 进度条前进处理方法，针对遍历tu节点总数不是workInterval的倍数情况下，程序运行要结束时，就前进一格。
	 * 如果是在程序运行中，就判断是tu节点遍历序列号是否是workInterval的倍数，若是，则前进一格
	 * @param monitor			进度条实例
	 * @param traversalTuIndex	遍历的序列号
	 * @param last				是否是程序运行的结尾处
	 */
/*	public void monitorWork(IProgressMonitor monitor, int traversalTuIndex, boolean last){
		if (last) {
			if (traversalTuIndex % workInterval != 0) {
				monitor.worked(1);
			}
		}else {
			if (traversalTuIndex % workInterval == 0) {
				monitor.worked(1);
			}
		}
		
	}*/
}
