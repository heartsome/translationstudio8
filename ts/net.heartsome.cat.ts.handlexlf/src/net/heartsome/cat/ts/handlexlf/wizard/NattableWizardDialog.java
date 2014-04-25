package net.heartsome.cat.ts.handlexlf.wizard;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.ts.handlexlf.Activator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Shell;

/**
 * 向导窗体,覆盖 getDialogBoundsSettings 方法，以记住此向导对话框上一次打开时的大小
 * @author robert	2011-10-28
 */
public class NattableWizardDialog extends TSWizardDialog{

	public NattableWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings();
	}
}
