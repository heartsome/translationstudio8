package net.sourceforge.nattable.layer;

import java.util.Collection;
import java.util.Properties;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.edit.command.UpdateDataCommandHandler;
import net.sourceforge.nattable.layer.event.StructuralRefreshEvent;
import net.sourceforge.nattable.resize.command.ColumnResizeCommandHandler;
import net.sourceforge.nattable.resize.command.MultiColumnResizeCommandHandler;
import net.sourceforge.nattable.resize.command.MultiRowResizeCommandHandler;
import net.sourceforge.nattable.resize.command.RowResizeCommandHandler;
import net.sourceforge.nattable.resize.event.ColumnResizeEvent;
import net.sourceforge.nattable.resize.event.RowResizeEvent;

/**
 * Wraps the {@link IDataProvider}, and serves as the data source for all
 * other layers. Also, tracks the size of the columns and the rows using
 * {@link SizeConfig} objects. Since this layer sits directly on top of the
 * data source, at this layer index == position.
 */
public class DataLayer extends AbstractLayer implements IUniqueIndexLayer {

	public static final String PERSISTENCE_KEY_ROW_HEIGHT = ".rowHeight";
	public static final String PERSISTENCE_KEY_COLUMN_WIDTH = ".columnWidth";

	public static final int DEFAULT_COLUMN_WIDTH = 100;
	public static final int DEFAULT_ROW_HEIGHT = 20;

	protected IDataProvider dataProvider;

	private final SizeConfig columnWidthConfig;
	private final SizeConfig rowHeightConfig;

	public DataLayer(IDataProvider dataProvider) {
		this(dataProvider, DEFAULT_COLUMN_WIDTH, DEFAULT_ROW_HEIGHT);
	}

	public DataLayer(IDataProvider dataProvider, int defaultColumnWidth, int defaultRowHeight) {
		this.dataProvider = dataProvider;

		columnWidthConfig = new SizeConfig(defaultColumnWidth);
		rowHeightConfig = new SizeConfig(defaultRowHeight);

		registerCommandHandlers();
	}

	protected DataLayer() {
		this(DEFAULT_COLUMN_WIDTH, DEFAULT_ROW_HEIGHT);
	}

	protected DataLayer(int defaultColumnWidth, int defaultRowHeight) {
		columnWidthConfig = new SizeConfig(defaultColumnWidth);
		rowHeightConfig = new SizeConfig(defaultRowHeight);

		registerCommandHandlers();
	}

	// Persistence

	@Override
	public void saveState(String prefix, Properties properties) {
		super.saveState(prefix, properties);
		columnWidthConfig.saveState(prefix + PERSISTENCE_KEY_COLUMN_WIDTH, properties);
		rowHeightConfig.saveState(prefix + PERSISTENCE_KEY_ROW_HEIGHT, properties);
	}

	@Override
	public void loadState(String prefix, Properties properties) {
		super.loadState(prefix, properties);
		columnWidthConfig.loadState(prefix + PERSISTENCE_KEY_COLUMN_WIDTH, properties);
		rowHeightConfig.loadState(prefix + PERSISTENCE_KEY_ROW_HEIGHT, properties);
		fireLayerEvent(new StructuralRefreshEvent(this));
	}

	// Configuration

	private void registerCommandHandlers() {
		registerCommandHandler(new ColumnResizeCommandHandler(this));
		registerCommandHandler(new MultiColumnResizeCommandHandler(this));
		registerCommandHandler(new RowResizeCommandHandler(this));
		registerCommandHandler(new MultiRowResizeCommandHandler(this));
		registerCommandHandler(new UpdateDataCommandHandler(this));
	}

	public IDataProvider getDataProvider() {
		return dataProvider;
	}

