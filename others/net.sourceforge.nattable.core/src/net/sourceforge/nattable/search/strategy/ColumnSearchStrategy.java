package net.sourceforge.nattable.search.strategy;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.search.ISearchDirection;

public class ColumnSearchStrategy extends AbstractSearchStrategy {

	private int[] columnPositions;
	private int startingRowPosition;
	private final String searchDirection;
	private final IConfigRegistry configRegistry;

	public ColumnSearchStrategy(int[] columnPositions, IConfigRegistry configRegistry) {
		this(columnPositions, 0, configRegistry, ISearchDirection.SEARCH_FORWARD);
	}
	
	public ColumnSearchStrategy(int[] columnPositions, int startingRowPosition, IConfigRegistry configRegistry, String searchDirection) {
		this.columnPositions = columnPositions;
		this.startingRowPosition = startingRowPosition;
		this.configRegistry = configRegistry;
		this.searchDirection = searchDirection;
	}
	
	public PositionCoordinate executeSearch(Object valueToMatch) {
		return CellDisplayValueSearchUtil.findCell(getContextLayer(), configRegistry, getColumnCellsToSearch(getContextLayer()), valueToMatch, getComparator(), isCaseSensitive());
	}
	
	public void setStartingRowPosition(int startingRowPosition) {
		this.startingRowPosition = startingRowPosition;
	}
	
	public void setColumnPositions(int[] columnPositions) {
		this.columnPositions = columnPositions;
	}
	
	protected PositionCoordinate[] getColumnCellsToSearch(ILayer contextLayer) {
		List<PositionCoordinate> cellsToSearch = new ArrayList<PositionCoordinate>();
		int rowPosition = startingRowPosition;
		// See how many rows we can add, depends on where the search is starting from
		final int rowCount = contextLayer.getRowCount();
		int height = rowCount;
		if (searchDirection.equals(ISearchDirection.SEARCH_FORWARD)) {
			height = height - startingRowPosition;
		} else {
			height = startingRowPosition;
		}
		for (int columnIndex = 0; columnIndex < columnPositions.length; columnIndex++) {
			final int startingColumnPosition = columnPositions[columnIndex];
			if (searchDirection.equals(ISearchDirection.SEARCH_BACKWARDS)) {
				cellsToSearch.addAll(CellDisplayValueSearchUtil.getDescendingCellCoordinates(getContextLayer(), startingColumnPosition, rowPosition, 1, height));
				rowPosition = rowCount - 1;
			} else {
				cellsToSearch.addAll(CellDisplayValueSearchUtil.getCellCoordinates(getContextLayer(), startingColumnPosition, rowPosition, 1, height));
				rowPosition = 0;
			}
			height = rowCount;
			// After first column is set, start the next column from the top
		}
		return cellsToSearch.toArray(new PositionCoordinate[0]);
	}
}