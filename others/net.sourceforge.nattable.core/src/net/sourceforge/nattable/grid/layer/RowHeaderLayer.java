package net.sourceforge.nattable.grid.layer;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.layer.LayerUtil;
import net.sourceforge.nattable.layer.config.DefaultRowHeaderLayerConfiguration;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.SelectionStyleLabels;

public class RowHeaderLayer extends DimensionallyDependentLayer {

	private final SelectionLayer selectionLayer;

	public RowHeaderLayer(IUniqueIndexLayer baseLayer, ILayer verticalLayerDependency, SelectionLayer selectionLayer) {
		this(baseLayer, verticalLayerDependency, selectionLayer, true);
	}
	
	public RowHeaderLayer(IUniqueIndexLayer baseLayer, ILayer verticalLayerDependency, SelectionLayer selectionLayer, boolean useDefaultConfiguration) {
		super(baseLayer, baseLayer, verticalLayerDependency);
		this.selectionLayer = selectionLayer;
		
		if (useDefaultConfiguration) {
			addConfiguration(new DefaultRowHeaderLayerConfiguration());
		}
	}
	
	@Override
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		int selectionLayerRowPosition = LayerUtil.convertRowPosition(this, rowPosition, selectionLayer);
		if (selectionLayer.isRowPositionSelected(selectionLayerRowPosition)) {
			return DisplayMode.SELECT;
		} else {
			return super.getDisplayModeByPosition(columnPosition, rowPosition);
		}
	}
	
	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		LabelStack labelStack = super.getConfigLabelsByPosition(columnPosition, rowPosition);
		
		final int selectionLayerRowPosition = LayerUtil.convertRowPosition(this, rowPosition, selectionLayer);
		if (selectionLayer.isRowFullySelected(selectionLayerRowPosition)) {
			labelStack.addLabel(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
		}
		
		return labelStack;
	}
	
}
