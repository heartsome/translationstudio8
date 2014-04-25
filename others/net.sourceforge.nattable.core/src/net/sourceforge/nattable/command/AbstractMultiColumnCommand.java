package net.sourceforge.nattable.command;

import java.util.Collection;
import java.util.HashSet;

import net.sourceforge.nattable.coordinate.ColumnPositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;

public abstract class AbstractMultiColumnCommand implements ILayerCommand {

	protected Collection<ColumnPositionCoordinate> columnPositionCoordinates;
	
	protected AbstractMultiColumnCommand(ILayer layer, int...columnPositions) {
		setColumnPositions(layer, columnPositions);
	}
	
	protected AbstractMultiColumnCommand(AbstractMultiColumnCommand command) {
		this.columnPositionCoordinates = new HashSet<ColumnPositionCoordinate>(command.columnPositionCoordinates);
	}
	
	public Collection<Integer> getColumnPositions() {
		Collection<Integer> columnPositions = new HashSet<Integer>();
		for (ColumnPositionCoordinate columnPositionCoordinate : columnPositionCoordinates) {
			columnPositions.add(Integer.valueOf(columnPositionCoordinate.columnPosition));
		}
		return columnPositions;
	}
	
	protected final void setColumnPositions(ILayer layer, int...columnPositions) {
		columnPositionCoordinates = new HashSet<ColumnPositionCoordinate>();
		for (int columnPosition : columnPositions) {
			columnPositionCoordinates.add(new ColumnPositionCoordinate(layer, columnPosition));
		}
	}
	
	public boolean convertToTargetLayer(ILayer targetLayer) {
		Collection<ColumnPositionCoordinate> convertedColumnPositionCoordinates = new HashSet<ColumnPositionCoordinate>();
		
		for (ColumnPositionCoordinate columnPositionCoordinate : columnPositionCoordinates) {
			ColumnPositionCoordinate convertedColumnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(columnPositionCoordinate, targetLayer);
			if (convertedColumnPositionCoordinate != null) {
				convertedColumnPositionCoordinates.add(convertedColumnPositionCoordinate);
			}
		}
		
		columnPositionCoordinates = convertedColumnPositionCoordinates;
		
		return columnPositionCoordinates.size() > 0;
	}
	
}
