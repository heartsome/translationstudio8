package net.heartsome.cat.ts.test.basecase.menu.db;

import static org.junit.Assert.assertTrue;
import net.heartsome.cat.ts.test.basecase.common.ExcelData;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.DB;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.Entry;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.ExpectedResult;
import net.heartsome.cat.ts.test.ui.dialogs.CreateDatabaseDialog;
import net.heartsome.cat.ts.test.ui.dialogs.CreateMemoryDbDialog;
import net.heartsome.cat.ts.test.ui.dialogs.MemoryDatabaseManagementDialog;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.msgdialogs.ConfirmDialog;
import net.heartsome.cat.ts.test.ui.msgdialogs.InformationDialog;
import net.heartsome.cat.ts.test.ui.tasks.Waits;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.utils.SWTBotUtils;
import net.heartsome.test.swtbot.widgets.HsSWTBotTable;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsRow;
import net.heartsome.test.utilities.sikuli.OsUtil;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 数据库的相关操作
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
/**
 * @author Roy Xiao
 * @version
 * @since JDK1.6
 */
/**
 * @author roy
 *
 */
public class MemoryDBManagement {

	private final TS ts = TS.getInstance();
	private MemoryDatabaseManagementDialog dialog;
	private DB dbType;
	private String server;
	private String port;
	private String instance;
	private String path;
	private String username;
	private String password;
	private boolean saveConn = true;
	private String dbName;
	private HsRow rowDBData;
	private ExcelData data;
	private String connectionName;
	private ExpectedResult expectedConnect = null;
	private ExpectedResult expectedCreate = null;
	// private boolean confirm = false;
	private HsSWTBotTable table;
	private int mode;
	private boolean isFromImportDb = false;
	
	/**
	 * true表示是记忆库， false表示是术语库
	 */
	private boolean isMemory = true;

	

	/**
	 * @param isMemory the isMemory to set
	 */
	public void setMemory(boolean isMemory) {
		this.isMemory = isMemory;
	}

	/**
	 * @param row
	 *            需要读取数据的 Excel 行。
	 */
	public MemoryDBManagement(HsRow row) {
		rowDBData = row;
		data = new ExcelData(row);
	}

	/**
	 * 完整流程：根据从 Excel 中读取的数据去连接数据库服务器;
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类的枚举;
	 */
	public void connectServer(Entry from) {
		mode = MemoryDatabaseManagementDialog.MANAGEMENT;
		openDBMgmgDialog(from);
		getDataConnect(mode);
		getDBListFromServer(false);
		dialog.btnClose().click();
		// TODO 可增加对数据库数据的相关验证
	}
	
	/**
	 * 完整流程：根据从 Excel 中读取的数据去创建保存数据库服务器;
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类的枚举;
	 */
	public void saveServer(Entry from) {
		mode = MemoryDatabaseManagementDialog.MANAGEMENT;
		openDBMgmgDialog(from);
		getDataConnect(mode);
		saveServer();
		dialog.btnClose().click();
		// TODO 可增加对数据库数据的相关验证
	}

	/**
	 * 完整步骤：根据从 Excel 中读取的数据删除已保存的连接信息
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类的枚举;
	 */
	public void deleteConnection(Entry from) {
		mode = MemoryDatabaseManagementDialog.MANAGEMENT;
		openDBMgmgDialog(from);
		getDataConnect(mode);
		assertTrue("未在已保存的连接信息中找到该连接：" + connectionName, isConnectionSaved());
		
		SWTBotTreeItem curDb = selectDBType();
		int countBefore = curDb.getItems().length;
		curDb.expandNode(connectionName).select();
		dialog.btnRemoveConn().click();
		
		int countRemove = curDb.getItems().length;
		assertTrue("仍可在已保存的连接信息中找到该连接：" + connectionName, countBefore == countRemove+1 );
		dialog.btnClose().click();
	}

