package net.sourceforge.nattable.search.command;

import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.search.event.SearchEvent;
import net.sourceforge.nattable.search.strategy.AbstractSearchStrategy;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.command.SelectCellCommand;

public class SearchGridCellsCommandHandler implements ILayerCommandHandler<SearchCommand> {
	
	private final SelectionLayer selectionLayer;
	private PositionCoordinate searchResultCellCoordinate;

	public SearchGridCellsCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}
	
	public Class<SearchCommand> getCommandClass() {
		return SearchCommand.class;
	};
	
	public boolean doCommand(ILayer targetLayer, SearchCommand searchCommand) {
		searchCommand.convertToTargetLayer(targetLayer);
		
		AbstractSearchStrategy searchStrategy = (AbstractSearchStrategy) searchCommand.getSearchStrategy();
		if (searchCommand.getSearchEventListener() != null) {
			selectionLayer.addLayerListener(searchCommand.getSearchEventListener());
		}
		PositionCoordinate anchor = selectionLayer.getSelectionAnchor();
		if (anchor.columnPosition < 0 || anchor.rowPosition < 0) {
			anchor = new PositionCoordinate(selectionLayer, 0, 0);
		}
		searchStrategy.setContextLayer(targetLayer);
		Object dataValueToFind = null;
		if ((dataValueToFind = searchCommand.getSearchText()) == null) {
			dataValueToFind = selectionLayer.getDataValueByPosition(anchor.columnPosition, anchor.rowPosition);
		}
		
		searchStrategy.setCaseSensitive(searchCommand.isCaseSensitive());
		searchStrategy.setWrapSearch(searchCommand.isWrapSearch());
		searchStrategy.setSearchDirection(searchCommand.getSearchDirection());
		searchStrategy.setComparator(searchCommand.getComparator());
		searchResultCellCoordinate = searchStrategy.executeSearch(dataValueToFind);
		
		selectionLayer.fireLayerEvent(new SearchEvent(searchResultCellCoordinate));
		if (searchResultCellCoordinate != null) {
			final SelectCellCommand command = new SelectCellCommand(selectionLayer, searchResultCellCoordinate.columnPosition, searchResultCellCoordinate.rowPosition, false, false);
			command.setForcingEntireCellIntoViewport(true);
			selectionLayer.doCommand(command);
		}
		
		return true;
	}
	
	public PositionCoordinate getSearchResultCellCoordinate() {
		return searchResultCellCoordinate;
	}
}