package net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.coordinate.CellRegion;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;

public class ColumnSearchStrategy implements ISearchStrategy {

	private ILayer contextLayer;
	private int[] columnPositions;
	private int startingRowPosition;
	private final XLIFFEditorImplWithNatTable xliffEditor;
	private final IConfigRegistry configRegistry;
	private ICellSearchStrategy cellSearchStrategy;
	private boolean isHorizontalLayout;

	public ColumnSearchStrategy(int[] columnPositions, XLIFFEditorImplWithNatTable xliffEditor) {
		this(columnPositions, 0, xliffEditor);
	}

	public ColumnSearchStrategy(int[] columnPositions, int startingRowPosition, XLIFFEditorImplWithNatTable xliffEditor) {
		this.columnPositions = columnPositions;
		this.startingRowPosition = startingRowPosition;
		this.xliffEditor = xliffEditor;
		this.configRegistry = xliffEditor.getTable().getConfigRegistry();
	}

	public CellRegion executeSearch(Object valueToMatch) {
		return CellDisplayValueSearchUtil.findCell(getContextLayer(), configRegistry,
				getColumnCellsToSearch(getContextLayer()), valueToMatch, getCellSearchStrategy());
	}

	public void setStartingRowPosition(int startingRowPosition) {
		this.startingRowPosition = startingRowPosition;
	}

	public void setColumnPositions(int[] columnPositions) {
		this.columnPositions = columnPositions;
		isHorizontalLayout = xliffEditor.isHorizontalLayout();
	}

	protected PositionCoordinate[] getColumnCellsToSearch(ILayer contextLayer) {
		List<PositionCoordinate> cellsToSearch = new ArrayList<PositionCoordinate>();
		int rowPosition = startingRowPosition;
		// See how many rows we can add, depends on where the search is starting from
		final int rowCount = contextLayer.getRowCount();
		int height = rowCount;
		boolean searchForward = getCellSearchStrategy().isSearchForward();
		if (searchForward) {
			height = height - startingRowPosition;
		} else {
			height = startingRowPosition;
		}
		for (int columnIndex = 0; columnIndex < columnPositions.length; columnIndex++) {
			final int startingColumnPosition = columnPositions[columnIndex];
			if (!searchForward) {
				cellsToSearch.addAll(CellDisplayValueSearchUtil.getDescendingCellCoordinates(getContextLayer(),
						startingColumnPosition, rowPosition, 1, height, isHorizontalLayout));
				rowPosition = rowCount - 1;
			} else {
				cellsToSearch.addAll(CellDisplayValueSearchUtil.getCellCoordinates(getContextLayer(),
						startingColumnPosition, rowPosition, 1, height, isHorizontalLayout));
				rowPosition = 0;
			}
			height = rowCount;
			// After first column is set, start the next column from the top
		}
		return cellsToSearch.toArray(new PositionCoordinate[0]);
	}

	public ILayer getContextLayer() {
		return contextLayer;
	}

	public void setContextLayer(ILayer contextLayer) {
		this.contextLayer = contextLayer;
	}

	public ICellSearchStrategy getCellSearchStrategy() {
		return cellSearchStrategy;
	}

	public void setCellSearchStrategy(ICellSearchStrategy cellSearchStrategy) {
		this.cellSearchStrategy = cellSearchStrategy;
	}
}