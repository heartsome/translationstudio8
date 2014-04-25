package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.go;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 跳转到最后一个文本段
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class ShowLastSegmentHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof XLIFFEditorImplWithNatTable)) {
			return null;
		}
		XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
		XLFHandler handler = xliffEditor.getXLFHandler();
		int lastRow = handler.countEditableTransUnit() - 1;
		xliffEditor.jumpToRow(lastRow);
		return null;
	}

}
