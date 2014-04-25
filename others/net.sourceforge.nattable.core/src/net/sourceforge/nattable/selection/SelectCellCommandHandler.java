package net.sourceforge.nattable.selection;

import static net.sourceforge.nattable.selection.SelectionUtils.isControlOnly;
import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.command.SelectCellCommand;

import org.eclipse.swt.graphics.Rectangle;

public class SelectCellCommandHandler implements ILayerCommandHandler<SelectCellCommand> {

	private final SelectionLayer selectionLayer;

	public SelectCellCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	public boolean doCommand(ILayer targetLayer, SelectCellCommand command) {
		if (command.convertToTargetLayer(selectionLayer)) {
			toggleCell(command.getColumnPosition(), command.getRowPosition(), command.isShiftMask(), command.isControlMask(), command.isForcingEntireCellIntoViewport());
			selectionLayer.fireCellSelectionEvent(command.getColumnPosition(), command.getRowPosition(), command.isForcingEntireCellIntoViewport(), command.isShiftMask(), command.isControlMask());
			return true;
		}
		return false;
	}

	/**
	 * Toggles the selection state of the given row and column.
	 * @return <code>false</code> if the cell was unselected.
	 */
	protected void toggleCell(int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask, boolean forcingEntireCellIntoViewport) {
		boolean selectCell = true;
		if (isControlOnly(withShiftMask, withControlMask)) {
			if (selectionLayer.isCellPositionSelected(columnPosition, rowPosition)) {
				selectionLayer.clearSelection(columnPosition, rowPosition);
				selectCell = false;
			}
		}
		if (selectCell) {
			selectCell(columnPosition, rowPosition, withShiftMask, withControlMask);
		}
	}

	/**
	 * Selects a cell, optionally clearing current selection
	 */
	public void selectCell(int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask) {
		if (!withShiftMask && !withControlMask) {
			selectionLayer.clear();
		}
		selectionLayer.setLastSelectedCell(columnPosition, rowPosition);

		// Shift pressed + row selected
		if (withShiftMask && selectionLayer.lastSelectedRegion != null && selectionLayer.hasRowSelection()) {

			selectionLayer.lastSelectedRegion.height = Math.abs(selectionLayer.selectionAnchor.rowPosition - rowPosition) + 1;
			selectionLayer.lastSelectedRegion.y = Math.min(selectionLayer.selectionAnchor.rowPosition, rowPosition);

			selectionLayer.lastSelectedRegion.width = Math.abs(selectionLayer.selectionAnchor.columnPosition - columnPosition) + 1;
			selectionLayer.lastSelectedRegion.x = Math.min(selectionLayer.selectionAnchor.columnPosition, columnPosition);

			selectionLayer.addSelection(selectionLayer.lastSelectedRegion);
		} else {
			selectionLayer.lastSelectedRegion = null;
			Rectangle selection = null;

			selection = new Rectangle(selectionLayer.lastSelectedCell.columnPosition, selectionLayer.lastSelectedCell.rowPosition, 1, 1);

			selectionLayer.addSelection(selection);
		}
	}

	public Class<SelectCellCommand> getCommandClass() {
		return SelectCellCommand.class;
	}

}
