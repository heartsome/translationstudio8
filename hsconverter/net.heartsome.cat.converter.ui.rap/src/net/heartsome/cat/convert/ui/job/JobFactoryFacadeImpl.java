package net.heartsome.cat.convert.ui.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.swt.widgets.Display;

/**
 * 创建执行文件转换的 job
 * @author cheney
 */
public class JobFactoryFacadeImpl extends JobFactoryFacade {

	@Override
	protected Job createJobInternal(final Display display, final String name, final JobRunnable runnable) {
		Job job = new Job(name) {
			final IStatus[] result = new IStatus[1];

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				// 允许后台线程访问 session 级别的用户数据
				UICallBack.runNonUIThreadWithFakeContext(display, new Runnable() {

					@Override
					public void run() {
						result[0] = runnable.run(monitor);
					}
				});
				return result[0];
			}
		};
		return job;
	}

}
