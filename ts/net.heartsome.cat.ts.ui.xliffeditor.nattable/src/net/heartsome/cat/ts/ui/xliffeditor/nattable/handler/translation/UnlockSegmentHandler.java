package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.translation;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 给当前界面所显示的所有文本段解锁
 * @author robert	2012-09-24
 */
public class UnlockSegmentHandler extends AbstractHandler{

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart part = HandlerUtil.getActiveEditor(event);
		if (part instanceof XLIFFEditorImplWithNatTable) {
			final XLIFFEditorImplWithNatTable nattable = (XLIFFEditorImplWithNatTable) part;
			XLFHandler handler = nattable.getXLFHandler();
			handler.unlockSegment();
			nattable.refresh();
			HsMultiActiveCellEditor.refrushCellsEditAbility();
		}
		
		return null;
	}

}