	/**
	 * 完整流程：根据从 Excel 中读取的数据创建数据库;
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类的枚举;
	 */
	public void createDB(Entry from) {
		mode = MemoryDatabaseManagementDialog.MANAGEMENT;
		openDBMgmgDialog(from);
		getDataCreateDB();
		
		selectDBType().expandNode(connectionName).select();
		dialog.btnSearch().click();
//		getDBListFromServer(true);
		table = dialog.bot().table();
		
		
		if (expectedCreate.equals(TsUIConstants.ExpectedResult.SUCCESS) && isExist(dbName)) { // 若预期结果为创建成功、而该数据库目前已存在，则先删除
			table.unselect();
			table.select(table.rowIndexOfColumn(dbName, TsUIConstants.getString("tblColDatabaseName")));
			dialog.btnDeleteDb().click();
			ConfirmDialog dlgConfirm = new ConfirmDialog(ConfirmDialog.dlgTitleTips,
					ConfirmDialog.msgDeleteDatabaseFromServer);
			dlgConfirm.btnOK().click();
			Waits.shellClosed(dlgConfirm);
		}
		inputDBName();
		if (expectedCreate.equals(TsUIConstants.ExpectedResult.SUCCESS)) {
			assertTrue("列表中没有该数据库：" + dbName, isExist(dbName));
		}
		dialog.btnClose().click();
	}

	/**
	 * 完整流程：根据从 Excel 中读取的数据删除数据库;
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类的枚举;
	 * @param confirm
	 *            是否确认删除
	 */
	public void deleteDB(Entry from, boolean confirm) {
		mode = MemoryDatabaseManagementDialog.MANAGEMENT;
		openDBMgmgDialog(from);
		getDataDeleteDB();
//		getDBListFromServer(true);
		
		selectDBType().expandNode(connectionName).select();
		dialog.btnSearch().click();
		table = dialog.bot().table();
		
		assertTrue("列表中没有该数据库：" + dbName, isExist(dbName));
		table.unselect();
		table.select(table.rowIndexOfColumn(dbName, TsUIConstants.getString("tblColDatabaseName")));
		dialog.btnDeleteDb().click();
		ConfirmDialog dlgConfirm = new ConfirmDialog(ConfirmDialog.dlgTitleTips,
				ConfirmDialog.msgDeleteDatabaseFromServer);
		if (confirm) {
			dlgConfirm.btnOK().click();
			Waits.shellClosed(dlgConfirm);
			assertTrue("数据库仍在列表中：" + dbName, !isExist(dbName));
		} else {
			dlgConfirm.btnCancel().click();
			Waits.shellClosed(dlgConfirm);
			assertTrue("列表中没有该数据库：" + dbName, isExist(dbName));
		}
		dialog.btnClose().click();
	}

	/**
	 * 完整流程：根据从 Excel 中读取的数据删除数据库;
	 * @param from
	 *            功能入口，请使用 TSUIConstants 类的枚举;
	 */
	public void deleteDB(Entry from) {
		deleteDB(from, data.getBoolean(ExcelData.colConfirm));
	}

	/**
	 * 完整流程：选择库;
	 * @param dbName
	 *            要选择的数据库名称，其他信息直接从 Excel 中读取
	 */
	public void selectDB(String dbName) {
		mode = MemoryDatabaseManagementDialog.SELECT;
		getDataConnect(mode);
		dialog = new MemoryDatabaseManagementDialog(MemoryDatabaseManagementDialog.SELECT,isMemory?"dlgTitleMemoryManagement":"dlgTitletreiTbManagement");
		dialog.activate();
		getDBListFromServer(false);
		assertTrue("数据库名称：" + dbName + "，实际内容：" + table.cell(0, 1), isExist(dbName));
		table.unselect();
		table.select(table.rowIndexOfColumn(dbName, TsUIConstants.getString("tblColDatabaseName")));
		dialog.btnOK().click();
		Waits.shellClosed(dialog);
	}
	
	

	// 以下为非完整步骤

