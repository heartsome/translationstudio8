package net.heartsome.cat.convert.ui.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.model.ConversionConfigBean;
import net.heartsome.cat.convert.ui.model.ConverterUtil;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.convert.ui.resource.Messages;
import net.heartsome.cat.convert.ui.utils.ConversionResource;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.ts.core.file.XLFHandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 预览翻译 Handler
 * @author weachy
 * @version 1.1
 * @since JDK1.5
 */
public class PreviewTranslationHandler extends AbstractHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PreviewTranslationHandler.class);

	private IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

	private Shell shell;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor == null) {
			return false;
		}
		IEditorInput input = editor.getEditorInput();
		IFile file = ResourceUtil.getFile(input);
		shell = HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell();
		if (file == null) {
			MessageDialog.openInformation(shell, Messages.getString("handler.PreviewTranslationHandler.msgTitle"),
					Messages.getString("handler.PreviewTranslationHandler.msg1"));
		} else {
			String fileExtension = file.getFileExtension();
			if (fileExtension != null && CommonFunction.validXlfExtension(fileExtension)) {
				ConverterViewModel model = getConverterViewModel(file);
				if (model != null) {
					// model.convert();
					try {
						previewFiles(new ArrayList<IFile>(Arrays.asList(new IFile[] { file })));
					} catch (Exception e) {
					  // 修改 当异常没有消息，提示信息为空
						MessageDialog.openInformation(shell,
								Messages.getString("handler.PreviewTranslationHandler.msgTitle"),
								Messages.getString("handler.PreviewTranslationHandler.msg7"));
						LOGGER.error("", e);
					}
				}
			} else if (fileExtension != null && "xlp".equalsIgnoreCase(fileExtension)) {
				// UNDO 合并打开的预览翻译有问题，是针对合并打开的文件，而不是针对项目所有的文件 robert 2012-07-12
				if (file.exists()) {
					// IFolder xliffFolder = file.getProject().getFolder(Constant.FOLDER_XLIFF);
					// Fixed Bug #2616 预览翻译--合并打开的文件不能进行预览翻译 by Jason
					XLFHandler hander = new XLFHandler();
					List<String> files = hander.getMultiFiles(file);
					List<IFile> ifileList = new ArrayList<IFile>();
					for (String tf : files) {
						ifileList.add(ResourceUtils.fileToIFile(tf));
					}
					// if (xliffFolder.exists()) {
					// ArrayList<IFile> files = new ArrayList<IFile>();
					// try {
					// ResourceUtils.getXliffs(xliffFolder, files);
					// } catch (CoreException e) {
					// throw new ExecutionException(e.getMessage(), e);
					// }
					previewFiles(ifileList);
					// } else {
					// MessageDialog
					// .openInformation(shell, Messages.getString("handler.PreviewTranslationHandler.msgTitle"),
					// Messages.getString("handler.PreviewTranslationHandler.msg2"));
					// }
				}
			} else {
				MessageDialog.openInformation(shell, Messages.getString("handler.PreviewTranslationHandler.msgTitle"),
						Messages.getString("handler.PreviewTranslationHandler.msg3"));
			}
		}
		return null;
	}

	/**
	 * 预览翻译多个文件
	 * @param files
	 *            文件集合
	 * @throws ExecutionException
	 *             ;
	 */
	private void previewFiles(final List<IFile> files) throws ExecutionException {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InvocationTargetException {
				monitor.setTaskName(Messages.getString("handler.PreviewTranslationHandler.task1"));
				monitor.beginTask(Messages.getString("handler.PreviewTranslationHandler.task2"), files.size());
				for (IFile file : files) {
					IProgressMonitor subMonitor = Progress.getSubMonitor(monitor, 1);
					subMonitor.setTaskName(Messages.getString("handler.PreviewTranslationHandler.task3")
							+ file.getLocation().toOSString());
					try {
						preview(file, subMonitor); // 预览单个文件
					} catch (ExecutionException e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
				monitor.done();
			}
		};
		try {
			new ProgressMonitorDialog(shell).run(true, true, runnable);
		} catch (InvocationTargetException e) {
			throw new ExecutionException(e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new ExecutionException(e.getMessage(), e);
		}
	}

	/**
	 * 预览单个文件
	 * @param file
	 *            IFile 对象
	 * @param subMonitor
	 *            进度监视器
	 * @throws ExecutionException
	 *             ;
	 */
	private void preview(IFile file, IProgressMonitor subMonitor) throws ExecutionException {
		ConverterViewModel model = getConverterViewModel(file);
		if (model != null) {
			try {
				Map<String, String> result = model.convertWithoutJob(subMonitor);
				String targetFile = result.get(Converter.ATTR_TARGET_FILE);
				if (targetFile == null) {
					MessageDialog.openError(shell, Messages.getString("handler.PreviewTranslationHandler.msgTitle"),
							Messages.getString("handler.PreviewTranslationHandler.msg4"));
				} else {
					final IFile input = root.getFileForLocation(new Path(targetFile));
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							try {
								// 使用外部编辑器（系统默认编辑器）打开文件。
								IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
										.getActivePage();
								Assert.isNotNull(page, Messages.getString("handler.PreviewTranslationHandler.msg5"));
								IDE.openEditor(page, input, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
							} catch (PartInitException e) {
								MessageDialog.openInformation(shell,
										Messages.getString("handler.PreviewTranslationHandler.msgTitle"),
										Messages.getString("handler.PreviewTranslationHandler.msg6") + e.getMessage());
								LOGGER.error("", e);
							}
						}
					});
				}
			} catch (ConverterException e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
		subMonitor.done();
	}

	/**
	 * 得到 ConverterViewModel 对象
	 * @param file
	 *            需要预览翻译的文件
	 * @return
	 * @throws ExecutionException
	 *             ;
	 */
	private ConverterViewModel getConverterViewModel(IFile file) throws ExecutionException {
		Object adapter = Platform.getAdapterManager().getAdapter(file, IConversionItem.class);
		if (adapter instanceof IConversionItem) {
			IConversionItem item = (IConversionItem) adapter;
			ConverterViewModel converterViewModel = new ConverterViewModel(Activator.getContext(),
					Converter.DIRECTION_REVERSE); // 逆向转换
			converterViewModel.setConversionItem(item);
			try {
				ConversionResource resource = new ConversionResource(Converter.DIRECTION_REVERSE, item);
				String xliffpath = resource.getXliffPath();
				String targetPath = resource.getPreviewPath();

				ConversionConfigBean configBean = converterViewModel.getConfigBean();
				configBean.setSource(xliffpath);
				configBean.setTarget(targetPath);
				configBean.setTargetEncoding("UTF-8");
				configBean.setPreviewMode(true); // 设为预览翻译模式

				final IStatus status = converterViewModel.validateXliffFile(ConverterUtil.toLocalPath(xliffpath),
						new XLFHandler(), null); // 注：验证的过程中，会为文件类型和骨架文件的路径赋值。
				if (status != null && status.isOK()) {
					return converterViewModel;
				} else {
					Display.getDefault().syncExec(new Runnable() {

						public void run() {
							MessageDialog.openInformation(shell,
									Messages.getString("handler.PreviewTranslationHandler.msgTitle"),
									status.getMessage());
						}
					});
					throw new ExecutionException(status.getMessage(), status.getException());
				}
			} catch (CoreException e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
		return null;
	}

}
