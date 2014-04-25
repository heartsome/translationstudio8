package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.translation;

import java.util.List;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 是否批准可编辑/所有文本段的 Handler
 * @author peason
 * @version
 * @since JDK1.6
 */
public class ApproveSegmentHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			final XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			List<String> rowIds = xliffEditor.getSelectedRowIds();


			if (rowIds.size() == 0) {
				return null;
			}

			String parameter = event.getParameter("approveSegment");
			if (parameter != null) {
				HsMultiActiveCellEditor.commit(true);
				NattableUtil util = NattableUtil.getInstance(xliffEditor);
				util.approveTransUnits(parameter.equalsIgnoreCase("approveAndJumpNext")); // 执行批准
			}
		}
		return null;
	}
}
