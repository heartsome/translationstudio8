package net.heartsome.cat.p2update.ui;

import java.util.ArrayList;

import net.heartsome.cat.p2update.util.P2UpdateUtil;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.model.AvailableUpdateElement;
import org.eclipse.equinox.internal.p2.ui.model.IUElementListRoot;
import org.eclipse.equinox.p2.operations.ProfileModificationJob;
import org.eclipse.equinox.p2.operations.Update;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * 更新向导
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class UpdateWizard extends Wizard {

	Update[] initialSelections;
	UpdateOperation operation;
	ProvisioningUI ui;
	// UpdateWizardPage fristPage;
	protected IUElementListRoot root;

	public UpdateWizard(ProvisioningUI ui, UpdateOperation operation, Object[] initialSelections) {
		Assert.isLegal(operation.hasResolved(), "Cannot create an update wizard on an unresolved operation"); //$NON-NLS-1$
		setWindowTitle(P2UpdateUtil.UI_WIZARD_DIALOG_TITLE);
		// setDefaultPageImageDescriptor(ProvUIImages.getImageDescriptor(ProvUIImages.WIZARD_BANNER_UPDATE));
		this.operation = operation;
		// this.initialSelections = (Update[]) initialSelections;
		this.ui = ui;
		// initializeResolutionModelElements(initialSelections);
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		// fristPage = new UpdateWizardPage(operation, root, ui);
		// addPage(fristPage);
		UpdateDescriptionPage descPage = new UpdateDescriptionPage(operation, root, ui);
		addPage(descPage);
	}

	@Override
	public boolean performFinish() {
		if (operation.getResolutionResult().getSeverity() != IStatus.ERROR) {
			ProfileModificationJob job = (ProfileModificationJob) operation.getProvisioningJob(null);
			job.setName(P2UpdateUtil.EXECUTE_UPDATE_JOB_NAME);
			job.setTaskName(P2UpdateUtil.EXECUTE_UPDATE_Task_NAME);
			ui.schedule(job, StatusManager.SHOW | StatusManager.LOG);
			return true;
		}
		return false;
	}

	protected void initializeResolutionModelElements(Object[] selectedElements) {
		root = new IUElementListRoot();
		ArrayList<AvailableUpdateElement> list = new ArrayList<AvailableUpdateElement>(selectedElements.length);
		for (int i = 0; i < selectedElements.length; i++) {
			if (selectedElements[i] instanceof AvailableUpdateElement) {
				AvailableUpdateElement element = (AvailableUpdateElement) selectedElements[i];
				AvailableUpdateElement newElement = new AvailableUpdateElement(root, element.getIU(),
						element.getIUToBeUpdated(), getProfileId(), shouldShowProvisioningPlanChildren());
				list.add(newElement);
			} else if (selectedElements[i] instanceof Update) {
				Update update = (Update) selectedElements[i];
				AvailableUpdateElement newElement = new AvailableUpdateElement(root, update.replacement,
						update.toUpdate, getProfileId(), shouldShowProvisioningPlanChildren());
				list.add(newElement);
			}
		}
		root.setChildren(list.toArray());
	}

	protected boolean shouldShowProvisioningPlanChildren() {
		return ProvUI.getQueryContext(getPolicy()).getShowProvisioningPlanChildren();
	}

	protected Policy getPolicy() {
		return ui.getPolicy();
	}

	protected String getProfileId() {
		return ui.getProfileId();
	}
}
