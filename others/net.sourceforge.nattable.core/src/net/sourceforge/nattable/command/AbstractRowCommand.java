package net.sourceforge.nattable.command;

import net.sourceforge.nattable.coordinate.RowPositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;

public abstract class AbstractRowCommand implements ILayerCommand {

	private RowPositionCoordinate rowPositionCoordinate;
	
	protected AbstractRowCommand(ILayer layer, int rowPosition) {
		rowPositionCoordinate = new RowPositionCoordinate(layer, rowPosition);
	}
	
	protected AbstractRowCommand(AbstractRowCommand command) {
		this.rowPositionCoordinate = command.rowPositionCoordinate;
	}
	
	public boolean convertToTargetLayer(ILayer targetLayer) {
		rowPositionCoordinate = LayerCommandUtil.convertRowPositionToTargetContext(rowPositionCoordinate, targetLayer);
		return rowPositionCoordinate != null;
	}
	
	public int getRowPosition() {
		return rowPositionCoordinate.getRowPosition();
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " rowPosition=" + rowPositionCoordinate.getRowPosition();
	}

}
