package net.heartsome.cat.ts.ui.xliffeditor.nattable.search.event;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.coordinate.CellRegion;
import net.sourceforge.nattable.layer.event.AbstractContextFreeEvent;

public class FindReplaceEvent extends AbstractContextFreeEvent {

	private final CellRegion cellRegion;

	public FindReplaceEvent(CellRegion cellRegion) {
		this.cellRegion = cellRegion;
	}

	public CellRegion getCellRegion() {
		return cellRegion;
	}

	public FindReplaceEvent cloneEvent() {
		return new FindReplaceEvent(cellRegion);
	}
}
