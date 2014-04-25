package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 复制来源到目标
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class CopySourceHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			XLFHandler handler = xliffEditor.getXLFHandler();
			List<String> rowIds = xliffEditor.getSelectedRowIds();
			
			boolean locked = false;
			Map<String,String> map = new HashMap<String, String>();
			for (int i = 0; i < rowIds.size(); i++) {
				String rowId = rowIds.get(i);
				if (handler.isLocked(rowId)) { // 已经批准或者已锁定，则不进行修改。
					locked = true;
					continue;
				}
				String srcContent = handler.getSrcContent(rowId);
				String newValue = srcContent == null ? "" : srcContent;
				map.put(rowId, newValue);
			}

			if (locked) {
				if (!MessageDialog.openConfirm(xliffEditor.getSite().getShell(),
						Messages.getString("handler.CopySourceHandler.msgTitle"),
						Messages.getString("handler.CopySourceHandler.msg"))) {
					return null;
				}
			}
			StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getTargetStyledEditor();
			if(cellEditor != null){
				HsMultiActiveCellEditor.setCellEditorForceFocus(cellEditor.getColumnPosition(), cellEditor.getRowPosition());
			}
			xliffEditor.updateSegments(map, xliffEditor.getTgtColumnIndex(), null, null);
		}
		return null;
	}

}
