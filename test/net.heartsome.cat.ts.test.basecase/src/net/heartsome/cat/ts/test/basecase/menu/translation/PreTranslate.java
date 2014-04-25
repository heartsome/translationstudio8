package net.heartsome.cat.ts.test.basecase.menu.translation;

import static org.junit.Assert.assertTrue;
import net.heartsome.cat.ts.test.basecase.common.ExcelData;
import net.heartsome.cat.ts.test.basecase.menu.file.ProjectFile;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.Entry;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.ExpectedResult;
import net.heartsome.cat.ts.test.ui.dialogs.PreTranslateDialog;
import net.heartsome.cat.ts.test.ui.dialogs.PreTranslateResultDialog;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsRow;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

/**
 * 预翻译基础用例，封装测试该功能常用的方法，预翻译用到的文件、数据库等，需要在此类之外复制或设置好。
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class PreTranslate {

	private TS ts;
	private PreTranslateDialog dlgPreTrans;
	private PreTranslateResultDialog dlgPreTransResult;

	private HsRow row;
	private ExcelData data;
	private ProjectFile prjFile;
	private String prjName;
	private String fileType;
	private String filePath;
	private String fileName;
	private String fileFullPath;
	private int dbType;
	private String server;
	private String port;
	private String instance;
	private String dbPath;
	private String username;
	private String password;
	private String dbName;
	private ExpectedResult expResult;

	/**
	 * @param row
	 *            用于读取测试数据的 Excel 行对象
	 */
	public PreTranslate(HsRow row) {
		this.row = row;
		data = new ExcelData(row);
		ts = TS.getInstance();
	}

	/**
	 * 选择文件;
	 */
	private void select() {
		prjFile = new ProjectFile(row);
		prjFile.select();
		fileFullPath = prjFile.getPath();
	}

	/**
	 * 打开预翻译对话框
	 * @param from
	 *            入口，请使用 TSUIConstants 类提供的常量
	 * @return 预翻译对话框对象;
	 */
	private PreTranslateDialog openPreTransDlg(Entry from) {
		select();

		switch (from) {
		case MENU: {
			ts.menuTranslationPreTrans().click();
			break;
		}
		case CONTEXT_MENU: {
			ProjectTreeView.getInstance().ctxMenuPreTranslate().click();
			break;
		}
		case SHORTCUT: {
			ProjectTreeView.getTree().pressShortcut(Keystrokes.SHIFT, Keystrokes.F5);
			break;
		}
		default: {
			assertTrue("参数错误，无此入口：" + from, false);
		}
		}

		dlgPreTrans = new PreTranslateDialog();
		assertTrue("预翻译对话框未正确打开。", dlgPreTrans.isOpen());
		return dlgPreTrans;
	}

	/**
	 * 预翻译
	 * @param from
	 *            入口，请使用 TSUIConstants 类提供的常量;
	 */
	public void preTranslate(Entry from) {
		getDataPreTrans();
		select();
		openPreTransDlg(from);
		assertTrue("未正确添加选中的文件：" + fileFullPath,
				dlgPreTrans.table().containsTextInColumn(fileFullPath, dlgPreTrans.tblColFile()));
		dlgPreTrans.btnOK().click();

		HSBot.bot().waitUntil(new DefaultCondition() {

			public boolean test() throws Exception {
				try {
					dlgPreTransResult = new PreTranslateResultDialog();
					return dlgPreTransResult.isOpen();
				} catch (WidgetNotFoundException e) {
					return false;
				}
			}

			public String getFailureMessage() {
				return "未正确显示预翻译结果对话框。";
			}
		}, 3600000);
		assertTrue("未正确该文件的预翻译结果：" + fileFullPath,
				dlgPreTransResult.table().containsTextInColumn(fileFullPath, dlgPreTransResult.tblColFile()));
		dlgPreTransResult.btnOK().click();
	}

	/**
	 * 从 Excel 文件中读取测试数据;
	 */
	private void getDataPreTrans() {
		assertTrue("参数错误，row 为 null。", row != null);
		prjName = data.getTextOrEmpty(ExcelData.colPrjName);
		fileType = data.getTextOrEmpty(ExcelData.colFileType);
		filePath = data.getTextOrEmpty(ExcelData.colFilePath);
		fileName = data.getTextOrEmpty(ExcelData.colFileName);
		expResult = data.getExpectedResult();
		// dbType = data.getDBType();
		// server = data.getServer();
		// port = data.getPort();
		// instance = data.getInstance();
		// dbPath = data.getDBPath();
		// dbName = data.getDBName();
		// username = data.getUsername();
		// password = data.getPassword();
	}

}
