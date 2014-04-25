package net.heartsome.cat.ts.ui.qa.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.util.MultiFilesOper;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Shell;

/**
 * 备注：此类中的配置非常重要。
 * @author  robert
 * @version 
 * @since   JDK1.6
 */
public class QAModel {
	/** 要检查的文件的集合 */
	private List<IFile> qaXlfList;
	/** 是否是批量检查 */
	private boolean isBatchQA;
	private boolean isMuliFiles;

	/** 批量检查时的所有检查项，这是从首选项中获取的 */
	private LinkedList<String> batchQAItemIdList;
	/**
	 * 品质检查的标识符，检查项名称，检查项类名的键值对
	 */
	private Map<String, HashMap<String, String>> qaItemId_Name_Class;
	private Shell shell;
	
	/**---------------------品质检查首选项中的参数   品质检查不包括的文本段，针对所有品质检查项--------------*/
	private Map<String, Boolean> notInclude;
	
	//---------------------品质检查首选项中的参数   术语一致性检查 ------------------------------------//
	
	//---------------------品质检查首选项中的参数   文本段一致性检查 ------------------------------------//
/*	*//** 文本段一致性检查中的忽略大小写，默认为false *//*
	private boolean paraIgnoreCase = false;
	*//** 文本段一致性检查中的忽略标记，默认为false *//*
	private boolean paraIgnoreTag = false;
	*//** 文本段一致性检查中的检查项：相同源文不同译文 *//*
	private boolean paraCheckSameSource = true;
	*//** 文本段一致性检查中的检查项：相同译文不同源文 *//*
	private boolean paraCheckSameTarget = true;*/
	
	//---------------------品质检查首选项中的参数   数字一致性检查------------------------------------//
	/** 数字一致性检查中的检查项：与源文一致性 */
	private boolean numberSameToSource = true;
	/** 数字一致性检查中的检查项：正确的数字界定符检查 */
	private boolean numberBoundarySign = false;
	/**	当前进行进行处理的某种语言对的rowid集合，针对合并打开 */
	private List<String> rowIdsList;
	/** 合并文件的处理类实例 */
	private MultiFilesOper multiOper;
	
