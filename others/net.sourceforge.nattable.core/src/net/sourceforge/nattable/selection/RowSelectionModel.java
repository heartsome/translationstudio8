package net.sourceforge.nattable.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.data.IRowDataProvider;
import net.sourceforge.nattable.data.IRowIdAccessor;

import org.eclipse.swt.graphics.Rectangle;

public class RowSelectionModel<R> implements ISelectionModel {

	private final SelectionLayer selectionLayer;
	private final IRowDataProvider<R> rowDataProvider;
	private final IRowIdAccessor<R> rowIdAccessor;
	
	private Map<Serializable, R> selectedRows;
	private Rectangle lastSelectedRange;  // *live* reference to last range parameter used in addSelection(range)
	private Set<Serializable> lastSelectedRowIds;
	private final ReadWriteLock selectionsLock;

	public RowSelectionModel(SelectionLayer selectionLayer, IRowDataProvider<R> rowDataProvider, IRowIdAccessor<R> rowIdAccessor) {
		this.selectionLayer = selectionLayer;
		this.rowDataProvider = rowDataProvider;
		this.rowIdAccessor = rowIdAccessor;
		
		selectedRows = new HashMap<Serializable, R>();
		selectionsLock = new ReentrantReadWriteLock();
	}
	
	public void clearSelection() {
		selectionsLock.writeLock().lock();
		try {
			selectedRows.clear();
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}
	
	public boolean isColumnPositionSelected(int columnPosition) {
		selectionsLock.readLock().lock();

		try {
			return !selectedRows.isEmpty();
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	public int[] getSelectedColumns() {
		if (!isEmpty()) {
			selectionsLock.readLock().lock();
			
			int columnCount;
			
			try {
				columnCount = selectionLayer.getColumnCount();
			} finally {
				selectionsLock.readLock().unlock();
			}
			
			int[] columns = new int[columnCount];
			for (int i = 0; i < columnCount; i++) {
				columns[i] = i;
			}
			return columns;
		}
		return new int[] {};
	}

	public void addSelection(int columnPosition, int rowPosition) {
		selectionsLock.writeLock().lock();
		
		try {
			R rowObject = getRowObjectByPosition(rowPosition);
			if (rowObject != null) {
				Serializable rowId = rowIdAccessor.getRowId(rowObject);
				selectedRows.put(rowId, rowObject);
			}
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	public void addSelection(Rectangle range) {
		selectionsLock.writeLock().lock();
		
		try {
			if (range == lastSelectedRange) {
				// Unselect all previously selected rowIds
				if (lastSelectedRowIds != null) {
					for (Serializable rowId : lastSelectedRowIds) {
						selectedRows.remove(rowId);
					}
				}
			}
			
			Map<Serializable, R> rowsToSelect = new HashMap<Serializable, R>();
			
			for (int rowPosition = range.y; rowPosition < range.y + range.height; rowPosition++) {
				R rowObject = getRowObjectByPosition(rowPosition);
				if (rowObject != null) {
					Serializable rowId = rowIdAccessor.getRowId(rowObject);
					rowsToSelect.put(rowId, rowObject);
				}
			}
			
			selectedRows.putAll(rowsToSelect);
			
			if (range == lastSelectedRange) {
				lastSelectedRowIds = rowsToSelect.keySet();
			} else {
				lastSelectedRowIds = null;
			}
			
			lastSelectedRange = range;
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	public int[] getFullySelectedColumns(int fullySelectedColumnRowCount) {
		selectionsLock.readLock().lock();
		
		try {
			if (isColumnFullySelected(0, fullySelectedColumnRowCount)) {
				return getSelectedColumns();
			}
		} finally {
			selectionsLock.readLock().unlock();
		}
		
		return new int[] {};
	}

	public int[] getFullySelectedRows(int rowWidth) {
		selectionsLock.readLock().lock();
		
		try {
			int selectedRowCount = selectedRows.size();
			int[] selectedRowPositions = new int[selectedRowCount];
			int i = 0;
			for (Serializable rowId : selectedRows.keySet()) {
				selectedRowPositions[i] = getRowPositionById(rowId);
				i++;
			}
			return selectedRowPositions;
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	public int getSelectedRowCount() {
		selectionsLock.readLock().lock();
		
		try {
			return selectedRows.size();
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	public Set<Range> getSelectedRows() {
		Set<Range> selectedRowRanges = new HashSet<Range>();
		
		selectionsLock.readLock().lock();
		
		try {
			for (Serializable rowId : selectedRows.keySet()) {
				int rowPosition = getRowPositionById(rowId);
				selectedRowRanges.add(new Range(rowPosition, rowPosition + 1));
			}
		} finally {
			selectionsLock.readLock().unlock();
		}
		
		return selectedRowRanges;
	}

	public List<Rectangle> getSelections() {
		List<Rectangle> selectionRectangles = new ArrayList<Rectangle>();
		
		selectionsLock.readLock().lock();
		
		try {
			int width = selectionLayer.getColumnCount();
			for (Serializable rowId : selectedRows.keySet()) {
				int rowPosition = getRowPositionById(rowId);
				selectionRectangles.add(new Rectangle(0, rowPosition, width, 1));
			}
		} finally {
			selectionsLock.readLock().unlock();
		}
		
		return selectionRectangles;
	}

	public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
		return isRowPositionSelected(rowPosition);
	}

	public boolean isColumnFullySelected(int columnPosition, int fullySelectedColumnRowCount) {
		selectionsLock.readLock().lock();
		
		try {
			return selectedRows.size() == fullySelectedColumnRowCount;
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	public boolean isEmpty() {
		selectionsLock.readLock().lock();
		
		try {
			return selectedRows.isEmpty();
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	public boolean isRowFullySelected(int rowPosition, int rowWidth) {
		return isRowPositionSelected(rowPosition);
	}

	public boolean isRowPositionSelected(int rowPosition) {
		selectionsLock.readLock().lock();
		
		try {
			Serializable rowId = getRowIdByPosition(rowPosition);
			return selectedRows.containsKey(rowId);
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	public void removeSelection(Rectangle removedSelection) {
		selectionsLock.writeLock().lock();
		
		try {
			for (int rowPosition = removedSelection.y; rowPosition < removedSelection.y + removedSelection.height; rowPosition++) {
				removeSelection(0, rowPosition);
			}
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	public void removeSelection(int columnPosition, int rowPosition) {
		selectionsLock.writeLock().lock();
		
		try {
			Serializable rowId = getRowIdByPosition(rowPosition);
			selectedRows.remove(rowId);
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	private Serializable getRowIdByPosition(int rowPosition) {
		R rowObject = getRowObjectByPosition(rowPosition);
		if (rowObject != null) {
			Serializable rowId = rowIdAccessor.getRowId(rowObject);
			return rowId;
		}
		return null;
	}

	private R getRowObjectByPosition(int rowPosition) {
		selectionsLock.readLock().lock();
		
		try {
			int rowIndex = selectionLayer.getRowIndexByPosition(rowPosition);
			if (rowIndex >= 0) {
				try {
					R rowObject = rowDataProvider.getRowObject(rowIndex);
					return rowObject;
				} catch (Exception e) {
					// row index is invalid for the data provider
				}
			}
		} finally {
			selectionsLock.readLock().unlock();
		}
		
		return null;
	}
	
	private int getRowPositionById(Serializable rowId) {
		selectionsLock.readLock().lock();
		
		try {
			R rowObject = selectedRows.get(rowId);
			int rowIndex = rowDataProvider.indexOfRowObject(rowObject);
			int rowPosition = selectionLayer.getRowPositionByIndex(rowIndex);
			return rowPosition;
		} finally {
			selectionsLock.readLock().unlock();
		}
	}
	
}
