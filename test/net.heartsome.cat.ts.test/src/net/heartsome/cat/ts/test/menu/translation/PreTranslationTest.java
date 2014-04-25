package net.heartsome.cat.ts.test.menu.translation;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.heartsome.cat.ts.test.ui.dialogs.TS;
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
public class PreTranslationTest {

	private static HsSheet shPreTransData;

	@BeforeClass
	public static void start() throws FileNotFoundException, IOException {
		String filePrjData = FileUtil.getExecutionPath() + "/testData/PreTranslationTest.xls";
		shPreTransData = new ExcelUtil(filePrjData).new HsSheet("Create");
	}

	@AfterClass
	public static void end() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
		OsUtil.typeEsc();
		OsUtil.typeEsc();
		HSBot.bot().sleep(500);
	}

//	@Repeat(3)
	@Test
	public void tc1DeleteProject() {
	}

	// @Ignore
//	@Repeat(5)
	@Test
	public void tc2CreateProject() {
	}

	// @Ignore
//	@Repeat(5)
	@Test
	public void tc3RenameProject() {
	}
}
