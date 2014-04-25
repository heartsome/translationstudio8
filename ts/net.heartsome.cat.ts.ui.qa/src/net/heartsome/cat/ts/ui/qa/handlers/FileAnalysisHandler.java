package net.heartsome.cat.ts.ui.qa.handlers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.core.qa.FAModel;
import net.heartsome.cat.ts.core.qa.FileAnalysis;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.dialogs.FileAnalysisDialog;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.util.ProgressIndicatorManager;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件分析子菜单下的字数分析的handler。
 * @author robert 2011-12-07
 */
public class FileAnalysisHandler extends AbstractHandler {
	private FAModel model;
	private Shell shell;
	/** 解析出错时是否继续执行，0为继续，1为出错时继续执行，2为出错时退出执行 */
	private int continuResponse;
	/** 文件分析项(字数分析，翻译进度分析，编辑进度分析) */
	private String faItemId;
	/** 文件分析项的名称 */
	private String title;
	private int allTUSize;
	/** 针对选择当前 nattable 编辑器是否合并打开 */
	private boolean isMultiFile;
	public final static Logger logger = LoggerFactory.getLogger(FileAnalysisHandler.class.getName());

	public Object execute(ExecutionEvent event) throws ExecutionException {
		model = new FAModel();
		faItemId = event.getParameter("faItemId");
		allTUSize = 0;

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		shell = window.getShell();
		IFile multiTempIFile = null;
		IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView("net.heartsome.cat.common.ui.navigator.view");
		ArrayList<IFile> selectIFiles = new ArrayList<IFile>();
		if (HandlerUtil.getActivePart(event) instanceof IViewPart) {
			ISelection currentSelection = (StructuredSelection) viewPart.getSite().getSelectionProvider()
					.getSelection();
			if (currentSelection != null && !currentSelection.isEmpty()
					&& currentSelection instanceof IStructuredSelection) {

				IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;
				@SuppressWarnings("unchecked")
				Iterator<Object> selectIt = structuredSelection.iterator();
				while (selectIt.hasNext()) {
					Object object = selectIt.next();
					if (object instanceof IFile) {
						IFile selectFile = (IFile) object;
						String fileExtension = selectFile.getFileExtension();
						// 如果后缀名不是xlf，那么就进行提示
						if (fileExtension == null || !CommonFunction.validXlfExtension(fileExtension)) {
							boolean isSure = MessageDialog.openConfirm(shell, Messages
									.getString("qa.all.dialog.warning"), MessageFormat.format(
									Messages.getString("qa.all.tip.notXliff"),
									new Object[] { selectFile.getFullPath() }));
							if (!isSure) {
								return null;
							}
						}
						selectIFiles.add(selectFile);
					} else if (object instanceof IContainer) {
						IContainer selectContainer = (IContainer) object;
						try {
							ResourceUtils.getXliffs(selectContainer, selectIFiles);

						} catch (Exception e) {
							e.printStackTrace();
							logger.error(Messages.getString("qa.handlers.FileAnalysisHandler.log1"), e);
						}
					}
				}
			}

		} else if (HandlerUtil.getActivePart(event) instanceof IEditorPart) {
			// 如果左边未选择品质检查的类型，那么，获取nattable中打开的文件
			IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
			String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
			if (activeEditor != null && !activeEditor.getSite().getId().equals(XLIFF_EDITOR_ID)) {
				MessageDialog.openWarning(shell, Messages.getString("qa.all.dialog.warning"),
						Messages.getString("qa.handlers.BatchQAHandler.tip2"));
				return null;
			}
			XLIFFEditorImplWithNatTable nattable = (XLIFFEditorImplWithNatTable) activeEditor;
			isMultiFile = nattable.isMultiFile();
			multiTempIFile = ((FileEditorInput) nattable.getEditorInput()).getFile();
			if (isMultiFile) {
				List<String> multiFilesList = new XLFHandler().getMultiFiles(multiTempIFile);
				for (String multiFileStr : multiFilesList) {
					selectIFiles.add(ResourceUtils.fileToIFile(multiFileStr));
				}
			} else {
				selectIFiles.add(multiTempIFile);
			}

		} else {
			MessageDialog.openWarning(shell, Messages.getString("qa.all.dialog.warning"),
					Messages.getString("qa.handlers.FileAnalysisHandler.tip3"));
			return null;
		}
		
		CommonFunction.removeRepeateSelect(selectIFiles);

		if (selectIFiles.size() <= 0) {
			MessageDialog.openWarning(shell, Messages.getString("qa.all.dialog.warning"),
					Messages.getString("qa.handlers.FileAnalysisHandler.tip1"));
			return null;
		}

		List<IFile> lstFiles = new ArrayList<IFile>();
		XLFValidator.resetFlag();
		for (IFile iFile : selectIFiles) {
			if (!XLFValidator.validateXliffFile(iFile)) {
				lstFiles.add(iFile);
			}
		}
		XLFValidator.resetFlag();
		selectIFiles.removeAll(lstFiles);
		if (selectIFiles.size() == 0) {
			return null;
		}

		// 判断要分析的文件是否是属于同一个项目中的.
		IProject project = selectIFiles.get(0).getProject();
		if (selectIFiles.size() >= 2) {
			for (int i = 1; i < selectIFiles.size(); i++) {
				if (selectIFiles.get(i).getProject() != project) {
					MessageDialog.openWarning(shell, Messages.getString("qa.all.dialog.warning"),
							Messages.getString("qa.handlers.FileAnalysisHandler.tip2"));
					return null;
				}
			}
		}

		model.setAnalysisIFileList(selectIFiles);
		model.setShell(shell);
		model.setMultiFile(isMultiFile);
		if (isMultiFile) {
			model.setMultiTempIFile(multiTempIFile);
		}

		// 文件分析框的框名
		title = model.getAnalysisItemMap().get(faItemId).get(QAConstant.FA_ITEM_NAME);
		FileAnalysisDialog dialog = new FileAnalysisDialog(shell, model, title, faItemId);
		int result = dialog.open();

		if (result == IDialogConstants.OK_ID) {
			analysisFile(title);
		}
		return null;
	}

