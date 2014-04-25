package net.heartsome.cat.convert.ui.job;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * 对 Workspace 中的资源进行操作时，考虑到的并发操作，需要把这些操作放在 WorkspaceJob 中执行，所以此 factory facade 返回的是 WorkspaceJob。
 * @author cheney
 * @since JDK1.6
 */
public class JobFactoryFacadeImpl extends JobFactoryFacade {

	@Override
	protected Job createJobInternal(final Display display, final String name, final JobRunnable runnable) {
		return new WorkspaceJob(name) {
			IStatus result;

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				result = runnable.run(monitor);
				if (isModal(this)) {
					// 直接显示结果
					if (display != null && !display.isDisposed()) {
						display.asyncExec(new Runnable() {
							public void run() {
								runnable.showResults(result);
							}
						});
					}
				} else {
					// 在进度显示视图中显示查看 runnable 运行结果的 action。
					setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
					setProperty(IProgressConstants.ACTION_PROPERTY, runnable.getRunnableCompletedAction(result));

				}
				return result;
			}

			/**
			 * 判断 job 对应的进度显示对话框的状态。如果是打开状态则返回 true，否则返回 false。
			 * @param job
			 *            后台线程 job
			 * @return 返回 job 对应的进度显示对话框的状态;
			 */
			private boolean isModal(Job job) {
				Boolean isModal = (Boolean) job.getProperty(IProgressConstants.PROPERTY_IN_DIALOG);
				if (isModal == null) {
					return false;
				}
				return isModal.booleanValue();
			}
		};
	}

}
