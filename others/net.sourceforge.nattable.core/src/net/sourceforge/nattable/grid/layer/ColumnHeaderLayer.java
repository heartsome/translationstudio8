package net.sourceforge.nattable.grid.layer;

import net.sourceforge.nattable.columnRename.DisplayColumnRenameDialogCommandHandler;
import net.sourceforge.nattable.columnRename.RenameColumnHeaderCommandHandler;
import net.sourceforge.nattable.columnRename.RenameColumnHelper;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.layer.LayerUtil;
import net.sourceforge.nattable.layer.config.DefaultColumnHeaderLayerConfiguration;
import net.sourceforge.nattable.painter.layer.CellLayerPainter;
import net.sourceforge.nattable.painter.layer.ILayerPainter;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.SelectionStyleLabels;

/**
 * Responsible for rendering, event handling etc on the column headers.
 */
public class ColumnHeaderLayer extends DimensionallyDependentLayer {

	private final SelectionLayer selectionLayer;
	private final ILayerPainter layerPainter = new CellLayerPainter();
	private final RenameColumnHelper renameColumnHelper;

	/**
	 * @param baseLayer data provider for the column header layer
	 * @param horizontalLayerDependency typically the body layer
	 * @param selectionLayer required to respond to selection events
	 */
	public ColumnHeaderLayer(IUniqueIndexLayer baseLayer, ILayer horizontalLayerDependency, SelectionLayer selectionLayer) {
		this(baseLayer, horizontalLayerDependency, selectionLayer, true);
	}

	public ColumnHeaderLayer(IUniqueIndexLayer baseLayer, ILayer horizontalLayerDependency, SelectionLayer selectionLayer, boolean useDefaultConfiguration) {
		super(baseLayer, horizontalLayerDependency, baseLayer);
		this.selectionLayer = selectionLayer;
		this.renameColumnHelper = new RenameColumnHelper(this);
		registerPersistable(renameColumnHelper);

		selectionLayer.addLayerListener(new ColumnHeaderSelectionListener(this));
		registerCommandHandler(new RenameColumnHeaderCommandHandler(this));
		registerCommandHandler(new DisplayColumnRenameDialogCommandHandler(this));

		if (useDefaultConfiguration) {
			addConfiguration(new DefaultColumnHeaderLayerConfiguration());
		}
	}

	@Override
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		int selectionLayerColumnPosition = LayerUtil.convertColumnPosition(this, columnPosition, selectionLayer);
		if (selectionLayer.isColumnPositionSelected(selectionLayerColumnPosition)) {
			return DisplayMode.SELECT;
		} else {
			return super.getDisplayModeByPosition(columnPosition, rowPosition);
		}
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		LabelStack labelStack = super.getConfigLabelsByPosition(columnPosition, rowPosition);

		final int selectionLayerColumnPosition = LayerUtil.convertColumnPosition(this, columnPosition, selectionLayer);
		if (selectionLayer.isColumnFullySelected(selectionLayerColumnPosition)) {
			labelStack.addLabel(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
		}

		return labelStack;
	}

	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}

	@Override
	public ILayerPainter getLayerPainter() {
		return layerPainter;
	}

	@Override
	public Object getDataValueByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (isColumnRenamed(columnIndex)) {
			return getRenamedColumnLabelByIndex(columnIndex);
		}
		return super.getDataValueByPosition(columnPosition, rowPosition);
	}

	// Column header renaming

	/**
	 * @return column header as defined by the data source
	 */
	public String getOriginalColumnLabel(int columnPosition) {
		return super.getDataValueByPosition(columnPosition, 0).toString();
	}

	/**
	 * @return renamed column header if the column has been renamed, NULL otherwise
	 */
	public String getRenamedColumnLabel(int columnPosition) {
		int index = getColumnIndexByPosition(columnPosition);
		return getRenamedColumnLabelByIndex(index);
	}

	/**
	 * @return renamed column header if the column has been renamed, NULL otherwise
	 */
	public String getRenamedColumnLabelByIndex(int columnIndex) {
		return renameColumnHelper.getRenamedColumnLabel(columnIndex);
	}

	/**
	 * @return TRUE if the column at the given index has been given a custom name by the user.
	 */
	public boolean isColumnRenamed(int columnIndex) {
		return renameColumnHelper.isColumnRenamed(columnIndex);
	}

	public boolean renameColumnPosition(int columnPosition, String customColumnName) {
		return renameColumnHelper.renameColumnPosition(columnPosition, customColumnName);
	}

}