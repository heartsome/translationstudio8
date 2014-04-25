package net.heartsome.cat.ts.test.ui.views;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.waits.IsEditorOpened;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

/**
 * 视图：项目树，单例模式
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public final class ProjectTreeView extends SWTBotView {

	private static SWTBotTree tree;
	private static ProjectTreeView view;

	/**
	 * 按名称查找视图
	 */
	private ProjectTreeView() {
		super(HSBot.bot().viewByTitle(TsUIConstants.getString("viewTitleProjectTree")).getReference(), HSBot.bot());
	}

	/**
	 * 得到项目树所在的视图
	 * @return TSProjectTreeView 项目树所在的视图
	 */
	public static ProjectTreeView getInstance() {
		if (view == null) {
			view = new ProjectTreeView();
		}
		if (tree == null) {
			tree = view.bot().tree();
		}
		return view;
	}

	/**
	 * 从视图中得到项目树
	 * @return SWTBotTree 项目树
	 */
	public static SWTBotTree getTree() {
		if (tree == null) {
			getInstance();
		}
		return tree;
	}

	/**
	 * @return 右键菜单：新建项目;
	 */
	public SWTBotMenu ctxMenuNewProject() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuNewProject"));
	}

	/**
	 * @return 右键菜单：新建其他;
	 */
	public SWTBotMenu ctxMenuNewOther() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuNewOther"));
	}

	/**
	 * @return 右键菜单：打开项目文件
	 */
	public SWTBotMenu ctxMenuOpenProjectFiles() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuOpenProjectFiles"));
	}

	/**
	 * @return 右键菜单：打开文件
	 */
	public SWTBotMenu ctxMenuOpenFile() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuOpenFile"));
	}

	/**
	 * @return 右键菜单：复制
	 */
	public SWTBotMenu ctxMenuCopy() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuCopy"));
	}

	/**
	 * @return 右键菜单：粘贴
	 */
	public SWTBotMenu ctxMenuPaste() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuPaste"));
	}

	/**
	 * @return 右键菜单：删除
	 */
	public SWTBotMenu ctxMenuDelete() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuDelete"));
	}

	/**
	 * @return 右键菜单：移动
	 */
	public SWTBotMenu ctxMenuMove() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuMove"));
	}

	/**
	 * @return 右键菜单：重命名
	 */
	public SWTBotMenu ctxMenuRename() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuRename"));
	}

	/**
	 * @return 右键菜单：刷新;
	 */
	public SWTBotMenu ctxMenuRefresh() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuRefresh"));
	}

	/**
	 * @return 右键菜单：关闭项目;
	 */
	public SWTBotMenu ctxMenuCloseProject() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuCloseProject"));
	}

	/**
	 * @return 右键菜单：关闭无关项目;
	 */
	public SWTBotMenu ctxMenuCloseUnrelatedProjects() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuCloseUnrelatedProjects"));
	}

	/**
	 * @return 右键菜单：分割 XLIFF 文件;
	 */
	public SWTBotMenu ctxMenuSplitXLIFF() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuSplitXLIFF"));
	}

	/**
	 * @return 右键菜单：合并 XLIFF 文件;
	 */
	public SWTBotMenu ctxMenuMergeXLIFF() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuMergeXLIFF"));
	}

	/**
	 * @return 右键菜单：预翻译;
	 */
	public SWTBotMenu ctxMenuPreTranslate() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuPreTranslate"));
	}

	/**
	 * @return 右键菜单：品质检查;
	 */
	public SWTBotMenu ctxMenuQACheck() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuQACheck"));
	}

	/**
	 * @return 右键菜单：项目设置;
	 */
	public SWTBotMenu ctxMenuProjectSetting() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuProjectSetting"));
	}

	/**
	 * @return 右键菜单：将源文件转换为 XLIFF 文件
	 */
	public SWTBotMenu ctxMenuConvertSrcFile2Xliff() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuConvertSrcFile2Xliff"));
	}

	/**
	 * @return 右键菜单：将 XLIFF 文件转换为目标文件
	 */
	public SWTBotMenu ctxMenuConvertXliffFile2Tgt() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuConvertXliffFile2Tgt"));
	}

	/**
	 * @return 右键菜单：属性;
	 */
	public SWTBotMenu ctxMenuProperties() {
		return tree.contextMenu(TsUIConstants.getString("ctxMenuProperties"));
	}

	/* ****** 鼠标双击功能 ****** */

	/**
	 * 双击打开 XLIFF 文件
	 * @param prjName
	 *            XLIFF 文件所在的项目名称
	 * @param xlfFileName
	 *            要打开的 XLIFF 文件名称
	 */
	public static void doubleClickXlfFile(String prjName, final String xlfFileName) {
		getTree().expandNode(prjName).expandNode("XLIFF").expandNode(xlfFileName).doubleClick();
		// 确认文件被打开
		SWTBotEditor editor = HSBot.bot().editorByTitle(xlfFileName);
		HSBot.bot().waitUntil(new IsEditorOpened(editor));
	}
}
