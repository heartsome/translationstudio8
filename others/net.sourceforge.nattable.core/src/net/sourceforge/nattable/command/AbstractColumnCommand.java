package net.sourceforge.nattable.command;

import net.sourceforge.nattable.coordinate.ColumnPositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;

public abstract class AbstractColumnCommand implements ILayerCommand {

	private ColumnPositionCoordinate columnPositionCoordinate;

	protected AbstractColumnCommand(ILayer layer, int columnPosition) {
		columnPositionCoordinate = new ColumnPositionCoordinate(layer, columnPosition);
	}
	
	protected AbstractColumnCommand(AbstractColumnCommand command) {
		this.columnPositionCoordinate = command.columnPositionCoordinate;
	}

	public boolean convertToTargetLayer(ILayer targetLayer) {
		columnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(columnPositionCoordinate, targetLayer);
		return columnPositionCoordinate != null;
	}
	
	public ILayer getLayer() {
		return columnPositionCoordinate.getLayer();
	}
	
	public int getColumnPosition() {
		return columnPositionCoordinate.getColumnPosition();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " columnPosition=" + columnPositionCoordinate.getColumnPosition();
	}
	
}
