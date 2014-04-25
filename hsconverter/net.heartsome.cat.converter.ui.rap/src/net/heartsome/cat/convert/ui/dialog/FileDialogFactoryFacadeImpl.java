package net.heartsome.cat.convert.ui.dialog;

import org.eclipse.swt.widgets.Shell;

/**
 * RAP 环境中的文件对话框工厂门面实现
 * @author cheney
 * @since JDK1.6
 */
public class FileDialogFactoryFacadeImpl extends FileDialogFactoryFacade {

	// 返回文件上传对话框
	@Override
	protected IConversionItemDialog createFileDialogInternal(Shell shell, int styled) {
		return createFileUploadDialog(shell, styled);
	}

	// 返回文件上传对话框
	@Override
	protected IConversionItemDialog createWorkspaceDialogInternal(Shell shell, int styled) {
		return createFileUploadDialog(shell, styled);
	}

	/**
	 * 创建文件上传对话框实例并返回
	 * @param shell
	 * @param styled
	 * @return ;
	 */
	private IConversionItemDialog createFileUploadDialog(Shell shell, int styled) {
		return new FileUploadConversionItemDialog(shell);
	}

}
