package net.heartsome.cat.ts.test.ui.msgdialogs;

import java.text.MessageFormat;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * 信息对话框：文件未找到
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class InfoFileNotFound extends SWTBotShell {

	private SWTBot dialogBot = this.bot();
	private String file;

	/**
	 * 按标题查找对话框
	 * @param file 文件名
	 */
	public InfoFileNotFound(String file) {
		super(HSBot.bot().shell(TsUIConstants.getString("dlgTitleFileNotFound")).widget);
		this.file = file;
	}

	/**
	 * @return 按钮：确定;
	 */
	public SWTBotButton btnOK() {
		return dialogBot.button(TsUIConstants.getString("btnOK"));
	}

	/**
	 * @return 文字标签：文件名非法;
	 */
	public SWTBotLabel msgFileNameInvalid() {
		return dialogBot.label(file + TsUIConstants.getString("msgFileNameInvalid"));
	}

	/**
	 * @return 文字标签：文件未找到;
	 */
	public SWTBotLabel msgFileNotFound() {
		String msg = TsUIConstants.getString("msgFileNotFound");
		return dialogBot.label(MessageFormat.format(msg, file));
	}
}
