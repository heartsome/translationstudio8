package net.heartsome.cat.ts.test.basecase.menu.file;

import static org.junit.Assert.assertTrue;

import java.io.File;

import net.heartsome.cat.ts.test.basecase.common.ExcelData;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.Entry;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.ExpectedResult;
import net.heartsome.cat.ts.test.ui.dialogs.FileNewDialog;
import net.heartsome.cat.ts.test.ui.dialogs.NewProjectDialog;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.tasks.Waits;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;
import net.heartsome.test.swtbot.utils.SWTBotUtils;
import net.heartsome.test.utilities.common.FileUtil;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsRow;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;

/**
 * 项目相关操作
 * @author felix_lu
 */
public class ProjectCreate {

	private final TS ts = TS.getInstance();

	/**
	 * 下一步操作
	 * @author felix_lu
	 * @version
	 * @since JDK1.6
	 */
	public enum NextAction {
		CANCEL, BACK, NEXT, FINISH,
	}

	/** 创建向导的对话框 shell. */
	private NewProjectDialog dialog;
	/** 创建项目信息相关数据 */
	private HsRow rowCreatePrjData;
	private ExcelData data;
	private String name;
	private String remark;
	private String client;
	private String company;
	private String eMail;
	private String srcLangCode;
	private String[] tgtLangCodes;
	// private List<String[]> dbTMs;
	// private List<String[]> dbTBs;
	// private String[] srcFiles;

	private ExpectedResult expectedResult;
	private String invalidChar;

	/**
	 * 
	 */
	public ProjectCreate() {
	}

	/**
	 * @param rowCreatePrjData
	 */
	public ProjectCreate(HsRow rowCreatePrjData) {
		this.rowCreatePrjData = rowCreatePrjData;
		data = new ExcelData(rowCreatePrjData);
	}

	/**
	 * @param name
	 * @param srcLangCode
	 * @param tgtLangCodes
	 */
	public ProjectCreate(String name, String srcLangCode, String... tgtLangCodes) {
		this.name = name;
		this.srcLangCode = srcLangCode;
		this.tgtLangCodes = tgtLangCodes;
	}

	/**
	 * 从指定的入口打开新建对话框
	 * @param from
	 * @throws ParseException
	 */
	public void openCreateProjectDialog(Entry from) {
		switch (from) {
		case MENU: {
			ts.menuFileNewProject().click();
			dialog = new NewProjectDialog(FileNewDialog.NEW_TYPE_PROJECT);
			break;
		}
		case SHORTCUT: {
			try {
				ProjectTreeView.getTree().pressShortcut(SWTBotUtils.getCtrlOrCmdKey(), KeyStroke.getInstance("N"));
			} catch (ParseException e) {
				assertTrue("快捷键解析错误。", false);
			}
			dialog = new NewProjectDialog(FileNewDialog.NEW);
			break;
		}
		case CONTEXT_MENU: {
			ProjectTreeView.getTree().contextMenu("新建记忆库...").click();
//			ProjectTreeView.getInstance().ctxMenuNewProject().click();
			dialog = new NewProjectDialog(FileNewDialog.NEW);
			break;
		}
//		case CONTEXT_MENU_ALT: {
//			ProjectTreeView.getInstance().ctxMenuNewProject().click();
//			dialog = new NewProjectDialog(FileNewDialog.NEW_TYPE_PROJECT);
//			break;
//		}
		default: {
			assertTrue("参数错误：创建项目入口 from 不正确。", false);
		}
		}
		assertTrue(dialog.isActive());

		// 选择新建类型为 Project，下一步
//		dialog.treeiProject().select();
//		assertTrue(dialog.btnNext().isEnabled());
//		dialog.btnNext().click();
	}

	/**
	 * 创建项目第一步：基本信息;
	 * @param nextAction
	 *            向导方向，请使用常量;
	 */
	public void createPrj1BasicInfo(NextAction nextAction, ExpectedResult expectedResult, String invalidChar) {
		assertTrue("参数错误：项目名称为 null。", name != null);
		dialog.setBasicInfo(name, remark, client, company, eMail);

		switch (nextAction) {
		case BACK: {
			assertTrue(dialog.btnBack().isEnabled());
			dialog.btnBack().click();
			break;
		}
		case NEXT: {
			switch (expectedResult) {
			case SUCCESS: {
				assertTrue(dialog.btnNext().isEnabled());
				dialog.btnNext().click();
				break;
			}
			case DUPLICATED_NAME: {
				assertTrue(!dialog.btnNext().isEnabled());
				assertTrue(dialog.msgDucplicatedProjectName().isVisible());
				break;
			}
			case INVALID_NAME: {
				assertTrue(!dialog.btnNext().isEnabled());
				assertTrue(dialog.msgInvalidCharInResourceName(invalidChar, name).isVisible());
				break;
			}
			default: {
				assertTrue("参数错误：expectedResult 不正确。", false);
			}
			}
			break;
		}
		default: {
			assertTrue("参数错误：nextAction 不正确。", false);
		}
		}
	}

