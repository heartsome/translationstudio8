package net.heartsome.cat.ts.exportproject.wizards;

import net.heartsome.cat.ts.exportproject.resource.Messages;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.slf4j.LoggerFactory;

/**
 * 导出项目向导
 * @author peason
 * @version
 * @since JDK1.6
 */
public class ExportProjectWizard extends Wizard implements IExportWizard {

	private IStructuredSelection selection;

	private ExportProjectWizardPage page;

	public ExportProjectWizard() {
		setWindowTitle(Messages.getString("wizard.ExportProjectWizard.title"));
		setNeedsProgressMonitor(true);
		
		 IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	        IDialogSettings section = workbenchSettings
	                .getSection("ExportProjectWizard");//$NON-NLS-1$
	        if (section == null) {
				section = workbenchSettings.addNewSection("ExportProjectWizard");//$NON-NLS-1$
			}
	        setDialogSettings(section);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		boolean isFinish = page.finish();
		if (isFinish) {
			MessageDialog.openInformation(getShell(), Messages.getString("wizard.ExportProjectWizard.msgTitle"),
					Messages.getString("wizard.ExportProjectWizard.msg"));
		}
		return isFinish;
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		super.addPages();
		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = workbenchPage.findView("net.heartsome.cat.common.ui.navigator.view");
		if (viewPart != null) {
			this.selection = (StructuredSelection) viewPart.getSite().getSelectionProvider().getSelection();
		}
		page = new ExportProjectWizardPage("", selection);
		addPage(page);
	}

}
