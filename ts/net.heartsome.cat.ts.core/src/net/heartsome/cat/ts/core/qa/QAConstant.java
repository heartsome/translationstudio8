package net.heartsome.cat.ts.core.qa;

import java.io.File;

import net.heartsome.cat.ts.core.resource.Messages;

/**
 * 质量检查中用到的常量
 * 文件分析中用到的常量
 * @author robert
 *
 */
public class QAConstant {
	/**
	 * 操作成功。
	 */
	public static final int RETURNVALUE_RESULT_SUCCESSFUL = 1;
	/**
	 * 返回值中的操作结果。
	 */
	public static final String RETURNVALUE_RESULT = "result";
	/**
	 * 调试模式。该模式下将在控制台输出所有的异常堆栈。
	 */
	public static final int MODE_DEBUG = 0;
	/**
	 * 当前系统运行模式。
	 */
	public static int RUNNING_MODE = 0;
	/**
	 * 操作失败。
	 */
	public static final int RETURNVALUE_RESULT_FAILURE = 0;
	/**
	 * 返回值中的消息文本。
	 */
	public static final String RETURNVALUE_MSG = "msg";
	/**
	 * 返回值中的异常对象。
	 */
	public static final String RETURNVALUE_EXCEPTION = "exception";
	
	public static final String RETURNVALUE_RESULT_RETURN = "progress_monitor_return";
	
	//----------------------------------------------------------------------//
	//---------------------程序中数字或字符串常量-------------------------------//
	//----------------------------------------------------------------------//
	
	public static final int QA_ZERO = 0;
	public static final int QA_FIRST = 1;
	public static final int QA_TWO = 2;
	public static final int QA_THREE = 3;
	/** 键盘上回车键的键码 */
	public static final int QA_CENTERKEY_1 = 13;
	/** 小键盘上回车键的键码 */
	public static final int QA_CENTERKEY_2 = 16777296;
	/** 空字符串，防止在代码过程中出现"  "的情况 */
	public static final String QA_NULL_STRING = "";
	/** 一个空格的字符串 */
	public static final String QA_ONE_BLANK = " ";
	/** 两个空格的字符串 */
	public static final String QA_TWO_BLANK = "  ";
	/** 一个空格字符 */
	public static final char QA_ONE_SPACE_CHAR = ' ';
	
	
	//----------------------------------------------------------------------//
	//--------trans-unit节点中的source和target的可能包括的行内标记名称常量---------//
	//----------------------------------------------------------------------//
	/** trans-unit节点下source与target节点的子节点<g>, 其值为text */
	public static final String QA_g = "g";
	/** trans-unit节点下source与target节点的子节点<x>, 其值为 空 */
	public static final String QA_x = "x";
	/** trans-unit节点下source与target节点的子节点<bx>, 其值为 空 */
	public static final String QA_bx = "bx";
	/** trans-unit节点下source与target节点的子节点<ex>, 其值为 空 */
	public static final String QA_ex = "ex";
	/** trans-unit节点下source与target节点的子节点<bpt>, 其值为 code, 或sub */
	public static final String QA_bpt = "bpt";
	/** trans-unit节点下source与target节点的子节点<ept>, 其值为 code, 或sub */
	public static final String QA_ept = "ept";
	/** trans-unit节点下source与target节点的子节点<ph>, 其值为 code, 或sub */
	public static final String QA_ph = "ph";
	/** trans-unit节点下source与target节点的子节点<it>, 其值为 code, 或sub */
	public static final String QA_it = "it";
	/** trans-unit节点下source与target节点的子节点<mrk>, 其值为 text */
	public static final String QA_mrk = "mrk";
	/** trans-unit节点下source与target节点的子节点<sub>, 其值为 text */
	public static final String QA_sub = "sub";
	
