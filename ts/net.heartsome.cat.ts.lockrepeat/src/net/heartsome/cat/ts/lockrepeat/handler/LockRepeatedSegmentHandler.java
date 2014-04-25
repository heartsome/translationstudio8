package net.heartsome.cat.ts.lockrepeat.handler;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.handlers.AbstractSelectProjectFilesHandler;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.lockrepeat.dialog.LockRepeatedSegmentDialog;
import net.heartsome.cat.ts.lockrepeat.dialog.LockRepeatedSegmentResultDialog;
import net.heartsome.cat.ts.lockrepeat.resource.Messages;
import net.heartsome.cat.ts.tm.match.TmMatcher;
import net.heartsome.cat.ts.ui.util.MultiFilesOper;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 锁定重复文本段 (修改者：robert 2012-03-24)
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class LockRepeatedSegmentHandler extends AbstractSelectProjectFilesHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LockRepeatedSegmentHandler.class);

	private static final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
	private IWorkbenchWindow window;
	/** 标识某文件是否被锁定 */
	private boolean isLocked;
	private List<IFile> list = new ArrayList<IFile>();
	private XLIFFEditorImplWithNatTable nattable;
	/** 在针对非编辑器打开文件的情况下，是否单个处理 */
	private boolean continuee;
	private XLIFFEditorImplWithNatTable singleNattable;
	/** 记忆库操作类，用来查询记忆库的数据 */
	private TmMatcher tmMatcher;
	private IProject curProject;
	/** 是否退出操作 */
	private boolean isCancel;

	/**
	 * 是否内部重复锁定
	 */
	private boolean isLockInnerRepeatedSegment;
	/**
	 * 是否完全匹配锁定
	 */
	private boolean isLockTM100Segment;
	/**
	 * 是否上下文锁定
	 */
	private boolean isLockTM101Segment;
	@Override
	public Object execute(final ExecutionEvent event, final List<IFile> iFileList) {
		list = iFileList;
		tmMatcher = new TmMatcher();
		isCancel = false;
		continuee = true;
		isLocked = false;

		if (list == null || list.isEmpty()) {
			if (list.size() == 0) {
				MessageDialog.openInformation(shell,
						Messages.getString("translation.LockRepeatedSegmentHandler.msgTitle"),
						Messages.getString("translation.LockRepeatedSegmentHandler.msg1"));
				return null;
			}
			return null;
		}

		try {
			window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		} catch (ExecutionException e1) {
			LOGGER.error("", e1);
			e1.printStackTrace();
		}

		// 首先验证是否是合并打开的文件 --robert
		if (isEditor) {
			IEditorReference[] editorRefe = window.getActivePage().findEditors(new FileEditorInput(list.get(0)),
					XLIFF_EDITOR_ID, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
			if (editorRefe.length <= 0) {
				return null;
			}

			nattable = (XLIFFEditorImplWithNatTable) editorRefe[0].getEditor(true);
			// 针对合并打开
			if (nattable.isMultiFile()) {
				list = ResourceUtils.filesToIFiles(nattable.getMultiFileList());
			}
		}

		// 添加验证 peason
		List<IFile> lstFiles = new ArrayList<IFile>();
		XLFValidator.resetFlag();
		for (IFile iFile : list) {
			if (!XLFValidator.validateXliffFile(iFile)) {
				lstFiles.add(iFile);
			}
		}
		XLFValidator.resetFlag();
		if (!(list instanceof ArrayList)) {
			list = new ArrayList<IFile>(list);
		}
		list.removeAll(lstFiles);
		if (list.size() == 0) {
			return null;
		}

		CommonFunction.removeRepeateSelect(list);

		final LockRepeatedSegmentDialog dialog = new LockRepeatedSegmentDialog(shell, list,
				Messages.getString("translation.LockRepeatedSegmentHandler.dialog"));
		if (dialog.open() == LockRepeatedSegmentDialog.OK) {
			isLockInnerRepeatedSegment =dialog.isLockInnerRepeatedSegment();
			isLockTM100Segment =dialog.isLockTM100Segment();
			isLockTM101Segment=dialog.isLockTM101Segment();
			if (!dialog.isLockInnerRepeatedSegment() && !dialog.isLockTM100Segment() && !dialog.isLockTM101Segment()) { // “锁定内部”、“锁定100%”、“锁定101%”都未选中。
				return null;
			}
			if (!isEditor) {
				// 如果针对单个文件， 先验证是否有合并打开的
				MultiFilesOper oper = new MultiFilesOper(list.get(0).getProject(), (ArrayList<IFile>) list);
				// 如果有合并打开的文件，那么将这种转换成针对编辑器的方式处理
				if (oper.validExist()) {
					final IFile multiTempIFile = oper.getMultiFilesTempIFile(true);
					IEditorReference[] editorRefe = window.getActivePage().findEditors(
							new FileEditorInput(multiTempIFile), XLIFF_EDITOR_ID,
							IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
					// 如果这几个文件没有合并打开，
					if (editorRefe.length > 0) {
						nattable = (XLIFFEditorImplWithNatTable) editorRefe[0].getEditor(true);
						continuee = false;
					}
				}
			}

			// 开始进行处理
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						int totalWork = dialog.isLockInnerRepeatedSegment() ? list.size() : 0;
						totalWork = (dialog.isLockTM100Segment() || dialog.isLockTM101Segment()) ? totalWork * 2
								: totalWork;
						monitor.beginTask(Messages.getString("translation.LockRepeatedSegmentHandler.task1"), totalWork);
						// 修改结果显示为是否成功
						 final LockRepeatedSegmentResultDialog lrsrd = new LockRepeatedSegmentResultDialog(shell,
						 list);
						// 是否进行外部匹配
						boolean checkTM = false;
						curProject = list.get(0).getProject();
						// 锁定外部完全匹配与外部上下文匹配
						if ((dialog.isLockTM100Segment() || dialog.isLockTM101Segment())) {
							// 如果是针对编辑器，那么将里面的文件进行统一处理
							if (isEditor) {
								LockTMSegment lts = lockTMSegmentOFEditor(list, dialog.isLockTM100Segment(),
										dialog.isLockTM101Segment(), monitor);
								// 用户指定退出操作
								if (lts == null && isCancel) {
									return;
								}
								 lrsrd.setLockedFullMatchResult(lts.getLockedFullMatchResult());
								 lrsrd.setLockedContextResult(lts.getLockedContextResult());
								 lrsrd.setTuNumResult(lts.getTuNumResult());
								 checkTM = true;
							} else {
								if (continuee) {
									Map<String, Integer> lockedFullMatchResultMap = new HashMap<String, Integer>();
									Map<String, Integer> lockedContextMatchResultMap = new HashMap<String, Integer>();
									Map<String, Integer> lockedTuNumResultMap = new HashMap<String, Integer>();
									for (int i = 0; i < list.size(); i++) {
										IFile iFile = list.get(i);
										LockTMSegment lts = lockTMSegment(Arrays.asList(iFile),
												dialog.isLockTM100Segment(), dialog.isLockTM101Segment(), monitor);
										if (lts == null && isCancel) {
											return;
										}
										// 返回的为空，是解析异常的文件被删除了。
										if (lts != null) {
											lockedFullMatchResultMap.putAll(lts.getLockedFullMatchResult());
											lockedContextMatchResultMap.putAll(lts.getLockedContextResult());
											lockedTuNumResultMap.putAll(lts.getTuNumResult());
										} else {
											i--;
										}
									}
									 lrsrd.setLockedFullMatchResult(lockedFullMatchResultMap);
									 lrsrd.setLockedContextResult(lockedContextMatchResultMap);
									 lrsrd.setTuNumResult(lockedTuNumResultMap);
									checkTM = true;
								} else {
									LockTMSegment lts = lockTMSegmentOFEditor(list, dialog.isLockTM100Segment(),
											dialog.isLockTM101Segment(), monitor);
									if (lts == null && isCancel) {
										return;
									}
									 lrsrd.setLockedFullMatchResult(lts.getLockedFullMatchResult());
									 lrsrd.setLockedContextResult(lts.getLockedContextResult());
									 lrsrd.setTuNumResult(lts.getTuNumResult());
									checkTM = true;
								}
							}
						}

						// 锁定内部重复
						if (dialog.isLockInnerRepeatedSegment()) {
							SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, list.size(),
									SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
							HashMap<String, Integer> lockedInnerRepeatedResault = new HashMap<String, Integer>();
							HashMap<String, Integer> tuNumResult = null;
							if (!checkTM) {
								tuNumResult = new HashMap<String, Integer>();
							}

							Map<String, int[]> resMap = lockInnerRepeatedSegment(list, subMonitor, checkTM);
							for (IFile iFile : list) {
								String filePath = ResourceUtils.iFileToOSPath(iFile);
								int[] res = resMap.get(filePath);
								if (!checkTM) {
									int countTU = res[0];
									tuNumResult.put(filePath, countTU);
								}
								int countLockedInnerRepeatedSegment = res[1];
								lockedInnerRepeatedResault.put(filePath, countLockedInnerRepeatedSegment);
							}
							 lrsrd.setLockedInnerRepeatedResault(lockedInnerRepeatedResault);
							 if (!checkTM) {
							 lrsrd.setTuNumResult(tuNumResult);
							 }
							
						}

						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								IEditorPart editor = HandlerUtil.getActiveEditor(event);
								if (editor != null && editor instanceof XLIFFEditorImplWithNatTable) {
									XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
									xliffEditor.reloadXliff();
								}
								// lrsrd.open();
								if(hasWrongResults(list, lrsrd)){
									MessageDialog.openInformation(shell,
											Messages.getString("translation.LockRepeatedSegmentHandler.msgTitle"),
											Messages.getString("dialog.LockRepeatedSegmentResultDialog.locksuccesful"));
								}else{
									MessageDialog.openInformation(shell,
											Messages.getString("translation.LockRepeatedSegmentHandler.msgTitle"),
											Messages.getString("dialog.LockRepeatedSegmentResultDialog.locksuccesful"));
								}
								lrsrd.close();
								HsMultiActiveCellEditor.refrushCellsEditAbility();
							}
						});
					} finally {
					
						monitor.done();
					}
				}
			};
			try {
				new ProgressMonitorDialog(shell).run(true, true, runnable);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 专门处理以 nattble 形式打开的文件
	 * @param iFileList
	 * @param isLockTM100Segment
	 * @param isLockTM101Segment
	 * @param monitor
	 * @return ;
	 */
	private LockTMSegment lockTMSegmentOFEditor(List<IFile> iFileList, boolean isLockTM100Segment,
			boolean isLockTM101Segment, IProgressMonitor monitor) {
		XLFHandler xlfHandler = nattable.getXLFHandler();
		SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, iFileList.size(),
				SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		subMonitor.beginTask("", 10);
		subMonitor.setTaskName(Messages.getString("translation.LockRepeatedSegmentHandler.task2"));

		// 解析文件，占 1/10，这里是直接获取编辑器的XLFHandler,故不需解析
		if (!monitorWork(subMonitor, 1)) {
			return null;
		}

		List<String> filesPath = ResourceUtils.IFilesToOsPath(iFileList);
		LockTMSegment lts = new LockTMSegment(xlfHandler, tmMatcher, filesPath, curProject);
		lts.setLockedContextMatch(isLockTM101Segment);
		lts.setLockedFullMatch(isLockTM100Segment);
		// 查记忆库并锁定，占剩下的 9/10。
		IProgressMonitor subSubMonitor = new SubProgressMonitor(monitor, 9,
				SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		if (!lts.executeTranslation(subSubMonitor)) {
			subSubMonitor.done();
			subMonitor.done();
			isCancel = true;
			return null;
		}
		subSubMonitor.done();
		subMonitor.done();

		if (nattable != null) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					nattable.getTable().redraw();
				}
			});
		}
		Map<String, List<String>> needLockRowIdMap = lts.getNeedLockRowIdMap();
		if (needLockRowIdMap.size() > 0) {
			lockTU(xlfHandler, needLockRowIdMap);
		}
		return lts;
	}

	private LockTMSegment lockTMSegment(final List<IFile> iFileList, boolean isLockTM100Segment,
			boolean isLockTM101Segment, IProgressMonitor monitor) {
		SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, iFileList.size(),
				SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		subMonitor.beginTask("", 10);
		subMonitor.setTaskName(Messages.getString("translation.LockRepeatedSegmentHandler.task2"));
		XLFHandler xlfHandler = null;
		singleNattable = null;
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IEditorReference[] editorRefer = window.getActivePage().findEditors(
						new FileEditorInput(iFileList.get(0)), XLIFF_EDITOR_ID,
						IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
				if (editorRefer.length > 0) {
					singleNattable = ((XLIFFEditorImplWithNatTable) editorRefer[0].getEditor(true));
				}
			}
		});
		if (singleNattable != null) {
			xlfHandler = singleNattable.getXLFHandler();
		}

		if (xlfHandler == null) {
			xlfHandler = new XLFHandler();
			for (final IFile iFile : iFileList) {
				File file = iFile.getLocation().toFile();
				try {
					Map<String, Object> resultMap = xlfHandler.openFile(file);
					if (resultMap == null
							|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap
									.get(Constant.RETURNVALUE_RESULT)) {
						// 打开文件失败。
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openInformation(shell, Messages
										.getString("translation.LockRepeatedSegmentHandler.msgTitle"), MessageFormat
										.format(Messages.getString("translation.LockRepeatedSegmentHandler.msg2"),
												iFile.getLocation().toOSString()));
							}
						});
						list.remove(iFile);
						return null;
					}
				} catch (Exception e) {
					LOGGER.error("", e);
					e.printStackTrace();
				}
				if (!monitorWork(monitor, 1)) {
					return null;
				}
			}
		} else {
			subMonitor.worked(1);
		}

		List<String> filesPath = ResourceUtils.IFilesToOsPath(iFileList);
		LockTMSegment lts = new LockTMSegment(xlfHandler, tmMatcher, filesPath, curProject);
		lts.setLockedContextMatch(isLockTM101Segment);
		lts.setLockedFullMatch(isLockTM100Segment);
		// 查记忆库并锁定，占剩下的 9/10。
		SubProgressMonitor subSubMonitor = new SubProgressMonitor(subMonitor, 9,
				SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		if (!lts.executeTranslation(subSubMonitor)) {
			isCancel = true;
			subSubMonitor.done();
			subMonitor.done();
			return null;
		}
		subSubMonitor.done();
		subMonitor.done();

		if (singleNattable != null) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					singleNattable.getTable().redraw();
				}
			});
		}
		Map<String, List<String>> needLockRowIdMap = lts.getNeedLockRowIdMap();
		if (needLockRowIdMap.size() > 0) {
			lockTU(xlfHandler, needLockRowIdMap);
		}
		return lts;
	}

	/**
	 * 概据内部匹配结果，锁定文本段。
	 * @param xlfHandler
	 * @param rowIdMap
	 */
	private void lockTU(final XLFHandler xlfHandler, Map<String, List<String>> rowIdMap) {
		Iterator<Entry<String, List<String>>> it = rowIdMap.entrySet().iterator();
		while (it.hasNext()) {
			isLocked = false;
			final Entry<String, List<String>> rowIdsEntry = it.next();
			final String fileLC = rowIdsEntry.getKey();
			// 查看该文件是否打开，若打开，则获editor的handler，若未打开，则直接使用当前handler
			final IEditorInput input = new FileEditorInput(ResourceUtils.fileToIFile(fileLC));
			final IEditorReference[] editorRefes = window.getActivePage().getEditorReferences();

			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					for (int i = 0; i < editorRefes.length; i++) {
						if (XLIFF_EDITOR_ID.equals(editorRefes[i].getId())) {
							// 先判断打开单个文件的情况
							XLIFFEditorImplWithNatTable nattable = (XLIFFEditorImplWithNatTable) (editorRefes[i]
									.getEditor(true));
							if (!nattable.isMultiFile()) {
								if (nattable.getEditorInput().equals(input)) {
									nattable.getXLFHandler().lockTransUnits(rowIdsEntry.getValue(), true);
									isLocked = true;
									nattable.getTable().redraw();
								}
							} else {
								// 这是合并打开的情况
								if (nattable.getMultiFileList().indexOf(new File(fileLC)) >= 0) {
									nattable.getXLFHandler().lockTransUnits(rowIdsEntry.getValue(), true);
									isLocked = true;
									nattable.getTable().redraw();
								}
								;
							}
						}
					}
					// 如果未被锁定（当前文件没有打开），就调用当前XLFHandler去锁定所有文本段
					if (!isLocked) {
						xlfHandler.lockTransUnits(rowIdsEntry.getValue(), true);
					}
				}
			});
		}
	}

	/**
	 * 锁定内部重复文本段
	 * @param iFile
	 * @param monitor
	 * @return ;
	 */
	private Map<String, int[]> lockInnerRepeatedSegment(List<IFile> iFileList, IProgressMonitor monitor, boolean checkTM) {
		Map<String, int[]> repeatedMap = new HashMap<String, int[]>();
		final XLFHandler handler = new XLFHandler();
		Map<String, Integer> lockedSizeMap = new HashMap<String, Integer>();
		monitor.beginTask(Messages.getString("translation.LockRepeatedSegmentHandler.task3"), iFileList.size() * 3);
		List<IFile> removeiFileList = new ArrayList<IFile>();

		for (final IFile iFile : iFileList) {
			File file = iFile.getLocation().toFile();
			try {
				Map<String, Object> resultMap = handler.openFile(file);
				if (resultMap == null
						|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap
								.get(Constant.RETURNVALUE_RESULT)) {
					// 打开文件失败。
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openInformation(shell, Messages
									.getString("translation.LockRepeatedSegmentHandler.msgTitle"), MessageFormat
									.format(Messages.getString("translation.LockRepeatedSegmentHandler.msg2"), iFile
											.getLocation().toOSString()));
						}
					});
					removeiFileList.add(iFile);
					repeatedMap.put(file.getPath(), new int[] { 0, 0 });
				}

			} catch (Exception e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
			lockedSizeMap.put(iFile.getLocation().toOSString(), 0);
			if (!monitorWork(monitor, 1)) {
				return null;
			}
		}

		Map<String, ArrayList<String>> languages = handler.getLanguages();
		for (Entry<String, ArrayList<String>> entry : languages.entrySet()) {
			String srcLanguage = entry.getKey();
			for (String tgtLanguage : entry.getValue()) {
				ArrayList<String> rowIds = handler.getRepeatedSegmentExceptFirstOne(srcLanguage, tgtLanguage);

				// 将所有的RowId进行按文件名排序
				Map<String, List<String>> lockedRowidsMap = RowIdUtil.groupRowIdByFileName(rowIds);
				// 将锁定的文件本个数添加到map中
				for (String rowId : rowIds) {
					String fileLC = RowIdUtil.getFileNameByRowId(rowId);
					lockedSizeMap.put(fileLC, lockedSizeMap.get(fileLC) + 1);
				}
				if (!monitorWork(monitor, iFileList.size())) {
					isCancel = true;
					return null;
				}
				lockTU(handler, lockedRowidsMap);
			}
		}

		// 如果没有进行外部匹配，那么就必须获取每个文件的TU节点数量
		for (IFile iFile : iFileList) {
			if (removeiFileList.indexOf(iFile) >= 0) {
				continue;
			}
			String iFileLc = iFile.getLocation().toOSString();
			repeatedMap.put(iFileLc,
					new int[] { checkTM ? -1 : handler.countTransUnit(iFileLc), lockedSizeMap.get(iFileLc) });
			if (!monitorWork(monitor, 1)) {
				isCancel = true;
				return null;
			}
		}

		monitor.done();
		return repeatedMap;
	}

	@Override
	public String[] getLegalFileExtensions() {
		return CommonFunction.xlfExtesionArray;
	}

	/**
	 * 进度条前进管理方法，如果返回false,则表示退出操作
	 * @param monitor
	 * @param interval
	 * @return
	 */
	private boolean monitorWork(IProgressMonitor monitor, int interval) {
		if (monitor.isCanceled()) {
			isCancel = true;
			monitor.done();
			return false;
		}
		monitor.worked(interval);
		return true;
	}

	/**
	 * 修改重复锁定结果是否有错误
	 * @param iFileList
	 * @param lrsrd ;
	 */
	private boolean  hasWrongResults(List<IFile> iFileList ,LockRepeatedSegmentResultDialog lrsrd){
		for(IFile iFile :iFileList ){
			String filePath = ResourceUtils.iFileToOSPath(iFile);
			//上下文匹配结果			 
			int lockedContextResult = lrsrd.getLockedContextResult(filePath);
			if(-1 ==lockedContextResult && isLockTM101Segment ){
				return true;
			}
			// 内部重复
			int lockedInnerRepeatedResault = lrsrd.getLockedInnerRepeatedResault(filePath);		
			if(-1 ==lockedInnerRepeatedResault && isLockInnerRepeatedSegment ){
				return true;
			}
			// 全部匹配
			int lockedFullMatchResult = lrsrd.getLockedFullMatchResult(filePath);	
			if(-1 ==lockedFullMatchResult && isLockTM100Segment ){
				return true;
			}
		}
		
		return false;
	}
}
