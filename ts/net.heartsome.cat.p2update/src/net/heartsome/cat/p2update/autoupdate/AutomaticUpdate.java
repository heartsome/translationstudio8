/**
 * AutomaticUpdate.java
 *
 * Version information :
 *
 * Date:2012-9-4
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.p2update.autoupdate;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.p2update.ui.UpdateWizard;
import net.heartsome.cat.p2update.util.P2UpdateUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.operations.RepositoryTracker;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自动更新
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class AutomaticUpdate {
	UpdateOperation operation;
	public static final Logger logger = LoggerFactory.getLogger(P2UpdateUtil.class);

	public void checkForUpdates() throws OperationCanceledException {
		// 检查 propfile
		String profileId = getProvisioningUI().getProfileId();
		IProvisioningAgent agent = getProvisioningUI().getSession().getProvisioningAgent();
		IProfile profile = null;
		if (agent != null) {
			IProfileRegistry registry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
			if (registry != null) {
				profile = registry.getProfile(profileId);
			}
		}
		if (profile == null) {
			// Inform the user nicely
			P2UpdateUtil.openConnectErrorInfoDialog(getShell(), P2UpdateUtil.INFO_TYPE_AUTO_CHECK);
			return;
		}

		// 开始检查前先确定是否有repository
		RepositoryTracker repoMan = getProvisioningUI().getRepositoryTracker();
		if (repoMan.getKnownRepositories(getProvisioningUI().getSession()).length == 0) {
			P2UpdateUtil.openConnectErrorInfoDialog(getShell(), P2UpdateUtil.INFO_TYPE_AUTO_CHECK);
			return;
		}

		final IStatus[] checkStatus = new IStatus[1];
		Job.getJobManager().cancel(LoadMetadataRepositoryJob.LOAD_FAMILY);
		final LoadMetadataRepositoryJob loadJob = new LoadMetadataRepositoryJob(getProvisioningUI()) {
			public IStatus runModal(IProgressMonitor monitor) {
				SubMonitor sub = SubMonitor.convert(monitor, P2UpdateUtil.CHECK_UPDATE_TASK_NAME, 1000);
				// load repository
				IStatus status = super.runModal(sub.newChild(500));
				if (status.getSeverity() == IStatus.CANCEL) {
					return status;
				}
				if (status.getSeverity() != Status.OK) {
					// load repository error
					checkStatus[0] = status;
				}
				operation = getProvisioningUI().getUpdateOperation(null, null);
				// check for updates
				IStatus resolveStatus = operation.resolveModal(sub.newChild(500));
				if (resolveStatus.getSeverity() == IStatus.CANCEL) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		loadJob.setName(P2UpdateUtil.ATUO_CHECK_UPDATE_JOB_NAME);
		loadJob.setProperty(LoadMetadataRepositoryJob.ACCUMULATE_LOAD_ERRORS, Boolean.toString(true));
		loadJob.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (PlatformUI.isWorkbenchRunning())
					if (event.getResult().isOK()) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								if (checkStatus[0] != null) {
									// 提示连接异常
									P2UpdateUtil.openConnectErrorInfoDialog(getShell(),
											P2UpdateUtil.INFO_TYPE_AUTO_CHECK);
									return;
								}
								doUpdate();
							}
						});
					}
			}
		});
		loadJob.setUser(true);
		loadJob.schedule();
	}

	private void doUpdate() {
		if (operation == null) {
			return;
		}
		IStatus status = operation.getResolutionResult();
		// user cancelled
		if (status.getSeverity() == IStatus.CANCEL)
			return;

		// Special case those statuses where we would never want to open a wizard
		if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {		
			return;
		}
		
		if (getProvisioningUI().getPolicy().continueWorkingWithOperation(operation, getShell())) {
			UpdateWizard wizard = new UpdateWizard(getProvisioningUI(), operation, operation.getSelectedUpdates());
			TSWizardDialog dialog = new TSWizardDialog(getShell(), wizard);
			dialog.create();
			dialog.open();
		}
	}

	protected ProvisioningUI getProvisioningUI() {
		return ProvisioningUI.getDefaultUI();
	}

	protected Shell getShell() {
		return PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
	}

}
