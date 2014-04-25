package net.heartsome.cat.p2update.handler;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.p2update.ui.UpdateWizard;
import net.heartsome.cat.p2update.util.P2UpdateUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.operations.RepositoryTracker;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;

/**
 * 更新检查Handler
 * 
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class CheckUpdateHandler extends PreloadingRepositoryHandler {
	boolean hasNoRepos = false;
	UpdateOperation operation;


	@Override
	protected String getProgressTaskName() {
		return P2UpdateUtil.CHECK_UPDATE_JOB_NAME;
	}

	@Override
	protected void doExecute(LoadMetadataRepositoryJob job) {
		// TODO Auto-generated method stub		
		if (hasNoRepos) {
			P2UpdateUtil.openConnectErrorInfoDialog(getShell(), P2UpdateUtil.INFO_TYPE_CHECK);
			return;
		}
		
		if (getProvisioningUI().getPolicy().continueWorkingWithOperation(operation, getShell())) {
			UpdateWizard wizard = new UpdateWizard(getProvisioningUI(), operation, operation.getSelectedUpdates());
			TSWizardDialog dialog = new TSWizardDialog(getShell(), wizard);
			dialog.create();
			dialog.open();
		}
	}
	
	@Override
	protected void doPostLoadBackgroundWork(IProgressMonitor monitor) throws OperationCanceledException {
		operation = getProvisioningUI().getUpdateOperation(null, null);
		// check for updates
		IStatus resolveStatus = operation.resolveModal(monitor);
		if (resolveStatus.getSeverity() == IStatus.CANCEL)
			throw new OperationCanceledException();
	}

	@Override
	protected boolean preloadRepositories() {
		hasNoRepos = false;
		RepositoryTracker repoMan = getProvisioningUI().getRepositoryTracker();
		if (repoMan.getKnownRepositories(getProvisioningUI().getSession()).length == 0) {
			hasNoRepos = true;
			return false;
		}
		return super.preloadRepositories();
	}
}
