package net.heartsome.cat.ts.ui.qa;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QATUDataBean;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.model.NontransElementBean;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.model.QAResultBean;
import net.heartsome.cat.ts.ui.qa.nonTransElement.NonTransElementOperate;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 品质检查－非译元素
 * 进度条提示格式为	MessageFormat.format("检查{0}文件，非译元素检查...）
 * 2011－11-22	完成功能：检查内置元素，如邮件，IP地址，web链接
 * @author robert	2011-11-21
 */
public class NonTranslationQA extends QARealization {
	/** 译文中丢失非译元素的集合 */
	private List<String> srcNonTransData;
	/** 源文中丢失非译元素的集合 */
	private List<String> tgtNonTransData;
	private String srcLang = "";
	private String tgtLang = "";
	private List<NontransElementBean> nonTransElementsMap;
	private int level;
	private IPreferenceStore preferenceStore;
	private boolean hasError;
	public final static Logger logger = LoggerFactory.getLogger(NonTranslationQA.class.getName());
	
	public NonTranslationQA(){
		preferenceStore = Activator.getDefault().getPreferenceStore();
		level = preferenceStore.getInt(QAConstant.QA_PREF_nonTrans_TIPLEVEL);
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
		
		if (nonTransElementsMap == null) {
			//非译元素库的操作类 
			NonTransElementOperate operater = new NonTransElementOperate();
			//先打开库
			operater.openNonTransDB();
			//开始从非译元素库中获取相关信息
			nonTransElementsMap = operater.getNonTransElements();
		}
		
		srcNonTransData = new LinkedList<String>();
		tgtNonTransData = new LinkedList<String>();
		//获取纯文本
		String sourceText = tuDataBean.getSrcPureText();
		String targetText = tuDataBean.getTgtPureText();
		String rowId = tuDataBean.getRowId();
		srcLang = tuDataBean.getSrcLang();
		tgtLang = tuDataBean.getTgtLang();
		
		// UNDO 这里存在一个问题，就是web地址检查与ip地址检查的语句差不多，故存在会引起一个非译元素会同时被检查并多次提示。
		for (int index = 0; index < nonTransElementsMap.size(); index++) {
			NontransElementBean elementBean = nonTransElementsMap.get(index);
			String elementContent = elementBean.getContent();
			String elementRegular = elementBean.getRegular();
			
			matchNontrans(sourceText, targetText, elementContent, elementRegular);

		}
		// UNDO 非译元素检查时，把数字也检查出来了
		String lineNumber = tuDataBean.getLineNumber();
		String fileName = tuDataBean.getFileName();
		String qaTypeText = Messages.getString("qa.all.qaItem.NonTranslationQA");
		
		//处理源文中的非译元素在译文中丢失的情况
		if (srcNonTransData.size() > 0) {
			String nonTransStr = "";
			for (int index = 0; index < srcNonTransData.size(); index++) {
				nonTransStr += "'" + srcNonTransData.get(index) + "', "; 
			}
			nonTransStr = nonTransStr.substring(0, nonTransStr.length() - 2);
//			String errorTip = MessageFormat.format(Messages.getString("qa.NonTranslationQA.tip1"), nonTransStr);
			hasError = true;
		}
		
		//处理译文中的非译元素在源文中丢失的情况
		if (tgtNonTransData.size() > 0) {
			String nonTransStr = "";
			for (int index = 0; index < tgtNonTransData.size(); index++) {
				nonTransStr += "'" + tgtNonTransData.get(index) + "', "; 
			}
			nonTransStr = nonTransStr.substring(0, nonTransStr.length() - 2);
//			String errorTip = MessageFormat.format(Messages.getString("qa.NonTranslationQA.tip2"), nonTransStr);
			hasError = true;
		}
		
		if (hasError) {
			super.printQAResult(new QAResultBean(level, QAConstant.QA_NONTRANSLATION, qaTypeText, null, fileName, lineNumber, sourceText, targetText, rowId));
		}
		
		
		String result = "";
		if (hasError && level == 0) {
			result = QAConstant.QA_NONTRANSLATION;
		}
		return result;
	}

	/**
	 * 检查邮件名
	 * @param sourceText
	 * @param targetText
	 */
	public void matchNontrans(String sourceText, String targetText, String elementContent, String elementRegular){
		if (elementRegular == null || "".equals(elementRegular)) {
			elementRegular = elementContent;
		}
		if (elementRegular == null || "".equals(elementRegular)) {
			return;
		}
		Pattern patt = Pattern.compile(CommonFunction.isAsiaLang(srcLang) ? elementRegular.replace("\\b", "") : elementRegular);
		
		//先匹配源文本里面是否有邮箱名
		List<String> sourceEmailList = new LinkedList<String>();
		Matcher matcher = patt.matcher(sourceText);
		while (matcher.find()) {
			sourceEmailList.add(matcher.group());
		}
		
		//现在检查目标文本中是否有相关匹配
		List<String> targetEmailList = new LinkedList<String>();
		patt = Pattern.compile(CommonFunction.isAsiaLang(tgtLang) ? elementRegular.replace("\\b", "") : elementRegular);
		Matcher tarMatcher = patt.matcher(targetText);
		while (tarMatcher.find()) {
			targetEmailList.add(tarMatcher.group());
		}
		
		//下面比较两个list所包含的email的不同
		if (sourceEmailList.size() > 0 ) {
			for (int sourIndex = 0; sourIndex < sourceEmailList.size(); sourIndex++) {
				String  nonTransStr = sourceEmailList.get(sourIndex);
				boolean equals = false;
				for (int tarIndex = 0; tarIndex < targetEmailList.size(); tarIndex++) {
					if (nonTransStr.equals(targetEmailList.get(tarIndex))) {
						equals = true;
						targetEmailList.remove(tarIndex);
						break;
					}
				}
				
				if (!equals && srcNonTransData.indexOf(nonTransStr) < 0) {
					srcNonTransData.add(nonTransStr);
				}
			}
		}
		
		// 下面比译文中有的非译元素，但是源文中没得的情况
		for (String nonTransStr : targetEmailList) {
			if (tgtNonTransData.indexOf(nonTransStr) < 0) {
				tgtNonTransData.add(nonTransStr);
			}
		}
		return;
	}
	
	/**
	 * 匹配IP，备注，这个方法改动比较大，之前的版本有备份，详见代码备份 ，task33
	 * @param sourceText
	 * @param targetText
	 */
	public void matchIP(String sourceText, String targetText, String elementContent, String elementRegular){
		if (elementRegular == null || "".equals(elementRegular)) {
			elementRegular = elementContent;
		}
		if (elementRegular == null || "".equals(elementRegular)) {
			return;
		}
		elementRegular = TextUtil.resetSpecialString(elementRegular);
		Pattern patt = Pattern.compile(elementRegular);
		
		//先匹配源文本里面是否有邮箱名
		List<String> sourceIpList = new LinkedList<String>();
		Matcher matcher = patt.matcher(sourceText);
		while (matcher.find()) {
			sourceIpList.add(matcher.group());
		}
		
		//现在检查目标文本中是否有相关匹配
		List<String> targetIpList = new LinkedList<String>();
		Matcher tarMatcher = patt.matcher(targetText);
		while (tarMatcher.find()) {
			targetIpList.add(tarMatcher.group());
		}
		
		//下面比较两个list所包含的email的不同
		if (sourceIpList.size() > 0 ) {
			for (int sourIndex = 0; sourIndex < sourceIpList.size(); sourIndex++) {
				String  nonTransStr = sourceIpList.get(sourIndex);
				boolean equals = false;
				for (int tarIndex = 0; tarIndex < targetIpList.size(); tarIndex++) {
					if (nonTransStr.equals(targetIpList.get(tarIndex))) {
						equals = true;
						targetIpList.remove(tarIndex);
						break;
					}
				}
				
				if (!equals && srcNonTransData.indexOf(nonTransStr) < 0) {
					srcNonTransData.add(nonTransStr);
				}
			}
		}
		
		// 下面比译文中有的非译元素，但是源文中没得的情况
		for (String nonTransStr : targetIpList) {
			if (tgtNonTransData.indexOf(nonTransStr) < 0) {
				tgtNonTransData.add(nonTransStr);
			}
		}
		return;
	}
	
	/**
	 * 匹配网址
	 * @param sourceText
	 * @param targetText
	 */
	public void matchWebAddress(String sourceText, String targetText, String elementContent, String elementRegular){
		if (elementRegular == null || "".equals(elementRegular)) {
			elementRegular = elementContent;
		}
		if (elementRegular == null || "".equals(elementRegular)) {
			return;
		}
		//这能匹配有IP的情况，但是无法保证IP是否是合法
		//String pattern = "((http|https|ftp)://(www.)?((((\\w+)+[.])*(net|com|cn|org|cc|tv|[0-9]{1,3}(:\\d{1,})?)))(/([+-_.=%&?#|]?(\\w+)[+-_.=%&?#|]?)*)*)";
		//String pattern = "(((http|https|ftp|gopher|wais)://)?((((\\w+|[-]*)+))+:(((\\w+|[-]*)+))+@)?((www[.])?((\\w+[.])+|((\\w+|[-]*)+[.]))+(net|com|org|mil|gov|edu|int)(.(cn|hk|uk|sg|us|jp|cc|tv))?|(\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}))(:\\d{1,})?(/?)([+-_.=%&?#|/]*([\\w\\S]+)[+-_.=%&?#|/]*)*(/?))";
//		String pattern = "(((http|https|ftp|gopher|wais)://)?(www\\.)?(([^,，\\s])*(\\.(net|com|cn|org|cc|tv|hk|uk|sg|us|jp|mil|gov|edu|int)))(((:port)|[0-9]{1,3}(:\\d{1,})?)?)([^,，\\s])*)";
		Pattern patt = Pattern.compile(elementRegular);
		// 先匹配源文本里面是否有邮箱名
		Matcher matcher = patt.matcher(sourceText);
		List<String> sourceURLList = new LinkedList<String>();
		while (matcher.find()) {
			sourceURLList.add(matcher.group());
		}
		
		//再匹配目标文本中是否有网址
		Matcher tarMatcher = patt.matcher(targetText);
		List<String> targetURLList = new LinkedList<String>();
		while (tarMatcher.find()) {
			targetURLList.add(tarMatcher.group());
		}
		
		//比较网址是否被翻译或缺失
		if (sourceURLList.size() > 0 ) {
			for (int sourIndex = 0; sourIndex < sourceURLList.size(); sourIndex++) {
				String  nonTransStr = sourceURLList.get(sourIndex);
				boolean equals = false;
				for (int tarIndex = 0; tarIndex < targetURLList.size(); tarIndex++) {
					if (nonTransStr.equals(targetURLList.get(tarIndex))) {
						equals = true;
						targetURLList.remove(tarIndex);
						break;
					}
				}
				
				if (!equals && srcNonTransData.indexOf(nonTransStr) < 0) {
					srcNonTransData.add(nonTransStr);
				}
			}
		}
		
		// 下面比译文中有的非译元素，但是源文中没得的情况
		for (String nonTransStr : targetURLList) {
			if (tgtNonTransData.indexOf(nonTransStr) < 0) {
				tgtNonTransData.add(nonTransStr);
			}
		}
		return;
	}
	
	/**
	 * 匹配除内置非译元素除
	 * @param sourceText
	 * @param targetText
	 * @param elementContent
	 * @param elementRegular
	 */
	public void matchOthers(String sourceText, String targetText, String elementName, String elementContent, String elementRegular){
		if (elementRegular == null || "".equals(elementRegular)) {
			elementRegular = elementContent;
		}
		if (elementRegular == null || "".equals(elementRegular)) {
			return;
		}
		Pattern patt = Pattern.compile(elementRegular);
		// 先匹配源文本里面是否有合适的匹配
		Matcher matcher = patt.matcher(sourceText);
		List<String> sourceMatchList = new LinkedList<String>();
		while (matcher.find()) {
			sourceMatchList.add(matcher.group());
		}
		
		//再匹配目标文本中是否有合适的匹配
		Matcher tarMatcher = patt.matcher(targetText);
		List<String> targetMatchList = new LinkedList<String>();
		while (tarMatcher.find()) {
			targetMatchList.add(tarMatcher.group());
		}
		
		
		//比较匹配结果是否被翻译或缺失
		if (sourceMatchList.size() > 0 ) {
			for (int sourIndex = 0; sourIndex < sourceMatchList.size(); sourIndex++) {
				String  nonTransStr = sourceMatchList.get(sourIndex);
				boolean equals = false;
				for (int tarIndex = 0; tarIndex < targetMatchList.size(); tarIndex++) {
					if (nonTransStr.equals(targetMatchList.get(tarIndex))) {
						equals = true;
						targetMatchList.remove(tarIndex);
						break;
					}
				}
				
				if (!equals) {
					srcNonTransData.add(nonTransStr);
				}
			}
		}
		
		// 下面比译文中有的非译元素，但是源文中没得的情况
		for (String nonTransStr : targetMatchList) {
			if (tgtNonTransData.indexOf(nonTransStr) < 0) {
				tgtNonTransData.add(nonTransStr);
			}
		}
		return;
	}
	
	public static void main(String[] args) {
//		String regular = "(?<=\\s+|^)[A-Z]+(?=\\s+|$)";
//		String regular = "\\b(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}(:\\d{1,})?\\b";
//		String regular = "\\b(abc){1}(?=\\s|$|\\b)";
		String regular = "(((http|https|ftp|gopher|wais)://)?(www\\.)?(([^,，\\s@])*(\\.(net|com|cn|org|cc|tv|hk|uk|sg|us|jp|mil|gov|edu|int)))(((:port)|[0-9]{1,3}(:\\d{1,})?)?)([^,，\\s])*)";
//		String text = "中国人的什么菏一 THEST 木";
//		String text = "an open source community and an ecosystem of complementary products and services.192.168.0.1.2";
//		String text = "aiabc";
		String text = "2、测试非译元素 398752906@qq.com398752906@qq.com398752906@qq.com398752906@qq.com";
		
		
		Pattern patt = Pattern.compile(regular);
		// 先匹配源文本里面是否有合适的匹配
		Matcher matcher = patt.matcher(text);
		while(matcher.find()){
			System.out.println(matcher.group());
		}
		


		
	}
}
