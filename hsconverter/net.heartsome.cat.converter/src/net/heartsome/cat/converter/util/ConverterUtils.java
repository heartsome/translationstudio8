/**
 * ConverterUtils.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.util;

import net.heartsome.cat.converter.ConverterException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * The Class ConverterUtils. 转换工具类，用于转换中需要用到的一些辅助的静态方法.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class ConverterUtils {

	/**
	 * Instantiates a new data constant.
	 */
	protected ConverterUtils() {
		throw new UnsupportedOperationException(); // prevents calls from subclass
	}

	/**
	 * Throw converter exception.
	 * @param plugin
	 *            the plugin
	 * @param message
	 *            the message
	 * @param e
	 *            the e
	 * @throws ConverterException
	 *             the converter exception
	 * @author John Zhu
	 */
	public static void throwConverterException(String plugin, String message, Throwable e) throws ConverterException {
		if (e instanceof OperationCanceledException) {
			return;
		}
		IStatus status = new Status(IStatus.ERROR, plugin, IStatus.ERROR, message, e);
		ConverterException ce = new ConverterException(status);
		throw ce;
	}

	/**
	 * Throw converter exception.
	 * @param plugin
	 *            the plugin
	 * @param message
	 *            the message
	 * @throws ConverterException
	 *             the converter exception
	 * @author John Zhu
	 * @exception ConverterException
	 */
	public static void throwConverterException(String plugin, String message) throws ConverterException {
		throwConverterException(plugin, message, null);
	}

	/**
	 * 获得当前已处理字节数的计算对象
	 * @param totalSize
	 *            字节总数
	 * @return ;
	 */
	public static CalculateProcessedBytes getCalculateProcessedBytes(long totalSize) {
		return new CalculateProcessedBytes(totalSize);
	}

	/**
	 * 获得处理指定文件进度的计算对象
	 * @param filePath
	 *            文件路径
	 * @return ;
	 */
	public static CalculateProcessedBytes getCalculateProcessedBytes(String filePath) {
		return new CalculateProcessedBytes(filePath);
	}

	/**
	 * 获得记录逆转换过程中的信息记录对象
	 * @return ;
	 */
	public static ReverseConversionInfoLogRecord getReverseConversionInfoLogRecord() {
		return new ReverseConversionInfoLogRecord();
	}
	
	/**
	 * 验证源文件类型是否是 OpenOffice 和 MSOffice 2007。
	 * @param typeName
	 * @return ;
	 */
	public static boolean isOpenOfficeOrMSOffice2007(String type) {
		if (type == null) {
			return false;
		}
		return type.contains("openoffice") || type.contains("msoffice2007") ||type.contains("msoffice2003") ;
	}
}
