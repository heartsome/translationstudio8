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
 * 项目已存在，是否要覆盖对话框
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class ProjectExistsDialog extends SWTBotShell {

	private SWTBot dialogBot = this.bot();
	private String name;

	/**
	 * 按钮标题查找对话框
	 */
	public ProjectExistsDialog() {
		super(HSBot.bot().shell(TsUIConstants.getString("dlgTitleProjectExists")).widget);
	}

	/**
	 * @param name
	 *            文件名，用于判断是否项目重名的提示
	 */
	public ProjectExistsDialog(String name) {
		this();
		this.name = name;
	}

	/************************************
	 * 常用方法
	 ************************************/

	/**
	 * @return 是表示出现项目已存在提示;
	 */
	public boolean isProjectExistsMsgVisible() {
		assertTrue("参数错误：未设置 name。", name != null);
		try {
			lblProjectExists(name).isVisible();
			return true;
		} catch (WidgetNotFoundException e) {
			return false;
		}
	}

	/************************************
	 * 界面映射
	 ************************************/

	/**
	 * @return 按钮：覆盖;
	 */
	public SWTBotButton btnYes() {
		return dialogBot.button(TsUIConstants.getString("btnYes"));
	}

	/**
	 * @return 按钮：不覆盖;
	 */
	public SWTBotButton btnNo() {
		return dialogBot.button(TsUIConstants.getString("btnNo"));
	}

	/**
	 * @param name 项目名称
	 * @return 文字标签：项目已存在的提示、并询问是否需要覆盖信息;
	 */
	public SWTBotLabel lblProjectExists(String name) {
		return dialogBot.label(MessageFormat.format(TsUIConstants.getString("lblProjectExists"), name, name));
	}
}
