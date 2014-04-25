package net.heartsome.cat.ts.test.ui.constants;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.heartsome.cat.common.locale.LocaleService;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

/**
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public final class TsUIConstants {

	private static final String BUNDLE_NAME = "net.heartsome.cat.ts.test.ui.constants.TsUIConstants"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * 私有构造函数
	 */
	private TsUIConstants() {
		// private constructor
	}

	/**
	 * 功能入口
	 * @author felix_lu
	 * @version
	 * @since JDK1.6
	 */
	public enum Entry {
		MENU, TOOLBAR, SHORTCUT, CONTEXT_MENU, CONTEXT_MENU_ALT, DOUBLE_CLICK,
	}

	/**
	 * 预期结果
	 * @author felix_lu
	 * @version
	 * @since JDK1.6
	 */
	public enum ExpectedResult {
		SUCCESS, DUPLICATED_NAME, INVALID_NAME, NO_FILE, NO_DB, FILE_ERROR, WRONG_TYPE, INVALID_FILE, INVALID_PATH, NO_SERVER, NO_PORT, NO_INSTANCE, NO_USERNAME, NO_PATH, CONNECTION_ERROR, LONG_NAME,
	}

	/**
	 * 更新策略
	 * @author felix_lu
	 * @version
	 * @since JDK1.6
	 */
	public enum UpdateMode {
		ALWAYS_ADD, OVERWRITE, IGNORE, MERGE, KEEP_CURRENT, OVERWRITE_IF_HIGHER
	}

	/**
	 * 数据库类型
	 * @author felix_lu
	 * @version
	 * @since JDK1.6
	 */
	public enum DB {
		INTERNAL, MYSQL, ORACLE, POSTGRESQL, MSSQL,
	}

	/**
	 * 导入类型
	 * @author felix_lu
	 * @version
	 * @since JDK1.6
	 */
	public enum ImportType {
		TMX, TBX,
	}

	/**
	 * 资源类型
	 * @author felix_lu
	 * @version
	 * @since JDK1.6
	 */
	public enum ResourceType {
		PROJECT, FOLDER, FILE,
	}
	
	/**
	 * 标记样式
	 * @author felix_lu
	 *
	 */
	public enum TagStyle {
		INDEX, SIMPLE, FULL,
	}

	/**
	 * 根据 key，获得相应的 value 值
	 * @param key
	 * @return ;
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * @param langCode
	 *            语言代码
	 * @return 字符串：语言代码 ＋ 空格 ＋ 语言名称;
	 */
	public static String getLang(String langCode) {
		String lang = LocaleService.getLanguage(langCode);
		assertTrue("未找到该语言代码对应的语言：" + langCode, !"".equals(lang));
		return lang;
	}

	/**
	 * @param langCodes
	 *            语言代码数组
	 * @return 语言数组，每个值为字符串：语言代码 ＋ 空格 ＋ 语言名称;
	 */
	public static String[] getLangs(String... langCodes) {
		int len = langCodes.length;
		String[] langs = new String[len];
		for (int i = 0; i < len; i++) {
			langs[i] = getLang(langCodes[i]);
		}
		return langs;
	}

	/**
	 * @param langCodes
	 * @return 语言 List;
	 */
	public static List<String> getLangs(List<String> langCodes) {
		List<String> lang = new ArrayList<String>();
		for (String langCode : langCodes) {
			lang.add(getLang(langCode));
		}
		return lang;
	}

	/**
	 * @param bot
	 *            对话框的 SWTBot 对象
	 * @param buttonKey
	 *            按钮上的文字标签
	 * @param index
	 *            索引号
	 * @return
	 */
	public static SWTBotButton button(SWTBot bot, String buttonKey, int index) {
		return bot.button(getString(buttonKey), index);
	}

	/**
	 * @param bot
	 *            对话框的 SWTBot 对象
	 * @param buttonKey
	 *            按钮上的文字标签
	 * @return
	 */
	public static SWTBotButton button(SWTBot bot, String buttonKey) {
		return bot.button(getString(buttonKey));
	}

	/**
	 * @param bot
	 *            对话框的 SWTBot 对象
	 * @param labelKey
	 *            列表的文字标签
	 * @return
	 */
	public static SWTBotList listWithLabel(SWTBot bot, String labelKey) {
		return bot.listWithLabel(getString(labelKey));
	}

	/**
	 * @param bot
	 * @param valueKey
	 * @return ;
	 */
	public static SWTBotCombo comboBox(SWTBot bot, String valueKey) {
		return bot.comboBox(getString(valueKey));
	}

	/**
	 * @param bot
	 * @param labelKey
	 * @return ;
	 */
	public static SWTBotCombo comboBoxWithLabel(SWTBot bot, String labelKey) {
		return bot.comboBoxWithLabel(getString(labelKey));
	}

	/**
	 * @param bot
	 * @param textKey
	 * @return ;
	 */
	public static SWTBotText text(SWTBot bot, String textKey) {
		return bot.text(getString(textKey));
	}

	/**
	 * @param bot
	 * @param labelKey
	 * @return ;
	 */
	public static SWTBotText textWithLabel(SWTBot bot, String labelKey) {
		return bot.textWithLabel(getString(labelKey));
	}

	/**
	 * @param bot
	 * @param radioKey
	 * @return ;
	 */
	public static SWTBotRadio radio(SWTBot bot, String radioKey) {
		return bot.radio(getString(radioKey));
	}
}
