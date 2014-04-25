package net.heartsome.cat.ts.test.demos;

import net.heartsome.cat.ts.test.ui.dialogs.OpenFileDialog;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;
import net.heartsome.test.swtbot.junit.HSJunit4ClassRunner;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HSJunit4ClassRunner.class)
public class FileOperationTest {
	SWTWorkbenchBot bot;
	TS ts;
	String prjName;
	String prjFileName;
	String extFilePath;

	@Before
	public void setUp() {
		bot = HSBot.bot();
		ts = TS.getInstance();
		prjName = "prjBot-001";
		prjFileName = "HSCAT8-23.xlf";
		extFilePath = "D:\\Temp\\Sample_1.xlf";
		bot.closeAllEditors();
	}
	
	@After
	public void tearDown() {
		bot.sleep(500);
	}

	@Test
	public void OpenExternalFile() {
		ts.menuFileOpenFile().click();
		OpenFileDialog.openFile(extFilePath);
	}
	
	@Test
	public void OpenWorkspaceFile() {
		ProjectTreeView.doubleClickXlfFile(prjName, prjFileName);
	}
}
