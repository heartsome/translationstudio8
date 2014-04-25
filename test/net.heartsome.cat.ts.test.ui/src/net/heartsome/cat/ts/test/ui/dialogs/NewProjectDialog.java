package net.heartsome.cat.ts.test.ui.dialogs;

import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.List;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.finders.HsSWTBot;
import net.heartsome.test.swtbot.widgets.SWTBotTableCombo;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

/**
 * @author  felix_lu
 * @version 
 * @since   JDK1.6
 */
public class NewProjectDialog extends FileNewDialog {
	
	private HsSWTBot dialogBot = this.bot();

	/**
	 * 先打开新建向导对话框
	 */
	public NewProjectDialog(String newType) {
		super(newType);
	}
	
	/*****************************************
	 * 				常用方法
	 *****************************************/
	
	/**
	 * @param name
	 * @param remark
	 * @param client
	 * @param company
	 * @param email
	 */
	public void setBasicInfo(String name, String remark, String client, String company, String eMail) {
		assertTrue("项目名称不能为 null。", name != null);
		setName(name);
		if (remark != null) {
			setRemark(remark);
		}
		if (client != null) {
			setClient(client);
		}
		if (company != null) {
			setCompany(company);
		}
		if (eMail != null) {
			setEMail(eMail);
		}
	}
	
	/**
	 * @param basicInfo 字符串数组：项目基本信息，按照界面顺序依次为项目名称、备注、客户、公司、邮箱。
	 */
	public void setBasicInfo(String[] basicInfo) {
		assertTrue("项目基本信息参数不正确。", basicInfo.length == 5);
		assertTrue("项目名称不能为 null。", basicInfo[0] != null);
		setName(basicInfo[0]);
		if (basicInfo[1] != null) {
			setRemark(basicInfo[1]);
		}
		if (basicInfo[2] != null) {
			setClient(basicInfo[2]);
		}
		if (basicInfo[3] != null) {
			setCompany(basicInfo[3]);
		}
		if (basicInfo[4] != null) {
			setEMail(basicInfo[4]);
		}
	}
	
	/**
	 * @param srcLangCode
	 * @param tgtLangCodes ;
	 */
	public void setLangPair(String srcLangCode, String... tgtLangCodes) {
		assertTrue("源语言代码不能为空或 null。", !(srcLangCode == null || "".equals(srcLangCode)));
		setSrcLang(srcLangCode);
		
		assertTrue("目标语言语言不能为空或 null。", tgtLangCodes.length > 0);
		for (int i = 0; i < tgtLangCodes.length; i++) {
			assertTrue("目标语言语言不能为空或 null。", !(tgtLangCodes[i] == null || "".equals(tgtLangCodes[i])));
		}
		setTgtLang(tgtLangCodes);
	}
	
	/**
	 * @param srcLangCode
	 * @param tgtLangCodes ;
	 */
	public void setLangPair(String srcLangCode, List<String> tgtLangCodes) {
		
		assertTrue("源语言代码不能为空或 null。", !(srcLangCode == null || "".equals(srcLangCode)));
		setSrcLang(srcLangCode);
		
		assertTrue("目标语言语言不能为空或 null。", tgtLangCodes.size() > 0);
		for (String tgtLangCode : tgtLangCodes) {
			assertTrue("目标语言语言不能为空或 null。", !(tgtLangCode == null || "".equals(tgtLangCode)));
		}
		setTgtLang(tgtLangCodes);
	}
	
	/**
	 *  断言对话框标题是否符合预期;
	 */
	public void isTitleCreateProject() {
		assertTrue("创建项目向导对话框的标题不正确。", getText().equals(TsUIConstants.getString("dlgTitleCreateProject")));
	}
	
	/**
	 *  断言对话框标题是否符合预期（从菜单、快捷键及右键新建其他）;
	 */
	public void isTitleNew() {
		assertTrue("新建向导对话框的标题不正确。", getText().equals(TsUIConstants.getString("dlgTitleFileNew")));
	}
	
