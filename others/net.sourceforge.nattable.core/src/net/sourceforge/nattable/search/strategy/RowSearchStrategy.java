package net.sourceforge.nattable.search.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.search.ISearchDirection;

public class RowSearchStrategy extends AbstractSearchStrategy {

	private final IConfigRegistry configRegistry;
	private final int[] rowPositions;
	private final String searchDirection;

	public RowSearchStrategy(int[] rowPositions, IConfigRegistry configRegistry) {
		this(rowPositions, configRegistry, ISearchDirection.SEARCH_FORWARD);
	}
	
	public RowSearchStrategy(int[] rowPositions, IConfigRegistry configRegistry, String searchDirection) {
		this.rowPositions = rowPositions;
		this.configRegistry = configRegistry;
		this.searchDirection = searchDirection;
	}
	
	public PositionCoordinate executeSearch(Object valueToMatch) {
		return CellDisplayValueSearchUtil.findCell(getContextLayer(), configRegistry, getRowCellsToSearch(getContextLayer()), valueToMatch, getComparator(), isCaseSensitive());
	}

	protected PositionCoordinate[] getRowCellsToSearch(ILayer contextLayer) {
		List<PositionCoordinate> cellsToSearch = new ArrayList<PositionCoordinate>();
		for (int rowPosition : rowPositions) {
			cellsToSearch.addAll(CellDisplayValueSearchUtil.getCellCoordinates(getContextLayer(), 0, rowPosition, contextLayer.getColumnCount(), 1));
		}
		if (searchDirection.equals(ISearchDirection.SEARCH_BACKWARDS)) {
			Collections.reverse(cellsToSearch);
		}
		return cellsToSearch.toArray(new PositionCoordinate[0]);
	}
}