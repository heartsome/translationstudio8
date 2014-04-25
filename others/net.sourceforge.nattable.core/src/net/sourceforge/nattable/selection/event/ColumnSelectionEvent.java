package net.sourceforge.nattable.selection.event;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.event.ColumnVisualChangeEvent;
import net.sourceforge.nattable.selection.SelectionLayer;

public class ColumnSelectionEvent extends ColumnVisualChangeEvent implements ISelectionEvent {

	private final SelectionLayer selectionLayer;
	
	public ColumnSelectionEvent(SelectionLayer selectionLayer, int columnPosition) {
		super(selectionLayer, new Range(columnPosition, columnPosition + 1));
		this.selectionLayer = selectionLayer;
	}
	
	protected ColumnSelectionEvent(ColumnSelectionEvent event) {
		super(event);
		this.selectionLayer = event.selectionLayer;
	}
	
	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}
	
	public ColumnSelectionEvent cloneEvent() {
		return new ColumnSelectionEvent(this);
	}
	
}
