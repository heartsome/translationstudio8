/**
 * IColorPreferenceConstant.java
 *
 * Version information :
 *
 * Date:2012-5-2
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.bean;

/**
 * 颜色配置首选项参数
 * @author jason
 * @version
 * @since JDK1.6
 */
public interface IColorPreferenceConstant {
	/** 繁殖翻译 */
	String PT_COLOR = "PT_COLOR";
	/** 快速翻译 */
	String QT_COLOR = "QT_COLOR";
	/** 机器翻译 */
	String MT_COLOR = "MT_COLOR";
	
	/** 上下文匹配 */
	String TM_MATCH101_COLOR = "TM_MATCH101_COLOR";

	/** 100% 匹配 */
	String TM_MATCH100_COLOR = "TM_MATCH100_COLOR";

	/** 90%-99% 匹配 */
	String TM_MATCH90_COLOR = "TM_MATCH90_COLOR";

	/** 80%-89% 匹配 */
	String TM_MATCH80_COLOR = "TM_MATCH80_COLOR";

	/** 70%-79% 匹配 */
	String TM_MATCH70_COLOR = "TM_MATCH70_COLOR";

	/** 小于 70% 匹配 */
	String TM_MATCH0_COLOR = "TM_MATCH0_COLOR";

	/** 源文本差异前景色 */
	String DIFFERENCE_FG_COLOR = "DIFFERENCE_FG_COLOR";

	/** 源文本差异背景色 */
	String DIFFERENCE_BG_COLOR = "DIFFERENCE_BG_COLOR";

	/** 标记背景色 */
	String TAG_BG_COLOR = "TAG_BG_COLOR";

	/** 标记背景色 */
	String TAG_FG_COLOR = "TAG_FG_COLOR";
	
	/** 错误标记色 */
	String WRONG_TAG_COLOR = "WRONG_TAG_COLOR";
	
	/** 术语高亮色 */
	String HIGHLIGHTED_TERM_COLOR = "HIGHLIGHTED_TERM_COLOR";
}
