/**
 * IConverterCallerImpl.java
 *
 * Version information :
 *
 * Date:2012-6-25
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.convert.ui.extension;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.convert.ui.resource.Messages;
import net.heartsome.cat.convert.ui.wizard.ConversionWizard;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.ts.ui.extensionpoint.IConverterCaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class IConverterCallerImpl implements IConverterCaller {

	private static final Logger LOGGER = LoggerFactory.getLogger(IConverterCallerImpl.class);

	/**
	 * 
	 */
	public IConverterCallerImpl() {
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.extensionpoint.IConverterCaller#openConverter(java.util.List)
	 */
	public void openConverter(List<IFile> files) {
		if (files == null || files.size() == 0) {
			return;
		}

		final Shell shell = Display.getCurrent().getActiveShell();

		ArrayList<IFile> list = new ArrayList<IFile>();
		ArrayList<IFile> wrongFiles = new ArrayList<IFile>();
		for (IFile file : files) {
			String fileExtension = file.getFileExtension();
			if (getLegalFileExtensions() == null || getLegalFileExtensions().length == 0) { // 未限制后缀名的情况
				list.add(file);
			} else { // 限制了后缀名的情况
				if (fileExtension == null) { // 无后缀名的文件
					fileExtension = "";
				}
				if (CommonFunction.containsIgnoreCase(getLegalFileExtensions(), fileExtension)) {
					list.add(file);
				} else {
					wrongFiles.add(file);
				}
			}
		}
		if (!wrongFiles.isEmpty()) {
			StringBuffer msg = new StringBuffer(Messages.getString("extension.IConverterCallerImpl.msg1"));
			for (IFile iFile : wrongFiles) {
				msg.append("\n").append(iFile.getFullPath().toOSString());
			}
			if (!MessageDialog.openConfirm(shell, Messages.getString("extension.IConverterCallerImpl.msgTitle1"),
					msg.toString())) {
				return;
			}
		}

		ArrayList<ConverterViewModel> converterViewModels = new ArrayList<ConverterViewModel>();
		for (int i = 0; i < list.size(); i++) {
			Object adapter = Platform.getAdapterManager().getAdapter(list.get(i), IConversionItem.class);
			IConversionItem sourceItem = null;
			if (adapter instanceof IConversionItem) {
				sourceItem = (IConversionItem) adapter;
			}
			ConverterViewModel converterViewModel = new ConverterViewModel(Activator.getContext(),
					Converter.DIRECTION_POSITIVE);
			converterViewModel.setConversionItem(sourceItem); // 记住所选择的文件
			converterViewModels.add(converterViewModel);
		}

		IProject project = list.get(0).getProject();
		ConversionWizard wizard = new ConversionWizard(converterViewModels, project);
		TSWizardDialog dialog = new TSWizardDialog(shell, wizard);
		int result = dialog.open();
		if (result == IDialogConstants.OK_ID) {
			final List<ConverterViewModel> models = wizard.getConverterViewModels();

			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.setTaskName(Messages.getString("extension.IConverterCallerImpl.task1"));
					monitor.beginTask(Messages.getString("extension.IConverterCallerImpl.task2"), models.size());
					for (ConverterViewModel converterViewModel : models) {
						try {
							IProgressMonitor subMonitor = Progress.getSubMonitor(monitor, 1);
							subMonitor.setTaskName(Messages.getString("extension.IConverterCallerImpl.task3")
									+ converterViewModel.getConversionItem().getLocation().toOSString());
							converterViewModel.convertWithoutJob(subMonitor);
						} catch (ConverterException e) {
							final String message = e.getMessage();
							LOGGER.error("", e);
							Display.getDefault().syncExec(new Runnable() {

								public void run() {
									MessageDialog.openInformation(shell,
											Messages.getString("extension.IConverterCallerImpl.msgTitle2"), message);
								}
							});
						} catch (Exception e) {
							final String message = e.getMessage();
							LOGGER.error("", e);
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									MessageDialog.openInformation(shell,
											Messages.getString("extension.IConverterCallerImpl.msgTitle2"), message);
								}
							});
						}
					}
					monitor.done();
				}
			};
			try {
				new ProgressMonitorDialog(shell).run(true, true, runnable);
			} catch (InvocationTargetException e) {
				MessageDialog.openInformation(shell, Messages.getString("extension.IConverterCallerImpl.msgTitle2"),
						e.getMessage());
				LOGGER.error("", e);
			} catch (InterruptedException e) {
				MessageDialog.openInformation(shell, Messages.getString("extension.IConverterCallerImpl.msgTitle2"),
						e.getMessage());
				LOGGER.error(e.getMessage());
			}
		}
	}
    /**
     * 修改支持的文件扩展名字
     * @return ;
     */
	private String[] getLegalFileExtensions() {
		// return new String[] { "html","htm", "inx", "properties", "js", "mif", "doc", "ppt", "xls", "docx", "xlsx",
		// "pptx",
		// "odg", "ods", "odt", "ods", "odt", "po", "rc", "resx", "rtf", "txt", "ttx", "xml" };

		return new String[] { "html", "htm", "inx", "properties", "js", "mif", "doc", "ppt", "xls", "docx", "xlsx",
				"pptx", "odg", "ods", "odt", "ods", "odt", "po", "rc", "resx", "rtf", "txt", "ttx", "xml", "sdlxliff",
				"xlf", "idml", "mqxlz", "txml" };
	}
}
