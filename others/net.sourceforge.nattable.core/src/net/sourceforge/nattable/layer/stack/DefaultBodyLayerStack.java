package net.sourceforge.nattable.layer.stack;

import net.sourceforge.nattable.copy.command.CopyDataCommandHandler;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.reorder.ColumnReorderLayer;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.util.IClientAreaProvider;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class DefaultBodyLayerStack extends AbstractLayerTransform {

	private final ColumnReorderLayer columnReorderLayer;
	private final ColumnHideShowLayer columnHideShowLayer;
	private final SelectionLayer selectionLayer;
	private final ViewportLayer viewportLayer;

	public DefaultBodyLayerStack(IUniqueIndexLayer underlyingLayer) {
		columnReorderLayer = new ColumnReorderLayer(underlyingLayer);
		columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
		selectionLayer = new SelectionLayer(columnHideShowLayer);
		viewportLayer = new ViewportLayer(selectionLayer);
		setUnderlyingLayer(viewportLayer);

		registerCommandHandler(new CopyDataCommandHandler(selectionLayer));
	}

	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
	}

	public ColumnReorderLayer getColumnReorderLayer() {
		return columnReorderLayer;
	}

	public ColumnHideShowLayer getColumnHideShowLayer() {
		return columnHideShowLayer;
	}

	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}

	public ViewportLayer getViewportLayer() {
		return viewportLayer;
	}
}
