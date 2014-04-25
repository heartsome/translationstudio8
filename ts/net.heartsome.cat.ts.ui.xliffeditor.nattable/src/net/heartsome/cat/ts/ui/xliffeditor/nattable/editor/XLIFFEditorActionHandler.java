package net.heartsome.cat.ts.ui.xliffeditor.nattable.editor;

import net.heartsome.cat.common.ui.listener.PartAdapter2;
import net.heartsome.cat.ts.ui.innertag.SegmentViewer;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.MixUndoBean;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.dialog.FindReplaceDialog;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy.ColumnSearchStrategy;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy.DefaultCellSearchStrategy;
import net.sourceforge.nattable.NatTable;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Handles the redirection of the global actions Cut, Copy, Paste, Delete, Select All, Find, Undo and Redo to either the
 * current XLIFF editor or the part's supplied action handler.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example usage:
 * 
 * <pre>
 * actionHandler = new XLIFFEditorActionHandler(this.getEditorSite().getActionBars());
 * actionHandler.addTextViewer(textCellEditor1);
 * actionHandler.setSelectAllAction(selectAllAction);
 * </pre>
 * 
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 * @author weachy
 * @version
 * @since JDK1.5
 * @see org.eclipse.ui.actions.TextActionHandler
 */
@SuppressWarnings("restriction")
public class XLIFFEditorActionHandler {

	private CutActionHandler textCutAction = new CutActionHandler();

	private CopyActionHandler textCopyAction = new CopyActionHandler();

	private PasteActionHandler textPasteAction = new PasteActionHandler();

	private DeleteActionHandler textDeleteAction = new DeleteActionHandler();

	private SelectAllActionHandler textSelectAllAction = new SelectAllActionHandler();

	private UndoActionHandler textUndoAction = new UndoActionHandler();

	private RedoActionHandler textRedoAction = new RedoActionHandler();

	private FindReplaceActionHandler textFindReplaceAction = new FindReplaceActionHandler();

	private IActionBars actionBar;

	private MixUndoBean undoBean = new MixUndoBean();

