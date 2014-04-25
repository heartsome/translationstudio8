/**
 * ConversionValidateStrategy.java
 *
 * Version information :
 *
 * Date:Apr 13, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.convert.ui.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * 验证文件转换的配置信息是否正确的顶层接口。
 * @author cheney
 * @since JDK1.6
 */
public interface ConversionValidateStrategy {

	/**
	 * 验证文件转换的配置信息
	 * @param path
	 *            文件路径
	 * @param configuraion
	 *            文件转换配置信息
	 * @param monitor
	 * @return 返回验证的结果状态;
	 */
	IStatus validate(String path, ConversionConfigBean configuraion, IProgressMonitor monitor);
}
