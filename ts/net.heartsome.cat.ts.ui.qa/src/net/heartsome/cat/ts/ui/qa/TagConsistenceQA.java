package net.heartsome.cat.ts.ui.qa;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * 标记一致性检查
 * 进度条提示格式为	MessageFormat.format("检查{0}文件，术语一致性检查：术语比较...）
 * 第一次编写时间：2011－11－18，主要获取所有的标记，在比较时只用比较目标文本中是否有缺时的标记，忽略标记与标记之间的关系。
 * @author robert	2011-11-18
 *
 */
public class TagConsistenceQA extends QARealization{
	private int level;
	private IPreferenceStore preferenceStore;
	private boolean hasError;
	
	public TagConsistenceQA(){
		preferenceStore = Activator.getDefault().getPreferenceStore();
		level = preferenceStore.getInt(QAConstant.QA_PREF_tag_TIPLEVEL);
	}
	
	@Override
	void setParentQaResult(QAResult qaResult) {
		super.setQaResult(qaResult);
	}

	@Override
	public String startQA(QAModel model, IProgressMonitor monitor, IFile iFile, QAXmlHandler xmlHandler,
			QATUDataBean tuDataBean) {
		
		if (tuDataBean.getTgtContent() == null || "".equals(tuDataBean.getTgtContent())) {
			return "";
		}
		hasError = false;
		//获取语言对
		String lineNumber = tuDataBean.getLineNumber();
		String rowId = tuDataBean.getRowId();
		String srcContent = tuDataBean.getSrcContent();
		String tgtContent = tuDataBean.getTgtContent();
		if (!(srcContent.indexOf("<") >=0 || srcContent.indexOf("</") >=0 || srcContent.indexOf("/>") >= 0) 
				&& !(tgtContent.indexOf("<") >=0 || tgtContent.indexOf("</") >=0 || tgtContent.indexOf("/>") >= 0)) {
			return "";
		}
		
		String srcContentXml = "<xml>" + srcContent + "</xml>";
		String tgtContentXml = "<xml>" + tgtContent + "</xml>";
		
		List<Map<String, String>> sourceTagList = xmlHandler.getTUTag(srcContentXml);
		List<Map<String, String>> targetTagList = xmlHandler.getTUTag(tgtContentXml);
		
		List<String> loseTag = new LinkedList<String>();
		List<String> changeTag = new LinkedList<String>();
		
		//开始比较源文本与目标文本标记的不同
		for (int sourceIndex = 0; sourceIndex < sourceTagList.size(); sourceIndex++) {
			Map<String, String> sourceTagMap = sourceTagList.get(sourceIndex);
			String tagName = sourceTagMap.get(QAConstant.QA_TAGNAME);
			String tagContent = sourceTagMap.get(QAConstant.QA_TAGCONTENT);
			
			boolean hasTag = false;	//检查目标文本是否有该标记
			boolean tagChanged = false;
			
			for (int targetIndex = 0; targetIndex < targetTagList.size(); targetIndex++) {
				Map<String, String> targetTagMap = targetTagList.get(targetIndex);
				if (tagName.equals(targetTagMap.get(QAConstant.QA_TAGNAME))) {
					hasTag = true;
					if (!tagContent.equals(targetTagMap.get(QAConstant.QA_TAGCONTENT))) {
						tagChanged = true;
					}else {
						targetTagList.remove(targetIndex);
						tagChanged = false;
						break;
					}
				}
			}
			
			//如果该标记不存在，那么就是目标文本中缺失该标记
			if (!hasTag) {
				loseTag.add(tagName);
			}
			//如果有这个标记，但是它的标记内容不正确。
			if (hasTag && tagChanged) {
				changeTag.add(tagName);
			}
		}
		
		//处理标记缺失的情况
		int loseTagSize;
		if ((loseTagSize = loseTag.size()) > 0) {
//			String qaTypeText = Messages.getString("qa.TagConsistenceQA.name1");
//			String tagLoseTip = "";
//			for (int index = 0; index < loseTagSize; index++) {
//				tagLoseTip += "'" + loseTag.get(index) + "', ";
//			}
//			tagLoseTip = tagLoseTip.substring(0, tagLoseTip.length() - 2);
//			String errorTip = MessageFormat.format(Messages.getString("qa.TagConsistenceQA.tip1"), tagLoseTip);
			
			hasError = true;
		}
		
		//处理标记不一致的情况
		int changeTagSize;
		if ((changeTagSize = changeTag.size()) > 0) {
//			String qaTypeText = Messages.getString("qa.TagConsistenceQA.name2");
//			String tagChangeTip = "";
//			for (int index = 0; index < changeTagSize; index++) {
//				tagChangeTip += "'" + changeTag.get(index) + "', ";
//			}
//			tagChangeTip = tagChangeTip.substring(0, tagChangeTip.length() - 2);
//			String errorTip = MessageFormat.format(Messages.getString("qa.TagConsistenceQA.tip2"), tagChangeTip);
			
			hasError = true;
		}
		
		// 处理译文标记多出的情况
		int moreTagSize;
		if ((moreTagSize = targetTagList.size()) > 0) {
//			String qaTypeText = Messages.getString("qa.TagConsistenceQA.addName1");
//			String moreTagTip = "";
//			for (int index = 0; index < moreTagSize; index++) {
//				moreTagTip += "'" + targetTagList.get(index).get(QAConstant.QA_TAGNAME) + "', ";
//			}
//			moreTagTip = moreTagTip.substring(0, moreTagTip.length() - 2);
//			String errorTip = MessageFormat.format(Messages.getString("qa.TagConsistenceQA.addTip1"), moreTagTip);
			
			hasError = true;
		}
		
		if (hasError) {
			String qaTypeText = Messages.getString("qa.TagConsistenceQA.name2");
			super.printQAResult(new QAResultBean(level, QAConstant.QA_TAG, qaTypeText, null, tuDataBean.getFileName(), lineNumber, srcContent, tgtContent, rowId));
		}
		
		String result = "";
		if (hasError && level == 0) {
			result = QAConstant.QA_TAG;
		}
		return result;
	}
}
