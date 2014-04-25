package net.sourceforge.nattable.group;

import java.util.List;
import java.util.Properties;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.group.command.ColumnGroupsCommandHandler;
import net.sourceforge.nattable.group.config.DefaultColumnGroupHeaderLayerConfiguration;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.layer.SizeConfig;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.event.ColumnStructuralRefreshEvent;
import net.sourceforge.nattable.painter.layer.CellLayerPainter;
import net.sourceforge.nattable.painter.layer.ILayerPainter;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.style.DisplayMode;

/**
 * Adds the Column grouping functionality to the column headers.<br/>
 * Also persists the state of the column groups when {@link NatTable#saveState()} is invoked.<br/>
 * <br/>
 * Internally uses the {@link ColumnGroupModel} to track the column groups.<br/>
 * @see ColumnGroupGridExample
 */
public class ColumnGroupHeaderLayer extends AbstractLayerTransform {

	private final SizeConfig rowHeightConfig = new SizeConfig(DataLayer.DEFAULT_ROW_HEIGHT);
	private final ColumnGroupModel model;
	private final ILayer columnHeaderLayer;
	private final ILayerPainter layerPainter = new CellLayerPainter();

	public ColumnGroupHeaderLayer(ILayer columnHeaderLayer, SelectionLayer selectionLayer, ColumnGroupModel columnGroupModel) {
		this(columnHeaderLayer, selectionLayer, columnGroupModel, true);
	}

	public ColumnGroupHeaderLayer(ILayer columnHeaderLayer, SelectionLayer selectionLayer, ColumnGroupModel columnGroupModel, boolean useDefaultConfiguration) {
		super(columnHeaderLayer);
		this.columnHeaderLayer = columnHeaderLayer;
		this.model = columnGroupModel;

		registerCommandHandler(new ColumnGroupsCommandHandler(model, selectionLayer, this));

		if (useDefaultConfiguration) {
			addConfiguration(new DefaultColumnGroupHeaderLayerConfiguration(columnGroupModel));
		}
	}

	// Persistence

	@Override
	public void saveState(String prefix, Properties properties) {
		super.saveState(prefix, properties);
		model.saveState(prefix, properties);
	}

	@Override
	public void loadState(String prefix, Properties properties) {
		super.loadState(prefix, properties);
		model.loadState(prefix, properties);
		fireLayerEvent(new ColumnStructuralRefreshEvent(this));
	}

	// Configuration

	@Override
	public ILayerPainter getLayerPainter() {
		return layerPainter;
	}

	// Vertical features

	// Rows

	@Override
	public int getRowCount() {
		return columnHeaderLayer.getRowCount() + 1;
	}

	@Override
	public int getPreferredRowCount() {
		return columnHeaderLayer.getPreferredRowCount() + 1;
	}

	@Override
	public int getRowIndexByPosition(int rowPosition) {
		if (rowPosition == 0) {
			return rowPosition;
		} else {
			return columnHeaderLayer.getRowIndexByPosition(rowPosition - 1);
		}
	}

	// Height

	@Override
	public int getHeight() {
		return rowHeightConfig.getAggregateSize(1) + columnHeaderLayer.getHeight();
	}

	@Override
	public int getPreferredHeight() {
		return rowHeightConfig.getAggregateSize(1) + columnHeaderLayer.getPreferredHeight();
	}

	@Override
	public int getRowHeightByPosition(int rowPosition) {
		if (rowPosition == 0) {
			return rowHeightConfig.getSize(rowPosition);
		} else {
			return columnHeaderLayer.getRowHeightByPosition(rowPosition - 1);
		}
	}

	public void setRowHeight(int rowHeight) {
		this.rowHeightConfig.setSize(0, rowHeight);
	}

	// Row resize

	@Override
	public boolean isRowPositionResizable(int rowPosition) {
		if (rowPosition == 0) {
			return rowHeightConfig.isPositionResizable(rowPosition);
		} else {
			return columnHeaderLayer.isRowPositionResizable(rowPosition - 1);
		}
	}

	// Y

	@Override
	public int getRowPositionByY(int y) {
		int row0Height = getRowHeightByPosition(0);
		if (y < row0Height) {
			return 0;
		} else {
			return 1 + columnHeaderLayer.getRowPositionByY(y - row0Height);
		}
	}

	@Override
	public int getStartYOfRowPosition(int rowPosition) {
		if (rowPosition == 0) {
			return rowHeightConfig.getAggregateSize(rowPosition);
		} else {
			return getRowHeightByPosition(0) + columnHeaderLayer.getStartYOfRowPosition(rowPosition - 1);
		}
	}

	// Cell features

