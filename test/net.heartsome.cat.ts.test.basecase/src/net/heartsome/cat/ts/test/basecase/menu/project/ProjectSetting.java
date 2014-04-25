package net.heartsome.cat.ts.test.basecase.menu.project;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import net.heartsome.cat.ts.test.basecase.common.ExcelData;
import net.heartsome.cat.ts.test.basecase.menu.db.DBManagement;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.Entry;
import net.heartsome.cat.ts.test.ui.dialogs.ProjectSettingDialog;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.msgdialogs.InformationDialog;
import net.heartsome.cat.ts.test.ui.tasks.Waits;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsRow;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

/**
 * 封装项目设置的常用方法
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class ProjectSetting {

	private ProjectSettingDialog dlgPrjSetting;
	private HsRow row;
	private ExcelData data;
	private String name;
	private String client;
	private String company;
	private String eMail;
	private String remark;
	private String srcLang;
	private String[] tgtLangs;
	// private int dbType;
	private String tMDBName;
	private String tBDBName;
	// private String address;
	private boolean isTMDBExist;
	private boolean isTBDBExist;

	/**
	 * 设置之后的动作
	 * @author felix_lu
	 * @version
	 * @since JDK1.6
	 */
	public enum NextAction {
		OK, CANCEL, WAIT,
	}

	/**
	 * @param row
	 *            存放测试数据的 Excel 表格行对象
	 */
	public ProjectSetting(HsRow row) {
		this.row = row;
		data = new ExcelData(row);
	}

	/**
	 * 完整的设置项目信息流程，含确定并关闭对话框
	 * @param from
	 *            ;
	 */
	public void setProjectInfo(Entry from) {
		// getDataPrjSetting();
		// openPrjSettingDlg(from);
		setPrjBasicInfo(from, NextAction.WAIT);
		setPrjLangs(from, NextAction.WAIT);
		setTMDB(from, NextAction.WAIT);
		setTBDB(from, NextAction.WAIT);
		nextAction(NextAction.OK);
	}

	/**
	 * 打开项目设置对话框
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类提供的常量
	 * @return 项目设置对话框对象;
	 */
	public ProjectSettingDialog openPrjSettingDlg(Entry from) {
		if (name == null) {
			getDataPrjSetting();
		}
		assertTrue(name != null && !name.equals(""));
		ProjectTreeView.getTree().expandNode(name).select();

		switch (from) {
		case CONTEXT_MENU: {
			ProjectTreeView.getInstance().ctxMenuProjectSetting().click();
			break;
		}
		case MENU: {
			TS.getInstance(); // TODO
			break;
		}
		case SHORTCUT: {
			// TODO
			break;
		}
		default: {
			assertTrue("无此入口：" + from, false);
		}
		}

		dlgPrjSetting = new ProjectSettingDialog();
		assertTrue(dlgPrjSetting.isActive());
		return dlgPrjSetting;
	}

	/**
	 * 设置项目基本信息
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类提供的常量;
	 * @param wait
	 *            下一步操作，请使用本类提供的常量;
	 */
	public void setPrjBasicInfo(Entry from, NextAction wait) {
		if (dlgPrjSetting == null) {
			openPrjSettingDlg(from);
		}
		dlgPrjSetting.treiProjectInfo().expand().select();
		dlgPrjSetting.txtWLblClient().setText(client);
		dlgPrjSetting.txtWLblCompany().setText(company);
		dlgPrjSetting.txtWLblEMail().setText(eMail);
		dlgPrjSetting.txtWLblRemark().setText(remark);
		nextAction(wait);
	}

	/**
	 * 设置项目语言
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类提供的常量;
	 * @param wait
	 *            下一步操作，请使用本类提供的常量;
	 */
	public void setPrjLangs(Entry from, NextAction wait) {
		if (dlgPrjSetting == null) {
			openPrjSettingDlg(from);
		}
		dlgPrjSetting.treiProjectLanguage().select();
		if (!srcLang.equals(dlgPrjSetting.cmbSrcLang().selection())) {
			dlgPrjSetting.cmbSrcLang().setSelection(srcLang);
		}
		dlgPrjSetting.lstTgtLangAvailable().select(tgtLangs);
		dlgPrjSetting.btnAddToRight().click();
		List<String> selectedLangs = Arrays.asList(dlgPrjSetting.lstTgtLangSelected().selection());
		for (String tgtLang : tgtLangs) {
			assertTrue(selectedLangs.contains(tgtLang));
		}
		nextAction(wait);
	}

	/**
	 * 设置记忆库
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类提供的常量;
	 * @param nextAction
	 *            下一步操作，请使用本类提供的常量;
	 */
	public void setTMDB(Entry from, NextAction nextAction) {
		if (dlgPrjSetting == null) {
			openPrjSettingDlg(from);
		}
		dlgPrjSetting.treiTmSetting().select();
		if (!dlgPrjSetting.table().containsTextInColumn(tMDBName, dlgPrjSetting.tblColName())) {
			if (isTMDBExist) {
				dlgPrjSetting.btnAdd().click();
				DBManagement dbMgmt = new DBManagement(row);
				dbMgmt.selectDB(tMDBName);
			} else {
				dlgPrjSetting.btnCreate().click();
				// TODO 目前弹出的是数据库创建向导，而该向导有较大的改进余地，暂不实现
			}
			try {
				InformationDialog dlgInfo = new InformationDialog(InformationDialog.dlgTitleTips,
						InformationDialog.msgNoMatchInDB);
				dlgInfo.btnOK().click();
				Waits.shellClosed(dlgInfo);
			} catch (WidgetNotFoundException e) {
				// e.printStackTrace();
			}
			assertTrue("未正确选择记忆库：" + tMDBName,
					dlgPrjSetting.table().containsTextInColumn(tMDBName, dlgPrjSetting.tblColName()));
		}
		nextAction(nextAction);
	}

	/**
	 * 设置术语库
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类提供的常量;
	 * @param nextAction
	 *            下一步操作，请使用本类提供的常量;
	 */
	public void setTBDB(Entry from, NextAction nextAction) {
		if (dlgPrjSetting == null) {
			openPrjSettingDlg(from);
		}
		dlgPrjSetting.treiTbSetting().select();
		if (!dlgPrjSetting.table().containsTextInColumn(tBDBName, dlgPrjSetting.tblColName())) {
			if (isTBDBExist) {
				dlgPrjSetting.btnAdd().click();
				DBManagement dbMgmt = new DBManagement(row);
				dbMgmt.selectDB(tBDBName);
			} else {
				dlgPrjSetting.btnCreate().click();
				// TODO 同上
			}
			try {
				InformationDialog dlgInfo = new InformationDialog(InformationDialog.dlgTitleTips,
						InformationDialog.msgNoMatchInDB);
				dlgInfo.btnOK().click();
				Waits.shellClosed(dlgInfo);
			} catch (WidgetNotFoundException e) {
				e.printStackTrace();
			}
			assertTrue("未正确选择术语库：" + tBDBName,
					dlgPrjSetting.table().containsTextInColumn(tBDBName, dlgPrjSetting.tblColName()));
		}
		nextAction(nextAction);
	}

	/**
	 * 操作完成后的下一步动作
	 * @param nextAction
	 *            动作代码，本类常量;
	 */
	private void nextAction(NextAction nextAction) {
		switch (nextAction) {
		case OK: {
			dlgPrjSetting.btnOK().click();
			Waits.shellClosed(dlgPrjSetting);
			break;
		}
		case CANCEL: {
			dlgPrjSetting.btnCancel().click();
			Waits.shellClosed(dlgPrjSetting);
			break;
		}
		case WAIT: {
			break;
		}
		default: {
			assertTrue("参数错误，无此下一步操作：" + nextAction, false);
		}
		}
	}

	/**
	 * 从 Excel 文件中读取测试数据并赋值给相应变量;
	 */
	private void getDataPrjSetting() {
		name = data.getTextOrEmpty(ExcelData.colPrjName);
		client = data.getTextOrEmpty(ExcelData.colClient);
		company = data.getTextOrEmpty(ExcelData.colCompany);
		eMail = data.getTextOrEmpty(ExcelData.colEMail);
		remark = data.getTextOrEmpty(ExcelData.colRemark);
		srcLang = TsUIConstants.getLang(data.getTextOrEmpty(ExcelData.colSrcLang));
		tgtLangs = TsUIConstants.getLangs(data.getTextArray(ExcelData.colTgtLang));
		// dbType = data.getDBType();
		tMDBName = data.getTextOrEmpty(ExcelData.colTMDBName);
		tBDBName = data.getTextOrEmpty(ExcelData.colTBDBName);
		// address = data.getServer();
		DBManagement dbMgmt = new DBManagement(row);
		isTMDBExist = dbMgmt.isDBExist(tMDBName, false);
		isTBDBExist = dbMgmt.isExist(tBDBName);
		dbMgmt.closeDialog();
	}
}
