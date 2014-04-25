package net.heartsome.cat.ts.ui.preferencepage.translation;

/**
 * 翻译/预翻译定义的常量类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public interface ITranslationPreferenceConstants {
	
	/**
	 * 接受翻译时是否调整空格位置
	 */
	String AUTO_ADAPT_SPACE_POSITION = "net.heartsome.cat.ts.ui.preferencepage.translation.autoAdaptSpacePosition";

	/**
	 * 无译文时自动应用最高记忆库匹配
	 */
	String AUTO_APPLY_TM_MATCH = "net.heartsome.cat.ts.ui.preferencepage.translation.autoApplyTmMatch";
	
	/**
	 * 无匹配时是否复制源文本到目标
	 */
	String COPY_SOURCE_TO_TARGET = "net.heartsome.cat.ts.ui.preferencepage.translation.copySourceToTarget";
	
	/**
	 * 是否跳过不可翻译的文本段
	 */
	String SKIP_NOT_TRANSLATE_TEXT = "net.heartsome.cat.ts.ui.preferencepage.translation.skipNotTranslateText";

	/**
	 * 自动快速翻译
	 */
	String AUTO_QUICK_TRANSLATION = "net.heartsome.cat.ts.ui.preferencepage.translation.autoQuickTranslation";
	
	/**
	 * OpenOffice 路径
	 */
	String PATH_OF_OPENOFFICE = "net.heartsome.cat.ts.ui.preferencepage.translation.pathOfOpenOffice";
	
	/**
	 * OpenOffice 端口
	 */
	String PORT_OF_OPENOFFICE = "net.heartsome.cat.ts.ui.preferencepage.translation.portOfOpenOffice";
	
	/**
	 * 是否启用
	 */
	String ENABLED_OF_OPENOFFICE = "net.heartsome.cat.ts.ui.preferencepage.translation.enabledOfOpenOffice";
	
}