	/**
	 *  断言对话框标题是否符合预期（从右键新建项目）;
	 */
	public void isTitleNewProject() {
		assertTrue("新建项目向导对话框的标题不正确。", getText().equals(TsUIConstants.getString("dlgTitleFileNewProject")));
	}
	
	/**
	 * @param name ;
	 */
	public void setName(String name) {
		txtWLblProjectName().setText(name);
	}
	
	/**
	 * @param prjRemark ;
	 */
	public void setRemark(String prjRemark) {
		txtWLblRemark().setText(prjRemark);
	}
	
	/**
	 * @param prjClient ;
	 */
	public void setClient(String prjClient) {
		txtWLblClient().setText(prjClient);
	}
	
	/**
	 * @param prjCompany ;
	 */
	public void setCompany(String prjCompany) {
		txtWLblCompany().setText(prjCompany);
	}
	
	/**
	 * @param eMail ;
	 */
	public void setEMail(String eMail) {
		txtWLblEMail().setText(eMail);
	}

	/**
	 * @param srcLangCode ;
	 */
	public void setSrcLang(String srcLangCode) {
		String srcLang = TsUIConstants.getLang(srcLangCode);
		cmbSourceLang().setSelection(srcLang);
		assertTrue("源语言设置失败。", srcLang.equals(cmbSourceLang().selection()));
	}
	
	/**
	 * @param tgtLangCodes ;
	 */
	public void setTgtLang(String... tgtLangCodes) {
		String[] tgtLangs = TsUIConstants.getLangs(tgtLangCodes);
		tabTargetLangAvailable().select(tgtLangs);
		btnAddToRight().click();
		for (int i = 0; i < tgtLangs.length; i++) {
			assertTrue("目标语言设置失败：" + tgtLangs[i], tabTargetLangSelected().indexOf(tgtLangs[i]) != -1);
		}
	}
	
	/**
	 * @param tgtLangCodes ;
	 */
	public void setTgtLang(List<String> tgtLangCodes) {
		String[] langs = new String[tgtLangCodes.size()];
		tgtLangCodes.toArray(langs);
		setTgtLang(langs);
	}
	
	/**
	 * @param tgtLangCodes ;
	 */
	public void rmTgtLang(String... tgtLangCodes) {
		assertTrue("目标语言语言不能为空或 null。", tgtLangCodes.length > 0);
		for (int i = 0; i < tgtLangCodes.length; i++) {
			assertTrue("目标语言语言不能为空或 null。", !(tgtLangCodes[i] == null || "".equals(tgtLangCodes[i])));
		}
		String[] tgtLangs = TsUIConstants.getLangs(tgtLangCodes);
		for (int i = 0; i < tgtLangs.length; i++) {
			assertTrue("该目标语言代码对应的语言未添加到已选列表：" + tgtLangCodes[i], tabTargetLangSelected().indexOf(tgtLangs[i]) != -1);
		}
		tabTargetLangSelected().select(tgtLangs);
		btnDeleteToLeft().click();
		for (int i = 0; i < tgtLangs.length; i++) {
			assertTrue("目标语言删除失败：" + tgtLangs[i], tabTargetLangSelected().indexOf(tgtLangs[i]) == -1);
		}
	}
	
	/**
	 *  ;
	 */
	public void rmAllTgtLang() {
		btnDeleteAll().click();
		assertTrue("目标语言删除失败。", tabTargetLangSelected().rowCount() == 0);
	}
	
	
	/*****************************************
	 * 				界面元素映射
	 *****************************************/

	/**
	 * @return 第 1 页：项目信息;
	 */
	public SWTBotLabel lblProjectInfo() {
		return dialogBot.label(TsUIConstants.getString("lblProjectInfo"));
	}
	
