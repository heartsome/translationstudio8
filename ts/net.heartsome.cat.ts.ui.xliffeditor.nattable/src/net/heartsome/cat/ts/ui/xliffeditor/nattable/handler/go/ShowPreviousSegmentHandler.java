package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.go;

import java.util.Arrays;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 跳转到上一个文本段(此类未使用，因此未做国际化)
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class ShowPreviousSegmentHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof XLIFFEditorImplWithNatTable)) {
			return null;
		}
		XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
		int[] selectedRows = xliffEditor.getSelectedRows();
		if (selectedRows.length < 1) {
			return null;
		}
		Arrays.sort(selectedRows);
		int firstSelectedRow = selectedRows[0];
		if (firstSelectedRow == 0) {
			firstSelectedRow = 1;
		}
		xliffEditor.jumpToRow(firstSelectedRow - 1);
		return null;
	}

}
