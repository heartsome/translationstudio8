package net.sourceforge.nattable.freeze.command;

import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.freeze.FreezeLayer;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.viewport.ViewportLayer;

class FreezeSelectionStrategy implements IFreezeCoordinatesProvider {

	private final FreezeLayer freezeLayer;

	private final ViewportLayer viewportLayer;
	
	private final SelectionLayer selectionLayer;

	FreezeSelectionStrategy(FreezeLayer freezeLayer, ViewportLayer viewportLayer, SelectionLayer selectionLayer) {
		this.freezeLayer = freezeLayer;
		this.viewportLayer = viewportLayer;
		this.selectionLayer = selectionLayer;
	}

	public PositionCoordinate getTopLeftPosition() {
		PositionCoordinate lastSelectedCellPosition = selectionLayer.getLastSelectedCellPosition();
		
		int columnPosition = viewportLayer.getOriginColumnPosition();
		if (columnPosition >= lastSelectedCellPosition.columnPosition) {
			columnPosition = lastSelectedCellPosition.columnPosition - 1;
		}
		
		int rowPosition = viewportLayer.getOriginRowPosition();
		if (rowPosition >= lastSelectedCellPosition.rowPosition) {
			rowPosition = lastSelectedCellPosition.rowPosition - 1;
		}
		
		return new PositionCoordinate(freezeLayer, columnPosition, rowPosition);
	}
	
	public PositionCoordinate getBottomRightPosition() {
		PositionCoordinate lastSelectedCellPosition = selectionLayer.getLastSelectedCellPosition();
		return new PositionCoordinate(freezeLayer, lastSelectedCellPosition.columnPosition - 1, lastSelectedCellPosition.rowPosition - 1);
	}

}
