package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

/**
 * 创建库对话框
 * @author felix_lu
 */
public class CreateDatabaseDialog extends SWTBotShell {

	private SWTBot dialogBot = this.bot();

	/**
	 * 按标题识别对话框
	 */
	public CreateDatabaseDialog() {
		super(HSBot.bot().shell(TsUIConstants.getString("dlgTitleCreateDatabase")).widget);
	}

	/**
	 * @return 文本框：数据库名称;
	 */
	public SWTBotText txtWLblDatabaseName() {
		// return dialogBot.text();
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblDatabaseName"));
	}

	/**
	 * @return 文本框：数据库名称非法信息;
	 */
	public SWTBotText msgDBNameInvalid() {
		return dialogBot.text(TsUIConstants.getString("msgDBNameInvalid"));
	}

	/**
	 * @return 文本框：数据库名称过长信息;
	 */
	public SWTBotText msgDBNameTooLong() {
		return dialogBot.text(TsUIConstants.getString("msgDBNameTooLong"));
	}

	/**
	 * @return 文本框：数据库名称已存在信息;
	 */
	public SWTBotText msgDBExists() {
		return dialogBot.text(TsUIConstants.getString("msgDBExists"));
	}

	/**
	 * @return 按钮：确定;
	 */
	public SWTBotButton btnOK() {
		return dialogBot.button(TsUIConstants.getString("btnOK"));
	}

	/**
	 * @return 按钮：取消;
	 */
	public SWTBotButton btnCancel() {
		return dialogBot.button(TsUIConstants.getString("btnCancel"));
	}
}
