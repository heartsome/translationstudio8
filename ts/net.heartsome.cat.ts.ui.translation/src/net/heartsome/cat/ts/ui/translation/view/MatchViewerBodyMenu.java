/**
 * MatchViewerMenuManager.java
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
package net.heartsome.cat.ts.ui.translation.view;

import java.util.Hashtable;

import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.cat.common.util.InnerTagClearUtil;
import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.translation.Activator;
import net.heartsome.cat.ts.ui.translation.ImageConstants;
import net.heartsome.cat.ts.ui.translation.dialog.TmMatchEditDialog;
import net.heartsome.cat.ts.ui.translation.resource.Messages;
import net.heartsome.cat.ts.ui.util.IntelligentTagPrcessor;
import net.heartsome.cat.ts.ui.util.TmUtils;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("restriction")
public class MatchViewerBodyMenu {
	private static final Logger logger = LoggerFactory.getLogger(MatchViewerBodyMenu.class);
	private Menu bodyMenu;
	private MatchViewPart view;
	CopyAction copyAction;
	DeleteTmAction deleteAction;
	EditTmAction editAction;
	AcceptMatchAction acceptMatchAction;

	private IXliffEditor editor;
	private int rowIndex;

	// AcceptMatchPureTextAction acceptMatchPureTextAction;

	public MatchViewerBodyMenu(MatchViewPart view) {
		this.view = view;
		createMenu();
		bodyMenu.addListener(SWT.Show, new Listener() {

			public void handleEvent(Event event) {
				updateActionState();
			}
		});
	}

	public Menu getBodyMenu() {
		return this.bodyMenu;
	}

	private void createMenu() {
		MenuManager menuMgr = new MenuManager();
		bodyMenu = menuMgr.createContextMenu(view.gridTable);
		copyAction = new CopyAction();
		menuMgr.add(copyAction);
		editAction = new EditTmAction();
		menuMgr.add(editAction);
		deleteAction = new DeleteTmAction();
		menuMgr.add(deleteAction);
		acceptMatchAction = new AcceptMatchAction();
		menuMgr.add(acceptMatchAction);
		// acceptMatchPureTextAction = new AcceptMatchPureTextAction();
		// menuMgr.add(acceptMatchPureTextAction);
	}

	void updateActionState() {
		copyAction.updateEnabledState();
		editAction.updateEnabledState();
		deleteAction.updateEnabledState();

		acceptMatchAction.updateEnabledState();
		// acceptMatchPureTextAction.updateEnabledState();
	}

	class EditTmAction extends Action {
		public EditTmAction() {
			setText(Messages.getString("view.MatchViewerBodyMenu.EditTmAction"));
			setToolTipText(Messages.getString("view.MatchViewerBodyMenu.EditTmAction.tooltip"));
			setImageDescriptor(Activator.getImageDescriptor(ImageConstants.EDIT_MATCH));
		}

		@Override
		public void run() {
			int selectionIndex = view.gridTable.getSelectionIndex();
			if (selectionIndex < 0 || selectionIndex > view.gridTable.getItemCount()) {
				return;
			}
			GridItem item = view.gridTable.getItem(selectionIndex);
			Object obj = item.getData("tmFuzzyInfo");
			if (obj == null) {
				return;
			}
			FuzzySearchResult fuzzyResult = (FuzzySearchResult) obj;

			TmMatchEditDialog dlg = new TmMatchEditDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					fuzzyResult);
			if (dlg.open() == Window.OK) {
				try {
					view.tmMatcher.updateFuzzResult(fuzzyResult);
					view.reLoadMatches(editor, rowIndex);
				} catch (Exception e) {
					MessageDialog.openError(view.getSite().getShell(),
							Messages.getString("view.MatchViewerBodyMenu.erorr.title"),
							Messages.getString("view.MatchViewerBodyMenu.error.editError") + e.getMessage());
					logger.error("Updaste TM matche Error", e);
				}
			}
		}

		public void updateEnabledState() {

			int selectionIndex = view.gridTable.getSelectionIndex();
			if (selectionIndex < 0 || selectionIndex > view.gridTable.getItemCount()) {
				setEnabled(false);
				return;
			}
			GridItem item = view.gridTable.getItem(selectionIndex);
			Object obj = item.getData("tmFuzzyInfo");
			if (obj == null) {
				setEnabled(false);
				return;
			}
			FuzzySearchResult fuzzyResult = (FuzzySearchResult) obj;
			if (fuzzyResult.getDbOp() == null) {
				setEnabled(false);
				return;
			}
			setEnabled(true);

		}
	}

	class DeleteTmAction extends Action {
		public DeleteTmAction() {
			setText(Messages.getString("view.MatchViewerBodyMenu.DeleteTmAction"));
			setToolTipText(Messages.getString("view.MatchViewerBodyMenu.DeleteTmAction.tooltip"));
			setImageDescriptor(Activator.getImageDescriptor(ImageConstants.DELETE_MATCH));
		}

		@Override
		public void run() {
			if (!MessageDialog.openConfirm(view.getSite().getShell(),
					Messages.getString("view.MatchViewerBodyMenu.confirm.title"),
					Messages.getString("view.MatchViewerBodyMenu.confirm.deleteInfo"))) {
				return;
			}
			int selectionIndex = view.gridTable.getSelectionIndex();
			if (selectionIndex < 0 || selectionIndex > view.gridTable.getItemCount()) {
				return;
			}
			GridItem item = view.gridTable.getItem(selectionIndex);
			Object obj = item.getData("tmFuzzyInfo");
			if (obj == null) {
				return;
			}
			FuzzySearchResult fuzzyResult = (FuzzySearchResult) obj;
			try {
				view.tmMatcher.deleteFuzzyResult(fuzzyResult);
				view.reLoadMatches(editor, rowIndex);
			} catch (Exception e) {
				MessageDialog.openError(view.getSite().getShell(),
						Messages.getString("view.MatchViewerBodyMenu.erorr.title"),
						Messages.getString("view.MatchViewerBodyMenu.error.deleteError") + e.getMessage());
			}
		}

		public void updateEnabledState() {
			int selectionIndex = view.gridTable.getSelectionIndex();
			if (selectionIndex < 0 || selectionIndex > view.gridTable.getItemCount()) {
				setEnabled(false);
				return;
			}
			GridItem item = view.gridTable.getItem(selectionIndex);
			Object obj = item.getData("tmFuzzyInfo");
			if (obj == null) {
				setEnabled(false);
				return;
			}
			FuzzySearchResult fuzzyResult = (FuzzySearchResult) obj;
			if (fuzzyResult.getDbOp() == null) {
				setEnabled(false);
				return;
			}
			setEnabled(true);
		}
	};

	class CopyAction extends Action {

		CopyAction() {
			super(WorkbenchMessages.Workbench_copy);
			ISharedImages sharedImages = view.getSite().getWorkbenchWindow().getWorkbench().getSharedImages();
			setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
			setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
			setEnabled(false);
		}

		@Override
		public void run() {
			view.copyEnable.doAction(ST.COPY);
		}

		public void updateEnabledState() {
			if (view.copyEnable != null && view.copyEnable.getSelectionCount() > 0) {
				setEnabled(true);
				return;
			}
			setEnabled(false);
		}
	};

	class AcceptMatchAction extends Action {
		AcceptMatchAction() {
			setEnabled(false);
			setText(Messages.getString("view.MatchViewPart.firstAction"));
			setToolTipText(Messages.getString("view.MatchViewPart.firstAction.tooltip"));
			setImageDescriptor(Activator.getIconDescriptor(ImageConstants.ACCEPT_FULLTEXT));
		}

		@Override
		public void run() {

			if (editor == null || rowIndex < 0) {
				return;
			}
			// 先判断文本段是否处理锁定状态,robert 2012-05-02
			TransUnitBean transUnit = editor.getRowTransUnitBean(rowIndex);
			String translate = transUnit.getTuProps().get("translate");
			if (translate != null && "no".equalsIgnoreCase(translate)) {
				// MessageDialog.openInformation(getSite().getShell(),
				// Messages.getString("view.MatchViewPart.msgTitle"),
				// Messages.getString("view.MatchViewPart.msg2"));
				return;
			}
			int selectionIndex = view.gridTable.getSelectionIndex();
			if (selectionIndex < 0 || selectionIndex > view.gridTable.getItemCount()) {
				return;
			}
			GridItem item = view.gridTable.getItem(selectionIndex);
			String tgtContent = item.getData("tgtContent").toString(); // 译文内容
			String matchType = item.getData("matchType").toString();
			String quality = item.getData("quality").toString();
			if (transUnit != null) {
				String srcContent = transUnit.getSrcContent();
				if (srcContent != null && !srcContent.equals("") && view.transParameter.isAdjustSpacePosition()) {
					tgtContent = TmUtils.adjustSpace(srcContent, tgtContent);
				}
			}
			try {
				if (!matchType.equals("TM")) {
					matchType = null;
					quality = null;
				}
				String srcFullText = transUnit.getSrcContent();
				String temp = IntelligentTagPrcessor.intelligentAppendTag(srcFullText, InnerTagClearUtil.clearTmx4Xliff(tgtContent));

				editor.updateCell(rowIndex, editor.getTgtColumnIndex(), temp, matchType, quality);
				// editor.setFocus(); // 焦点给回编辑器
			} catch (ExecutionException e) {
				MatchViewPart.LOGGER.error("", e);
				MessageDialog.openInformation(view.getSite().getShell(),
						Messages.getString("view.MatchViewPart.msgTitle"),
						Messages.getString("view.MatchViewPart.msg3") + e.getMessage());
			}

		}

		public void updateEnabledState() {
			Item[] items = view.gridTable.getItems();
			if (items.length == 0) {
				setEnabled(false);
				return;
			}

			if (view.gridTable.getSelectionIndex() == -1) {
				setEnabled(false);
				return;
			}
			setEnabled(true);
		}
	}

	class AcceptMatchPureTextAction extends Action {
		AcceptMatchPureTextAction() {
			setEnabled(false);
			setText(Messages.getString("view.MatchViewPart.secondAction"));
			setToolTipText(Messages.getString("view.MatchViewPart.secondAction.tooltip"));
			setImageDescriptor(Activator.getIconDescriptor(ImageConstants.ACCEPT_TEXT));
		}

		@Override
		public void run() {

			if (editor == null || rowIndex < 0) {
				return;
			}
			TransUnitBean transUnit = editor.getRowTransUnitBean(rowIndex);
			Hashtable<String, String> tuProp = transUnit.getTuProps();
			if (tuProp != null) {
				String translate = tuProp.get("translate");
				if (translate != null && translate.equalsIgnoreCase("no")) {
					MessageDialog.openInformation(view.getSite().getShell(),
							Messages.getString("view.MatchViewPart.msgTitle"),
							Messages.getString("view.MatchViewPart.msg2"));
					return;
				}
			}

			int selectionIndex = view.gridTable.getSelectionIndex();
			if (selectionIndex < 0 || selectionIndex > view.gridTable.getItemCount()) {
				return;
			}
			GridItem item = view.gridTable.getItem(selectionIndex);
			String pureText = (String) item.getData("tgtText");
			String matchType = item.getData("matchType").toString();
			String quality = item.getData("quality").toString();
			try {
				if (!matchType.equals("TM")) {
					matchType = null;
					quality = null;
				}
				editor.updateCell(rowIndex, editor.getTgtColumnIndex(), pureText, matchType, quality);
				// editor.setFocus(); // 焦点给回编辑器
			} catch (ExecutionException e) {
				MatchViewPart.LOGGER.error("", e);
				MessageDialog.openInformation(view.getSite().getShell(),
						Messages.getString("view.MatchViewPart.msgTitle"),
						Messages.getString("view.MatchViewPart.msg3") + e.getMessage());
			}

		}

		public void updateEnabledState() {
			Item[] items = view.gridTable.getItems();
			if (items.length == 0) {
				setEnabled(false);
				return;
			}

			if (view.gridTable.getSelectionIndex() == -1) {
				setEnabled(false);
				return;
			}
			setEnabled(true);
		}
	}

	/**
	 * @param editor
	 *            the editor to set
	 */
	public void setEditor(IXliffEditor editor) {
		this.editor = editor;
	}

	/**
	 * @param rowIndex
	 *            the rowIndex to set
	 */
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

}
