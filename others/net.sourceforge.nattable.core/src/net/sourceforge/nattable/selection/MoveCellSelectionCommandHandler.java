package net.sourceforge.nattable.selection;

import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;

/**
 * Specifies the semantics of moving the selection in the table, based on selecting the adjoining cell(s).
 */
public class MoveCellSelectionCommandHandler extends MoveSelectionCommandHandler<MoveSelectionCommand> {

	protected int newSelectedColumnPosition;
	protected int newSelectedRowPosition;
	protected PositionCoordinate lastSelectedCell;

	public MoveCellSelectionCommandHandler(SelectionLayer selectionLayer) {
		super(selectionLayer);
	}

	@Override
	protected void moveLastSelectedLeft(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasColumnSelection()) {
			lastSelectedCell = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			int newSelectedRowPosition = lastSelectedCell.rowPosition;

			newSelectedColumnPosition = (stepSize >= 0) ? (lastSelectedCell.columnPosition - stepSize) : 0;
			if (newSelectedColumnPosition < 0) {
				newSelectedColumnPosition = 0;
			}
			if(stepSize == SelectionLayer.MOVE_ALL && !withShiftMask){
				selectionLayer.clear();
			}
			selectionLayer.selectCell(newSelectedColumnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
			selectionLayer.fireCellSelectionEvent(lastSelectedCell.columnPosition, lastSelectedCell.rowPosition, false, withShiftMask, withControlMask);
		}
	}

	@Override
	protected void moveLastSelectedRight(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasColumnSelection()) {
			lastSelectedCell = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			int newSelectedRowPosition = lastSelectedCell.rowPosition;

			newSelectedColumnPosition = (stepSize >= 0) 
											? (lastSelectedCell.columnPosition + stepSize) 
											: selectionLayer.getColumnCount() - 1;
			if (newSelectedColumnPosition >= selectionLayer.getColumnCount()) {
				newSelectedColumnPosition = selectionLayer.getColumnCount() - 1;
			}
			if(stepSize == SelectionLayer.MOVE_ALL && !withShiftMask){
				selectionLayer.clear();
			}

			selectionLayer.selectCell(newSelectedColumnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
			selectionLayer.fireCellSelectionEvent(lastSelectedCell.columnPosition, lastSelectedCell.rowPosition, false, withShiftMask, withControlMask);
		}
	}

	@Override
	protected void moveLastSelectedUp(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasRowSelection()) {
			lastSelectedCell = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			newSelectedRowPosition = stepSize >= 0 ? lastSelectedCell.rowPosition - stepSize : 0;
			if (newSelectedRowPosition < 0) {
				newSelectedRowPosition = 0;
			}
			selectionLayer.selectCell(lastSelectedCell.columnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
			selectionLayer.fireCellSelectionEvent(lastSelectedCell.columnPosition, lastSelectedCell.rowPosition, false, withShiftMask, withControlMask);
		}
	}

	@Override
	protected void moveLastSelectedDown(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasRowSelection()) {
			lastSelectedCell = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			newSelectedRowPosition = stepSize >= 0 ? lastSelectedCell.rowPosition + stepSize : selectionLayer.getRowCount() - 1;
			if (newSelectedRowPosition >= selectionLayer.getRowCount()) {
				newSelectedRowPosition = selectionLayer.getRowCount() - 1;
			}
			selectionLayer.selectCell(lastSelectedCell.columnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
			selectionLayer.fireCellSelectionEvent(lastSelectedCell.columnPosition, lastSelectedCell.rowPosition, false, withShiftMask, withControlMask);
		}
	}

	public Class<MoveSelectionCommand> getCommandClass() {
		return MoveSelectionCommand.class;
	}

}
