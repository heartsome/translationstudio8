package net.heartsome.cat.ts.test.demos;

import static org.junit.Assert.assertTrue;

import java.util.List;

import net.heartsome.cat.ts.test.ui.dialogs.OpenFileDialog;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.editors.XlfEditor;
import net.heartsome.cat.ts.test.ui.utils.XliffUtil;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeItem;
import net.heartsome.test.swtbot.junit.HSJunit4ClassRunner;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.utilities.common.FileUtil;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HSJunit4ClassRunner.class)
public class TestDemos {
	private static SWTWorkbenchBot bot;
	private static String prjName;
	
	@BeforeClass
	public static void startTest() {
		if (DEBUG_MODE == 2) {
			SWTBotPreferences.PLAYBACK_DELAY = 500;
		}
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		bot = HSBot.bot();
	}

	@Before
	public void setUp() {
		bot.closeAllEditors();
		prjName = "prjBot-001";
	}

	@After
	public void tearDown() {
//		bot.sleep(50000);
	}
	
	// 0 表示关闭 Debug 功能，即每次测试完成后还原测试文件，且用默认速度回放；
	// 1 表示每次测试完成后不还原测试文件，且用默认速度回放；
	// 2 表示每次测试完成后不还原测试文件，且用慢速回放。
	private static int DEBUG_MODE = 1;

	@AfterClass
	public static void exitTest() {
		if (DEBUG_MODE == 0) {
			String filePath = FileUtil.joinPath(FileUtil.getExecutionPath() + prjName + "XLIFF" + "HSCAT8-83T.xlf");
			String originalFilePath = FileUtil.joinPath(FileUtil.getExecutionPath() + prjName + "XLIFF" + "HSCAT8-83a.xlf");
			FileUtil.replaceFile(filePath, originalFilePath);
		}
	}
	
	@Ignore
	@Test
	public void TestProject() {
//		String fileName = "temp.xlf";
		String srcLang = "en-US";
		String tgtLang = "zh-CN";
//		String icon = "E:\\temp\\imgs\\LineNumber.png";
//		String menuText = "设置文本段批准状态";
		ProjectTreeItem pTN = ProjectTreeItem.getInstance(prjName);
		pTN.ctxMenuOpenProjectFiles();
		
//		String filePath = "D:\\Temp\\ts.win32.win32.x86\\workspace\\prjBot-001\\XLIFF\\temp.xlf";
//		String fileName = new Path(filePath).lastSegment();
		TS ts = TS.getInstance();
//		ts.menuFileOpenFile().click();
//		OpenFileDialog.openFile(filePath);
		XlfEditor xe = ts.getXlfEditor(prjName);
		xe.selectLangPair(srcLang, tgtLang);
		xe.changeEditorLayout();
		xe.selectSourceCell(4);
		System.out.println(xe.rowIdOfSelectedSeg());
//		String filePath = "D:\\Temp\\TestExcel.xls";
//		String column = "password";
//		ExcelUtil exl = new ExcelUtil(filePath);
//		System.err.println(exl.getNextText(column));
		
//		XLIFFUtil xu = new XLIFFUtil(filePath);
//		System.err.println(xu.getSouceLang());
//		System.err.println(xu.getTargetLang());
		
	}
	
	@Ignore
	@Test
	public void TestFile() {
		String fileName = "HSCAT8-83.xlf";
		ProjectTreeItem pTN = ProjectTreeItem.getInstance(prjName);
		pTN.ctxMenuOpenFile(fileName);
		TS ts = TS.getInstance();
		XlfEditor xe = ts.getXlfEditor(fileName);
		
		int segNum = 2;
		xe.gotoSeg(segNum);
		String rowID = xe.rowIdOfSegNum(segNum);

		XliffUtil xu = new XliffUtil(rowID);
		assertTrue(!xu.tuIsApproved());
		xe.clickContextMenu(xe.ctxMenuApproveStatus(), 
				xe.ctxMenuapproveStatusApproveCurrentSeg());
		xu = new XliffUtil(rowID);
		assertTrue(xu.tuIsApproved());
	}

	@Ignore
	@Test
	public void TestFile2() {
//		String filePath = "E:\\My Documents\\junit-workspace\\prjBot-001\\XLIFF\\HSCAT8-3.xlf";
//		String filePath = "/home/felix/junit-workspace/prjBot-001/XLIFF/HSCAT8-3.xlf";
		String filePath = "/Users/felix_lu/bin/ts.cocoa.macosx.x86_64/Eclipse.app/Contents/MacOS/workspace/testProject/XLIFF/HSCAT8-83.xlf";
		TS ts = TS.getInstance();
		ts.menuFileOpenFile().click();
		OpenFileDialog.openFile(filePath, true, true);
		List<String[]> langPairs = XliffUtil.getAllLangPairs(filePath);
		for (String[] langPair : langPairs) {
			System.err.println(langPair[0] + " -> " + langPair[1] + ": " + 
					XliffUtil.tuCountOfLangPair(filePath, langPair[0], langPair[1], false));
		}
	}
	
	@Ignore
	@Test
	public void Test1() {
		System.out.println(FileUtil.getExecutionPath());
		System.out.println(FileUtil.getWorkspacePath());
	}
}
