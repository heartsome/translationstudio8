package net.heartsome.cat.ts.test.menu.db;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.heartsome.cat.ts.test.basecase.menu.db.DBManagement;
import net.heartsome.cat.ts.test.basecase.menu.db.ImportToDB;
import net.heartsome.cat.ts.test.basecase.menu.db.MemoryDBManagement;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.junit.HSJunit4ClassRunner;
import net.heartsome.test.swtbot.junit.Repeat;
import net.heartsome.test.utilities.common.FileUtil;
import net.heartsome.test.utilities.poi.ExcelUtil;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsRow;

import org.eclipse.swtbot.mockdialogs.factory.NativeDialogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HSJunit4ClassRunner.class)
public class ImportToDBTest {
	private static HsSheet shImportTMXData;
	private static HsSheet shImportTBXData;
	private int importTMXNum = 1;
	private int importTBXNum = 1;

	@BeforeClass
	public static void start() throws FileNotFoundException, IOException {

		NativeDialogFactory.setMode(NativeDialogFactory.OperationMode.TESTING);

		String filePrjData = FileUtil.getExecutionPath() + "/testData/ImportToDBTestSZ.xls";
		shImportTMXData = new ExcelUtil(filePrjData).new HsSheet("ImportTMX");
		shImportTBXData = new ExcelUtil(filePrjData).new HsSheet("ImportTBX");
		// 创建导入所需的数据库
//		HsRow dbRow = shImportTMXData.new HsRow(1);
//		MemoryDBManagement db = new MemoryDBManagement(dbRow);
//		db.createDB(TsUIConstants.Entry.MENU);
	}

	@AfterClass
	public static void end() {
		// 删除用完的数据库
		HsRow dbRow = shImportTMXData.new HsRow(1);
		MemoryDBManagement db = new MemoryDBManagement(dbRow);
		db.deleteDB(TsUIConstants.Entry.MENU, true);
	}

	@Before
	public void setUp() {
		// TsTasks.closeDialogs();
		// TsTasks.closeDialogs();
	}

	@After
	public void tearDown() {
		// OsUtil.typeEsc();
		// OsUtil.typeEsc();
		// HSBot.bot().sleep(500);
	}

//	@Repeat(10)
//	@Test
//	public void tc1ImportTMX() {
//		HsRow row = shImportTMXData.new HsRow(importTMXNum);
//		ImportToDB tmx = new ImportToDB(TsUIConstants.ImportType.TMX, row);
//		tmx.importTMX(TsUIConstants.Entry.MENU);
//		importTMXNum++;
//	}

	@Repeat(10)
	@Test
	public void tc2ImportTBX() {
		HsRow row = shImportTBXData.new HsRow(importTBXNum);
		ImportToDB tbx = new ImportToDB(TsUIConstants.ImportType.TBX, row);
		tbx.importTBX(TsUIConstants.Entry.MENU);
		importTBXNum++;
	}

}
