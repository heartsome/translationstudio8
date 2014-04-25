package net.heartsome.cat.ts.handlexlf.handler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.handlexlf.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 跳到下一个分割点
 * @author robert	2013-10-17
 */
public class NextSplitPointHandler extends AbstractHandler {
	private String curSelectionRowId = null;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final Shell shell = window.getShell();

		XLIFFEditorImplWithNatTable xliffEditor = null;
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		
		if (activePart instanceof IEditorPart) {
			if (XLIFF_EDITOR_ID.equals(activePart.getSite().getId())) {
				xliffEditor = (XLIFFEditorImplWithNatTable) activePart;
				List<String> splitPointList = xliffEditor.getSplitXliffPoints();
				
				if (splitPointList.size() <= 0) {
					MessageDialog.openInformation(shell, Messages.getString("all.dialog.info"), 
							Messages.getString("NextSplitPointHandler.msg.nullSplitPoint"));
					return null;
				}
				
				final XLFHandler xlfHander = xliffEditor.getXLFHandler();
				// 先对 splitPointList 进行排序
				Collections.sort(splitPointList, new Comparator<String>() {
					public int compare(String rowId1, String rowId2) {
						int rowIndex1 = xlfHander.getRowIndex(rowId1);
						int rowIndex2 = xlfHander.getRowIndex(rowId2);
						return rowIndex1 > rowIndex2 ? 1 : -1;
					}
				});
				
				List<String> selectionRowIdList = xliffEditor.getSelectedRowIds();
				if (selectionRowIdList != null && selectionRowIdList.size() > 0) {
					curSelectionRowId = selectionRowIdList.get(0);
				}
				
				// 开始定位，定位之前让 nattable 恢复默认布局
				xliffEditor.resetOrder();
				
				if (curSelectionRowId == null) {
					curSelectionRowId = splitPointList.get(0);
				}else {
					int curSelectionRowIndex = xlfHander.getRowIndex(curSelectionRowId);
					for (String curRowId : splitPointList) {
						int pointRowIndex = xlfHander.getRowIndex(curRowId);
						if (pointRowIndex > curSelectionRowIndex) {
							curSelectionRowId = curRowId;
							xliffEditor.jumpToRow(curSelectionRowId);
							break;
						}
					}
				}
			}
		}
		
		return null;
	}
	
}
