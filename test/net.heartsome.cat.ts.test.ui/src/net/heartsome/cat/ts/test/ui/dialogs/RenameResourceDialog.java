package net.heartsome.cat.ts.test.ui.dialogs;

import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

/**
 * 重命名资源对话框
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class RenameResourceDialog extends SWTBotShell {

	private SWTBot dialogBot = this.bot();
	private String newName;
	private String oldName;

	/**
	 * 按钮标题查找对话框
	 */
	public RenameResourceDialog() {
		super(HSBot.bot().shell(TsUIConstants.getString("dlgTitleRenameResource")).widget);
	}

	/**
	 * @param oldName
	 *            旧文件名，仅用于判断是否出现 out of sync 的提示
	 */
	public RenameResourceDialog(String oldName) {
		this();
		this.oldName = oldName;

	}

	/**
	 * @param oldName
	 *            旧文件名，仅用于判断是否出现 out of sync 的提示
	 * @param newName
	 *            新文件名，仅用于输入
	 */
	public RenameResourceDialog(String oldName, String newName) {
		this(oldName);
		this.newName = newName;
	}

	/************************************
	 * 				常用方法
	 ************************************/
	
	/**
	 * 在文本框中填写新名称
	 */
	public void setNewName() {
		assertTrue("参数错误：未设置 newName。", newName != null);
		txtWLblNewName().isActive();
		txtWLblNewName().setText(newName);
	}

	/**
	 * @param newName
	 *            需要在文本框中填写的新名称
	 */
	public void setNewName(String newName) {
		this.newName = newName;
		setNewName();
	}

	/**
	 * @return 是表示出现 out of sync 提示
	 */
	public boolean isOutOfSync() {
		assertTrue("参数错误：未设置 oldName。", oldName != null);
		try {
			lblOutOfSync(oldName).isVisible();
			return true;
		} catch (WidgetNotFoundException e) {
			return false;
		}
	}

	/************************************
	 * 				界面映射
	 ************************************/
	
	/**
	 * @return 按钮：取消;
	 */
	public SWTBotButton btnCancel() {
		return dialogBot.button(TsUIConstants.getString("btnCancel"));
	}

	/**
	 * @return 按钮：确定;
	 */
	public SWTBotButton btnOK() {
		return dialogBot.button(TsUIConstants.getString("btnOK"));
	}

	/**
	 * @return 按钮：预览 未展开;
	 */
	public SWTBotButton btnPreviewCollapsed() {
		return dialogBot.button(TsUIConstants.getString("btnPreviewCollapsed"));
	}

	/**
	 * @return 按钮：预览 展开;
	 */
	public SWTBotButton btnPreviewExpanded() {
		return dialogBot.button(TsUIConstants.getString("btnPreviewExpanded"));
	}

	/**
	 * @param oldName
	 *            旧文件名，在信息中包含
	 * @return 文字标签：out of sync 信息的内容
	 */
	public SWTBotLabel lblOutOfSync(String oldName) {
		return dialogBot.label(MessageFormat.format(TsUIConstants.getString("lblOutOfSync"), oldName));
	}

	/**
	 * @return 文本框：新名称;
	 */
	public SWTBotText txtWLblNewName() {
		return dialogBot.textWithLabel(TsUIConstants.getString("txtWLblNewName"));
	}
}
