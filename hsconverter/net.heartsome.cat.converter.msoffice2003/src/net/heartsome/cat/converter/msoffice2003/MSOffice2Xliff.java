/**
 * MSOffice2Xliff.java
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * The Class MSOffice2Xliff.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class MSOffice2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-msoffice2003";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("msoffice2003.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "MS Office Document to XLIFF Conveter";

	// 内部实现所依赖的转换器
	/** The dependant converter. */
	private Converter dependantConverter;

	/**
	 * for test to initialize depend on converter.
	 */
	public MSOffice2Xliff() {
		dependantConverter = Activator.getOpenOfficeConverter(Converter.DIRECTION_POSITIVE);
	}

	/**
	 * 运行时把所依赖的转换器，在初始化的时候通过构造函数注入。.
	 * @param converter
	 *            the converter
	 */
	public MSOffice2Xliff(Converter converter) {
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
		MSOffice2XliffImpl converter = new MSOffice2XliffImpl();
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
	 * Gets the suffix name.
	 * @param str
	 *            the str
	 * @return the suffix name
	 */
	public static String getSuffixName(String str) {
		String suffixName = ""; //$NON-NLS-1$
		int i = str.lastIndexOf("."); //$NON-NLS-1$
		suffixName = str.substring(i + 1);
		return suffixName;
	}

	/**
	 * Gets the output file path.
	 * @param inputstr
	 *            the inputstr
	 * @return the output file path
	 */
	public static String getOutputFilePath(String inputstr) {
		String suffixPath = ""; //$NON-NLS-1$
		int i = inputstr.lastIndexOf("."); //$NON-NLS-1$
		String suffName = getSuffixName(inputstr);
		suffixPath = inputstr.substring(0, i);
		if (suffName.equalsIgnoreCase("doc")) { //$NON-NLS-1$
			suffixPath = suffixPath + ".odt"; //$NON-NLS-1$
		} else if (suffName.equalsIgnoreCase("xls")) { //$NON-NLS-1$
			suffixPath = suffixPath + ".ods"; //$NON-NLS-1$
		} else if (suffName.equalsIgnoreCase("ppt")) { //$NON-NLS-1$
			suffixPath = suffixPath + ".odp"; //$NON-NLS-1$
		} else if (suffName.equalsIgnoreCase("rtf")) { //$NON-NLS-1$
			suffixPath = suffixPath + ".odt"; //$NON-NLS-1$
		} else {
			suffixPath = suffixPath + "." + suffName;
		}

		return suffixPath;
	}

	/**
	 * Gets the oupt file path.
	 * @param inputstr
	 *            the inputstr
	 * @param suffName
	 *            the suff name
	 * @return the oupt file path
	 */
	public static String getOuptFilePath(String inputstr, String suffName) {
		String suffixPath = ""; //$NON-NLS-1$
		int i = inputstr.lastIndexOf("."); //$NON-NLS-1$
		suffixPath = inputstr.substring(0, i);
		if (suffName.equalsIgnoreCase("doc")) { //$NON-NLS-1$
			suffixPath = suffixPath + ".odt"; //$NON-NLS-1$
		}
		if (suffName.equalsIgnoreCase("xls")) { //$NON-NLS-1$
			suffixPath = suffixPath + ".ods"; //$NON-NLS-1$
		}

		if (suffName.equalsIgnoreCase("ppt")) { //$NON-NLS-1$
			suffixPath = suffixPath + ".odp"; //$NON-NLS-1$
		}
		return suffixPath;
	}

	/**
	 * The Class MSOffice2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class MSOffice2XliffImpl {

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
			Map<String, String> result = new HashMap<String, String>();
			// 在外层添加一个 try{}finally{}，确保 IProgressMonitor 的 done 方法都会调用到。
			try {
				// 把转换操作分为 5 个部分：检查 open office 服务是否可用占 1 个部分，用 open office 转文件占 2 个部分，最后用 xml 转换器转文件占 2 个部分。
				monitor.beginTask(Messages.getString("msoffice2003.MSOffice2Xliff.task1"), 5);
				
				monitor.subTask(Messages.getString("msoffice2003.MSOffice2Xliff.task2"));
				boolean openbyuser = DetectOORunning.isRunning(); // 判断是否手动启动
				boolean configration = false;
				
				// 检查是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("msoffice2003.cancel"));
				}
				monitor.worked(1);

				Hashtable<String, String> ooParams = new Hashtable<String, String>();
				ooParams.put("ooPath", args.get("ooPath"));
				ooParams.put("port", args.get("ooPort"));

				StartupOO startupoo = new StartupOO(ooParams);
				configration = startupoo.detectConfOO();
				if (!configration) {
					ConverterUtils
							.throwConverterException(
									Activator.PLUGIN_ID,
									Messages.getString("msoffice2003.MSOffice2Xliff.msg1"));
				}

				// 先转成OO，OO在转成XLIF
				String inputFileStr = args.get(Converter.ATTR_SOURCE_FILE);
				String suffixName = getSuffixName(inputFileStr);
				File inputFile = new File(inputFileStr);
				String outputstr = getOuptFilePath(inputFileStr, suffixName);
				File outputFile = new File(outputstr);

				MS2OOConverter ms2ooConverter = new MS2OOConverter(ooParams);
				// fixed a bug 917 by john.
				try {
					outputstr = outputFile.getName();
					int i = outputstr.lastIndexOf("."); //$NON-NLS-1$
					String suffixPath = ""; //$NON-NLS-1$
					String prefixPath = ""; //$NON-NLS-1$
					if (i > 0) {
						prefixPath = outputstr.substring(0, i);
						suffixPath = outputstr.substring(i, outputstr.length());
					} else {
						prefixPath = outputstr;
					}
					outputFile = null;
					//File.createTempFile 这个方法必须要求文件名的长度大于3
					if(prefixPath.length()<3){
						prefixPath = "prefixPath"+prefixPath;
					}
					outputFile = File.createTempFile(prefixPath, suffixPath);
					outputstr = outputFile.getAbsolutePath();
					// 检查是否取消操作
					if (monitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("msoffice2003.cancel"));
					}
					monitor.subTask(Messages.getString("msoffice2003.MSOffice2Xliff.task3"));
					ms2ooConverter.startconvert(inputFile, outputFile, openbyuser);
					monitor.worked(2);
				} catch (OperationCanceledException e) {
					throw e;
				} catch (ConverterException e) {
					throw e;
				} catch (Exception e) {
					if (Converter.DEBUG_MODE) {
						e.printStackTrace();
					}

					try {
						openbyuser = false;
						ms2ooConverter.startconvert(inputFile, outputFile, openbyuser);
					} catch (Exception e1) {
						if (Converter.DEBUG_MODE) {
							e1.printStackTrace();
						}

						if (!openbyuser) {
							CloseOOconnection.closeService(ms2ooConverter.getPort());
						}
						ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
								Messages.getString("msoffice2003.MSOffice2Xliff.msg2"), e);
					}
				}

				// 这里将MS－－DOC，XLS，PPT转成 ODT，ODX，ODP文件
				args.put(Converter.ATTR_SOURCE_FILE, outputstr);
				args.put(Converter.ATTR_FORMAT, TYPE_VALUE);
				args.put("isofficefile", "true");
				// 检查是否取消操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("msoffice2003.cancel"));
				}
				monitor.subTask(Messages.getString("msoffice2003.MSOffice2Xliff.task4"));
				result = dependantConverter.convert(args, Progress.getSubMonitor(monitor, 2));
				outputFile.delete();
			} finally {
				monitor.done();
			}
			return result;
		}
	}
}
