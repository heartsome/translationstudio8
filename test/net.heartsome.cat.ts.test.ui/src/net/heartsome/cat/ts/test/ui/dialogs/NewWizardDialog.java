package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.widgets.HsSWTBotShell;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.hamcrest.SelfDescribing;

/**
 * 新建...向导对话框
 * @author felix_lu
 */
public class NewWizardDialog extends HsSWTBotShell {

	private SWTBot dialogBot = this.bot();

	/**
	 * @param shell
	 */
	public NewWizardDialog(Shell shell) {
		super(shell);
	}

	/**
	 * @param shell
	 * @param description
	 */
	public NewWizardDialog(Shell shell, SelfDescribing description) {
		super(shell, description);
	}

	/**
	 * @return SWTBotButton 上一步
	 */
	public SWTBotButton btnBack() {
		return dialogBot.button(TsUIConstants.getString("btnBack"));
	}

	/**
	 * @return SWTBotButton 取消
	 */
	public SWTBotButton btnCancel() {
		return dialogBot.button(TsUIConstants.getString("btnCancel"));
	}

	/**
	 * @return SWTBotButton 完成
	 */
	public SWTBotButton btnFinish() {
		return dialogBot.button(TsUIConstants.getString("btnFinish"));
	}

	/**
	 * @return SWTBotButton 下一步
	 */
	public SWTBotButton btnNext() {
		return dialogBot.button(TsUIConstants.getString("btnNext"));
	}
}
