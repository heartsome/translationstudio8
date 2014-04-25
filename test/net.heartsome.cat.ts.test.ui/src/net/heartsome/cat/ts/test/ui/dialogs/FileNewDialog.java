package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 从菜单：文件 > 新建 打开的对话框
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class FileNewDialog extends NewWizardDialog {

	/** 从右键菜单新建其他打开的对话框标题 */
	public static final String NEW = "dlgTitleFileNew";
	/** 从右键菜单新建项目打开的对话框标题 */
	public static final String NEW_TYPE_PROJECT = "dlgTitleNewProject";

	private SWTBot dialogBot = this.bot();

	/**
	 * 根据标题识别对话框
	 */
	public FileNewDialog(String from) {
		super(HSBot.bot().shell(TsUIConstants.getString(from)).widget);
	}

	/**
	 * @return SWTBotTreeItem 新建项目
	 */
	public SWTBotTreeItem treeiProject() {
		return dialogBot.tree().getTreeItem(TsUIConstants.getString("treiCreateProject")).select();
	}

	/**
	 * @return SWTBotTreeItem 新建记忆库/术语库
	 */
	public SWTBotTreeItem treeiUntitledTextFile() {
		return dialogBot.tree().getTreeItem(TsUIConstants.getString("treiCreateTmTbDB")).select();
	}
}
