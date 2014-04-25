package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import static net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder.PATTERN;

import java.util.Hashtable;
import java.util.regex.Matcher;

import net.heartsome.cat.ts.ui.innertag.SegmentViewer;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.NatTableConstant;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 目标文本段中删除光标后或者标记前所有内容的 Handler
 * @author peason
 * @version
 * @since JDK1.6
 */
public class DeleteToEndOrToTagHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			String deleteType = event.getParameter("DeleteContent");
			if (deleteType == null) {
				return null;
			}
			StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getFocusCellEditor();
			if (cellEditor == null || !cellEditor.getCellType().equals(NatTableConstant.TARGET)) {
				return null;
			}
			if (!cellEditor.isEditable()) {
				// cellEditor.showUneditableMessage();
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event), Messages
						.getString("handler.DeleteToEndOrToTagHandler.msgTitle"), cellEditor.getEditableManager()
						.getUneditableMessage());
				return null;
			}
			StyledText styledText = cellEditor.getSegmentViewer().getTextWidget();
			int offset = styledText.getCaretOffset();
			Point p = styledText.getSelection();
			if (p != null) {
				int len = styledText.getText().length();
				String preText = "";
				String nextText = "";
				SegmentViewer viewer = (SegmentViewer) cellEditor.getSegmentViewer();
				if (offset > 0) {
					preText = styledText.getText(0, offset - 1);
					preText = viewer.convertDisplayTextToOriginalText(preText);
				}
				// 删除标记前所有内容
				if (deleteType.equals("DeleteToTag") && offset < len) {
					nextText = styledText.getText(offset, len - 1);
					Matcher matcher = PATTERN.matcher(nextText);
					if (matcher.find()) {
						int index = matcher.start();
						nextText = nextText.substring(index);
					} else {
						// 选择删除标记前所有内容时，如果当前光标之后没有标记，则删除光标之后的所有内容
						nextText = "";
					}
				}
				nextText = viewer.convertDisplayTextToOriginalText(nextText);
				String newText = preText + nextText;
				Hashtable<String, String> map = new Hashtable<String, String>();
				// Fix Bug #2883 删除光标后内容--同时选择多个文本段进行操作时，界面出错 By Jason
				// for (String rowId : xliffEditor.getSelectedRowIds()) {
				// map.put(rowId, newText);
				// }
				int index = cellEditor.getRowIndex();
				String rowId = xliffEditor.getXLFHandler().getRowId(index);
				map.put(rowId, newText);
				xliffEditor.updateSegments(map, xliffEditor.getTgtColumnIndex(), null, null);
				// 定位光标
				styledText.setCaretOffset(offset);
			}
		}
		return null;
	}

}
