/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.heartsome.cat.p2update.handler;

import net.heartsome.cat.p2update.util.P2UpdateUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
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
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.LoggerFactory;

/**
 * PreloadingRepositoryHandler provides background loading of repositories before executing the provisioning handler.
 * @since 3.5
 */
public abstract class PreloadingRepositoryHandler extends AbstractHandler {

	/**
	 * The constructor.
	 */
	public PreloadingRepositoryHandler() {
		// constructor
	}

	/**
	 * Execute the command.
	 */
	public Object execute(ExecutionEvent event) {
		// Look for a profile. We may not immediately need it in the
		// handler, but if we don't have one, whatever we are trying to do
		// will ultimately fail in a more subtle/low-level way. So determine
		// up front if the system is configured properly.
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
			P2UpdateUtil.openConnectErrorInfoDialog(getShell(), P2UpdateUtil.INFO_TYPE_CHECK);
			return null;
		} else {
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
				public void run() {
					doExecuteAndLoad();
				}
			});
		}
		return null;
	}

	void doExecuteAndLoad() {
		if (preloadRepositories()) {
			// cancel any load that is already running
			final IStatus[] checkStatus = new IStatus[1];
			Job.getJobManager().cancel(LoadMetadataRepositoryJob.LOAD_FAMILY);
			final LoadMetadataRepositoryJob loadJob = new LoadMetadataRepositoryJob(getProvisioningUI()) {
				public IStatus runModal(IProgressMonitor monitor) {
					SubMonitor sub = SubMonitor.convert(monitor, getProgressTaskName(), 1000);
					IStatus status = super.runModal(sub.newChild(500));
					if (status.getSeverity() == IStatus.CANCEL)
						return status;
					if (status.getSeverity() != IStatus.OK) {
						// 记录检查错误
						checkStatus[0] = status;
						return Status.OK_STATUS;
					}
					try {
						doPostLoadBackgroundWork(sub.newChild(500));
					} catch (OperationCanceledException e) {
						return Status.CANCEL_STATUS;
					}
					return status;
				}
			};
			setLoadJobProperties(loadJob);
			loadJob.setName(P2UpdateUtil.CHECK_UPDATE_JOB_NAME);
			if (waitForPreload()) {
				loadJob.addJobChangeListener(new JobChangeAdapter() {
					public void done(IJobChangeEvent event) {
						if (PlatformUI.isWorkbenchRunning())
							if (event.getResult().isOK()) {
								PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
									public void run() {
										if (checkStatus[0] != null) {
											// 提示连接异常
											P2UpdateUtil.openConnectErrorInfoDialog(getShell(),
													P2UpdateUtil.INFO_TYPE_CHECK);
											return;
										}
										doExecute(loadJob);
									}
								});
							}
					}
				});
				loadJob.setUser(true);
				loadJob.schedule();

			} else {
				loadJob.setSystem(true);
				loadJob.setUser(false);
				loadJob.schedule();
				doExecute(null);
			}
		} else {
			doExecute(null);
		}
	}

	protected abstract String getProgressTaskName();

	protected abstract void doExecute(LoadMetadataRepositoryJob job);

	protected boolean preloadRepositories() {
		return true;
	}

	protected void doPostLoadBackgroundWork(IProgressMonitor monitor) throws OperationCanceledException {
		// default is to do nothing more.
	}

	protected boolean waitForPreload() {
		return true;
	}

	protected void setLoadJobProperties(Job loadJob) {
		loadJob.setProperty(LoadMetadataRepositoryJob.ACCUMULATE_LOAD_ERRORS, Boolean.toString(true));
	}

	protected ProvisioningUI getProvisioningUI() {
		return ProvisioningUI.getDefaultUI();
	}

	/**
	 * Return a shell appropriate for parenting dialogs of this handler.
	 * @return a Shell
	 */
	protected Shell getShell() {
		return PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
	}
}