	/**
	 * 准备分析文件
	 */
	public void analysisFile(String title) {
		final QAXmlHandler handler = new QAXmlHandler();
		Job job = new Job(title) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// 要分析的文件的个数
				int fileNum = model.getAnalysisIFileList().size();
				model.setSubFileNum(fileNum);
				// 定义的进度条总共四格，其中，解析文件一格，分析文件三格
				monitor.beginTask("", fileNum * 4);

				// 解析文件,如果解析不成功，退出程序, 解析要分析的文件，用掉fileNum*1个格子
				if (!openXliff(handler, monitor)) {
					monitor.done();
					return Status.CANCEL_STATUS;
				}

				if (model.getAnalysisIFileList().size() == 0) {
					MessageDialog.openInformation(shell, Messages.getString("qa.all.dialog.info"),
							Messages.getString("qa.handlers.FileAnalysisHandler.tip4"));
					return Status.CANCEL_STATUS;
				}

				// 填充要分析文件的所有trans-unit节点个数的总和
				model.setAllTuSize(allTUSize);

				FileAnalysis fileAnalysis = getClassInstance(faItemId);

				// 分析文件用去fileNum*3个格子
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, fileNum * 3,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);

				int analysisResult = fileAnalysis.beginAnalysis(model, subMonitor, handler);
				if (analysisResult == -1 || analysisResult == QAConstant.QA_ZERO) {
					return Status.CANCEL_STATUS;
				}

				subMonitor.done();
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		
		
		// 当程序退出时，检测当前　job 是否正常关闭
		CommonFunction.jobCantCancelTip(job);
		job.addJobChangeListener(new JobChangeAdapter(){
			@Override
			public void running(IJobChangeEvent event) {
				ProgressIndicatorManager.displayProgressIndicator();
				super.running(event);
			}
			@Override
			public void done(IJobChangeEvent event) {
				ProgressIndicatorManager.hideProgressIndicator();
				super.done(event);
			}
		});
		job.setUser(true);
		job.schedule();

	}

	/**
	 * 解析所有的xliff文件
	 * @param handler
	 * @param monitor
	 * @return
	 */
	public boolean openXliff(QAXmlHandler handler, IProgressMonitor monitor) {
		for (int fileIndex = 0; fileIndex < model.getAnalysisIFileList().size(); fileIndex++) {
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);

			final IFile iFile = model.getAnalysisIFileList().get(fileIndex);
			subMonitor.setTaskName(MessageFormat.format(Messages.getString("qa.handlers.FileAnalysisHandler.tip5"),
					new Object[] { title, iFile.getFullPath().toString() }));

			continuResponse = QAConstant.QA_ZERO;
			try {
				Map<String, Object> newResultMap = handler.openFile(iFile.getLocation().toOSString(), subMonitor);
				// 针对退出解析
				if (newResultMap != null
						&& QAConstant.RETURNVALUE_RESULT_RETURN.equals(newResultMap.get(QAConstant.RETURNVALUE_RESULT))) {
					return false;
				}

				if (newResultMap == null
						|| QAConstant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) newResultMap
								.get(QAConstant.RETURNVALUE_RESULT)) {
					model.getErrorIFileList().add(iFile);
					// 针对文件解析出错
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							boolean response = MessageDialog.openConfirm(shell, Messages
									.getString("qa.all.dialog.error"), MessageFormat.format(Messages
									.getString("qa.all.tip.openXliffError"), new Object[] { iFile.getFullPath()
									.toOSString() }));
							if (response) {
								continuResponse = QAConstant.QA_FIRST;
							} else {
								continuResponse = QAConstant.QA_TWO;
							}
						}
					});
				}

				if (continuResponse == QAConstant.QA_FIRST) {
					model.getAnalysisIFileList().remove(fileIndex);
					fileIndex--;
					continue;
				} else if (continuResponse == QAConstant.QA_TWO) {
					return false;
				}

				allTUSize += handler.getTuSizeMap().get(iFile.getLocation().toOSString());
			} catch (Exception e) {
				MessageDialog.openError(shell, Messages.getString("qa.all.dialog.info"),
						Messages.getString("qa.all.log.openXmlError") + e);
				logger.error(Messages.getString("qa.all.log.openXmlError"), e);
				return false;
			}
		}
		return true;

	}

	/**
	 * 获取某个检查项实现类的实例
	 * @param faItemId
	 *            文件分析项，即字数分析，翻译进度分析，编辑进度分析
	 * @return
	 */
	public FileAnalysis getClassInstance(String faItemId) {
		try {
			Map<String, String> valueMap = model.getAnalysisItemMap().get(faItemId);
			Object obj = null;
			try {
				obj = Class.forName(valueMap.get(QAConstant.FA_ITEM_CLASSNAME)).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.handlers.FileAnalysisHandler.log2"), e);
			}
			if (FileAnalysis.class.isInstance(obj)) {
				return (FileAnalysis) obj;
			}
		} catch (Exception e) {
			MessageDialog.openError(shell, Messages.getString("qa.all.dialog.info"),
					Messages.getString("qa.handlers.FileAnalysisHandler.tip6") + e);
			logger.error(Messages.getString("qa.handlers.FileAnalysisHandler.tip6"), e);
		}
		return null;
	}
}
