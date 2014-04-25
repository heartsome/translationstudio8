package net.heartsome.cat.ts.test.ui.msgdialogs;

import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.utilities.common.FileUtil;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * 确认对话框：删除项目
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class ConfirmProjectDeleteDialog extends SWTBotShell {

	private SWTBot dialogBot = this.bot();
	private String prjName;

//	/**
//	 * 按标题查找对话框
//	 */
//	public ConfirmProjectDeleteDialog() {
//		// super(HSBot.bot().shell(TsUIConstants.getString("dlgTitleConfirmProjectDelete")).widget);
//	}

	/**
	 * @param prjName
	 *            项目名称
	 */
	public ConfirmProjectDeleteDialog(String prjName) {
		super(HSBot.bot().shells()[1].widget); // 由于在 MAC OS 中该对话框没有独立的标题栏，所以无法通过对话框标题来确定。
		this.prjName = prjName;
		assertTrue(msgConfirmDeleteProject().isVisible());
	}

	/**
	 * @return 按钮：否;
	 */
	public SWTBotButton btnNo() {
		return dialogBot.button(TsUIConstants.getString("btnNo"));
	}

	/**
	 * @return 按钮：是;
	 */
	public SWTBotButton btnYes() {
		return dialogBot.button(TsUIConstants.getString("btnYes"));
	}

	// /**
	// * @return 按钮：预览 未展开;
	// */
	// public SWTBotButton btnPreviewCollapsed() {
	// return dialogBot.button(TsUIConstants.getString("btnPreviewCollapsed"));
	// }
	//
	// /**
	// * @return 按钮：预览 展开;
	// */
	// public SWTBotButton btnPreviewExpanded() {
	// return dialogBot.button(TsUIConstants.getString("btnPreviewExpanded"));
	// }
	//
	// /**
	// * @return 复选框：删除磁盘内容;
	// */
	// public SWTBotCheckBox chkbxDeleteProjectContentsOnDisk() {
	// return dialogBot.checkBox(TsUIConstants.getString("chkbxDeleteProjectContentsOnDisk"));
	// }

	/**
	 * @return 文字标签：确认是否删除项目;
	 */
	public SWTBotLabel msgConfirmDeleteProject() {
		assertTrue("参数错误，项目名称为 null。", prjName != null);
		return dialogBot.label(MessageFormat.format(TsUIConstants.getString("msgConfirmDeleteProject"), prjName));
	}

	/**
	 * @return 单选按钮：同时删除目录下的内容;
	 */
	public SWTBotRadio radBtnAlsoDeleteContentsUnder() {
		assertTrue("参数错误，项目名称为 null。", prjName != null);
		String path = FileUtil.joinPath(FileUtil.getWorkspacePath(), prjName);
		String msg = TsUIConstants.getString("radBtnAlsoDeleteContentsUnder");
		return dialogBot.radio(MessageFormat.format(msg, path));
	}

	/**
	 * @return 单选按钮：不删除内容;
	 */
	public SWTBotRadio radBtnDoNotDeleteContents() {
		return dialogBot.radio(TsUIConstants.getString("radBtnDoNotDeleteContents"));
	}
}