  	/**
  	 * 	qaItemId				qaItemName			qaItemClassName
  	 *	batch					批量检查				
	 *	term					术语一致性检查			net.heartsome.cat.ts.ui.qa.TermConsistenceQA
	 *	paragraph				文本段一致性检查		net.heartsome.cat.ts.ui.qa.ParagraphConsistenceQA
	 *	number					数字一致性检查			net.heartsome.cat.ts.ui.qa.NumberConsistenceQA
	 *	tag						标记一致性检查			net.heartsome.cat.ts.ui.qa.TagConsistenceQA
	 * 	nonTranslation			非译元素检查			net.heartsome.cat.ts.ui.qa.NonTranslationQA
	 * 	spaceOfParaCheck		段首段末空格检查		net.heartsome.cat.ts.ui.qa.NonTranslationQA
	 * 	paragraphCompleteness	文本段完整性检查		net.heartsome.cat.ts.ui.qa.ParaCompletenessQA
	 * 	targetTextLengthLimit	目标文本段长度限制检查	net.heartsome.cat.ts.ui.qa.TgtTextLengthLimitQA
	 * 	spell					拼写检查				net.heartsome.cat.ts.ui.qa.SpellQA
  	 */
	public QAModel(){
		qaItemId_Name_Class = new LinkedHashMap<String, HashMap<String,String>>();
		//术语一致性检查
		HashMap<String, String> valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.QA_ITEM_NAME, Messages.getString("qa.all.qaItem.TermConsistenceQA"));
		valueMap.put(QAConstant.QA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.TermConsistenceQA");
		qaItemId_Name_Class.put(QAConstant.QA_TERM, valueMap);
		
		//文本段一致性检查
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.QA_ITEM_NAME, Messages.getString("qa.all.qaItem.ParagraphConsistenceQA"));
		valueMap.put(QAConstant.QA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.ParagraphConsistenceQA");
		qaItemId_Name_Class.put(QAConstant.QA_PARAGRAPH, valueMap);
		
		//数字一致性检查
		valueMap  = new HashMap<String, String>();
		valueMap.put(QAConstant.QA_ITEM_NAME, Messages.getString("qa.all.qaItem.NumberConsistenceQA"));
		valueMap.put(QAConstant.QA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.NumberConsistenceQA");
		qaItemId_Name_Class.put(QAConstant.QA_NUMBER, valueMap);
		
		//标记一致性检查
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.QA_ITEM_NAME, Messages.getString("qa.all.qaItem.TagConsistenceQA"));
		valueMap.put(QAConstant.QA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.TagConsistenceQA");
		qaItemId_Name_Class.put(QAConstant.QA_TAG, valueMap);
		
		//非译元素检查
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.QA_ITEM_NAME, Messages.getString("qa.all.qaItem.NonTranslationQA"));
		valueMap.put(QAConstant.QA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.NonTranslationQA");
		qaItemId_Name_Class.put(QAConstant.QA_NONTRANSLATION, valueMap);
		
		//段首、段末空格检查
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.QA_ITEM_NAME, Messages.getString("qa.all.qaItem.SpaceOfParaCheck"));
		valueMap.put(QAConstant.QA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.SpaceOfParaCheckQA");
		qaItemId_Name_Class.put(QAConstant.QA_SPACEOFPARACHECK, valueMap);

		//文本段完整性检查
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.QA_ITEM_NAME, Messages.getString("qa.all.qaItem.ParaCompletenessQA"));
		valueMap.put(QAConstant.QA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.ParaCompletenessQA");
		qaItemId_Name_Class.put(QAConstant.QA_PARACOMPLETENESS, valueMap);
		
		//目标文本段长度限制检查
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.QA_ITEM_NAME, Messages.getString("qa.all.qaItem.TgtTextLengthLimitQA"));
		valueMap.put(QAConstant.QA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.TgtTextLengthLimitQA");
		qaItemId_Name_Class.put(QAConstant.QA_TGTTEXTLENGTHLIMIT, valueMap);
		
		//拼写检查
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.QA_ITEM_NAME, Messages.getString("qa.all.qaItem.SpellQA"));
		valueMap.put(QAConstant.QA_ITEM_CLASSNAME, "net.heartsome.cat.ts.ui.qa.SpellQA");
		qaItemId_Name_Class.put(QAConstant.QA_SPELL, valueMap);
	}
	
