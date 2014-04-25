/**
 * TmMatchEditorBodyMenu.java
 *
 * Version information :
 *
 * Date:2013-3-28
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.translation.dialog;

import net.heartsome.cat.ts.ui.innertag.SegmentViewer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("restriction")
public class TmMatchEditorBodyMenu {
	CopyAction copyAction;
	CutAction cutAction;
	public PasteAction pasteAction;

	UndoAction undoAction;
	RedoAction redoAction;

	SegmentViewer viewer;
	Menu bodyMenu;

	public TmMatchEditorBodyMenu(SegmentViewer viewer) {
		this.viewer = viewer;
		createMenu();
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActionsEnableState();
			}
		});

		viewer.getTextWidget().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateActionsEnableState();
			}
		});

		viewer.getTextWidget().addListener(SWT.KeyDown, new Listener() {

			public void handleEvent(Event event) {
				int t = event.keyCode | event.stateMask;
				if (t == ('Z' | SWT.MOD1) || t == ('z' | SWT.MOD1)) {
					undoAction.runWithEvent(event);
				} else if (t == ('Y' | SWT.MOD1) || t == ('y' | SWT.MOD1)) {
					redoAction.runWithEvent(event);
				} else if (t == ('V' | SWT.MOD1) || t == ('v' | SWT.MOD1)) {
					pasteAction.runWithEvent(event);
				} else if (t == ('C' | SWT.MOD1) || t == ('c' | SWT.MOD1)) {
					copyAction.runWithEvent(event);
				}
			}
		});
		viewer.getTextWidget().setKeyBinding('V' | SWT.MOD1, SWT.NULL);
		viewer.getTextWidget().setKeyBinding(SWT.INSERT | SWT.MOD2, SWT.NULL);
	}

	public Menu getBodyMenu() {
		return this.bodyMenu;
	}

	private void createMenu() {
		MenuManager menuMgr = new MenuManager();
		bodyMenu = menuMgr.createContextMenu(viewer.getControl());
		copyAction = new CopyAction();
		cutAction = new CutAction();
		pasteAction = new PasteAction();

		undoAction = new UndoAction();
		redoAction = new RedoAction();

		menuMgr.add(undoAction);
		menuMgr.add(redoAction);

		menuMgr.add(new Separator());
		menuMgr.add(copyAction);
		menuMgr.add(cutAction);
		menuMgr.add(pasteAction);

	}

	public void updateActionsEnableState() {
		cutAction.updateEnabledState();
		copyAction.updateEnabledState();
		pasteAction.updateEnabledState();
		undoAction.updateEnabledState();
		redoAction.updateEnabledState();
	}

	private class CopyAction extends Action {
		protected CopyAction() {
			super(WorkbenchMessages.Workbench_copy);
			setEnabled(false);
			ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
			setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
			setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
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

	private class PasteAction extends Action {
		protected PasteAction() {
			super(WorkbenchMessages.Workbench_paste);
			setEnabled(false);
			ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
			setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
			setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
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

	private class CutAction extends Action {
		protected CutAction() {
			super(WorkbenchMessages.Workbench_cut);
			setEnabled(false);
			ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
			setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
			setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
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
	private class UndoAction extends Action {
		protected UndoAction() {
			super(WorkbenchMessages.Workbench_undo);
			setEnabled(false);
			ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
			setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
			setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO_DISABLED));
		}

		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				// 使用TextViewer组件的撤销功能
				viewer.doOperation(ITextOperationTarget.UNDO);
				updateActionsEnableState();
				return;
			}
			if (undoAction != null) {
				undoAction.runWithEvent(event);
				return;
			}
		}

		/**
		 * Update the state.
		 */
		public void updateEnabledState() {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.UNDO));
				return;
			}
			if (undoAction != null) {
				setEnabled(undoAction.isEnabled());
				return;
			}
			setEnabled(false);
		}
	}

	/**
	 * 重做处理
	 * @author Leakey
	 * @version
	 * @since JDK1.6
	 */
	private class RedoAction extends Action {
		protected RedoAction() {
			super(WorkbenchMessages.Workbench_redo);
			setEnabled(false);
			ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
			setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
			setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO_DISABLED));
		}

		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				// 使用TextViewer提供的重做功能
				viewer.doOperation(ITextOperationTarget.REDO);
				updateActionsEnableState();
				return;
			}
			if (redoAction != null) {
				redoAction.runWithEvent(event);
				return;
			}
		}

		/**
		 * Update the state.
		 */
		public void updateEnabledState() {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.REDO));
				return;
			}
			if (undoAction != null) {
				setEnabled(redoAction.isEnabled());
				return;
			}
			setEnabled(false);
		}
	}
}
