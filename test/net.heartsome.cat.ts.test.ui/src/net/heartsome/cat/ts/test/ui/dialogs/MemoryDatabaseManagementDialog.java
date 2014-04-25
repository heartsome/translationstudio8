package net.heartsome.cat.ts.test.ui.dialogs;

import static org.junit.Assert.assertTrue;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.widgets.HsSWTBotShell;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 库管理对话框
 * @author roy_xiao
 */
public class MemoryDatabaseManagementDialog extends HsSWTBotShell {

	/** 对话框模式：管理模式. */
	public static final int MANAGEMENT = 0;
	/** 对话框模式：选择模式. */
	public static final int SELECT = 0;

	private SWTBot dialogBot = this.bot();
	private int mode;

	/**
	 * 按标题识别对话框
	 * @param mode
	 *            对话框模式，请使用本类提供的常量
	 */
	public MemoryDatabaseManagementDialog(int mode,String title) {
		super(HSBot.bot().shell(TsUIConstants.getString(title)).widget);
		this.mode = mode;
	}

	/**
	 * @return 文本框：服务器地址;
	 */
	public SWTBotText txtWLblServer() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblServer"));
	}

	/**
	 * @return 文本框：服务器端口;
	 */
	public SWTBotText txtWLblPort() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblPort"));
	}

	/**
	 * @return 文本框：服务器实例名称;
	 */
	public SWTBotText txtWLblInstance() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblInstance"));
	}

	/**
	 * @return 文本框：数据库保存路径;
	 */
	public SWTBotText txtWLblPath() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblPath"));
	}

	/**
	 * @return 按钮：浏览;
	 */
	public SWTBotButton btnBrowse() {
		return dialogBot.button(TsUIConstants.getString("btnBrowse"));
	}

	/**
	 * @return 文本框：数据库用户名;
	 */
	public SWTBotText txtWLblUsername() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblUsername"));
	}

	/**
	 * @return 文本框：数据库密码;
	 */
	public SWTBotText txtWLblPassword() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblPassword"));
	}

//	/**
//	 * @return 复选框：记住信息;
//	 */
//	public SWTBotCheckBox chkbxRemeber() {
//		return dialogBot.checkBox(TsUIConstants.getString("chkbxRemeber"));
//	}

	
	
	
	/**
	 * @return 按钮：查询;
	 */
	public SWTBotButton btnSaveConn() {
		return dialogBot.button(TsUIConstants.getString("btnSaveS"));
	}
	
	/**
	 * @return 按钮：查询;
	 */
	public SWTBotButton btnSearch() {
		return dialogBot.button(TsUIConstants.getString("btnSearchS"));
	}

	/**
	 * @return 文本：表格列头 编号;
	 */
	public String tblColNum() {
		return TsUIConstants.getString("tblColNum");
	}

	/**
	 * @return 文本：表格列头 数据库名称;
	 */
	public String tblColDatabaseName() {
		return TsUIConstants.getString("tblColDatabaseName");
	}

	/**
	 * @return 文本：表格列头 语言;
	 */
	public String tblColLanguage() {
		return TsUIConstants.getString("tblColLanguage");
	}

	/**
	 * @return 按钮：创建，仅在管理模式下可用;
	 */
	public SWTBotButton btnCreate() {
		assertTrue("参数错误，此功能仅在管理模式下可以使用。", mode == MANAGEMENT);
		return dialogBot.button(TsUIConstants.getString("btnCreateN"));
	}
	
	/**
	 * @return 按钮：添加，仅在管理模式下可用;
	 */
	public SWTBotButton btnAdd() {
		assertTrue("参数错误，此功能仅在管理模式下可以使用。", mode == MANAGEMENT);
		return dialogBot.button(TsUIConstants.getString("btnAddA"));
	}

	/**
	 * @return 按钮：删除，仅在管理模式下可用;
	 */
	public SWTBotButton btnDelete() {
		assertTrue("参数错误，此功能仅在管理模式下可以使用。", mode == MANAGEMENT);
		return dialogBot.button(TsUIConstants.getString("btnDeleteD"));
	}

	/**
	 * @return 按钮：确定，仅在选择模式下可用;
	 */
	public SWTBotButton btnOK() {
		assertTrue("参数错误，此功能仅在选择模式下可以使用。", mode == SELECT);
		return dialogBot.button(TsUIConstants.getString("btnOK"));
	}

	/**
	 * @return 按钮：关闭;
	 */
	public SWTBotButton btnClose() {
		return dialogBot.button(TsUIConstants.getString("btnCancel"));
	}
	
	/**
	 * @return 按钮：创建库;
	 */
	public SWTBotButton btnCreateDb() {
		return dialogBot.button(TsUIConstants.getString("btnCreateDb"));
	}
	
	/**
	 * @return 按钮：删除库;
	 */
	public SWTBotButton btnDeleteDb() {
		return dialogBot.button(TsUIConstants.getString("btnDeleteDb"));
	}
	
	

	/**
	 * @return 按钮：移除连接
	 */
	public SWTBotButton btnRemoveConn() {
		return dialogBot.button(TsUIConstants.getString("btnRemoveR"));
	}
	
	/**
	 * @return 树节点：PostgreSQL;
	 */
	public SWTBotTreeItem treiDBPostgreSQL() {
		return dialogBot.tree().expandNode(TsUIConstants.getString("dbPostgreSQL"));
	}

	/**
	 * @return 树节点：MySQL;
	 */
	public SWTBotTreeItem treiDBMySQL() {
		return dialogBot.tree().expandNode(TsUIConstants.getString("dbMySQL"));
	}

	/**
	 * @return 树节点：Oracle;
	 */
	public SWTBotTreeItem treiDBOracle() {
		return dialogBot.tree().expandNode(TsUIConstants.getString("dbOracle"));
	}

	/**
	 * @return 树节点：InternalDB;
	 */
	public SWTBotTreeItem treiDBInternal() {
		return dialogBot.tree().expandNode(TsUIConstants.getString("dbInternal"));
	}

	/**
	 * @return 树节点：DB_MSSQL;
	 */
	public SWTBotTreeItem treiDBMSSQL() {
		return dialogBot.tree().expandNode(TsUIConstants.getString("dbMSSQL"));
	}

	/**
	 * @return 右键菜单：删除;
	 */
	public SWTBotMenu ctxMenuDelete() {
		return dialogBot.tree().contextMenu(TsUIConstants.getString("ctxMenuDelete"));
	}
}