	/**
	 * 创建项目第二步：语言对;
	 * @param nextAction
	 *            向导方向，请使用常量;
	 */
	public void createPrj2LangPair(NextAction nextAction) {
		assertTrue("参数错误：项目源语言为 null。", srcLangCode != null);
		assertTrue("参数错误：项目目标语言为 null。", tgtLangCodes != null);
		for (String tgtLangCode : tgtLangCodes) {
			assertTrue("参数错误：项目目标语言为 null。", tgtLangCode != null);
		}
		dialog.setLangPair(srcLangCode, tgtLangCodes);

		switch (nextAction) {
		case BACK: {
			dialog.btnBack().isActive();
			dialog.btnBack().click();
			break;
		}
		case NEXT: {
			dialog.btnNext().isActive();
			dialog.btnNext().click();
			break;
		}
		case FINISH: {
			dialog.btnFinish().isActive();
			dialog.btnFinish().click();
			break;
		}
		default: {
			assertTrue("参数错误：nextAction 不正确。", false);
		}
		}
	}

	/**
	 * 直接从指定的 Excel 表格行中读取数据并创建项目。
	 * @param from
	 *            ;
	 * @throws ParseException
	 */
	public void createPrj(Entry from) {
		assertTrue("参数错误：Excel 表格数据为 null。", rowCreatePrjData != null);
		openCreateProjectDialog(from);
		getDataBasicInfo();
		if (expectedResult.equals(TsUIConstants.ExpectedResult.SUCCESS)) { // 预期结果为创建成功的话，需要先删除可能以前创建过但未删除的项目目录，避免影响后面的创建结果
			File oldPrjFile = new File(FileUtil.joinPath(FileUtil.getWorkspacePath(), name));
			FileUtil.deleteIfExist(oldPrjFile);
		}
		createPrj1BasicInfo(NextAction.NEXT, expectedResult, invalidChar);
		if (expectedResult.equals(TsUIConstants.ExpectedResult.SUCCESS)) {
			getDataLangPairs();
			createPrj2LangPair(NextAction.FINISH);
			Waits.shellClosed(dialog);
			Waits.prjExistOnTree(name);
			Waits.prjExistInWorkspace(name);
		} else {
			dialog.btnCancel().click();
			Waits.shellClosed(dialog);
		}
	}

	// /**
	// * @param from
	// * @param prjBasicInfo
	// * @param srcLangCode
	// * @param tgtLangCodes
	// * @param dbTMs
	// * @param dbTB
	// * @param srcFiles
	// * ;
	// * @throws ParseException
	// */
	// public void createPrj(int from, String name, String remark, String client, String company, String eMail,
	// String srcLangCode, List<String> tgtLangCodes, List<String[]> dbTMs, List<String[]> dbTB,
	// List<String[]> srcFiles) throws ParseException {
	// openCreateProjectDialog(from);
	// dialog.setBasicInfo(name, remark, client, company, eMail);
	// dialog.btnNext().isActive();
	// dialog.btnNext().click();
	// dialog.setLangPair(srcLangCode, tgtLangCodes);
	// dialog.btnFinish().isActive();
	// dialog.btnFinish().click();
	// Waits.shellClosed(dialog);
	// Waits.prjExist(name);
	// }

	/**
	 * 从 Excel 文件中取项目基本信息数据，并赋值给相应的成员变量;
	 */
	public void getDataBasicInfo() {
		name = data.getTextOrEmpty(ExcelData.colPrjName);
		remark = data.getTextOrEmpty(ExcelData.colRemark);
		client = data.getTextOrEmpty(ExcelData.colClient);
		company = data.getTextOrEmpty(ExcelData.colCompany);
		eMail = data.getTextOrEmpty(ExcelData.colEMail);
		expectedResult = data.getExpectedResult();
		invalidChar = data.getTextOrEmpty(ExcelData.colInvChar);
	}

	/**
	 * 从 Excel 指定序号的行中读取语言对数据，并赋值给相应的成员变量;
	 */
	public void getDataLangPairs() {
		srcLangCode = data.getTextOrEmpty(ExcelData.colSrcLang);
		tgtLangCodes = data.getTextArray(ExcelData.colTgtLang);
	}

}
