package net.heartsome.cat.convert.ui.dialog;

import java.io.File;

import net.heartsome.cat.convert.ui.model.ConverterContext;
import net.heartsome.cat.convert.ui.model.DefaultConversionItem;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.converter.ui.rcp.resource.Messages;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * 从文件对话框中选择所需要的转换项目
 * @author cheney
 * @since JDK1.6
 */
public class FileConversionItemDialog implements IConversionItemDialog {
	private FileDialog fileDialog;
	private IConversionItem conversionItem;

	/**
	 * 转换项目选择对话框构造函数
	 * @param shell
	 */
	public FileConversionItemDialog(Shell shell) {
		fileDialog = new FileDialog(shell);
	}

	public int open() {
		File folder = new File(ConverterContext.srxFolder);
		fileDialog.setFilterPath(folder.getAbsolutePath());
		String[] extensions = { "*.srx" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String[] names = {
				Messages.getString("dialog.FileConversionItemDialog.filterName1"), Messages.getString("dialog.FileConversionItemDialog.filterName2"), net.heartsome.cat.converter.ui.rcp.resource.Messages.getString("dialog.FileConversionItemDialog.filterName3") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		fileDialog.setFilterNames(names);
		fileDialog.setFilterExtensions(extensions);
		String name = fileDialog.open();
		if (name != null) {
			File f = new File(name);
			if (f.exists() && f.getParent().equals(folder.getAbsolutePath())) {
				name = ConverterContext.srxFolder + System.getProperty("file.separator") + f.getName(); //$NON-NLS-1$
			}
			conversionItem = new DefaultConversionItem(new Path(name));
			return IDialogConstants.OK_ID;
		}
		return IDialogConstants.CANCEL_ID;
	}

	public IConversionItem getConversionItem() {
		if (conversionItem == null) {
			conversionItem = DefaultConversionItem.EMPTY_CONVERSION_ITEM;
		}
		return conversionItem;
	}

}