	/**
	 * Creates a <code>StyledText</code> control action handler for the global Cut, Copy, Paste, Delete, and Select All
	 * of the action bar.
	 * @param actionBar
	 *            the action bar to register global action handlers for Cut, Copy, Paste, Delete, and Select All
	 */
	public XLIFFEditorActionHandler(IActionBars actionBar) {
		this.actionBar = actionBar;

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			final PartAdapter2 partListener2 = new PartAdapter2() {
				@Override
				public void partBroughtToTop(IWorkbenchPartReference partRef) {
					if ("net.heartsome.cat.ts.ui.xliffeditor.nattable.editor".equals(partRef.getId())) {
						XLIFFEditorActionHandler.this.actionBar.setGlobalActionHandler(ActionFactory.FIND.getId(),
								textFindReplaceAction);
						textFindReplaceAction.updateEnabledState();
						XLIFFEditorActionHandler.this.actionBar.updateActionBars();
					}
				}
			};
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				page.addPartListener(partListener2);
			} else {
				window.addPageListener(new IPageListener() {

					public void pageOpened(IWorkbenchPage page) {
						page.addPartListener(partListener2);
					}

					public void pageClosed(IWorkbenchPage page) {
						page.addPartListener(partListener2);
					}

					public void pageActivated(IWorkbenchPage page) {
					}
				});
			}
		}
	}

	private IAction deleteAction;

	private IAction cutAction;

	private IAction copyAction;

	private IAction pasteAction;

	private IAction selectAllAction;

	private IAction undoAction;

	private IAction redoAction;

	private IAction findReplaceAction;

	private IPropertyChangeListener cutActionListener = new PropertyChangeListener(textCutAction);

	private IPropertyChangeListener copyActionListener = new PropertyChangeListener(textCopyAction);

	private IPropertyChangeListener pasteActionListener = new PropertyChangeListener(textPasteAction);

	private IPropertyChangeListener deleteActionListener = new PropertyChangeListener(textDeleteAction);

	private IPropertyChangeListener selectAllActionListener = new PropertyChangeListener(textSelectAllAction);

	private IPropertyChangeListener undoActionListener = new PropertyChangeListener(textUndoAction);

	private IPropertyChangeListener redoActionListener = new PropertyChangeListener(textRedoAction);

	private IPropertyChangeListener findReplaceActionListener = new PropertyChangeListener(textFindReplaceAction);

	/** 封装StyledText，提供撤销/重做管理器的组件. */
	private SegmentViewer viewer;

	private class PropertyChangeListener implements IPropertyChangeListener {
		private IAction actionHandler;

		protected PropertyChangeListener(IAction actionHandler) {
			super();
			this.actionHandler = actionHandler;
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (viewer != null) {
				return;
			}
			if (event.getProperty().equals(IAction.ENABLED)) {
				Boolean bool = (Boolean) event.getNewValue();
				actionHandler.setEnabled(bool.booleanValue());
			}
		}
	}

	private class CutActionHandler extends Action {
		protected CutActionHandler() {
			super(IDEWorkbenchMessages.Cut);
			setId("XLIFFEditorCutActionHandler");//$NON-NLS-1$
			setEnabled(false);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.TEXT_CUT_ACTION);
		}

		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				viewer.doOperation(ITextOperationTarget.CUT);
				updateActionsEnableState();
				return;
			}
			if (cutAction != null) {
				cutAction.runWithEvent(event);
				return;
			}
		}

		/**
		 * Update state.
		 */
		public void updateEnabledState() {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.CUT));
				return;
			}
			if (cutAction != null) {
				setEnabled(cutAction.isEnabled());
				return;
			}
			setEnabled(false);
		}
	}

	/**
	 * 撤销处理
	 * @author Leakey
	 * @version
	 * @since JDK1.6
	 */
	private class UndoActionHandler extends Action {
		protected UndoActionHandler() {
			super("UNDO");//$NON-NLS-1$
			setId("XLIFFEditorUndoActionHandler");//$NON-NLS-1$
			setEnabled(true);
		}

		@Override
		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				XLIFFEditorImplWithNatTable xliffEditor = XLIFFEditorImplWithNatTable.getCurrent();
				// 先保存在撤销，除非以后取消两种模式，否则不要删除此判断
				if (viewer.canDoOperation(ITextOperationTarget.UNDO)) {
					HsMultiActiveCellEditor.commit(true);
				}
				IOperationHistory history = OperationHistoryFactory.getOperationHistory();
				IUndoContext undoContext = (IUndoContext) xliffEditor.getTable().getData(IUndoContext.class.getName());
				if (history.canUndo(undoContext)) {
					try {
						history.undo(undoContext, null, null);
						undoBean.setCrosseStep(undoBean.getCrosseStep() + 1);
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				XLIFFEditorImplWithNatTable.getCurrent().redraw();
				updateActionsEnableState();
				return;
			}
			if (undoAction != null) {
				undoAction.runWithEvent(event);
				return;
			}
		}

	}

	/**
	 * 重做处理
	 * @author Leakey
	 * @version
	 * @since JDK1.6
	 */
	private class RedoActionHandler extends Action {
		protected RedoActionHandler() {
			super("REDO");//$NON-NLS-1$
			setId("XLIFFEditorRedoActionHandler");//$NON-NLS-1$
			setEnabled(true);
		}

		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				// 如果跨越焦点撤销，则先撤销非焦点
				try {
					IOperationHistory history = OperationHistoryFactory.getOperationHistory();
					IUndoContext undoContext = (IUndoContext) XLIFFEditorImplWithNatTable.getCurrent().getTable()
							.getData(IUndoContext.class.getName());
					history.redo(undoContext, null, null);
					// int crossSegment = undoBean.getCrosseStep();
					// if (crossSegment > 0) {
					// history.redo(undoContext, null, null);
					// undoBean.setCrosseStep(crossSegment - 1);
					// undoBean.setSaveStatus(-1);
					// } else if (undoBean.getSaveStatus() == -1) {
					// XLIFFEditorImplWithNatTable.getCurrent().jumpToRow(undoBean.getUnSaveRow());
					// viewer.setText(undoBean.getUnSaveText());
					// undoBean.setCrosseStep(0);
					// undoBean.setSaveStatus(0);
					// } else {
					// viewer.doOperation(ITextOperationTarget.REDO);
					// }
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				System.out.println(undoBean.getCrosseStep());
				updateActionsEnableState();
				return;
			}
			if (redoAction != null) {
				redoAction.runWithEvent(event);
				return;
			}
		}
	}

	private class CopyActionHandler extends Action {
		protected CopyActionHandler() {
			super(IDEWorkbenchMessages.Copy);
			setId("XLIFFEditorCopyActionHandler");//$NON-NLS-1$
			setEnabled(false);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.TEXT_COPY_ACTION);
		}

		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				viewer.doOperation(ITextOperationTarget.COPY);
				updateActionsEnableState();
				return;
			}
			if (copyAction != null) {
				copyAction.runWithEvent(event);
				return;
			}
		}

		/**
		 * Update the state.
		 */
		public void updateEnabledState() {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.COPY));
				return;
			}
			if (copyAction != null) {
				setEnabled(copyAction.isEnabled());
				return;
			}
			setEnabled(false);
		}
	}

	private class PasteActionHandler extends Action {
		protected PasteActionHandler() {
			super(IDEWorkbenchMessages.Paste);
			setId("XLIFFEditorPasteActionHandler");//$NON-NLS-1$
			setEnabled(false);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.TEXT_PASTE_ACTION);
		}

		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				viewer.doOperation(ITextOperationTarget.PASTE);
				updateActionsEnableState();
				return;
			}
		}

		/**
		 * Update the state
		 */
		public void updateEnabledState() {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.PASTE));
				return;
			}
			if (pasteAction != null) {
				setEnabled(pasteAction.isEnabled());
				return;
			}
			setEnabled(false);
		}
	}

	private class DeleteActionHandler extends Action {
		protected DeleteActionHandler() {
			super(IDEWorkbenchMessages.Delete);
			setId("TextDeleteActionHandler");//$NON-NLS-1$
			setEnabled(false);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.TEXT_DELETE_ACTION);
		}

		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				StyledTextCellEditor editor = HsMultiActiveCellEditor.getFocusCellEditor();
				boolean isSrc = false;
				if (editor != null && editor.getCellType().equals(NatTableConstant.SOURCE)) {
					isSrc = true;
				}
				StyledText styledText = viewer.getTextWidget();
				String text = styledText.getText();
				String selectionText = styledText.getSelectionText();
				// 当选择源文时，要判断是否是删除所有源文
				if (isSrc) {
					if (selectionText != null && text != null && text.equals(selectionText)) {
						MessageDialog.openInformation(viewer.getTextWidget().getShell(),
								Messages.getString("editor.XLIFFEditorActionHandler.msgTitle"),
								Messages.getString("editor.XLIFFEditorActionHandler.msg"));
						return;
					}
				}
				viewer.doOperation(ITextOperationTarget.DELETE);
				updateActionsEnableState();
				return;
			}
			if (deleteAction != null) {
				deleteAction.runWithEvent(event);
				return;
			}
		}

		/**
		 * Update state.
		 */
		public void updateEnabledState() {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.DELETE));
				return;
			}
			if (deleteAction != null) {
				setEnabled(deleteAction.isEnabled());
				return;
			}
			setEnabled(false);
		}
	}

	private class SelectAllActionHandler extends Action {
		protected SelectAllActionHandler() {
			super(IDEWorkbenchMessages.TextAction_selectAll);
			setId("TextSelectAllActionHandler");//$NON-NLS-1$
			setEnabled(false);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.TEXT_SELECT_ALL_ACTION);
		}

		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				viewer.doOperation(ITextOperationTarget.SELECT_ALL);
				updateActionsEnableState();
				return;
			}
			if (selectAllAction != null) {
				selectAllAction.runWithEvent(event);
				return;
			}
		}

		/**
		 * Update the state.
		 */
		public void updateEnabledState() {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.SELECT_ALL));
				return;
			}
			if (selectAllAction != null) {
				setEnabled(selectAllAction.isEnabled());
				return;
			}
			setEnabled(false);
		}
	}

	private class FindReplaceActionHandler extends Action {
		protected FindReplaceActionHandler() {
			super("FindReplace");
			setId("TextFindReplaceActionHandler");//$NON-NLS-1$
			setEnabled(false);
		}

		private FindReplaceDialog searchDialog;

		public void runWithEvent(Event event) {
			XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();
			if (editor != null) {
				String selectionText = editor.getSelectPureText();
				if (searchDialog == null) {
					searchDialog = FindReplaceDialog.createDialog(editor.getEditorSite().getShell());
				}
				NatTable natTable = editor.getTable();
				int srcColumnIndex = editor.getSrcColumnIndex();
				int[] columnPositions = { LayerUtil.getColumnPositionByIndex(natTable, srcColumnIndex) }; // 默认查询 source
				ColumnSearchStrategy searchStrategy = new ColumnSearchStrategy(columnPositions, editor);
				searchDialog.setSearchStrategy(searchStrategy, new DefaultCellSearchStrategy());
				searchDialog.open();

				// ICellEditor iCellEditor = ActiveCellEditor.getCellEditor();
				// String selectionText = "";
				// if (iCellEditor != null) {
				// if (iCellEditor instanceof StyledTextCellEditor) {
				// StyledTextCellEditor cellEditor = (StyledTextCellEditor)
				// iCellEditor;
				// StyledText styledText =
				// cellEditor.getSegmentViewer().getTextWidget();
				// Point p = styledText.getSelection();
				// if (p != null) {
				// if (p.x != p.y) {
				// // selectionText = cellEditor.getSelectedOriginalText();
				// // 只获取纯文本，清除标记
				// selectionText = cellEditor.getSelectedPureText();
				// // 将换行符替换为空
				// selectionText = selectionText.replaceAll("\n", "");
				// }
				// }
				// }
				//
				// }
				searchDialog.setSearchText(selectionText != null ? selectionText : "");

				updateEnabledState();
				return;
			}
			if (findReplaceAction != null) {
				findReplaceAction.runWithEvent(event);
				return;
			}
		}

		/**
		 * Update the state.
		 */
		public void updateEnabledState() {
			XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();
			if (editor != null) {
				setEnabled(true);
				return;
			}
			if (selectAllAction != null) {
				setEnabled(selectAllAction.isEnabled());
				return;
			}
			setEnabled(false);
		}
	}

	/**
	 * Add a <code>Text</code> control to the handler so that the Cut, Copy, Paste, Delete, Undo, Redo and Select All
	 * actions are redirected to it when active.
	 * @param viewer
	 *            the inline <code>Text</code> control
	 */
	public void addTextViewer(SegmentViewer viewer) {
		if (viewer == null) {
			return;
		}
		this.viewer = viewer;
		StyledText textControl = viewer.getTextWidget();

		// 移除 StyledText 默认绑定的 Delete 键。解决“按下 Delete 键后会删除两次”的 Bug。
		textControl.setKeyBinding(SWT.DEL, SWT.NULL);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActionsEnableState();
			}
		});

		textControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateActionsEnableState();
			}
		});

		actionBar.setGlobalActionHandler(ActionFactory.CUT.getId(), textCutAction);
		actionBar.setGlobalActionHandler(ActionFactory.COPY.getId(), textCopyAction);
		actionBar.setGlobalActionHandler(ActionFactory.PASTE.getId(), textPasteAction);
		actionBar.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), textSelectAllAction);
		actionBar.setGlobalActionHandler(ActionFactory.DELETE.getId(), textDeleteAction);
		actionBar.setGlobalActionHandler(ActionFactory.UNDO.getId(), textUndoAction);
		actionBar.setGlobalActionHandler(ActionFactory.REDO.getId(), textRedoAction);

		if (textControl.isFocusControl()) {
			updateActionsEnableState();
		} else {
			actionBar.updateActionBars();
		}
	}

	public void updateGlobalActionHandler() {
		actionBar.setGlobalActionHandler(ActionFactory.CUT.getId(), textCutAction);
		actionBar.setGlobalActionHandler(ActionFactory.COPY.getId(), textCopyAction);
		actionBar.setGlobalActionHandler(ActionFactory.PASTE.getId(), textPasteAction);
		actionBar.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), textSelectAllAction);
		actionBar.setGlobalActionHandler(ActionFactory.DELETE.getId(), textDeleteAction);
		actionBar.setGlobalActionHandler(ActionFactory.UNDO.getId(), textUndoAction);
		actionBar.setGlobalActionHandler(ActionFactory.REDO.getId(), textRedoAction);

		if (viewer != null && viewer.getTextWidget().isFocusControl()) {
			updateActionsEnableState();
		} else {
			actionBar.updateActionBars();
		}
	}

	/**
	 * Dispose of this action handler
	 */
	public void dispose() {
		setCutAction(null);
		setCopyAction(null);
		setPasteAction(null);
		setSelectAllAction(null);
		setDeleteAction(null);
		setUndoAction(null);
	}

	/**
	 * Removes a <code>Text</code> control from the handler so that the Cut, Copy, Paste, Delete, and Select All actions
	 * are no longer redirected to it when active.
	 * @param textControl
	 *            the inline <code>Text</code> control
	 */
	public void removeTextViewer() {
		if (viewer == null) {
			return;
		}
		viewer = null;
		updateActionsEnableState();
	}

	/**
	 * Set the default <code>IAction</code> handler for the Copy action. This <code>IAction</code> is run only if no
	 * active inline text control.
	 * @param action
	 *            the <code>IAction</code> to run for the Copy action, or <code>null</code> if not interested.
	 */
	public void setCopyAction(IAction action) {
		if (copyAction == action) {
			return;
		}

		if (copyAction != null) {
			copyAction.removePropertyChangeListener(copyActionListener);
		}

		copyAction = action;

		if (copyAction != null) {
			copyAction.addPropertyChangeListener(copyActionListener);
		}

		textCopyAction.updateEnabledState();
	}

	/**
	 * Set the default <code>IAction</code> handler for the Cut action. This <code>IAction</code> is run only if no
	 * active inline text control.
	 * @param action
	 *            the <code>IAction</code> to run for the Cut action, or <code>null</code> if not interested.
	 */
	public void setCutAction(IAction action) {
		if (cutAction == action) {
			return;
		}

		if (cutAction != null) {
			cutAction.removePropertyChangeListener(cutActionListener);
		}

		cutAction = action;

		if (cutAction != null) {
			cutAction.addPropertyChangeListener(cutActionListener);
		}

		textCutAction.updateEnabledState();
	}

	/**
	 * Set the default <code>IAction</code> handler for the Undo action. This <code>IAction</code> is run only if no
	 * active inline text control.
	 * @param action
	 *            the <code>IAction</code> to run for the Undo action, or <code>null</code> if not interested.
	 */
	public void setUndoAction(IAction action) {
		if (undoAction == action) {
			return;
		}

		if (undoAction != null) {
			undoAction.removePropertyChangeListener(undoActionListener);
		}

		undoAction = action;

		if (undoAction != null) {
			undoAction.addPropertyChangeListener(undoActionListener);
		}

	}

	public void setRedoAction(IAction action) {
		if (redoAction == action) {
			return;
		}

		if (redoAction != null) {
			redoAction.removePropertyChangeListener(redoActionListener);
		}

		redoAction = action;

		if (redoAction != null) {
			redoAction.addPropertyChangeListener(redoActionListener);
		}
	}

	/**
	 * Set the default <code>IAction</code> handler for the Paste action. This <code>IAction</code> is run only if no
	 * active inline text control.
	 * @param action
	 *            the <code>IAction</code> to run for the Paste action, or <code>null</code> if not interested.
	 */
	public void setPasteAction(IAction action) {
		if (pasteAction == action) {
			return;
		}

		if (pasteAction != null) {
			pasteAction.removePropertyChangeListener(pasteActionListener);
		}

		pasteAction = action;

		if (pasteAction != null) {
			pasteAction.addPropertyChangeListener(pasteActionListener);
		}

		textPasteAction.updateEnabledState();
	}

	/**
	 * Set the default <code>IAction</code> handler for the Select All action. This <code>IAction</code> is run only if
	 * no active inline text control.
	 * @param action
	 *            the <code>IAction</code> to run for the Select All action, or <code>null</code> if not interested.
	 */
	public void setSelectAllAction(IAction action) {
		if (selectAllAction == action) {
			return;
		}

		if (selectAllAction != null) {
			selectAllAction.removePropertyChangeListener(selectAllActionListener);
		}

		selectAllAction = action;

		if (selectAllAction != null) {
			selectAllAction.addPropertyChangeListener(selectAllActionListener);
		}

		textSelectAllAction.updateEnabledState();
	}

	/**
	 * Set the default <code>IAction</code> handler for the Delete action. This <code>IAction</code> is run only if no
	 * active inline text control.
	 * @param action
	 *            the <code>IAction</code> to run for the Delete action, or <code>null</code> if not interested.
	 */
	public void setDeleteAction(IAction action) {
		if (deleteAction == action) {
			return;
		}

		if (deleteAction != null) {
			deleteAction.removePropertyChangeListener(deleteActionListener);
		}

		deleteAction = action;

		if (deleteAction != null) {
			deleteAction.addPropertyChangeListener(deleteActionListener);
		}

		textDeleteAction.updateEnabledState();
	}

	/**
	 * Set the default <code>IAction</code> handler for the Find/Replcae action. This <code>IAction</code> is run only
	 * if no active xliffeditor.
	 * @param action
	 *            the <code>IAction</code> to run for the Find/Replcae action, or <code>null</code> if not interested.
	 */
	public void setFindReplaceAction(IAction action) {
		if (findReplaceAction == action) {
			return;
		}

		if (findReplaceAction != null) {
			findReplaceAction.removePropertyChangeListener(findReplaceActionListener);
		}

		findReplaceAction = action;

		if (findReplaceAction != null) {
			findReplaceAction.addPropertyChangeListener(findReplaceActionListener);
		}

		textFindReplaceAction.updateEnabledState();
	}

	/**
	 * Update the enable state of the Cut, Copy, Paste, Delete, Undo, Redo and Select All action handlers
	 */
	public void updateActionsEnableState() {
		textCutAction.updateEnabledState();
		textCopyAction.updateEnabledState();
		textPasteAction.updateEnabledState();
		textSelectAllAction.updateEnabledState();
		textDeleteAction.updateEnabledState();
		actionBar.updateActionBars();
	}
}
