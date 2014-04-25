/**
 * Xliff2MSOffice.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msoffice2003;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.ms2oo.CloseOOconnection;
import net.heartsome.cat.converter.ms2oo.DetectOORunning;
import net.heartsome.cat.converter.ms2oo.MS2OOConverter;
import net.heartsome.cat.converter.ms2oo.StartupOO;
import net.heartsome.cat.converter.msoffice2003.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.util.ReverseConversionInfoLogRecord;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Xliff2MSOffice.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Xliff2MSOffice implements Converter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Xliff2MSOffice.class);

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-msoffice2003";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("msoffice2003.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to MS Office Document Conveter";

	// 内部实现所依赖的转换器
	/** The dependant converter. */
	private Converter dependantConverter;

	/**
	 * for test to initialize depend on converter.
	 */
	public Xliff2MSOffice() {
		dependantConverter = Activator.getOpenOfficeConverter(Converter.DIRECTION_REVERSE);
	}

	/**
	 * 运行时把所依赖的转换器，在初始化的时候通过构造函数注入。.
	 * @param converter
	 *            the converter
	 */
	public Xliff2MSOffice(Converter converter) {
		dependantConverter = converter;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2MSOfficeImpl converter = new Xliff2MSOfficeImpl();
		return converter.run(args, monitor);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getName()
	 * @return
	 */
	public String getName() {
		return NAME_VALUE;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getType()
	 * @return
	 */
	public String getType() {
		return TYPE_VALUE;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getTypeName()
	 * @return
	 */
	public String getTypeName() {
		return TYPE_NAME_VALUE;
	}

	/**
	 * The Class Xliff2MSOfficeImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2MSOfficeImpl {
		private boolean isInfoEnabled = LOGGER.isInfoEnabled();

		/**
		 * Run.
		 * @param args
		 *            the args
		 * @param monitor
		 *            the monitor
		 * @return the map< string, string>
		 * @throws ConverterException
		 *             the converter exception
		 */
		public Map<String, String> run(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			ReverseConversionInfoLogRecord infoLogger = ConverterUtils.getReverseConversionInfoLogRecord();
			infoLogger.startConversion();
			Map<String, String> result = new HashMap<String, String>();
			String backfile = args.get(Converter.ATTR_TARGET_FILE);
			String middleBackfile = MSOffice2Xliff.getOutputFilePath(backfile);
			// 先转换成ODT,ODS,ODP等文件,然后在转换成DOC,XLS,PPT
			args.put(Converter.ATTR_TARGET_FILE, middleBackfile);
			try {
				// 把转换过程分为三部分共 10 个任，其中委派其它转换的操作占 4，检测 Open Office Service 是否可用占 2，调用 Open Office Service 进行的操作占 4。
				monitor.beginTask(Messages.getString("msoffice2003.Xliff2MSOffice.task1"), 10);
				long startTime = 0;
				if (isInfoEnabled) {
					startTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("msoffice2003.Xliff2MSOffice.logger1"), startTime);
				}
				result = dependantConverter.convert(args, Progress.getSubMonitor(monitor, 4));
				long endTime = System.currentTimeMillis();
				if (isInfoEnabled) {
					LOGGER.info(Messages.getString("msoffice2003.Xliff2MSOffice.logger2"), endTime);
					LOGGER.info(Messages.getString("msoffice2003.Xliff2MSOffice.logger3"), endTime - startTime);
				}

				// 是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("msoffice2003.cancel"));
				}
				monitor.subTask(Messages.getString("msoffice2003.Xliff2MSOffice.task2"));
				if (isInfoEnabled) {
					startTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("msoffice2003.Xliff2MSOffice.logger4"), startTime);
				}
				File backup = new File(backfile);
				File middleFile = new File(middleBackfile);
				boolean openbyuser = DetectOORunning.isRunning(); // 判断是否手动启动
				boolean configration = false;
				Hashtable<String, String> ooParams = new Hashtable<String, String>();
				ooParams.put("ooPath", args.get("ooPath"));
				ooParams.put("port", args.get("ooPort"));
				StartupOO startupoo = new StartupOO(ooParams);
				configration = startupoo.detectConfOO();
				if (!configration) {
					ConverterUtils
							.throwConverterException(
									Activator.PLUGIN_ID,
									Messages.getString("msoffice2003.Xliff2MSOffice.msg1"));
				}
				if (isInfoEnabled) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("msoffice2003.Xliff2MSOffice.logger5"), endTime);
					LOGGER.info(Messages.getString("msoffice2003.Xliff2MSOffice.logger6"), endTime - startTime);
				}
				monitor.worked(2);

				// 是否取消操作
				monitor.subTask(Messages.getString("msoffice2003.Xliff2MSOffice.task3"));
				if (isInfoEnabled) {
					startTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("msoffice2003.Xliff2MSOffice.logger7"), startTime);
				}
				MS2OOConverter ms2ooConverter = new MS2OOConverter(ooParams);
				try {
					ms2ooConverter.startconvert(middleFile, backup, openbyuser);
				} catch (Exception e) {
					if (Converter.DEBUG_MODE) {
						e.printStackTrace();
					}
					try {
						openbyuser = false;
						ms2ooConverter.startconvert(middleFile, backup, openbyuser);
					} catch (Exception e1) {
						if (Converter.DEBUG_MODE) {
							e1.printStackTrace();
						}
						if (!openbyuser) {
							CloseOOconnection.closeService(ms2ooConverter.getPort());
						}
						ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
								Messages.getString("msoffice2003.Xliff2MSOffice.msg2"), e);
					}
				}
				middleFile.delete();
				if (isInfoEnabled) {
					endTime = System.currentTimeMillis();
					LOGGER.info(Messages.getString("msoffice2003.Xliff2MSOffice.logger8"), endTime);
					LOGGER.info(Messages.getString("msoffice2003.Xliff2MSOffice.logger9"), endTime - startTime);
				}
				monitor.worked(4);
			} catch (OperationCanceledException e) {
				throw e;
			} catch (ConverterException e) {
				throw e;
			} catch (Exception e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
						Messages.getString("msoffice2003.Xliff2MSOffice.msg2"), e);
			} finally {
				monitor.done();
			}
			result.put(Converter.ATTR_TARGET_FILE, backfile);
			infoLogger.endConversion();
			return result;
		}
	}
}
