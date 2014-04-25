package net.heartsome.cat.ts.test.menu.file;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.heartsome.cat.ts.test.basecase.menu.file.ProjectCreate;
import net.heartsome.cat.ts.test.basecase.menu.file.ProjectDelete;
import net.heartsome.cat.ts.test.basecase.menu.file.ProjectRename;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;
import net.heartsome.test.swtbot.junit.HSJunit4ClassRunner;
import net.heartsome.test.swtbot.junit.Repeat;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.utilities.common.FileUtil;
import net.heartsome.test.utilities.poi.ExcelUtil;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet;
import net.heartsome.test.utilities.sikuli.OsUtil;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HSJunit4ClassRunner.class)
public class ProjectOperationTest {
	private static HsSheet shCreatePrjData;
	private static HsSheet shRenamePrjData;
	private static HsSheet shDeletePrjData;
	private int createPrjNum = 1;
	private int renamePrjNum = 1;
	private int deletePrjNum = 1;

	@BeforeClass
	public static void start() throws FileNotFoundException, IOException {
		String filePrjData = FileUtil.getExecutionPath() + "/testData/ProjectOperationTest.xls";
		shCreatePrjData = new ExcelUtil(filePrjData).new HsSheet("Create");
		shRenamePrjData = new ExcelUtil(filePrjData).new HsSheet("Rename");
		shDeletePrjData = new ExcelUtil(filePrjData).new HsSheet("Delete");
	}

	@AfterClass
	public static void end() {
	}

	@Before
	public void setUp() {
//		ProjectTreeView.getInstance().ctxMenuRefresh().click();
	}

	@After
	public void tearDown() {
		OsUtil.typeEsc();
		OsUtil.typeEsc();
		HSBot.bot().sleep(500);
	}

	@Repeat(3)
	@Test
	public void tc1DeleteProject() {
		ProjectCreate prj1 = new ProjectCreate(shCreatePrjData.new HsRow(deletePrjNum));
		prj1.createPrj(TsUIConstants.Entry.MENU);
		ProjectDelete prj2 = new ProjectDelete(shDeletePrjData.new HsRow(deletePrjNum));
		prj2.deletePrj(TsUIConstants.Entry.MENU);
		deletePrjNum++;
	}

	// @Ignore
	@Repeat(5)
	@Test
	public void tc2CreateProject() {
		ProjectCreate prj = new ProjectCreate(shCreatePrjData.new HsRow(createPrjNum));
		prj.createPrj(TsUIConstants.Entry.MENU);
		createPrjNum++;
	}

	// @Ignore
	@Repeat(5)
	@Test
	public void tc3RenameProject() {
		ProjectRename prj = new ProjectRename(shRenamePrjData.new HsRow(renamePrjNum));
		prj.renamePrj(TsUIConstants.Entry.SHORTCUT);
		renamePrjNum++;
	}
}