	/**
	 * @return 文本框：项目名
	 */
	public SWTBotText txtWLblProjectName() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblProjectName"));
	}
	
	/**
	 * @return 文本框：备注
	 */
	public SWTBotText txtWLblRemark() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblProjectRemark"));
	}
	
	/**
	 * @return 文本框：客户
	 */
	public SWTBotText txtWLblClient() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblProjectClient"));
	}
	
	/**
	 * @return 文本框：公司
	 */
	public SWTBotText txtWLblCompany() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblProjectCompany"));
	}
	
	/**
	 * @return 文本框：电子邮箱
	 */
	public SWTBotText txtWLblEMail() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblProjectEMail"));
	}
	
	/**
	 * @return 文本：项目名称重复
	 */
	public SWTBotText msgDucplicatedProjectName() {
		return dialogBot.text(TsUIConstants.getString("msgDucplicatedProjectName"));
	}
	
	/**
	 * @return 文本：项目名称含非法字符
	 */
	public SWTBotText msgInvalidCharInResourceName(String invalidChar, String resourceName) {
		String msg = TsUIConstants.getString("msgInvalidCharInResourceName");
		return dialogBot.text(MessageFormat.format(msg, invalidChar, resourceName));
	}
	
	/**
	 * @return 第 2 页：语言对设置;
	 */
	public SWTBotLabel lblLangSetting() {
		return dialogBot.label(TsUIConstants.getString("lblLangSetting"));
	}
	
	/**
	 * @return 下拉列表：源语言
	 */
	public SWTBotTableCombo cmbSourceLang() {
		return dialogBot.tableCombo();
	}
	
	/**
	 * @return 表格：可供选择的目标语言
	 */
	public SWTBotTable tabTargetLangAvailable() {
		return dialogBot.table(0);
	}
	
	/**
	 * @return 表格：已选择的目标语言
	 */
	public SWTBotTable tabTargetLangSelected() {
		return dialogBot.table(1);
	}
	
	/**
	 * @return 按钮：添加目标语言
	 */
	public SWTBotButton btnAddToRight() {
		return dialogBot.button(TsUIConstants.getString("btnAddToRight"));
	}
	
	/**
	 * @return 按钮：删除目标语言
	 */
	public SWTBotButton btnDeleteToLeft() {
		return dialogBot.button(TsUIConstants.getString("btnDeleteToLeft"));
	}
	
	/**
	 * @return 按钮：删除所有目标语言
	 */
	public SWTBotButton btnDeleteAll() {
		return dialogBot.button(TsUIConstants.getString("btnDeleteAll"));
	}
	
	/**
	 * @return 第 3 页：记忆库设置;
	 */
	public SWTBotLabel lblTmSetting() {
		return dialogBot.label(TsUIConstants.getString("lblTmSetting"));
	}
	
	/**
	 * @return ;
	 */
	public SWTBotTable tblTMDB() {
		return dialogBot.table();
	}
	
	/**
	 * @return ;
	 */
	public SWTBotButton btnAdd() {
		return dialogBot.button(TsUIConstants.getString("btnAddA"));
	}
	
	/**
	 * @return ;
	 */
	public SWTBotButton btnCreate() {
		return dialogBot.button(TsUIConstants.getString("btnCreate"));
	}
	
	/**
	 * @return ;
	 */
	public SWTBotButton btnDelete() {
		return dialogBot.button(TsUIConstants.getString("btnDeleteD"));
	}
	
	/**
	 * @return ;
	 */
	public SWTBotButton btnImportTMX() {
		return dialogBot.button(TsUIConstants.getString("btnImportTMX"));
	}
	
	/**
	 * @return ;
	 */
	public SWTBotLabel lblTbSetting() {
		return dialogBot.label(TsUIConstants.getString("lblTbSetting"));
	}
	
	/**
	 * @return 第 4 页：术语库设置;
	 */
	public SWTBotTable tblTBDB() {
		return dialogBot.table();
	}
	
	/**
	 * @return ;
	 */
	public SWTBotButton btnImportTBX() {
		return dialogBot.button(TsUIConstants.getString("btnImportTBX"));
	}
	
	/**
	 * @return 第 5 页：添加源文件;
	 */
	public SWTBotLabel lblAddSrcFile() {
		return dialogBot.label(TsUIConstants.getString("lblAddSrcFile"));
	}
	
	/**
	 * @return ;
	 */
	public SWTBotList lstSrcFile() {
		return dialogBot.list();
	}
}