	/**
	 * @param from
	 *            入口，请使用 TSUIConstants 类中提供的枚举。
	 * @return 打开的库管理对话框;
	 */
	public MemoryDatabaseManagementDialog openDBMgmgDialog(Entry from) {
		if (from.equals(TsUIConstants.Entry.MENU)) {
			if (isMemory){
				ts.menuDBManagement().click();
			} else {
				ts.menuTeriDBManagement().click();
			}
		} else if (from.equals(TsUIConstants.Entry.SHORTCUT)) {
			try {
				ts.pressShortcut(SWTBotUtils.getCtrlOrCmdKey(), Keystrokes.SHIFT, KeyStroke.getInstance("D"));
			} catch (ParseException e) {
				e.printStackTrace();
				assertTrue("快捷键解析错误。", false);
			}
		} else {
			assertTrue("参数错误，该功能无此入口：" + from, false);
		}
		dialog = new MemoryDatabaseManagementDialog(MemoryDatabaseManagementDialog.MANAGEMENT,isMemory?"dlgTitleMemoryManagement":"dlgTitletreiTbManagement");
		return dialog;
	}

	/**
	 * 在库管理对话框左边的树上选择并展开指定类型的数据库;
	 * @return 指定数据库类型的树节点;
	 */
	public SWTBotTreeItem selectDBType() {
		
		return selectDBType(dbType);
	}

	/**
	 * 在库管理对话框左边的树上选择并展开指定类型的数据库
	 * @param dbType
	 *            数据库类型;
	 * @return 指定数据库类型的树节点;
	 */
	public SWTBotTreeItem selectDBType(DB dbType) {
		SWTBotTreeItem treei = null;
		assertTrue("库管理对话框为 null。", dialog != null);

		switch (dbType) {

		case INTERNAL: {
			treei = dialog.treiDBInternal().expand().select();
			verifyInternalDBWidgets();
			break;
		}

		case MYSQL: {
			treei = dialog.treiDBMySQL().expand().select();
			verifyExternalDBWidgets(dbType);
			break;
		}

		case ORACLE: {
			treei = dialog.treiDBOracle().expand().select();
			verifyExternalDBWidgets(dbType);
			break;
		}

		case POSTGRESQL: {
			treei = dialog.treiDBPostgreSQL().expand().select();
			verifyExternalDBWidgets(dbType);
			break;
		}

		case MSSQL: {
			treei = dialog.treiDBMSSQL().expand().select();
			verifyExternalDBWidgets(dbType);
			break;
		}

		default: {
			assertTrue("无此数据库类型" + dbType, false);
		}
		}

		return treei;
	}

	/**
	 * 确认选中内置库后，除路径和浏览按钮外，其他输入控件的状态为不可用;
	 */
	private void verifyInternalDBWidgets() {
		dialog.btnAdd().click();
		assertTrue(dialog.txtWLblPath().isEnabled());
		assertTrue(dialog.btnBrowse().isEnabled());
		assertTrue(!dialog.txtWLblServer().isEnabled());
		assertTrue(!dialog.txtWLblPort().isEnabled());
		assertTrue(!dialog.txtWLblInstance().isEnabled());
		assertTrue(!dialog.txtWLblUsername().isEnabled());
		assertTrue(!dialog.txtWLblPassword().isEnabled());
	}

	/**
	 * 确认选中外部库后，各输入控件的状态与内置库相反，及 Oracle 特有的实例
	 * @param dbType
	 *            数据库类型;
	 */
	private void verifyExternalDBWidgets(DB dbType) {
		dialog.btnAdd().click();
		assertTrue(dialog.txtWLblServer().isEnabled());
		assertTrue(dialog.txtWLblPort().isEnabled());
		assertTrue(dialog.txtWLblUsername().isEnabled());
		assertTrue(dialog.txtWLblPassword().isEnabled());
		assertTrue(!dialog.txtWLblPath().isEnabled());
		assertTrue(!dialog.btnBrowse().isEnabled());
		if (dbType.equals(TsUIConstants.DB.ORACLE)) {
			assertTrue(dialog.txtWLblInstance().isEnabled());
		} else {
			assertTrue(!dialog.txtWLblInstance().isEnabled());
		}
	}

