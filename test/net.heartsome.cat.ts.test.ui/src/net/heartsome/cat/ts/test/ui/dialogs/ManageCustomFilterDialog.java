package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

/**
 * 管理自定义过滤器条件对话框
 * @author felix_lu
 */
public class ManageCustomFilterDialog extends SWTBotShell {

	private SWTBot dialogBot = this.bot();

	/**
	 * 按标题识别对话框
	 */
	public ManageCustomFilterDialog() {
		super(HSBot.bot().shell(TsUIConstants.getString("dlgTitleManageCustomFilter")).widget);
	}

	/**
	 * @return 按钮：添加自定义过滤规则;
	 */
	public SWTBotButton btnAdd() {
		return dialogBot.button(TsUIConstants.getString("btnAdd"));
	}

	/**
	 * @return 按钮：添加条件;
	 */
	public SWTBotButton btnAddCondition() {
		return dialogBot.button(TsUIConstants.getString("btnAddCondition"));
	}

	/**
	 * @param index 指定按钮索引
	 * @return 按钮：添加条件;
	 */
	public SWTBotButton btnAddCondition(int index) {
		return dialogBot.button(TsUIConstants.getString("btnAddCondition"), index);
	}

	/**
	 * @return 按钮：关闭;
	 */
	public SWTBotButton btnClose() {
		return dialogBot.button(TsUIConstants.getString("btnClose"));
	}

	/**
	 * @return 按钮：删除自定义过滤规则;
	 */
	public SWTBotButton btnDelete() {
		return dialogBot.button(TsUIConstants.getString("btnDelete"));
	}

	/**
	 * @param index 指定按钮索引
	 * @return 按钮：删除条件;
	 */
	public SWTBotButton btnDeleteCondition(int index) {
		return dialogBot.button(TsUIConstants.getString("btnDeleteCondition"), index);
	}

	/**
	 * @return 按钮：编辑规则;
	 */
	public SWTBotButton btnEdit() {
		return dialogBot.button(TsUIConstants.getString("btnEdit"));
	}

	/**
	 * @return 按钮：保存;
	 */
	public SWTBotButton btnSave() {
		return dialogBot.button(TsUIConstants.getString("btnSave"));
	}

	/**
	 * @return 列表：自定义过滤规则;
	 */
	public SWTBotList lstWLblCustomFilter() {
		return dialogBot.listWithLabel(TsUIConstants.getString("lstWLblCustomFilter"));
	}

	/**
	 * @return 单选按钮：匹配所有条件;
	 */
	public SWTBotRadio radBtnMatchAllConditions() {
		return dialogBot.radio(TsUIConstants.getString("radBtnMatchAllConditions"));
	}

	/**
	 * @return 单选按钮：匹配任一条件;
	 */
	public SWTBotRadio radBtnMatchAnyCondition() {
		return dialogBot.radio(TsUIConstants.getString("radBtnMatchAnyCondition"));
	}

	/**
	 * @return 文本框：规则名称;
	 */
	public SWTBotText txtWLblFilterName() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblFilterName"));
	}

}
