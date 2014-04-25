package net.heartsome.cat.convert.ui.dialog;

import org.eclipse.swt.widgets.Shell;

/**
 * 文件对话框工厂门面的具体实现
 * @author cheney
 * @since JDK1.6
 */
public class FileDialogFactoryFacadeImpl extends FileDialogFactoryFacade {

	@Override
	protected IConversionItemDialog createFileDialogInternal(Shell shell, int styled) {
		return new FileConversionItemDialog(shell);
	}

	@Override
	protected IConversionItemDialog createWorkspaceDialogInternal(Shell shell, int styled) {
		return new WorkspaceConversionItemDialog(shell);
	}
}