	//----------------------------------------------------------------------//
	//---------------------品质检查项的常量------------------------------------//
	//----------------------------------------------------------------------//
	/** 术语一致性检查 */
	public static final String QA_TERM = "term";
	/** 文本段一致性检查 */
	public static final String QA_PARAGRAPH = "paragraph";
	/** 数字一致性检查 */
	public static final String QA_NUMBER = "number";
	/** 标记一致性检查 */
	public static final String QA_TAG = "tag";
	/** 非译元素检查 */
	public static final String QA_NONTRANSLATION = "nonTranslation";
	/** 段首段末空格检查 */
	public static final String QA_SPACEOFPARACHECK = "spaceOfParaCheck";
	/** 文本段完整性检查 */
	public static final String QA_PARACOMPLETENESS = "paragraphCompleteness";	
	/** 目标文本段长度限制检查 */
	public static final String QA_TGTTEXTLENGTHLIMIT = "targetTextLengthLimit";
	/** 拼写检查 */
	public static final String QA_SPELL = "spell";
	
	
	
	/** 品质检查项中存放品质检查名的key值常量 */
	public static final String QA_ITEM_NAME = "qaItemName";
	/** 品质检查项中存放品质检查类名的key值常量 */
	public static final String QA_ITEM_CLASSNAME = "qaItemClassName";
	
	//----------------------------------------------------------------------//
	//---------------------标记一致性检查常量----------------------------------//
	//----------------------------------------------------------------------//
	/** 标记一致性检查中的标记名称 */
	public static final String QA_TAGNAME = "tagName";
	/** 标记一致性检查中的标记内容 */
	public static final String QA_TAGCONTENT = "tagContent";
	
	
	//-----------------------非译元素-----------------------
	public static final String QA_NONTRANS_ID = "id";
	public static final String QA_NONTRANS_NAME = "name";
	public static final String QA_NONTRANS_CONTENT = "content";
	public static final String QA_NONTRANS_REGULAR = "regular";
	public static final String QA_NONTRANS_config = "config";
	
	//非译元素正则表达式的配置的常量
	/** 起始字符 */
	public static final String QA_NONTRANS_STARTCHAR = Messages.getString("qa.QAConstant.startChar");
	/** 结束字符 */
	public static final String QA_NONTRANS_ENDCHAR = Messages.getString("qa.QAConstant.endChar");
	/** 起始和结束字符 */
	public static final String QA_NONTRANS_STARTANDENDCHAR = Messages.getString("qa.QAConstant.startAndEndChar");
	/** 前一字符 */
	public static final String QA_NONTRANS_PRECHAR = Messages.getString("qa.QAConstant.preChar");
	/** 后一字符 */
	public static final String QA_NONTRANS_LASTCHAR = Messages.getString("qa.QAConstant.lastChar");
	/** 前后字符 */
	public static final String QA_NONTRANS_PREANDLASTCHAR = Messages.getString("qa.QAConstant.preAndLastChar");
	/** 所有字符 */
	public static final String QA_NONTRANS_ALLCHAR = Messages.getString("qa.QAConstant.allChar");
	
	
	/** 等于 */
	public static final String QA_NONTRANS_EQUALS = Messages.getString("qa.QAConstant.eq");
	/** 不等于 */
	public static final String QA_NONTRANS_NOTEQUALS = Messages.getString("qa.QAConstant.neq");
	
	/** 空格 */
	public static final String QA_NONTRANS_SPACE = Messages.getString("qa.QAConstant.space");
	/** 逗号 */
	public static final String QA_NONTRANS_COMMA = Messages.getString("qa.QAConstant.comma");
	/** 大写 */
	public static final String QA_NONTRANS_CAPITAL = Messages.getString("qa.QAConstant.upper");	
	/** 小写 */
	public static final String QA_NONTRANS_LOWERCASE = Messages.getString("qa.QAConstant.lower"); 
	
