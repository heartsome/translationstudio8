package net.heartsome.cat.convert.ui.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.action.ConversionCompleteAction;
import net.heartsome.cat.convert.ui.job.JobFactoryFacade;
import net.heartsome.cat.convert.ui.job.JobRunnable;
import net.heartsome.cat.convert.ui.resource.Messages;
import net.heartsome.cat.convert.ui.utils.FileFormatUtils;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.ConverterTracker;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.preferencepage.translation.ITranslationPreferenceConstants;
import net.heartsome.util.TextUtil;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * 在界面上显示转换器列表的 View Model
 * @author cheney,weachy
 * @since JDK1.5
 */
public class ConverterViewModel extends ConverterTracker {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConverterViewModel.class.getName());
	private ConversionConfigBean configBean;
	private IConversionItem conversionItem;
	
	private List<File> generateTgtFileList = new ArrayList<File>();

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
		// System.out.print(getConfigBean().toString());
		// 以用户最后在配置对话框所选择的源文件为准
		JobRunnable runnalbe = new JobRunnable() {

			private Map<String, String> conversionResult;

			public IStatus run(IProgressMonitor monitor) {
				IStatus result = Status.OK_STATUS;
				try {
					conversionResult = convertWithoutJob(monitor);
				} catch (OperationCanceledException e) {
					LOGGER.info(Messages.getString("model.ConverterViewModel.logger2"), e);
					result = Status.CANCEL_STATUS;
				} catch (ConverterException e) {
					String msg = Messages.getString("model.ConverterViewModel.logger3");
					Object[] args = { getConfigBean().getSource() };
					LOGGER.error(new MessageFormat(msg).format(args), e);
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
				return new ConversionCompleteAction(Messages.getString("model.ConverterViewModel.msg1"), status,
						conversionResult);
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
		boolean convertFlg = false;
		
		String xliffDir = ConverterUtil.toLocalPath(configBean.getXliffDir());
		String targetFile = ConverterUtil.toLocalPath(configBean.getTarget());
		String skeletonFile = ConverterUtil.toLocalPath(configBean.getSkeleton());
		
		// 转换前的准备
		ConverterContext converterContext = new ConverterContext(configBean);
		final Map<String, String> configuration = converterContext.getConvertConfiguration();

		// 转换前，生成临时的XLIFF文件，用此文件生成指定目标语言的XLIFF文件
		File targetTempFile = null;
		try {
			targetTempFile = File.createTempFile("tempxlf", "xlf");
		} catch (IOException e) {
			LOGGER.error(Messages.getString("model.ConverterViewModel.msg10"), e);
		}
		configuration.put(Converter.ATTR_XLIFF_FILE, targetTempFile.getAbsolutePath());

		if (configBean.getFileType().equals(FileFormatUtils.MS)) {
			IPreferenceStore ps = net.heartsome.cat.ts.ui.Activator.getDefault().getPreferenceStore();
			String path = ps.getString(ITranslationPreferenceConstants.PATH_OF_OPENOFFICE);
			String port = ps.getString(ITranslationPreferenceConstants.PORT_OF_OPENOFFICE);
			configuration.put("ooPath", path);
			configuration.put("ooPort", port);
		}
		
		// 创建skeleton文件
		File skeleton = new File(skeletonFile);
		if (!skeleton.exists()) {
			try {
				File parent = skeleton.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				skeleton.createNewFile();
			} catch (IOException e) {
				String message = MessageFormat.format(Messages.getString("model.ConverterViewModel.msg11"), skeletonFile);
				LOGGER.error(message, e);
				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message+"\n"+e.getMessage());
				throw new ConverterException(status);
			}
		}

		try {
			// 执行转换
			Converter converter = getConverter();
			if (converter == null) {
				// Build a message
				String message = Messages.getString("model.ConverterViewModel.msg2") + configBean.getFileType();
				// Build a new IStatus
				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
				throw new ConverterException(status);
			}
			result = converter.convert(configuration, monitor);
			final String alert= result.get("ttx2xlfAlert39238409230481092830");
			if (result.containsKey("ttx2xlfAlert39238409230481092830")) {//ttx 转 xlf 时，提示含有未预翻译，不推荐，但没办法。
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.getString("handler.PreviewTranslationHandler.msgTitle"), alert);
					}
				});
			}
			// 处理骨架文件，将骨架文件路径修改为项目相对路径，此路径写入external-file节点的href属性
			String projectPath = sourceItem.getProject().getLocation().toOSString();
			String sklPath = skeletonFile.replace(projectPath, "");

			// 处理目标语言, 创建多个目标语言的文件
			List<Language> tgtLang = configBean.getHasSelTgtLangList();
			if (tgtLang != null && tgtLang.size() > 0) {
				// 解析XLIFF文件
				File f = new File(targetTempFile.getAbsolutePath());
				FileInputStream is = null;
				byte[] b = new byte[(int) f.length()];
				try {
					is = new FileInputStream(f);
					is.read(b);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				VTDGen vg = new VTDGen();
				vg.setDoc(b);
				try {
					vg.parse(true);
				} catch (VTDException e) {
					String message = Messages.getString("model.ConverterViewModel.msg12");
					LOGGER.error(message, e);
					IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message+"\n"+e.getMessage());
					throw new ConverterException(status);
				}
				VTDNav vn = vg.getNav();
				VTDUtils vu = new VTDUtils();

				// 生成多个XLIFF文件，只是修改目标语言和骨架文件路径
				for (Language lang : tgtLang) {
					
					// 修复　bug 2949 ,当文件名中出现　XLIFF 时，文件名获取失败，下面注释代码为之前的代码。	--robert	2013-04-01
//					String[] pathArray = targetFile.split(Constant.FOLDER_XLIFF);
//					StringBuffer xlffPath = new StringBuffer(pathArray[0]);
//					xlffPath.append(Constant.FOLDER_XLIFF).append(File.separator).append(lang.getCode())
//							.append(pathArray[1]);
					String fileName = targetFile.substring(xliffDir.length());
					StringBuffer xlfPahtSB = new StringBuffer();
					xlfPahtSB.append(xliffDir);
					xlfPahtSB.append(File.separator);
					xlfPahtSB.append(lang.getCode());
					xlfPahtSB.append(fileName);
					
					
					
					File tmpFile = new File(xlfPahtSB.toString());
					generateTgtFileList.add(tmpFile);
					if (!tmpFile.exists()) {
						File parent = tmpFile.getParentFile();
						if (!parent.exists()) {
							parent.mkdirs();
						}
						try {
							tmpFile.createNewFile();
						} catch (IOException e) {
							String message = Messages.getString("model.ConverterViewModel.msg13");
							LOGGER.error(message, e);
							IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message+"\n"+e.getMessage());
							throw new ConverterException(status);
						}
					}
					try {
						vu.bind(vn.duplicateNav());
					} catch (NavException e) {
						LOGGER.error("", e);
					}
					XMLModifier xm = vu.update("/xliff/file/@target-language", lang.getCode(),
							VTDUtils.CREATE_IF_NOT_EXIST);
					xm = vu.update(null, xm, "/xliff/file/header/skl/external-file/@href",
							TextUtil.cleanString(sklPath));

					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(tmpFile);
						xm.output(fos); // 写入文件
					} catch (Exception e) {
						String message = Messages.getString("model.ConverterViewModel.msg13");
						LOGGER.error(message, e);
						IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message+"\n"+e.getMessage());
						throw new ConverterNotFoundException(status);
					} finally {
						if (fos != null) {
							try {
								fos.close();
							} catch (IOException e) {
								LOGGER.error("",e);
							}
						}
					}
				}
			vg.clear();
			}
			convertFlg = true;
		} catch (OperationCanceledException e) {
			LOGGER.info("ConverterViewerModel: 取消转换");
		} finally {
			if (!convertFlg) {
				for (File f : generateTgtFileList) {
					if (f != null && f.exists()) {
						f.delete();
					}
				}
				if(skeleton != null && skeleton.exists()){
					skeleton.delete();
				}
			}
			targetTempFile.delete();
			sourceItem.refresh();
		}
		return result;
	}

	/**
	 * 获取转换后生成的多个目标文件
	 * @return ;
	 */
	public List<File> getGenerateTgtFileList(){
		return this.generateTgtFileList;
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
						String msg = Messages.getString("model.ConverterViewModel.logger4");
						Object[] args = { targetFile };
						LOGGER.error(new MessageFormat(msg).format(args), e);
					}
				}
			}
			Converter converter = getConverter();
			if (converter == null) {
				// Build a message
				String message = Messages.getString("model.ConverterViewModel.msg3") + configBean.getFileType();
				// Build a new IStatus
				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
				throw new ConverterNotFoundException(status);
			}
			
			ConverterContext converterContext = new ConverterContext(configBean);
			final Map<String, String> configuration = converterContext.getReverseConvertConfiguraion();
			if (configBean.getFileType().equals("x-msoffice2003")) {
				IPreferenceStore ps = net.heartsome.cat.ts.ui.Activator.getDefault().getPreferenceStore();
				String path = ps.getString(ITranslationPreferenceConstants.PATH_OF_OPENOFFICE);
				String port = ps.getString(ITranslationPreferenceConstants.PORT_OF_OPENOFFICE);
				configuration.put("ooPath", path);
				configuration.put("ooPort", port);
			}
			result = converter.convert(configuration, monitor);
			convertSuccessful = true;
		} catch (OperationCanceledException e) {
			// 捕获用户取消操作的异常
			LOGGER.info(Messages.getString("model.ConverterViewModel.logger2"), e);
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
				LOGGER.error(
						MessageFormat.format(Messages.getString("model.ConverterViewModel.logger3"), sourcePathStr), e);
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

			result = validIsSplitedXliff(handler, xliffPath);
			if (!result.isOK()) {
				return result;
			}
			setSelectedType(fileType);
			result = Status.OK_STATUS;
		} else {
			result = ValidationStatus.error(Messages.getString("model.ConverterViewModel.msg3") + fileType);
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
			return ValidationStatus.error(Messages.getString("model.ConverterViewModel.msg4")); //$NON-NLS-1$
		}
		try {
			int fileCount = handler.getFileCountInXliff(xliffPath);
			if (fileCount < 1) { // 不存在 file 节点。提示为不合法的 XLIFF
				return ValidationStatus.error(Messages.getString("model.ConverterViewModel.msg4")); //$NON-NLS-1$
			} else if (fileCount > 1) { // 多个 file 节点，提示分割。
				if (ConverterUtils.isOpenOfficeOrMSOffice2007(type)) {
					// 验证源文件是 OpenOffice 和 MSOffice 2007 的XLIFF 的 file 节点信息是否完整
					return validateOpenOfficeOrMSOffice2007(handler, xliffPath);
				}
				return ValidationStatus.error(Messages.getString("model.ConverterViewModel.msg5")); //$NON-NLS-1$
			} else { // 只有一个 file 节点
				return Status.OK_STATUS;
			}
		} catch (Exception e) { // 当前打开了多个 XLIFF 文件，参看 XLFHandler.getFileCountInXliff() 方法
			return ValidationStatus.error(Messages.getString("model.ConverterViewModel.msg5")); //$NON-NLS-1$
		}
	}

	/**
	 * 验证是否是分割文件，若是，提示不允许通过 robert 2012-06-09
	 * @param handler
	 * @param xliffPath
	 * @return
	 */
	private IStatus validIsSplitedXliff(XLFHandler handler, String xliffPath) {
		if (!handler.validateSplitedXlf(xliffPath)) {
			return ValidationStatus.error(Messages.getString("model.ConverterViewModel.msg6"));
		}
		return Status.OK_STATUS;
	}

	/**
	 * 验证源文件是 OpenOffice 和 MSOffice 2007 的XLIFF 的 file 节点信息是否完整
	 * @param handler
	 * @param path
	 * @return ;
	 */
	private IStatus validateOpenOfficeOrMSOffice2007(XLFHandler handler, String path) {
		if (!handler.validateMultiFileNodes(path)) {
			return ValidationStatus.error(Messages.getString("model.ConverterViewModel.msg7"));
		}
		return Status.OK_STATUS;
	}

	/**
	 * 正向转换验证
	 * @return ;
	 */
	private IStatus validateConversion() {
		if (selectedType == null || selectedType.trim().equals("")) {
			return ValidationStatus.error(Messages.getString("model.ConverterViewModel.msg8"));
		}
		if (configBean.getFileType().equals(FileFormatUtils.MS)) {
			IPreferenceStore ps = net.heartsome.cat.ts.ui.Activator.getDefault().getPreferenceStore();
			boolean enableOO = ps.getBoolean(ITranslationPreferenceConstants.ENABLED_OF_OPENOFFICE);
			if (!enableOO) {
				return ValidationStatus.error(Messages.getString("model.ConverterViewModel.msg9"));
			}
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
