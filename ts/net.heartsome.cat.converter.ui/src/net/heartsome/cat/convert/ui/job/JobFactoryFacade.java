package net.heartsome.cat.convert.ui.job;

import net.heartsome.cat.convert.ui.ImplementationLoader;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

/**
 * 创建 Job 的 factory facade
 * @author cheney
 * @since JDK1.6
 */
public abstract class JobFactoryFacade {
	private static final JobFactoryFacade IMPL;

	static {
		IMPL = (JobFactoryFacade) ImplementationLoader.newInstance(JobFactoryFacade.class);
	}

	/**
	 * 创建 Job
	 * @param display
	 * @param name
	 * @param runnable
	 * @return ;
	 */
	public static Job createJob(final Display display, final String name, final JobRunnable runnable) {
		return IMPL.createJobInternal(display, name, runnable);
	}

	/**
	 * 创建 Job 的内部实现
	 * @param display
	 * @param name
	 * @param runnable
	 * @return ;
	 */
	protected abstract Job createJobInternal(Display display, String name, JobRunnable runnable);

}
