package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import java.util.List;

import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable.MergeSegmentOperation;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 与下一文本段合并(此类未使用，因此未做国际化)
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class MergeNextHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof XLIFFEditorImplWithNatTable)) {
			return null;
		}
		XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
		List<String> selectedRowIds = xliffEditor.getSelectedRowIds();
		if (selectedRowIds.size() < 1) {
			return null;
		}
		String rowId = selectedRowIds.get(selectedRowIds.size() - 1);
		XLFHandler handler = xliffEditor.getXLFHandler();

		int rowIndex = handler.getRowIndex(rowId);

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		if (rowIndex == handler.countEditableTransUnit() - 1) { // 是最后一行
			MessageDialog.openWarning(window.getShell(), "", "不存在下一文本段，不能合并。");
			return null;
		}
		String rowId2 = handler.getRowId(rowIndex + 1);
		if (handler.isApproved(rowId) || handler.isApproved(rowId2)) {
			MessageDialog.openWarning(window.getShell(), "", "已批准文本段，不能合并。");
			return null;
		}

		String fileName1 = RowIdUtil.getFileNameByRowId(rowId);
		String fileName2 = RowIdUtil.getFileNameByRowId(rowId2);
		if (fileName1 == null || fileName2 == null || !fileName1.equals(fileName2)) {
			MessageDialog.openWarning(window.getShell(), "", "文本段不在同一个文件内，不能合并。");
			return null;
		}

//		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
//		try {
//			operationHistory.execute(new MergeSegmentOperation("Merge Segment", xliffEditor, handler,
//					rowIndex), null, null);
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}

		return null;
	}

}
