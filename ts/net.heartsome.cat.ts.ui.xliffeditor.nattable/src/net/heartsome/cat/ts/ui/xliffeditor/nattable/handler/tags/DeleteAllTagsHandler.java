package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.tags;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 删除所有标记
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class DeleteAllTagsHandler extends AbstractHandler {


	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			int[] rows = xliffEditor.getSelectedRows();
			if (rows.length == 0){
				return null;
			}
			HsMultiCellEditor targetCellEditor  = HsMultiActiveCellEditor.getTargetEditor();
			StyledTextCellEditor cellEditor = targetCellEditor.getCellEditor();
			if(cellEditor == null || cellEditor.isClosed()){
				return null;
			}
			if(rows.length == 1){
				cellEditor.clearTags(); // 清除内部标记
				return null;
			} else {
				int activeEditRowIndex = targetCellEditor.getRowIndex();
				cellEditor.clearTags(); // 清除内部标记
				if(!xliffEditor.isHorizontalLayout()){
					activeEditRowIndex = activeEditRowIndex / 2;
				}
				// non active row
				int[] nonActiveRows = new int[rows.length - 1];
				int i = 0;
				for(int row : rows){
					if(row == activeEditRowIndex){
						continue;
					}
					nonActiveRows[i++] = row;
				}
				List<String> rowIdList =  new ArrayList<String>(xliffEditor.getXLFHandler().getRowIds(rows));
				xliffEditor.getXLFHandler().removeAllTags(rowIdList);
				xliffEditor.refresh();
			}				
		}
		return null;
	}
}
