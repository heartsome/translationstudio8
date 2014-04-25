/**
 * ConverterUtil.java
 *
 * Version information :
 *
 * Date:2013-10-8
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.tmx.converter;

import java.io.File;
import java.io.IOException;

import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.database.resource.Messages;
import net.heartsome.cat.document.converter.AbstractConverter;
import net.heartsome.cat.document.converter.ConverterFactory;
import net.heartsome.cat.tmx.converter.bean.File2TmxConvertBean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class ConverterUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConverterUtil.class);
	public static File convert2Tmx(String fileName, IProgressMonitor monitor) throws ImportException {
		File file = new File(fileName);
		if (!file.exists()) {
			throw new ImportException(fileName +Messages.getString("converter.ConverterUtil.msg"));
		}

		if (fileName.toLowerCase().endsWith(".tmx")) {
			return null;
		}
		AbstractFile2Tmx file2TmxConverter = ConverterFactory.getFile2TmxConverter(fileName);
		if (null == file2TmxConverter) {
			return null;
		}
		File createTempFile = null;
		boolean hasError = true;
		try {
			createTempFile = File.createTempFile("Tmx_", "" + System.currentTimeMillis() + ".tmx");
			SubProgressMonitor spm = new SubProgressMonitor(monitor, 30);
			File2TmxConvertBean bean = new File2TmxConvertBean();
			bean.sourceFilePath = fileName;
			bean.newTmxFilePath = createTempFile.getAbsolutePath();
			file2TmxConverter.doCovnerter(bean, spm);
			hasError = false;
		} catch (IOException e) {
			LOGGER.error("", e);
			throw new ImportException(e.getMessage().replace("\n", " "));
		} catch (Exception e) {
			LOGGER.error("", e);
			throw new ImportException(e.getMessage().replace("\n", " "));
		} finally {
			if (hasError && null != createTempFile) {
				createTempFile.delete();
			}
		}
		return createTempFile;
	}

	public static File convert2Tbx(String fileName, IProgressMonitor monitor) throws ImportException {
		File file = new File(fileName);
		if (!file.exists()) {
			throw new ImportException(fileName +Messages.getString("converter.ConverterUtil.msg"));
		}
		if (fileName.toLowerCase().endsWith(".tbx")) {
			return null;
		}
		AbstractConverter conveter = ConverterFactory.getFile2TbxConverter(fileName);
		if (null == conveter) {
			return null;
		}
		File createTempFile = null;
		boolean hasError = true;
		monitor.beginTask("", 1);
		try {
			createTempFile = File.createTempFile("Tbx_", "" + System.currentTimeMillis() + ".tbx");
			conveter.doConvert(createTempFile.getAbsolutePath(), new SubProgressMonitor(monitor, 1));
			hasError = false;
		} catch (OperationCanceledException e) {
			throw e;
		} catch (IOException e) {
			LOGGER.error("", e);
			throw new ImportException(e.getMessage().replace("\n", " "));
		} catch (Exception e) {
			LOGGER.error("", e);
			throw new ImportException(e.getMessage().replace("\n", " "));
		} finally {
			if (hasError && null != createTempFile) {
				createTempFile.delete();
			}
			monitor.done();
		}
		return createTempFile;
	}
}
