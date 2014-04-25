package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.widgets.HsSWTBotTable;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * 预翻译结果对话框
 * @author felix_lu
 */
public class PreTranslateResultDialog extends SWTBotShell {

	private SWTBot dlgBot = this.bot();

	/**
	 * 按标题识别对话框
	 */
	public PreTranslateResultDialog() {
		super(HSBot.bot().shell(TsUIConstants.getString("dlgTitlePreTransResult")).widget);
	}

	/**
	 * @return 表格：预翻译结果列表;
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
	 * @return 列名文本：翻译单元个数;
	 */
	public String tblColTransUnitNum() {
		return TsUIConstants.getString("tblColTransUnitNum");
	}

	/**
	 * @return 列名文本：已翻译单元;
	 */
	public String tblColTranslatedUnit() {
		return TsUIConstants.getString("tblColTranslatedUnit");
	}

	/**
	 * @return 列名文本：锁定上下文匹配;
	 */
	public String tblColLockedContextMatches() {
		return TsUIConstants.getString("tblColLockedContextMatches");
	}

	/**
	 * @return 列名文本：锁定完全匹配;
	 */
	public String tblColLocked100Matches() {
		return TsUIConstants.getString("tblColLocked100Matches");
	}

	/**
	 * @return 按钮：确定;
	 */
	public SWTBotButton btnOK() {
		return dlgBot.button(TsUIConstants.getString("btnOK"));
	}
}
