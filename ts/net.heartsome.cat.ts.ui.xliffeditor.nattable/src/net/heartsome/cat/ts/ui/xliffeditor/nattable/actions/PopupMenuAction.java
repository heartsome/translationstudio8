package net.heartsome.cat.ts.ui.xliffeditor.nattable.actions;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.action.AbstractMouseSelectionAction;
import net.sourceforge.nattable.selection.command.SelectCellCommand;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;

public class PopupMenuAction extends AbstractMouseSelectionAction {

	private SelectionLayer selectionLayer;

	private Menu menu;

	public PopupMenuAction(Menu menu) {
		this.menu = menu;
	}

	@Override
	public void run(NatTable natTable, MouseEvent event) {
//		ActiveCellEditor.commit(); // 执行弹出菜单前先关闭编辑模式的单元格
		super.run(natTable, event);

		if (selectionLayer == null) {
			selectionLayer = LayerUtil.getLayer(natTable, SelectionLayer.class);
		}

		int rowIndex = natTable.getRowIndexByPosition(getGridRowPosition());
		XLIFFEditorImplWithNatTable editor  = XLIFFEditorImplWithNatTable.getCurrent();
		if(!editor.isHorizontalLayout()){
			rowIndex = rowIndex / 2;
		}
		// 如果该行已经选中的了，直接显示出右键菜单。
		if (!isSelected(rowIndex)) {
			HsMultiActiveCellEditor.commit(true);
			natTable.doCommand(new SelectCellCommand(natTable, getGridColumnPosition(), getGridRowPosition(),
					isWithShiftMask(), isWithControlMask()));
			HsMultiCellEditorControl.activeSourceAndTargetCell(editor);
		}

		menu.setData(event.data);
		menu.setVisible(true);
	}

	/**
	 * 是否选中了该行
	 * @param rowIndex
	 *            行索引
	 * @return 是否选中;
	 */
	private boolean isSelected(int rowIndex) {
		int[] selectedRows = selectionLayer.getFullySelectedRowPositions();
		for (int row : selectedRows) {
			if (row == rowIndex) {
				return true;
			}
		}
		return false;
	}
}
