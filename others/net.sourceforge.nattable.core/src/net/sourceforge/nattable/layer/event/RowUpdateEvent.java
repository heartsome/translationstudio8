package net.sourceforge.nattable.layer.event;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;

public class RowUpdateEvent extends RowVisualChangeEvent {
	
	public RowUpdateEvent(ILayer layer, int rowPosition) {
		this(layer, new Range(rowPosition, rowPosition + 1));
	}
	
	public RowUpdateEvent(ILayer layer, Range rowPositionRange) {
		super(layer, rowPositionRange);
	}
	
	public RowUpdateEvent(RowUpdateEvent event) {
		super(event);
	}
	
	public RowUpdateEvent cloneEvent() {
		return new RowUpdateEvent(this);
	}

}
