package net.sourceforge.nattable.search.event;

import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.event.AbstractContextFreeEvent;

public class SearchEvent extends AbstractContextFreeEvent {
	
	private final PositionCoordinate cellCoordinate;

	public SearchEvent(PositionCoordinate cellCoordinate) {
		this.cellCoordinate = cellCoordinate;
	}
	
	public PositionCoordinate getCellCoordinate() {
		return cellCoordinate;
	}
	
	public SearchEvent cloneEvent() {
		return new SearchEvent(cellCoordinate);
	}
	
}
