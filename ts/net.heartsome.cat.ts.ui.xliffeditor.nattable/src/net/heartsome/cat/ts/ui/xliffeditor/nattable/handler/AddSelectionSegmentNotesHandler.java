package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.dialog.AddOrUpdateNoteDialog;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 添加批注的 Handler
 * @author peason
 * @version
 * @since JDK1.6
 */
public class AddSelectionSegmentNotesHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		if (editorPart instanceof XLIFFEditorImplWithNatTable) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editorPart;
			AddOrUpdateNoteDialog dialog = new AddOrUpdateNoteDialog(xliffEditor.getSite().getShell(), xliffEditor,
					AddOrUpdateNoteDialog.DIALOG_ADD, null);
			dialog.open();
		}

		return null;
	}

}
