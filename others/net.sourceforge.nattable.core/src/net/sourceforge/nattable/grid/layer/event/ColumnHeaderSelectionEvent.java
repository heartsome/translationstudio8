package net.sourceforge.nattable.grid.layer.event;

import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.ColumnVisualChangeEvent;

public class ColumnHeaderSelectionEvent extends ColumnVisualChangeEvent {

	public ColumnHeaderSelectionEvent(ILayer layer, int columnPosition) {
		this(layer, new Range(columnPosition, columnPosition + 1));
	}
	
	public ColumnHeaderSelectionEvent(ILayer layer, Range...columnPositionRanges) {
		this(layer, Arrays.asList(columnPositionRanges));
	}
	
	public ColumnHeaderSelectionEvent(ILayer layer, Collection<Range> columnPositionRanges) {
		super(layer, columnPositionRanges);
	}

	protected ColumnHeaderSelectionEvent(ColumnHeaderSelectionEvent event) {
		super(event);
	}
	
	public ColumnHeaderSelectionEvent cloneEvent() {
		return new ColumnHeaderSelectionEvent(this);
	}

}
