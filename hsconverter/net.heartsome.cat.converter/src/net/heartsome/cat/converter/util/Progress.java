package net.heartsome.cat.converter.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * 管理 progress monitor 的帮助类
 * @author cheney
 */
public final class Progress {

	/**
	 * 私有构建函数，防止类会外部实例化
	 */
	private Progress() {

	}

	/**
	 * 对 monitor 进行检查，如果为 <code>NULL</code> 则返回<code>NullProgressMonitor</code>
	 * @param monitor
	 * @return ;
	 */
	public static IProgressMonitor getMonitor(IProgressMonitor monitor) {
		return monitor == null ? new NullProgressMonitor() : monitor;
	}

	/**
	 * 返回<code>SubProgressMonitor</code>
	 * @param parent
	 * @param ticks
	 * @return ;
	 */
	public static IProgressMonitor getSubMonitor(IProgressMonitor parent, int ticks) {
		return new SubProgressMonitor(getMonitor(parent), ticks, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
	}

	/**
	 * 返回<code>NullProgressMonitor</code>
	 * @return ;
	 */
	public static IProgressMonitor getMonitor() {
		return new NullProgressMonitor();
	}
}
