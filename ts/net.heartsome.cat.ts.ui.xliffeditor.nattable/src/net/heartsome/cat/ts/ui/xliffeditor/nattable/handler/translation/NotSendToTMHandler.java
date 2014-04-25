package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.translation;

import java.util.List;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 不添加到记忆库
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class NotSendToTMHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			List<String> selectedRowIds = xliffEditor.getSelectedRowIds();
			if (selectedRowIds != null && selectedRowIds.size() > 0) {
				boolean isSendtoTm = true;
				XLFHandler handler = xliffEditor.getXLFHandler();
				for (String rowId : selectedRowIds) {
					if (!handler.isSendToTM(rowId) && isSendtoTm) {
						isSendtoTm = false;
						break;
					}
				}
				NattableUtil util = NattableUtil.getInstance(xliffEditor);
				util.changeSendToTmState(selectedRowIds, isSendtoTm ? "yes" : "no");
			}
		}
		return null;
	}

}
