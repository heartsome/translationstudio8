package net.sourceforge.nattable.sort.event;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.ColumnVisualChangeEvent;

public class SortColumnEvent extends ColumnVisualChangeEvent {

	public SortColumnEvent(ILayer layer, int columnPosition) {
		super(layer, new Range(columnPosition, columnPosition + 1));
	}
	
	protected SortColumnEvent(SortColumnEvent event) {
		super(event);
	}
	
	public SortColumnEvent cloneEvent() {
		return new SortColumnEvent(this);
	}

}