	/** 正则表达式配置位置下拉框的值 */
	public static final String[] QA_NONTRANS_ARRAY_POSITION = new String[] { QA_NONTRANS_STARTCHAR,
			QA_NONTRANS_ENDCHAR, QA_NONTRANS_STARTANDENDCHAR, QA_NONTRANS_PRECHAR, QA_NONTRANS_LASTCHAR,
			QA_NONTRANS_PREANDLASTCHAR, QA_NONTRANS_ALLCHAR };
	/** 正则表达式配置 操作下拉框的值,等于，不等于 */
	public static final String[] QA_NONTRANS_ARRAY_OPERATE_EQUAL = new String[] {QA_NONTRANS_EQUALS, QA_NONTRANS_NOTEQUALS };
	/** 正则表达式值下拉框的值，大写，小写 */
	public static final String[] QA_NONTRANS_ARRAY_VALUE_LC = new String[] {QA_NONTRANS_CAPITAL, QA_NONTRANS_LOWERCASE };	
	/** 正则表达式值下拉框的值,空格，逗号 */
	public static final String[] QA_NONTRANS_ARRAY_VALUE_SC = new String[] { QA_NONTRANS_SPACE, QA_NONTRANS_COMMA};	
	
	
	//-----------------------拼写检查-----------------------
	/** 拼写检查存放 hunspell 与 aspell 文件的位置 */
	public static final String QA_SPELL_preferenceFolder = ".metadata/.preference";
	/** 拼写检查中aspell检查器配置文件的路径 */
	public static final String QA_SPELL_ASPELLCONFIGFILE = ".metadata/.preference/aspellConfigure.xml";
	/** 拼写检查中 hunspell 检查器配置文件的路径 */
	public static final String QA_SPELL_hunspellConfigFile = "net.heartsome.cat.ts.ui/hunspell/spellcheck_config.xml";
	/** 拼写检查中 hunspell 检查器词典所在的文件夹 */
	public static final String QA_SPELL_hunspellDictionaryFolder = ".metadata/.preference/hunspell/hunspellDictionaries";
	/** 拼写检查中 hunspell 运行库所处的文件夹 */
	public static final String QA_SPELL_hunspellLibraryFolder = ".metadata/.preference/hunspell/native-library";
	/** 拼写检查中 hunspell 所有东有所存放的位置 (相对于工作空间) */
	public static final String QA_SPELL_hunspellFolder = ".metadata/.preference/hunspell"; 
	
	
	//----------------------------------------------------------------------//
	//------------------------首选项中的常量-----------------------------------//
	//---------------常量规则为qa_首选项＿检查项＿检查项下值,----------------------//
	//----------------------------------------------------------------------//
	//-----------------------品质检查
	/** 不包括上下文匹配的文本段 */
	public static final String QA_PREF_CONTEXT_NOTINCLUDE = "qa_pref_context_notInclude";
	/** 不包括完全匹配的文本段 */
	public static final String QA_PREF_FULLMATCH_NOTINCLUDE = "qa_pref_fullMatch_totInclude";
	/** 不包括已锁定的文本段 */
	public static final String QA_PREF_LOCKED_NOTINCLUDE = "qa_pref_locked_notInclude";
	/** 是否检查目标文本段最小长度 */
	public static final String QA_PREF_isCheckTgtMinLength = "qa_pref_isCheckTgtMinLength";
	/** 是否检查目标文本段最大长度 */
	public static final String QA_PREF_isCheckTgtMaxLength = "qa_pref_isCheckTgtMaxLength";
	/** 目标文本段长度限制检查的最小长度值－－保存的是相对值 */
	public static final String QA_PREF_tgtMinLength = "qa_pref_tgtMinLength";
	/** 目标文本段长度限制检查的最大长度值－－保存的是相对值 */
	public static final String QA_PREF_tgtMaxLength = "qa_pref_tgtMaxLength";

	
	//-----------------------文本段一致性检查
	/** 相同源文不同译文，备注：默认选中 */
	public static final String QA_PREF_PARA_SAMESOURCE = "qa_pref_para_sameSource";
	/** 相同源文不同译文下的忽略大小写 */
	public static final String QA_PREF_PARA_SRC_IGNORCECASE = "qa_pref_para_src_ignorceCase";
	/** 相同源文不同译文下的忽略标记 */
	public static final String QA_PREF_PARA_SRC_IGNORCETAG = "qa_pref_para_src_ignorceTag";
	/** 相同译文不同源文 */
	public static final String QA_PREF_PARA_SAMETARGET = "qa_pref_para_sameTarget";
	/** 相同译文不同源文下的忽略大小写 */
	public static final String QA_PREF_PARA_TAR_IGNORCECASE = "qa_pref_para_tar_ignorceCase";
	/** 相同译文不同源文下的忽略标记 */
	public static final String QA_PREF_PARA_TAR_IGNORCETAG = "qa_pref_para_tar_ignorceTag";
	
	
	//-----------------------品质检查设置(包括自动检查，批量检查)
	/** 自动检查时要检查的检查项，以","进行组合 */
	public static final String QA_PREF_AUTO_QAITEMS = "qa_pref_auto_qaItems";
	/** 批量检查时要检查的检查项，以","进行组合, 默认选择所有的 */
	public static final String QA_PREF_BATCH_QAITEMS = "qa_pref_batch_qaItems";
	/** 自动检查的时间，默认为入库时执行，当此值等于0时：为从不执行;等于1时，为入库时执行;等于2时，为批准文本段时执行; */
	public static final String QA_PREF_AUTO_QARUNTIME = "qa_pref_auto_qaRuntime";
	
