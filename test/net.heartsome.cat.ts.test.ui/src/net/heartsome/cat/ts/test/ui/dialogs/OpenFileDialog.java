package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.msgdialogs.InfoFileNotFound;
import net.heartsome.test.swtbot.finders.HsSWTWorkbenchBot;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.waits.IsEditorOpened;
import net.heartsome.test.utilities.sikuli.OsUtil;

import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;

/**
 * 打开文件对话框
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public final class OpenFileDialog {
	
	/**
	 * 
	 */
	private OpenFileDialog() {
	}

	/**
	 * 默认文件路径有效
	 * @param filePath
	 *            文件路径
	 */
	public static void openFile(String filePath) {
		openFile(filePath, true, true);
	}

	/**
	 * 在打开文件对话框中输入指定文件名称或路径
	 * @param filePath
	 *            要打开的文件名称或路径
	 * @param isValid
	 *            是否为有效文件名称或路径
	 * @param isFile
	 *            true 表示输入的是文件名称，false 表示文件路径
	 */
	public static void openFile(String filePath, boolean isValid, boolean isFile) {
		OsUtil.typePath(filePath); // typePath 方法处理了多种操作系统的情况

		String fileName = new Path(filePath).lastSegment(); // 从路径中取出文件名

		if (isValid) {
			// 确认编辑器成功打开
			HsSWTWorkbenchBot bot = HSBot.bot();
			final SWTBotEditor editor = bot.editorByTitle(fileName);
			bot.waitUntil(new IsEditorOpened(editor));
		} else {
			if (isFile) {
				// 弹出信息对话框
				InfoFileNotFound fnf = new InfoFileNotFound(filePath);
				fnf.msgFileNotFound().isVisible(); // 文件未找到
				fnf.btnOK().click();
			} else {
				try {
					OsUtil.typeEnter(); // 在系统对话框的信息提示框上点确认
					OsUtil.typeEsc(); // 按 Esc 取消打开文件
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
