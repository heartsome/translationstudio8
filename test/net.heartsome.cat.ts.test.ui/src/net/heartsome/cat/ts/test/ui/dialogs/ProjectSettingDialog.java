package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.widgets.HsSWTBotTable;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 项目设置对话框
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class ProjectSettingDialog extends SWTBotShell {

	private SWTBot dlgBot = this.bot();

	/**
	 * 按标题查找对话框
	 */
	public ProjectSettingDialog() {
		super(HSBot.bot().shell(TsUIConstants.getString("dlgTitleProjectSetting")).widget);
	}

	/**
	 * @return 树节点：项目信息;
	 */
	public SWTBotTreeItem treiProjectInfo() {
		return dlgBot.tree().expandNode(TsUIConstants.getString("treiProjectInfo"));
	}

	/**
	 * @return 文本框：客户;
	 */
	public SWTBotText txtWLblClient() {
		return dlgBot.textWithLabel(TsUIConstants.getString("txtWLblClient_"));
	}

	/**
	 * @return 文本框：公司;
	 */
	public SWTBotText txtWLblCompany() {
		return dlgBot.textWithLabel(TsUIConstants.getString("txtWLblCompany"));
	}

	/**
	 * @return 文本框：邮箱;
	 */
	public SWTBotText txtWLblEMail() {
		return dlgBot.textWithLabel(TsUIConstants.getString("txtWLblEMail"));
	}

	/**
	 * @return 文本框：备注;
	 */
	public SWTBotText txtWLblRemark() {
		return dlgBot.textWithLabel(TsUIConstants.getString("txtWLblRemark"));
	}

	/**
	 * @return 树节点：项目语言;
	 */
	public SWTBotTreeItem treiProjectLanguage() {
		return treiProjectInfo().expandNode(TsUIConstants.getString("treiProjectLanguage"));
	}

	/**
	 * @return 下拉列表：源语言;
	 */
	public SWTBotCombo cmbSrcLang() {
		return dlgBot.comboBox();
	}

	/**
	 * @return 列表：可选目标语言;
	 */
	public SWTBotList lstTgtLangAvailable() {
		return dlgBot.list();
	}

	/**
	 * @return 列表：已选目标语言;
	 */
	public SWTBotList lstTgtLangSelected() {
		return dlgBot.list(1);
	}

	/**
	 * @return 按钮：添加;
	 */
	public SWTBotButton btnAddToRight() {
		return dlgBot.button(TsUIConstants.getString("btnAddToRight"));
	}

	/**
	 * @return 按钮：删除;
	 */
	public SWTBotButton btnDeleteToLeft() {
		return dlgBot.button(TsUIConstants.getString("btnDeleteToLeft"));
	}

	/**
	 * @return 按钮：删除所有;
	 */
	public SWTBotButton btnDeleteAll() {
		return dlgBot.button(TsUIConstants.getString("btnDeleteAll"));
	}

	/**
	 * @return 树节点：记忆库设置;
	 */
	public SWTBotTreeItem treiTmSetting() {
		return treiProjectInfo().expandNode(TsUIConstants.getString("treiTmSetting"));
	}

	/**
	 * @return 按钮：导入 TMX;
	 */
	public SWTBotButton btnImportTMX() {
		return dlgBot.button(TsUIConstants.getString("btnImportTMX"));
	}

	/**
	 * @return 树节点：术语库设置;
	 */
	public SWTBotTreeItem treiTbSetting() {
		return treiProjectInfo().expandNode(TsUIConstants.getString("treiTbSetting"));
	}

	/**
	 * @return 按钮：导入 TBX;
	 */
	public SWTBotButton btnImportTBX() {
		return dlgBot.button(TsUIConstants.getString("btnImportTBX"));
	}

	/**
	 * @return 表格：记忆库/术语库列表;
	 */
	public HsSWTBotTable table() {
		return new HsSWTBotTable(dlgBot.table());
	}

	/**
	 * @return 文本 列名：名称;
	 */
	public String tblColName() {
		return TsUIConstants.getString("tblColName");
	}

	/**
	 * @return 文本 列名：类型;
	 */
	public String tblColType() {
		return TsUIConstants.getString("tblColType");
	}

	/**
	 * @return 文本 列名：服务器地址;
	 */
	public String tblColAddress() {
		return TsUIConstants.getString("tblColAddress");
	}

	/**
	 * @return 文本 列名：是否匹配;
	 */
	public String tblColMatch() {
		return TsUIConstants.getString("tblColMatch");
	}

	/**
	 * @return 文本 列名：默认库;
	 */
	public String tblColDefaultDB() {
		return TsUIConstants.getString("tblColDefaultDB");
	}

	/**
	 * @return 按钮：添加;
	 */
	public SWTBotButton btnAdd() {
		return dlgBot.button(TsUIConstants.getString("btnAddA"));
	}

	/**
	 * @return 按钮：创建;
	 */
	public SWTBotButton btnCreate() {
		return dlgBot.button(TsUIConstants.getString("btnCreate"));
	}

	/**
	 * @return 按钮：删除;
	 */
	public SWTBotButton btnDelete() {
		return dlgBot.button(TsUIConstants.getString("btnDeleteD"));
	}

	/**
	 * @return 按钮：取消;
	 */
	public SWTBotButton btnCancel() {
		return dlgBot.button(TsUIConstants.getString("btnCancel"));
	}

	/**
	 * @return 按钮：确定;
	 */
	public SWTBotButton btnOK() {
		return dlgBot.button(TsUIConstants.getString("btnOK"));
	}

}
