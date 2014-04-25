package net.heartsome.cat.ts.ui.qa;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * 品质检查－－术语一致性检查
 * 进度条提示格式为	MessageFormat.format("检查{0}文件，术语一致性检查：术语比较...）
 * 进度条推进方法：设一个间隔值workInterval,先计算出要循环的所有的tu节点的数量，每workInterval个tu节点，推进一格
 * @author robert
 *
 */
public class NumberConsistenceQA extends QARealization {
	private int level;
	private IPreferenceStore preferenceStore;
	private boolean hasError;
	
	public NumberConsistenceQA(){
		preferenceStore = Activator.getDefault().getPreferenceStore();
		level = preferenceStore.getInt(QAConstant.QA_PREF_number_TIPLEVEL);
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
		
		//获取纯文本
		String srcPureText = tuDataBean.getSrcPureText();
		String tgtPureText = tuDataBean.getTgtPureText();
		
		String pattern = "-?\\d{1,}(\\.?\\d{1,})?";
		Pattern patt = Pattern.compile(pattern);
		//获取源文本中的所有数字
		Matcher matcher = patt.matcher(srcPureText);
		List<String> sourceNumbers = new LinkedList<String>();
		while (matcher.find()) {
			sourceNumbers.add(matcher.group()) ;
		}
		
		//获取目标文本中的数字
		matcher = patt.matcher(tgtPureText);
		List<String> targetNumbers = new LinkedList<String>();
		while (matcher.find()) {
			targetNumbers.add(matcher.group()) ;
		}
		
		String lineNumber = tuDataBean.getLineNumber();
		String rowId = tuDataBean.getRowId();
		String qaTypeText = Messages.getString("qa.all.qaItem.NumberConsistenceQA");
		
		Map<String, List<String>> resultMap = compareNumber(sourceNumbers, targetNumbers);
		if (resultMap.get("source") != null && resultMap.get("source").size() > 0) {
			//输出数字一致性中目标文件所遗失的数字
			List<String> resultList = resultMap.get("source");
			String resultStr = "";
			for (int index = 0; index < resultList.size(); index++) {
				resultStr += "'" + resultList.get(index) + "', ";
			}
			
			if (resultStr.length() > 0) {
				resultStr = resultStr.substring(QAConstant.QA_ZERO, resultStr.length() - QAConstant.QA_TWO);
//				String errorTip = MessageFormat.format(Messages.getString("qa.NumberConsistenceQA.tip1"), resultStr);
				hasError = true;
			}
		}
		//输出数字一致性检查中目标文件中多出的数字
		if (resultMap.get("target") != null && resultMap.get("target").size() > 0) {
			//输出数字一致性中目标文件所遗失的数字
			List<String> resultList = resultMap.get("target");
			String resultStr = "";
			for (int index = 0; index < resultList.size(); index++) {
				resultStr += "'" + resultList.get(index) + "', ";
			}
			
			if (resultStr.length() > 0) {
				resultStr = resultStr.substring(QAConstant.QA_ZERO, resultStr.length() - QAConstant.QA_TWO);
//				String errorTip = MessageFormat.format(Messages.getString("qa.NumberConsistenceQA.tip2"), resultStr);
				hasError = true;
			}
		}
		
		if (hasError) {
			super.printQAResult(new QAResultBean(level, QAConstant.QA_NUMBER, qaTypeText, null, tuDataBean.getFileName(), lineNumber, tuDataBean.getSrcContent(), tuDataBean.getTgtContent(), rowId));
		}
		
		String result = "";
		if (hasError && level == 0) {
			result = QAConstant.QA_NUMBER;
		}
		return result;
	}

	/**
	 * 将查出的数字与目标文本进行相关的比较
	 * @param findNumber
	 * @param targetText
	 * @return
	 */
	public Map<String, List<String>> compareNumber(List<String> sourceNumbers, List<String> targetNumbers){
		Map<String, List<String>> resultMap  = new HashMap<String, List<String>>();
		List<String> resultList = new LinkedList<String>();
		//下面查看其目标数字集合中是否有源文本中的数字，如果有，则将这个数字从目标数字集合中删除
		int tarIndex;
		if (targetNumbers.size() == 0) {
			resultMap.put("source", sourceNumbers);
			return resultMap;
		}
		for (int index = 0; index < sourceNumbers.size(); index++) {
			String sourceNumber = sourceNumbers.get(index);
			
			if ((tarIndex = targetNumbers.indexOf(sourceNumber)) >= 0) {
				targetNumbers.remove(tarIndex);
			}else {
				resultList.add(sourceNumber);
			}
		}
		resultMap.put("source", resultList);
		if (targetNumbers.size() > 0) {
			resultMap.put("target", targetNumbers);
		}
		return resultMap;
	}

}
