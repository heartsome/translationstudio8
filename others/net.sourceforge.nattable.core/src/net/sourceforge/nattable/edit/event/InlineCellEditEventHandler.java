package net.sourceforge.nattable.edit.event;

import net.sourceforge.nattable.edit.InlineCellEditController;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.event.ILayerEventHandler;

public class InlineCellEditEventHandler implements ILayerEventHandler<InlineCellEditEvent> {
	
	private final GridLayer gridLayer;

	public InlineCellEditEventHandler(GridLayer gridLayer) {
		this.gridLayer = gridLayer;
	}

	public Class<InlineCellEditEvent> getLayerEventClass() {
		return InlineCellEditEvent.class;
	}

	public void handleLayerEvent(InlineCellEditEvent event) {
		if (event.convertToLocal(gridLayer)) {
			LayerCell cell = gridLayer.getCellByPosition(event.getColumnPosition(), event.getRowPosition());
			InlineCellEditController.editCellInline(cell, event.getInitialValue(), event.getParent(), event.getConfigRegistry());
		}
	}
}
