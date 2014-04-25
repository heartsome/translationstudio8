package net.heartsome.cat.converter.javascript;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.javascript.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 *Jscript 转 xliff 的具体实现
 * @author cheney
 * @since JDK1.6
 */
class Jscript2xliffImpl extends Jscript2xliffAbstract {

	/**
	 * 构造函数
	 * @param params
	 *            转换所需的配置信息
	 * @throws FileNotFoundException
	 *             如果所需转换的文件找不到，则抛此异常
	 * @throws UnsupportedEncodingException
	 *             如果以平台不支持的编码读取文件字符流时，则抛此异常
	 */
	public Jscript2xliffImpl(Map<String, String> params) throws FileNotFoundException, UnsupportedEncodingException {
		inputFile = params.get(Converter.ATTR_SOURCE_FILE);
		xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
		skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
		sourceLanguage = params.get(Converter.ATTR_SOURCE_LANGUAGE);
		targetLanguage = params.get(Converter.ATTR_TARGET_LANGUAGE);
		// fixed a bug 1293 by john.
		encoding = params.get(Converter.ATTR_SOURCE_ENCODING);

		input = new InputStreamReader(new FileInputStream(inputFile), encoding);
		buffer = new BufferedReader(input);

		output = new FileOutputStream(xliffFile);

		skeleton = new FileOutputStream(skeletonFile);
		if (Converter.TRUE.equalsIgnoreCase(params.get(Converter.ATTR_IS_SUITE))) {
			isSuite = true;
		}
		qtToolId = params.get(Converter.ATTR_QT_TOOLID) != null ? params.get(Converter.ATTR_QT_TOOLID)
				: Converter.QT_TOOLID_DEFAULT_VALUE;
	}

	/**
	 * 转换
	 * @param params
	 *            转换所需的配置信息
	 * @param monitor
	 *            监视器
	 * @return 转换后的 xliff 文件
	 * @throws ConverterException
	 *             在转换过程中出错，则抛此异常 ;
	 */
	public static Map<String, String> run(Map<String, String> params, IProgressMonitor monitor)
			throws ConverterException {
		Map<String, String> result = null;
		try {
			result = new Jscript2xliffImpl(params).run(monitor);
		} catch (OperationCanceledException e) {
			throw e;
		} catch (ConverterException e) {
			throw e;
		} catch (Exception e) {
			if (Converter.DEBUG_MODE) {
				e.printStackTrace();
			}
			ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("javascript.Jscript2xliffImpl.msg"), e);
		}
		return result;
	}

}
