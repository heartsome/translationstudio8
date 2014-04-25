package net.heartsome.cat.convert.ui.action;

import java.io.File;
import java.util.Map;

import net.heartsome.cat.convert.ui.resource.Messages;
import net.heartsome.cat.converter.Converter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * 演示文件转换后台线程的转换结果 action
 * @author cheney,weachy
 * @since JDK1.5
 */
public class ConversionCompleteAction extends Action {

	private IStatus status;

	private Map<String, String> conversionResult;

	/**
	 * 转换完成 action 的构造函数
	 * @param name
	 *            action 的显示文本
	 * @param status
	 *            转换的结果状态
	 * @param conversionResult
	 */
	public ConversionCompleteAction(String name, IStatus status, Map<String, String> conversionResult) {
		setText(name);
		setToolTipText(name);
		this.status = status;
		this.conversionResult = conversionResult;
	}

	@Override
	public void run() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		Assert.isNotNull(window);
		Shell shell = window.getShell();
		if (status.getSeverity() == IStatus.ERROR) {
			MessageDialog.openError(shell, Messages.getString("action.ConversionCompleteAction.msgTitle1"),
					status.getMessage());
		} else {
			// 转换完成后直接打开编辑器，不再进行弹框提示。
			// MessageDialog.openInformation(shell, "文件转换完成", status.getMessage());
			final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
			String xliffFile = conversionResult.get(Converter.ATTR_XLIFF_FILE);
			IWorkbenchPage page = window.getActivePage();
			Assert.isNotNull(page, Messages.getString("action.ConversionCompleteAction.msg1"));
			if (xliffFile != null) {
				IEditorDescriptor editorDescriptor = PlatformUI.getWorkbench().getEditorRegistry()
						.findEditor(XLIFF_EDITOR_ID);
				if (editorDescriptor != null) {
					try {
						IDE.openEditor(page, new File(xliffFile).toURI(), XLIFF_EDITOR_ID, true);
					} catch (PartInitException e) {
						MessageDialog.openInformation(shell,
								Messages.getString("action.ConversionCompleteAction.msgTitle2"),
								Messages.getString("action.ConversionCompleteAction.msg2") + e.getMessage());
						e.printStackTrace();
					}
				}
			} else {
				String targetFile = conversionResult.get(Converter.ATTR_TARGET_FILE);
				if (targetFile == null) {
					MessageDialog.openInformation(shell,
							Messages.getString("action.ConversionCompleteAction.msgTitle2"), Messages.getString("action.ConversionCompleteAction.msg3"));
				} else {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					IFile input = root.getFileForLocation(new Path(targetFile));
					try {
						// 使用外部编辑器（系统默认编辑器）打开文件。
						IDE.openEditor(page, input, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
					} catch (PartInitException e) {
						MessageDialog.openInformation(shell,
								Messages.getString("action.ConversionCompleteAction.msgTitle2"),
								Messages.getString("action.ConversionCompleteAction.msg4") + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
	}
}
