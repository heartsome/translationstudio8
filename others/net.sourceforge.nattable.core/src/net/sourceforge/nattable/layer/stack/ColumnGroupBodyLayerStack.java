package net.sourceforge.nattable.layer.stack;

import net.sourceforge.nattable.copy.command.CopyDataCommandHandler;
import net.sourceforge.nattable.group.ColumnGroupExpandCollapseLayer;
import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.group.ColumnGroupReorderLayer;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.reorder.ColumnReorderLayer;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.viewport.ViewportLayer;

/**
 * A pre-configured layer stack which includes the following layers (in that order):<br/>
 * <ol>
 * <li>ColumnReorderLayer</li>
 * <li>ColumnGroupReorderLayer</li>
 * <li>ColumnHideShowLayer</li>
 * <li>ColumnGroupExpandCollapseLayer</li>
 * <li>SelectionLayer</li>
 * <li>ViewportLayer</li>
 * </ol>
 */
public class ColumnGroupBodyLayerStack extends AbstractLayerTransform {

	private ColumnReorderLayer columnReorderLayer;
	private ColumnGroupReorderLayer columnGroupReorderLayer;
	private ColumnHideShowLayer columnHideShowLayer;
	private ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;
	private SelectionLayer selectionLayer;
	private ViewportLayer viewportLayer;

	public ColumnGroupBodyLayerStack(IUniqueIndexLayer underlyingLayer, ColumnGroupModel columnGroupModel) {
		columnReorderLayer = new ColumnReorderLayer(underlyingLayer);
		columnGroupReorderLayer = new ColumnGroupReorderLayer(columnReorderLayer, columnGroupModel);
		columnHideShowLayer = new ColumnHideShowLayer(columnGroupReorderLayer);
		columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(columnHideShowLayer, columnGroupModel);
		selectionLayer = new SelectionLayer(columnGroupExpandCollapseLayer);
		viewportLayer = new ViewportLayer(selectionLayer);
		setUnderlyingLayer(viewportLayer);

		registerCommandHandler(new CopyDataCommandHandler(selectionLayer));
	}

	public ColumnReorderLayer getColumnReorderLayer() {
		return columnReorderLayer;
	}

	public ColumnGroupReorderLayer getColumnGroupReorderLayer() {
		return columnGroupReorderLayer;
	}

	public ColumnHideShowLayer getColumnHideShowLayer() {
		return columnHideShowLayer;
	}

	public ColumnGroupExpandCollapseLayer getColumnGroupExpandCollapseLayer() {
		return columnGroupExpandCollapseLayer;
	}

	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}

	public ViewportLayer getViewportLayer() {
		return viewportLayer;
	}

}
