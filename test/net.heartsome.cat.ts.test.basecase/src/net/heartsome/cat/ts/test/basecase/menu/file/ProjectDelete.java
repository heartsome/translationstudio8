package net.heartsome.cat.ts.test.basecase.menu.file;

import static org.junit.Assert.assertTrue;
import net.heartsome.cat.ts.test.basecase.common.ExcelData;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.Entry;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.msgdialogs.ConfirmProjectDeleteDialog;
import net.heartsome.cat.ts.test.ui.msgdialogs.ProgressDialog;
import net.heartsome.cat.ts.test.ui.tasks.Waits;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsRow;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

/**
 * 项目相关操作
 * @author felix_lu
 */
public class ProjectDelete {

	/** 创建项目信息相关数据 */
	// private HsRow rowDeletePrjData;
	private String name;
	private boolean deleteContent;
	// private int expectedResult;
	private ExcelData data;

	/**
	 * 
	 */
	public ProjectDelete() {
	}

	/**
	 * @param rowDeletePrjData
	 */
	public ProjectDelete(HsRow rowDeletePrjData) {
		// this.rowDeletePrjData = rowDeletePrjData;
		data = new ExcelData(rowDeletePrjData);
	}

	/**
	 * @param name
	 * @param deleteContent
	 */
	public ProjectDelete(String name, boolean deleteContent) {
		this.name = name;
		this.deleteContent = deleteContent;
	}

	/**
	 * @param from
	 *            从指定的入口、按照从 Excel 中读取的数据删除项目;
	 */
	public void deletePrj(Entry from) {
		getDataDeleteProject();
		deletePrj(from, name, deleteContent);
	}

	/**
	 * 删除项目
	 * @param name
	 *            项目名称
	 * @param deleteContent
	 *            是否同时删除项目文件夹
	 */
	public void deletePrj(Entry from, String name, boolean deleteContent) {
		ProjectTreeView ptv = ProjectTreeView.getInstance();
		SWTBotTree treePrj = ProjectTreeView.getTree();
		Waits.prjExistOnTree(name);

		treePrj.unselect(); // 取消选中任何项目，避免误操作
		// 选择项目并删除
		treePrj.select(name).isActive();

		switch (from) {
		case CONTEXT_MENU: {
			ptv.ctxMenuDelete().click();
			break;
		}
		case MENU: {
			TS.getInstance().menuEditDelete().click();
			break;
		}
		default: {
			assertTrue("无此入口：" + from, false);
		}
		}

		// 确认删除对话框
		ConfirmProjectDeleteDialog cpd = new ConfirmProjectDeleteDialog(name);
		cpd.isActive();

		// 选择是否删除内容
		if (deleteContent) {
			cpd.radBtnAlsoDeleteContentsUnder().click();
			cpd.btnYes().click();
			Waits.shellClosed(cpd);

			SWTBotShell[] shells = HSBot.bot().shells();
			if (shells.length > 1) {
				ProgressDialog dlgProg = new ProgressDialog("dlgTitleProgressDeleteResource");
				Waits.shellClosed(dlgProg);
			}

			Waits.prjNotExistOnTree(name);
			Waits.prjNotExistInWorkspace(name);
		} else {
			cpd.radBtnDoNotDeleteContents().click();
			cpd.btnYes().click();
			Waits.shellClosed(cpd);

			SWTBotShell[] shells = HSBot.bot().shells();
			if (shells.length > 1) {
				ProgressDialog dlgProg = new ProgressDialog("dlgTitleProgressDeleteResource");
				Waits.shellClosed(dlgProg);
			}

			Waits.prjNotExistOnTree(name);
			Waits.prjExistInWorkspace(name);
		}
	}

	/**
	 * 从 Excel 文件中取删除项目数据，并赋值给相应的成员变量;
	 */
	public void getDataDeleteProject() {
		name = data.getTextOrEmpty(ExcelData.colPrjName);
		deleteContent = data.getBoolean(ExcelData.colDeleteContent);
		// expectedResult = data.getExpectedResult();
	}

}
