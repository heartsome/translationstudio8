package net.sourceforge.nattable.selection;

import net.sourceforge.nattable.coordinate.PositionCoordinate;

/**
 * Preserves the basic semantics of the cell selection. <br/>
 * Additionally it selects the entire row when a cell in the row is selected.<br/>
 */
public class MoveRowSelectionCommandHandler extends MoveCellSelectionCommandHandler {

	public MoveRowSelectionCommandHandler(SelectionLayer selectionLayer) {
		super(selectionLayer);
	}

	@Override
	protected void moveLastSelectedLeft(int stepSize, boolean withShiftMask, boolean withControlMask) {
		super.moveLastSelectedLeft(stepSize, withShiftMask, withControlMask);
		selectionLayer.selectRow(newSelectedColumnPosition, lastSelectedCell.rowPosition, withShiftMask, withControlMask);
	}

	@Override
	protected void moveLastSelectedRight(int stepSize, boolean withShiftMask, boolean withControlMask) {
		super.moveLastSelectedRight(stepSize, withShiftMask, withControlMask);
		selectionLayer.selectRow(lastSelectedCell.columnPosition, lastSelectedCell.rowPosition, withShiftMask, withControlMask);
	}

	@Override
	protected void moveLastSelectedUp(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasRowSelection()) {
			PositionCoordinate lastSelectedCell = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			int newSelectedRowPosition = stepSize >= 0 ? lastSelectedCell.rowPosition - stepSize : 0;
			if (newSelectedRowPosition < 0) {
				newSelectedRowPosition = 0;
			}
			selectionLayer.selectRow(lastSelectedCell.columnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
		}
	}

	@Override
	protected void moveLastSelectedDown(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasRowSelection()) {
			PositionCoordinate lastSelectedCell = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			int newSelectedRowPosition = stepSize >= 0 ? lastSelectedCell.rowPosition + stepSize : selectionLayer.getRowCount() - 1;
			if (newSelectedRowPosition >= selectionLayer.getRowCount()) {
				newSelectedRowPosition = selectionLayer.getRowCount() - 1;
			}
			selectionLayer.selectRow(lastSelectedCell.columnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
		}
	}

}
