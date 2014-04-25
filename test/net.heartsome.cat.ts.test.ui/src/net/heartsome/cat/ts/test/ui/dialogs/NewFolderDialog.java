package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 新建文件夹向导对话框
 * @author  felix_lu
 * @version 
 * @since   JDK1.6
 */
public class NewFolderDialog extends NewWizardDialog {

	private SWTBot dialogBot = this.bot();

	/**
	 * 按标题查找对话框
	 */
	public NewFolderDialog() {
		super(HSBot.bot().shell(TsUIConstants.getString("dlgTitleNewFolder")).widget);
	}

	/**
	 * @return SWTBotButton 高级 非展开状态
	 */
	public SWTBotButton btnAdvancedCollapsed() {
		return dialogBot.button(TsUIConstants.getString("btnAdvancedCollapsed"));
	}

	/**
	 * @return SWTBotButton 高级 展开状态
	 */
	public SWTBotButton btnAdvancedExpanded() {
		return dialogBot.button(TsUIConstants.getString("btnAdvancedExpanded"));
	}

	/**
	 * @return 对话框中的项目树
	 */
	private SWTBotTree treeProject() {
		return dialogBot.tree();
	}

	/**
	 * @param prjName
	 *            项目名称
	 * @return SWTBotTreeItem 项目所在的树节点
	 */
	public SWTBotTreeItem treeProject(String prjName) {
		return treeProject().getTreeItem(prjName).select();
	}

	/**
	 * @param prjName
	 *            项目名称
	 * @param type
	 *            文件类型，即一级目录名称
	 * @param fileName
	 *            文件名称
	 * @return SWTBotTreeItem 文件所在的树节点
	 */
	public SWTBotTreeItem treeProject(String prjName, String type, String fileName) {
		return treeProject().expandNode(prjName, type, fileName).select();
	}

	/**
	 * @return SWTBotText 文件夹名文本框
	 */
	public SWTBotText txtWLblFolderName() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblFolderName"));
	}
}
