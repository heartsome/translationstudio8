package net.heartsome.cat.ts.test.ui.dialogs;

import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * 重命名问题对话框
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class RenameProblemsDialog extends SWTBotShell {

	private SWTBot dialogBot = this.bot();
	private String name;
	private String invalidChar;

	/**
	 * 按钮标题查找对话框
	 */
	public RenameProblemsDialog() {
		super(HSBot.bot().shell(TsUIConstants.getString("dlgTitleRenameProblems")).widget);
	}

	/**
	 * @param name
	 *            文件名，用于判断是否出现 out of sync 或名称非法的提示
	 * @param invalidChar
	 *            若为名称非法的情况，需要指出其中的非法字符
	 */
	public RenameProblemsDialog(String name, String invalidChar) {
		this();
		this.name = name;
		if (invalidChar != null) {
			this.invalidChar = invalidChar;
		}
	}

	/************************************
	 * 常用方法
	 ************************************/

	/**
	 * @return 是表示出现 out of sync 提示
	 */
	public boolean isOutOfSync() {
		assertTrue("参数错误：未设置 name。", name != null);
		try {
			lblOutOfSync(name).isVisible();
			return true;
		} catch (WidgetNotFoundException e) {
			return false;
		}
	}

	/**
	 * @return 是表示出现名称非法提示;
	 */
	public boolean isInvalidMsgVisible() {
		assertTrue("参数错误：未设置 name 或 invalidChar。", (name != null && invalidChar != null));
		try {
			lblInvalidChar(name, invalidChar).isVisible();
			return true;
		} catch (WidgetNotFoundException e) {
			return false;
		}
	}

	/************************************
	 * 界面映射
	 ************************************/

	/**
	 * @return 按钮：确定;
	 */
	public SWTBotButton btnOK() {
		return dialogBot.button(TsUIConstants.getString("btnOK"));
	}

	/**
	 * @return 按钮：详情 未展开;
	 */
	public SWTBotButton btnDetailsCollapsed() {
		return dialogBot.button(TsUIConstants.getString("btnDetailsCollapsed"));
	}

	/**
	 * @return 按钮：详情 展开;
	 */
	public SWTBotButton btnDetailsExpanded() {
		return dialogBot.button(TsUIConstants.getString("btnDetailsExpanded"));
	}

	/**
	 * @param name
	 *            文件名，在信息中包含
	 * @return 文字标签：out of sync 信息的内容
	 */
	public SWTBotLabel lblOutOfSync(String name) { // FIXME: 新的 Out Of Sync 信息控件可能不是 Label 了。
		return dialogBot.label(MessageFormat.format(TsUIConstants.getString("lblOutOfSync"), name));
	}

	/**
	 * @param name
	 *            含非法字符的项目名
	 * @param invalidChar
	 *            名称中的非法字符
	 * @return 文字标签：项目名称非法的信息内容;
	 */
	public SWTBotLabel lblInvalidChar(String name, String invalidChar) {
		return dialogBot.label(MessageFormat.format(TsUIConstants.getString("lblInvalidChar"), invalidChar, name));
	}
}
