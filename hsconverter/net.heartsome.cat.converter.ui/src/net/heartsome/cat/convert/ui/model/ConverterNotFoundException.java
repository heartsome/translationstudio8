package net.heartsome.cat.convert.ui.model;

import net.heartsome.cat.converter.ConverterException;

import org.eclipse.core.runtime.IStatus;

/**
 * 找不到所需转换器的需捕获异常
 * @author cheney
 * @since JDK1.6
 */
public class ConverterNotFoundException extends ConverterException {

	/** serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * 构造函数
	 * @param status
	 */
	public ConverterNotFoundException(IStatus status) {
		super(status);
	}

}
