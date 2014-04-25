package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.widgets.HsSWTBotTable;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * 预翻译对话框
 * @author felix_lu
 */
public class PreTranslateDialog extends SWTBotShell {

	private SWTBot dlgBot = this.bot();

	/**
	 * 按标题识别对话框
	 */
	public PreTranslateDialog() {
		super(HSBot.bot().shell(TsUIConstants.getString("dlgTitlePreTranslation")).widget);
	}
	
	/**
	 * @return 表格：预翻译文件列表;
	 */
	public HsSWTBotTable table() {
		return new HsSWTBotTable(dlgBot.table());
	}

	/**
	 * @return 列名文本：序号;
	 */
	public String tblColNum() {
		return TsUIConstants.getString("tblColNum2");
	}

	/**
	 * @return 列名文本：文件;
	 */
	public String tblColFile() {
		return TsUIConstants.getString("tblColFile");
	}

	/**
	 * @return 列名文本：源语言;
	 */
	public String tblColSrcLang() {
		return TsUIConstants.getString("tblColSrcLang");
	}

	/**
	 * @return 列名文本：目标语言;
	 */
	public String tblColTgtLang() {
		return TsUIConstants.getString("tblColTgtLang");
	}

	/**
	 * @return 复选框：是否锁定完全匹配
	 */
	public SWTBotCheckBox chkbxLock100Matches() {
		return dlgBot.checkBox(TsUIConstants.getString("chkbxLock100Matches"));
	}

	/**
	 * @return 复选框：是否锁定上下文匹配
	 */
	public SWTBotCheckBox chkbxLockContextMatches() {
		return dlgBot.checkBox(TsUIConstants.getString("chkbxLockContextMatches"));
	}

	/**
	 * @return 按钮：参数设置;
	 */
	public SWTBotButton btnPreferencesSetting() {
		return dlgBot.button(TsUIConstants.getString("btnPreferencesSetting"));
	}

	/**
	 * @return 按钮：取消;
	 */
	public SWTBotButton btnCancel() {
		return dlgBot.button(TsUIConstants.getString("btnCancelC"));
	}

	/**
	 * @return 按钮：确定;
	 */
	public SWTBotButton btnOK() {
		return dlgBot.button(TsUIConstants.getString("btnOKO"));
	}
}