	/**
	 * 往界面中填写服务器连接信息;
	 */
	public void setConnectionInfo() {
		if (dbType.equals(TsUIConstants.DB.INTERNAL)) {
			dialog.txtWLblPath().setText(path);
		} else {
			dialog.txtWLblServer().setText(server);
			dialog.txtWLblPort().setText(port);
			dialog.txtWLblUsername().setText(username);
			dialog.txtWLblPassword().setText(password);
			if (dbType.equals(TsUIConstants.DB.ORACLE)) {
				dialog.txtWLblInstance().setText(instance);
			}
		}
//		if (saveConn) {
//			dialog.chkbxRemeber().select();
//		}
	}

	/**
	 * 判断返回结果，并进行处理，
	 * @param isSave true 表示是保存连接操作，不需要考虑连接错误的； false 表示是查询连接操作。
	 * @param isFromImportDb true 表示是从导入记忆库界面打开的，数据库已经确保是对的，不用检查expectedConnect。
	 * @return true 表示需要保存连接   false 表示不需要保存连接，只对保存连接调用时起作用，查询连接调用此方法无需理会此返回值
	 */
	public boolean checkMsg(boolean isSave){

		if (isFromImportDb){
			return true;
		}
		
		if (expectedConnect.equals(TsUIConstants.ExpectedResult.SUCCESS)) {
			assertTrue(HSBot.bot().activeShell().getText().equals(dialog.getText()));
			return true;
		} else {		
			
		
			InformationDialog msgDlg = null;
			// 如果是保存操作，且返回值是连接错误，此时不用弹出提示框，直接返回true
			if (isSave && expectedConnect == TsUIConstants.ExpectedResult.CONNECTION_ERROR) {
				return true;
			} else {
				assertTrue(HSBot.bot().shells().length == 3);	
			}
			switch (expectedConnect) {

			case SUCCESS: {
				break;
			}

			case NO_SERVER: {
				msgDlg = new InformationDialog(InformationDialog.dlgTitleErrorInfo,
						InformationDialog.msgServerIsRequired);
				break;
			}

			case NO_PORT: {
				msgDlg = new InformationDialog(InformationDialog.dlgTitleErrorInfo, InformationDialog.msgPortIsRequired);
				break;
			}

			case NO_INSTANCE: {
				msgDlg = new InformationDialog(InformationDialog.dlgTitleErrorInfo,
						InformationDialog.msgInstanceIsRequired);
				break;
			}

			case NO_PATH: {
				msgDlg = new InformationDialog(InformationDialog.dlgTitleErrorInfo, InformationDialog.msgPathIsRequired);
				break;
			}

			case NO_USERNAME: {
				msgDlg = new InformationDialog(InformationDialog.dlgTitleErrorInfo,
						InformationDialog.msgUsernameIsRequired);
				break;
			}

			case CONNECTION_ERROR: {
			
				msgDlg = new InformationDialog(InformationDialog.dlgTitleTips,
						InformationDialog.msgServerConnectionError);
				break;
			}

			default: {
					assertTrue("参数错误，无此预期结果：" + expectedConnect, false);
			}
			}

			assertTrue(msgDlg.lblMessage().isVisible());
			msgDlg.btnOK().click();
			Waits.shellClosed(msgDlg);
			
			
			return false;
		}
	
		
	}	
	

