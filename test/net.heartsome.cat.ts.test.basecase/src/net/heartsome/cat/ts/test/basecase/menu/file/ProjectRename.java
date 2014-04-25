package net.heartsome.cat.ts.test.basecase.menu.file;

import static org.junit.Assert.assertTrue;
import net.heartsome.cat.ts.test.basecase.common.ExcelData;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.Entry;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.ExpectedResult;
import net.heartsome.cat.ts.test.ui.dialogs.ProjectExistsDialog;
import net.heartsome.cat.ts.test.ui.dialogs.RenameProblemsDialog;
import net.heartsome.cat.ts.test.ui.tasks.Waits;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsRow;

import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

/**
 * 项目相关操作
 * @author felix_lu
 */
public class ProjectRename {

	// /** 刷新文件系统时的最大重试次数 */
	// private static final int MAX_RETRY = 10;

	/** 重命名项目相关数据 */
	// private HsRow rowRenamePrjData;
	private String oldName;
	private String newName;
	private ExpectedResult expectedResult;
	private String invalidChar;
	private boolean overwrite;
	private ExcelData data;

	private ProjectTreeView viewPrjTree = ProjectTreeView.getInstance();
	private SWTBotTree treePrj = ProjectTreeView.getTree();

	/**
	 * 
	 */
	public ProjectRename() {
	}

	/**
	 * @param rowRenamePrjData
	 */
	public ProjectRename(HsRow rowRenamePrjData) {
		// this.rowRenamePrjData = rowRenamePrjData;
		data = new ExcelData(rowRenamePrjData);
	}

	/**
	 * 重命名项目
	 * @param oldName
	 *            旧项目名称
	 * @param newName
	 *            新项目名称
	 */
	private void renamePrj(Entry from, String oldName, String newName, ExpectedResult expectedResult,
			String invalidChar, boolean overwrite) {

		// 先取消选中项目，避免后面没有找到匹配项目时仍有之前选中的项目被误操作
		treePrj.unselect();
		Waits.prjExistOnTree(oldName);

		// 选择项目并重命名
		treePrj.select(oldName);

		switch (from) {
		case CONTEXT_MENU: {
			viewPrjTree.ctxMenuRename().click();
			break;
		}
		case SHORTCUT: {
			treePrj.pressShortcut(Keystrokes.F2);
			break;
		}
		default: {
			assertTrue("错误的入口参数：" + from, false);
		}
		}

		// 进入重命名状态
		SWTBotText txtPrjName = viewPrjTree.bot().text(oldName);
		// 输入新名称并回车确认
		txtPrjName.typeText(newName + "\n");

		/* 以下为异常情况处理及重命名结果验证部分 */

		// 若有多余的对话框，说明两种可能：一是出现进度对话框；二是重命名出现了问题（如文件系统不同步）。
		// 不同步的问题暂不考虑——若要解决，思路是判断是否出现不同步的信息，若有则刷新后重试重命名，直到达到最大重试次数后放弃。
		if (HSBot.bot().shells().length > 1) { // FIXME: 可能需要修改这种判断方式，试试 activeShell()?
			Waits.progressFinished();

			String title = HSBot.bot().activeShell().getText();
			String errMsg = "重命名出错。对话框标题：" + title;

			switch (expectedResult) {

			case SUCCESS: {
				// 验证结果
				assertRenamed(oldName, newName);
				break;
			}

			case INVALID_NAME: {
				if (title.equals(TsUIConstants.getString("dlgTitleRenameProblems"))) {
					RenameProblemsDialog rpd = new RenameProblemsDialog(newName, invalidChar);
					assertTrue("未正确显示项目名非法的信息。", rpd.isInvalidMsgVisible());
					assertTrue(rpd.btnOK().isEnabled());
					rpd.btnOK().click();
					Waits.shellClosed(rpd);

					assertNotRenamed(oldName, newName);
				} else {
					assertTrue(errMsg, false);
				}
				break;
			}

			case DUPLICATED_NAME: {
				if (title.equals(TsUIConstants.getString("dlgTitleProjectExists"))) {
					ProjectExistsDialog ped = new ProjectExistsDialog(newName);
					assertTrue("未正确显示项目名称已存在的信息。", ped.isProjectExistsMsgVisible());
					assertTrue(ped.btnYes().isEnabled());
					assertTrue(ped.btnNo().isEnabled());
					if (overwrite) {
						ped.btnYes().click();
						Waits.shellClosed(ped);
						Waits.progressFinished();

						assertRenamed(oldName, newName);
					} else {
						ped.btnNo().click();
						Waits.shellClosed(ped);
						Waits.progressFinished();

						assertNotOverwritten(oldName, newName);
					}
				} else {
					assertTrue(errMsg, false);
				}
				break;
			}

			default: {
				assertTrue(errMsg, false);
			}
			}

		} else { // 重命名正常，验证结果
			assertRenamed(oldName, newName);
		}
	}

	/**
	 * 根据从 Excel 文件里读取到的数据，重命名项目;
	 */
	public void renamePrj(Entry from) {
		getDataNames();
		renamePrj(from, oldName, newName, expectedResult, invalidChar, overwrite);
	}

	/**
	 * 断言（等待）成功重命名，即旧项目不存在、新项目存在
	 * @param oldName
	 * @param newName
	 */
	private void assertRenamed(String oldName, String newName) {
		treePrj.unselect();
		Waits.prjNotExistOnTree(oldName);
		Waits.prjNotExistInWorkspace(oldName);
		Waits.prjExistOnTree(newName);
		Waits.prjExistInWorkspace(newName);
	}

	/**
	 * 断言（等待）未重命名，即旧项目存在、新项目不存在
	 * @param oldName
	 * @param newName
	 */
	private void assertNotRenamed(String oldName, String newName) {
		treePrj.unselect();
		Waits.prjExistOnTree(oldName);
		Waits.prjExistInWorkspace(oldName);
		Waits.prjNotExistOnTree(newName);
		Waits.prjNotExistInWorkspace(newName);
	}

	/**
	 * 断言（等待）未覆盖，即新、旧名称的项目均存在
	 * @param oldName
	 * @param newName
	 */
	private void assertNotOverwritten(String oldName, String newName) {
		treePrj.unselect();
		Waits.prjExistOnTree(oldName);
		Waits.prjExistInWorkspace(oldName);
		Waits.prjExistOnTree(newName);
		Waits.prjExistInWorkspace(newName);
	}

	/**
	 * 从 Excel 文件中取重命名项目的数据，并赋值给相应的成员变量;
	 */
	public void getDataNames() {
		oldName = data.getTextOrEmpty(ExcelData.colOldName);
		newName = data.getTextOrEmpty(ExcelData.colNewName);
		expectedResult = data.getExpectedResult();
		invalidChar = data.getTextOrEmpty(ExcelData.colInvChar);
		overwrite = data.getBoolean(ExcelData.colOverwrite);
	}

}
