package net.heartsome.cat.database.ui.tb.wizard;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.database.ui.tb.preference.TBDatabasePage;
import net.heartsome.cat.database.ui.tb.resource.Messages;
import net.heartsome.cat.ts.ui.util.PreferenceUtil;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * 导入 TBX 向导框
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class TermDbManagerImportWizardDialog extends TSWizardDialog {

	private Button btnSetting;

	public TermDbManagerImportWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSetting = createButton(parent, -1, Messages.getString("wizard.TermDbManagerImportWizardTbxPage.settingBtn"),
				true);
		super.createButtonsForButtonBar(parent);
		btnSetting.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferenceUtil.openPreferenceDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
						TBDatabasePage.ID);
			}
		});
	}

	@Override
	public void updateButtons() {
		super.updateButtons();
		btnSetting.setVisible(getCurrentPage() instanceof NewTermDbImportPage
				|| getCurrentPage() instanceof TbxImportWizardTbxPage
				|| getCurrentPage() instanceof TermDbManagerImportWizardTbxPage);
	}
}
