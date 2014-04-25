package net.sourceforge.nattable.grid.layer;

import net.sourceforge.nattable.grid.layer.event.ColumnHeaderSelectionEvent;
import net.sourceforge.nattable.layer.ILayerListener;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.selection.event.ColumnSelectionEvent;

/**
 * Marks the ColumnHeader as selected in response to a {@link ColumnSelectionEvent}
 */
public class ColumnHeaderSelectionListener implements ILayerListener {
	
	private ColumnHeaderLayer columnHeaderLayer;
	
	public ColumnHeaderSelectionListener(ColumnHeaderLayer columnHeaderLayer) {
		this.columnHeaderLayer = columnHeaderLayer;
	}

	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof ColumnSelectionEvent) {
			ColumnSelectionEvent selectionEvent = (ColumnSelectionEvent) event;
			ColumnHeaderSelectionEvent colHeaderSelectionEvent = new ColumnHeaderSelectionEvent(columnHeaderLayer, selectionEvent.getColumnPositionRanges());
			columnHeaderLayer.fireLayerEvent(colHeaderSelectionEvent);
		}
	}
}
