package net.heartsome.cat.ts.test.ui.dialogs;

import static org.junit.Assert.assertTrue;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

/**
 * 导入 TMX、TBX 对话框
 * @author felix_lu
 */
public class ImportDialog extends SWTBotShell {

	public static final String TMX = "dlgTitleImportTMX";
	public static final String TBX = "dlgTitleImportTBX";

	private SWTBot dialogBot = this.bot();
	private String importType;

	/**
	 * 按标题识别对话框
	 * @param importType
	 *            导入类型，请使用本类提供的常量
	 */
	public ImportDialog(String importType) {
		super(HSBot.bot().shell(TsUIConstants.getString(importType)).widget);
		assertTrue("无此导入类型：" + importType, importType.equals(TMX) || importType.equals(TBX));
		this.importType = importType;
	}

	/**
	 * @return 文本框：TMX/TBX 文件;
	 */
	public SWTBotText txtWLblFile() {
		if (importType.equals(TMX)) {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblTMXFile"));
		}
		else {
			return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblTBXFile"));
		}
	}

	/**
	 * @return 文本框：数据库;
	 */
	public SWTBotText txtWLblDatabase() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblDatabase"));
	}

	/**
	 * @return 按钮：浏览;
	 */
	public SWTBotButton btnBrowse() {
		return dialogBot.button(TsUIConstants.getString("btnBrowseB"));
	}

	/**
	 * @return 按钮：选择库;
	 */
	public SWTBotButton btnSelectDB() {
		return dialogBot.button(TsUIConstants.getString("btnSelectDB"));
	}

	/**
	 * @return 按钮：设置;
	 */
	public SWTBotButton btnSetting() {
		return dialogBot.button(TsUIConstants.getString("btnSetting"));
	}

	/**
	 * @return 按钮：完成;
	 */
	public SWTBotButton btnFinish() {
		return dialogBot.button(TsUIConstants.getString("btnFinish"));
	}

	/**
	 * @return 按钮：取消;
	 */
	public SWTBotButton btnCancel() {
		return dialogBot.button(TsUIConstants.getString("btnCancel"));
	}

	/**
	 * @return 文本信息：导入成功;
	 */
	public SWTBotText msgImportSuccess() {
		return dialogBot.text(TsUIConstants.getString("txtImportSuccess"));
	}

	/**
	 * @return 文本信息：提示选择要导入的文件;
	 */
	public SWTBotText msgNoFileToImport() {
		return dialogBot.text(TsUIConstants.getString("txtNoFileToImport"));
	}

	/**
	 * @return 文本信息：提示选择要导入的库;
	 */
	public SWTBotText msgNoDBToImport() {
		return dialogBot.text(TsUIConstants.getString("txtNoDBToImport"));
	}

	/**
	 * @return 文本信息：提示文件错误;
	 */
	public SWTBotText msgFileError() {
		if (importType.equals(TMX)) {
			return dialogBot.text(TsUIConstants.getString("txtTMXFileError"));
		} else {
			return dialogBot.text(TsUIConstants.getString("txtTBXFileError"));
		}
	}
	
	/**
	 * @return 文字标签：正在导入 TYPE_TMX/TYPE_TBX 文件;
	 */
	public SWTBotLabel msgImporting() {
		if (importType.equals(TMX)) {
			return dialogBot.label(TsUIConstants.getString("lblImportingTMX"));
		} else {
			return dialogBot.label(TsUIConstants.getString("lblImportingTBX"));
		}
	}
	
	/**
	 * @return 工具栏按钮：取消操作;
	 */
	public SWTBotToolbarButton tlbBtnWTltCancelOperation() {
		return dialogBot.toolbarButtonWithTooltip(TsUIConstants.getString("tlbBtnWTltCancelOperation"));
	}
	
	/**
	 * @return 文本信息：用户取消了导入操作;
	 */
	public SWTBotText msgCancelImport() {
		if (importType.equals(TMX)) {
			return dialogBot.text(TsUIConstants.getString("txtCancelImportTMX"));
		} else {
			return dialogBot.text(TsUIConstants.getString("txtCancelImportTBX"));
		}
	}
}
