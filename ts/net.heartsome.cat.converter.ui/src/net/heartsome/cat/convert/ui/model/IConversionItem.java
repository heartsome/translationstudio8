package net.heartsome.cat.convert.ui.model;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * 转换过程中表示源、目标或骨架等的转换项目，具体的实现可以是文件或其他形式，定义此接口的目的是适配 org.eclipse.core.resources 中 的 IFile 等接口，org.eclipse.core.resources
 * 不包含在 RAP 目标平台中，且 org.eclipse.core.resources 的实现不是多用户的，所以不建议把 org.eclipse.core.resources 及其依赖放到 RAP 平台中直接使用。同时实现
 * ISchedulingRule 接口定义资源的访问规则。
 * @author cheney
 */
public interface IConversionItem extends ISchedulingRule {

	/**
	 * 刷新 conversion item ;
	 */
	void refresh();

	/**
	 * @return 返回此转换项目的位置;
	 */
	IPath getLocation();

	/**
	 * 获得转换项目的父
	 * @return ;
	 */
	IConversionItem getParent();

	/**
	 * 获得转换项目所在的项目，如果转换项目不在工作空间中，则直接返回其上一层转换项目
	 * @return ;
	 */
	IConversionItem getProject();

	/**
	 * 获得转换项目的名称
	 * @return ;
	 */
	String getName();

}
