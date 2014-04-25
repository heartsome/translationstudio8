package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.finders.HsSWTBot;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.widgets.HsSWTBotShell;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotSpinner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 首选项对话框
 * @author felix_lu
 */
public class PreferencesDialog extends HsSWTBotShell {

	private HsSWTBot dlgBot = this.bot();
	private SWTBotTree tree = dlgBot.tree();

	/**
	 * 按标题识别对话框
	 */
	public PreferencesDialog() {
		super(HSBot.bot().shell(TsUIConstants.getString("dlgTitlePreferences")).widget);
	}

	/* 记忆库相关设置 */

	/**
	 * @return 树节点：记忆库;
	 */
	public SWTBotTreeItem treiTMDB() {
		return tree.expandNode(TsUIConstants.getString("treiTMDB"));
	}

	/**
	 * @return 复选框：启用共享库提示;
	 */
	public SWTBotCheckBox tmChkbxShareDBTips() {
		return dlgBot.checkBox(TsUIConstants.getString("prefTMChkbxShareDBTips"));
	}

	/**
	 * @return 复选框：使用自动快速翻译;
	 */
	public SWTBotCheckBox tmChkbxAutoQT() {
		return dlgBot.checkBox(TsUIConstants.getString("prefTMChkbxAutoQT"));
	}

	/**
	 * @return 复选框：匹配存在时不应用记忆库;
	 */
	public SWTBotCheckBox tmChkbxMatchExistNotApplyTM() {
		return dlgBot.checkBox(TsUIConstants.getString("prefTMChkbxMatchExistNotApplyTM"));
	}

	/**
	 * @return 复选框：搜索记忆库时区分大小写;
	 */
	public SWTBotCheckBox tmChkbxMatchCase() {
		return dlgBot.checkBox(TsUIConstants.getString("prefTMChkbxMatchCase"));
	}

	/**
	 * @return 复选框：匹配时使用标记自动补全;
	 */
	public SWTBotCheckBox tmChkbxAutoCompleteTag() {
		return dlgBot.checkBox(TsUIConstants.getString("prefTMChkbxAutoCompleteTag"));
	}

	/**
	 * @return 复选框：匹配时是否忽略标记;
	 */
	public SWTBotCheckBox tmChkbxIgnoreTag() {
		return dlgBot.checkBox(TsUIConstants.getString("prefTMChkbxIgnoreTag"));
	}

	/**
	 * @return 带上下箭头按钮的数字文本框：上下文匹配文本段数量;
	 */
	public SWTBotSpinner tmSpinContextNumber() {
		return dlgBot.spinner(TsUIConstants.getString("prefTMSpinContextNumber"));
	}

	/**
	 * @return 带上下箭头按钮的数字文本框：记忆库最大匹配数;
	 */
	public SWTBotSpinner tmSpinMatchNumber() {
		return dlgBot.spinner(TsUIConstants.getString("prefTMSpinMatchNumber"));
	}

	/**
	 * @return 带上下箭头按钮的数字文本框：记忆库最低匹配率;
	 */
	public SWTBotSpinner tmSpinMinMatchPercentage() {
		return dlgBot.spinner(TsUIConstants.getString("prefTMSpinMinMatchPercentage"));
	}

	/**
	 * @return 单选按钮：匹配率相同时排序策略，默认库优先;
	 */
	public SWTBotRadio tmRadBtnSortDefaultDBFirst() {
		return dlgBot.radio(TsUIConstants.getString("prefTMRadBtnSortDefaultDBFirst"));
	}

	/**
	 * @return 单选按钮：匹配率相同时排序策略，更新时间倒序排列;
	 */
	public SWTBotRadio tmRadBtnSortUpdateTimeReverse() {
		return dlgBot.radio(TsUIConstants.getString("prefTMRadBtnSortUpdateTimeReverse"));
	}

	/**
	 * @return 单选按钮：记忆库更新策略，始终增加;
	 */
	public SWTBotRadio tmRadBtnUpdateModeAlwaysAdd() {
		return dlgBot.radio(TsUIConstants.getString("prefTMRadBtnUpdateModeAlwaysAdd"));
	}

	/**
	 * @return 单选按钮：记忆库更新策略，重复覆盖;
	 */
	public SWTBotRadio tmRadBtnUpdateModeOverwrite() {
		return dlgBot.radio(TsUIConstants.getString("prefTMRadBtnUpdateModeOverwrite"));
	}

