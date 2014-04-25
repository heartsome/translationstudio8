package net.heartsome.cat.ts.ui.qa.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QATUDataBean;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.NumberConsistenceQA;
import net.heartsome.cat.ts.ui.qa.TagConsistenceQA;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.qa.views.QAResultViewPart;
import net.heartsome.cat.ts.ui.util.MultiFilesOper;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.util.ProgressIndicatorManager;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

/**
 * 字数分析与标记一致性检查的触发类
 * @author robert 2012-06-04
 */
public class NumberOrTagConsisQAHandler extends AbstractHandler {
	/** 这是进度条的前进间隔，也就是当循环多少个trans-unit节点后前进一格 */
	private int workInterval = 1;
	private QAXmlHandler xmlHandler;
	private String _INFO = Messages.getString("qa.all.dialog.info");
	private String _ERROR = Messages.getString("qa.all.dialog.error");
	
	@SuppressWarnings("unused")
	private Shell shell;
	/** 连接符号，用于连接源语言和目标语言的种类，例如“zh-CN -&gt; en” ，这个要与nattable界面上的过滤条件保持一致 */
	private static final String Hyphen = " -> ";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean isMultiFile = false;
		IFile multiTempIFile = null;
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		// 改为布局
		if (editorPart != null && editorPart instanceof XLIFFEditorImplWithNatTable) {
			String qaItem = event.getParameter("qaItemId");
			XLIFFEditorImplWithNatTable nattable = (XLIFFEditorImplWithNatTable) editorPart;
			ArrayList<IFile> selectIFiles = new ArrayList<IFile>();
			FileEditorInput input = (FileEditorInput) nattable.getEditorInput();

			// 首先判断是否是合并打开的文件
			if (nattable.isMultiFile()) {
				isMultiFile = true;
			}
			if (isMultiFile) {
				multiTempIFile = input.getFile();
				List<String> multiFilesList = new XLFHandler().getMultiFiles(multiTempIFile);
				for (String filePath : multiFilesList) {
					selectIFiles.add(ResourceUtils.fileToIFile(filePath));
				}
			} else {
				selectIFiles.add(input.getFile());
			}

			QAModel model = new QAModel();
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			shell = window.getShell();
			// 先调用方法，查看品质检查结果视图是否处于显示状态，如果是显示的，就删除数据
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = workbenchPage.findView(QAResultViewPart.ID);
			if (view != null) {
				// 运行时，将结果视图中列表的数据清除
				((QAResultViewPart) view).clearTableData();
			}

			QAResult qaResult = new QAResult();

			// 存储品质检查的检查项
			// model.setBatchQAItemIdList(getBatchQAItems());
			// 存储品质检查的检查时不包括的文本段
			model.setNotInclude(getNotIncludePara());

			// 给品质检查结果视图发出通告，本次检查对象为合并打开文件
			qaResult.firePropertyChange(isMultiFile, new MultiFilesOper(selectIFiles.get(0).getProject(), selectIFiles,
					multiTempIFile));
			if (isMultiFile) {
				model.setMuliFiles(true);
				model.setMultiOper(new MultiFilesOper(selectIFiles.get(0).getProject(), selectIFiles,multiTempIFile));
			} else {
				model.setMuliFiles(false);
			}

			boolean isNumberQA = false;
			if (QAConstant.QA_NUMBER.equals(qaItem)) {
				isNumberQA = true;
			} else if (QAConstant.QA_TAG.equals(qaItem)) {
				isNumberQA = false;
			}
			List<String> fileList = new ArrayList<String>();
			for(IFile iFIle : selectIFiles){
				fileList.add(iFIle.getLocation().toOSString());
			}
			qaResult.setFilePathList(fileList);
			HsMultiActiveCellEditor.commit(true);
			beginQA(selectIFiles, model, isNumberQA, qaResult);
		}
		return null;
	}

	private void beginQA(final ArrayList<IFile> selectIFiles, final QAModel model, final boolean isNumberQA,
			final QAResult qaResult) {
		final String titile = isNumberQA ? Messages.getString("qa.NumberOrTagConsisQAHandler.jobTitle1") : Messages
				.getString("qa.NumberOrTagConsisQAHandler.jobTitle2");
		Job job = new Job(titile) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// 解析一格，其余九格
				monitor.beginTask(titile, 10 * selectIFiles.size());
				xmlHandler = new QAXmlHandler();
				// 首先解析文件，如果为false，则退出
				if (!openFile(selectIFiles, monitor)) {
					return Status.OK_STATUS;
				}
				int allTUSize = 0;
				for (IFile iFile : selectIFiles) {
					allTUSize += xmlHandler.getTuSizeMap().get(iFile.getLocation().toOSString());
				}
				initWorkInterval(allTUSize);

				// 开始进行数据处理
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 9,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
				subMonitor.beginTask(titile + "...", allTUSize % workInterval == 0 ? (allTUSize / workInterval)
						: (allTUSize / workInterval) + 1);

				Map<String, ArrayList<String>> languageList = xmlHandler.getLanguages();
				int lineNumber = 0;
				int traversalTuIndex = 0;
				IFile iFile;
				for (Entry<String, ArrayList<String>> langEntry : languageList.entrySet()) {
					String srcLang = langEntry.getKey();
					for (String tgtLang : langEntry.getValue()) {
						List<String> rowIdsList = xmlHandler.getAllRowIdsByLanguages(srcLang.toUpperCase(),
								tgtLang.toUpperCase());
						model.setRowIdsList(rowIdsList);
						// 开始针对每一个文本段进行检查
						for (String rowId : rowIdsList) {
							traversalTuIndex++;
							lineNumber = rowIdsList.indexOf(rowId) + 1; // 行号
							String filePath = RowIdUtil.getFileNameByRowId(rowId);
							iFile = ResourceUtils.fileToIFile(filePath);
							String langPair = srcLang + Hyphen + tgtLang;
							QATUDataBean tuDataBean = xmlHandler.getFilteredTUText(filePath,
									RowIdUtil.parseRowIdToXPath(rowId), model.getNotInclude());
							if (tuDataBean == null) {
								if (!xmlHandler.monitorWork(subMonitor, traversalTuIndex, workInterval, false)) {
									return Status.CANCEL_STATUS;
								}
								continue;
							}

							if (!tuDataBean.isPassFilter()) {
								if (!xmlHandler.monitorWork(subMonitor, traversalTuIndex, workInterval, false)) {
									return Status.CANCEL_STATUS;
								}
								continue;
							} else if (tuDataBean.getTgtContent() == null || "".equals(tuDataBean.getTgtContent())) { // 正常情况下应有四个值
								// 因为文本段完整性检查要判断译文是否为空的情况，所以，如果译文为空，只有文本段完整性要进行检查，其他检查项都跳过。
								continue;
							}

							tuDataBean.setLineNumber(lineNumber + "");
							tuDataBean.setFileName(iFile.getName());
							tuDataBean.setSrcLang(srcLang);
							tuDataBean.setTgtLang(tgtLang);

							if (isNumberQA) {
								NumberConsistenceQA numberQA = new NumberConsistenceQA();
								qaResult.setMultiOper(model.getMultiOper());
								numberQA.setQaResult(qaResult);
								numberQA.startQA(model, subMonitor, iFile, xmlHandler, tuDataBean);
							} else {
								TagConsistenceQA tagQA = new TagConsistenceQA();
								qaResult.setMultiOper(model.getMultiOper());
								tagQA.setQaResult(qaResult);
								tagQA.startQA(model, subMonitor, iFile, xmlHandler, tuDataBean);
							}
							
							qaResult.sendDataToViewer(null);

							if (!xmlHandler.monitorWork(subMonitor, traversalTuIndex, workInterval, false)) {
								return Status.CANCEL_STATUS;
							}
						}
					}
					if (!xmlHandler.monitorWork(subMonitor, traversalTuIndex, workInterval, false)) {
						return Status.CANCEL_STATUS;
					}
				}
				qaResult.informQAEndFlag();
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
	 * 解析文件
	 * @param selectIFiles
	 */
	private boolean openFile(ArrayList<IFile> selectIFiles, IProgressMonitor monitor) {
		for (final IFile iFile : selectIFiles) {
			IProgressMonitor openMonitor = new SubProgressMonitor(monitor, 1,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
			Map<String, Object> newResultMap = xmlHandler.openFile(iFile.getLocation().toOSString(), openMonitor);
			// 针对退出解析
			if (newResultMap != null
					&& QAConstant.RETURNVALUE_RESULT_RETURN.equals(newResultMap.get(QAConstant.RETURNVALUE_RESULT))) {
				return false;
			}

			if (newResultMap == null
					|| QAConstant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) newResultMap
							.get(QAConstant.RETURNVALUE_RESULT)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取首选项中品质检查的不包括的文本段
	 * @return
	 */
	public Map<String, Boolean> getNotIncludePara() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		Map<String, Boolean> notInclude = new HashMap<String, Boolean>();

		// 不包括上下文匹配
		notInclude.put(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE,
				preferenceStore.getBoolean(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE));
		// 不包括完成匹配
		notInclude.put(QAConstant.QA_PREF_FULLMATCH_NOTINCLUDE,
				preferenceStore.getBoolean(QAConstant.QA_PREF_FULLMATCH_NOTINCLUDE));
		// 不包括已锁文本段
		notInclude.put(QAConstant.QA_PREF_LOCKED_NOTINCLUDE,
				preferenceStore.getBoolean(QAConstant.QA_PREF_LOCKED_NOTINCLUDE));

		return notInclude;
	}

	/**
	 * 初始化进度条前进前隔值，使之总值不大于五百。
	 */
	private int initWorkInterval(int allTUSize) {
		if (allTUSize > 500) {
			workInterval = allTUSize / 500;
		}
		return allTUSize;
	}
	
	public static void main(String[] args) {
		System.out.println(System.getProperty("user.name"));
		
		
	}

}