	/** 术语一致性检查提示级别，若为0，则为错误，若值为1,则为警告 */
	public static final String QA_PREF_term_TIPLEVEL = "qa_pref_term_tipLevel";
	/** 文本段一致性检查提示级别，若为0，则为错误，若值为1,则为警告 */
	public static final String QA_PREF_para_TIPLEVEL = "qa_pref_para_tipLevel";
	/** 数字一致性检查提示级别，若为0，则为错误，若值为1,则为警告 */
	public static final String QA_PREF_number_TIPLEVEL = "qa_pref_number_tipLevel";
	/** 标记一致性检查提示级别，若为0，则为错误，若值为1,则为警告 */
	public static final String QA_PREF_tag_TIPLEVEL = "qa_pref_tag_tipLevel";
	/** 非译元素检查提示级别，若为0，则为错误，若值为1,则为警告 */
	public static final String QA_PREF_nonTrans_TIPLEVEL = "qa_pref_nonTrans_tipLevel";
	/** 段首段末空格检查提示级别，若为0，则为错误，若值为1,则为警告 */
	public static final String QA_PREF_spaceOfPara_TIPLEVEL = "qa_pref_spaceOfPara_tipLevel";
	/** 文本段完整性检查提示级别，若为0，则为错误，若值为1,则为警告 */
	public static final String QA_PREF_paraComplete_TIPLEVEL = "qa_pref_paraComplete_tipLevel";
	/** 目标文本段长度限制检查提示级别，若为0，则为错误，若值为1,则为警告 */
	public static final String QA_PREF_tgtLengthLimit_TIPLEVEL = "qa_pref_tgtLengthLimit_tipLevel";
	/** 拼写检查提示级别，若为0，则为错误，若值为1,则为警告 */
	public static final String QA_PREF_spell_TIPLEVEL = "qa_pref_spell_tipLevel";
	
	//-----------------------非译元素
	/** 标志非译元素是否发生改变，若改变，其值递加，这个参数目前只用于实时拼写检查 */
	public static final String QA_PREF_nonTrans_changeTag = "qa_pref_nonTrans_changeTag";

	//-----------------------拼写检查
	/** 是否是 hunspell 拼写检查 */
	public static final String QA_PREF_isHunspell = "qa_pref_isHunspell";
	/** 是否进行实时拼写检查 */
	public static final String QA_PREF_realTimeSpell = "qa_pref_realTimeSpell";
	/** 是否忽略非译元素 */
	public static final String QA_PREF_ignoreNontrans = "qa_pref_ignoreNontrans";
	/** 是否忽略单词首字母为数字 */
	public static final String QA_PREF_ignoreDigitalFirst = "qa_pref_ignoreDigitalFirst";
	/** 是否忽略单词首字母为大写 */
	public static final String QA_PREF_ignoreUpperCaseFirst = "qa_pref_ignoreUpperCaseFirst";
	/** 忽略全大写单词 */
	public static final String QA_PREF_ignoreAllUpperCase = "qa_pref_ignoreAllUpperCase";
	/** 标志 aspell 配置是否发生改变，若改变，其值递加，这个参数目前只用于实时拼写检查 */
	public static final String QA_PREF_aspellConfig_changeTag = "qa_pref_aspellConfig_changeTag";
	
	
	/** >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> */
	/** >>>>>>>>>>>>>>>>>>>>>>>>>>>   文件分析中的常量	  2011-12-07  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> */
	/** >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> */
	
