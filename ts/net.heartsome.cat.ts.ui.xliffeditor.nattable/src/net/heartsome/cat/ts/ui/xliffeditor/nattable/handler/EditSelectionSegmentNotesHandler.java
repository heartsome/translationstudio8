package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.dialog.UpdateNoteDialog;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 编辑批注的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class EditSelectionSegmentNotesHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		if (editorPart instanceof XLIFFEditorImplWithNatTable) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editorPart;
			if(xliffEditor.getSelectedRows().length == 0){
				return null;
			}
			if (xliffEditor.getSelectedRows().length > 1) {
				MessageDialog.openInformation(xliffEditor.getSite().getShell(),
						Messages.getString("menu.BodyMenuConfiguration.msgTitle"),
						Messages.getString("menu.BodyMenuConfiguration.msg5"));
				return null;
			}
			UpdateNoteDialog dialog = new UpdateNoteDialog(xliffEditor.getSite().getShell(), xliffEditor,xliffEditor.getSelectedRows()[0]);
			dialog.open();
		}
		return null;
	}

}