	/**
	 * @return 单选按钮：记忆库更新策略，重复忽略;
	 */
	public SWTBotRadio tmRadBtnUpdateModeIgnore() {
		return dlgBot.radio(TsUIConstants.getString("prefTMRadBtnUpdateModeIgnore"));
	}

	/* 术语库相关设置 */

	/**
	 * @return 树节点：术语库;
	 */
	public SWTBotTreeItem treiTBDB() {
		return tree.expandNode(TsUIConstants.getString("treiTBDB"));
	}

	/**
	 * @return 单选按钮：术语库更新策略，始终增加;
	 */
	public SWTBotRadio tbRadBtnUpdateModeAlwaysAdd() {
		return dlgBot.radio(TsUIConstants.getString("prefTBRadBtnUpdateModeAlwaysAdd"));
	}

	/**
	 * @return 单选按钮：术语库更新策略，重复覆盖;
	 */
	public SWTBotRadio tbRadBtnUpdateModeOverwrite() {
		return dlgBot.radio(TsUIConstants.getString("prefTBRadBtnUpdateModeOverwrite"));
	}

	/**
	 * @return 单选按钮：术语库更新策略，重复合并;
	 */
	public SWTBotRadio tbRadBtnUpdateModeMerge() {
		return dlgBot.radio(TsUIConstants.getString("prefTBRadBtnUpdateModeMerge"));
	}

	/**
	 * @return 单选按钮：术语库更新策略，重复忽略;
	 */
	public SWTBotRadio tbRadBtnUpdateModeIgnore() {
		return dlgBot.radio(TsUIConstants.getString("prefTBRadBtnUpdateModeIgnore"));
	}

	/* 预翻译 */

	/**
	 * @return 树节点：预翻译;
	 */
	public SWTBotTreeItem treiPreTranslation() {
		return tree.expandNode(TsUIConstants.getString("treiPreTranslation"));
	}

	/**
	 * @return 复选框：预翻译区分大小写;
	 */
	public SWTBotCheckBox transPreChkbxCaseSensitive() {
		return dlgBot.checkBox(TsUIConstants.getString("prefTransPreChkbxCaseSensitive"));
	}

	/**
	 * @return Spinner：预翻译最低匹配率;
	 */
	public SWTBotSpinner transPreSpinMinMatchPercentage() {
		return dlgBot.spinner(TsUIConstants.getString("prefTransPreSpinMinMatchPercentage"));
	}

	/**
	 * @return 单选按钮：保留现有匹配;
	 */
	public SWTBotRadio transPreRadBtnOverwriteModeKeepCurrent() {
		return dlgBot.radio(TsUIConstants.getString("prefTransPreRadBtnOverwriteModeKeepCurrent"));
	}

	/**
	 * @return 单选按钮：如果匹配率比当前高，覆盖现有匹配;
	 */
	public SWTBotRadio transPreRadBtnOverwriteModeOverwriteIfHigher() {
		return dlgBot.radio(TsUIConstants.getString("prefTransPreRadBtnOverwriteModeOverwriteIfHigher"));
	}

	/**
	 * @return 单选按钮：始终覆盖现有匹配;
	 */
	public SWTBotRadio transPreRadBtnOverwriteModeAlwaysOverwrite() {
		return dlgBot.radio(TsUIConstants.getString("prefTransPreRadBtnOverwriteModeAlwaysOverwrite"));
	}

	/* 通用按钮 */

	/**
	 * @return 按钮：恢复默认设置;
	 */
	public SWTBotButton btnRestoreDefaults() {
		return dlgBot.button(TsUIConstants.getString("btnRestoreDefaults"));
	}

	/**
	 * @return 按钮：应用设置;
	 */
	public SWTBotButton btnApply() {
		return dlgBot.button(TsUIConstants.getString("btnApply"));
	}

	/**
	 * @return 按钮：确定;
	 */
	public SWTBotButton btnOK() {
		return dlgBot.button(TsUIConstants.getString("btnOK"));
	}

	/**
	 * @return 按钮：取消;
	 */
	public SWTBotButton btnCancel() {
		return dlgBot.button(TsUIConstants.getString("btnCancel"));
	}
}
