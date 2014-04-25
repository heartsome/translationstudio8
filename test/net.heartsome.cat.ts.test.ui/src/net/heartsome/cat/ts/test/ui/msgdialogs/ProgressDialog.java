package net.heartsome.cat.ts.test.ui.msgdialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * 进度对话框
 * @author felix_lu
 */
public class ProgressDialog extends SWTBotShell {
	
	/** 对话框标题：进度信息 */
	public static final String TITLE_PROGRESS_INFO = "dlgTitleProgressInformation";
	/** 对话框标题：预翻译进度 */
	public static final String TITLE_PRE_TRANSLATING = "dlgTitlePreTranslating";
	/** 信息内容：正在预翻译 */
	public static final String MSG_PRE_TRANSLATING = "msgPreTranslating";

	private SWTBot dlgBot = this.bot();

	/**
	 * @param dialogTitleKey
	 *            进度对话框标题在资源文件中的 Key，请使用本类常量
	 */
	public ProgressDialog(String dialogTitleKey) {
		super(HSBot.bot().shell(TsUIConstants.getString(dialogTitleKey)).widget);
	}
	
	/**
	 * @param msgKey 信息内容在资源文件中的 Key，请使用本类常量
	 * @return 文字标签：进度信息内容;
	 */
	public SWTBotLabel lblMsg(String msgKey) {
		return dlgBot.label(TsUIConstants.getString(msgKey));
	}

	/**
	 * @return 按钮：取消;
	 */
	public SWTBotButton btnCancel() {
		return dlgBot.button(TsUIConstants.getString("btnCancel"));
	}

	/**
	 * @return 按钮：详情 未展开;
	 */
	public SWTBotButton btnDetailsCollapsed() {
		return dlgBot.button(TsUIConstants.getString("btnDetailsCollapsed"));
	}

	/**
	 * @return 按钮：详情 展开;
	 */
	public SWTBotButton btnDetailsExpanded() {
		return dlgBot.button(TsUIConstants.getString("btnDetailsExpanded"));
	}

	/**
	 * @return 按钮：在后台运行;
	 */
	public SWTBotButton btnRunInBackground() {
		return dlgBot.button(TsUIConstants.getString("btnRunInBackground"));
	}

	/**
	 * @return 复选框：总是在后台运行;
	 */
	public SWTBotCheckBox chkbxAlwaysRunInBackground() {
		return dlgBot.checkBox(TsUIConstants.getString("chkbxAlwaysRunInBackground"));
	}
}
