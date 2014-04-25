package net.sourceforge.nattable.search.strategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.search.ISearchDirection;
import net.sourceforge.nattable.selection.SelectionLayer;

public class SelectionSearchStrategy extends AbstractSearchStrategy {

	private final IConfigRegistry configRegistry;
	private final String searchDirection;

	public SelectionSearchStrategy(IConfigRegistry configRegistry) {
		this(configRegistry, ISearchDirection.SEARCH_FORWARD);
	}
	
	public SelectionSearchStrategy(IConfigRegistry configRegistry, String searchDirection) {
		this.configRegistry = configRegistry;
		this.searchDirection = searchDirection;
	}
	
	public PositionCoordinate executeSearch(Object valueToMatch) {
		ILayer contextLayer = getContextLayer();
		if (! (contextLayer instanceof SelectionLayer)) {
			throw new RuntimeException("For the GridSearchStrategy to work it needs the selectionLayer to be passed as the contextLayer.");
		}
		SelectionLayer selectionLayer = (SelectionLayer)contextLayer;
		PositionCoordinate coordinate = CellDisplayValueSearchUtil.findCell(selectionLayer, configRegistry, getSelectedCells(selectionLayer), valueToMatch, getComparator(), isCaseSensitive());		
		return coordinate;
	}

	protected PositionCoordinate[] getSelectedCells(SelectionLayer selectionLayer) {
		PositionCoordinate[] selectedCells = null;
		if (searchDirection.equals(ISearchDirection.SEARCH_BACKWARDS)) {
			List<PositionCoordinate> coordinates = Arrays.asList(selectionLayer.getSelectedCells());
			Collections.reverse(coordinates);
			selectedCells = coordinates.toArray(new PositionCoordinate[0]);
		} else {
			selectedCells = selectionLayer.getSelectedCells();
		}
		return selectedCells;
	}
}