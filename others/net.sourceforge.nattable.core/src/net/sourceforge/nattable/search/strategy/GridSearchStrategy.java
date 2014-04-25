package net.sourceforge.nattable.search.strategy;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.search.ISearchDirection;
import net.sourceforge.nattable.selection.SelectionLayer;

public class GridSearchStrategy extends AbstractSearchStrategy {
	
	private final IConfigRegistry configRegistry;
	
	public GridSearchStrategy(IConfigRegistry configRegistry, boolean wrapSearch) {
		this(configRegistry, wrapSearch, ISearchDirection.SEARCH_FORWARD);
	}
	
	public GridSearchStrategy(IConfigRegistry configRegistry, boolean wrapSearch, String searchDirection) {		
		this.configRegistry = configRegistry;
		this.wrapSearch = wrapSearch;
		this.searchDirection = searchDirection;
	}

	public PositionCoordinate executeSearch(Object valueToMatch) {
		ILayer contextLayer = getContextLayer();
		if (! (contextLayer instanceof SelectionLayer)) {
			throw new RuntimeException("For the GridSearchStrategy to work it needs the selectionLayer to be passed as the contextLayer.");
		}
		
		SelectionLayer selectionLayer = (SelectionLayer) contextLayer;
		PositionCoordinate selectionAnchor = selectionLayer .getSelectionAnchor();
		boolean hadSelectionAnchor = true;
		if (selectionAnchor.columnPosition < 0 || selectionAnchor.rowPosition < 0) {
			selectionAnchor.columnPosition = 0;
			selectionAnchor.rowPosition = 0;
			hadSelectionAnchor = false;
		}
		int anchorColumnPosition = selectionAnchor.columnPosition;
		
		int startingRowPosition;
		int[] columnsToSearch = null;
		final int columnCount = selectionLayer.getColumnCount();
		if (searchDirection.equals(ISearchDirection.SEARCH_FORWARD)) {
			int rowPosition = hadSelectionAnchor ? selectionAnchor.rowPosition + 1 : selectionAnchor.rowPosition;
			if (rowPosition > (contextLayer.getRowCount() - 1)) {
				rowPosition = wrapSearch ? 0 : contextLayer.getRowCount() - 1;
			}
			int rowCount = selectionLayer.getRowCount();
			startingRowPosition = rowPosition < rowCount ? rowPosition : 0;
			if (selectionAnchor.rowPosition + 1 >= rowCount && anchorColumnPosition + 1 >= columnCount && hadSelectionAnchor) {
				if (wrapSearch) {
					anchorColumnPosition = 0;
				} else {
					return null;
				}
			} else if (selectionAnchor.rowPosition == rowCount - 1 && anchorColumnPosition < columnCount - 1) {
				anchorColumnPosition++;
			}
			columnsToSearch = getColumnsToSearchArray(columnCount, anchorColumnPosition);
		} else {
			int rowPosition = selectionAnchor.rowPosition - 1;
			if (rowPosition < 0) {
				rowPosition  = wrapSearch ? contextLayer.getRowCount() - 1 : 0;
			}
			startingRowPosition = rowPosition > 0 ? rowPosition : 0;

			if (selectionAnchor.rowPosition - 1 < 0 && anchorColumnPosition - 1 < 0 && hadSelectionAnchor) {
				if (wrapSearch) {
					anchorColumnPosition = columnCount - 1;
				} else {
					return null;
				}
			} else if (selectionAnchor.rowPosition == 0 && anchorColumnPosition > 0) {
				anchorColumnPosition--;
			}
			columnsToSearch = getDescendingColumnsToSearchArray(anchorColumnPosition);
		}
		
		PositionCoordinate executeSearch = searchGrid(valueToMatch, contextLayer, selectionLayer, anchorColumnPosition,
				startingRowPosition, columnsToSearch);
		return executeSearch;
	}

	private PositionCoordinate searchGrid(Object valueToMatch, ILayer contextLayer, SelectionLayer selectionLayer,
			final int anchorColumnPosition, int startingRowPosition, int[] columnsToSearch) {
		// Search for value across columns		
		ColumnSearchStrategy columnSearcher = new ColumnSearchStrategy(columnsToSearch, startingRowPosition, configRegistry, searchDirection);
		columnSearcher.setCaseSensitive(caseSensitive);
		columnSearcher.setWrapSearch(wrapSearch);
		columnSearcher.setContextLayer(contextLayer);
		columnSearcher.setComparator(getComparator());
		PositionCoordinate executeSearch = columnSearcher.executeSearch(valueToMatch);
		
		if (executeSearch == null && wrapSearch) {
			if (searchDirection.equals(ISearchDirection.SEARCH_FORWARD)) {				
				columnSearcher.setColumnPositions(getColumnsToSearchArray(anchorColumnPosition + 1, 0));
			} else {
				columnSearcher.setColumnPositions(getDescendingColumnsToSearchArray(anchorColumnPosition));
			}
			columnSearcher.setStartingRowPosition(0);
			executeSearch = columnSearcher.executeSearch(valueToMatch);
		}
		return executeSearch;
	}

	protected int[] getColumnsToSearchArray(int columnCount, int startingColumnPosition) {
		final int numberOfColumnsToSearch = (columnCount - startingColumnPosition);
		final int[] columnPositions = new int[numberOfColumnsToSearch];
		for (int columnPosition = 0; columnPosition < numberOfColumnsToSearch; columnPosition++) {
			columnPositions[columnPosition] = startingColumnPosition + columnPosition;
		}
		return columnPositions;
	}
	
	protected int[] getDescendingColumnsToSearchArray(int startingColumnPosition) {
		final int[] columnPositions = new int[startingColumnPosition + 1];
		for (int columnPosition = 0; startingColumnPosition >= 0; columnPosition++) {
			columnPositions[columnPosition] = startingColumnPosition-- ;
		}
		return columnPositions;
	}
}
