package net.heartsome.cat.common.core;

public class Constant {
	/**
	 * 当前系统运行模式。
	 */
	public static int RUNNING_MODE = 0;

	/**
	 * 调试模式。该模式下将在控制台输出所有的异常堆栈。
	 */
	public static final int MODE_DEBUG = 0;

	/**
	 * 部署模式。该模式下将在控制台输出所有的异常堆栈。
	 */
	public static final int MODE_DEPLOY = 1;

	/**
	 * 操作成功。
	 */
	public static final int RETURNVALUE_RESULT_SUCCESSFUL = 1;

	/**
	 * 操作失败。
	 */
	public static final int RETURNVALUE_RESULT_FAILURE = 0;

	/**
	 * 返回值中的操作结果。
	 */
	public static final String RETURNVALUE_RESULT = "result";

	/**
	 * 返回值。
	 */
	public static final String RETURNVALUE_VALUE = "value";

	/**
	 * 返回值中的消息文本。
	 */
	public static final String RETURNVALUE_MSG = "msg";

	/**
	 * 返回值中的异常对象。
	 */
	public static final String RETURNVALUE_EXCEPTION = "exception";

	/**
	 * 更改大小写模式－－全部大写
	 */
	public static final int CHANGECASE_UPPER = 1;

	/**
	 * 更改大小写模式－－全部小写
	 */
	public static final int CHANGECASE_LOWER = 2;

	/**
	 * 更改大小写模式－－句子大写
	 */
	public static final int CHANGECASE_SENTENCE = 3;

	/**
	 * 更改大小写模式－－标题大写
	 */
	public static final int CHANGECASE_TITLE = 4;

	/**
	 * 更改大小写模式－－全部切换
	 */
	public static final int CHANGECASE_TOGGLE = 5;

	/**
	 * 分隔符
	 */
	public static final String SEPARATORS_1 = " \r\n\f\t\u2028\u2029,.;\":<>?!()[]{}=+-/*\u00AB\u00BB\u201C\u201D\u201E\uFF00"; //$NON-NLS-1$
	
	/**
	 * 切割单词所用到的分割符，用于拼写检查等
	 */
	public static final String SEPARATORS = " \r\n\f\t\u2028\u2029\u00A0,.;\":<>¿?¡!()[]{}=-+/*\u00A7\u00A9\u00AE\u00D7\u2122\u2010\u2011\u2012\u2013\u2014\u2015\u00AB\u00BB\u22D6\u22D7\u227A\u227B\u201C\u201D\u201E\uFF00\uF000\uF001\uF002\uF003\u2310\u00AC\uE000\uE001"; //$NON-NLS-1$ 
	
	/**
	 * 产品版本－－Ultimate 旗舰版
	 */
	public static final int EDITION_UE = 4;

	/**
	 * 产品版本－－Professional 专业版
	 */
	public static final int EDITION_PRO = 3;

	/**
	 * 产品版本－－Personal 个人版
	 */
	public static final int EDITION_PE = 2;

	/**
	 * 产品版本－－LITE 精简版
	 */
	public static final int EDITION_LITE = 1;
	
	/**
	 * “源文件”文件夹
	 */
	public static final String FOLDER_SRC = "Source";

	/**
	 * “源文件”文件夹
	 */
	public static final String FOLDER_TGT = "Target";

	/**
	 * “XLIFF”文件夹
	 */
	public static final String FOLDER_XLIFF = "XLIFF";

	/**
	 * “SKL”文件夹
	 */
	public static final String FOLDER_SKL = "SKL";

	/**
	 * “Report“文件夹
	 */
	public static final String FOLDER_REPORT = "Report";
	
	/**
	 * Intermeddiate文件夹
	 */
	public static final String FOLDER_INTERMEDDIATE = "Intermediate";
	/**
	 * “其他”文件夹
	 */
	public static final String FOLDER_OTHER = "Other";

	/**
	 * 内部骨架文件类型
	 */
	public static final String SKL_INTERNAL_FILE = "internal-file";

	/**
	 * 导航视图ID
	 */
	public static final String NAVIGATOR_VIEW_ID = "net.heartsome.cat.common.ui.navigator.view";
	
	/**
	 * 项目下的子文件， .config 文件
	 */
	public static final String FILE_CONFIG = ".config";
	
	/**
	 * 产品打开时，删除　workbench.xml 中的编辑器记录，保存在如下文件中	robert	2013-05-16
	 */
	public static final String TEMP_EDITOR_HISTORY = ".metadata/.tempEditorHistory.xml";
	
	/**
	 * 产品中资源名称中不能出现的错误字符	robert	2013-05-26
	 */
	public static final char[] RESOURCE_ERRORCHAR = new char[]{'/', '\\', ':', '?', '"', '<', '>', '|'}; 
	
	/**
	 * 项目导出时的包后缀名	robert	2013-05-26
	 */
	public static final String PROJECT_PACK_EXTENSSION = ".hszip";

}