	/**
	 * 在数据库服务器上查询数据库列表
	 * @param useSavedConnection
	 *            是否使用已保存的连接;
	 * @return
	 */
	public HsSWTBotTable getDBListFromServer(boolean useSavedConnection) {
		assertTrue("库管理对话框为 null。", dialog != null);
		dialog.activate();
		if (useSavedConnection && isConnectionSaved()) {
			selectDBType().expandNode(connectionName).select();
			dialog.btnAdd().click();
			assertTrue("服务器地址未正确赋值。", server.equals(dialog.txtWLblServer().getText()));
			assertTrue("服务器端口未正确赋值。", port.equals(dialog.txtWLblPort().getText()));
		} else {
			selectDBType();			
			setConnectionInfo();
			dialog.btnSearch().click();
		}

		checkMsg(false);

		table = dialog.bot().table();
		return table;
	}
	

	
	/**
	 * 在数据库服务器上保存链接
	 * @return
	 */
	public void saveServer() {
		assertTrue("库管理对话框为 null。", dialog != null);
		dialog.activate();
		
		int countBefore = selectDBType().getItems().length;
		//点击添加按钮
		dialog.btnAdd().click();
		// 设置连接信息
		setConnectionInfo();
		assertTrue("服务器地址未正确赋值。", server.equals(dialog.txtWLblServer().getText()));
		assertTrue("服务器端口未正确赋值。", port.equals(dialog.txtWLblPort().getText()));
		//点击保存按钮
		dialog.btnSaveConn().click();
		
		int countNew = selectDBType().getItems().length;
		//如果当前数据库类型的子节点已经增加了，则说明保存成功。
		//checkMsg 返回 true 表示参数可以保存，对应的数据库类型节点的子集数量需要增加1， 如果为false，表示缺少必要的参数，不能保存，对应的数据库类型的子集数量应该不变。
		if (checkMsg(true)){
			assertTrue("保存连接错误1>"+dbType+":"+server+":"+port+":"+username,countNew== countBefore+1);
		} else {
			assertTrue("保存连接错误2>"+dbType+":"+server+":"+port+":"+username, countBefore == countNew);
		};
		
		
	}
	

