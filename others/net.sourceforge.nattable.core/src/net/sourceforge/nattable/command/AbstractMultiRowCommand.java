package net.sourceforge.nattable.command;

import java.util.Collection;
import java.util.HashSet;

import net.sourceforge.nattable.coordinate.RowPositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;

public abstract class AbstractMultiRowCommand implements ILayerCommand {

	private Collection<RowPositionCoordinate> rowPositionCoordinates;
	
	protected AbstractMultiRowCommand(ILayer layer, int...rowPositions) {
		setRowPositions(layer, rowPositions);
	}
	
	protected AbstractMultiRowCommand(AbstractMultiRowCommand command) {
		this.rowPositionCoordinates = new HashSet<RowPositionCoordinate>(command.rowPositionCoordinates);
	}
	
	public Collection<Integer> getRowPositions() {
		Collection<Integer> rowPositions = new HashSet<Integer>();
		for (RowPositionCoordinate rowPositionCoordinate : rowPositionCoordinates) {
			rowPositions.add(Integer.valueOf(rowPositionCoordinate.rowPosition));
		}
		return rowPositions;
	}
	
	protected final void setRowPositions(ILayer layer, int...rowPositions) {
		rowPositionCoordinates = new HashSet<RowPositionCoordinate>();
		for (int rowPosition : rowPositions) {
			rowPositionCoordinates.add(new RowPositionCoordinate(layer, rowPosition));
		}
	}
	
	public boolean convertToTargetLayer(ILayer targetLayer) {
		Collection<RowPositionCoordinate> convertedRowPositionCoordinates = new HashSet<RowPositionCoordinate>();
		for (RowPositionCoordinate rowPositionCoordinate : rowPositionCoordinates) {
			RowPositionCoordinate convertedRowPositionCoordinate = LayerCommandUtil.convertRowPositionToTargetContext(rowPositionCoordinate, targetLayer);
			if (convertedRowPositionCoordinate != null) {
				convertedRowPositionCoordinates.add(convertedRowPositionCoordinate);
			}
		}
		
		rowPositionCoordinates = convertedRowPositionCoordinates;
		
		return rowPositionCoordinates.size() > 0;
	}
	
}