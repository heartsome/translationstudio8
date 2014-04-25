package net.sourceforge.nattable.reorder.command;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.command.LayerCommandUtil;
import net.sourceforge.nattable.coordinate.ColumnPositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;

public class MultiColumnReorderCommand implements ILayerCommand {

	private List<ColumnPositionCoordinate> fromColumnPositionCoordinates;
	private ColumnPositionCoordinate toColumnPositionCoordinate;

	public MultiColumnReorderCommand(ILayer layer, List<Integer> fromColumnPositions, int toColumnPositions) {
		fromColumnPositionCoordinates = new ArrayList<ColumnPositionCoordinate>();
		for (Integer fromColumnPosition : fromColumnPositions) {
			fromColumnPositionCoordinates.add(new ColumnPositionCoordinate(layer, fromColumnPosition.intValue()));
		}
		
		toColumnPositionCoordinate = new ColumnPositionCoordinate(layer, toColumnPositions);
	}

	protected MultiColumnReorderCommand(MultiColumnReorderCommand command) {
		this.fromColumnPositionCoordinates = new ArrayList<ColumnPositionCoordinate>(command.fromColumnPositionCoordinates);
		this.toColumnPositionCoordinate = command.toColumnPositionCoordinate;
	}

	public List<Integer> getFromColumnPositions() {
		List<Integer> fromColumnPositions = new ArrayList<Integer>();
		for (ColumnPositionCoordinate fromColumnPositionCoordinate : fromColumnPositionCoordinates) {
			fromColumnPositions.add(Integer.valueOf(fromColumnPositionCoordinate.getColumnPosition()));
		}
		return fromColumnPositions;
	}

	public int getToColumnPosition() {
		return toColumnPositionCoordinate.getColumnPosition();
	}

	public boolean convertToTargetLayer(ILayer targetLayer) {
		List<ColumnPositionCoordinate> convertedFromColumnPositionCoordinates = new ArrayList<ColumnPositionCoordinate>();
		
		for (ColumnPositionCoordinate fromColumnPositionCoordinate : fromColumnPositionCoordinates) {
			ColumnPositionCoordinate convertedFromColumnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(fromColumnPositionCoordinate, targetLayer);
			if (convertedFromColumnPositionCoordinate != null) {
				convertedFromColumnPositionCoordinates.add(convertedFromColumnPositionCoordinate);
			}
		}
		
		fromColumnPositionCoordinates = convertedFromColumnPositionCoordinates;
		
		toColumnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(toColumnPositionCoordinate, targetLayer);
		
		return fromColumnPositionCoordinates.size() > 0 && toColumnPositionCoordinate != null;
	}

	public MultiColumnReorderCommand cloneCommand() {
		return new MultiColumnReorderCommand(this);
	}

}
