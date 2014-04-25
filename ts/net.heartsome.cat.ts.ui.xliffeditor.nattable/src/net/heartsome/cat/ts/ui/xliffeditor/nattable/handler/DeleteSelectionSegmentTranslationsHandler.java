package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
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

/**
 * 删除选中文本段的译文
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class DeleteSelectionSegmentTranslationsHandler extends AbstractHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteSelectionSegmentTranslationsHandler.class);

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof XLIFFEditorImplWithNatTable)) {
			return null;
		}
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		boolean res = MessageDialog.openConfirm(window.getShell(),
				Messages.getString("handler.DeleteSelectionSegmentTranslationsHandler.msgTitle1"),
				Messages.getString("handler.DeleteSelectionSegmentTranslationsHandler.msg1"));
		if (res) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			try {
				XLFHandler handler = xliffEditor.getXLFHandler();
				int[] selectedRows = xliffEditor.getSelectedRows();
				List<Integer> rows = new ArrayList<Integer>();
				boolean exitFlag = false;
				for (int i = 0; i < selectedRows.length; i++) {
					String tgt = handler.getCaseTgtContent(handler.getRowId(selectedRows[i]));
					if (null != tgt) {
						if (tgt.equals("no")) {
							exitFlag = true;
							continue;
						}
					}
					rows.add(selectedRows[i]);
				}
			
				if (rows.size() != 0) {
					int columnIndex = xliffEditor.getTgtColumnIndex();
					StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getTargetStyledEditor();
					if (cellEditor != null && cellEditor.getColumnIndex() == columnIndex) {
						cellEditor.getSegmentViewer().getTextWidget().forceFocus();
					}
					int[] updateRows = new int[rows.size()];
					for (int i = 0; i < rows.size(); i++) {
						int ri = rows.get(i);
						updateRows[i] = ri;
					}
					xliffEditor.updateCells(updateRows, columnIndex, "");
					if (exitFlag) {
						MessageDialog.openInformation(xliffEditor.getSite().getShell(),
								Messages.getString("handler.DeleteSelectionSegmentTranslationsHandler.msgTitle2"),
								Messages.getString("handler.DeleteSelectionSegmentTranslationsHandler.msg2"));
					}
				}
			} catch (Exception e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
		}
		return null;
	}
}