	/**
	 * @return 是否能在该数据库类型下找到名为 指定服务器:端口号 的已保存连接;
	 */
	public boolean isConnectionSaved() {
		SWTBotTreeItem[] items = selectDBType().getItems();
		for (SWTBotTreeItem item : items) {
			if (item.getText().equals(connectionName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return 在当前对话框中验证该数据库是否存在;
	 */
	public boolean isExist(String dbName) {
		return table.containsTextInColumn(dbName, TsUIConstants.getString("tblColDatabaseName"));
	}

	/**
	 * 未打开库管理对话框时，直接验证数据库是否存在
	 * @param dbName
	 *            要验证的数据库名称
	 * @param closeDlg
	 *            是否在获取结果后关闭对话框
	 * @return 若该数据库存在则返回 true，否则为 false;
	 */
	public boolean isDBExist(String dbName, boolean closeDlg) {
		mode = MemoryDatabaseManagementDialog.SELECT;
		openDBMgmgDialog(TsUIConstants.Entry.MENU);
		getDataConnect(mode);
		getDBListFromServer(false);
		boolean result = table.containsTextInColumn(dbName, dialog.tblColDatabaseName());
		if (closeDlg) {
			closeDialog();
		}
		return result;
	}

	/**
	 * 输入数据库名称，含项目名称合法性校验;
	 */
	public void inputDBName() {
		assertTrue(dialog.btnCreateDb().isEnabled());
		dialog.btnCreateDb().click();
		CreateMemoryDbDialog dlgInputDBName = new CreateMemoryDbDialog(isMemory?"dlgTitleCreateMemoryDatabase":"dlgTitleCreatetreiTbDatabase");
		dlgInputDBName.txtWLblDatabaseName().setText(dbName);
		dlgInputDBName.btnOK().click();	
				

		switch (expectedCreate) {

		case INVALID_NAME: {
			assertTrue("未正确显示名称非法信息。", dlgInputDBName.msgDBNameInvalid().isVisible());
			dlgInputDBName.btnCancel().click();
			break;
		}

		case DUPLICATED_NAME: {
			assertTrue("未正确显示重名信息。", dlgInputDBName.msgDBExists().isVisible());
			dlgInputDBName.btnCancel().click();
			break;
		}

		case LONG_NAME: {
			assertTrue("未正确显示名称过长信息。", dlgInputDBName.msgDBNameTooLong().isVisible());
			dlgInputDBName.btnCancel().click();
			break;
		}
		}
		Waits.shellClosed(dlgInputDBName);
	}

	/**
	 * 关闭库管理对话框;
	 */
	public void closeDialog() {
		dialog.btnClose().click();
	}

	// 以下为读取 Excel 文件中的测试数据相关方法

	/**
	 * 从 Excel 文件中读取连接数据库服务器相关数据，并赋值给成员变量;
	 */
	public void getDataConnect(int mode) {
		assertTrue("参数错误：Excel 列为 null。", rowDBData != null);
		dbType = data.getDBType();
		server = data.getTextOrEmpty(ExcelData.colServer);
		port = data.getTextOrEmpty(ExcelData.colPort);
		instance = data.getTextOrEmpty(ExcelData.colInstance);
		path = data.getTextOrEmpty(ExcelData.colDBPath);
		username = data.getTextOrEmpty(ExcelData.colUsername);
		password = data.getTextOrEmpty(ExcelData.colPassword);
		saveConn = data.getBoolean(ExcelData.colSaveConn);
		if (mode == MemoryDatabaseManagementDialog.MANAGEMENT) {
			expectedCreate = data.getExpectedResult();
			if (expectedCreate.equals(TsUIConstants.ExpectedResult.INVALID_NAME)
					|| expectedCreate.equals(TsUIConstants.ExpectedResult.DUPLICATED_NAME)
					|| expectedCreate.equals(TsUIConstants.ExpectedResult.LONG_NAME)) {
				expectedConnect = TsUIConstants.ExpectedResult.SUCCESS; // 创建失败，隐含连接成功
			} else {
				expectedConnect = data.getExpectedResult();
			}
		} else if (mode == MemoryDatabaseManagementDialog.SELECT) {
			dbName = data.getTextOrEmpty(ExcelData.colDBName);
			// assertTrue(dbName != null); 因选择模式可能用直接传进来的数据库名称而非本用例 Excel 中的数据，取消此验证
			expectedConnect = TsUIConstants.ExpectedResult.SUCCESS;
		} else {
			assertTrue("无此模式：" + mode, false);
		}

		// 未取到某些值，且不是测试该为空的情况，自动取默认值
		if (server.equals("") && expectedConnect != TsUIConstants.ExpectedResult.NO_SERVER) {
			if (dbType != TsUIConstants.DB.INTERNAL) {
				server = "localhost";
			}
		}
		if (port.equals("") && expectedConnect != TsUIConstants.ExpectedResult.NO_PORT) {
			if (dbType.equals(TsUIConstants.DB.MYSQL)) {
				port = "3306";
			} else if (dbType.equals(TsUIConstants.DB.ORACLE)) {
				port = "1521";
			} else if (dbType.equals(TsUIConstants.DB.POSTGRESQL)) {
				port = "5432";
			} else if (dbType.equals(TsUIConstants.DB.MSSQL)) {
				port = "1433";
			}
		}
		if (dbType.equals(TsUIConstants.DB.INTERNAL)) {
			connectionName = path;
		} else if (dbType.equals(TsUIConstants.DB.ORACLE)) {
			connectionName = server + ":" + port + "/" + instance;
		} else {
			connectionName = server + ":" + port;
		}
	}

	/**
	 * 从 Excel 文件中读取创建数据库相关数据，并赋值给成员变量;
	 */
	public void getDataCreateDB() {
		getDataConnect(MemoryDatabaseManagementDialog.MANAGEMENT);
		dbName = data.getTextOrEmpty(ExcelData.colDBName);
		assertTrue("数据错误，数据库名称为 null。", dbName != null);
	}

	/**
	 * 从 Excel 文件中读取删除数据库相关数据，并赋值给成员变量;
	 */
	public void getDataDeleteDB() {
		getDataCreateDB();
		// confirm = data.getConfirm();
	}
	
	public void setFromImportDb(boolean isFromImportDb) {
		this.isFromImportDb = isFromImportDb;
	}

}
