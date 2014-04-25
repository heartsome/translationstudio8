package net.heartsome.cat.convert.ui.dialog;

import net.heartsome.cat.convert.ui.model.DefaultConversionItem;
import net.heartsome.cat.convert.ui.model.FileConversionItem;
import net.heartsome.cat.convert.ui.model.IConversionItem;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;

/**
 * 工作空间中的转换项目选择对话框
 * @author cheney
 * @since JDK1.6
 */
public class WorkspaceConversionItemDialog implements IConversionItemDialog {

	private WorkspaceDialog worksapceDialog;
	private IConversionItem conversionItem;

	/**
	 * 构造函数
	 * @param shell
	 */
	public WorkspaceConversionItemDialog(Shell shell) {
		worksapceDialog = new WorkspaceDialog(shell);
	}

	public int open() {
		int result = worksapceDialog.open();
		IFile file = null;
		if (result == IDialogConstants.OK_ID) {
			file = worksapceDialog.getSelectedFile();
			if (file != null) {
				conversionItem = new FileConversionItem(file);
			}
			return IDialogConstants.OK_ID;
		}
		return IDialogConstants.CANCEL_ID;
	}

	public IConversionItem getConversionItem() {
		if (conversionItem == null) {
			conversionItem = new DefaultConversionItem(Path.EMPTY);
		}
		return conversionItem;
	}

}
