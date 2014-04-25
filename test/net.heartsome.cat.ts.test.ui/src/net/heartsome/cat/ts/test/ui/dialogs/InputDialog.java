package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

/**
 * 输入对话框
 * @author felix_lu
 */
public class InputDialog extends SWTBotShell {

	public static final String OPEN_FILE = "dlgTitleOpenFile";
	
	private SWTBot dialogBot = this.bot();

	/**
	 * 按标题识别对话框
	 * @param dlgTitle
	 *            对话框标题，请使用本类提供的常量
	 */
	public InputDialog(String dlgTitle) {
		super(HSBot.bot().shell(TsUIConstants.getString(dlgTitle)).widget);
	}

	/**
	 * @return 文本框（这种对话框通常只有一个文本框）;
	 */
	public SWTBotText txt() {
		return dialogBot.text();
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
