package net.heartsome.cat.ts.test.basecase.menu.file;

import static org.junit.Assert.assertTrue;
import net.heartsome.cat.ts.test.basecase.common.ExcelData;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.Entry;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants.ResourceType;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.utils.TreeItemUtil;
import net.heartsome.test.swtbot.waits.IsFileOpenedInEditor;
import net.heartsome.test.utilities.poi.ExcelUtil.HsSheet.HsRow;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 对项目文件夹、文件的一些操作，主要基于项目导航树。
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class ProjectFile {

	private HsRow row;
	private String prjName;
	private String fileType;
	private String filePath;
	private String fileName;
	// private int expResult;
	private ExcelData data;

	private ProjectTreeView view;
	private SWTBotTree tree;
	private SWTBotTreeItem treeItem;
	private ResourceType selectType;

	/**
	 * @param row
	 *            存放测试数据的 Excel 行
	 */
	public ProjectFile(HsRow row) {
		this.row = row;
		view = ProjectTreeView.getInstance();
		tree = ProjectTreeView.getTree();
		data = new ExcelData(row);
	}

	/**
	 * @param from
	 *            打开文件的入口：右键菜单、双击，请使用 TS 类提供的常量;
	 */
	public void openFile(Entry from) {
		assertTrue("参数错误，Excel 数据行 row 为 null。", row != null);
		getDataFile();
		SWTBotTreeItem item = select();
		assertTrue("如下选择类型不是文件：" + selectType, selectType == TsUIConstants.ResourceType.FILE);

		switch (from) {
		case DOUBLE_CLICK: {
			item.doubleClick();
			break;
		}
		case CONTEXT_MENU: {
			view.ctxMenuOpenFile().click();
			break;
		}
		default: {
			assertTrue("参数错误，无此入口：" + from, false);
		}
		}
		HSBot.bot().waitUntil(new IsFileOpenedInEditor(fileName));
	}

	// TODO

	/**
	 * @return 选中的项目、文件类型（文件夹）、子文件夹或文件所在的项目导航树节点;
	 */
	public SWTBotTreeItem select() {
		if (prjName == null) {
			getDataFile();
		}
		if (fileType == null || fileType.equals("")) { // 文件类型为空，说明只选项目
			treeItem = tree.expandNode(prjName).select();
			selectType = TsUIConstants.ResourceType.PROJECT;
		} else {
			if (filePath != null && !filePath.equals("")) { // 在指定的类型下还有子文件夹
				String[] paths = filePath.split("/");
				if (fileName != null && !fileName.equals("")) { // 文件名不为空，则选择文件
					treeItem = tree.expandNode(prjName).expandNode(fileType).expandNode(paths).expandNode(fileName)
							.select();
					selectType = TsUIConstants.ResourceType.FILE;
				} else { // 文件名为空，即只选指定文件类型文件夹下的子文件夹
					treeItem = tree.expandNode(prjName).expandNode(fileType).expandNode(paths).select();
					selectType = TsUIConstants.ResourceType.FOLDER;
				}
			} else { // 直接在在指定的类型下，没有子文件夹
				if (fileName != null && !fileName.equals("")) { // 文件名不为空，则选择文件
					treeItem = tree.expandNode(prjName).expandNode(fileType).expandNode(fileName).select();
					selectType = TsUIConstants.ResourceType.FILE;
				} else { // 文件名为空，即选择该文件类型文件夹
					treeItem = tree.expandNode(prjName).expandNode(fileType).select();
					selectType = TsUIConstants.ResourceType.FOLDER;
				}
			}
		}
		return treeItem;
	}

	/**
	 * @return 取得当前文件在工作空间中的路径;
	 */
	public String getPath() {
		if (treeItem == null) {
			select();
		}
		return TreeItemUtil.getPath(treeItem);
	}

	/**
	 * 从 Excel 文件中读取打开文件所需的测试数据;
	 */
	public void getDataFile() {
		assertTrue("参数错误，row 为 null。", row != null);
		prjName = data.getTextOrEmpty(ExcelData.colPrjName);
		fileType = data.getTextOrEmpty(ExcelData.colFileType);
		filePath = data.getTextOrEmpty(ExcelData.colFilePath);
		fileName = data.getTextOrEmpty(ExcelData.colFileName);
		// expResult = data.getExpectedResult();
	}

}
