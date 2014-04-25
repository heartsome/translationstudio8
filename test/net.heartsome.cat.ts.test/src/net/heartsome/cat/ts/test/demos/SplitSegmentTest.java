package net.heartsome.cat.ts.test.demos;

import net.heartsome.cat.ts.test.basecase.SplitSegment;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.editors.XlfEditor;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;
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
public class SplitSegmentTest {
	
	SWTWorkbenchBot bot;
	TS ts;
	String prjName;
	String fileName;
	XlfEditor xe;
	int segNum;
	char INVISIBLE_CHAR;
	String reTag;
	
	
	/**
	 * 所有测试开始之前执行
	 */
	@BeforeClass
	public static void oneTimeSetUp() {
		
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
//		SWTBotPreferences.PLAYBACK_DELAY = 500;
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		INVISIBLE_CHAR = XlfEditor.INVISIBLE_CHAR;
		reTag = "(" + INVISIBLE_CHAR + "\\d+)?" + INVISIBLE_CHAR + "(x|bx|ex|g|bpt|ept|mrk|sub|ph|it)" + INVISIBLE_CHAR + "(\\d+" + INVISIBLE_CHAR + ")?";
		bot = HSBot.bot();
		bot.closeAllEditors();
		ts = TS.getInstance();
		prjName = "prjBot-001";
		fileName = "HSCAT8-2T.xlf";
		ProjectTreeView.doubleClickXlfFile(prjName, fileName);
		xe = new XlfEditor(bot.editorByTitle(fileName));
	}

	
	/**
	 * 每次测试完成之后执行
	 */
	@After
	public void tearDown() {
		String filePath = FileUtil.joinPath(FileUtil.getWorkspacePath(), prjName, "XLIFF", "HSCAT8-2T.xlf");
		String originalFilePath = "testFiles/HSCAT8-2.xlf";
		FileUtil.replaceFile(filePath, originalFilePath);
//		bot.sleep(500);
	}
	
	
	@Ignore
	@Test
	public void SplitSegment1() {
		String segText = "Test segment 2a.";
		String afterText = "Test segment 2";
		String beforeText = "a.";
		segNum = xe.segNumContainsSource(segText);
		SplitSegment.splitBetween(xe, segNum, afterText, beforeText);
	}
	
	@Ignore
	@Test
	public void SplitSegment2() {
		String segText = "Test segment 3a";
		segNum = xe.segNumContainsSource(segText);
		SplitSegment.splitAtBeginning(xe, segNum);
	}
	
	@Ignore
	@Test
	public void SplitSegment3() {
		String segText = "Test segment 3a";
		segNum = xe.segNumContainsSource(segText);
		SplitSegment.splitAtEnd(xe, segNum);
	}
	
	@Ignore
	@Test
	public void SplitSegment4() {
		String segText = "Test segment 23";
		segNum = xe.segNumContainsSource(segText);
		SplitSegment.splitApprovedSeg(xe, segNum);
	}
}
