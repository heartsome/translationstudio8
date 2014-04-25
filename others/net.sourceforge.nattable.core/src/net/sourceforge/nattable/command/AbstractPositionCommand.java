package net.sourceforge.nattable.command;

import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;

public abstract class AbstractPositionCommand implements ILayerCommand {

	private PositionCoordinate positionCoordinate;
	
	protected AbstractPositionCommand(ILayer layer, int columnPosition, int rowPosition) {
		positionCoordinate = new PositionCoordinate(layer, columnPosition, rowPosition);
	}
	
	protected AbstractPositionCommand(AbstractPositionCommand command) {
		this.positionCoordinate = command.positionCoordinate;
	}
	
	public boolean convertToTargetLayer(ILayer targetLayer) {
		positionCoordinate = LayerCommandUtil.convertPositionToTargetContext(positionCoordinate, targetLayer);
		return positionCoordinate != null;
	}
	
	public int getColumnPosition() {
		return positionCoordinate.getColumnPosition();
	}
	
	public int getRowPosition() {
		return positionCoordinate.getRowPosition();
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " columnPosition=" + positionCoordinate.getColumnPosition() + ", rowPosition=" + positionCoordinate.getRowPosition();
	}

}
