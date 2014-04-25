package net.heartsome.cat.ts.ui.xliffeditor.nattable.actions;

import java.util.Vector;

import net.heartsome.cat.ts.core.bean.NoteBean;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.dialog.UpdateNoteDialog;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.painter.XliffEditorGUIHelper;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.painter.XliffEditorGUIHelper.ImageName;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.style.CellStyleUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import com.ximpleware.NavException;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 当进入编辑模式后，刷新删除光标后内容和删除标记前内容的 Command
 * @author peason
 * @version
 * @since JDK1.6
 */
public class MouseEditAction extends net.sourceforge.nattable.edit.action.MouseEditAction {

	public void run(NatTable natTable, MouseEvent event) {
		XLIFFEditorImplWithNatTable xliffEditor = XLIFFEditorImplWithNatTable.getCurrent();
		if (xliffEditor == null) {
			return;
		}
		int columnPosition = natTable.getColumnPositionByX(event.x);
		int rowPosition = natTable.getRowPositionByY(event.y);

		boolean withShiftMask = (event.stateMask & SWT.SHIFT) != 0;
		boolean withCtrlMask = (event.stateMask & SWT.CTRL) != 0;

		if (!xliffEditor.isHorizontalLayout()
				&& rowPosition != HsMultiActiveCellEditor.targetRowPosition
				&& (rowPosition != HsMultiActiveCellEditor.sourceRowPosition || columnPosition != xliffEditor
						.getSrcColumnIndex())) {
			HsMultiActiveCellEditor.commit(true);
			natTable.doCommand(new SelectCellCommand(natTable, columnPosition, rowPosition, withShiftMask, withCtrlMask));
			if (columnPosition == xliffEditor.getTgtColumnIndex()) {
				HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);				
			}
		} else if (rowPosition != HsMultiActiveCellEditor.targetRowPosition
				|| columnPosition != xliffEditor.getSrcColumnIndex()
				|| columnPosition != xliffEditor.getTgtColumnIndex()) {
			HsMultiActiveCellEditor.commit(true);
			natTable.doCommand(new SelectCellCommand(natTable, columnPosition, rowPosition, withShiftMask, withCtrlMask));
			if (columnPosition == xliffEditor.getSrcColumnIndex() || columnPosition == xliffEditor.getTgtColumnIndex()) {
				HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
			}
		}

		// 点击批注图片时打开编辑批注对话框
		Image image = XliffEditorGUIHelper.getImage(ImageName.HAS_NOTE);
		// int columnPosition = natTable.getColumnPositionByX(event.x);
		// int rowPosition = natTable.getRowPositionByY(event.y);
		LayerCell cell = natTable.getCellByPosition(columnPosition, rowPosition);
		Rectangle imageBounds = image.getBounds();
		if (cell == null) {
			return;
		}
		Rectangle cellBounds = cell.getBounds();
		int x = cellBounds.x + imageBounds.width * 3 + 20;
		int y = cellBounds.y
				+ CellStyleUtil.getVerticalAlignmentPadding(
						CellStyleUtil.getCellStyle(cell, natTable.getConfigRegistry()), cellBounds, imageBounds.height);
		if (columnPosition == xliffEditor.getStatusColumnIndex() && event.x >= x && event.x <= (x + imageBounds.width)
				&& event.y >= y && event.y <= (y + imageBounds.height)) {
			if ((xliffEditor.isHorizontalLayout() && columnPosition == 2)
					|| (!xliffEditor.isHorizontalLayout() && columnPosition == 1)) {
				Vector<NoteBean> noteBeans = null;
				try {
					int rowIndex = natTable.getRowIndexByPosition(rowPosition);
					if (!xliffEditor.isHorizontalLayout()) {
						rowIndex = rowIndex / VerticalNatTableConfig.ROW_SPAN;
					}
					noteBeans = xliffEditor.getXLFHandler().getNotes(xliffEditor.getXLFHandler().getRowId(rowIndex));

					if (noteBeans != null && noteBeans.size() > 0) {
						UpdateNoteDialog dialog = new UpdateNoteDialog(xliffEditor.getSite().getShell(), xliffEditor,
								rowIndex);
						dialog.open();
					}
				} catch (NavException e) {
					e.printStackTrace();
				} catch (XPathParseException e) {
					e.printStackTrace();
				} catch (XPathEvalException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
