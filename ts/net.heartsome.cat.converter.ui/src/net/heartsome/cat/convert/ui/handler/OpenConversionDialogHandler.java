package net.heartsome.cat.convert.ui.handler;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.ui.handlers.AbstractSelectProjectFilesHandler;
import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.convert.extenstion.IExecutePretranslation;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.model.ConverterUtil;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.convert.ui.resource.Messages;
import net.heartsome.cat.convert.ui.wizard.ConversionWizard;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.Progress;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 打开项目转换文件配置对话框
 * @author weachy
 * @since JDK1.5
 */
public class OpenConversionDialogHandler extends AbstractSelectProjectFilesHandler {

	@Override
	public Object execute(ExecutionEvent event, List<IFile> list) {
		CommonFunction.removeRepeateSelect(list);
		if (list == null || list.size() == 0) {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
					Messages.getString("handler.OpenConversionDialogHandler.msgTitle1"),
					Messages.getString("handler.OpenConversionDialogHandler.msg1"));
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
					Converter.DIRECTION_POSITIVE);
			converterViewModel.setConversionItem(sourceItem); // 记住所选择的文件
			converterViewModels.add(converterViewModel);
		}

		IProject project = list.get(0).getProject();
		ConversionWizard wizard = new ConversionWizard(converterViewModels, project);
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
			final List<IFile> targetFiles = new ArrayList<IFile>();

			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.setTaskName(Messages.getString("handler.OpenConversionDialogHandler.task1"));
					monitor.beginTask(Messages.getString("handler.OpenConversionDialogHandler.task2"), models.size());
					for (ConverterViewModel converterViewModel : models) {
						try {
							IProgressMonitor subMonitor = Progress.getSubMonitor(monitor, 1);
							subMonitor.setTaskName(Messages.getString("handler.OpenConversionDialogHandler.task3")
									+ converterViewModel.getConversionItem().getLocation().toOSString());
							converterViewModel.convertWithoutJob(subMonitor);
							List<File> tgtFileList = converterViewModel.getGenerateTgtFileList();
							for(File f : tgtFileList){
								IFile tgtIfile = ConverterUtil.localPath2IFile(f.getAbsolutePath());
								if (tgtIfile != null) {
									targetFiles.add(tgtIfile);
								}
							}
							
							// 若新转换的 xliff 文件重复，那么关闭已经打开的重复文件，防止文件同步冲突	robert	2013-04-01
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									CommonFunction.closePointEditor(targetFiles);
								}
							});
						} catch (ConverterException e) {
							final String message = e.getMessage();
							LOGGER.error("", e);
							Display.getDefault().syncExec(new Runnable() {

								public void run() {
									MessageDialog.openInformation(shell, Messages.getString("handler.OpenConversionDialogHandler.msgTitle2"), message);
								}
							});
						} catch (Exception e) {
							final String message = e.getMessage();
							LOGGER.error("", e);
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									MessageDialog.openInformation(shell, Messages.getString("handler.OpenConversionDialogHandler.msgTitle2"), message);
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
				MessageDialog.openError(shell, Messages.getString("handler.OpenConversionDialogHandler.msgTitle2"), e.getMessage());
				LOGGER.error("", e);
			} catch (InterruptedException e) {
				MessageDialog.openError(shell, Messages.getString("handler.OpenConversionDialogHandler.msgTitle2"), e.getMessage());
				LOGGER.error(e.getMessage());
			}

			if (wizard.isOpenPreTranslation()) {
				// 加载转换器扩展
				final IExecutePretranslation[] impls = new IExecutePretranslation[1];
				IConfigurationElement[] config2 = Platform.getExtensionRegistry().getConfigurationElementsFor(
						"net.heartsome.converter.extension.pretranslation");
				try {
					for (IConfigurationElement e : config2) {
						final Object o = e.createExecutableExtension("class");
						if (o instanceof IExecutePretranslation) {
							ISafeRunnable runnable1 = new ISafeRunnable() {

								public void handleException(Throwable exception) {
									LOGGER.error(Messages.getString("handler.OpenConversionDialogHandler.logger1"), exception);
								}

								public void run() throws Exception {
									impls[0] = (IExecutePretranslation) o;
								}
							};
							SafeRunner.run(runnable1);
						}
					}
				} catch (CoreException ex) {
					LOGGER.error(Messages.getString("handler.OpenConversionDialogHandler.logger1"), ex);
				}
				if (impls[0] != null) {
					if(targetFiles.size() < 1){
						return null;
					}
					impls[0].executePreTranslation(targetFiles);
				}
			}
		}
		return null;
	}

	@Override
	public String[] getLegalFileExtensions() {
		return new String[] { "html", "htm", "inx", "properties", "js", "mif", "doc", "ppt", "xls", "docx", "xlsx",
				"pptx", "odg", "ods", "odt", "ods", "odt", "po", "rc", "resx", "rtf", "txt", "ttx", "xml", "sdlxliff", "xlf", "idml", "mqxlz", "txml" };
	}
}
