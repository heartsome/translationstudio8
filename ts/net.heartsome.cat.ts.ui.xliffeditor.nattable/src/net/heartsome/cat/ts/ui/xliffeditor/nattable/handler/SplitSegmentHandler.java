package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.UpdateDataBean;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.NatTableConstant;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable.SplitSegmentOperation;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 分割文本段
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class SplitSegmentHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof XLIFFEditorImplWithNatTable)) {
			return null;
		}
		XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;

		StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getFocusCellEditor();
		if (cellEditor == null) {
			return null;
		}
		if (!cellEditor.getCellType().equals(NatTableConstant.SOURCE)) {
			showInformation(event, Messages.getString("handler.SplitSegmentHandler.msg1"));
			return null;
		}
		int rowIndex = cellEditor.getRowIndex();

		// 如果是垂直布局，那么 rowIndex 要除以2 --robert
		if (!xliffEditor.isHorizontalLayout()) {
			rowIndex = rowIndex / 2;
		}

		int caretOffset = cellEditor.getRealSplitOffset();
		if (caretOffset < 0) { // 文本框已经关闭时
			showInformation(event, Messages.getString("handler.SplitSegmentHandler.msg1"));
			return null;
		}

		// 不能选择多个字符进行分割
		String selText = cellEditor.getSegmentViewer().getTextWidget().getSelectionText();
		if (selText.length() != 0) {
			showInformation(event, Messages.getString("handler.SplitSegmentHandler.msg1"));
			return null;
		}

		XLFHandler handler = xliffEditor.getXLFHandler();
		String rowId = handler.getRowId(rowIndex);
		/* burke 修改锁定文本段不能被分割和光标在文本段段首或者段末时，不能进行分割的BUG 添加代码 起 */
		String tgt = handler.getCaseTgtContent(rowId);
		if (null != tgt) {
			if (tgt.equals("no")) {
				showInformation(event, Messages.getString("handler.SplitSegmentHandler.msg2"));
				return null;
			}
		}

		int cellTextLength = ((UpdateDataBean) cellEditor.getCanonicalValue()).getText().length();
		if (caretOffset <= 0 || caretOffset >= cellTextLength) {
			showInformation(event, Messages.getString("handler.SplitSegmentHandler.msg3"));
			return null;
		}
		/* burke 修改锁定文本段不能被分割和光标在文本段段首或者段末时，不能进行分割的BUG 添加代码 终 */

		cellEditor.close(); // 关闭Editor
		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
		try {
			operationHistory.execute(new SplitSegmentOperation("Split Segment", xliffEditor, handler, rowIndex,
					caretOffset), null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void showInformation(ExecutionEvent event, String message) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShellChecked(event);
		MessageDialog.openInformation(shell, Messages.getString("handler.SplitSegmentHandler.msgTitle"), message);
	}

}
