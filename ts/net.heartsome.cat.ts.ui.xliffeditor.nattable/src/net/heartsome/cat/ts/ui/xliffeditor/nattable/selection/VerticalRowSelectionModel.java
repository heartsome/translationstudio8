package net.heartsome.cat.ts.ui.xliffeditor.nattable.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.selection.ISelectionModel;
import net.sourceforge.nattable.selection.SelectionLayer;

import org.eclipse.swt.graphics.Rectangle;

/**
 * 垂直布局下使用的选中行模型
 * @author weachy
 * @version
 * @since JDK1.5
 * @param <R>
 */
public class VerticalRowSelectionModel<R> implements ISelectionModel {

	private final SelectionLayer selectionLayer;
	private final IRowIdAccessor rowIdAccessor;

	private Rectangle lastSelectedRange; // *live* reference to last range parameter used in addSelection(range)
	private Set<Serializable> selectedRows; // Key: rowId, Value: rowIndexes
	private Set<? extends Serializable> lastSelectedRowIds;
	private final ReadWriteLock selectionsLock;

	public ReadWriteLock getSelectionsLock() {
		return selectionsLock;
	}

	public VerticalRowSelectionModel(SelectionLayer selectionLayer, IRowIdAccessor rowIdAccessor) {
		this.selectionLayer = selectionLayer;
		this.rowIdAccessor = rowIdAccessor;

		selectedRows = new HashSet<Serializable>();
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
		return !isEmpty();
	}

	public int[] getSelectedColumns() {
		if (!isEmpty()) {
			selectionsLock.readLock().lock();

			try {
				int columnCount = selectionLayer.getColumnCount();
				int[] columns = new int[columnCount];
				for (int i = 0; i < columnCount; i++) {
					columns[i] = i;
				}
				return columns;
			} finally {
				selectionsLock.readLock().unlock();
			}
		}
		return new int[] {};
	}

	public void addSelection(int columnPosition, int rowPosition) {
		selectionsLock.writeLock().lock();

		try {
			Serializable rowId = getRowIdByPosition(rowPosition);
			if (rowId != null) {
				selectedRows.add(rowId);
			}
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	@SuppressWarnings("unchecked")
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

			int rowPosition = range.y / VerticalNatTableConfig.ROW_SPAN;
			int end = (int) Math.ceil((range.y + range.height) * 1.0 / VerticalNatTableConfig.ROW_SPAN);
			int length = end - range.y / VerticalNatTableConfig.ROW_SPAN;
			Set<Serializable> rowsToSelect = (Set<Serializable>) rowIdAccessor.getRowIdsByPositionRange(rowPosition,
					length);

			selectedRows.addAll(rowsToSelect);

			if (range == lastSelectedRange) {
				lastSelectedRowIds = rowsToSelect;
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

	/**
	 * 得到被整行选中的所有行
	 * @see net.sourceforge.nattable.selection.ISelectionModel#getFullySelectedRows(int)
	 */
	public int[] getFullySelectedRows(int rowWidth) {
		selectionsLock.readLock().lock();

		try {
			int[] selectedRowPositions = new int[selectedRows.size()];
			int i = 0;
			for (Serializable selectedRow : selectedRows) {
				int rowPosition = rowIdAccessor.getRowIds().indexOf(selectedRow);
				if (rowPosition > -1) {
					selectedRowPositions[i++] = rowPosition;
				}
			}
			return selectedRowPositions;
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	/**
	 * 得到选中行的个数
	 * @see net.sourceforge.nattable.selection.ISelectionModel#getSelectedRowCount()
	 */
	public int getSelectedRowCount() {
		selectionsLock.readLock().lock();

		try {
			return selectedRows.size() * VerticalNatTableConfig.ROW_SPAN;
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	/**
	 * 得到所有选中行
	 * @see net.sourceforge.nattable.selection.ISelectionModel#getSelectedRows()
	 */
	public Set<Range> getSelectedRows() {
		Set<Range> selectedRowRanges = new HashSet<Range>();

		selectionsLock.readLock().lock();

		try {
			for (Serializable selectedRow : selectedRows) {
				int rowPosition = rowIdAccessor.getRowIds().indexOf(selectedRow) * VerticalNatTableConfig.ROW_SPAN;
				if (rowPosition > -1) {
					for (int j = rowPosition; j < VerticalNatTableConfig.ROW_SPAN; j++) {
						selectedRowRanges.add(new Range(rowPosition, rowPosition + 1));
					}
				}
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
			for (Serializable selectedRow : selectedRows) {
				int rowPosition = rowIdAccessor.getRowIds().indexOf(selectedRow) * VerticalNatTableConfig.ROW_SPAN;
				if (rowPosition > -1) {
					for (int j = rowPosition; j < VerticalNatTableConfig.ROW_SPAN; j++) {
						selectionRectangles.add(new Rectangle(0, rowPosition, width, 1));
					}
				}
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
			return selectedRows.contains(rowId);
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

	/**
	 * 通过行的位置得到行对应的翻译单元的Id
	 * @param rowPosition
	 *            行的相对位置，从0开始
	 * @return 行对应的翻译单元的Id;
	 */
	private Serializable getRowIdByPosition(int rowPosition) {
		return rowIdAccessor.getRowIdByPosition(rowPosition / VerticalNatTableConfig.ROW_SPAN);
	}

}