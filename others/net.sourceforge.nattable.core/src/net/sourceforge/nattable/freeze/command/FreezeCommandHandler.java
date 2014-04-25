package net.sourceforge.nattable.freeze.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.freeze.FreezeLayer;
import net.sourceforge.nattable.freeze.event.FreezeEvent;
import net.sourceforge.nattable.freeze.event.UnfreezeEvent;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class FreezeCommandHandler extends AbstractLayerCommandHandler<IFreezeCommand> {

	private final FreezeLayer freezeLayer;
	
	private final ViewportLayer viewportLayer;
	
	private final SelectionLayer selectionLayer;

	public FreezeCommandHandler(FreezeLayer freezeLayer, ViewportLayer viewportLayer, SelectionLayer selectionLayer) {
		this.freezeLayer = freezeLayer;
		this.viewportLayer = viewportLayer;
		this.selectionLayer = selectionLayer;
	}
	
	public Class<IFreezeCommand> getCommandClass() {
		return IFreezeCommand.class;
	}
	
	public boolean doCommand(IFreezeCommand command) {
		if (command instanceof FreezeColumnCommand) {
			FreezeColumnCommand freezeColumnCommand = (FreezeColumnCommand)command;
			IFreezeCoordinatesProvider coordinatesProvider = new FreezeColumnStrategy(freezeLayer, freezeColumnCommand.getColumnPosition());
			handleFreezeCommand(coordinatesProvider);
			return true;
		} else if (command instanceof FreezeSelectionCommand) {
			IFreezeCoordinatesProvider coordinatesProvider = new FreezeSelectionStrategy(freezeLayer, viewportLayer, selectionLayer);
			handleFreezeCommand(coordinatesProvider);
			return true;
		} else if (command instanceof UnFreezeGridCommand) {
			handleUnfreeze();
			return true;
		}
		return false;
	}

	protected void handleFreezeCommand(IFreezeCoordinatesProvider coordinatesProvider) {
		// if not already frozen
		if (freezeLayer.getColumnCount() == 0 && freezeLayer.getRowCount() == 0) {
			final PositionCoordinate topLeftPosition = coordinatesProvider.getTopLeftPosition();
			final PositionCoordinate bottomRightPosition = coordinatesProvider.getBottomRightPosition();
	
			freezeLayer.setTopLeftPosition(topLeftPosition.columnPosition, topLeftPosition.rowPosition);
			freezeLayer.setBottomRightPosition(bottomRightPosition.columnPosition, bottomRightPosition.rowPosition);
	
			viewportLayer.setMinimumOriginPosition(bottomRightPosition.columnPosition + 1, bottomRightPosition.rowPosition + 1);
			
			viewportLayer.fireLayerEvent(new FreezeEvent(viewportLayer));
		}
	}
	
	protected void handleUnfreeze() {
		resetFrozenArea();
		viewportLayer.fireLayerEvent(new UnfreezeEvent(viewportLayer));
	}

	private void resetFrozenArea() {
		freezeLayer.setTopLeftPosition(-1, -1);
		freezeLayer.setBottomRightPosition(-1, -1);
		viewportLayer.resetOrigin();
	}
	
}