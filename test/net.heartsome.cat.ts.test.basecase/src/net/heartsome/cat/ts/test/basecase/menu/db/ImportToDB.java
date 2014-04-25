package net.heartsome.cat.ts.test.basecase.menu.db;

import static org.junit.Assert.assertTrue;

import java.io.File;

import net.heartsome.cat.ts.test.basecase.common.ExcelData;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.Entry;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.ExpectedResult;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.ImportType;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.UpdateMode;
import net.heartsome.cat.ts.test.ui.dialogs.ImportDialog;
import net.heartsome.cat.ts.test.ui.dialogs.InputDialog;
import net.heartsome.cat.ts.test.ui.dialogs.PreferencesDialog;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.tasks.Waits;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.waits.IsWidgetInvisible;
import net.heartsome.test.swtbot.waits.IsWidgetTextEquals;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsRow;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

/**
 * 导入 TMX、TBX 的基础用例封装。
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class ImportToDB {

	private TS ts;
	private HsRow row;
	private ExcelData data;
	private ImportDialog dialog;
	private ImportType importType;
	private String filePath;
	private String dbName;
	private UpdateMode updateMode;
	private ExpectedResult expectedResult;

	/**
	 * @param row
	 *            Excel 数据来源
	 */
	public ImportToDB(ImportType importType, HsRow row) {
		this.row = row;
		data = new ExcelData(row);
		this.importType = importType;
		ts = TS.getInstance();
	}

	/**
	 * 完整流程：导入 ImportType.TMX 文件
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类提供的枚举;
	 */
	public void importTMX(Entry from) {
		assertTrue("仅功能仅在导入模式为 ImportType.TMX 时可用。", importType.equals(TsUIConstants.ImportType.TMX));
		importFile(from);
	}

	/**
	 * 完整流程：导入 ImportType.TBX 文件
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类提供的枚举;
	 */
	public void importTBX(Entry from) {
		assertTrue("仅功能仅在导入模式为 ImportType.TBX 时可用。", importType.equals(TsUIConstants.ImportType.TBX));
		importFile(from);
	}

	/**
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类提供的枚举;
	 */
	private void importFile(Entry from) {
		getDataAll();
		openImportDialog(from);

		// 预期结果验证：未选择文件
		if (expectedResult.equals(TsUIConstants.ExpectedResult.NO_FILE)) {
			assertTrue(dialog.txtWLblFile().getText().equals(""));
			dialog.btnFinish().click();
			assertTrue(dialog.msgNoFileToImport().isVisible());

		} else {
			// 无效路径（文件不存在）的预处理
			if (expectedResult.equals(TsUIConstants.ExpectedResult.INVALID_PATH)) {
				File file = new File(filePath);
				if (file.exists()) { // 此预期结果分支不应该存在该文件，故需先删除
					file.delete();
				}
			}

			// 浏览文件
			dialog.btnBrowse().click();

			// 用 Mock 的输入对话框代替系统原生对话框来输入文件路径
			InputDialog idlg = new InputDialog(InputDialog.OPEN_FILE);
			idlg.txt().setText(filePath);
			idlg.btnOK().click();
			Waits.shellClosed(idlg);

			HSBot.bot().waitUntil(new IsWidgetTextEquals(dialog.txtWLblFile(), filePath), 10000);

			// 预期结果验证：未选择数据库
			if (expectedResult.equals(TsUIConstants.ExpectedResult.NO_DB)) {
				assertTrue(dialog.txtWLblDatabase().getText().equals(""));
				assertTrue(!dialog.btnFinish().isEnabled());
				assertTrue(dialog.msgNoDBToImport().isVisible());

			} else {

				// 选择库
				dialog.btnSelectDB().click();
				MemoryDBManagement md = new MemoryDBManagement(row);
				md.setFromImportDb(true);
				md.selectDB(dbName);
//				new DBManagement(row).selectDB(dbName);
				assertTrue("记忆库/术语库未正确选择。", dbName.equals(dialog.txtWLblDatabase().getText()));

				// 选择更新策略
				dialog.btnSetting().click();
				PreferencesDialog dlgPref = new PreferencesDialog();
				selectUpdateMode(dlgPref);

				// 开始导入
				dialog.btnFinish().click();

				// 预期结果验证：成功导入文件
				if (expectedResult.equals(TsUIConstants.ExpectedResult.SUCCESS)) {
					try {
						HSBot.bot().waitUntil(new IsWidgetInvisible(dialog.msgImporting()), 600000); // 暂时只设置等待 10 分钟
					} catch (WidgetNotFoundException e) {
						e.printStackTrace();
					}
					assertTrue(dialog.msgImportSuccess().isVisible());

					// 预期结果验证：文件内容有误、无效路径（文件不存在）、错误的文件类型
				} else if (expectedResult.equals(TsUIConstants.ExpectedResult.FILE_ERROR)
						|| expectedResult.equals(TsUIConstants.ExpectedResult.INVALID_PATH)
						|| expectedResult.equals(TsUIConstants.ExpectedResult.WRONG_TYPE)) {
					assertTrue(dialog.msgFileError().isVisible());

				} else {
					assertTrue("无此预期结果：" + expectedResult, false);
				}
			}
		}
		if (dialog.isOpen()) {
			dialog.btnCancel().click();
			Waits.shellClosed(dialog);
		}
	}

	/**
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类提供的枚举;
	 * @return 导入对话框对象
	 */
	public ImportDialog openImportDialog(Entry from) {
		switch (from) {

		case MENU: {

			switch (importType) {

			case TMX: {
				ts.menuDBImportTMXFile().click();
				dialog = new ImportDialog(ImportDialog.TMX);
				break;
			}
			case TBX: {
				ts.menuDBImportTBXFile().click();
				dialog = new ImportDialog(ImportDialog.TBX);
				break;
			}
			default: {
				assertTrue("参数错误，无此导入类型：" + importType, false);
			}
			}
			break;
		}

		default: {
			assertTrue("参数错误，该功能无此入口：" + from, false);
		}
		}

		return dialog;
	}

	/**
	 * 选择记忆库/术语库的更新策略。
	 * @param dlg
	 *            首选项对话框;
	 */
	public void selectUpdateMode(PreferencesDialog dlg) {
		String msg = "参数错误，无此更新策略：" + updateMode;
		switch (importType) {

		case TMX: {
			assertTrue(dlg.treiTMDB().isSelected());

			switch (updateMode) {
			case ALWAYS_ADD: {
				dlg.tmRadBtnUpdateModeAlwaysAdd().click();
				break;
			}
			case OVERWRITE: {
				dlg.tmRadBtnUpdateModeOverwrite().click();
				break;
			}
			case IGNORE: {
				dlg.tmRadBtnUpdateModeIgnore().click();
				break;
			}
			default: {
				assertTrue(msg, false);
			}
			}
			break;
		}

		case TBX: {
			assertTrue(dlg.treiTBDB().isSelected());

			switch (updateMode) {
			case ALWAYS_ADD: {
				dlg.tbRadBtnUpdateModeAlwaysAdd().click();
				break;
			}
			case OVERWRITE: {
				dlg.tbRadBtnUpdateModeOverwrite().click();
				break;
			}
			case MERGE: {
				dlg.tbRadBtnUpdateModeMerge().click();
				break;
			}
			case IGNORE: {
				dlg.tbRadBtnUpdateModeIgnore().click();
				break;
			}
			default: {
				assertTrue(msg, false);
			}
			}
			break;
		}

		default: {
			assertTrue("参数错误，无此导入类型：" + importType, false);
		}
		}
		dlg.btnOK().click();
		Waits.shellClosed(dlg);
	}

	/**
	 * 从 Excel 文件中读取所需的测试数据，其中库管理部分直接传 HsRow 对象即可，无需在此读取;
	 */
	public void getDataAll() {
		filePath = data.getTextOrEmpty(ExcelData.colFilePath);
		dbName = data.getTextOrEmpty(ExcelData.colDBName);
		updateMode = data.getUpdateMode();
		expectedResult = data.getExpectedResult();
	}

}
