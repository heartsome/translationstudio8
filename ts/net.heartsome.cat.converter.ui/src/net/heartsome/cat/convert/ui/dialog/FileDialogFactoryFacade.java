package net.heartsome.cat.convert.ui.dialog;

import net.heartsome.cat.convert.ui.ImplementationLoader;

import org.eclipse.swt.widgets.Shell;

/**
 * 打开文件对话框的 factory facade
 * @author cheney
 * @since JDK1.6
 */
public abstract class FileDialogFactoryFacade {
	private static final FileDialogFactoryFacade IMPL;

	static {
		IMPL = (FileDialogFactoryFacade) ImplementationLoader.newInstance(FileDialogFactoryFacade.class);
	}

	/**
	 * @param shell
	 * @return 返回具体的文件对话框实现;
	 */
	public static IConversionItemDialog createFileDialog(final Shell shell, int styled) {
		return IMPL.createFileDialogInternal(shell, styled);
	}

	/**
	 * @param shell
	 * @param styled
	 * @return 返回显示工作空间中的文件的对话框;
	 */
	public static IConversionItemDialog createWorkspaceDialog(final Shell shell, int styled) {
		return IMPL.createWorkspaceDialogInternal(shell, styled);
	}

	/**
	 * @param shell
	 * @return 返回文件对话框的内部实现;
	 */
	protected abstract IConversionItemDialog createFileDialogInternal(Shell shell, int styled);

	/**
	 * @param shell
	 * @param styled
	 * @return 返回显示工作空间中的文件的对话框;;
	 */
	protected abstract IConversionItemDialog createWorkspaceDialogInternal(Shell shell, int styled);

}
