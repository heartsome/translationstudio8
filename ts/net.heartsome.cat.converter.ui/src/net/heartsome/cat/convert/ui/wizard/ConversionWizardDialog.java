package net.heartsome.cat.convert.ui.wizard;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.convert.ui.Activator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Shell;

/**
 * 继承 WizardDialog，并覆盖 getDialogBoundsSettings 方法，以记住此向导对话框上一次打开时的大小
 * @author cheney
 * @since JDK1.6
 */
public class ConversionWizardDialog extends TSWizardDialog {

	/**
	 * 正向转换向导对话框构造函数
	 * @param parentShell
	 * @param newWizard
	 */
	public ConversionWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings();
	}

}
