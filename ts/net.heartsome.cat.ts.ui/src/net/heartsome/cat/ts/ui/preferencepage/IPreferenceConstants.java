package net.heartsome.cat.ts.ui.preferencepage;

/**
 * TS 应用中定义的首选项常量.
 * @author cheney
 */
public interface IPreferenceConstants {

	/**
	 * 语言代码
	 */
	String LANGUAGECODE = net.heartsome.cat.common.core.IPreferenceConstants.LANGUAGECODE;
	
	/**
	 * 可忽略字符
	 */
	String IGNORABLE_CHARS = "ignorableChars";

	/**
	 * 标记罚分
	 */
	String TAG_PENALTY = "tagPenalty";

	/**
	 * 最后一次打开文件目录对话框所选择的目录路径
	 */
	String LAST_DIRECTORY = net.heartsome.cat.common.core.IPreferenceConstants.LAST_DIRECTORY;

	/**
	 * 是否启用 OO（Open Office），用以支持转换 MSOffice 2003。
	 */
	String AUTOMATIC_OO = net.heartsome.cat.common.core.IPreferenceConstants.AUTOMATIC_OO;

	/**
	 * 自动更新策略
	 */
	String SYSTEM_AUTO_UPDATE = "net.heartsome.cat.ts.ui.preferencepage.autoupdate";

	/**
	 * 启动时检查更新
	 */
	int SYSTEM_CHECK_UPDATE_WITH_STARTUP = 0;

	/**
	 * 每月检查更新
	 */
	int SYSTEM_CHECK_UPDATE_WITH_MONTHLY = 1;

	/**
	 * 每月检查更新时所选的日期
	 */
	String SYSTEM_CHECK_UPDATE_WITH_MONTHLY_DATE = "net.heartsome.cat.ts.ui.preferencepage.systemCheckUpdateWithMonthlyDate";

	/**
	 * 每周检查更新
	 */
	int SYSTEM_CHECK_UPDATE_WITH_WEEKLY = 2;

	/**
	 * 每周检查更新时所选的日期
	 */
	String SYSTEM_CHECK_UPDATE_WITH_WEEKLY_DATE = "net.heartsome.cat.ts.ui.preferencepage.systemCheckUpdateWithWeeklyDate";

	/**
	 * 立即检查更新
	 */
//	int SYSTEM_CHECK_UPDATE_WITH_IMMEDIATELY = 3;

	/**
	 * 从不检查更新
	 */
	int SYSTEM_CHECK_UPDATE_WITH_NEVER = 4;

	/**
	 * 用户界面语言
	 */
	String SYSTEM_LANGUAGE = "net.heartsome.cat.ts.ui.preferencepage.systemLanguage";

	/**
	 * 用户界面语言为英文
	 */
	int SYSTEM_LANGUAGE_WITH_EN = 0;

	/**
	 * 用户界面语言为简体中文
	 */
	int SYSTEM_LANGUAGE_WITH_ZH_CN = 1;

	/**
	 * 系统用户
	 */
	String SYSTEM_USER = "net.heartsome.cat.ts.ui.preferencepage.systemUser";

	/**
	 * XLIFF编辑器字体名称
	 */
	String XLIFF_EDITOR_FONT_NAME = "net.heartsome.cat.ts.ui.preferencepage.systemDefaultFontName";

	String XLIFF_EDITOR_SHOWHIDEN_NONPRINTCHARACTER = "net.heartsome.cat.ts.ui.nonPrinttingCharacter";
	/**
	 * XLIFF编辑器字体大小
    */
	String XLIFF_EDITOR_FONT_SIZE = "net.heartsome.cat.ts.ui.preferencepage.systemDefaultFontSize";

	/** 匹配结果面板字体名称 */
	String MATCH_VIEW_FONT_NAME = "net.heartsome.cat.ts.ui.preferencepage.matchViewFontName";
	/** 匹配结果面板字体大小 */
	String MATCH_VIEW_FONT_SIZE = "net.heartsome.cat.ts.ui.preferencepage.matchViewFontSize";


	
	//------------定义记住信息常量----------------//
	String NEW_PROJECT_SRC_LANG = "net.heartsome.cat.ts.ui.wizards.new.rm.srclang";
	
	String NEW_PROJECT_TGT_LANG = "net.heartsome.cat.ts.ui.wizards.new.rm.tgtlang";
	
	/** 标志程序是否是第一次运行	robert	2013-01-04 */
	String INITIAL_RUN = "net.heartsome.cat.ts.InitialRun";
}
