package net.heartsome.cat.convert.ui.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.handlers.AbstractSelectProjectFilesHandler;
import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.convert.ui.resource.Messages;
import net.heartsome.cat.convert.ui.wizard.ReverseConversionWizard;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.ts.core.file.XLFHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 打开转换文件配置对话框
 * @author weachy
 * @since JDK1.5
 */
public class OpenReverseConversionDialogHandler extends AbstractSelectProjectFilesHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenReverseConversionDialogHandler.class);

	public Object execute(ExecutionEvent event, List<IFile> list) {
		if (list == null || list.size() == 0) {
			MessageDialog.openInformation(HandlerUtil.getActiveWorkbenchWindow(event).getShell(),
					Messages.getString("handler.OpenReverseConversionDialogHandler.msgTitle1"),
					Messages.getString("handler.OpenReverseConversionDialogHandler.msg1"));
			return null;
		}
		
		// 首先验证是否是合并打开的文件 --robert	2012-10-17
		System.out.println(list.get(0).getFullPath().toOSString());
		if (isEditor) {
			// 针对合并打开
			if (list.get(0).getFullPath().toOSString().endsWith(".xlp")) {
				List<String> multiFiles = new XLFHandler().getMultiFiles(list.get(0));
				if (multiFiles.size() > 0) {
					list = new ArrayList<IFile>();
				}
				for (String filePath : multiFiles) {
					list.add(ResourceUtils.fileToIFile(filePath));
				}
			}
		}
		
		List<IFile> lstFiles = new ArrayList<IFile>();
		XLFValidator.resetFlag();
		for (IFile iFile : list) {
			if (!XLFValidator.validateXliffFile(iFile)) {
				lstFiles.add(iFile);
			}
		}
		
		CommonFunction.removeRepeateSelect(list);
		XLFValidator.resetFlag();
		if (!(list instanceof ArrayList)) {
			list = new ArrayList<IFile>(list);
		}
		list.removeAll(lstFiles);
		if (list.size() == 0) {
			return null;
		}
		ArrayList<ConverterViewModel> converterViewModels = new ArrayList<ConverterViewModel>();
		for (int i = 0; i < list.size(); i++) {
			Object adapter = Platform.getAdapterManager().getAdapter(list.get(i), IConversionItem.class);
			IConversionItem sourceItem = null;
			if (adapter instanceof IConversionItem) {
				sourceItem = (IConversionItem) adapter;
			}
			ConverterViewModel converterViewModel = new ConverterViewModel(Activator.getContext(),
					Converter.DIRECTION_REVERSE);
			converterViewModel.setConversionItem(sourceItem); // 记住所选择的文件
			converterViewModels.add(converterViewModel);
		}

		IProject project = list.get(0).getProject();
		ReverseConversionWizard wizard = new ReverseConversionWizard(converterViewModels, project);
		TSWizardDialog dialog = new TSWizardDialog(shell, wizard) {
			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				super.createButtonsForButtonBar(parent);
				getButton(IDialogConstants.FINISH_ID).setText(Messages.getString("handler.OpenConversionDialogHandler.finishLbl"));
			}
		};
		int result = dialog.open();
		if (result == IDialogConstants.OK_ID) {
			final List<ConverterViewModel> models = wizard.getConverterViewModels();

			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.setTaskName(Messages.getString("handler.OpenReverseConversionDialogHandler.task1"));
					monitor.beginTask(Messages.getString("handler.OpenReverseConversionDialogHandler.task2"), models.size());
					for (ConverterViewModel converterViewModel : models) {
						try {
							IProgressMonitor subMonitor = Progress.getSubMonitor(monitor, 1);
							subMonitor.setTaskName(Messages.getString("handler.OpenReverseConversionDialogHandler.task3")
									+ converterViewModel.getConversionItem().getLocation().toOSString());
							converterViewModel.convertWithoutJob(subMonitor);
						} catch (ConverterException e) {
//							Bug #2485:转换文件失败时，提示框显示有问题
							throw new InvocationTargetException(e, e.getMessage());
						}
					}
					monitor.done();
				}
			};
			try {
				new ProgressMonitorDialog(shell).run(true, true, runnable);
			} catch (InvocationTargetException e) {
				MessageDialog.openInformation(shell, Messages.getString("handler.OpenReverseConversionDialogHandler.msgTitle2"), e.getMessage());
				LOGGER.error("", e);
			} catch (InterruptedException e) {
				MessageDialog.openInformation(shell, Messages.getString("handler.OpenReverseConversionDialogHandler.msgTitle2"), e.getMessage());
				LOGGER.error("", e);
			}
		}
		return null;
	}

	@Override
	public String[] getLegalFileExtensions() {
		return CommonFunction.xlfExtesionArray;
	}
}
