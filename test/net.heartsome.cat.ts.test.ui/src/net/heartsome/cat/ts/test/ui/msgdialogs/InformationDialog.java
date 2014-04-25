package net.heartsome.cat.ts.test.ui.msgdialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * 信息对话框：只有一个 OK 按钮
 * @author felix_lu
 */
public class InformationDialog extends SWTBotShell {

	/** 对话框标题文本：提示信息 */
	public static String dlgTitleTips = TsUIConstants.getString("dlgTitleTips");
	/** 对话框标题文本：错误提示 */
	public static String dlgTitleErrorInfo = TsUIConstants.getString("dlgTitleErrorInfo");
	public static String dleTitleError = TsUIConstants.getString("dlgTitleError");

	/** 提示信息：服务器为必填项。 */
	public static String msgServerIsRequired = TsUIConstants.getString("msgServerIsRequired");
	/** 提示信息：端口为必填项。 */
	public static String msgPortIsRequired = TsUIConstants.getString("msgPortIsRequired");
	/** 提示信息：实例为必填项。 */
	public static String msgInstanceIsRequired = TsUIConstants.getString("msgInstanceIsRequired");
	/** 提示信息：路径为必填项。 */
	public static String msgPathIsRequired = TsUIConstants.getString("msgPathIsRequired");
	/** 提示信息：用户名为必填项。 */
	public static String msgUsernameIsRequired = TsUIConstants.getString("msgUsernameIsRequired");
	/** 提示信息：连接错误。 */
	public static String msgServerConnectionError = TsUIConstants.getString("msgServerConnectionError");
	/** 提示信息：库与项目语言对无匹配。 */
	public static String msgNoMatchInDB = TsUIConstants.getString("msgNoMatchInDB");

	private SWTBot dialogBot = this.bot();
	private String msg;

	/**
	 * @param shellIndex
	 *            对话框 Shell 索引，适用无标题的对话框
	 * @param msg
	 *            信息内容在资源文件中的 Key
	 */
	public InformationDialog(int shellIndex, String msg) {
		super(HSBot.bot().shells()[shellIndex].widget);
		this.msg = msg;
	}

	/**
	 * 不提供标题时，默认为通用标题 Information
	 * @param msg
	 *            信息内容在资源文件中的 Key
	 */
	public InformationDialog(String msg) {
		this(TsUIConstants.getString("dlgTitleInformation"), msg);
	}

	/**
	 * @param dialogTitle
	 *            对话框标题
	 * @param msg
	 *            信息内容
	 */
	public InformationDialog(String dialogTitle, String msg) {
		super(HSBot.bot().shell(dialogTitle).widget);
		this.msg = msg;
	}

	/**
	 * @param msg
	 *            信息内容;
	 */
	public void setMsg(String msg) {
		this.msg = msg;
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
