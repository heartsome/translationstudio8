package net.heartsome.cat.ts.test.ui.msgdialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * 确认对话框：只有 OK 和 Cancel 按钮
 * @author felix_lu
 */
public class ConfirmDialog extends SWTBotShell {

	/** 对话框标题文本：提示信息 */
	public static String dlgTitleTips = TsUIConstants.getString("dlgTitleTips");
	/** 确认信息：是否确实要从服务器上删除数据库。 */
	public static String msgDeleteDatabaseFromServer = TsUIConstants.getString("msgDeleteDatabaseFromServer");

	private SWTBot dialogBot = this.bot();
	private String msg;

	/**
	 * @param shellIndex
	 *            对话框所在 Shell 索引号
	 * @param msg
	 *            信息内容
	 */
	public ConfirmDialog(int shellIndex, String msg) {
		super(HSBot.bot().shells()[shellIndex].widget);
		this.msg = msg;
	}

	/**
	 * 不指定标题和索引时，默认为通用标题 Confirm
	 * @param msg
	 *            信息内容
	 */
	public ConfirmDialog(String msg) {
		this(TsUIConstants.getString("dlgTitleConfirm"), msg);
	}

	/**
	 * @param dialogTitle
	 *            对话框标题
	 * @param msg
	 *            信息内容
	 */
	public ConfirmDialog(String dialogTitle, String msg) {
		super(HSBot.bot().shell(dialogTitle).widget);
		this.msg = msg;
	}

	/**
	 * @return 按钮：取消;
	 */
	public SWTBotButton btnCancel() {
		return dialogBot.button(TsUIConstants.getString("btnCancel"));
	}

	/**
	 * @return 按钮：确定;
	 */
	public SWTBotButton btnOK() {
		return dialogBot.button(TsUIConstants.getString("btnOK"));
	}

	/**
	 * @return 文字标签：信息内容;
	 */
	public SWTBotLabel lblMessage() {
		return dialogBot.label(msg);
	}
}
