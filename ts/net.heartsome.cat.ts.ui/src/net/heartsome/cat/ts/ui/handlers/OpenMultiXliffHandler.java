package net.heartsome.cat.ts.ui.handlers;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.resource.Messages;
import net.heartsome.cat.ts.ui.util.MultiFilesOper;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 合并打开所选中的 XLIFF 文件 备注：所打开的文件不能跨项目
 * @author robert 2012-03-26
 * @version
 * @since JDK1.6
 */
public class OpenMultiXliffHandler extends AbstractHandler {
	private Shell shell;
	private static final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
	public final static Logger logger = LoggerFactory.getLogger(OpenMultiXliffHandler.class.getName());

	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView("net.heartsome.cat.common.ui.navigator.view");
		ISelection currentSelection = (StructuredSelection) viewPart.getSite().getSelectionProvider().getSelection();

		if (currentSelection.isEmpty() || !(currentSelection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;
		Iterator<Object> selectIt = structuredSelection.iterator();
		final ArrayList<IFile> selectIFiles = new ArrayList<IFile>();

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		shell = window.getShell();

		// 先验证是否跨项目
		IProject selectedProject = null;
		while (selectIt.hasNext()) {
			Object object = selectIt.next();
			if (object instanceof IFile) {
				IFile iFile = (IFile) object;
				if (!CommonFunction.validXlfExtension(iFile.getFileExtension())) {
					MessageDialog.openInformation(shell, Messages.getString("handlers.OpenMultiXliffHandler.msgTitle"),
							MessageFormat.format(Messages.getString("handlers.OpenMultiXliffHandler.msg1"), iFile
									.getFullPath().toOSString()));
					continue;
				}
				selectIFiles.add(iFile);
				if (selectedProject == null) {
					selectedProject = iFile.getProject();
				} else {
					if (selectedProject != iFile.getProject()) {
						MessageDialog.openInformation(shell,
								Messages.getString("handlers.OpenMultiXliffHandler.msgTitle"),
								Messages.getString("handlers.OpenMultiXliffHandler.msg2"));
						return null;
					}
				}
			} else if (object instanceof IContainer) {
				IContainer selectContainer = (IContainer) object;
				if (selectedProject == null) {
					selectedProject = selectContainer.getProject();
				}
				// 判断当前文件夹是否处于 XLIFF 文件夹下
				try {
					ResourceUtils.getXliffs(selectContainer, selectIFiles);
				} catch (CoreException e) {
					logger.error(Messages.getString("handlers.OpenMultiXliffHandler.logger1"), selectContainer
							.getFullPath().toOSString(), e);
				}
			}
		}

		// 过滤重复选择文件
		CommonFunction.removeRepeateSelect(selectIFiles);

		if (selectIFiles.size() < 2) {
			MessageDialog.openInformation(shell, Messages.getString("handlers.OpenMultiXliffHandler.msgTitle"),
					Messages.getString("handlers.OpenMultiXliffHandler.msg3"));
			return null;
		}

		MultiFilesOper oper = new MultiFilesOper(selectedProject, selectIFiles);
		// 先验证这些文件是否已经合并打开，如果是，则退出
		if (oper.validExist()) {
			return null;
		}
		// 判断是否有重复打开的文件，并删除缓存中要合并打开的文件。
		if (oper.hasOpenedIFile()) {
			if (oper.getSelectIFiles().size() <= 0) {
				MessageDialog.openInformation(shell, Messages.getString("handlers.OpenMultiXliffHandler.msgTitle"),
						Messages.getString("handlers.OpenMultiXliffHandler.msg4"));
				return null;
			} else {
				boolean isResponse = MessageDialog.openConfirm(shell,
						Messages.getString("handlers.OpenMultiXliffHandler.msgTitle2"),
						Messages.getString("handlers.OpenMultiXliffHandler.msg5"));
				if (isResponse) {
					if (oper.getSelectIFiles().size() < 2) {
						MessageDialog.openInformation(shell,
								Messages.getString("handlers.OpenMultiXliffHandler.msgTitle"),
								Messages.getString("handlers.OpenMultiXliffHandler.msg6"));
						return null;
					}
				} else {
					return null;
				}
			}
		}

		final IFile multiIFile = oper.createMultiTempFile();
		if (multiIFile != null && multiIFile.exists()) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Messages.getString("handler.OpenMultiXliffHandler.tip1"), 10);
					IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 7,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);

					// 进行判断所选择文件的语言对是否符合标准，先解析文件
					final XLFHandler xlfHander = new XLFHandler();
					final Map<String, Object> newResultMap = xlfHander.openFiles(
							ResourceUtils.iFilesToFiles(selectIFiles), subMonitor);
					// 针对解析失败
					if (newResultMap == null
							|| QAConstant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) newResultMap
									.get(QAConstant.RETURNVALUE_RESULT)) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openInformation(shell,
										Messages.getString("handlers.OpenMultiXliffHandler.msgTitle"),
										(String) newResultMap.get(Constant.RETURNVALUE_MSG));
							}
						});
						return;
					}
					// 验证是否有多个语言
					boolean hasDiffrentLangPair = false;
					Map<String, ArrayList<String>> langMap = xlfHander.getLanguages();
					if (langMap.size() > 1) {
						hasDiffrentLangPair = true;
					} else {
						for (Entry<String, ArrayList<String>> entry : langMap.entrySet()) {
							if (entry.getValue().size() > 1) {
								hasDiffrentLangPair = true;
							}
						}
					}
					if (monitor.isCanceled()) {
						return;
					}
					monitor.worked(1);
					if (hasDiffrentLangPair) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openInformation(shell,
										Messages.getString("handlers.OpenMultiXliffHandler.msgTitle"),
										Messages.getString("handlers.OpenMultiXliffHandler.msg7"));
							}
						});
						// 先删除临时文件，再退出
						try {
							multiIFile.delete(true, monitor);
						} catch (CoreException e) {
							logger.error(Messages.getString("handlers.OpenMultiXliffHandler.logger2"), e);
						}
						return;
					}
					final boolean[] validateResult = new boolean[]{false};
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							validateResult[0] = XLFValidator.validateXlifIFiles(selectIFiles);
						}
					});
					if(!validateResult[0]){
						try {
							multiIFile.delete(true, monitor);
						} catch (CoreException e) {
							logger.error(Messages.getString("handlers.OpenMultiXliffHandler.logger2"), e);
						}
						return;
					}
					final FileEditorInput input = new FileEditorInput(multiIFile);
					if (monitor.isCanceled()) {
						return;
					}
					monitor.worked(1);

					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							try {
								// UNDO 这里合并打开时，要考虑传入参数xlfHandler，以防多次解析文件带来的消耗。
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
										.openEditor(input, XLIFF_EDITOR_ID, true);

							} catch (PartInitException e) {
								e.printStackTrace();
							}
						}
					});
					if (monitor.isCanceled()) {
						return;
					}
					monitor.worked(1);
					monitor.done();
				}
			};
			try {
				new ProgressMonitorDialog(shell).run(true, true, runnable);
			} catch (Exception e) {
				logger.error(Messages.getString("handlers.OpenMultiXliffHandler.logger3"), e);
			}
		} else {
			MessageDialog.openInformation(shell, Messages.getString("handlers.OpenMultiXliffHandler.msgTitle"),
					Messages.getString("handlers.OpenMultiXliffHandler.msg8"));
			return null;
		}
		return null;
	}
}
