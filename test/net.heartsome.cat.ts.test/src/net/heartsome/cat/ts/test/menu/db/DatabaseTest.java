package net.heartsome.cat.ts.test.menu.db;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.heartsome.cat.ts.test.basecase.menu.db.DBManagement;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.junit.HSJunit4ClassRunner;
import net.heartsome.test.swtbot.junit.Repeat;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.utilities.common.FileUtil;
import net.heartsome.test.utilities.poi.ExcelUtil;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsRow;
import net.heartsome.test.utilities.sikuli.OsUtil;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HSJunit4ClassRunner.class)
public class DatabaseTest {
	private static HsSheet shConnectServerData;
	private static HsSheet shCreateDBData;
	private static HsSheet shDeleteDBData;
	private int connectServerNum = 1;
	private int createDBNum = 1;
	private int deleteDBNum = 1;
	private int deleteConnectionNum = 1;

	@BeforeClass
	public static void start() throws FileNotFoundException, IOException {
		String filePrjData = FileUtil.getExecutionPath() + "/testData/DatabaseTest.xls";
		shConnectServerData = new ExcelUtil(filePrjData).new HsSheet("Connect");
		shCreateDBData = new ExcelUtil(filePrjData).new HsSheet("Create");
		shDeleteDBData = new ExcelUtil(filePrjData).new HsSheet("Delete");
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

	@Repeat(23)
	@Test
	public void tc1ConnectServer() {
		HsRow row = shConnectServerData.new HsRow(connectServerNum);
		DBManagement db = new DBManagement(row);
		db.connectServer(TsUIConstants.Entry.SHORTCUT);
		connectServerNum++;
	}

	@Repeat(25)
	@Test
	public void tc2CreateDB() {
		HsRow row = shCreateDBData.new HsRow(createDBNum);
		DBManagement db = new DBManagement(row);
		db.createDB(TsUIConstants.Entry.MENU);
		createDBNum++;
	}

	@Repeat(15)
	@Test
	public void tc3DeleteDB() {
		HsRow row = shDeleteDBData.new HsRow(deleteDBNum);
		DBManagement db = new DBManagement(row);
		db.deleteDB(TsUIConstants.Entry.MENU);
		deleteDBNum++;
	}

	@Repeat(5)
	@Test
	public void tc4DeleteConnection() {
		HsRow row = shConnectServerData.new HsRow(deleteConnectionNum);
		DBManagement db = new DBManagement(row);
		db.deleteConnection(TsUIConstants.Entry.SHORTCUT);
		deleteConnectionNum++;
	}
}
