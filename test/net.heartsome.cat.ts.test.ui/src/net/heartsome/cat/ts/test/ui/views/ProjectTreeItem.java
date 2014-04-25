package net.heartsome.cat.ts.test.ui.views;

import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.waits.IsEditorOpened;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 树节点：指定项目在树上的节点，单例模式
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public final class ProjectTreeItem extends SWTBotTreeItem {

	private String prjName;
	private ProjectTreeView ptv = ProjectTreeView.getInstance();
	private static ProjectTreeItem ptn;

	/**
	 * @param prjName
	 */
	private ProjectTreeItem(String prjName) {
		super(ProjectTreeView.getTree().expandNode(prjName).select().widget);
		this.prjName = prjName;
	}

	/**
	 * 获得指定名称的项目所在节点
	 * @param prjName
	 *            指定的项目名称
	 * @return TSProjectTreeNode 项目所在的树节点
	 */
	public static ProjectTreeItem getInstance(String prjName) {
		if (ptn == null) {
			ptn = new ProjectTreeItem(prjName);
		}
		return ptn;
	}

	/**
	 * 选择指定项目中指定类型的文件
	 * @param fileType
	 *            指定的类型，即项目的第一级子目录，默认目录有：Source, Target, XLIFF, SKL, TMX, TBX, Other
	 * @param fileName
	 *            指定的文件名称
	 * @return SWTBotTreeItem 指定的文件所在的树节点
	 */
	public SWTBotTreeItem selectFile(String fileType, String fileName) {
		return this.expandNode(fileType).select(fileName);
	}

	/**
	 * 合并打开当前项目中的所有 XLIFF
	 */
	public void ctxMenuOpenProjectFiles() {
		ptn.select();
		SWTBotMenu openProjectFiles = ptv.ctxMenuOpenProjectFiles();
		openProjectFiles.isEnabled(); // 确认右键菜单中的打开项目功能可用
		openProjectFiles.click(); // 点击该菜单项
		// 确认文件被成功打开
		SWTBotEditor editor = HSBot.bot().editorByTitle(prjName);
		HSBot.bot().waitUntil(new IsEditorOpened(editor));
	}

	/**
	 * 打开当前项目中的一个 XLIFF 文件
	 * @param xlfFileName
	 *            要打开的 XLIFF 文件名称
	 */
	public void ctxMenuOpenFile(final String xlfFileName) {
		selectFile("XLIFF", xlfFileName);
		SWTBotMenu openFiles = ptv.ctxMenuOpenFile();
		openFiles.isEnabled();
		openFiles.click();

		SWTBotEditor editor = HSBot.bot().editorByTitle(xlfFileName);
		HSBot.bot().waitUntil(new IsEditorOpened(editor));
	}

	/**
	 * 转换当前项目中的一个源文件为 XLIFF
	 * @param srcFileName
	 *            要转换的源文件名称
	 */
	public void ctxMenuConvertFile(String srcFileName) {
		selectFile("Source", srcFileName);
		SWTBotMenu convertFiles = ptv.ctxMenuConvertSrcFile2Xliff();
		convertFiles.isEnabled();
		convertFiles.click();
		// TODO：确认转换对话框正确打开
	}

	/**
	 * 转换当前项目中的一个 XLIFF 为源格式
	 * @param xlfFileName
	 *            要转换为源格式的 XLIFF 文件名称
	 */
	public void ctxMenuReverseConvertFile(String xlfFileName) {
		selectFile("XLIFF", xlfFileName);
		SWTBotMenu reverseConvertFile = ptv.ctxMenuConvertXliffFile2Tgt();
		reverseConvertFile.isEnabled();
		reverseConvertFile.click();
		// TODO：确认转换对话框正确打开
	}

}
