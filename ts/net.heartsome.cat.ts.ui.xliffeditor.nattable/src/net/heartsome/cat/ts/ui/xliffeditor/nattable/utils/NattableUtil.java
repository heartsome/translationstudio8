package net.heartsome.cat.ts.ui.xliffeditor.nattable.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.tb.importer.TbImporter;
import net.heartsome.cat.ts.tm.importer.TmImporter;
import net.heartsome.cat.ts.tm.importer.extension.ITmImporter;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;
import net.heartsome.cat.ts.ui.util.IntelligentTagPrcessor;
import net.heartsome.cat.ts.ui.view.IMatchViewPart;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.UpdateDataBean;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.AddSegmentToTMPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.SignOffPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.UnTranslatedPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.qa.AutomaticQATrigger;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable.MergeSegmentOperation;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable.NeedsReviewOperation;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable.SendTOTmOperation;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable.StateOperation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.services.IEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于对 Nattable 中的文本段进行处理的工具类
 * @author peason
 * @version
 * @since JDK1.6
 */
public class NattableUtil {

	public static final Logger LOGGER = LoggerFactory.getLogger(NattableUtil.class);

	private static NattableUtil instance;

	private XLIFFEditorImplWithNatTable xliffEditor;

	private TmImporter importer;

	// /** 首选项存取器 */
	// private IPreferenceStore store = net.heartsome.cat.ts.ui.Activator
	// .getDefault().getPreferenceStore();

	public static synchronized NattableUtil getInstance(XLIFFEditorImplWithNatTable xliffEditor) {
		if (instance == null) {
			instance = new NattableUtil(xliffEditor);
		} else {
			if (xliffEditor != instance.xliffEditor) {
				instance.importer.clearResources();
			}
			instance.xliffEditor = xliffEditor;
		}
		return instance;
	}

	private NattableUtil(XLIFFEditorImplWithNatTable xliffEditor) {
		this.xliffEditor = xliffEditor;
		this.importer = TmImporter.getInstance();
	}