	//--------------------文件分析首选项中的常量
	/** 是否忽略大小写 */
	public static final String FA_PREF_ignoreCase = "ts.ui.qa.fa.preference.ignoreCase";
	/** 是否忽略标记 */
	public static final String FA_PREF_ignoreTag = "ts.ui.qa.fa.preference.ignoreTag";
	/** 匹配的上下文个数 */
	public static final String FA_PREF_contextNum = "ts.ui.qa.fa.preference.contextNum";
	/** 罚分制度 */
	public static final String FA_PREF_tagPenalty = "ts.ui.qa.fa.preference.tagPenalty";
	/** 是否检查内部重复 */
	public static final String FA_PREF_interRepeate = "ts.ui.qa.fa.preference.interRepeate";
	/** 是否检查内部匹配 */
	public static final String FA_PREF_interMatch = "ts.ui.qa.fa.preference.interMatch";

	
	/** 加权系数的设置，保存格式为"100-101:0.5;89-99:0.4" */
	public static final String FA_PREF_equivalent = "ts.ui.qa.fa.preference.equivalent";
	
	
	//--------------------文件分析代码中常量
	/** 品质检查项中存放品质检查名的key值常量 */
	public static final String FA_ITEM_NAME = "qaItemName";
	/** 品质检查项中存放品质检查类名的key值常量 */
	public static final String FA_ITEM_CLASSNAME = "qaItemClassName";
	
	/** 文件分析的字数分析 */
	public static final String FA_WORDS_ANALYSIS = "faWordsAnalysis";
	/** 文件分析的翻译进度分析 */
	public static final String FA_TRANSLATION_PROGRESS_ANALYSIS = "faTranslationProgressAnalysis";
	/** 文件分析的编辑进度分析 */
	public static final String FA_EDITING_PROGRESS_ANALYSIS = "faEditingProgressAnalysis";
	
	/** trans-unit节点的唯一标识符 */
	public static final String FA_SRC_TU_ROWID = "faSourceTURowId";
	/** source节点的内容，包括标记 */
	public static final String FA_SRC_CONTENT = "faSourceContent";
	/** source节点的纯文本 */
	public static final String FA_SRC_PURE_TEXT = "faSourcePureText";
	/** 目标语言 */
	public static final String FA_TARGET_LANG = "faTargetLanguage";
	/** 源文本的上文的hash码 */
	public static final String FA_PRE_CONTEXT_CODE = "faPreContextHashCode";

	/** 源文本的下文的hash码 */
	public static final String FA_NEXT_CONTEXT_CODE = "faNextContextHashCode";
	/** trans-unit节点是否锁定 */
	public static final String FA_IS_LOCKED = "faTransUnitIsLocked";
	
	
	
	public static final String FA_SEPERATOR = "/".equals(File.separator) ? "/" : "\\\\";
	
	/** 文件分析结果html文件的文档类型 */
	public static final String FA_HtmlDoctype = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n";
	
	/**
	 * 这是 html 报告前面的信息提示
	 */
	public static final String FA_Report_Info = "		<tr>\n" +
			"			<td class=\"infoTitle\" align=\"right\" valign=\"top\" width=\"1%\"><nobr>###Title###</nobr></td>\n" +
			"			<td class=\"infoContent\" valign=\"top\">###Content###</td>\n" +
		"		</tr>\n";
	
	public static final String FA_HtmlBrowserEditor = "net.heartsome.cat.ts.ui.editor.HtmlBrowser";
	
