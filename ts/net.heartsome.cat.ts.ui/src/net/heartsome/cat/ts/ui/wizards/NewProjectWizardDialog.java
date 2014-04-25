package net.heartsome.cat.ts.ui.wizards;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage;
import net.heartsome.cat.ts.ui.resource.Messages;
import net.heartsome.cat.ts.ui.util.PreferenceUtil;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * 新建项目向导框
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class NewProjectWizardDialog extends TSWizardDialog {
	
	private Button btnSetting;

	public NewProjectWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSetting = createButton(parent, -1, Messages.getString("wizards.NewProjectWizardDialog.btnSetting"), true);
		super.createButtonsForButtonBar(parent);
		getButton(-1).addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferenceUtil.openPreferenceDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), ProjectPropertiesPreferencePage.ID);
				if (getCurrentPage() instanceof NewProjectWizardProjInfoPage) {
					NewProjectWizardProjInfoPage page = (NewProjectWizardProjInfoPage) getCurrentPage();
					page.reload();
				}
			}
		});
	}

	@Override
	public void updateButtons() {
		super.updateButtons();
		btnSetting.setVisible(getCurrentPage().getPreviousPage() == null);
	}
}