	/**
	 * 批准或取消批准文本段
	 * @param selectedRowIds1
	 *            选中行的rowId集合
	 * @param approve
	 *            true：批准；false：取消批准;
	 */
	public void approveTransUnits(boolean isJumpNext) {
		List<String> selectedRowIds = xliffEditor.getSelectedRowIds();
		if (selectedRowIds.size() == 0) {
			return;
		}

		final Map<String, List<String>> tmpGroup = RowIdUtil.groupRowIdByFileName(selectedRowIds);
		boolean hasEmpty = false;
		XLFHandler handler = xliffEditor.getXLFHandler();
		for (Entry<String, List<String>> entry : tmpGroup.entrySet()) {
			// 目标文本为空不能执行批准，将要跳过
			List<String> rowIds = entry.getValue();
			int size = rowIds.size();
			handler.removeNullTgtContentRowId(rowIds);
			if (rowIds.size() != size) {
				hasEmpty = true;
			}
			handler.removeLockedRowIds(rowIds);
		}

		// 入库前进行品质检查 --robert
		int i = 0;
		final AutomaticQATrigger auto = new AutomaticQATrigger(xliffEditor.getXLFHandler());
		for (Entry<String, List<String>> entry : tmpGroup.entrySet()) {
			List<String> rowIdList = tmpGroup.get(entry.getKey());
			for (Iterator<String> it = rowIdList.iterator(); it.hasNext();) {
				String rowId = it.next();
				String result = auto.beginAutoQa(false, rowId, i == 0 ? true : false);
				if (result == null) {
					return;
				}
				if (result.length() > 1) {
					boolean respons = MessageDialog.openConfirm(xliffEditor.getSite().getShell(),
							Messages.getString("translation.ApproveSegmentHandler.msgTitle"), result);
					// 若选择ok，则继续操作
					if (!respons) {
						auto.bringQAResultViewerToTop();
						it.remove();
						selectedRowIds.remove(rowId);
					}
				}
				i++;
			}
		}
		auto.informQAEndFlag();

		String message = null;
		if (hasEmpty) {
			message = Messages.getString("utils.NattableUtil.msg1");
		}
		if (message != null) {
			if (!MessageDialog.openConfirm(xliffEditor.getTable().getShell(),
					Messages.getString("utils.NattableUtil.msgTitle"), message)) {
				return;
			}
		}
		if (selectedRowIds.size() == 0) {
			return;
		}
		HsMultiActiveCellEditor.commit(true);
		// 将选中的文本段添加记忆库,在生成TMX的过程中会过滤掉标记不添加到记忆库的文本段
		final IProject project = ((FileEditorInput) (xliffEditor.getEditorInput())).getFile().getProject();
		this.importer.setProject(project);
		final int contextSize = importer.getContextSize();
		BusyIndicator.showWhile(xliffEditor.getSite().getShell().getDisplay(), new Runnable() {
			public void run() {
				// monitor.beginTask(Messages.getString("utils.NattableUtil.task1"), 8);
				if (!CommonFunction.checkEdition("L")) {
					if (!importer.checkImporter()
							&& ProjectConfigerFactory.getProjectConfiger(project).getDefaultTMDb() != null) {
						final boolean[] state = new boolean[] { true };
						Display.getDefault().syncExec(new Runnable() {

							public void run() {
								state[0] = MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
										Messages.getString("utils.NattableUtil.msgTitle2"),
										Messages.getString("utils.NattableUtil.msg.cantConnDefaultDb"));
							}
						});
						if (!state[0]) {
							return;
						}
					}
				}
				final List<String> addToTmResultRowIds = new ArrayList<String>();
				for (Entry<String, List<String>> entry : tmpGroup.entrySet()) {
					List<String> rowIdList = tmpGroup.get(entry.getKey());
					if (importer.checkImporter()) {
						String systemUser = Activator.getDefault().getPreferenceStore()
								.getString(IPreferenceConstants.SYSTEM_USER);
						StringBuffer fileContent = xliffEditor.getXLFHandler().generateTMXFileContent(systemUser,
								rowIdList, xliffEditor.getSrcColumnName(), xliffEditor.getTgtColumnName(),
								/* new SubProgressMonitor(monitor, 3) */null, contextSize, project);
						// if (monitor.isCanceled()) {
						// return;
						// }
						if (fileContent != null) {
							int state = -1;
							try {
								state = importer.executeImport(fileContent.toString(), xliffEditor.getSrcColumnName(),
								/* monitor */null);
							} catch (ImportException e) {
								final String msg = e.getMessage();
								Display.getDefault().syncExec(new Runnable() {

									public void run() {
										MessageDialog.openInformation(Display.getDefault().getActiveShell(),
												Messages.getString("utils.NattableUtil.msgTitle"), msg);
									}
								});
								return;
							}
							if (state == ITmImporter.IMPORT_STATE_FAILED) {
								Display.getDefault().syncExec(new Runnable() {

									public void run() {
										MessageDialog.openInformation(Display.getDefault().getActiveShell(),
												Messages.getString("utils.NattableUtil.msgTitle"),
												Messages.getString("utils.NattableUtil.msg2"));
									}
								});
								return;
							} else if (state == ITmImporter.IMPORT_STATE_NODB) {
								Display.getDefault().syncExec(new Runnable() {

									public void run() {
										MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
												Messages.getString("utils.NattableUtil.msgTitle"),
												Messages.getString("utils.NattableUtil.msg3"));
									}
								});
							}
						}
					}
					addToTmResultRowIds.addAll(rowIdList);
				}

				// monitor.setTaskName(Messages.getString("utils.NattableUtil.task2"));
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						// 修改文本段状态,目前没有取消批准的功能，所以直接传入true,即所有的操作都是批准
						List<String> rowIds = xliffEditor.getXLFHandler().approveTransUnits(addToTmResultRowIds, true);
						if (rowIds.size() > 0) {
							String message;
							if (addToTmResultRowIds != null && addToTmResultRowIds.size() == 1) {
								message = Messages.getString("utils.NattableUtil.msg4");
							} else {
								message = MessageFormat.format(Messages.getString("utils.NattableUtil.msg5"),
										rowIds.size());
							}
							boolean res = MessageDialog.openQuestion(xliffEditor.getTable().getShell(), null, message);
							if (res) {
								xliffEditor.getXLFHandler().approveTransUnits(rowIds, true, false);
							}
						}
						xliffEditor.updateStatusLine();
						xliffEditor.getTable().redraw();
					}
				});
				// monitor.worked(2);
				// if (monitor.isCanceled()) {
				// return;
				// }
				//
				// // 批准时需要进行繁殖翻译
				// // propagateTranslations(addToTmResultRowIds, new
				// // SubProgressMonitor(monitor, 2));
				// monitor.done();
			}
		});

		// try {
		// new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, true,
		// runnable);
		if (isJumpNext) {
			int[] selectedRows = xliffEditor.getSelectedRows();
			Arrays.sort(selectedRows);
			xliffEditor.jumpToRow(selectedRows[selectedRows.length - 1] + 1);
		} else {
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.findView("net.heartsome.cat.ts.ui.translation.view.matchview");
			int[] selected = xliffEditor.getSelectedRows();
			if (viewPart != null && viewPart instanceof IMatchViewPart && selected.length != 0) {
				((IMatchViewPart) viewPart).reLoadMatches(xliffEditor, selected[selected.length - 1]);
			}
			HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
		}
		// } catch (InvocationTargetException e) {
		// e.printStackTrace();
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
	}

	/**
	 * 锁定或取消锁定文本段
	 * @param selectedRowIds
	 *            选中行的rowId集合
	 * @param lock
	 *            true：锁定；false：取消锁定;
	 */
	public void lockTransUnits(List<String> selectedRowIds, boolean lock) {
		xliffEditor.getXLFHandler().lockTransUnits(selectedRowIds, lock);
		xliffEditor.getTable().redraw();
		// IOperationHistory operationHistory =
		// OperationHistoryFactory.getOperationHistory();
		// try {
		// operationHistory
		// .execute(
		// new LockOperation("Lock", xliffEditor.getTable(), selectedRowIds,
		// xliffEditor
		// .getXLFHandler(), lock), null, null);
		// } catch (ExecutionException e) {
		// e.printStackTrace();
		// }
	}

	/**
	 * 改变Target的状态,signed-off需要修改approved=yes,改为new或translated需要删除approved=yes属性
	 * @param state
	 *            状态值("new", "final", "translated", "signed-off", "needs-adaptation", "needs-review-adaptation",
	 *            "needs-l10n", "needs-review-l10n", "needs-translation", "needs-review-translation");
	 */
	public void changeTgtState(List<String> selectedRowIds, String state) {
		if (selectedRowIds != null && selectedRowIds.size() > 0) {
			xliffEditor.getXLFHandler().changeTransUnitState(selectedRowIds, state);
			xliffEditor.updateStatusLine();
			xliffEditor.getTable().redraw();
			NattableUtil.refreshCommand(AddSegmentToTMPropertyTester.PROPERTY_NAMESPACE,
					AddSegmentToTMPropertyTester.PROPERTY_ENABLED);
			NattableUtil.refreshCommand(SignOffPropertyTester.PROPERTY_NAMESPACE,
					SignOffPropertyTester.PROPERTY_ENABLED);
			NattableUtil.refreshCommand(UnTranslatedPropertyTester.PROPERTY_NAMESPACE,
					UnTranslatedPropertyTester.PROPERTY_ENABLED);
		}
	}

	/**
	 * 添加或者取消疑问
	 * @param selectedRowIds
	 * @param state
	 *            ;
	 */
	public void changIsQuestionState(List<String> selectedRowIds, String state) {
		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
		try {
			operationHistory.execute(new NeedsReviewOperation("need-review", xliffEditor.getTable(), selectedRowIds,
					xliffEditor.getXLFHandler(), state), null, null);
		} catch (ExecutionException e) {
			LOGGER.error("", e);
			MessageDialog.openError(xliffEditor.getSite().getShell(),
					Messages.getString("utils.NattableUtil.msgTitle2"), e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 设置是否添加到记忆库
	 * @param selectedRowIds
	 * @param state
	 *            "yes" or "no";
	 */
	public void changeSendToTmState(List<String> selectedRowIds, String state) {
		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
		try {
			operationHistory.execute(new SendTOTmOperation("send-to-tm", xliffEditor.getTable(), selectedRowIds,
					xliffEditor.getXLFHandler(), state), null, null);
		} catch (ExecutionException e) {
			LOGGER.error("", e);
			MessageDialog.openError(xliffEditor.getSite().getShell(),
					Messages.getString("utils.NattableUtil.msgTitle2"), e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 改变Target的状态
	 * @param state
	 *            状态值("new", "final", "translated", "signed-off", "needs-adaptation", "needs-review-adaptation",
	 *            "needs-l10n", "needs-review-l10n", "needs-translation", "needs-review-translation");
	 */
	public void changeTgtState(final List<String> selectedRowIds, final String state, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(Messages.getString("utils.NattableUtil.task4"), 1);
		monitor.worked(1);
		final IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
		final IProgressMonitor monitor2 = monitor;

		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				try {

					operationHistory.execute(new StateOperation("State", xliffEditor.getTable(), selectedRowIds,
							xliffEditor.getXLFHandler(), state), monitor2, null);
				} catch (ExecutionException e) {
					LOGGER.error("", e);
					MessageDialog.openError(xliffEditor.getSite().getShell(),
							Messages.getString("utils.NattableUtil.msgTitle2"), e.getMessage());
					e.printStackTrace();
				}
			}
		});

		monitor.done();
	}

	/**
	 * 添加选中文本段到记忆库
	 * @param modelBean
	 *            数据库元数据
	 * @return boolean true : 完成入库　false 不完成入库
	 */
	public boolean addSelectSegmentToTM() {
		List<String> selectedRowIds = xliffEditor.getSelectedRowIds();
		if (selectedRowIds.size() == 0) {
			return false;
		}

		final Map<String, List<String>> tmpGroup = RowIdUtil.groupRowIdByFileName(selectedRowIds);
		boolean hasEmpty = false;
		XLFHandler handler = xliffEditor.getXLFHandler();
		for (Entry<String, List<String>> entry : tmpGroup.entrySet()) {
			// 目标文本为空不添加到记忆库
			List<String> rowIds = entry.getValue();
			int size = rowIds.size();
			handler.removeNullTgtContentRowId(rowIds);
			if (rowIds.size() != size) {
				hasEmpty = true;
			}
			handler.removeLockedRowIds(rowIds);
		}

		// 入库前进行品质检查 --robert
		final AutomaticQATrigger auto = new AutomaticQATrigger(xliffEditor.getXLFHandler());
		int i = 0;
		for (Entry<String, List<String>> entry : tmpGroup.entrySet()) {
			List<String> rowIdList = tmpGroup.get(entry.getKey());
			for (Iterator<String> it = rowIdList.iterator(); it.hasNext();) {
				String rowId = it.next();
				String result = auto.beginAutoQa(true, rowId, i == 0 ? true : false);
				if (result == null) {
					return false;
				}
				if (result.length() > 1) {
					boolean respons = MessageDialog.openConfirm(xliffEditor.getTable().getShell(),
							Messages.getString("utils.NattableUtil.msgTitle3"), result);
					// 若选择ok，则继续操作
					if (!respons) {
						auto.bringQAResultViewerToTop();
						it.remove();
						selectedRowIds.remove(rowId);
					}
				}
				i++;
			}
		}
		auto.informQAEndFlag();

		if (selectedRowIds.size() <= 0) {
			return false;
		}

		String message = null;
		if (hasEmpty) {
			message = Messages.getString("utils.NattableUtil.msg1");
		}
		if (message != null) {
			if (!MessageDialog.openConfirm(xliffEditor.getTable().getShell(),
					Messages.getString("utils.NattableUtil.msgTitle"), message)) {
				return false;
			}
		}

		final ArrayList<String> lstRowId = new ArrayList<String>();

		final IProject project = ((FileEditorInput) (xliffEditor.getEditorInput())).getFile().getProject();
		this.importer.setProject(project);
		final int contextSize = importer.getContextSize();

		// IRunnableWithProgress runnable = new IRunnableWithProgress() {
		// public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		BusyIndicator.showWhile(xliffEditor.getSite().getShell().getDisplay(), new Runnable() {
			public void run() {
				// monitor.setTaskName(Messages.getString("utils.NattableUtil.task5"));
				// monitor.beginTask(Messages.getString("utils.NattableUtil.task5"), 2);
				if (!CommonFunction.checkEdition("L")) {
					if (!importer.checkImporter()
							&& ProjectConfigerFactory.getProjectConfiger(project).getDefaultTMDb() != null) {
						final boolean[] state = new boolean[] { true };
						Display.getDefault().syncExec(new Runnable() {

							public void run() {
								state[0] = MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
										Messages.getString("utils.NattableUtil.msgTitle2"),
										Messages.getString("utils.NattableUtil.msg.cantConnDefaultDb"));
							}
						});
						if (!state[0]) {
							return;
						}
					}
				}
				for (Entry<String, List<String>> entry : tmpGroup.entrySet()) {
					List<String> rowIdList = tmpGroup.get(entry.getKey());
					if (importer.checkImporter()) {
						String systemUser = Activator.getDefault().getPreferenceStore()
								.getString(IPreferenceConstants.SYSTEM_USER);
						StringBuffer fileContent = xliffEditor.getXLFHandler().generateTMXFileContent(systemUser,
								rowIdList, xliffEditor.getSrcColumnName(), xliffEditor.getTgtColumnName(),
								/* new SubProgressMonitor(monitor, 1) */null, contextSize, project);
						// if (monitor.isCanceled()) {
						// monitor.setTaskName(Messages.getString("utils.NattableUtil.task6"));
						// throw new OperationCanceledException();
						// }
						if (fileContent != null) {
							int state = -1;
							try {
								state = importer.executeImport(fileContent.toString(), xliffEditor.getSrcColumnName(),
								/* monitor */null);
							} catch (ImportException e) {
								final String msg = e.getMessage();
								Display.getDefault().syncExec(new Runnable() {

									public void run() {
										MessageDialog.openInformation(Display.getDefault().getActiveShell(),
												Messages.getString("utils.NattableUtil.msgTitle"), msg);
									}
								});
								return;
							}
							if (state == ITmImporter.IMPORT_STATE_FAILED) {
								Display.getDefault().syncExec(new Runnable() {

									public void run() {
										MessageDialog.openInformation(xliffEditor.getTable().getShell(),
												Messages.getString("utils.NattableUtil.msgTitle"),
												Messages.getString("utils.NattableUtil.msg6"));
									}
								});
								return;
							} else if (state == ITmImporter.IMPORT_STATE_NODB) {
								Display.getDefault().syncExec(new Runnable() {

									public void run() {
										MessageDialog.openInformation(xliffEditor.getTable().getShell(),
												Messages.getString("utils.NattableUtil.msgTitle"),
												Messages.getString("utils.NattableUtil.msg7"));
									}
								});
							}
						}
					}
					// Bug #2306:文本段添加不入库标记后不能改变为完成翻译状态
					lstRowId.addAll(rowIdList);
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						changeTgtState(lstRowId, "translated");
						// xliffEditor.updateStatusLine();
					}
				});
				// monitor.done();
			}
		});

		// try {
		// new ProgressMonitorDialog(xliffEditor.getTable().getShell()).run(true, true, runnable);
		// } catch (InvocationTargetException e) {
		// e.printStackTrace();
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		return true;
	}

	// /**
	// * 获得记忆库更新策略
	// * @return ;
	// */
	// public int getTmxImportStrategy() {
	// IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
	// return ps.getInt(PreferenceConstants.TM_UPDATE);
	// }

	/**
	 * 繁殖翻译 robert
	 * @param rowIdsMap
	 * @param monitor
	 * @return
	 */
	public IStatus propagateTranslations(Map<String, List<String>> rowIdsMap, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 9,
				SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		subMonitor.beginTask(Messages.getString("utils.NattableUtil.task7"), rowIdsMap.keySet().size());

		Iterator<Entry<String, List<String>>> it = rowIdsMap.entrySet().iterator();
		final XLFHandler handler = xliffEditor.getXLFHandler();

		while (it.hasNext()) {
			Entry<String, List<String>> entry = it.next();
			// 这是源文本，也就是繁殖翻译中的父
			String rootRowId = entry.getKey();
			// 这是要被繁殖的所有rowIds，其源文与rootRowId的源文一致
			final List<String> rowIds = entry.getValue();
			// TransUnitBean tu = handler.getTransUnit(rootRowId);
			// String tgtContent = tu.getTgtContent();
			final String rootTgtPureText = handler.getTUPureTextByRowId(rootRowId, false);

			String rootSrcFullText = handler.getTUFullTextByRowId(rootRowId, true);
			String rootTgtFullText = handler.getTUFullTextByRowId(rootRowId, false);
			for (String rowId : rowIds) {
				String temp = rootTgtPureText;
				String srcFullText = handler.getTUFullTextByRowId(rowId, true);
				if (srcFullText.trim().equals(rootSrcFullText.trim())) {
					temp = rootTgtFullText;
				} else {
					temp = IntelligentTagPrcessor.intelligentAppendTag(srcFullText, rootTgtFullText);
				}
				handler.changeTgtTextValue(rowId, temp, null, null);
			}

			// 下面这是处理处于获得焦点状态的文本段。无法繁殖翻译的情况
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					int focusRowIndex = HsMultiActiveCellEditor.sourceRowIndex;
					if (focusRowIndex == -1) {
						return;
					}
					if (!XLIFFEditorImplWithNatTable.getCurrent().isHorizontalLayout()) {
						focusRowIndex = VerticalNatTableConfig.getRealRowIndex(focusRowIndex);
					}
					String focusRowId = handler.getRowId(focusRowIndex);
					if (rowIds.contains(focusRowId)) {
						HsMultiActiveCellEditor.getTargetStyledEditor().setCanonicalValue(
								new UpdateDataBean(rootTgtPureText, null, null));
					}
				}
			});

			if (subMonitor.isCanceled()) {
				return Status.OK_STATUS;
			}
			subMonitor.worked(1);
		}
		subMonitor.done();
		return Status.OK_STATUS;
	}

	/**
	 * 刷新 Command 的可用状态
	 * @param nameSpace
	 * @param properties
	 *            ;
	 */
	public static void refreshCommand(String nameSpace, String properties) {
		if (nameSpace != null && properties != null) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IEvaluationService evaluationService = (IEvaluationService) window.getService(IEvaluationService.class);
			if (evaluationService != null) {
				evaluationService.requestEvaluation(nameSpace + "." + properties);
			}
		}
	}

	/**
	 * 合并文本段 ;
	 */
	public void mergeSegment() {
		XLFHandler handler = xliffEditor.getXLFHandler();
		List<String> lstRowId = xliffEditor.getSelectedRowIds();
		List<String> lstAllRowId = xliffEditor.getXLFHandler().getAllRowIds();
		Shell shell = xliffEditor.getSite().getShell();
		if (lstRowId.size() < 2) {
			MessageDialog.openInformation(shell, Messages.getString("utils.NattableUtil.mergeSegment.msgTitle"),
					Messages.getString("utils.NattableUtil.mergeSegment.msg1"));
			return;
		}
		Collections.sort(lstRowId, new SortRowIdComparator());
		Collections.sort(lstAllRowId, new SortRowIdComparator());
		String rowId1 = lstRowId.get(0);
		String fileName = RowIdUtil.getFileNameByRowId(rowId1);
		if (fileName == null) {
			return;
		}
		if (handler.isLocked(rowId1)) {
			MessageDialog.openInformation(shell, Messages.getString("utils.NattableUtil.mergeSegment.msgTitle"),
					Messages.getString("utils.NattableUtil.mergeSegment.msg3"));
			return;
		}
		for (int i = 1; i < lstRowId.size(); i++) {
			String rowId = lstRowId.get(i);
			if (handler.isLocked(rowId)) {
				MessageDialog.openInformation(shell, Messages.getString("utils.NattableUtil.mergeSegment.msgTitle"),
						Messages.getString("utils.NattableUtil.mergeSegment.msg3"));
				return;
			}
			String fileName2 = RowIdUtil.getFileNameByRowId(rowId);
			// 数组集合必须在一个文件中才能合并
			if (fileName2 == null || !fileName.equals(fileName2)) {
				MessageDialog.openInformation(shell, Messages.getString("utils.NattableUtil.mergeSegment.msgTitle"),
						Messages.getString("utils.NattableUtil.mergeSegment.msg4"));
				return;
			}
			// 判断所选文本段是否连续
			String strCurTuId = RowIdUtil.getTUIdByRowId(rowId);
			String strPreTuId = RowIdUtil.getTUIdByRowId(lstRowId.get(i - 1));
			if (strCurTuId == null || strPreTuId == null) {
				return;
			}

			if ((lstAllRowId.indexOf(rowId) - lstAllRowId.indexOf(lstRowId.get(i - 1))) != 1) {
				MessageDialog.openInformation(shell, Messages.getString("utils.NattableUtil.mergeSegment.msgTitle"),
						Messages.getString("utils.NattableUtil.mergeSegment.msg5"));
				return;
			} else {
				String curOriginal = RowIdUtil.getOriginalByRowId(rowId);
				String preOriginal = RowIdUtil.getOriginalByRowId(lstRowId.get(i - 1));
				if (!curOriginal.equals(preOriginal)) {
					MessageDialog.openInformation(shell,
							Messages.getString("utils.NattableUtil.mergeSegment.msgTitle"),
							Messages.getString("utils.NattableUtil.mergeSegment.msg5"));
					return;
				}
			}
		}

		// Bug #2373:选择全部文本段合并后，无显示内容
		if (lstRowId.size() == xliffEditor.getXLFHandler().getRowIds().size()) {
			xliffEditor.jumpToRow(0);
		}

		MergeSegmentOperation mergeOper = new MergeSegmentOperation("merge segment", xliffEditor, handler, lstRowId);
		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
		try {
			operationHistory.execute(mergeOper, null, null);
		} catch (Exception e) {
			LOGGER.error("", e);
		}

	}

	/**
	 * 对 RowId 进行排序的类
	 * @author peason
	 * @version
	 * @since JDK1.6
	 */
	private class SortRowIdComparator implements Comparator<String> {
		public int compare(String arg0, String arg1) {
			String pre0 = arg0.substring(0, arg0.lastIndexOf(RowIdUtil.SPLIT_MARK));
			String pre1 = arg1.substring(0, arg1.lastIndexOf(RowIdUtil.SPLIT_MARK));
			if (pre0.equals(pre1)) {
				String tuId0 = RowIdUtil.getTUIdByRowId(arg0);
				String tuId1 = RowIdUtil.getTUIdByRowId(arg1);
				if (tuId0 != null && tuId1 != null) {
					String[] arr0 = tuId0.split("-");
					String[] arr1 = tuId1.split("-");
					for (int i = 0; i < Math.min(arr0.length, arr1.length); i++) {
						int value0 = Integer.parseInt(arr0[i]);
						int value1 = Integer.parseInt(arr1[i]);
						if (value0 == value1) {
							continue;
						} else if (value0 > value1) {
							return 1;
						} else {
							return -1;
						}
					}
					return 0;
				} else if (tuId0 == null && tuId1 == null) {
					return 0;
				} else if (tuId0 != null) {
					return 1;
				} else {
					return -1;
				}
			} else {
				return pre0.compareTo(pre1);
			}
		}

	}

	/**
	 * 对选中文本段执行签发操作 ;
	 */
	public void changeToSignedOffState() {
		List<String> selectedRowIds = getRowIdsNoEmptyTranslate(true);
		for (int i = 0; i < selectedRowIds.size(); i++) {
			String rowId = selectedRowIds.get(i);
			String tgtContent = xliffEditor.getXLFHandler().getTgtContent(rowId);
			boolean isDraft = xliffEditor.getXLFHandler().isDraft(rowId);
			if (tgtContent == null || tgtContent.trim().equals("") || isDraft) {
				selectedRowIds.remove(i);
				i--;
			}
		}
		if (selectedRowIds.size() > 0) {
			changeTgtState(selectedRowIds, "signed-off");
		}
	}

	/**
	 * 对选中的文本段进行过滤
	 * @param isSignedOff
	 * @return ;
	 */
	public List<String> getRowIdsNoEmptyTranslate(boolean isSignedOff) {
		List<String> selRowIds = xliffEditor.getSelectedRowIds();
		int oldSize = selRowIds.size();
		XLFHandler handler = xliffEditor.getXLFHandler();
		handler.removeNullTgtContentRowId(selRowIds);
		boolean hasEmpty = false;
		boolean hasDraft = false;
		if (oldSize != selRowIds.size()) {
			hasEmpty = true;
		}
		if (isSignedOff) { // 判断执行签发时是否有草稿状态的文本段
			for (int i = 0; i < selRowIds.size(); i++) {
				String rowId = selRowIds.get(i);
				if (handler.isDraft(rowId)) {
					selRowIds.remove(i);
					i--;
					hasDraft = true;
				}
			}
		}
		String message = null;
		if (hasEmpty && hasDraft) {
			message = Messages.getString("utils.NattableUtil.msg8");
		} else if (hasDraft) {
			message = Messages.getString("utils.NattableUtil.msg9");
		} else if (hasEmpty) {
			message = Messages.getString("utils.NattableUtil.msg1");
		}
		if (message != null) {
			if (!MessageDialog.openConfirm(xliffEditor.getTable().getShell(),
					Messages.getString("utils.NattableUtil.msgTitle"), message)) {
				selRowIds.clear();
			}
		}
		return selRowIds;
	}

	public void releaseResource() {
		importer.setProject(null);
		TbImporter.getInstance().setProject(null);
	}
}
