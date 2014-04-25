/**
 * ConverterException.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * The Class ConverterException. 转换器在转换的过程中发生错误时，抛出的需检测异常。 此类继承自<code>CoreException</code>，<code>CoreException</code>
 * 位于插件 org.eclipse.equinox.common 的 org.eclipse.core.runtime 包内。 插件 org.eclipse.equinox.common 内的的类是不依赖 OSGI
 * 平台的，所以转换器接口及其实现依赖此类，并不会使转换器实现依赖于特定的 eclipse 平台或 OSGI 平台。但转换器内部的错误异常转化为<code>CoreException</code>
 * 的子类实例进行抛出，可以方便客户端代码(UI 和相关逻辑层）以 eclipse 平台统一的方式进行处理。 转换器在转换的过程中，以会遇到不同种类的异常，具体的异常可在此类的基础上进行扩展。
 * @author cheney
 * @version
 * @since JDK1.6
 */
public class ConverterException extends CoreException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new converter exception.
	 * @param status
	 *            the status
	 */
	public ConverterException(IStatus status) {
		super(status);
	}

}
