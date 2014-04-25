package net.heartsome.cat.convert.ui.model;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.heartsome.cat.bean.Constant;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.action.ConversionCompleteAction;
import net.heartsome.cat.convert.ui.job.JobFactoryFacade;
import net.heartsome.cat.convert.ui.job.JobRunnable;
import net.heartsome.cat.convert.ui.wizard.Messages;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.ConverterTracker;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.ts.core.file.XLFHandler;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在界面上显示转换器列表的 View Model
 * @author cheney,weachy
 * @since JDK1.5
 */
public class ConverterViewModel extends ConverterTracker {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConverterViewModel.class.getName());
	private ConversionConfigBean configBean;
	private IConversionItem conversionItem;

	/**
	 * UI 跟 转换器之间的 View Model
	 * @param bundleContext
	 * @param direction
	 */
	public ConverterViewModel(BundleContext bundleContext, String direction) {
		super(bundleContext, direction);
		configBean = new ConversionConfigBean();
	}

	/**
	 * 获得转换文件时存储配置信息的对象
	 * @return ;
	 */
	public ConversionConfigBean getConfigBean() {
		return configBean;
	}

	@Override
	public Map<String, String> convert(Map<String, String> parameters) {
		return convert();
	}

	/**
	 * 根据用户的选择和配置信息，执行文件的转换功能
	 * @return ;
	 */
	public Map<String, String> convert() {
		System.out.print(getConfigBean().toString());
		// 以用户最后在配置对话框所选择的源文件为准
		JobRunnable runnalbe = new JobRunnable() {

			private Map<String, String> conversionResult;

			public IStatus run(IProgressMonitor monitor) {
				IStatus result = Status.OK_STATUS;
				try {
					conversionResult = convertWithoutJob(monitor);
				} catch (OperationCanceledException e) {
					result = Status.CANCEL_STATUS;
				} catch (ConverterException e) {
					result = e.getStatus();
				} finally {
					ConverterViewModel.this.close();
				}
				return result;
			}

			public void showResults(IStatus status) {
				IAction action = getRunnableCompletedAction(status);
				if (action != null) {
					action.run();
				}
			}

			public IAction getRunnableCompletedAction(IStatus status) {
				return new ConversionCompleteAction("文件转换", status, conversionResult);
			}
		};
		Job conversionJob = JobFactoryFacade.createJob(Display.getDefault(), "conversion job", runnalbe);
		conversionJob.setUser(true);
		conversionJob.setRule(conversionItem.getProject());
		conversionJob.schedule();
		return null;
	}

	/**
	 * 正向转换（只是转换的过程，未放入后台线程，未处理转换结果提示信息）；
	 * @param sourceItem
	 * @param monitor
	 * @return ;
	 * @throws ConverterException
	 */
	public Map<String, String> convertWithoutJob(IProgressMonitor subMonitor) throws ConverterException {
		if (getDirection().equals(Converter.DIRECTION_POSITIVE)) {
			return convert(conversionItem, subMonitor);
		} else {
			return reverseConvert(conversionItem, subMonitor);
		}
	}

	/**
	 * 正向转换
	 * @param sourceItem
	 * @param monitor
	 * @return ;
	 */
	private Map<String, String> convert(final IConversionItem sourceItem, IProgressMonitor monitor)
			throws ConverterException {
		Map<String, String> result = null;

		String sourcePathStr = configBean.getSource();
		String targetFile = ConverterUtil.toLocalPath(configBean.getTarget());
		String skeletonFile = ConverterUtil.toLocalPath(configBean.getSkeleton());
		boolean convertSuccessful = false;
		File target = null; // 正向转换的目标文件，即 XLIFF 文件
		File skeleton = null; // 骨架文件
		try {
			target = new File(targetFile);
			if (!target.exists()) {
				try {
					File parent = target.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					target.createNewFile();
				} catch (IOException e) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("创建目标文件：" + targetFile + "失败。", e);
					}
				}
			}
			skeleton = new File(skeletonFile);
			if (!skeleton.exists()) {
				try {
					File parent = skeleton.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					skeleton.createNewFile();
				} catch (IOException e) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("创建骨架文件：" + skeletonFile + "失败。", e);
					}
				}
			}
			ConverterContext converterContext = new ConverterContext(configBean);
			final Map<String, String> configuration = converterContext.getConvertConfiguration();
			Converter converter = getConverter();
			if (converter == null) {
				// Build a message
				String message = "此源文件类型对应的转换器不存在：" + configBean.getFileType();
				// Build a new IStatus
				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
				throw new ConverterNotFoundException(status);
			}
			result = converter.convert(configuration, monitor);
			convertSuccessful = true;
		} catch (OperationCanceledException e) {
			// 捕获用户取消操作的异常
			LOGGER.info("用户取消操作。", e);
			throw e;
		} catch (ConverterException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("转换文件:" + sourcePathStr + " 失败。", e);
			}
			throw e;
		} finally {
			// 在转换失败或用户取消转换时，清除目标文件和骨架文件
			if (!convertSuccessful) {
				if (target != null && target.exists()) {
					target.delete();
				}
				if (skeleton != null && skeleton.exists()) {
					skeleton.delete();
				}
			}
			sourceItem.refresh();
		}
		return result;
	}

	/**
	 * 逆向转换
	 * @param sourceItem
	 * @param monitor
	 * @return ;
	 */
	private Map<String, String> reverseConvert(IConversionItem sourceItem, IProgressMonitor monitor)
			throws ConverterException {
		Map<String, String> result = null;
		String sourcePathStr = configBean.getSource();
		String targetFile = ConverterUtil.toLocalPath(configBean.getTarget());
		boolean convertSuccessful = false;
		File target = null;
		try {
			target = new File(targetFile);
			if (!target.exists()) {
				try {
					File parent = target.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					target.createNewFile();
				} catch (IOException e) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("创建目标文件：" + targetFile + "失败。", e);
					}
				}
			}
			Converter converter = getConverter();
			if (converter == null) {
				// Build a message
				String message = "xliff 对应的源文件类型的转换器不存在：" + configBean.getFileType();
				// Build a new IStatus
				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
				throw new ConverterNotFoundException(status);
			}
			ConverterContext converterContext = new ConverterContext(configBean);
			final Map<String, String> configuration = converterContext.getReverseConvertConfiguraion();
			result = converter.convert(configuration, monitor);
			convertSuccessful = true;
		} catch (OperationCanceledException e) {
			// 捕获用户取消操作的异常
			LOGGER.info("用户取消操作。", e);
			throw e;
		} catch (ConverterNotFoundException e) {
			// Let the StatusManager handle the Status and provide a hint
			StatusManager.getManager().handle(e.getStatus(), StatusManager.LOG | StatusManager.SHOW);
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getMessage(), e);
			}
			throw e;
		} catch (ConverterException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("转换文件:{}失败。", sourcePathStr, e);
			}
			throw e;
		} finally {
			if (!convertSuccessful) {
				// 在转换失败或用户取消转换时，清除目标文件和骨架文件
				if (target != null && target.exists()) {
					target.delete();
				}
			}
			sourceItem.refresh();
		}
		return result;
	}

	/**
	 * 验证
	 * @return ;
	 */
	public IStatus validate() {
		if (direction.equals(Converter.DIRECTION_POSITIVE)) {
			return validateConversion();
		} else {
			return validateReverseConversion();
		}
	}

	/**
	 * 逆向转换验证
	 * @return ;
	 */
	private IStatus validateReverseConversion() {
		return configBean.validateReverseConversion();
	}

	/**
	 * 验证 xliff 所需要转换的 xliff 文件
	 * @param xliffPath
	 *            xliff 文件路径
	 * @param monitor
	 * @return ;
	 */
	public IStatus validateXliffFile(String xliffPath, XLFHandler handler, IProgressMonitor monitor) {
		IStatus result = new ReverseConversionValidateWithLibrary3().validate(xliffPath, configBean, monitor);
		if (!result.isOK()) {
			return result;
		}
		// 验证是否存在 xliff 对应的源文件类型的转换器实现
		String fileType = configBean.getFileType();
		Converter converter = getConverter(fileType);
		if (converter != null) {
			result = validateFileNodeInXliff(handler, xliffPath, converter.getType()); // 验证 XLIFF 文件的 file 节点
			if (!result.isOK()) {
				return result;
			}

			setSelectedType(fileType);
			result = Status.OK_STATUS;
		} else {
			result = ValidationStatus.error("xliff 对应的源文件类型的转换器不存在：" + fileType);
		}
		return result;
	}

	/**
	 * 验证 XLIFF 文件中的 file 节点
	 * @param handler
	 *            XLFHandler 实例
	 * @param xliffPath
	 *            XLIFF 文件路径
	 * @param type
	 *            文件类型，（Converter.getType() 的值，XLIIF 文件 file 节点的 datatype 属性值）
	 * @return ;
	 */
	private IStatus validateFileNodeInXliff(XLFHandler handler, String xliffPath, String type) {
		handler.reset(); // 重置，以便重新使用。
		Map<String, Object> resultMap = handler.openFile(xliffPath);
		if (resultMap == null
				|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap.get(Constant.RETURNVALUE_RESULT)) {
			// 打开文件失败。
			return ValidationStatus.error(Messages.getString("ConverterViewModel.0")); //$NON-NLS-1$
		}
		try {
			int fileCount = handler.getFileCountInXliff(xliffPath);
			if (fileCount < 1) { // 不存在 file 节点。提示为不合法的 XLIFF
				return ValidationStatus.error(Messages.getString("ConverterViewModel.0")); //$NON-NLS-1$
			} else if (fileCount > 1) { // 多个 file 节点，提示分割。
				if (ConverterUtils.isOpenOfficeOrMSOffice2007(type)) {
					// 验证源文件是 OpenOffice 和 MSOffice 2007 的XLIFF 的 file 节点信息是否完整
					return validateOpenOfficeOrMSOffice2007(handler, xliffPath);
				}
				return ValidationStatus.error(Messages.getString("ConverterViewModel.1")); //$NON-NLS-1$
			} else { // 只有一个 file 节点
				return Status.OK_STATUS;
			}
		} catch (Exception e) { // 当前打开了多个 XLIFF 文件，参看 XLFHandler.getFileCountInXliff() 方法
			return ValidationStatus.error(Messages.getString("ConverterViewModel.1")); //$NON-NLS-1$
		}
	}

	/**
	 * 验证源文件是 OpenOffice 和 MSOffice 2007 的XLIFF 的 file 节点信息是否完整
	 * @param handler
	 * @param path
	 * @return ;
	 */
	private IStatus validateOpenOfficeOrMSOffice2007(XLFHandler handler, String path) {
		if (!handler.validateMultiFileNodes(path)) {
			return ValidationStatus.error("XLIFF 文件中 “document” 属性组（prop-group）信息缺失或者不完整，无法转换。");
		}
		return Status.OK_STATUS;
	}

	/**
	 * 正向转换验证
	 * @return ;
	 */
	private IStatus validateConversion() {
		if (selectedType == null || selectedType.trim().equals("")) {
			return ValidationStatus.error("请选择文件类型。");
		}
		return configBean.validateConversion();
	}

	/**
	 * 在导航视图中所选择的文件
	 * @param file
	 *            ;
	 */
	public void setConversionItem(IConversionItem file) {
		this.conversionItem = file;
	}

	/**
	 * 在导航视图中所选择的文件
	 * @return ;
	 */
	public IConversionItem getConversionItem() {
		return conversionItem;
	}

}
