package net.heartsome.cat.ts.test.menu.db;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.heartsome.cat.ts.test.basecase.menu.db.MemoryDBManagement;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HSJunit4ClassRunner.class)
public class MemoryDatabaseTest {
	private static HsSheet shConnectServerData;
	private static HsSheet shCreateDBData;
	private static HsSheet shDeleteDBData;
	private int connectServerNum = 1;
	private int createDBNum = 1;
	private int deleteDBNum = 1;
	private int deleteConnectionNum = 1;
	private int saveServerNum = 1;
	

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

//	@Repeat(23)
//	@Ignore
//	@Test
//	public void tc1ConnectServer() {
//		HsRow row = shConnectServerData.new HsRow(connectServerNum);
//		MemoryDBManagement db = new MemoryDBManagement(row);
//		db.connectServer(TsUIConstants.Entry.MENU);
//		connectServerNum++;
//	}
//	
//	@Repeat(5)
//	@Test
//	public void tc12CreateServer() {
//		HsRow row = shConnectServerData.new HsRow(saveServerNum);
//		MemoryDBManagement db = new MemoryDBManagement(row);
//		db.saveServer(TsUIConstants.Entry.MENU);
//		System.out.println(saveServerNum);
//		saveServerNum ++;
//	}
//	
//	
//
//	@Repeat(5)
//	@Test
//	public void tc2CreateDB() {
//		HsRow row = shCreateDBData.new HsRow(createDBNum);
//		MemoryDBManagement db = new MemoryDBManagement(row);
//		db.createDB(TsUIConstants.Entry.MENU);
//		createDBNum++;
//	}
//
//	@Repeat(15)
//	@Ignore
//	@Test
//	public void tc3DeleteDB() {
//		HsRow row = shDeleteDBData.new HsRow(deleteDBNum);
//		MemoryDBManagement db = new MemoryDBManagement(row);
//		db.deleteDB(TsUIConstants.Entry.MENU);
//		deleteDBNum++;
//	}
//
//	@Repeat(5)
//	@Ignore
//	@Test
//	public void tc4DeleteConnection() {
//		HsRow row = shConnectServerData.new HsRow(deleteConnectionNum);
//		MemoryDBManagement db = new MemoryDBManagement(row);
//		db.deleteConnection(TsUIConstants.Entry.MENU);
//		deleteConnectionNum++;
//	}
	
	
	@Repeat(23)
	@Ignore
	@Test
	public void tc5ConnecttreiTbServer() {
		HsRow row = shConnectServerData.new HsRow(connectServerNum);
		MemoryDBManagement db = new MemoryDBManagement(row);
		db.setMemory(false);
		db.connectServer(TsUIConstants.Entry.MENU);
		connectServerNum++;
	}
	
	@Repeat(5)
	@Test
	public void tc6CreatetreiTbServer() {
		HsRow row = shConnectServerData.new HsRow(saveServerNum);
		MemoryDBManagement db = new MemoryDBManagement(row);
		db.setMemory(false);
		db.saveServer(TsUIConstants.Entry.MENU);
		System.out.println(saveServerNum);
		saveServerNum ++;
	}
	
	

	@Repeat(5)
	@Test
	public void tc7CreatetreiTbDB() {
		HsRow row = shCreateDBData.new HsRow(createDBNum);
		MemoryDBManagement db = new MemoryDBManagement(row);
		db.setMemory(false);
		db.createDB(TsUIConstants.Entry.MENU);
		createDBNum++;
	}

	@Repeat(15)
	@Ignore
	@Test
	public void tc8DeletetreiTbDB() {
		HsRow row = shDeleteDBData.new HsRow(deleteDBNum);
		MemoryDBManagement db = new MemoryDBManagement(row);
		db.setMemory(false);
		db.deleteDB(TsUIConstants.Entry.MENU);
		deleteDBNum++;
	}

	@Repeat(5)
	@Ignore
	@Test
	public void tc9DeletetreiTbConnection() {
		HsRow row = shConnectServerData.new HsRow(deleteConnectionNum);
		MemoryDBManagement db = new MemoryDBManagement(row);
		db.setMemory(false);
		db.deleteConnection(TsUIConstants.Entry.MENU);
		deleteConnectionNum++;
	}
	
	
	
	
	
}
