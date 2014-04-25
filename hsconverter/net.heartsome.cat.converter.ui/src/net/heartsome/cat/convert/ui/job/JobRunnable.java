package net.heartsome.cat.convert.ui.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;

/**
 * 定义此接口来封装需要在 Job 中运行的代码
 * @author cheney
 * @since JDK1.6
 */
public interface JobRunnable {

	/**
	 * 在 run 方法中包含所要执行的代码
	 * @param monitor
	 *            监视器，可以为 NULL
	 * @return 代码执行的结果;
	 */
	IStatus run(IProgressMonitor monitor);

	/**
	 * 显示此 runnable 的运行结果，如直接弹出成功或失败对话框，主要用于进度显示对话框没有关闭的情况，即进度显示在模式对话框中。
	 * @param status
	 *            runnable 运行结果的 status;
	 */
	void showResults(IStatus status);

	/**
	 * 用于显示 runnable 运行结果的 action，通常是 job 在后台运行完成后，通过在进度显示视图中触发相关的 action 链接来查看结果。
	 * @param status
	 *            runnable 运行结果的 status
	 * @return 返回显示 runnable 运行结果的 action;
	 */
	IAction getRunnableCompletedAction(IStatus status);

}
