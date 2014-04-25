package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import net.heartsome.cat.ts.core.bean.NoteBean;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.NavException;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 删除所选文本段批注
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class DeleteSelectionSegmentNotesHandler extends AbstractHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteSelectionSegmentNotesHandler.class);

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof XLIFFEditorImplWithNatTable)) {
			return null;
		}
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		boolean res = MessageDialog.openConfirm(window.getShell(),
				Messages.getString("handler.DeleteSelectionSegmentNotesHandler.msgTitle1"),
				Messages.getString("handler.DeleteSelectionSegmentNotesHandler.msg1"));
		if (res) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			XLFHandler handler = xliffEditor.getXLFHandler();
			List<String> lstRowId = xliffEditor.getSelectedRowIds();
			// 先将应用范围为当前文本段的批注删除
			handler.deleteEditableSegmentNote(lstRowId);

			try {
				HashMap<String, Vector<NoteBean>> mapNote = new HashMap<String, Vector<NoteBean>>();
				for (String rowId : lstRowId) {
					// 删除应用范围为所有文本段的批注
					Vector<NoteBean> noteBeans = xliffEditor.getXLFHandler().getNotes(rowId);
					if (noteBeans != null && noteBeans.size() > 0) {
						mapNote.put(rowId, noteBeans);
					}
				}
				xliffEditor.getXLFHandler().deleteNote(mapNote);
			} catch (NavException e) {
				LOGGER.error("", e);
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
						Messages.getString("handler.DeleteSelectionSegmentNotesHandler.msgTitle2"),
						Messages.getString("handler.DeleteSelectionSegmentNotesHandler.msg2"));
			} catch (XPathParseException e) {
				LOGGER.error("", e);
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
						Messages.getString("handler.DeleteSelectionSegmentNotesHandler.msgTitle2"),
						Messages.getString("handler.DeleteSelectionSegmentNotesHandler.msg2"));
			} catch (XPathEvalException e) {
				LOGGER.error("", e);
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
						Messages.getString("handler.DeleteSelectionSegmentNotesHandler.msgTitle2"),
						Messages.getString("handler.DeleteSelectionSegmentNotesHandler.msg2"));
			} finally {
				xliffEditor.refresh();
			}

		}
		return null;
	}

}
