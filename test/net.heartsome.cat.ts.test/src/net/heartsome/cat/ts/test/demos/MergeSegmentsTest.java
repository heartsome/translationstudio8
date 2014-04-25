package net.heartsome.cat.ts.test.demos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.heartsome.cat.ts.test.basecase.MergeSegments;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.editors.XlfEditor;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;
import net.heartsome.test.swtbot.junit.HSJunit4ClassRunner;
import net.heartsome.test.swtbot.junit.Repeat;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.utilities.common.FileUtil;
import net.heartsome.test.utilities.poi.ExcelUtil;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsColumn;

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
public class MergeSegmentsTest {
	
	SWTWorkbenchBot bot;
	TS ts;
	String prjName;
	String fileName;
	XlfEditor xe;
	int segNum;
	char INVISIBLE_CHAR;
	String reTag;
	static HsSheet hss;
	static HsColumn srcTextCol1;
	static HsColumn srcTextCol2;
	
	static boolean RESTORE_TYPE_FILE = true;	// 默认每次测试完成后还原测试文件
	static boolean SLOW_PLAYBACK = false;	// 默认用正常速度回放
	
	
	/**
	 * 所有测试开始之前执行
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@BeforeClass
	public static void oneTimeSetUp() throws FileNotFoundException, IOException {
		String excelPath = "testData/SegmentMerge.xls";
		hss = new ExcelUtil(excelPath).new HsSheet();
		srcTextCol1 = hss.new HsColumn("SourceText1");
		srcTextCol2 = hss.new HsColumn("SourceText2");
	}
	
	
	/**
	 * 所有测试完成之后执行
	 */
	@AfterClass
	public static void oneTimeTearDown() {
		
	}
	
	
	/**
	 * 每个测试开始之前执行
	 */
	@Before
	public void setUp() {
		if (SLOW_PLAYBACK) {
			SWTBotPreferences.PLAYBACK_DELAY = 500;
		}
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		INVISIBLE_CHAR = XlfEditor.INVISIBLE_CHAR;
		reTag = "(" + INVISIBLE_CHAR + "\\d+)?" + INVISIBLE_CHAR + "(x|bx|ex|g|bpt|ept|mrk|sub|ph|it)" + INVISIBLE_CHAR + "(\\d+" + INVISIBLE_CHAR + ")?";
		bot = HSBot.bot();
		bot.closeAllEditors();
		ts = TS.getInstance();
		prjName = "swtBot-Project-001";
		fileName = "HSCAT8-3.xlf";
		ProjectTreeView.doubleClickXlfFile(prjName, fileName);
		xe = new XlfEditor(bot.editorByTitle(fileName));
	}

	
	/**
	 * 每次测试完成之后执行
	 */
	@After
	public void tearDown() {
		if (RESTORE_TYPE_FILE) {
			String filePath = "testFiles/HSCAT8-3.xlf";
			String originalFilePath = "testFiles/HSCAT8-3a.xlf";
			File file = new File(filePath);
			if (file.exists()) {
				file.delete();
			}
			File fileO = new File(originalFilePath);
			try {
				FileUtil.copyFile(fileO, file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		bot.sleep(500);
	}

	
//	@Ignore
	@Repeat(5)
	@Test
	public void MergeSegment1() {
//		String seg1Text = "Test segment 1a.";
//		String seg2Text = "Test segment 1b.";
		String seg1Text = srcTextCol1.getNextText();
		String seg2Text = srcTextCol2.getNextText();
		int[] segNum = new int[2];
		segNum[0] = xe.segNumContainsSource(seg1Text);
		segNum[1] = xe.segNumContainsSource(seg2Text);
		MergeSegments.MergeWithNextSeg(xe, segNum);
	}
	
	@Ignore
	@Test
	public void MergeSegment2() {
		String seg1Text = "Test segment 2a.";
		String seg2Text = "Test segment 2b.";
		int[] segNum = new int[2];
		segNum[0] = xe.segNumContainsSource(seg1Text);
		segNum[1] = xe.segNumContainsSource(seg2Text);
		MergeSegments.MergeWithPrevSeg(xe, segNum);
	}
	
	@Ignore
	@Test
	public void MergeSegment3() {
//		String segText = "Test segment 19.";
//		segNum = xe.segNumContainsSource(segText);
		segNum = 1;
		MergeSegments.MergeWithNoPrev(xe, segNum);
	}
	
	@Ignore
	@Test
	public void MergeSegment4() {
//		String segText = "Test segment 20, Test segment 23a, Test segment 24a.";
//		segNum = xe.segNumContainsSource(segText);
		segNum = xe.segCount();
		MergeSegments.MergeWithNoNext(xe, segNum);
	}
	
}