	/** 这是文件分析结果html文件的头元素，包括css，javascript，其中的javascript代码是控制文件夹的关闭与打开的 */
	public static final String FA_htmlHeader = "  <head>\n" +
			"    <title>###Title###</title>\n" +
			"    <meta http-equiv=\"keywords\" content=\"keyword1,keyword2,keyword3\">\n" +
			"    <meta http-equiv=\"description\" content=\"this is my page\">\n" +
			"    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n" +
			"    <style type=\"text/css\">\n" +
			"		BODY{padding-left: 5px;padding-right: 5px;}\n" +
			"		.fieldsetStyle {border: 1px solid #B5B8C8;padding: 6px;display: block;font-size: 9pt;}\n" +
			"		.legendStyle {font-size: 9pt;color: #15428B;}\n" +
			"		A.link:LINK {FONT: bold 9pt;COLOR: #3366CC;TEXT-DECORATION: none;}\n" +
			"		A.link:VISITED {FONT-SIZE: 9pt;COLOR: #3366CC;TEXT-DECORATION: none;}\n" +
			"		A.link:ACTIVE {FONT-SIZE: 9pt;COLOR: blue;TEXT-DECORATION: none;}\n" +
			"		A.link:HOVER {FONT-SIZE: 10pt;COLOR: blue;TEXT-DECORATION: none;}\n" +
			"		.title{FONT-SIZE: 11pt;COLOR: #15428B;font-weight:bold;}\n" +
			"		.tableStyle{width: 100%;background-color: #95A5D2;}\n" +
			"		.headerTd{background-color: #F3F8FD;font-size: 9pt;height: 25;text-align: center;font-weight:bold;COLOR: #15428B;}\n" + 
			"		.fileTd {height: 24;padding-right: 6;font-size: 9pt;}\n" + 
			"		.folderTd{height: 25;font-size: 9pt;padding-right: 6;COLOR: #3366CC;}\n" + 
			"		.infoTableStyle{width: 100%;}\n" + 
			"		.infoTitle{font-size: 9pt;padding-right: 6;padding-bottom: 4px;COLOR: #3366CC;font-weight: bold;}" + 
			"       .infoContent{font-size: 9pt;padding-bottom: 4px; padding-left: 50px;}" +
			"	</style>\n" +
			"<script type=\"text/javascript\">\n" +
			"	function clickFolder(id, name){\n" +
			"		var folders = document.getElementsByTagName('a');\n" +
			"		for(var f = 0; f < folders.length; f++){\n" +
			"			var folder = folders[f];\n" +
			"			if(folder.name == name && folder.id == id){\n" +
			"				var status = folder.childNodes[0].innerHTML;\n" +
			"				if('-'==status){\n" +
			"					folder.childNodes[0].innerHTML='+';\n" +
			"					folder.title='" + Messages.getString("qa.QAConstant.expand") + "';\n" +
			"					var trNodes = document.getElementsByTagName('tr');\n" +
			"					for(var i = 0; i < trNodes.length; i++){\n" +
			"						var lastChar = trNodes[i].id.charAt(id.length);\n" +
			"						var trName = trNodes[i].getAttribute('name');\n" +
			"						if( trName == name && trNodes[i].id.indexOf(id) == 0 && (lastChar == '' || lastChar == '"+ FA_SEPERATOR +"' ) ){\n" +
			"							trNodes[i].style.display='none';\n" +
			"						}\n" +
			"					}\n" +
			"				}else if('+'==status){\n" +
			"					folder.childNodes[0].innerHTML='-';\n" +
			"					folder.title='" + Messages.getString("qa.QAConstant.collapse") + "';\n" +
			"					var trNodes = document.getElementsByTagName('tr');\n" +
			"					for(var i = 0; i < trNodes.length; i++){\n" +
			"						var trName = trNodes[i].getAttribute('name');\n" +
			"						var lastChar = trNodes[i].id.charAt(id.length);\n" +
			"						if( trName == name && trNodes[i].id.indexOf(id) == 0  && (lastChar == '' || lastChar == '"+ FA_SEPERATOR +"' ) ){\n" +
			"							var curFolders = document.getElementsByTagName('a');\n" +
			"							for(var j = 0; j < curFolders.length; j++){\n" +
			"								curFolder = curFolders[j];\n" +
			"								if(curFolder.name==name && curFolder.id==trNodes[i].id){\n" +
			"									var curStatus = curFolder.childNodes[0].innerHTML;\n" +
			"									if(curStatus=='-' && curFolder.parentNode.parentNode.style.display == ''){\n" +
			"										trNodes[i].style.display='';\n" +
			"									}\n" +
			"								}\n" +
			"							}\n" +
			"						}\n" +
			"					}\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n"+
			"</script>\n" +
			"</head>\n";
	
	/**
	 * 加权系数用到的常量。用于数据结果封装，不能本地化，这是内部重复
	 */
	public static final String _InternalRepeat = "internalRepeat";
	/**
	 * 加权系数用到的常量。用于数据结果封装，不能本地化，这是外部101%匹配
	 */
	public static final String _External101 = "external101";
	/**
	 * 加权系数用到的常量。用于数据结果封装，不能本地化，这是 外部 100％ 匹配。
	 */
	public static final String _External100 = "external100";
	
}