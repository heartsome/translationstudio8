package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.translation;

import java.util.List;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 草稿
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class DraftSegmentHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			NattableUtil util = NattableUtil.getInstance(xliffEditor);
			List<String> selectedRowIds = util.getRowIdsNoEmptyTranslate(false);
			if (selectedRowIds != null && selectedRowIds.size() > 0) {
				util.changeTgtState(selectedRowIds, "new");
			}
		}
		return null;
	}

}
