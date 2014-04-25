package net.heartsome.cat.convert.ui.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.ui.handlers.AbstractSelectProjectFilesHandler;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.convert.ui.wizard.ReverseConversionWizard;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.Progress;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;

/**
 * 打开转换文件配置对话框
 * @author weachy
 * @since JDK1.5
 */
public class OpenReverseConversionDialogHandler extends AbstractSelectProjectFilesHandler {

	public Object execute(ExecutionEvent event, List<IFile> list) {
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
		WizardDialog dialog = new WizardDialog(shell, wizard);

		int result = dialog.open();
		if (result == IDialogConstants.OK_ID) {
			final List<ConverterViewModel> models = wizard.getConverterViewModels();

			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.setTaskName("转换项目");
					monitor.beginTask("转换项目中的 XLIFF 为目标文件...", models.size());
					for (ConverterViewModel converterViewModel : models) {
						try {
							IProgressMonitor subMonitor = Progress.getSubMonitor(monitor, 1);
							subMonitor.setTaskName("开始转换文件："
									+ converterViewModel.getConversionItem().getLocation().toOSString());
							converterViewModel.convertWithoutJob(subMonitor);
						} catch (Exception e) {
							MessageDialog.openError(shell, "文件转换失败", e.getMessage());
							e.printStackTrace();
						}
					}
					monitor.done();
				}
			};
			try {
				new ProgressMonitorDialog(shell).run(true, true, runnable);
			} catch (InvocationTargetException e) {
				MessageDialog.openError(shell, "文件转换失败", e.getMessage());
				e.printStackTrace();
			} catch (InterruptedException e) {
				MessageDialog.openError(shell, "文件转换失败", e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String[] getLegalFileExtensions() {
		return new String[] { "xlf" };
	}
}