	/**
	 * 获取内置非译元素，包括检查ip地址，web地址，邮件地址
	 * @return
	 */
	public static final List<NontransElementBean> getInterNonTransElements(){
		List<NontransElementBean> interNonTransElements = new ArrayList<NontransElementBean>();
		//添加内置ip
		String id = "qaInternalNonTrans_ip";
		String name = Messages.getString("qa.model.QAModel.ipAdd");
		String regular = "\\b(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}(:\\d{1,})?\\b";
		String content = "";
		interNonTransElements.add(new NontransElementBean(id, name, content, TextUtil.cleanSpecialString(regular)));
		
		//添加内置非译元素，web地址
		id = "qaInternalNonTrans_web";
		name = Messages.getString("qa.model.QAModel.webAdd");
//		regular = "((http|https|ftp)://(www.)?((((\\w+)+[.])*(net|com|cn|org|cc|tv|[0-9]{1,3}(:\\d{1,})?)))(/([+-_.=%&?#|]?(\\w+)[+-_.=%&?#|]?)*)*)";
		/**  burke  修改内置非译元素检查，web地址检查不全面BUG，修改正则表达式regular  */
//		regular = "(((http|https|ftp|gopher|wais)://)?((\\w+|[-]*)+:(\\w+|[-]*)+@)?((www[.])?((\\w+|[-]*)+[.])+(net|com|org|mil|gov|edu|int)([.](cn|hk|uk|sg|us|jp|cc|tv))?|(\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}))(:\\d{1,})?(/?)([+-_.=%&?#|/]*([\\w\\S]+)[+-_.=%&?#|/]*)*(/?))";
		//这是最新版的web地址匹配	robert	2012-05-22
//		regular = "(((http|https|ftp|gopher|wais)://)?(www\\.)?((([^,，\\s])*([\\.](net|com|cn|org|cc|tv|hk|uk|sg|us|jp|mil|gov|edu|int)|(:port)|[0-9]{1,3}(:\\d{1,})?)))([^,，\\s])*)";
//		regular = "(((http|https|ftp|gopher|wais)://)?(www\\.)?(([^,，\\s])*(\\.(net|com|cn|org|cc|tv|hk|uk|sg|us|jp|mil|gov|edu|int)))(((:port)|[0-9]{1,3}(:\\d{1,})?)?)([^,，\\s])*)";
		regular = "\\b(((http|https|ftp|gopher|wais)://)?(www\\.)(([^,，\\s])*(\\.(net|com|cn|org|cc|tv|hk|uk|sg|us|jp|mil|gov|edu|int)))(((:port)|[0-9]{1,3}(:\\d{1,})?)?)([^,，\\s])*)\\b" +
				"|\\b(((http|https|ftp|gopher|wais)://)(www\\.)?(([^,，\\s])*(\\.(net|com|cn|org|cc|tv|hk|uk|sg|us|jp|mil|gov|edu|int))?)(((:port)|[0-9]{1,3}(:\\d{1,})?)?)([^,，\\s])*)\\b";
		interNonTransElements.add(new NontransElementBean(id, name, content, TextUtil.cleanSpecialString(regular)));
		
		//添加内置非译元素，邮件地址
		id = "qaInternalNonTrans_email";
		name = Messages.getString("qa.model.QAModel.mailAdd");
//		regular = "\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+";
		regular = "\\b[-\\w]+([-.]\\w+)*@\\w+([-.]\\w+)*\\.([A-Za-z])+\\b";
		interNonTransElements.add(new NontransElementBean(id, name, content, TextUtil.cleanSpecialString(regular)));

		return interNonTransElements;
	}
	
	
	public List<IFile> getQaXlfList() {
		return qaXlfList;
	}
	public void setQaXlfList(List<IFile> qaXlfList) {
		this.qaXlfList = qaXlfList;
	}
	public boolean isBatchQA() {
		return isBatchQA;
	}
	public void setBatchQA(boolean isBatchQA) {
		this.isBatchQA = isBatchQA;
	}
	/**
	 * 首选项中的各项检查的主键的集合,它里面的值的存储进程是在BatchQAHandler中进行的。
	 */
	public LinkedList<String> getBatchQAItemIdList() {
		return batchQAItemIdList;
	}
	public void setBatchQAItemIdList(LinkedList<String> batchQAItemIdList) {
		this.batchQAItemIdList = batchQAItemIdList;
	}
	public Map<String, HashMap<String, String>> getQaItemId_Name_Class() {
		return qaItemId_Name_Class;
	}
	public Shell getShell() {
		return shell;
	}
	public void setShell(Shell shell) {
		this.shell = shell;
	}
	public boolean isNumberSameToSource() {
		return numberSameToSource;
	}
	public void setNumberSameToSource(boolean numberSameToSource) {
		this.numberSameToSource = numberSameToSource;
	}
	public boolean isNumberBoundarySign() {
		return numberBoundarySign;
	}
	public void setNumberBoundarySign(boolean numberBoundarySign) {
		this.numberBoundarySign = numberBoundarySign;
	}
	public Map<String, Boolean> getNotInclude() {
		return notInclude;
	}
	public void setNotInclude(Map<String, Boolean> notInclude) {
		this.notInclude = notInclude;
	}
	
	public boolean isMuliFiles() {
		return isMuliFiles;
	}

	public void setMuliFiles(boolean isMuliFiles) {
		this.isMuliFiles = isMuliFiles;
	}

	public List<String> getRowIdsList() {
		return rowIdsList;
	}

	public void setRowIdsList(List<String> rowIdsList) {
		this.rowIdsList = rowIdsList;
	}

	public MultiFilesOper getMultiOper() {
		return multiOper;
	}

	public void setMultiOper(MultiFilesOper multiOper) {
		this.multiOper = multiOper;
	}
	

}
