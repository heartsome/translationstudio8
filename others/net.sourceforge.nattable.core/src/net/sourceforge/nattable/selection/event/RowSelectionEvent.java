package net.sourceforge.nattable.selection.event;

import java.util.Collection;

import net.sourceforge.nattable.coordinate.PositionUtil;
import net.sourceforge.nattable.layer.event.RowVisualChangeEvent;
import net.sourceforge.nattable.selection.SelectionLayer;

public class RowSelectionEvent extends RowVisualChangeEvent implements ISelectionEvent {

	private final SelectionLayer selectionLayer;

	public RowSelectionEvent(SelectionLayer selectionLayer, Collection<Integer> rowPositions) {
		super(selectionLayer, PositionUtil.getRanges(rowPositions));
		this.selectionLayer = selectionLayer;
	}
	
	// Copy constructor
	protected RowSelectionEvent(RowSelectionEvent event) {
		super(event);
		this.selectionLayer = event.selectionLayer;
	}
	
	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}
	
	public RowSelectionEvent cloneEvent() {
		return new RowSelectionEvent(this);
	}
	
}