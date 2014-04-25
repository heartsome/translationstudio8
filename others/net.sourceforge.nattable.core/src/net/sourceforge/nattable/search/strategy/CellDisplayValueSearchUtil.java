package net.sourceforge.nattable.search.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.style.DisplayMode;

public class CellDisplayValueSearchUtil {
	
	static List<PositionCoordinate> getCellCoordinates(ILayer contextLayer, int startingColumnPosition, int startingRowPosition, int width, int height) {
		List<PositionCoordinate> coordinates = new ArrayList<PositionCoordinate>();
		for (int columnPosition = 0; columnPosition < width; columnPosition++) {
			for (int rowPosition = 0; rowPosition < height; rowPosition++) {
				PositionCoordinate coordinate = new PositionCoordinate(contextLayer, startingColumnPosition, startingRowPosition++);
				coordinates.add(coordinate);
			}
			startingColumnPosition++;
		}
		return coordinates;
	}
	
	static List<PositionCoordinate> getDescendingCellCoordinates(ILayer contextLayer, int startingColumnPosition, int startingRowPosition, int width, int height) {
		List<PositionCoordinate> coordinates = new ArrayList<PositionCoordinate>();
		for (int columnPosition = width; columnPosition >= 0 && startingColumnPosition >= 0; columnPosition--) {
			for (int rowPosition = height; rowPosition >= 0 && startingRowPosition >= 0; rowPosition--) {
				PositionCoordinate coordinate = new PositionCoordinate(contextLayer, startingColumnPosition, startingRowPosition--);
				coordinates.add(coordinate);
			}
			startingColumnPosition--;
		}
		return coordinates;
	}
	
	
	@SuppressWarnings("unchecked")
	static PositionCoordinate findCell(final ILayer layer, final IConfigRegistry configRegistry, final PositionCoordinate[] cellsToSearch, final Object valueToMatch, final Comparator comparator, final boolean caseSensitive) {	
		final List<PositionCoordinate> cellCoordinates = Arrays.asList(cellsToSearch);		
		// Find cell
		PositionCoordinate targetCoordinate = null;
		
		String stringValue = caseSensitive ? valueToMatch.toString() : valueToMatch.toString().toLowerCase();
		for (int cellIndex = 0; cellIndex < cellCoordinates.size(); cellIndex++) {
			final PositionCoordinate cellCoordinate = cellCoordinates.get(cellIndex);
			final int columnPosition = cellCoordinate.columnPosition;
			final int rowPosition = cellCoordinate.rowPosition;
			
			// Convert cell's data
			final IDisplayConverter displayConverter = configRegistry.getConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, DisplayMode.NORMAL, layer.getConfigLabelsByPosition(columnPosition, rowPosition).getLabels());
			final Object dataValue = displayConverter.canonicalToDisplayValue(layer.getDataValueByPosition(columnPosition, rowPosition));
			
			// Compare with valueToMatch
			if (dataValue instanceof Comparable<?>) {
				String dataValueString = caseSensitive ? dataValue.toString() : dataValue.toString().toLowerCase();
				if (comparator.compare(stringValue, dataValueString) == 0 || dataValueString.contains(stringValue)) {
					targetCoordinate = cellCoordinate;
					break;
				}
			}
		}
		
		return targetCoordinate;
	}
}
