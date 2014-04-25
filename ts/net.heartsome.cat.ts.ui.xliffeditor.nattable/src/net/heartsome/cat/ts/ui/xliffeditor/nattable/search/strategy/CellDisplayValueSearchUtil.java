package net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.TagDisplayConverter;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.coordinate.CellRegion;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.style.DisplayMode;

import org.eclipse.jface.text.IRegion;

public class CellDisplayValueSearchUtil {

	static List<PositionCoordinate> getCellCoordinates(ILayer contextLayer, int startingColumnPosition,
			int startingRowPosition, int width, int height, boolean isHorizontalLayout) {
		if (!isHorizontalLayout) {
			height = (int) Math.ceil(height * 1.0 / VerticalNatTableConfig.ROW_SPAN);
		}
		List<PositionCoordinate> coordinates = new ArrayList<PositionCoordinate>();
		for (int columnPosition = 0; columnPosition < width; columnPosition++) {
			for (int rowPosition = 0; rowPosition < height; rowPosition++) {
				PositionCoordinate coordinate = new PositionCoordinate(contextLayer, startingColumnPosition,
						startingRowPosition);
				coordinates.add(coordinate);

				startingRowPosition += isHorizontalLayout ? 1 : VerticalNatTableConfig.ROW_SPAN;
			}
			startingColumnPosition++;
		}
		return coordinates;
	}

	static List<PositionCoordinate> getDescendingCellCoordinates(ILayer contextLayer, int startingColumnPosition,
			int startingRowPosition, int width, int height, boolean isHorizontalLayout) {
		if (!isHorizontalLayout) {
			width = width / VerticalNatTableConfig.ROW_SPAN;
		}
		List<PositionCoordinate> coordinates = new ArrayList<PositionCoordinate>();
		for (int columnPosition = width; columnPosition >= 0 && startingColumnPosition >= 0; columnPosition--) {
			for (int rowPosition = height; rowPosition >= 0 && startingRowPosition >= 0; rowPosition--) {
				PositionCoordinate coordinate = new PositionCoordinate(contextLayer, startingColumnPosition,
						startingRowPosition);
				coordinates.add(coordinate);

				startingRowPosition -= isHorizontalLayout ? 1 : VerticalNatTableConfig.ROW_SPAN;
			}
			startingColumnPosition--;
		}
		return coordinates;
	}

	static CellRegion findCell(final ILayer layer, final IConfigRegistry configRegistry,
			final PositionCoordinate[] cellsToSearch, final Object valueToMatch,
			final ICellSearchStrategy cellSearchStrategy) {
		final List<PositionCoordinate> cellCoordinates = Arrays.asList(cellsToSearch);
		// Find cell
		CellRegion targetCoordinate = null;

		String stringValue = valueToMatch.toString();

		final IDisplayConverter displayConverter = configRegistry
				.getConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, DisplayMode.NORMAL,
						XLIFFEditorImplWithNatTable.SOURCE_EDIT_CELL_LABEL);

		for (int cellIndex = 0; cellIndex < cellCoordinates.size(); cellIndex++) {
			final PositionCoordinate cellCoordinate = cellCoordinates.get(cellIndex);
			final int columnPosition = cellCoordinate.columnPosition;
			final int rowPosition = cellCoordinate.rowPosition;

			// Convert cell's data
			if (displayConverter instanceof TagDisplayConverter) {
				LayerCell cell = new LayerCell(cellCoordinate.getLayer(), cellCoordinate.getColumnPosition(),
						cellCoordinate.getRowPosition());
				((TagDisplayConverter) displayConverter).setCell(cell);
			}
			final Object dataValue = displayConverter.canonicalToDisplayValue(layer.getDataValueByPosition(
					columnPosition, rowPosition));

			// Compare with valueToMatch
			if (dataValue instanceof String) {
				String dataValueString = dataValue.toString();
				IRegion region;
				if ((region = cellSearchStrategy.executeSearch(stringValue, dataValueString)) != null) {
					targetCoordinate = new CellRegion(cellCoordinate, region);
					break;
				}
				((DefaultCellSearchStrategy)cellSearchStrategy).setStartOffset(-1);
			}
		}

		return targetCoordinate;
	}
}