	/**
	 * If a cell belongs to a column group:
	 * 	 column position - set to the start position of the group
	 * 	 span - set to the width/size of the column group
	 *
	 * NOTE: gc.setClip() is used in the CompositeLayerPainter to ensure that partially visible
	 * Column group header cells are rendered properly.
	 */
	@Override
	public LayerCell getCellByPosition(int columnPosition, int rowPosition) {
		int bodyColumnIndex = getColumnIndexByPosition(columnPosition);

		// Column group header cell
		if (model.isPartOfAGroup(bodyColumnIndex)) {
			if (rowPosition == 0) {
				return new LayerCell(
						this,
						getStartPositionOfGroup(columnPosition), rowPosition,
						columnPosition, rowPosition,
						getColumnSpan(columnPosition), 1
				);
			} else {
				return new LayerCell(this, columnPosition, rowPosition);
			}
		} else {
			// render column header w/ rowspan = 2
			LayerCell cell = columnHeaderLayer.getCellByPosition(columnPosition, 0);
			if (cell != null) {
				cell.updateLayer(this);
				cell.updateRowSpan(2);
			}
			return cell;
		}
	}

	/**
	 * Calculates the span of a cell in a Column Group.
	 * Takes into account collapsing and hidden columns in the group.
	 *
	 * @param selectionLayerColumnPosition of any column belonging to the group
	 */
	protected int getColumnSpan(int columnPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (model.isCollapsed(columnIndex)) {
			return 1;
		} else {
			int startPositionOfGroup = getStartPositionOfGroup(columnPosition);
			int sizeOfGroup = model.sizeOfGroup(columnIndex);
			int endPositionOfGroup = startPositionOfGroup + sizeOfGroup;
			List<Integer> columnIndexesInGroup = model.getColumnIndexesInGroup(columnIndex);

			for (int i = startPositionOfGroup; i < endPositionOfGroup; i++) {
				int index = getColumnIndexByPosition(i);
				if (!columnIndexesInGroup.contains(Integer.valueOf(index))) {
					sizeOfGroup--;
				}
			}
			return sizeOfGroup;
		}
	}

	/**
	 * Figures out the start position of the group.
	 *
	 * @param selectionLayerColumnPosition of any column belonging to the group
	 * @return first position of the column group
	 */
	private int getStartPositionOfGroup(int columnPosition) {
		int bodyColumnIndex = getColumnIndexByPosition(columnPosition);
		int leastPossibleStartPositionOfGroup = columnPosition - model.sizeOfGroup(bodyColumnIndex);
		int i = 0;
		for (i = leastPossibleStartPositionOfGroup; i < columnPosition; i++) {
			if (ColumnGroupUtils.isInTheSameGroup(getColumnIndexByPosition(i), bodyColumnIndex, model)) {
				break;
			}
		}
		return i;
	}

	@Override
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (rowPosition == 0 && model.isPartOfAGroup(columnIndex)) {
			return DisplayMode.NORMAL;
		} else {
			return columnHeaderLayer.getDisplayModeByPosition(columnPosition, rowPosition);
		}
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (rowPosition == 0 && model.isPartOfAGroup(columnIndex)) {
			return new LabelStack(GridRegion.COLUMN_GROUP_HEADER);
		} else {
			return columnHeaderLayer.getConfigLabelsByPosition(columnPosition, rowPosition);
		}
	}

	@Override
	public Object getDataValueByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (rowPosition == 0 && model.isPartOfAGroup(columnIndex)) {
			return model.getColumnGroupNameForIndex(columnIndex);
		} else {
			return columnHeaderLayer.getDataValueByPosition(columnPosition, 0);
		}
	}

	@Override
	public LabelStack getRegionLabelsByXY(int x, int y) {
		int columnIndex = getColumnIndexByPosition(getColumnPositionByX(x));
		if (model.isPartOfAGroup(columnIndex) && y < getRowHeightByPosition(0)) {
			return new LabelStack(GridRegion.COLUMN_GROUP_HEADER);
		} else {
			return columnHeaderLayer.getRegionLabelsByXY(x, y - getRowHeightByPosition(0));
		}
	}

	// ColumnGroupModel delegates

	public void addColumnsIndexesToGroup(String colGroupName, int... colIndexes) {
		model.addColumnsIndexesToGroup(colGroupName, colIndexes);
	}

	public void collapseColumnGroupByIndex(int columnIndex) {
		model.collapse(columnIndex);
	}

	public void clearAllGroups(){
		model.clear();
	}

	/**
	 * @see ColumnGroupModel#setGroupUnBreakable(int)
	 */
	public void setGroupUnbreakable(int columnIndex){
		model.setGroupUnBreakable(columnIndex);
	}

	public void setGroupAsCollapsed(int i) {
		model.collapse(i);
	}
}