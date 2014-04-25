package net.heartsome.cat.convert.ui.model;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * 默认的转项目实现
 * @author cheney
 * @since JDK1.6
 */
public class DefaultConversionItem implements IConversionItem {

	/**
	 * 空转换项目
	 */
	public static final IConversionItem EMPTY_CONVERSION_ITEM = new DefaultConversionItem(Path.EMPTY);

	/**
	 * 转换项目的路径
	 */
	protected IPath path;

	/**
	 * 相对于转换项目路径的上一层转换项目
	 */
	protected IConversionItem parent;

	/**
	 * 默认转换项目的构建函数
	 * @param path
	 */
	public DefaultConversionItem(IPath path) {
		this.path = path;

	}

	public void refresh() {
		// do nothing
	}

	public boolean contains(ISchedulingRule rule) {
		if (this == rule) {
			return true;
		}
		if (!(rule instanceof DefaultConversionItem)) {
			return false;
		}
		DefaultConversionItem defaultConversionItem = (DefaultConversionItem) rule;
		// 处理路径相同的情况
		return path.equals(defaultConversionItem.path);
	}

	public boolean isConflicting(ISchedulingRule rule) {
		if (!(rule instanceof DefaultConversionItem)) {
			return false;
		}
		DefaultConversionItem defaultConversionItem = (DefaultConversionItem) rule;
		// 处理路径相同的情况
		return path.equals(defaultConversionItem.path);
	}

	public IPath getLocation() {
		return path;
	}

	public IConversionItem getParent() {
		if (parent == null) {
			File temp = path.toFile().getParentFile();
			if (temp != null) {
				parent = new DefaultConversionItem(new Path(temp.getAbsolutePath()));
			}
		}
		return parent;
	}

	public String getName() {
		return path.toFile().getName();
	}

	public IConversionItem getProject() {
		return getParent();
	}

}
