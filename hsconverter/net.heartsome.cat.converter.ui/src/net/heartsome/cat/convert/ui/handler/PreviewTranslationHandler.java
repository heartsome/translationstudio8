package net.heartsome.cat.convert.ui.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.bean.Constant;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.model.ConversionConfigBean;
import net.heartsome.cat.convert.ui.model.ConverterUtil;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.convert.ui.utils.ConversionResource;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.ts.core.file.XLFHandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
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

/**
 * 预览翻译 Handler
 * @author weachy
 * @version 1.1
 * @since JDK1.5
 */
public class PreviewTranslationHandler extends AbstractHandler {

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
			MessageDialog.openInformation(shell, "提示", "未找当前编辑器打开的文件资源。");
		} else {
			String fileExtension = file.getFileExtension();
			if (fileExtension != null && "xlf".equalsIgnoreCase(fileExtension)) {
				ConverterViewModel model = getConverterViewModel(file);
				if (model != null) {
					model.convert();
				}
			} else if (fileExtension != null && "xlp".equalsIgnoreCase(fileExtension)) {
				if (file.exists()) {
					IFolder xliffFolder = file.getProject().getFolder(Constant.FOLDER_XLIFF);
					if (xliffFolder.exists()) {
						ArrayList<IFile> files = new ArrayList<IFile>();
						try {
							getChildFiles(xliffFolder, "xlf", files);
						} catch (CoreException e) {
							throw new ExecutionException(e.getMessage(), e);
						}
						previewFiles(files);
					} else {
						MessageDialog.openWarning(shell, "提示", "未找到系统默认的 XLIFF 文件夹！");
					}
				}
			} else {
				MessageDialog.openInformation(shell, "提示", "当前编辑器打开的文件不是一个合法的 XLIFF 文件。");
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
				monitor.setTaskName("项目预览翻译");
				monitor.beginTask("开始预翻译文件...", files.size());
				for (IFile file : files) {
					IProgressMonitor subMonitor = Progress.getSubMonitor(monitor, 1);
					subMonitor.setTaskName("开始转换文件：" + file.getLocation().toOSString());
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
					MessageDialog.openError(shell, "错误", "未找到已转换文件的路径！");
				} else {
					final IFile input = root.getFileForLocation(new Path(targetFile));
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							try {
								// 使用外部编辑器（系统默认编辑器）打开文件。
								IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
										.getActivePage();
								Assert.isNotNull(page, "当前的 Active Page 为 null。无法打开转换后的文件。");
								IDE.openEditor(page, input, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
							} catch (PartInitException e) {
								MessageDialog.openInformation(shell, "提示", "转换完成！但是在尝试使用系统默认程序打开该文件时发生异常：\n"
										+ e.getMessage());
								e.printStackTrace();
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

				IStatus status = converterViewModel.validateXliffFile(ConverterUtil.toLocalPath(xliffpath),
						new XLFHandler(), null); // 注：验证的过程中，会为文件类型和骨架文件的路径赋值。
				if (status != null && status.isOK()) {
					return converterViewModel;
				} else {
					throw new ExecutionException(status.getMessage(), status.getException());
				}
			} catch (CoreException e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * 得到子文件
	 * @param folder
	 *            文件夹
	 * @param fileExtension
	 *            子文件后缀名
	 * @param list
	 *            子文件集合
	 * @throws CoreException
	 *             ;
	 */
	public static void getChildFiles(IFolder folder, String fileExtension, List<IFile> list) throws CoreException {
		if (list == null) {
			list = new ArrayList<IFile>();
		}
		if (folder.isAccessible() && folder.exists()) {
			IResource[] members = folder.members();
			for (IResource resource : members) {
				if (resource instanceof IFile && (fileExtension).equalsIgnoreCase(resource.getFileExtension())) {
					list.add((IFile) resource);
				} else if (resource instanceof IFolder) {
					getChildFiles((IFolder) resource, fileExtension, list);
				}
			}
		}
	}
}
