package net.heartsome.cat.p2update;

import net.heartsome.cat.p2update.util.P2UpdateUtil;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.engine.EngineActivator;
import org.eclipse.equinox.p2.operations.ProfileChangeOperation;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * 更新策略配置类
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class UpdatePolicy extends Policy {
	
	@Override
	public boolean continueWorkingWithOperation(ProfileChangeOperation operation, Shell shell) {

		Assert.isTrue(operation.getResolutionResult() != null);
		IStatus status = operation.getResolutionResult();
		// user cancelled
		if (status.getSeverity() == IStatus.CANCEL)
			return false;

		// Special case those statuses where we would never want to open a wizard
		if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
			MessageDialog.openInformation(shell, P2UpdateUtil.CHECK_UPDATE_JOB_NAME, P2UpdateUtil.UPDATE_PROMPT_INFO_NO_UPDATE);
			return false;
		}

		// there is no plan, so we can't continue. Report any reason found
		if (operation.getProvisioningPlan() == null && !status.isOK()) {
			StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
			return false;
		}

		// Allow the wizard to open otherwise.
		return true;
	}

	public void updateForPreferences() {
		setRestartPolicy(RESTART_POLICY_PROMPT);
		setRepositoriesVisible(false);
		System.setProperty(EngineActivator.PROP_UNSIGNED_POLICY, EngineActivator.UNSIGNED_ALLOW);
	}
}