	protected void setDataProvider(IDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	// Horizontal features

	// Columns

	public int getColumnCount() {
		return dataProvider.getColumnCount();
	}

	public int getPreferredColumnCount() {
		return getColumnCount();
	}

    /**
	 * This is the root coordinate system, so the column index is always equal to the column position.
	 */
	public int getColumnIndexByPosition(int columnPosition) {
		if (columnPosition >=0 && columnPosition < getColumnCount()) {
			return columnPosition;
		} else {
			return -1;
		}
	}

	/**
	 * This is the root coordinate system, so the column position is always equal to the column index.
	 */
	public int getColumnPositionByIndex(int columnIndex) {
		if (columnIndex >=0 && columnIndex < getColumnCount()) {
			return columnIndex;
		} else {
			return -1;
		}
	}

	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		return localColumnPosition;
	}

	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		return underlyingColumnPosition;
	}
	
	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
		return underlyingColumnPositionRanges;
	}

	// Width

    public int getWidth() {
		return columnWidthConfig.getAggregateSize(getColumnCount());
	}

	public int getPreferredWidth() {
		return getWidth();
	}

	public int getColumnWidthByPosition(int columnPosition) {
        return columnWidthConfig.getSize(columnPosition);
    }

	public void setColumnWidthByPosition(int columnPosition, int width) {
		columnWidthConfig.setSize(columnPosition, width);
		fireLayerEvent(new ColumnResizeEvent(this, columnPosition));
	}

	public void setDefaultColumnWidth(int width) {
		columnWidthConfig.setDefaultSize(width);
	}

	public void setDefaultColumnWidthByPosition(int columnPosition, int width) {
		columnWidthConfig.setDefaultSize(columnPosition, width);
	}

	// Column resize

	public boolean isColumnPositionResizable(int columnPosition) {
		return columnWidthConfig.isPositionResizable(columnPosition);
	}

	public void setColumnPositionResizable(int columnPosition, boolean resizable) {
		columnWidthConfig.setPositionResizable(columnPosition, resizable);
	}

	public void setColumnsResizableByDefault(boolean resizableByDefault) {
		columnWidthConfig.setResizableByDefault(resizableByDefault);
	}

	// Underlying

	public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition) {
		return null;
	}

	// Vertical features

	// Rows

	public int getRowCount() {
		return dataProvider.getRowCount();
	}

	public int getPreferredRowCount() {
		return getRowCount();
	}

	/**
	 * This is the root coordinate system, so the row index is always equal to the row position.
	 */
	public int getRowIndexByPosition(int rowPosition) {
		if (rowPosition >=0 && rowPosition < getRowCount()) {
			return rowPosition;
		} else {
			return -1;
		}
	}

	/**
	 * This is the root coordinate system, so the row position is always equal to the row index.
	 */
	public int getRowPositionByIndex(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < getRowCount()) {
			return rowIndex;
		} else {
			return -1;
		}
	}

	public int localToUnderlyingRowPosition(int localRowPosition) {
		return localRowPosition;
	}

	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		return underlyingRowPosition;
	}
	
	public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
		return underlyingRowPositionRanges;
	}

	// Height

    public int getHeight() {
		return rowHeightConfig.getAggregateSize(getRowCount());
	}

	public int getPreferredHeight() {
		return getHeight();
	}

	public int getRowHeightByPosition(int rowPosition) {
		return rowHeightConfig.getSize(rowPosition);
	}

	/**
	 * Set current row size without notify resize event;
	 * @param row
	 * @param height ;
	 */
	public void setRowHeightByPositionWithoutEvent(int rowPosition, int height) {
		rowHeightConfig.setSize(rowPosition, height);
	}
	
	public void setRowHeightByPosition(int rowPosition, int height) {
		rowHeightConfig.setSize(rowPosition, height);
		fireLayerEvent(new RowResizeEvent(this, rowPosition));
	}

	public void setDefaultRowHeight(int height) {
		rowHeightConfig.setDefaultSize(height);
	}

	public void setDefaultRowHeightByPosition(int rowPosition, int height) {
		rowHeightConfig.setDefaultSize(rowPosition, height);
	}

	// Row resize

	public boolean isRowPositionResizable(int rowPosition) {
		return rowHeightConfig.isPositionResizable(rowPosition);
	}

	public void setRowPositionResizable(int rowPosition, boolean resizable) {
		rowHeightConfig.setPositionResizable(rowPosition, resizable);
	}

	public void setRowsResizableByDefault(boolean resizableByDefault) {
		rowHeightConfig.setResizableByDefault(resizableByDefault);
	}

	// Underlying

	public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
		return null;
	}

	// Cell features

	public Object getDataValueByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		int rowIndex = getRowIndexByPosition(rowPosition);
		return dataProvider.getDataValue(columnIndex, rowIndex);
	}

	public int getColumnPositionByX(int x) {
		return LayerUtil.getColumnPositionByX(this, x);
	}

	public int getRowPositionByY(int y) {
		return LayerUtil.getRowPositionByY(this, y);
	}

	public int getStartXOfColumnPosition(int columnPosition) {
		return columnWidthConfig.getAggregateSize(columnPosition);
	}

	public int getStartYOfRowPosition(int rowPosition) {
		return rowHeightConfig.getAggregateSize(rowPosition);
	}

	public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
		return null;
	}

}
