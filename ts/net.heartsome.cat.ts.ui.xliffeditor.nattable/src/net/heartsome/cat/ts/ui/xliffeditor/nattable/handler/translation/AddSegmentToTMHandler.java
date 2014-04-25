package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.translation;

import java.util.Arrays;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.view.IMatchViewPart;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 添加选中文本段到记忆库的 Handler
 * @author peason
 * @version
 * @since JDK1.6｀
 */
public class AddSegmentToTMHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof XLIFFEditorImplWithNatTable)) {
			return null;
		}
		XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
		String addSegmentToTM = event.getParameter("addSegmentToTM");
		if (addSegmentToTM == null) {
			return null;
		}
		NattableUtil util = NattableUtil.getInstance(xliffEditor);
		HsMultiActiveCellEditor.commit(true);
		boolean state = util.addSelectSegmentToTM();
		if (!state) {
			return null;
		}
		// 刷新项目
		ResourceUtils.refreshCurentSelectProject();
		if (addSegmentToTM.equals("addToTMAndJumpNext")) {
			// 添加到记忆库并跳转到下一文本段
			int[] selectedRows = xliffEditor.getSelectedRows();
			if (selectedRows.length < 1) {
				return null;
			}
			Arrays.sort(selectedRows);
			int lastSelectedRow = selectedRows[selectedRows.length - 1];
			// 假如当前选择了第1，3行，则跳转到下一文本段时是跳转到第2行
			for (int rowNum = 0; rowNum < selectedRows.length - 1; rowNum++) {
				if (selectedRows[rowNum + 1] != (selectedRows[rowNum] + 1)) {
					lastSelectedRow = rowNum;
					break;
				}
			}
			XLFHandler handler = xliffEditor.getXLFHandler();
			int lastRow = handler.countEditableTransUnit() - 1;
			if (lastSelectedRow == lastRow) {
				lastSelectedRow = lastRow - 1;
			}
			xliffEditor.jumpToRow(lastSelectedRow + 1);

		} else if (addSegmentToTM.equals("addToTMAndJumpNextNotCompleteMatch")) {
			// 添加到记忆库并跳转到下一非完全匹配
			int[] selectedRows = xliffEditor.getSelectedRows();
			if (selectedRows.length < 1) {
				return null;
			}
			Arrays.sort(selectedRows);
			int lastSelectedRow = selectedRows[selectedRows.length - 1];
			// 假如当前选择了第1，3行，则跳转到下一非完全匹配文本段时要从第2行开始检查
			for (int rowNum = 0; rowNum < selectedRows.length - 1; rowNum++) {
				if (selectedRows[rowNum + 1] != (selectedRows[rowNum] + 1)) {
					lastSelectedRow = rowNum;
					break;
				}
			}
			XLFHandler handler = xliffEditor.getXLFHandler();

			int row = handler.getNextFuzzySegmentIndex(lastSelectedRow);
			if (row != -1) {
				xliffEditor.jumpToRow(row);
			} else {
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
						Messages.getString("translation.AddSegmentToTMHandler.msgTitle"),
						Messages.getString("translation.AddSegmentToTMHandler.msg"));
			}
		} else {
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(
					"net.heartsome.cat.ts.ui.translation.view.matchview");
			int[] selected =xliffEditor.getSelectedRows();
			if (viewPart != null && viewPart instanceof IMatchViewPart && selected.length != 0) {				
				((IMatchViewPart) viewPart).reLoadMatches(xliffEditor,  selected[selected.length - 1]);
			}
			HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
		}
		return null;
	}

}
